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
import com.drdisagree.iconify.xposed.utils.SystemUtil;

import java.util.ArrayList;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

@SuppressWarnings("RedundantThrows")
public class QSBlackThemeA14 extends ModPack {

    private static final String TAG = "Iconify - " + QSBlackThemeA14.class.getSimpleName() + ": ";
    private static boolean blackQSHeaderEnabled = false;
    private Object mBehindColors;
    private boolean isDark;
    private Integer colorText = null;
    private Integer colorTextAlpha = null;
    private Object mClockViewQSHeader = null;
    private boolean qsTextAlwaysWhite = false;
    private boolean qsTextFollowAccent = false;
    private Object ShadeCarrierGroupController;
    private static final int PM_LITE_BACKGROUND_CODE = 1;
    private final ArrayList<Object> ModernShadeCarrierGroupMobileViews = new ArrayList<>();

    public QSBlackThemeA14(Context context) {
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
        Class<?> ScrimStateEnum = findClass(SYSTEMUI_PACKAGE + ".statusbar.phone.ScrimState", loadPackageParam.classLoader);
        Class<?> QSIconViewImplClass = findClass(SYSTEMUI_PACKAGE + ".qs.tileimpl.QSIconViewImpl", loadPackageParam.classLoader);
        Class<?> CentralSurfacesImplClass = findClassIfExists(SYSTEMUI_PACKAGE + ".statusbar.phone.CentralSurfacesImpl", loadPackageParam.classLoader);
        Class<?> QSCustomizerClass = findClass(SYSTEMUI_PACKAGE + ".qs.customize.QSCustomizer", loadPackageParam.classLoader);
        Class<?> QSContainerImplClass = findClass(SYSTEMUI_PACKAGE + ".qs.QSContainerImpl", loadPackageParam.classLoader);
        Class<?> ShadeCarrierClass = findClass(SYSTEMUI_PACKAGE + ".shade.carrier.ShadeCarrier", loadPackageParam.classLoader);
        Class<?> InterestingConfigChangesClass = findClass("com.android.settingslib.applications.InterestingConfigChanges", loadPackageParam.classLoader);
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
                if (blackQSHeaderEnabled) {
                    ((LinearLayout) getObjectField(param.thisObject, "roundedContainer")).getBackground().setTint(Color.DKGRAY);

                    callMethod(getObjectField(param.thisObject, "batteryMeterView"), "updateColors", Color.WHITE, Color.GRAY, Color.WHITE);
                }
            }
        };

        hookAllConstructors(BatteryStatusChipClass, batteryStatusChipColorHook);
        hookAllMethods(BatteryStatusChipClass, "onConfigurationChanged", batteryStatusChipColorHook);

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

                        if (blackQSHeaderEnabled) {
                            setMobileIconTint(param.getResult(), Color.WHITE);
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
                if (!blackQSHeaderEnabled) return;

                try {
                    Resources res = mContext.getResources();
                    ViewGroup view = (ViewGroup) param.thisObject;

                    View settings_button_container = view.findViewById(res.getIdentifier("settings_button_container", "id", mContext.getPackageName()));
                    ImageView settings_icon = settings_button_container.findViewById(res.getIdentifier("icon", "id", mContext.getPackageName()));
                    settings_icon.setImageTintList(ColorStateList.valueOf(Color.WHITE));
                } catch (Throwable throwable) {
                    log(TAG + throwable);
                }
            }
        });

        hookAllConstructors(QSCustomizerClass, new XC_MethodHook() { //QS Customize panel
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (blackQSHeaderEnabled) {
                    ViewGroup mainView = (ViewGroup) param.thisObject;
                    for (int i = 0; i < mainView.getChildCount(); i++) {
                        mainView.getChildAt(i).setBackgroundColor(Color.BLACK);
                    }
                }
            }
        });

        hookAllMethods(ShadeCarrierClass, "updateState", new XC_MethodHook() { //mobile signal icons - this is the legacy model. new model uses viewmodels
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (!blackQSHeaderEnabled) return;

                ((ImageView) getObjectField(param.thisObject, "mMobileSignal")).setImageTintList(ColorStateList.valueOf(Color.WHITE));
            }
        });

        hookAllConstructors(NumberButtonViewHolderClass, new XC_MethodHook() { //QS security footer count circle
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (blackQSHeaderEnabled) {
                    ((ImageView) getObjectField(param.thisObject, "newDot")).setColorFilter(Color.WHITE);

                    ((TextView) getObjectField(param.thisObject, "number")).setTextColor(Color.WHITE);
                }
            }
        });

        hookAllConstructors(TextButtonViewHolderClass, new XC_MethodHook() { //QS security footer
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (blackQSHeaderEnabled) {
                    ((ImageView) getObjectField(param.thisObject, "chevron")).setColorFilter(Color.WHITE);

                    ((ImageView) getObjectField(param.thisObject, "icon")).setColorFilter(Color.WHITE);

                    ((ImageView) getObjectField(param.thisObject, "newDot")).setColorFilter(Color.WHITE);

                    ((TextView) getObjectField(param.thisObject, "text")).setTextColor(Color.WHITE);
                }
            }
        });

        try {
            hookAllMethods(QSFooterViewClass, "onFinishInflate", new XC_MethodHook() { //QS Footer built text row
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (blackQSHeaderEnabled) {
                        ((TextView) getObjectField(param.thisObject, "mBuildText")).setTextColor(Color.WHITE);

                        ((ImageView) getObjectField(param.thisObject, "mEditButton")).setColorFilter(Color.WHITE);

                        setObjectField(getObjectField(param.thisObject, "mPageIndicator"), "mTint", ColorStateList.valueOf(Color.WHITE));
                    }
                }
            });
        } catch (Throwable ignored) {
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

        try {
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
        } catch (Throwable ignored) {
        }

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
            hookAllMethods(QSContainerImplClass, "updateResources", new XC_MethodHook() {
                @SuppressLint("DiscouragedApi")
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (!blackQSHeaderEnabled) return;

                    try {
                        Resources res = mContext.getResources();
                        ViewGroup view = ((ViewGroup) param.thisObject).findViewById(res.getIdentifier("qs_footer_actions", "id", mContext.getPackageName()));

                        try {
                            ViewGroup pm_button_container = view.findViewById(res.getIdentifier("pm_lite", "id", mContext.getPackageName()));
                            ((ImageView) pm_button_container.getChildAt(0)).setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
                        } catch (Throwable ignored) {
                            ImageView pm_button = view.findViewById(res.getIdentifier("pm_lite", "id", mContext.getPackageName()));
                            pm_button.setImageTintList(ColorStateList.valueOf(Color.BLACK));
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
                    if (!blackQSHeaderEnabled) return;

                    Class<?> GraphicsColorKtClass = findClass("androidx.compose.ui.graphics.ColorKt", loadPackageParam.classLoader);
                    param.args[1] = callStaticMethod(GraphicsColorKtClass, "Color", Color.WHITE);
                }
            });

            hookAllMethods(ThemeColorKtClass, "colorAttr", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (!blackQSHeaderEnabled) return;

                    int code = (int) param.args[0];

                    int result = 0;

                    if (code != PM_LITE_BACKGROUND_CODE) {
                        try {
                            if (mContext.getResources().getResourceName(code).split("/")[1].equals("onShadeInactiveVariant")) {
                                result = Color.WHITE; //number button text
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
                    if (!blackQSHeaderEnabled) return;

                    // Power button
                    Object power = getObjectField(param.thisObject, "power");
                    setObjectField(power, "iconTint", Color.BLACK);

                    // Settings button
                    Object settings = getObjectField(param.thisObject, "settings");
                    setObjectField(settings, "iconTint", Color.WHITE);

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
                    if (!blackQSHeaderEnabled) return;

                    LinearLayout view = (LinearLayout) param.args[0];
                    view.setBackgroundColor(Color.BLACK);
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
            try { //A14 ap11
                callMethod(iconManager, "setTint", textColor, textColor);

                ModernShadeCarrierGroupMobileViews.forEach(view -> setMobileIconTint(view, textColor));
                setModernSignalTextColor(textColor);
            } catch (Throwable ignored) { //A14 older
                callMethod(iconManager, "setTint", textColor);
            }

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