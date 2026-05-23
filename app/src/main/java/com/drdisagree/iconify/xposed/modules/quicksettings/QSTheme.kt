package com.drdisagree.iconify.xposed.modules.quicksettings

import android.content.Context
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.GraphicsColorKt
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getAnyField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.log
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.setField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.setFieldSilently
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import de.robv.android.xposed.callbacks.XC_LoadPackage

class QSTheme(context: Context) : ModPack(context) {

    private var customQsTheme = false

    private var activeBgColor = "#FFFFFF"
    private var activeIconColor = "#FFFFFF"
    private var activeIconBgColor = "#FFFFFF"
    private var activeLabelColor = "#FFFFFF"
    private var activeSecondaryLabelColor = "#FFFFFF"

    private var inactiveBgColor = "#FFFFFF"
    private var inactiveIconColor = "#FFFFFF"
    private var inactiveIconBgColor = "#FFFFFF"
    private var inactiveLabelColor = "#FFFFFF"
    private var inactiveSecondaryLabelColor = "#FFFFFF"

    private var unavailableBgColor = "#FFFFFF"
    private var unavailableIconColor = "#FFFFFF"
    private var unavailableIconBgColor = "#FFFFFF"
    private var unavailableLabelColor = "#FFFFFF"
    private var unavailableSecondaryLabelColor = "#FFFFFF"

    override fun updatePrefs(vararg key: String) {
        Xprefs.apply {
            customQsTheme = getBoolean(XposedKey.CUSTOM_QS_THEME)

            activeBgColor = getString(XposedKey.ACTIVE_QS_TILE_BACKGROUND_COLOR)
            activeIconColor = getString(XposedKey.ACTIVE_QS_TILE_ICON_COLOR)
            activeIconBgColor = getString(XposedKey.ACTIVE_QS_TILE_ICON_BACKGROUND_COLOR)
            activeLabelColor = getString(XposedKey.ACTIVE_QS_TILE_LABEL_COLOR)
            activeSecondaryLabelColor = getString(XposedKey.ACTIVE_QS_TILE_SECONDARY_LABEL_COLOR)

            inactiveBgColor = getString(XposedKey.INACTIVE_QS_TILE_BACKGROUND_COLOR)
            inactiveIconColor = getString(XposedKey.INACTIVE_QS_TILE_ICON_COLOR)
            inactiveIconBgColor = getString(XposedKey.INACTIVE_QS_TILE_ICON_BACKGROUND_COLOR)
            inactiveLabelColor = getString(XposedKey.INACTIVE_QS_TILE_LABEL_COLOR)
            inactiveSecondaryLabelColor =
                getString(XposedKey.INACTIVE_QS_TILE_SECONDARY_LABEL_COLOR)

            unavailableBgColor = getString(XposedKey.UNAVAILABLE_QS_TILE_BACKGROUND_COLOR)
            unavailableIconColor = getString(XposedKey.UNAVAILABLE_QS_TILE_ICON_COLOR)
            unavailableIconBgColor = getString(XposedKey.UNAVAILABLE_QS_TILE_ICON_BACKGROUND_COLOR)
            unavailableLabelColor = getString(XposedKey.UNAVAILABLE_QS_TILE_LABEL_COLOR)
            unavailableSecondaryLabelColor =
                getString(XposedKey.UNAVAILABLE_QS_TILE_SECONDARY_LABEL_COLOR)
        }
    }

    override fun handleLoadPackage(loadPackageParam: XC_LoadPackage.LoadPackageParam) {
        val tileDefaultsClass =
            findClass("$SYSTEMUI_PACKAGE.qs.panels.ui.compose.infinitegrid.TileDefaults")

        //        findClass("androidx.compose.material3.SliderColors")
        //            .hookConstructor()
        //            .runAfter { param ->
        //                Thread.currentThread().stackTrace.firstOrNull { it.className.contains("BrightnessSliderKt") }
        //                    ?.let {
        //                        listOf(
        //                            "activeTickColor",
        //                            "activeTrackColor",
        //                            "disabledActiveTickColor",
        //                            "disabledActiveTrackColor",
        //                            "disabledInactiveTickColor",
        //                            "disabledInactiveTrackColor",
        //                            "disabledThumbColor",
        //                            "inactiveTickColor",
        //                            "inactiveTrackColor",
        //                            "thumbColor",
        //                        ).forEach { name ->
        //                            param.thisObject.setField(
        //                                name,
        //                                GraphicsColorKt.colorOf(Color.RED)
        //                            )
        //                        }
        //                    }
        //            }

        tileDefaultsClass
            .hookMethod("getColorForState")
            .runAfter { param ->
                if (!customQsTheme) return@runAfter

                val tileUiState = param.args[0]
                val iconOnly = param.args[1] as Boolean
                val state = tileUiState.getAnyField("visualState", "state") as? Int
                val handlesSecondaryClick = tileUiState.getAnyField(
                    "handlesSecondaryClick",
                    "handlesToggleClick"
                ) as Boolean
                val isDualTarget = handlesSecondaryClick && !iconOnly

                param.result.apply {
                    when (state) {
                        STATE_ACTIVE -> {
                            setField(
                                "background",
                                GraphicsColorKt.colorOf(if (isDualTarget) activeBgColor else activeIconBgColor)
                            )
                            setField(
                                "icon",
                                GraphicsColorKt.colorOf(activeIconColor)
                            )
                            setField(
                                "iconBackground",
                                GraphicsColorKt.colorOf(activeIconBgColor)
                            )
                            setField(
                                "label",
                                GraphicsColorKt.colorOf(activeLabelColor)
                            )
                            setField(
                                "secondaryLabel",
                                GraphicsColorKt.colorOf(activeSecondaryLabelColor)
                            )
                        }

                        STATE_INACTIVE -> {
                            setField(
                                "background",
                                GraphicsColorKt.colorOf(inactiveBgColor)
                            )
                            setField(
                                "icon",
                                GraphicsColorKt.colorOf(inactiveIconColor)
                            )
                            setField(
                                "iconBackground",
                                GraphicsColorKt.colorOf(inactiveIconBgColor)
                            )
                            setField(
                                "label",
                                GraphicsColorKt.colorOf(inactiveLabelColor)
                            )
                            setField(
                                "secondaryLabel",
                                GraphicsColorKt.colorOf(inactiveSecondaryLabelColor)
                            )
                        }

                        STATE_UNAVAILABLE -> {
                            setField(
                                "background",
                                GraphicsColorKt.colorOf(unavailableBgColor)
                            )
                            setField(
                                "icon",
                                GraphicsColorKt.colorOf(unavailableIconColor)
                            )
                            setField(
                                "iconBackground",
                                GraphicsColorKt.colorOf(unavailableIconBgColor)
                            )
                            setField(
                                "label",
                                GraphicsColorKt.colorOf(unavailableLabelColor)
                            )
                            setField(
                                "secondaryLabel",
                                GraphicsColorKt.colorOf(unavailableSecondaryLabelColor)
                            )
                        }

                        else -> log(this@QSTheme, "Unknown state: $state")
                    }

                    setFieldSilently("iconBackgroundGradient", null)
                }
            }
    }

    companion object {
        private const val STATE_UNAVAILABLE = 0
        private const val STATE_INACTIVE = 1
        private const val STATE_ACTIVE = 2
    }
}