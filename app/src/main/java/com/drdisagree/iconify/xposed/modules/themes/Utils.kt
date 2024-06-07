package com.drdisagree.iconify.xposed.modules.themes

import android.service.quicksettings.Tile
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
}