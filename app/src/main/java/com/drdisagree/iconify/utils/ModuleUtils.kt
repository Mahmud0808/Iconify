package com.drdisagree.iconify.utils

import android.content.Context
import android.util.Log
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Const
import com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.common.Dynamic.skippedInstallation
import com.drdisagree.iconify.common.Preferences.COLOR_ACCENT_PRIMARY
import com.drdisagree.iconify.common.Preferences.COLOR_ACCENT_SECONDARY
import com.drdisagree.iconify.common.Preferences.RESTART_SYSUI_AFTER_BOOT
import com.drdisagree.iconify.common.References.ICONIFY_COLOR_ACCENT_PRIMARY
import com.drdisagree.iconify.common.References.ICONIFY_COLOR_ACCENT_SECONDARY
import com.drdisagree.iconify.common.Resources
import com.drdisagree.iconify.config.RPrefs
import com.drdisagree.iconify.utils.helper.BackupRestore.backupFiles
import com.drdisagree.iconify.utils.helper.BinaryInstaller.symLinkBinaries
import com.drdisagree.iconify.utils.overlay.FabricatedUtils
import com.drdisagree.iconify.utils.overlay.OverlayUtils
import com.topjohnwu.superuser.Shell
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.CompressionLevel
import net.lingala.zip4j.model.enums.CompressionMethod
import java.io.File

object ModuleUtils {

    private val TAG = ModuleUtils::class.java.getSimpleName()

    fun handleModule() {
        if (moduleExists()) {
            // Clean temporary directory
            Shell.cmd("rm -rf " + Resources.TEMP_DIR).exec()

            // Backup necessary files
            backupFiles()
        }

        installModule()
    }

    private fun installModule() {
        Log.d(TAG, "Magisk module does not exist, creating...")

        // Clean temporary directory
        Shell.cmd("mkdir -p " + Resources.TEMP_DIR).exec()
        Shell.cmd("mkdir -p " + Resources.TEMP_MODULE_DIR).exec()

        Shell.cmd(
            "printf 'id=Iconify\n" +
                    "name=Iconify\n" +
                    "version=${BuildConfig.VERSION_NAME}\n" +
                    "versionCode=${BuildConfig.VERSION_CODE}\n" +
                    "author=@DrDisagree\n" +
                    "description=Systemless module for Iconify. ${appContext.resources.getString(R.string.app_moto)}.\n" +
                    "' > ${Resources.TEMP_MODULE_DIR}/module.prop\n".trimIndent()
        ).exec()

        Shell.cmd(
            "printf 'MODDIR=${"$"}{0%%/*}\n\n" +
                    "' > ${Resources.TEMP_MODULE_DIR}/post-fs-data.sh\n".trimIndent()
        ).exec()

        if (!skippedInstallation) {
            Shell.cmd(
                "printf 'MODDIR=${"$"}{0%%/*}\n\n" +
                        "while [ \"$(getprop sys.boot_completed | tr -d \"\\r\")\" != \"1\" ]\n" +
                        "do\n" +
                        " sleep 1\n" +
                        "done\n" +
                        "sleep 5\n\n" +
                        "sh ${"$"}MODDIR/post-exec.sh\n\n" +
                        "until [ -d /storage/emulated/0/Android ]; do\n" +
                        "  sleep 1\n" +
                        "done\n" +
                        "sleep 3\n\n" +
                        "${
                            if (RPrefs.getBoolean(
                                    RESTART_SYSUI_AFTER_BOOT,
                                    false
                                )
                            ) "killall $SYSTEMUI_PACKAGE\n" else ""
                        }sleep 6\n\n" +
                        "handle_overlay() {\n" +
                        "  local overlay_name=\"\$1\"\n\n" +
                        "  local overlay=\$(cmd overlay list | grep -E \"^.x..${"$"}{overlay_name}.overlay\" | sed -E \"s/^.x..//\")\n" +
                        "  local disableMonet=\$(cmd overlay list | grep -E \"^.x..IconifyComponentDM.overlay\" | sed -E \"s/^.x..//\")\n\n" +
                        "  if ([ ! -z \"${"$"}{overlay}\" ] && [ -z \"${"$"}{disableMonet}\" ])\n" +
                        "  then\n" +
                        "    cmd overlay disable --user current \"${"$"}{overlay_name}.overlay\"\n" +
                        "    cmd overlay enable --user current \"${"$"}{overlay_name}.overlay\"\n" +
                        "    cmd overlay set-priority \"${"$"}{overlay_name}.overlay\" highest\n" +
                        "  fi\n" +
                        "}\n\n" +
                        "handle_overlay \"IconifyComponentQSPBD\"\n" +
                        "handle_overlay \"IconifyComponentQSPBA\"\n\n" +
                        "' > ${Resources.TEMP_MODULE_DIR}/service.sh".trimIndent()
            ).exec()
        } else {
            Shell.cmd(
                "printf 'MODDIR=${"$"}{0%%/*}\n\n" +
                        "while [ \"$(getprop sys.boot_completed | tr -d \"\\r\")\" != \"1\" ]\n" +
                        "do\n" +
                        " sleep 1\n" +
                        "done\n" +
                        "sleep 5\n\n" +
                        "sh ${"$"}MODDIR/post-exec.sh\n" +
                        "' > ${Resources.TEMP_MODULE_DIR}/service.sh".trimIndent()
            ).exec()
        }

        Shell.cmd("touch " + Resources.TEMP_MODULE_DIR + "/system.prop").exec()
        Shell.cmd("touch " + Resources.TEMP_MODULE_DIR + "/auto_mount").exec()
        Shell.cmd("mkdir -p " + Resources.TEMP_MODULE_DIR + "/system").exec()
        Shell.cmd("mkdir -p " + Resources.TEMP_MODULE_DIR + "/system/product").exec()
        Shell.cmd("mkdir -p " + Resources.TEMP_MODULE_DIR + "/system/product/overlay").exec()

        createMETAINF()
        writePostExec()
        symLinkBinaries()

        Log.i(TAG, "Magisk module successfully created.")
    }

    private fun createMETAINF() {
        Shell.cmd("mkdir -p " + Resources.TEMP_MODULE_DIR + "/META-INF").exec()
        Shell.cmd("mkdir -p " + Resources.TEMP_MODULE_DIR + "/META-INF/com").exec()
        Shell.cmd("mkdir -p " + Resources.TEMP_MODULE_DIR + "/META-INF/com/google").exec()
        Shell.cmd("mkdir -p " + Resources.TEMP_MODULE_DIR + "/META-INF/com/google/android").exec()
        Shell.cmd(
            "printf '" + Const.MAGISK_UPDATE_BINARY + "' > " + Resources.TEMP_MODULE_DIR + "/META-INF/com/google/android/update-binary"
        ).exec()
        Shell.cmd(
            "printf '#MAGISK' > " + Resources.TEMP_MODULE_DIR + "/META-INF/com/google/android/updater-script"
        ).exec()
    }

    private fun writePostExec() {
        val postExec = StringBuilder()
        var primaryColorEnabled = false
        var secondaryColorEnabled = false
        val prefs = appContext.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)
        val map = prefs.all

        for ((key, value) in map) {
            if (value is Boolean && value && key.startsWith("fabricated")) {
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
            }
        }

        if (!primaryColorEnabled && shouldUseDefaultColors() && !skippedInstallation) {
            postExec.append("cmd overlay fabricate --target android --name IconifyComponentcolorAccentPrimary android:color/holo_blue_light 0x1c $ICONIFY_COLOR_ACCENT_PRIMARY\n")
            postExec.append("cmd overlay enable --user current com.android.shell:IconifyComponentcolorAccentPrimary\n")
            postExec.append("cmd overlay fabricate --target android --name IconifyComponentcolorAccentPrimaryLight android:color/holo_green_light 0x1c $ICONIFY_COLOR_ACCENT_PRIMARY\n")
            postExec.append("cmd overlay enable --user current com.android.shell:IconifyComponentcolorAccentPrimaryLight\n")
        }

        if (!secondaryColorEnabled && shouldUseDefaultColors() && !skippedInstallation) {
            postExec.append("cmd overlay fabricate --target android --name IconifyComponentcolorAccentSecondary android:color/holo_blue_dark 0x1c $ICONIFY_COLOR_ACCENT_SECONDARY\n")
            postExec.append("cmd overlay enable --user current com.android.shell:IconifyComponentcolorAccentSecondary\n")
            postExec.append("cmd overlay fabricate --target android --name IconifyComponentcolorAccentSecondaryLight android:color/holo_green_dark 0x1c $ICONIFY_COLOR_ACCENT_SECONDARY\n")
            postExec.append("cmd overlay enable --user current com.android.shell:IconifyComponentcolorAccentSecondaryLight\n")
        }

        Shell.cmd("printf '" + postExec + "' > " + Resources.TEMP_MODULE_DIR + "/post-exec.sh")
            .exec()
    }

    private fun shouldUseDefaultColors(): Boolean {
        return OverlayUtils.isOverlayDisabled("IconifyComponentAMAC.overlay") && OverlayUtils.isOverlayDisabled(
            "IconifyComponentAMGC.overlay"
        ) && OverlayUtils.isOverlayDisabled("IconifyComponentME.overlay")
    }

    fun moduleExists(): Boolean {
        return RootUtils.folderExists(Resources.OVERLAY_DIR)
    }

    @Throws(Exception::class)
    fun createModule(sourceFolder: String, destinationFilePath: String): String {
        val input = File(sourceFolder)
        val output = File(destinationFilePath)
        val parameters = ZipParameters()
        parameters.isIncludeRootFolder = false
        parameters.isOverrideExistingFilesInZip = true
        parameters.compressionMethod = CompressionMethod.DEFLATE
        parameters.compressionLevel = CompressionLevel.NORMAL
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
