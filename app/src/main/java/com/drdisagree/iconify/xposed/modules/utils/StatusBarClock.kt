package com.drdisagree.iconify.xposed.modules.utils

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.getObjectField

object StatusBarClock {

    private val TAG = "Iconify - ${StatusBarClock::class.java.simpleName}: "

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

    @SuppressLint("DiscouragedApi")
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

    @SuppressLint("RtlHardcoded")
    fun setClockGravity(clockView: View?, gravity: Int) {
        if (clockView == null) return

        val layoutParams = clockView.layoutParams
        when (layoutParams) {
            is LinearLayout.LayoutParams,  -> {
                layoutParams.gravity = gravity
            }

            is FrameLayout.LayoutParams -> {
                layoutParams.gravity = gravity
            }

            is RelativeLayout.LayoutParams -> {
                when (gravity) {
                    Gravity.LEFT or Gravity.CENTER -> {
                        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL)
                        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
                    }

                    Gravity.CENTER -> {
                        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT)
                    }

                    Gravity.RIGHT or Gravity.CENTER -> {
                        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL)
                        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                    }

                    else -> {
                        Log.w(
                            "${TAG} LayoutParamsCheck",
                            "Unsupported gravity type for RelativeLayout: $gravity"
                        )
                    }
                }
            }

            else -> {
                Log.w(
                    "${TAG} LayoutParamsCheck",
                    "Unknown LayoutParams type: ${layoutParams.javaClass.name}"
                )
            }
        }
        clockView.layoutParams = layoutParams

        (clockView as TextView).includeFontPadding = false
        clockView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT
        clockView.setGravity(Gravity.CENTER)
        clockView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER)
        clockView.requestLayout()
    }
}