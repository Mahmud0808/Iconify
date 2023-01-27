package com.drdisagree.iconify.xposed.mods;

import static com.drdisagree.iconify.common.References.CHIP_QSCLOCK_STYLE;
import static com.drdisagree.iconify.common.References.CHIP_QSDATE_STYLE;
import static com.drdisagree.iconify.common.References.CHIP_QSSTATUSICONS_STYLE;
import static com.drdisagree.iconify.common.References.HEADER_CLOCK_SWITCH;
import static com.drdisagree.iconify.common.References.HIDE_STATUS_ICONS_SWITCH;
import static com.drdisagree.iconify.common.References.QSPANEL_CLOCKBG_SWITCH;
import static com.drdisagree.iconify.common.References.QSPANEL_DATEBG_SWITCH;
import static com.drdisagree.iconify.common.References.QSPANEL_HIDE_CARRIER;
import static com.drdisagree.iconify.common.References.QSPANEL_STATUSICONSBG_SWITCH;
import static com.drdisagree.iconify.common.References.STATUSBAR_CLOCKBG_SWITCH;
import static com.drdisagree.iconify.common.References.SYSTEM_UI_PACKAGE;
import static com.drdisagree.iconify.config.XPrefs.Xprefs;
import static com.drdisagree.iconify.xposed.HookRes.resparams;
import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
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

    private static final String TAG = "Iconify - BackgroundChip: ";
    private static final String CLASS_CLOCK = SYSTEM_UI_PACKAGE + ".statusbar.policy.Clock";
    private static final String CollapsedStatusBarFragmentClass = SYSTEM_UI_PACKAGE + ".statusbar.phone.fragment.CollapsedStatusBarFragment";
    boolean mShowSBClockBg = false;
    boolean mShowQSClockBg = false;
    boolean mShowQSDateBg = false;
    boolean hideStatusIcons = false;
    boolean mShowQSStatusIconsBg = false;
    boolean QSCarrierGroupHidden = false;
    private Object mCollapsedStatusBarFragment = null;
    private ViewGroup mStatusBar = null;
    private View mClockView = null;
    private View mCenterClockView = null;
    private View mRightClockView = null;
    boolean showHeaderClock = false;
    int clockWidth = -1;
    int clockHeight = 1;
    int QSStatusIconsChipStyle = 0;
    int QSClockChipStyle = 0;
    int QSDateChipStyle = 0;
    private String rootPackagePath = "";

    public BackgroundChip(Context context) {
        super(context);
        if (!listensTo(context.getPackageName())) return;
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;

        mShowSBClockBg = Xprefs.getBoolean(STATUSBAR_CLOCKBG_SWITCH, false);

        mShowQSClockBg = Xprefs.getBoolean(QSPANEL_CLOCKBG_SWITCH, false);
        QSClockChipStyle = Xprefs.getInt(CHIP_QSCLOCK_STYLE, 0);

        mShowQSDateBg = Xprefs.getBoolean(QSPANEL_DATEBG_SWITCH, false);
        QSDateChipStyle = Xprefs.getInt(CHIP_QSDATE_STYLE, 0);

        mShowQSStatusIconsBg = Xprefs.getBoolean(QSPANEL_STATUSICONSBG_SWITCH, false);
        QSStatusIconsChipStyle = Xprefs.getInt(CHIP_QSSTATUSICONS_STYLE, 0);

        showHeaderClock = Xprefs.getBoolean(HEADER_CLOCK_SWITCH, false);
        QSCarrierGroupHidden = Xprefs.getBoolean(QSPANEL_HIDE_CARRIER, false);
        hideStatusIcons = Xprefs.getBoolean(HIDE_STATUS_ICONS_SWITCH, false);

        updateStatusBarClock();
        setQSClockBg();
        setQSDateBg();
        setQSStatusIconsBg();
        hideQSCarrierGroup();
    }

    @Override
    public boolean listensTo(String packageName) {
        return packageName.equals(SYSTEM_UI_PACKAGE);
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!lpparam.packageName.equals(SYSTEM_UI_PACKAGE))
            return;

        rootPackagePath = lpparam.appInfo.sourceDir;

        final Class<?> Clock = findClass(CLASS_CLOCK, lpparam.classLoader);
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

        hookAllMethods(Clock, "updateClock", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                try {
                    TextView clock = (TextView) param.thisObject;
                    clockWidth = clock.getWidth();
                    clockHeight = clock.getHeight();

                    if (mShowQSClockBg && !hideStatusIcons) {
                        int paddingStartEnd = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, mContext.getResources().getDisplayMetrics());
                        int paddingTopBottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, mContext.getResources().getDisplayMetrics());
                        clock.setPadding(paddingStartEnd, paddingTopBottom, paddingStartEnd, paddingTopBottom);
                    }
                } catch (Throwable t) {
                    log(TAG + t);
                }
            }
        });

        updateStatusBarClock();
    }

    private void updateStatusBarClock() {
        float corner = (Xprefs.getInt("cornerRadius", 16) + 8) * mContext.getResources().getDisplayMetrics().density;
        GradientDrawable mDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{
                        mContext.getResources().getColor(android.R.color.holo_blue_light),
                        mContext.getResources().getColor(android.R.color.holo_green_light)
                });
        mDrawable.setCornerRadius(corner);

        if (mShowSBClockBg && clockWidth != -1 && clockHeight != -1) {
            if (mClockView != null) {
                mClockView.setPadding(12, 2, 12, 2);
                mClockView.setBackground(mDrawable);
            }

            if (mCenterClockView != null) {
                mCenterClockView.setPadding(12, 2, 12, 2);
                mCenterClockView.setBackground(mDrawable);
            }

            if (mRightClockView != null) {
                mRightClockView.setPadding(12, 2, 12, 2);
                mRightClockView.setBackground(mDrawable);
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

        XC_InitPackageResources.InitPackageResourcesParam ourResparam = resparams.get(SYSTEM_UI_PACKAGE);
        if (ourResparam == null || !mShowSBClockBg) return;

        ourResparam.res.hookLayout(SYSTEM_UI_PACKAGE, "layout", "status_bar", new XC_LayoutInflated() {
            @Override
            public void handleLayoutInflated(XC_LayoutInflated.LayoutInflatedParam liparam) {
                try {
                    @SuppressLint("DiscouragedApi") TextView clock = liparam.view.findViewById(liparam.res.getIdentifier("clock", "id", SYSTEM_UI_PACKAGE));
                    ((LinearLayout.LayoutParams) clock.getLayoutParams()).gravity = Gravity.CENTER_VERTICAL | Gravity.START;
                    clock.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, mContext.getResources().getDisplayMetrics());
                    clock.setGravity(Gravity.CENTER_VERTICAL);
                    clock.requestLayout();
                } catch (Throwable t) {
                    log(TAG + t);
                }
            }
        });

        ourResparam.res.hookLayout(SYSTEM_UI_PACKAGE, "layout", "status_bar", new XC_LayoutInflated() {
            @Override
            public void handleLayoutInflated(XC_LayoutInflated.LayoutInflatedParam liparam) {
                try {
                    @SuppressLint("DiscouragedApi") TextView clock_center = liparam.view.findViewById(liparam.res.getIdentifier("clock_center", "id", SYSTEM_UI_PACKAGE));
                    ((LinearLayout.LayoutParams) clock_center.getLayoutParams()).gravity = Gravity.CENTER_VERTICAL | Gravity.START;
                    clock_center.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, mContext.getResources().getDisplayMetrics());
                    clock_center.setGravity(Gravity.CENTER_VERTICAL);
                    clock_center.requestLayout();
                } catch (Throwable ignored) {
                }
            }
        });
    }

    private void setQSClockBg() {
        XC_InitPackageResources.InitPackageResourcesParam ourResparam = resparams.get(SYSTEM_UI_PACKAGE);
        if (ourResparam == null) return;

        if (!mShowQSClockBg || hideStatusIcons)
            return;

        try {
            ourResparam.res.hookLayout(SYSTEM_UI_PACKAGE, "layout", "quick_qs_status_icons", new XC_LayoutInflated() {
                @Override
                public void handleLayoutInflated(LayoutInflatedParam liparam) {
                    @SuppressLint("DiscouragedApi") TextView clock = liparam.view.findViewById(liparam.res.getIdentifier("clock", "id", SYSTEM_UI_PACKAGE));
                    ((LinearLayout.LayoutParams) clock.getLayoutParams()).gravity = Gravity.CENTER_VERTICAL | Gravity.START;
                    clock.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 28, mContext.getResources().getDisplayMetrics());
                    ((LinearLayout.LayoutParams) clock.getLayoutParams()).setMarginEnd((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, mContext.getResources().getDisplayMetrics()));
                    clock.requestLayout();

                    int paddingTopBottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, mContext.getResources().getDisplayMetrics());
                    int paddingStartEnd = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, mContext.getResources().getDisplayMetrics());
                    clock.setPadding(paddingStartEnd, paddingTopBottom, paddingStartEnd, paddingTopBottom);

                    float corner1 = (Xprefs.getInt("cornerRadius", 16) + 8) * mContext.getResources().getDisplayMetrics().density;
                    float corner2 = (Xprefs.getInt("cornerRadius", 16) + 6) * mContext.getResources().getDisplayMetrics().density;
                    float corner3 = (Xprefs.getInt("cornerRadius", 16) + 4) * mContext.getResources().getDisplayMetrics().density;
                    int px2dp2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, mContext.getResources().getDisplayMetrics());
                    int px2dp4 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, mContext.getResources().getDisplayMetrics());

                    GradientDrawable mDrawable1;
                    GradientDrawable mDrawable2;
                    GradientDrawable mDrawable3;
                    LayerDrawable layerDrawable;
                    switch (QSClockChipStyle) {
                        case 0:
                            mDrawable1 = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                                    new int[]{
                                            mContext.getResources().getColor(android.R.color.holo_blue_light),
                                            mContext.getResources().getColor(android.R.color.holo_green_light)
                                    });
                            mDrawable1.setCornerRadius(corner1);

                            clock.setBackground(mDrawable1);
                            break;
                        case 1:
                            mDrawable1 = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                                    new int[]{
                                            mContext.getResources().getColor(android.R.color.holo_blue_light),
                                            mContext.getResources().getColor(android.R.color.holo_green_light)
                                    });
                            mDrawable1.setCornerRadius(corner1);

                            mDrawable2 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                                    new int[]{
                                            Color.parseColor("#FFD4D4D4"),
                                            Color.parseColor("#FFF0F0F0")
                                    });
                            mDrawable2.setCornerRadius(corner2);

                            mDrawable3 = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                    new int[]{
                                            Color.parseColor("#FFD4D4D4"),
                                            Color.parseColor("#FFF0F0F0")
                                    });
                            mDrawable3.setCornerRadius(corner3);
                            clock.setBackground(mDrawable3);

                            layerDrawable = new LayerDrawable(new Drawable[]{
                                    mDrawable1,
                                    mDrawable2,
                                    mDrawable3
                            });
                            layerDrawable.setLayerInset(0, 0, 0, 0, 0);
                            layerDrawable.setLayerInset(1, px2dp2, px2dp2, px2dp2, px2dp2);
                            layerDrawable.setLayerInset(2, px2dp4, px2dp4, px2dp4, px2dp4);

                            clock.setBackground(layerDrawable);
                            break;
                        case 2:
                            mDrawable1 = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                                    new int[]{
                                            mContext.getResources().getColor(android.R.color.holo_blue_light),
                                            mContext.getResources().getColor(android.R.color.holo_green_light)
                                    });
                            mDrawable1.setCornerRadius(corner1);

                            mDrawable2 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                                    new int[]{
                                            Color.parseColor("#FF363636"),
                                            Color.parseColor("#FF0F0F0F")
                                    });
                            mDrawable2.setCornerRadius(corner2);

                            mDrawable3 = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                    new int[]{
                                            Color.parseColor("#FF363636"),
                                            Color.parseColor("#FF0F0F0F")
                                    });
                            mDrawable3.setCornerRadius(corner3);
                            clock.setBackground(mDrawable3);

                            layerDrawable = new LayerDrawable(new Drawable[]{
                                    mDrawable1,
                                    mDrawable2,
                                    mDrawable3
                            });
                            layerDrawable.setLayerInset(0, 0, 0, 0, 0);
                            layerDrawable.setLayerInset(1, px2dp2, px2dp2, px2dp2, px2dp2);
                            layerDrawable.setLayerInset(2, px2dp4, px2dp4, px2dp4, px2dp4);

                            clock.setBackground(layerDrawable);
                            break;
                        case 3:
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

                            layerDrawable = new LayerDrawable(new Drawable[]{
                                    mDrawable1,
                                    mDrawable2
                            });
                            layerDrawable.setLayerInset(0, 0, 0, 0, 0);
                            layerDrawable.setLayerInset(1, px2dp2, px2dp2, px2dp2, px2dp2);

                            clock.setBackground(layerDrawable);
                            break;
                        case 4:
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

                            layerDrawable = new LayerDrawable(new Drawable[]{
                                    mDrawable1,
                                    mDrawable2,
                                    mDrawable3
                            });
                            layerDrawable.setLayerInset(0, 0, 0, 0, 0);
                            layerDrawable.setLayerInset(1, 0, 0, 0, 0);
                            layerDrawable.setLayerInset(2, px2dp2, px2dp2, px2dp2, px2dp2);

                            clock.setBackground(layerDrawable);
                            break;
                    }
                }
            });
        } catch (Throwable t) {
            log(TAG + t);
        }
    }

    private void setQSDateBg() {
        XC_InitPackageResources.InitPackageResourcesParam ourResparam = resparams.get(SYSTEM_UI_PACKAGE);
        if (ourResparam == null) return;

        if (!mShowQSDateBg || hideStatusIcons)
            return;

        try {
            ourResparam.res.hookLayout(SYSTEM_UI_PACKAGE, "layout", "quick_qs_status_icons", new XC_LayoutInflated() {
                @Override
                public void handleLayoutInflated(LayoutInflatedParam liparam) {
                    @SuppressLint("DiscouragedApi") TextView date_clock = liparam.view.findViewById(liparam.res.getIdentifier("date_clock", "id", SYSTEM_UI_PACKAGE));
                    ((LinearLayout.LayoutParams) date_clock.getLayoutParams()).gravity = Gravity.CENTER_VERTICAL | Gravity.START;
                    date_clock.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 28, mContext.getResources().getDisplayMetrics());
                    date_clock.requestLayout();

                    int paddingTopBottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, mContext.getResources().getDisplayMetrics());
                    int paddingStartEnd = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, mContext.getResources().getDisplayMetrics());
                    date_clock.setPadding(paddingStartEnd, paddingTopBottom, paddingStartEnd, paddingTopBottom);

                    float corner1 = (Xprefs.getInt("cornerRadius", 16) + 8) * mContext.getResources().getDisplayMetrics().density;
                    float corner2 = (Xprefs.getInt("cornerRadius", 16) + 6) * mContext.getResources().getDisplayMetrics().density;
                    float corner3 = (Xprefs.getInt("cornerRadius", 16) + 4) * mContext.getResources().getDisplayMetrics().density;
                    int px2dp2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, mContext.getResources().getDisplayMetrics());
                    int px2dp4 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, mContext.getResources().getDisplayMetrics());

                    GradientDrawable mDrawable1;
                    GradientDrawable mDrawable2;
                    GradientDrawable mDrawable3;
                    LayerDrawable layerDrawable;
                    switch (QSDateChipStyle) {
                        case 0:
                            mDrawable1 = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                                    new int[]{
                                            mContext.getResources().getColor(android.R.color.holo_blue_light),
                                            mContext.getResources().getColor(android.R.color.holo_green_light)
                                    });
                            mDrawable1.setCornerRadius(corner1);

                            date_clock.setBackground(mDrawable1);
                            break;
                        case 1:
                            mDrawable1 = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                                    new int[]{
                                            mContext.getResources().getColor(android.R.color.holo_blue_light),
                                            mContext.getResources().getColor(android.R.color.holo_green_light)
                                    });
                            mDrawable1.setCornerRadius(corner1);

                            mDrawable2 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                                    new int[]{
                                            Color.parseColor("#FFD4D4D4"),
                                            Color.parseColor("#FFF0F0F0")
                                    });
                            mDrawable2.setCornerRadius(corner2);

                            mDrawable3 = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                    new int[]{
                                            Color.parseColor("#FFD4D4D4"),
                                            Color.parseColor("#FFF0F0F0")
                                    });
                            mDrawable3.setCornerRadius(corner3);
                            date_clock.setBackground(mDrawable3);

                            layerDrawable = new LayerDrawable(new Drawable[]{
                                    mDrawable1,
                                    mDrawable2,
                                    mDrawable3
                            });
                            layerDrawable.setLayerInset(0, 0, 0, 0, 0);
                            layerDrawable.setLayerInset(1, px2dp2, px2dp2, px2dp2, px2dp2);
                            layerDrawable.setLayerInset(2, px2dp4, px2dp4, px2dp4, px2dp4);

                            date_clock.setBackground(layerDrawable);
                            break;
                        case 2:
                            mDrawable1 = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                                    new int[]{
                                            mContext.getResources().getColor(android.R.color.holo_blue_light),
                                            mContext.getResources().getColor(android.R.color.holo_green_light)
                                    });
                            mDrawable1.setCornerRadius(corner1);

                            mDrawable2 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                                    new int[]{
                                            Color.parseColor("#FF363636"),
                                            Color.parseColor("#FF0F0F0F")
                                    });
                            mDrawable2.setCornerRadius(corner2);

                            mDrawable3 = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                    new int[]{
                                            Color.parseColor("#FF363636"),
                                            Color.parseColor("#FF0F0F0F")
                                    });
                            mDrawable3.setCornerRadius(corner3);
                            date_clock.setBackground(mDrawable3);

                            layerDrawable = new LayerDrawable(new Drawable[]{
                                    mDrawable1,
                                    mDrawable2,
                                    mDrawable3
                            });
                            layerDrawable.setLayerInset(0, 0, 0, 0, 0);
                            layerDrawable.setLayerInset(1, px2dp2, px2dp2, px2dp2, px2dp2);
                            layerDrawable.setLayerInset(2, px2dp4, px2dp4, px2dp4, px2dp4);

                            date_clock.setBackground(layerDrawable);
                            break;
                        case 3:
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

                            layerDrawable = new LayerDrawable(new Drawable[]{
                                    mDrawable1,
                                    mDrawable2
                            });
                            layerDrawable.setLayerInset(0, 0, 0, 0, 0);
                            layerDrawable.setLayerInset(1, px2dp2, px2dp2, px2dp2, px2dp2);

                            date_clock.setBackground(layerDrawable);
                            break;
                        case 4:
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

                            layerDrawable = new LayerDrawable(new Drawable[]{
                                    mDrawable1,
                                    mDrawable2,
                                    mDrawable3
                            });
                            layerDrawable.setLayerInset(0, 0, 0, 0, 0);
                            layerDrawable.setLayerInset(1, 0, 0, 0, 0);
                            layerDrawable.setLayerInset(2, px2dp2, px2dp2, px2dp2, px2dp2);

                            date_clock.setBackground(layerDrawable);
                            break;
                    }
                }
            });
        } catch (Throwable t) {
            log(TAG + t);
        }

        try {
            ourResparam.res.hookLayout(SYSTEM_UI_PACKAGE, "layout", "quick_status_bar_header_date_privacy", new XC_LayoutInflated() {
                @Override
                public void handleLayoutInflated(LayoutInflatedParam liparam) {
                    @SuppressLint("DiscouragedApi") TextView date = liparam.view.findViewById(liparam.res.getIdentifier("date", "id", SYSTEM_UI_PACKAGE));
                    ((FrameLayout.LayoutParams) date.getLayoutParams()).gravity = Gravity.CENTER_VERTICAL | Gravity.START;
                    date.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 28, mContext.getResources().getDisplayMetrics());
                    date.requestLayout();

                    int paddingTopBottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, mContext.getResources().getDisplayMetrics());
                    int paddingStartEnd = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, mContext.getResources().getDisplayMetrics());
                    date.setPadding(paddingStartEnd, paddingTopBottom, paddingStartEnd, paddingTopBottom);

                    float corner1 = (Xprefs.getInt("cornerRadius", 16) + 8) * mContext.getResources().getDisplayMetrics().density;
                    float corner2 = (Xprefs.getInt("cornerRadius", 16) + 6) * mContext.getResources().getDisplayMetrics().density;
                    float corner3 = (Xprefs.getInt("cornerRadius", 16) + 4) * mContext.getResources().getDisplayMetrics().density;
                    int px2dp2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, mContext.getResources().getDisplayMetrics());
                    int px2dp4 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, mContext.getResources().getDisplayMetrics());

                    GradientDrawable mDrawable1;
                    GradientDrawable mDrawable2;
                    GradientDrawable mDrawable3;
                    LayerDrawable layerDrawable;
                    switch (QSDateChipStyle) {
                        case 0:
                            mDrawable1 = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                                    new int[]{
                                            mContext.getResources().getColor(android.R.color.holo_blue_light),
                                            mContext.getResources().getColor(android.R.color.holo_green_light)
                                    });
                            mDrawable1.setCornerRadius(corner1);

                            date.setBackground(mDrawable1);
                            break;
                        case 1:
                            mDrawable1 = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                                    new int[]{
                                            mContext.getResources().getColor(android.R.color.holo_blue_light),
                                            mContext.getResources().getColor(android.R.color.holo_green_light)
                                    });
                            mDrawable1.setCornerRadius(corner1);

                            mDrawable2 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                                    new int[]{
                                            Color.parseColor("#FFD4D4D4"),
                                            Color.parseColor("#FFF0F0F0")
                                    });
                            mDrawable2.setCornerRadius(corner2);

                            mDrawable3 = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                    new int[]{
                                            Color.parseColor("#FFD4D4D4"),
                                            Color.parseColor("#FFF0F0F0")
                                    });
                            mDrawable3.setCornerRadius(corner3);
                            date.setBackground(mDrawable3);

                            layerDrawable = new LayerDrawable(new Drawable[]{
                                    mDrawable1,
                                    mDrawable2,
                                    mDrawable3
                            });
                            layerDrawable.setLayerInset(0, 0, 0, 0, 0);
                            layerDrawable.setLayerInset(1, px2dp2, px2dp2, px2dp2, px2dp2);
                            layerDrawable.setLayerInset(2, px2dp4, px2dp4, px2dp4, px2dp4);

                            date.setBackground(layerDrawable);
                            break;
                        case 2:
                            mDrawable1 = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                                    new int[]{
                                            mContext.getResources().getColor(android.R.color.holo_blue_light),
                                            mContext.getResources().getColor(android.R.color.holo_green_light)
                                    });
                            mDrawable1.setCornerRadius(corner1);

                            mDrawable2 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                                    new int[]{
                                            Color.parseColor("#FF363636"),
                                            Color.parseColor("#FF0F0F0F")
                                    });
                            mDrawable2.setCornerRadius(corner2);

                            mDrawable3 = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                    new int[]{
                                            Color.parseColor("#FF363636"),
                                            Color.parseColor("#FF0F0F0F")
                                    });
                            mDrawable3.setCornerRadius(corner3);
                            date.setBackground(mDrawable3);

                            layerDrawable = new LayerDrawable(new Drawable[]{
                                    mDrawable1,
                                    mDrawable2,
                                    mDrawable3
                            });
                            layerDrawable.setLayerInset(0, 0, 0, 0, 0);
                            layerDrawable.setLayerInset(1, px2dp2, px2dp2, px2dp2, px2dp2);
                            layerDrawable.setLayerInset(2, px2dp4, px2dp4, px2dp4, px2dp4);

                            date.setBackground(layerDrawable);
                            break;
                        case 3:
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

                            layerDrawable = new LayerDrawable(new Drawable[]{
                                    mDrawable1,
                                    mDrawable2
                            });
                            layerDrawable.setLayerInset(0, 0, 0, 0, 0);
                            layerDrawable.setLayerInset(1, px2dp2, px2dp2, px2dp2, px2dp2);

                            date.setBackground(layerDrawable);
                            break;
                        case 4:
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

                            layerDrawable = new LayerDrawable(new Drawable[]{
                                    mDrawable1,
                                    mDrawable2,
                                    mDrawable3
                            });
                            layerDrawable.setLayerInset(0, 0, 0, 0, 0);
                            layerDrawable.setLayerInset(1, 0, 0, 0, 0);
                            layerDrawable.setLayerInset(2, px2dp2, px2dp2, px2dp2, px2dp2);

                            date.setBackground(layerDrawable);
                            break;
                    }
                }
            });
        } catch (Throwable t) {
            log(TAG + t);
        }
    }

    private void setQSStatusIconsBg() {
        XC_InitPackageResources.InitPackageResourcesParam ourResparam = resparams.get(SYSTEM_UI_PACKAGE);
        if (ourResparam == null) return;

        if (!mShowQSStatusIconsBg || hideStatusIcons)
            return;

        try {
            ourResparam.res.hookLayout(SYSTEM_UI_PACKAGE, "layout", "quick_qs_status_icons", new XC_LayoutInflated() {
                @Override
                public void handleLayoutInflated(XC_LayoutInflated.LayoutInflatedParam liparam) {
                    @SuppressLint("DiscouragedApi") FrameLayout rightLayout = liparam.view.findViewById(liparam.res.getIdentifier("rightLayout", "id", SYSTEM_UI_PACKAGE));
                    LinearLayout statusIcons = (LinearLayout) rightLayout.getChildAt(0);
                    ((FrameLayout.LayoutParams) statusIcons.getLayoutParams()).gravity = Gravity.CENTER_VERTICAL | Gravity.END;
                    statusIcons.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 28, mContext.getResources().getDisplayMetrics());
                    statusIcons.requestLayout();

                    int paddingTopBottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, mContext.getResources().getDisplayMetrics());
                    int paddingStartEnd = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, mContext.getResources().getDisplayMetrics());
                    statusIcons.setPadding(paddingStartEnd, paddingTopBottom, paddingStartEnd, paddingTopBottom);

                    float corner1 = (Xprefs.getInt("cornerRadius", 16) + 8) * mContext.getResources().getDisplayMetrics().density;
                    float corner2 = (Xprefs.getInt("cornerRadius", 16) + 6) * mContext.getResources().getDisplayMetrics().density;
                    float corner3 = (Xprefs.getInt("cornerRadius", 16) + 4) * mContext.getResources().getDisplayMetrics().density;
                    int px2dp2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, mContext.getResources().getDisplayMetrics());
                    int px2dp4 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, mContext.getResources().getDisplayMetrics());

                    GradientDrawable mDrawable1;
                    GradientDrawable mDrawable2;
                    GradientDrawable mDrawable3;
                    LayerDrawable layerDrawable;
                    switch (QSStatusIconsChipStyle) {
                        case 0:
                            mDrawable1 = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                                    new int[]{
                                            mContext.getResources().getColor(android.R.color.holo_blue_light),
                                            mContext.getResources().getColor(android.R.color.holo_green_light)
                                    });
                            mDrawable1.setCornerRadius(corner1);

                            statusIcons.setBackground(mDrawable1);
                            break;
                        case 1:
                            mDrawable1 = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                                    new int[]{
                                            mContext.getResources().getColor(android.R.color.holo_blue_light),
                                            mContext.getResources().getColor(android.R.color.holo_green_light)
                                    });
                            mDrawable1.setCornerRadius(corner1);

                            mDrawable2 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                                    new int[]{
                                            Color.parseColor("#FFD4D4D4"),
                                            Color.parseColor("#FFF0F0F0")
                                    });
                            mDrawable2.setCornerRadius(corner2);

                            mDrawable3 = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                    new int[]{
                                            Color.parseColor("#FFD4D4D4"),
                                            Color.parseColor("#FFF0F0F0")
                                    });
                            mDrawable3.setCornerRadius(corner3);
                            statusIcons.setBackground(mDrawable3);

                            layerDrawable = new LayerDrawable(new Drawable[]{
                                    mDrawable1,
                                    mDrawable2,
                                    mDrawable3
                            });
                            layerDrawable.setLayerInset(0, 0, 0, 0, 0);
                            layerDrawable.setLayerInset(1, px2dp2, px2dp2, px2dp2, px2dp2);
                            layerDrawable.setLayerInset(2, px2dp4, px2dp4, px2dp4, px2dp4);

                            statusIcons.setBackground(layerDrawable);
                            break;
                        case 2:
                            mDrawable1 = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                                    new int[]{
                                            mContext.getResources().getColor(android.R.color.holo_blue_light),
                                            mContext.getResources().getColor(android.R.color.holo_green_light)
                                    });
                            mDrawable1.setCornerRadius(corner1);

                            mDrawable2 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                                    new int[]{
                                            Color.parseColor("#FF363636"),
                                            Color.parseColor("#FF0F0F0F")
                                    });
                            mDrawable2.setCornerRadius(corner2);

                            mDrawable3 = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                    new int[]{
                                            Color.parseColor("#FF363636"),
                                            Color.parseColor("#FF0F0F0F")
                                    });
                            mDrawable3.setCornerRadius(corner3);
                            statusIcons.setBackground(mDrawable3);

                            layerDrawable = new LayerDrawable(new Drawable[]{
                                    mDrawable1,
                                    mDrawable2,
                                    mDrawable3
                            });
                            layerDrawable.setLayerInset(0, 0, 0, 0, 0);
                            layerDrawable.setLayerInset(1, px2dp2, px2dp2, px2dp2, px2dp2);
                            layerDrawable.setLayerInset(2, px2dp4, px2dp4, px2dp4, px2dp4);

                            statusIcons.setBackground(layerDrawable);
                            break;
                        case 3:
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

                            layerDrawable = new LayerDrawable(new Drawable[]{
                                    mDrawable1,
                                    mDrawable2
                            });
                            layerDrawable.setLayerInset(0, 0, 0, 0, 0);
                            layerDrawable.setLayerInset(1, px2dp2, px2dp2, px2dp2, px2dp2);

                            statusIcons.setBackground(layerDrawable);
                            break;
                        case 4:
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

                            layerDrawable = new LayerDrawable(new Drawable[]{
                                    mDrawable1,
                                    mDrawable2,
                                    mDrawable3
                            });
                            layerDrawable.setLayerInset(0, 0, 0, 0, 0);
                            layerDrawable.setLayerInset(1, 0, 0, 0, 0);
                            layerDrawable.setLayerInset(2, px2dp2, px2dp2, px2dp2, px2dp2);

                            statusIcons.setBackground(layerDrawable);
                            break;
                    }
                }
            });
        } catch (Throwable t) {
            log(TAG + t);
        }
    }

    private void hideQSCarrierGroup() {
        XC_InitPackageResources.InitPackageResourcesParam ourResparam = resparams.get(SYSTEM_UI_PACKAGE);
        if (ourResparam == null) return;

        if (!QSCarrierGroupHidden)
            return;

        try {
            ourResparam.res.hookLayout(SYSTEM_UI_PACKAGE, "layout", "quick_qs_status_icons", new XC_LayoutInflated() {
                @Override
                public void handleLayoutInflated(XC_LayoutInflated.LayoutInflatedParam liparam) {
                    @SuppressLint("DiscouragedApi") LinearLayout carrier_group = liparam.view.findViewById(liparam.res.getIdentifier("carrier_group", "id", SYSTEM_UI_PACKAGE));
                    carrier_group.getLayoutParams().height = 0;
                    carrier_group.getLayoutParams().width = 0;
                }
            });
        } catch (Throwable t) {
            log(TAG + t);
        }
    }
}
