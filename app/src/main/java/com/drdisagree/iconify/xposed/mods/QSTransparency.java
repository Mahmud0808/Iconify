package com.drdisagree.iconify.xposed.mods;

import static com.drdisagree.iconify.common.References.QSALPHA_LEVEL;
import static com.drdisagree.iconify.common.References.QSTRANSPARENCY_SWITCH;
import static com.drdisagree.iconify.common.References.SYSTEM_UI_PACKAGE;
import static com.drdisagree.iconify.config.XPrefs.Xprefs;
import static de.robv.android.xposed.XposedBridge.log;

import android.content.Context;
import android.graphics.Color;

import com.drdisagree.iconify.xposed.ModPack;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class QSTransparency extends ModPack {

    private static final String TAG = "Iconify - QSTransparency";
    private String rootPackagePath = "";
    boolean QsTransparencyActive = false;
    float alpha;
    private static final String CLASS_SCRIMCONTROLLER = SYSTEM_UI_PACKAGE + ".statusbar.phone.ScrimController";
    private static final String CLASS_SCRIMSTATE = SYSTEM_UI_PACKAGE + ".statusbar.phone.ScrimState";
    private static final String CLASS_SCRIMVIEW = SYSTEM_UI_PACKAGE + ".scrim.ScrimView";

    public QSTransparency(Context context) {
        super(context);
        if (!listensTo(context.getPackageName())) return;
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;
        QsTransparencyActive = Xprefs.getBoolean(QSTRANSPARENCY_SWITCH, false);
        alpha = (float) ((float) Xprefs.getInt(QSALPHA_LEVEL, 60) / 100.0);
    }

    @Override
    public boolean listensTo(String packageName) {
        return packageName.equals(SYSTEM_UI_PACKAGE);
    }

    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpParam) {
        log("Loaded App: " + lpParam.packageName);

        if (!lpParam.packageName.equals(SYSTEM_UI_PACKAGE))
            return;

        rootPackagePath = lpParam.appInfo.sourceDir;

        final Class<?> ScrimController = XposedHelpers.findClass(CLASS_SCRIMCONTROLLER, lpParam.classLoader);
        final Class<?> ScrimState = XposedHelpers.findClass(CLASS_SCRIMSTATE, lpParam.classLoader);
        final Class<?> ScrimView = XposedHelpers.findClass(CLASS_SCRIMVIEW, lpParam.classLoader);

        XposedBridge.hookAllConstructors(ScrimController, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (!QsTransparencyActive) return;

                XposedHelpers.setFloatField(param.thisObject, "mDefaultScrimAlpha", alpha);
                XposedHelpers.setFloatField(param.thisObject, "mBehindAlpha", alpha);
                XposedHelpers.setFloatField(param.thisObject, "mNotificationsAlpha", alpha);
                XposedHelpers.setIntField(param.thisObject, "mBehindTint", Color.TRANSPARENT);
            }
        });

        XposedBridge.hookAllConstructors(ScrimState, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (!QsTransparencyActive) return;

                XposedHelpers.setFloatField(param.thisObject, "mDefaultScrimAlpha", alpha);
                XposedHelpers.setFloatField(param.thisObject, "mBehindAlpha", alpha);
                XposedHelpers.setFloatField(param.thisObject, "mNotifAlpha", alpha);
                XposedHelpers.setIntField(param.thisObject, "mBehindTint", Color.TRANSPARENT);
            }
        });

        XposedBridge.hookAllConstructors(ScrimView, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (!QsTransparencyActive) return;

                XposedHelpers.setFloatField(param.thisObject, "mViewAlpha", alpha);
            }
        });

        log("Qs Transparency: " + alpha + "\nTransparency isActive: " + QsTransparencyActive);
    }
}
