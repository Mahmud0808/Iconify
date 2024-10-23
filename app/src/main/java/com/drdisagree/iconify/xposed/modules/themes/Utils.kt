package com.drdisagree.iconify.xposed.modules.themes

import android.service.quicksettings.Tile
import com.drdisagree.iconify.xposed.HookEntry.Companion.enqueueProxyCommand
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers.getObjectField

object Utils {

    fun getTileState(param: XC_MethodHook.MethodHookParam): Pair<Boolean, Boolean> {
        var isDisabledState: Boolean
        var isActiveState: Boolean

        try {
            isDisabledState = try {
                getObjectField(
                    param.args[1],
                    "disabledByPolicy"
                ) as Boolean ||
                        getObjectField(
                            param.args[1],
                            "state"
                        ) as Int == Tile.STATE_UNAVAILABLE
            } catch (throwable: Throwable) {
                getObjectField(
                    param.args[1],
                    "state"
                ) as Int == Tile.STATE_UNAVAILABLE
            }

            isActiveState = try {
                getObjectField(
                    param.args[1],
                    "state"
                ) as Int == Tile.STATE_ACTIVE
            } catch (throwable: Throwable) {
                try {
                    param.args[1] as Int == Tile.STATE_ACTIVE
                } catch (throwable1: Throwable) {
                    try {
                        param.args[1] as Boolean
                    } catch (throwable2: Throwable) {
                        false
                    }
                }
            }
        } catch (ignored: Throwable) {
            isDisabledState = param.args[1] == Tile.STATE_UNAVAILABLE
            isActiveState = param.args[1] == Tile.STATE_ACTIVE
        }

        return Pair(isDisabledState, isActiveState)
    }

    fun enableOverlay(pkgName: String) {
        enqueueProxyCommand { proxy ->
            proxy.runCommand("cmd overlay enable --user current $pkgName")
            proxy.runCommand("cmd overlay set-priority $pkgName highest")
        }
    }

    fun enableOverlay(pkgName: String, priority: String) {
        enqueueProxyCommand { proxy ->
            proxy.runCommand("cmd overlay enable --user current $pkgName")
            proxy.runCommand("cmd overlay set-priority $pkgName $priority")
        }
    }

    fun enableOverlays(vararg pkgNames: String?) {
        val command = StringBuilder()

        for (pkgName in pkgNames) {
            command.append("cmd overlay enable --user current ").append(pkgName)
                .append("; cmd overlay set-priority ").append(pkgName).append(" highest; ")
        }

        enqueueProxyCommand { proxy ->
            proxy.runCommand(command.toString().trim { it <= ' ' })
        }
    }

    fun disableOverlay(pkgName: String) {
        enqueueProxyCommand { proxy ->
            proxy.runCommand("cmd overlay disable --user current $pkgName")
        }
    }

    fun disableOverlays(vararg pkgNames: String?) {
        val command = StringBuilder()

        for (pkgName in pkgNames) {
            command.append("cmd overlay disable --user current ").append(pkgName).append("; ")
        }

        enqueueProxyCommand { proxy ->
            proxy.runCommand(command.toString().trim { it <= ' ' })
        }
    }
}