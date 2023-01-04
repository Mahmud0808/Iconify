package com.drdisagree.iconify.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.drdisagree.iconify.BuildConfig;
import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.PrefConfig;
import com.drdisagree.iconify.utils.ModuleUtil;
import com.drdisagree.iconify.utils.OverlayUtils;
import com.drdisagree.iconify.utils.RootUtil;

import java.io.IOException;

public class WelcomePage extends AppCompatActivity {

    private final int versionCode = BuildConfig.VERSION_CODE;
    private final String versionName = BuildConfig.VERSION_NAME;
    LoadingDialog loadingDialog;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_page);

        // Loading dialog while installing module
        loadingDialog = new LoadingDialog(this);

        // Continue button
        Button checkRoot = findViewById(R.id.checkRoot);

        // Dialog to show warns
        LinearLayout warn = findViewById(R.id.warn);
        TextView warning = findViewById(R.id.warning);

        // Check for root onClick
        checkRoot.setOnClickListener(v -> {
            if (RootUtil.isDeviceRooted()) {
                if (RootUtil.isMagiskInstalled()) {
                    if (!Environment.isExternalStorageManager()) {
                        warning.setText("Grant storage access first!");
                        warn.setVisibility(View.VISIBLE);
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                        Uri uri = Uri.fromParts("package", this.getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    } else {
                        if ((PrefConfig.loadPrefInt(this, "versionCode") < versionCode) || !ModuleUtil.moduleExists() || !OverlayUtils.overlayExists()) {
                            warn.setVisibility(View.INVISIBLE);
                            // Show loading dialog
                            loadingDialog.show("Installing");

                            Runnable runnable = () -> {
                                try {
                                    ModuleUtil.handleModule(Iconify.getAppContext());
                                } catch (IOException e) {
                                    Toast.makeText(Iconify.getAppContext(), "Something went wrong.", Toast.LENGTH_LONG).show();
                                    e.printStackTrace();
                                }
                                runOnUiThread(() -> {
                                    // Hide loading dialog
                                    loadingDialog.hide();

                                    if (OverlayUtils.overlayExists()) {
                                        new Handler().postDelayed(() -> {
                                            Intent intent = new Intent(WelcomePage.this, HomePage.class);
                                            startActivity(intent);
                                            finish();
                                        }, 10);
                                    } else {
                                        warning.setText("Reboot your device first!");
                                        warn.setVisibility(View.VISIBLE);
                                    }
                                });
                            };
                            Thread thread = new Thread(runnable);
                            thread.start();
                        } else {
                            Intent intent = new Intent(WelcomePage.this, HomePage.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                } else {
                    warning.setText("Use Magisk to root your device!");
                    warn.setVisibility(View.VISIBLE);
                }
            } else {
                warning.setText("Looks like your device is not rooted!");
                warn.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onDestroy() {
        loadingDialog.hide();
        super.onDestroy();
    }
}