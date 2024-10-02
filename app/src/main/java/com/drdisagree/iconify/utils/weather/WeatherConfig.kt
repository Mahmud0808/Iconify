package com.drdisagree.iconify.utils.weather

import android.Manifest
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.content.pm.PackageManager
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.common.Preferences.LOCKSCREEN_WIDGETS
import com.drdisagree.iconify.common.Preferences.LOCKSCREEN_WIDGETS_EXTRAS
import com.drdisagree.iconify.common.Preferences.WEATHER_CUSTOM_LOCATION
import com.drdisagree.iconify.common.Preferences.WEATHER_ICON_PACK
import com.drdisagree.iconify.common.Preferences.WEATHER_OWM_KEY
import com.drdisagree.iconify.common.Preferences.WEATHER_PROVIDER
import com.drdisagree.iconify.common.Preferences.WEATHER_SWITCH
import com.drdisagree.iconify.common.Preferences.WEATHER_UNITS
import com.drdisagree.iconify.common.Preferences.WEATHER_UPDATE_INTERVAL
import com.drdisagree.iconify.common.Preferences.WEATHER_YANDEX_KEY
import com.drdisagree.iconify.common.Resources.SHARED_XPREFERENCES
import com.drdisagree.iconify.utils.weather.providers.METNorwayProvider
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import com.drdisagree.iconify.utils.weather.providers.OpenMeteoProvider
import com.drdisagree.iconify.utils.weather.providers.OpenWeatherMapProvider
import com.drdisagree.iconify.utils.weather.providers.YandexProvider

object WeatherConfig {

    private const val PREF_KEY_LOCATION_LAT: String = "location_lat"
    private const val PREF_KEY_LOCATION_LON: String = "location_lon"
    private const val PREF_KEY_LOCATION_NAME: String = "location_name"
    private const val PREF_KEY_WEATHER_DATA: String = "weather_data"
    private const val PREF_KEY_LAST_UPDATE: String = "last_update"
    private const val PREF_KEY_UPDATE_ERROR: String = "update_error"
    private const val WEATHER_PREFS: String = BuildConfig.APPLICATION_ID + "_weatherprefs"

    private fun getPrefs(context: Context): SharedPreferences {
        try {
            if (Xprefs != null) return Xprefs as SharedPreferences
            return context.createDeviceProtectedStorageContext().getSharedPreferences(
                SHARED_XPREFERENCES, MODE_PRIVATE
            )
        } catch (t: Throwable) {
            return context.createDeviceProtectedStorageContext().getSharedPreferences(
                SHARED_XPREFERENCES, MODE_PRIVATE
            )
        }
    }

    private fun getWeatherPrefs(context: Context): SharedPreferences {
        val deviceProtectedContext = context.createDeviceProtectedStorageContext()
        return deviceProtectedContext.getSharedPreferences(WEATHER_PREFS, MODE_PRIVATE)
    }

    fun clear(context: Context) {
        getWeatherPrefs(context).edit().clear().apply()
        val prefs = listOf(
            WEATHER_PROVIDER,
            WEATHER_UNITS,
            WEATHER_UPDATE_INTERVAL,
            WEATHER_OWM_KEY,
            PREF_KEY_UPDATE_ERROR
        )
        prefs.forEach {
            getPrefs(context).edit().remove(it).apply()
        }
    }

    fun getProvider(context: Context): AbstractWeatherProvider {
        val provider = getPrefs(context).getString(WEATHER_PROVIDER, "0")
        return when (provider) {
            "1" -> return OpenWeatherMapProvider(context)
            "2" -> return YandexProvider(context)
            "3" -> return METNorwayProvider(context)
            else -> return OpenMeteoProvider(context)
        }
    }

    fun getProviderId(context: Context): String {
        val provider = getPrefs(context).getString(WEATHER_PROVIDER, "0")
        return when (provider) {
            "1" -> "OpenWeatherMap"
            "2" -> "Yandex"
            "3" -> "MET Norway"
            else -> "OpenMeteo"
        }
    }

    fun isMetric(context: Context): Boolean {
        return getPrefs(context).getString(WEATHER_UNITS, "0") == "0"
    }

    fun isCustomLocation(context: Context): Boolean {
        return getPrefs(context).getBoolean(WEATHER_CUSTOM_LOCATION, false)
    }

    fun getLocationLat(context: Context): String? {
        return getWeatherPrefs(context).getString(PREF_KEY_LOCATION_LAT, null)
    }

    fun getLocationLon(context: Context): String? {
        return getWeatherPrefs(context).getString(PREF_KEY_LOCATION_LON, null)
    }

    fun setLocationId(context: Context, lat: String?, lon: String?) {
        getWeatherPrefs(context).edit().putString(PREF_KEY_LOCATION_LAT, lat).apply()
        getWeatherPrefs(context).edit().putString(PREF_KEY_LOCATION_LON, lon).apply()
    }

    fun getLocationName(context: Context): String? {
        return getWeatherPrefs(context).getString(PREF_KEY_LOCATION_NAME, null)
    }

    fun setLocationName(context: Context, name: String?) {
        getWeatherPrefs(context).edit().putString(PREF_KEY_LOCATION_NAME, name).apply()
    }

    fun getWeatherData(context: Context): WeatherInfo? {
        var str: String? = null
        try {
            str = getWeatherPrefs(context).getString(PREF_KEY_WEATHER_DATA, null)
        } catch (ignored: Throwable) {
        }

        if (str != null) {
            return WeatherInfo.fromSerializedString(context, str)
        }
        return null
    }

    fun setWeatherData(data: WeatherInfo, context: Context) {
        getWeatherPrefs(context).edit().putString(PREF_KEY_WEATHER_DATA, data.toSerializedString())
            .apply()
        getWeatherPrefs(context).edit().putLong(PREF_KEY_LAST_UPDATE, System.currentTimeMillis())
            .apply()
    }

    fun clearLastUpdateTime(context: Context) {
        getWeatherPrefs(context).edit().putLong(PREF_KEY_LAST_UPDATE, 0).apply()
    }

    fun isEnabled(context: Context): Boolean {
        val lsWeather = getPrefs(context).getBoolean(WEATHER_SWITCH, false)
        val bigWidgets = getPrefs(context).getString(LOCKSCREEN_WIDGETS, "")
        val miniWidgets = getPrefs(context).getString(LOCKSCREEN_WIDGETS_EXTRAS, "")
        return lsWeather || bigWidgets!!.contains("weather") || miniWidgets!!.contains("weather")
    }

    fun setEnabled(context: Context, value: Boolean, key: String?) {
        getPrefs(context).edit().putBoolean(key, value).apply()
    }

    fun getUpdateInterval(context: Context): Int {
        var updateValue = 2
        try {
            updateValue = getPrefs(context).getString(WEATHER_UPDATE_INTERVAL, "2")!!.toInt()
        } catch (ignored: Throwable) {
        }

        return updateValue
    }

    fun getIconPack(context: Context): String? {
        return getPrefs(context).getString(WEATHER_ICON_PACK, null)
    }

    fun setUpdateError(context: Context, value: Boolean) {
        getWeatherPrefs(context).edit().putBoolean(PREF_KEY_UPDATE_ERROR, value).apply()
    }

    fun isSetupDone(context: Context): Boolean {
        return (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
    }

    fun getOwmKey(context: Context): String {
        return getPrefs(context).getString(WEATHER_OWM_KEY, "") ?: ""
    }

    fun getYandexKey(context: Context): String {
        return getPrefs(context).getString(WEATHER_YANDEX_KEY, "") ?: ""
    }

}