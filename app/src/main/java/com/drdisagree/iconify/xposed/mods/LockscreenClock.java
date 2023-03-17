package com.drdisagree.iconify.xposed.mods;

import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_BOTTOMMARGIN;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_FONT_LINEHEIGHT;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_FONT_SWITCH;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_FONT_TEXT_SCALING;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_STYLE;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_SWITCH;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_TEXT_WHITE;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_TOPMARGIN;
import static com.drdisagree.iconify.config.XPrefs.Xprefs;
import static com.drdisagree.iconify.xposed.HookRes.resparams;

import android.annotation.SuppressLint;
import android.content.Context;
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
import android.widget.RelativeLayout;
import android.widget.TextClock;

import com.drdisagree.iconify.utils.XSystemUtil;
import com.drdisagree.iconify.xposed.ModPack;

import java.io.File;
import java.util.Objects;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class LockscreenClock extends ModPack implements IXposedHookLoadPackage {

    private static final String TAG = "Iconify - LockscreenClock: ";
    boolean showLockscreenClock = false;
    int topMargin = 100;
    int bottomMargin = 40;
    int lockscreenClockStyle = 0;
    int lineHeight = 0;
    float textScaling = 1;
    boolean clockAdded = false;
    boolean customFontEnabled = false;
    boolean forceWhiteText = false;
    private LinearLayout status_view_container = null;

    public LockscreenClock(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;

        showLockscreenClock = Xprefs.getBoolean(LSCLOCK_SWITCH, false);
        lockscreenClockStyle = Xprefs.getInt(LSCLOCK_STYLE, 0);
        topMargin = Xprefs.getInt(LSCLOCK_TOPMARGIN, 100);
        bottomMargin = Xprefs.getInt(LSCLOCK_BOTTOMMARGIN, 40);
        lineHeight = Xprefs.getInt(LSCLOCK_FONT_LINEHEIGHT, 0);
        customFontEnabled = Xprefs.getBoolean(LSCLOCK_FONT_SWITCH, false);
        forceWhiteText = Xprefs.getBoolean(LSCLOCK_TEXT_WHITE, false);
        textScaling = (float) (Xprefs.getInt(LSCLOCK_FONT_TEXT_SCALING, 10) / 10.0);

        if (Key.length > 0 && (Objects.equals(Key[0], LSCLOCK_SWITCH) || Objects.equals(Key[0], LSCLOCK_STYLE) || Objects.equals(Key[0], LSCLOCK_TOPMARGIN) || Objects.equals(Key[0], LSCLOCK_BOTTOMMARGIN) || Objects.equals(Key[0], LSCLOCK_FONT_LINEHEIGHT) || Objects.equals(Key[0], LSCLOCK_FONT_SWITCH) || Objects.equals(Key[0], LSCLOCK_TEXT_WHITE) || Objects.equals(Key[0], LSCLOCK_FONT_TEXT_SCALING))) {
            if (status_view_container == null || status_view_container.getChildCount() >= 3) {
                removeAddedClock();
            }
            setLockscreenClock();
            hideStockClockDate();
        }
    }

    @Override
    public boolean listensTo(String packageName) {
        return packageName.equals(SYSTEMUI_PACKAGE);
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!lpparam.packageName.equals(SYSTEMUI_PACKAGE)) return;

        setLockscreenClock();
        hideStockClockDate();
    }

    private void setLockscreenClock() {
        XC_InitPackageResources.InitPackageResourcesParam ourResparam = resparams.get(SYSTEMUI_PACKAGE);
        if (ourResparam == null) return;

        try {
            ourResparam.res.hookLayout(SYSTEMUI_PACKAGE, "layout", "keyguard_status_view", new XC_LayoutInflated() {
                @SuppressLint({"DiscouragedApi"})
                @Override
                public void handleLayoutInflated(LayoutInflatedParam liparam) {
                    if (!showLockscreenClock) return;

                    status_view_container = liparam.view.findViewById(liparam.res.getIdentifier("status_view_container", "id", SYSTEMUI_PACKAGE));

                    if (status_view_container.getChildCount() >= 3) return;

                    Typeface typeface = null;

                    if (customFontEnabled && (new File(Environment.getExternalStorageDirectory() + "/.iconify_files/lsclock_font.ttf").exists()))
                        typeface = Typeface.createFromFile(new File(Environment.getExternalStorageDirectory() + "/.iconify_files/lsclock_font.ttf"));

                    switch (lockscreenClockStyle) {
                        case 0:
                            final LinearLayout blank0 = new LinearLayout(mContext);
                            LinearLayout.LayoutParams blankParams0 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            blankParams0.setMargins(0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, topMargin, mContext.getResources().getDisplayMetrics()), 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, bottomMargin, mContext.getResources().getDisplayMetrics()));
                            blank0.setLayoutParams(blankParams0);
                            blank0.setOrientation(LinearLayout.VERTICAL);

                            status_view_container.addView(blank0, 0);
                            break;
                        case 1:
                            final TextClock date1 = new TextClock(mContext);
                            date1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            date1.setFormat12Hour("EEEE d MMMM");
                            date1.setFormat24Hour("EEEE d MMMM");
                            date1.setTextColor(mContext.getResources().getColor(forceWhiteText ? android.R.color.white : android.R.color.holo_blue_light));
                            date1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20 * textScaling);
                            date1.setTypeface(typeface != null ? typeface : date1.getTypeface(), Typeface.BOLD);
                            ViewGroup.MarginLayoutParams dateParams1 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                            dateParams1.setMargins(0, 0, 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -16, mContext.getResources().getDisplayMetrics()));
                            date1.setLayoutParams(dateParams1);

                            final TextClock clock1 = new TextClock(mContext);
                            clock1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            clock1.setFormat12Hour("hh:mm");
                            clock1.setFormat24Hour("HH:mm");
                            clock1.setTextColor(mContext.getResources().getColor(forceWhiteText ? android.R.color.white : android.R.color.holo_blue_light));
                            clock1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 100 * textScaling);
                            clock1.setTypeface(typeface != null ? typeface : clock1.getTypeface(), Typeface.BOLD);
                            ViewGroup.MarginLayoutParams clockParams1 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                            clockParams1.setMargins(0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, lineHeight, mContext.getResources().getDisplayMetrics()), 0, 0);
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

                            status_view_container.addView(clockContainer1, 0);
                            break;
                        case 2:
                            final TextClock day2 = new TextClock(mContext);
                            day2.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            day2.setFormat12Hour("EEEE");
                            day2.setFormat24Hour("EEEE");
                            day2.setTextColor(forceWhiteText ? mContext.getResources().getColor(android.R.color.white) : XSystemUtil.getColorResCompat(mContext, android.R.attr.textColorPrimary));
                            day2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50 * textScaling);
                            day2.setTypeface(typeface != null ? typeface : day2.getTypeface());

                            final TextClock clock2 = new TextClock(mContext);
                            clock2.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            clock2.setFormat12Hour("hh:mm");
                            clock2.setFormat24Hour("HH:mm");
                            clock2.setTextColor(forceWhiteText ? mContext.getResources().getColor(android.R.color.white) : XSystemUtil.getColorResCompat(mContext, android.R.attr.textColorPrimary));
                            clock2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50 * textScaling);
                            clock2.setTypeface(typeface != null ? typeface : clock2.getTypeface());

                            final TextClock clockOverlay2 = new TextClock(mContext);
                            clockOverlay2.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            clockOverlay2.setFormat12Hour("hh");
                            clockOverlay2.setFormat24Hour("HH");
                            clockOverlay2.setTextColor(mContext.getResources().getColor(forceWhiteText ? android.R.color.white : android.R.color.holo_blue_light));
                            clockOverlay2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50 * textScaling);
                            clockOverlay2.setMaxLines(1);
                            clockOverlay2.setTypeface(typeface != null ? typeface : clockOverlay2.getTypeface());
                            int maxLength2 = 1;
                            InputFilter[] fArray2 = new InputFilter[1];
                            fArray2[0] = new InputFilter.LengthFilter(maxLength2);
                            clockOverlay2.setFilters(fArray2);

                            final FrameLayout clockContainer2 = new FrameLayout(mContext);
                            clockContainer2.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            ((FrameLayout.LayoutParams) clockContainer2.getLayoutParams()).setMargins(0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -12 + lineHeight, mContext.getResources().getDisplayMetrics()), 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -8 + lineHeight, mContext.getResources().getDisplayMetrics()));

                            clockContainer2.addView(clock2);
                            clockContainer2.addView(clockOverlay2);

                            final TextClock month2 = new TextClock(mContext);
                            month2.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            month2.setFormat12Hour("MMMM d");
                            month2.setFormat24Hour("MMMM d");
                            month2.setTextColor(forceWhiteText ? mContext.getResources().getColor(android.R.color.white) : XSystemUtil.getColorResCompat(mContext, android.R.attr.textColorPrimary));
                            month2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20 * textScaling);
                            month2.setTypeface(typeface != null ? typeface : month2.getTypeface());

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

                            status_view_container.addView(wholeContainer2, 0);
                            break;
                        case 3:
                            final TextClock date3 = new TextClock(mContext);
                            date3.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            date3.setFormat12Hour("EEE, MMM dd");
                            date3.setFormat24Hour("EEE, MMM dd");
                            date3.setTextColor(mContext.getResources().getColor(forceWhiteText ? android.R.color.white : android.R.color.holo_blue_light));
                            date3.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24 * textScaling);
                            date3.setTypeface(typeface != null ? typeface : date3.getTypeface(), Typeface.BOLD);

                            final TextClock clockHour3 = new TextClock(mContext);
                            clockHour3.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            clockHour3.setFormat12Hour("hh");
                            clockHour3.setFormat24Hour("HH");
                            clockHour3.setTextColor(mContext.getResources().getColor(forceWhiteText ? android.R.color.white : android.R.color.holo_blue_light));
                            clockHour3.setTextSize(TypedValue.COMPLEX_UNIT_SP, 160 * textScaling);
                            clockHour3.setTypeface(typeface != null ? typeface : clockHour3.getTypeface(), Typeface.BOLD);
                            ViewGroup.MarginLayoutParams clockHourParams3 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                            clockHourParams3.setMargins(0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -30 + lineHeight, mContext.getResources().getDisplayMetrics()), 0, 0);
                            clockHour3.setLayoutParams(clockHourParams3);

                            final TextClock clockMinute3 = new TextClock(mContext);
                            clockMinute3.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            clockMinute3.setFormat12Hour("mm");
                            clockMinute3.setFormat24Hour("mm");
                            clockMinute3.setTextColor(mContext.getResources().getColor(forceWhiteText ? android.R.color.white : android.R.color.holo_blue_light));
                            clockMinute3.setTextSize(TypedValue.COMPLEX_UNIT_SP, 160 * textScaling);
                            clockMinute3.setTypeface(typeface != null ? typeface : clockMinute3.getTypeface(), Typeface.BOLD);
                            ViewGroup.MarginLayoutParams clockMinuteParams3 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                            clockMinuteParams3.setMargins(0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -80 + lineHeight, mContext.getResources().getDisplayMetrics()), 0, 0);
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

                            status_view_container.addView(clockContainer3, 0);
                            break;
                        case 4:
                            final AnalogClock analogClock4 = new AnalogClock(mContext);
                            analogClock4.setLayoutParams(new LinearLayout.LayoutParams((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 180 * textScaling, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 180 * textScaling, mContext.getResources().getDisplayMetrics())));
                            ((LinearLayout.LayoutParams) analogClock4.getLayoutParams()).gravity = Gravity.CENTER_HORIZONTAL;

                            final TextClock day4 = new TextClock(mContext);
                            day4.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            day4.setFormat12Hour("EEE dd MMM");
                            day4.setFormat24Hour("EEE dd MMM");
                            day4.setTextColor(mContext.getResources().getColor(android.R.color.system_neutral1_200));
                            day4.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28 * textScaling);
                            day4.setTypeface(typeface != null ? typeface : day4.getTypeface(), Typeface.BOLD);
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

                            status_view_container.addView(clockContainer4, 0);
                            break;
                        case 5:
                            final TextClock hour5 = new TextClock(mContext);
                            hour5.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            hour5.setFormat12Hour("hh");
                            hour5.setFormat24Hour("HH");
                            hour5.setTextColor(mContext.getResources().getColor(android.R.color.white));
                            hour5.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40 * textScaling);
                            hour5.setTypeface(typeface != null ? typeface : hour5.getTypeface(), Typeface.BOLD);

                            final TextClock minute5 = new TextClock(mContext);
                            minute5.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            minute5.setFormat12Hour("mm");
                            minute5.setFormat24Hour("mm");
                            minute5.setTextColor(mContext.getResources().getColor(android.R.color.white));
                            minute5.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40 * textScaling);
                            minute5.setTypeface(typeface != null ? typeface : minute5.getTypeface(), Typeface.BOLD);
                            ViewGroup.MarginLayoutParams minuteParams5 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                            minuteParams5.setMargins(0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4 + lineHeight, mContext.getResources().getDisplayMetrics()), 0, 0);
                            minute5.setLayoutParams(minuteParams5);

                            final LinearLayout time5 = new LinearLayout(mContext);
                            LinearLayout.LayoutParams timeLayoutParams5 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
                            timeLayoutParams5.gravity = Gravity.CENTER;
                            time5.setLayoutParams(timeLayoutParams5);
                            time5.setOrientation(LinearLayout.VERTICAL);
                            time5.setPadding((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, mContext.getResources().getDisplayMetrics()));
                            GradientDrawable timeDrawable5 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{mContext.getResources().getColor(android.R.color.black), mContext.getResources().getColor(android.R.color.black)});
                            timeDrawable5.setCornerRadius((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, mContext.getResources().getDisplayMetrics()));
                            time5.setBackground(timeDrawable5);
                            time5.setGravity(Gravity.CENTER);

                            time5.addView(hour5);
                            time5.addView(minute5);

                            final TextClock day5 = new TextClock(mContext);
                            day5.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            day5.setFormat12Hour("EEE");
                            day5.setFormat24Hour("EEE");
                            day5.setAllCaps(true);
                            day5.setTextColor(forceWhiteText ? mContext.getResources().getColor(android.R.color.white) : XSystemUtil.getColorResCompat(mContext, android.R.attr.textColorPrimary));
                            day5.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28 * textScaling);
                            day5.setTypeface(typeface != null ? typeface : day5.getTypeface(), Typeface.BOLD);
                            day5.setLetterSpacing(0.2f);

                            final TextClock date5 = new TextClock(mContext);
                            date5.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            date5.setFormat12Hour("dd");
                            date5.setFormat24Hour("dd");
                            date5.setTextColor(forceWhiteText ? mContext.getResources().getColor(android.R.color.white) : XSystemUtil.getColorResCompat(mContext, android.R.attr.textColorPrimary));
                            date5.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28 * textScaling);
                            date5.setTypeface(typeface != null ? typeface : date5.getTypeface(), Typeface.BOLD);
                            date5.setLetterSpacing(0.2f);
                            ViewGroup.MarginLayoutParams dateParams5 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                            dateParams5.setMargins(0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -2 + lineHeight, mContext.getResources().getDisplayMetrics()), 0, 0);
                            date5.setLayoutParams(dateParams5);

                            final TextClock month5 = new TextClock(mContext);
                            month5.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            month5.setFormat12Hour("MMM");
                            month5.setFormat24Hour("MMM");
                            month5.setAllCaps(true);
                            month5.setTextColor(forceWhiteText ? mContext.getResources().getColor(android.R.color.white) : XSystemUtil.getColorResCompat(mContext, android.R.attr.textColorPrimary));
                            month5.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28 * textScaling);
                            month5.setTypeface(typeface != null ? typeface : month5.getTypeface(), Typeface.BOLD);
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
                            GradientDrawable mDrawable5 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{mContext.getResources().getColor(android.R.color.holo_blue_light), mContext.getResources().getColor(android.R.color.holo_blue_light)});
                            mDrawable5.setCornerRadius((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 28, mContext.getResources().getDisplayMetrics()));
                            container5.setBackground(mDrawable5);

                            container5.addView(time5);
                            container5.addView(right5);

                            status_view_container.addView(container5, 0);
                            break;
                    }
                    clockAdded = true;
                }
            });
        } catch (Throwable ignored) {
        }
    }

    private void removeAddedClock() {
        XC_InitPackageResources.InitPackageResourcesParam ourResparam = resparams.get(SYSTEMUI_PACKAGE);
        if (ourResparam == null) return;

        try {
            ourResparam.res.hookLayout(SYSTEMUI_PACKAGE, "layout", "keyguard_status_view", new XC_LayoutInflated() {
                @SuppressLint({"DiscouragedApi"})
                @Override
                public void handleLayoutInflated(LayoutInflatedParam liparam) {
                    if (!showLockscreenClock) return;

                    status_view_container = liparam.view.findViewById(liparam.res.getIdentifier("status_view_container", "id", SYSTEMUI_PACKAGE));

                    if (clockAdded && status_view_container.getChildCount() >= 3)
                        status_view_container.removeViewAt(0);
                }
            });
        } catch (Throwable ignored) {
        }
    }

    private void hideStockClockDate() {
        XC_InitPackageResources.InitPackageResourcesParam ourResparam = resparams.get(SYSTEMUI_PACKAGE);
        if (ourResparam == null) return;

        try {
            ourResparam.res.hookLayout(SYSTEMUI_PACKAGE, "layout", "keyguard_status_view", new XC_LayoutInflated() {
                @SuppressLint({"DiscouragedApi"})
                @Override
                public void handleLayoutInflated(LayoutInflatedParam liparam) {
                    if (!showLockscreenClock) return;

                    @SuppressLint("DiscouragedApi") RelativeLayout keyguard_clock_container = liparam.view.findViewById(liparam.res.getIdentifier("keyguard_clock_container", "id", SYSTEMUI_PACKAGE));
                    keyguard_clock_container.getLayoutParams().height = 0;
                    keyguard_clock_container.getLayoutParams().width = 0;

                    @SuppressLint("DiscouragedApi") FrameLayout status_view_media_container = liparam.view.findViewById(liparam.res.getIdentifier("status_view_media_container", "id", SYSTEMUI_PACKAGE));
                    status_view_media_container.getLayoutParams().height = 0;
                    status_view_media_container.getLayoutParams().width = 0;
                }
            });
        } catch (Throwable ignored) {
        }
    }
}