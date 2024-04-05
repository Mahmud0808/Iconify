package com.drdisagree.iconify.xposed.modules.themes;

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

import static android.service.quicksettings.Tile.STATE_ACTIVE;
import static android.service.quicksettings.Tile.STATE_UNAVAILABLE;
import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.BLACK_QSPANEL;
import static com.drdisagree.iconify.common.Preferences.QS_TEXT_ALWAYS_WHITE;
import static com.drdisagree.iconify.common.Preferences.QS_TEXT_FOLLOW_ACCENT;
import static com.drdisagree.iconify.config.XPrefs.Xprefs;
import static com.drdisagree.iconify.xposed.modules.utils.SettingsLibUtils.getColorAttr;
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
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;

import com.drdisagree.iconify.xposed.ModPack;
import com.drdisagree.iconify.xposed.utils.SystemUtil;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

@SuppressWarnings("RedundantThrows")
public class QSBlackThemeA13 extends ModPack {

    private static final String TAG = "Iconify - " + QSBlackThemeA13.class.getSimpleName() + ": ";
    private static boolean blackQSHeaderEnabled = false;
    private Object mBehindColors;
    private boolean isDark;
    private Integer colorText = null;
    private Integer colorTextAlpha = null;
    private Object mClockViewQSHeader = null;
    private boolean qsTextAlwaysWhite = false;
    private boolean qsTextFollowAccent = false;

    public QSBlackThemeA13(Context context) {
        super(context);

        isDark = SystemUtil.isDarkMode();
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;

        blackQSHeaderEnabled = Xprefs.getBoolean(BLACK_QSPANEL, false);

        qsTextAlwaysWhite = Xprefs.getBoolean(QS_TEXT_ALWAYS_WHITE, false);
        qsTextFollowAccent = Xprefs.getBoolean(QS_TEXT_FOLLOW_ACCENT, false);

        initColors(true);
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        Class<?> QSTileViewImplClass = findClass(SYSTEMUI_PACKAGE + ".qs.tileimpl.QSTileViewImpl", loadPackageParam.classLoader);
        Class<?> FragmentHostManagerClass = findClass(SYSTEMUI_PACKAGE + ".fragments.FragmentHostManager", loadPackageParam.classLoader);
        Class<?> ScrimControllerClass = findClass(SYSTEMUI_PACKAGE + ".statusbar.phone.ScrimController", loadPackageParam.classLoader);
        Class<?> GradientColorsClass = findClass("com.android.internal.colorextraction.ColorExtractor$GradientColors", loadPackageParam.classLoader);
        Class<?> QSPanelControllerClass = findClass(SYSTEMUI_PACKAGE + ".qs.QSPanelController", loadPackageParam.classLoader);
        Class<?> InterestingConfigChangesClass = findClass("com.android.settingslib.applications.InterestingConfigChanges", loadPackageParam.classLoader);
        Class<?> ScrimStateEnum = findClass(SYSTEMUI_PACKAGE + ".statusbar.phone.ScrimState", loadPackageParam.classLoader);
        Class<?> QSIconViewImplClass = findClass(SYSTEMUI_PACKAGE + ".qs.tileimpl.QSIconViewImpl", loadPackageParam.classLoader);
        Class<?> CentralSurfacesImplClass = findClassIfExists(SYSTEMUI_PACKAGE + ".statusbar.phone.CentralSurfacesImpl", loadPackageParam.classLoader);
        Class<?> ClockClass = findClass(SYSTEMUI_PACKAGE + ".statusbar.policy.Clock", loadPackageParam.classLoader);
        Class<?> QuickStatusBarHeaderClass = findClass(SYSTEMUI_PACKAGE + ".qs.QuickStatusBarHeader", loadPackageParam.classLoader);
        Class<?> BrightnessControllerClass = findClass(SYSTEMUI_PACKAGE + ".settings.brightness.BrightnessController", loadPackageParam.classLoader);
        Class<?> BrightnessMirrorControllerClass = findClass(SYSTEMUI_PACKAGE + ".statusbar.policy.BrightnessMirrorController", loadPackageParam.classLoader);
        Class<?> BrightnessSliderControllerClass = findClassIfExists(SYSTEMUI_PACKAGE + ".settings.brightness.BrightnessSliderController", loadPackageParam.classLoader);

        hookAllConstructors(QSPanelControllerClass, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                calculateColors();
            }
        });

        try { //QPR1
            Class<?> QSFgsManagerFooterClass = findClass(SYSTEMUI_PACKAGE + ".qs.QSFgsManagerFooter", loadPackageParam.classLoader);

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
            Class<?> ShadeHeaderControllerClass = findClassIfExists(SYSTEMUI_PACKAGE + ".shade.ShadeHeaderController", loadPackageParam.classLoader);
            //QPR2
            if (ShadeHeaderControllerClass == null)
                ShadeHeaderControllerClass = findClassIfExists(SYSTEMUI_PACKAGE + ".shade.LargeScreenShadeHeaderController", loadPackageParam.classLoader);
            Class<?> QSContainerImplClass = findClass(SYSTEMUI_PACKAGE + ".qs.QSContainerImpl", loadPackageParam.classLoader);

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
                            if (Build.VERSION.SDK_INT < 34) {
                                log(TAG + throwable1);
                            }
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

                if (blackQSHeaderEnabled && mClockViewQSHeader != null) {
                    try {
                        ((TextView) mClockViewQSHeader).setTextColor(Color.WHITE);
                    } catch (Throwable throwable) {
                        log(TAG + throwable);
                    }
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
                    if (!qsTextAlwaysWhite && !qsTextFollowAccent) {
                        setObjectField(param.thisObject, "colorLabelActive", colorText);
                        setObjectField(param.thisObject, "colorSecondaryLabelActive", colorTextAlpha);
                    }
                    setObjectField(param.thisObject, "colorLabelInactive", Color.WHITE);
                    setObjectField(param.thisObject, "colorSecondaryLabelInactive", 0x80FFFFFF);

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
                boolean isActiveState = isActiveState(param);
                boolean isDisabledState = isDisabledState(param);

                if (blackQSHeaderEnabled) {
                    if (isDisabledState) {
                        param.setResult(0x80FFFFFF);
                    } else if (isActiveState && !qsTextAlwaysWhite && !qsTextFollowAccent) {
                        param.setResult(colorText);
                    } else if (!isActiveState) {
                        param.setResult(Color.WHITE);
                    }
                }
            }
        });

        try {
            hookAllMethods(QSIconViewImplClass, "updateIcon", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (qsTextAlwaysWhite || qsTextFollowAccent) return;

                    boolean isActiveState = isActiveState(param);
                    boolean isDisabledState = isDisabledState(param);

                    if (blackQSHeaderEnabled) {
                        ImageView mIcon = (ImageView) param.args[0];
                        if (isDisabledState) {
                            mIcon.setImageTintList(ColorStateList.valueOf(0x80FFFFFF));
                        } else if (isActiveState && !qsTextAlwaysWhite && !qsTextFollowAccent) {
                            mIcon.setImageTintList(ColorStateList.valueOf(colorText));
                        } else if (!isActiveState) {
                            mIcon.setImageTintList(ColorStateList.valueOf(Color.WHITE));
                        }
                    }
                }
            });
        } catch (Throwable ignored) {
        }

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

    private static boolean isActiveState(@NonNull XC_MethodHook.MethodHookParam param) {
        boolean isActiveState;

        try {
            isActiveState = (int) getObjectField(param.args[1], "state") == STATE_ACTIVE;
        } catch (Throwable throwable) {
            try {
                isActiveState = (int) param.args[1] == STATE_ACTIVE;
            } catch (Throwable throwable1) {
                try {
                    isActiveState = (boolean) param.args[1];
                } catch (Throwable throwable2) {
                    isActiveState = false;
                    log(TAG + throwable2);
                }
            }
        }

        return isActiveState;
    }

    private static boolean isDisabledState(@NonNull XC_MethodHook.MethodHookParam param) {
        boolean isDisabledState;

        try {
            isDisabledState = (boolean) getObjectField(param.args[1], "disabledByPolicy") ||
                    (int) getObjectField(param.args[1], "state") == STATE_UNAVAILABLE;
        } catch (Throwable throwable) {
            try {
                isDisabledState = (int) getObjectField(param.args[1], "state") == STATE_UNAVAILABLE;
            } catch (Throwable throwable1) {
                isDisabledState = (int) param.args[1] == STATE_UNAVAILABLE;
            }
        }

        return isDisabledState;
    }

    private void initColors(boolean force) {
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
            colorTextAlpha = (colorText & 0xFFFFFF) | (Math.round(Color.alpha(colorText) * 0.8f) << 24);
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