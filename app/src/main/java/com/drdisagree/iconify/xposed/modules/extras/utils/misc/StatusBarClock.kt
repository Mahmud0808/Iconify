package com.drdisagree.iconify.xposed.modules.extras.utils.misc

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.drdisagree.iconify.xposed.core.log.log
import com.drdisagree.iconify.xposed.core.reflect.callMethod
import com.drdisagree.iconify.xposed.core.reflect.getField

@SuppressLint("DiscouragedApi")
object StatusBarClock {

    fun getLeftClockView(mContext: Context, statusBar: Any?) = try {
        statusBar.getField("mClockView") as View
    } catch (_: Throwable) {
        try {
            statusBar.getField("mLeftClock") as View
        } catch (_: Throwable) {
            try {
                statusBar
                    .getField("mClockController")
                    .callMethod("getClock") as View
            } catch (_: Throwable) {
                try {
                    val mActiveClock = statusBar
                        .getField("mClockController")
                        .getField("mActiveClock") as View
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
                } catch (_: Throwable) {
                    try {
                        (statusBar.getField(
                            "mStatusBar"
                        ) as ViewGroup).findViewById(
                            mContext.resources.getIdentifier(
                                "clock",
                                "id",
                                mContext.packageName
                            )
                        )
                    } catch (_: Throwable) {
                        null
                    }
                }
            }
        }
    }

    fun getCenterClockView(mContext: Context, statusBar: Any?) = try {
        statusBar.getField("mCenterClockView") as View
    } catch (_: Throwable) {
        try {
            statusBar.getField("mCenterClock") as View
        } catch (_: Throwable) {
            try {
                statusBar
                    .getField("mClockController")
                    .callMethod("mCenterClockView") as View
            } catch (_: Throwable) {
                try {
                    val mActiveClock = statusBar
                        .getField("mClockController")
                        .getField("mActiveClock") as View
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
                } catch (_: Throwable) {
                    try {
                        (statusBar.getField(
                            "mCenterClockLayout"
                        ) as LinearLayout).getChildAt(0)
                    } catch (_: Throwable) {
                        null
                    }
                }
            }
        }
    }

    fun getRightClockView(mContext: Context, statusBar: Any?) = try {
        statusBar.getField("mRightClockView") as View
    } catch (_: Throwable) {
        try {
            statusBar.getField("mRightClock") as View
        } catch (_: Throwable) {
            try {
                statusBar
                    .getField("mClockController")
                    .callMethod("mRightClockView") as View
            } catch (_: Throwable) {
                try {
                    val mActiveClock = statusBar
                        .getField("mClockController")
                        .getField("mActiveClock") as View
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
                } catch (_: Throwable) {
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
                        log(
                            this@StatusBarClock,
                            "LayoutParamsCheck - Unsupported gravity type for RelativeLayout: $gravity"
                        )
                    }
                }
            }

            else -> {
                log(
                    this@StatusBarClock,
                    "LayoutParamsCheck - Unknown LayoutParams type: ${layoutParams.javaClass.name}"
                )
            }
        }

        clockView.apply {
            this.layoutParams = layoutParams
            (this as TextView).includeFontPadding = false
            this.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            this.gravity = Gravity.CENTER
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            requestLayout()
        }
    }
}