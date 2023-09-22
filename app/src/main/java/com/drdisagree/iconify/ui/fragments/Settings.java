package com.drdisagree.iconify.ui.fragments;

import static com.drdisagree.iconify.common.Const.FRAGMENT_BACK_BUTTON_DELAY;
import static com.drdisagree.iconify.common.Preferences.APP_ICON;
import static com.drdisagree.iconify.common.Preferences.APP_LANGUAGE;
import static com.drdisagree.iconify.common.Preferences.APP_THEME;
import static com.drdisagree.iconify.common.Preferences.AUTO_UPDATE;
import static com.drdisagree.iconify.common.Preferences.EASTER_EGG;
import static com.drdisagree.iconify.common.Preferences.FIRST_INSTALL;
import static com.drdisagree.iconify.common.Preferences.ON_HOME_PAGE;
import static com.drdisagree.iconify.common.Preferences.RESTART_SYSUI_AFTER_BOOT;
import static com.drdisagree.iconify.common.Preferences.RESTART_SYSUI_BEHAVIOR;
import static com.drdisagree.iconify.common.Preferences.SHOW_HOME_CARD;
import static com.drdisagree.iconify.common.Preferences.SHOW_XPOSED_WARN;
import static com.drdisagree.iconify.common.Preferences.UPDATE_OVER_WIFI;
import static com.drdisagree.iconify.common.Resources.MODULE_DIR;
import static com.drdisagree.iconify.utils.AppUtil.restartApplication;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
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

import com.drdisagree.iconify.BuildConfig;
import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.common.Const;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.config.RPrefs;
import com.drdisagree.iconify.databinding.FragmentSettingsBinding;
import com.drdisagree.iconify.services.UpdateScheduler;
import com.drdisagree.iconify.ui.activities.AppUpdates;
import com.drdisagree.iconify.ui.activities.Changelog;
import com.drdisagree.iconify.ui.activities.Credits;
import com.drdisagree.iconify.ui.activities.Experimental;
import com.drdisagree.iconify.ui.views.LoadingDialog;
import com.drdisagree.iconify.ui.views.RadioDialog;
import com.drdisagree.iconify.utils.CacheUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.drdisagree.iconify.utils.helpers.ImportExport;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.topjohnwu.superuser.Shell;

import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.Executors;

public class Settings extends BaseFragment implements RadioDialog.RadioDialogListener {

    final double SECONDS_FOR_CLICKS = 3;
    final int NUM_CLICKS_REQUIRED = 7;
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
    long[] clickTimestamps = new long[NUM_CLICKS_REQUIRED];
    int oldestIndex = 0;
    int nextIndex = 0;
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
        binding.header.toolbar.setTitle(getResources().getString(R.string.activity_title_settings));
        ((AppCompatActivity) requireActivity()).setSupportActionBar(binding.header.toolbar);
        setHasOptionsMenu(true);
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setDisplayShowHomeEnabled(true);
        binding.header.toolbar.setNavigationOnClickListener(view1 -> new Handler(Looper.getMainLooper()).postDelayed(() -> getParentFragmentManager().popBackStack(), FRAGMENT_BACK_BUTTON_DELAY));


        // Show loading dialog
        loadingDialog = new LoadingDialog(requireActivity());

        // Language
        int current_language = Arrays.asList(getResources().getStringArray(R.array.locale_code)).indexOf(Prefs.getString(APP_LANGUAGE, getResources().getConfiguration().getLocales().get(0).getLanguage()));
        rd_app_language = new RadioDialog(requireActivity(), 0, current_language == -1 ? 0 : current_language);
        rd_app_language.setRadioDialogListener(this);
        binding.settingsGeneral.appLanguage.setOnClickListener(v -> rd_app_language.show(R.string.settings_app_language, R.array.locale_name, binding.settingsGeneral.selectedAppLanguage));
        binding.settingsGeneral.selectedAppLanguage.setText(Arrays.asList(getResources().getStringArray(R.array.locale_name)).get(rd_app_language.getSelectedIndex()));

        // App Icon
        rd_app_icon = new RadioDialog(requireActivity(), 2, Prefs.getInt(APP_ICON, 0));
        rd_app_icon.setRadioDialogListener(this);
        binding.settingsGeneral.appIcon.setOnClickListener(v -> rd_app_icon.show(R.string.settings_app_icon, R.array.app_icon, binding.settingsGeneral.selectedAppIcon));
        binding.settingsGeneral.selectedAppIcon.setText(Arrays.asList(getResources().getStringArray(R.array.app_icon)).get(rd_app_icon.getSelectedIndex()));

        // App Theme
        rd_app_theme = new RadioDialog(requireActivity(), 1, Prefs.getInt(APP_THEME, 2));
        rd_app_theme.setRadioDialogListener(this);
        binding.settingsGeneral.appTheme.setOnClickListener(v -> rd_app_theme.show(R.string.settings_app_theme, R.array.app_theme, binding.settingsGeneral.selectedAppTheme));
        binding.settingsGeneral.selectedAppTheme.setText(Arrays.asList(getResources().getStringArray(R.array.app_theme)).get(rd_app_theme.getSelectedIndex()));

        // Check for update
        binding.settingsUpdate.currentVersion.setText(getResources().getString(R.string.settings_current_version, BuildConfig.VERSION_NAME));
        binding.settingsUpdate.checkUpdate.setOnClickListener(v -> startActivity(new Intent(requireActivity(), AppUpdates.class)));

        // Auto update
        binding.settingsUpdate.buttonAutoUpdate.setChecked(Prefs.getBoolean(AUTO_UPDATE, true));
        binding.settingsUpdate.buttonAutoUpdate.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Prefs.putBoolean(AUTO_UPDATE, isChecked);
            UpdateScheduler.scheduleUpdates(requireContext().getApplicationContext());
            binding.settingsUpdate.buttonAutoUpdateWifiOnly.setEnabled(isChecked);
        });
        binding.settingsUpdate.autoUpdate.setOnClickListener(v -> binding.settingsUpdate.buttonAutoUpdate.toggle());
        binding.settingsUpdate.buttonAutoUpdateWifiOnly.setEnabled(binding.settingsUpdate.buttonAutoUpdate.isChecked());

        // Check over wifi only
        binding.settingsUpdate.buttonAutoUpdateWifiOnly.setChecked(Prefs.getBoolean(UPDATE_OVER_WIFI, true));
        binding.settingsUpdate.buttonAutoUpdateWifiOnly.setOnCheckedChangeListener((buttonView, isChecked) -> Prefs.putBoolean(UPDATE_OVER_WIFI, isChecked));
        binding.settingsUpdate.autoUpdateWifiOnlyContainer.setOnClickListener(v -> {
            if (binding.settingsUpdate.buttonAutoUpdateWifiOnly.isEnabled())
                binding.settingsUpdate.buttonAutoUpdateWifiOnly.toggle();
        });

        // Show xposed warn
        binding.settingsXposed.hideWarnMessage.setChecked(Prefs.getBoolean(SHOW_XPOSED_WARN, true));
        binding.settingsXposed.hideWarnMessage.setOnCheckedChangeListener((buttonView, isChecked) -> Prefs.putBoolean(SHOW_XPOSED_WARN, isChecked));
        binding.settingsXposed.hideWarnMessageContainer.setOnClickListener(v -> binding.settingsXposed.hideWarnMessage.toggle());

        // Restart systemui behavior
        binding.settingsXposed.restartSystemuiBehavior.setChecked(Prefs.getBoolean(RESTART_SYSUI_BEHAVIOR, true));
        binding.settingsXposed.restartSystemuiBehavior.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Prefs.putBoolean(RESTART_SYSUI_BEHAVIOR, isChecked);
            if (isChecked) {
                binding.settingsXposed.restartSystemuiBehaviorDesc.setText(getResources().getString(R.string.settings_auto_restart_systemui));
            } else {
                binding.settingsXposed.restartSystemuiBehaviorDesc.setText(getResources().getString(R.string.settings_manual_restart_systemui));
            }
        });
        binding.settingsXposed.restartSystemuiBehaviorContainer.setOnClickListener(v -> binding.settingsXposed.restartSystemuiBehavior.toggle());
        if (binding.settingsXposed.restartSystemuiBehavior.isChecked()) {
            binding.settingsXposed.restartSystemuiBehaviorDesc.setText(getResources().getString(R.string.settings_auto_restart_systemui));
        } else {
            binding.settingsXposed.restartSystemuiBehaviorDesc.setText(getResources().getString(R.string.settings_manual_restart_systemui));
        }

        // Restart sysui after boot
        binding.settingsMisc.restartSysuiAfterBoot.setChecked(Prefs.getBoolean(RESTART_SYSUI_AFTER_BOOT, false));
        binding.settingsMisc.restartSysuiAfterBoot.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Prefs.putBoolean(RESTART_SYSUI_AFTER_BOOT, isChecked);
            if (isChecked) SystemUtil.enableRestartSystemuiAfterBoot();
            else SystemUtil.disableRestartSystemuiAfterBoot();
        });
        binding.settingsMisc.restartSysuiAfterBootContainer.setOnClickListener(v -> binding.settingsMisc.restartSysuiAfterBoot.toggle());

        // Home page card
        binding.settingsMisc.homePageCard.setChecked(Prefs.getBoolean(SHOW_HOME_CARD, true));
        binding.settingsMisc.homePageCard.setOnCheckedChangeListener((buttonView, isChecked) -> Prefs.putBoolean(SHOW_HOME_CARD, isChecked));
        binding.settingsMisc.homePageCardContainer.setOnClickListener(v -> binding.settingsMisc.homePageCard.toggle());

        // Clear App Cache
        binding.settingsMisc.clearCache.setOnClickListener(v -> {
            CacheUtil.clearCache(Iconify.getAppContext());
            Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_clear_cache), Toast.LENGTH_SHORT).show();
        });

        // Experimental features
        binding.settingsMisc.settingsMiscTitle.setOnClickListener(v -> onEasterViewClicked());
        binding.settingsMisc.experimentalFeatures.setOnClickListener(v -> startActivity(new Intent(requireActivity(), Experimental.class)));
        binding.settingsMisc.experimentalFeatures.setVisibility(Prefs.getBoolean(EASTER_EGG) ? View.VISIBLE : View.GONE);

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

        // Github repository
        binding.settingsAbout.githubRepository.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(Const.GITHUB_REPO))));

        // Telegram group
        binding.settingsAbout.telegramGroup.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(Const.TELEGRAM_GROUP));
            startActivity(intent);
        });

        // Credits
        binding.settingsAbout.credits.setOnClickListener(v -> startActivity(new Intent(requireActivity(), Credits.class)));

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
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemID = item.getItemId();

        if (itemID == R.id.menu_changelog) {
            Intent intent = new Intent(requireActivity(), Changelog.class);
            startActivity(intent);
        } else if (itemID == R.id.menu_export_settings) {
            importExportSettings(true);
        } else if (itemID == R.id.menu_import_settings) {
            importExportSettings(false);
        } else if (itemID == R.id.restart_systemui) {
            new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::restartSystemUI, 300);
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
        if (!SystemUtil.hasStoragePermission()) {
            SystemUtil.requestStoragePermission(requireContext());
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

    private void onEasterViewClicked() {
        long timeMillis = (new Date()).getTime();

        if (nextIndex == (NUM_CLICKS_REQUIRED - 1) || oldestIndex > 0) {
            int diff = (int) (timeMillis - clickTimestamps[oldestIndex]);
            if (diff < SECONDS_FOR_CLICKS * 1000) {
                if (!Prefs.getBoolean(EASTER_EGG)) {
                    Prefs.putBoolean(EASTER_EGG, true);
                    binding.settingsMisc.experimentalFeatures.setVisibility(View.VISIBLE);
                    Toast.makeText(requireContext(), requireContext().getResources().getString(R.string.toast_easter_egg), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), requireContext().getResources().getString(R.string.toast_easter_egg_activated), Toast.LENGTH_SHORT).show();
                }
                oldestIndex = 0;
                nextIndex = 0;
            } else oldestIndex++;
        }

        clickTimestamps[nextIndex] = timeMillis;
        nextIndex++;

        if (nextIndex == NUM_CLICKS_REQUIRED) nextIndex = 0;

        if (oldestIndex == NUM_CLICKS_REQUIRED) oldestIndex = 0;
    }
}