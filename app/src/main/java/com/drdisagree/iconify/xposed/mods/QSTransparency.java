package com.drdisagree.iconify.xposed.mods;

import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.QSALPHA_LEVEL;
import static com.drdisagree.iconify.common.Preferences.QSTRANSPARENCY_SWITCH;
import static com.drdisagree.iconify.config.XPrefs.Xprefs;
import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setFloatField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;

import android.content.Context;
import android.graphics.Color;

import com.drdisagree.iconify.xposed.ModPack;

import java.util.Objects;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class QSTransparency extends ModPack {

    private static final String TAG = "Iconify - QSTransparency: ";
    private static final String CLASS_SCRIMCONTROLLER = SYSTEMUI_PACKAGE + ".statusbar.phone.ScrimController";
    private final int tint = Color.TRANSPARENT;
    boolean QsTransparencyActive = false;
    private Float behindFraction = null;
    private Object lpparamCustom = null;
    private Object mScrimInFront = null;
    private Object mNotificationsScrim = null;
    private float alpha;

    public QSTransparency(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;

        QsTransparencyActive = Xprefs.getBoolean(QSTRANSPARENCY_SWITCH, false);
        alpha = (float) ((float) Xprefs.getInt(QSALPHA_LEVEL, 60) / 100.0);

        if (Key.length > 0 && (Objects.equals(Key[0], QSTRANSPARENCY_SWITCH) || Objects.equals(Key[0], QSALPHA_LEVEL))) {
            if (lpparamCustom != null) {
                try {
                    setFloatField(lpparamCustom, "mDefaultScrimAlpha", alpha);
                    setFloatField(lpparamCustom, "mBehindAlpha", alpha);

                    setObjectField(lpparamCustom, "mInFrontTint", tint);
                    setObjectField(lpparamCustom, "mNotificationsTint", tint);

                    try {
                        setFloatField(lpparamCustom, "mCustomScrimAlpha", alpha);
                    } catch (Throwable ignored) {
                    }

                    callMethod(lpparamCustom, "updateScrims");
                } catch (Throwable ignored) {
                }
            }
        }
    }

    @Override
    public boolean listensTo(String packageName) {
        return packageName.equals(SYSTEMUI_PACKAGE);
    }

    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpParam) {
        if (!lpParam.packageName.equals(SYSTEMUI_PACKAGE)) return;

        final Class<?> ScrimController = XposedHelpers.findClass(CLASS_SCRIMCONTROLLER, lpParam.classLoader);

        hookAllConstructors(ScrimController, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (!QsTransparencyActive) return;

                lpparamCustom = param.thisObject;
                mScrimInFront = getObjectField(param.thisObject, "mScrimInFront");
                mNotificationsScrim = getObjectField(param.thisObject, "mNotificationsScrim");

                try {
                    setFloatField(param.thisObject, "mDefaultScrimAlpha", alpha);
                } catch (Throwable ignored) {
                }

                try {
                    setFloatField(param.thisObject, "mBehindAlpha", alpha);
                } catch (Throwable ignored) {
                }

                try {
                    setFloatField(param.thisObject, "mInFrontAlpha", alpha);
                    setObjectField(param.thisObject, "mInFrontTint", tint);
                } catch (Throwable ignored) {
                }

                try {
                    setFloatField(param.thisObject, "mNotificationsAlpha", alpha);
                    setObjectField(param.thisObject, "mNotificationsTint", tint);
                } catch (Throwable ignored) {
                }

                try {
                    setFloatField(param.thisObject, "BUSY_SCRIM_ALPHA", alpha);
                } catch (Throwable ignored) {
                }

                try {
                    setFloatField(param.thisObject, "mCustomScrimAlpha", alpha);
                } catch (Throwable ignored) {
                }
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
                    if (behindFraction != null)
                        setObjectField(param.thisObject, "mNotificationsAlpha", behindFraction * alpha);
                }

                setObjectField(param.thisObject, "mInFrontTint", tint);
                setObjectField(param.thisObject, "mNotificationsTint", tint);
            }
        });

        hookAllMethods(ScrimController, "updateScrimColor", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (!QsTransparencyActive) return;

                if ((mScrimInFront != null && param.args[0] == mScrimInFront) || (mNotificationsScrim != null && param.args[0] == mNotificationsScrim)) {
                    param.args[1] = alpha;
                    param.args[2] = tint;
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
        }
    }
}
