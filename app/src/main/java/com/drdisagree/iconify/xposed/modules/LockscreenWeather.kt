package com.drdisagree.iconify.xposed.modules

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.drdisagree.iconify.common.Preferences.ICONIFY_LOCKSCREEN_CLOCK_TAG
import com.drdisagree.iconify.common.Preferences.LSCLOCK_SWITCH
import com.drdisagree.iconify.common.Preferences.WEATHER_CUSTOM_MARGINS_BOTTOM
import com.drdisagree.iconify.common.Preferences.WEATHER_CUSTOM_MARGINS_LEFT
import com.drdisagree.iconify.common.Preferences.WEATHER_CUSTOM_MARGINS_TOP
import com.drdisagree.iconify.common.Preferences.WEATHER_ICON_SIZE
import com.drdisagree.iconify.common.Preferences.WEATHER_SHOW_CONDITION
import com.drdisagree.iconify.common.Preferences.WEATHER_SHOW_HUMIDITY
import com.drdisagree.iconify.common.Preferences.WEATHER_SHOW_LOCATION
import com.drdisagree.iconify.common.Preferences.WEATHER_SHOW_WIND
import com.drdisagree.iconify.common.Preferences.WEATHER_STYLE
import com.drdisagree.iconify.common.Preferences.WEATHER_SWITCH
import com.drdisagree.iconify.common.Preferences.WEATHER_TEXT_COLOR
import com.drdisagree.iconify.common.Preferences.WEATHER_TEXT_COLOR_SWITCH
import com.drdisagree.iconify.common.Preferences.WEATHER_TEXT_SIZE
import com.drdisagree.iconify.config.XPrefs.Xprefs
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.utils.ViewHelper.setMargins
import com.drdisagree.iconify.xposed.views.CurrentWeatherView
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge.hookAllMethods
import de.robv.android.xposed.XposedHelpers.findClass
import de.robv.android.xposed.XposedHelpers.getObjectField
import de.robv.android.xposed.callbacks.XC_LoadPackage

class LockscreenWeather(context: Context?) : ModPack(context!!) {

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
    private var mLeftMargin: Int = 0
    private var mTopMargin: Int = 0
    private var mBottomMargin: Int = 0
    private var mWeatherBackground = 0
    private lateinit var mWeatherContainer: LinearLayout

    private var mStatusViewContainer: ViewGroup? = null
    private var mStatusArea: ViewGroup? = null


    override fun updatePrefs(vararg key: String) {
        if (Xprefs == null) return

        customLockscreenClock = Xprefs!!.getBoolean(LSCLOCK_SWITCH, false)
        weatherEnabled = Xprefs!!.getBoolean(WEATHER_SWITCH, false)
        weatherShowLocation = Xprefs!!.getBoolean(WEATHER_SHOW_LOCATION, true)
        weatherShowCondition = Xprefs!!.getBoolean(WEATHER_SHOW_CONDITION, true)
        weatherShowHumidity = Xprefs!!.getBoolean(WEATHER_SHOW_HUMIDITY, false)
        weatherShowWind = Xprefs!!.getBoolean(WEATHER_SHOW_WIND, false)
        weatherCustomColor = Xprefs!!.getBoolean(WEATHER_TEXT_COLOR_SWITCH, false)
        weatherColor = Xprefs!!.getInt(WEATHER_TEXT_COLOR, Color.WHITE)
        weatherTextSize = Xprefs!!.getInt(WEATHER_TEXT_SIZE, 16)
        weatherImageSize = Xprefs!!.getInt(WEATHER_ICON_SIZE, 18)
        mLeftMargin = Xprefs!!.getInt(WEATHER_CUSTOM_MARGINS_LEFT, 20)
        mTopMargin = Xprefs!!.getInt(WEATHER_CUSTOM_MARGINS_TOP, 20)
        mBottomMargin = Xprefs!!.getInt(WEATHER_CUSTOM_MARGINS_BOTTOM, 20)
        mWeatherBackground = Xprefs!!.getInt(WEATHER_STYLE, 0)

        if (key.isNotEmpty() &&
            (key[0] == (WEATHER_SHOW_LOCATION) ||
                    key[0] == (WEATHER_SHOW_CONDITION) ||
                    key[0] == (WEATHER_SHOW_HUMIDITY) ||
                    key[0] == (WEATHER_SHOW_WIND) ||
                    key[0] == (WEATHER_TEXT_COLOR_SWITCH) ||
                    key[0] == (WEATHER_TEXT_COLOR) ||
                    key[0] == (WEATHER_TEXT_SIZE) ||
                    key[0] == (WEATHER_ICON_SIZE) ||
                    key[0] == (WEATHER_STYLE) ||
                    key[0] == (WEATHER_CUSTOM_MARGINS_BOTTOM) ||
                    key[0] == (WEATHER_CUSTOM_MARGINS_LEFT) ||
                    key[0] == (WEATHER_CUSTOM_MARGINS_TOP))
        ) {
            updateWeatherView()
        }

    }

    override fun handleLoadPackage(loadPackageParam: XC_LoadPackage.LoadPackageParam) {

        mWeatherContainer = LinearLayout(mContext)
        mWeatherContainer.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val keyguardStatusViewClass = findClass(
            "com.android.keyguard.KeyguardStatusView",
            loadPackageParam.classLoader
        )

        hookAllMethods(keyguardStatusViewClass, "onFinishInflate", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (!weatherEnabled) return

                mStatusViewContainer = getObjectField(
                    param.thisObject,
                    "mStatusViewContainer"
                ) as ViewGroup

                placeWeatherView()

            }
        })

        val keyguardClockSwitch = findClass(
            "com.android.keyguard.KeyguardClockSwitch",
            loadPackageParam.classLoader
        )

        hookAllMethods(keyguardClockSwitch, "onFinishInflate", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (!weatherEnabled) return


                mStatusArea = getObjectField(
                    param.thisObject,
                    "mStatusArea"
                ) as ViewGroup

                placeWeatherView()

            }
        })
    }

    private fun placeWeatherView() {
        try {
            val currentWeatherView: CurrentWeatherView =
                CurrentWeatherView.getInstance(mContext, LOCKSCREEN_WEATHER)
            try {
                (currentWeatherView.parent as ViewGroup).removeView(currentWeatherView)
            } catch (ignored: Throwable) {
            }
            try {
                (mWeatherContainer.parent as ViewGroup).removeView(mWeatherContainer)
            } catch (ignored: Throwable) {
            }
            mWeatherContainer.addView(currentWeatherView)
           if (customLockscreenClock) {
               mStatusViewContainer!!.addView(mWeatherContainer)
           } else {
               // Put weather view inside the status area
               // But before notifications
               mStatusArea!!.addView(mWeatherContainer, mStatusArea!!.childCount - 1)
           }
            refreshWeatherView(currentWeatherView)
            updateMargins()
        } catch (ignored: Throwable) {
        }
    }

    private fun updateMargins() {
        setMargins(
            mWeatherContainer,
            mContext,
            mLeftMargin,
            mTopMargin,
            mLeftMargin,
            mBottomMargin
        )
    }

    private fun refreshWeatherView(currentWeatherView: CurrentWeatherView?) {
        if (currentWeatherView == null) return
        currentWeatherView.updateSizes(
            weatherTextSize,
            weatherImageSize,
            LOCKSCREEN_WEATHER
        )
        currentWeatherView.updateColors(
            if (weatherCustomColor) weatherColor else Color.WHITE,
            LOCKSCREEN_WEATHER
        )
        currentWeatherView.updateWeatherSettings(
            weatherShowLocation,
            weatherShowCondition,
            weatherShowHumidity,
            weatherShowWind,
            LOCKSCREEN_WEATHER
        )
        currentWeatherView.visibility = if (weatherEnabled) View.VISIBLE else View.GONE
        currentWeatherView.updateWeatherBg(
            mWeatherBackground,
            LOCKSCREEN_WEATHER
        )
        updateMargins()
    }

    private fun updateWeatherView() {
        refreshWeatherView(CurrentWeatherView.getInstance(LOCKSCREEN_WEATHER))
    }

    companion object {
        const val LOCKSCREEN_WEATHER = "iconify_ls_weather"
    }

}