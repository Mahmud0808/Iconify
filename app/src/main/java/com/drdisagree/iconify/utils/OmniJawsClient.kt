package com.drdisagree.iconify.utils

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import android.text.format.DateFormat
import android.util.Log
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.utils.weather.WeatherConfig
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/*
 * Copyright (C) 2021 The OmniROM Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

class OmniJawsClient(private val mContext: Context) {

    class WeatherInfo(private val mContext: Context) {
        var city: String? = null
        var windSpeed: String? = null
        var windDirection: String? = null
        var conditionCode: Int = 0
        var temp: String? = null
        var humidity: String? = null
        var condition: String? = null
        var timeStamp: Long = 0
        var dailyForecasts: List<DayForecast>? = null
        var hourlyForecasts: List<HourForecast>? = null
        var tempUnits: String? = null
        var windUnits: String? = null
        var provider: String? = null
        var pinWheel: String? = null
        var iconPack: String? = null

        override fun toString(): String {
            return city + ":" + Date(timeStamp) + ": " + windSpeed + ":" + windDirection + ":" + conditionCode + ":" + temp + ":" + humidity + ":" + condition + ":" + tempUnits + ":" + windUnits + ": " + hourlyForecasts + ": " + dailyForecasts + ": " + iconPack
        }

        val lastUpdateTime: String
            get() {
                val hourFormat = if (DateFormat.is24HourFormat(mContext)) "HH" else "hh"
                val sdf = SimpleDateFormat("$hourFormat:mm:ss", Locale.getDefault())
                return sdf.format(Date(timeStamp))
            }
    }

    class DayForecast {
        var low: String? = null
        var high: String? = null
        var conditionCode: Int = 0
        var condition: String? = null
        var date: String? = null

        override fun toString(): String {
            return "[$low:$high:$conditionCode:$condition:$date]"
        }
    }

    class HourForecast {
        var condition: String? = null
        var time: String? = null
        var conditionCode: Int = 0
        var temperature: String? = null

        override fun toString(): String {
            return "[$temperature:$conditionCode:$condition:$time]"
        }
    }

    fun getSettingsIntent(): Intent {
        val launchIntent = Intent()
        launchIntent.setComponent(
            ComponentName(
                BuildConfig.APPLICATION_ID,
                BuildConfig.APPLICATION_ID.replace(".debug", "") + ".ui.activities.MainActivity"
            )
        )
        launchIntent.putExtra("openWeatherSettings", true)
        return launchIntent
    }

    interface OmniJawsObserver {
        fun weatherUpdated()
        fun weatherError(errorReason: Int)
        fun updateSettings() {}
    }

    private inner class WeatherUpdateReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            for (observer in mObserver) {
                if (action == WEATHER_UPDATE) {
                    observer.weatherUpdated()
                }
                if (action == WEATHER_ERROR) {
                    val errorReason = intent.getIntExtra(EXTRA_ERROR, 0)
                    observer.weatherError(errorReason)
                }
            }
        }
    }

    var weatherInfo: WeatherInfo? = null
    private var mRes: Resources? = null
    private var mPackageName: String? = null
    private var mIconPrefix: String? = null
    private var mSettingIconPackage: String? = null
    private var mMetric = false
    private val mObserver: MutableList<OmniJawsObserver> = ArrayList()
    private var mReceiver: WeatherUpdateReceiver? = null

    fun queryWeather() {
        try {
            weatherInfo = null

            var cursor = mContext.contentResolver.query(
                WEATHER_URI, WEATHER_PROJECTION,
                null, null, null
            )

            cursor?.use {
                val count = it.count

                if (count > 0) {
                    weatherInfo = WeatherInfo(mContext)
                    val forecastList: MutableList<DayForecast> = ArrayList()
                    val hourlyForecastList: MutableList<HourForecast> = ArrayList()
                    var i = 0

                    while (i < count) {
                        it.moveToPosition(i)

                        val isDailyForecast =
                            it.getString(12) != null && !TextUtils.isEmpty(it.getString(12))
                        val isHourlyForecast =
                            it.getString(14) != null && !TextUtils.isEmpty(it.getString(14))

                        if (i == 0) {
                            weatherInfo?.apply {
                                city = it.getString(0)
                                windSpeed = getFormattedValue(it.getFloat(1))
                                weatherInfo!!.windDirection = it.getInt(2).toString() + "\u00b0"
                                weatherInfo!!.conditionCode = it.getInt(3)
                                weatherInfo!!.temp = getFormattedValue(it.getFloat(4))
                                weatherInfo!!.humidity = it.getString(5)
                                weatherInfo!!.condition = it.getString(6)
                                weatherInfo!!.timeStamp = it.getString(11).toLong()
                                weatherInfo!!.pinWheel = it.getString(13)
                            }
                        } else if (isHourlyForecast) {
                            val hour = HourForecast()
                            hour.temperature = getFormattedValue(it.getFloat(4))
                            hour.time = it.getString(14)
                            hour.temperature = getFormattedValue(it.getFloat(15))
                            hour.condition = it.getString(16)
                            hour.conditionCode = it.getInt(17)
                            hourlyForecastList.add(hour)
                        } else if (isDailyForecast){
                            val day = DayForecast()
                            day.low = getFormattedValue(it.getFloat(7))
                            day.high = getFormattedValue(it.getFloat(8))
                            day.condition = it.getString(9)
                            day.conditionCode = it.getInt(10)
                            day.date = it.getString(12)
                            forecastList.add(day)
                        }
                        i++
                    }
                    weatherInfo!!.dailyForecasts = forecastList
                    weatherInfo!!.hourlyForecasts = hourlyForecastList
                }
            }

            cursor = mContext.contentResolver.query(
                SETTINGS_URI, SETTINGS_PROJECTION,
                null, null, null
            )

            cursor?.use {
                val count = it.count

                if (count == 1) {
                    it.moveToPosition(0)
                    mMetric = it.getInt(1) == 0

                    weatherInfo?.apply {
                        tempUnits = temperatureUnit
                        windUnits = windUnit
                        provider = it.getString(2)
                        iconPack = it.getString(4)
                    }
                }
            }

            updateSettings()
        } catch (e: Exception) {
            Log.e(TAG, "queryWeather", e)
        }
    }

    private fun loadDefaultIconsPackage() {
        mPackageName = ICON_PACKAGE_DEFAULT
        mIconPrefix = ICON_PREFIX_DEFAULT
        mSettingIconPackage = "$mPackageName.$mIconPrefix"

        if (DEBUG) Log.d(
            TAG,
            "Load default icon pack $mSettingIconPackage $mPackageName $mIconPrefix"
        )

        try {
            val packageManager = mContext.packageManager
            mRes = packageManager.getResourcesForApplication(mPackageName!!)
        } catch (e: Exception) {
            Log.d(TAG, "loadDefaultIconsPackage", e)
            mRes = null
        }

        if (mRes == null) {
            Log.w(TAG, "No default package found")
        }
    }

    @Suppress("deprecation")
    private val defaultConditionImage: Drawable
        @SuppressLint("DiscouragedApi", "UseCompatLoadingForDrawables")
        get() {
            val packageName = ICON_PACKAGE_DEFAULT
            val iconPrefix = ICON_PREFIX_DEFAULT

            try {
                val packageManager = mContext.packageManager
                val res = packageManager.getResourcesForApplication(packageName)
                val resId = res.getIdentifier(iconPrefix + "_na", "drawable", packageName)
                val d = mRes!!.getDrawable(resId)

                if (d != null) {
                    return d
                }
            } catch (e: Exception) {
                Log.e(TAG, "defaultConditionImage", e)
            }

            // absolute absolute fallback
            Log.w(TAG, "No default package found")

            return ColorDrawable(Color.RED)
        }

    private fun loadCustomIconPackage() {
        if (DEBUG) Log.d(
            TAG,
            "Load custom icon pack $mSettingIconPackage"
        )

        val idx = mSettingIconPackage!!.lastIndexOf(".")
        mPackageName = mSettingIconPackage!!.substring(0, idx)
        mIconPrefix = mSettingIconPackage!!.substring(idx + 1)

        if (DEBUG) Log.d(
            TAG,
            "Load custom icon pack $mPackageName $mIconPrefix"
        )

        try {
            val packageManager = mContext.packageManager
            mRes = packageManager.getResourcesForApplication(mPackageName!!)
        } catch (e: Exception) {
            Log.w(TAG, "Icon pack loading failed - loading default")
            loadDefaultIconsPackage()
        }
    }

    @Suppress("deprecation")
    @SuppressLint("DiscouragedApi", "UseCompatLoadingForDrawables")
    fun getWeatherConditionImage(conditionCode: Int): Drawable {
        try {
            var resId = mRes!!.getIdentifier(
                mIconPrefix + "_" + conditionCode,
                "drawable",
                mPackageName
            )
            var d = mRes!!.getDrawable(resId)
            if (d != null) {
                return d
            }

            Log.w(
                TAG,
                "Failed to get condition image for $conditionCode use default"
            )

            resId = mRes!!.getIdentifier(mIconPrefix + "_na", "drawable", mPackageName)
            d = mRes!!.getDrawable(resId)

            if (d != null) {
                return d
            }
        } catch (e: Exception) {
            Log.e(TAG, "getWeatherConditionImage", e)
        }
        Log.w(
            TAG,
            "Failed to get condition image for $conditionCode"
        )
        return defaultConditionImage
    }

    val isOmniJawsEnabled: Boolean
        get() = WeatherConfig.isEnabled(mContext)

    private val temperatureUnit: String
        get() = "\u00b0" + (if (mMetric) "C" else "F")

    private val windUnit: String
        get() = if (mMetric) "km/h" else "mph"

    private fun updateSettings() {
        val iconPack = if (weatherInfo != null) weatherInfo!!.iconPack else null
        if (TextUtils.isEmpty(iconPack)) {
            Log.d(TAG, "updateSettings No icon pack set, using default")
            loadDefaultIconsPackage()
        } else if (iconPack != mSettingIconPackage) {
            Log.d(
                TAG,
                "updateSettings New icon pack set, loading $iconPack"
            )
            mSettingIconPackage = iconPack
            loadCustomIconPackage()
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    fun addObserver(observer: OmniJawsObserver) {
        if (mObserver.isEmpty()) {
            if (mReceiver != null) {
                try {
                    mContext.unregisterReceiver(mReceiver)
                } catch (ignored: Exception) {
                }
            }
            mReceiver = WeatherUpdateReceiver()
            val filter = IntentFilter()
            filter.addAction(WEATHER_UPDATE)
            filter.addAction(WEATHER_ERROR)
            if (DEBUG) Log.d(TAG, "registerReceiver")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                mContext.registerReceiver(mReceiver, filter, Context.RECEIVER_EXPORTED)
            } else {
                mContext.registerReceiver(mReceiver, filter)
            }
        }
        mObserver.add(observer)
    }

    fun removeObserver(observer: OmniJawsObserver) {
        mObserver.remove(observer)
        if (mObserver.isEmpty() && mReceiver != null) {
            try {
                if (DEBUG) Log.d(TAG, "unregisterReceiver")
                mContext.unregisterReceiver(mReceiver)
            } catch (ignored: Exception) {
            }
            mReceiver = null
        }
    }

    companion object {
        private const val TAG = "OmniJawsClient"
        private val DEBUG = BuildConfig.DEBUG
        private const val SERVICE_PACKAGE: String = BuildConfig.APPLICATION_ID
        val WEATHER_URI
                : Uri = Uri.parse("content://com.drdisagree.iconify.weatherprovider/weather")
        val SETTINGS_URI
                : Uri = Uri.parse("content://com.drdisagree.iconify.weatherprovider/settings")
        val CONTROL_URI
                : Uri = Uri.parse("content://com.drdisagree.iconify.weatherprovider/control")

        private const val ICON_PACKAGE_DEFAULT = BuildConfig.APPLICATION_ID
        private const val ICON_PREFIX_DEFAULT = "google"
        private const val ICON_PREFIX_OUTLINE = "outline"
        private const val EXTRA_ERROR = "error"
        const val EXTRA_ERROR_NETWORK: Int = 0 // No Network
        const val EXTRA_ERROR_LOCATION: Int = 1 // No Location Found
        const val EXTRA_ERROR_DISABLED: Int = 2 // Disabled
        const val EXTRA_ERROR_NO_PERMISSIONS: Int = 3 // No Permissions

        val WEATHER_PROJECTION: Array<String> = arrayOf(
            "city",
            "wind_speed",
            "wind_direction",
            "condition_code",
            "temperature",
            "humidity",
            "condition",
            "forecast_low",
            "forecast_high",
            "forecast_condition",
            "forecast_condition_code",
            "time_stamp",
            "forecast_date",
            "pin_wheel",
            "forecast_hour",
            "forecast_hour_temp",
            "forecast_hour_condition",
            "forecast_hour_condition_code"
        )

        val SETTINGS_PROJECTION: Array<String> = arrayOf(
            "enabled",
            "units",
            "provider",
            "setup",
            "icon_pack"
        )

        private const val WEATHER_UPDATE = "$SERVICE_PACKAGE.WEATHER_UPDATE"
        private const val WEATHER_ERROR = "$SERVICE_PACKAGE.WEATHER_ERROR"

        private val sNoDigitsFormat = DecimalFormat("0")

        private fun getFormattedValue(value: Float): String {
            if (java.lang.Float.isNaN(value)) {
                return "-"
            }
            var formatted = sNoDigitsFormat.format(value.toDouble())
            if (formatted == "-0") {
                formatted = "0"
            }
            return formatted
        }
    }
}