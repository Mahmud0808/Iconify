package com.drdisagree.iconify.xposed.mods;

import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.FIXED_STATUS_ICONS_SIDEMARGIN;
import static com.drdisagree.iconify.common.Preferences.FIXED_STATUS_ICONS_SWITCH;
import static com.drdisagree.iconify.common.Preferences.FIXED_STATUS_ICONS_TOPMARGIN;
import static com.drdisagree.iconify.common.Preferences.HIDE_DATA_DISABLED_ICON;
import static com.drdisagree.iconify.common.Preferences.HIDE_LOCKSCREEN_CARRIER;
import static com.drdisagree.iconify.common.Preferences.HIDE_LOCKSCREEN_STATUSBAR;
import static com.drdisagree.iconify.common.Preferences.HIDE_STATUS_ICONS_SWITCH;
import static com.drdisagree.iconify.common.Preferences.QSPANEL_HIDE_CARRIER;
import static com.drdisagree.iconify.config.XPrefs.Xprefs;
import static com.drdisagree.iconify.xposed.HookRes.resparams;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findClassIfExists;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.drdisagree.iconify.xposed.ModPack;

import java.util.Objects;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Miscellaneous extends ModPack implements IXposedHookLoadPackage {

    private static final String TAG = "Iconify - " + Miscellaneous.class.getSimpleName() + ": ";
    boolean QSCarrierGroupHidden = false;
    boolean hideStatusIcons = false;
    boolean fixedStatusIcons = false;
    boolean hideLockscreenCarrier = false;
    boolean hideLockscreenStatusbar = false;
    boolean hideDataDisabledIcon = false;
    int sideMarginStatusIcons = 0;
    int topMarginStatusIcons = 8;
    LinearLayout statusIcons = null;
    LinearLayout statusIconContainer = null;
    private Object MobileSignalControllerParam = null;

    public Miscellaneous(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;

        QSCarrierGroupHidden = Xprefs.getBoolean(QSPANEL_HIDE_CARRIER, false);
        hideStatusIcons = Xprefs.getBoolean(HIDE_STATUS_ICONS_SWITCH, false);
        fixedStatusIcons = Xprefs.getBoolean(FIXED_STATUS_ICONS_SWITCH, false);
        topMarginStatusIcons = Xprefs.getInt(FIXED_STATUS_ICONS_TOPMARGIN, 0);
        sideMarginStatusIcons = Xprefs.getInt(FIXED_STATUS_ICONS_SIDEMARGIN, 0);
        hideLockscreenCarrier = Xprefs.getBoolean(HIDE_LOCKSCREEN_CARRIER, false);
        hideLockscreenStatusbar = Xprefs.getBoolean(HIDE_LOCKSCREEN_STATUSBAR, false);
        hideDataDisabledIcon = Xprefs.getBoolean(HIDE_DATA_DISABLED_ICON, false);

        if (Key.length > 0) {
            if (Objects.equals(Key[0], QSPANEL_HIDE_CARRIER)) hideQSCarrierGroup();

            if (Objects.equals(Key[0], HIDE_STATUS_ICONS_SWITCH)) hideStatusIcons();

            if (Objects.equals(Key[0], FIXED_STATUS_ICONS_SWITCH) || Objects.equals(Key[0], HIDE_STATUS_ICONS_SWITCH) || Objects.equals(Key[0], FIXED_STATUS_ICONS_TOPMARGIN) || Objects.equals(Key[0], FIXED_STATUS_ICONS_SIDEMARGIN))
                fixedStatusIconsA12();

            if (Objects.equals(Key[0], HIDE_LOCKSCREEN_CARRIER) || Objects.equals(Key[0], HIDE_LOCKSCREEN_STATUSBAR))
                hideLockscreenCarrierOrStatusbar();

            if (Objects.equals(Key[0], HIDE_DATA_DISABLED_ICON) && MobileSignalControllerParam != null)
                callMethod(MobileSignalControllerParam, "updateTelephony");
        }
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!lpparam.packageName.equals(SYSTEMUI_PACKAGE)) return;

        final Class<?> QuickStatusBarHeader = findClass(SYSTEMUI_PACKAGE + ".qs.QuickStatusBarHeader", lpparam.classLoader);

        try {
            hookAllMethods(QuickStatusBarHeader, "onFinishInflate", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (hideStatusIcons) {
                        try {
                            View mDateView = (View) getObjectField(param.thisObject, "mDateView");
                            mDateView.getLayoutParams().height = 0;
                            mDateView.getLayoutParams().width = 0;
                            mDateView.setVisibility(View.INVISIBLE);
                        } catch (Throwable ignored) {
                        }

                        try {
                            TextView mClockDateView = (TextView) getObjectField(param.thisObject, "mClockDateView");
                            mClockDateView.setVisibility(View.INVISIBLE);
                            mClockDateView.setTextAppearance(0);
                            mClockDateView.setTextColor(0);
                        } catch (Throwable ignored) {
                        }

                        try {
                            TextView mClockView = (TextView) getObjectField(param.thisObject, "mClockView");
                            mClockView.setVisibility(View.INVISIBLE);
                            mClockView.setTextAppearance(0);
                            mClockView.setTextColor(0);
                        } catch (Throwable ignored) {
                        }
                    }

                    if (hideStatusIcons || QSCarrierGroupHidden) {
                        try {
                            View mQSCarriers = (View) getObjectField(param.thisObject, "mQSCarriers");
                            mQSCarriers.setVisibility(View.INVISIBLE);
                        } catch (Throwable ignored) {
                        }
                    }
                }
            });
        } catch (Throwable ignored) {
        }

        try {
            Class<?> ShadeHeaderControllerClass = findClassIfExists(SYSTEMUI_PACKAGE + ".shade.LargeScreenShadeHeaderController", lpparam.classLoader);
            if (ShadeHeaderControllerClass == null)
                ShadeHeaderControllerClass = findClass(SYSTEMUI_PACKAGE + ".shade.ShadeHeaderController", lpparam.classLoader);

            hookAllMethods(ShadeHeaderControllerClass, "onInit", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (hideStatusIcons) {
                        try {
                            LinearLayout iconContainer = (LinearLayout) getObjectField(param.thisObject, "iconContainer");
                            ((ViewGroup) iconContainer.getParent()).removeView(iconContainer);
                        } catch (Throwable ignored) {
                        }

                        try {
                            LinearLayout batteryIcon = (LinearLayout) getObjectField(param.thisObject, "batteryIcon");
                            ((ViewGroup) batteryIcon.getParent()).removeView(batteryIcon);
                        } catch (Throwable ignored) {
                        }
                    }

                    if (hideStatusIcons || QSCarrierGroupHidden) {
                        try {
                            LinearLayout qsCarrierGroup = (LinearLayout) getObjectField(param.thisObject, "qsCarrierGroup");
                            ((ViewGroup) qsCarrierGroup.getParent()).removeView(qsCarrierGroup);
                        } catch (Throwable ignored) {
                        }

                        try {
                            LinearLayout mShadeCarrierGroup = (LinearLayout) getObjectField(param.thisObject, "mShadeCarrierGroup");
                            ((ViewGroup) mShadeCarrierGroup.getParent()).removeView(mShadeCarrierGroup);
                        } catch (Throwable ignored) {
                        }
                    }
                }
            });
        } catch (Throwable ignored) {
        }

        hideQSCarrierGroup();
        hideStatusIcons();
        fixedStatusIconsA12();
        hideLockscreenCarrierOrStatusbar();

        try {
            Class<?> MobileSignalController = findClass(SYSTEMUI_PACKAGE + ".statusbar.connectivity.MobileSignalController", lpparam.classLoader);
            final boolean[] alwaysShowDataRatIcon = {false};
            final boolean[] mDataDisabledIcon = {false};

            hookAllMethods(MobileSignalController, "updateTelephony", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (MobileSignalControllerParam == null)
                        MobileSignalControllerParam = param.thisObject;

                    if (!hideDataDisabledIcon) return;

                    alwaysShowDataRatIcon[0] = (boolean) getObjectField(getObjectField(param.thisObject, "mConfig"), "alwaysShowDataRatIcon");
                    setObjectField(getObjectField(param.thisObject, "mConfig"), "alwaysShowDataRatIcon", false);

                    try {
                        mDataDisabledIcon[0] = (boolean) getObjectField(param.thisObject, "mDataDisabledIcon");
                        setObjectField(param.thisObject, "mDataDisabledIcon", false);
                    } catch (Throwable ignored) {
                    }
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (MobileSignalControllerParam == null)
                        MobileSignalControllerParam = param.thisObject;

                    if (!hideDataDisabledIcon) return;

                    setObjectField(getObjectField(param.thisObject, "mConfig"), "alwaysShowDataRatIcon", alwaysShowDataRatIcon[0]);

                    try {
                        setObjectField(param.thisObject, "mDataDisabledIcon", mDataDisabledIcon[0]);
                    } catch (Throwable ignored) {
                    }
                }
            });
        } catch (Throwable ignored) {
            log(TAG + "Not a crash... MobileSignalController class not found.");
        }
    }

    private void hideQSCarrierGroup() {
        XC_InitPackageResources.InitPackageResourcesParam ourResparam = resparams.get(SYSTEMUI_PACKAGE);
        if (ourResparam == null) return;

        try {
            ourResparam.res.hookLayout(SYSTEMUI_PACKAGE, "layout", "quick_qs_status_icons", new XC_LayoutInflated() {
                @Override
                public void handleLayoutInflated(XC_LayoutInflated.LayoutInflatedParam liparam) {
                    if (!QSCarrierGroupHidden) return;

                    @SuppressLint("DiscouragedApi") LinearLayout carrier_group = liparam.view.findViewById(liparam.res.getIdentifier("carrier_group", "id", mContext.getPackageName()));
                    carrier_group.getLayoutParams().height = 0;
                    carrier_group.getLayoutParams().width = 0;
                    carrier_group.setMinimumWidth(0);
                    carrier_group.setVisibility(View.INVISIBLE);
                }
            });
        } catch (Throwable ignored) {
        }
    }

    private void hideStatusIcons() {
        XC_InitPackageResources.InitPackageResourcesParam ourResparam = resparams.get(SYSTEMUI_PACKAGE);
        if (ourResparam == null) return;
        try {
            ourResparam.res.hookLayout(SYSTEMUI_PACKAGE, "layout", "quick_qs_status_icons", new XC_LayoutInflated() {
                @SuppressLint({"DiscouragedApi"})
                @Override
                public void handleLayoutInflated(XC_LayoutInflated.LayoutInflatedParam liparam) {
                    if (!hideStatusIcons) return;

                    try {
                        @SuppressLint("DiscouragedApi") TextView clock = liparam.view.findViewById(liparam.res.getIdentifier("clock", "id", mContext.getPackageName()));
                        clock.getLayoutParams().height = 0;
                        clock.getLayoutParams().width = 0;
                        clock.setTextAppearance(0);
                        clock.setTextColor(0);
                    } catch (Throwable ignored) {
                    }

                    try {
                        @SuppressLint("DiscouragedApi") TextView date_clock = liparam.view.findViewById(liparam.res.getIdentifier("date_clock", "id", mContext.getPackageName()));
                        date_clock.getLayoutParams().height = 0;
                        date_clock.getLayoutParams().width = 0;
                        date_clock.setTextAppearance(0);
                        date_clock.setTextColor(0);
                    } catch (Throwable ignored) {
                    }

                    try {
                        @SuppressLint("DiscouragedApi") LinearLayout carrier_group = liparam.view.findViewById(liparam.res.getIdentifier("carrier_group", "id", mContext.getPackageName()));
                        carrier_group.getLayoutParams().height = 0;
                        carrier_group.getLayoutParams().width = 0;
                        carrier_group.setMinimumWidth(0);
                        carrier_group.setVisibility(View.INVISIBLE);
                    } catch (Throwable ignored) {
                    }

                    try {
                        @SuppressLint("DiscouragedApi") LinearLayout statusIcons = liparam.view.findViewById(liparam.res.getIdentifier("statusIcons", "id", mContext.getPackageName()));
                        statusIcons.getLayoutParams().height = 0;
                        statusIcons.getLayoutParams().width = 0;
                    } catch (Throwable ignored) {
                    }

                    try {
                        @SuppressLint("DiscouragedApi") LinearLayout batteryRemainingIcon = liparam.view.findViewById(liparam.res.getIdentifier("batteryRemainingIcon", "id", mContext.getPackageName()));
                        batteryRemainingIcon.getLayoutParams().height = 0;
                        batteryRemainingIcon.getLayoutParams().width = 0;
                    } catch (Throwable ignored) {
                    }

                    try {
                        @SuppressLint("DiscouragedApi") FrameLayout rightLayout = liparam.view.findViewById(liparam.res.getIdentifier("rightLayout", "id", mContext.getPackageName()));
                        rightLayout.getLayoutParams().height = 0;
                        rightLayout.getLayoutParams().width = 0;
                        rightLayout.setVisibility(View.INVISIBLE);
                    } catch (Throwable ignored) {
                    }

                    // Ricedroid date
                    try {
                        @SuppressLint("DiscouragedApi") TextView date = liparam.view.findViewById(liparam.res.getIdentifier("date", "id", mContext.getPackageName()));
                        date.getLayoutParams().height = 0;
                        date.getLayoutParams().width = 0;
                        date.setTextAppearance(0);
                        date.setTextColor(0);
                    } catch (Throwable ignored) {
                    }

                    // Nusantara clock
                    try {
                        @SuppressLint("DiscouragedApi") TextView jr_clock = liparam.view.findViewById(liparam.res.getIdentifier("jr_clock", "id", mContext.getPackageName()));
                        jr_clock.getLayoutParams().height = 0;
                        jr_clock.getLayoutParams().width = 0;
                        jr_clock.setTextAppearance(0);
                        jr_clock.setTextColor(0);
                    } catch (Throwable ignored) {
                    }

                    // Nusantara date
                    try {
                        @SuppressLint("DiscouragedApi") LinearLayout jr_date_container = liparam.view.findViewById(liparam.res.getIdentifier("jr_date_container", "id", mContext.getPackageName()));
                        TextView jr_date = (TextView) jr_date_container.getChildAt(0);
                        jr_date.getLayoutParams().height = 0;
                        jr_date.getLayoutParams().width = 0;
                        jr_date.setTextAppearance(0);
                        jr_date.setTextColor(0);
                    } catch (Throwable ignored) {
                    }
                }
            });
        } catch (Throwable ignored) {
        }

        try {
            ourResparam.res.hookLayout(SYSTEMUI_PACKAGE, "layout", "quick_status_bar_header_date_privacy", new XC_LayoutInflated() {
                @SuppressLint({"DiscouragedApi"})
                @Override
                public void handleLayoutInflated(XC_LayoutInflated.LayoutInflatedParam liparam) {
                    if (!hideStatusIcons) return;

                    try {
                        @SuppressLint("DiscouragedApi") TextView date = liparam.view.findViewById(liparam.res.getIdentifier("date", "id", mContext.getPackageName()));
                        date.setTextAppearance(0);
                        date.getLayoutParams().height = 0;
                        date.getLayoutParams().width = 0;
                        date.setTextAppearance(0);
                        date.setTextColor(0);
                    } catch (Throwable ignored) {
                    }
                }
            });
        } catch (Throwable ignored) {
        }
    }

    private void fixedStatusIconsA12() {
        if (Build.VERSION.SDK_INT >= 33) return;

        XC_InitPackageResources.InitPackageResourcesParam ourResparam = resparams.get(SYSTEMUI_PACKAGE);
        if (ourResparam == null) return;

        try {
            ourResparam.res.hookLayout(SYSTEMUI_PACKAGE, "layout", "quick_qs_status_icons", new XC_LayoutInflated() {
                @SuppressLint("DiscouragedApi")
                @Override
                public void handleLayoutInflated(XC_LayoutInflated.LayoutInflatedParam liparam) {
                    if (!fixedStatusIcons || hideStatusIcons) return;

                    try {
                        statusIcons = liparam.view.findViewById(liparam.res.getIdentifier("statusIcons", "id", mContext.getPackageName()));
                        LinearLayout batteryRemainingIcon = liparam.view.findViewById(liparam.res.getIdentifier("batteryRemainingIcon", "id", mContext.getPackageName()));

                        if (statusIcons != null) {
                            statusIconContainer = (LinearLayout) statusIcons.getParent();
                            statusIcons.getLayoutParams().height = 0;
                            statusIcons.getLayoutParams().width = 0;
                            statusIcons.setVisibility(View.GONE);
                            statusIcons.requestLayout();
                        }

                        if (batteryRemainingIcon != null) {
                            ((LinearLayout.LayoutParams) batteryRemainingIcon.getLayoutParams()).weight = 0;
                            batteryRemainingIcon.getLayoutParams().height = 0;
                            batteryRemainingIcon.getLayoutParams().width = 0;
                            batteryRemainingIcon.setVisibility(View.GONE);
                            batteryRemainingIcon.requestLayout();
                        }
                    } catch (Throwable ignored) {
                    }
                }
            });
        } catch (Throwable ignored) {
        }

        try {
            ourResparam.res.hookLayout(SYSTEMUI_PACKAGE, "layout", "quick_status_bar_header_date_privacy", new XC_LayoutInflated() {
                @Override
                public void handleLayoutInflated(XC_LayoutInflated.LayoutInflatedParam liparam) {
                    if (!fixedStatusIcons || hideStatusIcons) return;

                    try {
                        @SuppressLint("DiscouragedApi") FrameLayout privacy_container = liparam.view.findViewById(liparam.res.getIdentifier("privacy_container", "id", mContext.getPackageName()));

                        if (statusIconContainer != null && statusIconContainer.getParent() != null && statusIcons != null) {
                            try {
                                ((FrameLayout) statusIconContainer.getParent()).removeView(statusIconContainer);
                            } catch (Throwable ignored) {
                                ((LinearLayout) statusIconContainer.getParent()).removeView(statusIconContainer);
                            }

                            LinearLayout statusIcons = (LinearLayout) statusIconContainer.getChildAt(0);
                            statusIcons.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 28, mContext.getResources().getDisplayMetrics());
                            statusIcons.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
                            statusIcons.setVisibility(View.VISIBLE);
                            statusIcons.requestLayout();

                            LinearLayout batteryRemainingIcon = (LinearLayout) statusIconContainer.getChildAt(1);
                            ((LinearLayout.LayoutParams) batteryRemainingIcon.getLayoutParams()).weight = 1;
                            batteryRemainingIcon.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 28, mContext.getResources().getDisplayMetrics());
                            batteryRemainingIcon.getLayoutParams().width = 0;
                            batteryRemainingIcon.setVisibility(View.VISIBLE);
                            batteryRemainingIcon.requestLayout();

                            statusIconContainer.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 28, mContext.getResources().getDisplayMetrics()), Gravity.END));
                            statusIconContainer.setGravity(Gravity.CENTER);
                            ((FrameLayout.LayoutParams) statusIconContainer.getLayoutParams()).setMargins(0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, topMarginStatusIcons, mContext.getResources().getDisplayMetrics()), 0, 0);
                            ((FrameLayout.LayoutParams) statusIconContainer.getLayoutParams()).setMarginEnd((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, sideMarginStatusIcons, mContext.getResources().getDisplayMetrics()));
                            statusIconContainer.requestLayout();

                            privacy_container.addView(statusIconContainer);
                        }
                    } catch (Throwable ignored) {
                    }
                }
            });
        } catch (Throwable ignored) {
        }
    }

    private void hideLockscreenCarrierOrStatusbar() {
        XC_InitPackageResources.InitPackageResourcesParam ourResparam = resparams.get(SYSTEMUI_PACKAGE);
        if (ourResparam == null) return;

        try {
            ourResparam.res.hookLayout(SYSTEMUI_PACKAGE, "layout", "keyguard_status_bar", new XC_LayoutInflated() {
                @Override
                public void handleLayoutInflated(XC_LayoutInflated.LayoutInflatedParam liparam) {
                    if (hideLockscreenCarrier) {
                        try {
                            @SuppressLint("DiscouragedApi") TextView keyguard_carrier_text = liparam.view.findViewById(liparam.res.getIdentifier("keyguard_carrier_text", "id", mContext.getPackageName()));
                            keyguard_carrier_text.getLayoutParams().height = 0;
                            keyguard_carrier_text.setVisibility(View.INVISIBLE);
                            keyguard_carrier_text.requestLayout();
                        } catch (Throwable ignored) {
                        }
                    }
                    if (hideLockscreenStatusbar) {
                        try {
                            @SuppressLint("DiscouragedApi") LinearLayout status_icon_area = liparam.view.findViewById(liparam.res.getIdentifier("status_icon_area", "id", mContext.getPackageName()));
                            status_icon_area.getLayoutParams().height = 0;
                            status_icon_area.setVisibility(View.INVISIBLE);
                            status_icon_area.requestLayout();
                        } catch (Throwable ignored) {
                        }

                        try {
                            @SuppressLint("DiscouragedApi") TextView keyguard_carrier_text = liparam.view.findViewById(liparam.res.getIdentifier("keyguard_carrier_text", "id", mContext.getPackageName()));
                            keyguard_carrier_text.getLayoutParams().height = 0;
                            keyguard_carrier_text.setVisibility(View.INVISIBLE);
                            keyguard_carrier_text.requestLayout();
                        } catch (Throwable ignored) {
                        }
                    }
                }
            });
        } catch (Throwable ignored) {
        }
    }
}
