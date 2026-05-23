package com.drdisagree.iconify.features.onboarding.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.preferences.PreferenceController
import com.drdisagree.iconify.core.utils.AssetsUtils.copyAssets
import com.drdisagree.iconify.core.utils.FileUtils
import com.drdisagree.iconify.core.utils.Logger.writeLog
import com.drdisagree.iconify.core.utils.ModuleUtils.createModule
import com.drdisagree.iconify.core.utils.ModuleUtils.flashModule
import com.drdisagree.iconify.core.utils.ModuleUtils.handleModule
import com.drdisagree.iconify.core.utils.ModuleUtils.moduleExists
import com.drdisagree.iconify.core.utils.RootUtils.deviceProperlyRooted
import com.drdisagree.iconify.core.utils.RootUtils.isDeviceRooted
import com.drdisagree.iconify.core.utils.RootUtils.isModuleUpdatePending
import com.drdisagree.iconify.core.utils.RootUtils.requireMetamodule
import com.drdisagree.iconify.core.utils.overlay.OverlayUtils.overlayExists
import com.drdisagree.iconify.core.utils.overlay.compilers.OnboardingCompiler.apkSigner
import com.drdisagree.iconify.core.utils.overlay.compilers.OnboardingCompiler.createManifest
import com.drdisagree.iconify.core.utils.overlay.compilers.OnboardingCompiler.runAapt
import com.drdisagree.iconify.core.utils.overlay.compilers.OnboardingCompiler.zipAlign
import com.drdisagree.iconify.data.common.Dynamic.DATA_DIR
import com.drdisagree.iconify.data.common.Resources.BACKUP_DIR
import com.drdisagree.iconify.data.common.Resources.MODULE_DIR
import com.drdisagree.iconify.data.common.Resources.SIGNED_DIR
import com.drdisagree.iconify.data.common.Resources.TEMP_DIR
import com.drdisagree.iconify.data.common.Resources.TEMP_MODULE_DIR
import com.drdisagree.iconify.data.common.Resources.TEMP_MODULE_OVERLAY_DIR
import com.drdisagree.iconify.data.common.Resources.TEMP_OVERLAY_DIR
import com.drdisagree.iconify.data.common.Resources.UNSIGNED_DIR
import com.drdisagree.iconify.data.common.Resources.UNSIGNED_UNALIGNED_DIR
import com.drdisagree.iconify.data.config.Config
import com.drdisagree.iconify.data.keys.SettingsKey
import com.drdisagree.iconify.features.onboarding.states.InstallationEvent
import com.drdisagree.iconify.features.onboarding.states.InstallationState
import com.drdisagree.iconify.helpers.BackupRestore.restoreFiles
import com.drdisagree.iconify.helpers.BinaryInstaller.symLinkBinaries
import com.topjohnwu.superuser.Shell
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val prefController: PreferenceController
) : ViewModel() {

    private val tag = OnboardingViewModel::class.java.simpleName

    private val _state = MutableStateFlow<InstallationState>(InstallationState.Idle)
    val state: StateFlow<InstallationState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<InstallationEvent>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val events: SharedFlow<InstallationEvent> = _events.asSharedFlow()

    private val _logs = mutableStateListOf<String>()
    val logs: List<String> get() = _logs

    var rebootRequired by mutableStateOf(false)
        private set

    private var hasErroredOut by mutableStateOf(false)

    private var installationJob: Job? = null

    val stepCount = 6

    init {
        viewModelScope.launch {
            rebootRequired = isModuleUpdatePending()
        }
    }

    fun startInstallation(skipInstallation: Boolean) {
        if (installationJob?.isActive == true) return

        hasErroredOut = false
        _logs.clear()

        installationJob = viewModelScope.launch(Dispatchers.IO) {
            awaitShell()

            if (!isDeviceRooted) {
                _events.emit(
                    InstallationEvent.Error(
                        R.string.root_not_found_title,
                        R.string.root_not_found_desc
                    )
                )
                return@launch
            }

            if (!deviceProperlyRooted()) {
                _events.emit(
                    InstallationEvent.Error(
                        R.string.compatible_root_not_found_title,
                        R.string.compatible_root_not_found_desc
                    )
                )
                return@launch
            }

            if (!skipInstallation && requireMetamodule()) {
                _events.emit(
                    InstallationEvent.Error(
                        R.string.metamodule_not_found_title,
                        R.string.metamodule_not_found_desc
                    )
                )
                return@launch
            }

            //            if (!hasStoragePermission()) {
            //                _events.emit(
            //                    InstallationEvent.Toast(R.string.need_storage_perm_title)
            //                )
            //                return@launch
            //            }

            val moduleExists = moduleExists()
            val overlayExists = overlayExists()

            if ((!skipInstallation && (Config.FORCE_OVERLAY_INSTALLATION ||
                        prefController.getInt(SettingsKey.OVERLAY_VERSION_CODE) != BuildConfig.OVERLAY_VERSION_CODE ||
                        !moduleExists ||
                        !overlayExists)) ||
                (skipInstallation && !moduleExists)
            ) {
                clearDatabase(skipInstallation, moduleExists, overlayExists)
                performInstallation(skipInstallation)
            } else {
                prefController.setBoolean(SettingsKey.FIRST_INSTALL, false)

                if (skipInstallation) {
                    _events.emit(
                        InstallationEvent.Toast(R.string.toast_skipped_installation)
                    )
                }

                _state.emit(InstallationState.Success)
            }
        }
    }

    private suspend fun awaitShell(): Boolean = suspendCancellableCoroutine { cont ->
        Shell.getShell {
            cont.resume(true) { _, _, _ -> }
        }
    }

    private suspend fun performInstallation(skip: Boolean) {
        _state.emit(InstallationState.Installing)

        var step = 0
        var dir: File

        suspend fun progress(
            nextStep: Boolean = false,
            log: String? = null,
            replace: Boolean = false
        ) {
            if (nextStep) ++step
            if (log != null) {
                if (_logs.isEmpty() || !replace) {
                    _logs.add(log)
                } else {
                    _logs[_logs.lastIndex] = log
                }
            }

            val descRes = when (step) {
                1 -> R.string.module_installation_step1
                2 -> R.string.module_installation_step2
                3 -> R.string.module_installation_step3
                4 -> R.string.module_installation_step4
                5 -> R.string.module_installation_step5
                6 -> R.string.module_installation_step6
                else -> R.string.loading_dialog_wait
            }

            _state.emit(InstallationState.Progressing(step = step, descRes = descRes, replace))
        }

        progress(log = "I: Creating blank module template", nextStep = true)
        handleModule(skip)

        progress(log = "I: Extracting binaries")
        hasErroredOut = symLinkBinaries()

        if (hasErroredOut) {
            progress(log = "E: Failed to extract binaries")
            Log.e(tag, "Failed to extract binaries")
        }

        try {
            if (!hasErroredOut) {
                // Clean data directory
                progress(log = "I: Cleaning iconify data directory", nextStep = true)
                Shell.cmd("rm -rf $DATA_DIR/Overlays").exec()

                progress(log = "I: Extracting overlays from assets")
                if (skip) {
                    delay(100)
                    progress(log = "W: Skipped...")
                    delay(100)
                } else {
                    // Extract overlays from assets
                    copyAssets("Overlays")
                }

                // Create temp directory
                progress(log = "I: Creating temporary directories")
                FileUtils.ensureDirs(
                    TEMP_OVERLAY_DIR,
                    UNSIGNED_UNALIGNED_DIR,
                    UNSIGNED_DIR,
                    SIGNED_DIR
                )
            }
        } catch (e: IOException) {
            hasErroredOut = true
            Log.e(tag, e.toString())
        }

        progress(nextStep = true)

        if (skip) {
            progress(log = "W: Skipping overlay builder...")
            delay(100)
        } else {
            // Create AndroidManifest.xml and build Overlay using AAPT
            dir = File("$DATA_DIR/Overlays")

            if (dir.listFiles() == null) hasErroredOut = true

            if (!hasErroredOut) {
                for (pkg in dir.listFiles()!!) {
                    if (pkg.isDirectory) {
                        for (overlay in pkg.listFiles()!!) {
                            if (overlay.isDirectory) {
                                val overlayName = overlay.toString()
                                    .replace("$pkg/", "")

                                progress(log = "I: Building Overlay for $overlayName")

                                if (createManifest(
                                        overlayName,
                                        pkg.toString()
                                            .replace("$DATA_DIR/Overlays/", ""),
                                        overlay.absolutePath
                                    )
                                ) {
                                    hasErroredOut = true
                                }

                                if (!hasErroredOut && runAapt(
                                        overlay.absolutePath,
                                        overlayName
                                    )
                                ) {
                                    hasErroredOut = true
                                }
                            }

                            if (hasErroredOut) break
                        }
                    }

                    if (hasErroredOut) break
                }
            }
        }

        progress(nextStep = true)

        if (skip) {
            progress(log = "W: Skipping zipalign process...")
            delay(100)
        } else {
            // ZipAlign the Overlay
            dir = File(UNSIGNED_UNALIGNED_DIR)

            if (dir.listFiles() == null) hasErroredOut = true

            if (!hasErroredOut) {
                for (overlay in dir.listFiles()!!) {
                    if (!overlay.isDirectory) {
                        val overlayName = overlay.toString()
                            .replace("$UNSIGNED_UNALIGNED_DIR/", "")
                            .replace("-unaligned", "")

                        progress(
                            log = "I: Zip aligning Overlay " + overlayName.replace(
                                "-unsigned.apk",
                                ""
                            )
                        )

                        if (zipAlign(overlay.absolutePath, overlayName)) {
                            hasErroredOut = true
                        }
                    }

                    if (hasErroredOut) break
                }
            }
        }

        progress(nextStep = true)

        if (skip) {
            progress(log = "W: Skipping signing process...")
            delay(100)
        } else {
            // Sign the Overlay
            dir = File(UNSIGNED_DIR)

            if (dir.listFiles() == null) hasErroredOut = true

            if (!hasErroredOut) {
                for (overlay in dir.listFiles()!!) {
                    if (!overlay.isDirectory) {
                        val overlayName =
                            overlay.toString()
                                .replace("$UNSIGNED_DIR/", "")
                                .replace("-unsigned", "")

                        progress(
                            log = "I: Signing Overlay " + overlayName.replace(
                                ".apk",
                                ""
                            )
                        )

                        var attempt = 3
                        while (attempt-- != 0) {
                            hasErroredOut = apkSigner(overlay.absolutePath, overlayName)

                            if (!hasErroredOut) {
                                break
                            } else {
                                delay(1000)
                            }
                        }
                    }

                    if (hasErroredOut) break
                }
            }
        }

        if (!hasErroredOut) {
            if (!skip) {
                progress(log = "I: Moving overlays to system directory", nextStep = true)
            } else {
                progress(log = "W: Skip moving overlays to system directory", nextStep = true)
            }
        }

        // Move all generated overlays to system dir and flash as module
        if (!hasErroredOut) {
            Shell.cmd("cp -a $SIGNED_DIR/. $TEMP_MODULE_OVERLAY_DIR").exec()

            restoreFiles()

            hasErroredOut = try {
                flashModule(
                    createModule(
                        TEMP_MODULE_DIR,
                        "$TEMP_DIR/Iconify.zip"
                    )
                )
            } catch (e: Exception) {
                writeLog(tag, "Failed to create/flash module zip", e)
                Log.e(tag, "Failed to create/flash module zip\n$e")

                if (e.toString().contains("Function not implemented", ignoreCase = true)) {
                    progress(log = "E: KernelSU function not implemented")
                    _events.emit(
                        InstallationEvent.Toast(R.string.toast_downgrade_kernelsu)
                    )
                } else if (e.toString().contains("Reboot to apply changes", ignoreCase = true)) {
                    progress(log = "E: Metamodule is pending a reboot")
                    _events.emit(
                        InstallationEvent.Toast(R.string.toast_reboot_and_try_again)
                    )
                }

                true
            }
        }

        progress(log = "I: Cleaning temporary directories")

        // Clean temp directory
        Shell.cmd(
            "rm -rf $TEMP_DIR",
            "rm -rf $DATA_DIR/Overlays"
        ).exec()

        if (!hasErroredOut) {
            progress(log = "I: Installation process finished")
            delay(100)
            progress(log = "I: You should reboot your device")
        } else {
            progress(log = "E: Installation process failed")
        }

        delay(500)
        progress(log = "")

        for (i in 3 downTo 1) {
            progress(log = "Closing in $i seconds...", replace = i != 3)
            delay(1000)
        }
        progress(log = "Done!")
        delay(500)

        if (!hasErroredOut) {
            if (prefController.getBoolean(SettingsKey.FIRST_INSTALL)) {
                prefController.setBoolean(SettingsKey.FIRST_INSTALL, false)
                prefController.setBoolean(SettingsKey.UPDATE_DETECTED, false)
            } else {
                prefController.setBoolean(SettingsKey.UPDATE_DETECTED, true)
            }

            if (!skip) {
                if (BuildConfig.OVERLAY_VERSION_CODE != prefController.getInt(SettingsKey.OVERLAY_VERSION_CODE)) {
                    prefController.setInt(
                        SettingsKey.OVERLAY_VERSION_CODE,
                        BuildConfig.OVERLAY_VERSION_CODE
                    )
                }

                if (moduleExists() && overlayExists()) {
                    _events.emit(
                        InstallationEvent.Toast(R.string.one_time_reboot_needed)
                    )
                    _state.emit(InstallationState.Success)
                } else {
                    _events.emit(
                        InstallationEvent.Toast(R.string.need_reboot_title)
                    )
                    rebootRequired = true
                    _state.emit(InstallationState.Reboot)
                }
            } else {
                _events.emit(
                    InstallationEvent.Toast(R.string.one_time_reboot_needed)
                )
                _state.emit(InstallationState.Success)
            }
        } else {
            onCancelled()

            _events.emit(
                InstallationEvent.Error(
                    R.string.installation_failed_title,
                    R.string.installation_failed_desc
                )
            )
        }
    }

    private fun clearDatabase(
        skip: Boolean,
        moduleExists: Boolean,
        overlayExists: Boolean
    ) {
        if (!skip && (!moduleExists || !overlayExists)) {
            prefController.reset()
            CoroutineScope(Dispatchers.IO).launch {
                //                DynamicResourceRepository(
                //                    DynamicResourceDatabase.getInstance().dynamicResourceDao()
                //                ).apply {
                //                    deleteResources(getAllResources())
                //                }
            }
        } else if (skip && !moduleExists) {
            prefController.reset()
            CoroutineScope(Dispatchers.IO).launch {
                //                DynamicResourceRepository(
                //                    DynamicResourceDatabase.getInstance().dynamicResourceDao()
                //                ).apply {
                //                    deleteResources(getAllResources())
                //                }
            }
        }
    }

    fun clearError() {
        _state.value = InstallationState.Idle
    }

    private fun onCancelled() {
        Shell.cmd(
            "rm -rf $DATA_DIR",
            "rm -rf $TEMP_DIR",
            "rm -rf $BACKUP_DIR",
            "rm -rf $MODULE_DIR"
        ).exec()
    }

    override fun onCleared() {
        installationJob?.cancel()
        super.onCleared()
    }
}