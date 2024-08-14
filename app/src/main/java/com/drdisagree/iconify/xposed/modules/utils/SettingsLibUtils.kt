package com.drdisagree.iconify.xposed.modules.utils

import android.content.Context
import android.content.res.ColorStateList
import com.drdisagree.iconify.xposed.ModPack
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.XposedHelpers.callStaticMethod
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

class SettingsLibUtils(context: Context) : ModPack(context) {

    override fun updatePrefs(vararg key: String) {}

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        try {
            UtilsClass = XposedHelpers.findClass(
                "com.android.settingslib.Utils",
                loadPackageParam.classLoader
            )
        } catch (ignored: Throwable) {
        }
    }

    companion object {
        private val TAG = SettingsLibUtils::class.java.simpleName
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

        private fun getColorFromUtils(
            methodName: String,
            context: Context,
            resID: Int,
            defValue: Int = 0
        ): Int {
            if (UtilsClass == null) return defValue

            return try {
                callStaticMethod(
                    UtilsClass,
                    methodName,
                    resID,
                    context
                ) as Int
            } catch (ignored: Throwable) {
                try {
                    callStaticMethod(
                        UtilsClass,
                        methodName,
                        context,
                        resID
                    ) as Int
                } catch (ignored: Throwable) {
                    try {
                        callStaticMethod(
                            UtilsClass,
                            methodName,
                            context,
                            resID,
                            defValue
                        ) as Int
                    } catch (ignored: Throwable) {
                        try {
                            callStaticMethod(
                                UtilsClass,
                                methodName,
                                resID,
                                defValue,
                                context
                            ) as Int
                        } catch (throwable: Throwable) {
                            log(TAG + throwable)
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
                callStaticMethod(
                    UtilsClass,
                    methodName,
                    resID,
                    context
                ) as ColorStateList
            } catch (ignored: Throwable) {
                try {
                    callStaticMethod(
                        UtilsClass,
                        methodName,
                        context,
                        resID
                    ) as ColorStateList
                } catch (throwable: Throwable) {
                    log(TAG + throwable)
                    ColorStateList.valueOf(0)
                }
            }
        }
    }
}