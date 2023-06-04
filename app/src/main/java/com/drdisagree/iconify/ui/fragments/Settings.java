package com.drdisagree.iconify.ui.fragments;

import static com.drdisagree.iconify.common.Const.SWITCH_ANIMATION_DELAY;
import static com.drdisagree.iconify.common.Preferences.APP_ICON;
import static com.drdisagree.iconify.common.Preferences.APP_LANGUAGE;
import static com.drdisagree.iconify.common.Preferences.APP_THEME;
import static com.drdisagree.iconify.common.Preferences.EASTER_EGG;
import static com.drdisagree.iconify.common.Preferences.FIRST_INSTALL;
import static com.drdisagree.iconify.common.Preferences.FORCE_APPLY_XPOSED_CHOICE;
import static com.drdisagree.iconify.common.Preferences.RESTART_SYSUI_AFTER_BOOT;
import static com.drdisagree.iconify.common.Preferences.SHOW_XPOSED_WARN;
import static com.drdisagree.iconify.common.Preferences.USE_LIGHT_ACCENT;
import static com.drdisagree.iconify.utils.AppUtil.restartApplication;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.text.LineBreaker;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.common.Resources;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.config.RPrefs;
import com.drdisagree.iconify.ui.activities.AppUpdates;
import com.drdisagree.iconify.ui.activities.Changelog;
import com.drdisagree.iconify.ui.activities.Experimental;
import com.drdisagree.iconify.ui.activities.Info;
import com.drdisagree.iconify.ui.views.LoadingDialog;
import com.drdisagree.iconify.ui.views.RadioDialog;
import com.drdisagree.iconify.utils.FabricatedUtil;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.topjohnwu.superuser.Shell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Settings extends BaseFragment implements RadioDialog.RadioDialogListener {

    public static List<String> EnabledOverlays = OverlayUtil.getEnabledOverlayList();
    public static List<String> EnabledFabricatedOverlays = new ArrayList<>();
    LoadingDialog loadingDialog;
    RadioDialog rd_force_apply_method, rd_app_language, rd_app_icon, rd_app_theme;

    public static void disableEverything() {
        SharedPreferences prefs = Iconify.getAppContext().getSharedPreferences(Iconify.getAppContext().getPackageName(), Context.MODE_PRIVATE);
        Map<String, ?> map = prefs.getAll();

        for (Map.Entry<String, ?> item : map.entrySet()) {
            if (item.getValue() instanceof Boolean && ((Boolean) item.getValue()) && item.getKey().contains("fabricated")) {
                EnabledFabricatedOverlays.add(item.getKey().replace("fabricated", ""));
            }
        }

        FabricatedUtil.disableOverlays(EnabledFabricatedOverlays.toArray(new String[0]));
        OverlayUtil.disableOverlays(EnabledOverlays.toArray(new String[0]));
        SystemUtil.disableBlur();
        Shell.cmd("touch " + Resources.MODULE_DIR + "/post-exec.sh").submit();

        Prefs.clearAllPrefs();
        SystemUtil.getBootId();
        SystemUtil.getVersionCode();
        Prefs.putBoolean(FIRST_INSTALL, false);
        RPrefs.clearAllPrefs();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

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
        rd_app_icon = new RadioDialog(requireActivity(), 3, Prefs.getInt(APP_ICON, 0));
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

        // Use light accent
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch use_light_accent = view.findViewById(R.id.use_light_accent);
        boolean useLightAccent = Prefs.getBoolean(USE_LIGHT_ACCENT, false) || Prefs.getBoolean("IconifyComponentAMACL.overlay") || Prefs.getBoolean("IconifyComponentAMGCL.overlay");

        Prefs.putBoolean(USE_LIGHT_ACCENT, useLightAccent);
        use_light_accent.setChecked(useLightAccent);

        use_light_accent.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Prefs.putBoolean(USE_LIGHT_ACCENT, isChecked);
            new Handler().postDelayed(() -> {
                if (isChecked) {
                    if (Prefs.getBoolean("IconifyComponentAMAC.overlay")) {
                        OverlayUtil.changeOverlayState("IconifyComponentAMAC.overlay", false, "IconifyComponentAMACL.overlay", true);
                    } else if (Prefs.getBoolean("IconifyComponentAMGC.overlay")) {
                        OverlayUtil.changeOverlayState("IconifyComponentAMGC.overlay", false, "IconifyComponentAMGCL.overlay", true);
                    }
                } else {
                    if (Prefs.getBoolean("IconifyComponentAMACL.overlay")) {
                        OverlayUtil.changeOverlayState("IconifyComponentAMACL.overlay", false, "IconifyComponentAMAC.overlay", true);
                    } else if (Prefs.getBoolean("IconifyComponentAMGCL.overlay")) {
                        OverlayUtil.changeOverlayState("IconifyComponentAMGCL.overlay", false, "IconifyComponentAMGC.overlay", true);
                    }
                }
            }, SWITCH_ANIMATION_DELAY);
        });

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

        // Force apply method
        LinearLayout force_apply_method = view.findViewById(R.id.force_apply_method);
        TextView selected_force_apply_method = view.findViewById(R.id.selected_force_apply_method);
        rd_force_apply_method = new RadioDialog(requireActivity(), 2, Prefs.getInt(FORCE_APPLY_XPOSED_CHOICE, 0) == -1 ? 2 : Prefs.getInt(FORCE_APPLY_XPOSED_CHOICE, 0));
        rd_force_apply_method.setRadioDialogListener(this);
        force_apply_method.setOnClickListener(v -> rd_force_apply_method.show(R.string.list_title_force_apply_method, R.array.xposed_force_apply_method, selected_force_apply_method));
        selected_force_apply_method.setText(Arrays.asList(getResources().getStringArray(R.array.xposed_force_apply_method)).get(rd_force_apply_method.getSelectedIndex() == -1 ? 2 : rd_force_apply_method.getSelectedIndex()));

        // Disable Everything
        TextView list_title_disableEverything = view.findViewById(R.id.list_title_disableEverything);
        TextView list_desc_disableEverything = view.findViewById(R.id.list_desc_disableEverything);
        Button button_disableEverything = view.findViewById(R.id.button_disableEverything);

        list_title_disableEverything.setText(getResources().getString(R.string.disable_everything_title));
        list_desc_disableEverything.setText(getResources().getString(R.string.disable_everything_desc));
        list_desc_disableEverything.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);

        button_disableEverything.setOnClickListener(v -> Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_disable_everything), Toast.LENGTH_SHORT).show());

        button_disableEverything.setOnLongClickListener(v -> {
            // Show loading dialog
            loadingDialog.show(getResources().getString(R.string.loading_dialog_wait));

            Runnable runnable = () -> {
                disableEverything();

                requireActivity().runOnUiThread(() -> new Handler().postDelayed(() -> {
                    // Hide loading dialog
                    loadingDialog.hide();

                    // Restart SystemUI
                    SystemUtil.restartSystemUI();
                }, 3000));
            };
            Thread thread = new Thread(runnable);
            thread.start();

            return true;
        });

        // Restart SystemUI
        TextView list_title_restartSysui = view.findViewById(R.id.list_title_restartSysui);
        TextView list_desc_restartSysui = view.findViewById(R.id.list_desc_restartSysui);
        Button button_restartSysui = view.findViewById(R.id.button_restartSysui);

        list_title_restartSysui.setText(getResources().getString(R.string.restart_sysui_title));
        list_desc_restartSysui.setText(getResources().getString(R.string.restart_sysui_desc));
        list_desc_restartSysui.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);

        button_restartSysui.setOnClickListener(v -> Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_restart_sysui), Toast.LENGTH_SHORT).show());

        button_restartSysui.setOnLongClickListener(v -> {
            // Show loading dialog
            loadingDialog.show(getResources().getString(R.string.loading_dialog_wait));

            new Handler().postDelayed(() -> {
                // Hide loading dialog
                loadingDialog.hide();

                // Restart SystemUI
                SystemUtil.restartSystemUI();
            }, 1000);

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
        if (rd_force_apply_method != null) rd_force_apply_method.dismiss();
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
                Prefs.putInt(FORCE_APPLY_XPOSED_CHOICE, selectedIndex == 2 ? -1 : selectedIndex);
                break;
            case 3:
                Prefs.putInt(APP_ICON, selectedIndex);
                String[] splashActivities = getResources().getStringArray(R.array.app_icon_identifier);
                changeIcon(splashActivities[selectedIndex]);
                break;
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