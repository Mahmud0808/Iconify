package com.drdisagree.iconify.utils

import android.content.Context
import android.util.Log
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.Iconify
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
import com.drdisagree.iconify.config.Prefs
import com.drdisagree.iconify.config.Prefs.getString
import com.drdisagree.iconify.utils.helper.BackupRestore.backupFiles
import com.drdisagree.iconify.utils.helper.BinaryInstaller.symLinkBinaries
import com.drdisagree.iconify.utils.overlay.FabricatedUtil
import com.drdisagree.iconify.utils.overlay.OverlayUtil
import com.topjohnwu.superuser.Shell
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.CompressionLevel
import net.lingala.zip4j.model.enums.CompressionMethod
import java.io.File

object ModuleUtil {

    private val TAG = ModuleUtil::class.java.getSimpleName()

    @JvmStatic
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
            """
    printf 'id=Iconify
    name=Iconify
    version=${BuildConfig.VERSION_NAME}
    versionCode=${BuildConfig.VERSION_CODE}
    author=@DrDisagree
    description=Systemless module for Iconify. ${Iconify.getAppContext().resources.getString(R.string.app_moto)}.
    ' > ${Resources.TEMP_MODULE_DIR}/module.prop
    """.trimIndent()
        ).exec()
        Shell.cmd(
            """
    printf 'MODDIR=${"$"}{0%%/*}
    
    ' > ${Resources.TEMP_MODULE_DIR}/post-fs-data.sh
    """.trimIndent()
        ).exec()
        if (!skippedInstallation) {
            Shell.cmd(
                """printf 'MODDIR=${"$"}{0%%/*}

while [ "$(getprop sys.boot_completed | tr -d "\r")" != "1" ]
do
 sleep 1
done
sleep 5

sh ${"$"}MODDIR/post-exec.sh

until [ -d /storage/emulated/0/Android ]; do
  sleep 1
done
sleep 3

${
                    if (Prefs.getBoolean(
                            RESTART_SYSUI_AFTER_BOOT,
                            false
                        )
                    ) "killall $SYSTEMUI_PACKAGE\n" else ""
                }sleep 6

qspbd=$(cmd overlay list |  grep -E "^.x..IconifyComponentQSPBD.overlay" | sed -E "s/^.x..//")
dm=$(cmd overlay list |  grep -E "^.x..IconifyComponentDM.overlay" | sed -E "s/^.x..//")
if ([ ! -z "${"$"}qspbd" ] && [ -z "${"$"}dm" ])
then
 cmd overlay disable --user current IconifyComponentQSPBD.overlay
 cmd overlay enable --user current IconifyComponentQSPBD.overlay
 cmd overlay set-priority IconifyComponentQSPBD.overlay highest
fi

qspba=$(cmd overlay list |  grep -E "^.x..IconifyComponentQSPBA.overlay" | sed -E "s/^.x..//")
dm=$(cmd overlay list |  grep -E "^.x..IconifyComponentDM.overlay" | sed -E "s/^.x..//")
if ([ ! -z "${"$"}qspba" ] && [ -z "${"$"}dm" ])
then
 cmd overlay disable --user current IconifyComponentQSPBA.overlay
 cmd overlay enable --user current IconifyComponentQSPBA.overlay
 cmd overlay set-priority IconifyComponentQSPBA.overlay highest
fi

' > ${Resources.TEMP_MODULE_DIR}/service.sh"""
            ).exec()
        } else {
            Shell.cmd(
                """printf 'MODDIR=${"$"}{0%%/*}

while [ "$(getprop sys.boot_completed | tr -d "\r")" != "1" ]
do
 sleep 1
done
sleep 5

sh ${"$"}MODDIR/post-exec.sh

' > ${Resources.TEMP_MODULE_DIR}/service.sh"""
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
        Shell.cmd("printf '" + Const.MAGISK_UPDATE_BINARY + "' > " + Resources.TEMP_MODULE_DIR + "/META-INF/com/google/android/update-binary")
            .exec()
        Shell.cmd("printf '#MAGISK' > " + Resources.TEMP_MODULE_DIR + "/META-INF/com/google/android/updater-script")
            .exec()
    }

    private fun writePostExec() {
        val postExec = StringBuilder()
        var primaryColorEnabled = false
        var secondaryColorEnabled = false
        val prefs = Iconify.getAppContext()
            .getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)
        val map = prefs.all
        for ((key, value) in map) {
            if (value is Boolean && value && key.startsWith("fabricated")) {
                val name = key.replace("fabricated", "")
                val commands = FabricatedUtil.buildCommands(
                    getString("FOCMDtarget$name"), getString(
                        "FOCMDname$name"
                    ), getString("FOCMDtype$name"), getString("FOCMDresourceName$name"), getString(
                        "FOCMDval$name"
                    )
                )
                postExec.append(commands[0]).append('\n').append(commands[1]).append('\n')
                if (name.contains(COLOR_ACCENT_PRIMARY)) primaryColorEnabled =
                    true else if (name.contains(COLOR_ACCENT_SECONDARY)) secondaryColorEnabled =
                    true
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
        return OverlayUtil.isOverlayDisabled("IconifyComponentAMAC.overlay") && OverlayUtil.isOverlayDisabled(
            "IconifyComponentAMGC.overlay"
        ) && OverlayUtil.isOverlayDisabled("IconifyComponentME.overlay")
    }

    @JvmStatic
    fun moduleExists(): Boolean {
        return RootUtil.folderExists(Resources.OVERLAY_DIR)
    }

    @JvmStatic
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

    @JvmStatic
    @Throws(Exception::class)
    fun flashModule(modulePath: String): Boolean {
        var result: Shell.Result? = null
        if (RootUtil.isMagiskInstalled) {
            result = Shell.cmd("magisk --install-module $modulePath").exec()
        } else if (RootUtil.isKSUInstalled) {
            result = Shell.cmd("/data/adb/ksud module install $modulePath").exec()
        } else if (RootUtil.isApatchInstalled) {
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
