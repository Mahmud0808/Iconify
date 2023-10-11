package com.drdisagree.iconify.xposed.mods;

/* Modified from AOSPMods
 * https://github.com/siavash79/AOSPMods/blob/canary/app/src/main/java/sh/siava/AOSPMods/systemui/QSThemeManager.java
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see [http://www.gnu.org/licenses/].
 */

import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.BLACK_QSPANEL;
import static com.drdisagree.iconify.config.XPrefs.Xprefs;
import static com.drdisagree.iconify.xposed.utils.SettingsLibUtils.getColorAttr;
import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findClassIfExists;
import static de.robv.android.xposed.XposedHelpers.getFloatField;
import static de.robv.android.xposed.XposedHelpers.getIntField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.graphics.ColorUtils;

import com.drdisagree.iconify.xposed.ModPack;
import com.drdisagree.iconify.xposed.utils.SystemUtil;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

@SuppressWarnings("RedundantThrows")
public class QSBlackTheme extends ModPack {

    public static final int STATE_ACTIVE = 2;
    private static final String TAG = "Iconify - " + QSBlackTheme.class.getSimpleName() + ": ";
    private static boolean blackQSHeaderEnabled = false;
    private Object mBehindColors;
    private boolean isDark;
    private Integer colorText = null;
    private Object mClockViewQSHeader = null;

    public QSBlackTheme(Context context) {
        super(context);

        isDark = SystemUtil.isDarkMode();
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;

        blackQSHeaderEnabled = Xprefs.getBoolean(BLACK_QSPANEL, false);

        try {
            initColors(true);
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        Class<?> QSTileViewImplClass = findClass(SYSTEMUI_PACKAGE + ".qs.tileimpl.QSTileViewImpl", lpparam.classLoader);
        Class<?> FragmentHostManagerClass = findClass(SYSTEMUI_PACKAGE + ".fragments.FragmentHostManager", lpparam.classLoader);
        Class<?> ScrimControllerClass = findClass(SYSTEMUI_PACKAGE + ".statusbar.phone.ScrimController", lpparam.classLoader);
        Class<?> GradientColorsClass = findClass("com.android.internal.colorextraction.ColorExtractor$GradientColors", lpparam.classLoader);
        Class<?> QSPanelControllerClass = findClass(SYSTEMUI_PACKAGE + ".qs.QSPanelController", lpparam.classLoader);
        Class<?> InterestingConfigChangesClass = findClass("com.android.settingslib.applications.InterestingConfigChanges", lpparam.classLoader);
        Class<?> ScrimStateEnum = findClass(SYSTEMUI_PACKAGE + ".statusbar.phone.ScrimState", lpparam.classLoader);
        Class<?> QSIconViewImplClass = findClass(SYSTEMUI_PACKAGE + ".qs.tileimpl.QSIconViewImpl", lpparam.classLoader);
        Class<?> CentralSurfacesImplClass = findClassIfExists(SYSTEMUI_PACKAGE + ".statusbar.phone.CentralSurfacesImpl", lpparam.classLoader);
        Class<?> ClockClass = findClass(SYSTEMUI_PACKAGE + ".statusbar.policy.Clock", lpparam.classLoader);
        Class<?> QuickStatusBarHeaderClass = findClass(SYSTEMUI_PACKAGE + ".qs.QuickStatusBarHeader", lpparam.classLoader);
        Class<?> BrightnessControllerClass = findClass(SYSTEMUI_PACKAGE + ".settings.brightness.BrightnessController", lpparam.classLoader);
        Class<?> BrightnessMirrorControllerClass = findClass(SYSTEMUI_PACKAGE + ".statusbar.policy.BrightnessMirrorController", lpparam.classLoader);
        Class<?> BrightnessSliderControllerClass = findClassIfExists(SYSTEMUI_PACKAGE + ".settings.brightness.BrightnessSliderController", lpparam.classLoader);

        hookAllConstructors(QSPanelControllerClass, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                calculateColors();
            }
        });

        try { //QPR1
            Class<?> QSFgsManagerFooterClass = findClass(SYSTEMUI_PACKAGE + ".qs.QSFgsManagerFooter", lpparam.classLoader);

            hookAllConstructors(QSFgsManagerFooterClass, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (!isDark && blackQSHeaderEnabled) {
                        try {
                            ((View) getObjectField(param.thisObject, "mNumberContainer")).getBackground().setTint(colorText);
                        } catch (Throwable throwable) {
                            log(TAG + throwable);
                        }
                    }
                }
            });

        } catch (Throwable throwable) { //QPR2&3
            //QPR3
            Class<?> ShadeHeaderControllerClass = findClassIfExists(SYSTEMUI_PACKAGE + ".shade.ShadeHeaderController", lpparam.classLoader);
            //QPR2
            if (ShadeHeaderControllerClass == null)
                ShadeHeaderControllerClass = findClassIfExists(SYSTEMUI_PACKAGE + ".shade.LargeScreenShadeHeaderController", lpparam.classLoader);
            Class<?> QSContainerImplClass = findClass(SYSTEMUI_PACKAGE + ".qs.QSContainerImpl", lpparam.classLoader);

            if (ShadeHeaderControllerClass != null) {
                hookAllMethods(ShadeHeaderControllerClass, "onInit", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        try {
                            View mView = (View) getObjectField(param.thisObject, "mView");
                            Object iconManager = getObjectField(param.thisObject, "iconManager");
                            Object batteryIcon = getObjectField(param.thisObject, "batteryIcon");
                            Object configurationControllerListener = getObjectField(param.thisObject, "configurationControllerListener");

                            hookAllMethods(configurationControllerListener.getClass(), "onConfigChanged", new XC_MethodHook() {
                                @SuppressLint("DiscouragedApi")
                                @Override
                                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                    setHeaderComponentsColor(mView, iconManager, batteryIcon);
                                }
                            });

                            setHeaderComponentsColor(mView, iconManager, batteryIcon);
                        } catch (Throwable throwable) {
                            log(TAG + throwable);
                        }
                    }
                });
            }

            hookAllMethods(QSContainerImplClass, "updateResources", new XC_MethodHook() {
                @SuppressLint("DiscouragedApi")
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (!blackQSHeaderEnabled) return;

                    try {
                        Resources res = mContext.getResources();
                        ViewGroup view = (ViewGroup) param.thisObject;

                        View settings_button_container = view.findViewById(res.getIdentifier("settings_button_container", "id", mContext.getPackageName()));
                        ImageView icon = settings_button_container.findViewById(res.getIdentifier("icon", "id", mContext.getPackageName()));
                        icon.setImageTintList(ColorStateList.valueOf(Color.WHITE));
                    } catch (Throwable throwable) {
                        log(TAG + throwable);
                    }
                }
            });
        }

        // QS tile primary label color
        hookAllMethods(QSTileViewImplClass, "getLabelColorForState", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (!blackQSHeaderEnabled) return;

                try {
                    if ((int) param.args[0] == STATE_ACTIVE) {
                        param.setResult(colorText);
                    }
                } catch (Throwable throwable) {
                    log(TAG + throwable);
                }
            }
        });

        // QS tile secondary label color
        hookAllMethods(QSTileViewImplClass, "getSecondaryLabelColorForState", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (!blackQSHeaderEnabled) return;

                try {
                    if ((int) param.args[0] == STATE_ACTIVE) {
                        param.setResult(colorText);
                    }
                } catch (Throwable throwable) {
                    log(TAG + throwable);
                }
            }
        });

        // Auto Brightness Icon Color
        hookAllMethods(BrightnessControllerClass, "updateIcon", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (!blackQSHeaderEnabled) return;

                try {
                    ((ImageView) getObjectField(param.thisObject, "mIcon")).setImageTintList(ColorStateList.valueOf(colorText));
                } catch (Throwable throwable) {
                    log(TAG + throwable);
                }
            }
        });

        if (BrightnessSliderControllerClass != null) {
            hookAllConstructors(BrightnessSliderControllerClass, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (!blackQSHeaderEnabled) return;

                    try {
                        ((ImageView) getObjectField(param.thisObject, "mIcon")).setImageTintList(ColorStateList.valueOf(colorText));
                    } catch (Throwable throwable) {
                        try {
                            ((ImageView) getObjectField(param.thisObject, "mIconView")).setImageTintList(ColorStateList.valueOf(colorText));
                        } catch (Throwable throwable1) {
                            log(TAG + throwable1);
                        }
                    }
                }
            });
        } else {
            log(TAG + "Not a crash... BrightnessSliderController class not found.");
        }

        hookAllMethods(BrightnessMirrorControllerClass, "updateIcon", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (!blackQSHeaderEnabled) return;

                try {
                    ((ImageView) getObjectField(param.thisObject, "mIcon")).setImageTintList(ColorStateList.valueOf(colorText));
                } catch (Throwable throwable) {
                    log(TAG + throwable);
                }
            }
        });

        hookAllMethods(QSIconViewImplClass, "updateIcon", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (blackQSHeaderEnabled) {
                    try {
                        if (getIntField(param.args[1], "state") == STATE_ACTIVE) {
                            ((ImageView) param.args[0]).setImageTintList(ColorStateList.valueOf(colorText));
                        }
                    } catch (Throwable throwable) {
                        log(TAG + throwable);
                    }
                }
            }
        });

        hookAllMethods(QSIconViewImplClass, "setIcon", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (blackQSHeaderEnabled) {
                    try {
                        if (param.args[0] instanceof ImageView && getIntField(param.args[1], "state") == STATE_ACTIVE) {
                            setObjectField(param.thisObject, "mTint", colorText);
                        }
                    } catch (Throwable throwable) {
                        log(TAG + throwable);
                    }
                }
            }
        });

        // White QS Clock bug
        hookAllMethods(QuickStatusBarHeaderClass, "onFinishInflate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    mClockViewQSHeader = getObjectField(param.thisObject, "mClockView");
                } catch (Throwable ignored) {
                }
            }
        });

        // White QS Clock bug
        hookAllMethods(ClockClass, "onColorsChanged", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (blackQSHeaderEnabled && mClockViewQSHeader != null) {
                    try {
                        ((TextView) mClockViewQSHeader).setTextColor(Color.WHITE);
                    } catch (Throwable throwable) {
                        log(TAG + throwable);
                    }
                }
            }
        });

        if (CentralSurfacesImplClass != null) {
            hookAllConstructors(CentralSurfacesImplClass, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    initColors(true);
                }
            });

            hookAllMethods(CentralSurfacesImplClass, "updateTheme", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    initColors(false);
                }
            });
        }

        hookAllConstructors(QSTileViewImplClass, new XC_MethodHook() {
            @Override
            @SuppressLint("DiscouragedApi")
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (!blackQSHeaderEnabled) return;

                try {
                    setObjectField(param.thisObject, "colorLabelActive", colorText);
                    setObjectField(param.thisObject, "colorSecondaryLabelActive", colorText);

                    ViewGroup sideView = (ViewGroup) getObjectField(param.thisObject, "sideView");
                    ImageView customDrawable = sideView.findViewById(mContext.getResources().getIdentifier("customDrawable", "id", mContext.getPackageName()));
                    customDrawable.setImageTintList(ColorStateList.valueOf(colorText));
                    ImageView chevron = sideView.findViewById(mContext.getResources().getIdentifier("chevron", "id", mContext.getPackageName()));
                    chevron.setImageTintList(ColorStateList.valueOf(colorText));
                } catch (Throwable throwable) {
                    log(TAG + throwable);
                }
            }
        });

        hookAllMethods(QSIconViewImplClass, "getIconColorForState", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    if (blackQSHeaderEnabled && ((boolean) param.args[1])) {
                        param.setResult(colorText);
                    }
                } catch (Throwable throwable) {
                    log(TAG + throwable);
                }
            }
        });

        try {
            mBehindColors = GradientColorsClass.getDeclaredConstructor().newInstance();
        } catch (Throwable throwable) {
            log(TAG + throwable);
        }

        hookAllMethods(ScrimControllerClass, "updateScrims", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (!blackQSHeaderEnabled) return;

                try {
                    Object mScrimBehind = getObjectField(param.thisObject, "mScrimBehind");
                    boolean mBlankScreen = (boolean) getObjectField(param.thisObject, "mBlankScreen");
                    float alpha = getFloatField(mScrimBehind, "mViewAlpha");
                    boolean animateBehindScrim = alpha != 0 && !mBlankScreen;

                    callMethod(mScrimBehind, "setColors", mBehindColors, animateBehindScrim);
                } catch (Throwable throwable) {
                    log(TAG + throwable);
                }
            }
        });

        hookAllMethods(ScrimControllerClass, "updateThemeColors", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                calculateColors();
            }
        });

        hookAllMethods(ScrimControllerClass, "updateThemeColors", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (!blackQSHeaderEnabled) return;

                try {
                    @SuppressLint("DiscouragedApi") ColorStateList states = getColorAttr(mContext.getResources().getIdentifier("android:attr/colorBackgroundFloating", "attr", mContext.getPackageName()), mContext);

                    int surfaceBackground = states.getDefaultColor();

                    @SuppressLint("DiscouragedApi") ColorStateList accentStates = getColorAttr(mContext.getResources().getIdentifier("colorAccent", "attr", "android"), mContext);
                    int accent = accentStates.getDefaultColor();

                    callMethod(mBehindColors, "setMainColor", surfaceBackground);
                    callMethod(mBehindColors, "setSecondaryColor", accent);

                    double contrast = ColorUtils.calculateContrast((int) callMethod(mBehindColors, "getMainColor"), Color.WHITE);

                    callMethod(mBehindColors, "setSupportsDarkText", contrast > 4.5);
                } catch (Throwable throwable) {
                    log(TAG + throwable);
                }
            }
        });

        hookAllMethods(ScrimControllerClass, "applyState", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (!blackQSHeaderEnabled) return;

                try {
                    boolean mClipsQsScrim = (boolean) getObjectField(param.thisObject, "mClipsQsScrim");
                    if (mClipsQsScrim) {
                        setObjectField(param.thisObject, "mBehindTint", Color.BLACK);
                    }
                } catch (Throwable throwable) {
                    log(TAG + throwable);
                }
            }
        });

        try {
            Object[] constants = ScrimStateEnum.getEnumConstants();
            for (Object constant : constants) {
                String enumVal = constant.toString();
                switch (enumVal) {
                    case "KEYGUARD" ->
                            hookAllMethods(constant.getClass(), "prepare", new XC_MethodHook() {
                                @Override
                                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                    if (!blackQSHeaderEnabled) return;
                                    boolean mClipQsScrim = (boolean) getObjectField(param.thisObject, "mClipQsScrim");
                                    if (mClipQsScrim) {
                                        Object mScrimBehind = getObjectField(param.thisObject, "mScrimBehind");
                                        int mTintColor = getIntField(mScrimBehind, "mTintColor");
                                        if (mTintColor != Color.BLACK) {
                                            setObjectField(mScrimBehind, "mTintColor", Color.BLACK);
                                            callMethod(mScrimBehind, "updateColorWithTint", false);
                                        }

                                        callMethod(mScrimBehind, "setViewAlpha", 1f);
                                    }
                                }
                            });
                    case "BOUNCER" ->
                            hookAllMethods(constant.getClass(), "prepare", new XC_MethodHook() {
                                @Override
                                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                    if (!blackQSHeaderEnabled) return;

                                    setObjectField(param.thisObject, "mBehindTint", Color.BLACK);
                                }
                            });
                    case "SHADE_LOCKED" -> {
                        hookAllMethods(constant.getClass(), "prepare", new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                if (!blackQSHeaderEnabled) return;

                                setObjectField(param.thisObject, "mBehindTint", Color.BLACK);
                                boolean mClipQsScrim = (boolean) getObjectField(param.thisObject, "mClipQsScrim");
                                if (mClipQsScrim) {
                                    Object mScrimBehind = getObjectField(param.thisObject, "mScrimBehind");
                                    int mTintColor = getIntField(mScrimBehind, "mTintColor");
                                    if (mTintColor != Color.BLACK) {
                                        setObjectField(mScrimBehind, "mTintColor", Color.BLACK);
                                        callMethod(mScrimBehind, "updateColorWithTint", false);
                                    }

                                    callMethod(mScrimBehind, "setViewAlpha", 1f);
                                }
                            }
                        });
                        hookAllMethods(constant.getClass(), "getBehindTint", new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                if (!blackQSHeaderEnabled) return;
                                param.setResult(Color.BLACK);
                            }
                        });
                    }
                    case "UNLOCKED" ->
                            hookAllMethods(constant.getClass(), "prepare", new XC_MethodHook() {
                                @Override
                                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                    if (!blackQSHeaderEnabled) return;

                                    setObjectField(param.thisObject, "mBehindTint", Color.BLACK);

                                    Object mScrimBehind = getObjectField(param.thisObject, "mScrimBehind");
                                    int mTintColor = getIntField(mScrimBehind, "mTintColor");
                                    if (mTintColor != Color.BLACK) {
                                        setObjectField(mScrimBehind, "mTintColor", Color.BLACK);
                                        callMethod(mScrimBehind, "updateColorWithTint", false);
                                    }
                                    callMethod(mScrimBehind, "setViewAlpha", 1f);
                                }
                            });
                }
            }
        } catch (Throwable throwable) {
            log(TAG + throwable);
        }

        hookAllConstructors(FragmentHostManagerClass, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    setObjectField(param.thisObject, "mConfigChanges", InterestingConfigChangesClass.getDeclaredConstructor(int.class).newInstance(0x40000000 | 0x0004 | 0x0100 | 0x80000000 | 0x0200));
                } catch (Throwable throwable) {
                    log(TAG + throwable);
                }
            }
        });
    }

    private void initColors(boolean force) throws Throwable {
        boolean isDark = SystemUtil.isDarkMode();

        if (isDark == this.isDark && !force) return;
        this.isDark = isDark;

        calculateColors();
    }

    @SuppressLint("DiscouragedApi")
    private void calculateColors() {
        try {
            Resources res = mContext.getResources();
            colorText = res.getColor(res.getIdentifier("android:color/system_neutral1_900", "color", mContext.getPackageName()), mContext.getTheme());
        } catch (Throwable throwable) {
            log(TAG + throwable);
        }
    }

    @SuppressLint("DiscouragedApi")
    private void setHeaderComponentsColor(View mView, Object iconManager, Object batteryIcon) {
        if (!blackQSHeaderEnabled) return;

        int textColor = Color.WHITE;

        try {
            ((TextView) mView.findViewById(mContext.getResources().getIdentifier("clock", "id", mContext.getPackageName()))).setTextColor(textColor);
            ((TextView) mView.findViewById(mContext.getResources().getIdentifier("date", "id", mContext.getPackageName()))).setTextColor(textColor);
        } catch (Throwable ignored) {
        }

        try {
            callMethod(iconManager, "setTint", textColor);

            for (int i = 1; i <= 3; i++) {
                String id = String.format("carrier%s", i);

                try {
                    ((TextView) getObjectField(mView.findViewById(mContext.getResources().getIdentifier(id, "id", mContext.getPackageName())), "mCarrierText")).setTextColor(textColor);
                    ((ImageView) getObjectField(mView.findViewById(mContext.getResources().getIdentifier(id, "id", mContext.getPackageName())), "mMobileSignal")).setImageTintList(ColorStateList.valueOf(textColor));
                    ((ImageView) getObjectField(mView.findViewById(mContext.getResources().getIdentifier(id, "id", mContext.getPackageName())), "mMobileRoaming")).setImageTintList(ColorStateList.valueOf(textColor));
                } catch (Throwable ignored) {
                }
            }

            callMethod(batteryIcon, "updateColors", textColor, textColor, textColor);
        } catch (Throwable throwable) {
            log(TAG + throwable);
        }
    }
}