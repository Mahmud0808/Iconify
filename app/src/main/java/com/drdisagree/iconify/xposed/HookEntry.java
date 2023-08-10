package com.drdisagree.iconify.xposed;

/* Modified from AOSPMods
 * https://github.com/siavash79/AOSPMods/blob/canary/app/src/main/java/sh/siava/AOSPMods/modpacks/XPLauncher.java
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

import static android.content.Context.CONTEXT_IGNORE_SECURITY;
import static com.drdisagree.iconify.BuildConfig.APPLICATION_ID;
import static com.drdisagree.iconify.xposed.utils.BootLoopProtector.isBootLooped;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import android.app.Instrumentation;
import android.content.Context;

import com.drdisagree.iconify.config.XPrefs;
import com.drdisagree.iconify.xposed.utils.SystemUtil;

import java.util.ArrayList;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookEntry implements IXposedHookLoadPackage {

    public static boolean isChildProcess = false;
    public static ArrayList<ModPack> runningMods = new ArrayList<>();
    public Context mContext = null;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        isChildProcess = lpparam.processName.contains(":");

        findAndHookMethod(Instrumentation.class, "newApplication", ClassLoader.class, String.class, Context.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (mContext == null) {
                    mContext = (Context) param.args[2];

                    XPrefs.init(mContext);

                    HookRes.modRes = mContext.createPackageContext(APPLICATION_ID, CONTEXT_IGNORE_SECURITY).getResources();

                    if (isBootLooped(mContext.getPackageName())) {
                        log(String.format("Possible bootloop in %s ; Iconify will not load for now...", mContext.getPackageName()));
                        return;
                    }

                    new SystemUtil(mContext);
                    XPrefs.loadEverything(mContext.getPackageName());
                }

                for (Class<? extends ModPack> mod : EntryList.getEntries()) {
                    try {
                        ModPack instance = ((ModPack) mod.getConstructor(Context.class).newInstance(mContext));
                        if (!instance.listensTo(lpparam.packageName)) continue;

                        try {
                            instance.updatePrefs();
                        } catch (Throwable ignored) {
                        }

                        instance.handleLoadPackage(lpparam);
                        runningMods.add(instance);
                    } catch (Throwable throwable) {
                        log("Start Error Dump - Occurred in " + mod.getName());
                        log(throwable);
                    }
                }
            }
        });
    }
}