package com.drdisagree.iconify.xposed.modules;

import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.DEPTH_WALLPAPER_FADE_ANIMATION;
import static com.drdisagree.iconify.common.Preferences.DEPTH_WALLPAPER_SWITCH;
import static com.drdisagree.iconify.common.Preferences.ICONIFY_DEPTH_WALLPAPER_TAG;
import static com.drdisagree.iconify.common.Preferences.ICONIFY_LOCKSCREEN_CLOCK_TAG;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_BOTTOMMARGIN;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_COLOR_CODE_ACCENT1;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_COLOR_CODE_ACCENT2;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_COLOR_CODE_ACCENT3;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_COLOR_CODE_TEXT1;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_COLOR_CODE_TEXT2;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_COLOR_SWITCH;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_FONT_LINEHEIGHT;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_FONT_SWITCH;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_FONT_TEXT_SCALING;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_STYLE;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_SWITCH;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_TOPMARGIN;
import static com.drdisagree.iconify.common.Resources.LOCKSCREEN_CLOCK_LAYOUT;
import static com.drdisagree.iconify.config.XPrefs.Xprefs;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.os.UserManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.drdisagree.iconify.BuildConfig;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.xposed.ModPack;
import com.drdisagree.iconify.xposed.modules.utils.ArcProgressWidget;
import com.drdisagree.iconify.xposed.modules.utils.ViewHelper;

import java.io.File;
import java.lang.reflect.Method;
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
    private UserManager mUserManager;
    private AudioManager mAudioManager;
    private ActivityManager mActivityManager;
    private Context appContext;
    private TextView mBatteryStatusView;
    private TextView mBatteryLevelView;
    private TextView mVolumeLevelView;
    private ProgressBar mBatteryProgress;
    private ProgressBar mVolumeProgress;
    private int mBatteryStatus = 1;
    private int mBatteryPercentage = 1;
    private ImageView mVolumeLevelArcProgress;
    private ImageView mRamUsageArcProgress;
    private static long lastUpdated = System.currentTimeMillis();
    private static final long thresholdTime = 500; // milliseconds
    private final BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null && intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
                mBatteryStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 1);
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
                mBatteryPercentage = (level * 100) / scale;
                initBatteryStatus();
            }
        }
    };
    private final BroadcastReceiver mVolumeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            initSoundManager();
        }
    };

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
                Objects.equals(Key[0], LSCLOCK_COLOR_CODE_ACCENT1) ||
                Objects.equals(Key[0], LSCLOCK_COLOR_CODE_ACCENT2) ||
                Objects.equals(Key[0], LSCLOCK_COLOR_CODE_ACCENT3) ||
                Objects.equals(Key[0], LSCLOCK_COLOR_CODE_TEXT1) ||
                Objects.equals(Key[0], LSCLOCK_COLOR_CODE_TEXT2) ||
                Objects.equals(Key[0], LSCLOCK_STYLE) ||
                Objects.equals(Key[0], LSCLOCK_TOPMARGIN) ||
                Objects.equals(Key[0], LSCLOCK_BOTTOMMARGIN) ||
                Objects.equals(Key[0], LSCLOCK_FONT_LINEHEIGHT) ||
                Objects.equals(Key[0], LSCLOCK_FONT_SWITCH) ||
                Objects.equals(Key[0], LSCLOCK_FONT_TEXT_SCALING) ||
                Objects.equals(Key[0], DEPTH_WALLPAPER_FADE_ANIMATION))) {
            updateClockView();
        }
    }

    @SuppressLint("DiscouragedApi")
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        initResources(mContext);

        Class<?> KeyguardStatusViewClass = findClass("com.android.keyguard.KeyguardStatusView", loadPackageParam.classLoader);

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

        Class<?> KeyguardBottomAreaViewClass = findClass(SYSTEMUI_PACKAGE + ".statusbar.phone.KeyguardBottomAreaView", loadPackageParam.classLoader);

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

    private void initResources(Context context) {
        try {
            appContext = context.createPackageContext(
                    BuildConfig.APPLICATION_ID,
                    Context.CONTEXT_IGNORE_SECURITY
            );
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        new Handler(Looper.getMainLooper()).post(() -> mUserManager = (UserManager) context.getSystemService(Context.USER_SERVICE));
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        try {
            context.registerReceiver(mBatteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        } catch (Exception ignored) {
        }
        try {
            context.registerReceiver(mVolumeReceiver, new IntentFilter("android.media.VOLUME_CHANGED_ACTION"));
        } catch (Exception ignored) {
        }
    }

    // Broadcast receiver for updating clock
    private void registerClockUpdater() {
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

        long currentTime = System.currentTimeMillis();
        boolean isClockAdded = mClockViewContainer.findViewWithTag(ICONIFY_LOCKSCREEN_CLOCK_TAG) != null;
        boolean isDepthClock = mClockViewContainer.getTag() == ICONIFY_DEPTH_WALLPAPER_TAG;

        if (isClockAdded && currentTime - lastUpdated < thresholdTime) {
            return;
        } else {
            lastUpdated = currentTime;
        }

        View clockView = getClockView();

        // Remove existing clock view
        if (isClockAdded) {
            mClockViewContainer.removeView(mClockViewContainer.findViewWithTag(ICONIFY_LOCKSCREEN_CLOCK_TAG));
        }

        if (clockView != null) {
            clockView.setTag(ICONIFY_LOCKSCREEN_CLOCK_TAG);

            int idx = 0;
            LinearLayout dummyLayout = null;

            if (isDepthClock) {
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
                    dummyLayout = mStatusViewContainer.findViewWithTag(dummy_tag);

                    if (dummyLayout == null) {
                        dummyLayout = new LinearLayout(mContext);
                        dummyLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                350
                        ));
                        dummyLayout.setTag(dummy_tag);

                        mStatusViewContainer.addView(dummyLayout, 0);
                    }
                }
            }

            if (clockView.getParent() != null) {
                ((ViewGroup) clockView.getParent()).removeView(clockView);
            }

//            TextUtil.convertTextViewsToTitleCase((ViewGroup) clockView);

            mClockViewContainer.addView(clockView, idx);
            modifyClockView(clockView);
            initSoundManager();
            initBatteryStatus();

            if (isDepthClock && dummyLayout != null) {
                ViewGroup.MarginLayoutParams dummyParams = (ViewGroup.MarginLayoutParams) dummyLayout.getLayoutParams();
                ViewGroup.MarginLayoutParams clockParams = (ViewGroup.MarginLayoutParams) clockView.getLayoutParams();
                dummyParams.topMargin = clockParams.topMargin;
                dummyParams.bottomMargin = clockParams.bottomMargin;
                dummyLayout.setLayoutParams(dummyParams);
            }
        }
    }

    @SuppressLint("DiscouragedApi")
    private View getClockView() {
        LayoutInflater inflater = LayoutInflater.from(appContext);
        int clockStyle = Xprefs.getInt(LSCLOCK_STYLE, 0);

        return inflater.inflate(
                appContext
                        .getResources()
                        .getIdentifier(
                                LOCKSCREEN_CLOCK_LAYOUT + clockStyle,
                                "layout",
                                BuildConfig.APPLICATION_ID
                        ),
                null
        );
    }

    private void modifyClockView(View clockView) {
        int clockStyle = Xprefs.getInt(LSCLOCK_STYLE, 0);
        int topMargin = Xprefs.getInt(LSCLOCK_TOPMARGIN, 100);
        int bottomMargin = Xprefs.getInt(LSCLOCK_BOTTOMMARGIN, 40);
        float clockScale = (float) (Xprefs.getInt(LSCLOCK_FONT_TEXT_SCALING, 10) / 10.0);
        String customFont = Environment.getExternalStorageDirectory() + "/.iconify_files/lsclock_font.ttf";
        int lineHeight = Xprefs.getInt(LSCLOCK_FONT_LINEHEIGHT, 0);
        boolean customFontEnabled = Xprefs.getBoolean(LSCLOCK_FONT_SWITCH, false);
        boolean customColorEnabled = Xprefs.getBoolean(LSCLOCK_COLOR_SWITCH, false);
        int accent1 = Xprefs.getInt(
                LSCLOCK_COLOR_CODE_ACCENT1,
                ContextCompat.getColor(mContext, android.R.color.system_accent1_300)
        );
        int accent2 = Xprefs.getInt(
                LSCLOCK_COLOR_CODE_ACCENT2,
                ContextCompat.getColor(mContext, android.R.color.system_accent2_300)
        );
        int accent3 = Xprefs.getInt(
                LSCLOCK_COLOR_CODE_ACCENT3,
                ContextCompat.getColor(mContext, android.R.color.system_accent3_300)
        );
        int text1 = Xprefs.getInt(
                LSCLOCK_COLOR_CODE_TEXT1,
                Color.WHITE
        );
        int text2 = Xprefs.getInt(
                LSCLOCK_COLOR_CODE_TEXT2,
                Color.BLACK
        );

        Typeface typeface = null;
        if (customFontEnabled && (new File(customFont).exists())) {
            typeface = Typeface.createFromFile(new File(customFont));
        }

        ViewHelper.setMargins(clockView, mContext, 0, topMargin, 0, bottomMargin);

        if (customColorEnabled) {
            ViewHelper.findViewWithTagAndChangeColor(clockView, "accent1", accent1);
            ViewHelper.findViewWithTagAndChangeColor(clockView, "accent2", accent2);
            ViewHelper.findViewWithTagAndChangeColor(clockView, "accent3", accent3);
            ViewHelper.findViewWithTagAndChangeColor(clockView, "text1", text1);
            ViewHelper.findViewWithTagAndChangeColor(clockView, "text2", text2);
        }

        if (typeface != null) {
            ViewHelper.applyFontRecursively(clockView, typeface);
        }

        ViewHelper.applyTextMarginRecursively(mContext, clockView, lineHeight);

        if (clockScale != 1) {
            ViewHelper.applyTextScalingRecursively(clockView, clockScale);
        }

        switch (clockStyle) {
            case 5 -> {
                mBatteryStatusView = clockView.findViewById(R.id.battery_status);
                mBatteryLevelView = clockView.findViewById(R.id.battery_percentage);
                mVolumeLevelView = clockView.findViewById(R.id.volume_level);
                mBatteryProgress = clockView.findViewById(R.id.battery_progressbar);
                mVolumeProgress = clockView.findViewById(R.id.volume_progressbar);
            }
            case 7 -> {
                TextView usernameView = clockView.findViewById(R.id.summary);
                usernameView.setText(getUserName());
                ImageView imageView = clockView.findViewById(R.id.user_profile_image);
                imageView.setImageDrawable(getUserImage());
            }
            case 19 -> {
                mBatteryLevelView = clockView.findViewById(R.id.battery_percentage);
                mBatteryProgress = clockView.findViewById(R.id.battery_progressbar);
                mVolumeLevelArcProgress = clockView.findViewById(R.id.volume_progress);
                mRamUsageArcProgress = clockView.findViewById(R.id.ram_usage_info);

                ((TextView) clockView.findViewById(R.id.device_name)).setText(Build.MODEL);
            }
            default -> {
                mBatteryStatusView = null;
                mBatteryLevelView = null;
                mVolumeLevelView = null;
                mBatteryProgress = null;
                mVolumeProgress = null;
            }
        }
    }

    private void initBatteryStatus() {
        if (mBatteryStatusView != null) {
            if (mBatteryStatus == BatteryManager.BATTERY_STATUS_CHARGING) {
                mBatteryStatusView.setText(R.string.battery_charging);
            } else if (mBatteryStatus == BatteryManager.BATTERY_STATUS_DISCHARGING ||
                    mBatteryStatus == BatteryManager.BATTERY_STATUS_NOT_CHARGING) {
                mBatteryStatusView.setText(R.string.battery_discharging);
            } else if (mBatteryStatus == BatteryManager.BATTERY_STATUS_FULL) {
                mBatteryStatusView.setText(R.string.battery_full);
            } else if (mBatteryStatus == BatteryManager.BATTERY_STATUS_UNKNOWN) {
                mBatteryStatusView.setText(R.string.battery_level_percentage);
            }
        }

        if (mBatteryProgress != null) {
            mBatteryProgress.setProgress(mBatteryPercentage);
        }
        if (mBatteryLevelView != null) {
            mBatteryLevelView.setText(appContext.getResources().getString(R.string.percentage_text, mBatteryPercentage));
        }

        initRamUsage();
    }

    private void initSoundManager() {
        int volLevel = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolLevel = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int volPercent = (int) (((float) volLevel / maxVolLevel) * 100);

        if (mVolumeProgress != null) {
            mVolumeProgress.setProgress(volPercent);
        }
        if (mVolumeLevelView != null) {
            mVolumeLevelView.setText(appContext.getResources().getString(R.string.percentage_text, volPercent));
        }

        if (mVolumeLevelArcProgress != null) {
            Bitmap widgetBitmap = ArcProgressWidget.generateBitmap(
                    mContext,
                    volPercent,
                    appContext.getResources().getString(R.string.percentage_text, volPercent),
                    40,
                    ContextCompat.getDrawable(appContext, R.drawable.ic_volume_up),
                    36
            );
            mVolumeLevelArcProgress.setImageBitmap(widgetBitmap);
        }
    }

    private void initRamUsage() {
        if (mActivityManager == null) return;

        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        mActivityManager.getMemoryInfo(memoryInfo);
        long usedMemory = memoryInfo.totalMem - memoryInfo.availMem;
        int usedMemoryPercentage = (int) ((usedMemory * 100) / memoryInfo.totalMem);

        if (mRamUsageArcProgress != null) {
            Bitmap widgetBitmap = ArcProgressWidget.generateBitmap(
                    mContext,
                    usedMemoryPercentage,
                    appContext.getResources().getString(R.string.percentage_text, usedMemoryPercentage),
                    40,
                    "RAM",
                    28
            );
            mRamUsageArcProgress.setImageBitmap(widgetBitmap);
        }
    }

    @SuppressLint("MissingPermission")
    private String getUserName() {
        if (mUserManager == null) {
            return "User";
        }

        String username = mUserManager.getUserName();
        return !username.isEmpty() ?
                mUserManager.getUserName() :
                appContext.getResources().getString(R.string.default_user_name);
    }

    @SuppressWarnings("all")
    private Drawable getUserImage() {
        if (mUserManager == null) {
            return appContext.getResources().getDrawable(R.drawable.default_avatar);
        }

        try {
            Method getUserIconMethod = mUserManager.getClass().getMethod("getUserIcon", int.class);
            int userId = (int) UserHandle.class.getDeclaredMethod("myUserId").invoke(null);
            Bitmap bitmapUserIcon = (Bitmap) getUserIconMethod.invoke(mUserManager, userId);
            return new BitmapDrawable(mContext.getResources(), bitmapUserIcon);
        } catch (Throwable throwable) {
            log(TAG + throwable);
            return appContext.getResources().getDrawable(R.drawable.default_avatar);
        }
    }
}