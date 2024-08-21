package com.drdisagree.iconify.config

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import com.drdisagree.iconify.ui.preferences.SliderPreference

class ExtendedPrefs private constructor(private val prefs: SharedPreferences) : SharedPreferences {

    fun getSliderInt(key: String?, defaultVal: Int): Int {
        return SliderPreference.getSingleIntValue(this, key, defaultVal)
    }

    fun getSliderValues(key: String?, defaultValue: Float): List<Float> {
        return SliderPreference.getValues(this, key, defaultValue)
    }

    fun getSliderFloat(key: String?, defaultVal: Float): Float {
        return SliderPreference.getSingleFloatValue(this, key, defaultVal)
    }

    override fun getAll(): Map<String, *> {
        return prefs.all
    }

    override fun getString(key: String, defValue: String?): String? {
        return prefs.getString(key, defValue)
    }

    override fun getStringSet(key: String, defValues: Set<String>?): Set<String>? {
        return prefs.getStringSet(key, defValues)
    }

    override fun getInt(key: String, defValue: Int): Int {
        return prefs.getInt(key, defValue)
    }

    override fun getLong(key: String, defValue: Long): Long {
        return prefs.getLong(key, defValue)
    }

    override fun getFloat(key: String, defValue: Float): Float {
        return prefs.getFloat(key, defValue)
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        return prefs.getBoolean(key, defValue)
    }

    override fun contains(key: String): Boolean {
        return prefs.contains(key)
    }

    override fun edit(): SharedPreferences.Editor {
        return prefs.edit()
    }

    fun putInt(key: String?, value: Int) {
        edit().putInt(key, value).apply()
    }

    override fun registerOnSharedPreferenceChangeListener(listener: OnSharedPreferenceChangeListener) {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun unregisterOnSharedPreferenceChangeListener(listener: OnSharedPreferenceChangeListener) {
        prefs.unregisterOnSharedPreferenceChangeListener(listener)
    }

    fun putString(setting: String?, value: String?) {
        edit().putString(setting, value).apply()
    }

    fun putBoolean(key: String?, enabled: Boolean) {
        edit().putBoolean(key, enabled).apply()
    }

    companion object {
        @JvmStatic
        fun from(prefs: SharedPreferences): ExtendedPrefs {
            return ExtendedPrefs(prefs)
        }
    }
}
