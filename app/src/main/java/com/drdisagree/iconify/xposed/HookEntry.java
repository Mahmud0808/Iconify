package com.drdisagree.iconify.xposed;

import static com.drdisagree.iconify.config.XPrefs.Xprefs;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import android.annotation.SuppressLint;
import android.app.Instrumentation;
import android.content.Context;

import com.drdisagree.iconify.config.XPrefs;
import com.drdisagree.iconify.utils.SystemUtil;
import com.drdisagree.iconify.xposed.mods.BlurRadius;
import com.drdisagree.iconify.xposed.mods.QSTransparency;
import com.drdisagree.iconify.xposed.mods.StatusbarClock;

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
        modPacks.add(QSTransparency.class);
        modPacks.add(BlurRadius.class);
        modPacks.add(StatusbarClock.class);
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