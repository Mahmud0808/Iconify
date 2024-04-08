package com.drdisagree.iconify.utils.overlay

import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.common.Resources
import com.drdisagree.iconify.config.Prefs
import com.topjohnwu.superuser.Shell
import java.util.Objects

object OverlayUtil {

    @JvmStatic
    val overlayList: List<String>
        get() = Shell.cmd(
            "cmd overlay list |  grep -E '....IconifyComponent' | sed -E 's/^....//'"
        ).exec().out

    @JvmStatic
    val enabledOverlayList: List<String>
        get() = Shell.cmd(
            "cmd overlay list |  grep -E '.x..IconifyComponent' | sed -E 's/^.x..//'"
        ).exec().out

    @JvmStatic
    val disabledOverlayList: List<String>
        get() = Shell.cmd(
            "cmd overlay list |  grep -E '. ..IconifyComponent' | sed -E 's/^. ..//'"
        ).exec().out

    @JvmStatic
    fun isOverlayEnabled(pkgName: String): Boolean {
        return Shell.cmd(
            "[[ $(cmd overlay list | grep -o '\\[x\\] $pkgName') ]] && echo 1 || echo 0"
        ).exec().out[0] == "1"
    }

    @JvmStatic
    fun isOverlayDisabled(pkgName: String): Boolean {
        return !isOverlayEnabled(pkgName)
    }

    @JvmStatic
    fun isOverlayInstalled(enabledOverlays: List<String>, pkgName: String): Boolean {
        for (line in enabledOverlays) {
            if (line == pkgName) return true
        }

        return false
    }

    @JvmStatic
    fun enableOverlay(pkgName: String) {
        Prefs.putBoolean(pkgName, true)
        Shell.cmd(
            "cmd overlay enable --user current $pkgName",
            "cmd overlay set-priority $pkgName highest"
        ).submit()
    }

    @JvmStatic
    fun enableOverlays(vararg pkgNames: String?) {
        val command = StringBuilder()

        for (pkgName in pkgNames) {
            Prefs.putBoolean(pkgName, true)

            command.append("cmd overlay enable --user current ").append(pkgName)
                .append("; cmd overlay set-priority ").append(pkgName).append(" highest; ")
        }

        Shell.cmd(command.toString().trim { it <= ' ' }).submit()
    }

    @JvmStatic
    fun enableOverlayExclusiveInCategory(pkgName: String) {
        Prefs.putBoolean(pkgName, true)
        Shell.cmd(
            "cmd overlay enable-exclusive --user current --category $pkgName",
            "cmd overlay set-priority $pkgName highest"
        ).submit()
    }

    @JvmStatic
    fun enableOverlaysExclusiveInCategory(vararg pkgNames: String?) {
        val command = StringBuilder()

        for (pkgName in pkgNames) {
            Prefs.putBoolean(pkgName, true)

            command.append("cmd overlay enable-exclusive --user current --category ")
                .append(pkgName).append("; cmd overlay set-priority ").append(pkgName)
                .append(" highest; ")
        }

        Shell.cmd(command.toString().trim { it <= ' ' }).submit()
    }

    @JvmStatic
    fun disableOverlay(pkgName: String) {
        Prefs.putBoolean(pkgName, false)
        Shell.cmd("cmd overlay disable --user current $pkgName").submit()
    }

    @JvmStatic
    fun disableOverlays(vararg pkgNames: String?) {
        val command = StringBuilder()

        for (pkgName in pkgNames) {
            Prefs.putBoolean(pkgName, false)

            command.append("cmd overlay disable --user current ").append(pkgName).append("; ")
        }

        Shell.cmd(command.toString().trim { it <= ' ' }).submit()
    }

    @JvmStatic
    fun changeOverlayState(vararg args: Any) {
        require(args.size % 2 == 0) { "Number of arguments must be even." }

        val command = StringBuilder()
        var i = 0

        while (i < args.size) {
            val pkgName = args[i] as String
            val state = args[i + 1] as Boolean

            Prefs.putBoolean(pkgName, state)

            if (state) {
                command.append("cmd overlay enable --user current ").append(pkgName)
                    .append("; cmd overlay set-priority ").append(pkgName).append(" highest; ")
            } else {
                command.append("cmd overlay disable --user current ").append(pkgName).append("; ")
            }

            i += 2
        }

        Shell.cmd(command.toString().trim { it <= ' ' }).submit()
    }

    @JvmStatic
    fun overlayExists(): Boolean {
        return Shell.cmd(
            "[ -f /system/product/overlay/IconifyComponentAMGC.apk ] && echo \"found\" || echo \"not found\""
        ).exec().out[0] == "found"
    }

    @Suppress("unused")
    fun matchOverlayAgainstAssets(): Boolean {
        return try {
            val packages = appContext.assets.list("Overlays")
            var numberOfOverlaysInAssets = 0

            for (overlay in packages!!) {
                numberOfOverlaysInAssets += Objects.requireNonNull(
                    appContext.assets.list(
                        "Overlays/$overlay"
                    )
                ).size
            }

            val numberOfOverlaysInstalled =
                Shell.cmd("find /" + Resources.OVERLAY_DIR + "/ -maxdepth 1 -type f -print| wc -l")
                    .exec().out[0].toInt()

            numberOfOverlaysInAssets <= numberOfOverlaysInstalled
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
