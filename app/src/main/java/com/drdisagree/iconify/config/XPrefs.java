package com.drdisagree.iconify.config;

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

import static com.drdisagree.iconify.common.Resources.SharedXPref;
import static de.robv.android.xposed.XposedBridge.log;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.XModuleResources;

import com.crossbowffs.remotepreferences.RemotePreferences;
import com.drdisagree.iconify.BuildConfig;
import com.drdisagree.iconify.xposed.HookEntry;
import com.drdisagree.iconify.xposed.ModPack;

import de.robv.android.xposed.IXposedHookZygoteInit;

public class XPrefs implements IXposedHookZygoteInit {

    public static String MOD_PATH = "";
    public static XModuleResources modRes;
    public static SharedPreferences Xprefs;
    static SharedPreferences.OnSharedPreferenceChangeListener listener = (sharedPreferences, key) -> loadEverything(key);
    private static String packageName;

    public static void init(Context context) {
        packageName = context.getPackageName();
        Xprefs = new RemotePreferences(context, BuildConfig.APPLICATION_ID, SharedXPref, true);
        log("Iconify Version: " + BuildConfig.VERSION_NAME);
        log("Iconify Records: " + Xprefs.getAll().keySet().size());
        Xprefs.registerOnSharedPreferenceChangeListener(listener);
    }

    public static void loadEverything(String... key) {
        for (ModPack thisMod : HookEntry.runningMods) {
            thisMod.updatePrefs(key);
        }
    }

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) {
        MOD_PATH = startupParam.modulePath;
        modRes = XModuleResources.createInstance(XPrefs.MOD_PATH, null);
    }
}