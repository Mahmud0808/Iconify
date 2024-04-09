package com.drdisagree.iconify.xposed.modules.utils

import android.content.Context
import android.content.res.ColorStateList
import com.drdisagree.iconify.xposed.ModPack
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

class SettingsLibUtils(context: Context?) : ModPack(context!!) {

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
        private var UtilsClass: Class<*>? = null

        fun getColorAttr(context: Context?, resID: Int): ColorStateList {
            return getColorAttr(resID, context)
        }

        fun getColorAttr(resID: Int, context: Context?): ColorStateList {
            return if (UtilsClass == null) ColorStateList.valueOf(0) else try {
                XposedHelpers.callStaticMethod(
                    UtilsClass,
                    "getColorAttr",
                    resID,
                    context
                ) as ColorStateList
            } catch (throwable: Throwable) {
                XposedHelpers.callStaticMethod(
                    UtilsClass,
                    "getColorAttr",
                    context,
                    resID
                ) as ColorStateList
            }
        }

        fun getColorAttrDefaultColor(resID: Int, context: Context?): Int {
            return getColorAttrDefaultColor(context, resID)
        }

        fun getColorAttrDefaultColor(context: Context?, resID: Int): Int {
            return getColorAttrDefaultColor(resID, context, 0)
        }

        fun getColorAttrDefaultColor(resID: Int, context: Context?, defValue: Int): Int {
            return if (UtilsClass == null) 0 else try {
                XposedHelpers.callStaticMethod(
                    UtilsClass,
                    "getColorAttrDefaultColor",
                    resID,
                    context
                ) as Int
            } catch (throwable: Throwable) {
                try {
                    XposedHelpers.callStaticMethod(
                        UtilsClass,
                        "getColorAttrDefaultColor",
                        context,
                        resID
                    ) as Int
                } catch (throwable1: Throwable) {
                    XposedHelpers.callStaticMethod(
                        UtilsClass,
                        "getColorAttrDefaultColor",
                        context,
                        resID,
                        defValue
                    ) as Int
                }
            }
        }

        fun getColorStateListDefaultColor(context: Context?, resID: Int): Int {
            return if (UtilsClass == null) 0 else try {
                (XposedHelpers.callStaticMethod(
                    UtilsClass,
                    "getColorStateListDefaultColor",
                    context,
                    resID
                ) as ColorStateList).defaultColor
            } catch (throwable: Throwable) {
                try {
                    (XposedHelpers.callStaticMethod(
                        UtilsClass,
                        "getColorStateListDefaultColor",
                        resID,
                        context
                    ) as ColorStateList).defaultColor
                } catch (throwable1: Throwable) {
                    0
                }
            }
        }
    }
}