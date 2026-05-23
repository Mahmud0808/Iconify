package com.drdisagree.iconify.data.config

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import com.drdisagree.iconify.app.Iconify.Companion.appContext
import com.drdisagree.iconify.data.common.XposedConst.PREF_FILE_NAME
import com.drdisagree.iconify.data.keys.Key

@Suppress("unused")
object RPrefs : SharedPreferences {

    val prefs: SharedPreferences by lazy {
        appContext
            .createDeviceProtectedStorageContext()
            .getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE)
    }

    private val editor: SharedPreferences.Editor by lazy { prefs.edit() }

    val getPrefs: SharedPreferences
        get() = prefs

    // Basic put methods
    fun putBoolean(key: String?, value: Boolean) {
        editor.putBoolean(key, value).apply()
    }

    fun putBoolean(key: Key, value: Boolean) = putBoolean(key.name, value)

    fun putInt(key: String?, value: Int) {
        editor.putInt(key, value).apply()
    }

    fun putInt(key: Key, value: Int) = putInt(key.name, value)

    fun putFloat(key: String?, value: Float) {
        editor.putFloat(key, value).apply()
    }

    fun putFloat(key: Key, value: Float) = putFloat(key.name, value)

    fun putLong(key: String?, value: Long) {
        editor.putLong(key, value).apply()
    }

    fun putLong(key: Key, value: Long) = putLong(key.name, value)

    fun putString(key: String?, value: String?) {
        editor.putString(key, value).apply()
    }

    fun putString(key: Key, value: String?) = putString(key.name, value)

    // Basic get methods
    fun getBoolean(key: String?): Boolean {
        return prefs.getBoolean(key, false)
    }

    fun getBoolean(key: Key): Boolean = getBoolean(key.name, key.default as? Boolean ?: false)

    fun getBoolean(key: Key, defValue: Boolean): Boolean =
        getBoolean(key.name, defValue)

    fun getInt(key: String?): Int {
        return prefs.getInt(key, 0)
    }

    fun getInt(key: Key): Int = getInt(key.name, key.default as? Int ?: 0)

    fun getInt(key: Key, defValue: Int): Int = getInt(key.name, defValue)

    fun getLong(key: String?): Long {
        return prefs.getLong(key, 0)
    }

    fun getLong(key: Key): Long = getLong(key.name, key.default as? Long ?: 0)

    fun getLong(key: Key, defValue: Long): Long = getLong(key.name, defValue)

    fun getFloat(key: String?): Float {
        return prefs.getFloat(key, 0f)
    }

    fun getFloat(key: Key): Float = getFloat(key.name, key.default as? Float ?: 0f)

    fun getFloat(key: Key, defValue: Float): Float = getFloat(key.name, defValue)

    fun getString(key: String?): String? {
        return prefs.getString(key, null)
    }

    fun getString(key: Key): String? = getString(key.name, key.default as? String)

    fun getString(key: Key, defValue: String?): String? = getString(key.name, defValue)

    // Clear methods
    fun clearPref(key: String?) {
        editor.remove(key).apply()
    }

    fun clearPref(key: Key) = clearPref(key.name)

    fun clearPrefs(vararg keys: String?) {
        keys.forEach { key -> clearPref(key) }
    }

    fun clearPrefs(vararg keys: Key) {
        keys.forEach { key -> clearPref(key.name) }
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
