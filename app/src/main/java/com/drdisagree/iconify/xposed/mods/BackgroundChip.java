package com.drdisagree.iconify.xposed.mods;

import static com.drdisagree.iconify.common.References.CHIP_QSSTATUSICONS_STYLE;
import static com.drdisagree.iconify.common.References.CHIP_STATUSBAR_CLOCKBG_STYLE;
import static com.drdisagree.iconify.common.References.FIXED_STATUS_ICONS_SWITCH;
import static com.drdisagree.iconify.common.References.HEADER_CLOCK_SWITCH;
import static com.drdisagree.iconify.common.References.HIDE_STATUS_ICONS_SWITCH;
import static com.drdisagree.iconify.common.References.QSPANEL_STATUSICONSBG_SWITCH;
import static com.drdisagree.iconify.common.References.STATUSBAR_CLOCKBG_SWITCH;
import static com.drdisagree.iconify.common.References.SYSTEMUI_PACKAGE;
import static com.drdisagree.iconify.common.References.UI_CORNER_RADIUS;
import static com.drdisagree.iconify.config.XPrefs.Xprefs;
import static com.drdisagree.iconify.xposed.HookRes.resparams;
import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.drdisagree.iconify.xposed.ModPack;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class BackgroundChip extends ModPack implements IXposedHookLoadPackage {

    private static final String TAG = "Iconify - XposedBackgroundChip: ";
    private static final String CLASS_CLOCK = SYSTEMUI_PACKAGE + ".statusbar.policy.Clock";
    private static final String CollapsedStatusBarFragmentClass = SYSTEMUI_PACKAGE + ".statusbar.phone.fragment.CollapsedStatusBarFragment";
    boolean mShowSBClockBg = false;
    boolean hideStatusIcons = false;
    boolean mShowQSStatusIconsBg = false;
    boolean showHeaderClock = false;
    int QSStatusIconsChipStyle = 0;
    int statusBarClockChipStyle = 0;
    float corner1, corner2, corner3;
    int px2dp2, px2dp4;
    GradientDrawable mDrawable1, mDrawable2, mDrawable3;
    LayerDrawable statusIconsDrawable1, statusIconsDrawable2, statusIconsDrawable3, statusIconsDrawable4, statusIconsDrawable5;
    LayerDrawable statusBarDrawable1, statusBarDrawable2, statusBarDrawable3, statusBarDrawable4, statusBarDrawable5;
    boolean fixedStatusIcons = false;
    private Object mCollapsedStatusBarFragment = null;
    private ViewGroup mStatusBar = null;
    private View mClockView = null;
    private View mCenterClockView = null;
    private View mRightClockView = null;
    private String rootPackagePath = "";

    public BackgroundChip(Context context) {
        super(context);
        if (!listensTo(context.getPackageName())) return;
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;

        mShowSBClockBg = Xprefs.getBoolean(STATUSBAR_CLOCKBG_SWITCH, false);
        mShowQSStatusIconsBg = Xprefs.getBoolean(QSPANEL_STATUSICONSBG_SWITCH, false);
        QSStatusIconsChipStyle = Xprefs.getInt(CHIP_QSSTATUSICONS_STYLE, 0);
        statusBarClockChipStyle = Xprefs.getInt(CHIP_STATUSBAR_CLOCKBG_STYLE, 0);
        showHeaderClock = Xprefs.getBoolean(HEADER_CLOCK_SWITCH, false);
        hideStatusIcons = Xprefs.getBoolean(HIDE_STATUS_ICONS_SWITCH, false);
        fixedStatusIcons = Xprefs.getBoolean(FIXED_STATUS_ICONS_SWITCH, false);

        initStatusBarDrawables();
        updateStatusBarClock();

        initStatusIconsDrawables();
        setQSStatusIconsBg();
    }

    @Override
    public boolean listensTo(String packageName) {
        return packageName.equals(SYSTEMUI_PACKAGE);
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!lpparam.packageName.equals(SYSTEMUI_PACKAGE))
            return;

        rootPackagePath = lpparam.appInfo.sourceDir;

        initStatusBarDrawables();
        initStatusIconsDrawables();

        final Class<?> CollapsedStatusBarFragment = findClass(CollapsedStatusBarFragmentClass, lpparam.classLoader);

        hookAllConstructors(CollapsedStatusBarFragment, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                mCollapsedStatusBarFragment = param.thisObject;
            }
        });

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
                            mCenterClockView = null;
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

                mStatusBar = (ViewGroup) getObjectField(mCollapsedStatusBarFragment, "mStatusBar");

                mStatusBar.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                    updateStatusBarClock();
                });
            }
        });

        updateStatusBarClock();
        setQSStatusIconsBg();
    }

    private void initStatusBarDrawables() {
        corner1 = (Xprefs.getInt(UI_CORNER_RADIUS, 16) + 8) * mContext.getResources().getDisplayMetrics().density;
        corner2 = (Xprefs.getInt(UI_CORNER_RADIUS, 16) + 6) * mContext.getResources().getDisplayMetrics().density;
        corner3 = (Xprefs.getInt(UI_CORNER_RADIUS, 16) + 4) * mContext.getResources().getDisplayMetrics().density;
        px2dp2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, mContext.getResources().getDisplayMetrics());
        px2dp4 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, mContext.getResources().getDisplayMetrics());

        // Status Icons Chip Style 1
        mDrawable1 = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{
                        mContext.getResources().getColor(android.R.color.holo_blue_light),
                        mContext.getResources().getColor(android.R.color.holo_green_light)
                });
        mDrawable1.setCornerRadius(corner1);
        statusBarDrawable1 = new LayerDrawable(new Drawable[]{
                mDrawable1
        });
        statusBarDrawable1.setLayerInset(0, 0, 0, 0, 0);

        // Status Icons Chip Style 2
        mDrawable1 = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{
                        mContext.getResources().getColor(android.R.color.holo_blue_light),
                        mContext.getResources().getColor(android.R.color.holo_green_light)
                });
        mDrawable1.setCornerRadii(new float[]{
                corner1, corner1,
                8 * mContext.getResources().getDisplayMetrics().density, 8 * mContext.getResources().getDisplayMetrics().density,
                corner1, corner1,
                8 * mContext.getResources().getDisplayMetrics().density, 8 * mContext.getResources().getDisplayMetrics().density});
        statusBarDrawable2 = new LayerDrawable(new Drawable[]{
                mDrawable1
        });
        statusBarDrawable2.setLayerInset(0, 0, 0, 0, 0);

        // Status Icons Chip Style 3
        mDrawable1 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{
                        mContext.getResources().getColor(android.R.color.holo_blue_light),
                        mContext.getResources().getColor(android.R.color.holo_green_light)
                });
        mDrawable1.setCornerRadius(corner1);
        mDrawable2 = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                new int[]{
                        mContext.getResources().getColor(android.R.color.holo_blue_light),
                        mContext.getResources().getColor(android.R.color.holo_green_light)
                });
        mDrawable2.setCornerRadius(corner2);
        statusBarDrawable3 = new LayerDrawable(new Drawable[]{
                mDrawable1,
                mDrawable2
        });
        statusBarDrawable3.setLayerInset(0, 0, 0, 0, 0);
        statusBarDrawable3.setLayerInset(1, px2dp2, px2dp2, px2dp2, px2dp2);

        // Status Icons Chip Style 4
        mDrawable1 = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{
                        mContext.getResources().getColor(android.R.color.holo_blue_light),
                        mContext.getResources().getColor(android.R.color.holo_green_light)
                });
        mDrawable1.setCornerRadius(corner1);
        mDrawable2 = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{
                        Color.parseColor("#40000000"),
                        Color.parseColor("#40000000")
                });
        mDrawable2.setCornerRadius(corner2);
        mDrawable3 = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{
                        mContext.getResources().getColor(android.R.color.holo_blue_light),
                        mContext.getResources().getColor(android.R.color.holo_green_light)
                });
        mDrawable3.setCornerRadius(corner3);
        statusBarDrawable4 = new LayerDrawable(new Drawable[]{
                mDrawable1,
                mDrawable2,
                mDrawable3
        });
        statusBarDrawable4.setLayerInset(0, 0, 0, 0, 0);
        statusBarDrawable4.setLayerInset(1, 0, 0, 0, 0);
        statusBarDrawable4.setLayerInset(2, px2dp2, px2dp2, px2dp2, px2dp2);

        // Status Icons Chip Style 5
        mDrawable1 = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{
                        mContext.getResources().getColor(android.R.color.transparent),
                        mContext.getResources().getColor(android.R.color.transparent)
                });
        mDrawable1.setCornerRadius(corner1);
        mDrawable1.setStroke(px2dp2, mContext.getResources().getColor(android.R.color.holo_blue_light));
        statusBarDrawable5 = new LayerDrawable(new Drawable[]{
                mDrawable1
        });
        statusBarDrawable5.setLayerInset(0, 0, 0, 0, 0);
    }

    private void initStatusIconsDrawables() {
        corner1 = (Xprefs.getInt(UI_CORNER_RADIUS, 16) + 8) * mContext.getResources().getDisplayMetrics().density;
        corner2 = (Xprefs.getInt(UI_CORNER_RADIUS, 16) + 6) * mContext.getResources().getDisplayMetrics().density;
        corner3 = (Xprefs.getInt(UI_CORNER_RADIUS, 16) + 4) * mContext.getResources().getDisplayMetrics().density;
        px2dp2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, mContext.getResources().getDisplayMetrics());
        px2dp4 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, mContext.getResources().getDisplayMetrics());

        // Status Icons Chip Style 1
        mDrawable1 = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{
                        mContext.getResources().getColor(android.R.color.holo_blue_light),
                        mContext.getResources().getColor(android.R.color.holo_green_light)
                });
        mDrawable1.setCornerRadius(corner1);
        statusIconsDrawable1 = new LayerDrawable(new Drawable[]{
                mDrawable1
        });
        statusIconsDrawable1.setLayerInset(0, 0, 0, 0, 0);

        // Status Icons Chip Style 2
        mDrawable1 = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{
                        mContext.getResources().getColor(android.R.color.holo_blue_light),
                        mContext.getResources().getColor(android.R.color.holo_green_light)
                });
        mDrawable1.setCornerRadius(corner1);
        mDrawable2 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{
                        mContext.getResources().getColor(android.R.color.holo_red_light),
                        mContext.getResources().getColor(android.R.color.holo_red_dark)
                });
        mDrawable2.setCornerRadius(corner2);
        mDrawable3 = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                new int[]{
                        mContext.getResources().getColor(android.R.color.holo_red_light),
                        mContext.getResources().getColor(android.R.color.holo_red_dark)
                });
        mDrawable3.setCornerRadius(corner3);
        statusIconsDrawable2 = new LayerDrawable(new Drawable[]{
                mDrawable1,
                mDrawable2,
                mDrawable3
        });
        statusIconsDrawable2.setLayerInset(0, 0, 0, 0, 0);
        statusIconsDrawable2.setLayerInset(1, px2dp2, px2dp2, px2dp2, px2dp2);
        statusIconsDrawable2.setLayerInset(2, px2dp4, px2dp4, px2dp4, px2dp4);

        // Status Icons Chip Style 3
        mDrawable1 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{
                        mContext.getResources().getColor(android.R.color.holo_blue_light),
                        mContext.getResources().getColor(android.R.color.holo_green_light)
                });
        mDrawable1.setCornerRadius(corner1);
        mDrawable2 = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                new int[]{
                        mContext.getResources().getColor(android.R.color.holo_blue_light),
                        mContext.getResources().getColor(android.R.color.holo_green_light)
                });
        mDrawable2.setCornerRadius(corner2);
        statusIconsDrawable3 = new LayerDrawable(new Drawable[]{
                mDrawable1,
                mDrawable2
        });
        statusIconsDrawable3.setLayerInset(0, 0, 0, 0, 0);
        statusIconsDrawable3.setLayerInset(1, px2dp2, px2dp2, px2dp2, px2dp2);

        // Status Icons Chip Style 4
        mDrawable1 = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{
                        mContext.getResources().getColor(android.R.color.holo_blue_light),
                        mContext.getResources().getColor(android.R.color.holo_green_light)
                });
        mDrawable1.setCornerRadius(corner1);
        mDrawable2 = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{
                        Color.parseColor("#40000000"),
                        Color.parseColor("#40000000")
                });
        mDrawable2.setCornerRadius(corner2);
        mDrawable3 = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{
                        mContext.getResources().getColor(android.R.color.holo_blue_light),
                        mContext.getResources().getColor(android.R.color.holo_green_light)
                });
        mDrawable3.setCornerRadius(corner3);
        statusIconsDrawable4 = new LayerDrawable(new Drawable[]{
                mDrawable1,
                mDrawable2,
                mDrawable3
        });
        statusIconsDrawable4.setLayerInset(0, 0, 0, 0, 0);
        statusIconsDrawable4.setLayerInset(1, 0, 0, 0, 0);
        statusIconsDrawable4.setLayerInset(2, px2dp2, px2dp2, px2dp2, px2dp2);

        // Status Icons Chip Style 5
        mDrawable1 = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{
                        mContext.getResources().getColor(android.R.color.transparent),
                        mContext.getResources().getColor(android.R.color.transparent)
                });
        mDrawable1.setCornerRadius(corner1);
        mDrawable1.setStroke(px2dp2, mContext.getResources().getColor(android.R.color.holo_blue_light));
        statusIconsDrawable5 = new LayerDrawable(new Drawable[]{
                mDrawable1
        });
        statusIconsDrawable5.setLayerInset(0, 0, 0, 0, 0);
    }

    private void updateStatusBarClock() {
        initStatusBarDrawables();

        if (mShowSBClockBg) {
            if (mClockView != null) {
                mClockView.setPadding(12, 2, 12, 2);
                setStatusBarBackgroundChip(mClockView);
            }

            if (mCenterClockView != null) {
                mCenterClockView.setPadding(12, 2, 12, 2);
                setStatusBarBackgroundChip(mCenterClockView);
            }

            if (mRightClockView != null) {
                mRightClockView.setPadding(12, 2, 12, 2);
                setStatusBarBackgroundChip(mRightClockView);
            }
        } else {
            @SuppressLint("DiscouragedApi") int clockPaddingStart = mContext.getResources().getDimensionPixelSize(mContext.getResources().getIdentifier("status_bar_clock_starting_padding", "dimen", mContext.getPackageName()));
            @SuppressLint("DiscouragedApi") int clockPaddingEnd = mContext.getResources().getDimensionPixelSize(mContext.getResources().getIdentifier("status_bar_clock_end_padding", "dimen", mContext.getPackageName()));
            @SuppressLint("DiscouragedApi") int leftClockPaddingStart = mContext.getResources().getDimensionPixelSize(mContext.getResources().getIdentifier("status_bar_left_clock_starting_padding", "dimen", mContext.getPackageName()));
            @SuppressLint("DiscouragedApi") int leftClockPaddingEnd = mContext.getResources().getDimensionPixelSize(mContext.getResources().getIdentifier("status_bar_left_clock_end_padding", "dimen", mContext.getPackageName()));

            if (mClockView != null) {
                mClockView.setBackgroundResource(0);
                mClockView.setPaddingRelative(leftClockPaddingStart, 0, leftClockPaddingEnd, 0);
            }

            if (mCenterClockView != null) {
                mCenterClockView.setBackgroundResource(0);
                mCenterClockView.setPaddingRelative(0, 0, 0, 0);
            }

            if (mRightClockView != null) {
                mRightClockView.setBackgroundResource(0);
                mRightClockView.setPaddingRelative(clockPaddingStart, 0, clockPaddingEnd, 0);
            }
        }

        XC_InitPackageResources.InitPackageResourcesParam ourResparam = resparams.get(SYSTEMUI_PACKAGE);
        if (ourResparam == null) return;

        ourResparam.res.hookLayout(SYSTEMUI_PACKAGE, "layout", "status_bar", new XC_LayoutInflated() {
            @Override
            public void handleLayoutInflated(XC_LayoutInflated.LayoutInflatedParam liparam) {
                if (!mShowSBClockBg)
                    return;

                try {
                    @SuppressLint("DiscouragedApi") TextView clock = liparam.view.findViewById(liparam.res.getIdentifier("clock", "id", SYSTEMUI_PACKAGE));
                    ((LinearLayout.LayoutParams) clock.getLayoutParams()).gravity = Gravity.CENTER_VERTICAL | Gravity.START;
                    clock.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, mContext.getResources().getDisplayMetrics());
                    clock.setGravity(Gravity.CENTER_VERTICAL);
                    clock.requestLayout();
                } catch (Throwable ignored) {
                }
            }
        });

        ourResparam.res.hookLayout(SYSTEMUI_PACKAGE, "layout", "status_bar", new XC_LayoutInflated() {
            @Override
            public void handleLayoutInflated(XC_LayoutInflated.LayoutInflatedParam liparam) {
                if (!mShowSBClockBg)
                    return;

                try {
                    @SuppressLint("DiscouragedApi") TextView clock_center = liparam.view.findViewById(liparam.res.getIdentifier("clock_center", "id", SYSTEMUI_PACKAGE));
                    ((LinearLayout.LayoutParams) clock_center.getLayoutParams()).gravity = Gravity.CENTER_VERTICAL | Gravity.START;
                    clock_center.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, mContext.getResources().getDisplayMetrics());
                    clock_center.setGravity(Gravity.CENTER_VERTICAL);
                    clock_center.requestLayout();
                } catch (Throwable ignored) {
                }
            }
        });
    }

    private void setQSStatusIconsBg() {
        XC_InitPackageResources.InitPackageResourcesParam ourResparam = resparams.get(SYSTEMUI_PACKAGE);
        if (ourResparam == null) return;

        initStatusIconsDrawables();

        ourResparam.res.hookLayout(SYSTEMUI_PACKAGE, "layout", "quick_qs_status_icons", new XC_LayoutInflated() {
            @Override
            public void handleLayoutInflated(XC_LayoutInflated.LayoutInflatedParam liparam) {
                if (!mShowQSStatusIconsBg || hideStatusIcons)
                    return;

                if (!fixedStatusIcons) {
                    try {
                        @SuppressLint("DiscouragedApi") FrameLayout rightLayout = liparam.view.findViewById(liparam.res.getIdentifier("rightLayout", "id", SYSTEMUI_PACKAGE));
                        LinearLayout statusIcons = (LinearLayout) rightLayout.getChildAt(0);
                        ((FrameLayout.LayoutParams) statusIcons.getLayoutParams()).gravity = Gravity.CENTER_VERTICAL | Gravity.END;
                        statusIcons.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 28, mContext.getResources().getDisplayMetrics());
                        statusIcons.requestLayout();

                        int paddingTopBottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, mContext.getResources().getDisplayMetrics());
                        int paddingStartEnd = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, mContext.getResources().getDisplayMetrics());
                        statusIcons.setPadding(paddingStartEnd, paddingTopBottom, paddingStartEnd, paddingTopBottom);

                        setStatusIconsBackgroundChip(statusIcons);
                    } catch (Throwable ignored) {
                    }
                }
            }
        });

        ourResparam.res.hookLayout(SYSTEMUI_PACKAGE, "layout", "quick_status_bar_header_date_privacy", new XC_LayoutInflated() {
            @Override
            public void handleLayoutInflated(LayoutInflatedParam liparam) {
                if (!mShowQSStatusIconsBg || hideStatusIcons)
                    return;

                if (fixedStatusIcons) {
                    try {
                        @SuppressLint("DiscouragedApi") LinearLayout statusIcons = liparam.view.findViewById(liparam.res.getIdentifier("statusIcons", "id", SYSTEMUI_PACKAGE));
                        if (statusIcons != null) {
                            LinearLayout statusIconContainer = (LinearLayout) statusIcons.getParent();

                            int paddingTopBottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, mContext.getResources().getDisplayMetrics());
                            int paddingStartEnd = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, mContext.getResources().getDisplayMetrics());
                            statusIconContainer.setPadding(paddingStartEnd, paddingTopBottom, paddingStartEnd, paddingTopBottom);

                            setStatusIconsBackgroundChip(statusIconContainer);
                        }
                    } catch (Throwable ignored) {
                    }
                }
            }
        });
    }

    private void setStatusBarBackgroundChip(View view) {
        switch (statusBarClockChipStyle) {
            case 0:
                view.setBackground(statusBarDrawable1);
                break;
            case 1:
                view.setBackground(statusBarDrawable2);
                break;
            case 2:
                view.setBackground(statusBarDrawable3);
                break;
            case 3:
                view.setBackground(statusBarDrawable4);
                break;
            case 4:
                view.setBackground(statusBarDrawable5);
                break;
        }
    }

    private void setStatusIconsBackgroundChip(LinearLayout layout) {
        switch (QSStatusIconsChipStyle) {
            case 0:
                layout.setBackground(statusIconsDrawable1);
                break;
            case 1:
                layout.setBackground(statusIconsDrawable2);
                break;
            case 2:
                layout.setBackground(statusIconsDrawable3);
                break;
            case 3:
                layout.setBackground(statusIconsDrawable4);
                break;
            case 4:
                layout.setBackground(statusIconsDrawable5);
                break;
        }
    }
}
