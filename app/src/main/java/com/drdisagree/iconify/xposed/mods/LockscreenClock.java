package com.drdisagree.iconify.xposed.mods;

import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_AUTOHIDE;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_BOTTOMMARGIN;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_COLOR_CODE;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_COLOR_SWITCH;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_FONT_LINEHEIGHT;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_FONT_SWITCH;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_FONT_TEXT_SCALING;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_STYLE;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_SWITCH;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_TEXT_WHITE;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_TOPMARGIN;
import static com.drdisagree.iconify.config.XPrefs.Xprefs;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Environment;
import android.os.Handler;
import android.text.InputFilter;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AnalogClock;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.drdisagree.iconify.xposed.ModPack;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class LockscreenClock extends ModPack implements IXposedHookLoadPackage {

    private static final String TAG = "Iconify - LockscreenClock: ";
    boolean showLockscreenClock = false;
    boolean autoHideClock = false;
    int topMargin = 100;
    int bottomMargin = 40;
    int lockscreenClockStyle = 0;
    int lineHeight = 0;
    float textScaling = 1;
    boolean customColorEnabled = false;
    int customColorCode = Color.WHITE;
    boolean customFontEnabled = false;
    boolean forceWhiteText = false;
    private ViewGroup mStatusViewContainer = null;
    private FrameLayout mLargeClockFrame = null;

    public LockscreenClock(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;

        showLockscreenClock = Xprefs.getBoolean(LSCLOCK_SWITCH, false);
        autoHideClock = Xprefs.getBoolean(LSCLOCK_AUTOHIDE, false);
        customColorEnabled = Xprefs.getBoolean(LSCLOCK_COLOR_SWITCH, false);
        customColorCode = Xprefs.getInt(LSCLOCK_COLOR_CODE, Color.WHITE);
        lockscreenClockStyle = Xprefs.getInt(LSCLOCK_STYLE, 0);
        topMargin = Xprefs.getInt(LSCLOCK_TOPMARGIN, 100);
        bottomMargin = Xprefs.getInt(LSCLOCK_BOTTOMMARGIN, 40);
        lineHeight = Xprefs.getInt(LSCLOCK_FONT_LINEHEIGHT, 0);
        customFontEnabled = Xprefs.getBoolean(LSCLOCK_FONT_SWITCH, false);
        forceWhiteText = Xprefs.getBoolean(LSCLOCK_TEXT_WHITE, false);
        textScaling = (float) (Xprefs.getInt(LSCLOCK_FONT_TEXT_SCALING, 10) / 10.0);

        if (Key.length > 0 && (Objects.equals(Key[0], LSCLOCK_SWITCH) || Objects.equals(Key[0], LSCLOCK_AUTOHIDE) || Objects.equals(Key[0], LSCLOCK_COLOR_SWITCH) || Objects.equals(Key[0], LSCLOCK_COLOR_CODE) || Objects.equals(Key[0], LSCLOCK_STYLE) || Objects.equals(Key[0], LSCLOCK_TOPMARGIN) || Objects.equals(Key[0], LSCLOCK_BOTTOMMARGIN) || Objects.equals(Key[0], LSCLOCK_FONT_LINEHEIGHT) || Objects.equals(Key[0], LSCLOCK_FONT_SWITCH) || Objects.equals(Key[0], LSCLOCK_TEXT_WHITE) || Objects.equals(Key[0], LSCLOCK_FONT_TEXT_SCALING))) {
            if (!autoHideClock && mStatusViewContainer != null) {
                updateClockView(mStatusViewContainer);
            }

            if (autoHideClock && mLargeClockFrame != null) {
                updateClockView(mLargeClockFrame);
            }
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

        Class<?> KeyguardStatusViewClass = findClass("com.android.keyguard.KeyguardStatusView", lpparam.classLoader);
        Class<?> KeyguardClockSwitchClass = findClass("com.android.keyguard.KeyguardClockSwitch", lpparam.classLoader);

        hookAllMethods(KeyguardClockSwitchClass, "onFinishInflate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (!showLockscreenClock || !autoHideClock) return;

                mLargeClockFrame = (FrameLayout) getObjectField(param.thisObject, "mLargeClockFrame");

                // Add broadcast receiver for updating clock
                IntentFilter filter = new IntentFilter();
                filter.addAction(Intent.ACTION_TIME_TICK);
                filter.addAction(Intent.ACTION_TIME_CHANGED);
                filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
                filter.addAction(Intent.ACTION_LOCALE_CHANGED);

                BroadcastReceiver timeChangedReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        if (mLargeClockFrame != null && intent != null) {
                            (new Handler()).post(() -> {
                                mLargeClockFrame.removeAllViews();
                                updateClockView(mLargeClockFrame);
                            });
                        }
                    }
                };

                mContext.registerReceiver(timeChangedReceiver, filter);
                mLargeClockFrame.removeAllViews();
                updateClockView(mLargeClockFrame);
            }
        });

        hookAllMethods(KeyguardClockSwitchClass, "updateClockViews", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (!showLockscreenClock || !autoHideClock) return;

                View mStatusArea = (View) getObjectField(param.thisObject, "mStatusArea");

                if ((boolean) param.args[0]) {
                    mStatusArea.findViewById(mContext.getResources().getIdentifier("keyguard_slice_view", "id", mContext.getPackageName())).setVisibility(View.INVISIBLE);
                } else {
                    mStatusArea.findViewById(mContext.getResources().getIdentifier("keyguard_slice_view", "id", mContext.getPackageName())).setVisibility(View.VISIBLE);
                }
            }
        });

        hookAllMethods(KeyguardStatusViewClass, "onFinishInflate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (!showLockscreenClock || autoHideClock) return;

                mStatusViewContainer = (ViewGroup) getObjectField(param.thisObject, "mStatusViewContainer");
                GridLayout KeyguardStatusView = (GridLayout) param.thisObject;

                // Hide stock clock
                RelativeLayout mClockView = KeyguardStatusView.findViewById(mContext.getResources().getIdentifier("keyguard_clock_container", "id", mContext.getPackageName()));
                mClockView.getLayoutParams().height = 0;
                mClockView.getLayoutParams().width = 0;
                mClockView.setVisibility(View.INVISIBLE);

                View mMediaHostContainer = (View) getObjectField(param.thisObject, "mMediaHostContainer");
                mMediaHostContainer.getLayoutParams().height = 0;
                mMediaHostContainer.getLayoutParams().width = 0;
                mMediaHostContainer.setVisibility(View.INVISIBLE);

                // Add broadcast receiver for updating clock
                IntentFilter filter = new IntentFilter();
                filter.addAction(Intent.ACTION_TIME_TICK);
                filter.addAction(Intent.ACTION_TIME_CHANGED);
                filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
                filter.addAction(Intent.ACTION_LOCALE_CHANGED);

                BroadcastReceiver timeChangedReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        if (mStatusViewContainer != null && intent != null) {
                            (new Handler()).post(() -> updateClockView(mStatusViewContainer));
                        }
                    }
                };

                mContext.registerReceiver(timeChangedReceiver, filter);
                updateClockView(mStatusViewContainer);
            }
        });
    }

    private void updateClockView(ViewGroup viewGroup) {
        ViewGroup clockView = getClock();
        String clock_tag = "iconify_lockscreen_clock";
        if (viewGroup.findViewWithTag(clock_tag) != null) {
            viewGroup.removeView(viewGroup.findViewWithTag(clock_tag));
        }
        if (clockView != null) {
            clockView.setTag(clock_tag);
            viewGroup.addView(clockView, 0);
            viewGroup.requestLayout();
        }
    }

    @SuppressLint("SetTextI18n")
    private ViewGroup getClock() {
        SimpleDateFormat sdf;
        Typeface typeface = null;

        if (customFontEnabled && (new File(Environment.getExternalStorageDirectory() + "/.iconify_files/lsclock_font.ttf").exists()))
            typeface = Typeface.createFromFile(new File(Environment.getExternalStorageDirectory() + "/.iconify_files/lsclock_font.ttf"));

        if (showLockscreenClock) {
            switch (lockscreenClockStyle) {
                case 0:
                    final LinearLayout blank0 = new LinearLayout(mContext);
                    LinearLayout.LayoutParams blankParams0 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    blankParams0.setMargins(0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, topMargin, mContext.getResources().getDisplayMetrics()), 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, bottomMargin, mContext.getResources().getDisplayMetrics()));
                    blank0.setLayoutParams(blankParams0);
                    blank0.setOrientation(LinearLayout.VERTICAL);

                    return blank0;
                case 1:
                    final TextView date1 = new TextView(mContext);
                    date1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    sdf = new SimpleDateFormat("EEEE d MMMM", Locale.getDefault());
                    date1.setText(sdf.format(new Date()));
                    date1.setTextColor(forceWhiteText ? mContext.getResources().getColor(android.R.color.white, mContext.getTheme()) : (customColorEnabled ? customColorCode : mContext.getResources().getColor(android.R.color.holo_blue_light, mContext.getTheme())));
                    date1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20 * textScaling);
                    date1.setTypeface(typeface != null ? typeface : date1.getTypeface(), Typeface.BOLD);
                    date1.setIncludeFontPadding(false);
                    ViewGroup.MarginLayoutParams dateParams1 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                    dateParams1.setMargins(0, 0, 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -16, mContext.getResources().getDisplayMetrics()));
                    date1.setLayoutParams(dateParams1);

                    final TextView clock1 = new TextView(mContext);
                    clock1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    sdf = new SimpleDateFormat(DateFormat.is24HourFormat(mContext) ? "HH:mm" : "hh:mm", Locale.getDefault());
                    clock1.setText(sdf.format(new Date()));
                    clock1.setTextColor(forceWhiteText ? mContext.getResources().getColor(android.R.color.white, mContext.getTheme()) : (customColorEnabled ? customColorCode : mContext.getResources().getColor(android.R.color.holo_blue_light, mContext.getTheme())));
                    clock1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 100 * textScaling);
                    clock1.setTypeface(typeface != null ? typeface : clock1.getTypeface(), Typeface.BOLD);
                    clock1.setIncludeFontPadding(false);
                    ViewGroup.MarginLayoutParams clockParams1 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                    clockParams1.setMargins(0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6 + lineHeight, mContext.getResources().getDisplayMetrics()), 0, 0);
                    clock1.setLayoutParams(clockParams1);

                    final LinearLayout clockContainer1 = new LinearLayout(mContext);
                    LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams1.gravity = Gravity.CENTER_HORIZONTAL;
                    layoutParams1.setMargins(0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, topMargin, mContext.getResources().getDisplayMetrics()), 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, bottomMargin, mContext.getResources().getDisplayMetrics()));
                    clockContainer1.setLayoutParams(layoutParams1);
                    clockContainer1.setGravity(Gravity.CENTER);
                    clockContainer1.setOrientation(LinearLayout.VERTICAL);

                    clockContainer1.addView(date1);
                    clockContainer1.addView(clock1);

                    return clockContainer1;
                case 2:
                    final TextView day2 = new TextView(mContext);
                    day2.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    sdf = new SimpleDateFormat("EEEE", Locale.getDefault());
                    day2.setText(sdf.format(new Date()));
                    day2.setTextColor(forceWhiteText ? mContext.getResources().getColor(android.R.color.white, mContext.getTheme()) : (customColorEnabled ? customColorCode : mContext.getResources().getColor(android.R.color.system_neutral1_10, mContext.getTheme())));
                    day2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50 * textScaling);
                    day2.setTypeface(typeface != null ? typeface : day2.getTypeface());
                    day2.setIncludeFontPadding(false);

                    final TextView clock2 = new TextView(mContext);
                    clock2.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    sdf = new SimpleDateFormat(DateFormat.is24HourFormat(mContext) ? "HH:mm" : "hh:mm", Locale.getDefault());
                    clock2.setText(sdf.format(new Date()));
                    clock2.setTextColor(forceWhiteText ? mContext.getResources().getColor(android.R.color.white, mContext.getTheme()) : (customColorEnabled ? customColorCode : mContext.getResources().getColor(android.R.color.system_neutral1_10, mContext.getTheme())));
                    clock2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50 * textScaling);
                    clock2.setTypeface(typeface != null ? typeface : clock2.getTypeface());
                    clock2.setIncludeFontPadding(false);

                    final TextView clockOverlay2 = new TextView(mContext);
                    clockOverlay2.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    sdf = new SimpleDateFormat(DateFormat.is24HourFormat(mContext) ? "HH" : "hh", Locale.getDefault());
                    clockOverlay2.setText(sdf.format(new Date()));
                    clockOverlay2.setTextColor(mContext.getResources().getColor(forceWhiteText ? android.R.color.white : android.R.color.holo_blue_light, mContext.getTheme()));
                    clockOverlay2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50 * textScaling);
                    clockOverlay2.setMaxLines(1);
                    clockOverlay2.setTypeface(typeface != null ? typeface : clockOverlay2.getTypeface());
                    clockOverlay2.setIncludeFontPadding(false);
                    int maxLength2 = 1;
                    InputFilter[] fArray2 = new InputFilter[1];
                    fArray2[0] = new InputFilter.LengthFilter(maxLength2);
                    clockOverlay2.setFilters(fArray2);

                    final FrameLayout clockContainer2 = new FrameLayout(mContext);
                    clockContainer2.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    ((FrameLayout.LayoutParams) clockContainer2.getLayoutParams()).setMargins(0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -8 + lineHeight, mContext.getResources().getDisplayMetrics()), 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -8 + lineHeight, mContext.getResources().getDisplayMetrics()));

                    clockContainer2.addView(clock2);
                    clockContainer2.addView(clockOverlay2);

                    final TextView month2 = new TextView(mContext);
                    month2.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    sdf = new SimpleDateFormat("MMMM d", Locale.getDefault());
                    month2.setText(sdf.format(new Date()));
                    month2.setTextColor(forceWhiteText ? mContext.getResources().getColor(android.R.color.white, mContext.getTheme()) : (customColorEnabled ? customColorCode : mContext.getResources().getColor(android.R.color.system_neutral1_10, mContext.getTheme())));
                    month2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20 * textScaling);
                    month2.setTypeface(typeface != null ? typeface : month2.getTypeface());
                    month2.setIncludeFontPadding(false);
                    ViewGroup.MarginLayoutParams monthParams2 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                    monthParams2.setMargins(0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6 + lineHeight, mContext.getResources().getDisplayMetrics()), 0, 0);
                    month2.setLayoutParams(monthParams2);

                    final LinearLayout wholeContainer2 = new LinearLayout(mContext);
                    LinearLayout.LayoutParams layoutparams2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutparams2.gravity = Gravity.START;
                    layoutparams2.setMargins((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, topMargin, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, bottomMargin, mContext.getResources().getDisplayMetrics()));
                    wholeContainer2.setLayoutParams(layoutparams2);
                    wholeContainer2.setGravity(Gravity.START);
                    wholeContainer2.setOrientation(LinearLayout.VERTICAL);

                    wholeContainer2.addView(day2);
                    wholeContainer2.addView(clockContainer2);
                    wholeContainer2.addView(month2);

                    return wholeContainer2;
                case 3:
                    final TextView date3 = new TextView(mContext);
                    date3.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    sdf = new SimpleDateFormat("EEE, MMM dd", Locale.getDefault());
                    date3.setText(sdf.format(new Date()));
                    date3.setTextColor(forceWhiteText ? mContext.getResources().getColor(android.R.color.white, mContext.getTheme()) : (customColorEnabled ? customColorCode : mContext.getResources().getColor(android.R.color.holo_blue_light, mContext.getTheme())));
                    date3.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24 * textScaling);
                    date3.setTypeface(typeface != null ? typeface : date3.getTypeface(), Typeface.BOLD);
                    date3.setIncludeFontPadding(false);

                    final TextView clockHour3 = new TextView(mContext);
                    clockHour3.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    sdf = new SimpleDateFormat(DateFormat.is24HourFormat(mContext) ? "HH" : "hh", Locale.getDefault());
                    clockHour3.setText(sdf.format(new Date()));
                    clockHour3.setTextColor(forceWhiteText ? mContext.getResources().getColor(android.R.color.white, mContext.getTheme()) : (customColorEnabled ? customColorCode : mContext.getResources().getColor(android.R.color.holo_blue_light, mContext.getTheme())));
                    clockHour3.setTextSize(TypedValue.COMPLEX_UNIT_SP, 160 * textScaling);
                    clockHour3.setTypeface(typeface != null ? typeface : clockHour3.getTypeface(), Typeface.BOLD);
                    clockHour3.setIncludeFontPadding(false);
                    ViewGroup.MarginLayoutParams clockHourParams3 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                    clockHourParams3.setMargins(0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -22 + lineHeight, mContext.getResources().getDisplayMetrics()), 0, 0);
                    clockHour3.setLayoutParams(clockHourParams3);

                    final TextView clockMinute3 = new TextView(mContext);
                    clockMinute3.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    sdf = new SimpleDateFormat("mm", Locale.getDefault());
                    clockMinute3.setText(sdf.format(new Date()));
                    clockMinute3.setTextColor(forceWhiteText ? mContext.getResources().getColor(android.R.color.white, mContext.getTheme()) : (customColorEnabled ? customColorCode : mContext.getResources().getColor(android.R.color.holo_blue_light, mContext.getTheme())));
                    clockMinute3.setTextSize(TypedValue.COMPLEX_UNIT_SP, 160 * textScaling);
                    clockMinute3.setTypeface(typeface != null ? typeface : clockMinute3.getTypeface(), Typeface.BOLD);
                    clockMinute3.setIncludeFontPadding(false);
                    ViewGroup.MarginLayoutParams clockMinuteParams3 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                    clockMinuteParams3.setMargins(0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -58 + lineHeight, mContext.getResources().getDisplayMetrics()), 0, 0);
                    clockMinute3.setLayoutParams(clockMinuteParams3);

                    final LinearLayout clockContainer3 = new LinearLayout(mContext);
                    LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams3.gravity = Gravity.CENTER_HORIZONTAL;
                    layoutParams3.setMargins(0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, topMargin, mContext.getResources().getDisplayMetrics()), 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, bottomMargin, mContext.getResources().getDisplayMetrics()));
                    clockContainer3.setLayoutParams(layoutParams3);
                    clockContainer3.setGravity(Gravity.CENTER_HORIZONTAL);
                    clockContainer3.setOrientation(LinearLayout.VERTICAL);

                    clockContainer3.addView(date3);
                    clockContainer3.addView(clockHour3);
                    clockContainer3.addView(clockMinute3);

                    return clockContainer3;
                case 4:
                    final AnalogClock analogClock4 = new AnalogClock(mContext);
                    analogClock4.setLayoutParams(new LinearLayout.LayoutParams((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 180 * textScaling, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 180 * textScaling, mContext.getResources().getDisplayMetrics())));
                    ((LinearLayout.LayoutParams) analogClock4.getLayoutParams()).gravity = Gravity.CENTER_HORIZONTAL;

                    final TextView day4 = new TextView(mContext);
                    day4.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    sdf = new SimpleDateFormat("EEE dd MMM", Locale.getDefault());
                    day4.setText(sdf.format(new Date()));
                    day4.setTextColor(customColorEnabled ? customColorCode : mContext.getResources().getColor(android.R.color.system_neutral1_200, mContext.getTheme()));
                    day4.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28 * textScaling);
                    day4.setTypeface(typeface != null ? typeface : day4.getTypeface(), Typeface.BOLD);
                    day4.setIncludeFontPadding(false);
                    ViewGroup.MarginLayoutParams dayParams4 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                    dayParams4.setMargins(0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6 + lineHeight, mContext.getResources().getDisplayMetrics()), 0, 0);
                    day4.setLayoutParams(dayParams4);

                    final LinearLayout clockContainer4 = new LinearLayout(mContext);
                    LinearLayout.LayoutParams layoutParams4 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams4.gravity = Gravity.CENTER_HORIZONTAL;
                    layoutParams4.setMargins(0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, topMargin, mContext.getResources().getDisplayMetrics()), 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, bottomMargin, mContext.getResources().getDisplayMetrics()));
                    clockContainer4.setLayoutParams(layoutParams4);
                    clockContainer4.setGravity(Gravity.CENTER_HORIZONTAL);
                    clockContainer4.setOrientation(LinearLayout.VERTICAL);

                    clockContainer4.addView(analogClock4);
                    clockContainer4.addView(day4);

                    return clockContainer4;
                case 5:
                    final TextView hour5 = new TextView(mContext);
                    hour5.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    sdf = new SimpleDateFormat(DateFormat.is24HourFormat(mContext) ? "HH" : "hh", Locale.getDefault());
                    hour5.setText(sdf.format(new Date()));
                    hour5.setTextColor(mContext.getResources().getColor(android.R.color.white, mContext.getTheme()));
                    hour5.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40 * textScaling);
                    hour5.setTypeface(typeface != null ? typeface : hour5.getTypeface(), Typeface.BOLD);
                    hour5.setIncludeFontPadding(false);

                    final TextView minute5 = new TextView(mContext);
                    minute5.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    sdf = new SimpleDateFormat("mm", Locale.getDefault());
                    minute5.setText(sdf.format(new Date()));
                    minute5.setTextColor(mContext.getResources().getColor(android.R.color.white, mContext.getTheme()));
                    minute5.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40 * textScaling);
                    minute5.setTypeface(typeface != null ? typeface : minute5.getTypeface(), Typeface.BOLD);
                    minute5.setIncludeFontPadding(false);
                    ViewGroup.MarginLayoutParams minuteParams5 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                    minuteParams5.setMargins(0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4 + lineHeight, mContext.getResources().getDisplayMetrics()), 0, 0);
                    minute5.setLayoutParams(minuteParams5);

                    final LinearLayout time5 = new LinearLayout(mContext);
                    LinearLayout.LayoutParams timeLayoutParams5 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    timeLayoutParams5.gravity = Gravity.CENTER;
                    time5.setLayoutParams(timeLayoutParams5);
                    time5.setOrientation(LinearLayout.VERTICAL);
                    time5.setPadding((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, mContext.getResources().getDisplayMetrics()));
                    GradientDrawable timeDrawable5 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{mContext.getResources().getColor(android.R.color.black, mContext.getTheme()), mContext.getResources().getColor(android.R.color.black, mContext.getTheme())});
                    timeDrawable5.setCornerRadius((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, mContext.getResources().getDisplayMetrics()));
                    time5.setBackground(timeDrawable5);
                    time5.setGravity(Gravity.CENTER);

                    time5.addView(hour5);
                    time5.addView(minute5);

                    final TextView day5 = new TextView(mContext);
                    day5.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    sdf = new SimpleDateFormat("EEE", Locale.getDefault());
                    day5.setText(sdf.format(new Date()));
                    day5.setAllCaps(true);
                    day5.setTextColor(forceWhiteText ? mContext.getResources().getColor(android.R.color.white, mContext.getTheme()) : (customColorEnabled ? customColorCode : mContext.getResources().getColor(android.R.color.system_neutral1_10, mContext.getTheme())));
                    day5.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28 * textScaling);
                    day5.setTypeface(typeface != null ? typeface : day5.getTypeface(), Typeface.BOLD);
                    day5.setIncludeFontPadding(false);
                    day5.setLetterSpacing(0.2f);

                    final TextView date5 = new TextView(mContext);
                    date5.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    sdf = new SimpleDateFormat("dd", Locale.getDefault());
                    date5.setText(sdf.format(new Date()));
                    date5.setTextColor(forceWhiteText ? mContext.getResources().getColor(android.R.color.white, mContext.getTheme()) : (customColorEnabled ? customColorCode : mContext.getResources().getColor(android.R.color.system_neutral1_10, mContext.getTheme())));
                    date5.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28 * textScaling);
                    date5.setTypeface(typeface != null ? typeface : date5.getTypeface(), Typeface.BOLD);
                    date5.setIncludeFontPadding(false);
                    date5.setLetterSpacing(0.2f);
                    ViewGroup.MarginLayoutParams dateParams5 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                    dateParams5.setMargins(0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -2 + lineHeight, mContext.getResources().getDisplayMetrics()), 0, 0);
                    date5.setLayoutParams(dateParams5);

                    final TextView month5 = new TextView(mContext);
                    month5.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    sdf = new SimpleDateFormat("MMM", Locale.getDefault());
                    month5.setText(sdf.format(new Date()));
                    month5.setAllCaps(true);
                    month5.setTextColor(forceWhiteText ? mContext.getResources().getColor(android.R.color.white, mContext.getTheme()) : (customColorEnabled ? customColorCode : mContext.getResources().getColor(android.R.color.system_neutral1_10, mContext.getTheme())));
                    month5.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28 * textScaling);
                    month5.setTypeface(typeface != null ? typeface : month5.getTypeface(), Typeface.BOLD);
                    month5.setIncludeFontPadding(false);
                    month5.setLetterSpacing(0.2f);
                    ViewGroup.MarginLayoutParams monthParams5 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                    monthParams5.setMargins(0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -2 + lineHeight, mContext.getResources().getDisplayMetrics()), 0, 0);
                    month5.setLayoutParams(monthParams5);

                    final LinearLayout right5 = new LinearLayout(mContext);
                    LinearLayout.LayoutParams rightLayoutParams5 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    rightLayoutParams5.gravity = Gravity.CENTER;
                    right5.setLayoutParams(rightLayoutParams5);
                    right5.setOrientation(LinearLayout.VERTICAL);
                    right5.setPadding((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, mContext.getResources().getDisplayMetrics()));
                    right5.setGravity(Gravity.CENTER);

                    right5.addView(day5);
                    right5.addView(date5);
                    right5.addView(month5);

                    final LinearLayout container5 = new LinearLayout(mContext);
                    LinearLayout.LayoutParams layoutParams5 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams5.gravity = Gravity.CENTER_HORIZONTAL;
                    layoutParams5.setMargins((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, topMargin, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, bottomMargin, mContext.getResources().getDisplayMetrics()));
                    container5.setLayoutParams(layoutParams5);
                    container5.setOrientation(LinearLayout.HORIZONTAL);
                    container5.setPadding((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, mContext.getResources().getDisplayMetrics()));
                    GradientDrawable mDrawable5 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{mContext.getResources().getColor(android.R.color.holo_blue_light, mContext.getTheme()), mContext.getResources().getColor(android.R.color.holo_blue_light, mContext.getTheme())});
                    mDrawable5.setCornerRadius((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 28, mContext.getResources().getDisplayMetrics()));
                    container5.setBackground(mDrawable5);

                    container5.addView(time5);
                    container5.addView(right5);

                    return container5;
                case 6:
                    int margin6 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, mContext.getResources().getDisplayMetrics());

                    final TextView day6 = new TextView(mContext);
                    day6.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    sdf = new SimpleDateFormat("EEE", Locale.getDefault());
                    String day6Text = sdf.format(new Date());
                    day6.setText(day6Text.substring(0, 2));
                    day6.setAllCaps(true);
                    day6.setTextColor(mContext.getResources().getColor(android.R.color.white, mContext.getTheme()));
                    day6.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40 * textScaling);
                    day6.setTypeface(typeface != null ? typeface : day6.getTypeface(), Typeface.NORMAL);
                    day6.setIncludeFontPadding(false);

                    final TextView dayText6 = new TextView(mContext);
                    dayText6.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    dayText6.setText("DAY");
                    dayText6.setAllCaps(true);
                    dayText6.setTextColor(mContext.getResources().getColor(android.R.color.white, mContext.getTheme()));
                    dayText6.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14 * textScaling);
                    dayText6.setTypeface(typeface != null ? typeface : dayText6.getTypeface(), Typeface.NORMAL);
                    dayText6.setIncludeFontPadding(false);
                    dayText6.setAlpha(0.4f);
                    ViewGroup.MarginLayoutParams dayTextParams6 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                    dayTextParams6.setMargins(0, 12 + lineHeight, 0, 0);
                    dayText6.setLayoutParams(dayTextParams6);

                    final LinearLayout dayContainer6 = new LinearLayout(mContext);
                    LinearLayout.LayoutParams dayLayoutParams6 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dayLayoutParams6.gravity = Gravity.CENTER;
                    dayLayoutParams6.setMargins(margin6, margin6, margin6, margin6);
                    dayContainer6.setLayoutParams(dayLayoutParams6);
                    dayContainer6.setGravity(Gravity.CENTER);
                    dayContainer6.setOrientation(LinearLayout.VERTICAL);

                    dayContainer6.addView(day6);
                    dayContainer6.addView(dayText6);

                    final TextView hour6 = new TextView(mContext);
                    hour6.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    sdf = new SimpleDateFormat(DateFormat.is24HourFormat(mContext) ? "HH" : "hh", Locale.getDefault());
                    hour6.setText(sdf.format(new Date()));
                    hour6.setAllCaps(true);
                    hour6.setTextColor(mContext.getResources().getColor(android.R.color.white, mContext.getTheme()));
                    hour6.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40 * textScaling);
                    hour6.setTypeface(typeface != null ? typeface : hour6.getTypeface(), Typeface.NORMAL);
                    hour6.setIncludeFontPadding(false);

                    final TextView hourText6 = new TextView(mContext);
                    hourText6.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    hourText6.setText("HOURS");
                    hourText6.setAllCaps(true);
                    hourText6.setTextColor(mContext.getResources().getColor(android.R.color.white, mContext.getTheme()));
                    hourText6.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14 * textScaling);
                    hourText6.setTypeface(typeface != null ? typeface : hourText6.getTypeface(), Typeface.NORMAL);
                    hourText6.setIncludeFontPadding(false);
                    hourText6.setAlpha(0.4f);
                    ViewGroup.MarginLayoutParams hourTextParams6 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                    hourTextParams6.setMargins(0, 12 + lineHeight, 0, 0);
                    hourText6.setLayoutParams(hourTextParams6);

                    final LinearLayout hourContainer6 = new LinearLayout(mContext);
                    LinearLayout.LayoutParams hourLayoutParams6 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    hourLayoutParams6.gravity = Gravity.CENTER;
                    hourLayoutParams6.setMargins(margin6, margin6, margin6, margin6);
                    hourContainer6.setLayoutParams(hourLayoutParams6);
                    hourContainer6.setGravity(Gravity.CENTER);
                    hourContainer6.setOrientation(LinearLayout.VERTICAL);

                    hourContainer6.addView(hour6);
                    hourContainer6.addView(hourText6);

                    final TextView minute6 = new TextView(mContext);
                    minute6.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    sdf = new SimpleDateFormat("mm", Locale.getDefault());
                    minute6.setText(sdf.format(new Date()));
                    minute6.setAllCaps(true);
                    minute6.setTextColor(mContext.getResources().getColor(android.R.color.white, mContext.getTheme()));
                    minute6.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40 * textScaling);
                    minute6.setTypeface(typeface != null ? typeface : minute6.getTypeface(), Typeface.NORMAL);
                    minute6.setIncludeFontPadding(false);

                    final TextView minuteText6 = new TextView(mContext);
                    minuteText6.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    minuteText6.setText("MINUTES");
                    minuteText6.setAllCaps(true);
                    minuteText6.setTextColor(mContext.getResources().getColor(android.R.color.white, mContext.getTheme()));
                    minuteText6.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14 * textScaling);
                    minuteText6.setTypeface(typeface != null ? typeface : minuteText6.getTypeface(), Typeface.NORMAL);
                    minuteText6.setIncludeFontPadding(false);
                    minuteText6.setAlpha(0.4f);
                    ViewGroup.MarginLayoutParams minuteTextParams6 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                    minuteTextParams6.setMargins(0, 12 + lineHeight, 0, 0);
                    minuteText6.setLayoutParams(minuteTextParams6);

                    final LinearLayout minuteContainer6 = new LinearLayout(mContext);
                    LinearLayout.LayoutParams minuteLayoutParams6 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    minuteLayoutParams6.gravity = Gravity.CENTER;
                    minuteLayoutParams6.setMargins(margin6, margin6, margin6, margin6);
                    minuteContainer6.setLayoutParams(minuteLayoutParams6);
                    minuteContainer6.setGravity(Gravity.CENTER);
                    minuteContainer6.setOrientation(LinearLayout.VERTICAL);

                    minuteContainer6.addView(minute6);
                    minuteContainer6.addView(minuteText6);

                    final LinearLayout container6 = new LinearLayout(mContext);
                    LinearLayout.LayoutParams layoutParams6 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams6.gravity = Gravity.CENTER_HORIZONTAL;
                    layoutParams6.setMargins((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, topMargin, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, bottomMargin, mContext.getResources().getDisplayMetrics()));
                    container6.setLayoutParams(layoutParams6);
                    container6.setGravity(Gravity.CENTER);
                    container6.setOrientation(LinearLayout.HORIZONTAL);
                    container6.setPadding(margin6, margin6, margin6, margin6 + (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, mContext.getResources().getDisplayMetrics()));
                    GradientDrawable mDrawable6 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{Color.parseColor("#090909"), Color.parseColor("#090909")});
                    mDrawable6.setCornerRadius((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, mContext.getResources().getDisplayMetrics()));
                    container6.setBackground(mDrawable6);

                    container6.addView(dayContainer6);
                    container6.addView(hourContainer6);
                    container6.addView(minuteContainer6);

                    return container6;
                case 7:
                    final TextView time71 = new TextView(mContext);
                    time71.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    time71.setText("It's");
                    time71.setTextColor(forceWhiteText ? mContext.getResources().getColor(android.R.color.white, mContext.getTheme()) : (customColorEnabled ? customColorCode : mContext.getResources().getColor(android.R.color.system_neutral1_10, mContext.getTheme())));
                    time71.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40 * textScaling);
                    time71.setTypeface(typeface != null ? typeface : time71.getTypeface(), Typeface.NORMAL);
                    time71.setIncludeFontPadding(false);
                    ViewGroup.MarginLayoutParams timeLayoutParams71 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                    timeLayoutParams71.setMargins(0, 12 + lineHeight, 0, 0);
                    time71.setLayoutParams(timeLayoutParams71);

                    final TextView time72 = new TextView(mContext);
                    time72.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    sdf = new SimpleDateFormat(DateFormat.is24HourFormat(mContext) ? "HH" : "hh", Locale.getDefault());
                    time72.setText(getNumericToText(sdf.format(new Date())));
                    time72.setTextColor(forceWhiteText ? mContext.getResources().getColor(android.R.color.white, mContext.getTheme()) : (customColorEnabled ? customColorCode : mContext.getResources().getColor(android.R.color.system_neutral1_10, mContext.getTheme())));
                    time72.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40 * textScaling);
                    time72.setTypeface(typeface != null ? typeface : time72.getTypeface(), Typeface.NORMAL);
                    time72.setIncludeFontPadding(false);
                    ViewGroup.MarginLayoutParams timeLayoutParams72 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                    timeLayoutParams72.setMargins(0, 12 + lineHeight, 0, 0);
                    time71.setLayoutParams(timeLayoutParams72);

                    final TextView time73 = new TextView(mContext);
                    time73.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    sdf = new SimpleDateFormat("mm", Locale.getDefault());
                    time73.setText(getNumericToText(sdf.format(new Date())));
                    time73.setTextColor(forceWhiteText ? mContext.getResources().getColor(android.R.color.white, mContext.getTheme()) : (customColorEnabled ? customColorCode : mContext.getResources().getColor(android.R.color.system_neutral1_10, mContext.getTheme())));
                    time73.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40 * textScaling);
                    time73.setTypeface(typeface != null ? typeface : time73.getTypeface(), Typeface.NORMAL);
                    time73.setIncludeFontPadding(false);
                    ViewGroup.MarginLayoutParams timeLayoutParams73 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                    timeLayoutParams73.setMargins(0, 12 + lineHeight, 0, 0);
                    time71.setLayoutParams(timeLayoutParams73);

                    final TextView date7 = new TextView(mContext);
                    date7.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    sdf = new SimpleDateFormat("EEEE, MMM dd", Locale.getDefault());
                    date7.setText(sdf.format(new Date()));
                    date7.setTextColor(forceWhiteText ? mContext.getResources().getColor(android.R.color.white, mContext.getTheme()) : (customColorEnabled ? customColorCode : mContext.getResources().getColor(android.R.color.system_neutral1_10, mContext.getTheme())));
                    date7.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20 * textScaling);
                    date7.setTypeface(typeface != null ? typeface : date7.getTypeface(), Typeface.NORMAL);
                    date7.setIncludeFontPadding(false);
                    ViewGroup.MarginLayoutParams dateLayoutParams7 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                    dateLayoutParams7.setMargins(0, 12 + lineHeight, 0, 0);
                    date7.setLayoutParams(dateLayoutParams7);

                    final LinearLayout container7 = new LinearLayout(mContext);
                    LinearLayout.LayoutParams layoutParams7 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams7.gravity = Gravity.CENTER_HORIZONTAL;
                    layoutParams7.setMargins((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, topMargin, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, bottomMargin, mContext.getResources().getDisplayMetrics()));
                    container7.setLayoutParams(layoutParams7);
                    container7.setGravity(Gravity.START | Gravity.CENTER_HORIZONTAL);
                    container7.setOrientation(LinearLayout.VERTICAL);

                    container7.addView(time71);
                    container7.addView(time72);
                    container7.addView(time73);
                    container7.addView(date7);

                    return container7;
                case 8:
                    final TextView day8 = new TextView(mContext);
                    day8.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    sdf = new SimpleDateFormat("EEEE", Locale.getDefault());
                    day8.setText(sdf.format(new Date()));
                    day8.setTextColor(forceWhiteText ? mContext.getResources().getColor(android.R.color.white, mContext.getTheme()) : (customColorEnabled ? customColorCode : mContext.getResources().getColor(android.R.color.system_neutral1_10, mContext.getTheme())));
                    day8.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20 * textScaling);
                    day8.setTypeface(typeface != null ? typeface : day8.getTypeface(), Typeface.BOLD);
                    day8.setIncludeFontPadding(false);
                    ViewGroup.MarginLayoutParams dayParams8 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                    dayParams8.setMargins(0, 0, 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -16, mContext.getResources().getDisplayMetrics()));
                    day8.setLayoutParams(dayParams8);

                    final TextView clock8 = new TextView(mContext);
                    clock8.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    sdf = new SimpleDateFormat(DateFormat.is24HourFormat(mContext) ? "HH:mm" : "hh:mm", Locale.getDefault());
                    clock8.setText(sdf.format(new Date()));
                    clock8.setTextColor(forceWhiteText ? mContext.getResources().getColor(android.R.color.white, mContext.getTheme()) : (customColorEnabled ? customColorCode : mContext.getResources().getColor(android.R.color.system_neutral1_10, mContext.getTheme())));
                    clock8.setTextSize(TypedValue.COMPLEX_UNIT_SP, 100 * textScaling);
                    clock8.setTypeface(typeface != null ? typeface : clock8.getTypeface(), Typeface.BOLD);
                    clock8.setIncludeFontPadding(false);
                    ViewGroup.MarginLayoutParams clockParams8 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                    clockParams8.setMargins(0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6 + lineHeight, mContext.getResources().getDisplayMetrics()), 0, 0);
                    clock8.setLayoutParams(clockParams8);

                    final TextView date8 = new TextView(mContext);
                    date8.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    sdf = new SimpleDateFormat("MMMM dd", Locale.getDefault());
                    date8.setText(sdf.format(new Date()));
                    date8.setTextColor(forceWhiteText ? mContext.getResources().getColor(android.R.color.white, mContext.getTheme()) : (customColorEnabled ? customColorCode : mContext.getResources().getColor(android.R.color.system_neutral1_10, mContext.getTheme())));
                    date8.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20 * textScaling);
                    date8.setTypeface(typeface != null ? typeface : date8.getTypeface(), Typeface.NORMAL);
                    date8.setIncludeFontPadding(false);
                    ViewGroup.MarginLayoutParams dateParams8 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                    dateParams8.setMargins(0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -10 + lineHeight, mContext.getResources().getDisplayMetrics()), 0, 0);
                    date8.setLayoutParams(dateParams8);

                    final LinearLayout container8 = new LinearLayout(mContext);
                    LinearLayout.LayoutParams layoutParams8 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams8.gravity = Gravity.CENTER_HORIZONTAL;
                    layoutParams8.setMargins(0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, topMargin, mContext.getResources().getDisplayMetrics()), 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, bottomMargin, mContext.getResources().getDisplayMetrics()));
                    container8.setLayoutParams(layoutParams8);
                    container8.setGravity(Gravity.CENTER);
                    container8.setOrientation(LinearLayout.VERTICAL);
                    container8.setPadding((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, mContext.getResources().getDisplayMetrics()));
                    GradientDrawable mDrawable8 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{forceWhiteText ? android.R.color.white : (customColorEnabled ? customColorCode : mContext.getResources().getColor(android.R.color.system_neutral1_10, mContext.getTheme())), forceWhiteText ? android.R.color.white : (customColorEnabled ? customColorCode : mContext.getResources().getColor(android.R.color.system_neutral1_10, mContext.getTheme()))});
                    mDrawable8.setCornerRadius((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, mContext.getResources().getDisplayMetrics()));
                    mDrawable8.setAlpha(50);
                    container8.setBackground(mDrawable8);

                    container8.addView(day8);
                    container8.addView(clock8);
                    container8.addView(date8);

                    return container8;
            }
        }
        return null;
    }

    private String getNumericToText(String number) {
        int num = Integer.parseInt(number);
        String[] numbers = {
                "Zero", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
                "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen",
                "Twenty", "Twenty One", "Twenty Two", "Twenty Three", "Twenty Four", "Twenty Five", "Twenty Six", "Twenty Seven", "Twenty Eight", "Twenty Nine",
                "Thirty", "Thirty One", "Thirty Two", "Thirty Three", "Thirty Four", "Thirty Five", "Thirty Six", "Thirty Seven", "Thirty Eight", "Thirty Nine",
                "Forty", "Forty One", "Forty Two", "Forty Three", "Forty Four", "Forty Five", "Forty Six", "Forty Seven", "Forty Eight", "Forty Nine",
                "Fifty", "Fifty One", "Fifty Two", "Fifty Three", "Fifty Four", "Fifty Five", "Fifty Six", "Fifty Seven", "Fifty Eight", "Fifty Nine",
                "Sixty"
        };
        return numbers[num];
    }
}