package com.drdisagree.iconify.xposed.mods;

import static com.drdisagree.iconify.common.References.QSALPHA_LEVEL;
import static com.drdisagree.iconify.common.References.QSTRANSPARENCY_SWITCH;
import static com.drdisagree.iconify.common.References.SYSTEM_UI_PACKAGE;
import static com.drdisagree.iconify.config.XPrefs.Xprefs;
import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setFloatField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;

import android.content.Context;

import com.drdisagree.iconify.xposed.ModPack;

import java.util.Objects;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class QSTransparency extends ModPack {

    private static final String TAG = "Iconify - QSTransparency: ";
    private static final String CLASS_SCRIMCONTROLLER = SYSTEM_UI_PACKAGE + ".statusbar.phone.ScrimController";
    boolean QsTransparencyActive = false;
    private Float behindFraction = null;
    private String SYSTEM_UI_PACKAGEPath = "";
    private Object lpparamCustom = null;
    private float alpha;

    public QSTransparency(Context context) {
        super(context);
        if (!listensTo(context.getPackageName())) return;
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;

        QsTransparencyActive = Xprefs.getBoolean(QSTRANSPARENCY_SWITCH, false);
        alpha = (float) ((float) Xprefs.getInt(QSALPHA_LEVEL, 60) / 100.0);

        if (Key.length > 0 && (Objects.equals(Key[0], QSTRANSPARENCY_SWITCH) || Objects.equals(Key[0], QSALPHA_LEVEL))) {
            if (lpparamCustom != null)
                callMethod(lpparamCustom, "applyState");
        }
    }

    @Override
    public boolean listensTo(String packageName) {
        return packageName.equals(SYSTEM_UI_PACKAGE);
    }

    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpParam) {
        log("Loaded App: " + lpParam.packageName);

        if (!lpParam.packageName.equals(SYSTEM_UI_PACKAGE))
            return;

        SYSTEM_UI_PACKAGEPath = lpParam.appInfo.sourceDir;

        final Class<?> ScrimController = XposedHelpers.findClass(CLASS_SCRIMCONTROLLER, lpParam.classLoader);

        hookAllMethods(ScrimController, "getInterpolatedFraction", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (!QsTransparencyActive) return;

                behindFraction = (float) param.getResult();

                lpparamCustom = param.thisObject;
            }
        });

        hookAllMethods(ScrimController, "applyState", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (!QsTransparencyActive) return;

                boolean mClipsQsScrim = (boolean) getObjectField(param.thisObject, "mClipsQsScrim");

                if (mClipsQsScrim) {
                    setObjectField(param.thisObject, "mBehindAlpha", alpha);
                    if (behindFraction != null)
                        setObjectField(param.thisObject, "mNotificationsAlpha", behindFraction * alpha);
                }
            }
        });

        hookAllConstructors(ScrimController, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (!QsTransparencyActive) return;

                setFloatField(param.thisObject, "mDefaultScrimAlpha", alpha);
                setFloatField(param.thisObject, "mInFrontAlpha", alpha);
                setFloatField(param.thisObject, "mBehindAlpha", alpha);
                setFloatField(param.thisObject, "mNotificationsAlpha", alpha);

                try {
                    setFloatField(param.thisObject, "BUSY_SCRIM_ALPHA", alpha);
                    setFloatField(param.thisObject, "mCustomScrimAlpha", alpha);
                } catch (Throwable ignored) {
                    ;
                }
            }
        });

        try {
            hookAllMethods(ScrimController, "setCustomScrimAlpha", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (!QsTransparencyActive) return;

                    param.args[0] = (int) (alpha * 100);
                }
            });
        } catch (Throwable ignored) {
            ;
        }

        log("Qs Transparency: " + alpha + "\nTransparency isActive: " + QsTransparencyActive);
    }
}
