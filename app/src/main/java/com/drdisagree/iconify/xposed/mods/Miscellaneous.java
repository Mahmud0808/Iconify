package com.drdisagree.iconify.xposed.mods;

import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.FIXED_STATUS_ICONS_SIDEMARGIN;
import static com.drdisagree.iconify.common.Preferences.FIXED_STATUS_ICONS_SWITCH;
import static com.drdisagree.iconify.common.Preferences.FIXED_STATUS_ICONS_TOPMARGIN;
import static com.drdisagree.iconify.common.Preferences.HIDE_STATUS_ICONS_SWITCH;
import static com.drdisagree.iconify.common.Preferences.QSPANEL_HIDE_CARRIER;
import static com.drdisagree.iconify.config.XPrefs.Xprefs;
import static com.drdisagree.iconify.xposed.HookRes.resparams;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

import android.annotation.SuppressLint;
import android.content.Context;
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

    private static final String TAG = "Iconify - Miscellaneous: ";
    private static final String QuickStatusBarHeaderClass = SYSTEMUI_PACKAGE + ".qs.QuickStatusBarHeader";
    boolean QSCarrierGroupHidden = false;
    boolean hideStatusIcons = false;
    boolean fixedStatusIcons = false;
    int sideMarginStatusIcons = 0;
    int topMarginStatusIcons = 8;
    LinearLayout statusIcons = null;
    LinearLayout statusIconContainer = null;

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

        if (Key.length > 0) {
            if (Objects.equals(Key[0], QSPANEL_HIDE_CARRIER)) hideQSCarrierGroup();

            if (Objects.equals(Key[0], HIDE_STATUS_ICONS_SWITCH)) hideStatusIcons();

            if (Objects.equals(Key[0], FIXED_STATUS_ICONS_SWITCH) || Objects.equals(Key[0], HIDE_STATUS_ICONS_SWITCH) || Objects.equals(Key[0], FIXED_STATUS_ICONS_TOPMARGIN) || Objects.equals(Key[0], FIXED_STATUS_ICONS_SIDEMARGIN))
                fixedStatusIcons();
        }
    }

    @Override
    public boolean listensTo(String packageName) {
        return packageName.equals(SYSTEMUI_PACKAGE);
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!lpparam.packageName.equals(SYSTEMUI_PACKAGE)) return;

        final Class<?> QuickStatusBarHeader = findClass(QuickStatusBarHeaderClass, lpparam.classLoader);

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
                            View mQSCarriers = (View) getObjectField(param.thisObject, "mQSCarriers");
                            mQSCarriers.setVisibility(View.INVISIBLE);
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

                    if (QSCarrierGroupHidden) {
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

        hideQSCarrierGroup();
        hideStatusIcons();
        fixedStatusIcons();
    }

    private void hideQSCarrierGroup() {
        XC_InitPackageResources.InitPackageResourcesParam ourResparam = resparams.get(SYSTEMUI_PACKAGE);
        if (ourResparam == null) return;

        try {
            ourResparam.res.hookLayout(SYSTEMUI_PACKAGE, "layout", "quick_qs_status_icons", new XC_LayoutInflated() {
                @Override
                public void handleLayoutInflated(XC_LayoutInflated.LayoutInflatedParam liparam) {
                    if (!QSCarrierGroupHidden) return;

                    @SuppressLint("DiscouragedApi") LinearLayout carrier_group = liparam.view.findViewById(liparam.res.getIdentifier("carrier_group", "id", SYSTEMUI_PACKAGE));
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
                        @SuppressLint("DiscouragedApi") TextView clock = liparam.view.findViewById(liparam.res.getIdentifier("clock", "id", SYSTEMUI_PACKAGE));
                        clock.getLayoutParams().height = 0;
                        clock.getLayoutParams().width = 0;
                        clock.setTextAppearance(0);
                        clock.setTextColor(0);
                    } catch (Throwable ignored) {
                    }

                    try {
                        @SuppressLint("DiscouragedApi") TextView date_clock = liparam.view.findViewById(liparam.res.getIdentifier("date_clock", "id", SYSTEMUI_PACKAGE));
                        date_clock.getLayoutParams().height = 0;
                        date_clock.getLayoutParams().width = 0;
                        date_clock.setTextAppearance(0);
                        date_clock.setTextColor(0);
                    } catch (Throwable ignored) {
                    }

                    try {
                        @SuppressLint("DiscouragedApi") LinearLayout carrier_group = liparam.view.findViewById(liparam.res.getIdentifier("carrier_group", "id", SYSTEMUI_PACKAGE));
                        carrier_group.getLayoutParams().height = 0;
                        carrier_group.getLayoutParams().width = 0;
                        carrier_group.setMinimumWidth(0);
                        carrier_group.setVisibility(View.INVISIBLE);
                    } catch (Throwable ignored) {
                    }

                    try {
                        @SuppressLint("DiscouragedApi") LinearLayout statusIcons = liparam.view.findViewById(liparam.res.getIdentifier("statusIcons", "id", SYSTEMUI_PACKAGE));
                        statusIcons.getLayoutParams().height = 0;
                        statusIcons.getLayoutParams().width = 0;
                    } catch (Throwable ignored) {
                    }

                    try {
                        @SuppressLint("DiscouragedApi") LinearLayout batteryRemainingIcon = liparam.view.findViewById(liparam.res.getIdentifier("batteryRemainingIcon", "id", SYSTEMUI_PACKAGE));
                        batteryRemainingIcon.getLayoutParams().height = 0;
                        batteryRemainingIcon.getLayoutParams().width = 0;
                    } catch (Throwable ignored) {
                    }

                    try {
                        @SuppressLint("DiscouragedApi") FrameLayout rightLayout = liparam.view.findViewById(liparam.res.getIdentifier("rightLayout", "id", SYSTEMUI_PACKAGE));
                        rightLayout.getLayoutParams().height = 0;
                        rightLayout.getLayoutParams().width = 0;
                        rightLayout.setVisibility(View.INVISIBLE);
                    } catch (Throwable ignored) {
                    }

                    // Ricedroid date
                    try {
                        @SuppressLint("DiscouragedApi") TextView date = liparam.view.findViewById(liparam.res.getIdentifier("date", "id", SYSTEMUI_PACKAGE));
                        date.getLayoutParams().height = 0;
                        date.getLayoutParams().width = 0;
                        date.setTextAppearance(0);
                        date.setTextColor(0);
                    } catch (Throwable ignored) {
                    }

                    // Nusantara clock
                    try {
                        @SuppressLint("DiscouragedApi") TextView jr_clock = liparam.view.findViewById(liparam.res.getIdentifier("jr_clock", "id", SYSTEMUI_PACKAGE));
                        jr_clock.getLayoutParams().height = 0;
                        jr_clock.getLayoutParams().width = 0;
                        jr_clock.setTextAppearance(0);
                        jr_clock.setTextColor(0);
                    } catch (Throwable ignored) {
                    }

                    // Nusantara date
                    try {
                        @SuppressLint("DiscouragedApi") LinearLayout jr_date_container = liparam.view.findViewById(liparam.res.getIdentifier("jr_date_container", "id", SYSTEMUI_PACKAGE));
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
                        @SuppressLint("DiscouragedApi") TextView date = liparam.view.findViewById(liparam.res.getIdentifier("date", "id", SYSTEMUI_PACKAGE));
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

    private void fixedStatusIcons() {
        XC_InitPackageResources.InitPackageResourcesParam ourResparam = resparams.get(SYSTEMUI_PACKAGE);
        if (ourResparam == null) return;

        try {
            ourResparam.res.hookLayout(SYSTEMUI_PACKAGE, "layout", "quick_qs_status_icons", new XC_LayoutInflated() {
                @SuppressLint("DiscouragedApi")
                @Override
                public void handleLayoutInflated(XC_LayoutInflated.LayoutInflatedParam liparam) {
                    if (!fixedStatusIcons || hideStatusIcons) return;

                    try {
                        statusIcons = liparam.view.findViewById(liparam.res.getIdentifier("statusIcons", "id", SYSTEMUI_PACKAGE));
                        LinearLayout batteryRemainingIcon = liparam.view.findViewById(liparam.res.getIdentifier("batteryRemainingIcon", "id", SYSTEMUI_PACKAGE));

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
                        @SuppressLint("DiscouragedApi") FrameLayout privacy_container = liparam.view.findViewById(liparam.res.getIdentifier("privacy_container", "id", SYSTEMUI_PACKAGE));

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
}
