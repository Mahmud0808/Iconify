package com.drdisagree.iconify.xposed.modules.statusbar

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.toColorInt
import com.drdisagree.iconify.data.common.Const
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.utils.StatusBarClock
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.toPx
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callStaticMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getStaticField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.log
import com.drdisagree.iconify.xposed.modules.extras.views.ChipDrawable
import com.drdisagree.iconify.xposed.utils.XPrefs
import de.robv.android.xposed.callbacks.XC_LoadPackage

@SuppressLint("DiscouragedApi")
class ClockChip(context: Context) : ModPack(context) {

    private var mShowSBClockBg = false
    private var statusBarClockColorOption = 0
    private var statusBarClockColorCode = Color.WHITE
    private var mClockView: View? = null
    private var mCenterClockView: View? = null
    private var mRightClockView: View? = null
    private var dependencyClass: Class<*>? = null
    private var darkIconDispatcherClass: Class<*>? = null
    private var fillColorOption: Int = 0
    private var startColor: Int = Color.RED
    private var endColor: Int = Color.BLUE
    private var gradientDirection: ChipDrawable.GradientDirection =
        ChipDrawable.GradientDirection.LEFT_RIGHT
    private var padding: IntArray = intArrayOf(8, 4, 8, 4)
    private var strokeEnabled: Boolean = false
    private var strokeWidth: Int = 2
    private var accentBorderEnabled: Boolean = true
    private var strokeColor: Int = Color.GREEN
    private var dashedBorderEnabled: Boolean = false
    private var strokeDashWidth: Int = 4
    private var strokeDashGap: Int = 4

    override fun updatePrefs(vararg key: String) {
        XPrefs.Xprefs.apply {
            mShowSBClockBg = getBoolean(XposedKey.STATUSBAR_CLOCK_CHIP)
            statusBarClockColorOption =
                getString(XposedKey.STATUSBAR_CLOCK_CHIP_TEXT_COLOR_OPTION).toInt()
            statusBarClockColorCode =
                getString(XposedKey.STATUSBAR_CLOCK_CHIP_TEXT_COLOR_CODE).toColorInt()
            fillColorOption =
                getString(XposedKey.STATUSBAR_CLOCK_CHIP_FILL_COLOR_OPTION).toInt()
            startColor =
                getString(XposedKey.STATUSBAR_CLOCK_CHIP_FILL_COLOR_GRADIENT_COLOR1).toColorInt()
            endColor =
                getString(XposedKey.STATUSBAR_CLOCK_CHIP_FILL_COLOR_GRADIENT_COLOR2).toColorInt()
            gradientDirection = ChipDrawable.GradientDirection.fromIndex(
                getInt(XposedKey.STATUSBAR_CLOCK_CHIP_FILL_COLOR_GRADIENT_DIRECTION)
            )
            padding = intArrayOf(
                getInt(XposedKey.STATUSBAR_CLOCK_CHIP_PADDING_LEFT),
                getInt(XposedKey.STATUSBAR_CLOCK_CHIP_PADDING_TOP),
                getInt(XposedKey.STATUSBAR_CLOCK_CHIP_PADDING_RIGHT),
                getInt(XposedKey.STATUSBAR_CLOCK_CHIP_PADDING_BOTTOM)
            )
            strokeEnabled = getBoolean(XposedKey.STATUSBAR_CLOCK_CHIP_BORDER)
            strokeWidth = getInt(XposedKey.STATUSBAR_CLOCK_CHIP_BORDER_THICKNESS)
            accentBorderEnabled =
                getString(XposedKey.STATUSBAR_CLOCK_CHIP_BORDER_COLOR_OPTION).toInt() == 0
            strokeColor = getString(XposedKey.STATUSBAR_CLOCK_CHIP_BORDER_COLOR_CODE).toColorInt()
            dashedBorderEnabled = getBoolean(XposedKey.STATUSBAR_CLOCK_CHIP_DASHED_BORDER)
            strokeDashWidth = getInt(XposedKey.STATUSBAR_CLOCK_CHIP_DASHED_BORDER_WIDTH)
            strokeDashGap = getInt(XposedKey.STATUSBAR_CLOCK_CHIP_DASHED_BORDER_GAP)
            cornerRadii = floatArrayOf(
                getFloat(XposedKey.STATUSBAR_CLOCK_CHIP_RADIUS_TOP_LEFT),
                getFloat(XposedKey.STATUSBAR_CLOCK_CHIP_RADIUS_TOP_LEFT),
                getFloat(XposedKey.STATUSBAR_CLOCK_CHIP_RADIUS_TOP_RIGHT),
                getFloat(XposedKey.STATUSBAR_CLOCK_CHIP_RADIUS_TOP_RIGHT),
                getFloat(XposedKey.STATUSBAR_CLOCK_CHIP_RADIUS_BOTTOM_RIGHT),
                getFloat(XposedKey.STATUSBAR_CLOCK_CHIP_RADIUS_BOTTOM_RIGHT),
                getFloat(XposedKey.STATUSBAR_CLOCK_CHIP_RADIUS_BOTTOM_LEFT),
                getFloat(XposedKey.STATUSBAR_CLOCK_CHIP_RADIUS_BOTTOM_LEFT),
            )
        }

        when (key.firstOrNull()) {
            in setOf(
                XposedKey.STATUSBAR_CLOCK_CHIP.name,
                XposedKey.STATUSBAR_CLOCK_CLICKABLE.name,
                XposedKey.STATUSBAR_CLOCK_CHIP_TEXT_COLOR_OPTION.name,
                XposedKey.STATUSBAR_CLOCK_CHIP_TEXT_COLOR_CODE.name,
                XposedKey.STATUSBAR_CLOCK_CHIP_FILL_COLOR_OPTION.name,
                XposedKey.STATUSBAR_CLOCK_CHIP_FILL_COLOR_GRADIENT_COLOR1.name,
                XposedKey.STATUSBAR_CLOCK_CHIP_FILL_COLOR_GRADIENT_COLOR2.name,
                XposedKey.STATUSBAR_CLOCK_CHIP_FILL_COLOR_GRADIENT_DIRECTION.name,
                XposedKey.STATUSBAR_CLOCK_CHIP_PADDING_LEFT.name,
                XposedKey.STATUSBAR_CLOCK_CHIP_PADDING_TOP.name,
                XposedKey.STATUSBAR_CLOCK_CHIP_PADDING_RIGHT.name,
                XposedKey.STATUSBAR_CLOCK_CHIP_PADDING_BOTTOM.name,
                XposedKey.STATUSBAR_CLOCK_CHIP_BORDER.name,
                XposedKey.STATUSBAR_CLOCK_CHIP_BORDER_THICKNESS.name,
                XposedKey.STATUSBAR_CLOCK_CHIP_BORDER_COLOR_OPTION.name,
                XposedKey.STATUSBAR_CLOCK_CHIP_BORDER_COLOR_CODE.name,
                XposedKey.STATUSBAR_CLOCK_CHIP_DASHED_BORDER.name,
                XposedKey.STATUSBAR_CLOCK_CHIP_DASHED_BORDER_WIDTH.name,
                XposedKey.STATUSBAR_CLOCK_CHIP_DASHED_BORDER_GAP.name,
                XposedKey.STATUSBAR_CLOCK_CHIP_RADIUS_TOP_LEFT.name,
                XposedKey.STATUSBAR_CLOCK_CHIP_RADIUS_TOP_RIGHT.name,
                XposedKey.STATUSBAR_CLOCK_CHIP_RADIUS_BOTTOM_RIGHT.name,
                XposedKey.STATUSBAR_CLOCK_CHIP_RADIUS_BOTTOM_LEFT.name
            ) -> updateStatusBarClock(true)
        }
    }

    override fun handleLoadPackage(loadPackageParam: XC_LoadPackage.LoadPackageParam) {
        statusBarClockChip()
    }

    private fun statusBarClockChip() {
        val phoneStatusBarViewControllerClass =
            findClass("com.android.systemui.statusbar.phone.PhoneStatusBarViewController")
        val shadeHeaderControllerClass =
            findClass("${Const.SYSTEMUI_PACKAGE}.shade.ShadeHeaderController")
        dependencyClass = findClass("${Const.SYSTEMUI_PACKAGE}.Dependency")
        darkIconDispatcherClass =
            findClass("${Const.SYSTEMUI_PACKAGE}.plugins.DarkIconDispatcher")

        val layoutChangeListener = View.OnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateStatusBarClock(false)
        }

        phoneStatusBarViewControllerClass
            .hookMethod("onViewAttached")
            .runAfter { param ->
                val mView = param.thisObject.getField("mView") as View
                mClockView = param.thisObject.getField("clock") as TextView
                mCenterClockView = null
                mRightClockView = null

                mView.addOnLayoutChangeListener(layoutChangeListener)

                updateStatusBarClock(true)

                if (mShowSBClockBg) {
                    try {
                        val statusBarStartSideContent =
                            mView.findViewById<FrameLayout>(
                                mContext.resources.getIdentifier(
                                    "status_bar_start_side_content",
                                    "id",
                                    mContext.packageName
                                )
                            )

                        statusBarStartSideContent.post {
                            statusBarStartSideContent.layoutParams.height =
                                FrameLayout.LayoutParams.MATCH_PARENT
                            statusBarStartSideContent.requestLayout()
                        }

                        val statusBarStartSideExceptHeadsUp =
                            mView.findViewById<LinearLayout>(
                                mContext.resources.getIdentifier(
                                    "status_bar_start_side_except_heads_up",
                                    "id",
                                    mContext.packageName
                                )
                            )

                        statusBarStartSideExceptHeadsUp.post {
                            (statusBarStartSideExceptHeadsUp.layoutParams as FrameLayout.LayoutParams).gravity =
                                Gravity.START or Gravity.CENTER
                        }

                        statusBarStartSideExceptHeadsUp.gravity =
                            Gravity.START or Gravity.CENTER
                        statusBarStartSideExceptHeadsUp.requestLayout()
                    } catch (throwable: Throwable) {
                        log(this@ClockChip, throwable)
                    }
                }
            }

        phoneStatusBarViewControllerClass
            .hookMethod("onViewAttached")
            .runAfter { param ->
                val mView = param.thisObject.getField("mView") as View

                mView.removeOnLayoutChangeListener(layoutChangeListener)
            }

        shadeHeaderControllerClass
            .hookMethod("updateQQSPaddings")
            .suppressError()
            .runAfter { updateStatusBarClock(true) }
    }

    @SuppressLint("RtlHardcoded")
    private fun updateStatusBarClock(force: Boolean) {
        if (!mShowSBClockBg) return

        if (mClockView != null && (mClockView!!.background == null || force)) {
            mClockView!!.post {
                updateClockView(
                    mClockView,
                    Gravity.LEFT or Gravity.CENTER
                )
            }
        }

        if (mCenterClockView != null && (mCenterClockView!!.background == null || force)) {
            mCenterClockView!!.post {
                updateClockView(
                    mCenterClockView,
                    Gravity.CENTER
                )
            }
        }

        if (mRightClockView != null && (mRightClockView!!.background == null || force)) {
            mRightClockView!!.post {
                updateClockView(
                    mRightClockView,
                    Gravity.RIGHT or Gravity.CENTER
                )
            }
        }
    }

    private fun setSBClockBackgroundChip(view: View) {
        if (mShowSBClockBg) {
            view.background = ChipDrawable.createChipDrawable(
                context = mContext,
                fillColorOption = fillColorOption,
                startColor = startColor,
                endColor = endColor,
                gradientDirection = gradientDirection,
                padding = intArrayOf(0, 0, 0, 0),
                strokeEnabled = strokeEnabled,
                accentStroke = accentBorderEnabled,
                strokeWidth = strokeWidth,
                strokeColor = strokeColor,
                dashedBorderEnabled = dashedBorderEnabled,
                dashWidth = strokeDashWidth,
                dashGap = strokeDashGap,
                cornerRadii = cornerRadii
            )
        } else {
            view.background = null
        }
    }

    private fun updateClockView(clockView: View?, gravity: Int) {
        if (clockView == null) return

        clockView.setPadding(
            mContext.toPx(padding[0]),
            mContext.toPx(padding[1]),
            mContext.toPx(padding[2]),
            mContext.toPx(padding[3])
        )

        setSBClockBackgroundChip(clockView)

        when (statusBarClockColorOption) {
            0 -> {
                (clockView as TextView).paint.xfermode = null
                try {
                    dependencyClass
                        .callStaticMethod("get", darkIconDispatcherClass)
                        .callMethod("addDarkReceiver", clockView)
                } catch (_: Throwable) {
                    dependencyClass
                        .getStaticField("sDependency")
                        .callMethod("getDependencyInner", darkIconDispatcherClass)
                        .callMethod("addDarkReceiver", clockView)
                }
            }

            1 -> {
                (clockView as TextView).paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
            }

            2 -> {
                (clockView as TextView).paint.xfermode = null
                try {
                    dependencyClass
                        .callStaticMethod("get", darkIconDispatcherClass)
                        .callMethod("removeDarkReceiver", clockView)
                } catch (_: Throwable) {
                    dependencyClass
                        .getStaticField("sDependency")
                        .callMethod("getDependencyInner", darkIconDispatcherClass)
                        .callMethod("removeDarkReceiver", clockView)
                }
                clockView.setTextColor(statusBarClockColorCode)
            }
        }

        StatusBarClock.setClockGravity(clockView, gravity)
        StatusbarMisc.Companion.setClockChipClickable(mContext, clockView, cornerRadii)
    }

    companion object {
        var cornerRadii: FloatArray = floatArrayOf(28f, 28f, 28f, 28f, 28f, 28f, 28f, 28f)
    }
}