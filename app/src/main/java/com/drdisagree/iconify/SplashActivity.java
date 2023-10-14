package com.drdisagree.iconify;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.airbnb.lottie.LottieCompositionFactory;
import com.drdisagree.iconify.ui.activities.HomePage;
import com.drdisagree.iconify.ui.activities.Onboarding;
import com.drdisagree.iconify.utils.ModuleUtil;
import com.drdisagree.iconify.utils.RootUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.google.android.material.color.DynamicColors;
import com.topjohnwu.superuser.Shell;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    public static final boolean SKIP_TO_HOMEPAGE_FOR_TESTING_PURPOSES = false;

    static {
        Shell.enableVerboseLogging = BuildConfig.DEBUG;
//        if (Shell.getCachedShell() == null) {
//            Shell.setDefaultBuilder(Shell.Builder.create()
//                    .setFlags(Shell.FLAG_MOUNT_MASTER)
//                    .setFlags(Shell.FLAG_REDIRECT_STDERR)
//                    .setTimeout(20)
//            );
//        }
    }

    private boolean keepShowing = true;
    private final Runnable runner = () -> Shell.getShell(shell -> {
        Intent intent;

        if ((SKIP_TO_HOMEPAGE_FOR_TESTING_PURPOSES && BuildConfig.DEBUG) ||
                (RootUtil.deviceProperlyRooted() && ModuleUtil.moduleProperlyInstalled() &&
                        BuildConfig.VERSION_CODE == SystemUtil.getSavedVersionCode())) {
            keepShowing = false;
            intent = new Intent(SplashActivity.this, HomePage.class);
        } else {
            keepShowing = false;
            intent = new Intent(SplashActivity.this, Onboarding.class);
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

        new Thread(runner).start();

        new Thread(() -> {
            LottieCompositionFactory.fromRawRes(this, R.raw.onboarding_lottie_1);
            LottieCompositionFactory.fromRawRes(this, R.raw.onboarding_lottie_2);
            LottieCompositionFactory.fromRawRes(this, R.raw.onboarding_lottie_3);
        }).start();
    }
}
