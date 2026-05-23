package com.drdisagree.iconify.xposed.modules.statusbar

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.doOnAttach
import com.drdisagree.iconify.data.common.Const.FRAMEWORK_PACKAGE
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_DEFAULT
import com.drdisagree.iconify.data.common.Preferences.ICONIFY_SB_BATTERY_ICON_TAG
import com.drdisagree.iconify.data.common.Preferences.ICONIFY_SB_CENTER_CLOCK_CONTAINER_TAG
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.callbacks.KeyguardShowingCallback
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.DisplayUtils.isLandscape
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.ViewHelper.reAddView
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.ViewHelper.removeViewFromParent
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.ViewHelper.toPx
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.ResourceHookManager
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethodSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getField
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
    private var customBatteryEnabled = false
    private var hideDefaultBattery = false

    override fun updatePrefs(vararg key: String) {
        Xprefs.apply {
            dualStatusbarEnabled = getBoolean(XposedKey.DUAL_STATUSBAR)
            portraitOnlyEnabled = getBoolean(XposedKey.DUAL_STATUSBAR_PORTRAIT_ONLY)
            singleRowStartSide = getBoolean(XposedKey.DUAL_STATUSBAR_START_SIDE_SINGLE_ROW)
            singleRowEndSide = getBoolean(XposedKey.DUAL_STATUSBAR_END_SIDE_SINGLE_ROW)
            swapStartSide = getBoolean(XposedKey.DUAL_STATUSBAR_SWAP_START_SIDE)
            swapEndSide = getBoolean(XposedKey.DUAL_STATUSBAR_SWAP_END_SIDE)
            statusbarHeight = getInt(XposedKey.DUAL_STATUSBAR_HEIGHT)
            statusbarStartPadding = getInt(XposedKey.DUAL_STATUSBAR_START_PADDING)
            statusbarEndPadding = getInt(XposedKey.DUAL_STATUSBAR_END_PADDING)
            statusbarTopPadding = getInt(XposedKey.DUAL_STATUSBAR_TOP_PADDING)
            startTopMargin = getInt(XposedKey.DUAL_STATUSBAR_START_TOP_MARGIN)
            startBottomMargin = getInt(XposedKey.DUAL_STATUSBAR_START_BOTTOM_MARGIN)
            endTopMargin = getInt(XposedKey.DUAL_STATUSBAR_END_TOP_MARGIN)
            endBottomMargin = getInt(XposedKey.DUAL_STATUSBAR_END_BOTTOM_MARGIN)
            clockPosition = getString(XposedKey.STATUSBAR_CLOCK_POSITION).toInt()
            customBatteryEnabled =
                getString(XposedKey.CUSTOM_BATTERY_STYLE).toInt() != BATTERY_STYLE_DEFAULT
            hideDefaultBattery = getBoolean(XposedKey.HIDE_DEFAULT_BATTERY_VIEW)
        }

        when (key.firstOrNull()) {
            in setOf(
                XposedKey.DUAL_STATUSBAR_PORTRAIT_ONLY.name,
                XposedKey.DUAL_STATUSBAR_START_SIDE_SINGLE_ROW.name,
                XposedKey.DUAL_STATUSBAR_END_SIDE_SINGLE_ROW.name,
                XposedKey.DUAL_STATUSBAR_SWAP_START_SIDE.name,
                XposedKey.DUAL_STATUSBAR_SWAP_END_SIDE.name
            ) -> updateRowsIfNeeded()

            in setOf(
                XposedKey.DUAL_STATUSBAR_START_PADDING.name,
                XposedKey.DUAL_STATUSBAR_END_PADDING.name,
                XposedKey.DUAL_STATUSBAR_TOP_PADDING.name
            ) -> setStatusbarPadding()

            in setOf(
                XposedKey.DUAL_STATUSBAR_START_TOP_MARGIN.name,
                XposedKey.DUAL_STATUSBAR_START_BOTTOM_MARGIN.name,
                XposedKey.DUAL_STATUSBAR_END_TOP_MARGIN.name,
                XposedKey.DUAL_STATUSBAR_END_BOTTOM_MARGIN.name
            ) -> setStatusbarRowMargin()

            XposedKey.DUAL_STATUSBAR_HEIGHT.name -> updateWindowHeight()

            XposedKey.STATUSBAR_CLOCK_POSITION.name -> handleClockPosition()
        }
    }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        val phoneStatusBarViewClass =
            findClass("$SYSTEMUI_PACKAGE.statusbar.phone.PhoneStatusBarView")
        val scrimControllerClass = findClass("$SYSTEMUI_PACKAGE.statusbar.phone.ScrimController")
        val notificationPanelViewControllerClass =
            findClass("$SYSTEMUI_PACKAGE.shade.NotificationPanelViewController")

        scrimControllerClass
            .hookConstructor()
            .runAfter { param -> mScrimControllerObj = param.thisObject }

        notificationPanelViewControllerClass
            .hookConstructor()
            .runAfter { param ->
                if (mScrimControllerObj == null) {
                    mScrimControllerObj = param.thisObject.getField("mScrimController")
                }
            }

        notificationPanelViewControllerClass
            .hookMethod(
                "onFinishInflate",
                "reInflateViews"
            )
            .runAfter { param ->
                if (mScrimControllerObj == null) {
                    mScrimControllerObj = param.thisObject.getField("mScrimController")
                }
            }

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
                    val statusbarStartSideContainer = runCatching {
                        phoneStatusBarView.findViewById<FrameLayout>(
                            mContext.resources.getIdentifier(
                                "status_bar_start_side_container",
                                "id",
                                mContext.packageName
                            )
                        )
                    }.getOrNull() ?: statusbarContents!!.getChildAt(0)
                    val statusbarEndSideContainer = runCatching {
                        phoneStatusBarView.findViewById<FrameLayout>(
                            mContext.resources.getIdentifier(
                                "status_bar_end_side_container",
                                "id",
                                mContext.packageName
                            )
                        )
                    }.getOrNull() ?: statusbarContents!!
                        .getChildAt(statusbarContents!!.childCount - 1)

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

                    endBottomSideContainer?.reAddView(statusbarEndSideContainer, 0)

                    newEndSideContainer?.reAddView(endTopSideContainer, 0)
                    newEndSideContainer?.reAddView(endBottomSideContainer, 1)
                    newEndSideContainer?.id = statusbarEndSideContainer.id
                    statusbarEndSideContainer.id = View.NO_ID
                }

                phoneStatusBarView.doOnAttach { view ->
                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            if (batteryIconView == null || batteryIconView!!.parent != endTopSideContainer) {
                                batteryIconView = view.findBatteryView()
                                endTopSideContainer?.reAddView(batteryIconView, 0)
                            }
                        },
                        200
                    )
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

        // Handle a bug where statusbar battery is duplicated on lockscreen
        KeyguardShowingCallback.getInstance().registerKeyguardShowingListener(
            object : KeyguardShowingCallback.KeyguardShowingListener {
                override fun onKeyguardShown() {
                    isKeyguardShown = true
                    batteryIconView?.post { batteryIconView?.visibility = View.GONE }
                }

                override fun onKeyguardDismissed() {
                    isKeyguardShown = false
                    batteryIconView?.post { batteryIconView?.visibility = View.VISIBLE }
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

        when (clockPosition) {
            0 -> { // Left
                centerClockContainer.removeViewFromParent()
                startTopSideContainer?.reAddView(mClockView)
                startTopSideContainer?.visibility = View.VISIBLE
                (mClockView?.layoutParams as? MarginLayoutParams)?.marginStart = mContext.toPx(0)
                (mClockView?.layoutParams as? LinearLayout.LayoutParams)?.gravity =
                    Gravity.CENTER_VERTICAL or Gravity.START
            }

            1 -> { // Center
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
            }

            2 -> { // Right
                centerClockContainer.removeViewFromParent()
                endTopSideContainer?.reAddView(mClockView)
                startTopSideContainer?.visibility = View.GONE
                (mClockView?.layoutParams as? MarginLayoutParams)?.marginStart = mContext.toPx(6)
                (mClockView?.layoutParams as? LinearLayout.LayoutParams)?.gravity =
                    Gravity.CENTER_VERTICAL or Gravity.END
            }
        }
    }

    private fun View.findBatteryView(): View? {
        val systemIconsView = findViewById<ViewGroup>(
            mContext.resources.getIdentifier(
                "system_icons",
                "id",
                mContext.packageName
            )
        )

        if (customBatteryEnabled) {
            return systemIconsView.findViewWithTag(ICONIFY_SB_BATTERY_ICON_TAG)
        } else if (!hideDefaultBattery) {
            for (i in systemIconsView.childCount - 1 downTo 0) {
                val child = systemIconsView.getChildAt(i)
                if (child.javaClass.simpleName == "ComposeView") {
                    return child
                }
            }
        }

        return null
    }

    private val isDsbResourceEnabled: Boolean
        get() = dualStatusbarEnabled && (!portraitOnlyEnabled || !mContext.isLandscape)

    companion object {
        var isKeyguardShown = true
    }
}