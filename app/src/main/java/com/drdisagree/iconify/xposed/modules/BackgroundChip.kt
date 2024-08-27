package com.drdisagree.iconify.xposed.modules

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.common.Preferences.CHIP_STATUSBAR_CLOCK_ACCENT
import com.drdisagree.iconify.common.Preferences.CHIP_STATUSBAR_CLOCK_END_COLOR
import com.drdisagree.iconify.common.Preferences.CHIP_STATUSBAR_CLOCK_GRADIENT_DIRECTION
import com.drdisagree.iconify.common.Preferences.CHIP_STATUSBAR_CLOCK_PADDING_BOTTOM
import com.drdisagree.iconify.common.Preferences.CHIP_STATUSBAR_CLOCK_PADDING_LEFT
import com.drdisagree.iconify.common.Preferences.CHIP_STATUSBAR_CLOCK_PADDING_RIGHT
import com.drdisagree.iconify.common.Preferences.CHIP_STATUSBAR_CLOCK_PADDING_TOP
import com.drdisagree.iconify.common.Preferences.CHIP_STATUSBAR_CLOCK_RADIUS_BOTTOM_LEFT
import com.drdisagree.iconify.common.Preferences.CHIP_STATUSBAR_CLOCK_RADIUS_BOTTOM_RIGHT
import com.drdisagree.iconify.common.Preferences.CHIP_STATUSBAR_CLOCK_RADIUS_TOP_LEFT
import com.drdisagree.iconify.common.Preferences.CHIP_STATUSBAR_CLOCK_RADIUS_TOP_RIGHT
import com.drdisagree.iconify.common.Preferences.CHIP_STATUSBAR_CLOCK_START_COLOR
import com.drdisagree.iconify.common.Preferences.CHIP_STATUSBAR_CLOCK_STROKE_ACCENT
import com.drdisagree.iconify.common.Preferences.CHIP_STATUSBAR_CLOCK_STROKE_COLOR
import com.drdisagree.iconify.common.Preferences.CHIP_STATUSBAR_CLOCK_STROKE_DASH
import com.drdisagree.iconify.common.Preferences.CHIP_STATUSBAR_CLOCK_STROKE_DASH_GAP
import com.drdisagree.iconify.common.Preferences.CHIP_STATUSBAR_CLOCK_STROKE_DASH_WIDTH
import com.drdisagree.iconify.common.Preferences.CHIP_STATUSBAR_CLOCK_STROKE_SWITCH
import com.drdisagree.iconify.common.Preferences.CHIP_STATUSBAR_CLOCK_STROKE_WIDTH
import com.drdisagree.iconify.common.Preferences.CHIP_STATUSBAR_CLOCK_STYLE_CHANGED
import com.drdisagree.iconify.common.Preferences.CHIP_STATUSBAR_CLOCK_SWITCH
import com.drdisagree.iconify.common.Preferences.CHIP_STATUSBAR_CLOCK_TEXT_COLOR_CODE
import com.drdisagree.iconify.common.Preferences.CHIP_STATUSBAR_CLOCK_TEXT_COLOR_OPTION
import com.drdisagree.iconify.common.Preferences.CHIP_STATUS_ICONS_ACCENT
import com.drdisagree.iconify.common.Preferences.CHIP_STATUS_ICONS_END_COLOR
import com.drdisagree.iconify.common.Preferences.CHIP_STATUS_ICONS_GRADIENT_DIRECTION
import com.drdisagree.iconify.common.Preferences.CHIP_STATUS_ICONS_PADDING_BOTTOM
import com.drdisagree.iconify.common.Preferences.CHIP_STATUS_ICONS_PADDING_LEFT
import com.drdisagree.iconify.common.Preferences.CHIP_STATUS_ICONS_PADDING_RIGHT
import com.drdisagree.iconify.common.Preferences.CHIP_STATUS_ICONS_PADDING_TOP
import com.drdisagree.iconify.common.Preferences.CHIP_STATUS_ICONS_RADIUS_BOTTOM_LEFT
import com.drdisagree.iconify.common.Preferences.CHIP_STATUS_ICONS_RADIUS_BOTTOM_RIGHT
import com.drdisagree.iconify.common.Preferences.CHIP_STATUS_ICONS_RADIUS_TOP_LEFT
import com.drdisagree.iconify.common.Preferences.CHIP_STATUS_ICONS_RADIUS_TOP_RIGHT
import com.drdisagree.iconify.common.Preferences.CHIP_STATUS_ICONS_START_COLOR
import com.drdisagree.iconify.common.Preferences.CHIP_STATUS_ICONS_STROKE_ACCENT
import com.drdisagree.iconify.common.Preferences.CHIP_STATUS_ICONS_STROKE_COLOR
import com.drdisagree.iconify.common.Preferences.CHIP_STATUS_ICONS_STROKE_DASH
import com.drdisagree.iconify.common.Preferences.CHIP_STATUS_ICONS_STROKE_DASH_GAP
import com.drdisagree.iconify.common.Preferences.CHIP_STATUS_ICONS_STROKE_DASH_WIDTH
import com.drdisagree.iconify.common.Preferences.CHIP_STATUS_ICONS_STROKE_SWITCH
import com.drdisagree.iconify.common.Preferences.CHIP_STATUS_ICONS_STROKE_WIDTH
import com.drdisagree.iconify.common.Preferences.CHIP_STATUS_ICONS_STYLE_CHANGED
import com.drdisagree.iconify.common.Preferences.CHIP_STATUS_ICONS_SWITCH
import com.drdisagree.iconify.common.Preferences.FIXED_STATUS_ICONS_SIDEMARGIN
import com.drdisagree.iconify.common.Preferences.FIXED_STATUS_ICONS_SWITCH
import com.drdisagree.iconify.common.Preferences.FIXED_STATUS_ICONS_TOPMARGIN
import com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_SWITCH
import com.drdisagree.iconify.common.Preferences.HIDE_STATUS_ICONS_SWITCH
import com.drdisagree.iconify.xposed.HookRes.Companion.resParams
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.utils.Helpers.findClassInArray
import com.drdisagree.iconify.xposed.modules.utils.StatusBarClock
import com.drdisagree.iconify.xposed.modules.utils.StatusBarClock.setClockGravity
import com.drdisagree.iconify.xposed.modules.utils.ViewHelper.toPx
import com.drdisagree.iconify.xposed.modules.views.ChipDrawable
import com.drdisagree.iconify.xposed.modules.views.ChipDrawable.GradientDirection.Companion.toIndex
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import com.drdisagree.iconify.xposed.utils.XPrefs.XprefsIsInitialized
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge.hookAllMethods
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.callStaticMethod
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import de.robv.android.xposed.XposedHelpers.findClass
import de.robv.android.xposed.XposedHelpers.findClassIfExists
import de.robv.android.xposed.XposedHelpers.getObjectField
import de.robv.android.xposed.XposedHelpers.getStaticObjectField
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam
import de.robv.android.xposed.callbacks.XC_LayoutInflated
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

@SuppressLint("DiscouragedApi")
class BackgroundChip(context: Context?) : ModPack(context!!) {

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
    private var mLoadPackageParam: LoadPackageParam? = null
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

    override fun updatePrefs(vararg key: String) {
        if (!XprefsIsInitialized) return

        Xprefs.apply {
            // Status bar clock chip
            mShowSBClockBg = getBoolean(CHIP_STATUSBAR_CLOCK_SWITCH, false)
            statusBarClockColorOption = getInt(CHIP_STATUSBAR_CLOCK_TEXT_COLOR_OPTION, 0)
            statusBarClockColorCode = getInt(CHIP_STATUSBAR_CLOCK_TEXT_COLOR_CODE, Color.WHITE)
            accentFillEnabled = getBoolean(CHIP_STATUSBAR_CLOCK_ACCENT, true)
            startColor = getInt(CHIP_STATUSBAR_CLOCK_START_COLOR, Color.RED)
            endColor = getInt(CHIP_STATUSBAR_CLOCK_END_COLOR, Color.BLUE)
            gradientDirection =
                ChipDrawable.GradientDirection.fromIndex(
                    getInt(
                        CHIP_STATUSBAR_CLOCK_GRADIENT_DIRECTION,
                        ChipDrawable.GradientDirection.LEFT_RIGHT.toIndex()
                    )
                )
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
        mLoadPackageParam = loadPackageParam
        statusBarClockChip(loadPackageParam)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            statusIconsChip(loadPackageParam)
        }
    }

    private fun statusBarClockChip(loadPackageParam: LoadPackageParam) {
        val collapsedStatusBarFragment = findClassInArray(
            loadPackageParam.classLoader,
            "$SYSTEMUI_PACKAGE.statusbar.phone.CollapsedStatusBarFragment",
            "$SYSTEMUI_PACKAGE.statusbar.phone.fragment.CollapsedStatusBarFragment"

        )
        dependencyClass = findClass(
            "$SYSTEMUI_PACKAGE.Dependency",
            loadPackageParam.classLoader
        )
        darkIconDispatcherClass = findClass(
            "$SYSTEMUI_PACKAGE.plugins.DarkIconDispatcher",
            loadPackageParam.classLoader
        )

        findAndHookMethod(
            collapsedStatusBarFragment,
            "onViewCreated",
            View::class.java,
            Bundle::class.java,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    mClockView = StatusBarClock.getLeftClockView(mContext, param)
                    mCenterClockView = StatusBarClock.getCenterClockView(mContext, param)
                    mRightClockView = StatusBarClock.getRightClockView(mContext, param)

                    (getObjectField(
                        param.thisObject,
                        "mStatusBar"
                    ) as ViewGroup).addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                        updateStatusBarClock(false)
                    }

                    updateStatusBarClock(true)

                    if (mShowSBClockBg) {
                        try {
                            val mStatusBar = getObjectField(
                                param.thisObject,
                                "mStatusBar"
                            ) as FrameLayout

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
                            log(TAG + throwable)
                        }
                    }
                }
            })
    }

    private fun statusIconsChip(loadPackageParam: LoadPackageParam) {
        setQSStatusIconsBgA12()
        setQSStatusIconsBgA13Plus(loadPackageParam)
    }

    @SuppressLint("RtlHardcoded")
    private fun updateStatusBarClock(force: Boolean) {
        if (!mShowSBClockBg) return

        if (mClockView != null && mClockView!!.background == null || force) {
            updateClockView(
                mClockView,
                Gravity.LEFT or Gravity.CENTER
            )
        }

        if (mCenterClockView != null && mCenterClockView!!.background == null || force) {
            updateClockView(
                mCenterClockView,
                Gravity.CENTER
            )
        }

        if (mRightClockView != null && mRightClockView!!.background == null || force) {
            updateClockView(
                mRightClockView,
                Gravity.RIGHT or Gravity.CENTER
            )
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

                (mQsStatusIconsContainer.layoutParams as FrameLayout.LayoutParams).setMarginEnd(
                    mContext.toPx(sideMarginStatusIcons)
                )
            }
        } else if (mQsStatusIconsContainer.layoutParams is LinearLayout.LayoutParams) {
            mQsStatusIconsContainer.post {
                (mQsStatusIconsContainer.layoutParams as LinearLayout.LayoutParams).setMargins(
                    0,
                    mContext.toPx(topMarginStatusIcons),
                    0,
                    0
                )

                (mQsStatusIconsContainer.layoutParams as LinearLayout.LayoutParams).setMarginEnd(
                    mContext.toPx(sideMarginStatusIcons)
                )
            }
        } else if (mLoadPackageParam != null && header != null && constraintLayoutId != -1) {
            try {
                val constraintSetClass = findClass(
                    "androidx.constraintlayout.widget.ConstraintSet",
                    mLoadPackageParam!!.classLoader
                )
                val mConstraintSet = constraintSetClass.getDeclaredConstructor().newInstance()

                callMethod(mConstraintSet, "clone", header)

                callMethod(
                    mConstraintSet,
                    "connect",
                    constraintLayoutId,
                    ConstraintSet.TOP,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.TOP,
                    0
                )

                callMethod(
                    mConstraintSet,
                    "connect",
                    constraintLayoutId,
                    ConstraintSet.END,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.END,
                    0
                )

                callMethod(mConstraintSet, "applyTo", header)

                callMethod(
                    callMethod(mQsStatusIconsContainer, "getLayoutParams"),
                    "setMargins",
                    0,
                    mContext.toPx(topMarginStatusIcons),
                    0,
                    0
                )

                callMethod(
                    callMethod(mQsStatusIconsContainer, "getLayoutParams"),
                    "setMarginEnd",
                    mContext.toPx(sideMarginStatusIcons)
                )
            } catch (throwable: Throwable) {
                log(TAG + throwable)
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
                    callMethod(
                        callStaticMethod(dependencyClass, "get", darkIconDispatcherClass),
                        "addDarkReceiver",
                        clockView
                    )
                } catch (ignored: Throwable) {
                    callMethod(
                        callMethod(
                            getStaticObjectField(
                                dependencyClass,
                                "sDependency"
                            ),
                            "getDependencyInner",
                            darkIconDispatcherClass
                        ),
                        "addDarkReceiver",
                        clockView
                    )
                }
            }

            1 -> {
                (clockView as TextView).paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.DST_OUT))
            }

            2 -> {
                (clockView as TextView).paint.setXfermode(null)
                try {
                    callMethod(
                        callStaticMethod(dependencyClass, "get", darkIconDispatcherClass),
                        "removeDarkReceiver",
                        clockView
                    )
                } catch (ignored: Throwable) {
                    callMethod(
                        callMethod(
                            getStaticObjectField(
                                dependencyClass,
                                "sDependency"
                            ),
                            "getDependencyInner",
                            darkIconDispatcherClass
                        ),
                        "removeDarkReceiver",
                        clockView
                    )
                }
                clockView.setTextColor(statusBarClockColorCode)
            }
        }

        setClockGravity(clockView, gravity)
    }

    private fun setQSStatusIconsBgA12() {
        if (Build.VERSION.SDK_INT >= 33) return

        val resParam: InitPackageResourcesParam = resParams[SYSTEMUI_PACKAGE] ?: return

        resParam.res.hookLayout(
            SYSTEMUI_PACKAGE,
            "layout",
            "quick_qs_status_icons",
            object : XC_LayoutInflated() {
                override fun handleLayoutInflated(liparam: LayoutInflatedParam) {
                    if (!mShowQSStatusIconsBg || hideStatusIcons || fixedStatusIcons) return

                    try {
                        val statusIcons =
                            liparam.view.findViewById<LinearLayout>(
                                liparam.res.getIdentifier(
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
                        log(TAG + throwable)
                    }
                }
            })

        resParam.res.hookLayout(
            SYSTEMUI_PACKAGE,
            "layout",
            "quick_status_bar_header_date_privacy",
            object : XC_LayoutInflated() {
                override fun handleLayoutInflated(liparam: LayoutInflatedParam) {
                    if (!mShowQSStatusIconsBg || hideStatusIcons || !fixedStatusIcons) return

                    try {
                        val statusIcons =
                            liparam.view.findViewById<LinearLayout>(
                                liparam.res.getIdentifier(
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
                        log(TAG + throwable)
                    }
                }
            })
    }

    private fun setQSStatusIconsBgA13Plus(loadPackageParam: LoadPackageParam) {
        if (Build.VERSION.SDK_INT < 33) return

        val quickStatusBarHeader = findClass(
            "$SYSTEMUI_PACKAGE.qs.QuickStatusBarHeader",
            loadPackageParam.classLoader
        )
        var correctClass = false
        val fs = quickStatusBarHeader.declaredFields
        for (f in fs) {
            if (f.getName() == "mIconContainer") {
                correctClass = true
            }
        }

        if (correctClass) {
            hookAllMethods(quickStatusBarHeader, "onFinishInflate", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (!mShowQSStatusIconsBg && !fixedStatusIcons || hideStatusIcons) return

                    val mQuickStatusBarHeader = param.thisObject as FrameLayout
                    val mIconContainer = getObjectField(
                        param.thisObject,
                        "mIconContainer"
                    ) as LinearLayout
                    val mBatteryRemainingIcon = getObjectField(
                        param.thisObject,
                        "mBatteryRemainingIcon"
                    ) as LinearLayout
                    var layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )

                    mQsStatusIconsContainer.post {
                        mQsStatusIconsContainer.setLayoutParams(
                            layoutParams
                        )
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
                        mIconContainer.setLayoutParams(layoutParams)
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
            })

            hookAllMethods(quickStatusBarHeader, "updateResources", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (!mShowQSStatusIconsBg && !fixedStatusIcons || hideStatusIcons) return

                    updateStatusIcons()
                }
            })
        } else {
            var shadeHeaderControllerClass = findClassIfExists(
                "$SYSTEMUI_PACKAGE.shade.ShadeHeaderController",
                loadPackageParam.classLoader
            )
            if (shadeHeaderControllerClass == null) {
                shadeHeaderControllerClass = findClass(
                    "$SYSTEMUI_PACKAGE.shade.LargeScreenShadeHeaderController",
                    loadPackageParam.classLoader
                )
            }

            hookAllMethods(shadeHeaderControllerClass, "onInit", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (!mShowQSStatusIconsBg && !fixedStatusIcons || hideStatusIcons) return

                    val iconContainer = getObjectField(
                        param.thisObject,
                        "iconContainer"
                    ) as LinearLayout
                    val batteryIcon = getObjectField(
                        param.thisObject,
                        "batteryIcon"
                    ) as LinearLayout
                    header = iconContainer.parent as ViewGroup
                    val constraintLayoutParams = ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.WRAP_CONTENT,
                        ConstraintLayout.LayoutParams.WRAP_CONTENT
                    )

                    constraintLayoutId = View.generateViewId()
                    constraintLayoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                    constraintLayoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID

                    mQsStatusIconsContainer.post {
                        mQsStatusIconsContainer.setLayoutParams(
                            constraintLayoutParams
                        )
                    }

                    mQsStatusIconsContainer.gravity = Gravity.CENTER
                    mQsStatusIconsContainer.orientation = LinearLayout.HORIZONTAL
                    mQsStatusIconsContainer.setId(constraintLayoutId)

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
                        iconContainer.setLayoutParams(linearLayoutParams)
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
            })

            hookAllMethods(shadeHeaderControllerClass, "updateResources", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (!mShowQSStatusIconsBg && !fixedStatusIcons || hideStatusIcons) return

                    updateStatusIcons()
                }
            })
        }
    }

    companion object {
        private val TAG = "Iconify - ${BackgroundChip::class.java.simpleName}: "
    }
}
