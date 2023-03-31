package com.drdisagree.iconify.ui.views;

import static com.drdisagree.iconify.common.Preferences.UI_CORNER_RADIUS;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.InputFilter;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.AnalogClock;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextClock;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.RPrefs;
import com.drdisagree.iconify.utils.XSystemUtil;

public class HeaderClockStyles {

    static int sideMargin = 0;
    static int topMargin = 0;
    static boolean forceWhiteText = false;
    static float textScaling = 1f;

    public static LinearLayout initHeaderClockStyle(Context mContext, int style) {
        LinearLayout container = new LinearLayout(mContext);
        container.setGravity(Gravity.START | Gravity.CENTER);

        switch (style) {
            case 1:
                final TextClock clockHour1 = new TextClock(mContext);
                clockHour1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                clockHour1.setFormat12Hour("hh");
                clockHour1.setFormat24Hour("HH");
                clockHour1.setTextColor(mContext.getResources().getColor(R.color.holo_blue_light));
                clockHour1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 40 * textScaling);
                clockHour1.setTypeface(clockHour1.getTypeface(), Typeface.BOLD);
                LinearLayout.LayoutParams clockHourParams1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                clockHourParams1.setMargins(0, 0, 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, mContext.getResources().getDisplayMetrics()));
                clockHour1.setLayoutParams(clockHourParams1);

                final TextClock clockMinute1 = new TextClock(mContext);
                clockMinute1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                clockMinute1.setFormat12Hour(":mm");
                clockMinute1.setFormat24Hour(":mm");
                clockMinute1.setTextColor(forceWhiteText ? mContext.getResources().getColor(R.color.white) : XSystemUtil.getColorResCompat(mContext, android.R.attr.textColorPrimary));
                clockMinute1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 40 * textScaling);
                clockMinute1.setTypeface(clockMinute1.getTypeface(), Typeface.BOLD);
                LinearLayout.LayoutParams clockMinuteParams1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                clockMinuteParams1.setMargins(0, 0, 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, mContext.getResources().getDisplayMetrics()));
                clockMinute1.setLayoutParams(clockMinuteParams1);

                final LinearLayout divider1 = new LinearLayout(mContext);
                ViewGroup.MarginLayoutParams dividerParams1 = new ViewGroup.MarginLayoutParams((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40 * textScaling, mContext.getResources().getDisplayMetrics()));
                dividerParams1.setMargins((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, mContext.getResources().getDisplayMetrics()));
                divider1.setLayoutParams(dividerParams1);
                GradientDrawable mDrawable1 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{mContext.getResources().getColor(R.color.holo_green_light), mContext.getResources().getColor(R.color.holo_green_light)});
                mDrawable1.setCornerRadius(8);
                divider1.setBackground(mDrawable1);

                final TextClock clockDay1 = new TextClock(mContext);
                clockDay1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                clockDay1.setFormat12Hour("EEEE");
                clockDay1.setFormat24Hour("EEEE");
                clockDay1.setTextColor(forceWhiteText ? mContext.getResources().getColor(R.color.white) : XSystemUtil.getColorResCompat(mContext, android.R.attr.textColorPrimary));
                clockDay1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14 * textScaling);
                clockDay1.setTypeface(clockDay1.getTypeface(), Typeface.BOLD);

                final TextClock clockDate1 = new TextClock(mContext);
                clockDate1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                clockDate1.setFormat12Hour("dd MMMM");
                clockDate1.setFormat24Hour("dd MMMM");
                clockDate1.setTextColor(forceWhiteText ? mContext.getResources().getColor(R.color.white) : XSystemUtil.getColorResCompat(mContext, android.R.attr.textColorPrimary));
                clockDate1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14 * textScaling);
                clockDate1.setTypeface(clockDate1.getTypeface(), Typeface.BOLD);

                final LinearLayout dateContainer1 = new LinearLayout(mContext);
                dateContainer1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
                dateContainer1.setOrientation(LinearLayout.VERTICAL);
                ((LinearLayout.LayoutParams) dateContainer1.getLayoutParams()).setMargins(0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, mContext.getResources().getDisplayMetrics()), 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, mContext.getResources().getDisplayMetrics()));

                dateContainer1.addView(clockDay1);
                dateContainer1.addView(clockDate1);

                final LinearLayout clockContainer1 = new LinearLayout(mContext);
                clockContainer1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                clockContainer1.setGravity(Gravity.CENTER_VERTICAL);
                clockContainer1.setOrientation(LinearLayout.HORIZONTAL);
                ((LinearLayout.LayoutParams) clockContainer1.getLayoutParams()).setMargins((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, sideMargin, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, topMargin, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, sideMargin, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, mContext.getResources().getDisplayMetrics()));

                clockContainer1.addView(clockHour1);
                clockContainer1.addView(clockMinute1);
                clockContainer1.addView(divider1);
                clockContainer1.addView(dateContainer1);

                container = clockContainer1;
                break;
            case 2:
                final TextClock clock2 = new TextClock(mContext);
                clock2.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                clock2.setFormat12Hour("h:mm");
                clock2.setFormat24Hour("H:mm");
                clock2.setTextColor(forceWhiteText ? mContext.getResources().getColor(R.color.white) : XSystemUtil.getColorResCompat(mContext, android.R.attr.textColorPrimary));
                clock2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 40 * textScaling);
                clock2.setTypeface(clock2.getTypeface(), Typeface.BOLD);

                final TextClock clockOverlay2 = new TextClock(mContext);
                clockOverlay2.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                clockOverlay2.setFormat12Hour("h");
                clockOverlay2.setFormat24Hour("H");
                clockOverlay2.setTextColor(mContext.getResources().getColor(R.color.holo_blue_light));
                clockOverlay2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 40 * textScaling);
                clockOverlay2.setTypeface(clockOverlay2.getTypeface(), Typeface.BOLD);
                clockOverlay2.setMaxLines(1);
                int maxLength2 = 1;
                InputFilter[] fArray2 = new InputFilter[1];
                fArray2[0] = new InputFilter.LengthFilter(maxLength2);
                clockOverlay2.setFilters(fArray2);

                final FrameLayout clockContainer2 = new FrameLayout(mContext);
                clockContainer2.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                ((FrameLayout.LayoutParams) clockContainer2.getLayoutParams()).setMargins(0, 0, 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -8, mContext.getResources().getDisplayMetrics()));

                clockContainer2.addView(clock2);
                clockContainer2.addView(clockOverlay2);

                final TextClock dayDate2 = new TextClock(mContext);
                dayDate2.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                dayDate2.setFormat12Hour("EEEE, MMM dd");
                dayDate2.setFormat24Hour("EEEE, MMM dd");
                dayDate2.setTextColor(forceWhiteText ? mContext.getResources().getColor(R.color.white) : XSystemUtil.getColorResCompat(mContext, android.R.attr.textColorPrimary));
                dayDate2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18 * textScaling);
                dayDate2.setTypeface(clockOverlay2.getTypeface(), Typeface.BOLD);

                final LinearLayout container2 = new LinearLayout(mContext);
                container2.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                container2.setGravity(Gravity.CENTER_VERTICAL);
                container2.setOrientation(LinearLayout.VERTICAL);
                ((LinearLayout.LayoutParams) container2.getLayoutParams()).setMargins((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, sideMargin, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, topMargin, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, sideMargin, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, mContext.getResources().getDisplayMetrics()));

                container2.addView(clockContainer2);
                container2.addView(dayDate2);

                container = container2;
                break;
            case 3:
                final TextClock clock3 = new TextClock(mContext);
                clock3.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                clock3.setFormat12Hour("hh:mm");
                clock3.setFormat24Hour("HH:mm");
                clock3.setTextColor(forceWhiteText ? mContext.getResources().getColor(R.color.white) : XSystemUtil.getColorResCompat(mContext, android.R.attr.textColorPrimary));
                clock3.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 28 * textScaling);
                clock3.setTypeface(clock3.getTypeface(), Typeface.BOLD);

                final TextClock clockOverlay3 = new TextClock(mContext);
                clockOverlay3.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                clockOverlay3.setFormat12Hour("hh:mm");
                clockOverlay3.setFormat24Hour("HH:mm");
                clockOverlay3.setTextColor(mContext.getResources().getColor(R.color.holo_blue_light));
                clockOverlay3.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 28 * textScaling);
                clockOverlay3.setTypeface(clockOverlay3.getTypeface(), Typeface.BOLD);
                clockOverlay3.setAlpha(0.2f);
                LinearLayout.LayoutParams clockOverlayParams3 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                clockOverlayParams3.setMargins(6, 6, 0, 0);
                clockOverlay3.setLayoutParams(clockOverlayParams3);

                final FrameLayout clockContainer3 = new FrameLayout(mContext);
                clockContainer3.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                ((FrameLayout.LayoutParams) clockContainer3.getLayoutParams()).setMargins(0, 0, 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -12, mContext.getResources().getDisplayMetrics()));
                clockContainer3.addView(clockOverlay3);
                clockContainer3.addView(clock3);

                final TextClock dayDate3 = new TextClock(mContext);
                dayDate3.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                dayDate3.setFormat12Hour("EEE, MMM dd");
                dayDate3.setFormat24Hour("EEE, MMM dd");
                dayDate3.setTextColor(forceWhiteText ? mContext.getResources().getColor(R.color.white) : XSystemUtil.getColorResCompat(mContext, android.R.attr.textColorPrimary));
                dayDate3.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18 * textScaling);
                dayDate3.setTypeface(clockOverlay3.getTypeface(), Typeface.BOLD);

                final TextClock dayDateOverlay3 = new TextClock(mContext);
                dayDateOverlay3.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                dayDateOverlay3.setFormat12Hour("EEE, MMM dd");
                dayDateOverlay3.setFormat24Hour("EEE, MMM dd");
                dayDateOverlay3.setTextColor(mContext.getResources().getColor(R.color.holo_green_light));
                dayDateOverlay3.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18 * textScaling);
                dayDateOverlay3.setTypeface(dayDateOverlay3.getTypeface(), Typeface.BOLD);
                dayDateOverlay3.setAlpha(0.2f);
                LinearLayout.LayoutParams dayDateOverlayParams3 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                dayDateOverlayParams3.setMargins(6, 6, 0, 0);
                dayDateOverlay3.setLayoutParams(dayDateOverlayParams3);

                final FrameLayout dayDateContainer3 = new FrameLayout(mContext);
                dayDateContainer3.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                ((FrameLayout.LayoutParams) dayDateContainer3.getLayoutParams()).setMargins(0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, mContext.getResources().getDisplayMetrics()), 0, 0);
                dayDateContainer3.addView(dayDateOverlay3);
                dayDateContainer3.addView(dayDate3);

                final LinearLayout container3 = new LinearLayout(mContext);
                container3.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                container3.setGravity(Gravity.BOTTOM);
                container3.setOrientation(LinearLayout.VERTICAL);
                ((LinearLayout.LayoutParams) container3.getLayoutParams()).setMargins((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, sideMargin, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, topMargin, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, sideMargin, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, mContext.getResources().getDisplayMetrics()));

                container3.addView(clockContainer3);
                container3.addView(dayDateContainer3);

                container = container3;
                break;
            case 4:
                final AnalogClock analogClock4 = new AnalogClock(mContext);
                analogClock4.setLayoutParams(new LinearLayout.LayoutParams((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48 * textScaling, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48 * textScaling, mContext.getResources().getDisplayMetrics())));
                ((LinearLayout.LayoutParams) analogClock4.getLayoutParams()).gravity = Gravity.CENTER_HORIZONTAL;

                final TextClock clockDay4 = new TextClock(mContext);
                clockDay4.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                clockDay4.setFormat12Hour("EEEE");
                clockDay4.setFormat24Hour("EEEE");
                clockDay4.setTextColor(mContext.getResources().getColor(R.color.holo_blue_light));
                clockDay4.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16 * textScaling);
                clockDay4.setTypeface(clockDay4.getTypeface(), Typeface.BOLD);

                final TextClock clockDate4 = new TextClock(mContext);
                clockDate4.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                clockDate4.setFormat12Hour("dd MMMM");
                clockDate4.setFormat24Hour("dd MMMM");
                clockDate4.setTextColor(forceWhiteText ? mContext.getResources().getColor(R.color.white) : XSystemUtil.getColorResCompat(mContext, android.R.attr.textColorPrimary));
                clockDate4.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16 * textScaling);
                clockDate4.setTypeface(clockDate4.getTypeface(), Typeface.BOLD);

                final LinearLayout dateContainer4 = new LinearLayout(mContext);
                dateContainer4.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
                dateContainer4.setOrientation(LinearLayout.VERTICAL);
                ((LinearLayout.LayoutParams) dateContainer4.getLayoutParams()).setMargins((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, mContext.getResources().getDisplayMetrics()), 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, mContext.getResources().getDisplayMetrics()), 0);

                dateContainer4.addView(clockDay4);
                dateContainer4.addView(clockDate4);

                final LinearLayout clockContainer4 = new LinearLayout(mContext);
                clockContainer4.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                clockContainer4.setGravity(Gravity.CENTER_VERTICAL);
                clockContainer4.setOrientation(LinearLayout.HORIZONTAL);
                ((LinearLayout.LayoutParams) clockContainer4.getLayoutParams()).setMargins((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, sideMargin, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, topMargin, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, sideMargin, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, mContext.getResources().getDisplayMetrics()));

                clockContainer4.addView(analogClock4);
                clockContainer4.addView(dateContainer4);

                container = clockContainer4;
                break;
            case 5:
                final TextClock time5 = new TextClock(mContext);
                time5.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                time5.setFormat12Hour("hh:mm");
                time5.setFormat24Hour("HH:mm");
                time5.setTextColor(mContext.getResources().getColor(R.color.white));
                time5.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14 * textScaling);
                time5.setTypeface(time5.getTypeface(), Typeface.BOLD);
                time5.setMaxLines(1);


                final LinearLayout timeContainer5 = new LinearLayout(mContext);
                LinearLayout.LayoutParams timeLayoutParams5 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                timeLayoutParams5.gravity = Gravity.CENTER;
                timeContainer5.setLayoutParams(timeLayoutParams5);
                timeContainer5.setOrientation(LinearLayout.VERTICAL);
                timeContainer5.setPadding((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, mContext.getResources().getDisplayMetrics()));
                GradientDrawable timeDrawable5 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{mContext.getResources().getColor(R.color.black), mContext.getResources().getColor(R.color.black)});
                timeDrawable5.setCornerRadius((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (RPrefs.getInt(UI_CORNER_RADIUS, 16) + 6) * mContext.getResources().getDisplayMetrics().density, mContext.getResources().getDisplayMetrics()));
                timeContainer5.setBackground(timeDrawable5);
                timeContainer5.setGravity(Gravity.CENTER);

                timeContainer5.addView(time5);

                final TextClock date5 = new TextClock(mContext);
                date5.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                date5.setFormat12Hour("EEE, MMM dd");
                date5.setFormat24Hour("EEE, MMM dd");
                date5.setTextColor(forceWhiteText ? mContext.getResources().getColor(R.color.white) : XSystemUtil.getColorResCompat(mContext, android.R.attr.textColorPrimary));
                date5.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14 * textScaling);
                date5.setTypeface(date5.getTypeface(), Typeface.BOLD);
                ViewGroup.MarginLayoutParams dateParams5 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                dateParams5.setMarginStart((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, mContext.getResources().getDisplayMetrics()));
                dateParams5.setMarginEnd((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, mContext.getResources().getDisplayMetrics()));
                date5.setLayoutParams(dateParams5);
                date5.setMaxLines(1);

                final LinearLayout container5 = new LinearLayout(mContext);
                LinearLayout.LayoutParams layoutParams5 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                container5.setLayoutParams(layoutParams5);
                container5.setGravity(Gravity.CENTER);
                container5.setOrientation(LinearLayout.HORIZONTAL);
                container5.setPadding((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, mContext.getResources().getDisplayMetrics()));
                GradientDrawable mDrawable5 = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{mContext.getResources().getColor(R.color.holo_blue_light), mContext.getResources().getColor(R.color.holo_green_light)});
                ((LinearLayout.LayoutParams) container5.getLayoutParams()).setMargins((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, sideMargin, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, topMargin, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, sideMargin, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, mContext.getResources().getDisplayMetrics()));
                mDrawable5.setCornerRadius((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (RPrefs.getInt(UI_CORNER_RADIUS, 16) + 8) * mContext.getResources().getDisplayMetrics().density, mContext.getResources().getDisplayMetrics()));
                container5.setBackground(mDrawable5);

                container5.addView(timeContainer5);
                container5.addView(date5);

                container = container5;
                break;
        }
        return container;
    }
}
