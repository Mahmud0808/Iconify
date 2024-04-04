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

import static android.service.quicksettings.Tile.STATE_UNAVAILABLE;
import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.DUALTONE_QSPANEL;
import static com.drdisagree.iconify.common.Preferences.LIGHT_QSPANEL;
import static com.drdisagree.iconify.common.Preferences.QS_TEXT_ALWAYS_WHITE;
import static com.drdisagree.iconify.common.Preferences.QS_TEXT_FOLLOW_ACCENT;
import static com.drdisagree.iconify.config.XPrefs.Xprefs;
import static com.drdisagree.iconify.xposed.modules.utils.SettingsLibUtils.getColorAttr;
import static com.drdisagree.iconify.xposed.modules.utils.SettingsLibUtils.getColorAttrDefaultColor;
import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
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
import android.graphics.PorterDuff;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.graphics.ColorUtils;

import com.drdisagree.iconify.xposed.ModPack;
import com.drdisagree.iconify.xposed.modules.utils.Helpers;
import com.drdisagree.iconify.xposed.modules.utils.SettingsLibUtils;
import com.drdisagree.iconify.xposed.utils.SystemUtil;

import java.util.ArrayList;
import java.util.Arrays;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

@SuppressWarnings("RedundantThrows")
public class QSLightThemeA14 extends ModPack {

    public static final int STATE_ACTIVE = 2;
    private static final String TAG = "Iconify - " + QSLightThemeA14.class.getSimpleName() + ": ";
    private static boolean lightQSHeaderEnabled = false;
    private static boolean dualToneQSEnabled = false;
    private Object mBehindColors;
    private boolean isDark;
    private Integer colorInactive = null;
    private Object unlockedScrimState;
    private boolean qsTextAlwaysWhite = false;
    private boolean qsTextFollowAccent = false;
    private int mScrimBehindTint = Color.BLACK;
    private Object ShadeCarrierGroupController;
    private Object mClockViewQSHeader = null;
    private static final int PM_LITE_BACKGROUND_CODE = 1;
    private final ArrayList<Object> ModernShadeCarrierGroupMobileViews = new ArrayList<>();

    public QSLightThemeA14(Context context) {
        super(context);

        isDark = SystemUtil.isDarkMode();
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;

        lightQSHeaderEnabled = Xprefs.getBoolean(LIGHT_QSPANEL, false);
        dualToneQSEnabled = lightQSHeaderEnabled && Xprefs.getBoolean(DUALTONE_QSPANEL, false);

        qsTextAlwaysWhite = Xprefs.getBoolean(QS_TEXT_ALWAYS_WHITE, false);
        qsTextFollowAccent = Xprefs.getBoolean(QS_TEXT_FOLLOW_ACCENT, false);

        applyOverlays(true);
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        Class<?> QSTileViewImplClass = findClass(SYSTEMUI_PACKAGE + ".qs.tileimpl.QSTileViewImpl", loadPackageParam.classLoader);
        Class<?> ScrimControllerClass = findClass(SYSTEMUI_PACKAGE + ".statusbar.phone.ScrimController", loadPackageParam.classLoader);
        Class<?> GradientColorsClass = findClass("com.android.internal.colorextraction.ColorExtractor$GradientColors", loadPackageParam.classLoader);
        Class<?> QSPanelControllerClass = findClass(SYSTEMUI_PACKAGE + ".qs.QSPanelController", loadPackageParam.classLoader);
        Class<?> ScrimStateEnum = findClass(SYSTEMUI_PACKAGE + ".statusbar.phone.ScrimState", loadPackageParam.classLoader);
        Class<?> QSIconViewImplClass = findClass(SYSTEMUI_PACKAGE + ".qs.tileimpl.QSIconViewImpl", loadPackageParam.classLoader);
        Class<?> CentralSurfacesImplClass = findClassIfExists(SYSTEMUI_PACKAGE + ".statusbar.phone.CentralSurfacesImpl", loadPackageParam.classLoader);
        Class<?> QSCustomizerClass = findClass(SYSTEMUI_PACKAGE + ".qs.customize.QSCustomizer", loadPackageParam.classLoader);
        Class<?> QSContainerImplClass = findClass(SYSTEMUI_PACKAGE + ".qs.QSContainerImpl", loadPackageParam.classLoader);
        Class<?> ShadeCarrierClass = findClass(SYSTEMUI_PACKAGE + ".shade.carrier.ShadeCarrier", loadPackageParam.classLoader);
        Class<?> BatteryStatusChipClass = findClass(SYSTEMUI_PACKAGE + ".statusbar.BatteryStatusChip", loadPackageParam.classLoader);
        Class<?> TextButtonViewHolderClass = findClass(SYSTEMUI_PACKAGE + ".qs.footer.ui.binder.TextButtonViewHolder", loadPackageParam.classLoader);
        Class<?> NumberButtonViewHolderClass = findClass(SYSTEMUI_PACKAGE + ".qs.footer.ui.binder.NumberButtonViewHolder", loadPackageParam.classLoader);
        Class<?> QSFooterViewClass = findClass(SYSTEMUI_PACKAGE + ".qs.QSFooterView", loadPackageParam.classLoader);
        Class<?> BrightnessControllerClass = findClass(SYSTEMUI_PACKAGE + ".settings.brightness.BrightnessController", loadPackageParam.classLoader);
        Class<?> BrightnessMirrorControllerClass = findClass(SYSTEMUI_PACKAGE + ".statusbar.policy.BrightnessMirrorController", loadPackageParam.classLoader);
        Class<?> BrightnessSliderControllerClass = findClassIfExists(SYSTEMUI_PACKAGE + ".settings.brightness.BrightnessSliderController", loadPackageParam.classLoader);
        Class<?> QuickStatusBarHeaderClass = findClass(SYSTEMUI_PACKAGE + ".qs.QuickStatusBarHeader", loadPackageParam.classLoader);
        Class<?> ClockClass = findClass(SYSTEMUI_PACKAGE + ".statusbar.policy.Clock", loadPackageParam.classLoader);
        Class<?> ThemeColorKtClass = findClassIfExists("com.android.compose.theme.ColorKt", loadPackageParam.classLoader);
        Class<?> ExpandableControllerImplClass = findClassIfExists("com.android.compose.animation.ExpandableControllerImpl", loadPackageParam.classLoader);
        Class<?> FooterActionsViewModelClass = findClassIfExists(SYSTEMUI_PACKAGE + ".qs.footer.ui.viewmodel.FooterActionsViewModel", loadPackageParam.classLoader);
        Class<?> FooterActionsViewBinderClass = findClassIfExists(SYSTEMUI_PACKAGE + ".qs.footer.ui.binder.FooterActionsViewBinder", loadPackageParam.classLoader);
        Class<?> ShadeHeaderControllerClass = findClassIfExists(SYSTEMUI_PACKAGE + ".shade.ShadeHeaderController", loadPackageParam.classLoader);
        if (ShadeHeaderControllerClass == null) {
            ShadeHeaderControllerClass = findClass(SYSTEMUI_PACKAGE + ".shade.LargeScreenShadeHeaderController", loadPackageParam.classLoader);
        }

        //background color of android 14's charging chip. Fix for light QS theme situation
        XC_MethodHook batteryStatusChipColorHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (lightQSHeaderEnabled && !isDark) {
                    ((LinearLayout) getObjectField(param.thisObject, "roundedContainer")).getBackground().setTint(colorInactive);

                    int colorPrimary = getColorAttrDefaultColor(mContext, android.R.attr.textColorPrimaryInverse);
                    int textColorSecondary = getColorAttrDefaultColor(mContext, android.R.attr.textColorSecondaryInverse);
                    callMethod(getObjectField(param.thisObject, "batteryMeterView"), "updateColors", colorPrimary, textColorSecondary, colorPrimary);
                }
            }
        };

        hookAllConstructors(BatteryStatusChipClass, batteryStatusChipColorHook);
        hookAllMethods(BatteryStatusChipClass, "onConfigurationChanged", batteryStatusChipColorHook);

        unlockedScrimState = Arrays.stream(ScrimStateEnum.getEnumConstants()).filter(c -> c.toString().equals("UNLOCKED")).findFirst().get();

        hookAllMethods(unlockedScrimState.getClass(), "prepare", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (!lightQSHeaderEnabled) return;

                setObjectField(unlockedScrimState, "mBehindTint", Color.TRANSPARENT);
            }
        });

        hookAllConstructors(QSPanelControllerClass, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                calculateColors();
            }
        });

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

        try { //A14 ap11 onwards - modern implementation of mobile icons
            Class<?> ShadeCarrierGroupControllerClass = findClass(SYSTEMUI_PACKAGE + ".shade.carrier.ShadeCarrierGroupController", loadPackageParam.classLoader);
            Class<?> MobileIconBinderClass = findClass(SYSTEMUI_PACKAGE + ".statusbar.pipeline.mobile.ui.binder.MobileIconBinder", loadPackageParam.classLoader);

            hookAllConstructors(ShadeCarrierGroupControllerClass, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    ShadeCarrierGroupController = param.thisObject;
                }
            });

            hookAllMethods(MobileIconBinderClass, "bind", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (param.args[1].getClass().getName().contains("ShadeCarrierGroupMobileIconViewModel")) {
                        ModernShadeCarrierGroupMobileViews.add(param.getResult());

                        if (!isDark && lightQSHeaderEnabled) {
                            int textColor = SettingsLibUtils.getColorAttrDefaultColor(android.R.attr.textColorPrimary, mContext);
                            setMobileIconTint(param.getResult(), textColor);
                        }
                    }
                }
            });
        } catch (Throwable ignored) {
        }

        hookAllMethods(QSContainerImplClass, "updateResources", new XC_MethodHook() {
            @SuppressLint("DiscouragedApi")
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (!isDark && lightQSHeaderEnabled) {
                    try {
                        Resources res = mContext.getResources();
                        ViewGroup view = (ViewGroup) param.thisObject;

                        View settings_button_container = view.findViewById(res.getIdentifier("settings_button_container", "id", mContext.getPackageName()));
                        ImageView settings_icon = settings_button_container.findViewById(res.getIdentifier("icon", "id", mContext.getPackageName()));
                        settings_icon.setImageTintList(ColorStateList.valueOf(Color.BLACK));
                    } catch (Throwable throwable) {
                        log(TAG + throwable);
                    }
                }
            }
        });

        hookAllConstructors(QSCustomizerClass, new XC_MethodHook() { //QS Customize panel
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (!isDark && lightQSHeaderEnabled) {
                    ViewGroup mainView = (ViewGroup) param.thisObject;
                    for (int i = 0; i < mainView.getChildCount(); i++) {
                        mainView.getChildAt(i).setBackgroundColor(mScrimBehindTint);
                    }
                }
            }
        });

        hookAllMethods(ShadeCarrierClass, "updateState", new XC_MethodHook() { //mobile signal icons - this is the legacy model. new model uses viewmodels
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (isDark || !lightQSHeaderEnabled) return;

                ((ImageView) getObjectField(param.thisObject, "mMobileSignal")).setImageTintList(ColorStateList.valueOf(Color.BLACK));
            }
        });

        hookAllConstructors(NumberButtonViewHolderClass, new XC_MethodHook() { //QS security footer count circle
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (!isDark && lightQSHeaderEnabled) {
                    ((ImageView) getObjectField(param.thisObject, "newDot")).setColorFilter(Color.BLACK);

                    ((TextView) getObjectField(param.thisObject, "number")).setTextColor(Color.BLACK);
                }
            }
        });

        hookAllConstructors(TextButtonViewHolderClass, new XC_MethodHook() { //QS security footer
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (!isDark && lightQSHeaderEnabled) {
                    ((ImageView) getObjectField(param.thisObject, "chevron")).setColorFilter(Color.BLACK);

                    ((ImageView) getObjectField(param.thisObject, "icon")).setColorFilter(Color.BLACK);

                    ((ImageView) getObjectField(param.thisObject, "newDot")).setColorFilter(Color.BLACK);

                    ((TextView) getObjectField(param.thisObject, "text")).setTextColor(Color.BLACK);
                }
            }
        });

        try {
            hookAllMethods(QSFooterViewClass, "onFinishInflate", new XC_MethodHook() { //QS Footer built text row
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (!isDark && lightQSHeaderEnabled) {
                        ((TextView) getObjectField(param.thisObject, "mBuildText")).setTextColor(Color.BLACK);

                        ((ImageView) getObjectField(param.thisObject, "mEditButton")).setColorFilter(Color.BLACK);

                        setObjectField(getObjectField(param.thisObject, "mPageIndicator"), "mTint", ColorStateList.valueOf(Color.BLACK));
                    }
                }
            });
        } catch (Throwable ignored) {
        }

        // Auto Brightness Icon Color
        hookAllMethods(BrightnessControllerClass, "updateIcon", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (isDark || !lightQSHeaderEnabled) return;

                try {
                    ((ImageView) getObjectField(param.thisObject, "mIcon")).setImageTintList(ColorStateList.valueOf(Color.BLACK));
                } catch (Throwable throwable) {
                    log(TAG + throwable);
                }
            }
        });

        if (BrightnessSliderControllerClass != null) {
            hookAllConstructors(BrightnessSliderControllerClass, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (isDark || !lightQSHeaderEnabled) return;

                    try {
                        ((ImageView) getObjectField(param.thisObject, "mIcon")).setImageTintList(ColorStateList.valueOf(Color.BLACK));
                    } catch (Throwable throwable) {
                        try {
                            ((ImageView) getObjectField(param.thisObject, "mIconView")).setImageTintList(ColorStateList.valueOf(Color.BLACK));
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
                if (isDark || !lightQSHeaderEnabled) return;

                try {
                    ((ImageView) getObjectField(param.thisObject, "mIcon")).setImageTintList(ColorStateList.valueOf(Color.BLACK));
                } catch (Throwable throwable) {
                    log(TAG + throwable);
                }
            }
        });

        hookAllMethods(QSIconViewImplClass, "updateIcon", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (lightQSHeaderEnabled && !isDark && getIntField(param.args[1], "state") == STATE_ACTIVE) {
                    try {
                        ((ImageView) param.args[0]).setImageTintList(ColorStateList.valueOf(colorInactive));
                    } catch (Throwable throwable) {
                        log(TAG + throwable);
                    }
                }
            }
        });

        hookAllMethods(QSIconViewImplClass, "setIcon", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (lightQSHeaderEnabled && !isDark) {
                    try {
                        if (param.args[0] instanceof ImageView && getIntField(param.args[1], "state") == STATE_ACTIVE) {
                            setObjectField(param.thisObject, "mTint", colorInactive);
                        }
                    } catch (Throwable throwable) {
                        log(TAG + throwable);
                    }
                }
            }
        });

        try {
            // White QS Clock bug
            hookAllMethods(QuickStatusBarHeaderClass, "onFinishInflate", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    try {
                        mClockViewQSHeader = getObjectField(param.thisObject, "mClockView");
                    } catch (Throwable ignored) {
                    }

                    if (!isDark && lightQSHeaderEnabled && mClockViewQSHeader != null) {
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
                    if (!isDark && lightQSHeaderEnabled && mClockViewQSHeader != null) {
                        try {
                            ((TextView) mClockViewQSHeader).setTextColor(Color.WHITE);
                        } catch (Throwable throwable) {
                            log(TAG + throwable);
                        }
                    }
                }
            });
        } catch (Throwable ignored) {
        }

        if (CentralSurfacesImplClass != null) {
            hookAllConstructors(CentralSurfacesImplClass, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    applyOverlays(true);
                }
            });

            hookAllMethods(CentralSurfacesImplClass, "updateTheme", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    applyOverlays(false);
                }
            });
        }

        hookAllConstructors(QSTileViewImplClass, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (!lightQSHeaderEnabled || isDark) return;

                try {
                    if (!qsTextAlwaysWhite && !qsTextFollowAccent) {
                        setObjectField(param.thisObject, "colorLabelActive", Color.WHITE);
                        setObjectField(param.thisObject, "colorSecondaryLabelActive", 0x80FFFFFF);
                    }
                    setObjectField(param.thisObject, "colorLabelInactive", Color.BLACK);
                    setObjectField(param.thisObject, "colorSecondaryLabelInactive", 0x80000000);
                } catch (Throwable throwable) {
                    log(TAG + throwable);
                }
            }
        });

        hookAllMethods(QSIconViewImplClass, "getIconColorForState", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                boolean isActiveState = false;
                boolean isDisabledState;

                try {
                    isDisabledState = (boolean) getObjectField(param.args[1], "disabledByPolicy") ||
                            (int) getObjectField(param.args[1], "state") == STATE_UNAVAILABLE;
                } catch (Throwable throwable) {
                    isDisabledState = (int) getObjectField(param.args[1], "state") == STATE_UNAVAILABLE;
                }

                try {
                    isActiveState = (int) getObjectField(param.args[1], "state") == STATE_ACTIVE;
                } catch (Throwable throwable) {
                    try {
                        isActiveState = (int) param.args[1] == STATE_ACTIVE;
                    } catch (Throwable throwable1) {
                        try {
                            isActiveState = (boolean) param.args[1];
                        } catch (Throwable throwable2) {
                            log(TAG + throwable2);
                        }
                    }
                }

                if (!isDark && lightQSHeaderEnabled) {
                    if (isDisabledState) {
                        param.setResult(0x80000000);
                    } else {
                        if (isActiveState && !qsTextAlwaysWhite && !qsTextFollowAccent) {
                            param.setResult(Color.WHITE);
                        } else if (!isActiveState) {
                            param.setResult(Color.BLACK);
                        }
                    }
                }
            }
        });

        try {
            hookAllMethods(QSIconViewImplClass, "updateIcon", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    boolean isActiveState = false;
                    boolean isDisabledState;

                    try {
                        isDisabledState = (boolean) getObjectField(param.args[1], "disabledByPolicy") ||
                                (int) getObjectField(param.args[1], "state") == STATE_UNAVAILABLE;
                    } catch (Throwable throwable) {
                        isDisabledState = (int) getObjectField(param.args[1], "state") == STATE_UNAVAILABLE;
                    }

                    try {
                        isActiveState = (int) getObjectField(param.args[1], "state") == STATE_ACTIVE;
                    } catch (Throwable throwable) {
                        try {
                            isActiveState = (int) param.args[1] == STATE_ACTIVE;
                        } catch (Throwable throwable1) {
                            try {
                                isActiveState = (boolean) param.args[1];
                            } catch (Throwable throwable2) {
                                log(TAG + throwable2);
                            }
                        }
                    }

                    if (!isDark && lightQSHeaderEnabled) {
                        ImageView mIcon = (ImageView) param.args[0];
                        if (isDisabledState) {
                            param.setResult(0x80000000);
                        } else {
                            if (isActiveState && !qsTextAlwaysWhite && !qsTextFollowAccent) {
                                mIcon.setImageTintList(ColorStateList.valueOf(Color.WHITE));
                            } else if (!isActiveState) {
                                mIcon.setImageTintList(ColorStateList.valueOf(Color.BLACK));
                            }
                        }
                    }
                }
            });
        } catch (Throwable ignored) {
        }

        try {
            hookAllMethods(QSContainerImplClass, "updateResources", new XC_MethodHook() {
                @SuppressLint("DiscouragedApi")
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (!lightQSHeaderEnabled || isDark) return;

                    try {
                        Resources res = mContext.getResources();
                        ViewGroup view = ((ViewGroup) param.thisObject).findViewById(res.getIdentifier("qs_footer_actions", "id", mContext.getPackageName()));

                        try {
                            ViewGroup pm_button_container = view.findViewById(res.getIdentifier("pm_lite", "id", mContext.getPackageName()));
                            ((ImageView) pm_button_container.getChildAt(0)).setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
                        } catch (Throwable ignored) {
                            ImageView pm_button = view.findViewById(res.getIdentifier("pm_lite", "id", mContext.getPackageName()));
                            pm_button.setImageTintList(ColorStateList.valueOf(Color.WHITE));
                        }
                    } catch (Throwable ignored) {
                    }
                }
            });
        } catch (Throwable ignored) {
        }

        try { // Compose implementation of QS Footer actions
            hookAllConstructors(ExpandableControllerImplClass, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (isDark || lightQSHeaderEnabled) return;

                    Class<?> GraphicsColorKtClass = findClass("androidx.compose.ui.graphics.ColorKt", loadPackageParam.classLoader);
                    param.args[1] = callStaticMethod(GraphicsColorKtClass, "Color", Color.BLACK);
                }
            });

            hookAllMethods(ThemeColorKtClass, "colorAttr", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (isDark || lightQSHeaderEnabled) return;

                    int code = (int) param.args[0];

                    int result = 0;

                    if (code != PM_LITE_BACKGROUND_CODE) {
                        try {
                            if (mContext.getResources().getResourceName(code).split("/")[1].equals("onShadeInactiveVariant")) {
                                result = Color.BLACK; //number button text
                            }
                        } catch (Throwable ignored) {
                        }
                    }

                    if (result != 0) {
                        Class<?> GraphicsColorKtClass = findClass("androidx.compose.ui.graphics.ColorKt", loadPackageParam.classLoader);
                        param.setResult(callStaticMethod(GraphicsColorKtClass, "Color", result));
                    }
                }
            });

            hookAllConstructors(FooterActionsViewModelClass, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (!lightQSHeaderEnabled || isDark) return;

                    // Power button
                    Object power = getObjectField(param.thisObject, "power");
                    setObjectField(power, "iconTint", Color.WHITE);

                    // Settings button
                    Object settings = getObjectField(param.thisObject, "settings");
                    setObjectField(settings, "iconTint", Color.BLACK);
                    setObjectField(settings, "backgroundColor", colorInactive);

                    // We must use the classes defined in the apk. Using our own will fail.
                    Class<?> StateFlowImplClass = findClass("kotlinx.coroutines.flow.StateFlowImpl", loadPackageParam.classLoader);
                    Class<?> ReadonlyStateFlowClass = findClass("kotlinx.coroutines.flow.ReadonlyStateFlow", loadPackageParam.classLoader);

                    Object zeroAlphaFlow = StateFlowImplClass.getConstructor(Object.class).newInstance(0f);
                    setObjectField(param.thisObject, "backgroundAlpha", ReadonlyStateFlowClass.getConstructors()[0].newInstance(zeroAlphaFlow));
                }
            });

            hookAllMethods(FooterActionsViewBinderClass, "bind", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (!lightQSHeaderEnabled || isDark) return;

                    LinearLayout view = (LinearLayout) param.args[0];
                    view.setBackgroundColor(mScrimBehindTint);
                    view.setElevation(0);
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
                if (!dualToneQSEnabled) return;

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

                if (!dualToneQSEnabled) return;

                try {
                    @SuppressLint("DiscouragedApi") ColorStateList states = getColorAttr(mContext.getResources().getIdentifier("android:attr/colorSurfaceHeader", "attr", mContext.getPackageName()), mContext);

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
                if (!lightQSHeaderEnabled) return;

                try {
                    boolean mClipsQsScrim = (boolean) getObjectField(param.thisObject, "mClipsQsScrim");
                    if (mClipsQsScrim) {
                        setObjectField(param.thisObject, "mBehindTint", Color.TRANSPARENT);
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
                                    if (!lightQSHeaderEnabled) return;

                                    boolean mClipQsScrim = (boolean) getObjectField(param.thisObject, "mClipQsScrim");
                                    if (mClipQsScrim) {
                                        Object mScrimBehind = getObjectField(param.thisObject, "mScrimBehind");
                                        int mTintColor = getIntField(mScrimBehind, "mTintColor");
                                        if (mTintColor != Color.TRANSPARENT) {
                                            setObjectField(mScrimBehind, "mTintColor", Color.TRANSPARENT);
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
                                    if (!lightQSHeaderEnabled) return;

                                    setObjectField(param.thisObject, "mBehindTint", Color.TRANSPARENT);
                                }
                            });
                    case "SHADE_LOCKED" -> {
                        hookAllMethods(constant.getClass(), "prepare", new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                if (!lightQSHeaderEnabled) return;

                                setObjectField(param.thisObject, "mBehindTint", Color.TRANSPARENT);
                                boolean mClipQsScrim = (boolean) getObjectField(param.thisObject, "mClipQsScrim");
                                if (mClipQsScrim) {
                                    Object mScrimBehind = getObjectField(param.thisObject, "mScrimBehind");
                                    int mTintColor = getIntField(mScrimBehind, "mTintColor");
                                    if (mTintColor != Color.TRANSPARENT) {
                                        setObjectField(mScrimBehind, "mTintColor", Color.TRANSPARENT);
                                        callMethod(mScrimBehind, "updateColorWithTint", false);
                                    }

                                    callMethod(mScrimBehind, "setViewAlpha", 1f);
                                }
                            }
                        });
                        hookAllMethods(constant.getClass(), "getBehindTint", new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                if (!lightQSHeaderEnabled) return;

                                param.setResult(Color.TRANSPARENT);
                            }
                        });
                    }
                    case "UNLOCKED" ->
                            hookAllMethods(constant.getClass(), "prepare", new XC_MethodHook() {
                                @Override
                                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                    if (!lightQSHeaderEnabled) return;

                                    setObjectField(param.thisObject, "mBehindTint", Color.TRANSPARENT);

                                    Object mScrimBehind = getObjectField(param.thisObject, "mScrimBehind");
                                    int mTintColor = getIntField(mScrimBehind, "mTintColor");
                                    if (mTintColor != Color.TRANSPARENT) {
                                        setObjectField(mScrimBehind, "mTintColor", Color.TRANSPARENT);
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
    }

    private void applyOverlays(boolean force) {
        boolean isCurrentlyDark = SystemUtil.isDarkMode();

        if (isCurrentlyDark == isDark && !force) return;
        isDark = isCurrentlyDark;

        String QS_LIGHT_THEME_OVERLAY = "IconifyComponentQSLT.overlay";
        String QS_DUAL_TONE_OVERLAY = "IconifyComponentQSDT.overlay";

        calculateColors();

        Helpers.disableOverlays(QS_LIGHT_THEME_OVERLAY, QS_DUAL_TONE_OVERLAY);

        try {
            Thread.sleep(50);
        } catch (Throwable ignored) {
        }

        if (lightQSHeaderEnabled) {
            if (!isCurrentlyDark)
                Helpers.enableOverlay(QS_LIGHT_THEME_OVERLAY);

            if (dualToneQSEnabled)
                Helpers.enableOverlay(QS_DUAL_TONE_OVERLAY);
        }
    }

    @SuppressLint("DiscouragedApi")
    private void calculateColors() {
        if (!lightQSHeaderEnabled) return;

        try {
            if (unlockedScrimState != null) {
                setObjectField(unlockedScrimState, "mBehindTint", Color.TRANSPARENT);
            }

            Resources res = mContext.getResources();
            if (!isDark) {
                colorInactive = res.getColor(res.getIdentifier("android:color/system_neutral1_10", "color", mContext.getPackageName()), mContext.getTheme());
            }
            mScrimBehindTint = isDark ?
                    res.getColor(res.getIdentifier("android:color/system_neutral1_1000", "color", mContext.getPackageName()), mContext.getTheme()) :
                    res.getColor(res.getIdentifier("android:color/system_neutral1_100", "color", mContext.getPackageName()), mContext.getTheme());
        } catch (Throwable throwable) {
            log(TAG + throwable);
        }
    }

    @SuppressLint("DiscouragedApi")
    private void setHeaderComponentsColor(View mView, Object iconManager, Object batteryIcon) {
        if (!lightQSHeaderEnabled) return;

        int textColorPrimary = getColorAttrDefaultColor(android.R.attr.textColorPrimary, mContext);
        int textColorSecondary = getColorAttrDefaultColor(android.R.attr.textColorSecondary, mContext);

        try {
            ((TextView) mView.findViewById(mContext.getResources().getIdentifier("clock", "id", mContext.getPackageName()))).setTextColor(textColorPrimary);
            ((TextView) mView.findViewById(mContext.getResources().getIdentifier("date", "id", mContext.getPackageName()))).setTextColor(textColorPrimary);
        } catch (Throwable ignored) {
        }

        try {
            try { //A14 ap11
                callMethod(iconManager, "setTint", textColorPrimary, textColorPrimary);

                ModernShadeCarrierGroupMobileViews.forEach(view -> setMobileIconTint(view, textColorPrimary));
                setModernSignalTextColor(textColorPrimary);
            } catch (Throwable ignored) { //A14 older
                callMethod(iconManager, "setTint", textColorPrimary);
            }

            for (int i = 1; i <= 3; i++) {
                String id = String.format("carrier%s", i);

                try {
                    ((TextView) getObjectField(mView.findViewById(mContext.getResources().getIdentifier(id, "id", mContext.getPackageName())), "mCarrierText")).setTextColor(textColorPrimary);
                    ((ImageView) getObjectField(mView.findViewById(mContext.getResources().getIdentifier(id, "id", mContext.getPackageName())), "mMobileSignal")).setImageTintList(ColorStateList.valueOf(textColorPrimary));
                    ((ImageView) getObjectField(mView.findViewById(mContext.getResources().getIdentifier(id, "id", mContext.getPackageName())), "mMobileRoaming")).setImageTintList(ColorStateList.valueOf(textColorPrimary));
                } catch (Throwable ignored) {
                }
            }

            callMethod(batteryIcon, "updateColors", textColorPrimary, textColorSecondary, textColorPrimary);
        } catch (Throwable throwable) {
            log(TAG + throwable);
        }
    }

    private void setMobileIconTint(Object ModernStatusBarViewBinding, int textColor) {
        callMethod(ModernStatusBarViewBinding, "onIconTintChanged", textColor, textColor);
    }

    @SuppressLint("DiscouragedApi")
    private void setModernSignalTextColor(int textColor) {
        Resources res = mContext.getResources();
        if (ShadeCarrierGroupController == null) return;

        for (View shadeCarrier : (View[]) getObjectField(ShadeCarrierGroupController, "mCarrierGroups")) {
            try {
                shadeCarrier = shadeCarrier.findViewById(res.getIdentifier("carrier_combo", "id", mContext.getPackageName()));
                ((TextView) shadeCarrier.findViewById(res.getIdentifier("mobile_carrier_text", "id", mContext.getPackageName()))).setTextColor(textColor);
            } catch (Throwable ignored) {
            }
        }
    }
}