package com.drdisagree.iconify.xposed.mods;

import static com.drdisagree.iconify.common.References.QSALPHA_LEVEL;
import static com.drdisagree.iconify.common.References.QSTRANSPARENCY_SWITCH;
import static com.drdisagree.iconify.common.References.SYSTEM_UI_PACKAGE;
import static com.drdisagree.iconify.config.XPrefs.Xprefs;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;

import android.content.Context;

import com.drdisagree.iconify.xposed.ModPack;

import java.lang.reflect.Array;
import java.util.Objects;
import java.util.concurrent.Executor;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class QSTransparency extends ModPack {

    private static final String TAG = "Iconify - QSTransparency: ";
    private static final String CLASS_SCRIMCONTROLLER = SYSTEM_UI_PACKAGE + ".statusbar.phone.ScrimController";
    private static final String CLASS_SCRIMSTATE = SYSTEM_UI_PACKAGE + ".statusbar.phone.ScrimState";
    private static final String CLASS_SCRIMVIEW = SYSTEM_UI_PACKAGE + ".scrim.ScrimView";
    private static float mCustomScrimAlpha = 0.6f;
    boolean QsTransparencyActive = false;
    float behindFraction;
    private String rootPackagePath = "";
    private Object Scrims;
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
            XposedHelpers.callMethod(Scrims, "updateScrims");
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

        rootPackagePath = lpParam.appInfo.sourceDir;

        final Class<?> ScrimController = XposedHelpers.findClass(CLASS_SCRIMCONTROLLER, lpParam.classLoader);
        final Class<?> ScrimState = XposedHelpers.findClass(CLASS_SCRIMSTATE, lpParam.classLoader);

        hookAllMethods(ScrimController, "attachViews", new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam methodHookParam) {
                if (!QsTransparencyActive) return false;

                Object mNotificationsScrim = methodHookParam.args[1];
                Object mScrimBehind = methodHookParam.args[0];
                Object mScrimInFront = methodHookParam.args[2];
                boolean mClipsQsScrim = (boolean) getObjectField(methodHookParam.thisObject, "mClipsQsScrim");
                Runnable mScrimBehindChangeRunnable = (Runnable) getObjectField(methodHookParam.thisObject, "mScrimBehindChangeRunnable");
                Executor mMainExecutor = (Executor) getObjectField(methodHookParam.thisObject, "mMainExecutor;");
                Object mDozeParameters = getObjectField(methodHookParam.thisObject, "mDozeParameters");
                Object mDockManager = getObjectField(methodHookParam.thisObject, "mDockManager");
                float mScrimBehindAlphaKeyguard = 0.2f;
                Object mKeyguardUpdateMonitor = getObjectField(methodHookParam.thisObject, "mKeyguardUpdateMonitor");
                Object mKeyguardVisibilityCallback = getObjectField(methodHookParam.thisObject, "mKeyguardVisibilityCallback");

                callMethod(methodHookParam.thisObject, "updateThemeColors");
                callMethod(methodHookParam.args[0], "enableBottomEdgeConcave", mClipsQsScrim);

                if (mScrimBehindChangeRunnable != null) {
                    callMethod(mScrimBehind, "setChangeRunnable", mScrimBehindChangeRunnable, mMainExecutor);
                    mScrimBehindChangeRunnable = null;
                }

                if (mScrimBehind != null) {
                    mCustomScrimAlpha = alpha;
                }

                final Object[] states = (Object[]) callMethod(Array.newInstance(ScrimState), "values");
                for (Object state : states) {
                    callMethod(state, "init", mScrimInFront, mScrimBehind, mDozeParameters, mDockManager);
                    callMethod(state, "setScrimBehindAlphaKeyguard", mScrimBehindAlphaKeyguard);
                    callMethod(state, "setDefaultScrimAlpha", 0.2f);
                }

                callMethod(mScrimBehind, "setDefaultFocusHighlightEnabled", false);
                callMethod(mNotificationsScrim, "setDefaultFocusHighlightEnabled", false);
                callMethod(mScrimInFront, "setDefaultFocusHighlightEnabled", false);
                callMethod(methodHookParam.thisObject, "updateScrims");
                callMethod(mKeyguardUpdateMonitor, "registerCallback", mKeyguardVisibilityCallback);

                return true;
            }
        });

        hookAllMethods(ScrimController, "getInterpolatedFraction", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (!QsTransparencyActive) return;

                behindFraction = (float) param.getResult();
            }
        });

        hookAllMethods(ScrimController, "applyState", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (!QsTransparencyActive) return;

                boolean mClipsQsScrim = (boolean) getObjectField(param.thisObject, "mClipsQsScrim");

                if (mClipsQsScrim) {
                    setObjectField(param.thisObject, "mBehindAlpha", alpha);
                    setObjectField(param.thisObject, "mNotificationsAlpha", behindFraction * alpha);
                }
            }
        });

        log("Qs Transparency: " + alpha + "\nTransparency isActive: " + QsTransparencyActive);
    }
}
