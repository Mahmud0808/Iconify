package com.drdisagree.iconify.config

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.drdisagree.iconify.Iconify
import com.drdisagree.iconify.common.Preferences.STR_NULL
import com.drdisagree.iconify.common.Resources.SHARED_XPREFERENCES

@Suppress("unused")
object RPrefs {

    @JvmField
    var prefs: SharedPreferences =
        Iconify.getAppContext().createDeviceProtectedStorageContext().getSharedPreferences(
            SHARED_XPREFERENCES, MODE_PRIVATE
        )
    private var editor: SharedPreferences.Editor = prefs.edit()

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
    fun putFloat(key: String?, `val`: Float) {
        editor.putFloat(key, `val`).apply()
    }

    @JvmStatic
    fun putString(key: String?, `val`: String?) {
        editor.putString(key, `val`).apply()
    }

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

    @JvmStatic
    fun getLong(key: String?): Long {
        return prefs.getLong(key, 0)
    }

    @JvmStatic
    fun getLong(key: String?, defValue: Long): Long {
        return prefs.getLong(key, defValue)
    }

    @JvmStatic
    fun getFloat(key: String?): Float {
        return prefs.getFloat(key, 0f)
    }

    @JvmStatic
    fun getFloat(key: String?, defValue: Float): Float {
        return prefs.getFloat(key, defValue)
    }

    @JvmStatic
    fun getString(key: String?): String? {
        return prefs.getString(key, STR_NULL)
    }

    @JvmStatic
    fun getString(key: String?, defValue: String?): String? {
        return prefs.getString(key, defValue)
    }

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

    @JvmStatic
    fun clearAllPrefs() {
        editor.clear().apply()
    }
}
