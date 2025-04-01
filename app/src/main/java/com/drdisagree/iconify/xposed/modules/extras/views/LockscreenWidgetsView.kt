package com.drdisagree.iconify.xposed.modules.extras.views

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaController.PlaybackInfo
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
import android.telephony.TelephonyManager
import android.text.TextUtils
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
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.R
import com.drdisagree.iconify.data.common.Const.FRAMEWORK_PACKAGE
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.utils.OmniJawsClient
import com.drdisagree.iconify.xposed.HookEntry.Companion.enqueueProxyCommand
import com.drdisagree.iconify.xposed.HookRes.Companion.modRes
import com.drdisagree.iconify.xposed.modules.extras.callbacks.ControllersProvider
import com.drdisagree.iconify.xposed.modules.extras.callbacks.ThemeChangeCallback
import com.drdisagree.iconify.xposed.modules.extras.utils.ActivityLauncherUtils
import com.drdisagree.iconify.xposed.modules.extras.utils.DisplayUtils.isNightMode
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.getExpandableView
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.reAddView
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.toPx
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.log
import com.drdisagree.iconify.xposed.modules.lockscreen.widgets.LockscreenWidgets.Companion.launchableImageViewClass
import com.drdisagree.iconify.xposed.modules.lockscreen.widgets.LockscreenWidgets.Companion.launchableLinearLayoutClass
import java.lang.reflect.Method
import java.util.Locale
import kotlin.math.abs
import kotlin.math.min

@SuppressLint("ViewConstructor")
class LockscreenWidgetsView(private val context: Context, activityStarter: Any?) :
    LinearLayout(context), OmniJawsClient.OmniJawsObserver {

    private val mContext: Context
    private var appContext: Context? = null

    private var mWeatherClient: OmniJawsClient? = null
    private var mWeatherInfo: OmniJawsClient.WeatherInfo? = null

    // Two Linear Layouts, one for main widgets and one for secondary widgets
    private var mDeviceWidgetContainer: LinearLayout? = null
    private var mMainWidgetsContainer: LinearLayout? = null
    private var mSecondaryWidgetsContainer: LinearLayout? = null
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
    private var mDarkColor: Int = 0
    private var mDarkColorActive: Int = 0
    private var mLightColor: Int = 0
    private var mLightColorActive: Int = 0

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

    // Widgets Dimens
    private var mFabWidth = 0
    private var mFabHeight = 0
    private var mFabMarginStart = 0
    private var mFabMarginEnd = 0
    private var mFabPadding = 0
    private var mWidgetCircleSize = 0
    private var mWidgetMarginHorizontal = 0
    private var mWidgetMarginVertical = 0
    private var mWidgetIconPadding = 0
    private var mWidgetsRoundness = 100
    private var mWidgetsScale = 1f

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

    private var mIsLongPress = false

    private val mCameraManager: CameraManager
    private var mCameraId: String? = null
    private var isFlashOn = false

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
                updateMobileDataState(isMobileDataEnabled)
            }

            override fun setNoSims(show: Boolean, simDetected: Boolean) {
                updateMobileDataState(simDetected && isMobileDataEnabled)
            }

            override fun setIsAirplaneMode(mIconState: Any?) {
                updateMobileDataState(
                    !(mIconState.getField("visible") as Boolean) && isMobileDataEnabled
                )
            }
        }

    private val mWifiCallback: ControllersProvider.OnWifiChanged =
        object : ControllersProvider.OnWifiChanged {
            override fun onWifiChanged(mWifiIndicators: Any?) {
                updateWiFiButtonState(isWifiEnabled)
            }
        }

    private val mBluetoothCallback: ControllersProvider.OnBluetoothChanged =
        object : ControllersProvider.OnBluetoothChanged {
            override fun onBluetoothChanged(enabled: Boolean) {
                isBluetoothOn = enabled
                updateBtState()
            }
        }

    private val mTorchCallback: ControllersProvider.OnTorchModeChanged =
        object : ControllersProvider.OnTorchModeChanged {
            override fun onTorchModeChanged(enabled: Boolean) {
                isFlashOn = enabled
                updateTorchButtonState()
            }
        }

    private val mHotspotCallback: ControllersProvider.OnHotspotChanged =
        object : ControllersProvider.OnHotspotChanged {
            override fun onHotspotChanged(enabled: Boolean, connectedDevices: Int) {
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

    private val mThemeChangeCallback: ThemeChangeCallback.OnThemeChangedListener =
        object : ThemeChangeCallback.OnThemeChangedListener {
            override fun onThemeChanged() {
                loadColors()
                updateWidgetViews()
            }
        }

    private fun createDeviceWidgetContainer(context: Context): LinearLayout {
        return LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            if (mDeviceWidgetView == null) mDeviceWidgetView = DeviceWidgetView(context).apply {
                setScaling(mWidgetsScale)
            }

            reAddView(mDeviceWidgetView)
            setPadding(0, 0, 0, mContext.toPx(18))
        }
    }

    private fun createMainWidgetsContainer(context: Context): LinearLayout {
        val mainWidgetsContainer: LinearLayout = try {
            launchableLinearLayoutClass!!.getConstructor(Context::class.java)
                .newInstance(context) as LinearLayout
        } catch (e: Exception) {
            // LaunchableLinearLayout not found or other error, ensure the creation of our ImageView
            LinearLayout(context)
        }.apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

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
        return ExtendedFAB(context).apply {
            id = generateViewId()
            layoutParams = LayoutParams(
                0,
                (mFabHeight * mWidgetsScale).toInt(),
                1f
            ).apply {
                maxWidth = (mFabWidth * mWidgetsScale).toInt()
                setMargins(
                    (mFabMarginStart * mWidgetsScale).toInt(),
                    0,
                    (mFabMarginEnd * mWidgetsScale).toInt(),
                    0
                )
            }
            setPadding(
                (mFabPadding * mWidgetsScale).toInt(),
                (mFabPadding * mWidgetsScale).toInt(),
                (mFabPadding * mWidgetsScale).toInt(),
                (mFabPadding * mWidgetsScale).toInt()
            )
            gravity = Gravity.CENTER_VERTICAL
            maxLines = 2
            ellipsize = TextUtils.TruncateAt.END
        }
    }

    private fun createSecondaryWidgetsContainer(context: Context): LinearLayout {
        val secondaryWidgetsContainer: LinearLayout = try {
            launchableLinearLayoutClass!!.getConstructor(Context::class.java)
                .newInstance(context) as LinearLayout
        } catch (e: Exception) {
            // LaunchableLinearLayout not found or other error, ensure the creation of our ImageView
            LinearLayout(context)
        }.apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_HORIZONTAL
            layoutParams = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            (layoutParams as MarginLayoutParams).apply {
                topMargin = modRes.getDimensionPixelSize(R.dimen.kg_widget_margin_vertical)
                bottomMargin = modRes.getDimensionPixelSize(R.dimen.kg_widget_margin_bottom)
            }
        }

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
        val imageView = try {
            launchableImageViewClass!!.getConstructor(Context::class.java)
                .newInstance(context) as ImageView
        } catch (e: Exception) {
            // LaunchableImageView not found or other error, ensure the creation of our ImageView
            ImageView(context)
        }.apply {
            id = generateViewId()
            layoutParams = LayoutParams(
                (mWidgetCircleSize * mWidgetsScale).toInt(),
                (mWidgetCircleSize * mWidgetsScale).toInt()
            ).apply {
                setMargins(
                    (mWidgetMarginHorizontal * mWidgetsScale).toInt(),
                    0,
                    (mWidgetMarginHorizontal * mWidgetsScale).toInt(),
                    0
                )
            }
            setPadding(
                (mWidgetIconPadding * mWidgetsScale).toInt(),
                (mWidgetIconPadding * mWidgetsScale).toInt(),
                (mWidgetIconPadding * mWidgetsScale).toInt(),
                (mWidgetIconPadding * mWidgetsScale).toInt()
            )
            isFocusable = true
            isClickable = true
        }

        return imageView
    }

    private val isMediaControllerAvailable: Boolean
        get() {
            val mediaController = activeLocalMediaController
            return mediaController != null && !mediaController.packageName.isNullOrEmpty()
        }

    private val activeLocalMediaController: MediaController?
        get() {
            val mediaSessionManager =
                mContext.getSystemService(MediaSessionManager::class.java)
            var localController: MediaController? = null
            val remoteMediaSessionLists: MutableList<String> = ArrayList()
            for (controller: MediaController in mediaSessionManager.getActiveSessions(null)) {
                val pi = controller.playbackInfo
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

        val localController = activeLocalMediaController
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
        if (visibility == VISIBLE && lockscreenWidgetsEnabled) {
            onVisible()
        }
    }

    private fun enableWeatherUpdates() {
        mWeatherClient?.addObserver(this)
        queryAndUpdateWeather()
    }

    private fun disableWeatherUpdates() {
        mWeatherClient?.removeObserver(this)
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
            if (mWeatherClient == null || !mWeatherClient!!.isOmniJawsEnabled) return

            mWeatherClient!!.queryWeather()
            mWeatherInfo = mWeatherClient!!.mCachedInfo

            if (mWeatherInfo != null) {
                // OpenWeatherMap
                var formattedCondition: String = mWeatherInfo!!.condition!!
                if (formattedCondition.lowercase(Locale.getDefault()).contains("clouds") ||
                    formattedCondition.lowercase(Locale.getDefault()).contains("overcast")
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
                    val words = formattedCondition.split("_".toRegex())
                        .dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                    val formattedConditionBuilder = StringBuilder()
                    for (word in words) {
                        val capitalizedWord = word.substring(0, 1)
                            .uppercase(Locale.getDefault()) + word.substring(1)
                        formattedConditionBuilder.append(capitalizedWord).append(" ")
                    }
                    formattedCondition = formattedConditionBuilder.toString().trim { it <= ' ' }
                }

                val drawable: Drawable =
                    mWeatherClient!!.getWeatherConditionImage(mWeatherInfo!!.conditionCode)

                if (weatherButtonFab != null) {
                    weatherButtonFab!!.icon = drawable
                    weatherButtonFab!!.text =
                        (mWeatherInfo!!.temp + mWeatherInfo!!.tempUnits) + " • " + formattedCondition
                    weatherButtonFab!!.iconTint = null
                }

                if (weatherButton != null) {
                    weatherButton!!.setImageDrawable(drawable)
                    weatherButton!!.imageTintList = null
                }
            }
        } catch (e: java.lang.Exception) {
            log(this@LockscreenWidgetsView, "Error updating weather: " + e.message)
        }
    }

    private fun onVisible() {
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
        updateWidgetViews()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        updateWidgetViews()
    }

    private fun updateContainerVisibility() {
        val isMainWidgetsEmpty = mMainLockscreenWidgetsList.isNullOrEmpty()
        val isSecondaryWidgetsEmpty = mSecondaryLockscreenWidgetsList.isNullOrEmpty()
        val isEmpty = isMainWidgetsEmpty && isSecondaryWidgetsEmpty

        mDeviceWidgetContainer?.visibility = if (deviceWidgetsEnabled) {
            if (mIsLargeClock) View.GONE else View.VISIBLE
        } else {
            View.GONE
        }
        mMainWidgetsContainer?.visibility = if (isMainWidgetsEmpty) GONE else VISIBLE
        mSecondaryWidgetsContainer?.visibility =
            if (isSecondaryWidgetsEmpty || mIsLargeClock) GONE else VISIBLE

        val shouldHideContainer = isEmpty || mDozing || !lockscreenWidgetsEnabled
        visibility = if (shouldHideContainer) GONE else VISIBLE
    }

    private fun updateWidgetViews() {
        if (mMainWidgetViews != null && mMainWidgetsList != null) {
            for (i in mMainWidgetViews!!.indices) {
                mMainWidgetViews?.get(i)?.visibility =
                    if (i < mMainWidgetsList!!.size) VISIBLE else GONE
            }

            for (i in 0 until min(mMainWidgetsList!!.size, mMainWidgetViews!!.size)) {
                val widgetType: String? = mMainWidgetsList?.get(i)

                if (mMainWidgetViews?.get(i) != null && widgetType != null) {
                    setUpWidgetViews(efab = mMainWidgetViews!![i], type = widgetType)
                    updateMainWidgetResources(mMainWidgetViews!![i])
                }
            }
        }

        if (mSecondaryWidgetViews != null && mSecondaryWidgetsList != null) {
            for (i in mSecondaryWidgetViews!!.indices) {
                mSecondaryWidgetViews?.get(i)?.visibility =
                    if (i < mSecondaryWidgetsList!!.size) VISIBLE else GONE
            }

            for (i in 0 until min(mSecondaryWidgetsList!!.size, mSecondaryWidgetViews!!.size)) {
                val widgetType: String? = mSecondaryWidgetsList?.get(i)

                if (mSecondaryWidgetViews?.get(i) != null && widgetType != null) {
                    setUpWidgetViews(iv = mSecondaryWidgetViews!![i], type = widgetType)
                    updateWidgetsResources(mSecondaryWidgetViews!![i])
                }
            }
        }

        updateContainerVisibility()
        updateMediaController()
    }

    @Suppress("SameParameterValue")
    private fun updateMainWidgetResources(efab: ExtendedFAB?) {
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
            efab.layoutParams = params
        }
    }

    private fun updateWidgetsResources(iv: ImageView?) {
        if (iv == null) return

        iv.background = createWidgetBackgroundDrawable()
        setButtonActiveState(iv, null, false)
    }

    private fun setUpWidgetViews(iv: ImageView? = null, efab: ExtendedFAB? = null, type: String) {
        when (type) {
            "none" -> {
                iv?.visibility = GONE
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
                    imageView = iv,
                    extendedFAB = efab,
                    clickListener = { toggleWiFi() },
                    icon = getDrawable(WIFI_INACTIVE, FRAMEWORK_PACKAGE),
                    text = getString(WIFI_LABEL, SYSTEMUI_PACKAGE)
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
                    imageView = iv,
                    extendedFAB = efab,
                    clickListener = { toggleMobileData() },
                    icon = getDrawable(DATA_ICON, FRAMEWORK_PACKAGE),
                    text = getString(DATA_LABEL, SYSTEMUI_PACKAGE)
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
                    imageView = iv,
                    extendedFAB = efab,
                    clickListener = { toggleRingerMode() },
                    icon = getDrawable(RING_VOLUME, SYSTEMUI_PACKAGE)
                        ?: ResourcesCompat.getDrawable(
                            modRes,
                            R.drawable.ic_ringer_normal,
                            mContext.theme
                        ),
                    text = ringerText
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
                    imageView = iv,
                    extendedFAB = efab,
                    clickListener = { toggleBluetoothState() },
                    icon = getDrawable(BT_ICON, SYSTEMUI_PACKAGE),
                    text = getString(BT_LABEL, SYSTEMUI_PACKAGE)
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
                    imageView = iv,
                    extendedFAB = efab,
                    clickListener = { toggleFlashlight() },
                    icon = getDrawable(TORCH_INACTIVE, SYSTEMUI_PACKAGE),
                    text = getString(TORCH_LABEL, SYSTEMUI_PACKAGE)
                )
            }

            "timer" -> setUpWidgetResources(
                imageView = iv,
                extendedFAB = efab,
                clickListener = {
                    mActivityLauncherUtils.launchTimer()
                    vibrate(1)
                },
                icon = getDrawable(ALARM_ICON, SYSTEMUI_PACKAGE),
                text = modRes.getString(R.string.clock_timer)
            )

            "camera" -> setUpWidgetResources(
                imageView = iv,
                extendedFAB = efab,
                clickListener = {
                    mActivityLauncherUtils.launchCamera()
                    vibrate(1)
                },
                icon = getDrawable(CAMERA_ICON1, SYSTEMUI_PACKAGE)
                    ?: getDrawable(CAMERA_ICON2, SYSTEMUI_PACKAGE),
                text = getString(CAMERA_LABEL, SYSTEMUI_PACKAGE)
            )

            "calculator" -> setUpWidgetResources(
                imageView = iv,
                extendedFAB = efab,
                clickListener = { openCalculator() },
                icon = getDrawable(CALCULATOR_ICON, SYSTEMUI_PACKAGE),
                text = getString(CALCULATOR_LABEL, SYSTEMUI_PACKAGE)
            )

            "homecontrols" -> setUpWidgetResources(
                imageView = iv,
                extendedFAB = efab,
                clickListener = { view: View -> this.launchHomeControls(view) },
                icon = getDrawable(HOME_CONTROLS, SYSTEMUI_PACKAGE),
                text = getString(HOME_CONTROLS_LABEL, SYSTEMUI_PACKAGE)
            )

            "wallet" -> setUpWidgetResources(
                imageView = iv,
                extendedFAB = efab,
                clickListener = { view: View -> this.launchWallet(view) },
                icon = getDrawable(WALLET_ICON, SYSTEMUI_PACKAGE),
                text = getString(WALLET_LABEL, SYSTEMUI_PACKAGE)
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
                    imageView = iv, extendedFAB = efab,
                    clickListener = { toggleMediaPlaybackState() },
                    icon = getDrawable(MEDIA_PLAY, SYSTEMUI_PACKAGE)
                        ?: ResourcesCompat.getDrawable(
                            modRes,
                            R.drawable.ic_play,
                            mContext.theme
                        ),
                    text = getString(MEDIA_PLAY_LABEL, SYSTEMUI_PACKAGE)
                )
            }

            "weather" -> {
                if (iv != null) {
                    weatherButton = iv
                }

                if (efab != null) {
                    weatherButtonFab = efab
                }

                // Set a null on click listener to weather button to avoid running previous button action
                setUpWidgetResources(
                    imageView = iv,
                    extendedFAB = efab,
                    clickListener = { mActivityLauncherUtils.launchWeatherActivity(false) },
                    icon = ResourcesCompat.getDrawable(
                        appContext!!.resources,
                        R.drawable.google_30,
                        appContext!!.theme
                    ),
                    text = appContext!!.getString(R.string.weather_settings)
                )
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
                    imageView = iv,
                    extendedFAB = efab,
                    clickListener = { toggleHotspot() },
                    icon = getDrawable(HOTSPOT_INACTIVE, SYSTEMUI_PACKAGE),
                    text = getString(HOTSPOT_LABEL, SYSTEMUI_PACKAGE)
                )
            }

            else -> {}
        }
    }

    private fun setUpWidgetResources(
        imageView: ImageView?,
        extendedFAB: ExtendedFAB?,
        clickListener: OnClickListener,
        icon: Drawable?,
        text: String
    ) {
        extendedFAB?.apply {
            setOnClickListener(clickListener)
            this.icon = icon
            this.text = text
            if (mediaButtonFab == this) {
                attachSwipeGesture(this)
            }
            iconSize =
                ((mWidgetCircleSize * mWidgetsScale) - (mWidgetIconPadding * 2 * mWidgetsScale)).toInt()
            textSize = 14f * mWidgetsScale
        }

        imageView?.apply {
            setOnClickListener(clickListener)
            setImageDrawable(icon)
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
        val fgColor: Int

        if (!mCustomColors) {
            if (active) {
                bgTint = if (mContext.isNightMode) mDarkColorActive else mLightColorActive
                fgColor = if (mContext.isNightMode) mDarkColor else mLightColor
            } else {
                bgTint = if (mContext.isNightMode) mDarkColor else mLightColor
                fgColor = if (mContext.isNightMode) mLightColor else mDarkColor
            }

            if (iv != null) {
                iv.backgroundTintList = ColorStateList.valueOf(bgTint)
                if (iv != weatherButton) {
                    iv.imageTintList = ColorStateList.valueOf(fgColor)
                } else {
                    iv.imageTintList = null
                }
            }

            if (efab != null) {
                efab.backgroundTintList = ColorStateList.valueOf(bgTint)
                if (efab != weatherButtonFab) {
                    efab.iconTint = ColorStateList.valueOf(fgColor)
                } else {
                    efab.iconTint = null
                }
                efab.setTextColor(fgColor)
            }
        } else {
            if (iv != null) {
                iv.backgroundTintList = ColorStateList.valueOf(
                    if (active) mSmallActiveColor
                    else mSmallInactiveColor
                )
                if (iv !== weatherButton) {
                    iv.imageTintList = ColorStateList.valueOf(
                        if (active) mSmallIconActiveColor
                        else mSmallIconInactiveColor
                    )
                } else {
                    iv.imageTintList = null
                }
            }

            if (efab != null) {
                efab.backgroundTintList = ColorStateList.valueOf(
                    if (active) mBigActiveColor
                    else mBigInactiveColor
                )
                if (efab !== weatherButtonFab) {
                    efab.iconTint = ColorStateList.valueOf(
                        if (active) mBigIconActiveColor
                        else mBigIconInactiveColor
                    )
                } else {
                    efab.iconTint = null
                }
                efab.setTextColor(
                    if (active) mBigIconActiveColor
                    else mBigIconInactiveColor
                )
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
        vibrate(1)
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
        val icon = getDrawable(
            if (isPlaying) MEDIA_PAUSE else MEDIA_PLAY,
            SYSTEMUI_PACKAGE
        ) ?: ResourcesCompat.getDrawable(
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
            log(this@LockscreenWidgetsView, "toggleFlashlight error: " + e.message)
        }
    }

    private fun launchHomeControls(view: View) {
        val controlsTile: Any = ControllersProvider.mDeviceControlsTile ?: return
        val finalView: View = if (view is ExtendedFAB) view.parent as View else view

        post {
            try {
                controlsTile.callMethod("handleClick", finalView)
            } catch (ignored: Throwable) {
                controlsTile.callMethod(
                    "handleClick",
                    finalView.getExpandableView()
                )
            }
        }

        vibrate(1)
    }

    private fun launchWallet(view: View) {
        val mWalletTile: Any? = ControllersProvider.mWalletTile

        if (mWalletTile != null) {
            val finalView: View = if (view is ExtendedFAB) view.parent as View else view

            post {
                try {
                    mWalletTile.callMethod("handleClick", finalView)
                } catch (ignored: Throwable) {
                    mWalletTile.callMethod(
                        "handleClick",
                        finalView.getExpandableView()
                    )
                }
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
        val enabled: Boolean = mWifiManager!!.isWifiEnabled
        mWifiManager.isWifiEnabled = !enabled
        updateWiFiButtonState(!enabled)
        mHandler.postDelayed({ updateWiFiButtonState(isWifiEnabled) }, 350L)
        vibrate(1)
    }

    private fun toggleHotspot() {
        val hotspotTile: Any = ControllersProvider.mHotspotTile ?: return
        val finalView = hotspotButton ?: hotspotButtonFab

        post {
            try {
                hotspotTile.callMethod("handleClick", finalView)
            } catch (ignored: Throwable) {
                hotspotTile.callMethod(
                    "handleClick",
                    finalView.getExpandableView()
                )
            }
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
                log(this@LockscreenWidgetsView, "isMobileDataEnabled error: " + e.message)
                return false
            }
        }

    private val isWifiEnabled: Boolean
        get() = mWifiManager!!.isWifiEnabled

    private fun toggleMobileData() {
        enqueueProxyCommand { proxy ->
            proxy.runCommand("svc data " + if (isMobileDataEnabled) "disable" else "enable")
        }
        updateMobileDataState(!isMobileDataEnabled)
        mHandler.postDelayed({ updateMobileDataState(isMobileDataEnabled) }, 250L)
        vibrate(1)
    }

    private fun showInternetDialog(view: View) {
        val finalView: View = if (view is ExtendedFAB) {
            view.parent as View
        } else {
            view
        }
        if (!ControllersProvider.showInternetDialog(finalView)) {
            mActivityLauncherUtils.launchApp(Intent(Settings.ACTION_WIFI_SETTINGS), false)
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
                AudioManager.RINGER_MODE_NORMAL -> mAudioManager.callMethod(
                    "setRingerModeInternal",
                    AudioManager.RINGER_MODE_VIBRATE
                )

                AudioManager.RINGER_MODE_VIBRATE -> mAudioManager.callMethod(
                    "setRingerModeInternal",
                    AudioManager.RINGER_MODE_SILENT
                )

                AudioManager.RINGER_MODE_SILENT -> mAudioManager.callMethod(
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

        updateTileButtonState(
            torchButton,
            torchButtonFab,
            isFlashOn,
            getDrawable(TORCH_ACTIVE, SYSTEMUI_PACKAGE),
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

        layoutParams = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        orientation = VERTICAL
        gravity = Gravity.CENTER

        mContext = context
        mAudioManager = mContext.getSystemService(AudioManager::class.java)
        mWifiManager = mContext.getSystemService(WifiManager::class.java)
        mTelephonyManager = mContext.getSystemService(TelephonyManager::class.java)
        mConnectivityManager = mContext.getSystemService(ConnectivityManager::class.java)
        mCameraManager = mContext.getSystemService(CameraManager::class.java)

        loadColors()

        mActivityLauncherUtils = ActivityLauncherUtils(mContext, activityStarter)

        mHandler = Handler(Looper.getMainLooper())
        if (mWeatherClient == null) {
            mWeatherClient = OmniJawsClient(context)
        }

        try {
            mCameraId = mCameraManager.cameraIdList[0]
        } catch (e: Throwable) {
            log(this@LockscreenWidgetsView, "mCameraId error: " + e.message)
        }

        val container = LinearLayout(context)
        container.orientation = VERTICAL
        container.layoutParams = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        setupDimens()
        drawUI()

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

        ThemeChangeCallback.getInstance().registerThemeChangedCallback(mThemeChangeCallback)

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

    private fun loadColors() {
        try {
            appContext = mContext.createPackageContext(
                BuildConfig.APPLICATION_ID,
                Context.CONTEXT_IGNORE_SECURITY
            )
        } catch (ignored: java.lang.Exception) {
        }
        mDarkColor = ResourcesCompat.getColor(
            appContext!!.resources,
            R.color.lockscreen_widget_background_color_dark,
            appContext!!.theme
        )
        mLightColor = ResourcesCompat.getColor(
            appContext!!.resources,
            R.color.lockscreen_widget_background_color_light,
            appContext!!.theme
        )
        mDarkColorActive = ResourcesCompat.getColor(
            appContext!!.resources,
            R.color.lockscreen_widget_active_color_dark,
            appContext!!.theme
        )
        mLightColorActive = ResourcesCompat.getColor(
            appContext!!.resources,
            R.color.lockscreen_widget_active_color_light,
            appContext!!.theme
        )
    }

    private fun setupDimens() {
        // Fab Dimens
        mFabWidth = modRes.getDimensionPixelSize(R.dimen.kg_widget_main_width)
        mFabHeight = modRes.getDimensionPixelSize(R.dimen.kg_widget_main_height)
        mFabMarginStart = modRes.getDimensionPixelSize(R.dimen.kg_widgets_main_margin_start)
        mFabMarginEnd = modRes.getDimensionPixelSize(R.dimen.kg_widgets_main_margin_end)
        mFabPadding = modRes.getDimensionPixelSize(R.dimen.kg_main_widgets_icon_padding)

        // Circle Dimens
        mWidgetCircleSize = modRes.getDimensionPixelSize(R.dimen.kg_widget_circle_size)
        mWidgetMarginHorizontal = modRes.getDimensionPixelSize(R.dimen.kg_widgets_margin_horizontal)
        mWidgetMarginVertical = modRes.getDimensionPixelSize(R.dimen.kg_widget_margin_vertical)
        mWidgetIconPadding = modRes.getDimensionPixelSize(R.dimen.kg_widgets_icon_padding)
    }

    private fun drawUI() {
        val container = LinearLayout(mContext)
        container.orientation = VERTICAL
        container.layoutParams = LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        container.gravity = Gravity.CENTER

        // Device Widget Container
        mDeviceWidgetContainer = createDeviceWidgetContainer(mContext)
        container.addView(mDeviceWidgetContainer)

        // Add main widgets container
        mMainWidgetsContainer = createMainWidgetsContainer(mContext)
        container.addView(mMainWidgetsContainer)

        // Add secondary widgets container
        mSecondaryWidgetsContainer = createSecondaryWidgetsContainer(mContext)
        container.addView(mSecondaryWidgetsContainer)

        addView(container)
    }

    @Suppress("deprecation")
    private fun updateWiFiButtonState(enabled: Boolean) {
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
            wifiButton,
            wifiButtonFab,
            isWifiEnabled,
            icon,
            theSsid
        )
    }

    private fun updateRingerButtonState() {
        if (!isWidgetEnabled("ringer")) return
        if (ringerButton == null && ringerButtonFab == null) return

        if (mAudioManager != null) {
            val soundActive = mAudioManager.ringerMode == AudioManager.RINGER_MODE_NORMAL
            updateTileButtonState(
                ringerButton,
                ringerButtonFab,
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
        val bluetoothTile: Any? = ControllersProvider.mBluetoothTile
        val bluetoothController: Any? = ControllersProvider.mBluetoothController

        try {
            bluetoothController.callMethod("setBluetoothEnabled", !isBluetoothEnabled)
        } catch (throwable: Throwable) {
            bluetoothTile.callMethod("toggleBluetooth")
        }

        updateBtState()
        mHandler.postDelayed({ this.updateBtState() }, 350L)
        vibrate(1)
    }

    private fun showBluetoothDialog(view: View) {
        val finalView: View = if (view is ExtendedFAB) {
            view.parent as View
        } else {
            view
        }
        if (!ControllersProvider.showBluetoothDialog(mContext, finalView)) {
            mActivityLauncherUtils.launchBluetoothSettings()
        }
        vibrate(0)
    }

    private fun updateBtState() {
        if (!isWidgetEnabled("bt")) return
        if (btButton == null && btButtonFab == null) return

        val bluetoothController: Any? = ControllersProvider.mBluetoothController
        var deviceName: String? = ""
        if (isBluetoothEnabled && bluetoothController != null)
            deviceName = bluetoothController.callMethod("getConnectedDeviceName") as String?
        val isConnected = !deviceName.isNullOrEmpty()
        val icon = getDrawable(
            BT_ICON,
            SYSTEMUI_PACKAGE
        )
        updateTileButtonState(
            btButton,
            btButtonFab,
            isBluetoothOn,
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
            log(this@LockscreenWidgetsView, "isHotspotEnabled error: " + t.message)
        }
        return false
    }

    @Suppress("deprecation")
    private fun getHotspotSSID(): String {
        try {
            val methods: Array<Method> = WifiManager::class.java.declaredMethods.toList().union(WifiManager::class.java.methods.toList()).toTypedArray()
            for (m in methods) {
                if (m.name == "getWifiApConfiguration") {
                    val config = m.invoke(mWifiManager) as android.net.wifi.WifiConfiguration
                    return config.SSID
                }
            }
        } catch (t: Throwable) {
            log(this@LockscreenWidgetsView, "getHotspotSSID error: " + t.message)
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
        instance?.apply {
            lockscreenWidgetsEnabled = lsWidgets
            deviceWidgetsEnabled = deviceWidget
            mMainLockscreenWidgetsList = mainWidgets
            mMainWidgetsList = listOf(
                *mMainLockscreenWidgetsList!!
                    .split(",".toRegex())
                    .dropLastWhile { it.isEmpty() }
                    .toTypedArray()
            )
            mSecondaryLockscreenWidgetsList = secondaryWidgets
            mSecondaryWidgetsList = listOf(
                *mSecondaryLockscreenWidgetsList!!
                    .split(",".toRegex())
                    .dropLastWhile { it.isEmpty() }
                    .toTypedArray()
            )
            updateWidgetViews()
        }
    }

    fun setIsLargeClock(isLargeClock: Boolean) {
        instance?.apply {
            mIsLargeClock = isLargeClock
            updateContainerVisibility()
        }
    }

    /**
     * Set the options for the Device Widget
     * @param deviceWidgetStyle style for device widget
     * @param customColor true if custom color is enabled
     * @param linearColor color for linear battery progressbar
     * @param circularColor color for circular progressbar
     * @param textColor color for text
     * @param devName device name, keep blank for default Build.MODEL
     */
    fun setDeviceWidgetOptions(
        deviceWidgetStyle: Int,
        customColor: Boolean,
        linearColor: Int,
        circularColor: Int,
        textColor: Int,
        devName: String?
    ) {
        instance?.mDeviceWidgetView?.apply {
            setDeviceWidgetStyle(deviceWidgetStyle)
            setCustomColor(customColor, linearColor, circularColor)
            setTextCustomColor(textColor)
            setDeviceName(devName)
        }
    }

    fun setCustomColors(
        customColorsEnabled: Boolean,
        bigInactive: Int, bigActive: Int, smallInactive: Int, smallActive: Int,
        bigIconInactive: Int, bigIconActive: Int, smallIconInactive: Int, smallIconActive: Int
    ) {
        instance?.apply {
            mCustomColors = customColorsEnabled
            mBigInactiveColor = bigInactive
            mBigActiveColor = bigActive
            mSmallInactiveColor = smallInactive
            mSmallActiveColor = smallActive
            mBigIconInactiveColor = bigIconInactive
            mBigIconActiveColor = bigIconActive
            mSmallIconInactiveColor = smallIconInactive
            mSmallIconActiveColor = smallIconActive
            updateWidgetViews()
        }
    }

    fun setRoundness(roundness: Int) {
        instance?.apply {
            mWidgetsRoundness = roundness
            removeAllViews()
            drawUI()
            updateWidgetViews()
        }
    }

    fun setScale(scale: Float) {
        instance?.apply {
            mWidgetsScale = scale
            removeAllViews()
            drawUI()
            updateWidgetViews()
            mDeviceWidgetView?.setScaling(scale)
        }
    }

    fun setActivityStarter(activityStarter: Any?) {
        mActivityLauncherUtils = ActivityLauncherUtils(mContext, activityStarter)
    }

    fun setDozingState(isDozing: Boolean) {
        instance?.apply {
            mDozing = isDozing
            updateContainerVisibility()
        }
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
            return when (drawableRes) {
                CALCULATOR_ICON -> ResourcesCompat.getDrawable(
                    modRes,
                    R.drawable.ic_calculator,
                    mContext.theme
                )

                HOTSPOT_ACTIVE -> getDrawable(HOTSPOT_A12, SYSTEMUI_PACKAGE)
                HOTSPOT_INACTIVE -> getDrawable(HOTSPOT_A12, SYSTEMUI_PACKAGE)
                BT_ICON -> getDrawable(BT_A12, FRAMEWORK_PACKAGE)
                TORCH_ACTIVE -> getDrawable(TORCH_A12, FRAMEWORK_PACKAGE)
                TORCH_INACTIVE -> getDrawable(TORCH_A12, FRAMEWORK_PACKAGE)

                else -> {
                    log(this@LockscreenWidgetsView, "getDrawable $drawableRes from $pkg error $t")
                    return null
                }
            }
        }
    }

    @Suppress("DiscouragedApi", "SameParameterValue")
    private fun getString(stringRes: String, pkg: String): String {
        try {
            return mContext.resources.getString(
                mContext.resources.getIdentifier(stringRes, "string", pkg)
            )
        } catch (t: Throwable) {
            // We have our own strings too, so return them if SystemUI doesn't
            return when (stringRes) {
                HOME_CONTROLS_LABEL -> modRes.getString(R.string.home_controls)
                CALCULATOR_LABEL -> modRes.getString(R.string.calculator)
                CAMERA_LABEL -> modRes.getString(R.string.camera)
                WALLET_LABEL -> modRes.getString(R.string.wallet)
                MEDIA_PLAY_LABEL -> "Play"
                else -> {
                    log(this@LockscreenWidgetsView, "getString $stringRes from $pkg error $t")
                    ""
                }
            }
        }
    }

    private val ringerDrawable: Drawable?
        get() {
            return getDrawable(
                when (mAudioManager!!.ringerMode) {
                    AudioManager.RINGER_MODE_NORMAL -> RING_VOLUME
                    AudioManager.RINGER_MODE_VIBRATE -> RING_VOLUME_VIBRATE
                    AudioManager.RINGER_MODE_SILENT -> RING_VOLUME_MUTE
                    else -> throw IllegalStateException("Unexpected value: " + mAudioManager.ringerMode)
                }, SYSTEMUI_PACKAGE
            ) ?: ResourcesCompat.getDrawable(
                modRes,
                when (mAudioManager.ringerMode) {
                    AudioManager.RINGER_MODE_NORMAL -> R.drawable.ic_ringer_normal
                    AudioManager.RINGER_MODE_VIBRATE -> R.drawable.ic_ringer_vibrate
                    AudioManager.RINGER_MODE_SILENT -> R.drawable.ic_ringer_mute
                    else -> throw IllegalStateException("Unexpected value: " + mAudioManager.ringerMode)
                },
                mContext.theme
            )
        }

    private val ringerText: String
        get() {
            val resName = when (mAudioManager!!.ringerMode) {
                AudioManager.RINGER_MODE_NORMAL -> R.string.ringer_normal
                AudioManager.RINGER_MODE_VIBRATE -> R.string.ringer_vibrate
                AudioManager.RINGER_MODE_SILENT -> R.string.ringer_silent
                else -> throw IllegalStateException("Unexpected value: " + mAudioManager.ringerMode)
            }

            return modRes.getString(resName)
        }

    private fun createWidgetBackgroundDrawable(): RippleDrawable {
        val rippleDrawable = ResourcesCompat.getDrawable(
            modRes,
            R.drawable.lockscreen_widget_background_circle,
            mContext.theme
        ) as RippleDrawable
        val cornerRadiusPx = mWidgetsRoundness.toFloat()
        val radii = FloatArray(8) { cornerRadiusPx }

        val backgroundDrawable = rippleDrawable.findDrawableByLayerId(android.R.id.background)
        if (backgroundDrawable is GradientDrawable) {
            backgroundDrawable.cornerRadius = cornerRadiusPx
        } else if (backgroundDrawable is ShapeDrawable) {
            backgroundDrawable.shape = RoundRectShape(radii, null, null)
        }

        val maskDrawable = rippleDrawable.findDrawableByLayerId(android.R.id.mask)
        if (maskDrawable is GradientDrawable) {
            maskDrawable.cornerRadius = cornerRadiusPx
        } else if (maskDrawable is ShapeDrawable) {
            maskDrawable.shape = RoundRectShape(radii, null, null)
        }

        return rippleDrawable
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
        const val TORCH_ACTIVE: String = "qs_flashlight_icon_on"
        const val TORCH_INACTIVE: String = "qs_flashlight_icon_off"
        const val WIFI_ACTIVE: String = "ic_wifi_signal_4"
        const val WIFI_INACTIVE: String = "ic_wifi_signal_0"
        const val HOME_CONTROLS: String = "controls_icon"
        const val CALCULATOR_ICON: String = "status_bar_qs_calculator_inactive"
        const val CAMERA_ICON1: String = "ic_camera_alt_24dp"
        const val CAMERA_ICON2: String = "ic_camera"
        const val ALARM_ICON: String = "ic_alarm"
        const val WALLET_ICON: String = "ic_wallet_lockscreen"
        const val HOTSPOT_ACTIVE: String = "qs_hotspot_icon_on"
        const val HOTSPOT_INACTIVE: String = "qs_hotspot_icon_off"
        const val RING_VOLUME: String = "ic_volume_ringer"
        const val RING_VOLUME_MUTE: String = "ic_volume_ringer_mute"
        const val RING_VOLUME_VIBRATE: String = "ic_volume_ringer_vibrate"
        const val MEDIA_PLAY: String = "ic_media_play"
        const val MEDIA_PAUSE: String = "ic_media_pause"

        // A12 icons
        const val HOTSPOT_A12: String = "ic_hotspot"
        const val BT_A12: String = "ic_qs_bluetooth"
        const val TORCH_A12: String = "ic_qs_flashlight"

        const val BT_LABEL: String = "quick_settings_bluetooth_label"
        const val DATA_LABEL: String = "quick_settings_internet_label"
        const val WIFI_LABEL: String = "quick_settings_wifi_label"
        const val TORCH_LABEL: String = "quick_settings_flashlight_label"
        const val HOME_CONTROLS_LABEL: String = "quick_controls_title"
        const val MEDIA_PLAY_LABEL: String = "controls_media_button_play"
        const val CALCULATOR_LABEL: String = "keyboard_shortcut_group_applications_calculator"
        const val CAMERA_LABEL: String = "accessibility_camera_button"
        const val WALLET_LABEL: String = "wallet_title"
        const val HOTSPOT_LABEL: String = "quick_settings_hotspot_label"

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
