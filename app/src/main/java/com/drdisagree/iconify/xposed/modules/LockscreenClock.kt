package com.drdisagree.iconify.xposed.modules

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
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
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.common.Preferences.DEPTH_WALLPAPER_FADE_ANIMATION
import com.drdisagree.iconify.common.Preferences.DEPTH_WALLPAPER_SWITCH
import com.drdisagree.iconify.common.Preferences.ICONIFY_DEPTH_WALLPAPER_TAG
import com.drdisagree.iconify.common.Preferences.ICONIFY_LOCKSCREEN_CLOCK_TAG
import com.drdisagree.iconify.common.Preferences.LSCLOCK_BOTTOMMARGIN
import com.drdisagree.iconify.common.Preferences.LSCLOCK_COLOR_CODE_ACCENT1
import com.drdisagree.iconify.common.Preferences.LSCLOCK_COLOR_CODE_ACCENT2
import com.drdisagree.iconify.common.Preferences.LSCLOCK_COLOR_CODE_ACCENT3
import com.drdisagree.iconify.common.Preferences.LSCLOCK_COLOR_CODE_TEXT1
import com.drdisagree.iconify.common.Preferences.LSCLOCK_COLOR_CODE_TEXT2
import com.drdisagree.iconify.common.Preferences.LSCLOCK_COLOR_SWITCH
import com.drdisagree.iconify.common.Preferences.LSCLOCK_FONT_LINEHEIGHT
import com.drdisagree.iconify.common.Preferences.LSCLOCK_FONT_SWITCH
import com.drdisagree.iconify.common.Preferences.LSCLOCK_FONT_TEXT_SCALING
import com.drdisagree.iconify.common.Preferences.LSCLOCK_STYLE
import com.drdisagree.iconify.common.Preferences.LSCLOCK_SWITCH
import com.drdisagree.iconify.common.Preferences.LSCLOCK_TOPMARGIN
import com.drdisagree.iconify.common.Resources.LOCKSCREEN_CLOCK_LAYOUT
import com.drdisagree.iconify.config.XPrefs.Xprefs
import com.drdisagree.iconify.utils.TextUtil
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.utils.ArcProgressWidget.generateBitmap
import com.drdisagree.iconify.xposed.modules.utils.ViewHelper.applyFontRecursively
import com.drdisagree.iconify.xposed.modules.utils.ViewHelper.applyTextMarginRecursively
import com.drdisagree.iconify.xposed.modules.utils.ViewHelper.applyTextScalingRecursively
import com.drdisagree.iconify.xposed.modules.utils.ViewHelper.findViewWithTagAndChangeColor
import com.drdisagree.iconify.xposed.modules.utils.ViewHelper.setMargins
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge.hookAllMethods
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.XposedHelpers.findClass
import de.robv.android.xposed.XposedHelpers.getObjectField
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@SuppressLint("DiscouragedApi")
class LockscreenClock(context: Context?) : ModPack(context!!) {

    private var showLockscreenClock = false
    private var showDepthWallpaper = false
    private var mClockViewContainer: ViewGroup? = null
    private var mStatusViewContainer: ViewGroup? = null
    private var mUserManager: UserManager? = null
    private var mAudioManager: AudioManager? = null
    private var mActivityManager: ActivityManager? = null
    private var appContext: Context? = null
    private var mBatteryStatusView: TextView? = null
    private var mBatteryLevelView: TextView? = null
    private var mVolumeLevelView: TextView? = null
    private var mBatteryProgress: ProgressBar? = null
    private var mVolumeProgress: ProgressBar? = null
    private var mBatteryStatus = 1
    private var mBatteryPercentage = 1
    private var mVolumeLevelArcProgress: ImageView? = null
    private var mRamUsageArcProgress: ImageView? = null
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

    override fun updatePrefs(vararg key: String) {
        if (Xprefs == null) return

        showLockscreenClock = Xprefs!!.getBoolean(LSCLOCK_SWITCH, false)
        showDepthWallpaper = Xprefs!!.getBoolean(DEPTH_WALLPAPER_SWITCH, false)

        if (key.isNotEmpty() &&
            (key[0] == LSCLOCK_SWITCH ||
                    key[0] == DEPTH_WALLPAPER_SWITCH ||
                    key[0] == LSCLOCK_COLOR_SWITCH ||
                    key[0] == LSCLOCK_COLOR_CODE_ACCENT1 ||
                    key[0] == LSCLOCK_COLOR_CODE_ACCENT2 ||
                    key[0] == LSCLOCK_COLOR_CODE_ACCENT3 ||
                    key[0] == LSCLOCK_COLOR_CODE_TEXT1 ||
                    key[0] == LSCLOCK_COLOR_CODE_TEXT2 ||
                    key[0] == LSCLOCK_STYLE ||
                    key[0] == LSCLOCK_TOPMARGIN ||
                    key[0] == LSCLOCK_BOTTOMMARGIN ||
                    key[0] == LSCLOCK_FONT_LINEHEIGHT ||
                    key[0] == LSCLOCK_FONT_SWITCH ||
                    key[0] == LSCLOCK_FONT_TEXT_SCALING ||
                    key[0] == DEPTH_WALLPAPER_FADE_ANIMATION)
        ) {
            updateClockView()
        }
    }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        initResources(mContext)

        val keyguardStatusViewClass = findClass(
            "com.android.keyguard.KeyguardStatusView",
            loadPackageParam.classLoader
        )

        hookAllMethods(keyguardStatusViewClass, "onFinishInflate", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (!showLockscreenClock) return

                mStatusViewContainer = getObjectField(
                    param.thisObject,
                    "mStatusViewContainer"
                ) as ViewGroup

                if (!showDepthWallpaper) {
                    mClockViewContainer = mStatusViewContainer
                }

                // Hide stock clock
                val keyguardStatusView = param.thisObject as GridLayout

                val mClockView = keyguardStatusView.findViewById<RelativeLayout>(
                    mContext.resources.getIdentifier(
                        "keyguard_clock_container",
                        "id",
                        mContext.packageName
                    )
                )
                mClockView.layoutParams.height = 0
                mClockView.layoutParams.width = 0
                mClockView.visibility = View.INVISIBLE

                val mMediaHostContainer = getObjectField(
                    param.thisObject,
                    "mMediaHostContainer"
                ) as View
                mMediaHostContainer.layoutParams.height = 0
                mMediaHostContainer.layoutParams.width = 0
                mMediaHostContainer.visibility = View.INVISIBLE

                registerClockUpdater()
            }
        })

        val keyguardBottomAreaViewClass = findClass(
            "$SYSTEMUI_PACKAGE.statusbar.phone.KeyguardBottomAreaView",
            loadPackageParam.classLoader
        )

        hookAllMethods(keyguardBottomAreaViewClass, "onFinishInflate", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (!showLockscreenClock || !showDepthWallpaper) return

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
                } catch (ignored: Throwable) {
                }
            }
        })

        try {
            val executor = Executors.newSingleThreadScheduledExecutor()
            executor.scheduleAtFixedRate({
                val androidDir =
                    File(Environment.getExternalStorageDirectory().toString() + "/Android")

                if (androidDir.isDirectory()) {
                    updateClockView()
                    executor.shutdown()
                    executor.shutdownNow()
                }
            }, 0, 5, TimeUnit.SECONDS)
        } catch (ignored: Throwable) {
        }
    }

    private fun initResources(context: Context) {
        try {
            appContext = context.createPackageContext(
                BuildConfig.APPLICATION_ID,
                Context.CONTEXT_IGNORE_SECURITY
            )
        } catch (ignored: PackageManager.NameNotFoundException) {
        }

        Handler(Looper.getMainLooper()).post {
            mUserManager = context.getSystemService(Context.USER_SERVICE) as UserManager
        }
        mAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mActivityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        try {
            context.registerReceiver(mBatteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        } catch (ignored: Exception) {
        }

        try {
            context.registerReceiver(
                mVolumeReceiver,
                IntentFilter("android.media.VOLUME_CHANGED_ACTION")
            )
        } catch (ignored: Exception) {
        }
    }

    // Broadcast receiver for updating clock
    private fun registerClockUpdater() {
        val filter = IntentFilter()

        filter.addAction(Intent.ACTION_TIME_TICK)
        filter.addAction(Intent.ACTION_TIME_CHANGED)
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED)
        filter.addAction(Intent.ACTION_LOCALE_CHANGED)

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
        val isDepthClock = mClockViewContainer!!.tag === ICONIFY_DEPTH_WALLPAPER_TAG

        if (isClockAdded && currentTime - lastUpdated < THRESHOLD_TIME) {
            return
        } else {
            lastUpdated = currentTime
        }

        val clockView = clockView

        // Remove existing clock view
        if (isClockAdded) {
            mClockViewContainer!!.removeView(
                mClockViewContainer!!.findViewWithTag<View>(
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
                if (mClockViewContainer!!.childCount > 0) {
                    idx = 1
                }

                // Add a dummy layout to the status view container so that we can still move notifications
                if (mStatusViewContainer != null) {
                    val dummyTag = "dummy_layout"
                    dummyLayout = mStatusViewContainer!!.findViewWithTag(dummyTag)

                    if (dummyLayout == null) {
                        dummyLayout = LinearLayout(mContext)
                        dummyLayout.setLayoutParams(
                            LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                350
                            )
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
                dummyLayout.setLayoutParams(dummyParams)
            }
        }
    }

    private val clockView: View?
        get() {
            if (appContext == null || Xprefs == null) return null

            val inflater = LayoutInflater.from(appContext)
            val clockStyle: Int = Xprefs!!.getInt(LSCLOCK_STYLE, 0)

            return inflater.inflate(
                appContext!!.resources.getIdentifier(
                    LOCKSCREEN_CLOCK_LAYOUT + clockStyle,
                    "layout",
                    BuildConfig.APPLICATION_ID
                ),
                null
            )
        }

    private fun modifyClockView(clockView: View) {
        if (Xprefs == null) return

        val clockStyle: Int = Xprefs!!.getInt(LSCLOCK_STYLE, 0)
        val topMargin: Int = Xprefs!!.getInt(LSCLOCK_TOPMARGIN, 100)
        val bottomMargin: Int = Xprefs!!.getInt(LSCLOCK_BOTTOMMARGIN, 40)
        val clockScale: Float = (Xprefs!!.getInt(LSCLOCK_FONT_TEXT_SCALING, 10) / 10.0).toFloat()
        val customFont = Environment.getExternalStorageDirectory().toString() +
                "/.iconify_files/lsclock_font.ttf"
        val lineHeight: Int = Xprefs!!.getInt(LSCLOCK_FONT_LINEHEIGHT, 0)
        val customFontEnabled: Boolean = Xprefs!!.getBoolean(LSCLOCK_FONT_SWITCH, false)
        val customColorEnabled: Boolean = Xprefs!!.getBoolean(LSCLOCK_COLOR_SWITCH, false)
        val accent1: Int = Xprefs!!.getInt(
            LSCLOCK_COLOR_CODE_ACCENT1,
            ContextCompat.getColor(mContext, android.R.color.system_accent1_300)
        )
        val accent2: Int = Xprefs!!.getInt(
            LSCLOCK_COLOR_CODE_ACCENT2,
            ContextCompat.getColor(mContext, android.R.color.system_accent2_300)
        )
        val accent3: Int = Xprefs!!.getInt(
            LSCLOCK_COLOR_CODE_ACCENT3,
            ContextCompat.getColor(mContext, android.R.color.system_accent3_300)
        )
        val text1: Int = Xprefs!!.getInt(
            LSCLOCK_COLOR_CODE_TEXT1,
            Color.WHITE
        )
        val text2: Int = Xprefs!!.getInt(
            LSCLOCK_COLOR_CODE_TEXT2,
            Color.BLACK
        )
        var typeface: Typeface? = null
        if (customFontEnabled && File(customFont).exists()) {
            typeface = Typeface.createFromFile(File(customFont))
        }

        setMargins(clockView, mContext, 0, topMargin, 0, bottomMargin)

        if (customColorEnabled) {
            findViewWithTagAndChangeColor(clockView, "accent1", accent1)
            findViewWithTagAndChangeColor(clockView, "accent2", accent2)
            findViewWithTagAndChangeColor(clockView, "accent3", accent3)
            findViewWithTagAndChangeColor(clockView, "text1", text1)
            findViewWithTagAndChangeColor(clockView, "text2", text2)
        }

        if (typeface != null) {
            applyFontRecursively(clockView, typeface)
        }

        applyTextMarginRecursively(mContext, clockView, lineHeight)

        if (clockScale != 1f) {
            applyTextScalingRecursively(clockView, clockScale)
        }

        if (clockStyle != 10) {
            TextUtil.convertTextViewsToTitleCase(clockView)
        }

        when (clockStyle) {
            5 -> {
                mBatteryStatusView = clockView.findViewById(R.id.battery_status)
                mBatteryLevelView = clockView.findViewById(R.id.battery_percentage)
                mVolumeLevelView = clockView.findViewById(R.id.volume_level)
                mBatteryProgress = clockView.findViewById(R.id.battery_progressbar)
                mVolumeProgress = clockView.findViewById(R.id.volume_progressbar)
            }

            7 -> {
                val usernameView = clockView.findViewById<TextView>(R.id.summary)
                usernameView.text = userName
                val imageView = clockView.findViewById<ImageView>(R.id.user_profile_image)
                userImage?.let { imageView.setImageDrawable(it) }
            }

            19 -> {
                mBatteryLevelView = clockView.findViewById(R.id.battery_percentage)
                mBatteryProgress = clockView.findViewById(R.id.battery_progressbar)
                mVolumeLevelArcProgress = clockView.findViewById(R.id.volume_progress)
                mRamUsageArcProgress = clockView.findViewById(R.id.ram_usage_info)
                (clockView.findViewById<View>(R.id.device_name) as TextView).text = Build.MODEL
            }

            else -> {
                mBatteryStatusView = null
                mBatteryLevelView = null
                mVolumeLevelView = null
                mBatteryProgress = null
                mVolumeProgress = null
            }
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

        if (mBatteryProgress != null) {
            mBatteryProgress!!.progress = mBatteryPercentage
        }

        if (mBatteryLevelView != null) {
            mBatteryLevelView!!.text =
                appContext!!.resources.getString(R.string.percentage_text, mBatteryPercentage)
        }

        initRamUsage()
    }

    private fun initSoundManager() {
        val volLevel = mAudioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVolLevel = mAudioManager!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val volPercent = (volLevel.toFloat() / maxVolLevel * 100).toInt()

        if (mVolumeProgress != null) {
            mVolumeProgress!!.progress = volPercent
        }

        if (mVolumeLevelView != null) {
            mVolumeLevelView!!.text =
                appContext!!.resources.getString(R.string.percentage_text, volPercent)
        }

        if (mVolumeLevelArcProgress != null) {
            val widgetBitmap = generateBitmap(
                mContext,
                volPercent,
                appContext!!.resources.getString(R.string.percentage_text, volPercent),
                40,
                ContextCompat.getDrawable(appContext!!, R.drawable.ic_volume_up),
                36
            )

            mVolumeLevelArcProgress!!.setImageBitmap(widgetBitmap)
        }
    }

    private fun initRamUsage() {
        if (mActivityManager == null) return

        val memoryInfo = ActivityManager.MemoryInfo()
        mActivityManager!!.getMemoryInfo(memoryInfo)
        val usedMemory = memoryInfo.totalMem - memoryInfo.availMem
        val usedMemoryPercentage = (usedMemory * 100 / memoryInfo.totalMem).toInt()

        if (mRamUsageArcProgress != null) {
            val widgetBitmap = generateBitmap(
                context = mContext,
                percentage = usedMemoryPercentage,
                textInside = appContext!!.resources.getString(
                    R.string.percentage_text,
                    usedMemoryPercentage
                ),
                textInsideSizePx = 40,
                textBottom = "RAM",
                textBottomSizePx = 28
            )

            mRamUsageArcProgress!!.setImageBitmap(widgetBitmap)
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
            else appContext!!.resources.getString(R.string.default_user_name)
        }

    private val userImage: Drawable?
        get() = if (mUserManager == null) {
            ResourcesCompat.getDrawable(
                appContext!!.resources,
                R.drawable.default_avatar,
                appContext!!.theme
            )
        } else try {
            val getUserIconMethod = mUserManager!!.javaClass
                .getMethod("getUserIcon", Int::class.javaPrimitiveType)
            val userId = UserHandle::class.java.getDeclaredMethod("myUserId").invoke(null) as Int
            val bitmapUserIcon = getUserIconMethod.invoke(mUserManager, userId) as Bitmap

            BitmapDrawable(mContext.resources, bitmapUserIcon)
        } catch (throwable: Throwable) {
            log(TAG + throwable)

            ResourcesCompat.getDrawable(
                appContext!!.resources,
                R.drawable.default_avatar,
                appContext!!.theme
            )
        }

    companion object {
        private val TAG = "Iconify - ${LockscreenClock::class.java.simpleName}: "
        private var lastUpdated = System.currentTimeMillis()
        private const val THRESHOLD_TIME: Long = 500 // milliseconds
    }
}