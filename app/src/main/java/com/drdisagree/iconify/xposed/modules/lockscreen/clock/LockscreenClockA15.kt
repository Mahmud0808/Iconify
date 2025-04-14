package com.drdisagree.iconify.xposed.modules.lockscreen.clock

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.UserHandle
import android.os.UserManager
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnAttachStateChangeListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextClock
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.graphics.drawable.toDrawable
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.R
import com.drdisagree.iconify.data.common.Const.ACTION_LS_CLOCK_INFLATED
import com.drdisagree.iconify.data.common.Const.RESET_LOCKSCREEN_CLOCK_COMMAND
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.common.Preferences.ICONIFY_LOCKSCREEN_CLOCK_TAG
import com.drdisagree.iconify.data.common.Preferences.LSCLOCK_BOTTOMMARGIN
import com.drdisagree.iconify.data.common.Preferences.LSCLOCK_COLOR_CODE_ACCENT1
import com.drdisagree.iconify.data.common.Preferences.LSCLOCK_COLOR_CODE_ACCENT2
import com.drdisagree.iconify.data.common.Preferences.LSCLOCK_COLOR_CODE_ACCENT3
import com.drdisagree.iconify.data.common.Preferences.LSCLOCK_COLOR_CODE_TEXT1
import com.drdisagree.iconify.data.common.Preferences.LSCLOCK_COLOR_CODE_TEXT2
import com.drdisagree.iconify.data.common.Preferences.LSCLOCK_COLOR_SWITCH
import com.drdisagree.iconify.data.common.Preferences.LSCLOCK_DEVICENAME
import com.drdisagree.iconify.data.common.Preferences.LSCLOCK_FONT_LINEHEIGHT
import com.drdisagree.iconify.data.common.Preferences.LSCLOCK_FONT_SWITCH
import com.drdisagree.iconify.data.common.Preferences.LSCLOCK_FONT_TEXT_SCALING
import com.drdisagree.iconify.data.common.Preferences.LSCLOCK_IMAGE_SWITCH
import com.drdisagree.iconify.data.common.Preferences.LSCLOCK_STYLE
import com.drdisagree.iconify.data.common.Preferences.LSCLOCK_SWITCH
import com.drdisagree.iconify.data.common.Preferences.LSCLOCK_TOPMARGIN
import com.drdisagree.iconify.data.common.Preferences.LSCLOCK_USERNAME
import com.drdisagree.iconify.data.common.Resources.LOCKSCREEN_CLOCK_LAYOUT
import com.drdisagree.iconify.data.common.XposedConst.LSCLOCK_FONT_FILE
import com.drdisagree.iconify.data.common.XposedConst.LSCLOCK_IMAGE1_FILE
import com.drdisagree.iconify.data.common.XposedConst.LSCLOCK_IMAGE2_FILE
import com.drdisagree.iconify.utils.TextUtils
import com.drdisagree.iconify.xposed.HookEntry.Companion.enqueueProxyCommand
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.callbacks.BootCallback
import com.drdisagree.iconify.xposed.modules.extras.callbacks.DozeCallback
import com.drdisagree.iconify.xposed.modules.extras.callbacks.ThemeChangeCallback
import com.drdisagree.iconify.xposed.modules.extras.utils.MyConstraintSet.Companion.applyTo
import com.drdisagree.iconify.xposed.modules.extras.utils.MyConstraintSet.Companion.clear
import com.drdisagree.iconify.xposed.modules.extras.utils.MyConstraintSet.Companion.clone
import com.drdisagree.iconify.xposed.modules.extras.utils.MyConstraintSet.Companion.connect
import com.drdisagree.iconify.xposed.modules.extras.utils.MyConstraintSet.Companion.constrainHeight
import com.drdisagree.iconify.xposed.modules.extras.utils.MyConstraintSet.Companion.constrainWidth
import com.drdisagree.iconify.xposed.modules.extras.utils.MyConstraintSet.Companion.constraintSetInstance
import com.drdisagree.iconify.xposed.modules.extras.utils.MyConstraintSet.Companion.setVisibility
import com.drdisagree.iconify.xposed.modules.extras.utils.TimeUtils
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.applyFontRecursively
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.applyTextMarginRecursively
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.applyTextScalingRecursively
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.assignIdsToViews
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.findViewContainsTag
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.findViewWithTagAndChangeColor
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.getLsItemsContainer
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.hideView
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.reAddView
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.removeViewFromParent
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.setMargins
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getFieldSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookConstructor
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.log
import com.drdisagree.iconify.xposed.modules.extras.views.AodBurnInProtection
import com.drdisagree.iconify.xposed.modules.extras.views.ArcProgressImageView
import com.drdisagree.iconify.xposed.modules.lockscreen.Lockscreen.Companion.isComposeLockscreen
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import com.drdisagree.iconify.xposed.utils.XPrefs.XprefsIsInitialized
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@SuppressLint("DiscouragedApi")
class LockscreenClockA15(context: Context) : ModPack(context) {

    private var showLockscreenClock = false
    private var mLockscreenRootView: ViewGroup? = null
    private var mLsItemsContainer: LinearLayout? = null
    private var mUserManager: UserManager? = null
    private var mAudioManager: AudioManager? = null
    private var mActivityManager: ActivityManager? = null
    private var mBatteryStatusView: TextView? = null
    private var mBatteryLevelView: TextView? = null
    private var mVolumeLevelView: TextView? = null
    private var mBatteryProgress: ProgressBar? = null
    private var mVolumeProgress: ProgressBar? = null
    private var mBatteryStatus = 1
    private var mBatteryPercentage = 1
    private var mVolumeLevelArcProgress: ArcProgressImageView? = null
    private var mRamUsageArcProgress: ArcProgressImageView? = null
    private var mBatteryLevelArcProgress: ArcProgressImageView? = null
    private var mTemperatureArcProgress: ArcProgressImageView? = null
    private var mAccentColor1 = 0
    private var mAccentColor2 = 0
    private var mAccentColor3 = 0
    private var mTextColor1 = Color.WHITE
    private var mTextColor2 = Color.BLACK
    private var mSystemAccent = 0
    private var clockStyle = 0
    private var topMargin = 100
    private var bottomMargin = 40
    private var textScaleFactor = 1f
    private var reduceFactor = 1f
    private var lineHeight = 0
    private var customColorEnabled = false
    private var customUserName = ""
    private var customDeviceName = ""
    private var customFontEnabled = false
    private var customImageEnabled = false
    private var currentClockView: View? = null

    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())
    private var aodBurnInProtection: AodBurnInProtection? = null

    private val mBatteryReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != null && intent.action == Intent.ACTION_BATTERY_CHANGED) {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)

                mBatteryStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 1)
                mBatteryPercentage = level * 100 / scale

                initBatteryStatus()
            }
        }
    }
    private val mVolumeReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            initSoundManager()
        }
    }
    private val mThemeChangeCallback: ThemeChangeCallback.OnThemeChangedListener =
        object : ThemeChangeCallback.OnThemeChangedListener {
            override fun onThemeChanged() {
                loadColors()
                updateClockView()
            }
        }
    private val timeChangedReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Handler(Looper.getMainLooper()).post { updateClockView() }
        }
    }

    init {
        ThemeChangeCallback.getInstance().registerThemeChangedCallback(mThemeChangeCallback)
    }

    override fun updatePrefs(vararg key: String) {
        if (!XprefsIsInitialized || !isComposeLockscreen) return

        Xprefs.apply {
            showLockscreenClock = getBoolean(LSCLOCK_SWITCH, false)
            clockStyle = getInt(LSCLOCK_STYLE, 0)
            topMargin = getSliderInt(LSCLOCK_TOPMARGIN, 100)
            bottomMargin = getSliderInt(LSCLOCK_BOTTOMMARGIN, 40)
            textScaleFactor = getSliderInt(LSCLOCK_FONT_TEXT_SCALING, 10) / 10.0f
            reduceFactor = if (textScaleFactor > 1f) 0.91f else 1f
            lineHeight = getSliderInt(LSCLOCK_FONT_LINEHEIGHT, 0)
            customColorEnabled = getBoolean(LSCLOCK_COLOR_SWITCH, false)
            customUserName = getString(LSCLOCK_USERNAME, "")!!
            customDeviceName = getString(LSCLOCK_DEVICENAME, "")!!
            customImageEnabled = getBoolean(LSCLOCK_IMAGE_SWITCH, false)
            customFontEnabled = getBoolean(LSCLOCK_FONT_SWITCH, false)
        }

        resetStockClock()

        when (key.firstOrNull()) {
            in setOf(
                LSCLOCK_SWITCH,
                LSCLOCK_STYLE,
                LSCLOCK_FONT_SWITCH,
                LSCLOCK_COLOR_SWITCH,
                LSCLOCK_FONT_LINEHEIGHT,
                LSCLOCK_FONT_TEXT_SCALING
            ) -> updateClockView(true)

            in setOf(
                LSCLOCK_IMAGE_SWITCH,
                LSCLOCK_USERNAME,
                LSCLOCK_DEVICENAME
            ) -> modifyClockView(currentClockView)

            in setOf(
                LSCLOCK_TOPMARGIN,
                LSCLOCK_BOTTOMMARGIN
            ) -> {
                mLsItemsContainer?.let { applyLayoutConstraints(it) }
                modifyClockView(currentClockView)
            }

            in setOf(
                LSCLOCK_COLOR_CODE_ACCENT1,
                LSCLOCK_COLOR_CODE_ACCENT2,
                LSCLOCK_COLOR_CODE_ACCENT3,
                LSCLOCK_COLOR_CODE_TEXT1,
                LSCLOCK_COLOR_CODE_TEXT2
            ) -> {
                loadColors()
                modifyClockView(currentClockView)
            }
        }
    }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        if (!isComposeLockscreen) return

        initResources(mContext)

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
                        if (!showLockscreenClock) return@postDelayed

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

                        mLockscreenRootView = rootView

                        mLsItemsContainer = rootView.getLsItemsContainer()
                        aodBurnInProtection =
                            AodBurnInProtection.registerForView(mLsItemsContainer!!)
                        aodBurnInProtection!!.setMovementEnabled(true)
                        applyLayoutConstraints(mLsItemsContainer!!)

                        // Hide stock clock
                        listOf(
                            "bc_smartspace_view",
                            "date_smartspace_view",
                            "lockscreen_clock_view",
                            "lockscreen_clock_view_large",
                            "keyguard_slice_view"
                        ).map { resourceName ->
                            val resourceId = mContext.resources.getIdentifier(
                                resourceName,
                                "id",
                                mContext.packageName
                            )
                            if (resourceId != -1) {
                                rootView.findViewById<View?>(resourceId)
                            } else {
                                null
                            }
                        }.forEach { view ->
                            view.hideView()
                        }

                        registerClockUpdater()
                    }, 1000)
                }

                override fun onViewDetachedFromWindow(v: View) {
                    unregisterClockUpdater()
                }
            })
        }

        aodBurnInLayerClass
            .hookConstructor()
            .runAfter { param ->
                if (!showLockscreenClock) return@runAfter

                aodBurnInLayerHooked = true

                initializeLockscreenLayout(param)
            }

        keyguardStatusViewClass
            .hookConstructor()
            .runAfter { param ->
                if (!showLockscreenClock) return@runAfter

                keyguardStatusViewHooked = true

                initializeLockscreenLayout(param)
            }

        // Hide stock clock for ROMs with MigrateClocksToBlueprint disabled
        val keyguardClockSwitchClass = findClass(
            "com.android.keyguard.KeyguardClockSwitch",
            suppressError = Build.VERSION.SDK_INT >= 36
        )

        keyguardClockSwitchClass
            .hookMethod("onFinishInflate")
            .runAfter { param ->
                if (!showLockscreenClock) return@runAfter

                val parent = param.thisObject as ViewGroup

                parent.findViewById<View?>(
                    mContext.resources.getIdentifier(
                        "lockscreen_clock_view",
                        "id",
                        SYSTEMUI_PACKAGE
                    )
                ).hideView()

                parent.findViewById<View?>(
                    mContext.resources.getIdentifier(
                        "lockscreen_clock_view_large",
                        "id",
                        SYSTEMUI_PACKAGE
                    )
                ).hideView()
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
                if (!showLockscreenClock) return@runAfter

                val constraintSet = param.args[0]

                if (mLsItemsContainer != null) {
                    constraintSet.clear(
                        notificationContainerId,
                        ConstraintSet.TOP
                    )
                    constraintSet.connect(
                        notificationContainerId,
                        ConstraintSet.TOP,
                        mLsItemsContainer!!.id,
                        ConstraintSet.BOTTOM
                    )
                }
            }

        val aodNotificationIconsSectionClass =
            findClass("$SYSTEMUI_PACKAGE.keyguard.ui.view.layout.sections.AodNotificationIconsSection")

        val aodNotificationContainerId = mContext.resources.getIdentifier(
            "aod_notification_icon_container",
            "id",
            mContext.packageName
        )

        val keyguardStatusViewBottomMarginId = mContext.resources.getDimensionPixelSize(
            mContext.resources.getIdentifier(
                "keyguard_status_view_bottom_margin",
                "dimen",
                mContext.packageName
            )
        )

        aodNotificationIconsSectionClass
            .hookMethod("applyConstraints")
            .runAfter { param ->
                if (!showLockscreenClock) return@runAfter

                val constraintSet = param.args[0]

                if (mLsItemsContainer != null) {
                    constraintSet.clear(
                        aodNotificationContainerId,
                        ConstraintSet.TOP
                    )
                    constraintSet.connect(
                        aodNotificationContainerId,
                        ConstraintSet.TOP,
                        mLsItemsContainer!!.id,
                        ConstraintSet.BOTTOM,
                        keyguardStatusViewBottomMarginId
                    )
                }
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
        val keyguardSliceViewId = mContext.resources.getIdentifier(
            "keyguard_slice_view",
            "id",
            mContext.packageName
        )
        val smartspaceViewIds = listOf(
            bcSmartSpaceViewId,
            dateSmartSpaceViewId,
            keyguardSliceViewId
        ).filter { it != -1 }

        smartspaceSectionClass
            .hookMethod("applyConstraints")
            .runAfter { param ->
                if (!showLockscreenClock) return@runAfter

                val constraintSet = param.args[0]

                // Connect bc smartspace to bottom of date smartspace
                constraintSet.clear(
                    bcSmartSpaceViewId,
                    ConstraintSet.TOP
                )
                constraintSet.connect(
                    bcSmartSpaceViewId,
                    ConstraintSet.TOP,
                    dateSmartSpaceViewId,
                    ConstraintSet.BOTTOM
                )

                // Connect date smartspace to bottom of clock container
                if (mLsItemsContainer != null) {
                    constraintSet.clear(
                        dateSmartSpaceViewId,
                        ConstraintSet.TOP
                    )
                    constraintSet.connect(
                        dateSmartSpaceViewId,
                        ConstraintSet.TOP,
                        mLsItemsContainer!!.id,
                        ConstraintSet.BOTTOM
                    )
                }

                // Hide smartspace views
                smartspaceViewIds.forEach { viewId ->
                    constraintSet.constrainHeight(viewId, 0)
                    constraintSet.constrainWidth(viewId, 0)
                    constraintSet.setVisibility(viewId, ConstraintSet.INVISIBLE)
                }

                smartspaceViewIds.map { resourceId ->
                    mLockscreenRootView?.findViewById<View?>(resourceId)
                }.forEach { view ->
                    view.hideView()
                }
            }

        val keyguardUpdateMonitor = findClass("com.android.keyguard.KeyguardUpdateMonitor")

        keyguardUpdateMonitor
            .hookMethod("registerCallback")
            .parameters("com.android.keyguard.KeyguardUpdateMonitorCallback")
            .runAfter { param ->
                if (!showLockscreenClock) return@runAfter

                val callback = param.args[0]

                callback.javaClass
                    .hookMethod(
                        "onTimeChanged",
                        "onTimeFormatChanged",
                        "onTimeZoneChanged"
                    )
                    .runAfter runAfter2@{
                        if (!showLockscreenClock) return@runAfter2

                        Handler(Looper.getMainLooper()).post { updateClockView() }
                    }
            }

        val dozeTriggersClass = findClass("$SYSTEMUI_PACKAGE.doze.DozeTriggers")

        dozeTriggersClass
            .hookMethod("gentleWakeUp")
            .runAfter {
                if (!showLockscreenClock) return@runAfter

                Handler(Looper.getMainLooper()).post { updateClockView() }
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
            if (!showLockscreenClock) return

            mLsItemsContainer?.layoutParams?.apply {
                width = ViewGroup.LayoutParams.MATCH_PARENT
                height = ViewGroup.LayoutParams.WRAP_CONTENT
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
                if (!showLockscreenClock) return@runAfter

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

        BootCallback.registerBootListener(
            object : BootCallback.BootListener {
                override fun onDeviceBooted() {
                    updateClockView(true)
                }
            }
        )
    }

    private fun initResources(context: Context) {
        Handler(Looper.getMainLooper()).post {
            mUserManager = context.getSystemService(Context.USER_SERVICE) as UserManager
        }
        mAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mActivityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        try {
            context.registerReceiver(mBatteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        } catch (_: Exception) {
        }

        try {
            context.registerReceiver(
                mVolumeReceiver,
                IntentFilter("android.media.VOLUME_CHANGED_ACTION")
            )
        } catch (_: Exception) {
        }

        loadColors()
    }

    // Broadcast receiver for updating clock
    private fun registerClockUpdater() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
            addAction(Intent.ACTION_LOCALE_CHANGED)
        }

        mContext.registerReceiver(timeChangedReceiver, filter)

        updateClockView()
    }

    private fun unregisterClockUpdater() {
        try {
            mContext.unregisterReceiver(timeChangedReceiver)
        } catch (_: Throwable) {
            // receiver was never registered
        }
    }

    @Synchronized
    private fun updateClockView(force: Boolean = false) {
        if (mLsItemsContainer == null) return

        val currentTime = System.currentTimeMillis()
        currentClockView = mLsItemsContainer!!.findViewWithTag(ICONIFY_LOCKSCREEN_CLOCK_TAG)
        val isClockAdded = currentClockView != null

        if (isClockAdded && currentTime - lastUpdated < THRESHOLD_TIME) {
            return
        } else {
            lastUpdated = currentTime
        }

        if (!isClockAdded || force) {
            currentClockView = clockViewLayout?.apply {
                tag = ICONIFY_LOCKSCREEN_CLOCK_TAG
                id = View.generateViewId()

                var currentClockView =
                    mLsItemsContainer!!.findViewWithTag<View?>(ICONIFY_LOCKSCREEN_CLOCK_TAG)
                while (currentClockView != null) {
                    currentClockView.removeViewFromParent()
                    currentClockView =
                        mLsItemsContainer!!.findViewWithTag(ICONIFY_LOCKSCREEN_CLOCK_TAG)
                }
                mLsItemsContainer!!.addView(this, 0)

                modifyClockView(this)
                updateScaling(this)
                initSoundManager()
                initBatteryStatus()

                // Clock placed, now inflate weather or widgets
                val broadcast = Intent(ACTION_LS_CLOCK_INFLATED)
                broadcast.setFlags(Intent.FLAG_RECEIVER_FOREGROUND)
                Thread { mContext.sendBroadcast(broadcast) }.start()
            }
        } else {
            currentClockView!!.apply {
                refreshTextClock()
                updateClockViewElements(this)
            }
        }
    }

    private val clockViewLayout: View?
        get() {
            if (!XprefsIsInitialized) return null

            return LayoutInflater.from(appContext).inflate(
                appContext.resources.getIdentifier(
                    LOCKSCREEN_CLOCK_LAYOUT + clockStyle,
                    "layout",
                    BuildConfig.APPLICATION_ID
                ),
                null
            )
        }

    private val allArcProgressImageViews: List<ArcProgressImageView?>
        get() = listOf(
            mVolumeLevelArcProgress,
            mRamUsageArcProgress,
            mBatteryLevelArcProgress,
            mTemperatureArcProgress
        )

    private fun modifyClockView(clockView: View?) {
        if (!XprefsIsInitialized || mLsItemsContainer == null || clockView == null) return

        var customTypeface = if (customFontEnabled && LSCLOCK_FONT_FILE.exists()) {
            Typeface.createFromFile(LSCLOCK_FONT_FILE)
        } else {
            null
        }

        clockView.layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT

        setMargins(clockView, mContext, 0, topMargin, 0, bottomMargin)

        if (customColorEnabled) {
            findViewWithTagAndChangeColor(clockView, "accent1", mAccentColor1)
            findViewWithTagAndChangeColor(clockView, "accent2", mAccentColor2)
            findViewWithTagAndChangeColor(clockView, "accent3", mAccentColor3)
            findViewWithTagAndChangeColor(clockView, "text1", mTextColor1)
            findViewWithTagAndChangeColor(clockView, "text2", mTextColor2)
        }

        customTypeface?.also {
            applyFontRecursively(clockView, it)
        }

        if (customImageEnabled) {
            listOf(
                "custom_image1" to LSCLOCK_IMAGE1_FILE.absolutePath,
                "custom_image2" to LSCLOCK_IMAGE2_FILE.absolutePath
            ).forEach { (tag, path) ->
                if (File(path).exists()) {
                    clockView.findViewContainsTag(tag)?.let { view ->
                        val bitmap = BitmapFactory.decodeFile(path)

                        val isRoundedImage = (clockStyle == 26 && tag.contains("1")) ||
                                clockStyle in setOf(27, 30, 40, 53)
                        val isCircleImage = (clockStyle == 26 && tag.contains("2")) ||
                                clockStyle == 39
                        val roundedSize = 32f

                        val drawable: Drawable = when {
                            isRoundedImage -> RoundedBitmapDrawableFactory
                                .create(mContext.resources, bitmap)
                                .apply {
                                    setCornerRadius(roundedSize)
                                }

                            isCircleImage -> RoundedBitmapDrawableFactory
                                .create(mContext.resources, bitmap)
                                .apply {
                                    setCornerRadius(12000f)
                                }

                            else -> bitmap.toDrawable(view.resources)
                        }

                        if (view is ImageView) {
                            view.setImageDrawable(drawable)
                        } else {
                            view.background = drawable
                        }
                    }
                }
            }
        }

        mBatteryLevelView = null
        mBatteryProgress = null
        mBatteryStatusView = null
        mVolumeLevelView = null
        mVolumeProgress = null
        mVolumeLevelArcProgress = null
        mRamUsageArcProgress = null

        clockView.apply {
            when (clockStyle) {
                5 -> {
                    mBatteryStatusView = findViewContainsTag("battery_status") as TextView?
                    mBatteryLevelView = findViewContainsTag("battery_percentage") as TextView?
                    mVolumeLevelView = findViewContainsTag("volume_level") as TextView?
                    mBatteryProgress = findViewContainsTag("battery_progressbar") as ProgressBar?
                    mVolumeProgress = findViewContainsTag("volume_progressbar") as ProgressBar?
                }

                19 -> {
                    mBatteryLevelView = findViewContainsTag("battery_percentage") as TextView?
                    mBatteryProgress = findViewContainsTag("battery_progressbar") as ProgressBar?
                    addArcProgressView("volume_progress")
                    addArcProgressView("ram_usage_info")
                }

                56 -> {
                    addArcProgressView("volume_progress")
                    addArcProgressView("ram_usage_info")
                    addArcProgressView("battery_progress_arc")
                    addArcProgressView("temperature_progress")
                }

                else -> {}
            }

            updateClockViewElements(this)
        }

        allArcProgressImageViews.forEach { arcProgressImageView ->
            arcProgressImageView?.apply {
                if (customColorEnabled) {
                    setColors(mTextColor1, mTextColor1)
                }
                customTypeface?.let { setTypeFace(it) }
            }
        }

        val deviceName = clockView.findViewContainsTag("device_name") as TextView?
        deviceName?.text = customDeviceName.ifEmpty { Build.MODEL }

        val usernameView = clockView.findViewContainsTag("username") as TextView?
        usernameView?.text = customUserName.ifEmpty { userName }

        val imageView = clockView.findViewContainsTag("profile_picture") as ImageView?
        userImage?.let { imageView?.setImageDrawable(it) }
    }

    private fun updateClockViewElements(clockView: View?) {
        if (clockView == null) return

        clockView.apply {
            if (clockStyle != 10) {
                TextUtils.convertTextViewsToTitleCase(this)
            }

            when (clockStyle) {
                2, 20 -> {
                    val tickIndicator = findViewContainsTag("tickIndicator") as TextClock
                    val hourView = findViewContainsTag("hours") as TextView

                    tickIndicator.setTextColor(Color.TRANSPARENT)
                    hourView.visibility = View.VISIBLE

                    TimeUtils.setCurrentTimeTextClockRed(
                        tickIndicator,
                        hourView,
                        if (customColorEnabled) mAccentColor1 else mSystemAccent
                    )
                }

                22 -> {
                    val hourView = findViewContainsTag("textHour") as TextView
                    val minuteView = findViewContainsTag("textMinute") as TextView
                    val tickIndicator = findViewContainsTag("tickIndicator") as TextClock

                    TimeUtils.setCurrentTimeTextClock(mContext, tickIndicator, hourView, minuteView)
                }
            }
        }
    }

    private fun updateScaling(clockView: View?) {
        if (clockView == null) return

        applyTextMarginRecursively(mContext, clockView, lineHeight)

        if (textScaleFactor != 1f) {
            applyTextScalingRecursively(clockView, textScaleFactor)

            allArcProgressImageViews.forEach { arcProgressImageView ->
                (arcProgressImageView?.parent as? ViewGroup)?.layoutParams?.apply {
                    width = (width * textScaleFactor * reduceFactor).toInt()
                    height = (height * textScaleFactor * reduceFactor).toInt()
                }
            }

            (mBatteryProgress?.parent as ViewGroup?)?.apply {
                layoutParams?.apply {
                    width = (width * textScaleFactor * reduceFactor).toInt()
                }
                requestLayout()
            }
        }
    }

    private fun View.addArcProgressView(parentTag: String) {
        val container = findViewContainsTag(parentTag) as LinearLayout?
        container?.setBackgroundResource(0)
        container?.removeAllViews()

        fun newArcProgress() = ArcProgressImageView(mContext).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        when (parentTag) {
            "volume_progress" -> {
                if (mVolumeLevelArcProgress == null) {
                    mVolumeLevelArcProgress = newArcProgress().apply {
                        setProgressType(ArcProgressImageView.ProgressType.VOLUME)
                    }
                }
                container?.reAddView(mVolumeLevelArcProgress)
            }

            "ram_usage_info" -> {
                if (mRamUsageArcProgress == null) {
                    mRamUsageArcProgress = newArcProgress().apply {
                        setProgressType(ArcProgressImageView.ProgressType.MEMORY)
                    }
                }
                container?.reAddView(mRamUsageArcProgress)
            }

            "battery_progress_arc" -> {
                if (mBatteryLevelArcProgress == null) {
                    mBatteryLevelArcProgress = newArcProgress().apply {
                        setProgressType(ArcProgressImageView.ProgressType.BATTERY)
                    }
                }
                container?.reAddView(mBatteryLevelArcProgress)
            }

            "temperature_progress" -> {
                if (mTemperatureArcProgress == null) {
                    mTemperatureArcProgress = newArcProgress().apply {
                        setProgressType(ArcProgressImageView.ProgressType.TEMPERATURE)
                    }
                }
                container?.reAddView(mTemperatureArcProgress)
            }
        }
    }

    private fun applyLayoutConstraints(containerView: ViewGroup) {
        if (mLockscreenRootView == null) return

        mLockscreenRootView.assignIdsToViews()

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

            // Connect clock view to parent
            constraintSet.connect(
                containerView.id,
                ConstraintSet.START,
                ConstraintSet.PARENT_ID,
                ConstraintSet.START
            )
            constraintSet.connect(
                containerView.id,
                ConstraintSet.END,
                ConstraintSet.PARENT_ID,
                ConstraintSet.END
            )
            constraintSet.connect(
                containerView.id,
                ConstraintSet.TOP,
                ConstraintSet.PARENT_ID,
                ConstraintSet.TOP
            )

            // Connect notification container below clock
            if (notificationContainerId != 0) {
                constraintSet.clear(
                    notificationContainerId,
                    ConstraintSet.TOP
                )
                constraintSet.connect(
                    notificationContainerId,
                    ConstraintSet.TOP,
                    containerView.id,
                    ConstraintSet.BOTTOM
                )
            }

            // Connect aod notification icon container below clock
            if (aodNotificationIconContainerId != 0) {
                constraintSet.clear(
                    aodNotificationIconContainerId,
                    ConstraintSet.TOP
                )
                constraintSet.connect(
                    aodNotificationIconContainerId,
                    ConstraintSet.TOP,
                    containerView.id,
                    ConstraintSet.BOTTOM
                )
            }

            constraintSet.applyTo(mLockscreenRootView!!)
        }
    }

    private fun loadColors() {
        if (!XprefsIsInitialized || !isComposeLockscreen) return

        Xprefs.apply {
            mAccentColor1 = getInt(
                LSCLOCK_COLOR_CODE_ACCENT1,
                mContext.resources.getColor(
                    mContext.resources.getIdentifier(
                        "android:color/system_accent1_300",
                        "color",
                        mContext.packageName
                    ), mContext.theme
                )
            )
            mAccentColor2 = getInt(
                LSCLOCK_COLOR_CODE_ACCENT2,
                mContext.resources.getColor(
                    mContext.resources.getIdentifier(
                        "android:color/system_accent2_300",
                        "color",
                        mContext.packageName
                    ), mContext.theme
                )
            )
            mAccentColor3 = getInt(
                LSCLOCK_COLOR_CODE_ACCENT3,
                mContext.resources.getColor(
                    mContext.resources.getIdentifier(
                        "android:color/system_accent3_300",
                        "color",
                        mContext.packageName
                    ), mContext.theme
                )
            )
            mTextColor1 = getInt(
                LSCLOCK_COLOR_CODE_TEXT1,
                Color.WHITE
            )
            mTextColor2 = getInt(
                LSCLOCK_COLOR_CODE_TEXT2,
                Color.BLACK
            )
            mSystemAccent = mContext.resources.getColor(
                mContext.resources.getIdentifier(
                    "android:color/system_accent1_300",
                    "color",
                    mContext.packageName
                ), mContext.theme
            )
        }
    }

    private fun initBatteryStatus() {
        if (mBatteryStatusView != null) {
            when (mBatteryStatus) {
                BatteryManager.BATTERY_STATUS_CHARGING -> {
                    mBatteryStatusView!!.setText(R.string.battery_charging)
                }

                BatteryManager.BATTERY_STATUS_DISCHARGING, BatteryManager.BATTERY_STATUS_NOT_CHARGING -> {
                    mBatteryStatusView!!.setText(R.string.battery_discharging)
                }

                BatteryManager.BATTERY_STATUS_FULL -> {
                    mBatteryStatusView!!.setText(R.string.battery_full)
                }

                BatteryManager.BATTERY_STATUS_UNKNOWN -> {
                    mBatteryStatusView!!.setText(R.string.battery_level_percentage)
                }
            }
        }

        mBatteryProgress?.progress = mBatteryPercentage

        mBatteryLevelView?.text = appContext.resources.getString(
            R.string.percentage_text,
            mBatteryPercentage
        )
    }

    private fun initSoundManager() {
        val volLevel = mAudioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVolLevel = mAudioManager!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val volPercent = (volLevel.toFloat() / maxVolLevel * 100).toInt()

        mVolumeProgress?.post {
            mVolumeProgress?.progress = volPercent
        }
        mVolumeLevelView?.post {
            mVolumeLevelView?.text = appContext.resources.getString(
                R.string.percentage_text,
                volPercent
            )
        }
    }

    private val userName: String
        @SuppressLint("MissingPermission")
        get() {
            if (mUserManager == null) {
                return "User"
            }

            val username = mUserManager!!.userName

            return if (username.isNotEmpty()) mUserManager!!.userName
            else appContext.resources.getString(R.string.default_user_name)
        }

    private val userImage: Drawable?
        get() = if (mUserManager == null) {
            ResourcesCompat.getDrawable(
                appContext.resources,
                R.drawable.default_avatar,
                appContext.theme
            )
        } else try {
            val getUserIconMethod = mUserManager!!.javaClass
                .getMethod("getUserIcon", Int::class.javaPrimitiveType)
            val userId = UserHandle::class.java.getDeclaredMethod("myUserId").invoke(null) as Int
            val bitmapUserIcon = getUserIconMethod.invoke(mUserManager, userId) as Bitmap

            bitmapUserIcon.toDrawable(mContext.resources)
        } catch (throwable: Throwable) {
            if (throwable !is NullPointerException) {
                log(this@LockscreenClockA15, throwable)
            }

            ResourcesCompat.getDrawable(
                appContext.resources,
                R.drawable.default_avatar,
                appContext.theme
            )
        }

    private fun View.refreshTextClock() {
        if (this !is ViewGroup) return

        for (i in 0 until childCount) {
            when (val child = getChildAt(i)) {
                is TextClock -> {
                    child.apply {
                        format12Hour = format12Hour
                        format24Hour = format24Hour
                        invalidate()
                        requestLayout()
                    }
                }

                is TextView -> {
                    child.apply {
                        text = text
                        invalidate()
                        requestLayout()
                    }
                }

                is ViewGroup -> child.refreshTextClock()
            }
        }
    }

    private fun resetStockClock() {
        if (showLockscreenClock) {
            enqueueProxyCommand { proxy ->
                proxy.runCommand(RESET_LOCKSCREEN_CLOCK_COMMAND)
            }
        }
    }

    companion object {
        private var lastUpdated = System.currentTimeMillis()
        private const val THRESHOLD_TIME: Long = 500 // milliseconds
    }
}