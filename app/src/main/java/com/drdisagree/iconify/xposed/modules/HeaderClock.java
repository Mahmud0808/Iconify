package com.drdisagree.iconify.xposed.modules;

import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_CENTERED;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_COLOR_CODE_ACCENT1;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_COLOR_CODE_ACCENT2;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_COLOR_CODE_ACCENT3;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_COLOR_CODE_TEXT1;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_COLOR_CODE_TEXT2;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_COLOR_SWITCH;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_FONT_SWITCH;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_FONT_TEXT_SCALING;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_LANDSCAPE_SWITCH;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_SIDEMARGIN;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_STYLE;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_SWITCH;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_TOPMARGIN;
import static com.drdisagree.iconify.common.Preferences.ICONIFY_HEADER_CLOCK_TAG;
import static com.drdisagree.iconify.common.Resources.HEADER_CLOCK_LAYOUT;
import static com.drdisagree.iconify.config.XPrefs.Xprefs;
import static com.drdisagree.iconify.xposed.HookRes.resparams;
import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findClassIfExists;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.AlarmClock;
import android.provider.CalendarContract;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.text.TextUtilsCompat;

import com.drdisagree.iconify.BuildConfig;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.utils.TextUtil;
import com.drdisagree.iconify.xposed.ModPack;
import com.drdisagree.iconify.xposed.modules.utils.Helpers;
import com.drdisagree.iconify.xposed.modules.utils.ViewHelper;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Locale;
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
    private Context appContext;
    boolean showHeaderClock = false;
    boolean centeredClockView = false;
    boolean hideLandscapeHeaderClock = true;
    LinearLayout mQsClockContainer = new LinearLayout(mContext);
    private UserManager mUserManager;
    private Object mActivityStarter;
    private final View.OnClickListener mOnClickListener = v -> {
        String tag = v.getTag().toString();
        if (tag.equals("clock")) {
            onClockClick();
        } else if (tag.equals("date")) {
            onDateClick();
        }
    };

    public HeaderClock(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;

        showHeaderClock = Xprefs.getBoolean(HEADER_CLOCK_SWITCH, false);
        centeredClockView = Xprefs.getBoolean(HEADER_CLOCK_CENTERED, false);
        hideLandscapeHeaderClock = Xprefs.getBoolean(HEADER_CLOCK_LANDSCAPE_SWITCH, true);

        if (Key.length > 0 && (Objects.equals(Key[0], HEADER_CLOCK_SWITCH) ||
                Objects.equals(Key[0], HEADER_CLOCK_COLOR_SWITCH) ||
                Objects.equals(Key[0], HEADER_CLOCK_COLOR_CODE_ACCENT1) ||
                Objects.equals(Key[0], HEADER_CLOCK_COLOR_CODE_ACCENT2) ||
                Objects.equals(Key[0], HEADER_CLOCK_COLOR_CODE_ACCENT3) ||
                Objects.equals(Key[0], HEADER_CLOCK_COLOR_CODE_TEXT1) ||
                Objects.equals(Key[0], HEADER_CLOCK_COLOR_CODE_TEXT2) ||
                Objects.equals(Key[0], HEADER_CLOCK_FONT_SWITCH) ||
                Objects.equals(Key[0], HEADER_CLOCK_SIDEMARGIN) ||
                Objects.equals(Key[0], HEADER_CLOCK_TOPMARGIN) ||
                Objects.equals(Key[0], HEADER_CLOCK_STYLE) ||
                Objects.equals(Key[0], HEADER_CLOCK_CENTERED) ||
                Objects.equals(Key[0], HEADER_CLOCK_FONT_TEXT_SCALING) ||
                Objects.equals(Key[0], HEADER_CLOCK_LANDSCAPE_SWITCH))) {
            updateClockView();
        }
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        initResources(mContext);

        final Class<?> QSSecurityFooterUtilsClass = findClass(SYSTEMUI_PACKAGE + ".qs.QSSecurityFooterUtils", loadPackageParam.classLoader);

        hookAllConstructors(QSSecurityFooterUtilsClass, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                mActivityStarter = getObjectField(param.thisObject, "mActivityStarter");
            }
        });

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

    private void initResources(Context context) {
        try {
            appContext = context.createPackageContext(
                    BuildConfig.APPLICATION_ID,
                    Context.CONTEXT_IGNORE_SECURITY
            );
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        mUserManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
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
        if (mQsClockContainer == null) return;

        if (!showHeaderClock) {
            mQsClockContainer.setVisibility(View.GONE);
            return;
        }

        boolean isClockAdded = mQsClockContainer.findViewWithTag(ICONIFY_HEADER_CLOCK_TAG) != null;

        View clockView = getClockView();

        if (isClockAdded) {
            mQsClockContainer.removeView(mQsClockContainer.findViewWithTag(ICONIFY_HEADER_CLOCK_TAG));
        }

        if (clockView != null) {
            if (centeredClockView) {
                mQsClockContainer.setGravity(Gravity.CENTER);
            } else {
                mQsClockContainer.setGravity(Gravity.START);
            }
            clockView.setTag(ICONIFY_HEADER_CLOCK_TAG);

            TextUtil.convertTextViewsToTitleCase((ViewGroup) clockView);

            mQsClockContainer.addView(clockView);
            modifyClockView(clockView);
            setOnClickListener((ViewGroup) clockView);
        }

        Configuration config = mContext.getResources().getConfiguration();
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE && hideLandscapeHeaderClock) {
            mQsClockContainer.setVisibility(View.GONE);
        } else {
            mQsClockContainer.setVisibility(View.VISIBLE);
        }
    }

    @SuppressLint("DiscouragedApi")
    private View getClockView() {
        LayoutInflater inflater = LayoutInflater.from(appContext);
        int clockStyle = Xprefs.getInt(HEADER_CLOCK_STYLE, 0);

        return inflater.inflate(
                appContext
                        .getResources()
                        .getIdentifier(
                                HEADER_CLOCK_LAYOUT + clockStyle,
                                "layout",
                                BuildConfig.APPLICATION_ID
                        ),
                null
        );
    }

    private void modifyClockView(View clockView) {
        int clockStyle = Xprefs.getInt(HEADER_CLOCK_STYLE, 0);
        boolean customFontEnabled = Xprefs.getBoolean(HEADER_CLOCK_FONT_SWITCH, false);
        float clockScale = (float) (Xprefs.getInt(HEADER_CLOCK_FONT_TEXT_SCALING, 10) / 10.0);
        int sideMargin = Xprefs.getInt(HEADER_CLOCK_SIDEMARGIN, 0);
        int topMargin = Xprefs.getInt(HEADER_CLOCK_TOPMARGIN, 8);
        String customFont = Environment.getExternalStorageDirectory() + "/.iconify_files/headerclock_font.ttf";

        int accent1 = Xprefs.getInt(
                HEADER_CLOCK_COLOR_CODE_ACCENT1,
                ContextCompat.getColor(mContext, android.R.color.system_accent1_300)
        );
        int accent2 = Xprefs.getInt(
                HEADER_CLOCK_COLOR_CODE_ACCENT2,
                ContextCompat.getColor(mContext, android.R.color.system_accent2_300)
        );
        int accent3 = Xprefs.getInt(
                HEADER_CLOCK_COLOR_CODE_ACCENT3,
                ContextCompat.getColor(mContext, android.R.color.system_accent3_300)
        );
        int textPrimary = Xprefs.getInt(
                HEADER_CLOCK_COLOR_CODE_TEXT1,
                Helpers.getColorResCompat(mContext, android.R.attr.textColorPrimary)
        );
        int textInverse = Xprefs.getInt(
                HEADER_CLOCK_COLOR_CODE_TEXT2,
                Helpers.getColorResCompat(mContext, android.R.attr.textColorPrimaryInverse)
        );

        Typeface typeface = null;
        if (customFontEnabled && (new File(customFont).exists()))
            typeface = Typeface.createFromFile(new File(customFont));

        if (TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == View.LAYOUT_DIRECTION_RTL) {
            ViewHelper.setMargins(clockView, mContext, 0, topMargin, sideMargin, 0);
        } else {
            ViewHelper.setMargins(clockView, mContext, sideMargin, topMargin, 0, 0);
        }

        ViewHelper.findViewWithTagAndChangeColor((ViewGroup) clockView, "accent1", accent1);
        ViewHelper.findViewWithTagAndChangeColor((ViewGroup) clockView, "accent2", accent2);
        ViewHelper.findViewWithTagAndChangeColor((ViewGroup) clockView, "accent3", accent3);
        ViewHelper.findViewWithTagAndChangeColor((ViewGroup) clockView, "text1", textPrimary);
        ViewHelper.findViewWithTagAndChangeColor((ViewGroup) clockView, "text2", textInverse);

        if (typeface != null) {
            ViewHelper.applyFontRecursively((ViewGroup) clockView, typeface);
        }

        if (clockScale != 1) {
            ViewHelper.applyTextScalingRecursively((ViewGroup) clockView, clockScale);
        }

        switch (clockStyle) {
            case 6 -> {
                ImageView imageView = clockView.findViewById(R.id.user_profile_image);
                imageView.setImageDrawable(getUserImage());
            }
        }
    }

    @SuppressWarnings("all")
    private Drawable getUserImage() {
        if (mUserManager == null) {
            return appContext.getResources().getDrawable(R.drawable.default_avatar);
        }

        try {
            Method getUserIconMethod = mUserManager.getClass().getMethod("getUserIcon", int.class);
            int userId = (int) UserHandle.class.getDeclaredMethod("myUserId").invoke(null);
            Bitmap bitmapUserIcon = (Bitmap) getUserIconMethod.invoke(mUserManager, userId);
            return new BitmapDrawable(mContext.getResources(), bitmapUserIcon);
        } catch (Throwable throwable) {
            log(TAG + throwable);
            return appContext.getResources().getDrawable(R.drawable.default_avatar);
        }
    }

    private void setOnClickListener(ViewGroup clockView) {
        for (int i = 0; i < clockView.getChildCount(); i++) {
            View child = clockView.getChildAt(i);

            String tag = child.getTag() == null ? "" : child.getTag().toString();
            if (tag.equals("clock") || tag.equals("date")) {
                child.setOnClickListener(mOnClickListener);
            }

            if (child instanceof ViewGroup) {
                setOnClickListener((ViewGroup) child);
            }
        }
    }

    private void onClockClick() {
        if (mActivityStarter == null) return;

        Intent intent = new Intent(AlarmClock.ACTION_SHOW_ALARMS);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP + Intent.FLAG_ACTIVITY_SINGLE_TOP);
        callMethod(mActivityStarter, "postStartActivityDismissingKeyguard", intent, 0);
    }

    private void onDateClick() {
        if (mActivityStarter == null) return;

        Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
        builder.appendPath("time");
        builder.appendPath(Long.toString(System.currentTimeMillis()));
        Intent intent = new Intent(Intent.ACTION_VIEW, builder.build());
        callMethod(mActivityStarter, "postStartActivityDismissingKeyguard", intent, 0);
    }
}