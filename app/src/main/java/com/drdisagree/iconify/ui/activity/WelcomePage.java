package com.drdisagree.iconify.ui.activity;

import static android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION;
import static com.drdisagree.iconify.common.References.FIRST_INSTALL;
import static com.drdisagree.iconify.common.References.UPDATE_DETECTED;
import static com.drdisagree.iconify.common.References.VER_CODE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.drdisagree.iconify.BuildConfig;
import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.common.References;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.ui.view.LoadingDialog;
import com.drdisagree.iconify.utils.ModuleUtil;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.drdisagree.iconify.utils.RootUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.topjohnwu.superuser.Shell;

import java.io.IOException;

public class WelcomePage extends AppCompatActivity {

    private static boolean hasErroredOut = false;
    LoadingDialog loadingDialog;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_page);

        // Loading dialog while installing module
        loadingDialog = new LoadingDialog(this);

        // Continue button
        Button install_module = findViewById(R.id.install_module);

        // Reboot button
        Button reboot_phone = findViewById(R.id.reboot_phone);
        reboot_phone.setOnClickListener(v -> new Handler().postDelayed(SystemUtil::restartDevice, 200));

        // Dialog to show warns
        LinearLayout warn = findViewById(R.id.warn);
        TextView warning = findViewById(R.id.warning);

        // Check for root onClick
        install_module.setOnClickListener(v -> {
            if (RootUtil.isDeviceRooted()) {
                if (RootUtil.isMagiskInstalled()) {
                    if (!Environment.isExternalStorageManager()) {
                        warning.setText(getResources().getString(R.string.perm_storage_access));
                        warn.setVisibility(View.VISIBLE);
                        Intent intent = new Intent();
                        intent.setAction(ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                        Uri uri = Uri.fromParts("package", this.getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    } else {
                        if ((Prefs.getInt(VER_CODE) != BuildConfig.VERSION_CODE) || !ModuleUtil.moduleExists() || !OverlayUtil.overlayExists()) {
                            warn.setVisibility(View.INVISIBLE);
                            // Show loading dialog
                            loadingDialog.show(getResources().getString(R.string.installing));

                            Runnable runnable = () -> {
                                try {
                                    hasErroredOut = ModuleUtil.handleModule();
                                } catch (IOException e) {
                                    Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_error), Toast.LENGTH_LONG).show();
                                    hasErroredOut = true;
                                    e.printStackTrace();
                                }
                                runOnUiThread(() -> {
                                    // Hide loading dialog
                                    loadingDialog.hide();

                                    if (!hasErroredOut) {
                                        if (BuildConfig.VERSION_CODE != Prefs.getInt(VER_CODE, -1)) {
                                            if (Prefs.getBoolean(FIRST_INSTALL, true)) {
                                                Prefs.putBoolean(FIRST_INSTALL, true);
                                                Prefs.putBoolean(UPDATE_DETECTED, false);
                                            } else {
                                                Prefs.putBoolean(FIRST_INSTALL, false);
                                                Prefs.putBoolean(UPDATE_DETECTED, true);
                                            }
                                            Prefs.putInt(VER_CODE, BuildConfig.VERSION_CODE);
                                        }

                                        if (OverlayUtil.overlayExists()) {
                                            new Handler().postDelayed(() -> {
                                                Intent intent = new Intent(WelcomePage.this, HomePage.class);
                                                startActivity(intent);
                                                finish();
                                            }, 10);
                                        } else {
                                            warning.setText(getResources().getString(R.string.reboot_needed));
                                            warn.setVisibility(View.VISIBLE);
                                            install_module.setVisibility(View.GONE);
                                            reboot_phone.setVisibility(View.VISIBLE);
                                        }
                                    } else {
                                        Shell.cmd("rm -rf " + References.MODULE_DIR).exec();
                                        warning.setText(getResources().getString(R.string.installation_failed));
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
        });
    }

    @Override
    public void onDestroy() {
        loadingDialog.hide();
        super.onDestroy();
    }
}