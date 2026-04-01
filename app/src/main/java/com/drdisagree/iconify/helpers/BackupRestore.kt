package com.drdisagree.iconify.helpers

import com.drdisagree.iconify.core.utils.FileUtils
import com.drdisagree.iconify.core.utils.RootUtils
import com.drdisagree.iconify.data.common.Resources.BACKUP_DIR
import com.drdisagree.iconify.data.common.Resources.MODULE_DIR
import com.drdisagree.iconify.data.common.Resources.OVERLAY_DIR
import com.drdisagree.iconify.data.common.Resources.TEMP_MODULE_DIR
import com.drdisagree.iconify.data.common.Resources.TEMP_MODULE_OVERLAY_DIR
import com.topjohnwu.superuser.Shell

object BackupRestore {

    private val ICONIFY_COMPONENTS = listOf(
        "CR1", "CR2", // Corner radius
        "Dynamic1", "Dynamic2", "Dynamic3", "Dynamic4", "Dynamic5", // Dynamically generated
        "NFN", // Notification
        "SGIC", // Signal icon
        "SIP1", "SIP2", "SIP3", // Settings icon pack
        "SIS", // System icon shape
        "TSTFRM", // Toast frame
        "WIFI" // WiFi icon
    )

    fun backupFiles() {
        Shell.cmd("rm -rf $BACKUP_DIR").exec()
        FileUtils.ensureDirs(BACKUP_DIR)

        val files = mutableListOf(
            "$MODULE_DIR/system.prop",
            "$MODULE_DIR/post-exec.sh"
        )

        files += ICONIFY_COMPONENTS.map { "$OVERLAY_DIR/IconifyComponent$it.apk" }

        backupFiles(*files.toTypedArray())
    }

    fun restoreFiles() {
        val files = mutableListOf(
            "system.prop" to TEMP_MODULE_DIR,
            "post-exec.sh" to TEMP_MODULE_DIR
        )

        files += ICONIFY_COMPONENTS.map { "IconifyComponent$it.apk" to TEMP_MODULE_OVERLAY_DIR }

        restoreFiles(*files.toTypedArray())

        restoreBlurSettings()

        // Remove backup directory
        Shell.cmd("rm -rf $BACKUP_DIR").exec()
    }

    private fun backupExists(fileName: String): Boolean {
        return RootUtils.fileExists("$BACKUP_DIR/$fileName")
    }

    @Suppress("SameParameterValue")
    private fun backupFiles(vararg sources: String) {
        for (file in sources) {
            backupFile(file)
        }
    }

    private fun backupFile(source: String) {
        if (RootUtils.fileExists(source)) Shell.cmd("cp -rf $source $BACKUP_DIR/")
            .exec()
    }

    @Suppress("SameParameterValue")
    private fun restoreFiles(vararg files: Pair<String, String>) {
        for ((fileName, dest) in files) {
            restoreFile(fileName, dest)
        }
    }

    private fun restoreFile(fileName: String, dest: String) {
        if (backupExists(fileName)) {
            Shell.cmd("rm -rf $dest/$fileName").exec()
            Shell.cmd("cp -rf $BACKUP_DIR/$fileName $dest/").exec()
        }
    }

    private fun restoreBlurSettings() {
        if (isBlurEnabled) {
            enableBlur()
        }
    }

    private val isBlurEnabled: Boolean
        get() {
            val outs = Shell.cmd(
                "if grep -q \"ro.surface_flinger.supports_background_blur=1\" $TEMP_MODULE_DIR/system.prop; then echo yes; else echo no; fi"
            ).exec().out
            return outs[0] == "yes"
        }

    private fun disableBlur() {
        Shell.cmd(
            "mv $TEMP_MODULE_DIR/system.prop $TEMP_MODULE_DIR/system.txt; grep -v \"ro.surface_flinger.supports_background_blur\" $TEMP_MODULE_DIR/system.txt > $TEMP_MODULE_DIR/system.txt.tmp; rm -rf $TEMP_MODULE_DIR/system.prop; mv $TEMP_MODULE_DIR/system.txt.tmp $TEMP_MODULE_DIR/system.prop; rm -rf $TEMP_MODULE_DIR/system.txt; rm -rf $TEMP_MODULE_DIR/system.txt.tmp"
        ).exec()
        Shell.cmd(
            "grep -v \"ro.surface_flinger.supports_background_blur\" $TEMP_MODULE_DIR/service.sh > $TEMP_MODULE_DIR/service.sh.tmp && mv $TEMP_MODULE_DIR/service.sh.tmp $TEMP_MODULE_DIR/service.sh"
        ).exec()
    }

    private fun enableBlur() {
        disableBlur()

        val blurCmd1 = "ro.surface_flinger.supports_background_blur=1"
        val blurCmd2 =
            "resetprop ro.surface_flinger.supports_background_blur 1 && killall surfaceflinger"

        Shell.cmd(
            "echo \"$blurCmd1\" >> $TEMP_MODULE_DIR/system.prop"
        ).exec()
        Shell.cmd(
            "sed '/*}/a $blurCmd2' $TEMP_MODULE_DIR/service.sh > $TEMP_MODULE_DIR/service.sh.tmp && mv $TEMP_MODULE_DIR/service.sh.tmp $TEMP_MODULE_DIR/service.sh"
        ).exec()
    }
}
