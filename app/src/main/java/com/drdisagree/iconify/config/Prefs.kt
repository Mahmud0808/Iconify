package com.drdisagree.iconify.config

import android.content.Context
import android.content.SharedPreferences
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.common.Preferences.STR_NULL
import com.drdisagree.iconify.common.Resources.SHARED_PREFERENCES

@Suppress("unused")
object Prefs {

    var prefs: SharedPreferences = appContext.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE)
    private var editor: SharedPreferences.Editor = prefs.edit()

    fun putBoolean(key: String?, `val`: Boolean) {
        editor.putBoolean(key, `val`).apply()
    }

    fun putInt(key: String?, `val`: Int) {
        editor.putInt(key, `val`).apply()
    }

    fun putLong(key: String?, `val`: Long) {
        editor.putLong(key, `val`).apply()
    }

    fun putString(key: String?, `val`: String?) {
        editor.putString(key, `val`).apply()
    }

    fun getBoolean(key: String?): Boolean {
        return prefs.getBoolean(key, false)
    }

    fun getBoolean(key: String?, defValue: Boolean?): Boolean {
        return prefs.getBoolean(key, defValue!!)
    }

    fun getInt(key: String?): Int {
        return prefs.getInt(key, 0)
    }

    fun getInt(key: String?, defValue: Int): Int {
        return prefs.getInt(key, defValue)
    }

    fun getLong(key: String?): Long {
        return prefs.getLong(key, 0)
    }

    fun getLong(key: String?, defValue: Long): Long {
        return prefs.getLong(key, defValue)
    }

    fun getString(key: String?): String? {
        return prefs.getString(key, STR_NULL)
    }

    fun getString(key: String?, defValue: String?): String? {
        return prefs.getString(key, defValue)
    }

    fun clearPref(key: String?) {
        editor.remove(key).apply()
    }

    fun clearPrefs(vararg keys: String?) {
        for (key in keys) {
            editor.remove(key).apply()
        }
    }

    fun clearAllPrefs() {
        editor.clear().apply()
    }
}
