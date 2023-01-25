package com.drdisagree.iconify.xposed.mods;

import static com.drdisagree.iconify.common.References.HEADER_CLOCK_QSTOPMARGIN;
import static com.drdisagree.iconify.common.References.HEADER_CLOCK_SIDEMARGIN;
import static com.drdisagree.iconify.common.References.HEADER_CLOCK_STYLE;
import static com.drdisagree.iconify.common.References.HEADER_CLOCK_SWITCH;
import static com.drdisagree.iconify.common.References.HEADER_CLOCK_TOPMARGIN;
import static com.drdisagree.iconify.common.References.SYSTEM_UI_PACKAGE;
import static com.drdisagree.iconify.config.XPrefs.Xprefs;
import static com.drdisagree.iconify.xposed.HookRes.resparams;
import static de.robv.android.xposed.XposedBridge.log;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XResources;
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
    int qsTopMargin = 0;
    int headerClockStyle = 0;
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
        qsTopMargin = Xprefs.getInt(HEADER_CLOCK_QSTOPMARGIN, 0);

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
        } catch (Throwable t) {
            log(TAG + t);
        }

        try {
            ourResparam.res.hookLayout(SYSTEM_UI_PACKAGE, "layout", "quick_status_bar_expanded_header", new XC_LayoutInflated() {
                @SuppressLint({"DiscouragedApi"})
                @Override
                public void handleLayoutInflated(LayoutInflatedParam liparam) {
                    @SuppressLint("DiscouragedApi") FrameLayout header = liparam.view.findViewById(liparam.res.getIdentifier("header", "id", SYSTEM_UI_PACKAGE));

                    switch (headerClockStyle) {
                        case 0:
                            final TextClock clockHour = new TextClock(mContext);
                            clockHour.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            clockHour.setFormat12Hour("hh");
                            clockHour.setFormat24Hour("HH");
                            clockHour.setTextColor(mContext.getResources().getColor(android.R.color.holo_blue_light));
                            clockHour.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);
                            clockHour.setTypeface(clockHour.getTypeface(), Typeface.BOLD);

                            final TextClock clockMinute = new TextClock(mContext);
                            clockMinute.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            clockMinute.setFormat12Hour(":mm");
                            clockMinute.setFormat24Hour(":mm");
                            clockMinute.setTextColor(getColorResCompat(mContext, android.R.attr.textColorPrimary));
                            clockMinute.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);
                            clockMinute.setTypeface(clockMinute.getTypeface(), Typeface.BOLD);

                            final LinearLayout divider = new LinearLayout(mContext);
                            ViewGroup.MarginLayoutParams dividerParams = new ViewGroup.MarginLayoutParams(
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, mContext.getResources().getDisplayMetrics()),
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 40, mContext.getResources().getDisplayMetrics()));
                            dividerParams.setMargins(
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, mContext.getResources().getDisplayMetrics()),
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, mContext.getResources().getDisplayMetrics()),
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, mContext.getResources().getDisplayMetrics()),
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, mContext.getResources().getDisplayMetrics()));
                            divider.setLayoutParams(dividerParams);
                            divider.setBackgroundColor(mContext.getResources().getColor(android.R.color.holo_green_light));

                            final TextClock clockDay = new TextClock(mContext);
                            clockDay.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            clockDay.setFormat12Hour("EEEE");
                            clockDay.setFormat24Hour("EEEE");
                            clockDay.setTextColor(getColorResCompat(mContext, android.R.attr.textColorPrimary));
                            clockDay.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                            clockDay.setTypeface(clockMinute.getTypeface(), Typeface.BOLD);

                            final TextClock clockDate = new TextClock(mContext);
                            clockDate.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            clockDate.setFormat12Hour("dd MMMM");
                            clockDate.setFormat24Hour("dd MMMM");
                            clockDate.setTextColor(getColorResCompat(mContext, android.R.attr.textColorPrimary));
                            clockDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                            clockDate.setTypeface(clockMinute.getTypeface(), Typeface.BOLD);

                            final LinearLayout dateContainer = new LinearLayout(mContext);
                            dateContainer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
                            dateContainer.setGravity(Gravity.BOTTOM);
                            dateContainer.setOrientation(LinearLayout.VERTICAL);
                            ((LinearLayout.LayoutParams) dateContainer.getLayoutParams()).setMargins(
                                    0,
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, mContext.getResources().getDisplayMetrics()),
                                    0,
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, mContext.getResources().getDisplayMetrics()));

                            dateContainer.addView(clockDay);
                            dateContainer.addView(clockDate);

                            final LinearLayout clockContainer = new LinearLayout(mContext);
                            clockContainer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            clockContainer.setGravity(Gravity.CENTER_VERTICAL);
                            clockContainer.setOrientation(LinearLayout.HORIZONTAL);
                            ((LinearLayout.LayoutParams) clockContainer.getLayoutParams()).setMargins(
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, sideMargin, mContext.getResources().getDisplayMetrics()),
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, topMargin, mContext.getResources().getDisplayMetrics()),
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, sideMargin, mContext.getResources().getDisplayMetrics()),
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, mContext.getResources().getDisplayMetrics()));

                            clockContainer.addView(clockHour);
                            clockContainer.addView(clockMinute);
                            clockContainer.addView(divider);
                            clockContainer.addView(dateContainer);

                            header.addView(clockContainer, header.getChildCount() - 1);

                            ourResparam.res.setReplacement(SYSTEM_UI_PACKAGE, "dimen", "qs_panel_padding_top", new XResources.DimensionReplacement(topMargin + 28 + qsTopMargin, TypedValue.COMPLEX_UNIT_DIP));
                            ourResparam.res.setReplacement(SYSTEM_UI_PACKAGE, "dimen", "qqs_layout_margin_top", new XResources.DimensionReplacement(topMargin + 28 + qsTopMargin, TypedValue.COMPLEX_UNIT_DIP));

                            log("Custom header clock1 added successfully.");
                            break;
                        case 1:
                            final TextClock clock = new TextClock(mContext);
                            clock.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            clock.setFormat12Hour("h:mm");
                            clock.setFormat24Hour("H:mm");
                            clock.setTextColor(getColorResCompat(mContext, android.R.attr.textColorPrimary));
                            clock.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);
                            clock.setTypeface(clock.getTypeface(), Typeface.BOLD);

                            final TextClock clockOverlay = new TextClock(mContext);
                            clockOverlay.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            clockOverlay.setFormat12Hour("h");
                            clockOverlay.setFormat24Hour("H");
                            clockOverlay.setTextColor(mContext.getResources().getColor(android.R.color.holo_blue_light));
                            clockOverlay.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);
                            clockOverlay.setTypeface(clockOverlay.getTypeface(), Typeface.BOLD);
                            clockOverlay.setMaxLines(1);
                            int maxLength = 1;
                            InputFilter[] fArray = new InputFilter[1];
                            fArray[0] = new InputFilter.LengthFilter(maxLength);
                            clockOverlay.setFilters(fArray);

                            final FrameLayout clockContainer2 = new FrameLayout(mContext);
                            clockContainer2.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            ((FrameLayout.LayoutParams) clockContainer2.getLayoutParams()).setMargins(
                                    0,
                                    0,
                                    0,
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -12, mContext.getResources().getDisplayMetrics()));

                            clockContainer2.addView(clock);
                            clockContainer2.addView(clockOverlay);

                            final TextClock dayDate = new TextClock(mContext);
                            dayDate.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            dayDate.setFormat12Hour("EEEE, MMM dd");
                            dayDate.setFormat24Hour("EEEE, MMM dd");
                            dayDate.setTextColor(getColorResCompat(mContext, android.R.attr.textColorPrimary));
                            dayDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                            dayDate.setTypeface(clockOverlay.getTypeface(), Typeface.BOLD);

                            final LinearLayout container = new LinearLayout(mContext);
                            container.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            container.setGravity(Gravity.CENTER_VERTICAL);
                            container.setOrientation(LinearLayout.VERTICAL);
                            ((LinearLayout.LayoutParams) container.getLayoutParams()).setMargins(
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, sideMargin, mContext.getResources().getDisplayMetrics()),
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, topMargin, mContext.getResources().getDisplayMetrics()),
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, sideMargin, mContext.getResources().getDisplayMetrics()),
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, mContext.getResources().getDisplayMetrics()));

                            container.addView(clockContainer2);
                            container.addView(dayDate);

                            header.addView(container, header.getChildCount() - 1);

                            ourResparam.res.setReplacement(SYSTEM_UI_PACKAGE, "dimen", "qs_panel_padding_top", new XResources.DimensionReplacement(topMargin + 40 + qsTopMargin, TypedValue.COMPLEX_UNIT_DIP));
                            ourResparam.res.setReplacement(SYSTEM_UI_PACKAGE, "dimen", "qqs_layout_margin_top", new XResources.DimensionReplacement(topMargin + 40 + qsTopMargin, TypedValue.COMPLEX_UNIT_DIP));

                            log("Custom header clock2 added successfully.");
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