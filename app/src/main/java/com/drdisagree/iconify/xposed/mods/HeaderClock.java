package com.drdisagree.iconify.xposed.mods;

import static com.drdisagree.iconify.common.References.HEADER_CLOCK_SIDEMARGIN;
import static com.drdisagree.iconify.common.References.HEADER_CLOCK_STYLE;
import static com.drdisagree.iconify.common.References.HEADER_CLOCK_SWITCH;
import static com.drdisagree.iconify.common.References.HEADER_CLOCK_TEXT_WHITE;
import static com.drdisagree.iconify.common.References.HEADER_CLOCK_TOPMARGIN;
import static com.drdisagree.iconify.common.References.SYSTEM_UI_PACKAGE;
import static com.drdisagree.iconify.config.XPrefs.Xprefs;
import static com.drdisagree.iconify.xposed.HookRes.resparams;
import static de.robv.android.xposed.XposedBridge.log;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.text.InputFilter;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextClock;
import android.widget.TextView;

import androidx.annotation.AttrRes;

import com.drdisagree.iconify.xposed.ModPack;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HeaderClock extends ModPack implements IXposedHookLoadPackage {

    private static final String TAG = "Iconify - HeaderClock: ";
    boolean showHeaderClock = false;
    int sideMargin = 0;
    int topMargin = 8;
    int headerClockStyle = 0;
    boolean forceWhiteText = false;
    private String rootPackagePath = "";

    public HeaderClock(Context context) {
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

        showHeaderClock = Xprefs.getBoolean(HEADER_CLOCK_SWITCH, false);
        sideMargin = Xprefs.getInt(HEADER_CLOCK_SIDEMARGIN, 0);
        topMargin = Xprefs.getInt(HEADER_CLOCK_TOPMARGIN, 8);
        headerClockStyle = Xprefs.getInt(HEADER_CLOCK_STYLE, 0);
        forceWhiteText = Xprefs.getBoolean(HEADER_CLOCK_TEXT_WHITE, false);

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

        if (!showHeaderClock)
            return;

        try {
            ourResparam.res.setReplacement(SYSTEM_UI_PACKAGE, "bool", "config_use_large_screen_shade_header", false);
        } catch (Throwable ignored) {}

        try {
            ourResparam.res.hookLayout(SYSTEM_UI_PACKAGE, "layout", "quick_status_bar_expanded_header", new XC_LayoutInflated() {
                @SuppressLint({"DiscouragedApi"})
                @Override
                public void handleLayoutInflated(LayoutInflatedParam liparam) {
                    @SuppressLint("DiscouragedApi") FrameLayout header = liparam.view.findViewById(liparam.res.getIdentifier("header", "id", SYSTEM_UI_PACKAGE));

                    switch (headerClockStyle) {
                        case 0:
                            final TextClock clockHour0 = new TextClock(mContext);
                            clockHour0.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            clockHour0.setFormat12Hour("hh");
                            clockHour0.setFormat24Hour("HH");
                            clockHour0.setTextColor(mContext.getResources().getColor(android.R.color.holo_blue_light));
                            clockHour0.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);
                            clockHour0.setTypeface(clockHour0.getTypeface(), Typeface.BOLD);

                            final TextClock clockMinute0 = new TextClock(mContext);
                            clockMinute0.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            clockMinute0.setFormat12Hour(":mm");
                            clockMinute0.setFormat24Hour(":mm");
                            clockMinute0.setTextColor(forceWhiteText ? mContext.getResources().getColor(android.R.color.white) : getColorResCompat(mContext, android.R.attr.textColorPrimary));
                            clockMinute0.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);
                            clockMinute0.setTypeface(clockMinute0.getTypeface(), Typeface.BOLD);

                            final LinearLayout divider0 = new LinearLayout(mContext);
                            ViewGroup.MarginLayoutParams dividerParams0 = new ViewGroup.MarginLayoutParams(
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, mContext.getResources().getDisplayMetrics()),
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 40, mContext.getResources().getDisplayMetrics()));
                            dividerParams0.setMargins(
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, mContext.getResources().getDisplayMetrics()),
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, mContext.getResources().getDisplayMetrics()),
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, mContext.getResources().getDisplayMetrics()),
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, mContext.getResources().getDisplayMetrics()));
                            divider0.setLayoutParams(dividerParams0);
                            divider0.setBackgroundColor(mContext.getResources().getColor(android.R.color.holo_green_light));

                            final TextClock clockDay0 = new TextClock(mContext);
                            clockDay0.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            clockDay0.setFormat12Hour("EEEE");
                            clockDay0.setFormat24Hour("EEEE");
                            clockDay0.setTextColor(forceWhiteText ? mContext.getResources().getColor(android.R.color.white) : getColorResCompat(mContext, android.R.attr.textColorPrimary));
                            clockDay0.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                            clockDay0.setTypeface(clockMinute0.getTypeface(), Typeface.BOLD);

                            final TextClock clockDate0 = new TextClock(mContext);
                            clockDate0.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            clockDate0.setFormat12Hour("dd MMMM");
                            clockDate0.setFormat24Hour("dd MMMM");
                            clockDate0.setTextColor(forceWhiteText ? mContext.getResources().getColor(android.R.color.white) : getColorResCompat(mContext, android.R.attr.textColorPrimary));
                            clockDate0.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                            clockDate0.setTypeface(clockMinute0.getTypeface(), Typeface.BOLD);

                            final LinearLayout dateContainer0 = new LinearLayout(mContext);
                            dateContainer0.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
                            dateContainer0.setGravity(Gravity.BOTTOM);
                            dateContainer0.setOrientation(LinearLayout.VERTICAL);
                            ((LinearLayout.LayoutParams) dateContainer0.getLayoutParams()).setMargins(
                                    0,
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, mContext.getResources().getDisplayMetrics()),
                                    0,
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, mContext.getResources().getDisplayMetrics()));

                            dateContainer0.addView(clockDay0);
                            dateContainer0.addView(clockDate0);

                            final LinearLayout clockContainer0 = new LinearLayout(mContext);
                            clockContainer0.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            clockContainer0.setGravity(Gravity.CENTER_VERTICAL);
                            clockContainer0.setOrientation(LinearLayout.HORIZONTAL);
                            ((LinearLayout.LayoutParams) clockContainer0.getLayoutParams()).setMargins(
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, sideMargin, mContext.getResources().getDisplayMetrics()),
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, topMargin, mContext.getResources().getDisplayMetrics()),
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, sideMargin, mContext.getResources().getDisplayMetrics()),
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, mContext.getResources().getDisplayMetrics()));

                            clockContainer0.addView(clockHour0);
                            clockContainer0.addView(clockMinute0);
                            clockContainer0.addView(divider0);
                            clockContainer0.addView(dateContainer0);

                            header.addView(clockContainer0, header.getChildCount() - 1);

                            log("Custom header clock1 added successfully.");
                            break;
                        case 1:
                            final TextClock clock1 = new TextClock(mContext);
                            clock1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            clock1.setFormat12Hour("h:mm");
                            clock1.setFormat24Hour("H:mm");
                            clock1.setTextColor(forceWhiteText ? mContext.getResources().getColor(android.R.color.white) : getColorResCompat(mContext, android.R.attr.textColorPrimary));
                            clock1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);
                            clock1.setTypeface(clock1.getTypeface(), Typeface.BOLD);

                            final TextClock clockOverlay1 = new TextClock(mContext);
                            clockOverlay1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            clockOverlay1.setFormat12Hour("h");
                            clockOverlay1.setFormat24Hour("H");
                            clockOverlay1.setTextColor(mContext.getResources().getColor(android.R.color.holo_blue_light));
                            clockOverlay1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);
                            clockOverlay1.setTypeface(clockOverlay1.getTypeface(), Typeface.BOLD);
                            clockOverlay1.setMaxLines(1);
                            int maxLength1 = 1;
                            InputFilter[] fArray1 = new InputFilter[1];
                            fArray1[0] = new InputFilter.LengthFilter(maxLength1);
                            clockOverlay1.setFilters(fArray1);

                            final FrameLayout clockContainer1 = new FrameLayout(mContext);
                            clockContainer1.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            ((FrameLayout.LayoutParams) clockContainer1.getLayoutParams()).setMargins(
                                    0,
                                    0,
                                    0,
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -12, mContext.getResources().getDisplayMetrics()));

                            clockContainer1.addView(clock1);
                            clockContainer1.addView(clockOverlay1);

                            final TextClock dayDate1 = new TextClock(mContext);
                            dayDate1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            dayDate1.setFormat12Hour("EEEE, MMM dd");
                            dayDate1.setFormat24Hour("EEEE, MMM dd");
                            dayDate1.setTextColor(forceWhiteText ? mContext.getResources().getColor(android.R.color.white) : getColorResCompat(mContext, android.R.attr.textColorPrimary));
                            dayDate1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                            dayDate1.setTypeface(clockOverlay1.getTypeface(), Typeface.BOLD);

                            final LinearLayout container1 = new LinearLayout(mContext);
                            container1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            container1.setGravity(Gravity.CENTER_VERTICAL);
                            container1.setOrientation(LinearLayout.VERTICAL);
                            ((LinearLayout.LayoutParams) container1.getLayoutParams()).setMargins(
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, sideMargin, mContext.getResources().getDisplayMetrics()),
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, topMargin, mContext.getResources().getDisplayMetrics()),
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, sideMargin, mContext.getResources().getDisplayMetrics()),
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, mContext.getResources().getDisplayMetrics()));

                            container1.addView(clockContainer1);
                            container1.addView(dayDate1);

                            header.addView(container1, header.getChildCount() - 1);

                            log("Custom header clock2 added successfully.");
                            break;
                        case 2:
                            final TextClock clock2 = new TextClock(mContext);
                            clock2.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            clock2.setFormat12Hour("hh:mm");
                            clock2.setFormat24Hour("HH:mm");
                            clock2.setTextColor(forceWhiteText ? mContext.getResources().getColor(android.R.color.white) : getColorResCompat(mContext, android.R.attr.textColorPrimary));
                            clock2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
                            clock2.setTypeface(clock2.getTypeface(), Typeface.BOLD);

                            final TextClock clockOverlay2 = new TextClock(mContext);
                            clockOverlay2.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            clockOverlay2.setFormat12Hour("hh:mm");
                            clockOverlay2.setFormat24Hour("HH:mm");
                            clockOverlay2.setTextColor(mContext.getResources().getColor(android.R.color.holo_blue_light));
                            clockOverlay2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
                            clockOverlay2.setTypeface(clockOverlay2.getTypeface(), Typeface.BOLD);
                            clockOverlay2.setAlpha(0.2f);
                            LinearLayout.LayoutParams clockOverlayParams2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            clockOverlayParams2.setMargins(6, 6, 0, 0);
                            clockOverlay2.setLayoutParams(clockOverlayParams2);

                            final FrameLayout clockContainer2 = new FrameLayout(mContext);
                            clockContainer2.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            ((FrameLayout.LayoutParams) clockContainer2.getLayoutParams()).setMargins(
                                    0,
                                    0,
                                    0,
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -12, mContext.getResources().getDisplayMetrics()));
                            clockContainer2.addView(clockOverlay2);
                            clockContainer2.addView(clock2);

                            final TextClock dayDate2 = new TextClock(mContext);
                            dayDate2.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            dayDate2.setFormat12Hour("EEE, MMM dd");
                            dayDate2.setFormat24Hour("EEE, MMM dd");
                            dayDate2.setTextColor(forceWhiteText ? mContext.getResources().getColor(android.R.color.white) : getColorResCompat(mContext, android.R.attr.textColorPrimary));
                            dayDate2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                            dayDate2.setTypeface(clockOverlay2.getTypeface(), Typeface.BOLD);

                            final TextClock dayDateOverlay2 = new TextClock(mContext);
                            dayDateOverlay2.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            dayDateOverlay2.setFormat12Hour("EEE, MMM dd");
                            dayDateOverlay2.setFormat24Hour("EEE, MMM dd");
                            dayDateOverlay2.setTextColor(mContext.getResources().getColor(android.R.color.holo_green_light));
                            dayDateOverlay2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                            dayDateOverlay2.setTypeface(dayDateOverlay2.getTypeface(), Typeface.BOLD);
                            dayDateOverlay2.setAlpha(0.2f);
                            LinearLayout.LayoutParams dayDateOverlayParams2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            dayDateOverlayParams2.setMargins(6, 6, 0, 0);
                            dayDateOverlay2.setLayoutParams(dayDateOverlayParams2);

                            final FrameLayout dayDateContainer2 = new FrameLayout(mContext);
                            dayDateContainer2.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            ((FrameLayout.LayoutParams) dayDateContainer2.getLayoutParams()).setMargins(
                                    0,
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, mContext.getResources().getDisplayMetrics()),
                                    0,
                                    0);
                            dayDateContainer2.addView(dayDateOverlay2);
                            dayDateContainer2.addView(dayDate2);

                            final LinearLayout container2 = new LinearLayout(mContext);
                            container2.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            container2.setGravity(Gravity.BOTTOM);
                            container2.setOrientation(LinearLayout.VERTICAL);
                            ((LinearLayout.LayoutParams) container2.getLayoutParams()).setMargins(
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, sideMargin, mContext.getResources().getDisplayMetrics()),
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, topMargin, mContext.getResources().getDisplayMetrics()),
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, sideMargin, mContext.getResources().getDisplayMetrics()),
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, mContext.getResources().getDisplayMetrics()));

                            container2.addView(clockContainer2);
                            container2.addView(dayDateContainer2);

                            header.addView(container2, header.getChildCount() - 1);

                            log("Custom header clock3 added successfully.");
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

        if (!showHeaderClock)
            return;

        try {
            ourResparam.res.hookLayout(SYSTEM_UI_PACKAGE, "layout", "quick_qs_status_icons", new XC_LayoutInflated() {
                @SuppressLint({"DiscouragedApi"})
                @Override
                public void handleLayoutInflated(LayoutInflatedParam liparam) {
                    @SuppressLint("DiscouragedApi") TextView clock = liparam.view.findViewById(liparam.res.getIdentifier("clock", "id", SYSTEM_UI_PACKAGE));
                    clock.getLayoutParams().height = 0;
                    clock.getLayoutParams().width = 0;

                    @SuppressLint("DiscouragedApi") TextView date_clock = liparam.view.findViewById(liparam.res.getIdentifier("date_clock", "id", SYSTEM_UI_PACKAGE));
                    date_clock.getLayoutParams().height = 0;
                    date_clock.getLayoutParams().width = 0;

                    @SuppressLint("DiscouragedApi") LinearLayout carrier_group = liparam.view.findViewById(liparam.res.getIdentifier("carrier_group", "id", SYSTEM_UI_PACKAGE));
                    carrier_group.getLayoutParams().height = 0;
                    carrier_group.getLayoutParams().width = 0;
                }
            });
        } catch (Throwable t) {
            log(TAG + t);
        }

        try {
            ourResparam.res.hookLayout(SYSTEM_UI_PACKAGE, "layout", "quick_status_bar_header_date_privacy", new XC_LayoutInflated() {
                @SuppressLint({"DiscouragedApi"})
                @Override
                public void handleLayoutInflated(LayoutInflatedParam liparam) {
                    @SuppressLint("DiscouragedApi") TextView date = liparam.view.findViewById(liparam.res.getIdentifier("date", "id", SYSTEM_UI_PACKAGE));
                    date.setTextColor(0);
                    date.setTextAppearance(0);
                    date.getLayoutParams().height = 0;
                    date.getLayoutParams().width = 0;
                }
            });
        } catch (Throwable t) {
            log(TAG + t);
        }
    }
}