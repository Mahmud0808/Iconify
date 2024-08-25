package com.drdisagree.iconify.config

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.common.Resources.SHARED_XPREFERENCES
import com.drdisagree.iconify.ui.preferences.SliderPreference

@Suppress("unused")
object RPrefs : SharedPreferences {

    private val prefs: SharedPreferences by lazy {
        appContext.createDeviceProtectedStorageContext().getSharedPreferences(
            SHARED_XPREFERENCES, MODE_PRIVATE
        )
    }

    private val editor: SharedPreferences.Editor by lazy { prefs.edit() }

    val instance: RPrefs
        get() = this

    val getPrefs: SharedPreferences
        get() = prefs

    // Basic put methods
    fun putBoolean(key: String?, value: Boolean) {
        editor.putBoolean(key, value).apply()
    }

    fun putInt(key: String?, value: Int) {
        editor.putInt(key, value).apply()
    }

    fun putFloat(key: String?, value: Float) {
        editor.putFloat(key, value).apply()
    }

    fun putLong(key: String?, value: Long) {
        editor.putLong(key, value).apply()
    }

    fun putString(key: String?, value: String?) {
        editor.putString(key, value).apply()
    }

    // Basic get methods
    fun getBoolean(key: String?): Boolean {
        return prefs.getBoolean(key, false)
    }

    fun getInt(key: String?): Int {
        return prefs.getInt(key, 0)
    }

    fun getLong(key: String?): Long {
        return prefs.getLong(key, 0)
    }

    fun getFloat(key: String?): Float {
        return prefs.getFloat(key, 0f)
    }

    fun getString(key: String?): String? {
        return prefs.getString(key, null)
    }

    // Custom slider preference methods
    fun getSliderInt(key: String?, defaultVal: Int): Int {
        return SliderPreference.getSingleIntValue(this, key, defaultVal)
    }

    fun getSliderValues(key: String?, defaultValue: Float): List<Float> {
        return SliderPreference.getValues(this, key, defaultValue)
    }

    fun getSliderFloat(key: String?, defaultVal: Float): Float {
        return SliderPreference.getSingleFloatValue(this, key, defaultVal)
    }

    // Clear methods
    fun clearPref(key: String?) {
        editor.remove(key).apply()
    }

    fun clearPrefs(vararg keys: String?) {
        keys.forEach { key ->
            editor.remove(key).apply()
        }
    }

    fun clearAllPrefs() {
        editor.clear().apply()
    }

    // Implementing SharedPreferences interface
    override fun getAll(): Map<String, *> {
        return prefs.all
    }

    override fun getBoolean(key: String?, defValue: Boolean): Boolean {
        return prefs.getBoolean(key, defValue)
    }

    override fun getInt(key: String?, defValue: Int): Int {
        return prefs.getInt(key, defValue)
    }

    override fun getLong(key: String?, defValue: Long): Long {
        return prefs.getLong(key, defValue)
    }

    override fun getFloat(key: String?, defValue: Float): Float {
        return prefs.getFloat(key, defValue)
    }

    override fun getString(key: String?, defValue: String?): String? {
        return prefs.getString(key, defValue)
    }

    override fun getStringSet(key: String, defValues: Set<String>?): Set<String>? {
        return prefs.getStringSet(key, defValues)
    }

    override fun contains(key: String): Boolean {
        return prefs.contains(key)
    }

    override fun edit(): SharedPreferences.Editor {
        return editor
    }

    override fun registerOnSharedPreferenceChangeListener(listener: OnSharedPreferenceChangeListener) {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun unregisterOnSharedPreferenceChangeListener(listener: OnSharedPreferenceChangeListener) {
        prefs.unregisterOnSharedPreferenceChangeListener(listener)
    }
}
