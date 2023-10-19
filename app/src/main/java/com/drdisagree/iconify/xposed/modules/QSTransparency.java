package com.drdisagree.iconify.xposed.modules;

import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.NOTIF_TRANSPARENCY_SWITCH;
import static com.drdisagree.iconify.common.Preferences.QSALPHA_LEVEL;
import static com.drdisagree.iconify.common.Preferences.QS_TRANSPARENCY_SWITCH;
import static com.drdisagree.iconify.config.XPrefs.Xprefs;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

import android.content.Context;

import com.drdisagree.iconify.xposed.ModPack;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class QSTransparency extends ModPack {

    private static final String TAG = "Iconify - " + QSTransparency.class.getSimpleName() + ": ";
    private final float keyguard_alpha = 0.85f;
    boolean qsTransparencyActive = false;
    boolean onlyNotifTransparencyActive = false;
    private float alpha = 60;

    public QSTransparency(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;

        qsTransparencyActive = Xprefs.getBoolean(QS_TRANSPARENCY_SWITCH, false);
        onlyNotifTransparencyActive = Xprefs.getBoolean(NOTIF_TRANSPARENCY_SWITCH, false);
        alpha = (float) ((float) Xprefs.getInt(QSALPHA_LEVEL, 60) / 100.0);
    }

    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpParam) {
        if (!lpParam.packageName.equals(SYSTEMUI_PACKAGE)) return;

        final Class<?> ScrimControllerClass = findClass(SYSTEMUI_PACKAGE + ".statusbar.phone.ScrimController", lpParam.classLoader);

        hookAllMethods(ScrimControllerClass, "updateScrimColor", new XC_MethodHook() {
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (!qsTransparencyActive && !onlyNotifTransparencyActive) return;

                int alphaIndex = param.args[2] instanceof Float ? 2 : 1;
                String scrimState = getObjectField(param.thisObject, "mState").toString();

                if (scrimState.equals("KEYGUARD")) {
                    param.args[alphaIndex] = 0.0f;
                } else if (scrimState.contains("BOUNCER")) {
                    param.args[alphaIndex] = (Float) param.args[alphaIndex] * keyguard_alpha;
                } else {
                    String scrimName = "unknown_scrim";

                    if (findField(ScrimControllerClass, "mScrimInFront").get(param.thisObject).equals(param.args[0])) {
                        scrimName = "front_scrim";
                    } else if (findField(ScrimControllerClass, "mScrimBehind").get(param.thisObject).equals(param.args[0])) {
                        scrimName = "behind_scrim";
                    } else if (findField(ScrimControllerClass, "mNotificationsScrim").get(param.thisObject).equals(param.args[0])) {
                        scrimName = "notifications_scrim";
                    }

                    switch (scrimName) {
                        case "behind_scrim":
                            if (onlyNotifTransparencyActive) {
                                break;
                            }
                        case "notifications_scrim":
                            param.args[alphaIndex] = (Float) param.args[alphaIndex] * alpha;
                            break;
                        default:
                            break;
                    }
                }
            }
        });
    }
}
