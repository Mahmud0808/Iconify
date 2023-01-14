package com.drdisagree.iconify.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.drdisagree.iconify.BuildConfig;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.ui.fragment.LoadingDialog;

public class WelcomePage extends AppCompatActivity {

    private final int versionCode = BuildConfig.VERSION_CODE;
    LoadingDialog loadingDialog;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_page);

        // Loading dialog while installing module
        loadingDialog = new LoadingDialog(this);

        // Continue button
        Button checkRoot = findViewById(R.id.checkRoot);

        // Dialog to show warns
        LinearLayout warn = findViewById(R.id.warn);
        TextView warning = findViewById(R.id.warning);

        Intent intent = new Intent(WelcomePage.this, HomePage.class);
        startActivity(intent);
        finish();/*
        // Check for root onClick
        checkRoot.setOnClickListener(v -> {
            if (RootUtil.isDeviceRooted()) {
                if (RootUtil.isMagiskInstalled()) {
                    if (!Environment.isExternalStorageManager()) {
                        warning.setText(getResources().getString(R.string.perm_storage_access));
                        warn.setVisibility(View.VISIBLE);
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                        Uri uri = Uri.fromParts("package", this.getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    } else {
                        if ((Prefs.getInt("versionCode") < versionCode) || !ModuleUtil.moduleExists() || !OverlayUtil.overlayExists()) {
                            warn.setVisibility(View.INVISIBLE);
                            // Show loading dialog
                            loadingDialog.show(getResources().getString(R.string.installing));

                            Runnable runnable = () -> {
                                try {
                                    ModuleUtil.handleModule();
                                } catch (IOException e) {
                                    Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_error), Toast.LENGTH_LONG).show();
                                    e.printStackTrace();
                                }
                                runOnUiThread(() -> {
                                    // Hide loading dialog
                                    loadingDialog.hide();

                                    if (Prefs.getInt("versionCode") == 0) {
                                        Prefs.putBoolean("firstInstall", true);
                                        Prefs.putBoolean("updateDetected", false);
                                    } else {
                                        Prefs.putBoolean("firstInstall", false);
                                        Prefs.putBoolean("updateDetected", true);
                                    }
                                    Prefs.putInt("versionCode", versionCode);

                                    if (OverlayUtil.overlayExists()) {
                                        new Handler().postDelayed(() -> {
                                            Intent intent = new Intent(WelcomePage.this, HomePage.class);
                                            startActivity(intent);
                                            finish();
                                        }, 10);
                                    } else {
                                        warning.setText(getResources().getString(R.string.reboot_needed));
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
                    warning.setText(getResources().getString(R.string.use_magisk));
                    warn.setVisibility(View.VISIBLE);
                }
            } else {
                warning.setText(getResources().getString(R.string.root_not_found));
                warn.setVisibility(View.VISIBLE);
            }
        });*/
    }

    @Override
    public void onDestroy() {
        loadingDialog.hide();
        super.onDestroy();
    }
}