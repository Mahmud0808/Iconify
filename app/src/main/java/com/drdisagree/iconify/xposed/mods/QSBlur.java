package com.drdisagree.iconify.xposed.mods;

import static com.drdisagree.iconify.common.References.QSBLUR_SWITCH;
import static com.drdisagree.iconify.common.References.SYSTEM_UI_PACKAGE;
import static com.drdisagree.iconify.config.XPrefs.Xprefs;
import static com.drdisagree.iconify.config.XPrefs.modRes;
import static de.robv.android.xposed.XposedBridge.log;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;

import com.drdisagree.iconify.xposed.ModPack;
import com.topjohnwu.superuser.Shell;

import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class QSBlur extends ModPack {

    private static final String TAG = "Iconify - QSBlur";
    private String rootPackagePath = "";
    boolean supportsBlur;
    boolean avoidAccel1;
    boolean avoidAccel2;
    boolean lowRam;
    boolean disableBlur;
    boolean QsBlurActive = false;
    private static final String CLASS_SCRIMCONTROLLER = SYSTEM_UI_PACKAGE + ".statusbar.phone.ScrimController";
    private static final String CLASS_SCRIMSTATE = SYSTEM_UI_PACKAGE + ".statusbar.phone.ScrimState";
    private static final String CLASS_SCRIMVIEW = SYSTEM_UI_PACKAGE + ".scrim.ScrimView";

    public QSBlur(Context context) {
        super(context);
        if (!listensTo(context.getPackageName())) return;
    }

    public static Boolean readProp(String propName) {
        List<String> out = Shell.cmd("getprop " + propName).exec().getOut();
        return out.get(0).contains("true") || out.get(0).contains("1");
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;
        QsBlurActive = Xprefs.getBoolean(QSBLUR_SWITCH, false);
    }

    @Override
    public boolean listensTo(String packageName) {
        return packageName.equals(SYSTEM_UI_PACKAGE);
    }

    @SuppressLint("DiscouragedApi")
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
        avoidAccel1 = modRes.getBoolean(modRes.getIdentifier("config_avoidGfxAccel", "bool", "android"));

        boolean capable = supportsBlur && !avoidAccel1 && !avoidAccel2 && !lowRam && !disableBlur;
        float alpha = capable ? 0.54f : 0.85f;

        final Class<?> ScrimController = XposedHelpers.findClass(CLASS_SCRIMCONTROLLER, lpParam.classLoader);
        final Class<?> ScrimState = XposedHelpers.findClass(CLASS_SCRIMSTATE, lpParam.classLoader);
        final Class<?> ScrimView = XposedHelpers.findClass(CLASS_SCRIMVIEW, lpParam.classLoader);

        XposedBridge.hookAllConstructors(ScrimController, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (!QsBlurActive) return;

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
                if (!QsBlurActive) return;

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
                if (!QsBlurActive) return;

                XposedHelpers.setFloatField(param.thisObject, "mViewAlpha", alpha);

                log("Hooked ScrimView");
            }
        });
    }
}
