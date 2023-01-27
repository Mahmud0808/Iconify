package com.drdisagree.iconify.xposed.mods;

import static com.drdisagree.iconify.common.References.LSCLOCK_BOTTOMMARGIN;
import static com.drdisagree.iconify.common.References.LSCLOCK_CLOCK_SWITCH;
import static com.drdisagree.iconify.common.References.LSCLOCK_FONT_LINEHEIGHT;
import static com.drdisagree.iconify.common.References.LSCLOCK_FONT_SWITCH;
import static com.drdisagree.iconify.common.References.LSCLOCK_STYLE;
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
import android.os.Environment;
import android.text.InputFilter;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
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
        theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, false);
        @SuppressLint("Recycle") TypedArray arr = context.obtainStyledAttributes(typedValue.data, new int[]{android.R.attr.textColorPrimary});
        return arr.getColor(0, -1);
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;

        showLockscreenClock = Xprefs.getBoolean(LSCLOCK_CLOCK_SWITCH, false);
        lockscreenClockStyle = Xprefs.getInt(LSCLOCK_STYLE, 0);
        topMargin = Xprefs.getInt(LSCLOCK_TOPMARGIN, 100);
        bottomMargin = Xprefs.getInt(LSCLOCK_BOTTOMMARGIN, 40);
        lineHeight = Xprefs.getInt(LSCLOCK_FONT_LINEHEIGHT, 0);
        customFontEnabled = Xprefs.getBoolean(LSCLOCK_FONT_SWITCH, false);
        forceWhiteText = Xprefs.getBoolean(LSCLOCK_TEXT_WHITE, false);

        if (status_view_container != null) {
            if (status_view_container.getChildCount() >= 3) {
                status_view_container.removeViewAt(0);
            }
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
                            date0.setFormat12Hour("EEEE, MMMM d");
                            date0.setFormat24Hour("EEEE, MMMM d");
                            date0.setTextColor(mContext.getResources().getColor(forceWhiteText ? android.R.color.white : android.R.color.holo_blue_light));
                            date0.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
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
                            clock0.setTextSize(TypedValue.COMPLEX_UNIT_SP, 100);
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
                            log(String.valueOf(lockscreenClockStyle));
                            break;
                        case 1:
                            final TextClock day1 = new TextClock(mContext);
                            day1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            day1.setFormat12Hour("EEEE");
                            day1.setFormat24Hour("EEEE");
                            day1.setTextColor(mContext.getResources().getColor(android.R.color.white));
                            day1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50);
                            day1.setTypeface(typeface != null ? typeface : day1.getTypeface());

                            final TextClock clock1 = new TextClock(mContext);
                            clock1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            clock1.setFormat12Hour("hh:mm");
                            clock1.setFormat24Hour("HH:mm");
                            clock1.setTextColor(mContext.getResources().getColor(android.R.color.white));
                            clock1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50);
                            clock1.setTypeface(typeface != null ? typeface : clock1.getTypeface());

                            final TextClock clockOverlay1 = new TextClock(mContext);
                            clockOverlay1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            clockOverlay1.setFormat12Hour("hh");
                            clockOverlay1.setFormat24Hour("HH");
                            clockOverlay1.setTextColor(mContext.getResources().getColor(forceWhiteText ? android.R.color.white : android.R.color.holo_blue_light));
                            clockOverlay1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50);
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
                            month1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
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
                            date2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                            date2.setTypeface(typeface != null ? typeface : date2.getTypeface(), Typeface.BOLD);

                            final TextClock clockHour2 = new TextClock(mContext);
                            clockHour2.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            clockHour2.setFormat12Hour("hh");
                            clockHour2.setFormat24Hour("HH");
                            clockHour2.setTextColor(mContext.getResources().getColor(forceWhiteText ? android.R.color.white : android.R.color.holo_blue_light));
                            clockHour2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 160);
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
                            clockMinute2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 160);
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
                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
                            layoutParams.setMargins(
                                    0,
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, topMargin, mContext.getResources().getDisplayMetrics()),
                                    0,
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, bottomMargin, mContext.getResources().getDisplayMetrics()));
                            clockContainer2.setLayoutParams(layoutParams);
                            clockContainer2.setGravity(Gravity.CENTER_HORIZONTAL);
                            clockContainer2.setOrientation(LinearLayout.VERTICAL);

                            clockContainer2.addView(date2);
                            clockContainer2.addView(clockHour2);
                            clockContainer2.addView(clockMinute2);

                            status_view_container.addView(clockContainer2, status_view_container.getChildCount() - 1);
                            log("Custom lockscreen clock3 added successfully.");
                            log(String.valueOf(lockscreenClockStyle));
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