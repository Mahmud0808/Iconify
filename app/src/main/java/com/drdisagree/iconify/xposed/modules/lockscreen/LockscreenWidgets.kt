package com.drdisagree.iconify.xposed.modules.lockscreen

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.View.OnAttachStateChangeListener
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.drdisagree.iconify.data.common.Const.ACTION_LS_CLOCK_INFLATED
import com.drdisagree.iconify.data.common.Const.ACTION_WEATHER_INFLATED
import com.drdisagree.iconify.data.common.Const.DISABLE_DYNAMIC_CLOCK_COMMAND
import com.drdisagree.iconify.data.common.Const.ENABLE_DYNAMIC_CLOCK_COMMAND
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.common.Preferences.ICONIFY_LOCKSCREEN_WIDGET_TAG
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.xposed.HookEntry.Companion.enqueueProxyCommand
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.callbacks.DozeCallback
import com.drdisagree.iconify.xposed.modules.extras.utils.MyConstraintSet.Companion.applyTo
import com.drdisagree.iconify.xposed.modules.extras.utils.MyConstraintSet.Companion.clear
import com.drdisagree.iconify.xposed.modules.extras.utils.MyConstraintSet.Companion.clone
import com.drdisagree.iconify.xposed.modules.extras.utils.MyConstraintSet.Companion.connect
import com.drdisagree.iconify.xposed.modules.extras.utils.MyConstraintSet.Companion.constraintSetInstance
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.assignIdsToViews
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.getLsItemsContainer
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.setMargins
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getFieldSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookConstructor
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.modules.extras.views.AodBurnInProtection
import com.drdisagree.iconify.xposed.modules.extras.views.LockscreenWidgetsView
import com.drdisagree.iconify.xposed.modules.extras.views.LockscreenWidgetsView.Companion.launchableImageViewClass
import com.drdisagree.iconify.xposed.modules.extras.views.LockscreenWidgetsView.Companion.launchableLinearLayoutClass
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import com.drdisagree.iconify.xposed.utils.XPrefs.XprefsIsInitialized
import de.robv.android.xposed.callbacks.XC_LoadPackage

class LockscreenWidgets(context: Context) : ModPack(context) {

    // Parent
    private var mLockscreenRootView: ViewGroup? = null
    private var mLsItemsContainer: LinearLayout? = null

    // Widgets Container
    private lateinit var mWidgetsContainer: LinearLayout

    // Activity Starter
    private var mActivityStarter: Any? = null

    // Ls custom clock
    private var mLockscreenClockEnabled = false
    private var mLockscreenClockInflated = false

    // Ls weather
    private var mWeatherEnabled = false
    private var mWeatherInflated = false

    // Widgets Prefs
    // Lockscreen Widgets
    private var mWidgetsEnabled = false
    private var mDeviceWidgetEnabled = false
    private var mDeviceCustomColor = false
    private var mDeviceLinearColor = Color.WHITE
    private var mDeviceCircularColor = Color.WHITE
    private var mDeviceTextColor = Color.WHITE
    private var mWidgetsCustomColor = false
    private var mBigInactiveColor = Color.BLACK
    private var mBigActiveColor = Color.WHITE
    private var mSmallInactiveColor = Color.BLACK
    private var mSmallActiveColor = Color.WHITE
    private var mBigIconActiveColor = Color.WHITE
    private var mBigIconInactiveColor = Color.BLACK
    private var mSmallIconActiveColor = Color.WHITE
    private var mSmallIconInactiveColor = Color.BLACK
    private var mDeviceName = ""
    private var mMainWidgets = ""
    private var mExtraWidgets = ""
    private var mTopMargin = 0
    private var mBottomMargin = 0
    private var mWidgetsRoundness = 100
    private var mWidgetsScale = 1.0f
    private var mDeviceWidgetStyle = 0
    private var dateSmartSpaceViewAvailable = false
    private var aodBurnInProtection: AodBurnInProtection? = null

    private var mBroadcastRegistered = false
    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null && intent.action != null && mWidgetsEnabled) {
                if (intent.action == ACTION_WEATHER_INFLATED) {
                    mWeatherInflated = true
                    placeWidgetsView()
                } else if (intent.action == ACTION_LS_CLOCK_INFLATED) {
                    mLockscreenClockInflated = true
                    if (!mWeatherEnabled) {
                        placeWidgetsView()
                    }
                }
            }
        }
    }

    override fun updatePrefs(vararg key: String) {
        if (!XprefsIsInitialized) return

        Xprefs.apply {
            // Widgets
            mWidgetsEnabled = getBoolean(XposedKey.LOCKSCREEN_WIDGETS)
            mDeviceWidgetEnabled = getBoolean(XposedKey.LOCKSCREEN_WIDGETS_DEVICE_INFO_WIDGET)
            mMainWidgets = getString(XposedKey.LOCKSCREEN_WIDGETS_MAIN)
            mExtraWidgets = getString(XposedKey.LOCKSCREEN_WIDGETS_EXTRAS)
            mDeviceCustomColor =
                getBoolean(XposedKey.LOCKSCREEN_WIDGETS_DEVICE_INFO_WIDGET_CUSTOM_COLORS)
            mDeviceLinearColor = getInt(XposedKey.LOCKSCREEN_WIDGETS_LINEAR_PROGRESS_COLOR)
            mDeviceCircularColor = getInt(XposedKey.LOCKSCREEN_WIDGETS_CIRCULAR_PROGRESS_COLOR)
            mDeviceTextColor = getInt(XposedKey.LOCKSCREEN_WIDGETS_TEXT_COLOR)
            mDeviceName = getString(XposedKey.LOCKSCREEN_WIDGETS_CUSTOM_DEVICE_NAME)
            mWidgetsCustomColor = getBoolean(XposedKey.LOCKSCREEN_WIDGETS_CUSTOM_WIDGET_COLORS)
            mBigInactiveColor = getInt(XposedKey.LOCKSCREEN_WIDGETS_LARGE_WIDGET_INACTIVE_COLOR)
            mBigActiveColor = getInt(XposedKey.LOCKSCREEN_WIDGETS_LARGE_WIDGET_ACTIVE_COLOR)
            mSmallInactiveColor = getInt(XposedKey.LOCKSCREEN_WIDGETS_MINI_WIDGET_INACTIVE_COLOR)
            mSmallActiveColor = getInt(XposedKey.LOCKSCREEN_WIDGETS_MINI_WIDGET_ACTIVE_COLOR)
            mBigIconActiveColor =
                getInt(XposedKey.LOCKSCREEN_WIDGETS_LARGE_WIDGET_ICON_ACTIVE_COLOR)
            mBigIconInactiveColor =
                getInt(XposedKey.LOCKSCREEN_WIDGETS_LARGE_WIDGET_ICON_INACTIVE_COLOR)
            mSmallIconActiveColor =
                getInt(XposedKey.LOCKSCREEN_WIDGETS_MINI_WIDGET_ICON_ACTIVE_COLOR)
            mSmallIconInactiveColor =
                getInt(XposedKey.LOCKSCREEN_WIDGETS_MINI_WIDGET_ICON_INACTIVE_COLOR)
            mTopMargin = getInt(XposedKey.LOCKSCREEN_WIDGETS_TOP_MARGIN)
            mBottomMargin = getInt(XposedKey.LOCKSCREEN_WIDGETS_BOTTOM_MARGIN)
            mWidgetsRoundness = getInt(XposedKey.LOCKSCREEN_WIDGETS_CORNER_RADIUS)
            mWidgetsScale = getFloat(XposedKey.LOCKSCREEN_WIDGETS_VIEW_SCALE)
            mDeviceWidgetStyle =
                getString(XposedKey.LOCKSCREEN_WIDGETS_DEVICE_INFO_WIDGET_STYLE).toInt()

            // Ls custom clock
            mLockscreenClockEnabled = getBoolean(XposedKey.CUSTOM_LOCKSCREEN_CLOCK)

            // Ls weather
            mWeatherEnabled = getBoolean(XposedKey.LOCKSCREEN_WEATHER)
        }

        when (key.firstOrNull()) {
            XposedKey.LOCKSCREEN_WIDGETS.name -> {
                resetDynamicClock()
                updateLockscreenWidgets()
            }

            in setOf(
                XposedKey.LOCKSCREEN_WIDGETS_DEVICE_INFO_WIDGET.name,
                XposedKey.LOCKSCREEN_WIDGETS.name,
                XposedKey.LOCKSCREEN_WIDGETS_EXTRAS.name
            ) -> updateLockscreenWidgets()

            in setOf(
                XposedKey.LOCKSCREEN_WIDGETS_DEVICE_INFO_WIDGET_CUSTOM_COLORS.name,
                XposedKey.LOCKSCREEN_WIDGETS_LINEAR_PROGRESS_COLOR.name,
                XposedKey.LOCKSCREEN_WIDGETS_CIRCULAR_PROGRESS_COLOR.name,
                XposedKey.LOCKSCREEN_WIDGETS_TEXT_COLOR.name,
                XposedKey.LOCKSCREEN_WIDGETS_CUSTOM_DEVICE_NAME.name,
                XposedKey.LOCKSCREEN_WIDGETS_DEVICE_INFO_WIDGET_STYLE.name
            ) -> updateLsDeviceWidget()

            in setOf(
                XposedKey.LOCKSCREEN_WIDGETS_CUSTOM_WIDGET_COLORS.name,
                XposedKey.LOCKSCREEN_WIDGETS_LARGE_WIDGET_ACTIVE_COLOR.name,
                XposedKey.LOCKSCREEN_WIDGETS_LARGE_WIDGET_INACTIVE_COLOR.name,
                XposedKey.LOCKSCREEN_WIDGETS_MINI_WIDGET_ACTIVE_COLOR.name,
                XposedKey.LOCKSCREEN_WIDGETS_MINI_WIDGET_INACTIVE_COLOR.name,
                XposedKey.LOCKSCREEN_WIDGETS_LARGE_WIDGET_ICON_ACTIVE_COLOR.name,
                XposedKey.LOCKSCREEN_WIDGETS_LARGE_WIDGET_ICON_INACTIVE_COLOR.name,
                XposedKey.LOCKSCREEN_WIDGETS_MINI_WIDGET_ICON_ACTIVE_COLOR.name,
                XposedKey.LOCKSCREEN_WIDGETS_MINI_WIDGET_ICON_INACTIVE_COLOR.name
            ) -> updateLockscreenWidgetsColors()

            in setOf(
                XposedKey.LOCKSCREEN_WIDGETS_TOP_MARGIN.name,
                XposedKey.LOCKSCREEN_WIDGETS_BOTTOM_MARGIN.name
            ) -> updateMargins()

            XposedKey.LOCKSCREEN_WIDGETS_CORNER_RADIUS.name -> updateLockscreenWidgetsRoundness()

            XposedKey.LOCKSCREEN_WIDGETS_VIEW_SCALE.name -> updateLockscreenWidgetsScale()
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag", "DiscouragedApi")
    override fun handleLoadPackage(loadPackageParam: XC_LoadPackage.LoadPackageParam) {
        // Receiver to handle weather inflated
        if (!mBroadcastRegistered) {
            val intentFilter = IntentFilter()
            intentFilter.addAction(ACTION_WEATHER_INFLATED)
            intentFilter.addAction(ACTION_LS_CLOCK_INFLATED)

            mContext.registerReceiver(
                mReceiver,
                intentFilter,
                Context.RECEIVER_EXPORTED
            )

            mBroadcastRegistered = true
        }

        mWidgetsContainer = LinearLayout(mContext).apply {
            id = View.generateViewId()
            tag = ICONIFY_LOCKSCREEN_WIDGET_TAG
            layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        launchableImageViewClass = findClass("$SYSTEMUI_PACKAGE.animation.view.LaunchableImageView")

        launchableLinearLayoutClass =
            findClass("$SYSTEMUI_PACKAGE.animation.view.LaunchableLinearLayout")

        val keyguardQuickAffordanceInteractorClass =
            findClass("$SYSTEMUI_PACKAGE.keyguard.domain.interactor.KeyguardQuickAffordanceInteractor")

        keyguardQuickAffordanceInteractorClass
            .hookConstructor()
            .runAfter { param ->
                mActivityStarter = param.thisObject.getFieldSilently("activityStarter")
                setActivityStarter()
            }

        val aodBurnInSectionClass =
            findClass("$SYSTEMUI_PACKAGE.keyguard.ui.view.layout.sections.AodBurnInSection")

        fun viewAttached(entryV: View) {
            Handler(Looper.getMainLooper()).postDelayed({
                if (!mWidgetsEnabled) return@postDelayed

                val rootView = (entryV.parent as? ViewGroup)
                    ?.rootView
                    ?.findViewById<ViewGroup>(
                        mContext.resources.getIdentifier(
                            "keyguard_root_view",
                            "id",
                            mContext.packageName
                        )
                    ) ?: return@postDelayed

                dateSmartSpaceViewAvailable = rootView.findViewById<View?>(
                    mContext.resources.getIdentifier(
                        "date_smartspace_view",
                        "id",
                        mContext.packageName
                    )
                ) != null

                mLockscreenRootView = rootView

                (mWidgetsContainer.parent as? ViewGroup)?.removeView(mWidgetsContainer)

                if (mLockscreenClockEnabled || mWeatherEnabled) {
                    mLsItemsContainer = rootView.getLsItemsContainer()

                    // Add widgets view at the end
                    mLsItemsContainer!!.addView(
                        mWidgetsContainer,
                        mLsItemsContainer!!.childCount - 1
                    )
                } else {
                    mLockscreenRootView!!.addView(mWidgetsContainer)
                }

                applyLayoutConstraints(mLsItemsContainer ?: mWidgetsContainer)
                aodBurnInProtection = AodBurnInProtection.registerForView(
                    mLsItemsContainer ?: mWidgetsContainer
                )

                placeWidgetsView()
            }, 1000)
        }

        aodBurnInSectionClass
            .hookMethod("addViews")
            .runAfter { param ->
                if (!mWidgetsEnabled) return@runAfter

                val entryV = param.args[0] as View

                entryV.addOnAttachStateChangeListener(object : OnAttachStateChangeListener {
                    override fun onViewAttachedToWindow(v: View) {
                        viewAttached(entryV)
                    }

                    override fun onViewDetachedFromWindow(v: View) {}
                })

                if (entryV.isAttachedToWindow) {
                    viewAttached(entryV)
                }
            }

        val defaultNotificationStackScrollLayoutSectionClass =
            findClass("$SYSTEMUI_PACKAGE.keyguard.ui.view.layout.sections.DefaultNotificationStackScrollLayoutSection")

        val notificationContainerId = mContext.resources.getIdentifier(
            "nssl_placeholder",
            "id",
            mContext.packageName
        )

        defaultNotificationStackScrollLayoutSectionClass
            .hookMethod("applyConstraints")
            .runAfter { param ->
                if (!mWidgetsEnabled) return@runAfter

                val constraintSet = param.args[0]

                constraintSet.clear(
                    notificationContainerId,
                    ConstraintSet.TOP
                )
                constraintSet.connect(
                    notificationContainerId,
                    ConstraintSet.TOP,
                    (mLsItemsContainer ?: mWidgetsContainer).id,
                    ConstraintSet.BOTTOM
                )
            }

        val smartspaceSectionClass =
            findClass("$SYSTEMUI_PACKAGE.keyguard.ui.view.layout.sections.SmartspaceSection")

        val bcSmartSpaceViewId = mContext.resources.getIdentifier(
            "bc_smartspace_view",
            "id",
            mContext.packageName
        )
        val dateSmartSpaceViewId = mContext.resources.getIdentifier(
            "date_smartspace_view",
            "id",
            mContext.packageName
        )

        smartspaceSectionClass
            .hookMethod("applyConstraints")
            .runAfter { param ->
                if (!mWidgetsEnabled) return@runAfter

                val constraintSet = param.args[0]

                val smartSpaceViewId = if (dateSmartSpaceViewAvailable) {
                    dateSmartSpaceViewId
                } else {
                    // Some ROMs don't have date smartspace view
                    bcSmartSpaceViewId
                }

                // Connect widget view to bottom of date smartspace
                if (!mLockscreenClockEnabled && mWeatherEnabled && mLsItemsContainer != null) {
                    constraintSet.clear(
                        mLsItemsContainer!!.id,
                        ConstraintSet.TOP
                    )
                    constraintSet.connect(
                        mLsItemsContainer!!.id,
                        ConstraintSet.TOP,
                        smartSpaceViewId,
                        ConstraintSet.BOTTOM
                    )
                } else if (mLockscreenClockEnabled && mLsItemsContainer != null) {
                    constraintSet.clear(
                        mLsItemsContainer!!.id,
                        ConstraintSet.TOP
                    )
                    constraintSet.connect(
                        mLsItemsContainer!!.id,
                        ConstraintSet.TOP,
                        ConstraintSet.PARENT_ID,
                        ConstraintSet.TOP
                    )
                } else if (!mLockscreenClockEnabled && !mWeatherEnabled) {
                    constraintSet.clear(
                        mWidgetsContainer.id,
                        ConstraintSet.TOP
                    )
                    constraintSet.connect(
                        mWidgetsContainer.id,
                        ConstraintSet.TOP,
                        smartSpaceViewId,
                        ConstraintSet.BOTTOM
                    )
                }
            }

        val keyguardClockSwitchClass = findClass(
            "com.android.keyguard.KeyguardClockSwitch",
            suppressError = true
        )

        keyguardClockSwitchClass
            .hookMethod("updateClockViews")
            .runAfter { param ->
                if (!mWidgetsEnabled) return@runAfter

                updateLockscreenWidgetsOnClock(param.args[0] as Boolean)
            }

        // For unknown reason, rotating device makes the height of view to 0
        // This is a workaround to make sure the view is visible
        DozeCallback.getInstance().registerDozeChangeListener(
            object : DozeCallback.DozeListener {
                fun updateLayoutParams() {
                    if (!mWidgetsEnabled || !::mWidgetsContainer.isInitialized) return

                    if (::mWidgetsContainer.isInitialized) {
                        if (mLsItemsContainer?.width == 0 || mLsItemsContainer?.height == 0) {
                            mLsItemsContainer?.layoutParams?.apply {
                                width = ViewGroup.LayoutParams.MATCH_PARENT
                                height = ViewGroup.LayoutParams.WRAP_CONTENT
                            }
                        }
                        if (mWidgetsContainer.width == 0 || mWidgetsContainer.height == 0) {
                            mWidgetsContainer.layoutParams.apply {
                                width = ViewGroup.LayoutParams.MATCH_PARENT
                                height = ViewGroup.LayoutParams.WRAP_CONTENT
                            }
                        }
                    }
                }

                override fun onDozingStarted() {
                    updateDozingState(true)
                    aodBurnInProtection?.setMovementEnabled(true)
                    updateLayoutParams()
                }

                override fun onDozingStopped() {
                    updateDozingState(false)
                    aodBurnInProtection?.setMovementEnabled(false)
                    updateLayoutParams()
                }
            }
        )
    }

    private fun placeWidgetsView() {
        if ((!mWidgetsEnabled || mLockscreenRootView == null) ||
            (mLockscreenClockEnabled && !mLockscreenClockInflated) ||
            (mWeatherEnabled && !mWeatherInflated)
        ) return

        val widgetView = LockscreenWidgetsView.getInstance(mContext, mActivityStarter)

        if (widgetView.parent != mWidgetsContainer) {
            (mWidgetsContainer.parent as? ViewGroup)?.removeView(widgetView)
            mWidgetsContainer.addView(widgetView)

            updateLockscreenWidgets()
            updateLsDeviceWidget()
            updateLockscreenWidgetsColors()
            updateMargins()
            updateLockscreenWidgetsRoundness()
            updateLockscreenWidgetsScale()
        }
    }

    @SuppressLint("DiscouragedApi")
    private fun applyLayoutConstraints(widgetView: ViewGroup) {
        if (mLockscreenRootView == null) return

        mLockscreenRootView.assignIdsToViews()

        mLsItemsContainer?.layoutParams?.width = LinearLayout.LayoutParams.MATCH_PARENT

        val notificationContainerId = mContext.resources.getIdentifier(
            "nssl_placeholder",
            "id",
            mContext.packageName
        )
        val aodNotificationIconContainerId = mContext.resources.getIdentifier(
            "aod_notification_icon_container",
            "id",
            mContext.packageName
        )

        constraintSetInstance?.also { constraintSet ->
            constraintSet.clone(mLockscreenRootView!!)

            // Connect widget view to parent
            constraintSet.connect(
                widgetView.id,
                ConstraintSet.START,
                ConstraintSet.PARENT_ID,
                ConstraintSet.START
            )
            constraintSet.connect(
                widgetView.id,
                ConstraintSet.END,
                ConstraintSet.PARENT_ID,
                ConstraintSet.END
            )

            if ((widgetView == mWidgetsContainer && !mLockscreenClockEnabled && !mWeatherEnabled) ||
                (widgetView == mLsItemsContainer && !mLockscreenClockEnabled && mWeatherEnabled)
            ) {
                val dateSmartspaceViewId = if (dateSmartSpaceViewAvailable) {
                    mContext.resources.getIdentifier(
                        "date_smartspace_view",
                        "id",
                        mContext.packageName
                    )
                } else {
                    // Some ROMs don't have date smartspace view
                    mContext.resources.getIdentifier(
                        "bc_smartspace_view",
                        "id",
                        mContext.packageName
                    )
                }
                // If no custom clock or widgets enabled, or only widgets enabled
                // then connect widget view to bottom of date smartspace
                constraintSet.connect(
                    widgetView.id,
                    ConstraintSet.TOP,
                    dateSmartspaceViewId,
                    ConstraintSet.BOTTOM
                )
            } else if (widgetView == mLsItemsContainer && mLockscreenClockEnabled) {
                // If custom clock enabled, then connect whole container to top of parent
                constraintSet.connect(
                    widgetView.id,
                    ConstraintSet.TOP,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.TOP
                )
            }

            // Connect notification container below widget
            if (notificationContainerId != 0) {
                constraintSet.clear(
                    notificationContainerId,
                    ConstraintSet.TOP
                )
                constraintSet.connect(
                    notificationContainerId,
                    ConstraintSet.TOP,
                    widgetView.id,
                    ConstraintSet.BOTTOM
                )
            }

            // Connect aod notification icon container below widget
            if (aodNotificationIconContainerId != 0) {
                constraintSet.clear(
                    aodNotificationIconContainerId,
                    ConstraintSet.TOP
                )
                constraintSet.connect(
                    aodNotificationIconContainerId,
                    ConstraintSet.TOP,
                    widgetView.id,
                    ConstraintSet.BOTTOM
                )
            }

            constraintSet.applyTo(mLockscreenRootView!!)
        }
    }

    private fun updateLockscreenWidgets() {
        LockscreenWidgetsView.getInstance()?.setOptions(
            mWidgetsEnabled,
            mDeviceWidgetEnabled,
            mMainWidgets,
            mExtraWidgets
        )
    }

    private fun updateLockscreenWidgetsOnClock(isLargeClock: Boolean) {
        LockscreenWidgetsView.getInstance()?.setIsLargeClock(
            if (mLockscreenClockEnabled) false else isLargeClock
        )
    }

    private fun updateLsDeviceWidget() {
        LockscreenWidgetsView.getInstance()?.setDeviceWidgetOptions(
            mDeviceWidgetStyle,
            mDeviceCustomColor,
            mDeviceLinearColor,
            mDeviceCircularColor,
            mDeviceTextColor,
            mDeviceName
        )
    }

    private fun updateLockscreenWidgetsColors() {
        LockscreenWidgetsView.getInstance()?.setCustomColors(
            mWidgetsCustomColor,
            mBigInactiveColor,
            mBigActiveColor,
            mSmallInactiveColor,
            mSmallActiveColor,
            mBigIconInactiveColor,
            mBigIconActiveColor,
            mSmallIconInactiveColor,
            mSmallIconActiveColor
        )
    }

    private fun updateMargins() {
        val childView = mWidgetsContainer.getChildAt(0) as? LinearLayout

        mWidgetsContainer.layoutParams.width = if (mLsItemsContainer == null) {
            0
        } else {
            LinearLayout.LayoutParams.MATCH_PARENT
        }

        mWidgetsContainer.gravity = Gravity.CENTER_HORIZONTAL
        childView?.gravity = Gravity.CENTER_HORIZONTAL

        if (childView != null) {
            setMargins(
                childView,
                mContext,
                0,
                mTopMargin,
                0,
                mBottomMargin
            )
        }
    }

    private fun updateLockscreenWidgetsRoundness() {
        LockscreenWidgetsView.getInstance()?.setRoundness(mWidgetsRoundness)
    }

    private fun updateLockscreenWidgetsScale() {
        LockscreenWidgetsView.getInstance()?.setScale(mWidgetsScale)
    }

    private fun updateDozingState(isDozing: Boolean) {
        LockscreenWidgetsView.getInstance()?.setDozingState(isDozing)
    }

    private fun setActivityStarter() {
        if (mActivityStarter != null) {
            LockscreenWidgetsView.getInstance()?.setActivityStarter(mActivityStarter)
        }
    }

    private fun resetDynamicClock() {
        enqueueProxyCommand { proxy ->
            proxy.runCommand(
                if (mWidgetsEnabled) DISABLE_DYNAMIC_CLOCK_COMMAND else ENABLE_DYNAMIC_CLOCK_COMMAND
            )
        }
    }
}