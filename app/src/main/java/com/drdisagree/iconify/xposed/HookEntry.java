package com.drdisagree.iconify.xposed;

/* Modified from AOSPMods
 * https://github.com/siavash79/AOSPMods/blob/canary/app/src/main/java/sh/siava/AOSPMods/AOSPMods.java
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

import static com.drdisagree.iconify.config.XPrefs.Xprefs;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import android.annotation.SuppressLint;
import android.app.Instrumentation;
import android.content.Context;

import com.drdisagree.iconify.config.XPrefs;
import com.drdisagree.iconify.utils.SystemUtil;
import com.drdisagree.iconify.xposed.mods.BackgroundChip;
import com.drdisagree.iconify.xposed.mods.HeaderClock;
import com.drdisagree.iconify.xposed.mods.HeaderImage;
import com.drdisagree.iconify.xposed.mods.LockscreenClock;
import com.drdisagree.iconify.xposed.mods.Miscellaneous;
import com.drdisagree.iconify.xposed.mods.QSTransparency;
import com.drdisagree.iconify.xposed.mods.QuickSettings;

import java.util.ArrayList;
import java.util.Calendar;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookEntry implements IXposedHookLoadPackage {

    public static boolean isSecondProcess = false;

    public static ArrayList<Class<?>> modPacks = new ArrayList<>();
    public static ArrayList<ModPack> runningMods = new ArrayList<>();
    public Context mContext = null;

    public HookEntry() {
        modPacks.add(Miscellaneous.class);
        modPacks.add(QSTransparency.class);
        modPacks.add(QuickSettings.class);
        modPacks.add(HeaderImage.class);
        modPacks.add(HeaderClock.class);
        modPacks.add(LockscreenClock.class);
        modPacks.add(BackgroundChip.class);
    }

    @SuppressLint("ApplySharedPref")
    private static boolean bootLooped(String packageName) {
        String loadTimeKey = String.format("packageLastLoad_%s", packageName);
        String strikeKey = String.format("packageStrike_%s", packageName);
        long currentTime = Calendar.getInstance().getTime().getTime();
        long lastLoadTime = Xprefs.getLong(loadTimeKey, 0);
        int strikeCount = Xprefs.getInt(strikeKey, 0);
        if (currentTime - lastLoadTime > 40000) {
            Xprefs.edit()
                    .putLong(loadTimeKey, currentTime)
                    .putInt(strikeKey, 0)
                    .commit();
        } else if (strikeCount >= 3) {
            log(String.format("HookEntry: Possible bootloop in %s. Will not load for now", packageName));
            return true;
        } else {
            Xprefs.edit().putInt(strikeKey, ++strikeCount).commit();
        }
        return false;
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        isSecondProcess = lpparam.processName.contains(":");

        findAndHookMethod(Instrumentation.class, "newApplication", ClassLoader.class, String.class, Context.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (mContext == null) {
                    mContext = (Context) param.args[2];

                    XPrefs.init(mContext);

                    if (bootLooped(mContext.getPackageName())) {
                        return;
                    }

                    new SystemUtil(mContext);
                    XPrefs.loadEverything(mContext.getPackageName());
                }

                for (Class<?> mod : modPacks) {
                    try {
                        ModPack instance = ((ModPack) mod.getConstructor(Context.class).newInstance(mContext));
                        if (!instance.listensTo(lpparam.packageName)) continue;
                        try {
                            instance.updatePrefs();
                        } catch (Throwable ignored) {
                        }
                        instance.handleLoadPackage(lpparam);
                        runningMods.add(instance);
                    } catch (Throwable T) {
                        log("Start Error Dump - Occurred in " + mod.getName() + '\n' + T);
                    }
                }
            }
        });

    }
}