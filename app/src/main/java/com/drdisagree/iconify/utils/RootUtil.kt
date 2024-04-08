package com.drdisagree.iconify.utils

import com.topjohnwu.superuser.Shell

object RootUtil {

    @JvmStatic
    val isDeviceRooted: Boolean
        get() = java.lang.Boolean.TRUE == Shell.isAppGrantedRoot()

    @JvmStatic
    val isMagiskInstalled: Boolean
        get() = Shell.cmd("magisk -v").exec().isSuccess

    @JvmStatic
    val isKSUInstalled: Boolean
        get() = Shell.cmd("/data/adb/ksud -h").exec().isSuccess

    @JvmStatic
    val isApatchInstalled: Boolean
        get() = Shell.cmd("apd --help").exec().isSuccess

    fun moduleExists(moduleId: String): Boolean {
        return folderExists("/data/adb/modules/$moduleId")
    }

    @JvmStatic
    fun setPermissions(permission: Int, filename: String) {
        Shell.cmd("chmod $permission $filename").exec()
    }

    fun setPermissionsRecursively(permission: Int, folderName: String) {
        Shell.cmd("chmod -R $permission $folderName").exec()
        val perm = permission.toString()

        if (!Shell.cmd("stat -c '%a' $folderName").exec().out.contains(perm) || !Shell.cmd(
                "fl=$(find '$folderName' -type f -mindepth 1 -print -quit); stat -c '%a' \$fl"
            ).exec().out.contains(perm)
        ) Shell.cmd("for file in $folderName*; do chmod $permission \"\$file\"; done").exec()
    }

    fun fileExists(dir: String): Boolean {
        val lines = Shell.cmd("test -f $dir && echo '1'").exec().out

        for (line in lines) {
            if (line.contains("1")) return true
        }

        return false
    }

    @JvmStatic
    fun folderExists(dir: String): Boolean {
        val lines = Shell.cmd("test -d $dir && echo '1'").exec().out

        for (line in lines) {
            if (line.contains("1")) return true
        }

        return false
    }

    @JvmStatic
    fun deviceProperlyRooted(): Boolean {
        return isDeviceRooted && (isMagiskInstalled || isKSUInstalled || isApatchInstalled)
    }
}
