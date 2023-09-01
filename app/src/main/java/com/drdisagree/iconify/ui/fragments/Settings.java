package com.drdisagree.iconify.ui.fragments;

import static com.drdisagree.iconify.common.Preferences.APP_ICON;
import static com.drdisagree.iconify.common.Preferences.APP_LANGUAGE;
import static com.drdisagree.iconify.common.Preferences.APP_THEME;
import static com.drdisagree.iconify.common.Preferences.EASTER_EGG;
import static com.drdisagree.iconify.common.Preferences.FIRST_INSTALL;
import static com.drdisagree.iconify.common.Preferences.ON_HOME_PAGE;
import static com.drdisagree.iconify.common.Preferences.RESTART_SYSUI_AFTER_BOOT;
import static com.drdisagree.iconify.common.Preferences.SHOW_XPOSED_WARN;
import static com.drdisagree.iconify.common.Resources.MODULE_DIR;
import static com.drdisagree.iconify.utils.AppUtil.restartApplication;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.config.RPrefs;
import com.drdisagree.iconify.databinding.FragmentSettingsBinding;
import com.drdisagree.iconify.ui.activities.AppUpdates;
import com.drdisagree.iconify.ui.activities.Changelog;
import com.drdisagree.iconify.ui.activities.Experimental;
import com.drdisagree.iconify.ui.activities.Info;
import com.drdisagree.iconify.ui.views.LoadingDialog;
import com.drdisagree.iconify.ui.views.RadioDialog;
import com.drdisagree.iconify.utils.CacheUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.drdisagree.iconify.utils.helpers.ImportExport;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.topjohnwu.superuser.Shell;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.Executors;

public class Settings extends BaseFragment implements RadioDialog.RadioDialogListener {

    ActivityResultLauncher<Intent> startExportActivityIntent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result1 -> {
                if (result1.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result1.getData();
                    if (data == null) return;

                    Executors.newSingleThreadExecutor().execute(() -> {
                        try {
                            ImportExport.exportSettings(Prefs.prefs, Objects.requireNonNull(Objects.requireNonNull(Iconify.getAppContext()).getContentResolver().openOutputStream(Objects.requireNonNull(data.getData()))));
                            new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(Iconify.getAppContext(), Iconify.getAppContext().getResources().getString(R.string.toast_export_settings_successfull), Toast.LENGTH_SHORT).show());
                        } catch (Exception exception) {
                            new Handler(Looper.getMainLooper()).post(() -> {
                                Toast.makeText(Iconify.getAppContext(), Objects.requireNonNull(Iconify.getAppContext()).getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                                Log.e("Settings", "Error exporting settings", exception);
                            });
                        }
                    });
                }
            });
    private FragmentSettingsBinding binding;
    private LoadingDialog loadingDialog;
    ActivityResultLauncher<Intent> startImportActivityIntent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result2 -> {
                if (result2.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result2.getData();
                    if (data == null) return;

                    new MaterialAlertDialogBuilder(requireContext(), R.style.MaterialComponents_MaterialAlertDialog)
                            .setTitle(requireContext().getResources().getString(R.string.import_settings_confirmation_title))
                            .setMessage(requireContext().getResources().getString(R.string.import_settings_confirmation_desc))
                            .setPositiveButton(requireContext().getResources().getString(R.string.btn_positive),
                                    (dialog, which) -> {
                                        dialog.dismiss();
                                        loadingDialog.show(getResources().getString(R.string.loading_dialog_wait));

                                        Executors.newSingleThreadExecutor().execute(() -> {
                                            try {
                                                boolean success = ImportExport.importSettings(Prefs.prefs, Objects.requireNonNull(Objects.requireNonNull(Iconify.getAppContext()).getContentResolver().openInputStream(Objects.requireNonNull(data.getData()))), true);

                                                new Handler(Looper.getMainLooper()).post(() -> {
                                                    loadingDialog.hide();

                                                    if (success) {
                                                        Toast.makeText(Iconify.getAppContext(), Objects.requireNonNull(Iconify.getAppContext()).getResources().getString(R.string.toast_import_settings_successfull), Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Toast.makeText(Iconify.getAppContext(), Objects.requireNonNull(Iconify.getAppContext()).getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            } catch (Exception exception) {
                                                new Handler(Looper.getMainLooper()).post(() -> {
                                                    Toast.makeText(Iconify.getAppContext(), Objects.requireNonNull(Iconify.getAppContext()).getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                                                    Log.e("Settings", "Error importing settings", exception);
                                                });
                                            }
                                        });
                                    })
                            .setNegativeButton(requireContext().getResources().getString(R.string.btn_negative), (dialog, which) -> dialog.dismiss())
                            .show();
                }
            });
    private RadioDialog rd_app_language, rd_app_icon, rd_app_theme;

    public static void disableEverything() {
        Prefs.clearAllPrefs();
        RPrefs.clearAllPrefs();

        SystemUtil.getBootId();
        SystemUtil.disableBlur();
        SystemUtil.saveVersionCode();
        Prefs.putBoolean(ON_HOME_PAGE, true);
        Prefs.putBoolean(FIRST_INSTALL, false);

        Shell.cmd("> " + MODULE_DIR + "/common/system.prop; > " + MODULE_DIR + "/post-exec.sh; for ol in $(cmd overlay list | grep -E '^.x.*IconifyComponent' | sed -E 's/^.x..//'); do cmd overlay disable $ol; done; killall com.android.systemui").submit();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Header
        binding.header.collapsingToolbar.setTitle(getResources().getString(R.string.activity_title_settings));
        ((AppCompatActivity) requireActivity()).setSupportActionBar(binding.header.toolbar);
        setHasOptionsMenu(true);

        // Show loading dialog
        loadingDialog = new LoadingDialog(requireActivity());

        // Language
        int current_language = Arrays.asList(getResources().getStringArray(R.array.locale_code)).indexOf(Prefs.getString(APP_LANGUAGE, getResources().getConfiguration().getLocales().get(0).getLanguage()));
        rd_app_language = new RadioDialog(requireActivity(), 0, current_language == -1 ? 0 : current_language);
        rd_app_language.setRadioDialogListener(this);
        binding.settingsGeneral.appLanguage.setOnClickListener(v -> rd_app_language.show(R.string.app_language, R.array.locale_name, binding.settingsGeneral.selectedAppLanguage));
        binding.settingsGeneral.selectedAppLanguage.setText(Arrays.asList(getResources().getStringArray(R.array.locale_name)).get(rd_app_language.getSelectedIndex()));

        // App Icon
        rd_app_icon = new RadioDialog(requireActivity(), 2, Prefs.getInt(APP_ICON, 0));
        rd_app_icon.setRadioDialogListener(this);
        binding.settingsGeneral.appIcon.setOnClickListener(v -> rd_app_icon.show(R.string.app_icon, R.array.app_icon, binding.settingsGeneral.selectedAppIcon));
        binding.settingsGeneral.selectedAppIcon.setText(Arrays.asList(getResources().getStringArray(R.array.app_icon)).get(rd_app_icon.getSelectedIndex()));

        // App Theme
        rd_app_theme = new RadioDialog(requireActivity(), 1, Prefs.getInt(APP_THEME, 2));
        rd_app_theme.setRadioDialogListener(this);
        binding.settingsGeneral.appTheme.setOnClickListener(v -> rd_app_theme.show(R.string.app_theme, R.array.app_theme, binding.settingsGeneral.selectedAppTheme));
        binding.settingsGeneral.selectedAppTheme.setText(Arrays.asList(getResources().getStringArray(R.array.app_theme)).get(rd_app_theme.getSelectedIndex()));

        // Restart sysui after boot
        binding.settingsGeneral.restartSysuiAfterBoot.setChecked(Prefs.getBoolean(RESTART_SYSUI_AFTER_BOOT, false));
        binding.settingsGeneral.restartSysuiAfterBoot.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Prefs.putBoolean(RESTART_SYSUI_AFTER_BOOT, isChecked);
            if (isChecked) SystemUtil.enableRestartSystemuiAfterBoot();
            else SystemUtil.disableRestartSystemuiAfterBoot();
        });

        // Show xposed warn
        binding.settingsXposed.hideWarnMessage.setChecked(Prefs.getBoolean(SHOW_XPOSED_WARN, true));
        binding.settingsXposed.hideWarnMessage.setOnCheckedChangeListener((buttonView, isChecked) -> Prefs.putBoolean(SHOW_XPOSED_WARN, isChecked));

        // Clear App Cache
        binding.settingsMisc.clearCache.setOnClickListener(v -> {
            CacheUtil.clearCache(Iconify.getAppContext());
            Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_clear_cache), Toast.LENGTH_SHORT).show();
        });

        // Restart SystemUI
        binding.settingsMisc.buttonRestartSysui.setOnClickListener(v -> {
            // Show loading dialog
            loadingDialog.show(getResources().getString(R.string.loading_dialog_wait));

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                // Hide loading dialog
                loadingDialog.hide();

                // Restart SystemUI
                SystemUtil.restartSystemUI();
            }, 1000);
        });

        // Disable Everything
        binding.settingsMisc.buttonDisableEverything.setOnClickListener(v -> new MaterialAlertDialogBuilder(requireActivity(), R.style.MaterialComponents_MaterialAlertDialog)
                .setCancelable(true)
                .setTitle(requireContext().getResources().getString(R.string.import_settings_confirmation_title))
                .setMessage(requireContext().getResources().getString(R.string.import_settings_confirmation_desc))
                .setPositiveButton(getString(R.string.positive), (dialog, i) -> {
                    dialog.dismiss();

                    // Show loading dialog
                    loadingDialog.show(getResources().getString(R.string.loading_dialog_wait));

                    Executors.newSingleThreadExecutor().execute(() -> {
                        disableEverything();

                        requireActivity().runOnUiThread(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            // Hide loading dialog
                            loadingDialog.hide();

                            // Restart SystemUI
                            SystemUtil.restartSystemUI();
                        }, 3000));
                    });
                })
                .setNegativeButton(getString(R.string.negative), (dialog, i) -> dialog.dismiss())
                .show());

        return view;
    }

    @Override
    public void onDestroy() {
        if (loadingDialog != null) loadingDialog.hide();
        if (rd_app_language != null) rd_app_language.dismiss();
        if (rd_app_icon != null) rd_app_icon.dismiss();
        if (rd_app_theme != null) rd_app_theme.dismiss();
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.settings_menu, menu);
        menu.findItem(R.id.menu_experimental_features).setVisible(Prefs.getBoolean(EASTER_EGG));

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemID = item.getItemId();

        if (itemID == R.id.menu_updates) {
            Intent intent = new Intent(requireActivity(), AppUpdates.class);
            startActivity(intent);
        } else if (itemID == R.id.menu_changelog) {
            Intent intent = new Intent(requireActivity(), Changelog.class);
            startActivity(intent);
        } else if (itemID == R.id.menu_export_settings) {
            importExportSettings(true);
        } else if (itemID == R.id.menu_import_settings) {
            importExportSettings(false);
        } else if (itemID == R.id.menu_experimental_features) {
            Intent intent = new Intent(requireActivity(), Experimental.class);
            startActivity(intent);
        } else if (itemID == R.id.menu_info) {
            Intent intent = new Intent(requireActivity(), Info.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(int dialogId, int selectedIndex) {
        switch (dialogId) {
            case 0:
                Prefs.putString(APP_LANGUAGE, Arrays.asList(getResources().getStringArray(R.array.locale_code)).get(selectedIndex));
                restartApplication(requireActivity());
                break;
            case 1:
                Prefs.putInt(APP_THEME, selectedIndex);
                restartApplication(requireActivity());
                break;
            case 2:
                Prefs.putInt(APP_ICON, selectedIndex);
                String[] splashActivities = getResources().getStringArray(R.array.app_icon_identifier);
                changeIcon(splashActivities[selectedIndex]);
                break;
        }
    }

    private void importExportSettings(boolean export) {
        if (!Environment.isExternalStorageManager()) {
            SystemUtil.getStoragePermission(requireContext());
        } else {
            Intent fileIntent = new Intent();
            fileIntent.setAction(export ? Intent.ACTION_CREATE_DOCUMENT : Intent.ACTION_GET_CONTENT);
            fileIntent.setType("*/*");
            fileIntent.putExtra(Intent.EXTRA_TITLE, "configs" + ".iconify");
            if (export) {
                startExportActivityIntent.launch(fileIntent);
            } else {
                startImportActivityIntent.launch(fileIntent);
            }
        }
    }

    private void changeIcon(String splash) {
        PackageManager manager = requireActivity().getPackageManager();
        String[] splashActivities = getResources().getStringArray(R.array.app_icon_identifier);

        for (String splashActivity : splashActivities) {
            manager.setComponentEnabledSetting(new ComponentName(requireActivity(), "com.drdisagree.iconify." + splashActivity), Objects.equals(splash, splashActivity) ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }
    }
}