package com.drdisagree.iconify.xposed.mods;

import static com.drdisagree.iconify.common.References.SYSTEM_UI_PACKAGE;
import static de.robv.android.xposed.XposedBridge.log;

import android.annotation.SuppressLint;
import android.content.res.XModuleResources;
import android.graphics.Color;

import com.topjohnwu.superuser.Shell;

import java.util.List;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class QSBlur implements IXposedHookLoadPackage, IXposedHookInitPackageResources {

    private String rootPackagePath = "";
    boolean supportsBlur;
    boolean avoidAccel1;
    boolean avoidAccel2;
    boolean lowRam;
    boolean disableBlur;

    public static Boolean readProp(String propName) {
        List<String> out = Shell.cmd("getprop " + propName).exec().getOut();
        return out.get(0).contains("true") || out.get(0).contains("1");
    }

    @SuppressLint("DiscouragedApi")
    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resParam) throws Throwable {
        XModuleResources modRes = XModuleResources.createInstance(rootPackagePath, null);
        avoidAccel1 = modRes.getBoolean(modRes.getIdentifier("config_avoidGfxAccel", "bool", "android"));
    }

    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpParam) {
        log("Loaded App: " + lpParam.packageName);

        if (!lpParam.packageName.equals(SYSTEM_UI_PACKAGE))
            return;
        else
            log("Hooking SystemUI...");

        rootPackagePath = lpParam.appInfo.sourceDir;

        supportsBlur = readProp("ro.surface_flinger.supports_background_blur");
        avoidAccel2 = readProp("ro.config.avoid_gfx_accel");
        lowRam = readProp("ro.config.low_ram");
        disableBlur = readProp("persist.sysui.disableBlur");

        boolean capable = supportsBlur && !avoidAccel1 && !avoidAccel2 && !lowRam && !disableBlur;
        float alpha = capable ? 0.54f : 0.85f;

        final Class<?> ScrimController = XposedHelpers.findClass(SYSTEM_UI_PACKAGE + ".statusbar.phone.ScrimController", lpParam.classLoader);
        final Class<?> ScrimState = XposedHelpers.findClass(SYSTEM_UI_PACKAGE + ".statusbar.phone.ScrimState", lpParam.classLoader);
        final Class<?> ScrimView = XposedHelpers.findClass(SYSTEM_UI_PACKAGE + ".scrim.ScrimView", lpParam.classLoader);

        XposedBridge.hookAllConstructors(ScrimController, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                XposedHelpers.setFloatField(param.thisObject, "mDefaultScrimAlpha", alpha);
                XposedHelpers.setFloatField(param.thisObject, "mBehindAlpha", alpha);
                XposedHelpers.setFloatField(param.thisObject, "mNotificationsAlpha", alpha);
                XposedHelpers.setIntField(param.thisObject, "mBehindTint", Color.TRANSPARENT);

                log("Hooked ScrimController");
            }
        });

        XposedBridge.hookAllConstructors(ScrimState, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                XposedHelpers.setFloatField(param.thisObject, "mDefaultScrimAlpha", alpha);
                XposedHelpers.setFloatField(param.thisObject, "mBehindAlpha", alpha);
                XposedHelpers.setFloatField(param.thisObject, "mNotifAlpha", alpha);
                XposedHelpers.setIntField(param.thisObject, "mBehindTint", Color.TRANSPARENT);

                log("Hooked ScrimState");
            }
        });

        XposedBridge.hookAllConstructors(ScrimView, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                XposedHelpers.setFloatField(param.thisObject, "mViewAlpha", alpha);

                log("Hooked ScrimView");
            }
        });
    }
}
