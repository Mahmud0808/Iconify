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
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.RelativeLayout;

import com.drdisagree.iconify.xposed.ModPack;
import com.drdisagree.iconify.xposed.utils.LockscreenClockStyles;

import java.util.Objects;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class LockscreenClock extends ModPack implements IXposedHookLoadPackage {

    private static final String TAG = "Iconify - " + LockscreenClock.class.getSimpleName() + ": ";
    boolean showLockscreenClock = false;
    boolean autoHideClock = false;
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

        if (Key.length > 0 && (Objects.equals(Key[0], LSCLOCK_SWITCH) || Objects.equals(Key[0], LSCLOCK_AUTOHIDE) || Objects.equals(Key[0], LSCLOCK_COLOR_SWITCH) || Objects.equals(Key[0], LSCLOCK_COLOR_CODE) || Objects.equals(Key[0], LSCLOCK_STYLE) || Objects.equals(Key[0], LSCLOCK_TOPMARGIN) || Objects.equals(Key[0], LSCLOCK_BOTTOMMARGIN) || Objects.equals(Key[0], LSCLOCK_FONT_LINEHEIGHT) || Objects.equals(Key[0], LSCLOCK_FONT_SWITCH) || Objects.equals(Key[0], LSCLOCK_TEXT_WHITE) || Objects.equals(Key[0], LSCLOCK_FONT_TEXT_SCALING))) {
            if (!autoHideClock && mStatusViewContainer != null) {
                updateClockView(mStatusViewContainer);
            }

            if (autoHideClock && mLargeClockFrame != null) {
                updateClockView(mLargeClockFrame);
            }
        }
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
                            new Handler(Looper.getMainLooper()).post(() -> {
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
                            new Handler(Looper.getMainLooper()).post(() -> updateClockView(mStatusViewContainer));
                        }
                    }
                };

                mContext.registerReceiver(timeChangedReceiver, filter);
                updateClockView(mStatusViewContainer);
            }
        });
    }

    private void updateClockView(ViewGroup viewGroup) {
        ViewGroup clockView = LockscreenClockStyles.getClock(mContext);
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
}