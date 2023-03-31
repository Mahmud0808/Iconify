package com.drdisagree.iconify;

import static com.drdisagree.iconify.common.Preferences.VER_CODE;
import static com.drdisagree.iconify.utils.SystemUtil.isDarkMode;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.airbnb.lottie.LottieCompositionFactory;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.ui.activities.HomePage;
import com.drdisagree.iconify.ui.activities.LandingPage1;
import com.drdisagree.iconify.utils.ModuleUtil;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.drdisagree.iconify.utils.RootUtil;
import com.google.android.material.color.DynamicColors;
import com.topjohnwu.superuser.Shell;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private static final boolean SKIP_TO_HOMEPAGE_FOR_TESTING_PURPOSES = false;

    static {
        Shell.enableVerboseLogging = BuildConfig.DEBUG;
        if (Shell.getCachedShell() == null)
            Shell.setDefaultBuilder(Shell.Builder.create().setFlags(Shell.FLAG_REDIRECT_STDERR).setTimeout(20));
    }

    private boolean keepShowing = true;
    private final Runnable runner = () -> Shell.getShell(shell -> {
        Intent intent;

        if (SKIP_TO_HOMEPAGE_FOR_TESTING_PURPOSES || (RootUtil.isDeviceRooted() && (RootUtil.isMagiskInstalled() || RootUtil.isKSUInstalled()) && ModuleUtil.moduleExists() && OverlayUtil.overlayExists() && (BuildConfig.VERSION_CODE == Prefs.getInt(VER_CODE)))) {
            keepShowing = false;
            intent = new Intent(SplashActivity.this, HomePage.class);
        } else {
            keepShowing = false;
            intent = new Intent(SplashActivity.this, LandingPage1.class);
        }

        startActivity(intent);
        finish();
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        splashScreen.setKeepOnScreenCondition(() -> keepShowing);
        DynamicColors.applyToActivitiesIfAvailable(getApplication());

        Thread thread = new Thread(runner);
        thread.start();

        new Thread(() -> {
            if (!isDarkMode()) {
                LottieCompositionFactory.fromRawRes(this, R.raw.anim_view_one_day);
                LottieCompositionFactory.fromRawRes(this, R.raw.anim_view_two_day);
                LottieCompositionFactory.fromRawRes(this, R.raw.anim_view_three_day);
            } else {
                LottieCompositionFactory.fromRawRes(this, R.raw.anim_view_one_night);
                LottieCompositionFactory.fromRawRes(this, R.raw.anim_view_two_night);
                LottieCompositionFactory.fromRawRes(this, R.raw.anim_view_three_night);
            }
        }).start();
    }
}
