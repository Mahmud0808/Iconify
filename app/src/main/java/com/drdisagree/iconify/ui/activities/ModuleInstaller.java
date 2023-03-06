package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Preferences.FIRST_INSTALL;
import static com.drdisagree.iconify.common.Preferences.UPDATE_DETECTED;
import static com.drdisagree.iconify.common.Preferences.VER_CODE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.drdisagree.iconify.BuildConfig;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.common.Resources;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.ui.views.InstallationDialog;
import com.drdisagree.iconify.utils.FileUtil;
import com.drdisagree.iconify.utils.ModuleUtil;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.drdisagree.iconify.utils.RootUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.drdisagree.iconify.utils.compiler.OverlayCompiler;
import com.drdisagree.iconify.utils.helpers.BackupRestore;
import com.topjohnwu.superuser.Shell;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class ModuleInstaller extends AppCompatActivity {

    private static final boolean SKIP_TO_HOMEPAGE_FOR_TESTING_PURPOSES = false;
    private static boolean hasErroredOut = false;
    @SuppressLint("StaticFieldLeak")
    private static LinearLayout warn;
    @SuppressLint("StaticFieldLeak")
    private static TextView warning;
    @SuppressLint("StaticFieldLeak")
    private static Button install_module, reboot_phone;
    private static startInstallationProcess installModule = null;
    private final String TAG = "ModuleInstaller";
    private InstallationDialog progressDialog;
    private String logger = null, prev_log = null;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module_installer);

        // Progress dialog while installing
        progressDialog = new InstallationDialog(this);

        // Continue button
        install_module = findViewById(R.id.install_module);

        // Reboot button
        reboot_phone = findViewById(R.id.btn_reboot);
        reboot_phone.setOnClickListener(v -> new Handler().postDelayed(SystemUtil::restartDevice, 200));

        // Warning messages
        warn = findViewById(R.id.warn);
        warning = findViewById(R.id.warning);

        AtomicBoolean clickedContinue = new AtomicBoolean(false);

        if (SKIP_TO_HOMEPAGE_FOR_TESTING_PURPOSES) {
            // Skip installation process for testing purposes
            Intent intent = new Intent(ModuleInstaller.this, HomePage.class);
            startActivity(intent);
            finish();
        } else {
            // Start installation on click
            install_module.setOnClickListener(v -> {
                hasErroredOut = false;
                if (RootUtil.isDeviceRooted()) {
                    if (RootUtil.isMagiskInstalled()) {
                        if (!Environment.isExternalStorageManager()) {
                            warning.setText(getResources().getString(R.string.perm_storage_access));
                            warn.setVisibility(View.VISIBLE);

                            new Handler().postDelayed(() -> {
                                clickedContinue.set(true);
                                SystemUtil.getStoragePermission(this);
                            }, clickedContinue.get() ? 10 : 1200);
                        } else {
                            if ((Prefs.getInt(VER_CODE) != BuildConfig.VERSION_CODE) || !ModuleUtil.moduleExists() || !OverlayUtil.overlayExists()) {
                                installModule = new startInstallationProcess();
                                installModule.execute();
                            } else {
                                Intent intent = new Intent(ModuleInstaller.this, HomePage.class);
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

            progressDialog.show(getResources().getString(R.string.installing), getResources().getString(R.string.init_module_installation));
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

            progressDialog.setMessage(title, desc);

            if (logger != null && !Objects.equals(prev_log, logger)) {
                progressDialog.setLogs(logger);
                prev_log = logger;
            }
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            int step = 0;

            logger = "Creating blank module template";
            publishProgress(++step);
            try {
                ModuleUtil.handleModule();
            } catch (IOException e) {
                hasErroredOut = true;
                Log.e(TAG, e.toString());
            }

            logger = null;
            publishProgress(++step);
            try {
                logger = "Cleaning iconify data directory";
                publishProgress(step);
                // Clean data directory
                Shell.cmd("rm -rf " + Resources.TEMP_DIR).exec();
                Shell.cmd("rm -rf " + Resources.DATA_DIR + "/Overlays").exec();

                logger = "Extracting overlays from assets";
                publishProgress(step);
                // Extract overlays from assets
                FileUtil.copyAssets("Overlays");
                ModuleUtil.extractPremadeOverlays();

                logger = "Creating temporary directories";
                publishProgress(step);
                // Create temp directory
                Shell.cmd("rm -rf " + Resources.TEMP_DIR + "; mkdir -p " + Resources.TEMP_DIR).exec();
                Shell.cmd("mkdir -p " + Resources.TEMP_OVERLAY_DIR).exec();
                Shell.cmd("mkdir -p " + Resources.UNSIGNED_UNALIGNED_DIR).exec();
                Shell.cmd("mkdir -p " + Resources.UNSIGNED_DIR).exec();
                Shell.cmd("mkdir -p " + Resources.SIGNED_DIR).exec();
            } catch (IOException e) {
                hasErroredOut = true;
                Log.e(TAG, e.toString());
            }

            logger = null;
            publishProgress(++step);
            // Create AndroidManifest.xml and build APK using AAPT
            File dir = new File(Resources.DATA_DIR + "/Overlays");
            if (dir.listFiles() == null) hasErroredOut = true;

            if (!hasErroredOut) {
                for (File pkg : Objects.requireNonNull(dir.listFiles())) {
                    if (pkg.isDirectory()) {
                        for (File overlay : Objects.requireNonNull(pkg.listFiles())) {
                            if (overlay.isDirectory()) {
                                String overlay_name = overlay.toString().replace(pkg.toString() + '/', "");

                                logger = "Creating manifest for " + overlay_name;
                                publishProgress(step);

                                if (OverlayCompiler.createManifest(overlay_name, pkg.toString().replace(Resources.DATA_DIR + "/Overlays/", ""), overlay.getAbsolutePath())) {
                                    hasErroredOut = true;
                                }

                                logger = "Building APK for " + overlay_name;
                                publishProgress(step);

                                if (!hasErroredOut && OverlayCompiler.runAapt(overlay.getAbsolutePath(), overlay_name)) {
                                    hasErroredOut = true;
                                }
                            }
                            if (hasErroredOut) break;
                        }
                    }
                    if (hasErroredOut) break;
                }
            }

            logger = null;
            publishProgress(++step);
            // ZipAlign the APK
            dir = new File(Resources.UNSIGNED_UNALIGNED_DIR);
            if (dir.listFiles() == null) hasErroredOut = true;

            if (!hasErroredOut) {
                for (File overlay : Objects.requireNonNull(dir.listFiles())) {
                    if (!overlay.isDirectory()) {
                        String overlay_name = overlay.toString().replace(Resources.UNSIGNED_UNALIGNED_DIR + '/', "").replace("-unaligned", "");

                        logger = "Zip aligning APK " + overlay_name.replace("-unsigned.apk", "");
                        publishProgress(step);

                        if (OverlayCompiler.zipAlign(overlay.getAbsolutePath(), overlay_name)) {
                            hasErroredOut = true;
                        }
                    }
                    if (hasErroredOut) break;
                }
            }

            logger = null;
            publishProgress(++step);
            // Sign the APK
            dir = new File(Resources.UNSIGNED_DIR);
            if (dir.listFiles() == null) hasErroredOut = true;

            if (!hasErroredOut) {
                for (File overlay : Objects.requireNonNull(dir.listFiles())) {
                    if (!overlay.isDirectory()) {
                        String overlay_name = overlay.toString().replace(Resources.UNSIGNED_DIR + '/', "").replace("-unsigned", "");

                        logger = "Signing APK " + overlay_name.replace(".apk", "");
                        publishProgress(step);

                        int attempt = 3;
                        while (attempt-- != 0) {
                            hasErroredOut = OverlayCompiler.apkSigner(overlay.getAbsolutePath(), overlay_name);

                            if (!hasErroredOut) break;
                            else try {
                                Thread.sleep(2000);
                            } catch (Exception ignored) {
                            }
                        }
                    }
                    if (hasErroredOut) break;
                }
            }

            logger = "Move overlays to module directory";
            publishProgress(++step);
            // Move all generated overlays to module dir
            if (!hasErroredOut) {
                Shell.cmd("cp -a " + Resources.SIGNED_DIR + "/. " + Resources.OVERLAY_DIR).exec();
                RootUtil.setPermissionsRecursively(644, Resources.OVERLAY_DIR + '/');
            }

            logger = "Cleaning temporary directories";
            publishProgress(step);
            // Clean temp directory
            Shell.cmd("rm -rf " + Resources.TEMP_DIR).exec();
            Shell.cmd("rm -rf " + Resources.DATA_DIR + "/Overlays").exec();

            // Restore backups
            BackupRestore.restoreFiles();
            logger = "Installtion process successfully finished";
            publishProgress(step);

            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);

            progressDialog.hide();

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
                        Intent intent = new Intent(ModuleInstaller.this, HomePage.class);
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
                Shell.cmd("rm -rf " + Resources.MODULE_DIR).exec();
                warning.setText(getResources().getString(R.string.installation_failed));
                warn.setVisibility(View.VISIBLE);
                install_module.setVisibility(View.VISIBLE);
                reboot_phone.setVisibility(View.GONE);
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Shell.cmd("rm -rf " + Resources.DATA_DIR).exec();
            Shell.cmd("rm -rf " + Resources.TEMP_DIR).exec();
            Shell.cmd("rm -rf " + Resources.BACKUP_DIR).exec();
            Shell.cmd("rm -rf " + Resources.MODULE_DIR).exec();
        }
    }
}