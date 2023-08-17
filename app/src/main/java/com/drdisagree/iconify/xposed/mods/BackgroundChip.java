package com.drdisagree.iconify.xposed.mods;

import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.CHIP_QSSTATUSICONS_STYLE;
import static com.drdisagree.iconify.common.Preferences.CHIP_STATUSBAR_CLOCKBG_STYLE;
import static com.drdisagree.iconify.common.Preferences.FIXED_STATUS_ICONS_SWITCH;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_SWITCH;
import static com.drdisagree.iconify.common.Preferences.HIDE_STATUS_ICONS_SWITCH;
import static com.drdisagree.iconify.common.Preferences.QSPANEL_STATUSICONSBG_SWITCH;
import static com.drdisagree.iconify.common.Preferences.STATUSBAR_CLOCKBG_SWITCH;
import static com.drdisagree.iconify.common.Preferences.STATUSBAR_CLOCK_COLOR_CODE;
import static com.drdisagree.iconify.common.Preferences.STATUSBAR_CLOCK_COLOR_OPTION;
import static com.drdisagree.iconify.config.XPrefs.Xprefs;
import static com.drdisagree.iconify.xposed.HookRes.resparams;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findClassIfExists;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

import android.annotation.SuppressLint;
import android.content.Context;
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

import androidx.core.content.res.ResourcesCompat;

import com.drdisagree.iconify.BuildConfig;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.xposed.ModPack;

import java.util.Objects;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class BackgroundChip extends ModPack implements IXposedHookLoadPackage {

    private static final String TAG = "Iconify - " + BackgroundChip.class.getSimpleName() + ": ";
    boolean mShowSBClockBg = false;
    boolean hideStatusIcons = false;
    boolean mShowQSStatusIconsBg = false;
    boolean showHeaderClock = false;
    int QSStatusIconsChipStyle = 0;
    int statusBarClockChipStyle = 0;
    int statusBarClockColorOption = 0;
    int statusBarClockColorCode = Color.WHITE;
    boolean fixedStatusIcons = false;
    private View mClockView = null;
    private View mCenterClockView = null;
    private View mRightClockView = null;
    private Class<?> DependencyClass = null;
    private Class<?> DarkIconDispatcherClass = null;

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

        if (Key.length > 0) {
            if (Objects.equals(Key[0], STATUSBAR_CLOCKBG_SWITCH) || Objects.equals(Key[0], CHIP_STATUSBAR_CLOCKBG_STYLE) || Objects.equals(Key[0], STATUSBAR_CLOCK_COLOR_OPTION) || Objects.equals(Key[0], STATUSBAR_CLOCK_COLOR_CODE))
                updateStatusBarClock();

            if (Objects.equals(Key[0], QSPANEL_STATUSICONSBG_SWITCH) || Objects.equals(Key[0], CHIP_STATUSBAR_CLOCKBG_STYLE) || Objects.equals(Key[0], HEADER_CLOCK_SWITCH) || Objects.equals(Key[0], HIDE_STATUS_ICONS_SWITCH) || Objects.equals(Key[0], FIXED_STATUS_ICONS_SWITCH))
                setQSStatusIconsBg();
        }
    }

    @Override
    public boolean listensTo(String packageName) {
        return packageName.equals(SYSTEMUI_PACKAGE);
    }

    @SuppressLint("DiscouragedApi")
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!lpparam.packageName.equals(SYSTEMUI_PACKAGE)) return;

        Class<?> CollapsedStatusBarFragment = findClassIfExists(SYSTEMUI_PACKAGE + ".statusbar.phone.fragment.CollapsedStatusBarFragment", lpparam.classLoader);
        if (CollapsedStatusBarFragment == null)
            CollapsedStatusBarFragment = findClass(SYSTEMUI_PACKAGE + ".statusbar.phone.CollapsedStatusBarFragment", lpparam.classLoader);

        DependencyClass = findClass(SYSTEMUI_PACKAGE + ".Dependency", lpparam.classLoader);
        DarkIconDispatcherClass = findClass(SYSTEMUI_PACKAGE + ".plugins.DarkIconDispatcher", lpparam.classLoader);

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

                ((ViewGroup) getObjectField(param.thisObject, "mStatusBar")).addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                    updateStatusBarClock();
                });

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

        setQSStatusIconsBg();
    }

    private void setQSStatusIconsBg() {
        XC_InitPackageResources.InitPackageResourcesParam ourResparam = resparams.get(SYSTEMUI_PACKAGE);
        if (ourResparam == null) return;

        if (Build.VERSION.SDK_INT < 33) {
            ourResparam.res.hookLayout(SYSTEMUI_PACKAGE, "layout", "quick_qs_status_icons", new XC_LayoutInflated() {
                @Override
                public void handleLayoutInflated(XC_LayoutInflated.LayoutInflatedParam liparam) {
                    if (!mShowQSStatusIconsBg || hideStatusIcons) return;

                    if (!fixedStatusIcons) {
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
                }
            });

            ourResparam.res.hookLayout(SYSTEMUI_PACKAGE, "layout", "quick_status_bar_header_date_privacy", new XC_LayoutInflated() {
                @Override
                public void handleLayoutInflated(LayoutInflatedParam liparam) {
                    if (!mShowQSStatusIconsBg || hideStatusIcons) return;

                    if (fixedStatusIcons) {
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
                }
            });
        }
    }

    @SuppressLint("DiscouragedApi")
    private void updateStatusBarClock() {
        if (!mShowSBClockBg) return;

        int clockPaddingStartEnd = (int) (8 * mContext.getResources().getDisplayMetrics().density);
        int clockPaddingTop = (int) (4 * mContext.getResources().getDisplayMetrics().density);
        int clockPaddingBottom = (int) (3 * mContext.getResources().getDisplayMetrics().density);

        updateClockView(mClockView, clockPaddingStartEnd, clockPaddingTop, clockPaddingStartEnd, clockPaddingBottom, Gravity.START | Gravity.CENTER);
        updateClockView(mCenterClockView, clockPaddingStartEnd, clockPaddingTop, clockPaddingStartEnd, clockPaddingBottom, Gravity.CENTER);
        updateClockView(mRightClockView, clockPaddingStartEnd, clockPaddingTop, clockPaddingStartEnd, clockPaddingBottom, Gravity.END | Gravity.CENTER);
    }

    private void updateClockView(View clockView, int start, int top, int end, int bottom, int gravity) {
        if (clockView != null) {
            clockView.setPadding(start, top, end, bottom);
            setStatusBarBackgroundChip(clockView);

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
            } catch (Throwable t) {
                ((FrameLayout.LayoutParams) clockView.getLayoutParams()).gravity = gravity;
            }

            ((TextView) clockView).setIncludeFontPadding(false);
            clockView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
            clockView.setForegroundGravity(Gravity.CENTER);
        }
    }

    private void setStatusBarBackgroundChip(View view) {
        try {
            Context pc = mContext.createPackageContext(BuildConfig.APPLICATION_ID, Context.CONTEXT_IGNORE_SECURITY);
            Resources res = pc.getResources();
            Drawable bg = null;

            switch (statusBarClockChipStyle) {
                case 0:
                    bg = ResourcesCompat.getDrawable(res, R.drawable.chip_status_bar_1, pc.getTheme());
                    break;
                case 1:
                    bg = ResourcesCompat.getDrawable(res, R.drawable.chip_status_bar_2, pc.getTheme());
                    break;
                case 2:
                    bg = ResourcesCompat.getDrawable(res, R.drawable.chip_status_bar_3, pc.getTheme());
                    break;
                case 3:
                    bg = ResourcesCompat.getDrawable(res, R.drawable.chip_status_bar_4, pc.getTheme());
                    break;
                case 4:
                    bg = ResourcesCompat.getDrawable(res, R.drawable.chip_status_bar_5, pc.getTheme());
                    break;
                case 5:
                    bg = ResourcesCompat.getDrawable(res, R.drawable.chip_status_bar_6, pc.getTheme());
                    break;
                case 6:
                    bg = ResourcesCompat.getDrawable(res, R.drawable.chip_status_bar_7, pc.getTheme());
                    break;
            }

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
            Drawable bg = null;

            switch (QSStatusIconsChipStyle) {
                case 0:
                    bg = ResourcesCompat.getDrawable(res, R.drawable.chip_status_icons_1, pc.getTheme());
                    break;
                case 1:
                    bg = ResourcesCompat.getDrawable(res, R.drawable.chip_status_icons_2, pc.getTheme());
                    break;
                case 2:
                    bg = ResourcesCompat.getDrawable(res, R.drawable.chip_status_icons_3, pc.getTheme());
                    break;
                case 3:
                    bg = ResourcesCompat.getDrawable(res, R.drawable.chip_status_icons_4, pc.getTheme());
                    break;
                case 4:
                    bg = ResourcesCompat.getDrawable(res, R.drawable.chip_status_icons_5, pc.getTheme());
                    break;
                case 5:
                    bg = ResourcesCompat.getDrawable(res, R.drawable.chip_status_icons_6, pc.getTheme());
                    break;
            }

            if (bg != null) {
                layout.setBackground(bg);
            }
        } catch (Throwable throwable) {
            log(TAG + throwable);
        }
    }
}
