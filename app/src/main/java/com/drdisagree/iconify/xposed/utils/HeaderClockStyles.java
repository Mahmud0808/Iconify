package com.drdisagree.iconify.xposed.utils;

import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_COLOR_CODE;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_COLOR_SWITCH;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_FONT_SWITCH;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_FONT_TEXT_SCALING;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_SIDEMARGIN;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_STYLE;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_SWITCH;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_TEXT_WHITE;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_TOPMARGIN;
import static com.drdisagree.iconify.common.Preferences.UI_CORNER_RADIUS;
import static com.drdisagree.iconify.config.XPrefs.Xprefs;
import static com.drdisagree.iconify.xposed.utils.ViewHelper.dp2px;
import static com.drdisagree.iconify.xposed.utils.ViewHelper.setMargins;
import static com.drdisagree.iconify.xposed.utils.ViewHelper.setPaddings;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Environment;
import android.text.InputFilter;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AnalogClock;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextClock;

import androidx.core.content.res.ResourcesCompat;

import java.io.File;

public class HeaderClockStyles {

    @SuppressWarnings("deprecation")
    public static ViewGroup getClock(Context mContext) {
        boolean showHeaderClock = Xprefs.getBoolean(HEADER_CLOCK_SWITCH, false);

        if (showHeaderClock) {
            int headerClockStyle = Xprefs.getInt(HEADER_CLOCK_STYLE, 1);
            boolean forceWhiteText = Xprefs.getBoolean(HEADER_CLOCK_TEXT_WHITE, false);
            boolean customFontEnabled = Xprefs.getBoolean(HEADER_CLOCK_FONT_SWITCH, false);
            float textScaling = (float) (Xprefs.getInt(HEADER_CLOCK_FONT_TEXT_SCALING, 10) / 10.0);
            boolean customColorEnabled = Xprefs.getBoolean(HEADER_CLOCK_COLOR_SWITCH, false);
            int customColorCode = Xprefs.getInt(HEADER_CLOCK_COLOR_CODE, Color.WHITE);
            int sideMargin = Xprefs.getInt(HEADER_CLOCK_SIDEMARGIN, 0);
            int topMargin = Xprefs.getInt(HEADER_CLOCK_TOPMARGIN, 8);
            int radius = Xprefs.getInt(UI_CORNER_RADIUS, 28);
            int radius2 = radius - 2;

            int accentPrimary = ResourcesCompat.getColor(mContext.getResources(), android.R.color.holo_blue_light, mContext.getTheme());
            int accentSecondary = ResourcesCompat.getColor(mContext.getResources(), android.R.color.holo_blue_dark, mContext.getTheme());
            int white = ResourcesCompat.getColor(mContext.getResources(), android.R.color.white, mContext.getTheme());
            int black = ResourcesCompat.getColor(mContext.getResources(), android.R.color.black, mContext.getTheme());
            int textColorPrimary = SystemUtil.getColorResCompat(mContext, android.R.attr.textColorPrimary);
            int textColor = forceWhiteText ? white : (customColorEnabled ? customColorCode : textColorPrimary);
            String customFont = Environment.getExternalStorageDirectory() + "/.iconify_files/headerclock_font.ttf";

            Typeface typeface = null;
            if (customFontEnabled && (new File(customFont).exists()))
                typeface = Typeface.createFromFile(new File(customFont));

            switch (headerClockStyle) {
                case 1:
                    final TextClock clockHour1 = new TextClock(mContext);
                    clockHour1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    clockHour1.setFormat12Hour("hh");
                    clockHour1.setFormat24Hour("HH");
                    clockHour1.setTextColor(accentPrimary);
                    clockHour1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40 * textScaling);
                    clockHour1.setTypeface(typeface, Typeface.BOLD);
                    clockHour1.setIncludeFontPadding(false);

                    final TextClock clockMinute1 = new TextClock(mContext);
                    clockMinute1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    clockMinute1.setFormat12Hour(":mm");
                    clockMinute1.setFormat24Hour(":mm");
                    clockMinute1.setTextColor(textColor);
                    clockMinute1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40 * textScaling);
                    clockMinute1.setTypeface(typeface, Typeface.BOLD);
                    clockMinute1.setIncludeFontPadding(false);

                    final LinearLayout divider1 = new LinearLayout(mContext);
                    ViewGroup.MarginLayoutParams dividerParams1 = new ViewGroup.MarginLayoutParams(dp2px(mContext, 4), dp2px(mContext, 40 * textScaling));
                    setMargins(dividerParams1, mContext, 8, 4, 8, 4);
                    divider1.setLayoutParams(dividerParams1);
                    GradientDrawable mDrawable1 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{accentSecondary, accentSecondary});
                    mDrawable1.setCornerRadius(8);
                    divider1.setBackground(mDrawable1);

                    final TextClock clockDay1 = new TextClock(mContext);
                    clockDay1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    clockDay1.setFormat12Hour("EEEE");
                    clockDay1.setFormat24Hour("EEEE");
                    clockDay1.setTextColor(textColor);
                    clockDay1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14 * textScaling);
                    clockDay1.setTypeface(typeface, Typeface.BOLD);
                    clockDay1.setIncludeFontPadding(false);

                    final TextClock clockDate1 = new TextClock(mContext);
                    clockDate1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    clockDate1.setFormat12Hour("dd MMMM");
                    clockDate1.setFormat24Hour("dd MMMM");
                    clockDate1.setTextColor(textColor);
                    clockDate1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14 * textScaling);
                    clockDate1.setTypeface(typeface, Typeface.BOLD);
                    clockDate1.setIncludeFontPadding(false);

                    final LinearLayout dateContainer1 = new LinearLayout(mContext);
                    dateContainer1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
                    dateContainer1.setOrientation(LinearLayout.VERTICAL);
                    setMargins(dateContainer1, mContext, 0, 8, 0, 8);

                    dateContainer1.addView(clockDay1);
                    dateContainer1.addView(clockDate1);

                    final LinearLayout clockContainer1 = new LinearLayout(mContext);
                    clockContainer1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    clockContainer1.setGravity(Gravity.CENTER_VERTICAL);
                    clockContainer1.setOrientation(LinearLayout.HORIZONTAL);
                    setMargins(clockContainer1, mContext, sideMargin, topMargin, sideMargin, 8);

                    clockContainer1.addView(clockHour1);
                    clockContainer1.addView(clockMinute1);
                    clockContainer1.addView(divider1);
                    clockContainer1.addView(dateContainer1);

                    return clockContainer1;
                case 2:
                    final TextClock clock2 = new TextClock(mContext);
                    clock2.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    clock2.setFormat12Hour("h:mm");
                    clock2.setFormat24Hour("H:mm");
                    clock2.setTextColor(textColor);
                    clock2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40 * textScaling);
                    clock2.setTypeface(typeface, Typeface.BOLD);
                    clock2.setIncludeFontPadding(false);

                    final TextClock clockOverlay2 = new TextClock(mContext);
                    clockOverlay2.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    clockOverlay2.setFormat12Hour("h");
                    clockOverlay2.setFormat24Hour("H");
                    clockOverlay2.setTextColor(accentPrimary);
                    clockOverlay2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40 * textScaling);
                    clockOverlay2.setTypeface(typeface, Typeface.BOLD);
                    clockOverlay2.setMaxLines(1);
                    clockOverlay2.setIncludeFontPadding(false);
                    int maxLength2 = 1;
                    InputFilter[] fArray2 = new InputFilter[1];
                    fArray2[0] = new InputFilter.LengthFilter(maxLength2);
                    clockOverlay2.setFilters(fArray2);

                    final FrameLayout clockContainer2 = new FrameLayout(mContext);
                    clockContainer2.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    setMargins(clockContainer2, mContext, 0, 0, 0, -8);

                    clockContainer2.addView(clock2);
                    clockContainer2.addView(clockOverlay2);

                    final TextClock dayDate2 = new TextClock(mContext);
                    dayDate2.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    dayDate2.setFormat12Hour("EEEE, MMM dd");
                    dayDate2.setFormat24Hour("EEEE, MMM dd");
                    dayDate2.setTextColor(textColor);
                    dayDate2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18 * textScaling);
                    dayDate2.setTypeface(typeface, Typeface.BOLD);
                    dayDate2.setIncludeFontPadding(false);

                    final LinearLayout container2 = new LinearLayout(mContext);
                    container2.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    container2.setGravity(Gravity.CENTER_VERTICAL);
                    container2.setOrientation(LinearLayout.VERTICAL);
                    setMargins(container2, mContext, sideMargin, topMargin, sideMargin, 8);

                    container2.addView(clockContainer2);
                    container2.addView(dayDate2);

                    return container2;
                case 3:
                    final TextClock clock3 = new TextClock(mContext);
                    clock3.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    clock3.setFormat12Hour("hh:mm");
                    clock3.setFormat24Hour("HH:mm");
                    clock3.setTextColor(textColor);
                    clock3.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28 * textScaling);
                    clock3.setTypeface(typeface, Typeface.BOLD);
                    clock3.setIncludeFontPadding(false);

                    final TextClock clockOverlay3 = new TextClock(mContext);
                    clockOverlay3.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    clockOverlay3.setFormat12Hour("hh:mm");
                    clockOverlay3.setFormat24Hour("HH:mm");
                    clockOverlay3.setTextColor(accentPrimary);
                    clockOverlay3.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28 * textScaling);
                    clockOverlay3.setTypeface(typeface, Typeface.BOLD);
                    clockOverlay3.setAlpha(0.2f);
                    clockOverlay3.setIncludeFontPadding(false);
                    LinearLayout.LayoutParams clockOverlayParams3 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    setMargins(clockOverlayParams3, mContext, 6, 6, 0, 0);
                    clockOverlay3.setLayoutParams(clockOverlayParams3);

                    final FrameLayout clockContainer3 = new FrameLayout(mContext);
                    clockContainer3.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    setMargins(clockContainer3, mContext, 0, 0, 0, -12);
                    clockContainer3.addView(clockOverlay3);
                    clockContainer3.addView(clock3);

                    final TextClock dayDate3 = new TextClock(mContext);
                    dayDate3.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    dayDate3.setFormat12Hour("EEE, MMM dd");
                    dayDate3.setFormat24Hour("EEE, MMM dd");
                    dayDate3.setTextColor(textColor);
                    dayDate3.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18 * textScaling);
                    dayDate3.setTypeface(typeface, Typeface.BOLD);
                    dayDate3.setIncludeFontPadding(false);

                    final TextClock dayDateOverlay3 = new TextClock(mContext);
                    dayDateOverlay3.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    dayDateOverlay3.setFormat12Hour("EEE, MMM dd");
                    dayDateOverlay3.setFormat24Hour("EEE, MMM dd");
                    dayDateOverlay3.setTextColor(accentSecondary);
                    dayDateOverlay3.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18 * textScaling);
                    dayDateOverlay3.setTypeface(typeface, Typeface.BOLD);
                    dayDateOverlay3.setAlpha(0.2f);
                    dayDateOverlay3.setIncludeFontPadding(false);
                    LinearLayout.LayoutParams dayDateOverlayParams3 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    setMargins(dayDateOverlayParams3, mContext, 6, 6, 0, 0);
                    dayDateOverlay3.setLayoutParams(dayDateOverlayParams3);

                    final FrameLayout dayDateContainer3 = new FrameLayout(mContext);
                    dayDateContainer3.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    setMargins(dayDateContainer3, mContext, 0, 4, 0, 0);
                    dayDateContainer3.addView(dayDateOverlay3);
                    dayDateContainer3.addView(dayDate3);

                    final LinearLayout container3 = new LinearLayout(mContext);
                    container3.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    container3.setGravity(Gravity.BOTTOM);
                    container3.setOrientation(LinearLayout.VERTICAL);
                    setMargins(container3, mContext, sideMargin, topMargin, sideMargin, 8);

                    container3.addView(clockContainer3);
                    container3.addView(dayDateContainer3);

                    return container3;
                case 4:
                    final AnalogClock analogClock4 = new AnalogClock(mContext);
                    analogClock4.setLayoutParams(new LinearLayout.LayoutParams(dp2px(mContext, 48 * textScaling), dp2px(mContext, 48 * textScaling)));
                    ((LinearLayout.LayoutParams) analogClock4.getLayoutParams()).gravity = Gravity.CENTER_HORIZONTAL;

                    final TextClock clockDay4 = new TextClock(mContext);
                    clockDay4.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    clockDay4.setFormat12Hour("EEEE");
                    clockDay4.setFormat24Hour("EEEE");
                    clockDay4.setTextColor(accentPrimary);
                    clockDay4.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16 * textScaling);
                    clockDay4.setTypeface(typeface, Typeface.BOLD);
                    clockDay4.setIncludeFontPadding(false);

                    final TextClock clockDate4 = new TextClock(mContext);
                    clockDate4.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    clockDate4.setFormat12Hour("dd MMMM");
                    clockDate4.setFormat24Hour("dd MMMM");
                    clockDate4.setTextColor(textColor);
                    clockDate4.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16 * textScaling);
                    clockDate4.setTypeface(typeface, Typeface.BOLD);
                    clockDate4.setIncludeFontPadding(false);

                    final LinearLayout dateContainer4 = new LinearLayout(mContext);
                    dateContainer4.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
                    dateContainer4.setOrientation(LinearLayout.VERTICAL);
                    setMargins(dateContainer4, mContext, 8, 0, 8, 0);

                    dateContainer4.addView(clockDay4);
                    dateContainer4.addView(clockDate4);

                    final LinearLayout clockContainer4 = new LinearLayout(mContext);
                    clockContainer4.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    clockContainer4.setGravity(Gravity.CENTER_VERTICAL);
                    clockContainer4.setOrientation(LinearLayout.HORIZONTAL);
                    setMargins(clockContainer4, mContext, sideMargin, topMargin, sideMargin, 8);

                    clockContainer4.addView(analogClock4);
                    clockContainer4.addView(dateContainer4);

                    return clockContainer4;
                case 5:
                    final TextClock time5 = new TextClock(mContext);
                    time5.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    time5.setFormat12Hour("hh:mm");
                    time5.setFormat24Hour("HH:mm");
                    time5.setTextColor(mContext.getResources().getColor(android.R.color.white, mContext.getTheme()));
                    time5.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14 * textScaling);
                    time5.setTypeface(typeface, Typeface.BOLD);
                    time5.setMaxLines(1);
                    time5.setIncludeFontPadding(false);

                    final LinearLayout timeContainer5 = new LinearLayout(mContext);
                    LinearLayout.LayoutParams timeLayoutParams5 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    timeLayoutParams5.gravity = Gravity.CENTER;
                    timeContainer5.setLayoutParams(timeLayoutParams5);
                    timeContainer5.setOrientation(LinearLayout.VERTICAL);
                    setPaddings(timeContainer5, mContext, 8, 4, 8, 4);
                    GradientDrawable timeDrawable5 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{black, black});
                    timeDrawable5.setCornerRadius(dp2px(mContext, radius2));
                    timeContainer5.setBackground(timeDrawable5);
                    timeContainer5.setGravity(Gravity.CENTER);

                    timeContainer5.addView(time5);

                    final TextClock date5 = new TextClock(mContext);
                    date5.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    date5.setFormat12Hour("EEE, MMM dd");
                    date5.setFormat24Hour("EEE, MMM dd");
                    date5.setTextColor(textColor);
                    date5.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14 * textScaling);
                    date5.setTypeface(typeface, Typeface.BOLD);
                    ViewGroup.MarginLayoutParams dateParams5 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                    dateParams5.setMarginStart(dp2px(mContext, 4));
                    dateParams5.setMarginEnd(dp2px(mContext, 8));
                    date5.setLayoutParams(dateParams5);
                    date5.setMaxLines(1);
                    date5.setIncludeFontPadding(false);

                    final LinearLayout container5 = new LinearLayout(mContext);
                    LinearLayout.LayoutParams layoutParams5 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    container5.setLayoutParams(layoutParams5);
                    container5.setGravity(Gravity.CENTER);
                    container5.setOrientation(LinearLayout.HORIZONTAL);
                    setPaddings(container5, mContext, 2, 2, 2, 2);
                    GradientDrawable mDrawable5 = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{accentPrimary, accentSecondary});
                    setMargins(container5, mContext, sideMargin, topMargin, sideMargin, 8);
                    mDrawable5.setCornerRadius(dp2px(mContext, radius));
                    container5.setBackground(mDrawable5);

                    container5.addView(timeContainer5);
                    container5.addView(date5);

                    return container5;
                case 6:
                    final TextClock time6 = new TextClock(mContext);
                    time6.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    time6.setFormat12Hour("hh:mm");
                    time6.setFormat24Hour("HH:mm");
                    time6.setTextColor(textColor);
                    time6.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18 * textScaling);
                    time6.setTypeface(typeface, Typeface.BOLD);
                    time6.setMaxLines(1);
                    time6.setIncludeFontPadding(false);

                    int px2dp8 = dp2px(mContext, 8);

                    final View view61 = new View(mContext);
                    LinearLayout.LayoutParams viewLayoutParams61 = new LinearLayout.LayoutParams(px2dp8, px2dp8);
                    setMargins(viewLayoutParams61, mContext, 40, 0, px2dp8, 0);
                    view61.setLayoutParams(viewLayoutParams61);
                    GradientDrawable mDrawable61 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{Color.parseColor("#3473B8"), Color.parseColor("#3473B8")});
                    mDrawable61.setCornerRadius(100);
                    view61.setBackground(mDrawable61);

                    final View view62 = new View(mContext);
                    LinearLayout.LayoutParams viewLayoutParams62 = new LinearLayout.LayoutParams(px2dp8, px2dp8);
                    setMargins(viewLayoutParams62, mContext, 0, 0, px2dp8, 0);
                    view62.setLayoutParams(viewLayoutParams62);
                    GradientDrawable mDrawable62 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{Color.parseColor("#38AA4A"), Color.parseColor("#38AA4A")});
                    mDrawable62.setCornerRadius(100);
                    view62.setBackground(mDrawable62);

                    final View view63 = new View(mContext);
                    LinearLayout.LayoutParams viewLayoutParams63 = new LinearLayout.LayoutParams(px2dp8, px2dp8);
                    setMargins(viewLayoutParams63, mContext, 0, 0, px2dp8, 0);
                    view63.setLayoutParams(viewLayoutParams63);
                    GradientDrawable mDrawable63 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{Color.parseColor("#FEBF32"), Color.parseColor("#FEBF32")});
                    mDrawable63.setCornerRadius(100);
                    view63.setBackground(mDrawable63);

                    final View view64 = new View(mContext);
                    LinearLayout.LayoutParams viewLayoutParams64 = new LinearLayout.LayoutParams(px2dp8, px2dp8);
                    setMargins(viewLayoutParams64, mContext, 0, 0, 0, 0);
                    view64.setLayoutParams(viewLayoutParams64);
                    GradientDrawable mDrawable64 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{Color.parseColor("#E33830"), Color.parseColor("#E33830")});
                    mDrawable64.setCornerRadius(100);
                    view64.setBackground(mDrawable64);

                    final LinearLayout container6 = new LinearLayout(mContext);
                    LinearLayout.LayoutParams layoutParams6 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams6.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
                    container6.setLayoutParams(layoutParams6);
                    container6.setOrientation(LinearLayout.HORIZONTAL);
                    setPaddings(container6, mContext, 16, 8, 16, 8);
                    setMargins(container6, mContext, sideMargin, topMargin, sideMargin, 8);
                    GradientDrawable mDrawable6 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{Color.parseColor("#090909"), Color.parseColor("#090909")});
                    mDrawable6.setCornerRadius(dp2px(mContext, radius2));
                    mDrawable6.setAlpha(102);
                    container6.setBackground(mDrawable6);
                    container6.setGravity(Gravity.CENTER);

                    container6.addView(time6);
                    container6.addView(view61);
                    container6.addView(view62);
                    container6.addView(view63);
                    container6.addView(view64);

                    return container6;
                case 7:
                    final TextClock time7 = new TextClock(mContext);
                    time7.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    time7.setFormat12Hour("hh.mm.");
                    time7.setFormat24Hour("HH.mm.");
                    time7.setTextColor(textColor);
                    time7.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28 * textScaling);
                    time7.setTypeface(typeface, Typeface.BOLD);
                    time7.setMaxLines(1);
                    time7.setIncludeFontPadding(false);
                    time7.setLetterSpacing(0.1f);

                    final TextClock second7 = new TextClock(mContext);
                    second7.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    second7.setFormat12Hour("ss");
                    second7.setFormat24Hour("ss");
                    second7.setTextColor(textColor);
                    second7.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20 * textScaling);
                    second7.setTypeface(typeface, Typeface.NORMAL);
                    second7.setMaxLines(1);
                    second7.setIncludeFontPadding(false);
                    second7.setLetterSpacing(0.1f);
                    second7.setAlpha(0.4f);
                    second7.setGravity(Gravity.BOTTOM);

                    final LinearLayout container7 = new LinearLayout(mContext);
                    LinearLayout.LayoutParams layoutParams7 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams7.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
                    container7.setLayoutParams(layoutParams7);
                    container7.setOrientation(LinearLayout.HORIZONTAL);
                    setMargins(container7, mContext, sideMargin, topMargin, sideMargin, 8);

                    container7.addView(time7);
                    container7.addView(second7);

                    return container7;
                case 8:
                    final TextClock clock8 = new TextClock(mContext);
                    clock8.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    clock8.setFormat12Hour("h:mm");
                    clock8.setFormat24Hour("H:mm");
                    clock8.setTextColor(textColor);
                    clock8.setTextSize(TypedValue.COMPLEX_UNIT_SP, 44 * textScaling);
                    clock8.setTypeface(typeface, Typeface.BOLD);
                    clock8.setMaxLines(1);
                    clock8.setIncludeFontPadding(false);

                    final TextClock clockOverlay8 = new TextClock(mContext);
                    clockOverlay8.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    clockOverlay8.setFormat12Hour("h");
                    clockOverlay8.setFormat24Hour("H");
                    clockOverlay8.setTextColor(accentPrimary);
                    clockOverlay8.setTextSize(TypedValue.COMPLEX_UNIT_SP, 44 * textScaling);
                    clockOverlay8.setTypeface(typeface, Typeface.BOLD);
                    clockOverlay8.setMaxLines(1);
                    clockOverlay8.setIncludeFontPadding(false);

                    final FrameLayout clockContainer8 = new FrameLayout(mContext);
                    clockContainer8.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    clockContainer8.addView(clock8);
                    clockContainer8.addView(clockOverlay8);

                    final TextClock dayDate8 = new TextClock(mContext);
                    dayDate8.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    dayDate8.setFormat12Hour("EEE d MMM");
                    dayDate8.setFormat24Hour("EEE d MMM");
                    dayDate8.setLetterSpacing(0.2f);
                    dayDate8.setAllCaps(true);
                    dayDate8.setTextColor(textColor);
                    dayDate8.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14 * textScaling);
                    dayDate8.setTypeface(clockOverlay8.getTypeface(), Typeface.NORMAL);
                    dayDate8.setIncludeFontPadding(false);
                    setMargins(dayDate8, mContext, 8, 0, 0, 4);

                    final LinearLayout container8 = new LinearLayout(mContext);
                    container8.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    container8.setGravity(Gravity.START | Gravity.BOTTOM);
                    container8.setOrientation(LinearLayout.HORIZONTAL);
                    setMargins(container8, mContext, sideMargin, topMargin, sideMargin, 8);

                    container8.addView(clockContainer8);
                    container8.addView(dayDate8);

                    return container8;
            }
        }
        return null;
    }
}
