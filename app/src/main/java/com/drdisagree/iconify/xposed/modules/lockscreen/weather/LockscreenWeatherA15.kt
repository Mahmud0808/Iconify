package com.drdisagree.iconify.xposed.modules.lockscreen.weather

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.View.OnAttachStateChangeListener
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.children
import com.drdisagree.iconify.data.common.Const.ACTION_LS_CLOCK_INFLATED
import com.drdisagree.iconify.data.common.Const.ACTION_WEATHER_INFLATED
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.common.Preferences.ICONIFY_LOCKSCREEN_CLOCK_TAG
import com.drdisagree.iconify.data.common.Preferences.ICONIFY_LOCKSCREEN_WEATHER_TAG
import com.drdisagree.iconify.data.common.Preferences.LOCKSCREEN_WIDGETS_ENABLED
import com.drdisagree.iconify.data.common.Preferences.LSCLOCK_SWITCH
import com.drdisagree.iconify.data.common.Preferences.WEATHER_CENTER_VIEW
import com.drdisagree.iconify.data.common.Preferences.WEATHER_CUSTOM_MARGINS_BOTTOM
import com.drdisagree.iconify.data.common.Preferences.WEATHER_CUSTOM_MARGINS_SIDE
import com.drdisagree.iconify.data.common.Preferences.WEATHER_CUSTOM_MARGINS_TOP
import com.drdisagree.iconify.data.common.Preferences.WEATHER_FONT_SWITCH
import com.drdisagree.iconify.data.common.Preferences.WEATHER_ICON_SIZE
import com.drdisagree.iconify.data.common.Preferences.WEATHER_SHOW_CONDITION
import com.drdisagree.iconify.data.common.Preferences.WEATHER_SHOW_HUMIDITY
import com.drdisagree.iconify.data.common.Preferences.WEATHER_SHOW_LOCATION
import com.drdisagree.iconify.data.common.Preferences.WEATHER_SHOW_WIND
import com.drdisagree.iconify.data.common.Preferences.WEATHER_STYLE
import com.drdisagree.iconify.data.common.Preferences.WEATHER_SWITCH
import com.drdisagree.iconify.data.common.Preferences.WEATHER_TEXT_COLOR
import com.drdisagree.iconify.data.common.Preferences.WEATHER_TEXT_COLOR_SWITCH
import com.drdisagree.iconify.data.common.Preferences.WEATHER_TEXT_SIZE
import com.drdisagree.iconify.data.common.Preferences.WEATHER_TRIGGER_UPDATE
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.callbacks.DozeCallback
import com.drdisagree.iconify.xposed.modules.extras.utils.MyConstraintSet.Companion.applyTo
import com.drdisagree.iconify.xposed.modules.extras.utils.MyConstraintSet.Companion.clear
import com.drdisagree.iconify.xposed.modules.extras.utils.MyConstraintSet.Companion.clone
import com.drdisagree.iconify.xposed.modules.extras.utils.MyConstraintSet.Companion.connect
import com.drdisagree.iconify.xposed.modules.extras.utils.MyConstraintSet.Companion.constraintSetInstance
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.applyFontRecursively
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.assignIdsToViews
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.getLsItemsContainer
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.setMargins
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getFieldSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookConstructor
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.modules.extras.views.AodBurnInProtection
import com.drdisagree.iconify.xposed.modules.extras.views.CurrentWeatherView
import com.drdisagree.iconify.xposed.modules.lockscreen.Lockscreen.Companion.isComposeLockscreen
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import com.drdisagree.iconify.xposed.utils.XPrefs.XprefsIsInitialized
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class LockscreenWeatherA15(context: Context) : ModPack(context) {

    private var mWeatherEnabled = false
    private var weatherShowLocation = true
    private var weatherShowCondition = true
    private var weatherShowHumidity = false
    private var weatherShowWind = false
    private var weatherCustomColor = false
    private var weatherColor = Color.WHITE
    private var weatherTextSize: Int = 16
    private var weatherImageSize: Int = 18
    private var mSideMargin: Int = 0
    private var mTopMargin: Int = 0
    private var mBottomMargin: Int = 0
    private var mWeatherBackground = 0
    private var mCenterWeather = false
    private var mLockscreenRootView: ViewGroup? = null
    private var mLsItemsContainer: LinearLayout? = null
    private var mLockscreenClockEnabled = false
    private var mLockscreenClockInflated = false
    private var mWidgetsEnabled = false
    private var dateSmartSpaceViewAvailable = false
    private lateinit var mWeatherContainer: LinearLayout
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())
    private var aodBurnInProtection: AodBurnInProtection? = null
    private var mCustomFontEnabled = false
    private val mCustomFontLocation = Environment.getExternalStorageDirectory().toString() +
            "/.iconify_files/lockscreen_weather_font.ttf"

    private var mBroadcastRegistered = false
    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null && intent.action != null) {
                if (intent.action == ACTION_LS_CLOCK_INFLATED && mWeatherEnabled) {
                    mLockscreenClockInflated = true
                    placeWeatherView()
                }
            }
        }
    }

    override fun updatePrefs(vararg key: String) {
        if (!XprefsIsInitialized || !isComposeLockscreen) return

        Xprefs.apply {
            mWeatherEnabled = getBoolean(WEATHER_SWITCH, false)
            weatherShowLocation = getBoolean(WEATHER_SHOW_LOCATION, true)
            weatherShowCondition = getBoolean(WEATHER_SHOW_CONDITION, true)
            weatherShowHumidity = getBoolean(WEATHER_SHOW_HUMIDITY, false)
            weatherShowWind = getBoolean(WEATHER_SHOW_WIND, false)
            weatherCustomColor = getBoolean(WEATHER_TEXT_COLOR_SWITCH, false)
            weatherColor = getInt(WEATHER_TEXT_COLOR, Color.WHITE)
            weatherTextSize = getSliderInt(WEATHER_TEXT_SIZE, 16)
            weatherImageSize = getSliderInt(WEATHER_ICON_SIZE, 18)
            mSideMargin = getSliderInt(WEATHER_CUSTOM_MARGINS_SIDE, 32)
            mTopMargin = getSliderInt(WEATHER_CUSTOM_MARGINS_TOP, 20)
            mBottomMargin = getSliderInt(WEATHER_CUSTOM_MARGINS_BOTTOM, 20)
            mWeatherBackground = Integer.parseInt(getString(WEATHER_STYLE, "0")!!)
            mCenterWeather = getBoolean(WEATHER_CENTER_VIEW, false)
            mLockscreenClockEnabled = getBoolean(LSCLOCK_SWITCH, false)
            mWidgetsEnabled = getBoolean(LOCKSCREEN_WIDGETS_ENABLED, false)
            mCustomFontEnabled = Xprefs.getBoolean(WEATHER_FONT_SWITCH, false)
        }

        when (key.firstOrNull()) {
            in setOf(
                WEATHER_TRIGGER_UPDATE,
                WEATHER_SHOW_LOCATION,
                WEATHER_SHOW_CONDITION,
                WEATHER_SHOW_HUMIDITY,
                WEATHER_SHOW_WIND,
                WEATHER_TEXT_COLOR_SWITCH,
                WEATHER_TEXT_COLOR,
                WEATHER_TEXT_SIZE,
                WEATHER_ICON_SIZE,
                WEATHER_STYLE,
                WEATHER_CUSTOM_MARGINS_BOTTOM,
                WEATHER_CUSTOM_MARGINS_SIDE,
                WEATHER_CUSTOM_MARGINS_TOP,
                WEATHER_CENTER_VIEW,
                WEATHER_FONT_SWITCH
            ) -> {
                if (::mWeatherContainer.isInitialized) {
                    applyLayoutConstraints(mLsItemsContainer ?: mWeatherContainer)
                    updateWeatherView()
                }
            }
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag", "DiscouragedApi")
    override fun handleLoadPackage(loadPackageParam: XC_LoadPackage.LoadPackageParam) {
        if (!isComposeLockscreen) return

        // Receiver to handle lockscreen clock inflated
        if (!mBroadcastRegistered) {
            val intentFilter = IntentFilter()
            intentFilter.addAction(ACTION_LS_CLOCK_INFLATED)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                mContext.registerReceiver(
                    mReceiver,
                    intentFilter,
                    Context.RECEIVER_EXPORTED
                )
            } else {
                mContext.registerReceiver(
                    mReceiver,
                    intentFilter
                )
            }

            mBroadcastRegistered = true
        }

        mWeatherContainer = LinearLayout(mContext).apply {
            id = View.generateViewId()
            tag = ICONIFY_LOCKSCREEN_WEATHER_TAG
            layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val aodBurnInLayerClass =
            findClass("$SYSTEMUI_PACKAGE.keyguard.ui.view.layout.sections.AodBurnInLayer")
        var aodBurnInLayerHooked = false

        // Apparently ROMs like CrDroid doesn't even use AodBurnInLayer class
        // So we hook which ever is available
        val keyguardStatusViewClass = findClass(
            "com.android.keyguard.KeyguardStatusView",
            suppressError = Build.VERSION.SDK_INT >= 36
        )
        var keyguardStatusViewHooked = false

        fun initializeLockscreenLayout(param: XC_MethodHook.MethodHookParam) {
            val entryV = param.thisObject as View

            // If both are already hooked, return. We only want to hook one
            if (aodBurnInLayerHooked && keyguardStatusViewHooked) return

            entryV.addOnAttachStateChangeListener(object : OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        if (!mWeatherEnabled) return@postDelayed

                        val rootView = v.parent as? ViewGroup ?: return@postDelayed

                        // If rootView is not R.id.keyguard_root_view, detach and return
                        if (rootView.id != mContext.resources.getIdentifier(
                                "keyguard_root_view",
                                "id",
                                mContext.packageName
                            )
                        ) {
                            entryV.removeOnAttachStateChangeListener(this)
                            return@postDelayed
                        }

                        dateSmartSpaceViewAvailable = rootView.findViewById<View?>(
                            mContext.resources.getIdentifier(
                                "date_smartspace_view",
                                "id",
                                mContext.packageName
                            )
                        ) != null

                        mLockscreenRootView = rootView

                        (mWeatherContainer.parent as? ViewGroup)?.removeView(mWeatherContainer)

                        if (mLockscreenClockEnabled || mWidgetsEnabled) {
                            mLsItemsContainer = rootView.getLsItemsContainer()

                            // Add weather view after clock view if exists
                            mLsItemsContainer!!.addView(
                                mWeatherContainer,
                                if (mLsItemsContainer!!.findViewWithTag<View?>(
                                        ICONIFY_LOCKSCREEN_CLOCK_TAG
                                    ) != null
                                ) 1 else 0
                            )
                        } else {
                            mLockscreenRootView!!.addView(mWeatherContainer)
                        }

                        applyLayoutConstraints(mLsItemsContainer ?: mWeatherContainer)
                        aodBurnInProtection = AodBurnInProtection.registerForView(
                            mLsItemsContainer ?: mWeatherContainer
                        )

                        placeWeatherView()
                    }, 1000)
                }

                override fun onViewDetachedFromWindow(v: View) {}
            })
        }

        aodBurnInLayerClass
            .hookConstructor()
            .runAfter { param ->
                if (!mWeatherEnabled) return@runAfter

                aodBurnInLayerHooked = true

                initializeLockscreenLayout(param)
            }

        keyguardStatusViewClass
            .hookConstructor()
            .runAfter { param ->
                if (!mWeatherEnabled) return@runAfter

                keyguardStatusViewHooked = true

                initializeLockscreenLayout(param)
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
                if (!mWeatherEnabled) return@runAfter

                val constraintSet = param.args[0]

                constraintSet.clear(
                    notificationContainerId,
                    ConstraintSet.TOP
                )
                constraintSet.connect(
                    notificationContainerId,
                    ConstraintSet.TOP,
                    (mLsItemsContainer ?: mWeatherContainer).id,
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
                if (!mWeatherEnabled) return@runAfter

                val constraintSet = param.args[0]

                val smartSpaceViewId = if (dateSmartSpaceViewAvailable) {
                    dateSmartSpaceViewId
                } else {
                    // Some ROMs don't have date smartspace view
                    bcSmartSpaceViewId
                }

                // Connect weather view to bottom of date smartspace
                if (!mLockscreenClockEnabled && mWidgetsEnabled && mLsItemsContainer != null) {
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
                } else if (!mLockscreenClockEnabled && !mWidgetsEnabled) {
                    constraintSet.clear(
                        mWeatherContainer.id,
                        ConstraintSet.TOP
                    )
                    constraintSet.connect(
                        mWeatherContainer.id,
                        ConstraintSet.TOP,
                        smartSpaceViewId,
                        ConstraintSet.BOTTOM
                    )
                }
            }

        DozeCallback.getInstance().registerDozeChangeListener(
            object : DozeCallback.DozeListener {
                override fun onDozingStarted() {
                    aodBurnInProtection?.setMovementEnabled(true)
                }

                override fun onDozingStopped() {
                    aodBurnInProtection?.setMovementEnabled(false)
                }
            }
        )

        // For unknown reason, rotating device makes the height of view to 0
        // This is a workaround to make sure the view is visible
        fun updateLayoutParams() {
            if (!mWeatherEnabled) return

            if (::mWeatherContainer.isInitialized) {
                (mLsItemsContainer ?: mWeatherContainer).layoutParams.apply {
                    width = ViewGroup.LayoutParams.MATCH_PARENT
                    height = ViewGroup.LayoutParams.WRAP_CONTENT
                }
            }
        }

        val statusBarKeyguardViewManagerClass =
            findClass("$SYSTEMUI_PACKAGE.statusbar.phone.StatusBarKeyguardViewManager")

        statusBarKeyguardViewManagerClass
            .hookMethod("onStartedWakingUp")
            .suppressError()
            .runAfter { updateLayoutParams() }

        val centralSurfacesImplClass = findClass(
            "$SYSTEMUI_PACKAGE.statusbar.phone.CentralSurfacesImpl",
            suppressError = true
        )

        centralSurfacesImplClass
            .hookMethod("onStartedWakingUp")
            .suppressError()
            .runAfter { updateLayoutParams() }

        centralSurfacesImplClass
            .hookConstructor()
            .runAfter { param ->
                if (!mWeatherEnabled) return@runAfter

                val mWakefulnessObserver = param.thisObject.getFieldSilently("mWakefulnessObserver")

                mWakefulnessObserver?.javaClass
                    .hookMethod("onStartedWakingUp")
                    .runAfter { updateLayoutParams() }
            }

        val dozeServiceClass = findClass("$SYSTEMUI_PACKAGE.doze.DozeService")

        dozeServiceClass
            .hookMethod("onDreamingStarted")
            .runAfter {
                coroutineScope.launch {
                    repeat(5) {
                        updateLayoutParams()
                        delay(500L)
                    }
                }
            }
    }

    @SuppressLint("DiscouragedApi")
    private fun placeWeatherView() {
        if (!isComposeLockscreen) return
        if (!mWeatherEnabled || mLockscreenRootView == null) return
        if (mLockscreenClockEnabled && !mLockscreenClockInflated) return

        val currentWeatherView: CurrentWeatherView = CurrentWeatherView.getInstance(
            mContext,
            LOCKSCREEN_WEATHER
        )

        if (currentWeatherView.parent != mWeatherContainer) {
            (currentWeatherView.parent as? ViewGroup)?.removeView(currentWeatherView)
            mWeatherContainer.addView(currentWeatherView)

            refreshWeatherView(currentWeatherView)
            applyLayoutConstraints(mLsItemsContainer ?: mWeatherContainer)

            // Weather placed, now inflate widgets
            val broadcast = Intent(ACTION_WEATHER_INFLATED)
            broadcast.setFlags(Intent.FLAG_RECEIVER_FOREGROUND)
            Thread { mContext.sendBroadcast(broadcast) }.start()
        }
    }

    @SuppressLint("DiscouragedApi")
    private fun applyLayoutConstraints(weatherView: ViewGroup) {
        if (mLockscreenRootView == null) return

        mLockscreenRootView.assignIdsToViews()

        weatherView.getChildAt(0)?.layoutParams?.width = LinearLayout.LayoutParams.MATCH_PARENT

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

            // Connect weather view to parent
            constraintSet.connect(
                weatherView.id,
                ConstraintSet.START,
                ConstraintSet.PARENT_ID,
                ConstraintSet.START
            )
            constraintSet.connect(
                weatherView.id,
                ConstraintSet.END,
                ConstraintSet.PARENT_ID,
                ConstraintSet.END
            )

            if ((weatherView == mWeatherContainer && !mLockscreenClockEnabled && !mWidgetsEnabled) ||
                (weatherView == mLsItemsContainer && !mLockscreenClockEnabled && mWidgetsEnabled)
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
                // then connect weather view to bottom of date smartspace
                constraintSet.connect(
                    weatherView.id,
                    ConstraintSet.TOP,
                    dateSmartspaceViewId,
                    ConstraintSet.BOTTOM
                )
            } else if (weatherView == mLsItemsContainer && mLockscreenClockEnabled) {
                // If custom clock enabled, then connect whole container to top of parent
                constraintSet.connect(
                    weatherView.id,
                    ConstraintSet.TOP,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.TOP
                )
            }

            // Connect notification container below weather
            if (notificationContainerId != 0) {
                constraintSet.clear(
                    notificationContainerId,
                    ConstraintSet.TOP
                )
                constraintSet.connect(
                    notificationContainerId,
                    ConstraintSet.TOP,
                    weatherView.id,
                    ConstraintSet.BOTTOM
                )
            }

            // Connect aod notification icon container below weather
            if (aodNotificationIconContainerId != 0) {
                constraintSet.clear(
                    aodNotificationIconContainerId,
                    ConstraintSet.TOP
                )
                constraintSet.connect(
                    aodNotificationIconContainerId,
                    ConstraintSet.TOP,
                    weatherView.id,
                    ConstraintSet.BOTTOM
                )
            }

            constraintSet.applyTo(mLockscreenRootView!!)
        }
    }

    private fun refreshWeatherView(currentWeatherView: CurrentWeatherView?) {
        if (currentWeatherView == null) return

        applyLayoutConstraints(mLsItemsContainer ?: mWeatherContainer)

        currentWeatherView.apply {
            updateSizes(
                weatherTextSize,
                weatherImageSize,
                LOCKSCREEN_WEATHER
            )
            updateColors(
                if (weatherCustomColor) weatherColor else Color.WHITE,
                LOCKSCREEN_WEATHER
            )
            updateWeatherSettings(
                weatherShowLocation,
                weatherShowCondition,
                weatherShowHumidity,
                weatherShowWind,
                LOCKSCREEN_WEATHER
            )
            visibility = if (mWeatherEnabled) View.VISIBLE else View.GONE
            updateWeatherBg(
                mWeatherBackground,
                LOCKSCREEN_WEATHER
            )
        }

        updateMargins()
        updateFont()
    }

    private fun updateMargins() {
        val childView = mWeatherContainer.getChildAt(0) as LinearLayout

        mWeatherContainer.layoutParams.width = if (mLsItemsContainer == null) {
            0
        } else {
            LinearLayout.LayoutParams.MATCH_PARENT
        }

        setMargins(
            childView,
            mContext,
            mSideMargin,
            mTopMargin,
            mSideMargin,
            mBottomMargin
        )

        mWeatherContainer.gravity = if (mCenterWeather) Gravity.CENTER_HORIZONTAL else Gravity.START
        childView.gravity = if (mCenterWeather) Gravity.CENTER_HORIZONTAL else Gravity.START
        (mWeatherContainer.getChildAt(0) as LinearLayout?)?.children?.forEach {
            (it as LinearLayout).gravity = if (mCenterWeather) {
                Gravity.CENTER_HORIZONTAL
            } else {
                Gravity.START or Gravity.CENTER_VERTICAL
            }
        }
    }

    private fun updateFont() {
        if (mCustomFontEnabled && File(mCustomFontLocation).exists()) {
            Typeface.createFromFile(File(mCustomFontLocation))
        } else {
            Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }.also { typeface ->
            applyFontRecursively(mWeatherContainer, typeface)
        }
    }

    private fun updateWeatherView() {
        if (isComposeLockscreen) {
            refreshWeatherView(CurrentWeatherView.getInstance(LOCKSCREEN_WEATHER))
        }
    }

    companion object {
        const val LOCKSCREEN_WEATHER = "iconify_ls_weather"
    }
}
