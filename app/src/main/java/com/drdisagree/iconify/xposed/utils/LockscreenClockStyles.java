package com.drdisagree.iconify.xposed.utils;

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
import static com.drdisagree.iconify.xposed.utils.TimeUtils.formatTime;
import static com.drdisagree.iconify.xposed.utils.TimeUtils.getNumericToText;
import static com.drdisagree.iconify.xposed.utils.TimeUtils.regionFormattedDate;
import static com.drdisagree.iconify.xposed.utils.ViewHelper.dp2px;
import static com.drdisagree.iconify.xposed.utils.ViewHelper.setMargins;
import static com.drdisagree.iconify.xposed.utils.ViewHelper.setPaddings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Environment;
import android.text.InputFilter;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.AnalogClock;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import java.io.File;

public class LockscreenClockStyles {

    @SuppressLint("SetTextI18n")
    @SuppressWarnings({"unused", "deprecation"})
    public static ViewGroup getClock(Context mContext) {
        boolean showLockscreenClock = Xprefs.getBoolean(LSCLOCK_SWITCH, false);

        if (showLockscreenClock) {
            boolean customColorEnabled = Xprefs.getBoolean(LSCLOCK_COLOR_SWITCH, false);
            int customColorCode = Xprefs.getInt(LSCLOCK_COLOR_CODE, Color.WHITE);
            int lockscreenClockStyle = Xprefs.getInt(LSCLOCK_STYLE, 0);
            int topMargin = Xprefs.getInt(LSCLOCK_TOPMARGIN, 100);
            int bottomMargin = Xprefs.getInt(LSCLOCK_BOTTOMMARGIN, 40);
            int lineHeight = Xprefs.getInt(LSCLOCK_FONT_LINEHEIGHT, 0);
            boolean customFontEnabled = Xprefs.getBoolean(LSCLOCK_FONT_SWITCH, false);
            boolean forceWhiteText = Xprefs.getBoolean(LSCLOCK_TEXT_WHITE, false);
            float textScaling = (float) (Xprefs.getInt(LSCLOCK_FONT_TEXT_SCALING, 10) / 10.0);

            int accent1 = ResourcesCompat.getColor(mContext.getResources(), android.R.color.holo_blue_light, mContext.getTheme());
            int accent2 = ResourcesCompat.getColor(mContext.getResources(), android.R.color.holo_blue_dark, mContext.getTheme());
            int white = ResourcesCompat.getColor(mContext.getResources(), android.R.color.white, mContext.getTheme());
            int black = ResourcesCompat.getColor(mContext.getResources(), android.R.color.black, mContext.getTheme());
            int textColorNormal = ResourcesCompat.getColor(mContext.getResources(), android.R.color.system_neutral1_10, mContext.getTheme());
            int textColorNormalVariant = ResourcesCompat.getColor(mContext.getResources(), android.R.color.system_neutral1_200, mContext.getTheme());
            int textColor = forceWhiteText ? white : (customColorEnabled ? customColorCode : textColorNormal);
            int textColorDark = forceWhiteText ? white : (customColorEnabled ? customColorCode : textColorNormalVariant);
            int accentPrimary = forceWhiteText ? white : accent1;
            int accentSecondary = forceWhiteText ? white : accent2;
            int accentPrimaryVariable = forceWhiteText ? white : (customColorEnabled ? customColorCode : accent1);
            int accentSecondaryVariable = forceWhiteText ? white : (customColorEnabled ? customColorCode : accent2);
            String customFont = Environment.getExternalStorageDirectory() + "/.iconify_files/lsclock_font.ttf";

            Typeface typeface = null;
            if (customFontEnabled && (new File(customFont).exists()))
                typeface = Typeface.createFromFile(new File(customFont));

            switch (lockscreenClockStyle) {
                case 0:
                    final LinearLayout blank0 = new LinearLayout(mContext);
                    LinearLayout.LayoutParams blankParams0 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    setMargins(blankParams0, mContext, 0, topMargin, 0, bottomMargin);
                    blank0.setLayoutParams(blankParams0);
                    blank0.setOrientation(LinearLayout.VERTICAL);

                    return blank0;
                case 1:
                    final TextView date1 = new TextView(mContext);
                    date1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    date1.setText(regionFormattedDate("EEEE, MMMM d", "EEEE d MMMM"));
                    date1.setTextColor(accentPrimaryVariable);
                    date1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20 * textScaling);
                    date1.setTypeface(typeface, Typeface.BOLD);
                    date1.setIncludeFontPadding(false);
                    ViewGroup.MarginLayoutParams dateParams1 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                    setMargins(dateParams1, mContext, 0, 0, 0, -16);
                    date1.setLayoutParams(dateParams1);

                    final TextView clock1 = new TextView(mContext);
                    clock1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    clock1.setText(formatTime(mContext, "HH:mm", "hh:mm"));
                    clock1.setTextColor(accentPrimaryVariable);
                    clock1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 100 * textScaling);
                    clock1.setTypeface(typeface, Typeface.BOLD);
                    clock1.setIncludeFontPadding(false);
                    ViewGroup.MarginLayoutParams clockParams1 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                    setMargins(clockParams1, mContext, 0, 6 + lineHeight, 0, 0);
                    clock1.setLayoutParams(clockParams1);

                    final LinearLayout clockContainer1 = new LinearLayout(mContext);
                    LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams1.gravity = Gravity.CENTER_HORIZONTAL;
                    setMargins(layoutParams1, mContext, 0, topMargin, 0, bottomMargin);
                    clockContainer1.setLayoutParams(layoutParams1);
                    clockContainer1.setGravity(Gravity.CENTER);
                    clockContainer1.setOrientation(LinearLayout.VERTICAL);

                    clockContainer1.addView(date1);
                    clockContainer1.addView(clock1);

                    return clockContainer1;
                case 2:
                    final TextView day2 = new TextView(mContext);
                    day2.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    day2.setText(formatTime("EEEE"));
                    day2.setTextColor(textColor);
                    day2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50 * textScaling);
                    day2.setTypeface(typeface);
                    day2.setIncludeFontPadding(false);

                    final TextView clock2 = new TextView(mContext);
                    clock2.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    clock2.setText(formatTime(mContext, "HH:mm", "hh:mm"));
                    clock2.setTextColor(textColor);
                    clock2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50 * textScaling);
                    clock2.setTypeface(typeface);
                    clock2.setIncludeFontPadding(false);

                    final TextView clockOverlay2 = new TextView(mContext);
                    clockOverlay2.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    clockOverlay2.setText(formatTime(mContext, "HH", "hh"));
                    clockOverlay2.setTextColor(accentPrimary);
                    clockOverlay2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50 * textScaling);
                    clockOverlay2.setMaxLines(1);
                    clockOverlay2.setTypeface(typeface);
                    clockOverlay2.setIncludeFontPadding(false);
                    int maxLength2 = 1;
                    InputFilter[] fArray2 = new InputFilter[1];
                    fArray2[0] = new InputFilter.LengthFilter(maxLength2);
                    clockOverlay2.setFilters(fArray2);

                    final FrameLayout clockContainer2 = new FrameLayout(mContext);
                    clockContainer2.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    setMargins(clockContainer2, mContext, 0, -2 + lineHeight, 0, -8 + lineHeight);

                    clockContainer2.addView(clock2);
                    clockContainer2.addView(clockOverlay2);

                    final TextView month2 = new TextView(mContext);
                    month2.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    month2.setText(regionFormattedDate("MMMM d", "d MMMM"));
                    month2.setTextColor(textColor);
                    month2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20 * textScaling);
                    month2.setTypeface(typeface);
                    month2.setIncludeFontPadding(false);
                    ViewGroup.MarginLayoutParams monthParams2 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                    setMargins(monthParams2, mContext, 0, 6 + lineHeight, 0, 0);
                    month2.setLayoutParams(monthParams2);

                    final LinearLayout wholeContainer2 = new LinearLayout(mContext);
                    LinearLayout.LayoutParams layoutparams2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutparams2.gravity = Gravity.START;
                    setMargins(layoutparams2, mContext, 24, topMargin, 24, bottomMargin);
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
                    date3.setText(regionFormattedDate("EEE, MMM dd", "EEE dd MMM"));
                    date3.setTextColor(accentPrimaryVariable);
                    date3.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24 * textScaling);
                    date3.setTypeface(typeface, Typeface.BOLD);
                    date3.setIncludeFontPadding(false);

                    final TextView clockHour3 = new TextView(mContext);
                    clockHour3.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    clockHour3.setText(formatTime(mContext, "HH", "hh"));
                    clockHour3.setTextColor(accentPrimaryVariable);
                    clockHour3.setTextSize(TypedValue.COMPLEX_UNIT_SP, 160 * textScaling);
                    clockHour3.setTypeface(typeface, Typeface.BOLD);
                    clockHour3.setIncludeFontPadding(false);
                    ViewGroup.MarginLayoutParams clockHourParams3 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                    setMargins(clockHourParams3, mContext, 0, -22 + lineHeight, 0, 0);
                    clockHour3.setLayoutParams(clockHourParams3);

                    final TextView clockMinute3 = new TextView(mContext);
                    clockMinute3.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    clockMinute3.setText(formatTime("mm"));
                    clockMinute3.setTextColor(accentPrimaryVariable);
                    clockMinute3.setTextSize(TypedValue.COMPLEX_UNIT_SP, 160 * textScaling);
                    clockMinute3.setTypeface(typeface, Typeface.BOLD);
                    clockMinute3.setIncludeFontPadding(false);
                    ViewGroup.MarginLayoutParams clockMinuteParams3 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                    setMargins(clockMinuteParams3, mContext, 0, -58 + lineHeight, 0, 0);
                    clockMinute3.setLayoutParams(clockMinuteParams3);

                    final LinearLayout clockContainer3 = new LinearLayout(mContext);
                    LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams3.gravity = Gravity.CENTER_HORIZONTAL;
                    setMargins(layoutParams3, mContext, 0, topMargin, 0, bottomMargin);
                    clockContainer3.setLayoutParams(layoutParams3);
                    clockContainer3.setGravity(Gravity.CENTER_HORIZONTAL);
                    clockContainer3.setOrientation(LinearLayout.VERTICAL);

                    clockContainer3.addView(date3);
                    clockContainer3.addView(clockHour3);
                    clockContainer3.addView(clockMinute3);

                    return clockContainer3;
                case 4:
                    final AnalogClock analogClock4 = new AnalogClock(mContext);
                    analogClock4.setLayoutParams(new LinearLayout.LayoutParams(dp2px(mContext, 180 * textScaling), dp2px(mContext, 180 * textScaling)));
                    ((LinearLayout.LayoutParams) analogClock4.getLayoutParams()).gravity = Gravity.CENTER_HORIZONTAL;

                    final TextView day4 = new TextView(mContext);
                    day4.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    day4.setText(regionFormattedDate("EEE, MMM dd", "EEE dd MMM"));
                    day4.setTextColor(textColorDark);
                    day4.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28 * textScaling);
                    day4.setTypeface(typeface, Typeface.BOLD);
                    day4.setIncludeFontPadding(false);
                    ViewGroup.MarginLayoutParams dayParams4 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                    setMargins(dayParams4, mContext, 0, 6 + lineHeight, 0, 0);
                    day4.setLayoutParams(dayParams4);

                    final LinearLayout clockContainer4 = new LinearLayout(mContext);
                    LinearLayout.LayoutParams layoutParams4 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams4.gravity = Gravity.CENTER_HORIZONTAL;
                    setMargins(layoutParams4, mContext, 0, topMargin, 0, bottomMargin);
                    clockContainer4.setLayoutParams(layoutParams4);
                    clockContainer4.setGravity(Gravity.CENTER_HORIZONTAL);
                    clockContainer4.setOrientation(LinearLayout.VERTICAL);

                    clockContainer4.addView(analogClock4);
                    clockContainer4.addView(day4);

                    return clockContainer4;
                case 5:
                    final TextView hour5 = new TextView(mContext);
                    hour5.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    hour5.setText(formatTime(mContext, "HH", "hh"));
                    hour5.setTextColor(white);
                    hour5.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40 * textScaling);
                    hour5.setTypeface(typeface, Typeface.BOLD);
                    hour5.setIncludeFontPadding(false);

                    final TextView minute5 = new TextView(mContext);
                    minute5.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    minute5.setText(formatTime("mm"));
                    minute5.setTextColor(white);
                    minute5.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40 * textScaling);
                    minute5.setTypeface(typeface, Typeface.BOLD);
                    minute5.setIncludeFontPadding(false);
                    ViewGroup.MarginLayoutParams minuteParams5 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                    setMargins(minuteParams5, mContext, 0, 4 + lineHeight, 0, 0);
                    minute5.setLayoutParams(minuteParams5);

                    final LinearLayout time5 = new LinearLayout(mContext);
                    LinearLayout.LayoutParams timeLayoutParams5 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    timeLayoutParams5.gravity = Gravity.CENTER;
                    time5.setLayoutParams(timeLayoutParams5);
                    time5.setOrientation(LinearLayout.VERTICAL);
                    setPaddings(time5, mContext, 16, 16, 16, 16);
                    GradientDrawable timeDrawable5 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{black, black});
                    timeDrawable5.setCornerRadius(dp2px(mContext, 16));
                    time5.setBackground(timeDrawable5);
                    time5.setGravity(Gravity.CENTER);

                    time5.addView(hour5);
                    time5.addView(minute5);

                    final TextView day5 = new TextView(mContext);
                    day5.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    day5.setText(formatTime("EEE"));
                    day5.setAllCaps(true);
                    day5.setTextColor(textColor);
                    day5.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28 * textScaling);
                    day5.setTypeface(typeface, Typeface.BOLD);
                    day5.setIncludeFontPadding(false);
                    day5.setLetterSpacing(0.2f);

                    final TextView date5 = new TextView(mContext);
                    date5.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    date5.setText(formatTime("dd"));
                    date5.setTextColor(textColor);
                    date5.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28 * textScaling);
                    date5.setTypeface(typeface, Typeface.BOLD);
                    date5.setIncludeFontPadding(false);
                    date5.setLetterSpacing(0.2f);
                    ViewGroup.MarginLayoutParams dateParams5 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                    setMargins(dateParams5, mContext, 0, -2 + lineHeight, 0, 0);
                    date5.setLayoutParams(dateParams5);

                    final TextView month5 = new TextView(mContext);
                    month5.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    month5.setText(formatTime("MMM"));
                    month5.setAllCaps(true);
                    month5.setTextColor(textColor);
                    month5.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28 * textScaling);
                    month5.setTypeface(typeface, Typeface.BOLD);
                    month5.setIncludeFontPadding(false);
                    month5.setLetterSpacing(0.2f);
                    ViewGroup.MarginLayoutParams monthParams5 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                    setMargins(monthParams5, mContext, 0, -2 + lineHeight, 0, 0);
                    month5.setLayoutParams(monthParams5);

                    final LinearLayout right5 = new LinearLayout(mContext);
                    LinearLayout.LayoutParams rightLayoutParams5 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    rightLayoutParams5.gravity = Gravity.CENTER;
                    right5.setLayoutParams(rightLayoutParams5);
                    right5.setOrientation(LinearLayout.VERTICAL);
                    setPaddings(right5, mContext, 16, 16, 16, 16);
                    right5.setGravity(Gravity.CENTER);

                    right5.addView(day5);
                    right5.addView(date5);
                    right5.addView(month5);

                    final LinearLayout container5 = new LinearLayout(mContext);
                    LinearLayout.LayoutParams layoutParams5 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams5.gravity = Gravity.CENTER_HORIZONTAL;
                    setMargins(layoutParams5, mContext, 24, topMargin, 24, bottomMargin);
                    container5.setLayoutParams(layoutParams5);
                    container5.setOrientation(LinearLayout.HORIZONTAL);
                    setPaddings(container5, mContext, 12, 12, 12, 12);
                    GradientDrawable mDrawable5 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{accent1, accent1});
                    mDrawable5.setCornerRadius(dp2px(mContext, 28));
                    container5.setBackground(mDrawable5);

                    container5.addView(time5);
                    container5.addView(right5);

                    return container5;
                case 6:
                    final TextView day6 = new TextView(mContext);
                    day6.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    day6.setText(formatTime("EEE").substring(0, 2));
                    day6.setAllCaps(true);
                    day6.setTextColor(white);
                    day6.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40 * textScaling);
                    day6.setTypeface(typeface, Typeface.NORMAL);
                    day6.setIncludeFontPadding(false);

                    final TextView dayText6 = new TextView(mContext);
                    dayText6.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    dayText6.setText("DAY");
                    dayText6.setAllCaps(true);
                    dayText6.setTextColor(white);
                    dayText6.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14 * textScaling);
                    dayText6.setTypeface(typeface, Typeface.NORMAL);
                    dayText6.setIncludeFontPadding(false);
                    dayText6.setAlpha(0.4f);
                    ViewGroup.MarginLayoutParams dayTextParams6 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                    setMargins(dayTextParams6, mContext, 0, 12 + lineHeight, 0, 0);
                    dayText6.setLayoutParams(dayTextParams6);

                    final LinearLayout dayContainer6 = new LinearLayout(mContext);
                    LinearLayout.LayoutParams dayLayoutParams6 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dayLayoutParams6.gravity = Gravity.CENTER;
                    setMargins(dayLayoutParams6, mContext, 20, 20, 20, 20);
                    dayContainer6.setLayoutParams(dayLayoutParams6);
                    dayContainer6.setGravity(Gravity.CENTER);
                    dayContainer6.setOrientation(LinearLayout.VERTICAL);

                    dayContainer6.addView(day6);
                    dayContainer6.addView(dayText6);

                    final TextView hour6 = new TextView(mContext);
                    hour6.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    hour6.setText(formatTime(mContext, "HH", "hh"));
                    hour6.setAllCaps(true);
                    hour6.setTextColor(white);
                    hour6.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40 * textScaling);
                    hour6.setTypeface(typeface, Typeface.NORMAL);
                    hour6.setIncludeFontPadding(false);

                    final TextView hourText6 = new TextView(mContext);
                    hourText6.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    hourText6.setText("HOURS");
                    hourText6.setAllCaps(true);
                    hourText6.setTextColor(white);
                    hourText6.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14 * textScaling);
                    hourText6.setTypeface(typeface, Typeface.NORMAL);
                    hourText6.setIncludeFontPadding(false);
                    hourText6.setAlpha(0.4f);
                    ViewGroup.MarginLayoutParams hourTextParams6 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                    setMargins(hourTextParams6, mContext, 0, 12 + lineHeight, 0, 0);
                    hourText6.setLayoutParams(hourTextParams6);

                    final LinearLayout hourContainer6 = new LinearLayout(mContext);
                    LinearLayout.LayoutParams hourLayoutParams6 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    hourLayoutParams6.gravity = Gravity.CENTER;
                    setMargins(hourLayoutParams6, mContext, 20, 20, 20, 20);
                    hourContainer6.setLayoutParams(hourLayoutParams6);
                    hourContainer6.setGravity(Gravity.CENTER);
                    hourContainer6.setOrientation(LinearLayout.VERTICAL);

                    hourContainer6.addView(hour6);
                    hourContainer6.addView(hourText6);

                    final TextView minute6 = new TextView(mContext);
                    minute6.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    minute6.setText(formatTime("mm"));
                    minute6.setAllCaps(true);
                    minute6.setTextColor(white);
                    minute6.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40 * textScaling);
                    minute6.setTypeface(typeface, Typeface.NORMAL);
                    minute6.setIncludeFontPadding(false);

                    final TextView minuteText6 = new TextView(mContext);
                    minuteText6.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    minuteText6.setText("MINUTES");
                    minuteText6.setAllCaps(true);
                    minuteText6.setTextColor(white);
                    minuteText6.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14 * textScaling);
                    minuteText6.setTypeface(typeface, Typeface.NORMAL);
                    minuteText6.setIncludeFontPadding(false);
                    minuteText6.setAlpha(0.4f);
                    ViewGroup.MarginLayoutParams minuteTextParams6 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                    setMargins(minuteTextParams6, mContext, 0, 12 + lineHeight, 0, 0);
                    minuteText6.setLayoutParams(minuteTextParams6);

                    final LinearLayout minuteContainer6 = new LinearLayout(mContext);
                    LinearLayout.LayoutParams minuteLayoutParams6 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    minuteLayoutParams6.gravity = Gravity.CENTER;
                    setMargins(minuteLayoutParams6, mContext, 20, 20, 20, 20);
                    minuteContainer6.setLayoutParams(minuteLayoutParams6);
                    minuteContainer6.setGravity(Gravity.CENTER);
                    minuteContainer6.setOrientation(LinearLayout.VERTICAL);

                    minuteContainer6.addView(minute6);
                    minuteContainer6.addView(minuteText6);

                    final LinearLayout container6 = new LinearLayout(mContext);
                    LinearLayout.LayoutParams layoutParams6 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams6.gravity = Gravity.CENTER_HORIZONTAL;
                    setMargins(layoutParams6, mContext, 24, topMargin, 24, bottomMargin);
                    container6.setLayoutParams(layoutParams6);
                    container6.setGravity(Gravity.CENTER);
                    container6.setOrientation(LinearLayout.HORIZONTAL);
                    setPaddings(container6, mContext, 20, 20, 20, 24);
                    GradientDrawable mDrawable6 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{Color.parseColor("#090909"), Color.parseColor("#090909")});
                    mDrawable6.setCornerRadius(dp2px(mContext, 12));
                    container6.setBackground(mDrawable6);

                    container6.addView(dayContainer6);
                    container6.addView(hourContainer6);
                    container6.addView(minuteContainer6);

                    return container6;
                case 7:
                    final TextView time71 = new TextView(mContext);
                    time71.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    time71.setText("It's");
                    time71.setTextColor(textColor);
                    time71.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40 * textScaling);
                    time71.setTypeface(typeface, Typeface.NORMAL);
                    time71.setIncludeFontPadding(false);
                    ViewGroup.MarginLayoutParams timeLayoutParams71 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                    setMargins(timeLayoutParams71, mContext, 0, 12 + lineHeight, 0, 0);
                    time71.setLayoutParams(timeLayoutParams71);

                    final TextView time72 = new TextView(mContext);
                    time72.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    time72.setText(getNumericToText(formatTime(mContext, "HH", "hh")));
                    time72.setTextColor(textColor);
                    time72.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40 * textScaling);
                    time72.setTypeface(typeface, Typeface.NORMAL);
                    time72.setIncludeFontPadding(false);
                    ViewGroup.MarginLayoutParams timeLayoutParams72 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                    setMargins(timeLayoutParams72, mContext, 0, 12 + lineHeight, 0, 0);
                    time71.setLayoutParams(timeLayoutParams72);

                    final TextView time73 = new TextView(mContext);
                    time73.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    time73.setText(getNumericToText(formatTime("mm")));
                    time73.setTextColor(textColor);
                    time73.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40 * textScaling);
                    time73.setTypeface(typeface, Typeface.NORMAL);
                    time73.setIncludeFontPadding(false);
                    ViewGroup.MarginLayoutParams timeLayoutParams73 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                    setMargins(timeLayoutParams73, mContext, 0, 12 + lineHeight, 0, 0);
                    time71.setLayoutParams(timeLayoutParams73);

                    final TextView date7 = new TextView(mContext);
                    date7.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    date7.setText(regionFormattedDate("EEEE, MMM dd", "EEEE dd MMM"));
                    date7.setTextColor(textColor);
                    date7.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20 * textScaling);
                    date7.setTypeface(typeface, Typeface.NORMAL);
                    date7.setIncludeFontPadding(false);
                    ViewGroup.MarginLayoutParams dateLayoutParams7 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                    setMargins(dateLayoutParams7, mContext, 0, 12 + lineHeight, 0, 0);
                    date7.setLayoutParams(dateLayoutParams7);

                    final LinearLayout container7 = new LinearLayout(mContext);
                    LinearLayout.LayoutParams layoutParams7 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams7.gravity = Gravity.CENTER_HORIZONTAL;
                    setMargins(layoutParams7, mContext, 24, topMargin, 24, bottomMargin);
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
                    day8.setText(formatTime("EEEE"));
                    day8.setTextColor(textColor);
                    day8.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20 * textScaling);
                    day8.setTypeface(typeface, Typeface.BOLD);
                    day8.setIncludeFontPadding(false);
                    ViewGroup.MarginLayoutParams dayParams8 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                    setMargins(dayParams8, mContext, 0, 0, 0, -16);
                    day8.setLayoutParams(dayParams8);

                    final TextView clock8 = new TextView(mContext);
                    clock8.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    clock8.setText(formatTime(mContext, "HH:mm", "hh:mm"));
                    clock8.setTextColor(textColor);
                    clock8.setTextSize(TypedValue.COMPLEX_UNIT_SP, 100 * textScaling);
                    clock8.setTypeface(typeface, Typeface.BOLD);
                    clock8.setIncludeFontPadding(false);
                    ViewGroup.MarginLayoutParams clockParams8 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                    setMargins(clockParams8, mContext, 0, 6 + lineHeight, 0, 0);
                    clock8.setLayoutParams(clockParams8);

                    final TextView date8 = new TextView(mContext);
                    date8.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    date8.setText(regionFormattedDate("MMMM dd", "dd MMMM"));
                    date8.setTextColor(textColor);
                    date8.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20 * textScaling);
                    date8.setTypeface(typeface, Typeface.NORMAL);
                    date8.setIncludeFontPadding(false);
                    ViewGroup.MarginLayoutParams dateParams8 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                    setMargins(dateParams8, mContext, 0, -10 + lineHeight, 0, 0);
                    date8.setLayoutParams(dateParams8);

                    final LinearLayout container8 = new LinearLayout(mContext);
                    LinearLayout.LayoutParams layoutParams8 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams8.gravity = Gravity.CENTER_HORIZONTAL;
                    setMargins(layoutParams8, mContext, 0, topMargin, 0, bottomMargin);
                    container8.setLayoutParams(layoutParams8);
                    container8.setGravity(Gravity.CENTER);
                    container8.setOrientation(LinearLayout.VERTICAL);
                    setPaddings(container8, mContext, 40, 24, 40, 24);
                    GradientDrawable mDrawable8 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{textColor, textColor});
                    mDrawable8.setCornerRadius(dp2px(mContext, 24));
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
}
