package com.drdisagree.iconify.config

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import com.crossbowffs.remotepreferences.RemotePreferences
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.common.Const.PREF_UPDATE_EXCLUSIONS
import com.drdisagree.iconify.common.Resources.SHARED_XPREFERENCES
import com.drdisagree.iconify.xposed.HookEntry

object XPrefs {

    @JvmField
    var Xprefs: SharedPreferences? = null
    private var packageName: String? = null
    private val listener =
        OnSharedPreferenceChangeListener { _: SharedPreferences?, key: String? ->
            loadEverything(
                key
            )
        }

    @JvmStatic
    fun init(context: Context) {
        packageName = context.packageName
        Xprefs = RemotePreferences(context, BuildConfig.APPLICATION_ID, SHARED_XPREFERENCES, true)
        (Xprefs as RemotePreferences).registerOnSharedPreferenceChangeListener(listener)
    }

    private fun loadEverything(vararg key: String?) {
        if (key.isEmpty() || key[0].isNullOrEmpty() || PREF_UPDATE_EXCLUSIONS.any { exclusion ->
                key[0]?.equals(exclusion) == true
            }) {
            return
        }

        HookEntry.runningMods.forEach { thisMod ->
            thisMod.updatePrefs(*key.filterNotNull().toTypedArray())
        }
    }
}
