package com.drdisagree.iconify.xposed.modules.extras.utils

import android.content.Context
import android.content.res.ColorStateList
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHelpers.callStaticMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.log
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam

class SettingsLibUtils(context: Context) : ModPack(context) {

    override fun onPreferenceUpdated(vararg key: String) {}

    override fun onPackageLoaded(packageReadyParam: PackageReadyParam) {
        UtilsClass = findClass(
            "com.android.settingslib.Utils",
            suppressError = true
        )
    }

    companion object {
        private var UtilsClass: Class<*>? = null

        fun getColorAttr(resID: Int, context: Context): ColorStateList {
            return getColorAttr(
                context,
                resID
            )
        }

        fun getColorAttr(context: Context, resID: Int): ColorStateList {
            return getColorStateListFromUtils(
                "getColorAttr",
                context,
                resID
            )
        }

        fun getColorAttrDefaultColor(resID: Int, context: Context, defValue: Int = 0): Int {
            return getColorFromUtils(
                "getColorAttrDefaultColor",
                context,
                resID,
                defValue
            )
        }

        fun getColorAttrDefaultColor(context: Context, resID: Int, defValue: Int = 0): Int {
            return getColorFromUtils(
                "getColorAttrDefaultColor",
                context,
                resID,
                defValue
            )
        }

        fun getColorStateListDefaultColor(context: Context, resID: Int): Int {
            return getColorStateListFromUtils(
                "getColorStateListDefaultColor",
                context,
                resID
            ).defaultColor
        }

        @Suppress("SameParameterValue")
        private fun getColorFromUtils(
            methodName: String,
            context: Context,
            resID: Int,
            defValue: Int = 0
        ): Int {
            if (UtilsClass == null) return defValue

            return try {
                UtilsClass.callStaticMethod(
                    methodName,
                    resID,
                    context
                ) as Int
            } catch (_: Throwable) {
                try {
                    UtilsClass.callStaticMethod(
                        methodName,
                        context,
                        resID
                    ) as Int
                } catch (_: Throwable) {
                    try {
                        UtilsClass.callStaticMethod(
                            methodName,
                            context,
                            resID,
                            defValue
                        ) as Int
                    } catch (_: Throwable) {
                        try {
                            UtilsClass.callStaticMethod(
                                methodName,
                                resID,
                                defValue,
                                context
                            ) as Int
                        } catch (throwable: Throwable) {
                            log(SettingsLibUtils, throwable)
                            defValue
                        }
                    }
                }
            }
        }

        private fun getColorStateListFromUtils(
            methodName: String,
            context: Context,
            resID: Int
        ): ColorStateList {
            if (UtilsClass == null) return ColorStateList.valueOf(0)

            return try {
                UtilsClass.callStaticMethod(
                    methodName,
                    resID,
                    context
                ) as ColorStateList
            } catch (_: Throwable) {
                try {
                    UtilsClass.callStaticMethod(
                        methodName,
                        context,
                        resID
                    ) as ColorStateList
                } catch (throwable: Throwable) {
                    log(SettingsLibUtils, throwable)
                    ColorStateList.valueOf(0)
                }
            }
        }
    }
}