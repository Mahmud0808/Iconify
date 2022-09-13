package com.drdisagree.iconify;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import com.drdisagree.iconify.config.PrefConfig;
import com.drdisagree.iconify.ui.HomePage;
import com.drdisagree.iconify.ui.MainActivity;
import com.drdisagree.iconify.utils.OverlayUtils;
import com.drdisagree.iconify.utils.RootUtil;
import com.topjohnwu.superuser.Shell;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private final int versionCode = BuildConfig.VERSION_CODE;
    private final String versionName = BuildConfig.VERSION_NAME;
    private static SplashActivity mContext;

    static {
        Shell.enableVerboseLogging = BuildConfig.DEBUG;
        Shell.setDefaultBuilder(Shell.Builder.create().setFlags(Shell.FLAG_REDIRECT_STDERR).setTimeout(10));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        Shell.getShell(shell -> {

        Intent intent;

        if (RootUtil.isDeviceRooted() && RootUtil.isMagiskInstalled() && OverlayUtils.moduleExists() && OverlayUtils.overlayExists() && (versionCode == PrefConfig.loadPrefInt(this, "versionCode"))) {
            intent = new Intent(SplashActivity.this, HomePage.class);
        } else {
            intent = new Intent(SplashActivity.this, MainActivity.class);
        }

        startActivity(intent);
        finish();
        });
    }

    public static SplashActivity getContext() {
        return mContext;
    }
}