package com.drdisagree.iconify.xposed.modules

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.StateListAnimator
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.content.res.XResources
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.os.Bundle
import android.provider.AlarmClock
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUSBAR_CLOCK_ACCENT
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUSBAR_CLOCK_END_COLOR
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUSBAR_CLOCK_GRADIENT_DIRECTION
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUSBAR_CLOCK_PADDING_BOTTOM
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUSBAR_CLOCK_PADDING_LEFT
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUSBAR_CLOCK_PADDING_RIGHT
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUSBAR_CLOCK_PADDING_TOP
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUSBAR_CLOCK_RADIUS_BOTTOM_LEFT
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUSBAR_CLOCK_RADIUS_BOTTOM_RIGHT
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUSBAR_CLOCK_RADIUS_TOP_LEFT
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUSBAR_CLOCK_RADIUS_TOP_RIGHT
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUSBAR_CLOCK_START_COLOR
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUSBAR_CLOCK_STROKE_ACCENT
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUSBAR_CLOCK_STROKE_COLOR
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUSBAR_CLOCK_STROKE_DASH
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUSBAR_CLOCK_STROKE_DASH_GAP
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUSBAR_CLOCK_STROKE_DASH_WIDTH
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUSBAR_CLOCK_STROKE_SWITCH
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUSBAR_CLOCK_STROKE_WIDTH
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUSBAR_CLOCK_STYLE_CHANGED
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUSBAR_CLOCK_SWITCH
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUSBAR_CLOCK_TEXT_COLOR_CODE
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUSBAR_CLOCK_TEXT_COLOR_OPTION
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUS_ICONS_ACCENT
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUS_ICONS_END_COLOR
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUS_ICONS_GRADIENT_DIRECTION
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUS_ICONS_PADDING_BOTTOM
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUS_ICONS_PADDING_LEFT
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUS_ICONS_PADDING_RIGHT
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUS_ICONS_PADDING_TOP
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUS_ICONS_RADIUS_BOTTOM_LEFT
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUS_ICONS_RADIUS_BOTTOM_RIGHT
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUS_ICONS_RADIUS_TOP_LEFT
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUS_ICONS_RADIUS_TOP_RIGHT
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUS_ICONS_START_COLOR
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUS_ICONS_STROKE_ACCENT
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUS_ICONS_STROKE_COLOR
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUS_ICONS_STROKE_DASH
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUS_ICONS_STROKE_DASH_GAP
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUS_ICONS_STROKE_DASH_WIDTH
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUS_ICONS_STROKE_SWITCH
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUS_ICONS_STROKE_WIDTH
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUS_ICONS_STYLE_CHANGED
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUS_ICONS_SWITCH
import com.drdisagree.iconify.data.common.Preferences.FIXED_STATUS_ICONS_SIDEMARGIN
import com.drdisagree.iconify.data.common.Preferences.FIXED_STATUS_ICONS_SWITCH
import com.drdisagree.iconify.data.common.Preferences.FIXED_STATUS_ICONS_TOPMARGIN
import com.drdisagree.iconify.data.common.Preferences.HEADER_CLOCK_SWITCH
import com.drdisagree.iconify.data.common.Preferences.HIDE_STATUS_ICONS_SWITCH
import com.drdisagree.iconify.data.common.Preferences.SB_CLOCK_CLICKABLE_SWITCH
import com.drdisagree.iconify.xposed.HookRes.Companion.resParams
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.utils.MyConstraintSet.Companion.applyTo
import com.drdisagree.iconify.xposed.modules.extras.utils.MyConstraintSet.Companion.clone
import com.drdisagree.iconify.xposed.modules.extras.utils.MyConstraintSet.Companion.connect
import com.drdisagree.iconify.xposed.modules.extras.utils.MyConstraintSet.Companion.constraintSetInstance
import com.drdisagree.iconify.xposed.modules.extras.utils.StatusBarClock
import com.drdisagree.iconify.xposed.modules.extras.utils.StatusBarClock.setClockGravity
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.toPx
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callStaticMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getStaticField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookLayout
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.log
import com.drdisagree.iconify.xposed.modules.extras.views.ChipDrawable
import com.drdisagree.iconify.xposed.modules.extras.views.ChipDrawable.GradientDirection.Companion.toIndex
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

@SuppressLint("DiscouragedApi")
class BackgroundChip(context: Context) : ModPack(context) {

    private val mQsStatusIconsContainer = LinearLayout(mContext)
    private var mShowSBClockBg = false
    private var hideStatusIcons = false
    private var mShowQSStatusIconsBg = false
    private var showHeaderClock = false
    private var topMarginStatusIcons = 8
    private var sideMarginStatusIcons = 0
    private var statusBarClockColorOption = 0
    private var statusBarClockColorCode = Color.WHITE
    private var fixedStatusIcons = false
    private var constraintLayoutId = -1
    private var header: ViewGroup? = null
    private var mClockView: View? = null
    private var mCenterClockView: View? = null
    private var mRightClockView: View? = null
    private var dependencyClass: Class<*>? = null
    private var darkIconDispatcherClass: Class<*>? = null
    private var accentFillEnabled: Boolean = true
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
    private var cornerRadii: FloatArray = floatArrayOf(28f, 28f, 28f, 28f, 28f, 28f, 28f, 28f)
    private var accentFillEnabled2: Boolean = true
    private var startColor2: Int = Color.RED
    private var endColor2: Int = Color.BLUE
    private var gradientDirection2: ChipDrawable.GradientDirection =
        ChipDrawable.GradientDirection.LEFT_RIGHT
    private var padding2: IntArray = intArrayOf(8, 4, 8, 4)
    private var strokeEnabled2: Boolean = false
    private var strokeWidth2: Int = 2
    private var accentBorderEnabled2: Boolean = true
    private var strokeColor2: Int = Color.GREEN
    private var dashedBorderEnabled2: Boolean = false
    private var strokeDashWidth2: Int = 4
    private var strokeDashGap2: Int = 4
    private var cornerRadii2: FloatArray = floatArrayOf(28f, 28f, 28f, 28f, 28f, 28f, 28f, 28f)
    private var mShowStatusBarClockBg = false
    private var mShowStatusIconsBg = false
    private var mDarkModeEnabled = false
    private var mForceMonetBg = false
    private var mRoundAllCorners = false
    private var mClockClickable = true
    private var statusBarClockStyleChanged = false

    override fun updatePrefs(vararg key: String) {
        Xprefs.apply {
            // Status bar clock chip
            mShowSBClockBg = getBoolean(CHIP_STATUSBAR_CLOCK_SWITCH, false)
            statusBarClockStyleChanged = getBoolean(CHIP_STATUSBAR_CLOCK_STYLE_CHANGED, false)
            statusBarClockColorOption = getInt(CHIP_STATUSBAR_CLOCK_TEXT_COLOR_OPTION, 0)
            statusBarClockColorCode = getInt(CHIP_STATUSBAR_CLOCK_TEXT_COLOR_CODE, Color.WHITE)
            accentFillEnabled = getBoolean(CHIP_STATUSBAR_CLOCK_ACCENT, true)
            startColor = getInt(CHIP_STATUSBAR_CLOCK_START_COLOR, Color.RED)
            endColor = getInt(CHIP_STATUSBAR_CLOCK_END_COLOR, Color.BLUE)
            gradientDirection = if (mRoundAllCorners) {
                ChipDrawable.GradientDirection.fromIndex(0)
            } else {
                ChipDrawable.GradientDirection.fromIndex(
                    getInt(
                        CHIP_STATUSBAR_CLOCK_GRADIENT_DIRECTION,
                        ChipDrawable.GradientDirection.LEFT_RIGHT.toIndex()
                    )
                )
            }
            padding = intArrayOf(
                getInt(CHIP_STATUSBAR_CLOCK_PADDING_LEFT, 8),
                getInt(CHIP_STATUSBAR_CLOCK_PADDING_TOP, 4),
                getInt(CHIP_STATUSBAR_CLOCK_PADDING_RIGHT, 8),
                getInt(CHIP_STATUSBAR_CLOCK_PADDING_BOTTOM, 4)
            )
            strokeEnabled = getBoolean(CHIP_STATUSBAR_CLOCK_STROKE_SWITCH)
            strokeWidth = getInt(CHIP_STATUSBAR_CLOCK_STROKE_WIDTH, 2)
            accentBorderEnabled = getBoolean(CHIP_STATUSBAR_CLOCK_STROKE_ACCENT, true)
            strokeColor = getInt(CHIP_STATUSBAR_CLOCK_STROKE_COLOR, Color.GREEN)
            dashedBorderEnabled = getBoolean(CHIP_STATUSBAR_CLOCK_STROKE_DASH)
            strokeDashWidth = getInt(CHIP_STATUSBAR_CLOCK_STROKE_DASH_WIDTH, 4)
            strokeDashGap = getInt(CHIP_STATUSBAR_CLOCK_STROKE_DASH_GAP, 4)
            cornerRadii = floatArrayOf(
                getInt(CHIP_STATUSBAR_CLOCK_RADIUS_TOP_LEFT, 28).toFloat(),
                getInt(CHIP_STATUSBAR_CLOCK_RADIUS_TOP_LEFT, 28).toFloat(),
                getInt(CHIP_STATUSBAR_CLOCK_RADIUS_TOP_RIGHT, 28).toFloat(),
                getInt(CHIP_STATUSBAR_CLOCK_RADIUS_TOP_RIGHT, 28).toFloat(),
                getInt(CHIP_STATUSBAR_CLOCK_RADIUS_BOTTOM_RIGHT, 28).toFloat(),
                getInt(CHIP_STATUSBAR_CLOCK_RADIUS_BOTTOM_RIGHT, 28).toFloat(),
                getInt(CHIP_STATUSBAR_CLOCK_RADIUS_BOTTOM_LEFT, 28).toFloat(),
                getInt(CHIP_STATUSBAR_CLOCK_RADIUS_BOTTOM_LEFT, 28).toFloat(),
            )

            // Status icons chip
            mShowQSStatusIconsBg = getBoolean(CHIP_STATUS_ICONS_SWITCH, false)
            accentFillEnabled2 = getBoolean(CHIP_STATUS_ICONS_ACCENT, true)
            startColor2 = getInt(CHIP_STATUS_ICONS_START_COLOR, Color.RED)
            endColor2 = getInt(CHIP_STATUS_ICONS_END_COLOR, Color.BLUE)
            gradientDirection2 =
                ChipDrawable.GradientDirection.fromIndex(
                    getInt(
                        CHIP_STATUS_ICONS_GRADIENT_DIRECTION,
                        ChipDrawable.GradientDirection.LEFT_RIGHT.toIndex()
                    )
                )
            padding2 = intArrayOf(
                getInt(CHIP_STATUS_ICONS_PADDING_LEFT, 8),
                getInt(CHIP_STATUS_ICONS_PADDING_TOP, 4),
                getInt(CHIP_STATUS_ICONS_PADDING_RIGHT, 8),
                getInt(CHIP_STATUS_ICONS_PADDING_BOTTOM, 4)
            )
            strokeEnabled2 = getBoolean(CHIP_STATUS_ICONS_STROKE_SWITCH)
            strokeWidth2 = getInt(CHIP_STATUS_ICONS_STROKE_WIDTH, 2)
            accentBorderEnabled2 = getBoolean(CHIP_STATUS_ICONS_STROKE_ACCENT, true)
            strokeColor2 = getInt(CHIP_STATUS_ICONS_STROKE_COLOR, Color.GREEN)
            dashedBorderEnabled2 = getBoolean(CHIP_STATUS_ICONS_STROKE_DASH)
            strokeDashWidth2 = getInt(CHIP_STATUS_ICONS_STROKE_DASH_WIDTH, 4)
            strokeDashGap2 = getInt(CHIP_STATUS_ICONS_STROKE_DASH_GAP, 4)
            cornerRadii2 = floatArrayOf(
                getInt(CHIP_STATUS_ICONS_RADIUS_TOP_LEFT, 28).toFloat(),
                getInt(CHIP_STATUS_ICONS_RADIUS_TOP_LEFT, 28).toFloat(),
                getInt(CHIP_STATUS_ICONS_RADIUS_TOP_RIGHT, 28).toFloat(),
                getInt(CHIP_STATUS_ICONS_RADIUS_TOP_RIGHT, 28).toFloat(),
                getInt(CHIP_STATUS_ICONS_RADIUS_BOTTOM_RIGHT, 28).toFloat(),
                getInt(CHIP_STATUS_ICONS_RADIUS_BOTTOM_RIGHT, 28).toFloat(),
                getInt(CHIP_STATUS_ICONS_RADIUS_BOTTOM_LEFT, 28).toFloat(),
                getInt(CHIP_STATUS_ICONS_RADIUS_BOTTOM_LEFT, 28).toFloat(),
            )

            // Others
            showHeaderClock = getBoolean(HEADER_CLOCK_SWITCH, false)
            hideStatusIcons = getBoolean(HIDE_STATUS_ICONS_SWITCH, false)
            fixedStatusIcons = getBoolean(FIXED_STATUS_ICONS_SWITCH, false)
            topMarginStatusIcons = getSliderInt(FIXED_STATUS_ICONS_TOPMARGIN, 8)
            sideMarginStatusIcons = getSliderInt(FIXED_STATUS_ICONS_SIDEMARGIN, 0)
            mClockClickable = getBoolean(SB_CLOCK_CLICKABLE_SWITCH, true)
        }

        if (key.isNotEmpty()) {
            if (key[0] == CHIP_STATUSBAR_CLOCK_SWITCH ||
                key[0] == CHIP_STATUSBAR_CLOCK_STYLE_CHANGED
            ) {
                updateStatusBarClock(true)
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                if (key[0] == CHIP_STATUS_ICONS_SWITCH ||
                    key[0] == CHIP_STATUS_ICONS_STYLE_CHANGED ||
                    key[0] == HEADER_CLOCK_SWITCH ||
                    key[0] == HIDE_STATUS_ICONS_SWITCH ||
                    key[0] == FIXED_STATUS_ICONS_SWITCH
                ) {
                    setQSStatusIconsBgA12()
                }

                if (key[0] == CHIP_STATUS_ICONS_SWITCH ||
                    key[0] == CHIP_STATUS_ICONS_STYLE_CHANGED ||
                    key[0] == FIXED_STATUS_ICONS_TOPMARGIN ||
                    key[0] == FIXED_STATUS_ICONS_SIDEMARGIN
                ) {
                    updateStatusIcons()
                }
            }
        }
    }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        statusBarClockChip()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            statusIconsChip()
        }
    }

    private fun statusBarClockChip() {
        val collapsedStatusBarFragment = findClass(
            "$SYSTEMUI_PACKAGE.statusbar.phone.CollapsedStatusBarFragment",
            "$SYSTEMUI_PACKAGE.statusbar.phone.fragment.CollapsedStatusBarFragment"
        )
        val shadeHeaderControllerClass =
            findClass("$SYSTEMUI_PACKAGE.shade.ShadeHeaderController")
        dependencyClass = findClass("$SYSTEMUI_PACKAGE.Dependency")
        darkIconDispatcherClass = findClass("$SYSTEMUI_PACKAGE.plugins.DarkIconDispatcher")

        collapsedStatusBarFragment
            .hookMethod("onViewCreated")
            .parameters(View::class.java, Bundle::class.java)
            .runAfter { param ->
                mClockView = StatusBarClock.getLeftClockView(mContext, param)
                mCenterClockView = StatusBarClock.getCenterClockView(mContext, param)
                mRightClockView = StatusBarClock.getRightClockView(mContext, param)

                (param.thisObject.getField(
                    "mStatusBar"
                ) as ViewGroup).addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                    updateStatusBarClock(false)
                }

                updateStatusBarClock(true)

                if (mShowSBClockBg) {
                    try {
                        val mStatusBar = param.thisObject.getField("mStatusBar") as FrameLayout

                        val statusBarStartSideContent =
                            mStatusBar.findViewById<FrameLayout>(
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
                            mStatusBar.findViewById<LinearLayout>(
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
                        log(this@BackgroundChip, throwable)
                    }
                }
            }

        shadeHeaderControllerClass
            .hookMethod("updateQQSPaddings")
            .suppressError()
            .runAfter { updateStatusBarClock(true) }
    }

    private fun statusIconsChip() {
        setQSStatusIconsBgA12()
        setQSStatusIconsBgA13Plus()
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

    private fun updateStatusIcons() {
        if (mQsStatusIconsContainer.childCount == 0) return

        if (mShowQSStatusIconsBg) {
            setStatusIconsBackgroundChip(mQsStatusIconsContainer)
            mQsStatusIconsContainer.setPadding(
                mContext.toPx(padding2[0]),
                mContext.toPx(padding2[1]),
                mContext.toPx(padding2[2]),
                mContext.toPx(padding2[3])
            )
        }

        if (mQsStatusIconsContainer.layoutParams is FrameLayout.LayoutParams) {
            mQsStatusIconsContainer.post {
                (mQsStatusIconsContainer.layoutParams as FrameLayout.LayoutParams).setMargins(
                    0,
                    mContext.toPx(topMarginStatusIcons),
                    0,
                    0
                )

                (mQsStatusIconsContainer.layoutParams as FrameLayout.LayoutParams).marginEnd =
                    mContext.toPx(sideMarginStatusIcons)
            }
        } else if (mQsStatusIconsContainer.layoutParams is LinearLayout.LayoutParams) {
            mQsStatusIconsContainer.post {
                (mQsStatusIconsContainer.layoutParams as LinearLayout.LayoutParams).setMargins(
                    0,
                    mContext.toPx(topMarginStatusIcons),
                    0,
                    0
                )

                (mQsStatusIconsContainer.layoutParams as LinearLayout.LayoutParams).marginEnd =
                    mContext.toPx(sideMarginStatusIcons)
            }
        } else if (header != null && constraintLayoutId != -1) {
            try {
                constraintSetInstance?.apply {
                    clone(header!!)
                    connect(
                        constraintLayoutId,
                        ConstraintSet.TOP,
                        ConstraintSet.PARENT_ID,
                        ConstraintSet.TOP,
                        0
                    )
                    connect(
                        constraintLayoutId,
                        ConstraintSet.END,
                        ConstraintSet.PARENT_ID,
                        ConstraintSet.END,
                        0
                    )
                    applyTo(header!!)
                }

                mQsStatusIconsContainer
                    .callMethod("getLayoutParams")
                    .callMethod(
                        "setMargins",
                        0,
                        mContext.toPx(topMarginStatusIcons),
                        0,
                        0
                    )

                mQsStatusIconsContainer
                    .callMethod("getLayoutParams")
                    .callMethod(
                    "setMarginEnd",
                    mContext.toPx(sideMarginStatusIcons)
                )
            } catch (throwable: Throwable) {
                log(this@BackgroundChip, throwable)
            }
        }

        mQsStatusIconsContainer.requestLayout()

        val config = mContext.resources.configuration

        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mQsStatusIconsContainer.visibility = View.GONE
        } else {
            mQsStatusIconsContainer.visibility = View.VISIBLE
        }
    }

    private fun setSBClockBackgroundChip(view: View) {
        if (mShowSBClockBg) {
            view.background = ChipDrawable.createChipDrawable(
                context = mContext,
                accentFill = accentFillEnabled,
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

    private fun setStatusIconsBackgroundChip(layout: LinearLayout) {
        if (mShowQSStatusIconsBg) {
            layout.background = ChipDrawable.createChipDrawable(
                context = mContext,
                accentFill = accentFillEnabled2,
                startColor = startColor2,
                endColor = endColor2,
                gradientDirection = gradientDirection2,
                padding = intArrayOf(0, 0, 0, 0),
                strokeEnabled = strokeEnabled2,
                accentStroke = accentBorderEnabled2,
                strokeWidth = strokeWidth2,
                strokeColor = strokeColor2,
                dashedBorderEnabled = dashedBorderEnabled2,
                dashWidth = strokeDashWidth2,
                dashGap = strokeDashGap2,
                cornerRadii = cornerRadii2
            )
        } else {
            layout.background = null
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
                (clockView as TextView).paint.setXfermode(null)
                try {
                    dependencyClass
                        .callStaticMethod("get", darkIconDispatcherClass)
                        .callMethod("addDarkReceiver", clockView)
                } catch (ignored: Throwable) {
                    dependencyClass
                        .getStaticField("sDependency")
                        .callMethod("getDependencyInner", darkIconDispatcherClass)
                        .callMethod("addDarkReceiver", clockView)
                }
            }

            1 -> {
                (clockView as TextView).paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.DST_OUT))
            }

            2 -> {
                (clockView as TextView).paint.setXfermode(null)
                try {
                    dependencyClass
                        .callStaticMethod("get", darkIconDispatcherClass)
                        .callMethod("removeDarkReceiver", clockView)
                } catch (ignored: Throwable) {
                    dependencyClass
                        .getStaticField("sDependency")
                        .callMethod("getDependencyInner", darkIconDispatcherClass)
                        .callMethod("removeDarkReceiver", clockView)
                }
                clockView.setTextColor(statusBarClockColorCode)
            }
        }

        setClockGravity(clockView, gravity)
        
        // Add click animation for Clock Chip
        try {
            // Enable clickability for animation
            clockView.isClickable = true
            clockView.isFocusable = true
            
            // Add a ripple effect
            val rippleColor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ColorStateList.valueOf(mContext.getColor(android.R.color.system_accent1_300))
            } else {
                ColorStateList.valueOf(Color.argb(80, 255, 255, 255))
            }
            
            // Create a RippleDrawable over the existing background
            val backgroundDrawable = clockView.background
            
            // Create a mask that exactly matches the shape of the chip - convert DP to pixels
            val pixelCornerRadii = cornerRadii.map { mContext.toPx(it.toInt()).toFloat() }.toFloatArray()
            val mask = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadii = pixelCornerRadii  // Use the properly converted corner radii
                setColor(Color.WHITE)
            }
            
            // Create a customized content drawable with the same corner radii
            val contentDrawable = if (backgroundDrawable is ChipDrawable) {
                // If the background is a ChipDrawable, create a new one with the same properties
                ChipDrawable.createChipDrawable(
                    context = mContext,
                    accentFill = accentFillEnabled,
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
                // Fallback for the unlikely case that the background is not a ChipDrawable
                backgroundDrawable
            }
            
            // Create the RippleDrawable that contains both the shape and the ripple effect
            val rippleDrawable = RippleDrawable(rippleColor, contentDrawable, mask)
            clockView.background = rippleDrawable
            
            // Add a StateListAnimator for scaling animation
            val stateListAnimator = StateListAnimator()
            
            // Animation for pressed state: Scale to 95%
            val pressedAnim = ObjectAnimator.ofPropertyValuesHolder(
                clockView,
                PropertyValuesHolder.ofFloat("scaleX", 0.95f),
                PropertyValuesHolder.ofFloat("scaleY", 0.95f)
            ).apply {
                duration = 100
                interpolator = AccelerateDecelerateInterpolator()
            }
            
            // Animation for normal state: Scale back to 100%
            val defaultAnim = ObjectAnimator.ofPropertyValuesHolder(
                clockView,
                PropertyValuesHolder.ofFloat("scaleX", 1.0f),
                PropertyValuesHolder.ofFloat("scaleY", 1.0f)
            ).apply {
                duration = 200
                interpolator = AccelerateDecelerateInterpolator()
            }
            
            // Add the animations to the StateListAnimator
            stateListAnimator.addState(intArrayOf(android.R.attr.state_pressed), pressedAnim)
            stateListAnimator.addState(intArrayOf(), defaultAnim)
            
            // Set the StateListAnimator for the ClockView
            clockView.stateListAnimator = stateListAnimator
        } catch (e: Exception) {
            log(this@BackgroundChip, "Could not add click animation: ${e.message}")
        }
        
        // Set OnClickListener for Clock Chip to open the clock app
        try {
            if (mClockClickable) {
                // First try to open the clock app via ACTION_SHOW_ALARMS
                clockView.setOnClickListener {
                    try {
                        val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        mContext.startActivity(intent)
                    } catch (e: Exception) {
                        try {
                            // Fallback: Open the Google Clock app directly
                            val intent = Intent(Intent.ACTION_MAIN).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                component = ComponentName(
                                    "com.google.android.deskclock",
                                    "com.android.deskclock.DeskClock"
                                )
                            }
                            mContext.startActivity(intent)
                        } catch (e2: Exception) {
                            try {
                                // Second fallback: Try AOSP Clock app
                                val intent = Intent(Intent.ACTION_MAIN).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    component = ComponentName(
                                        "com.android.deskclock",
                                        "com.android.deskclock.DeskClock"
                                    )
                                }
                                mContext.startActivity(intent)
                            } catch (e3: Exception) {
                                log(this@BackgroundChip, "Could not open any clock app: ${e3.message}")
                            }
                        }
                    }
                }
            } else {
                // Remove OnClickListener if the feature is disabled
                clockView.setOnClickListener(null)
            }
        } catch (e: Exception) {
            log(this@BackgroundChip, "Could not set OnClickListener: ${e.message}")
        }
    }

    private fun setQSStatusIconsBgA12() {
        if (Build.VERSION.SDK_INT >= 33) return

        val xResources: XResources = resParams[SYSTEMUI_PACKAGE]?.res ?: return

        xResources.hookLayout()
            .packageName(SYSTEMUI_PACKAGE)
            .resource("layout", "quick_qs_status_icons")
            .suppressError()
            .run { param ->
                if (mShowQSStatusIconsBg == false || hideStatusIcons || fixedStatusIcons) return@run

                try {
                    val statusIcons =
                        param.view.findViewById<LinearLayout>(
                            param.res.getIdentifier(
                                "statusIcons",
                                "id",
                                mContext.packageName
                            )
                        )

                    val statusIconContainer = statusIcons.parent as LinearLayout
                    statusIconContainer.post {
                        (statusIconContainer.layoutParams as FrameLayout.LayoutParams).gravity =
                            Gravity.CENTER_VERTICAL or Gravity.END
                        statusIconContainer.layoutParams.height = TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            28f,
                            mContext.resources.displayMetrics
                        ).toInt()
                        statusIconContainer.requestLayout()
                    }

                    statusIconContainer.setPadding(
                        mContext.toPx(padding2[0]),
                        mContext.toPx(padding2[1]),
                        mContext.toPx(padding2[2]),
                        mContext.toPx(padding2[3])
                    )

                    setStatusIconsBackgroundChip(statusIconContainer)
                } catch (throwable: Throwable) {
                    log(this@BackgroundChip, throwable)
                }
            }

        xResources.hookLayout()
            .packageName(SYSTEMUI_PACKAGE)
            .resource("layout", "quick_status_bar_header_date_privacy")
            .suppressError()
            .run { param ->
                if (mShowQSStatusIconsBg == false || hideStatusIcons || fixedStatusIcons == false) return@run

                try {
                    val statusIcons =
                        param.view.findViewById<LinearLayout>(
                            param.res.getIdentifier(
                                "statusIcons",
                                "id",
                                mContext.packageName
                            )
                        )
                    if (statusIcons != null) {
                        val statusIconContainer = statusIcons.parent as LinearLayout

                        statusIconContainer.setPadding(
                            mContext.toPx(padding2[0]),
                            mContext.toPx(padding2[1]),
                            mContext.toPx(padding2[2]),
                            mContext.toPx(padding2[3])
                        )

                        setStatusIconsBackgroundChip(statusIconContainer)
                    }
                } catch (throwable: Throwable) {
                    log(this@BackgroundChip, throwable)
                }
            }
    }

    private fun setQSStatusIconsBgA13Plus() {
        if (Build.VERSION.SDK_INT < 33) return

        val quickStatusBarHeader = findClass("$SYSTEMUI_PACKAGE.qs.QuickStatusBarHeader")
        var correctClass = false
        val fs = quickStatusBarHeader!!.declaredFields
        for (f in fs) {
            if (f.name == "mIconContainer") {
                correctClass = true
            }
        }

        if (correctClass) {
            quickStatusBarHeader
                .hookMethod("onFinishInflate")
                .runAfter { param ->
                    if ((mShowQSStatusIconsBg == false && fixedStatusIcons == false) || hideStatusIcons) return@runAfter

                    val mQuickStatusBarHeader = param.thisObject as FrameLayout
                    val mIconContainer = param.thisObject.getField("mIconContainer") as LinearLayout
                    val mBatteryRemainingIcon =
                        param.thisObject.getField("mBatteryRemainingIcon") as LinearLayout
                    var layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )

                    mQsStatusIconsContainer.post {
                        mQsStatusIconsContainer.layoutParams = layoutParams
                    }

                    mQsStatusIconsContainer.gravity = Gravity.CENTER
                    mQsStatusIconsContainer.orientation = LinearLayout.HORIZONTAL

                    if (mQsStatusIconsContainer.parent != null) {
                        (mQsStatusIconsContainer.parent as ViewGroup).removeView(
                            mQsStatusIconsContainer
                        )
                    }

                    if (mQsStatusIconsContainer.childCount > 0) {
                        mQsStatusIconsContainer.removeAllViews()
                    }

                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )

                    (mIconContainer.parent as ViewGroup).removeView(mIconContainer)

                    mIconContainer.post {
                        mIconContainer.layoutParams = layoutParams
                        mIconContainer.layoutParams.height = mContext.toPx(32)
                    }

                    (mBatteryRemainingIcon.parent as ViewGroup).removeView(mBatteryRemainingIcon)

                    mBatteryRemainingIcon.post {
                        mBatteryRemainingIcon.layoutParams.height = mContext.toPx(32)
                    }

                    mQsStatusIconsContainer.addView(mIconContainer)
                    mQsStatusIconsContainer.addView(mBatteryRemainingIcon)

                    mQuickStatusBarHeader.addView(
                        mQsStatusIconsContainer,
                        mQuickStatusBarHeader.childCount - 1
                    )

                    mQsStatusIconsContainer.post {
                        (mQsStatusIconsContainer.layoutParams as FrameLayout.LayoutParams).gravity =
                            Gravity.TOP or Gravity.END
                    }

                    updateStatusIcons()
                }

            quickStatusBarHeader
                .hookMethod("updateResources")
                .runAfter {
                    if ((mShowQSStatusIconsBg == false && fixedStatusIcons == false) || hideStatusIcons) return@runAfter

                    updateStatusIcons()
                }
        } else {
            val shadeHeaderControllerClass = findClass(
                "$SYSTEMUI_PACKAGE.shade.ShadeHeaderController",
                "$SYSTEMUI_PACKAGE.shade.LargeScreenShadeHeaderController"
            )

            shadeHeaderControllerClass
                .hookMethod("onInit")
                .runAfter { param ->
                    if ((mShowQSStatusIconsBg == false && fixedStatusIcons == false) || hideStatusIcons) return@runAfter

                    val iconContainer = param.thisObject.getField("iconContainer") as LinearLayout
                    val batteryIcon = param.thisObject.getField("batteryIcon") as LinearLayout
                    header = iconContainer.parent as ViewGroup
                    val constraintLayoutParams = ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.WRAP_CONTENT,
                        ConstraintLayout.LayoutParams.WRAP_CONTENT
                    )

                    constraintLayoutId = View.generateViewId()
                    constraintLayoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                    constraintLayoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID

                    mQsStatusIconsContainer.post {
                        mQsStatusIconsContainer.layoutParams = constraintLayoutParams
                    }

                    mQsStatusIconsContainer.gravity = Gravity.CENTER
                    mQsStatusIconsContainer.orientation = LinearLayout.HORIZONTAL
                    mQsStatusIconsContainer.id = constraintLayoutId

                    if (mQsStatusIconsContainer.parent != null) {
                        (mQsStatusIconsContainer.parent as ViewGroup).removeView(
                            mQsStatusIconsContainer
                        )
                    }

                    if (mQsStatusIconsContainer.childCount > 0) {
                        mQsStatusIconsContainer.removeAllViews()
                    }

                    val linearLayoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )

                    (iconContainer.parent as ViewGroup).removeView(iconContainer)

                    iconContainer.post {
                        iconContainer.layoutParams = linearLayoutParams
                        iconContainer.layoutParams.height = mContext.toPx(32)
                    }

                    (batteryIcon.parent as ViewGroup).removeView(batteryIcon)

                    batteryIcon.post {
                        batteryIcon.layoutParams.height = mContext.toPx(32)
                    }

                    mQsStatusIconsContainer.addView(iconContainer)
                    mQsStatusIconsContainer.addView(batteryIcon)

                    header!!.addView(mQsStatusIconsContainer, header!!.childCount - 1)

                    updateStatusIcons()
                }

            shadeHeaderControllerClass
                .hookMethod("updateResources")
                .runAfter {
                    if ((mShowQSStatusIconsBg == false && fixedStatusIcons == false) || hideStatusIcons) return@runAfter

                    updateStatusIcons()
                }
        }
    }
}
