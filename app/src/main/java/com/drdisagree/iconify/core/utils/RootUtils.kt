package com.drdisagree.iconify.core.utils

import com.drdisagree.iconify.data.common.Resources.OVERLAY_DIR
import com.topjohnwu.superuser.Shell

object RootUtils {

    val isDeviceRooted: Boolean
        get() = Shell.isAppGrantedRoot() == java.lang.Boolean.TRUE

    val isMagiskInstalled: Boolean
        get() = Shell.cmd("magisk -v").exec().isSuccess

    val isKSUInstalled: Boolean
        get() = Shell.cmd("ksud -h").exec().isSuccess

    val isApatchInstalled: Boolean
        get() = Shell.cmd("apd --help").exec().isSuccess

    val isSusfsBinaryAvailable: Boolean
        get() = !isMagiskInstalled &&
                isKSUInstalled &&
                fileExists("/data/adb/ksu/bin/ksu_susfs")

    fun moduleExists(moduleId: String): Boolean {
        return folderExists("/data/adb/modules/$moduleId")
    }

    fun moduleUpdateExists(moduleId: String): Boolean {
        return fileExists("/data/adb/modules_update/$moduleId/module.prop")
    }

    fun setPermissions(permission: Int, filename: String) {
        Shell.cmd("chmod $permission $filename").exec()
    }

    fun setPermissionsRecursively(
        folderName: String,
        dirPerm: Int = 755,
        filePerm: Int = 644,
        execPerm: Int = 755
    ) {
        Shell.cmd(
            "find '$folderName' -type d -exec chmod $dirPerm {} \\;",
            "find '$folderName' -type f -name '*.sh' -exec chmod $execPerm {} \\;",
            "find '$folderName' -type f ! -name '*.sh' -exec chmod $filePerm {} \\;",
            "find '$folderName/META-INF' -type f -name 'update-binary' -exec chmod $execPerm {} \\;",
        ).exec()
    }

    fun fileExists(path: String): Boolean {
        return Shell.cmd("[ -f \"$path\" ]").exec().code == 0
    }

    fun folderExists(path: String): Boolean {
        return Shell.cmd("[ -d \"$path\" ]").exec().code == 0
    }

    fun deviceProperlyRooted(): Boolean {
        return isDeviceRooted && (isMagiskInstalled || isKSUInstalled || isApatchInstalled)
    }

    fun requireMetamodule(): Boolean {
        return isKSUInstalled && getKsuVersion() >= 3 && !isMetaModuleInstalled()
    }

    fun getKsuVersion(): Int {
        val ksuVersion = Shell.cmd("ksud -V").exec().out.joinToString().trim()
        val majorVersion = Regex("""\b(\d+)(?=\.)""")
            .find(ksuVersion)
            ?.groupValues
            ?.get(1)

        return majorVersion?.toInt() ?: 0
    }

    fun isMetaModuleInstalled(): Boolean {
        val result = Shell.cmd(
            $$"for d in /data/adb/modules/*; do prop=\"$d/module.prop\"; [ -f \"$prop\" ] && grep -qiE \"^metamodule[[:space:]]*=[[:space:]]*(1|true)$\" \"$prop\" && echo true && exit 0; done; echo false"
        ).exec()

        return result.out.firstOrNull() == "true"
    }

    fun isModuleUpdatePending(): Boolean {
        return !folderExists(OVERLAY_DIR) &&
                folderExists(
                    OVERLAY_DIR.replace(
                        "modules",
                        "modules_update"
                    )
                )
    }
}
