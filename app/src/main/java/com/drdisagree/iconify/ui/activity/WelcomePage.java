package com.drdisagree.iconify.ui.activity;

import static android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION;
import static com.drdisagree.iconify.common.References.FIRST_INSTALL;
import static com.drdisagree.iconify.common.References.UPDATE_DETECTED;
import static com.drdisagree.iconify.common.References.VER_CODE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.drdisagree.iconify.BuildConfig;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.common.References;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.ui.view.LoadingDialogAlt;
import com.drdisagree.iconify.utils.ModuleUtil;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.drdisagree.iconify.utils.RootUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.drdisagree.iconify.utils.compilerutil.OverlayCompilerUtil;
import com.topjohnwu.superuser.Shell;

import java.io.IOException;

public class WelcomePage extends AppCompatActivity {

    private static boolean hasErroredOut = false;
    @SuppressLint("StaticFieldLeak")
    private static LinearLayout warn;
    @SuppressLint("StaticFieldLeak")
    private static TextView warning;
    @SuppressLint("StaticFieldLeak")
    private static Button install_module, reboot_phone;
    private static startInstallationProcess installModule = null;
    private LoadingDialogAlt loadingDialog;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_page);

        // Loading dialog while installing
        loadingDialog = new LoadingDialogAlt(this);

        // Continue button
        install_module = findViewById(R.id.install_module);

        // Reboot button
        reboot_phone = findViewById(R.id.reboot_phone);
        reboot_phone.setOnClickListener(v -> new Handler().postDelayed(SystemUtil::restartDevice, 200));

        warn = findViewById(R.id.warn);
        warning = findViewById(R.id.warning);

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
                            installModule = new startInstallationProcess();
                            installModule.execute();
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
        if (installModule != null) installModule.cancel(true);
        super.onDestroy();
    }

    @SuppressLint("StaticFieldLeak")
    private class startInstallationProcess extends AsyncTask<Void, Integer, Integer> {
        @SuppressLint("SetTextI18n")
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            warn.setVisibility(View.INVISIBLE);
            reboot_phone.setVisibility(View.GONE);

            loadingDialog.show(getResources().getString(R.string.installing), getResources().getString(R.string.init_module_installation));
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            String title = getResources().getString(R.string.step) + ' ' + values[0] + "/6";
            String desc = getResources().getString(R.string.loading_dialog_wait);

            switch (values[0]) {
                case 1:
                    desc = getResources().getString(R.string.module_installation_step1);
                    break;
                case 2:
                    desc = getResources().getString(R.string.module_installation_step2);
                    break;
                case 3:
                    desc = getResources().getString(R.string.module_installation_step3);
                    break;
                case 4:
                    desc = getResources().getString(R.string.module_installation_step4);
                    break;
                case 5:
                    desc = getResources().getString(R.string.module_installation_step5);
                    break;
                case 6:
                    desc = getResources().getString(R.string.module_installation_step6);
                    break;
            }

            loadingDialog.setMessage(title, desc);
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            int step = 1;

            publishProgress(step++);
            try {
                ModuleUtil.handleModule();
            } catch (IOException e) {
                hasErroredOut = true;
                e.printStackTrace();
            }

            publishProgress(step++);
            ModuleUtil.extractTools();
            ModuleUtil.extractPremadeOverlays();

            publishProgress(step++);
            try {
                OverlayCompilerUtil.preExecute();
            } catch (IOException e) {
                hasErroredOut = true;
                e.printStackTrace();
            }
            hasErroredOut = OverlayCompilerUtil.buildAPK();

            publishProgress(step++);
            hasErroredOut = OverlayCompilerUtil.alignAPK();

            publishProgress(step++);
            hasErroredOut = OverlayCompilerUtil.signAPK();

            publishProgress(step);
            OverlayCompilerUtil.postExecute(false);

            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);

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
                    install_module.setVisibility(View.GONE);
                    reboot_phone.setVisibility(View.VISIBLE);
                }
            } else {
                Shell.cmd("rm -rf " + References.MODULE_DIR).exec();
                warning.setText(getResources().getString(R.string.installation_failed));
                install_module.setVisibility(View.VISIBLE);
                reboot_phone.setVisibility(View.GONE);
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Shell.cmd("rm -rf " + References.DATA_DIR).exec();
            Shell.cmd("rm -rf " + References.TEMP_DIR).exec();
            Shell.cmd("rm -rf " + References.BACKUP_DIR).exec();
            Shell.cmd("rm -rf " + References.MODULE_DIR).exec();
        }
    }
}