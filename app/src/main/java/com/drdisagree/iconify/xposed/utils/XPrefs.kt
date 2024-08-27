package com.drdisagree.iconify.xposed.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import com.crossbowffs.remotepreferences.RemotePreferences
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.common.Const.PREF_UPDATE_EXCLUSIONS
import com.drdisagree.iconify.common.Resources.SHARED_XPREFERENCES
import com.drdisagree.iconify.xposed.HookEntry
import de.robv.android.xposed.XposedBridge.log

object XPrefs {

    @SuppressLint("StaticFieldLeak")
    lateinit var Xprefs: ExtendedRemotePreferences
    private val TAG = "Iconify - ${XPrefs::class.java.simpleName}: "
    private val listener = OnSharedPreferenceChangeListener { _: SharedPreferences?, key: String? ->
        loadEverything(
            key
        )
    }

    val XprefsIsInitialized: Boolean
        get() = ::Xprefs.isInitialized

    fun init(context: Context) {
        Xprefs = ExtendedRemotePreferences(
            context,
            BuildConfig.APPLICATION_ID,
            SHARED_XPREFERENCES,
            true
        )
        (Xprefs as RemotePreferences).registerOnSharedPreferenceChangeListener(listener)
    }

    private fun loadEverything(vararg key: String?) {
        if (key.isEmpty() || key[0].isNullOrEmpty() || PREF_UPDATE_EXCLUSIONS.any { exclusion ->
                key[0]?.equals(exclusion) == true
            }) {
            return
        }

        HookEntry.runningMods.forEach { thisMod ->
            try {
                thisMod.updatePrefs(*key.filterNotNull().toTypedArray())
            } catch (throwable: Throwable) {
                log(TAG + "${thisMod.javaClass.simpleName} -> " + throwable)
            }
        }
    }
}
