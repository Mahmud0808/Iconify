package com.drdisagree.iconify.xposed.modules;

import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_CENTERED;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_COLOR_CODE;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_COLOR_SWITCH;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_FONT_SWITCH;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_FONT_TEXT_SCALING;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_LANDSCAPE_SWITCH;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_SIDEMARGIN;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_STYLE;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_SWITCH;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_TEXT_WHITE;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_TOPMARGIN;
import static com.drdisagree.iconify.common.Preferences.ICONIFY_HEADER_CLOCK_TAG;
import static com.drdisagree.iconify.config.XPrefs.Xprefs;
import static com.drdisagree.iconify.xposed.HookRes.resparams;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findClassIfExists;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.drdisagree.iconify.xposed.ModPack;
import com.drdisagree.iconify.xposed.modules.utils.HeaderClockStyles;
import com.drdisagree.iconify.xposed.modules.utils.Helpers;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HeaderClock extends ModPack implements IXposedHookLoadPackage {

    private static final String TAG = "Iconify - " + HeaderClock.class.getSimpleName() + ": ";
    boolean showHeaderClock = false;
    boolean centeredClockView = false;
    boolean hideLandscapeHeaderClock = true;
    LinearLayout mQsClockContainer = new LinearLayout(mContext);

    public HeaderClock(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;

        showHeaderClock = Xprefs.getBoolean(HEADER_CLOCK_SWITCH, false);
        centeredClockView = Xprefs.getBoolean(HEADER_CLOCK_CENTERED, false);
        hideLandscapeHeaderClock = Xprefs.getBoolean(HEADER_CLOCK_LANDSCAPE_SWITCH, true);

        if (Key.length > 0 && (Objects.equals(Key[0], HEADER_CLOCK_SWITCH) || Objects.equals(Key[0], HEADER_CLOCK_COLOR_SWITCH) || Objects.equals(Key[0], HEADER_CLOCK_COLOR_CODE) || Objects.equals(Key[0], HEADER_CLOCK_FONT_SWITCH) || Objects.equals(Key[0], HEADER_CLOCK_SIDEMARGIN) || Objects.equals(Key[0], HEADER_CLOCK_TOPMARGIN) || Objects.equals(Key[0], HEADER_CLOCK_STYLE) || Objects.equals(Key[0], HEADER_CLOCK_CENTERED) || Objects.equals(Key[0], HEADER_CLOCK_TEXT_WHITE) || Objects.equals(Key[0], HEADER_CLOCK_FONT_TEXT_SCALING) || Objects.equals(Key[0], HEADER_CLOCK_LANDSCAPE_SWITCH))) {
            updateClockView();

            if (Objects.equals(Key[0], HEADER_CLOCK_SWITCH)) {
                Helpers.forceReloadUI(mContext);
            }
        }
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        final Class<?> QuickStatusBarHeader = findClass(SYSTEMUI_PACKAGE + ".qs.QuickStatusBarHeader", loadPackageParam.classLoader);

        hookAllMethods(QuickStatusBarHeader, "onFinishInflate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (!showHeaderClock) return;

                FrameLayout mQuickStatusBarHeader = (FrameLayout) param.thisObject;

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                mQsClockContainer.setLayoutParams(layoutParams);
                mQsClockContainer.setVisibility(View.GONE);

                if (mQsClockContainer.getParent() != null) {
                    ((ViewGroup) mQsClockContainer.getParent()).removeView(mQsClockContainer);
                }
                mQuickStatusBarHeader.addView(mQsClockContainer, mQuickStatusBarHeader.getChildCount());

                // Hide stock clock, date and carrier group
                try {
                    View mDateView = (View) getObjectField(param.thisObject, "mDateView");
                    mDateView.getLayoutParams().height = 0;
                    mDateView.getLayoutParams().width = 0;
                    mDateView.setVisibility(View.INVISIBLE);
                } catch (Throwable ignored) {
                }

                try {
                    TextView mClockView = (TextView) getObjectField(param.thisObject, "mClockView");
                    mClockView.setVisibility(View.INVISIBLE);
                    mClockView.setTextAppearance(0);
                    mClockView.setTextColor(0);
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
                    View mQSCarriers = (View) getObjectField(param.thisObject, "mQSCarriers");
                    mQSCarriers.setVisibility(View.INVISIBLE);
                } catch (Throwable ignored) {
                }

                updateClockView();
            }
        });

        hookAllMethods(QuickStatusBarHeader, "updateResources", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                updateClockView();
            }
        });

        if (Build.VERSION.SDK_INT < 33) {
            try {
                XC_InitPackageResources.InitPackageResourcesParam ourResparam = resparams.get(SYSTEMUI_PACKAGE);
                if (ourResparam != null) {
                    ourResparam.res.setReplacement(SYSTEMUI_PACKAGE, "bool", "config_use_large_screen_shade_header", false);
                }
            } catch (Throwable ignored) {
            }
        }

        try {
            Class<?> ShadeHeaderControllerClass = findClassIfExists(SYSTEMUI_PACKAGE + ".shade.LargeScreenShadeHeaderController", loadPackageParam.classLoader);
            if (ShadeHeaderControllerClass == null)
                ShadeHeaderControllerClass = findClass(SYSTEMUI_PACKAGE + ".shade.ShadeHeaderController", loadPackageParam.classLoader);

            hookAllMethods(ShadeHeaderControllerClass, "onInit", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (!showHeaderClock) return;

                    try {
                        TextView clock = (TextView) getObjectField(param.thisObject, "clock");
                        ((ViewGroup) clock.getParent()).removeView(clock);
                    } catch (Throwable ignored) {
                    }

                    try {
                        TextView date = (TextView) getObjectField(param.thisObject, "date");
                        ((ViewGroup) date.getParent()).removeView(date);
                    } catch (Throwable ignored) {
                    }

                    try {
                        LinearLayout qsCarrierGroup = (LinearLayout) getObjectField(param.thisObject, "qsCarrierGroup");
                        ((ViewGroup) qsCarrierGroup.getParent()).removeView(qsCarrierGroup);
                    } catch (Throwable ignored) {
                    }
                }
            });
        } catch (Throwable ignored) {
        }

        hideStockClockDate();

        try {
            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            executor.scheduleAtFixedRate(() -> {
                File Android = new File(Environment.getExternalStorageDirectory() + "/Android");

                if (Android.isDirectory()) {
                    updateClockView();
                    executor.shutdown();
                    executor.shutdownNow();
                }
            }, 0, 5, TimeUnit.SECONDS);
        } catch (Throwable ignored) {
        }
    }

    private void hideStockClockDate() {
        XC_InitPackageResources.InitPackageResourcesParam ourResparam = resparams.get(SYSTEMUI_PACKAGE);
        if (ourResparam == null) return;

        try {
            ourResparam.res.hookLayout(SYSTEMUI_PACKAGE, "layout", "quick_qs_status_icons", new XC_LayoutInflated() {
                @SuppressLint({"DiscouragedApi"})
                @Override
                public void handleLayoutInflated(LayoutInflatedParam liparam) {
                    if (!showHeaderClock) return;

                    // Ricedroid date
                    try {
                        @SuppressLint("DiscouragedApi") TextView date = liparam.view.findViewById(liparam.res.getIdentifier("date", "id", mContext.getPackageName()));
                        date.getLayoutParams().height = 0;
                        date.getLayoutParams().width = 0;
                        date.setTextAppearance(0);
                        date.setTextColor(0);
                        date.setVisibility(View.GONE);
                    } catch (Throwable ignored) {
                    }

                    // Nusantara clock
                    try {
                        @SuppressLint("DiscouragedApi") TextView jr_clock = liparam.view.findViewById(liparam.res.getIdentifier("jr_clock", "id", mContext.getPackageName()));
                        jr_clock.getLayoutParams().height = 0;
                        jr_clock.getLayoutParams().width = 0;
                        jr_clock.setTextAppearance(0);
                        jr_clock.setTextColor(0);
                        jr_clock.setVisibility(View.GONE);
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
                        jr_date.setVisibility(View.GONE);
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
                public void handleLayoutInflated(LayoutInflatedParam liparam) {
                    if (!showHeaderClock) return;

                    try {
                        @SuppressLint("DiscouragedApi") TextView date = liparam.view.findViewById(liparam.res.getIdentifier("date", "id", mContext.getPackageName()));
                        date.getLayoutParams().height = 0;
                        date.getLayoutParams().width = 0;
                        date.setTextAppearance(0);
                        date.setTextColor(0);
                        date.setVisibility(View.GONE);
                    } catch (Throwable ignored) {
                    }
                }
            });
        } catch (Throwable ignored) {
        }
    }

    private void updateClockView() {
        if (!showHeaderClock) {
            mQsClockContainer.setVisibility(View.GONE);
            return;
        }

        ViewGroup clockView = HeaderClockStyles.getClock(mContext);
        if (mQsClockContainer.findViewWithTag(ICONIFY_HEADER_CLOCK_TAG) != null) {
            mQsClockContainer.removeView(mQsClockContainer.findViewWithTag(ICONIFY_HEADER_CLOCK_TAG));
        }
        if (clockView != null) {
            if (centeredClockView) {
                mQsClockContainer.setGravity(Gravity.CENTER);
            } else {
                mQsClockContainer.setGravity(Gravity.START);
            }
            clockView.setTag(ICONIFY_HEADER_CLOCK_TAG);
            mQsClockContainer.addView(clockView);
            mQsClockContainer.requestLayout();
        }

        Configuration config = mContext.getResources().getConfiguration();
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE && hideLandscapeHeaderClock) {
            mQsClockContainer.setVisibility(View.GONE);
        } else {
            mQsClockContainer.setVisibility(View.VISIBLE);
        }
    }
}