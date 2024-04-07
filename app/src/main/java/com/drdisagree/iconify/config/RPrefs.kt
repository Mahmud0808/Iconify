package com.drdisagree.iconify.config

import android.content.Context
import android.content.SharedPreferences
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.common.Preferences
import com.drdisagree.iconify.common.Resources

@Suppress("unused")
object RPrefs {

    @JvmField
    var prefs: SharedPreferences = appContext.createDeviceProtectedStorageContext()
        .getSharedPreferences(Resources.SharedXPref, Context.MODE_PRIVATE)
    private var editor: SharedPreferences.Editor = prefs.edit()

    // Save sharedPref config
    @JvmStatic
    fun putBoolean(key: String?, `val`: Boolean) {
        editor.putBoolean(key, `val`).apply()
    }

    @JvmStatic
    fun putInt(key: String?, `val`: Int) {
        editor.putInt(key, `val`).apply()
    }

    @JvmStatic
    fun putLong(key: String?, `val`: Long) {
        editor.putLong(key, `val`).apply()
    }

    fun putFloat(key: String?, `val`: Float) {
        editor.putFloat(key, `val`).apply()
    }

    fun putString(key: String?, `val`: String?) {
        editor.putString(key, `val`).apply()
    }

    // Load sharedPref config
    fun getBoolean(key: String?): Boolean {
        return prefs.getBoolean(key, false)
    }

    @JvmStatic
    fun getBoolean(key: String?, defValue: Boolean?): Boolean {
        return prefs.getBoolean(key, defValue!!)
    }

    fun getInt(key: String?): Int {
        return prefs.getInt(key, 0)
    }

    @JvmStatic
    fun getInt(key: String?, defValue: Int): Int {
        return prefs.getInt(key, defValue)
    }

    fun getLong(key: String?): Long {
        return prefs.getLong(key, 0)
    }

    fun getLong(key: String?, defValue: Long): Long {
        return prefs.getLong(key, defValue)
    }

    fun getFloat(key: String?): Float {
        return prefs.getFloat(key, 0f)
    }

    fun getFloat(key: String?, defValue: Float): Float {
        return prefs.getFloat(key, defValue)
    }

    fun getString(key: String?): String? {
        return prefs.getString(key, Preferences.STR_NULL)
    }

    fun getString(key: String?, defValue: String?): String? {
        return prefs.getString(key, defValue)
    }

    // Clear specific sharedPref config
    @JvmStatic
    fun clearPref(key: String?) {
        editor.remove(key).apply()
    }

    fun clearPrefs(vararg keys: String?) {
        for (key in keys) {
            editor.remove(key).apply()
        }
    }

    // Clear all sharedPref config
    @JvmStatic
    fun clearAllPrefs() {
        editor.clear().apply()
    }
}
