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
import static com.drdisagree.iconify.common.Preferences.RESTART_SYSUI_BEHAVIOR_EXT;
import static com.drdisagree.iconify.common.Preferences.SHOW_HOME_CARD;
import static com.drdisagree.iconify.common.Preferences.SHOW_XPOSED_WARN;
import static com.drdisagree.iconify.common.Preferences.UPDATE_OVER_WIFI;
import static com.drdisagree.iconify.common.Resources.MODULE_DIR;
import static com.drdisagree.iconify.utils.AppUtil.restartApplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.LocaleList;
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
import androidx.navigation.Navigation;

import com.drdisagree.iconify.BuildConfig;
import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.common.Const;
import com.drdisagree.iconify.common.Preferences;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.config.RPrefs;
import com.drdisagree.iconify.databinding.FragmentSettingsBinding;
import com.drdisagree.iconify.services.UpdateScheduler;
import com.drdisagree.iconify.ui.base.BaseFragment;
import com.drdisagree.iconify.ui.dialogs.LoadingDialog;
import com.drdisagree.iconify.utils.CacheUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.drdisagree.iconify.utils.helper.ImportExport;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.topjohnwu.superuser.Shell;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;

public class Settings extends BaseFragment {

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
                            ImportExport.exportSettings(Prefs.prefs, Objects.requireNonNull(Iconify.Companion.getAppContext().getContentResolver().openOutputStream(Objects.requireNonNull(data.getData()))));
                            new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(Iconify.Companion.getAppContext(), Iconify.Companion.getAppContextLocale().getResources().getString(R.string.toast_export_settings_successfull), Toast.LENGTH_SHORT).show());
                        } catch (Exception exception) {
                            new Handler(Looper.getMainLooper()).post(() -> {
                                Toast.makeText(Iconify.Companion.getAppContext(), Iconify.Companion.getAppContext().getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
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

                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle(requireContext().getResources().getString(R.string.import_settings_confirmation_title))
                            .setMessage(requireContext().getResources().getString(R.string.import_settings_confirmation_desc))
                            .setPositiveButton(requireContext().getResources().getString(R.string.btn_positive),
                                    (dialog, which) -> {
                                        dialog.dismiss();
                                        loadingDialog.show(getResources().getString(R.string.loading_dialog_wait));

                                        Executors.newSingleThreadExecutor().execute(() -> {
                                            try {
                                                boolean success = ImportExport.importSettings(Prefs.prefs, Objects.requireNonNull(Iconify.Companion.getAppContext().getContentResolver().openInputStream(Objects.requireNonNull(data.getData()))), true);

                                                new Handler(Looper.getMainLooper()).post(() -> {
                                                    loadingDialog.hide();

                                                    if (success) {
                                                        Toast.makeText(Iconify.Companion.getAppContext(), Iconify.Companion.getAppContext().getResources().getString(R.string.toast_import_settings_successfull), Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Toast.makeText(Iconify.Companion.getAppContext(), Iconify.Companion.getAppContext().getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            } catch (Exception exception) {
                                                new Handler(Looper.getMainLooper()).post(() -> {
                                                    Toast.makeText(Iconify.Companion.getAppContext(), Iconify.Companion.getAppContext().getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                                                    Log.e("Settings", "Error importing settings", exception);
                                                });
                                            }
                                        });
                                    })
                            .setNegativeButton(requireContext().getResources().getString(R.string.btn_negative), (dialog, which) -> dialog.dismiss())
                            .show();
                }
            });

    public static void disableEverything() {
        Prefs.clearAllPrefs();
        RPrefs.clearAllPrefs();

        SystemUtil.getBootId();
        SystemUtil.disableBlur(false);
        SystemUtil.saveVersionCode();
        Prefs.putBoolean(ON_HOME_PAGE, true);
        Prefs.putBoolean(FIRST_INSTALL, false);

        Shell.cmd("> " + MODULE_DIR + "/system.prop; > " + MODULE_DIR + "/post-exec.sh; for ol in $(cmd overlay list | grep -E '.x.*IconifyComponent' | sed -E 's/^.x..//'); do cmd overlay disable $ol; done; killall com.android.systemui").submit();
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
        int current_language = Arrays.asList(getResources().getStringArray(R.array.locale_code)).indexOf("en-US");
        LocaleList locales = getResources().getConfiguration().getLocales();
        List<String> locale_codes = Arrays.asList(getResources().getStringArray(R.array.locale_code));
        for (int i = 0; i < locales.size(); i++) {
            String languageCode = locales.get(i).getLanguage();
            String countryCode = locales.get(i).getCountry();
            String languageFormat = languageCode + "-" + countryCode;

            if (locale_codes.contains(Prefs.getString(APP_LANGUAGE, languageFormat))) {
                current_language = locale_codes.indexOf(Prefs.getString(APP_LANGUAGE, languageFormat));
                break;
            }
        }
        binding.settingsGeneral.appLanguage.setSelectedIndex(current_language);
        binding.settingsGeneral.appLanguage.setOnItemSelectedListener(index -> {
            Prefs.putString(APP_LANGUAGE, Arrays.asList(getResources().getStringArray(R.array.locale_code)).get(index));
            restartApplication(requireActivity());
        });

        // App Icon
        binding.settingsGeneral.appIcon.setSelectedIndex(Prefs.getInt(APP_ICON, 0));
        binding.settingsGeneral.appIcon.setOnItemSelectedListener(index -> {
            Prefs.putInt(APP_ICON, index);
            String[] splashActivities = Iconify.Companion.getAppContextLocale().getResources().getStringArray(R.array.app_icon_identifier);
            changeIcon(splashActivities[index]);
        });

        // App Theme
        binding.settingsGeneral.appTheme.setSelectedIndex(Prefs.getInt(APP_THEME, 2));
        binding.settingsGeneral.appTheme.setOnItemSelectedListener(index -> {
            Prefs.putInt(APP_THEME, index);
            restartApplication(requireActivity());
        });

        // Check for update
        binding.settingsUpdate.checkUpdate.setSummary(getResources().getString(R.string.settings_current_version, BuildConfig.VERSION_NAME));
        binding.settingsUpdate.checkUpdate.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_settings_to_appUpdates));

        // Auto update
        binding.settingsUpdate.autoUpdate.setSwitchChecked(Prefs.getBoolean(AUTO_UPDATE, true));
        binding.settingsUpdate.autoUpdate.setSwitchChangeListener((buttonView, isChecked) -> {
            Prefs.putBoolean(AUTO_UPDATE, isChecked);
            UpdateScheduler.scheduleUpdates(requireContext().getApplicationContext());
            binding.settingsUpdate.autoUpdateWifiOnly.setEnabled(isChecked);
        });

        // Check over wifi only
        binding.settingsUpdate.autoUpdateWifiOnly.setEnabled(binding.settingsUpdate.autoUpdate.isSwitchChecked());
        binding.settingsUpdate.autoUpdateWifiOnly.setSwitchChecked(Prefs.getBoolean(UPDATE_OVER_WIFI, true));
        binding.settingsUpdate.autoUpdateWifiOnly.setSwitchChangeListener((buttonView, isChecked) -> Prefs.putBoolean(UPDATE_OVER_WIFI, isChecked));

        // Show xposed warn
        binding.settingsXposed.hideWarnMessage.setSwitchChecked(Prefs.getBoolean(SHOW_XPOSED_WARN, true));
        binding.settingsXposed.hideWarnMessage.setSwitchChangeListener((buttonView, isChecked) -> Prefs.putBoolean(SHOW_XPOSED_WARN, isChecked));

        // Restart systemui behavior
        binding.settingsXposed.modApplyingMethod.setSelectedIndex(RPrefs.getInt(RESTART_SYSUI_BEHAVIOR_EXT, 0));
        binding.settingsXposed.modApplyingMethod.setOnItemSelectedListener(index -> RPrefs.putInt(RESTART_SYSUI_BEHAVIOR_EXT, index));

        // Restart sysui after boot
        binding.settingsMisc.restartSysuiAfterBoot.setSwitchChecked(Prefs.getBoolean(RESTART_SYSUI_AFTER_BOOT, false));
        binding.settingsMisc.restartSysuiAfterBoot.setSwitchChangeListener((buttonView, isChecked) -> {
            Prefs.putBoolean(RESTART_SYSUI_AFTER_BOOT, isChecked);
            if (isChecked) SystemUtil.enableRestartSystemuiAfterBoot();
            else SystemUtil.disableRestartSystemuiAfterBoot();
        });

        // Home page card
        binding.settingsMisc.homePageCard.setSwitchChecked(Prefs.getBoolean(SHOW_HOME_CARD, true));
        binding.settingsMisc.homePageCard.setSwitchChangeListener((buttonView, isChecked) -> Prefs.putBoolean(SHOW_HOME_CARD, isChecked));
        binding.settingsMisc.homePageCard.setVisibility(Preferences.isXposedOnlyMode ? View.GONE : View.VISIBLE);

        // Clear App Cache
        binding.settingsMisc.clearCache.setOnClickListener(v -> {
            CacheUtil.clearCache(Iconify.Companion.getAppContext());
            Toast.makeText(Iconify.Companion.getAppContext(), Iconify.Companion.getAppContextLocale().getResources().getString(R.string.toast_clear_cache), Toast.LENGTH_SHORT).show();
        });

        // Experimental features
        binding.settingsMisc.settingsMiscTitle.setOnClickListener(v -> onEasterViewClicked());
        binding.settingsMisc.experimentalFeatures.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_settings_to_experimental));
        binding.settingsMisc.experimentalFeatures.setVisibility(Prefs.getBoolean(EASTER_EGG) ? View.VISIBLE : View.GONE);

        // Disable Everything
        binding.settingsMisc.buttonDisableEverything.setOnClickListener(v -> new MaterialAlertDialogBuilder(requireActivity())
                .setCancelable(true)
                .setTitle(requireContext().getResources().getString(R.string.import_settings_confirmation_title))
                .setMessage(requireContext().getResources().getString(R.string.import_settings_confirmation_desc))
                .setPositiveButton(getString(R.string.positive), (dialog, i) -> {
                    dialog.dismiss();

                    // Show loading dialog
                    loadingDialog.show(getResources().getString(R.string.loading_dialog_wait));

                    Executors.newSingleThreadExecutor().execute(() -> {
                        disableEverything();

                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            // Hide loading dialog
                            loadingDialog.hide();

                            // Restart SystemUI
                            SystemUtil.restartSystemUI();
                        }, 3000);
                    });
                })
                .setNegativeButton(getString(R.string.negative), (dialog, i) -> dialog.dismiss())
                .show());
        binding.settingsMisc.buttonDisableEverything.setVisibility(Preferences.isXposedOnlyMode ? View.GONE : View.VISIBLE);

        // Github repository
        binding.settingsAbout.githubRepository.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(Const.GITHUB_REPO))));

        // Telegram group
        binding.settingsAbout.telegramGroup.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(Const.TELEGRAM_GROUP));
            startActivity(intent);
        });

        // Credits
        binding.settingsAbout.credits.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_settings_to_credits2));

        return view;
    }

    @Override
    public void onDestroy() {
        if (loadingDialog != null) loadingDialog.hide();
        super.onDestroy();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.settings_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_changelog ->
                    Navigation.findNavController(requireView()).navigate(R.id.action_settings_to_changelog2);
            case R.id.menu_export_settings -> importExportSettings(true);
            case R.id.menu_import_settings -> importExportSettings(false);
            case R.id.restart_systemui ->
                    new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::restartSystemUI, 300);
        }

        return super.onOptionsItemSelected(item);
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
        String[] splashActivities = Iconify.Companion.getAppContextLocale().getResources().getStringArray(R.array.app_icon_identifier);

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