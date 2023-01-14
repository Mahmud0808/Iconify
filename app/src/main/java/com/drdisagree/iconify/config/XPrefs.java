package com.drdisagree.iconify.config;

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
    private static String packageName;

    static SharedPreferences.OnSharedPreferenceChangeListener listener = (sharedPreferences, key) -> loadEverything(key);

    public static void init(Context context) {
        packageName = context.getPackageName();
        Xprefs = new RemotePreferences(context, BuildConfig.APPLICATION_ID, BuildConfig.APPLICATION_ID + "_xpreferences", true);
        log("Iconify Version: " + BuildConfig.VERSION_NAME);
        log("Iconify Records: " + Xprefs.getAll().keySet().size());
        Xprefs.registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) {
        MOD_PATH = startupParam.modulePath;
        modRes = XModuleResources.createInstance(XPrefs.MOD_PATH, null);
    }

    public static void loadEverything(String... key) {
        for (ModPack thisMod : HookEntry.runningMods) {
            thisMod.updatePrefs(key);
        }
    }
}