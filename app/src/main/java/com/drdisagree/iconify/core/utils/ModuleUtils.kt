package com.drdisagree.iconify.core.utils

import android.content.Context
import android.util.Log
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.R
import com.drdisagree.iconify.app.Iconify.Companion.appContext
import com.drdisagree.iconify.app.MainActivity
import com.drdisagree.iconify.core.utils.AssetsUtils.readRawResource
import com.drdisagree.iconify.core.utils.RootUtils.setPermissions
import com.drdisagree.iconify.core.utils.overlay.FabricatedUtils
import com.drdisagree.iconify.core.utils.overlay.OverlayUtils
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.common.Preferences.COLOR_ACCENT_PRIMARY
import com.drdisagree.iconify.data.common.Preferences.COLOR_ACCENT_SECONDARY
import com.drdisagree.iconify.data.common.Preferences.RESTART_SYSUI_AFTER_BOOT
import com.drdisagree.iconify.data.common.References.ICONIFY_COLOR_ACCENT_PRIMARY
import com.drdisagree.iconify.data.common.References.ICONIFY_COLOR_ACCENT_SECONDARY
import com.drdisagree.iconify.data.common.Resources
import com.drdisagree.iconify.data.common.Resources.MODULE_DIR
import com.drdisagree.iconify.data.common.Resources.TEMP_DIR
import com.drdisagree.iconify.data.common.Resources.TEMP_MODULE_DIR
import com.drdisagree.iconify.data.config.RPrefs
import com.drdisagree.iconify.data.keys.SettingsKey
import com.drdisagree.iconify.helpers.BackupRestore
import com.drdisagree.iconify.helpers.replaceAll
import com.topjohnwu.superuser.Shell
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.CompressionLevel
import net.lingala.zip4j.model.enums.CompressionMethod
import java.io.File

object ModuleUtils {

    private val TAG = ModuleUtils::class.java.simpleName

    fun handleModule(skippedInstallation: Boolean) {
        if (moduleExists()) {
            // Clean temporary directory
            Shell.cmd("rm -rf $TEMP_DIR").exec()

            // Backup necessary files
            BackupRestore.backupFiles()
        }

        installModule(skippedInstallation)
    }

    private fun installModule(skippedInstallation: Boolean) {
        Log.d(TAG, "Module does not exist, creating...")

        // Clean temporary directory
        FileUtils.ensureDirs(
            TEMP_DIR,
            TEMP_MODULE_DIR
        )

        val moduleProp = readRawResource(R.raw.module_module).replaceAll(
            "{{VERSION_NAME}}" to BuildConfig.VERSION_NAME,
            "{{VERSION_CODE}}" to BuildConfig.VERSION_CODE,
            "{{APP_MOTO}}" to appContext.resources.getString(R.string.app_motto)
        )
        val postFsDataSh = readRawResource(
            if (RootUtils.isSusfsBinaryAvailable) R.raw.module_post_fs_data_susfs
            else R.raw.module_post_fs_data
        )
        val actionSh = readRawResource(R.raw.module_action).replaceAll(
            "{{PACKAGE_NAME}}" to BuildConfig.APPLICATION_ID,
            "{{CLASS_PATH}}" to MainActivity::class.qualifiedName!!
        )
        val serviceSh = readRawResource(R.raw.module_service).replaceAll(
            "{{RESTART_SYSUI_AFTER_BOOT}}" to ("killall $SYSTEMUI_PACKAGE\n"
                .takeIf { RPrefs.getBoolean(RESTART_SYSUI_AFTER_BOOT, false) }
                ?: "")
        )
        val skippedServiceSh = readRawResource(R.raw.module_service_skipped)

        Shell.cmd(
            "printf '$moduleProp' > $TEMP_MODULE_DIR/module.prop",
            "printf '$postFsDataSh' > $TEMP_MODULE_DIR/post-fs-data.sh",
            if (!skippedInstallation) "printf '$serviceSh' > $TEMP_MODULE_DIR/service.sh"
            else "printf '$skippedServiceSh' > $TEMP_MODULE_DIR/service.sh",
            "printf '$actionSh' > $TEMP_MODULE_DIR/action.sh",
            "touch $TEMP_MODULE_DIR/system.prop",
            "touch $TEMP_MODULE_DIR/auto_mount",
        ).exec()

        FileUtils.ensureDirs("$TEMP_MODULE_DIR/system/product/overlay")

        createMETAINF()
        writePostExec(skippedInstallation)

        Log.i(TAG, "Module successfully created.")
    }

    private fun createMETAINF() {
        val updateBinary = readRawResource(R.raw.module_update_binary)

        FileUtils.ensureDirs("$TEMP_MODULE_DIR/META-INF/com/google/android")

        Shell.cmd(
            "printf '$updateBinary' > $TEMP_MODULE_DIR/META-INF/com/google/android/update-binary",
            "printf '#MAGISK' > $TEMP_MODULE_DIR/META-INF/com/google/android/updater-script"
        ).exec()
    }

    private fun writePostExec(skippedInstallation: Boolean) {
        val postExec = StringBuilder()
        var primaryColorEnabled = false
        var secondaryColorEnabled = false
        val firstInstall = RPrefs.getBoolean(SettingsKey.FIRST_INSTALL)
        val map = RPrefs.prefs.all

        for ((key, value) in map) {
            if (value is Boolean && value && key.startsWith("fabricated")) {
                try {
                    val name = key.replace("fabricated", "")
                    val commands = FabricatedUtils.buildCommands(
                        RPrefs.getString("FOCMDtarget$name")!!,
                        RPrefs.getString("FOCMDname$name")!!,
                        RPrefs.getString("FOCMDtype$name")!!,
                        RPrefs.getString("FOCMDresourceName$name")!!,
                        RPrefs.getString("FOCMDval$name")!!
                    )

                    postExec.append(commands[0]).append('\n').append(commands[1]).append('\n')

                    if (name.contains(COLOR_ACCENT_PRIMARY)) {
                        primaryColorEnabled = true
                    } else if (name.contains(COLOR_ACCENT_SECONDARY)) {
                        secondaryColorEnabled = true
                    }
                } catch (_: Exception) {
                }
            }
        }

        if (!firstInstall && shouldUseDefaultColors() && !skippedInstallation) {
            if (!primaryColorEnabled) {
                postExec.append("cmd overlay fabricate --target android --name IconifyComponentcolorAccentPrimary android:color/holo_blue_light 0x1c $ICONIFY_COLOR_ACCENT_PRIMARY\n")
                postExec.append("cmd overlay enable --user current com.android.shell:IconifyComponentcolorAccentPrimary\n")
                postExec.append("cmd overlay fabricate --target android --name IconifyComponentcolorAccentPrimaryLight android:color/holo_green_light 0x1c $ICONIFY_COLOR_ACCENT_PRIMARY\n")
                postExec.append("cmd overlay enable --user current com.android.shell:IconifyComponentcolorAccentPrimaryLight\n")
            }
            if (!secondaryColorEnabled) {
                postExec.append("cmd overlay fabricate --target android --name IconifyComponentcolorAccentSecondary android:color/holo_blue_dark 0x1c $ICONIFY_COLOR_ACCENT_SECONDARY\n")
                postExec.append("cmd overlay enable --user current com.android.shell:IconifyComponentcolorAccentSecondary\n")
                postExec.append("cmd overlay fabricate --target android --name IconifyComponentcolorAccentSecondaryLight android:color/holo_green_dark 0x1c $ICONIFY_COLOR_ACCENT_SECONDARY\n")
                postExec.append("cmd overlay enable --user current com.android.shell:IconifyComponentcolorAccentSecondaryLight\n")
            }
        }

        Shell.cmd("printf '$postExec' > $TEMP_MODULE_DIR/post-exec.sh").exec()
    }

    private fun shouldUseDefaultColors(): Boolean {
        return OverlayUtils.isOverlayDisabled("IconifyComponentAMAC.overlay") &&
                OverlayUtils.isOverlayDisabled("IconifyComponentAMGC.overlay")
    }

    fun moduleExists(): Boolean {
        return RootUtils.folderExists(Resources.OVERLAY_DIR)
    }

    @Throws(Exception::class)
    fun createModule(sourceFolder: String, destinationFilePath: String): String {
        val input = File(sourceFolder)
        val output = File(destinationFilePath)
        val parameters = ZipParameters().apply {
            isIncludeRootFolder = false
            isOverrideExistingFilesInZip = true
            compressionMethod = CompressionMethod.DEFLATE
            compressionLevel = CompressionLevel.NORMAL
        }

        ZipFile(output).use { zipFile ->
            zipFile.addFolder(input, parameters)
            return zipFile.file.absolutePath
        }
    }

    @Throws(Exception::class)
    fun flashModule(modulePath: String): Boolean {
        var result: Shell.Result? = null

        if (RootUtils.isMagiskInstalled) {
            result = Shell.cmd("magisk --install-module $modulePath").exec()
        } else if (RootUtils.isKSUInstalled) {
            result = Shell.cmd("/data/adb/ksud module install $modulePath").exec()
        } else if (RootUtils.isApatchInstalled) {
            result = Shell.cmd("apd module install $modulePath").exec()
            setPermissions(755, MODULE_DIR)
        }

        if (result == null) {
            throw Exception("No supported root found")
        } else if (result.isSuccess) {
            Log.i(TAG, "Successfully flashed module")
        } else {
            Log.e(TAG, "Failed to flash module")
            throw Exception(java.lang.String.join("\n", result.out))
        }

        return !result.isSuccess
    }
}
