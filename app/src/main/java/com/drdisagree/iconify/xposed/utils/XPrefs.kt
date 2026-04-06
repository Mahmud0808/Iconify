package com.drdisagree.iconify.xposed.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.data.common.Const.PREF_UPDATE_EXCLUSIONS
import com.drdisagree.iconify.xposed.HookEntry
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.log

object XPrefs {

    @SuppressLint("StaticFieldLeak")
    lateinit var Xprefs: ExtendedRemotePreferences
    private val listener = OnSharedPreferenceChangeListener { _: SharedPreferences?, key: String? ->
        loadEverything(key)
    }

    val XprefsIsInitialized: Boolean
        get() = ::Xprefs.isInitialized

    fun init(context: Context) {
        Xprefs = ExtendedRemotePreferences(
            context,
            BuildConfig.APPLICATION_ID,
            "${BuildConfig.APPLICATION_ID}.preferences",
            true
        )
    }

    fun onContentProviderLoaded() {
        loadEverything()
        Xprefs.registerOnSharedPreferenceChangeListener(listener)
    }

    private fun loadEverything(vararg key: String?) {
        if (key.isEmpty() ||
            key[0].isNullOrEmpty() ||
            PREF_UPDATE_EXCLUSIONS.any { exclusion -> key[0]?.equals(exclusion) == true }
        ) return

        HookEntry.runningMods.forEach { thisMod ->
            try {
                thisMod.onPreferenceUpdated(*key.filterNotNull().toTypedArray())
            } catch (throwable: Throwable) {
                log(this@XPrefs, "${thisMod.javaClass.simpleName} -> " + throwable)
            }
        }
    }
}
