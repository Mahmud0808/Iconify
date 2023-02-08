package com.drdisagree.iconify.xposed.mods;

import static com.drdisagree.iconify.common.References.LSCLOCK_BOTTOMMARGIN;
import static com.drdisagree.iconify.common.References.LSCLOCK_FONT_LINEHEIGHT;
import static com.drdisagree.iconify.common.References.LSCLOCK_FONT_SWITCH;
import static com.drdisagree.iconify.common.References.LSCLOCK_FONT_TEXT_SCALING;
import static com.drdisagree.iconify.common.References.LSCLOCK_STYLE;
import static com.drdisagree.iconify.common.References.LSCLOCK_SWITCH;
import static com.drdisagree.iconify.common.References.LSCLOCK_TEXT_WHITE;
import static com.drdisagree.iconify.common.References.LSCLOCK_TOPMARGIN;
import static com.drdisagree.iconify.common.References.SYSTEM_UI_PACKAGE;
import static com.drdisagree.iconify.config.XPrefs.Xprefs;
import static com.drdisagree.iconify.xposed.HookRes.resparams;
import static de.robv.android.xposed.XposedBridge.log;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
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

import androidx.annotation.AttrRes;

import com.drdisagree.iconify.xposed.ModPack;

import java.io.File;

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
    boolean customFontEnabled = false;
    boolean forceWhiteText = false;
    private String rootPackagePath = "";
    private LinearLayout status_view_container = null;

    public LockscreenClock(Context context) {
        super(context);
        if (!listensTo(context.getPackageName())) return;
    }

    private static int getColorResCompat(Context context, @AttrRes int id) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(id, typedValue, false);
        @SuppressLint("Recycle") TypedArray arr = context.obtainStyledAttributes(typedValue.data, new int[]{id});
        return arr.getColor(0, -1);
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

        if (status_view_container != null) {
            if (status_view_container.getChildCount() >= 3)
                return;
        }
        setHeaderClock();
        hideStockClockDate();
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
    }

    private void setHeaderClock() {
        XC_InitPackageResources.InitPackageResourcesParam ourResparam = resparams.get(SYSTEM_UI_PACKAGE);
        if (ourResparam == null) return;

        if (!showLockscreenClock)
            return;

        try {
            ourResparam.res.hookLayout(SYSTEM_UI_PACKAGE, "layout", "keyguard_status_view", new XC_LayoutInflated() {
                @SuppressLint({"DiscouragedApi"})
                @Override
                public void handleLayoutInflated(LayoutInflatedParam liparam) {
                    status_view_container = liparam.view.findViewById(liparam.res.getIdentifier("status_view_container", "id", SYSTEM_UI_PACKAGE));
                    if (status_view_container.getChildCount() >= 3) {
                        return;
                    }

                    Typeface typeface = null;

                    if (customFontEnabled && (new File(Environment.getExternalStorageDirectory() + "/.iconify_files/lsclock_font.ttf").exists()))
                        typeface = Typeface.createFromFile(new File(Environment.getExternalStorageDirectory() + "/.iconify_files/lsclock_font.ttf"));

                    switch (lockscreenClockStyle) {
                        case 0:
                            final TextClock date0 = new TextClock(mContext);
                            date0.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            date0.setFormat12Hour("EEEE d MMMM");
                            date0.setFormat24Hour("EEEE d MMMM");
                            date0.setTextColor(mContext.getResources().getColor(forceWhiteText ? android.R.color.white : android.R.color.holo_blue_light));
                            date0.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20 * textScaling);
                            date0.setTypeface(typeface != null ? typeface : date0.getTypeface(), Typeface.BOLD);
                            ViewGroup.MarginLayoutParams dateParams0 = new ViewGroup.MarginLayoutParams(
                                    ViewGroup.MarginLayoutParams.WRAP_CONTENT,
                                    ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                            dateParams0.setMargins(
                                    0,
                                    0,
                                    0,
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -16, mContext.getResources().getDisplayMetrics()));
                            date0.setLayoutParams(dateParams0);

                            final TextClock clock0 = new TextClock(mContext);
                            clock0.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            clock0.setFormat12Hour("hh:mm");
                            clock0.setFormat24Hour("HH:mm");
                            clock0.setTextColor(mContext.getResources().getColor(forceWhiteText ? android.R.color.white : android.R.color.holo_blue_light));
                            clock0.setTextSize(TypedValue.COMPLEX_UNIT_SP, 100 * textScaling);
                            clock0.setTypeface(typeface != null ? typeface : clock0.getTypeface(), Typeface.BOLD);
                            ViewGroup.MarginLayoutParams clockParams0 = new ViewGroup.MarginLayoutParams(
                                    ViewGroup.MarginLayoutParams.WRAP_CONTENT,
                                    ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                            clockParams0.setMargins(
                                    0,
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, lineHeight, mContext.getResources().getDisplayMetrics()),
                                    0,
                                    0);
                            clock0.setLayoutParams(clockParams0);

                            final LinearLayout clockContainer0 = new LinearLayout(mContext);
                            LinearLayout.LayoutParams layoutParams0 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            layoutParams0.gravity = Gravity.CENTER_HORIZONTAL;
                            layoutParams0.setMargins(
                                    0,
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, topMargin, mContext.getResources().getDisplayMetrics()),
                                    0,
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, bottomMargin, mContext.getResources().getDisplayMetrics()));
                            clockContainer0.setLayoutParams(layoutParams0);
                            clockContainer0.setGravity(Gravity.CENTER);
                            clockContainer0.setOrientation(LinearLayout.VERTICAL);

                            clockContainer0.addView(date0);
                            clockContainer0.addView(clock0);

                            status_view_container.addView(clockContainer0, status_view_container.getChildCount() - 1);

                            log("Custom lockscreen clock1 added successfully.");
                            break;
                        case 1:
                            final TextClock day1 = new TextClock(mContext);
                            day1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            day1.setFormat12Hour("EEEE");
                            day1.setFormat24Hour("EEEE");
                            day1.setTextColor(mContext.getResources().getColor(android.R.color.white));
                            day1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50 * textScaling);
                            day1.setTypeface(typeface != null ? typeface : day1.getTypeface());

                            final TextClock clock1 = new TextClock(mContext);
                            clock1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            clock1.setFormat12Hour("hh:mm");
                            clock1.setFormat24Hour("HH:mm");
                            clock1.setTextColor(mContext.getResources().getColor(android.R.color.white));
                            clock1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50 * textScaling * textScaling);
                            clock1.setTypeface(typeface != null ? typeface : clock1.getTypeface());

                            final TextClock clockOverlay1 = new TextClock(mContext);
                            clockOverlay1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            clockOverlay1.setFormat12Hour("hh");
                            clockOverlay1.setFormat24Hour("HH");
                            clockOverlay1.setTextColor(mContext.getResources().getColor(forceWhiteText ? android.R.color.white : android.R.color.holo_blue_light));
                            clockOverlay1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50 * textScaling);
                            clockOverlay1.setMaxLines(1);
                            clockOverlay1.setTypeface(typeface != null ? typeface : clockOverlay1.getTypeface());
                            int maxLength = 1;
                            InputFilter[] fArray = new InputFilter[1];
                            fArray[0] = new InputFilter.LengthFilter(maxLength);
                            clockOverlay1.setFilters(fArray);

                            final FrameLayout clockContainer1 = new FrameLayout(mContext);
                            clockContainer1.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            ((FrameLayout.LayoutParams) clockContainer1.getLayoutParams()).setMargins(
                                    0,
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -12 + lineHeight, mContext.getResources().getDisplayMetrics()),
                                    0,
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -8 + lineHeight, mContext.getResources().getDisplayMetrics()));

                            clockContainer1.addView(clock1);
                            clockContainer1.addView(clockOverlay1);

                            final TextClock month1 = new TextClock(mContext);
                            month1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            month1.setFormat12Hour("MMMM d");
                            month1.setFormat24Hour("MMMM d");
                            month1.setTextColor(mContext.getResources().getColor(android.R.color.white));
                            month1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20 * textScaling);
                            month1.setTypeface(typeface != null ? typeface : month1.getTypeface());

                            final LinearLayout wholeContainer1 = new LinearLayout(mContext);
                            LinearLayout.LayoutParams layoutparams1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            layoutparams1.gravity = Gravity.START;
                            layoutparams1.setMargins(
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, mContext.getResources().getDisplayMetrics()),
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, topMargin, mContext.getResources().getDisplayMetrics()),
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, mContext.getResources().getDisplayMetrics()),
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, bottomMargin, mContext.getResources().getDisplayMetrics()));
                            wholeContainer1.setLayoutParams(layoutparams1);
                            wholeContainer1.setGravity(Gravity.START);
                            wholeContainer1.setOrientation(LinearLayout.VERTICAL);

                            wholeContainer1.addView(day1);
                            wholeContainer1.addView(clockContainer1);
                            wholeContainer1.addView(month1);

                            status_view_container.addView(wholeContainer1, status_view_container.getChildCount() - 1);

                            log("Custom lockscreen clock2 added successfully.");
                            break;
                        case 2:
                            final TextClock date2 = new TextClock(mContext);
                            date2.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            date2.setFormat12Hour("EEE, MMM dd");
                            date2.setFormat24Hour("EEE, MMM dd");
                            date2.setTextColor(mContext.getResources().getColor(forceWhiteText ? android.R.color.white : android.R.color.holo_blue_light));
                            date2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24 * textScaling);
                            date2.setTypeface(typeface != null ? typeface : date2.getTypeface(), Typeface.BOLD);

                            final TextClock clockHour2 = new TextClock(mContext);
                            clockHour2.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            clockHour2.setFormat12Hour("hh");
                            clockHour2.setFormat24Hour("HH");
                            clockHour2.setTextColor(mContext.getResources().getColor(forceWhiteText ? android.R.color.white : android.R.color.holo_blue_light));
                            clockHour2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 160 * textScaling);
                            clockHour2.setTypeface(typeface != null ? typeface : clockHour2.getTypeface(), Typeface.BOLD);
                            ViewGroup.MarginLayoutParams clockHourParams2 = new ViewGroup.MarginLayoutParams(
                                    ViewGroup.MarginLayoutParams.WRAP_CONTENT,
                                    ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                            clockHourParams2.setMargins(
                                    0,
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -30 + lineHeight, mContext.getResources().getDisplayMetrics()),
                                    0,
                                    0);
                            clockHour2.setLayoutParams(clockHourParams2);

                            final TextClock clockMinute2 = new TextClock(mContext);
                            clockMinute2.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            clockMinute2.setFormat12Hour("mm");
                            clockMinute2.setFormat24Hour("mm");
                            clockMinute2.setTextColor(mContext.getResources().getColor(forceWhiteText ? android.R.color.white : android.R.color.holo_blue_light));
                            clockMinute2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 160 * textScaling);
                            clockMinute2.setTypeface(typeface != null ? typeface : clockMinute2.getTypeface(), Typeface.BOLD);
                            ViewGroup.MarginLayoutParams clockMinuteParams2 = new ViewGroup.MarginLayoutParams(
                                    ViewGroup.MarginLayoutParams.WRAP_CONTENT,
                                    ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                            clockMinuteParams2.setMargins(
                                    0,
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -80 + lineHeight, mContext.getResources().getDisplayMetrics()),
                                    0,
                                    0);
                            clockMinute2.setLayoutParams(clockMinuteParams2);

                            final LinearLayout clockContainer2 = new LinearLayout(mContext);
                            LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            layoutParams2.gravity = Gravity.CENTER_HORIZONTAL;
                            layoutParams2.setMargins(
                                    0,
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, topMargin, mContext.getResources().getDisplayMetrics()),
                                    0,
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, bottomMargin, mContext.getResources().getDisplayMetrics()));
                            clockContainer2.setLayoutParams(layoutParams2);
                            clockContainer2.setGravity(Gravity.CENTER_HORIZONTAL);
                            clockContainer2.setOrientation(LinearLayout.VERTICAL);

                            clockContainer2.addView(date2);
                            clockContainer2.addView(clockHour2);
                            clockContainer2.addView(clockMinute2);

                            status_view_container.addView(clockContainer2, status_view_container.getChildCount() - 1);

                            log("Custom lockscreen clock3 added successfully.");
                            break;
                        case 3:
                            final AnalogClock analogClock3 = new AnalogClock(mContext);
                            analogClock3.setLayoutParams(new LinearLayout.LayoutParams(
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 180 * textScaling, mContext.getResources().getDisplayMetrics()),
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 180 * textScaling, mContext.getResources().getDisplayMetrics())));
                            ((LinearLayout.LayoutParams) analogClock3.getLayoutParams()).gravity = Gravity.CENTER_HORIZONTAL;

                            final TextClock day3 = new TextClock(mContext);
                            day3.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            day3.setFormat12Hour("EEE dd MMM");
                            day3.setFormat24Hour("EEE dd MMM");
                            day3.setTextColor(mContext.getResources().getColor(android.R.color.system_neutral1_200));
                            day3.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32 * textScaling);
                            day3.setTypeface(typeface != null ? typeface : day3.getTypeface(), Typeface.BOLD);
                            ViewGroup.MarginLayoutParams dayParams3 = new ViewGroup.MarginLayoutParams(
                                    ViewGroup.MarginLayoutParams.WRAP_CONTENT,
                                    ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                            dayParams3.setMargins(
                                    0,
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6 + lineHeight, mContext.getResources().getDisplayMetrics()),
                                    0,
                                    0);
                            day3.setLayoutParams(dayParams3);

                            final LinearLayout clockContainer3 = new LinearLayout(mContext);
                            LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            layoutParams3.gravity = Gravity.CENTER_HORIZONTAL;
                            layoutParams3.setMargins(
                                    0,
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, topMargin, mContext.getResources().getDisplayMetrics()),
                                    0,
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, bottomMargin, mContext.getResources().getDisplayMetrics()));
                            clockContainer3.setLayoutParams(layoutParams3);
                            clockContainer3.setGravity(Gravity.CENTER_HORIZONTAL);
                            clockContainer3.setOrientation(LinearLayout.VERTICAL);

                            clockContainer3.addView(analogClock3);
                            clockContainer3.addView(day3);

                            status_view_container.addView(clockContainer3, status_view_container.getChildCount() - 1);

                            log("Custom lockscreen clock4 added successfully.");
                            break;
                        case 4:
                            final TextClock hour4 = new TextClock(mContext);
                            hour4.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            hour4.setFormat12Hour("hh");
                            hour4.setFormat24Hour("HH");
                            hour4.setTextColor(mContext.getResources().getColor(android.R.color.white));
                            hour4.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40 * textScaling);
                            hour4.setTypeface(typeface != null ? typeface : hour4.getTypeface(), Typeface.BOLD);

                            final TextClock minute4 = new TextClock(mContext);
                            minute4.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            minute4.setFormat12Hour("mm");
                            minute4.setFormat24Hour("mm");
                            minute4.setTextColor(mContext.getResources().getColor(android.R.color.white));
                            minute4.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40 * textScaling);
                            minute4.setTypeface(typeface != null ? typeface : minute4.getTypeface(), Typeface.BOLD);
                            ViewGroup.MarginLayoutParams minuteParams4 = new ViewGroup.MarginLayoutParams(
                                    ViewGroup.MarginLayoutParams.WRAP_CONTENT,
                                    ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                            minuteParams4.setMargins(
                                    0,
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4 + lineHeight, mContext.getResources().getDisplayMetrics()),
                                    0,
                                    0);
                            minute4.setLayoutParams(minuteParams4);

                            final LinearLayout time4 = new LinearLayout(mContext);
                            LinearLayout.LayoutParams timeLayoutParams4 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
                            timeLayoutParams4.gravity = Gravity.CENTER;
                            time4.setLayoutParams(timeLayoutParams4);
                            time4.setOrientation(LinearLayout.VERTICAL);
                            time4.setPadding(
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, mContext.getResources().getDisplayMetrics()),
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, mContext.getResources().getDisplayMetrics()),
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, mContext.getResources().getDisplayMetrics()),
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, mContext.getResources().getDisplayMetrics()));
                            GradientDrawable timeDrawable4 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                                    new int[]{
                                            mContext.getResources().getColor(android.R.color.black),
                                            mContext.getResources().getColor(android.R.color.black)
                                    });
                            timeDrawable4.setCornerRadius((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, mContext.getResources().getDisplayMetrics()));
                            time4.setBackground(timeDrawable4);
                            time4.setGravity(Gravity.CENTER);

                            time4.addView(hour4);
                            time4.addView(minute4);

                            final TextClock day4 = new TextClock(mContext);
                            day4.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            day4.setFormat12Hour("EEE");
                            day4.setFormat24Hour("EEE");
                            day4.setAllCaps(true);
                            day4.setTextColor(mContext.getResources().getColor(android.R.color.white));
                            day4.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28 * textScaling);
                            day4.setTypeface(typeface != null ? typeface : day4.getTypeface(), Typeface.BOLD);
                            day4.setLetterSpacing(0.2f);

                            final TextClock date4 = new TextClock(mContext);
                            date4.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            date4.setFormat12Hour("dd");
                            date4.setFormat24Hour("dd");
                            date4.setTextColor(mContext.getResources().getColor(android.R.color.white));
                            date4.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28 * textScaling);
                            date4.setTypeface(typeface != null ? typeface : date4.getTypeface(), Typeface.BOLD);
                            date4.setLetterSpacing(0.2f);
                            ViewGroup.MarginLayoutParams dateParams4 = new ViewGroup.MarginLayoutParams(
                                    ViewGroup.MarginLayoutParams.WRAP_CONTENT,
                                    ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                            dateParams4.setMargins(
                                    0,
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -2 + lineHeight, mContext.getResources().getDisplayMetrics()),
                                    0,
                                    0);
                            date4.setLayoutParams(dateParams4);

                            final TextClock month4 = new TextClock(mContext);
                            month4.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            month4.setFormat12Hour("MMM");
                            month4.setFormat24Hour("MMM");
                            month4.setAllCaps(true);
                            month4.setTextColor(mContext.getResources().getColor(android.R.color.white));
                            month4.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28 * textScaling);
                            month4.setTypeface(typeface != null ? typeface : month4.getTypeface(), Typeface.BOLD);
                            month4.setLetterSpacing(0.2f);
                            ViewGroup.MarginLayoutParams monthParams4 = new ViewGroup.MarginLayoutParams(
                                    ViewGroup.MarginLayoutParams.WRAP_CONTENT,
                                    ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                            monthParams4.setMargins(
                                    0,
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -2 + lineHeight, mContext.getResources().getDisplayMetrics()),
                                    0,
                                    0);
                            month4.setLayoutParams(monthParams4);

                            final LinearLayout right4 = new LinearLayout(mContext);
                            LinearLayout.LayoutParams rightLayoutParams4 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
                            rightLayoutParams4.gravity = Gravity.CENTER;
                            right4.setLayoutParams(rightLayoutParams4);
                            right4.setOrientation(LinearLayout.VERTICAL);
                            right4.setPadding(
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, mContext.getResources().getDisplayMetrics()),
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, mContext.getResources().getDisplayMetrics()),
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, mContext.getResources().getDisplayMetrics()),
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, mContext.getResources().getDisplayMetrics()));
                            right4.setGravity(Gravity.CENTER);

                            right4.addView(day4);
                            right4.addView(date4);
                            right4.addView(month4);

                            final LinearLayout container4 = new LinearLayout(mContext);
                            LinearLayout.LayoutParams layoutParams4 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            layoutParams4.gravity = Gravity.CENTER_HORIZONTAL;
                            layoutParams4.setMargins(
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, mContext.getResources().getDisplayMetrics()),
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, topMargin, mContext.getResources().getDisplayMetrics()),
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, mContext.getResources().getDisplayMetrics()),
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, bottomMargin, mContext.getResources().getDisplayMetrics()));
                            container4.setLayoutParams(layoutParams4);
                            container4.setOrientation(LinearLayout.HORIZONTAL);
                            container4.setPadding(
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, mContext.getResources().getDisplayMetrics()),
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, mContext.getResources().getDisplayMetrics()),
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, mContext.getResources().getDisplayMetrics()),
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, mContext.getResources().getDisplayMetrics()));
                            GradientDrawable mDrawable4 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                                    new int[]{
                                            mContext.getResources().getColor(android.R.color.holo_blue_light),
                                            mContext.getResources().getColor(android.R.color.holo_blue_light)
                                    });
                            mDrawable4.setCornerRadius((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 28, mContext.getResources().getDisplayMetrics()));
                            container4.setBackground(mDrawable4);

                            container4.addView(time4);
                            container4.addView(right4);

                            status_view_container.addView(container4, status_view_container.getChildCount() - 1);

                            log("Custom lockscreen clock5 added successfully.");
                            break;
                    }
                }
            });
        } catch (Throwable t) {
            log(TAG + t);
        }
    }

    private void hideStockClockDate() {
        XC_InitPackageResources.InitPackageResourcesParam ourResparam = resparams.get(SYSTEM_UI_PACKAGE);
        if (ourResparam == null) return;

        if (!showLockscreenClock)
            return;

        try {
            ourResparam.res.hookLayout(SYSTEM_UI_PACKAGE, "layout", "keyguard_status_view", new XC_LayoutInflated() {
                @SuppressLint({"DiscouragedApi"})
                @Override
                public void handleLayoutInflated(LayoutInflatedParam liparam) {
                    @SuppressLint("DiscouragedApi") RelativeLayout keyguard_clock_container = liparam.view.findViewById(liparam.res.getIdentifier("keyguard_clock_container", "id", SYSTEM_UI_PACKAGE));
                    keyguard_clock_container.getLayoutParams().height = 0;
                    keyguard_clock_container.getLayoutParams().width = 0;

                    @SuppressLint("DiscouragedApi") FrameLayout status_view_media_container = liparam.view.findViewById(liparam.res.getIdentifier("status_view_media_container", "id", SYSTEM_UI_PACKAGE));
                    status_view_media_container.getLayoutParams().height = 0;
                    status_view_media_container.getLayoutParams().width = 0;
                    log("Stock lockscreen clock hidden");
                }
            });
        } catch (Throwable t) {
            log(TAG + t);
        }
    }
}