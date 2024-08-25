package com.drdisagree.iconify.xposed.modules.views

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.media.AudioManager
import android.os.BatteryManager
import android.os.Build
import android.text.TextUtils
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.R
import com.drdisagree.iconify.xposed.modules.utils.ArcProgressWidget.generateBitmap
import com.drdisagree.iconify.xposed.modules.utils.ViewHelper.findViewWithTagAndChangeColor

class DeviceWidgetView(private val mContext: Context) : FrameLayout(mContext) {
    private var appContext: Context? = null

    private var mBatteryLevelView: TextView? = null
    private var mBatteryProgress: ProgressBar? = null
    private var mBatteryPercentage = 1
    private var mVolumeLevelArcProgress: ImageView? = null
    private var mRamUsageArcProgress: ImageView? = null

    private val mAudioManager: AudioManager
    private val mActivityManager: ActivityManager?

    private var mCustomColor = false
    private var mProgressColor = 0
    private var mLinearProgressColor = 0
    private var mTextColor = 0

    init {
        try {
            appContext = mContext.createPackageContext(
                BuildConfig.APPLICATION_ID,
                Context.CONTEXT_IGNORE_SECURITY
            )
        } catch (ignored: PackageManager.NameNotFoundException) {
        }

        mAudioManager = mContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mActivityManager = mContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        try {
            val mBatteryReceiver: BroadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    if (intent.action != null && intent.action == Intent.ACTION_BATTERY_CHANGED) {
                        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
                        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
                        mBatteryPercentage = (level * 100) / scale
                        initBatteryStatus()
                    }
                }
            }
            mContext.registerReceiver(mBatteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        } catch (ignored: Exception) {
        }
        try {
            val mVolumeReceiver: BroadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    initSoundManager()
                }
            }
            mContext.registerReceiver(
                mVolumeReceiver,
                IntentFilter("android.media.VOLUME_CHANGED_ACTION")
            )
        } catch (ignored: Exception) {
        }

        inflateView()
    }

    private fun inflateView() {
        inflate(appContext, R.layout.device_widget, this)
        setupViews()
        initSoundManager()
    }

    private fun setupViews() {
        mBatteryLevelView = findViewById(R.id.battery_percentage)
        mBatteryProgress = findViewById(R.id.battery_progressbar)
        mVolumeLevelArcProgress = findViewById(R.id.volume_progress)
        mRamUsageArcProgress = findViewById(R.id.ram_usage_info)

        mBatteryProgress!!.setProgressTintList(
            ColorStateList.valueOf(
                if (mCustomColor)
                    if (mLinearProgressColor == 0) mContext.resources.getColor(
                    mContext.resources.getIdentifier(
                        "android:color/system_accent1_300",
                        "color",
                        mContext.packageName
                    ), mContext.theme)
                    else mLinearProgressColor
                else
                    mLinearProgressColor
            )
        )

        (findViewById<View>(R.id.device_name) as TextView).text =
            Build.MODEL
    }

    private fun initBatteryStatus() {
        if (mBatteryProgress != null) {
            post {
                mBatteryProgress!!.progress = mBatteryPercentage
                mBatteryProgress!!.progressTintList = ColorStateList.valueOf(
                    if (mCustomColor)
                        if (mLinearProgressColor == 0) mContext.resources.getColor(
                            mContext.resources.getIdentifier(
                                "android:color/system_accent1_300",
                                "color",
                                mContext.packageName
                            ), mContext.theme)
                        else mLinearProgressColor
                    else
                        mLinearProgressColor
                )
            }
        }
        if (mBatteryLevelView != null) {
            post {
                mBatteryLevelView!!.text = appContext!!.resources
                    .getString(R.string.percentage_text, mBatteryPercentage)
            }
        }

        initRamUsage()
    }

    private fun initSoundManager() {
        val volLevel = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVolLevel = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val volPercent = ((volLevel.toFloat() / maxVolLevel) * 100).toInt()

        if (mVolumeLevelArcProgress != null) {
            val widgetBitmap = generateBitmap(
                mContext,
                volPercent,
                appContext!!.resources.getString(R.string.percentage_text, volPercent),
                40,
                ContextCompat.getDrawable(appContext!!, R.drawable.ic_volume_up),
                36
            )
            post { mVolumeLevelArcProgress!!.setImageBitmap(widgetBitmap) }
        }
    }

    private fun initRamUsage() {
        if (mActivityManager == null) return

        val memoryInfo = ActivityManager.MemoryInfo()
        mActivityManager.getMemoryInfo(memoryInfo)
        val usedMemory = memoryInfo.totalMem - memoryInfo.availMem
        if (memoryInfo.totalMem == 0L) return
        val usedMemoryPercentage = ((usedMemory * 100) / memoryInfo.totalMem).toInt()

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
            post { mRamUsageArcProgress!!.setImageBitmap(widgetBitmap) }
        }
    }

    fun setCustomColor(customColor: Boolean, linearColor: Int, circularColor: Int) {
        mCustomColor = customColor
        mProgressColor = linearColor
        mLinearProgressColor = circularColor
        post { this.initSoundManager() }
        post {
            findViewWithTagAndChangeColor(
                this,
                "circularprogress",
                if (mCustomColor) mProgressColor else Color.WHITE
            )
        }
    }

    fun setTextCustomColor(color: Int) {
        mTextColor = color
        post {
            findViewWithTagAndChangeColor(
                this,
                "text1",
                mTextColor
            )
        }
    }

    fun setDeviceName(devName: String?) {
        val deviceName = if (!TextUtils.isEmpty(devName)) {
            devName
        } else {
            Build.MODEL
        }

        post {
            (findViewById<View>(R.id.device_name) as TextView).text =
                deviceName
        }
    }
}