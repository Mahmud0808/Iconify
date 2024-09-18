package com.drdisagree.iconify.utils.helper

import com.drdisagree.iconify.common.Resources
import com.drdisagree.iconify.utils.RootUtils
import com.topjohnwu.superuser.Shell

object BackupRestore {

    fun backupFiles() {
        // Create backup directory
        Shell.cmd("rm -rf " + Resources.BACKUP_DIR, "mkdir -p " + Resources.BACKUP_DIR).exec()

        backupFile(Resources.MODULE_DIR + "/system.prop")
        backupFile(Resources.OVERLAY_DIR + "/IconifyComponentME.apk")
        backupFile(Resources.OVERLAY_DIR + "/IconifyComponentCR1.apk")
        backupFile(Resources.OVERLAY_DIR + "/IconifyComponentCR2.apk")
        backupFile(Resources.OVERLAY_DIR + "/IconifyComponentSIS.apk")
        backupFile(Resources.OVERLAY_DIR + "/IconifyComponentSIP1.apk")
        backupFile(Resources.OVERLAY_DIR + "/IconifyComponentSIP2.apk")
        backupFile(Resources.OVERLAY_DIR + "/IconifyComponentSIP3.apk")
        backupFile(Resources.OVERLAY_DIR + "/IconifyComponentPGB.apk")
        backupFile(Resources.OVERLAY_DIR + "/IconifyComponentSWITCH1.apk")
        backupFile(Resources.OVERLAY_DIR + "/IconifyComponentSWITCH2.apk")
        backupFile(Resources.OVERLAY_DIR + "/IconifyComponentDynamic1.apk")
        backupFile(Resources.OVERLAY_DIR + "/IconifyComponentDynamic2.apk")
    }

    fun restoreFiles() {
        restoreFile("system.prop", Resources.TEMP_MODULE_DIR)
        restoreFile("IconifyComponentME.apk", Resources.TEMP_MODULE_OVERLAY_DIR)
        restoreFile("IconifyComponentCR1.apk", Resources.TEMP_MODULE_OVERLAY_DIR)
        restoreFile("IconifyComponentCR2.apk", Resources.TEMP_MODULE_OVERLAY_DIR)
        restoreFile("IconifyComponentSIS.apk", Resources.TEMP_MODULE_OVERLAY_DIR)
        restoreFile("IconifyComponentSIP1.apk", Resources.TEMP_MODULE_OVERLAY_DIR)
        restoreFile("IconifyComponentSIP2.apk", Resources.TEMP_MODULE_OVERLAY_DIR)
        restoreFile("IconifyComponentSIP3.apk", Resources.TEMP_MODULE_OVERLAY_DIR)
        restoreFile("IconifyComponentPGB.apk", Resources.TEMP_MODULE_OVERLAY_DIR)
        restoreFile("IconifyComponentSWITCH1.apk", Resources.TEMP_MODULE_OVERLAY_DIR)
        restoreFile("IconifyComponentSWITCH2.apk", Resources.TEMP_MODULE_OVERLAY_DIR)
        restoreFile("IconifyComponentDynamic1.apk", Resources.TEMP_MODULE_OVERLAY_DIR)
        restoreFile("IconifyComponentDynamic2.apk", Resources.TEMP_MODULE_OVERLAY_DIR)

        restoreBlurSettings()

        // Remove backup directory
        Shell.cmd("rm -rf " + Resources.BACKUP_DIR).exec()
    }

    private fun backupExists(fileName: String): Boolean {
        return RootUtils.fileExists(Resources.BACKUP_DIR + "/" + fileName)
    }

    private fun backupFile(source: String) {
        if (RootUtils.fileExists(source)) Shell.cmd("cp -rf " + source + " " + Resources.BACKUP_DIR + "/")
            .exec()
    }

    private fun restoreFile(fileName: String, dest: String) {
        if (backupExists(fileName)) {
            Shell.cmd("rm -rf $dest/$fileName").exec()
            Shell.cmd("cp -rf " + Resources.BACKUP_DIR + "/" + fileName + " " + dest + "/").exec()
        }
    }

    private fun restoreBlurSettings() {
        if (isBlurEnabled) {
            enableBlur()
        }
    }

    private val isBlurEnabled: Boolean
        get() {
            val outs =
                Shell.cmd("if grep -q \"ro.surface_flinger.supports_background_blur=1\" " + Resources.TEMP_MODULE_DIR + "/system.prop; then echo yes; else echo no; fi")
                    .exec().out
            return outs[0] == "yes"
        }

    private fun disableBlur() {
        Shell.cmd("mv " + Resources.TEMP_MODULE_DIR + "/system.prop " + Resources.TEMP_MODULE_DIR + "/system.txt; grep -v \"ro.surface_flinger.supports_background_blur\" " + Resources.TEMP_MODULE_DIR + "/system.txt > " + Resources.TEMP_MODULE_DIR + "/system.txt.tmp; rm -rf " + Resources.TEMP_MODULE_DIR + "/system.prop; mv " + Resources.TEMP_MODULE_DIR + "/system.txt.tmp " + Resources.TEMP_MODULE_DIR + "/system.prop; rm -rf " + Resources.TEMP_MODULE_DIR + "/system.txt; rm -rf " + Resources.TEMP_MODULE_DIR + "/system.txt.tmp")
            .exec()
        Shell.cmd("grep -v \"ro.surface_flinger.supports_background_blur\" " + Resources.TEMP_MODULE_DIR + "/service.sh > " + Resources.TEMP_MODULE_DIR + "/service.sh.tmp && mv " + Resources.TEMP_MODULE_DIR + "/service.sh.tmp " + Resources.TEMP_MODULE_DIR + "/service.sh")
            .exec()
    }

    private fun enableBlur() {
        disableBlur()

        val blurCmd1 = "ro.surface_flinger.supports_background_blur=1"
        val blurCmd2 =
            "resetprop ro.surface_flinger.supports_background_blur 1 && killall surfaceflinger"

        Shell.cmd("echo \"" + blurCmd1 + "\" >> " + Resources.TEMP_MODULE_DIR + "/system.prop")
            .exec()
        Shell.cmd("sed '/*}/a " + blurCmd2 + "' " + Resources.TEMP_MODULE_DIR + "/service.sh > " + Resources.TEMP_MODULE_DIR + "/service.sh.tmp && mv " + Resources.TEMP_MODULE_DIR + "/service.sh.tmp " + Resources.TEMP_MODULE_DIR + "/service.sh")
            .exec()
    }
}
