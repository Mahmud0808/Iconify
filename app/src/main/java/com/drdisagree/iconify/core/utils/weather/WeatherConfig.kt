package com.drdisagree.iconify.core.utils.weather

import android.Manifest
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.edit
import com.drdisagree.iconify.core.utils.weather.providers.METNorwayProvider
import com.drdisagree.iconify.core.utils.weather.providers.OpenMeteoProvider
import com.drdisagree.iconify.core.utils.weather.providers.OpenWeatherMapProvider
import com.drdisagree.iconify.core.utils.weather.providers.YandexProvider
import com.drdisagree.iconify.data.common.XposedConst.WEATHER_PREF_FILE_NAME
import com.drdisagree.iconify.data.config.RPrefs
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import com.drdisagree.iconify.xposed.utils.XPrefs.XprefsIsInitialized

object WeatherConfig {

    private const val PREF_KEY_LOCATION_LAT: String = "location_lat"
    private const val PREF_KEY_LOCATION_LON: String = "location_lon"
    private const val PREF_KEY_LOCATION_NAME: String = "location_name"
    private const val PREF_KEY_WEATHER_DATA: String = "weather_data"
    private const val PREF_KEY_LAST_UPDATE: String = "last_update"
    private const val PREF_KEY_UPDATE_ERROR: String = "update_error"

    private fun getPrefs(): SharedPreferences {
        if (XprefsIsInitialized) return Xprefs

        return RPrefs.getPrefs
    }

    private fun Context.getWeatherPrefs(): SharedPreferences {
        return createDeviceProtectedStorageContext()
            .getSharedPreferences(WEATHER_PREF_FILE_NAME, MODE_PRIVATE)
    }

    fun clear(context: Context) {
        context.getWeatherPrefs().edit { clear() }
        val prefs = listOf(
            XposedKey.WEATHER_PROVIDER.name,
            XposedKey.WEATHER_UNITS.name,
            XposedKey.WEATHER_UPDATE_INTERVAL.name,
            XposedKey.WEATHER_OWM_KEY.name,
            PREF_KEY_UPDATE_ERROR
        )
        prefs.forEach {
            getPrefs().edit { remove(it) }
        }
    }

    fun getProvider(context: Context): AbstractWeatherProvider {
        val provider = getPrefs().getString(XposedKey.WEATHER_PROVIDER.name, "0")
        return when (provider) {
            "1" -> OpenWeatherMapProvider(context)
            "2" -> YandexProvider(context)
            "3" -> METNorwayProvider(context)
            else -> OpenMeteoProvider(context)
        }
    }

    fun getProviderId(): String {
        val provider = getPrefs().getString(XposedKey.WEATHER_PROVIDER.name, "0")
        return when (provider) {
            "1" -> "OpenWeatherMap"
            "2" -> "Yandex"
            "3" -> "MET Norway"
            else -> "OpenMeteo"
        }
    }

    fun isMetric(): Boolean {
        return getPrefs().getString(XposedKey.WEATHER_UNITS.name, "0") == "0"
    }

    fun isCustomLocation(): Boolean {
        return getPrefs().getBoolean(XposedKey.WEATHER_CUSTOM_LOCATION.name, false)
    }

    fun getLocationLat(context: Context): String? {
        return context.getWeatherPrefs().getString(PREF_KEY_LOCATION_LAT, null)
    }

    fun getLocationLon(context: Context): String? {
        return context.getWeatherPrefs().getString(PREF_KEY_LOCATION_LON, null)
    }

    fun setLocationId(context: Context, lat: String?, lon: String?) {
        context.getWeatherPrefs().edit { putString(PREF_KEY_LOCATION_LAT, lat) }
        context.getWeatherPrefs().edit { putString(PREF_KEY_LOCATION_LON, lon) }
    }

    fun getLocationName(context: Context): String? {
        return context.getWeatherPrefs().getString(PREF_KEY_LOCATION_NAME, null)
    }

    fun setLocationName(context: Context, name: String?) {
        context.getWeatherPrefs().edit { putString(PREF_KEY_LOCATION_NAME, name) }
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
        context.getWeatherPrefs().edit {
            putString(
                PREF_KEY_WEATHER_DATA,
                data.toSerializedString()
            )
        }
        context.getWeatherPrefs().edit {
            putLong(
                PREF_KEY_LAST_UPDATE,
                System.currentTimeMillis()
            )
        }
    }

    fun clearLastUpdateTime(context: Context) {
        context.getWeatherPrefs().edit { putLong(PREF_KEY_LAST_UPDATE, 0) }
    }

    fun isEnabled(): Boolean {
        val lsWeather = getPrefs().getBoolean(XposedKey.LOCKSCREEN_WEATHER.name, false)
        val lsWidgets = getPrefs().getBoolean(XposedKey.LOCKSCREEN_WIDGETS.name, false)

        val bigWidgets = getPrefs().getString(XposedKey.LOCKSCREEN_WIDGETS_MAIN.name, "")!!
        val miniWidgets =
            getPrefs().getString(XposedKey.LOCKSCREEN_WIDGETS_EXTRAS.name, "")!!

        return lsWeather || (lsWidgets && (listOf(
            bigWidgets,
            miniWidgets
        ).any { it.contains("weather") }))
    }

    fun setEnabled(value: Boolean, key: String?) {
        getPrefs().edit { putBoolean(key, value) }
    }

    fun getUpdateInterval(): Int {
        var updateValue = 2
        try {
            updateValue =
                getPrefs().getString(XposedKey.WEATHER_UPDATE_INTERVAL.name, "2")!!.toInt()
        } catch (_: Throwable) {
        }

        return updateValue
    }

    fun getIconPack(): String? {
        return getPrefs().getString(XposedKey.WEATHER_ICON_PACK.name, null)
    }

    fun setUpdateError(context: Context, value: Boolean) {
        context.getWeatherPrefs().edit { putBoolean(PREF_KEY_UPDATE_ERROR, value) }
    }

    fun isSetupDone(context: Context): Boolean {
        return (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
    }

    fun getOwmKey(): String {
        return getPrefs().getString(XposedKey.WEATHER_OWM_KEY.name, "") ?: ""
    }

    fun getYandexKey(): String {
        return getPrefs().getString(XposedKey.WEATHER_YANDEX_KEY.name, "") ?: ""
    }

}