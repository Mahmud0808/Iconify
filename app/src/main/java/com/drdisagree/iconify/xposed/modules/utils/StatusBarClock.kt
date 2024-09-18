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

    fun getLeftClockView(mContext: Context, param: XC_MethodHook.MethodHookParam) = try {
        getObjectField(param.thisObject, "mClockView") as View
    } catch (throwable1: Throwable) {
        try {
            getObjectField(param.thisObject, "mLeftClock") as View
        } catch (throwable2: Throwable) {
            try {
                callMethod(
                    getObjectField(
                        param.thisObject,
                        "mClockController"
                    ),
                    "getClock"
                ) as View
            } catch (throwable3: Throwable) {
                try {
                    val mActiveClock = getObjectField(
                        getObjectField(
                            param.thisObject,
                            "mClockController"
                        ),
                        "mActiveClock"
                    ) as View
                    val mLeftClockId = mContext.resources.getIdentifier(
                        "clock",
                        "id",
                        mContext.packageName
                    )

                    if (mActiveClock.id == mLeftClockId) {
                        mActiveClock
                    } else {
                        null
                    }
                } catch (throwable4: Throwable) {
                    log(TAG + throwable4)
                    null
                }
            }
        }
    }

    fun getCenterClockView(mContext: Context, param: XC_MethodHook.MethodHookParam) = try {
        getObjectField(param.thisObject, "mCenterClockView") as View
    } catch (throwable1: Throwable) {
        try {
            getObjectField(
                param.thisObject,
                "mCenterClock"
            ) as View
        } catch (throwable2: Throwable) {
            try {
                callMethod(
                    getObjectField(
                        param.thisObject,
                        "mClockController"
                    ),
                    "mCenterClockView"
                ) as View
            } catch (throwable3: Throwable) {
                try {
                    val mActiveClock = getObjectField(
                        getObjectField(
                            param.thisObject,
                            "mClockController"
                        ),
                        "mActiveClock"
                    ) as View
                    val mCenterClockId = mContext.resources.getIdentifier(
                        "clock_center",
                        "id",
                        mContext.packageName
                    )

                    if (mActiveClock.id == mCenterClockId) {
                        mActiveClock
                    } else {
                        null
                    }
                } catch (throwable4: Throwable) {
                    try {
                        (getObjectField(
                            param.thisObject,
                            "mCenterClockLayout"
                        ) as LinearLayout).getChildAt(0)
                    } catch (throwable5: Throwable) {
                        null
                    }
                }
            }
        }
    }

    fun getRightClockView(mContext: Context, param: XC_MethodHook.MethodHookParam) = try {
        getObjectField(param.thisObject, "mRightClockView") as View
    } catch (throwable1: Throwable) {
        try {
            getObjectField(param.thisObject, "mRightClock") as View
        } catch (throwable2: Throwable) {
            try {
                callMethod(
                    getObjectField(
                        param.thisObject,
                        "mClockController"
                    ),
                    "mRightClockView"
                ) as View
            } catch (throwable3: Throwable) {
                try {
                    val mActiveClock = getObjectField(
                        getObjectField(
                            param.thisObject,
                            "mClockController"
                        ),
                        "mActiveClock"
                    ) as View
                    val mRightClockId = mContext.resources.getIdentifier(
                        "clock_right",
                        "id",
                        mContext.packageName
                    )

                    if (mActiveClock.id == mRightClockId) {
                        mActiveClock
                    } else {
                        null
                    }
                } catch (throwable4: Throwable) {
                    null
                }
            }
        }
    }

    @SuppressLint("RtlHardcoded")
    fun setClockGravity(clockView: View?, gravity: Int) {
        if (clockView == null) return

        val layoutParams = clockView.layoutParams
        when (layoutParams) {
            is LinearLayout.LayoutParams -> {
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
                            "$TAG LayoutParamsCheck",
                            "Unsupported gravity type for RelativeLayout: $gravity"
                        )
                    }
                }
            }

            else -> {
                Log.w(
                    "$TAG LayoutParamsCheck",
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