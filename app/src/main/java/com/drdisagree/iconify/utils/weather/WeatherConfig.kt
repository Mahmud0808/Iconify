package com.drdisagree.iconify.utils.weather

import android.Manifest
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.util.Log
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.data.common.Preferences.LOCKSCREEN_WIDGETS
import com.drdisagree.iconify.data.common.Preferences.LOCKSCREEN_WIDGETS_EXTRAS
import com.drdisagree.iconify.data.common.Preferences.WEATHER_CUSTOM_LOCATION
import com.drdisagree.iconify.data.common.Preferences.WEATHER_ICON_PACK
import com.drdisagree.iconify.data.common.Preferences.WEATHER_OWM_KEY
import com.drdisagree.iconify.data.common.Preferences.WEATHER_PROVIDER
import com.drdisagree.iconify.data.common.Preferences.WEATHER_SWITCH
import com.drdisagree.iconify.data.common.Preferences.WEATHER_UNITS
import com.drdisagree.iconify.data.common.Preferences.WEATHER_UPDATE_INTERVAL
import com.drdisagree.iconify.data.common.Preferences.WEATHER_YANDEX_KEY
import com.drdisagree.iconify.data.common.Resources.SHARED_XPREFERENCES
import com.drdisagree.iconify.utils.weather.providers.METNorwayProvider
import com.drdisagree.iconify.utils.weather.providers.OpenMeteoProvider
import com.drdisagree.iconify.utils.weather.providers.OpenWeatherMapProvider
import com.drdisagree.iconify.utils.weather.providers.YandexProvider
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import com.drdisagree.iconify.xposed.utils.XPrefs.XprefsIsInitialized

object WeatherConfig {

    private const val PREF_KEY_LOCATION_LAT: String = "location_lat"
    private const val PREF_KEY_LOCATION_LON: String = "location_lon"
    private const val PREF_KEY_LOCATION_NAME: String = "location_name"
    private const val PREF_KEY_WEATHER_DATA: String = "weather_data"
    private const val PREF_KEY_LAST_UPDATE: String = "last_update"
    private const val PREF_KEY_UPDATE_ERROR: String = "update_error"
    private const val WEATHER_PREFS: String = BuildConfig.APPLICATION_ID + "_weatherprefs"

    private fun Context.getPrefs(): SharedPreferences {
        if (XprefsIsInitialized) return Xprefs
        return createDeviceProtectedStorageContext()
            .getSharedPreferences(SHARED_XPREFERENCES, MODE_PRIVATE)
    }

    private fun Context.getWeatherPrefs(): SharedPreferences {
        val deviceProtectedContext = createDeviceProtectedStorageContext()
        return deviceProtectedContext.getSharedPreferences(WEATHER_PREFS, MODE_PRIVATE)
    }

    fun clear(context: Context) {
        context.getWeatherPrefs().edit().clear().apply()
        val prefs = listOf(
            WEATHER_PROVIDER,
            WEATHER_UNITS,
            WEATHER_UPDATE_INTERVAL,
            WEATHER_OWM_KEY,
            PREF_KEY_UPDATE_ERROR
        )
        prefs.forEach {
            context.getPrefs().edit().remove(it).apply()
        }
    }

    fun getProvider(context: Context): AbstractWeatherProvider {
        val provider = context.getPrefs().getString(WEATHER_PROVIDER, "0")
        return when (provider) {
            "1" -> OpenWeatherMapProvider(context)
            "2" -> YandexProvider(context)
            "3" -> METNorwayProvider(context)
            else -> OpenMeteoProvider(context)
        }
    }

    fun getProviderId(context: Context): String {
        val provider = context.getPrefs().getString(WEATHER_PROVIDER, "0")
        return when (provider) {
            "1" -> "OpenWeatherMap"
            "2" -> "Yandex"
            "3" -> "MET Norway"
            else -> "OpenMeteo"
        }
    }

    fun isMetric(context: Context): Boolean {
        return context.getPrefs().getString(WEATHER_UNITS, "0") == "0"
    }

    fun isCustomLocation(context: Context): Boolean {
        return context.getPrefs().getBoolean(WEATHER_CUSTOM_LOCATION, false)
    }

    fun getLocationLat(context: Context): String? {
        return context.getWeatherPrefs().getString(PREF_KEY_LOCATION_LAT, null)
    }

    fun getLocationLon(context: Context): String? {
        return context.getWeatherPrefs().getString(PREF_KEY_LOCATION_LON, null)
    }

    fun setLocationId(context: Context, lat: String?, lon: String?) {
        context.getWeatherPrefs().edit().putString(PREF_KEY_LOCATION_LAT, lat).apply()
        context.getWeatherPrefs().edit().putString(PREF_KEY_LOCATION_LON, lon).apply()
    }

    fun getLocationName(context: Context): String? {
        return context.getWeatherPrefs().getString(PREF_KEY_LOCATION_NAME, null)
    }

    fun setLocationName(context: Context, name: String?) {
        context.getWeatherPrefs().edit().putString(PREF_KEY_LOCATION_NAME, name).apply()
    }

    fun getWeatherData(context: Context): WeatherInfo? {
        var str: String? = null

        try {
            str = context.getWeatherPrefs().getString(PREF_KEY_WEATHER_DATA, null)
        } catch (t: Throwable) {
            Log.e("WeatherConfig", t.toString())
        }

        if (str != null) {
            return WeatherInfo.fromSerializedString(context, str)
        }

        return null
    }

    fun setWeatherData(data: WeatherInfo, context: Context) {
        context.getWeatherPrefs().edit().putString(
            PREF_KEY_WEATHER_DATA,
            data.toSerializedString()
        ).apply()
        context.getWeatherPrefs().edit().putLong(
            PREF_KEY_LAST_UPDATE,
            System.currentTimeMillis()
        ).apply()
    }

    fun clearLastUpdateTime(context: Context) {
        context.getWeatherPrefs().edit().putLong(PREF_KEY_LAST_UPDATE, 0).apply()
    }

    fun isEnabled(context: Context): Boolean {
        val lsWeather = context.getPrefs().getBoolean(WEATHER_SWITCH, false)
        val bigWidgets = context.getPrefs().getString(LOCKSCREEN_WIDGETS, "")
        val miniWidgets = context.getPrefs().getString(LOCKSCREEN_WIDGETS_EXTRAS, "")
        return lsWeather || bigWidgets!!.contains("weather") || miniWidgets!!.contains("weather")
    }

    fun setEnabled(context: Context, value: Boolean, key: String?) {
        context.getPrefs().edit().putBoolean(key, value).apply()
    }

    fun getUpdateInterval(context: Context): Int {
        var updateValue = 2
        try {
            updateValue = context.getPrefs().getString(WEATHER_UPDATE_INTERVAL, "2")!!.toInt()
        } catch (_: Throwable) {
        }

        return updateValue
    }

    fun getIconPack(context: Context): String? {
        return context.getPrefs().getString(WEATHER_ICON_PACK, null)
    }

    fun setUpdateError(context: Context, value: Boolean) {
        context.getWeatherPrefs().edit().putBoolean(PREF_KEY_UPDATE_ERROR, value).apply()
    }

    fun isSetupDone(context: Context): Boolean {
        return (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
    }

    fun getOwmKey(context: Context): String {
        return context.getPrefs().getString(WEATHER_OWM_KEY, "") ?: ""
    }

    fun getYandexKey(context: Context): String {
        return context.getPrefs().getString(WEATHER_YANDEX_KEY, "") ?: ""
    }

}