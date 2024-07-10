package com.drdisagree.iconify.xposed.utils

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.findClass
import de.robv.android.xposed.XposedHelpers.getObjectField
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

object XposedIcHelper {

    private val TAG = "Iconify - ${XposedIcHelper::class.java.simpleName}: "

    fun findClassInArray(lpparam: LoadPackageParam, vararg classNames: String): Class<*>? {
        for (className in classNames) {
            try {
                val clazz = findClass(className, lpparam.classLoader)
                return clazz
            } catch (ignored: Throwable) {
            }
        }
        return null
    }

    fun getLeftClockView(mContext: Context, param: XC_MethodHook.MethodHookParam): View? {
        return getClockView(
            mContext,
            param,
            listOf("mClockView", "mLeftClock"),
            "getClock",
            "clock"
        )
    }

    fun getCenterClockView(mContext: Context, param: XC_MethodHook.MethodHookParam): View? {
        return getClockView(
            mContext,
            param,
            listOf("mCenterClockView", "mCenterClock"),
            "getCenterClock",
            "clock_center"
        ) ?: try {
            (getObjectField(param.thisObject, "mCenterClockLayout") as LinearLayout).getChildAt(0)
        } catch (ignored: Throwable) {
            null
        }
    }

    fun getRightClockView(mContext: Context, param: XC_MethodHook.MethodHookParam): View? {
        return getClockView(
            mContext,
            param,
            listOf("mRightClockView", "mRightClock"),
            "getRightClock",
            "clock_right"
        )
    }

    private fun getClockView(
        mContext: Context,
        param: XC_MethodHook.MethodHookParam,
        fieldNames: List<String>,
        methodName: String,
        resourceIdName: String
    ): View? {
        for (fieldName in fieldNames) {
            try {
                return getObjectField(param.thisObject, fieldName) as View
            } catch (ignored: Throwable) {
            }
        }

        try {
            return callMethod(getObjectField(param.thisObject, "mClockController"), methodName) as View
        } catch (ignored: Throwable) {
        }

        return try {
            val mActiveClock = getObjectField(getObjectField(param.thisObject, "mClockController"), "mActiveClock") as View
            val mClockId = mContext.resources.getIdentifier(resourceIdName, "id", mContext.packageName)
            if (mActiveClock.id == mClockId) mActiveClock else null
        } catch (throwable: Throwable) {
            log(TAG + throwable)
            null
        }
    }

}