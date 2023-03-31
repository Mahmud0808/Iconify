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

                try {
                    setFloatField(param.thisObject, "mDefaultScrimAlpha", alpha);
                } catch (Throwable ignored) {
                }

                try {
                    setFloatField(param.thisObject, "mBehindAlpha", alpha);
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
