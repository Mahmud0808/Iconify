package com.drdisagree.iconify.xposed.modules;

import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.DEPTH_WALLPAPER_SWITCH;
import static com.drdisagree.iconify.common.Preferences.ICONIFY_DEPTH_WALLPAPER_TAG;
import static com.drdisagree.iconify.common.Preferences.ICONIFY_LOCKSCREEN_CLOCK_TAG;
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
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.drdisagree.iconify.xposed.ModPack;
import com.drdisagree.iconify.xposed.utils.LockscreenClockStyles;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class LockscreenClock extends ModPack implements IXposedHookLoadPackage {

    private static final String TAG = "Iconify - " + LockscreenClock.class.getSimpleName() + ": ";
    private boolean showLockscreenClock = false;
    private boolean showDepthWallpaper = false;
    private ViewGroup mClockViewContainer = null;
    private ViewGroup mStatusViewContainer = null;

    public LockscreenClock(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;

        showLockscreenClock = Xprefs.getBoolean(LSCLOCK_SWITCH, false);
        showDepthWallpaper = Xprefs.getBoolean(DEPTH_WALLPAPER_SWITCH, false);

        if (Key.length > 0 && (Objects.equals(Key[0], LSCLOCK_SWITCH) ||
                Objects.equals(Key[0], DEPTH_WALLPAPER_SWITCH) ||
                Objects.equals(Key[0], LSCLOCK_COLOR_SWITCH) ||
                Objects.equals(Key[0], LSCLOCK_COLOR_CODE) ||
                Objects.equals(Key[0], LSCLOCK_STYLE) ||
                Objects.equals(Key[0], LSCLOCK_TOPMARGIN) ||
                Objects.equals(Key[0], LSCLOCK_BOTTOMMARGIN) ||
                Objects.equals(Key[0], LSCLOCK_FONT_LINEHEIGHT) ||
                Objects.equals(Key[0], LSCLOCK_FONT_SWITCH) ||
                Objects.equals(Key[0], LSCLOCK_TEXT_WHITE) ||
                Objects.equals(Key[0], LSCLOCK_FONT_TEXT_SCALING))) {
            updateClockView();
        }
    }

    @SuppressLint("DiscouragedApi")
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!lpparam.packageName.equals(SYSTEMUI_PACKAGE)) return;

        Class<?> KeyguardStatusViewClass = findClass("com.android.keyguard.KeyguardStatusView", lpparam.classLoader);

        hookAllMethods(KeyguardStatusViewClass, "onFinishInflate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (!showLockscreenClock) return;

                mStatusViewContainer = (ViewGroup) getObjectField(param.thisObject, "mStatusViewContainer");

                if (!showDepthWallpaper) {
                    mClockViewContainer = mStatusViewContainer;
                }

                // Hide stock clock
                GridLayout KeyguardStatusView = (GridLayout) param.thisObject;

                RelativeLayout mClockView = KeyguardStatusView.findViewById(mContext.getResources().getIdentifier("keyguard_clock_container", "id", mContext.getPackageName()));
                mClockView.getLayoutParams().height = 0;
                mClockView.getLayoutParams().width = 0;
                mClockView.setVisibility(View.INVISIBLE);

                View mMediaHostContainer = (View) getObjectField(param.thisObject, "mMediaHostContainer");
                mMediaHostContainer.getLayoutParams().height = 0;
                mMediaHostContainer.getLayoutParams().width = 0;
                mMediaHostContainer.setVisibility(View.INVISIBLE);

                registerClockUpdater();
            }
        });

        Class<?> KeyguardBottomAreaViewClass = findClass(SYSTEMUI_PACKAGE + ".statusbar.phone.KeyguardBottomAreaView", lpparam.classLoader);

        hookAllMethods(KeyguardBottomAreaViewClass, "onFinishInflate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (!showLockscreenClock || !showDepthWallpaper) return;

                View view = (View) param.thisObject;
                ViewGroup mIndicationArea = view.findViewById(mContext.getResources().getIdentifier("keyguard_indication_area", "id", mContext.getPackageName()));

                // Get the depth wallpaper layout and register clock updater
                try {
                    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
                    executor.scheduleAtFixedRate(() -> {
                        mClockViewContainer = mIndicationArea.findViewWithTag(ICONIFY_DEPTH_WALLPAPER_TAG);

                        if (mClockViewContainer != null) {
                            registerClockUpdater();
                            executor.shutdown();
                            executor.shutdownNow();
                        }

                        if (!showLockscreenClock || !showDepthWallpaper) {
                            executor.shutdown();
                            executor.shutdownNow();
                        }
                    }, 0, 200, TimeUnit.MILLISECONDS);
                } catch (Throwable ignored) {
                }
            }
        });

        try {
            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            executor.scheduleAtFixedRate(() -> {
                File Android = new File(Environment.getExternalStorageDirectory() + "/Android");

                if (Android.isDirectory()) {
                    updateClockView();
                    executor.shutdown();
                    executor.shutdownNow();
                }
            }, 0, 5, TimeUnit.SECONDS);
        } catch (Throwable ignored) {
        }
    }

    // Broadcast receiver for updating clock
    private void registerClockUpdater() {
        if (mClockViewContainer == null) return;

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        filter.addAction(Intent.ACTION_LOCALE_CHANGED);

        BroadcastReceiver timeChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    new Handler(Looper.getMainLooper()).post(() -> updateClockView());
                }
            }
        };

        mContext.registerReceiver(timeChangedReceiver, filter);

        updateClockView();
    }

    private void updateClockView() {
        if (mClockViewContainer == null) return;

        View clockView = LockscreenClockStyles.getClock(mContext);

        // Remove existing clock view
        if (mClockViewContainer.findViewWithTag(ICONIFY_LOCKSCREEN_CLOCK_TAG) != null) {
            mClockViewContainer.removeView(mClockViewContainer.findViewWithTag(ICONIFY_LOCKSCREEN_CLOCK_TAG));
        }

        if (clockView != null) {
            clockView.setTag(ICONIFY_LOCKSCREEN_CLOCK_TAG);

            int idx = 0;

            if (mClockViewContainer.getTag() == ICONIFY_DEPTH_WALLPAPER_TAG) {
                /*
                 If the clock view container is the depth wallpaper container, we need to
                 add the clock view to the middle of foreground and background images
                 */
                if (mClockViewContainer.getChildCount() > 0) {
                    idx = 1;
                }

                // Add a dummy layout to the status view container so that we can still move notifications
                if (mStatusViewContainer != null) {
                    String dummy_tag = "dummy_layout";
                    LinearLayout dummyLayout = mStatusViewContainer.findViewWithTag(dummy_tag);

                    if (dummyLayout == null) {
                        dummyLayout = new LinearLayout(mContext);
                        dummyLayout.setTag(dummy_tag);

                        mStatusViewContainer.addView(dummyLayout, 0);
                    }

                    dummyLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 300));
                    ((LinearLayout.LayoutParams) clockView.getLayoutParams()).gravity = Gravity.CENTER_VERTICAL;

                    ViewGroup.MarginLayoutParams dummyParams = (ViewGroup.MarginLayoutParams) dummyLayout.getLayoutParams();
                    ViewGroup.MarginLayoutParams clockParams = (ViewGroup.MarginLayoutParams) clockView.getLayoutParams();
                    dummyParams.topMargin = clockParams.topMargin;
                    dummyParams.bottomMargin = clockParams.bottomMargin;
                    dummyLayout.setLayoutParams(dummyParams);
                }
            }

            if (clockView.getParent() != null) {
                ((ViewGroup) clockView.getParent()).removeView(clockView);
            }
            mClockViewContainer.addView(clockView, idx);
        }
    }
}