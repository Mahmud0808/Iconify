package com.drdisagree.iconify.config
/* Modified from AOSPMods
 * https://github.com/siavash79/AOSPMods/blob/canary/app/src/main/java/sh/siava/AOSPMods/XPrefs.java
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see [http://www.gnu.org/licenses/].
 */
import android.content.Context
import android.content.SharedPreferences
import com.crossbowffs.remotepreferences.RemotePreferences
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.common.Const
import com.drdisagree.iconify.common.Resources
import com.drdisagree.iconify.xposed.HookEntry
import com.drdisagree.iconify.xposed.ModPack

object XPrefs {
    private val listener: SharedPreferences.OnSharedPreferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences: SharedPreferences?, key: String? ->
            loadEverything(key)
        }
    @JvmField
    var Xprefs: SharedPreferences? = null
    private var packageName: String? = null
    fun init(context: Context) {
        packageName = context.packageName
        Xprefs = RemotePreferences(context, BuildConfig.APPLICATION_ID, Resources.SharedXPref, true)
        Xprefs.registerOnSharedPreferenceChangeListener(listener)
    }

    fun loadEverything(vararg key: String?) {
        if (key.size > 0 && (key[0] == null || Const.PREF_UPDATE_EXCLUSIONS.stream()
                .anyMatch { exclusion: String? ->
                    key[0]!!
                        .startsWith(exclusion!!)
                })
        ) return
        for (thisMod in HookEntry.runningMods) {
            if (thisMod != null) {
                thisMod.updatePrefs(*key)
            }
        }
    }
}
