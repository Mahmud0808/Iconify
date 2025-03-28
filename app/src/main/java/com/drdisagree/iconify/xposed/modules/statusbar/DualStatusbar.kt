package com.drdisagree.iconify.xposed.modules.statusbar

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.drdisagree.iconify.data.common.Const.FRAMEWORK_PACKAGE
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.common.Preferences.DUAL_STATUSBAR
import com.drdisagree.iconify.data.common.Preferences.DUAL_STATUSBAR_END_BOTTOM_MARGIN
import com.drdisagree.iconify.data.common.Preferences.DUAL_STATUSBAR_END_PADDING
import com.drdisagree.iconify.data.common.Preferences.DUAL_STATUSBAR_END_SIDE_SINGLE_ROW
import com.drdisagree.iconify.data.common.Preferences.DUAL_STATUSBAR_END_TOP_MARGIN
import com.drdisagree.iconify.data.common.Preferences.DUAL_STATUSBAR_HEIGHT
import com.drdisagree.iconify.data.common.Preferences.DUAL_STATUSBAR_ONLY_PORTRAIT
import com.drdisagree.iconify.data.common.Preferences.DUAL_STATUSBAR_START_BOTTOM_MARGIN
import com.drdisagree.iconify.data.common.Preferences.DUAL_STATUSBAR_START_PADDING
import com.drdisagree.iconify.data.common.Preferences.DUAL_STATUSBAR_START_SIDE_SINGLE_ROW
import com.drdisagree.iconify.data.common.Preferences.DUAL_STATUSBAR_START_TOP_MARGIN
import com.drdisagree.iconify.data.common.Preferences.DUAL_STATUSBAR_SWAP_END_SIDE
import com.drdisagree.iconify.data.common.Preferences.DUAL_STATUSBAR_SWAP_START_SIDE
import com.drdisagree.iconify.data.common.Preferences.DUAL_STATUSBAR_TOP_PADDING
import com.drdisagree.iconify.data.common.Preferences.ICONIFY_SB_CENTER_CLOCK_CONTAINER_TAG
import com.drdisagree.iconify.data.common.Preferences.STATUSBAR_CLOCK_POSITION
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.callbacks.KeyguardShowingCallback
import com.drdisagree.iconify.xposed.modules.extras.utils.DisplayUtils.isLandscape
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.reAddView
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.removeViewFromParent
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.toPx
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.ResourceHookManager
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethodSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookConstructor
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.modules.extras.views.AlphaOptimizedLinearLayout
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

@SuppressLint("DiscouragedApi")
class DualStatusbar(context: Context) : ModPack(context) {

    private var dualStatusbarEnabled = false
    private var portraitOnlyEnabled = false
    private var singleRowStartSide = false
    private var singleRowEndSide = false
    private var swapStartSide = false
    private var swapEndSide = false
    private var statusbarHeight = -1
    private var statusbarStartPadding = -1
    private var statusbarEndPadding = -1
    private var statusbarTopPadding = -1
    private var startTopMargin = 0
    private var startBottomMargin = 0
    private var endTopMargin = 0
    private var endBottomMargin = 0
    private var statusbarContents: LinearLayout? = null
    private var newStartSideContainer: LinearLayout? = null
    private var newEndSideContainer: LinearLayout? = null
    private var startTopSideContainer: LinearLayout? = null
    private var startBottomSideContainer: LinearLayout? = null
    private var endTopSideContainer: LinearLayout? = null
    private var endBottomSideContainer: LinearLayout? = null
    private var mClockView: View? = null
    private var batteryIconView: View? = null
    private var cutoutSpaceView: View? = null
    private var mPhoneStatusBarViewObj: Any? = null
    private var mScrimControllerObj: Any? = null
    private var clockPosition = 0

    override fun updatePrefs(vararg key: String) {
        Xprefs.apply {
            dualStatusbarEnabled = getBoolean(DUAL_STATUSBAR, false)
            portraitOnlyEnabled = getBoolean(DUAL_STATUSBAR_ONLY_PORTRAIT, false)
            singleRowStartSide = getBoolean(DUAL_STATUSBAR_START_SIDE_SINGLE_ROW, false)
            singleRowEndSide = getBoolean(DUAL_STATUSBAR_END_SIDE_SINGLE_ROW, false)
            swapStartSide = getBoolean(DUAL_STATUSBAR_SWAP_START_SIDE, false)
            swapEndSide = getBoolean(DUAL_STATUSBAR_SWAP_END_SIDE, false)
            statusbarHeight = getSliderInt(DUAL_STATUSBAR_HEIGHT, -1)
            statusbarStartPadding = getSliderInt(DUAL_STATUSBAR_START_PADDING, -1)
            statusbarEndPadding = getSliderInt(DUAL_STATUSBAR_END_PADDING, -1)
            statusbarTopPadding = getSliderInt(DUAL_STATUSBAR_TOP_PADDING, -1)
            startTopMargin = getSliderInt(DUAL_STATUSBAR_START_TOP_MARGIN, 0)
            startBottomMargin = getSliderInt(DUAL_STATUSBAR_START_BOTTOM_MARGIN, 0)
            endTopMargin = getSliderInt(DUAL_STATUSBAR_END_TOP_MARGIN, 0)
            endBottomMargin = getSliderInt(DUAL_STATUSBAR_END_BOTTOM_MARGIN, 0)
            clockPosition = getString(STATUSBAR_CLOCK_POSITION, "0")!!.toInt()
        }

        when (key.firstOrNull()) {
            in setOf(
                DUAL_STATUSBAR_ONLY_PORTRAIT,
                DUAL_STATUSBAR_START_SIDE_SINGLE_ROW,
                DUAL_STATUSBAR_END_SIDE_SINGLE_ROW,
                DUAL_STATUSBAR_SWAP_START_SIDE,
                DUAL_STATUSBAR_SWAP_END_SIDE
            ) -> updateRowsIfNeeded()

            in setOf(
                DUAL_STATUSBAR_START_PADDING,
                DUAL_STATUSBAR_END_PADDING,
                DUAL_STATUSBAR_TOP_PADDING
            ) -> setStatusbarPadding()

            in setOf(
                DUAL_STATUSBAR_START_TOP_MARGIN,
                DUAL_STATUSBAR_START_BOTTOM_MARGIN,
                DUAL_STATUSBAR_END_TOP_MARGIN,
                DUAL_STATUSBAR_END_BOTTOM_MARGIN
            ) -> setStatusbarRowMargin()

            DUAL_STATUSBAR_HEIGHT -> updateWindowHeight()

            STATUSBAR_CLOCK_POSITION -> handleClockPosition()
        }
    }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        val phoneStatusBarViewClass =
            findClass("$SYSTEMUI_PACKAGE.statusbar.phone.PhoneStatusBarView")
        val scrimControllerClass = findClass("$SYSTEMUI_PACKAGE.statusbar.phone.ScrimController")

        scrimControllerClass
            .hookConstructor()
            .runAfter { param -> mScrimControllerObj = param.thisObject }

        phoneStatusBarViewClass
            .hookConstructor()
            .runAfter { param -> mPhoneStatusBarViewObj = param.thisObject }

        phoneStatusBarViewClass
            .hookMethod(
                "onFinishInflate",
                "onConfigurationChanged"
            )
            .runAfter { param ->
                if (!dualStatusbarEnabled) return@runAfter

                val phoneStatusBarView = param.thisObject as ViewGroup

                val dsbSetupDone =
                    (phoneStatusBarView.findViewWithTag<View?>("dual_statusbar_start_side") != null
                            && phoneStatusBarView.findViewWithTag<View?>("dual_statusbar_end_side") != null)

                if (!dsbSetupDone) {
                    statusbarContents = phoneStatusBarView.findViewById(
                        mContext.resources.getIdentifier(
                            "status_bar_contents",
                            "id",
                            mContext.packageName
                        )
                    )
                    val statusbarStartSideContainer = phoneStatusBarView.findViewById<FrameLayout>(
                        mContext.resources.getIdentifier(
                            "status_bar_start_side_container",
                            "id",
                            mContext.packageName
                        )
                    )
                    val statusbarEndSideContainer = phoneStatusBarView.findViewById<FrameLayout>(
                        mContext.resources.getIdentifier(
                            "status_bar_end_side_container",
                            "id",
                            mContext.packageName
                        )
                    )

                    newStartSideContainer = LinearLayout(mContext).apply {
                        orientation = LinearLayout.VERTICAL
                        layoutParams = LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            1f
                        )
                        gravity = Gravity.START or Gravity.CENTER_VERTICAL
                        tag = "dual_statusbar_start_side"
                    }
                    cutoutSpaceView = phoneStatusBarView.findViewById(
                        mContext.resources.getIdentifier(
                            "cutout_space_view",
                            "id",
                            mContext.packageName
                        )
                    )
                    newEndSideContainer = LinearLayout(mContext).apply {
                        orientation = LinearLayout.VERTICAL
                        layoutParams = LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            1f
                        )
                        gravity = Gravity.END or Gravity.CENTER_VERTICAL
                        tag = "dual_statusbar_end_side"
                    }

                    statusbarContents?.reAddView(newStartSideContainer, 0)
                    statusbarContents?.reAddView(cutoutSpaceView, 1)
                    statusbarContents?.reAddView(newEndSideContainer, 2)

                    mClockView = phoneStatusBarView.findViewById<View>(
                        mContext.resources.getIdentifier(
                            "clock",
                            "id",
                            mContext.packageName
                        )
                    )
                    startTopSideContainer = LinearLayout(mContext).apply {
                        orientation = LinearLayout.HORIZONTAL
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            0,
                            1f
                        )
                        gravity = Gravity.START or Gravity.CENTER_VERTICAL
                    }
                    startBottomSideContainer = LinearLayout(mContext).apply {
                        orientation = LinearLayout.HORIZONTAL
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            0,
                            1f
                        )
                        gravity = Gravity.START or Gravity.CENTER_VERTICAL
                    }

                    startTopSideContainer?.reAddView(mClockView, 0)
                    startBottomSideContainer?.reAddView(statusbarStartSideContainer, 0)

                    newStartSideContainer?.reAddView(startTopSideContainer, 0)
                    newStartSideContainer?.reAddView(startBottomSideContainer, 1)
                    newStartSideContainer?.id = statusbarStartSideContainer.id
                    statusbarStartSideContainer.id = View.NO_ID

                    batteryIconView = phoneStatusBarView.findViewById<View>(
                        mContext.resources.getIdentifier(
                            "battery",
                            "id",
                            mContext.packageName
                        )
                    )
                    endTopSideContainer = LinearLayout(mContext).apply {
                        orientation = LinearLayout.HORIZONTAL
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            0,
                            1f
                        )
                        gravity = Gravity.END or Gravity.CENTER_VERTICAL
                    }
                    endBottomSideContainer = LinearLayout(mContext).apply {
                        orientation = LinearLayout.HORIZONTAL
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            0,
                            1f
                        )
                        gravity = Gravity.END or Gravity.CENTER_VERTICAL
                    }

                    endTopSideContainer?.reAddView(batteryIconView, 0)
                    endBottomSideContainer?.reAddView(statusbarEndSideContainer, 0)

                    newEndSideContainer?.reAddView(endTopSideContainer, 0)
                    newEndSideContainer?.reAddView(endBottomSideContainer, 1)
                    newEndSideContainer?.id = statusbarEndSideContainer.id
                    statusbarEndSideContainer.id = View.NO_ID
                }

                updateRowsIfNeeded()
                handleClockPosition()
            }

        ResourceHookManager
            .hookDimen()
            .forPackageName(FRAMEWORK_PACKAGE)
            .whenCondition { isDsbResourceEnabled && statusbarHeight != -1 }
            .addResource("status_bar_height_default") { statusbarHeight }
            .forPackageName(SYSTEMUI_PACKAGE)
            .whenCondition { isDsbResourceEnabled && statusbarHeight != -1 }
            .addResource("status_bar_height") { statusbarHeight }
            .whenCondition { isDsbResourceEnabled && statusbarStartPadding != -1 }
            .addResource("status_bar_padding_start") { statusbarStartPadding }
            .whenCondition { isDsbResourceEnabled && statusbarEndPadding != -1 }
            .addResource("status_bar_padding_end") { statusbarEndPadding }
            .whenCondition { isDsbResourceEnabled && statusbarTopPadding != -1 }
            .addResource("status_bar_padding_top") { statusbarTopPadding }
            .whenCondition { isDsbResourceEnabled && !singleRowEndSide }
            .addResource("signal_cluster_battery_padding") { 0 }
            .addResource("status_bar_battery_end_padding") { 0 }
            .addResource("status_bar_icons_padding_start") { 0 }
            .addResource("status_bar_icons_padding_end") { 0 }
            .apply()

        // Handle a bug where statusbar battery is shown on lockscreen too
        KeyguardShowingCallback.getInstance().registerKeyguardShowingListener(
            object : KeyguardShowingCallback.KeyguardShowingListener {
                override fun onKeyguardShown() {
                    batteryIconView?.visibility = View.INVISIBLE
                }

                override fun onKeyguardDismissed() {
                    batteryIconView?.visibility = View.VISIBLE
                }
            }
        )
    }

    private fun updateRowsIfNeeded() {
        if (!dualStatusbarEnabled) return

        val requiresSingleLine = portraitOnlyEnabled && mContext.isLandscape
        val startSideRequiresSingleLine = singleRowStartSide || requiresSingleLine
        val endSideRequiresSingleLine = singleRowEndSide || requiresSingleLine

        if (startSideRequiresSingleLine) {
            newStartSideContainer?.apply {
                orientation = LinearLayout.HORIZONTAL
            }
            startTopSideContainer?.apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
            }
            startBottomSideContainer?.apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
            }
        } else {
            newStartSideContainer?.apply {
                orientation = LinearLayout.VERTICAL
            }
            startTopSideContainer?.apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    1f
                )
            }
            startBottomSideContainer?.apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    1f
                )
            }
        }

        if (endSideRequiresSingleLine) {
            newEndSideContainer?.apply {
                orientation = LinearLayout.HORIZONTAL
                reAddView(endTopSideContainer)
            }
            endTopSideContainer?.apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
            }
            endBottomSideContainer?.apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
            }
        } else {
            newEndSideContainer?.apply {
                orientation = LinearLayout.VERTICAL
                reAddView(endBottomSideContainer)
            }
            endTopSideContainer?.apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    1f
                )
            }
            endBottomSideContainer?.apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    1f
                )
            }
        }

        if (swapStartSide) {
            newStartSideContainer?.reAddView(startBottomSideContainer, 0)
        } else {
            newStartSideContainer?.reAddView(startTopSideContainer, 0)
        }

        if (!endSideRequiresSingleLine) {
            if (swapEndSide) {
                newEndSideContainer?.reAddView(endBottomSideContainer, 0)
            } else {
                newEndSideContainer?.reAddView(endTopSideContainer, 0)
            }
        } else {
            newEndSideContainer?.reAddView(endBottomSideContainer, 0)
        }

        setStatusbarRowMargin()
    }

    private fun setStatusbarPadding() {
        val startPadding = mContext.resources.getDimensionPixelSize(
            mContext.resources.getIdentifier(
                "status_bar_padding_start",
                "dimen",
                mContext.packageName
            )
        )
        val endPadding = mContext.resources.getDimensionPixelSize(
            mContext.resources.getIdentifier(
                "status_bar_padding_end",
                "dimen",
                mContext.packageName
            )
        )
        val topPadding = mContext.resources.getDimensionPixelSize(
            mContext.resources.getIdentifier(
                "status_bar_padding_top",
                "dimen",
                mContext.packageName
            )
        )

        statusbarContents?.apply {
            setPaddingRelative(
                startPadding,
                topPadding,
                endPadding,
                paddingBottom
            )
        }
    }

    private fun setStatusbarRowMargin() {
        val requiresSingleLine = portraitOnlyEnabled && mContext.isLandscape
        val startSideRequiresSingleLine = singleRowStartSide || requiresSingleLine
        val endSideRequiresSingleLine = singleRowEndSide || requiresSingleLine

        newStartSideContainer?.getChildAt(0)?.apply {
            setPaddingRelative(mContext.toPx(startTopMargin), 0, 0, 0)
        }
        newStartSideContainer?.getChildAt(1)?.apply {
            if (!startSideRequiresSingleLine) {
                setPaddingRelative(mContext.toPx(startBottomMargin), 0, 0, 0)
            } else {
                setPaddingRelative(0, 0, 0, 0)
            }
        }
        newEndSideContainer?.getChildAt(0)?.apply {
            if (!endSideRequiresSingleLine) {
                setPaddingRelative(0, 0, mContext.toPx(endTopMargin), 0)
            } else {
                setPaddingRelative(0, 0, 0, 0)
            }
        }
        newEndSideContainer?.getChildAt(1)?.apply {
            setPaddingRelative(0, 0, mContext.toPx(endBottomMargin), 0)
        }
    }

    private fun updateWindowHeight() {
        val statusbarHeight = mContext.resources.getDimensionPixelSize(
            mContext.resources.getIdentifier(
                "status_bar_height",
                "dimen",
                mContext.packageName
            )
        )

        (statusbarContents?.parent as? ViewGroup)?.layoutParams?.height = statusbarHeight

        mPhoneStatusBarViewObj.callMethodSilently("updateWindowHeight")
    }

    private fun handleClockPosition() {
        if (!dualStatusbarEnabled) return

        val centerClockContainer = (statusbarContents?.parent as? ViewGroup)
            ?.findViewWithTag<LinearLayout>(ICONIFY_SB_CENTER_CLOCK_CONTAINER_TAG)

        if (clockPosition == 0) { // Left
            centerClockContainer.removeViewFromParent()
            startTopSideContainer?.reAddView(mClockView)
            startTopSideContainer?.visibility = View.VISIBLE
            (mClockView?.layoutParams as? MarginLayoutParams)?.marginStart = mContext.toPx(0)
            (mClockView?.layoutParams as? LinearLayout.LayoutParams)?.gravity =
                Gravity.CENTER_VERTICAL or Gravity.START
        } else if (clockPosition == 1) { // Center
            val container = centerClockContainer ?: AlphaOptimizedLinearLayout(mContext).apply {
                tag = ICONIFY_SB_CENTER_CLOCK_CONTAINER_TAG
                gravity = Gravity.CENTER
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                setPadding(
                    0,
                    mContext.resources.getDimensionPixelSize(
                        mContext.resources.getIdentifier(
                            "status_bar_padding_top",
                            "dimen",
                            mContext.packageName
                        )
                    ),
                    0,
                    0
                )
                reAddView(mClockView)
            }

            (statusbarContents?.parent as? ViewGroup)?.reAddView(container)
            startTopSideContainer?.visibility = View.GONE
            (mClockView?.layoutParams as? MarginLayoutParams)?.marginStart = mContext.toPx(0)
            (mClockView?.layoutParams as? LinearLayout.LayoutParams)?.gravity = Gravity.CENTER
        } else if (clockPosition == 2) { // Right
            centerClockContainer.removeViewFromParent()
            endTopSideContainer?.reAddView(mClockView)
            startTopSideContainer?.visibility = View.GONE
            (mClockView?.layoutParams as? MarginLayoutParams)?.marginStart = mContext.toPx(6)
            (mClockView?.layoutParams as? LinearLayout.LayoutParams)?.gravity =
                Gravity.CENTER_VERTICAL or Gravity.END
        }
    }

    private val isDsbResourceEnabled: Boolean
        get() = dualStatusbarEnabled && (!portraitOnlyEnabled || !mContext.isLandscape)
}