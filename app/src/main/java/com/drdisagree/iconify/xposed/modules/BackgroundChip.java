package com.drdisagree.iconify.xposed.modules;

import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.CHIP_QSSTATUSICONS_STYLE;
import static com.drdisagree.iconify.common.Preferences.CHIP_STATUSBAR_CLOCKBG_STYLE;
import static com.drdisagree.iconify.common.Preferences.FIXED_STATUS_ICONS_SIDEMARGIN;
import static com.drdisagree.iconify.common.Preferences.FIXED_STATUS_ICONS_SWITCH;
import static com.drdisagree.iconify.common.Preferences.FIXED_STATUS_ICONS_TOPMARGIN;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_SWITCH;
import static com.drdisagree.iconify.common.Preferences.HIDE_STATUS_ICONS_SWITCH;
import static com.drdisagree.iconify.common.Preferences.QSPANEL_STATUSICONSBG_SWITCH;
import static com.drdisagree.iconify.common.Preferences.STATUSBAR_CLOCKBG_SWITCH;
import static com.drdisagree.iconify.common.Preferences.STATUSBAR_CLOCK_COLOR_CODE;
import static com.drdisagree.iconify.common.Preferences.STATUSBAR_CLOCK_COLOR_OPTION;
import static com.drdisagree.iconify.config.XPrefs.Xprefs;
import static com.drdisagree.iconify.xposed.HookRes.resparams;
import static com.drdisagree.iconify.xposed.modules.utils.ViewHelper.dp2px;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findClassIfExists;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.res.ResourcesCompat;

import com.drdisagree.iconify.BuildConfig;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.xposed.ModPack;

import java.lang.reflect.Field;
import java.util.Objects;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class BackgroundChip extends ModPack implements IXposedHookLoadPackage {

    private static final String TAG = "Iconify - " + BackgroundChip.class.getSimpleName() + ": ";
    private final LinearLayout mQsStatusIconsContainer = new LinearLayout(mContext);
    boolean mShowSBClockBg = false;
    boolean hideStatusIcons = false;
    boolean mShowQSStatusIconsBg = false;
    boolean showHeaderClock = false;
    int topMarginStatusIcons = 8;
    int sideMarginStatusIcons = 0;
    int QSStatusIconsChipStyle = 0;
    int statusBarClockChipStyle = 0;
    int statusBarClockColorOption = 0;
    int statusBarClockColorCode = Color.WHITE;
    boolean fixedStatusIcons = false;
    private int constraintLayoutId = -1;
    private ViewGroup header = null;
    private View mClockView = null;
    private View mCenterClockView = null;
    private View mRightClockView = null;
    private Class<?> DependencyClass = null;
    private Class<?> DarkIconDispatcherClass = null;
    private XC_LoadPackage.LoadPackageParam mLoadPackageParam = null;

    public BackgroundChip(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;

        mShowSBClockBg = Xprefs.getBoolean(STATUSBAR_CLOCKBG_SWITCH, false);
        mShowQSStatusIconsBg = Xprefs.getBoolean(QSPANEL_STATUSICONSBG_SWITCH, false);
        QSStatusIconsChipStyle = Xprefs.getInt(CHIP_QSSTATUSICONS_STYLE, 0);
        statusBarClockChipStyle = Xprefs.getInt(CHIP_STATUSBAR_CLOCKBG_STYLE, 0);
        statusBarClockColorOption = Xprefs.getInt(STATUSBAR_CLOCK_COLOR_OPTION, 0);
        statusBarClockColorCode = Xprefs.getInt(STATUSBAR_CLOCK_COLOR_CODE, Color.WHITE);
        showHeaderClock = Xprefs.getBoolean(HEADER_CLOCK_SWITCH, false);
        hideStatusIcons = Xprefs.getBoolean(HIDE_STATUS_ICONS_SWITCH, false);
        fixedStatusIcons = Xprefs.getBoolean(FIXED_STATUS_ICONS_SWITCH, false);
        topMarginStatusIcons = Xprefs.getInt(FIXED_STATUS_ICONS_TOPMARGIN, 8);
        sideMarginStatusIcons = Xprefs.getInt(FIXED_STATUS_ICONS_SIDEMARGIN, 0);

        if (Key.length > 0) {
            if (Objects.equals(Key[0], STATUSBAR_CLOCKBG_SWITCH) ||
                    Objects.equals(Key[0], CHIP_STATUSBAR_CLOCKBG_STYLE) ||
                    Objects.equals(Key[0], STATUSBAR_CLOCK_COLOR_OPTION) ||
                    Objects.equals(Key[0], STATUSBAR_CLOCK_COLOR_CODE)
            ) {
                updateStatusBarClock();
            }

            if (Objects.equals(Key[0], QSPANEL_STATUSICONSBG_SWITCH) ||
                    Objects.equals(Key[0], CHIP_STATUSBAR_CLOCKBG_STYLE) ||
                    Objects.equals(Key[0], HEADER_CLOCK_SWITCH) ||
                    Objects.equals(Key[0], HIDE_STATUS_ICONS_SWITCH) ||
                    Objects.equals(Key[0], FIXED_STATUS_ICONS_SWITCH)
            ) {
                setQSStatusIconsBgA12();
            }

            if (Objects.equals(Key[0], CHIP_QSSTATUSICONS_STYLE) ||
                    Objects.equals(Key[0], FIXED_STATUS_ICONS_TOPMARGIN) ||
                    Objects.equals(Key[0], FIXED_STATUS_ICONS_SIDEMARGIN)
            ) {
                updateStatusIcons();
            }
        }
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        mLoadPackageParam = loadPackageParam;

        statusbarClockChip(loadPackageParam);
        statusIconsChip(loadPackageParam);
    }

    @SuppressLint("DiscouragedApi")
    private void statusbarClockChip(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        Class<?> CollapsedStatusBarFragment = findClassIfExists(SYSTEMUI_PACKAGE + ".statusbar.phone.fragment.CollapsedStatusBarFragment", loadPackageParam.classLoader);
        if (CollapsedStatusBarFragment == null)
            CollapsedStatusBarFragment = findClass(SYSTEMUI_PACKAGE + ".statusbar.phone.CollapsedStatusBarFragment", loadPackageParam.classLoader);

        DependencyClass = findClass(SYSTEMUI_PACKAGE + ".Dependency", loadPackageParam.classLoader);
        DarkIconDispatcherClass = findClass(SYSTEMUI_PACKAGE + ".plugins.DarkIconDispatcher", loadPackageParam.classLoader);

        findAndHookMethod(CollapsedStatusBarFragment, "onViewCreated", View.class, Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                try {
                    mClockView = (View) getObjectField(param.thisObject, "mClockView");
                } catch (Throwable t) {
                    try {
                        Object mClockController = getObjectField(param.thisObject, "mClockController");
                        mClockView = (View) callMethod(mClockController, "getClock");
                    } catch (Throwable th) {
                        try {
                            mClockView = (View) getObjectField(param.thisObject, "mLeftClock");
                        } catch (Throwable thr) {
                            log(TAG + thr);
                            mClockView = null;
                        }
                    }
                }

                try {
                    mCenterClockView = (View) getObjectField(param.thisObject, "mCenterClockView");
                } catch (Throwable t) {
                    try {
                        Object mClockController = getObjectField(param.thisObject, "mClockController");
                        mCenterClockView = (View) callMethod(mClockController, "mCenterClockView");
                    } catch (Throwable th) {
                        try {
                            mCenterClockView = (View) getObjectField(param.thisObject, "mCenterClock");
                        } catch (Throwable thr) {
                            try {
                                mCenterClockView = ((LinearLayout) getObjectField(param.thisObject, "mCenterClockLayout")).getChildAt(0);
                            } catch (Throwable thrw) {
                                mCenterClockView = null;
                            }
                        }
                    }
                }

                try {
                    mRightClockView = (View) getObjectField(param.thisObject, "mRightClockView");
                } catch (Throwable t) {
                    try {
                        Object mClockController = getObjectField(param.thisObject, "mClockController");
                        mRightClockView = (View) callMethod(mClockController, "mRightClockView");
                    } catch (Throwable th) {
                        try {
                            mRightClockView = (View) getObjectField(param.thisObject, "mRightClock");
                        } catch (Throwable thr) {
                            mRightClockView = null;
                        }
                    }
                }

                ((ViewGroup) getObjectField(param.thisObject, "mStatusBar")).addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> updateStatusBarClock());

                updateStatusBarClock();

                if (mShowSBClockBg) {
                    try {
                        FrameLayout mStatusBar = (FrameLayout) getObjectField(param.thisObject, "mStatusBar");

                        FrameLayout status_bar_start_side_content = mStatusBar.findViewById(mContext.getResources().getIdentifier("status_bar_start_side_content", "id", mContext.getPackageName()));
                        status_bar_start_side_content.getLayoutParams().height = FrameLayout.LayoutParams.MATCH_PARENT;
                        status_bar_start_side_content.requestLayout();

                        LinearLayout status_bar_start_side_except_heads_up = mStatusBar.findViewById(mContext.getResources().getIdentifier("status_bar_start_side_except_heads_up", "id", mContext.getPackageName()));
                        ((FrameLayout.LayoutParams) status_bar_start_side_except_heads_up.getLayoutParams()).gravity = Gravity.START | Gravity.CENTER;
                        status_bar_start_side_except_heads_up.setGravity(Gravity.START | Gravity.CENTER);
                        status_bar_start_side_except_heads_up.requestLayout();
                    } catch (Throwable throwable) {
                        log(TAG + throwable);
                    }
                }
            }
        });
    }

    @SuppressLint("DiscouragedApi")
    private void statusIconsChip(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        setQSStatusIconsBgA12();
        setQSStatusIconsBgA13Plus(loadPackageParam);
    }

    @SuppressLint("RtlHardcoded")
    private void updateStatusBarClock() {
        if (!mShowSBClockBg) return;

        int clockPaddingStartEnd = dp2px(mContext, 8);
        int clockPaddingTopBottom = dp2px(mContext, 2);

        updateClockView(mClockView, clockPaddingStartEnd, clockPaddingTopBottom, Gravity.LEFT | Gravity.CENTER);
        updateClockView(mCenterClockView, clockPaddingStartEnd, clockPaddingTopBottom, Gravity.CENTER);
        updateClockView(mRightClockView, clockPaddingStartEnd, clockPaddingTopBottom, Gravity.RIGHT | Gravity.CENTER);
    }

    private void updateStatusIcons() {
        if (mQsStatusIconsContainer.getChildCount() == 0) return;

        int paddingTopBottom = dp2px(mContext, 4);
        int paddingStartEnd = dp2px(mContext, 12);

        if (mShowQSStatusIconsBg) {
            setStatusIconsBackgroundChip(mQsStatusIconsContainer);
            mQsStatusIconsContainer.setPadding(paddingStartEnd, paddingTopBottom, paddingStartEnd, paddingTopBottom);
        }

        if (mQsStatusIconsContainer.getLayoutParams() instanceof FrameLayout.LayoutParams) {
            ((FrameLayout.LayoutParams) mQsStatusIconsContainer.getLayoutParams()).setMargins(0, dp2px(mContext, topMarginStatusIcons), 0, 0);
            ((FrameLayout.LayoutParams) mQsStatusIconsContainer.getLayoutParams()).setMarginEnd(dp2px(mContext, sideMarginStatusIcons));
        } else if (mQsStatusIconsContainer.getLayoutParams() instanceof LinearLayout.LayoutParams) {
            ((LinearLayout.LayoutParams) mQsStatusIconsContainer.getLayoutParams()).setMargins(0, dp2px(mContext, topMarginStatusIcons), 0, 0);
            ((LinearLayout.LayoutParams) mQsStatusIconsContainer.getLayoutParams()).setMarginEnd(dp2px(mContext, sideMarginStatusIcons));
        } else if (mLoadPackageParam != null && header != null && constraintLayoutId != -1) {
            try {
                Class<?> ConstraintSetClass = findClass("androidx.constraintlayout.widget.ConstraintSet", mLoadPackageParam.classLoader);
                Object mConstraintSet = ConstraintSetClass.newInstance();

                callMethod(mConstraintSet, "clone", header);
                callMethod(mConstraintSet,
                        "connect",
                        constraintLayoutId,
                        ConstraintSet.TOP,
                        ConstraintSet.PARENT_ID,
                        ConstraintSet.TOP,
                        0);
                callMethod(mConstraintSet,
                        "connect",
                        constraintLayoutId,
                        ConstraintSet.END,
                        ConstraintSet.PARENT_ID,
                        ConstraintSet.END,
                        0);
                callMethod(mConstraintSet, "applyTo", header);

                callMethod(callMethod(mQsStatusIconsContainer, "getLayoutParams"), "setMargins", 0, dp2px(mContext, topMarginStatusIcons), 0, 0);
                callMethod(callMethod(mQsStatusIconsContainer, "getLayoutParams"), "setMarginEnd", dp2px(mContext, sideMarginStatusIcons));
            } catch (Throwable throwable) {
                log(TAG + throwable);
            }
        }

        mQsStatusIconsContainer.requestLayout();

        Configuration config = mContext.getResources().getConfiguration();
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mQsStatusIconsContainer.setVisibility(View.GONE);
        } else {
            mQsStatusIconsContainer.setVisibility(View.VISIBLE);
        }

    }

    private void setSBClockBackgroundChip(View view) {
        try {
            Context pc = mContext.createPackageContext(BuildConfig.APPLICATION_ID, Context.CONTEXT_IGNORE_SECURITY);
            Resources res = pc.getResources();
            Drawable bg = switch (statusBarClockChipStyle) {
                case 0 ->
                        ResourcesCompat.getDrawable(res, R.drawable.chip_status_bar_1, pc.getTheme());
                case 1 ->
                        ResourcesCompat.getDrawable(res, R.drawable.chip_status_bar_2, pc.getTheme());
                case 2 ->
                        ResourcesCompat.getDrawable(res, R.drawable.chip_status_bar_3, pc.getTheme());
                case 3 ->
                        ResourcesCompat.getDrawable(res, R.drawable.chip_status_bar_4, pc.getTheme());
                case 4 ->
                        ResourcesCompat.getDrawable(res, R.drawable.chip_status_bar_5, pc.getTheme());
                case 5 ->
                        ResourcesCompat.getDrawable(res, R.drawable.chip_status_bar_6, pc.getTheme());
                case 6 ->
                        ResourcesCompat.getDrawable(res, R.drawable.chip_status_bar_7, pc.getTheme());
                default -> null;
            };

            if (bg != null) {
                view.setBackground(bg);
            }
        } catch (Throwable throwable) {
            log(TAG + throwable);
        }
    }

    private void setStatusIconsBackgroundChip(LinearLayout layout) {
        try {
            Context pc = mContext.createPackageContext(BuildConfig.APPLICATION_ID, Context.CONTEXT_IGNORE_SECURITY);
            Resources res = pc.getResources();
            Drawable bg = switch (QSStatusIconsChipStyle) {
                case 0 ->
                        ResourcesCompat.getDrawable(res, R.drawable.chip_status_icons_1, pc.getTheme());
                case 1 ->
                        ResourcesCompat.getDrawable(res, R.drawable.chip_status_icons_2, pc.getTheme());
                case 2 ->
                        ResourcesCompat.getDrawable(res, R.drawable.chip_status_icons_3, pc.getTheme());
                case 3 ->
                        ResourcesCompat.getDrawable(res, R.drawable.chip_status_icons_4, pc.getTheme());
                case 4 ->
                        ResourcesCompat.getDrawable(res, R.drawable.chip_status_icons_5, pc.getTheme());
                case 5 ->
                        ResourcesCompat.getDrawable(res, R.drawable.chip_status_icons_6, pc.getTheme());
                default -> null;
            };

            if (bg != null) {
                layout.setBackground(bg);
            }
        } catch (Throwable throwable) {
            log(TAG + throwable);
        }
    }

    private void updateClockView(View clockView, int startEnd, int topBottom, int gravity) {
        if (clockView == null) return;

        clockView.setPadding(startEnd, topBottom, startEnd, topBottom);
        setSBClockBackgroundChip(clockView);

        if (statusBarClockColorOption == 0) {
            ((TextView) clockView).getPaint().setXfermode(null);
            callMethod(callStaticMethod(DependencyClass, "get", DarkIconDispatcherClass), "addDarkReceiver", clockView);
        } else if (statusBarClockColorOption == 1) {
            ((TextView) clockView).getPaint().setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        } else if (statusBarClockColorOption == 2) {
            ((TextView) clockView).getPaint().setXfermode(null);
            callMethod(callStaticMethod(DependencyClass, "get", DarkIconDispatcherClass), "removeDarkReceiver", clockView);
            ((TextView) clockView).setTextColor(statusBarClockColorCode);
        }

        try {
            ((LinearLayout.LayoutParams) clockView.getLayoutParams()).gravity = gravity;
        } catch (Throwable ignored) {
            ((FrameLayout.LayoutParams) clockView.getLayoutParams()).gravity = gravity;
        }

        ((TextView) clockView).setIncludeFontPadding(false);
        clockView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        ((TextView) clockView).setGravity(Gravity.CENTER);
        clockView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        clockView.requestLayout();
    }

    private void setQSStatusIconsBgA12() {
        if (Build.VERSION.SDK_INT >= 33) return;

        XC_InitPackageResources.InitPackageResourcesParam ourResparam = resparams.get(SYSTEMUI_PACKAGE);
        if (ourResparam == null) return;

        ourResparam.res.hookLayout(SYSTEMUI_PACKAGE, "layout", "quick_qs_status_icons", new XC_LayoutInflated() {
            @Override
            public void handleLayoutInflated(XC_LayoutInflated.LayoutInflatedParam liparam) {
                if (!mShowQSStatusIconsBg || hideStatusIcons || fixedStatusIcons) return;

                try {
                    @SuppressLint("DiscouragedApi") LinearLayout statusIcons = liparam.view.findViewById(liparam.res.getIdentifier("statusIcons", "id", mContext.getPackageName()));
                    LinearLayout statusIconContainer = (LinearLayout) statusIcons.getParent();

                    ((FrameLayout.LayoutParams) statusIconContainer.getLayoutParams()).gravity = Gravity.CENTER_VERTICAL | Gravity.END;
                    statusIconContainer.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 28, mContext.getResources().getDisplayMetrics());
                    statusIconContainer.requestLayout();

                    int paddingTopBottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, mContext.getResources().getDisplayMetrics());
                    int paddingStartEnd = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, mContext.getResources().getDisplayMetrics());
                    statusIconContainer.setPadding(paddingStartEnd, paddingTopBottom, paddingStartEnd, paddingTopBottom);

                    setStatusIconsBackgroundChip(statusIconContainer);
                } catch (Throwable throwable) {
                    log(TAG + throwable);
                }
            }
        });

        ourResparam.res.hookLayout(SYSTEMUI_PACKAGE, "layout", "quick_status_bar_header_date_privacy", new XC_LayoutInflated() {
            @Override
            public void handleLayoutInflated(LayoutInflatedParam liparam) {
                if (!mShowQSStatusIconsBg || hideStatusIcons || !fixedStatusIcons) return;

                try {
                    @SuppressLint("DiscouragedApi") LinearLayout statusIcons = liparam.view.findViewById(liparam.res.getIdentifier("statusIcons", "id", mContext.getPackageName()));
                    if (statusIcons != null) {
                        LinearLayout statusIconContainer = (LinearLayout) statusIcons.getParent();

                        int paddingTopBottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, mContext.getResources().getDisplayMetrics());
                        int paddingStartEnd = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, mContext.getResources().getDisplayMetrics());
                        statusIconContainer.setPadding(paddingStartEnd, paddingTopBottom, paddingStartEnd, paddingTopBottom);

                        setStatusIconsBackgroundChip(statusIconContainer);
                    }
                } catch (Throwable throwable) {
                    log(TAG + throwable);
                }
            }
        });
    }

    private void setQSStatusIconsBgA13Plus(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        if (Build.VERSION.SDK_INT < 33) return;

        Class<?> QuickStatusBarHeader = findClass(SYSTEMUI_PACKAGE + ".qs.QuickStatusBarHeader", loadPackageParam.classLoader);

        boolean correctClass = false;
        Field[] fs = QuickStatusBarHeader.getDeclaredFields();
        for (Field f : fs) {
            if (f.getName().equals("mIconContainer")) {
                correctClass = true;
            }
        }

        if (correctClass) {
            hookAllMethods(QuickStatusBarHeader, "onFinishInflate", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if ((!mShowQSStatusIconsBg && !fixedStatusIcons) || hideStatusIcons) return;

                    FrameLayout mQuickStatusBarHeader = (FrameLayout) param.thisObject;
                    LinearLayout mIconContainer = (LinearLayout) getObjectField(param.thisObject, "mIconContainer");
                    LinearLayout mBatteryRemainingIcon = (LinearLayout) getObjectField(param.thisObject, "mBatteryRemainingIcon");

                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                    mQsStatusIconsContainer.setLayoutParams(layoutParams);
                    mQsStatusIconsContainer.setGravity(Gravity.CENTER);
                    mQsStatusIconsContainer.setOrientation(LinearLayout.HORIZONTAL);

                    if (mQsStatusIconsContainer.getParent() != null) {
                        ((ViewGroup) mQsStatusIconsContainer.getParent()).removeView(mQsStatusIconsContainer);
                    }

                    if (mQsStatusIconsContainer.getChildCount() > 0) {
                        mQsStatusIconsContainer.removeAllViews();
                    }

                    layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    ((ViewGroup) mIconContainer.getParent()).removeView(mIconContainer);
                    mIconContainer.setLayoutParams(layoutParams);
                    mIconContainer.getLayoutParams().height = dp2px(mContext, 32);

                    ((ViewGroup) mBatteryRemainingIcon.getParent()).removeView(mBatteryRemainingIcon);
                    mBatteryRemainingIcon.getLayoutParams().height = dp2px(mContext, 32);

                    mQsStatusIconsContainer.addView(mIconContainer);
                    mQsStatusIconsContainer.addView(mBatteryRemainingIcon);

                    mQuickStatusBarHeader.addView(mQsStatusIconsContainer, mQuickStatusBarHeader.getChildCount() - 1);
                    ((FrameLayout.LayoutParams) mQsStatusIconsContainer.getLayoutParams()).gravity = Gravity.TOP | Gravity.END;

                    updateStatusIcons();
                }
            });

            hookAllMethods(QuickStatusBarHeader, "updateResources", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if ((!mShowQSStatusIconsBg && !fixedStatusIcons) || hideStatusIcons) return;

                    updateStatusIcons();
                }
            });
        } else {
            Class<?> ShadeHeaderControllerClass = findClassIfExists(SYSTEMUI_PACKAGE + ".shade.ShadeHeaderController", loadPackageParam.classLoader);
            if (ShadeHeaderControllerClass == null) {
                ShadeHeaderControllerClass = findClass(SYSTEMUI_PACKAGE + ".shade.LargeScreenShadeHeaderController", loadPackageParam.classLoader);
            }

            hookAllMethods(ShadeHeaderControllerClass, "onInit", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if ((!mShowQSStatusIconsBg && !fixedStatusIcons) || hideStatusIcons) return;

                    LinearLayout iconContainer = (LinearLayout) getObjectField(param.thisObject, "iconContainer");
                    LinearLayout batteryIcon = (LinearLayout) getObjectField(param.thisObject, "batteryIcon");
                    header = (ViewGroup) iconContainer.getParent();

                    ConstraintLayout.LayoutParams constraintLayoutParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
                    constraintLayoutId = View.generateViewId();
                    constraintLayoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
                    constraintLayoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;

                    mQsStatusIconsContainer.setLayoutParams(constraintLayoutParams);
                    mQsStatusIconsContainer.setGravity(Gravity.CENTER);
                    mQsStatusIconsContainer.setOrientation(LinearLayout.HORIZONTAL);
                    mQsStatusIconsContainer.setId(constraintLayoutId);

                    if (mQsStatusIconsContainer.getParent() != null) {
                        ((ViewGroup) mQsStatusIconsContainer.getParent()).removeView(mQsStatusIconsContainer);
                    }

                    if (mQsStatusIconsContainer.getChildCount() > 0) {
                        mQsStatusIconsContainer.removeAllViews();
                    }

                    LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    ((ViewGroup) iconContainer.getParent()).removeView(iconContainer);
                    iconContainer.setLayoutParams(linearLayoutParams);
                    iconContainer.getLayoutParams().height = dp2px(mContext, 32);

                    ((ViewGroup) batteryIcon.getParent()).removeView(batteryIcon);
                    batteryIcon.getLayoutParams().height = dp2px(mContext, 32);

                    mQsStatusIconsContainer.addView(iconContainer);
                    mQsStatusIconsContainer.addView(batteryIcon);

                    header.addView(mQsStatusIconsContainer, header.getChildCount() - 1);

                    updateStatusIcons();
                }
            });

            hookAllMethods(ShadeHeaderControllerClass, "updateResources", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if ((!mShowQSStatusIconsBg && !fixedStatusIcons) || hideStatusIcons) return;

                    updateStatusIcons();
                }
            });
        }
    }
}
