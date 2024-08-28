package com.drdisagree.iconify.xposed.modules.views

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaController.PlaybackInfo
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.telephony.TelephonyManager
import android.util.Log
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Const.FRAMEWORK_PACKAGE
import com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.utils.OmniJawsClient
import com.drdisagree.iconify.xposed.HookEntry.Companion.enqueueProxyCommand
import com.drdisagree.iconify.xposed.HookRes.Companion.modRes
import com.drdisagree.iconify.xposed.modules.ControllersProvider
import com.drdisagree.iconify.xposed.modules.LockscreenWidgets.Companion.LaunchableImageView
import com.drdisagree.iconify.xposed.modules.LockscreenWidgets.Companion.LaunchableLinearLayout
import com.drdisagree.iconify.xposed.modules.utils.ActivityLauncherUtils
import com.drdisagree.iconify.xposed.modules.utils.ExtendedFAB
import com.drdisagree.iconify.xposed.modules.utils.ViewHelper.toPx
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.getBooleanField
import java.lang.reflect.Method
import java.util.Locale
import kotlin.math.abs
import kotlin.math.min

@SuppressLint("ViewConstructor")
class LockscreenWidgetsView(context: Context, activityStarter: Any?) :
    LinearLayout(context), OmniJawsClient.OmniJawsObserver {

    private val mContext: Context

    private var mWeatherClient: OmniJawsClient? = null
    private var mWeatherInfo: OmniJawsClient.WeatherInfo? = null

    // Two Linear Layouts, one for main widgets and one for secondary widgets
    private val mDeviceWidgetContainer: LinearLayout?
    private val mMainWidgetsContainer: LinearLayout?
    private val mSecondaryWidgetsContainer: LinearLayout?
    private var mDeviceWidgetView: DeviceWidgetView? = null

    private var mediaButtonFab: ExtendedFAB? = null
    private var torchButtonFab: ExtendedFAB? = null
    private var weatherButtonFab: ExtendedFAB? = null
    private var wifiButtonFab: ExtendedFAB? = null
    private var dataButtonFab: ExtendedFAB? = null
    private var ringerButtonFab: ExtendedFAB? = null
    private var btButtonFab: ExtendedFAB? = null
    private var hotspotButtonFab: ExtendedFAB? = null
    private var mediaButton: ImageView? = null
    private var torchButton: ImageView? = null
    private var weatherButton: ImageView? = null
    private var hotspotButton: ImageView? = null
    private var wifiButton: ImageView? = null
    private var dataButton: ImageView? = null
    private var ringerButton: ImageView? = null
    private var btButton: ImageView? = null
    private val mDarkColor: Int
    private val mDarkColorActive: Int
    private val mLightColor: Int
    private val mLightColorActive: Int

    // Custom Widgets Colors
    private var mCustomColors = false
    private var mBigInactiveColor = 0
    private var mBigActiveColor = 0
    private var mSmallInactiveColor = 0
    private var mSmallActiveColor = 0
    private var mBigIconInactiveColor = 0
    private var mBigIconActiveColor = 0
    private var mSmallIconInactiveColor = 0
    private var mSmallIconActiveColor = 0

    private var mMainLockscreenWidgetsList: String? = null
    private var mSecondaryLockscreenWidgetsList: String? = null
    private var mMainWidgetViews: Array<ExtendedFAB>? = null
    private var mSecondaryWidgetViews: Array<ImageView>? = null
    private var mMainWidgetsList: List<String>? = ArrayList()
    private var mSecondaryWidgetsList: List<String>? = ArrayList()
    private var mIsLargeClock: Boolean = false

    private val mAudioManager: AudioManager?
    private val mWifiManager: WifiManager?
    private val mTelephonyManager: TelephonyManager?
    private val mConnectivityManager: ConnectivityManager?
    private var mController: MediaController? = null
    private var mMediaMetadata: MediaMetadata? = null
    private var mLastTrackTitle: String? = null

    private var lockscreenWidgetsEnabled = false
    private var deviceWidgetsEnabled = false

    private var isBluetoothOn = false

    private var mIsInflated = false
    private var mIsLongPress = false

    private val mCameraManager: CameraManager
    private var mCameraId: String? = null
    private var isFlashOn = false

    private val mAudioMode = 0
    private val mMediaUpdater: Runnable
    private val mHandler: Handler

    // Dozing State
    private var mDozing: Boolean = false

    private var mActivityLauncherUtils: ActivityLauncherUtils

    private val mScreenOnReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (Intent.ACTION_SCREEN_ON == intent.action) {
                onVisible()
            }
        }
    }

    private val mMediaCallback: MediaController.Callback = object : MediaController.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            updateMediaController()
        }

        override fun onMetadataChanged(metadata: MediaMetadata?) {
            super.onMetadataChanged(metadata)
            mMediaMetadata = metadata
            updateMediaController()
        }
    }

    private val mMobileDataCallback: ControllersProvider.OnMobileDataChanged =
        object : ControllersProvider.OnMobileDataChanged {
            override fun setMobileDataIndicators(mMobileDataIndicators: Any?) {
                log("LockscreenWidgets setMobileDataIndicators")
                updateMobileDataState(isMobileDataEnabled)
            }

            override fun setNoSims(show: Boolean, simDetected: Boolean) {
                log("LockscreenWidgets setNoSims")
                updateMobileDataState(simDetected && isMobileDataEnabled)
            }

            override fun setIsAirplaneMode(mIconState: Any?) {
                log("LockscreenWidgets setIsAirplaneMode")
                updateMobileDataState(
                    !getBooleanField(
                        mIconState,
                        "visible"
                    ) && isMobileDataEnabled
                )
            }
        }

    private val mWifiCallback: ControllersProvider.OnWifiChanged =
        object : ControllersProvider.OnWifiChanged {
            override fun onWifiChanged(mWifiIndicators: Any?) {
                log("LockscreenWidgets onWifiChanged")
                updateWiFiButtonState(isWifiEnabled)
            }
        }

    private val mBluetoothCallback: ControllersProvider.OnBluetoothChanged =
        object : ControllersProvider.OnBluetoothChanged {
            override fun onBluetoothChanged(enabled: Boolean) {
                log("LockscreenWidgets onBluetoothChanged $enabled")
                isBluetoothOn = enabled
                updateBtState()
            }
        }

    private val mTorchCallback: ControllersProvider.OnTorchModeChanged =
        object : ControllersProvider.OnTorchModeChanged {
            override fun onTorchModeChanged(enabled: Boolean) {
                log("LockscreenWidgets onTorchChanged $enabled")
                isFlashOn = enabled
                updateTorchButtonState()
            }
        }

    private val mHotspotCallback: ControllersProvider.OnHotspotChanged =
        object : ControllersProvider.OnHotspotChanged {
            override fun onHotspotChanged(enabled: Boolean, connectedDevices: Int) {
                log("LockscreenWidgets onHotspotChanged $enabled")
                updateHotspotButtonState(connectedDevices)
            }
        }

    private val mDozeCallback: ControllersProvider.OnDozingChanged =
        object : ControllersProvider.OnDozingChanged {
            override fun onDozingChanged(dozing: Boolean) {
                if (mDozing == dozing) {
                    return
                }
                mDozing = dozing
                updateContainerVisibility()
            }
        }

    private fun createDeviceWidgetContainer(context: Context): LinearLayout {
        val deviceWidget = LinearLayout(context)
        deviceWidget.orientation = HORIZONTAL
        deviceWidget.gravity = Gravity.CENTER
        deviceWidget.layoutParams = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        mDeviceWidgetView = DeviceWidgetView(context)

        deviceWidget.addView(mDeviceWidgetView)
        deviceWidget.setPadding(0, 0, 0, mContext.toPx(18))

        return deviceWidget
    }

    private fun createMainWidgetsContainer(context: Context): LinearLayout {
        var mainWidgetsContainer: LinearLayout?
        log("LockscreenWidgets createMainWidgetsContainer LaunchableLinearLayout " + (LaunchableLinearLayout != null))
        try {
            mainWidgetsContainer =
                LaunchableLinearLayout!!.getConstructor(Context::class.java)
                    .newInstance(context) as LinearLayout?
        } catch (e: Exception) {
            log("LockscreenWidgets createMainWidgetsContainer LaunchableLinearLayout not found: " + e.message)
            mainWidgetsContainer = LinearLayout(context)
        }

        if (mainWidgetsContainer == null) {
            mainWidgetsContainer = LinearLayout(context) // Ensure the creation on our linear layout
        }

        mainWidgetsContainer.orientation = HORIZONTAL
        mainWidgetsContainer.gravity = Gravity.CENTER
        mainWidgetsContainer.setLayoutParams(
            LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        // Add FABs to the main widgets container
        mMainWidgetViews = arrayOf(
            createFAB(context),
            createFAB(context)
        )

        for (mMainWidgetView: ExtendedFAB in mMainWidgetViews!!) {
            mainWidgetsContainer.addView(mMainWidgetView)
        }

        return mainWidgetsContainer
    }

    private fun createFAB(context: Context): ExtendedFAB {
        val fab = ExtendedFAB(context)
        fab.setId(generateViewId())
        val params = LayoutParams(
            modRes.getDimensionPixelSize(R.dimen.kg_widget_main_width),
            modRes.getDimensionPixelSize(R.dimen.kg_widget_main_height)
        )
        params.setMargins(
            modRes.getDimensionPixelSize(R.dimen.kg_widgets_main_margin_start),
            0,
            modRes.getDimensionPixelSize(R.dimen.kg_widgets_main_margin_end),
            0
        )
        fab.setLayoutParams(params)
        fab.setPadding(
            modRes.getDimensionPixelSize(R.dimen.kg_main_widgets_icon_padding),
            modRes.getDimensionPixelSize(R.dimen.kg_main_widgets_icon_padding),
            modRes.getDimensionPixelSize(R.dimen.kg_main_widgets_icon_padding),
            modRes.getDimensionPixelSize(R.dimen.kg_main_widgets_icon_padding)
        )
        fab.setGravity(Gravity.CENTER)
        return fab
    }

    private fun createSecondaryWidgetsContainer(context: Context): LinearLayout {
        var secondaryWidgetsContainer: LinearLayout?
        log("LockscreenWidgets createSecondaryWidgetsContainer LaunchableLinearLayout " + (LaunchableLinearLayout != null))
        try {
            secondaryWidgetsContainer =
                LaunchableLinearLayout?.getConstructor(Context::class.java)
                    ?.newInstance(context) as LinearLayout?
        } catch (e: Exception) {
            log("LockscreenWidgets createMainWidgetsContainer LaunchableLinearLayout not found: " + e.message)
            secondaryWidgetsContainer = LinearLayout(context)
        }

        if (secondaryWidgetsContainer == null) {
            secondaryWidgetsContainer =
                LinearLayout(context) // Ensure the creation on our linear layout
        }

        secondaryWidgetsContainer.orientation = HORIZONTAL
        secondaryWidgetsContainer.gravity = Gravity.CENTER_HORIZONTAL
        secondaryWidgetsContainer.setLayoutParams(
            LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        (secondaryWidgetsContainer.layoutParams as MarginLayoutParams).topMargin =
            modRes.getDimensionPixelSize(R.dimen.kg_widget_margin_vertical)
        (secondaryWidgetsContainer.layoutParams as MarginLayoutParams).bottomMargin =
            modRes.getDimensionPixelSize(R.dimen.kg_widget_margin_bottom)

        // Add ImageViews to the secondary widgets container
        mSecondaryWidgetViews = arrayOf(
            createImageView(context),
            createImageView(context),
            createImageView(context),
            createImageView(context)
        )

        for (mSecondaryWidgetView: ImageView? in mSecondaryWidgetViews!!) {
            secondaryWidgetsContainer.addView(mSecondaryWidgetView)
        }

        return secondaryWidgetsContainer
    }

    private fun createImageView(context: Context): ImageView {
        val imageView: ImageView = try {
            LaunchableImageView?.getConstructor(Context::class.java)
                ?.newInstance(context) as ImageView
        } catch (e: Exception) {
            // LaunchableImageView not found or other error, ensure the creation of our ImageView
            ImageView(context)
        }

        imageView.id = generateViewId()
        val params = LayoutParams(
            modRes.getDimensionPixelSize(R.dimen.kg_widget_circle_size),
            modRes.getDimensionPixelSize(R.dimen.kg_widget_circle_size)
        )
        params.setMargins(
            modRes.getDimensionPixelSize(R.dimen.kg_widgets_margin_horizontal),
            0,
            modRes.getDimensionPixelSize(R.dimen.kg_widgets_margin_horizontal),
            0
        )
        imageView.layoutParams = params
        imageView.setPadding(
            modRes.getDimensionPixelSize(R.dimen.kg_widgets_icon_padding),
            modRes.getDimensionPixelSize(R.dimen.kg_widgets_icon_padding),
            modRes.getDimensionPixelSize(R.dimen.kg_widgets_icon_padding),
            modRes.getDimensionPixelSize(R.dimen.kg_widgets_icon_padding)
        )
        imageView.isFocusable = true
        imageView.isClickable = true

        return imageView
    }

    private val isMediaControllerAvailable: Boolean
        get() {
            val mediaController =
                activeLocalMediaController
            return mediaController != null && !mediaController.packageName.isNullOrEmpty()
        }

    private val activeLocalMediaController: MediaController?
        get() {
            val mediaSessionManager =
                mContext.getSystemService(MediaSessionManager::class.java)
            var localController: MediaController? = null
            val remoteMediaSessionLists: MutableList<String> = ArrayList()
            for (controller: MediaController in mediaSessionManager.getActiveSessions(null)) {
                val pi = controller.playbackInfo ?: continue
                val playbackState = controller.playbackState ?: continue
                if (playbackState.state != PlaybackState.STATE_PLAYING) {
                    continue
                }
                if (pi.playbackType == PlaybackInfo.PLAYBACK_TYPE_REMOTE) {
                    if (localController != null
                        && localController.packageName!!.contentEquals(controller.packageName)
                    ) {
                        localController = null
                    }
                    if (!remoteMediaSessionLists.contains(controller.packageName)) {
                        remoteMediaSessionLists.add(controller.packageName)
                    }
                    continue
                }
                if (pi.playbackType == PlaybackInfo.PLAYBACK_TYPE_LOCAL) {
                    if (localController == null
                        && !remoteMediaSessionLists.contains(controller.packageName)
                    ) {
                        localController = controller
                    }
                }
            }
            return localController
        }

    private fun isWidgetEnabled(widget: String): Boolean {
        if (mMainWidgetViews == null || mSecondaryWidgetViews == null) {
            return false
        }
        return mMainWidgetsList!!.contains(widget) || mSecondaryWidgetsList!!.contains(widget)
    }

    private fun updateMediaController() {
        if (!isWidgetEnabled("media")) return
        val localController =
            activeLocalMediaController
        if (localController != null && !sameSessions(mController, localController)) {
            if (mController != null) {
                mController!!.unregisterCallback(mMediaCallback)
                mController = null
            }
            mController = localController
            mController!!.registerCallback(mMediaCallback)
        }
        mMediaMetadata = if (isMediaControllerAvailable) mController!!.metadata else null
        updateMediaState()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility == VISIBLE && isAttachedToWindow) {
            onVisible()
            updateMediaController()
        }
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        if (!lockscreenWidgetsEnabled) return
        if (visibility == VISIBLE) {
            onVisible()
        }
    }

    private fun enableWeatherUpdates() {
        if (mWeatherClient != null) {
            mWeatherClient!!.addObserver(this)
            queryAndUpdateWeather()
        }
    }

    private fun disableWeatherUpdates() {
        if (mWeatherClient != null) {
            weatherButton = null
            weatherButtonFab = null
            mWeatherClient!!.removeObserver(this)
        }
    }

    override fun weatherError(errorReason: Int) {
        if (errorReason == OmniJawsClient.EXTRA_ERROR_DISABLED) {
            mWeatherInfo = null
        }
    }

    override fun weatherUpdated() {
        queryAndUpdateWeather()
    }

    override fun updateSettings() {
        queryAndUpdateWeather()
    }

    @SuppressLint("SetTextI18n")
    private fun queryAndUpdateWeather() {
        try {
            if (mWeatherClient == null || !mWeatherClient!!.isOmniJawsEnabled) {
                return
            }
            mWeatherClient!!.queryWeather()
            mWeatherInfo = mWeatherClient!!.weatherInfo
            if (mWeatherInfo != null) {
                // OpenWeatherMap
                var formattedCondition: String = mWeatherInfo!!.condition!!
                if (formattedCondition.lowercase(Locale.getDefault())
                        .contains("clouds") || formattedCondition.lowercase(
                        Locale.getDefault()
                    ).contains("overcast")
                ) {
                    formattedCondition = modRes.getString(R.string.weather_condition_clouds)
                } else if (formattedCondition.lowercase(Locale.getDefault()).contains("rain")) {
                    formattedCondition = modRes.getString(R.string.weather_condition_rain)
                } else if (formattedCondition.lowercase(Locale.getDefault()).contains("clear")) {
                    formattedCondition = modRes.getString(R.string.weather_condition_clear)
                } else if (formattedCondition.lowercase(Locale.getDefault()).contains("storm")) {
                    formattedCondition = modRes.getString(R.string.weather_condition_storm)
                } else if (formattedCondition.lowercase(Locale.getDefault()).contains("snow")) {
                    formattedCondition = modRes.getString(R.string.weather_condition_snow)
                } else if (formattedCondition.lowercase(Locale.getDefault()).contains("wind")) {
                    formattedCondition = modRes.getString(R.string.weather_condition_wind)
                } else if (formattedCondition.lowercase(Locale.getDefault()).contains("mist")) {
                    formattedCondition = modRes.getString(R.string.weather_condition_mist)
                }

                // MET Norway
                if (formattedCondition.lowercase(Locale.getDefault()).contains("_")) {
                    val words =
                        formattedCondition.split("_".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()
                    val formattedConditionBuilder = StringBuilder()
                    for (word in words) {
                        val capitalizedWord =
                            word.substring(0, 1).uppercase(Locale.getDefault()) + word.substring(1)
                        formattedConditionBuilder.append(capitalizedWord).append(" ")
                    }
                    formattedCondition = formattedConditionBuilder.toString().trim { it <= ' ' }
                }

                val d: Drawable =
                    mWeatherClient!!.getWeatherConditionImage(mWeatherInfo!!.conditionCode)
                if (weatherButtonFab != null) {
                    weatherButtonFab!!.icon = d
                    weatherButtonFab!!.text =
                        (mWeatherInfo!!.temp + mWeatherInfo!!.tempUnits) + " • " + formattedCondition
                    weatherButtonFab!!.iconTint = null
                }
                if (weatherButton != null) {
                    weatherButton!!.setImageDrawable(d)
                    weatherButton!!.imageTintList = null
                }
            }
        } catch (e: java.lang.Exception) {
            Log.e("LockscreenWidgets", "Error updating weather: " + e.message)
        }
    }

    private fun onVisible() {
        log("LockscreenWidgets onVisible")

        // Update the widgets when the view is visible
        if (isWidgetEnabled("weather")) {
            enableWeatherUpdates()
        }
        updateTorchButtonState()
        updateRingerButtonState()
        updateBtState()
        updateWiFiButtonState(isWifiEnabled)
        updateMobileDataState(isMobileDataEnabled)
        updateHotspotButtonState(0)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        log("LockscreenWidgets onAttachedToWindow")
        onVisible()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (isWidgetEnabled("weather")) {
            disableWeatherUpdates()
        }
    }

    public override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        log("LockscreenWidgets onConfigurationChanged")
        updateWidgetViews()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        log("LockscreenWidgets onFinishInflate")
        mIsInflated = true
        updateWidgetViews()
    }

    private fun updateContainerVisibility() {
        val isMainWidgetsEmpty = (mMainLockscreenWidgetsList == null
                || mMainLockscreenWidgetsList.isNullOrEmpty())
        val isSecondaryWidgetsEmpty = (mSecondaryLockscreenWidgetsList == null
                || mSecondaryLockscreenWidgetsList.isNullOrEmpty())
        val isEmpty = isMainWidgetsEmpty && isSecondaryWidgetsEmpty

        if (mDeviceWidgetContainer != null) {
            mDeviceWidgetContainer.visibility = if (deviceWidgetsEnabled) {
                if (mIsLargeClock) View.GONE else View.VISIBLE
            } else {
                View.GONE
            }
        }
        if (mMainWidgetsContainer != null) {
            mMainWidgetsContainer.visibility = if (isMainWidgetsEmpty) GONE else VISIBLE
        }
        if (mSecondaryWidgetsContainer != null) {
            mSecondaryWidgetsContainer.visibility =
                if (isSecondaryWidgetsEmpty || mIsLargeClock) GONE else VISIBLE
        }
        val shouldHideContainer = isEmpty || mDozing || !lockscreenWidgetsEnabled
        visibility = if (shouldHideContainer) GONE else VISIBLE
    }

    private fun updateWidgetViews() {
        log("LockscreenWidgets updateWidgetViews lockscreenWidgetsEnabled $lockscreenWidgetsEnabled")

        if (mMainWidgetViews != null && mMainWidgetsList != null) {
            for (i in mMainWidgetViews!!.indices) {
                mMainWidgetViews!![i].visibility =
                    if (i < mMainWidgetsList!!.size) VISIBLE else GONE
            }
            for (i in 0 until min(
                mMainWidgetsList!!.size.toDouble(),
                mMainWidgetViews!!.size.toDouble()
            )
                .toInt()) {
                val widgetType: String = mMainWidgetsList!![i]
                if (i < mMainWidgetViews!!.size) {
                    log("LockscreenWidgets updateWidgetViews mMainWidgetsList $widgetType")
                    setUpWidgetWiews(null, mMainWidgetViews!![i], widgetType)
                    updateMainWidgetResources(mMainWidgetViews!![i], false)
                }
            }
        }
        if (mSecondaryWidgetViews != null && mSecondaryWidgetsList != null) {
            for (i in mSecondaryWidgetViews!!.indices) {
                mSecondaryWidgetViews!![i].visibility =
                    if (i < mSecondaryWidgetsList!!.size) VISIBLE else GONE
            }
            for (i in 0 until min(
                mSecondaryWidgetsList!!.size.toDouble(),
                mSecondaryWidgetViews!!.size.toDouble()
            )
                .toInt()) {
                val widgetType: String = mSecondaryWidgetsList!![i]
                if (i < mSecondaryWidgetViews!!.size) {
                    log("LockscreenWidgets updateWidgetViews mSecondaryWidgetsList $widgetType")
                    setUpWidgetWiews(mSecondaryWidgetViews!![i], null, widgetType)
                    updateWidgetsResources(mSecondaryWidgetViews!![i])
                }
            }
        }
        updateContainerVisibility()
        updateMediaController()
    }

    private fun updateMainWidgetResources(efab: ExtendedFAB?, active: Boolean) {
        if (efab == null) return
        efab.setElevation(0F)
        setButtonActiveState(null, efab, false)
        val params: ViewGroup.LayoutParams = efab.layoutParams
        if (params is LayoutParams) {
            if (efab.visibility == VISIBLE && mMainWidgetsList!!.size == 1) {
                params.width = modRes.getDimensionPixelSize(R.dimen.kg_widget_main_width)
                params.height = modRes.getDimensionPixelSize(R.dimen.kg_widget_main_height)
            } else {
                params.width = 0
                params.weight = 1f
            }
            efab.setLayoutParams(params)
        }
    }

    private fun updateWidgetsResources(iv: ImageView?) {
        if (iv == null) return
        val d = ResourcesCompat.getDrawable(
            modRes,
            R.drawable.lockscreen_widget_background_circle,
            mContext.theme
        )
        iv.background = d
        setButtonActiveState(iv, null, false)
    }

    private val isNightMode: Boolean
        get() {
            val config = mContext.resources.configuration
            return ((config.uiMode and Configuration.UI_MODE_NIGHT_MASK)
                    == Configuration.UI_MODE_NIGHT_YES)
        }

    private fun setUpWidgetWiews(iv: ImageView?, efab: ExtendedFAB?, type: String) {
        when (type) {
            "none" -> {
                if (iv != null) {
                    iv.visibility = GONE
                }
                efab?.visibility = GONE
            }

            "wifi" -> {
                if (iv != null) {
                    wifiButton = iv
                    wifiButton!!.setOnLongClickListener { v: View ->
                        showInternetDialog(v)
                        true
                    }
                }
                if (efab != null) {
                    wifiButtonFab = efab
                    wifiButtonFab!!.setOnLongClickListener { v ->
                        showInternetDialog(v)
                        true
                    }
                }
                setUpWidgetResources(
                    iv, efab,
                    { toggleWiFi() }, getDrawable(WIFI_INACTIVE, FRAMEWORK_PACKAGE), getString(
                        WIFI_LABEL, SYSTEMUI_PACKAGE
                    )
                )
            }

            "data" -> {
                if (iv != null) {
                    dataButton = iv
                    dataButton!!.setOnLongClickListener { v: View ->
                        showInternetDialog(v)
                        true
                    }
                }
                if (efab != null) {
                    dataButtonFab = efab
                    dataButtonFab!!.setOnLongClickListener { v ->
                        showInternetDialog(v)
                        true
                    }
                }
                setUpWidgetResources(
                    iv, efab,
                    { toggleMobileData() }, getDrawable(DATA_ICON, FRAMEWORK_PACKAGE), getString(
                        DATA_LABEL, SYSTEMUI_PACKAGE
                    )
                )
            }

            "ringer" -> {
                if (iv != null) {
                    ringerButton = iv
                    ringerButton!!.setOnLongClickListener {
                        mActivityLauncherUtils.launchAudioSettings()
                        true
                    }
                }
                if (efab != null) {
                    ringerButtonFab = efab
                    ringerButtonFab!!.setOnLongClickListener {
                        mActivityLauncherUtils.launchAudioSettings()
                        true
                    }
                }
                setUpWidgetResources(
                    iv, efab,
                    { toggleRingerMode() },
                    ResourcesCompat.getDrawable(
                        modRes,
                        R.drawable.ic_ringer_normal,
                        mContext.theme
                    ),
                    getString(RINGER_LABEL_INACTIVE, SYSTEMUI_PACKAGE)
                )
            }

            "bt" -> {
                if (iv != null) {
                    btButton = iv
                    btButton!!.setOnLongClickListener { v: View ->
                        showBluetoothDialog(v)
                        true
                    }
                }
                if (efab != null) {
                    btButtonFab = efab
                    btButtonFab!!.setOnLongClickListener { v ->
                        showBluetoothDialog(v)
                        true
                    }
                }
                setUpWidgetResources(
                    iv, efab,
                    { toggleBluetoothState() }, getDrawable(
                        BT_ICON,
                        SYSTEMUI_PACKAGE
                    ), getString(BT_LABEL, SYSTEMUI_PACKAGE)
                )
            }

            "torch" -> {
                if (iv != null) {
                    torchButton = iv
                }
                if (efab != null) {
                    torchButtonFab = efab
                }
                setUpWidgetResources(
                    iv,
                    efab,
                    { toggleFlashlight() },
                    getDrawable(TORCH_RES_INACTIVE, SYSTEMUI_PACKAGE),
                    getString(
                        TORCH_LABEL, SYSTEMUI_PACKAGE
                    )
                )
            }

            "timer" -> setUpWidgetResources(iv, efab, {
                mActivityLauncherUtils.launchTimer()
                vibrate(1)
            }, getDrawable("ic_alarm", SYSTEMUI_PACKAGE), modRes.getString(R.string.clock_timer))

            "camera" -> setUpWidgetResources(
                iv,
                efab,
                {
                    mActivityLauncherUtils.launchCamera()
                    vibrate(1)
                },
                getDrawable(CAMERA_ICON, SYSTEMUI_PACKAGE),
                getString(CAMERA_LABEL, SYSTEMUI_PACKAGE)
            )

            "calculator" -> setUpWidgetResources(
                iv,
                efab,
                { openCalculator() },
                getDrawable(CALCULATOR_ICON, SYSTEMUI_PACKAGE),
                getString(
                    CALCULATOR_LABEL, SYSTEMUI_PACKAGE
                )
            )

            "homecontrols" -> setUpWidgetResources(
                iv,
                efab,
                { view: View ->
                    this.launchHomeControls(
                        view
                    )
                },
                getDrawable(HOME_CONTROLS, SYSTEMUI_PACKAGE),
                getString(HOME_CONTROLS_LABEL, SYSTEMUI_PACKAGE)
            )

            "wallet" -> setUpWidgetResources(
                iv,
                efab,
                { view: View ->
                    this.launchWallet(
                        view
                    )
                },
                getDrawable(WALLET_ICON, SYSTEMUI_PACKAGE),
                getString(WALLET_LABEL, SYSTEMUI_PACKAGE)
            )

            "media" -> {
                if (iv != null) {
                    mediaButton = iv
                    mediaButton!!.setOnLongClickListener { true }
                }
                if (efab != null) {
                    mediaButtonFab = efab
                    mediaButtonFab!!.setOnLongClickListener { true }
                }
                setUpWidgetResources(
                    iv, efab,
                    { toggleMediaPlaybackState() },
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_play, mContext.theme),
                    getString(MEDIA_PLAY_LABEL, SYSTEMUI_PACKAGE)
                )
            }

            "weather" -> {
                if (iv != null) {
                    weatherButton = iv
                }
                if (efab != null) {
                    weatherButtonFab = efab
                }
                //setUpWidgetResources(iv, efab, v -> mActivityLauncherUtils.launchWeatherApp(), "ic_alarm", R.string.weather_data_unavailable);
//                enableWeatherUpdates()
            }

            "hotspot" -> {
                if (iv != null) {
                    hotspotButton = iv
                    hotspotButton!!.setOnLongClickListener {
                        mActivityLauncherUtils.launchSettingsComponent("com.android.settings.TetherSettings")
                        true
                    }
                }
                if (efab != null) {
                    hotspotButtonFab = efab
                    hotspotButtonFab!!.setOnLongClickListener {
                        mActivityLauncherUtils.launchSettingsComponent("com.android.settings.TetherSettings")
                        true
                    }
                }
                setUpWidgetResources(
                    iv, efab, { toggleHotspot() },
                    getDrawable(HOTSPOT_INACTIVE, SYSTEMUI_PACKAGE),
                    getString(HOTSPOT_LABEL, SYSTEMUI_PACKAGE)
                )
            }

            else -> {}
        }
    }

    private fun setUpWidgetResources(
        iv: ImageView?, efab: ExtendedFAB?,
        cl: OnClickListener, icon: Drawable?, text: String
    ) {
        if (efab != null) {
            efab.setOnClickListener(cl)
            efab.icon = icon
            efab.text = text
            if (mediaButtonFab === efab) {
                attachSwipeGesture(efab)
            }
        }
        if (iv != null) {
            iv.setOnClickListener(cl)
            iv.setImageDrawable(icon)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun attachSwipeGesture(view: View) {
        val gestureDetector = GestureDetector(mContext, object : SimpleOnGestureListener() {
            private val SWIPE_THRESHOLD = 100
            private val SWIPE_VELOCITY_THRESHOLD = 100
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                val diffX = e2.x - e1!!.x
                if (abs(diffX.toDouble()) > SWIPE_THRESHOLD && abs(velocityX.toDouble()) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        dispatchMediaKeyWithWakeLockToMediaSession(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
                    } else {
                        dispatchMediaKeyWithWakeLockToMediaSession(KeyEvent.KEYCODE_MEDIA_NEXT)
                    }
                    vibrate(1)
                    updateMediaController()
                    return true
                }
                return false
            }

            override fun onLongPress(e: MotionEvent) {
                super.onLongPress(e)
                mIsLongPress = true
                mHandler.postDelayed({ mIsLongPress = false }, 2500)
            }
        })
        view.setOnTouchListener { v, event ->
            val isClick: Boolean = gestureDetector.onTouchEvent(event)
            if ((event.action == MotionEvent.ACTION_UP) && !isClick && !mIsLongPress) {
                v.performClick()
            }
            true
        }
    }

    private fun setButtonActiveState(iv: ImageView?, efab: ExtendedFAB?, active: Boolean) {
        val bgTint: Int
        val tintColor: Int

        if (!mCustomColors) {
            if (active) {
                bgTint = if (isNightMode) mDarkColorActive else mLightColorActive
                tintColor = if (isNightMode) mDarkColor else mLightColor
            } else {
                bgTint = if (isNightMode) mDarkColor else mLightColor
                tintColor = if (isNightMode) mLightColor else mDarkColor
            }
            if (iv != null) {
                iv.backgroundTintList = ColorStateList.valueOf(bgTint)
                if (iv !== weatherButton) {
                    iv.imageTintList = ColorStateList.valueOf(tintColor)
                } else {
                    iv.imageTintList = null
                }
            }
            if (efab != null) {
                efab.backgroundTintList = ColorStateList.valueOf(bgTint)
                if (efab !== weatherButtonFab) {
                    efab.iconTint = ColorStateList.valueOf(tintColor)
                } else {
                    efab.iconTint = null
                }
                efab.setTextColor(tintColor)
            }
        } else {
            if (iv != null) {
                iv.backgroundTintList =
                    ColorStateList.valueOf(if (active) mSmallActiveColor else mSmallInactiveColor)
                if (iv !== weatherButton) {
                    iv.imageTintList =
                        ColorStateList.valueOf(if (active) mSmallIconActiveColor else mSmallIconInactiveColor)
                } else {
                    iv.imageTintList = null
                }
            }
            if (efab != null) {
                efab.backgroundTintList =
                    ColorStateList.valueOf(if (active) mBigActiveColor else mBigInactiveColor)
                if (efab !== weatherButtonFab) {
                    efab.iconTint =
                        ColorStateList.valueOf(if (active) mBigIconActiveColor else mBigIconInactiveColor)
                } else {
                    efab.iconTint = null
                }
                efab.setTextColor(if (active) mBigIconActiveColor else mBigIconInactiveColor)
            }
        }
    }

    private fun updateMediaState() {
        updateMediaPlaybackState()
        mHandler.postDelayed({ this.updateMediaPlaybackState() }, 250)
    }

    private fun toggleMediaPlaybackState() {
        if (isMediaPlaying) {
            mHandler.removeCallbacks(mMediaUpdater)
            dispatchMediaKeyWithWakeLockToMediaSession(KeyEvent.KEYCODE_MEDIA_PAUSE)
            updateMediaController()
        } else {
            mMediaUpdater.run()
            dispatchMediaKeyWithWakeLockToMediaSession(KeyEvent.KEYCODE_MEDIA_PLAY)
        }
    }

    private fun dispatchMediaKeyWithWakeLockToMediaSession(keycode: Int) {
        val keyIntent = Intent(Intent.ACTION_MEDIA_BUTTON, null)
        val keyEvent = KeyEvent(
            SystemClock.uptimeMillis(),
            SystemClock.uptimeMillis(),
            KeyEvent.ACTION_DOWN,
            keycode,
            0
        )
        keyIntent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent)
        var mediaEvent: KeyEvent? = KeyEvent(KeyEvent.ACTION_DOWN, keycode)
        mAudioManager!!.dispatchMediaKeyEvent(mediaEvent)

        mediaEvent = KeyEvent.changeAction(mediaEvent, KeyEvent.ACTION_UP)
        keyIntent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent)
        mAudioManager.dispatchMediaKeyEvent(mediaEvent)
    }

    private fun updateMediaPlaybackState() {
        val isPlaying = isMediaPlaying
        val icon = ResourcesCompat.getDrawable(
            modRes,
            if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play,
            mContext.theme
        )
        if (mediaButton != null) {
            mediaButton!!.setImageDrawable(icon)
            setButtonActiveState(mediaButton, null, isPlaying)
        }
        if (mediaButtonFab != null) {
            val trackTitle =
                if (mMediaMetadata != null) mMediaMetadata!!.getString(MediaMetadata.METADATA_KEY_TITLE) else ""
            if (!trackTitle.isNullOrEmpty() && mLastTrackTitle !== trackTitle) {
                mLastTrackTitle = trackTitle
            }
            val canShowTrackTitle = isPlaying || !mLastTrackTitle.isNullOrEmpty()
            mediaButtonFab!!.icon = icon
            mediaButtonFab!!.text = if (canShowTrackTitle) mLastTrackTitle else "Play"
            setButtonActiveState(null, mediaButtonFab, isPlaying)
        }
    }

    private val isMediaPlaying: Boolean
        get() = (isMediaControllerAvailable
                && PlaybackState.STATE_PLAYING == getMediaControllerPlaybackState(mController))

    private fun toggleFlashlight() {
        if (torchButton == null && torchButtonFab == null) return
        try {
            mCameraManager.setTorchMode(mCameraId!!, !isFlashOn)
            isFlashOn = !isFlashOn
            updateTorchButtonState()
            vibrate(1)
        } catch (e: Exception) {
            log("LockscreenWidgets toggleFlashlight error: " + e.message)
        }
    }

    private fun launchHomeControls(view: View) {
        log("LockscreenWidgets launchHomeControls")
        val controlsTile: Any = ControllersProvider.mDeviceControlsTile ?: return
        val finalView: View = if (view is ExtendedFAB) {
            view.parent as View
        } else {
            view
        }
        post {
            callMethod(
                controlsTile,
                "handleClick",
                finalView
            )
        }
        vibrate(1)
    }

    private fun launchWallet(view: View) {
        val mWalletTile: Any? = ControllersProvider.mWalletTile
        if (mWalletTile != null) {
            val finalView: View = if (view is ExtendedFAB) {
                view.parent as View
            } else {
                view
            }
            post {
                callMethod(
                    mWalletTile,
                    "handleClick",
                    finalView
                )
            }
        } else {
            mActivityLauncherUtils.launchWallet()
        }
        vibrate(1)
    }

    private fun openCalculator() {
        mActivityLauncherUtils.launchCalculator()
        vibrate(1)
    }

    @Suppress("deprecation")
    private fun toggleWiFi() {
        log("LockscreenWidgets toggleWiFi")
        val enabled: Boolean = mWifiManager!!.isWifiEnabled
        mWifiManager.isWifiEnabled = !enabled
        updateWiFiButtonState(!enabled)
        mHandler.postDelayed({ updateWiFiButtonState(isWifiEnabled) }, 350L)
        vibrate(1)
    }

    private fun toggleHotspot() {
        val mHotspotTile = ControllersProvider.mHotspotTile
        if (mHotspotTile != null) {
            val finalView = hotspotButton ?: hotspotButtonFab
            callMethod(mHotspotTile, "handleClick", finalView)
        }
        updateHotspotButtonState(0)
        postDelayed({ updateHotspotButtonState(0) }, 350L)
        vibrate(1)
    }

    private val isMobileDataEnabled: Boolean
        get() {
            try {
                val connectivityManager =
                    mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val cmClass = Class.forName(ConnectivityManager::class.java.name)
                val method = cmClass.getDeclaredMethod("getMobileDataEnabled")
                method.isAccessible = true
                // Call the method on the ConnectivityManager instance
                val result = method.invoke(connectivityManager)
                // Safely handle the return value
                return if (result is Boolean) result else false
            } catch (e: Exception) {
                log("LockscreenWidgets isMobileDataEnabled error: " + e.message)
                return false
            }
        }

    private val isWifiEnabled: Boolean
        get() {
            val enabled: Boolean = mWifiManager!!.isWifiEnabled
            log("LockscreenWidgets isWifiEnabled $enabled")
            return enabled
        }

    private fun toggleMobileData() {
        enqueueProxyCommand { proxy ->
            proxy?.runCommand("svc data " + if (isMobileDataEnabled) "disable" else "enable")
        }
        updateMobileDataState(!isMobileDataEnabled)
        mHandler.postDelayed({ updateMobileDataState(isMobileDataEnabled) }, 250L)
        vibrate(1)
    }

    private fun showInternetDialog(view: View) {
        if (Build.VERSION.SDK_INT < 34) {
            mActivityLauncherUtils.launchInternetSettings()
            return
        }

        log("LockscreenWidgets showInternetDialog")
        if (ControllersProvider.mCellularTile == null) return
        val finalView: View = if (view is ExtendedFAB) {
            view.parent as View
        } else {
            view
        }
        post {
            callMethod(
                ControllersProvider.mCellularTile,
                "handleClick",
                finalView
            )
        }
        vibrate(0)
    }

    /**
     * Toggles the ringer modes
     * Normal -> Vibrate -> Silent -> Normal
     */
    private fun toggleRingerMode() {
        if (mAudioManager != null) {
            val mode = mAudioManager.ringerMode
            when (mode) {
                AudioManager.RINGER_MODE_NORMAL -> callMethod(
                    mAudioManager,
                    "setRingerModeInternal",
                    AudioManager.RINGER_MODE_VIBRATE
                )

                AudioManager.RINGER_MODE_VIBRATE -> callMethod(
                    mAudioManager,
                    "setRingerModeInternal",
                    AudioManager.RINGER_MODE_SILENT
                )

                AudioManager.RINGER_MODE_SILENT -> callMethod(
                    mAudioManager,
                    "setRingerModeInternal",
                    AudioManager.RINGER_MODE_NORMAL
                )
            }
            updateRingerButtonState()
            vibrate(1)
        }
    }

    private fun updateTileButtonState(
        iv: ImageView?,
        efab: ExtendedFAB?,
        active: Boolean,
        icon: Drawable?,
        text: String
    ) {
        post {
            if (iv != null) {
                iv.setImageDrawable(icon)
                setButtonActiveState(iv, null, active)
            }
            if (efab != null) {
                efab.icon = icon
                efab.text = text
                setButtonActiveState(null, efab, active)
            }
        }
    }

    fun updateTorchButtonState() {
        if (!isWidgetEnabled("torch")) return
        log("LockscreenWidgets updateTorchButtonState $isFlashOn")
        updateTileButtonState(
            torchButton,
            torchButtonFab,
            isFlashOn,
            getDrawable(TORCH_RES_ACTIVE, SYSTEMUI_PACKAGE),
            getString(TORCH_LABEL, SYSTEMUI_PACKAGE)
        )
    }

    private val mRingerModeReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            updateRingerButtonState()
        }
    }

    init {
        instance = this

        this.layoutParams = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        mContext = context
        mAudioManager = mContext.getSystemService(AudioManager::class.java)
        mWifiManager = mContext.getSystemService(WifiManager::class.java)
        mTelephonyManager = mContext.getSystemService(TelephonyManager::class.java)
        mConnectivityManager = mContext.getSystemService(ConnectivityManager::class.java)
        mCameraManager = mContext.getSystemService(CameraManager::class.java)
        mDarkColor = ResourcesCompat.getColor(
            modRes,
            R.color.lockscreen_widget_background_color_dark,
            mContext.theme
        )
        mLightColor = ResourcesCompat.getColor(
            modRes,
            R.color.lockscreen_widget_background_color_light,
            mContext.theme
        )
        mDarkColorActive = ResourcesCompat.getColor(
            modRes,
            R.color.lockscreen_widget_active_color_dark,
            mContext.theme
        )
        mLightColorActive = ResourcesCompat.getColor(
            modRes,
            R.color.lockscreen_widget_active_color_light,
            mContext.theme
        )

        mActivityLauncherUtils = ActivityLauncherUtils(mContext, activityStarter)

        mHandler = Handler(Looper.getMainLooper())
        if (mWeatherClient == null) {
            mWeatherClient = OmniJawsClient(context)
        }

        try {
            mCameraId = mCameraManager.cameraIdList[0]
        } catch (e: Throwable) {
            log("LockscreenWidgets mCameraId error: " + e.message)
        }

        val container = LinearLayout(context)
        container.orientation = VERTICAL
        container.layoutParams = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // Device Widget Container
        mDeviceWidgetContainer = createDeviceWidgetContainer(context)
        container.addView(mDeviceWidgetContainer)

        // Add main widgets container
        mMainWidgetsContainer = createMainWidgetsContainer(context)
        container.addView(mMainWidgetsContainer)

        // Add secondary widgets container
        mSecondaryWidgetsContainer = createSecondaryWidgetsContainer(context)
        container.addView(mSecondaryWidgetsContainer)

        addView(container)

        val ringerFilter = IntentFilter("android.media.INTERNAL_RINGER_MODE_CHANGED_ACTION")
        mContext.registerReceiver(mRingerModeReceiver, ringerFilter)
        mMediaUpdater = object : Runnable {
            override fun run() {
                updateMediaController()
                mHandler.postDelayed(this, 1000)
            }
        }
        updateMediaController()

        ControllersProvider.getInstance().registerMobileDataCallback(mMobileDataCallback)
        ControllersProvider.getInstance().registerWifiCallback(mWifiCallback)
        ControllersProvider.getInstance().registerBluetoothCallback(mBluetoothCallback)
        ControllersProvider.getInstance().registerTorchModeCallback(mTorchCallback)
        ControllersProvider.getInstance().registerHotspotCallback(mHotspotCallback)
        ControllersProvider.getInstance().registerDozingCallback(mDozeCallback)

        // Add a Screen On Receiver so we can update the widgets state when the screen is turned on
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mContext.registerReceiver(
                mScreenOnReceiver,
                IntentFilter(Intent.ACTION_SCREEN_ON),
                Context.RECEIVER_EXPORTED
            )
        } else {
            mContext.registerReceiver(
                mScreenOnReceiver,
                IntentFilter(Intent.ACTION_SCREEN_ON)
            )
        }
    }

    @Suppress("deprecation")
    private fun updateWiFiButtonState(enabled: Boolean) {
        log(
            "LockscreenWidgets updateWiFiButtonState $enabled | " + isWidgetEnabled(
                "wifi"
            )
        )
        if (!isWidgetEnabled("wifi")) return
        if (wifiButton == null && wifiButtonFab == null) return
        val connected: Boolean
        var theSsid: String = mWifiManager!!.connectionInfo.ssid
        if (theSsid == WifiManager.UNKNOWN_SSID) {
            theSsid = getString(WIFI_LABEL, SYSTEMUI_PACKAGE)
            connected = false
        } else {
            if (theSsid.startsWith("\"") && theSsid.endsWith("\"")) {
                theSsid = theSsid.substring(1, theSsid.length - 1)
            }
            connected = true
        }
        val icon: Drawable? = getDrawable(
            if (enabled && connected) WIFI_ACTIVE
            else if (enabled) WIFI_ACTIVE
            else WIFI_INACTIVE, FRAMEWORK_PACKAGE
        )
        updateTileButtonState(
            wifiButton, wifiButtonFab,
            isWifiEnabled,
            icon, theSsid
        )
    }

    private fun updateRingerButtonState() {
        log("LockscreenWidgets updateRingerButtonState " + (isWidgetEnabled("ringer")) + " | " + (ringerButton == null) + " | " + (ringerButtonFab == null))
        if (!isWidgetEnabled("ringer")) return
        if (ringerButton == null && ringerButtonFab == null) return
        if (mAudioManager != null) {
            val soundActive = mAudioManager.ringerMode == AudioManager.RINGER_MODE_NORMAL
            updateTileButtonState(
                ringerButton, ringerButtonFab,
                soundActive,
                ringerDrawable,
                ringerText
            )
        }
    }

    private fun updateMobileDataState(enabled: Boolean) {
        if (!isWidgetEnabled("data")) return
        if (dataButton == null && dataButtonFab == null) return
        val inactive = getString(DATA_LABEL, SYSTEMUI_PACKAGE)
        val networkName = activeMobileDataCarrier.ifEmpty { inactive }
        val hasNetwork = enabled && networkName.isNotEmpty()
        updateTileButtonState(
            dataButton,
            dataButtonFab,
            enabled,
            getDrawable(DATA_ICON, FRAMEWORK_PACKAGE),
            if (hasNetwork) networkName else inactive
        )
    }

    private val activeMobileDataCarrier: String
        get() {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val telephonyManager =
                context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

            val activeNetwork = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)

            if (networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true) {
                return telephonyManager.networkOperatorName
            }

            return "" // No active mobile data connection
        }

    private fun toggleBluetoothState() {
        val bluetoothController: Any = ControllersProvider.mBluetoothController ?: return
        callMethod(bluetoothController, "setBluetoothEnabled", !isBluetoothEnabled)
        updateBtState()
        mHandler.postDelayed({ this.updateBtState() }, 350L)
        vibrate(1)
    }

    private fun showBluetoothDialog(view: View) {
        if (Build.VERSION.SDK_INT < 34) {
            mActivityLauncherUtils.launchBluetoothSettings()
            return
        }
        val finalView: View = if (view is ExtendedFAB) {
            view.parent as View
        } else {
            view
        }
        post {
            callMethod(
                ControllersProvider.mBluetoothTile,
                "handleSecondaryClick",
                finalView
            )
        }
        vibrate(0)
    }

    private fun updateBtState() {
        if (!isWidgetEnabled("bt")) return
        log("LockscreenWidgets updateBtState $isBluetoothOn")
        if (btButton == null && btButtonFab == null) return
        val bluetoothController: Any? = ControllersProvider.mBluetoothController
        var deviceName: String? = ""
        if (isBluetoothEnabled && bluetoothController != null)
            deviceName = callMethod(bluetoothController, "getConnectedDeviceName") as String?
        val isConnected = !deviceName.isNullOrEmpty()
        val icon = getDrawable(
            BT_ICON,
            SYSTEMUI_PACKAGE
        )
        updateTileButtonState(
            btButton, btButtonFab, isBluetoothOn,
            icon,
            if (isConnected) deviceName!!
            else getString(BT_LABEL, SYSTEMUI_PACKAGE)
        )
    }

    private fun updateHotspotButtonState(numDevices: Int) {
        if (!isWidgetEnabled("hotspot")) return
        val inactiveString = getString(HOTSPOT_LABEL, SYSTEMUI_PACKAGE)
        var activeString = inactiveString
        val hotspotEnabled = isHotspotEnabled()
        if (hotspotEnabled) {
            val hotspotSSID: String = getHotspotSSID()
            val devices = "($numDevices)"
            if (hotspotSSID.isNotEmpty()) {
                activeString = if (numDevices > 0) "$hotspotSSID $devices"
                else hotspotSSID
            }
        }
        updateTileButtonState(
            hotspotButton,
            hotspotButtonFab,
            isHotspotEnabled(),
            getDrawable(if (hotspotEnabled) HOTSPOT_ACTIVE else HOTSPOT_INACTIVE, SYSTEMUI_PACKAGE),
            if (hotspotEnabled) activeString
            else inactiveString
        )
    }

    @Suppress("deprecation")
    private val isBluetoothEnabled: Boolean
        get() {
            val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            return mBluetoothAdapter != null && mBluetoothAdapter.isEnabled
        }

    private fun isHotspotEnabled(): Boolean {
        try {
            val wifiManager = mContext.getSystemService(WifiManager::class.java)
            val method: Method = wifiManager.javaClass.getDeclaredMethod("getWifiApState")
            method.isAccessible = true
            val actualState =
                method.invoke(wifiManager) as Int
            return actualState == HOTSPOT_ENABLED
        } catch (t: Throwable) {
            log("LockscreenWidgetsView isHotspotEnabled error: " + t.message)
        }
        return false
    }

    @Suppress("deprecation")
    private fun getHotspotSSID(): String {
        try {
            val methods: Array<Method> = WifiManager::class.java.getDeclaredMethods()
            for (m in methods) {
                if (m.name == "getWifiApConfiguration") {
                    val config = m.invoke(mWifiManager) as WifiConfiguration
                    return config.SSID
                }
            }
        } catch (t: Throwable) {
            log("LockscreenWidgetsView getHotspotSSID error: " + t.message)
        }
        return ""
    }

    private fun sameSessions(a: MediaController?, b: MediaController): Boolean {
        if (a == b) {
            return true
        }
        if (a == null) {
            return false
        }
        return false
    }

    private fun getMediaControllerPlaybackState(controller: MediaController?): Int {
        if (controller != null) {
            val playbackState = controller.playbackState
            if (playbackState != null) {
                return playbackState.state
            }
        }
        return PlaybackState.STATE_NONE
    }

    /**
     * Set the options for the lockscreen widgets
     * @param lsWidgets true if lockscreen widgets are enabled
     * @param deviceWidget true if device widget is enabled
     * @param mainWidgets comma separated list of main widgets
     * @param secondaryWidgets comma separated list of secondary widgets
     */
    fun setOptions(
        lsWidgets: Boolean, deviceWidget: Boolean,
        mainWidgets: String, secondaryWidgets: String
    ) {
        log(
            "LockscreenWidgets setOptions " + lsWidgets +
                    " | " + deviceWidget + " | " + mainWidgets + " | " + secondaryWidgets
        )
        instance!!.lockscreenWidgetsEnabled = lsWidgets
        instance!!.deviceWidgetsEnabled = deviceWidget
        instance!!.mMainLockscreenWidgetsList = mainWidgets
        instance!!.mMainWidgetsList = listOf(
            *instance!!.mMainLockscreenWidgetsList!!.split(",".toRegex())
                .dropLastWhile { it.isEmpty() }
                .toTypedArray())
        instance!!.mSecondaryLockscreenWidgetsList = secondaryWidgets
        instance!!.mSecondaryWidgetsList = listOf(
            *instance!!.mSecondaryLockscreenWidgetsList!!.split(",".toRegex())
                .dropLastWhile { it.isEmpty() }
                .toTypedArray())
        instance!!.updateWidgetViews()
    }

    fun setIsLargeClock(isLargeClock: Boolean) {
        log("LockscreenWidgets setIsLargeClock $isLargeClock")
        instance!!.mIsLargeClock = isLargeClock
        instance!!.updateContainerVisibility()
    }

    /**
     * Set the options for the Device Widget
     * @param customColor true if custom color is enabled
     * @param linearColor color for linear battery progressbar
     * @param circularColor color for circular progressbar
     * @param textColor color for text
     * @param devName device name, keep blank for default Build.MODEL
     */
    fun setDeviceWidgetOptions(
        customColor: Boolean,
        linearColor: Int,
        circularColor: Int,
        textColor: Int,
        devName: String?
    ) {
        if (instance!!.mDeviceWidgetView == null) return
        instance!!.mDeviceWidgetView!!.setCustomColor(customColor, linearColor, circularColor)
        instance!!.mDeviceWidgetView!!.setTextCustomColor(textColor)
        instance!!.mDeviceWidgetView!!.setDeviceName(devName)
    }

    fun setCustomColors(
        customColorsEnabled: Boolean,
        bigInactive: Int, bigActive: Int, smallInactive: Int, smallActive: Int,
        bigIconInactive: Int, bigIconActive: Int, smallIconInactive: Int, smallIconActive: Int
    ) {
        log("LockscreenWidgets setCustomColors $customColorsEnabled")
        instance!!.mCustomColors = customColorsEnabled
        instance!!.mBigInactiveColor = bigInactive
        instance!!.mBigActiveColor = bigActive
        instance!!.mSmallInactiveColor = smallInactive
        instance!!.mSmallActiveColor = smallActive
        instance!!.mBigIconInactiveColor = bigIconInactive
        instance!!.mBigIconActiveColor = bigIconActive
        instance!!.mSmallIconInactiveColor = smallIconInactive
        instance!!.mSmallIconActiveColor = smallIconActive
        instance!!.updateWidgetViews()
    }

    fun setActivityStarter(activityStarter: Any?) {
        mActivityLauncherUtils = ActivityLauncherUtils(mContext, activityStarter)
    }

    fun setDozingState(isDozing: Boolean) {
        log("LockscreenWidgets setDozingState $isDozing")
        instance!!.mDozing = isDozing
        instance!!.updateContainerVisibility()
    }

    @Suppress("DiscouragedApi")
    private fun getDrawable(drawableRes: String, pkg: String): Drawable? {
        try {
            return ContextCompat.getDrawable(
                mContext,
                mContext.resources.getIdentifier(drawableRes, "drawable", pkg)
            )
        } catch (t: Throwable) {
            // We have a calculator icon, so if SystemUI doesn't just return ours
            if ((drawableRes == CALCULATOR_ICON)) return ResourcesCompat.getDrawable(
                modRes,
                R.drawable.ic_calculator,
                mContext.theme
            )

            log("LockscreenWidgets getDrawable $drawableRes from $pkg error $t")
            return null
        }
    }

    @Suppress("DiscouragedApi")
    private fun getString(stringRes: String, pkg: String): String {
        try {
            return mContext.resources.getString(
                mContext.resources.getIdentifier(stringRes, "string", pkg)
            )
        } catch (t: Throwable) {
            // We have our own strings too, so return them if SystemUI doesn't
            when (stringRes) {
                HOME_CONTROLS_LABEL -> {
                    return modRes.getString(R.string.home_controls)
                }

                CALCULATOR_LABEL -> {
                    return modRes.getString(R.string.calculator)
                }

                CAMERA_LABEL -> {
                    return modRes.getString(R.string.camera)
                }

                WALLET_LABEL -> {
                    return modRes.getString(R.string.wallet)
                }
            }
            log("LockscreenWidgets getString $stringRes from $pkg error $t")
            return ""
        }
    }

    private val ringerDrawable: Drawable?
        get() {
            val resName = when (mAudioManager!!.ringerMode) {
                AudioManager.RINGER_MODE_NORMAL -> R.drawable.ic_ringer_normal
                AudioManager.RINGER_MODE_VIBRATE -> R.drawable.ic_ringer_vibrate
                AudioManager.RINGER_MODE_SILENT -> R.drawable.ic_ringer_mute
                else -> throw IllegalStateException("Unexpected value: " + mAudioManager.ringerMode)
            }

            return ResourcesCompat.getDrawable(
                modRes,
                resName,
                mContext.theme
            )
        }

    private val ringerText: String
        get() {
            val resName = when (mAudioManager!!.ringerMode) {
                AudioManager.RINGER_MODE_NORMAL -> RINGER_NORMAL_TEXT
                AudioManager.RINGER_MODE_VIBRATE -> RINGER_VIBRATE_TEXT
                AudioManager.RINGER_MODE_SILENT -> RINGER_SILENT_TEXT
                else -> throw IllegalStateException("Unexpected value: " + mAudioManager.ringerMode)
            }

            return getString(resName, SYSTEMUI_PACKAGE)
        }

    /**
     * Vibrate the device
     * @param type 0 = click, 1 = tick
     */
    private fun vibrate(type: Int) {
        if (type == 0) {
            this.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        } else if (type == 1) {
            this.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
        }
    }

    companion object {

        const val HOTSPOT_ENABLED = 13

        const val BT_ICON: String = "qs_bluetooth_icon_on"
        const val DATA_ICON: String = "perm_group_network"
        const val TORCH_RES_ACTIVE: String = "qs_flashlight_icon_on"
        const val TORCH_RES_INACTIVE: String = "qs_flashlight_icon_off"
        const val WIFI_ACTIVE: String = "ic_wifi_signal_4"
        const val WIFI_INACTIVE: String = "ic_wifi_signal_0"
        const val HOME_CONTROLS: String = "controls_icon"
        const val CALCULATOR_ICON: String = "status_bar_qs_calculator_inactive"
        const val CAMERA_ICON: String = "ic_camera" // Use qs camera access icon for camera
        const val WALLET_ICON: String = "ic_wallet_lockscreen"
        const val HOTSPOT_ACTIVE: String = "qs_hotspot_icon_on"
        const val HOTSPOT_INACTIVE: String = "qs_hotspot_icon_off"

        const val GENERAL_INACTIVE: String = "switch_bar_off"
        const val GENERAL_ACTIVE: String = "switch_bar_on"

        const val BT_LABEL: String = "quick_settings_bluetooth_label"
        const val DATA_LABEL: String = "quick_settings_internet_label"
        const val WIFI_LABEL: String = "quick_settings_wifi_label"
        const val RINGER_LABEL_INACTIVE: String = "state_button_silence"
        const val TORCH_LABEL: String = "quick_settings_flashlight_label"
        const val HOME_CONTROLS_LABEL: String = "quick_controls_title"
        const val MEDIA_PLAY_LABEL: String = "controls_media_button_play"
        const val CALCULATOR_LABEL: String = "keyboard_shortcut_group_applications_calculator"
        const val CAMERA_LABEL: String = "accessibility_camera_button"
        const val WALLET_LABEL: String = "wallet_title"
        const val HOTSPOT_LABEL: String = "quick_settings_hotspot_label"

        const val RINGER_NORMAL_TEXT = "volume_footer_ring"
        const val RINGER_VIBRATE_TEXT = "state_button_vibration"
        const val RINGER_SILENT_TEXT = "state_button_silence"

        @Volatile
        private var instance: LockscreenWidgetsView? = null

        fun getInstance(context: Context, activityStarter: Any?): LockscreenWidgetsView {
            return instance ?: synchronized(this) {
                instance ?: LockscreenWidgetsView(context, activityStarter).also { instance = it }
            }
        }

        fun getInstance(): LockscreenWidgetsView? {
            return instance
        }
    }
}
