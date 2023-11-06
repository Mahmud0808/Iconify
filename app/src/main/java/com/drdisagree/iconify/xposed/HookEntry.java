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
import static com.drdisagree.iconify.common.Const.FRAMEWORK_PACKAGE;
import static com.drdisagree.iconify.config.XPrefs.Xprefs;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

import android.app.Instrumentation;
import android.content.Context;

import com.drdisagree.iconify.BuildConfig;
import com.drdisagree.iconify.config.XPrefs;
import com.drdisagree.iconify.xposed.utils.BootLoopProtector;
import com.drdisagree.iconify.xposed.utils.SystemUtil;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookEntry implements IXposedHookLoadPackage {

    public static boolean isChildProcess = false;
    public static ArrayList<ModPack> runningMods = new ArrayList<>();
    public Context mContext = null;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        try {
            isChildProcess = loadPackageParam.processName.contains(":");
        } catch (Throwable ignored) {
            isChildProcess = false;
        }

        if (loadPackageParam.packageName.equals(FRAMEWORK_PACKAGE)) {
            Class<?> PhoneWindowManagerClass = findClass("com.android.server.policy.PhoneWindowManager", loadPackageParam.classLoader);

            hookAllMethods(PhoneWindowManagerClass, "init", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    try {
                        if (mContext == null) {
                            mContext = (Context) param.args[0];

                            HookRes.modRes = mContext.createPackageContext(APPLICATION_ID, CONTEXT_IGNORE_SECURITY).getResources();

                            XPrefs.init(mContext);

                            CompletableFuture.runAsync(() -> waitForXprefsLoad(loadPackageParam));
                        }
                    } catch (Throwable throwable) {
                        log(throwable);
                    }
                }
            });
        } else {
            findAndHookMethod(Instrumentation.class, "newApplication", ClassLoader.class, String.class, Context.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    try {
                        if (mContext == null) {
                            mContext = (Context) param.args[2];

                            XPrefs.init(mContext);

                            HookRes.modRes = mContext.createPackageContext(APPLICATION_ID, CONTEXT_IGNORE_SECURITY).getResources();

                            XPrefs.init(mContext);

                            waitForXprefsLoad(loadPackageParam);
                        }
                    } catch (Throwable throwable) {
                        log(throwable);
                    }
                }
            });
        }
    }

    private void onXPrefsReady(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        if (BootLoopProtector.isBootLooped(loadPackageParam.packageName)) {
            log(String.format("Possible bootloop in %s ; Iconify will not load for now...", loadPackageParam.packageName));
            return;
        }

        new SystemUtil(mContext);

        loadModpacks(loadPackageParam);
    }

    private void loadModpacks(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        for (Class<? extends ModPack> mod : EntryList.getEntries(loadPackageParam.packageName)) {
            try {
                ModPack instance = mod.getConstructor(Context.class).newInstance(mContext);

                try {
                    instance.updatePrefs();
                } catch (Throwable ignored) {
                }

                instance.handleLoadPackage(loadPackageParam);
                runningMods.add(instance);
            } catch (Throwable throwable) {
                log("Start Error Dump - Occurred in " + mod.getName());
                log(throwable);
            }
        }
    }

    @SuppressWarnings("BusyWait")
    private void waitForXprefsLoad(XC_LoadPackage.LoadPackageParam lpparam) {
        while (true) {
            try {
                Xprefs.getBoolean("LoadTestBooleanValue", false);
                break;
            } catch (Throwable ignored) {
                try {
                    Thread.sleep(1000);
                } catch (Throwable ignored1) {
                }
            }
        }

        log("Iconify Version: " + BuildConfig.VERSION_NAME);

        onXPrefsReady(lpparam);
    }
}