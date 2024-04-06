package com.drdisagree.iconify.config

import android.content.Context
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.common.Preferences
import com.drdisagree.iconify.common.Resources

@Suppress("unused")
object Prefs {
    @JvmField
    var prefs = appContext!!.getSharedPreferences(Resources.SharedPref, Context.MODE_PRIVATE)
    var editor = prefs.edit()

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

    @JvmStatic
    fun putString(key: String?, `val`: String?) {
        editor.putString(key, `val`).apply()
    }

    // Load sharedPref config
    @JvmStatic
    fun getBoolean(key: String?): Boolean {
        return prefs.getBoolean(key, false)
    }

    @JvmStatic
    fun getBoolean(key: String?, defValue: Boolean?): Boolean {
        return prefs.getBoolean(key, defValue!!)
    }

    @JvmStatic
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

    @JvmStatic
    fun getLong(key: String?, defValue: Long): Long {
        return prefs.getLong(key, defValue)
    }

    @JvmStatic
    fun getString(key: String?): String? {
        return prefs.getString(key, Preferences.STR_NULL)
    }

    @JvmStatic
    fun getString(key: String?, defValue: String?): String? {
        return prefs.getString(key, defValue)
    }

    // Clear specific sharedPref config
    @JvmStatic
    fun clearPref(key: String?) {
        editor.remove(key).apply()
    }

    @JvmStatic
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

