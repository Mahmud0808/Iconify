package com.drdisagree.iconify.xposed.modules.quicksettings.themes

import android.service.quicksettings.Tile
import com.drdisagree.iconify.xposed.HookEntry.Companion.enqueueProxyCommand
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.log
import de.robv.android.xposed.XC_MethodHook

object Utils {

    fun getTileState(param: XC_MethodHook.MethodHookParam): Pair<Boolean, Boolean> {
        var isDisabledState: Boolean
        var isActiveState: Boolean
        val idx = if (param.args.size > 1) 1 else 0

        try {
            isDisabledState = try {
                param.args[idx].getField("disabledByPolicy") as Boolean ||
                        param.args[idx].getField("state") as Int == Tile.STATE_UNAVAILABLE
            } catch (throwable: Throwable) {
                param.args[idx].getField("state") as Int == Tile.STATE_UNAVAILABLE
            }

            isActiveState = try {
                param.args[idx].getField("state") as Int == Tile.STATE_ACTIVE
            } catch (throwable: Throwable) {
                try {
                    param.args[idx] as Int == Tile.STATE_ACTIVE
                } catch (throwable1: Throwable) {
                    try {
                        param.args[idx] as Boolean
                    } catch (throwable2: Throwable) {
                        false
                    }
                }
            }
        } catch (ignored: Throwable) {
            isDisabledState = param.args[idx] == Tile.STATE_UNAVAILABLE
            isActiveState = param.args[idx] == Tile.STATE_ACTIVE
        }

        return Pair(isDisabledState, isActiveState)
    }

    fun enableOverlay(pkgName: String) {
        enqueueProxyCommand { proxy ->
            try {
                proxy.runCommand("cmd overlay enable --user current $pkgName")
                proxy.runCommand("cmd overlay set-priority $pkgName highest")
            } catch (e: Throwable) {
                log("QSThemeUtils", "Failed to enable overlay $pkgName: ${e.message}")
            }
        }
    }

    fun enableOverlay(pkgName: String, priority: String) {
        enqueueProxyCommand { proxy ->
            try {
                proxy.runCommand("cmd overlay enable --user current $pkgName")
                proxy.runCommand("cmd overlay set-priority $pkgName $priority")
            } catch (e: Throwable) {
                log("QSThemeUtils", "Failed to enable overlay $pkgName: ${e.message}")
            }
        }
    }

    fun enableOverlays(vararg pkgNames: String?) {
        val command = StringBuilder()

        for (pkgName in pkgNames) {
            command.append("cmd overlay enable --user current ").append(pkgName)
                .append("; cmd overlay set-priority ").append(pkgName).append(" highest; ")
        }

        enqueueProxyCommand { proxy ->
            try {
                proxy.runCommand(command.toString().trim { it <= ' ' })
            } catch (e: Throwable) {
                log("QSThemeUtils", "Failed to enable overlays: ${e.message}")
            }
        }
    }

    fun disableOverlay(pkgName: String) {
        enqueueProxyCommand { proxy ->
            try {
                proxy.runCommand("cmd overlay disable --user current $pkgName")
            } catch (e: Throwable) {
                log("QSThemeUtils", "Failed to disable overlay $pkgName: ${e.message}")
            }
        }
    }

    fun disableOverlays(vararg pkgNames: String?) {
        val command = StringBuilder()

        for (pkgName in pkgNames) {
            command.append("cmd overlay disable --user current ").append(pkgName).append("; ")
        }

        enqueueProxyCommand { proxy ->
            try {
                proxy.runCommand(command.toString().trim { it <= ' ' })
            } catch (e: Throwable) {
                log("QSThemeUtils", "Failed to disable overlays: ${e.message}")
            }
        }
    }
}