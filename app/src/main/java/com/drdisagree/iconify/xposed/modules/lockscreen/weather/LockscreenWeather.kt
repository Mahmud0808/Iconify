package com.drdisagree.iconify.xposed.modules.lockscreen.weather

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.children
import com.drdisagree.iconify.data.common.Const.ACTION_WEATHER_INFLATED
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
import com.drdisagree.iconify.data.common.XposedConst.LOCKSCREEN_WEATHER_FONT_FILE
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.callbacks.BootCallback
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.applyFontRecursively
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.setMargins
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getFieldSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.modules.extras.views.CurrentWeatherView
import com.drdisagree.iconify.xposed.modules.lockscreen.Lockscreen.Companion.isComposeLockscreen
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import com.drdisagree.iconify.xposed.utils.XPrefs.XprefsIsInitialized
import de.robv.android.xposed.callbacks.XC_LoadPackage

class LockscreenWeather(context: Context) : ModPack(context) {

    private var customLockscreenClock = false
    private var weatherEnabled = false
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
    private lateinit var mWeatherContainer: LinearLayout
    private var mStatusViewContainer: ViewGroup? = null
    private var mStatusArea: ViewGroup? = null
    private var mCustomFontEnabled = false

    override fun updatePrefs(vararg key: String) {
        if (!XprefsIsInitialized || isComposeLockscreen) return

        Xprefs.apply {
            customLockscreenClock = getBoolean(LSCLOCK_SWITCH, false)
            weatherEnabled = getBoolean(WEATHER_SWITCH, false)
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
            ) -> updateWeatherView()
        }
    }

    @SuppressLint("DiscouragedApi")
    override fun handleLoadPackage(loadPackageParam: XC_LoadPackage.LoadPackageParam) {
        if (isComposeLockscreen) return

        mWeatherContainer = LinearLayout(mContext)
        mWeatherContainer.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val keyguardStatusViewClass = findClass("com.android.keyguard.KeyguardStatusView")

        keyguardStatusViewClass
            .hookMethod("onFinishInflate")
            .runAfter { param ->
                if (!weatherEnabled) return@runAfter

                mStatusViewContainer =
                    param.thisObject.getFieldSilently("mStatusViewContainer") as? ViewGroup
                        ?: (param.thisObject as ViewGroup).findViewById(
                            mContext.resources.getIdentifier(
                                "status_view_container",
                                "id",
                                mContext.packageName
                            )
                        )

                placeWeatherView()
            }

        val keyguardClockSwitch = findClass("com.android.keyguard.KeyguardClockSwitch")

        keyguardClockSwitch
            .hookMethod("onFinishInflate")
            .runAfter { param ->
                if (!weatherEnabled) return@runAfter

                mStatusArea = param.thisObject.getField("mStatusArea") as ViewGroup

                placeWeatherView()
            }

        BootCallback.registerBootListener(
            object : BootCallback.BootListener {
                override fun onDeviceBooted() {
                    updateWeatherView()
                }
            }
        )
    }

    private fun placeWeatherView() {
        if (isComposeLockscreen) return

        try {
            val currentWeatherView: CurrentWeatherView = CurrentWeatherView.getInstance(
                mContext,
                LOCKSCREEN_WEATHER
            )

            (currentWeatherView.parent as ViewGroup?)?.removeView(currentWeatherView)
            (mWeatherContainer.parent as ViewGroup?)?.removeView(mWeatherContainer)

            mWeatherContainer.addView(currentWeatherView)

            if (customLockscreenClock) {
                mStatusViewContainer!!.addView(mWeatherContainer)
            } else {
                // Put weather view inside the status area
                // But before notifications
                mStatusArea!!.addView(mWeatherContainer, mStatusArea!!.childCount - 1)
            }

            refreshWeatherView(currentWeatherView)

            // Weather placed, now inflate widgets
            val broadcast = Intent(ACTION_WEATHER_INFLATED)
            broadcast.setFlags(Intent.FLAG_RECEIVER_FOREGROUND)
            Thread { mContext.sendBroadcast(broadcast) }.start()
        } catch (_: Throwable) {
        }
    }

    private fun refreshWeatherView(currentWeatherView: CurrentWeatherView?) {
        if (currentWeatherView == null) return

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
            visibility = if (weatherEnabled) View.VISIBLE else View.GONE
            updateWeatherBg(
                mWeatherBackground,
                LOCKSCREEN_WEATHER
            )
        }

        updateMargins()
        updateFont()
    }

    private fun updateMargins() {
        setMargins(
            mWeatherContainer,
            mContext,
            mSideMargin,
            mTopMargin,
            mSideMargin,
            mBottomMargin
        )

        mWeatherContainer.gravity = if (mCenterWeather) {
            Gravity.CENTER_HORIZONTAL
        } else {
            Gravity.START
        }
        (mWeatherContainer.getChildAt(0) as LinearLayout?)?.children?.forEach {
            (it as LinearLayout).gravity = if (mCenterWeather) {
                Gravity.CENTER_HORIZONTAL
            } else {
                Gravity.START or Gravity.CENTER_VERTICAL
            }
        }
    }

    private fun updateFont() {
        if (mCustomFontEnabled && LOCKSCREEN_WEATHER_FONT_FILE.exists()) {
            Typeface.createFromFile(LOCKSCREEN_WEATHER_FONT_FILE)
        } else {
            Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }.also { typeface ->
            applyFontRecursively(mWeatherContainer, typeface)
        }
    }

    private fun updateWeatherView() {
        if (!isComposeLockscreen) {
            refreshWeatherView(CurrentWeatherView.getInstance(LOCKSCREEN_WEATHER))
        }
    }

    companion object {
        const val LOCKSCREEN_WEATHER = "iconify_ls_weather"
    }
}
