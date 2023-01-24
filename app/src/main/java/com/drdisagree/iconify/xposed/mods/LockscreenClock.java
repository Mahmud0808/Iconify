package com.drdisagree.iconify.xposed.mods;

import static com.drdisagree.iconify.common.References.LSCLOCK_BOTTOMMARGIN;
import static com.drdisagree.iconify.common.References.LSCLOCK_CLOCK_SWITCH;
import static com.drdisagree.iconify.common.References.LSCLOCK_STYLE;
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
    private String rootPackagePath = "";
    private LinearLayout status_view_container = null;

    public LockscreenClock(Context context) {
        super(context);
        if (!listensTo(context.getPackageName())) return;
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;

        showLockscreenClock = Xprefs.getBoolean(LSCLOCK_CLOCK_SWITCH, false);
        lockscreenClockStyle = Xprefs.getInt(LSCLOCK_STYLE, 0);
        topMargin = Xprefs.getInt(LSCLOCK_TOPMARGIN, 100);
        bottomMargin = Xprefs.getInt(LSCLOCK_BOTTOMMARGIN, 40);

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

                    switch (lockscreenClockStyle) {
                        case 0:
                            final TextClock date = new TextClock(mContext);
                            date.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            date.setFormat12Hour("EEEE, MMMM d");
                            date.setFormat12Hour("EEEE, MMMM d");
                            date.setTextColor(mContext.getResources().getColor(android.R.color.system_accent1_100));
                            date.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                            date.setTypeface(date.getTypeface(), Typeface.BOLD);
                            ViewGroup.MarginLayoutParams dateParams = new ViewGroup.MarginLayoutParams(
                                    ViewGroup.MarginLayoutParams.WRAP_CONTENT,
                                    ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                            dateParams.setMargins(
                                    0,
                                    0,
                                    0,
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -16, mContext.getResources().getDisplayMetrics()));
                            date.setLayoutParams(dateParams);

                            final TextClock clock = new TextClock(mContext);
                            clock.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            clock.setFormat12Hour("hh:mm");
                            clock.setFormat24Hour("HH:mm");
                            clock.setTextColor(mContext.getResources().getColor(android.R.color.system_accent1_100));
                            clock.setTextSize(TypedValue.COMPLEX_UNIT_SP, 100);
                            clock.setTypeface(clock.getTypeface(), Typeface.BOLD);

                            final LinearLayout clockContainer = new LinearLayout(mContext);
                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
                            layoutParams.setMargins(
                                    0,
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, topMargin, mContext.getResources().getDisplayMetrics()),
                                    0,
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, bottomMargin, mContext.getResources().getDisplayMetrics()));
                            clockContainer.setLayoutParams(layoutParams);
                            clockContainer.setGravity(Gravity.CENTER);
                            clockContainer.setOrientation(LinearLayout.VERTICAL);

                            clockContainer.addView(date);
                            clockContainer.addView(clock);

                            status_view_container.addView(clockContainer, status_view_container.getChildCount() - 1);
                            log("Custom lockscreen clock1 added successfully.");
                            log(String.valueOf(lockscreenClockStyle));
                            break;
                        case 1:
                            final TextClock day = new TextClock(mContext);
                            day.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            day.setFormat12Hour("EEEE");
                            day.setFormat24Hour("EEEE");
                            day.setTextColor(mContext.getResources().getColor(android.R.color.white));
                            day.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50);

                            final TextClock clockWhite = new TextClock(mContext);
                            clockWhite.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            clockWhite.setFormat12Hour("hh:mm");
                            clockWhite.setFormat24Hour("HH:mm");
                            clockWhite.setTextColor(mContext.getResources().getColor(android.R.color.white));
                            clockWhite.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50);

                            final TextClock clockOverlay = new TextClock(mContext);
                            clockOverlay.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            clockOverlay.setFormat12Hour("h");
                            clockOverlay.setFormat24Hour("HH");
                            clockOverlay.setTextColor(mContext.getResources().getColor(android.R.color.system_accent1_200));
                            clockOverlay.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50);
                            clockOverlay.setMaxLines(1);
                            int maxLength = 1;
                            InputFilter[] fArray = new InputFilter[1];
                            fArray[0] = new InputFilter.LengthFilter(maxLength);
                            clockOverlay.setFilters(fArray);

                            final FrameLayout timeContainer = new FrameLayout(mContext);
                            timeContainer.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            ((FrameLayout.LayoutParams) timeContainer.getLayoutParams()).setMargins(
                                    0,
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -12, mContext.getResources().getDisplayMetrics()),
                                    0,
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -12, mContext.getResources().getDisplayMetrics()));

                            timeContainer.addView(clockWhite);
                            timeContainer.addView(clockOverlay);

                            final TextClock month = new TextClock(mContext);
                            month.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            month.setFormat12Hour("MMMM d");
                            month.setFormat12Hour("MMMM d");
                            month.setTextColor(mContext.getResources().getColor(android.R.color.white));
                            month.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);

                            final LinearLayout wholeContainer = new LinearLayout(mContext);
                            LinearLayout.LayoutParams layoutparams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            layoutparams.gravity = Gravity.START;
                            layoutparams.setMargins(
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, mContext.getResources().getDisplayMetrics()),
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, topMargin, mContext.getResources().getDisplayMetrics()),
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, mContext.getResources().getDisplayMetrics()),
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, bottomMargin, mContext.getResources().getDisplayMetrics()));
                            wholeContainer.setLayoutParams(layoutparams);
                            wholeContainer.setGravity(Gravity.START);
                            wholeContainer.setOrientation(LinearLayout.VERTICAL);

                            wholeContainer.addView(day);
                            wholeContainer.addView(timeContainer);
                            wholeContainer.addView(month);

                            status_view_container.addView(wholeContainer, status_view_container.getChildCount() - 1);
                            log("Custom lockscreen clock2 added successfully.");
                            break;
                    }
                }
            });
        } catch (Throwable t) {
            log(TAG + t);
        }
    }

    private static int getColorResCompat(Context context, @AttrRes int id) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, false);
        @SuppressLint("Recycle") TypedArray arr = context.obtainStyledAttributes(typedValue.data, new int[]{android.R.attr.textColorPrimary});
        return arr.getColor(0, -1);
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