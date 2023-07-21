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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.config.RPrefs;
import com.drdisagree.iconify.ui.activities.AppUpdates;
import com.drdisagree.iconify.ui.activities.Changelog;
import com.drdisagree.iconify.ui.activities.Experimental;
import com.drdisagree.iconify.ui.activities.Info;
import com.drdisagree.iconify.ui.views.LoadingDialog;
import com.drdisagree.iconify.ui.views.RadioDialog;
import com.drdisagree.iconify.utils.CacheUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.drdisagree.iconify.utils.helpers.ImportExport;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.topjohnwu.superuser.Shell;

import java.util.Arrays;
import java.util.Objects;

public class Settings extends BaseFragment implements RadioDialog.RadioDialogListener {

    private View view;
    private LoadingDialog loadingDialog;
    private RadioDialog rd_app_language, rd_app_icon, rd_app_theme;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = view.findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_settings));
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);

        // Show loading dialog
        loadingDialog = new LoadingDialog(requireActivity());

        // Language
        LinearLayout app_language = view.findViewById(R.id.app_language);
        TextView selected_app_language = view.findViewById(R.id.selected_app_language);
        int current_language = Arrays.asList(getResources().getStringArray(R.array.locale_code)).indexOf(Prefs.getString(APP_LANGUAGE, getResources().getConfiguration().getLocales().get(0).getLanguage()));
        rd_app_language = new RadioDialog(requireActivity(), 0, current_language == -1 ? 0 : current_language);
        rd_app_language.setRadioDialogListener(this);
        app_language.setOnClickListener(v -> rd_app_language.show(R.string.app_language, R.array.locale_name, selected_app_language));
        selected_app_language.setText(Arrays.asList(getResources().getStringArray(R.array.locale_name)).get(rd_app_language.getSelectedIndex()));

        // App Icon
        LinearLayout app_icon = view.findViewById(R.id.app_icon);
        TextView selected_app_icon = view.findViewById(R.id.selected_app_icon);
        rd_app_icon = new RadioDialog(requireActivity(), 2, Prefs.getInt(APP_ICON, 0));
        rd_app_icon.setRadioDialogListener(this);
        app_icon.setOnClickListener(v -> rd_app_icon.show(R.string.app_icon, R.array.app_icon, selected_app_icon));
        selected_app_icon.setText(Arrays.asList(getResources().getStringArray(R.array.app_icon)).get(rd_app_icon.getSelectedIndex()));

        // App Theme
        LinearLayout app_theme = view.findViewById(R.id.app_theme);
        TextView selected_app_theme = view.findViewById(R.id.selected_app_theme);
        rd_app_theme = new RadioDialog(requireActivity(), 1, Prefs.getInt(APP_THEME, 2));
        rd_app_theme.setRadioDialogListener(this);
        app_theme.setOnClickListener(v -> rd_app_theme.show(R.string.app_theme, R.array.app_theme, selected_app_theme));
        selected_app_theme.setText(Arrays.asList(getResources().getStringArray(R.array.app_theme)).get(rd_app_theme.getSelectedIndex()));

        // Restart sysui after boot
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch restart_sysui_after_boot = view.findViewById(R.id.restart_sysui_after_boot);
        restart_sysui_after_boot.setChecked(Prefs.getBoolean(RESTART_SYSUI_AFTER_BOOT, false));
        restart_sysui_after_boot.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Prefs.putBoolean(RESTART_SYSUI_AFTER_BOOT, isChecked);
            if (isChecked) SystemUtil.enableRestartSystemuiAfterBoot();
            else SystemUtil.disableRestartSystemuiAfterBoot();
        });

        // Show xposed warn
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch hide_warn_message = view.findViewById(R.id.hide_warn_message);
        hide_warn_message.setChecked(Prefs.getBoolean(SHOW_XPOSED_WARN, true));
        hide_warn_message.setOnCheckedChangeListener((buttonView, isChecked) -> Prefs.putBoolean(SHOW_XPOSED_WARN, isChecked));

        // Clear App Cache
        LinearLayout clear_cache = view.findViewById(R.id.clear_cache);
        clear_cache.setOnClickListener(v -> {
            CacheUtil.clearCache(Iconify.getAppContext());
            Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_clear_cache), Toast.LENGTH_SHORT).show();
        });

        // Restart SystemUI
        LinearLayout button_restartSysui = view.findViewById(R.id.button_restartSysui);
        button_restartSysui.setOnClickListener(v -> {
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
        LinearLayout button_disableEverything = view.findViewById(R.id.button_disableEverything);
        button_disableEverything.setOnClickListener(v -> Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_disable_everything), Toast.LENGTH_SHORT).show());
        button_disableEverything.setOnLongClickListener(v -> {
            // Show loading dialog
            loadingDialog.show(getResources().getString(R.string.loading_dialog_wait));

            new Handler(Looper.getMainLooper()).post(() -> {
                disableEverything();

                requireActivity().runOnUiThread(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    // Hide loading dialog
                    loadingDialog.hide();

                    // Restart SystemUI
                    SystemUtil.restartSystemUI();
                }, 3000));
            });

            return true;
        });

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

    ActivityResultLauncher<Intent> startExportActivityIntent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result1 -> {
                if (result1.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result1.getData();
                    if (data == null) return;

                    try {
                        ImportExport.exportSettings(Prefs.prefs, requireContext().getContentResolver().openOutputStream(data.getData()));
                        Toast.makeText(requireContext(), requireContext().getResources().getString(R.string.toast_export_settings_successfull), Toast.LENGTH_SHORT).show();
                    } catch (Exception exception) {
                        Toast.makeText(requireContext(), requireContext().getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                        Log.e("Settings", "Error exporting settings", exception);
                    }
                }
            });

    ActivityResultLauncher<Intent> startImportActivityIntent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result2 -> {
                if (result2.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result2.getData();
                    if (data == null) return;

                    AlertDialog alertDialog = new AlertDialog.Builder(requireContext()).create();
                    alertDialog.setTitle(requireContext().getResources().getString(R.string.import_settings_confirmation_title));
                    alertDialog.setMessage(requireContext().getResources().getString(R.string.import_settings_confirmation_desc));
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, requireContext().getResources().getString(R.string.btn_positive),
                            (dialog, which) -> {
                                dialog.dismiss();
                                new Handler(Looper.getMainLooper()).post(() -> {
                                    try {
                                        boolean success = ImportExport.importSettings(Prefs.prefs, requireContext().getContentResolver().openInputStream(data.getData()), true);
                                        if (success) {
                                            Toast.makeText(requireContext(), requireContext().getResources().getString(R.string.toast_import_settings_successfull), Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(requireContext(), requireContext().getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (Exception exception) {
                                        Toast.makeText(requireContext(), requireContext().getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                                        Log.e("Settings", "Error importing settings", exception);
                                    }
                                });
                            });
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, requireContext().getResources().getString(R.string.btn_negative),
                            (dialog, which) -> dialog.dismiss());
                    alertDialog.show();
                }
            });

    private void changeIcon(String splash) {
        PackageManager manager = requireActivity().getPackageManager();
        String[] splashActivities = getResources().getStringArray(R.array.app_icon_identifier);

        for (String splashActivity : splashActivities) {
            manager.setComponentEnabledSetting(new ComponentName(requireActivity(), "com.drdisagree.iconify." + splashActivity), Objects.equals(splash, splashActivity) ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }
    }

    public static void disableEverything() {
        Prefs.clearAllPrefs();
        RPrefs.clearAllPrefs();

        SystemUtil.getBootId();
        SystemUtil.disableBlur();
        SystemUtil.getVersionCode();
        Prefs.putBoolean(ON_HOME_PAGE, true);
        Prefs.putBoolean(FIRST_INSTALL, false);

        Shell.cmd("> " + MODULE_DIR + "/common/system.prop; > " + MODULE_DIR + "/post-exec.sh; for ol in $(cmd overlay list | grep -E '^.x.*IconifyComponent' | sed -E 's/^.x..//'); do cmd overlay disable $ol; done; killall com.android.systemui").submit();
    }
}