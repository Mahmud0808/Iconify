package com.drdisagree.iconify.core.utils.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.content.ContextCompat
import com.drdisagree.iconify.app.Iconify.Companion.appContext
import com.drdisagree.iconify.data.common.Resources.OVERLAY_DIR
import com.topjohnwu.superuser.Shell
import java.util.Objects

object OverlayUtils {

    val overlayList: List<String>
        get() = Shell.cmd(
            "cmd overlay list |  grep -E '....IconifyComponent' | sed -E 's/^....//'"
        ).exec().out

    val enabledOverlayList: List<String>
        get() = Shell.cmd(
            "cmd overlay list |  grep -E '.x..IconifyComponent' | sed -E 's/^.x..//'"
        ).exec().out

    val disabledOverlayList: List<String>
        get() = Shell.cmd(
            "cmd overlay list |  grep -E '. ..IconifyComponent' | sed -E 's/^. ..//'"
        ).exec().out

    fun getOverlayForComponent(componentName: String): List<String> {
        return Shell.cmd("cmd overlay list | grep '....IconifyComponent$componentName'")
            .exec().out
    }

    fun isOverlayEnabled(pkgName: String): Boolean {
        return Shell.cmd(
            "[[ $(cmd overlay list | grep -o '\\[x\\] $pkgName') ]] && echo 1 || echo 0"
        ).exec().out[0] == "1"
    }

    fun isOverlayDisabled(pkgName: String): Boolean {
        return !isOverlayEnabled(pkgName)
    }

    fun isOverlayInstalled(enabledOverlays: List<String>, pkgName: String): Boolean {
        for (line in enabledOverlays) {
            if (line == pkgName) return true
        }

        return false
    }

    fun enableOverlay(pkgName: String, priority: String = "highest") {
        Shell.cmd(
            "cmd overlay enable --user current $pkgName",
            "cmd overlay set-priority $pkgName $priority"
        ).submit()
    }

    fun enableOverlays(vararg pkgNames: String?) {
        val command = StringBuilder()

        for (pkgName in pkgNames) {
            command.append("cmd overlay enable --user current ").append(pkgName)
                .append("; cmd overlay set-priority ").append(pkgName).append(" highest; ")
        }

        Shell.cmd(command.toString().trim { it <= ' ' }).submit()
    }

    fun enableOverlayExclusiveInCategory(pkgName: String) {
        Shell.cmd(
            "cmd overlay enable-exclusive --user current --category $pkgName",
            "cmd overlay set-priority $pkgName highest"
        ).submit()
    }

    fun enableOverlayExclusiveInCategory(pkgName: String, priority: String) {
        Shell.cmd(
            "cmd overlay enable-exclusive --user current --category $pkgName",
            "cmd overlay set-priority $pkgName $priority"
        ).submit()
    }

    fun enableOverlaysExclusiveInCategory(vararg pkgNames: String?) {
        val command = StringBuilder()

        for (pkgName in pkgNames) {
            command.append("cmd overlay enable-exclusive --user current --category ")
                .append(pkgName).append("; cmd overlay set-priority ").append(pkgName)
                .append(" highest; ")
        }

        Shell.cmd(command.toString().trim { it <= ' ' }).submit()
    }

    fun disableOverlay(pkgName: String) {
        Shell.cmd("cmd overlay disable --user current $pkgName").submit()
    }

    fun disableOverlays(vararg pkgNames: String?) {
        val command = StringBuilder()

        for (pkgName in pkgNames) {
            command.append("cmd overlay disable --user current ").append(pkgName).append("; ")
        }

        Shell.cmd(command.toString().trim { it <= ' ' }).submit()
    }

    fun changeOverlayState(vararg args: Any) {
        require(args.size % 2 == 0) { "Number of arguments must be even." }

        val command = StringBuilder()
        var i = 0

        while (i < args.size) {
            val pkgName = args[i] as String
            val state = args[i + 1] as Boolean

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

    fun overlayExists(): Boolean {
        return Shell.cmd(
            "[ -f /system/product/overlay/IconifyComponentAMGC.apk ] && echo \"found\" || echo \"not found\""
        ).exec().takeIf { it.out.isNotEmpty() }?.out[0] == "found"
    }

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
                Shell.cmd("find /$OVERLAY_DIR/ -maxdepth 1 -type f -print| wc -l")
                    .exec().out[0].toInt()

            numberOfOverlaysInAssets <= numberOfOverlaysInstalled
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    @SuppressLint("DiscouragedApi")
    fun getDrawableFromOverlay(context: Context, pkg: String?, drawableName: String?): Drawable? {
        try {
            val pm = context.packageManager
            val res = pm.getResourcesForApplication(pkg!!)
            val resId = res.getIdentifier(drawableName, "drawable", pkg)
            return if (resId != 0X0) ContextCompat.getDrawable(context, resId)
            else null
        } catch (e: PackageManager.NameNotFoundException) {
            Log.d("OverlayUtil", "getDrawableFromOverlay: Package Not Found " + e.message)
            return null
        }
    }

    @SuppressLint("DiscouragedApi")
    fun getStringFromOverlay(context: Context, pkg: String, stringName: String): String? {
        return try {
            val pm = context.packageManager
            val res = pm.getResourcesForApplication(pkg)
            val resId = res.getIdentifier(stringName, "string", pkg)
            if (resId != 0) res.getString(resId) else null
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("OverlayUtil", "getStringFromOverlay: Package Not Found" + e.message)
            null
        }
    }

    fun checkEnabledOverlay(componentName: String): String {
        val component = Shell.cmd(
            "cmd overlay list | grep \".x..IconifyComponent$componentName\""
        ).exec().out

        if (component.isNotEmpty()) {
            val num = component[0].split("IconifyComponent$componentName".toRegex())
                .dropLastWhile { it.isEmpty() }
                .toTypedArray()[1].split("\\.overlay".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()[0]
            return "IconifyComponent$componentName$num.overlay"
        }

        return ""
    }

    fun enableRoundnessIfDisabled() {
        if (!isOverlayEnabled("IconifyComponentCR1.overlay") || !isOverlayEnabled("IconifyComponentCR2.overlay")) {
            enableOverlays("IconifyComponentCR1.overlay", "IconifyComponentCR2.overlay")
        }
    }
}