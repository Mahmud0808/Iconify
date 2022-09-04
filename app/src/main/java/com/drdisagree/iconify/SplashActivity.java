package com.drdisagree.iconify;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.topjohnwu.superuser.Shell;

public class SplashActivity extends AppCompatActivity {

    private final int versionCode = BuildConfig.VERSION_CODE;
    private final String versionName = BuildConfig.VERSION_NAME;

    static {
        Shell.enableVerboseLogging = true;
        Shell.setDefaultBuilder(Shell.Builder.create().setFlags(Shell.FLAG_REDIRECT_STDERR).setTimeout(10));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
}