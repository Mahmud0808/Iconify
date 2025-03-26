package com.drdisagree.iconify.xposed.modules.extras.views

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.BatteryManager
import android.os.Build
import android.text.TextUtils
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.R
import com.drdisagree.iconify.xposed.modules.extras.callbacks.ThemeChangeCallback
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.findViewContainsTag
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.findViewWithTagAndChangeColor
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.toPx

class DeviceWidgetView(private val mContext: Context) : FrameLayout(mContext) {

    private var appContext: Context? = null

    private var mDeviceWidgetStyle = DEVICE_WIDGET_CLASSIC
    private var mBatteryLevelView: TextView? = null
    private var mBatteryProgress: ProgressBar? = null
    private var mBatteryPercentage = 1

    private var mClassicRow: LinearLayout? = null
    private var mArcRow: LinearLayout? = null
    private var mVolumeLevelContainer: LinearLayout? = null
    private var mRamUsageContainer: LinearLayout? = null
    private var mVolumeLevelContainerArc: LinearLayout? = null
    private var mRamUsageContainerArc: LinearLayout? = null
    private var mBatteryPercentArc: ArcProgressImageView? = null
    private var mBatteryTempArc: ArcProgressImageView? = null
    private var mVolumeLevelArcProgress: ArcProgressImageView? = null
    private var mRamUsageArcProgress: ArcProgressImageView? = null

    private var mCustomColor = false
    private var mProgressColor = 0
    private var mLinearProgressColor = 0
    private var mTextColor = 0
    private var mScaling = 1f

    private val batteryRegistered = false
    private val mBatteryReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent?.action != null && intent.action == Intent.ACTION_BATTERY_CHANGED) {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
                mBatteryPercentage = (level * 100) / scale
                initBatteryStatus()
            }
        }
    }

    private val mThemeChangeCallback: ThemeChangeCallback.OnThemeChangedListener =
        object : ThemeChangeCallback.OnThemeChangedListener {
            override fun onThemeChanged() {
                reloadView()
            }
        }

    init {
        appContext = mContext.createPackageContext(
            BuildConfig.APPLICATION_ID,
            Context.CONTEXT_IGNORE_SECURITY
        )

        inflateView()

        ThemeChangeCallback.getInstance().registerThemeChangedCallback(mThemeChangeCallback)
    }

    private fun inflateView() {
        inflate(appContext, R.layout.view_device_widget, this)
        setupViews()
    }

    @SuppressLint("DiscouragedApi")
    private fun setupViews() {
        mClassicRow = findViewContainsTag("device_widget_classic") as LinearLayout
        mArcRow = findViewContainsTag("device_widget_arc") as LinearLayout

        // First row
        mBatteryLevelView = findViewContainsTag("battery_percentage") as TextView
        mBatteryProgress = findViewContainsTag("battery_progressbar") as ProgressBar
        mVolumeLevelContainer = findViewContainsTag("volume_progress") as LinearLayout
        mRamUsageContainer = findViewContainsTag("ram_usage_info") as LinearLayout

        // Volume progress
        if (mVolumeLevelArcProgress == null) {
            mVolumeLevelArcProgress = ArcProgressImageView(mContext).apply {
                setProgressType(ArcProgressImageView.ProgressType.VOLUME)
            }
        }
        mVolumeLevelContainer!!.addView(mVolumeLevelArcProgress);

        // Ram usage progress
        if (mRamUsageArcProgress == null) {
            mRamUsageArcProgress = ArcProgressImageView(mContext).apply {
                setProgressType(ArcProgressImageView.ProgressType.MEMORY)
            }
        }
        mRamUsageContainer!!.addView(mRamUsageArcProgress);

        mBatteryProgress!!.progressTintList = ColorStateList.valueOf(
            if (mCustomColor)
                if (mLinearProgressColor == 0) mContext.resources.getColor(
                    mContext.resources.getIdentifier(
                        "android:color/system_accent1_300",
                        "color",
                        mContext.packageName
                    ), mContext.theme
                )
                else mLinearProgressColor
            else
                mLinearProgressColor
        )

        (findViewContainsTag("device_name") as TextView).text = Build.MODEL

        // Second Row
        mVolumeLevelContainerArc = findViewContainsTag("volume_progress_2") as LinearLayout
        mRamUsageContainerArc = findViewContainsTag("memory_progress") as LinearLayout

        val batteryArc = findViewContainsTag("battery_progress_arc") as LinearLayout
        val batteryTemp = findViewContainsTag("temperature_progress") as LinearLayout

        if (mBatteryPercentArc == null) {
            mBatteryPercentArc = ArcProgressImageView(mContext)
            mBatteryPercentArc!!.setProgressType(ArcProgressImageView.ProgressType.BATTERY)
        }
        batteryArc.addView(mBatteryPercentArc)

        if (mBatteryTempArc == null) {
            mBatteryTempArc = ArcProgressImageView(mContext)
            mBatteryTempArc!!.setProgressType(ArcProgressImageView.ProgressType.TEMPERATURE)
        }
        batteryTemp.addView(mBatteryTempArc)

        setupRows()
        updateScaling()
    }

    private fun setupRows() {
        val isClassicWidget = mDeviceWidgetStyle == DEVICE_WIDGET_CLASSIC
        val isCircularWidget = mDeviceWidgetStyle == DEVICE_WIDGET_CIRCULAR

        val volumeContainer =
            if (isClassicWidget) mVolumeLevelContainer!! else mVolumeLevelContainerArc!!
        val ramContainer = if (isClassicWidget) mRamUsageContainer!! else mRamUsageContainerArc!!

        (mVolumeLevelArcProgress?.parent as? ViewGroup)?.removeView(mVolumeLevelArcProgress)
        (mRamUsageArcProgress?.parent as? ViewGroup)?.removeView(mRamUsageArcProgress)

        volumeContainer.addView(mVolumeLevelArcProgress)
        ramContainer.addView(mRamUsageArcProgress)

        mClassicRow!!.visibility = if (isClassicWidget) VISIBLE else GONE
        mArcRow!!.visibility = if (isCircularWidget) VISIBLE else GONE
    }

    private fun updateScaling() {
        val arcContainerSize = context.toPx(60)
        val batteryProgressContainerSize = context.toPx(120)

        listOf(
            mVolumeLevelArcProgress,
            mRamUsageArcProgress,
            mBatteryPercentArc,
            mBatteryTempArc
        ).forEach { arcProgressImageView ->
            (arcProgressImageView?.parent as? ViewGroup)?.layoutParams?.apply {
                width = (arcContainerSize * mScaling).toInt()
                height = (arcContainerSize * mScaling).toInt()
            }
            (arcProgressImageView?.parent?.parent as? ViewGroup)?.requestLayout()
        }

        (mBatteryProgress?.parent as ViewGroup?)?.apply {
            layoutParams?.apply {
                width = (batteryProgressContainerSize * mScaling).toInt()
            }
            requestLayout()
        }
    }

    fun setScaling(scaling: Float) {
        mScaling = scaling
        updateScaling()
    }

    fun setDeviceWidgetStyle(newStyle: Int) {
        if (mDeviceWidgetStyle == newStyle) return
        mDeviceWidgetStyle = newStyle
        setupRows()
        updateScaling()
    }

    @SuppressLint("DiscouragedApi")
    private fun initBatteryStatus() {
        mBatteryProgress?.apply {
            post {
                progress = mBatteryPercentage
                progressTintList = ColorStateList.valueOf(
                    if (mCustomColor)
                        if (mLinearProgressColor == 0) mContext.resources.getColor(
                            mContext.resources.getIdentifier(
                                "android:color/system_accent1_300",
                                "color",
                                mContext.packageName
                            ), mContext.theme
                        )
                        else mLinearProgressColor
                    else
                        mLinearProgressColor
                )
            }
        }

        post {
            mBatteryLevelView?.text = appContext!!.resources.getString(
                R.string.percentage_text,
                mBatteryPercentage
            )
        }
    }

    fun setCustomColor(customColor: Boolean, linearColor: Int, circularColor: Int) {
        mCustomColor = customColor
        mProgressColor = circularColor
        mLinearProgressColor = linearColor
        mVolumeLevelArcProgress!!.setColors(
            if (mCustomColor) mProgressColor else Color.WHITE,
            mTextColor
        )
        mRamUsageArcProgress!!.setColors(
            if (mCustomColor) mProgressColor else Color.WHITE,
            mTextColor
        )
        mBatteryPercentArc!!.setColors(
            if (mCustomColor) mProgressColor else Color.WHITE,
            mTextColor
        )
        mBatteryTempArc!!.setColors(
            if (mCustomColor) mProgressColor else Color.WHITE,
            mTextColor
        )
    }

    fun setTextCustomColor(color: Int) {
        mTextColor = color
        mVolumeLevelArcProgress!!.setColors(
            if (mCustomColor) mProgressColor else Color.WHITE,
            mTextColor
        )
        mRamUsageArcProgress!!.setColors(
            if (mCustomColor) mProgressColor else Color.WHITE,
            mTextColor
        )
        mBatteryPercentArc!!.setColors(
            if (mCustomColor) mProgressColor else Color.WHITE,
            mTextColor
        )
        mBatteryTempArc!!.setColors(
            if (mCustomColor) mProgressColor else Color.WHITE,
            mTextColor
        )
        post {
            findViewWithTagAndChangeColor(
                this,
                "text1",
                mTextColor
            )
        }
    }

    fun setDeviceName(deviceName: String?) {
        post {
            (findViewContainsTag("device_name") as TextView).text =
                deviceName?.takeIf { !TextUtils.isEmpty(it) } ?: Build.MODEL
        }
    }

    private fun reloadView() {
        initBatteryStatus()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        try {
            if (!batteryRegistered) mContext.registerReceiver(
                mBatteryReceiver,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )
        } catch (ignored: Exception) {
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        try {
            if (batteryRegistered) mContext.unregisterReceiver(mBatteryReceiver)
        } catch (ignored: Exception) {
        }
    }

    companion object {
        private const val DEVICE_WIDGET_CLASSIC: Int = 0
        private const val DEVICE_WIDGET_CIRCULAR: Int = 1
    }
}