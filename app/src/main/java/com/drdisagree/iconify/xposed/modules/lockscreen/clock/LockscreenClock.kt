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
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextClock
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.graphics.drawable.toDrawable
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.R
import com.drdisagree.iconify.data.common.Const.RESET_LOCKSCREEN_CLOCK_COMMAND
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.common.Preferences.DEPTH_WALLPAPER_FADE_ANIMATION
import com.drdisagree.iconify.data.common.Preferences.DEPTH_WALLPAPER_SWITCH
import com.drdisagree.iconify.data.common.Preferences.ICONIFY_DEPTH_WALLPAPER_TAG
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
import com.drdisagree.iconify.data.common.Preferences.LSCLOCK_MOVE_NOTIFICATION_ICONS
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
import com.drdisagree.iconify.xposed.modules.extras.callbacks.ThemeChangeCallback
import com.drdisagree.iconify.xposed.modules.extras.utils.TimeUtils
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.applyFontRecursively
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.applyTextMarginRecursively
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.applyTextScalingRecursively
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.findViewContainsTag
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.findViewWithTagAndChangeColor
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.hideView
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.reAddView
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.setMargins
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getFieldSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.log
import com.drdisagree.iconify.xposed.modules.extras.views.ArcProgressImageView
import com.drdisagree.iconify.xposed.modules.lockscreen.Lockscreen.Companion.isComposeLockscreen
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import com.drdisagree.iconify.xposed.utils.XPrefs.XprefsIsInitialized
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@SuppressLint("DiscouragedApi")
class LockscreenClock(context: Context) : ModPack(context) {

    private val isAndroid13OrBelow = Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU
    private var showLockscreenClock = false
    private var showDepthWallpaper = false // was used in android 13 and below
    private var moveNotificationIcons = !isAndroid13OrBelow
    private var mClockViewContainer: ViewGroup? = null
    private var mStatusViewContainer: ViewGroup? = null
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
    private var customFontEnabled: Boolean = false
    private var customImageEnabled = false

    init {
        ThemeChangeCallback.getInstance().registerThemeChangedCallback(mThemeChangeCallback)
    }

    override fun updatePrefs(vararg key: String) {
        if (!XprefsIsInitialized || isComposeLockscreen) return

        Xprefs.apply {
            showLockscreenClock = getBoolean(LSCLOCK_SWITCH, false)
            showDepthWallpaper = isAndroid13OrBelow && getBoolean(DEPTH_WALLPAPER_SWITCH, false)
            moveNotificationIcons = getBoolean(LSCLOCK_MOVE_NOTIFICATION_ICONS, !isAndroid13OrBelow)
            customImageEnabled = getBoolean(LSCLOCK_IMAGE_SWITCH, false)
            customFontEnabled = getBoolean(LSCLOCK_FONT_SWITCH, false)
        }

        resetStockClock()

        when (key.firstOrNull()) {
            in setOf(
                LSCLOCK_SWITCH,
                LSCLOCK_COLOR_SWITCH,
                LSCLOCK_STYLE,
                LSCLOCK_TOPMARGIN,
                LSCLOCK_BOTTOMMARGIN,
                LSCLOCK_FONT_LINEHEIGHT,
                LSCLOCK_FONT_SWITCH,
                LSCLOCK_IMAGE_SWITCH,
                LSCLOCK_FONT_TEXT_SCALING,
                LSCLOCK_USERNAME,
                LSCLOCK_DEVICENAME
            ) -> updateClockView()

            in setOf(
                LSCLOCK_COLOR_CODE_ACCENT1,
                LSCLOCK_COLOR_CODE_ACCENT2,
                LSCLOCK_COLOR_CODE_ACCENT3,
                LSCLOCK_COLOR_CODE_TEXT1,
                LSCLOCK_COLOR_CODE_TEXT2
            ) -> {
                loadColors()
                updateClockView()
            }

            in setOf(
                DEPTH_WALLPAPER_SWITCH,
                DEPTH_WALLPAPER_FADE_ANIMATION
            ) -> {
                if (isAndroid13OrBelow) {
                    updateClockView()
                }
            }
        }
    }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        if (isComposeLockscreen) return

        initResources(mContext)

        val keyguardStatusViewClass = findClass("com.android.keyguard.KeyguardStatusView")

        keyguardStatusViewClass
            .hookMethod("onFinishInflate")
            .runAfter { param ->
                if (!showLockscreenClock) return@runAfter

                mStatusViewContainer =
                    param.thisObject.getFieldSilently("mStatusViewContainer") as? ViewGroup
                        ?: (param.thisObject as ViewGroup).findViewById(
                            mContext.resources.getIdentifier(
                                "status_view_container",
                                "id",
                                mContext.packageName
                            )
                        )

                if (!showDepthWallpaper) {
                    mClockViewContainer = mStatusViewContainer
                }

                val aodNotificationIconContainer = (param.thisObject as ViewGroup)
                    .findViewById<View?>(
                        mContext.resources.getIdentifier(
                            "left_aligned_notification_icon_container",
                            "id",
                            mContext.packageName
                        )
                    )

                if (aodNotificationIconContainer != null) {
                    if (moveNotificationIcons) {
                        (aodNotificationIconContainer.parent as? ViewGroup)
                            ?.removeView(aodNotificationIconContainer)
                        mClockViewContainer?.addView(aodNotificationIconContainer, -1)
                    } else {
                        aodNotificationIconContainer.hideView()
                    }
                }

                // Hide stock clock
                (param.thisObject as GridLayout).findViewById<RelativeLayout>(
                    mContext.resources.getIdentifier(
                        "keyguard_clock_container",
                        "id",
                        mContext.packageName
                    )
                ).apply {
                    layoutParams.height = 0
                    layoutParams.width = 0
                    visibility = View.INVISIBLE
                }

                val mMediaHostContainer =
                    param.thisObject.getFieldSilently("mMediaHostContainer") as? View
                        ?: (param.thisObject as ViewGroup).findViewById(
                            mContext.resources.getIdentifier(
                                "status_view_media_container",
                                "id",
                                mContext.packageName
                            )
                        )

                mMediaHostContainer.apply {
                    layoutParams.height = 0
                    layoutParams.width = 0
                    visibility = View.INVISIBLE
                }

                registerClockUpdater()
            }

        val keyguardBottomAreaViewClass =
            findClass("$SYSTEMUI_PACKAGE.statusbar.phone.KeyguardBottomAreaView")

        keyguardBottomAreaViewClass
            .hookMethod("onFinishInflate")
            .runAfter { param ->
                if (!showLockscreenClock || !showDepthWallpaper) return@runAfter

                val view = param.thisObject as View
                val mIndicationArea = view.findViewById<ViewGroup>(
                    mContext.resources.getIdentifier(
                        "keyguard_indication_area",
                        "id",
                        mContext.packageName
                    )
                )

                // Get the depth wallpaper layout and register clock updater
                try {
                    val executor = Executors.newSingleThreadScheduledExecutor()
                    executor.scheduleAtFixedRate({
                        mClockViewContainer = mIndicationArea.findViewWithTag(
                            ICONIFY_DEPTH_WALLPAPER_TAG
                        )

                        if (mClockViewContainer != null) {
                            registerClockUpdater()
                            executor.shutdown()
                            executor.shutdownNow()
                        }

                        if (!showLockscreenClock || !showDepthWallpaper) {
                            executor.shutdown()
                            executor.shutdownNow()
                        }
                    }, 0, 200, TimeUnit.MILLISECONDS)
                } catch (_: Throwable) {
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

        val legacyNotificationIconAreaControllerImplClass = findClass(
            "$SYSTEMUI_PACKAGE.statusbar.phone.LegacyNotificationIconAreaControllerImpl",
            "$SYSTEMUI_PACKAGE.statusbar.phone.NotificationIconAreaController",
            suppressError = true
        )

        legacyNotificationIconAreaControllerImplClass
            .hookMethod("setupAodIcons")
            .runBefore { param ->
                if (!showLockscreenClock || !moveNotificationIcons) return@runBefore

                if (param.args[0] == null && mClockViewContainer != null) {
                    param.args[0] = mClockViewContainer!!.findViewById(
                        mContext.resources.getIdentifier(
                            "left_aligned_notification_icon_container",
                            "id",
                            mContext.packageName
                        )
                    )
                }
            }

        val dozeTriggersClass = findClass("$SYSTEMUI_PACKAGE.doze.DozeTriggers")

        dozeTriggersClass
            .hookMethod("gentleWakeUp")
            .runAfter {
                if (!showLockscreenClock) return@runAfter

                Handler(Looper.getMainLooper()).post { updateClockView() }
            }

        val ntWidgetContainerControllerClass = findClass(
            "com.nothing.systemui.widget.NTWidgetContainerController",
            suppressError = true
        )

        ntWidgetContainerControllerClass
            .hookMethod("updateWidgetViewPosistion")
            .runBefore { param ->
                if (!showLockscreenClock) return@runBefore

                param.result = null
            }

        BootCallback.registerBootListener(
            object : BootCallback.BootListener {
                override fun onDeviceBooted() {
                    updateClockView()
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

        val timeChangedReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Handler(Looper.getMainLooper()).post { updateClockView() }
            }
        }

        mContext.registerReceiver(timeChangedReceiver, filter)

        updateClockView()
    }

    private fun updateClockView() {
        if (mClockViewContainer == null) return

        val currentTime = System.currentTimeMillis()
        val isClockAdded =
            mClockViewContainer!!.findViewWithTag<View?>(ICONIFY_LOCKSCREEN_CLOCK_TAG) != null
        val isDepthClock = mClockViewContainer!!.tag === ICONIFY_DEPTH_WALLPAPER_TAG &&
                Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU

        if (isClockAdded && currentTime - lastUpdated < THRESHOLD_TIME) {
            return
        } else {
            lastUpdated = currentTime
        }

        val clockView = clockView

        // Remove existing clock view
        if (isClockAdded) {
            mClockViewContainer!!.removeView(
                mClockViewContainer!!.findViewWithTag(
                    ICONIFY_LOCKSCREEN_CLOCK_TAG
                )
            )
        }

        if (clockView != null) {
            clockView.tag = ICONIFY_LOCKSCREEN_CLOCK_TAG
            var idx = 0
            var dummyLayout: LinearLayout? = null
            if (isDepthClock) {
                /*
                 If the clock view container is the depth wallpaper container, we need to
                 add the clock view to the middle of foreground and background images
                 */
                if (mClockViewContainer!!.childCount > 1) {
                    idx = 1
                }

                // Add a dummy layout to the status view container so that we can still move notifications
                if (mStatusViewContainer != null) {
                    val dummyTag = "dummy_layout"
                    dummyLayout = mStatusViewContainer!!.findViewWithTag(dummyTag)

                    if (dummyLayout == null) {
                        dummyLayout = LinearLayout(mContext)
                        dummyLayout.layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            350
                        )
                        dummyLayout.tag = dummyTag

                        mStatusViewContainer!!.addView(dummyLayout, 0)
                    }
                }
            }

            if (clockView.parent != null) {
                (clockView.parent as ViewGroup).removeView(clockView)
            }

            mClockViewContainer!!.addView(clockView, idx)

            modifyClockView(clockView)
            initSoundManager()
            initBatteryStatus()

            if (isDepthClock && dummyLayout != null) {
                val dummyParams = dummyLayout.layoutParams as MarginLayoutParams
                val clockParams = clockView.layoutParams as MarginLayoutParams

                dummyParams.topMargin = clockParams.topMargin
                dummyParams.bottomMargin = clockParams.bottomMargin
                dummyLayout.layoutParams = dummyParams
            }
        }
    }

    private val clockView: View?
        get() {
            if (!XprefsIsInitialized) return null

            val inflater = LayoutInflater.from(appContext)
            val clockStyle: Int = Xprefs.getInt(LSCLOCK_STYLE, 0)

            val view: View = inflater.inflate(
                appContext.resources.getIdentifier(
                    LOCKSCREEN_CLOCK_LAYOUT + clockStyle,
                    "layout",
                    BuildConfig.APPLICATION_ID
                ),
                null
            )

            return view
        }

    private val allArcProgressImageViews: List<ArcProgressImageView?>
        get() = listOf(
            mVolumeLevelArcProgress,
            mRamUsageArcProgress,
            mBatteryLevelArcProgress,
            mTemperatureArcProgress
        )

    private fun modifyClockView(clockView: View) {
        val clockStyle: Int = Xprefs.getInt(LSCLOCK_STYLE, 0)
        val topMargin: Int = Xprefs.getSliderInt(LSCLOCK_TOPMARGIN, 100)
        val bottomMargin: Int = Xprefs.getSliderInt(LSCLOCK_BOTTOMMARGIN, 40)
        val textScaleFactor: Float =
            (Xprefs.getSliderInt(LSCLOCK_FONT_TEXT_SCALING, 10) / 10.0).toFloat()
        val reduceFactor = if (textScaleFactor > 1f) 0.91f else 1f
        val lineHeight: Int = Xprefs.getSliderInt(LSCLOCK_FONT_LINEHEIGHT, 0)
        val customColorEnabled: Boolean = Xprefs.getBoolean(LSCLOCK_COLOR_SWITCH, false)
        val customUserName: String = Xprefs.getString(LSCLOCK_USERNAME, "")!!
        val customDeviceName: String = Xprefs.getString(LSCLOCK_DEVICENAME, "")!!
        var customTypeface = if (customFontEnabled && LSCLOCK_FONT_FILE.exists()) {
            Typeface.createFromFile(LSCLOCK_FONT_FILE)
        } else {
            null
        }

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

        applyTextMarginRecursively(mContext, clockView, lineHeight)

        if (clockStyle != 10) {
            TextUtils.convertTextViewsToTitleCase(clockView)
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
        mBatteryLevelArcProgress = null
        mTemperatureArcProgress = null

        clockView.apply {
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

                22 -> {
                    val hourView = findViewContainsTag("textHour") as TextView
                    val minuteView = findViewContainsTag("textMinute") as TextView
                    val tickIndicator = findViewContainsTag("tickIndicator") as TextClock

                    TimeUtils.setCurrentTimeTextClock(mContext, tickIndicator, hourView, minuteView)
                }

                56 -> {
                    addArcProgressView("volume_progress")
                    addArcProgressView("ram_usage_info")
                    addArcProgressView("battery_progress_arc")
                    addArcProgressView("temperature_progress")
                }

                else -> {}
            }
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

    private fun loadColors() {
        if (!XprefsIsInitialized || isComposeLockscreen) return

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

    @get:SuppressLint("MissingPermission")
    private val userName: String
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
                log(this@LockscreenClock, throwable)
            }

            ResourcesCompat.getDrawable(
                appContext.resources,
                R.drawable.default_avatar,
                appContext.theme
            )
        }

    private fun resetStockClock() {
        val isAndroid13OrBelow = Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU

        if (!isAndroid13OrBelow && showLockscreenClock) {
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