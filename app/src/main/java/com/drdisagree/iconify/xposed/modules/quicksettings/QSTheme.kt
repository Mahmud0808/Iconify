package com.drdisagree.iconify.xposed.modules.quicksettings

import android.content.Context
import android.graphics.Color
import androidx.core.graphics.toColorInt
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callStaticMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getAnyField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.log
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.setField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.setFieldSilently
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import de.robv.android.xposed.callbacks.XC_LoadPackage

class QSTheme(context: Context) : ModPack(context) {

    private var customQsTheme = false

    private var activeBgColor = Color.WHITE
    private var activeIconColor = Color.WHITE
    private var activeIconBgColor = Color.WHITE
    private var activeLabelColor = Color.WHITE
    private var activeSecondaryLabelColor = Color.WHITE

    private var inactiveBgColor = Color.WHITE
    private var inactiveIconColor = Color.WHITE
    private var inactiveIconBgColor = Color.WHITE
    private var inactiveLabelColor = Color.WHITE
    private var inactiveSecondaryLabelColor = Color.WHITE

    private var unavailableBgColor = Color.WHITE
    private var unavailableIconColor = Color.WHITE
    private var unavailableIconBgColor = Color.WHITE
    private var unavailableLabelColor = Color.WHITE
    private var unavailableSecondaryLabelColor = Color.WHITE

    override fun updatePrefs(vararg key: String) {
        Xprefs.apply {
            customQsTheme = getBoolean(XposedKey.CUSTOM_QS_THEME)

            fun readColor(key: XposedKey) = getString(key).toColorInt()

            activeBgColor = readColor(XposedKey.ACTIVE_QS_TILE_BACKGROUND_COLOR)
            activeIconColor = readColor(XposedKey.ACTIVE_QS_TILE_ICON_COLOR)
            activeIconBgColor = readColor(XposedKey.ACTIVE_QS_TILE_ICON_BACKGROUND_COLOR)
            activeLabelColor = readColor(XposedKey.ACTIVE_QS_TILE_LABEL_COLOR)
            activeSecondaryLabelColor = readColor(XposedKey.ACTIVE_QS_TILE_SECONDARY_LABEL_COLOR)

            inactiveBgColor = readColor(XposedKey.INACTIVE_QS_TILE_BACKGROUND_COLOR)
            inactiveIconColor = readColor(XposedKey.INACTIVE_QS_TILE_ICON_COLOR)
            inactiveIconBgColor = readColor(XposedKey.INACTIVE_QS_TILE_ICON_BACKGROUND_COLOR)
            inactiveLabelColor = readColor(XposedKey.INACTIVE_QS_TILE_LABEL_COLOR)
            inactiveSecondaryLabelColor =
                readColor(XposedKey.INACTIVE_QS_TILE_SECONDARY_LABEL_COLOR)

            unavailableBgColor = readColor(XposedKey.UNAVAILABLE_QS_TILE_BACKGROUND_COLOR)
            unavailableIconColor = readColor(XposedKey.UNAVAILABLE_QS_TILE_ICON_COLOR)
            unavailableIconBgColor = readColor(XposedKey.UNAVAILABLE_QS_TILE_ICON_BACKGROUND_COLOR)
            unavailableLabelColor = readColor(XposedKey.UNAVAILABLE_QS_TILE_LABEL_COLOR)
            unavailableSecondaryLabelColor =
                readColor(XposedKey.UNAVAILABLE_QS_TILE_SECONDARY_LABEL_COLOR)
        }
    }

    override fun handleLoadPackage(loadPackageParam: XC_LoadPackage.LoadPackageParam) {
        val colorKtClass = findClass("androidx.compose.ui.graphics.ColorKt")
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
        //                                colorKtClass.callStaticMethod("Color", Color.RED)
        //                            )
        //                        }
        //                    }
        //            }

        tileDefaultsClass
            .hookMethod("getColorForState")
            .runAfter { param ->
                if (!customQsTheme) return@runAfter

                val tileUiState = param.args[0]
                val state = tileUiState.getAnyField("visualState", "state") as? Int

                param.result.apply {
                    when (state) {
                        STATE_ACTIVE -> {
                            setField(
                                "background",
                                colorKtClass.callStaticMethod("Color", activeBgColor)
                            )
                            setField(
                                "icon",
                                colorKtClass.callStaticMethod("Color", activeIconColor)
                            )
                            setField(
                                "iconBackground",
                                colorKtClass.callStaticMethod("Color", activeIconBgColor)
                            )
                            setField(
                                "label",
                                colorKtClass.callStaticMethod("Color", activeLabelColor)
                            )
                            setField(
                                "secondaryLabel",
                                colorKtClass.callStaticMethod("Color", activeSecondaryLabelColor)
                            )
                            setFieldSilently("iconBackgroundGradient", null)
                        }

                        STATE_INACTIVE -> {
                            setField(
                                "background",
                                colorKtClass.callStaticMethod("Color", inactiveBgColor)
                            )
                            setField(
                                "icon",
                                colorKtClass.callStaticMethod("Color", inactiveIconColor)
                            )
                            setField(
                                "iconBackground",
                                colorKtClass.callStaticMethod("Color", inactiveIconBgColor)
                            )
                            setField(
                                "label",
                                colorKtClass.callStaticMethod("Color", inactiveLabelColor)
                            )
                            setField(
                                "secondaryLabel",
                                colorKtClass.callStaticMethod("Color", inactiveSecondaryLabelColor)
                            )
                            setFieldSilently("iconBackgroundGradient", null)
                        }

                        STATE_UNAVAILABLE -> {
                            setField(
                                "background",
                                colorKtClass.callStaticMethod("Color", unavailableBgColor)
                            )
                            setField(
                                "icon",
                                colorKtClass.callStaticMethod("Color", unavailableIconColor)
                            )
                            setField(
                                "iconBackground",
                                colorKtClass.callStaticMethod("Color", unavailableIconBgColor)
                            )
                            setField(
                                "label",
                                colorKtClass.callStaticMethod("Color", unavailableLabelColor)
                            )
                            setField(
                                "secondaryLabel",
                                colorKtClass.callStaticMethod(
                                    "Color",
                                    unavailableSecondaryLabelColor
                                )
                            )
                            setFieldSilently("iconBackgroundGradient", null)
                        }

                        else -> log(this@QSTheme, "Unknown state: $state")
                    }
                }
            }
    }

    companion object {
        private const val STATE_UNAVAILABLE = 0
        private const val STATE_INACTIVE = 1
        private const val STATE_ACTIVE = 2
    }
}