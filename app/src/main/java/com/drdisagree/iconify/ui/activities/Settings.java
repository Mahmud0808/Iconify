package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Preferences.EASTER_EGG;
import static com.drdisagree.iconify.common.Preferences.FIRST_INSTALL;
import static com.drdisagree.iconify.common.Preferences.FORCE_APPLY_XPOSED_CHOICE;
import static com.drdisagree.iconify.common.Preferences.RESTART_SYSUI_AFTER_BOOT;
import static com.drdisagree.iconify.common.Preferences.SHOW_XPOSED_WARN;
import static com.drdisagree.iconify.common.Preferences.USE_LIGHT_ACCENT;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.text.LineBreaker;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.config.RPrefs;
import com.drdisagree.iconify.ui.views.LoadingDialog;
import com.drdisagree.iconify.utils.FabricatedUtil;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Settings extends AppCompatActivity {

    private static final int REQUESTCODE_IMPORT = 1;
    private static final int REQUESTCODE_EXPORT = 2;
    public static List<String> EnabledOverlays = OverlayUtil.getEnabledOverlayList();
    LoadingDialog loadingDialog;

    public static void disableEverything() {
        SharedPreferences prefs = Iconify.getAppContext().getSharedPreferences(Iconify.getAppContext().getPackageName(), Context.MODE_PRIVATE);
        Map<String, ?> map = prefs.getAll();

        for (Map.Entry<String, ?> item : map.entrySet()) {
            if (item.getValue() instanceof Boolean && ((Boolean) item.getValue()) && item.getKey().contains("fabricated")) {
                Prefs.putBoolean(item.getKey(), (Boolean) item.getValue());
                FabricatedUtil.disableOverlay(item.getKey().replace("fabricated", ""));
            }
        }

        for (String overlay : EnabledOverlays) {
            OverlayUtil.disableOverlay(overlay);
        }

        Prefs.clearAllPrefs();
        SystemUtil.getBootId();
        SystemUtil.getVersionCode();
        Prefs.putBoolean(FIRST_INSTALL, false);

        RPrefs.clearAllPrefs();

        SystemUtil.restartSystemUI();
    }

    @SuppressLint({"SetTextI18n", "WorldReadableFiles"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Show loading dialog
        loadingDialog = new LoadingDialog(this);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_settings));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Use light accent
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch use_light_accent = findViewById(R.id.use_light_accent);
        if (Prefs.getBoolean(USE_LIGHT_ACCENT, false) || Prefs.getBoolean("IconifyComponentAMACL.overlay") || Prefs.getBoolean("IconifyComponentAMGCL.overlay")) {
            Prefs.putBoolean(USE_LIGHT_ACCENT, true);
            use_light_accent.setChecked(true);
        } else {
            Prefs.putBoolean(USE_LIGHT_ACCENT, false);
            use_light_accent.setChecked(false);
        }
        use_light_accent.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Prefs.putBoolean(USE_LIGHT_ACCENT, isChecked);
            if (isChecked) {
                if (Prefs.getBoolean("IconifyComponentAMAC.overlay")) {
                    OverlayUtil.disableOverlay("IconifyComponentAMAC.overlay");
                    OverlayUtil.enableOverlay("IconifyComponentAMACL.overlay");
                } else if (Prefs.getBoolean("IconifyComponentAMGC.overlay")) {
                    OverlayUtil.disableOverlay("IconifyComponentAMGC.overlay");
                    OverlayUtil.enableOverlay("IconifyComponentAMGCL.overlay");
                }
            } else {
                if (Prefs.getBoolean("IconifyComponentAMACL.overlay")) {
                    OverlayUtil.disableOverlay("IconifyComponentAMACL.overlay");
                    OverlayUtil.enableOverlay("IconifyComponentAMAC.overlay");
                } else if (Prefs.getBoolean("IconifyComponentAMGCL.overlay")) {
                    OverlayUtil.disableOverlay("IconifyComponentAMGCL.overlay");
                    OverlayUtil.enableOverlay("IconifyComponentAMGC.overlay");
                }
            }
        });

        // Restart sysui after boot
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch restart_sysui_after_boot = findViewById(R.id.restart_sysui_after_boot);
        restart_sysui_after_boot.setChecked(Prefs.getBoolean(RESTART_SYSUI_AFTER_BOOT, false));
        restart_sysui_after_boot.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Prefs.putBoolean(RESTART_SYSUI_AFTER_BOOT, isChecked);
            if (isChecked)
                SystemUtil.enableRestartSystemuiAfterBoot();
            else
                SystemUtil.disableRestartSystemuiAfterBoot();
        });

        // Show xposed warn
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch hide_warn_message = findViewById(R.id.hide_warn_message);
        hide_warn_message.setChecked(Prefs.getBoolean(SHOW_XPOSED_WARN, true));
        hide_warn_message.setOnCheckedChangeListener((buttonView, isChecked) -> Prefs.putBoolean(SHOW_XPOSED_WARN, isChecked));

        // Force apply method
        if (Prefs.getInt(FORCE_APPLY_XPOSED_CHOICE, 0) == 0)
            ((RadioButton) findViewById(R.id.apply_method_dark_mode)).setChecked(true);
        else if (Prefs.getInt(FORCE_APPLY_XPOSED_CHOICE, 0) == 1)
            ((RadioButton) findViewById(R.id.apply_method_restart_sysui)).setChecked(true);
        else if (Prefs.getInt(FORCE_APPLY_XPOSED_CHOICE, 0) == -1)
            ((RadioButton) findViewById(R.id.apply_method_do_nothing)).setChecked(true);

        // Statusbar color source select
        RadioGroup force_apply_method_selector = findViewById(R.id.force_apply_method_selector);

        force_apply_method_selector.setOnCheckedChangeListener((group, checkedId) -> {
            if (Objects.equals(checkedId, R.id.apply_method_dark_mode))
                Prefs.putInt(FORCE_APPLY_XPOSED_CHOICE, 0);
            else if (Objects.equals(checkedId, R.id.apply_method_restart_sysui))
                Prefs.putInt(FORCE_APPLY_XPOSED_CHOICE, 1);
            else if (Objects.equals(checkedId, R.id.apply_method_do_nothing))
                Prefs.putInt(FORCE_APPLY_XPOSED_CHOICE, -1);
        });

        // Disable Everything
        TextView list_title_disableEverything = findViewById(R.id.list_title_disableEverything);
        TextView list_desc_disableEverything = findViewById(R.id.list_desc_disableEverything);
        Button button_disableEverything = findViewById(R.id.button_disableEverything);

        list_title_disableEverything.setText(getResources().getString(R.string.disable_everything_title));
        list_desc_disableEverything.setText(getResources().getString(R.string.disable_everything_desc));
        list_desc_disableEverything.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);

        button_disableEverything.setOnClickListener(v -> Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_disable_everything), Toast.LENGTH_SHORT).show());

        button_disableEverything.setOnLongClickListener(v -> {
            // Show loading dialog
            loadingDialog.show(getResources().getString(R.string.loading_dialog_wait));

            Runnable runnable = () -> {
                disableEverything();

                runOnUiThread(() -> new Handler().postDelayed(() -> {
                    // Hide loading dialog
                    loadingDialog.hide();

                    Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_disabled_everything), Toast.LENGTH_SHORT).show();
                }, 3000));
            };
            Thread thread = new Thread(runnable);
            thread.start();

            return true;
        });

        // Restart SystemUI
        TextView list_title_restartSysui = findViewById(R.id.list_title_restartSysui);
        TextView list_desc_restartSysui = findViewById(R.id.list_desc_restartSysui);
        Button button_restartSysui = findViewById(R.id.button_restartSysui);

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
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);

        menu.findItem(R.id.menu_experimental_features).setVisible(Prefs.getBoolean(EASTER_EGG));

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemID = item.getItemId();

        if (itemID == android.R.id.home) {
            onBackPressed();
        } else if (itemID == R.id.menu_updates) {
            Intent intent = new Intent(Settings.this, AppUpdates.class);
            startActivity(intent);
        } else if (itemID == R.id.menu_changelog) {
            Intent intent = new Intent(Settings.this, Changelog.class);
            startActivity(intent);
        } else if (itemID == R.id.menu_exportPrefs) {
            exportSettings();
        } else if (itemID == R.id.menu_importPrefs) {
            importSettings();
        } else if (itemID == R.id.menu_experimental_features) {
            Intent intent = new Intent(Settings.this, Experimental.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    private void exportSettings() {
        Intent fileIntent = new Intent();
        fileIntent.setAction(Intent.ACTION_CREATE_DOCUMENT);
        fileIntent.setType("*/*");
        startActivityForResult(fileIntent, REQUESTCODE_EXPORT);
    }

    private void importSettings() {
        Intent fileIntent = new Intent();
        fileIntent.setAction(Intent.ACTION_GET_CONTENT);
        fileIntent.setType("*/*");
        startActivityForResult(fileIntent, REQUESTCODE_IMPORT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null) return;

        if (resultCode == RESULT_OK) {
            SharedPreferences prefs = Iconify.getAppContext().getSharedPreferences(Iconify.getAppContext().getPackageName(), Context.MODE_PRIVATE);
            switch (requestCode) {
                case REQUESTCODE_IMPORT:
                    AlertDialog alertDialog = new AlertDialog.Builder(Settings.this).create();
                    alertDialog.setTitle(getResources().getString(R.string.confirmation));
                    alertDialog.setMessage(getResources().getString(R.string.you_will_loose_current_setup));
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.positive), (dialog, which) -> {
                        dialog.dismiss();
                        try {
                            Prefs.importPrefs(getContentResolver().openInputStream(data.getData()));
                            Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.imported_settings), Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    });
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getResources().getString(R.string.negative), (dialog, which) -> dialog.dismiss());
                    alertDialog.show();
                    break;
                case REQUESTCODE_EXPORT:
                    try {
                        Prefs.exportPrefs(prefs, getContentResolver().openOutputStream(data.getData()));
                        Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.exported_settings), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    @Override
    public void onDestroy() {
        loadingDialog.hide();
        super.onDestroy();
    }
}