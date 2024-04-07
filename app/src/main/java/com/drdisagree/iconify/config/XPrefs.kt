package com.drdisagree.iconify.config

import android.content.Context
import android.content.SharedPreferences
import com.crossbowffs.remotepreferences.RemotePreferences
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.common.Const.PREF_UPDATE_EXCLUSIONS
import com.drdisagree.iconify.common.Resources
import com.drdisagree.iconify.xposed.HookEntry

object XPrefs {

    var Xprefs: SharedPreferences? = null
    private lateinit var packageName: String
    private val listener: SharedPreferences.OnSharedPreferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _: SharedPreferences?, key: String? ->
            loadEverything(key)
        }

    fun init(context: Context) {
        packageName = context.packageName
        Xprefs = RemotePreferences(context, BuildConfig.APPLICATION_ID, Resources.SharedXPref, true)
        (Xprefs as RemotePreferences).registerOnSharedPreferenceChangeListener(listener)
    }

    private fun loadEverything(vararg key: String?) {
        val filteredKeys = key.filterNotNull()

        if (filteredKeys.isEmpty() || PREF_UPDATE_EXCLUSIONS.any { exclusion ->
                filteredKeys.any { key ->
                    key == exclusion
                }
            }
        ) return

        for (thisMod in HookEntry.runningMods) {
            thisMod.updatePrefs(*filteredKeys.toTypedArray())
        }
    }
}
