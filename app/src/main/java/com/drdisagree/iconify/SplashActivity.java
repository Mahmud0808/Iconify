package com.drdisagree.iconify;

import static com.drdisagree.iconify.common.Preferences.XPOSED_ONLY_MODE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.ui.activities.MainActivity;
import com.drdisagree.iconify.ui.activities.OnboardingActivity;
import com.drdisagree.iconify.utils.ModuleUtil;
import com.drdisagree.iconify.utils.RootUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.drdisagree.iconify.utils.overlay.OverlayUtil;
import com.google.android.material.color.DynamicColors;
import com.topjohnwu.superuser.Shell;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    public static final boolean SKIP_INSTALLATION = false;
    public static final boolean SKIP_TO_HOMEPAGE_FOR_TESTING = SKIP_INSTALLATION && BuildConfig.DEBUG;

    static {
        Shell.enableVerboseLogging = BuildConfig.DEBUG;
        if (Shell.getCachedShell() == null) {
            Shell.setDefaultBuilder(Shell.Builder.create()
                    .setFlags(Shell.FLAG_MOUNT_MASTER)
                    .setFlags(Shell.FLAG_REDIRECT_STDERR)
                    .setTimeout(20)
            );
        }
    }

    private boolean keepShowing = true;
    private final Runnable runner = () -> Shell.getShell(shell -> {
        Intent intent;

        boolean isRooted = RootUtil.deviceProperlyRooted();
        boolean isModuleInstalled = ModuleUtil.moduleExists();
        boolean isOverlayInstalled = OverlayUtil.overlayExists();
        boolean isXposedOnlyMode = Prefs.getBoolean(XPOSED_ONLY_MODE, false);
        boolean isVersionCodeCorrect = BuildConfig.VERSION_CODE == SystemUtil.getSavedVersionCode();

        if (isRooted) {
            if (isOverlayInstalled) {
                Prefs.putBoolean(XPOSED_ONLY_MODE, false);
            } else if (isModuleInstalled) {
                Prefs.putBoolean(XPOSED_ONLY_MODE, true);
                isXposedOnlyMode = true;
            }
        }

        boolean isModuleProperlyInstalled = isModuleInstalled && (isOverlayInstalled || isXposedOnlyMode);

        if (SKIP_TO_HOMEPAGE_FOR_TESTING || (isRooted && isModuleProperlyInstalled && isVersionCodeCorrect)) {
            keepShowing = false;
            intent = new Intent(SplashActivity.this, MainActivity.class);
        } else {
            keepShowing = false;
            intent = new Intent(SplashActivity.this, OnboardingActivity.class);
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
    }
}
