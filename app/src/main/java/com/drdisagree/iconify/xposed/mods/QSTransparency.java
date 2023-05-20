package com.drdisagree.iconify.xposed.mods;

import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.NOTIF_TRANSPARENCY_SWITCH;
import static com.drdisagree.iconify.common.Preferences.QSALPHA_LEVEL;
import static com.drdisagree.iconify.common.Preferences.QS_TRANSPARENCY_SWITCH;
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
    private static final String CLASS_SCRIMCONTROLLER = SYSTEMUI_PACKAGE + ".statusbar.phone.ScrimController";
    boolean qsTransparencyActive = false;
    boolean onlyNotifTransparencyActive = false;
    private Object paramThisObject = null;
    private float alpha;

    public QSTransparency(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;

        qsTransparencyActive = Xprefs.getBoolean(QS_TRANSPARENCY_SWITCH, false);
        onlyNotifTransparencyActive = Xprefs.getBoolean(NOTIF_TRANSPARENCY_SWITCH, false);
        alpha = (float) ((float) Xprefs.getInt(QSALPHA_LEVEL, 60) / 100.0);

        if (Key.length > 0 && (Objects.equals(Key[0], QS_TRANSPARENCY_SWITCH) || Objects.equals(Key[0], NOTIF_TRANSPARENCY_SWITCH) || Objects.equals(Key[0], QSALPHA_LEVEL))) {
            if (paramThisObject != null) {
                if (qsTransparencyActive || onlyNotifTransparencyActive) {
                    try {
                        setFloatField(paramThisObject, "mDefaultScrimAlpha", alpha);
                    } catch (Throwable throwable) {
                        log(TAG + throwable);
                    }

                    if (!onlyNotifTransparencyActive) {
                        try {
                            setFloatField(paramThisObject, "mBehindAlpha", alpha);
                        } catch (Throwable throwable) {
                            log(TAG + throwable);
                        }

                        try {
                            setFloatField(paramThisObject, "mCustomScrimAlpha", alpha);
                        } catch (Throwable ignored) {
                        }
                    }
                }

                try {
                    callMethod(paramThisObject, "updateScrims");
                } catch (Throwable throwable) {
                    log(TAG + throwable);
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
                if (!qsTransparencyActive && !onlyNotifTransparencyActive) return;

                paramThisObject = param.thisObject;

                if (qsTransparencyActive || onlyNotifTransparencyActive) {
                    try {
                        setFloatField(param.thisObject, "mDefaultScrimAlpha", alpha);
                    } catch (Throwable throwable) {
                        log(TAG + throwable);
                    }

                    if (!onlyNotifTransparencyActive) {
                        try {
                            setFloatField(param.thisObject, "mBehindAlpha", alpha);
                        } catch (Throwable throwable) {
                            log(TAG + throwable);
                        }

                        try {
                            setFloatField(param.thisObject, "mCustomScrimAlpha", alpha);
                        } catch (Throwable ignored) {
                        }
                    }
                }
            }
        });

        hookAllMethods(ScrimController, "applyState", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (!qsTransparencyActive) return;

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
                    if (!qsTransparencyActive) return;

                    param.args[0] = (int) (alpha * 100);
                }
            });
        } catch (Throwable ignored) {
        }
    }
}
