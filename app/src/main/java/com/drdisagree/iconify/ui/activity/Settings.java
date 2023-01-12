package com.drdisagree.iconify.ui.activity;

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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.PrefConfig;
import com.drdisagree.iconify.ui.fragment.LoadingDialog;
import com.drdisagree.iconify.utils.FabricatedOverlayUtil;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.topjohnwu.superuser.Shell;

import java.util.List;
import java.util.Objects;

public class Settings extends AppCompatActivity {

    public static List<String> EnabledOverlays = OverlayUtil.getEnabledOverlayList();
    public static List<String> FabricatedEnabledOverlays = FabricatedOverlayUtil.getEnabledOverlayList();

    LoadingDialog loadingDialog;
    private static final int REQUESTCODE_IMPORT = 1;
    private static final int REQUESTCODE_EXPORT = 2;

    public static void disableEverything() {
        for (String overlay : EnabledOverlays) {
            OverlayUtil.disableOverlay(overlay);
            PrefConfig.clearPref(overlay);
            PrefConfig.clearPref("cornerRadius");
            PrefConfig.clearPref("qsTextSize");
            PrefConfig.clearPref("qsIconSize");
            PrefConfig.clearPref("qsMoveIcon");
        }

        for (String fabricatedOverlay : FabricatedEnabledOverlays) {
            FabricatedOverlayUtil.disableOverlay(fabricatedOverlay);
            PrefConfig.clearPref(fabricatedOverlay);
            PrefConfig.clearPref("fabricatedqsRowColumn");
            PrefConfig.clearPref("customColor");
            PrefConfig.clearPref("colorAccentPrimary");
            PrefConfig.clearPref("colorAccentSecondary");
            PrefConfig.clearPref("fabricatedqsTextSize");
            PrefConfig.clearPref("fabricatedqsIconSize");
            PrefConfig.clearPref("fabricatedqsMoveIcon");
        }

        PrefConfig.savePrefSettings("colorAccentPrimary", "null");
        PrefConfig.savePrefSettings("colorAccentSecondary", "null");
        PrefConfig.savePrefSettings("dialogCornerRadius", "null");
        PrefConfig.savePrefSettings("insetCornerRadius2", "null");
        PrefConfig.savePrefSettings("insetCornerRadius4", "null");
        PrefConfig.savePrefBool("fabricatedcolorAccentPrimary", false);
        PrefConfig.savePrefBool("fabricatedcolorAccentSecondary", false);
        PrefConfig.savePrefBool("fabricatedcornerRadius", false);
    }

    @SuppressLint("SetTextI18n")
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

        // Disable Everything
        TextView list_title_disableEverything = findViewById(R.id.list_title_disableEverything);
        TextView list_desc_disableEverything = findViewById(R.id.list_desc_disableEverything);
        Button button_disableEverything = findViewById(R.id.button_disableEverything);

        list_title_disableEverything.setText(getResources().getString(R.string.disable_everything_title));
        list_desc_disableEverything.setText(getResources().getString(R.string.disable_everything_desc));
        list_desc_disableEverything.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);

        button_disableEverything.setOnClickListener(v -> Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_disable_everything), Toast.LENGTH_SHORT).show());

        button_disableEverything.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
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
            }
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
                Shell.cmd("killall com.android.systemui").exec();
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

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemID = item.getItemId();

        if (itemID == android.R.id.home) {
            onBackPressed();
        } else if (itemID == R.id.menu_updates) {
            Toast.makeText(Iconify.getAppContext(), "Coming soon", Toast.LENGTH_SHORT).show();
        } else if (itemID == R.id.menu_changelog) {
            Toast.makeText(Iconify.getAppContext(), "Coming soon", Toast.LENGTH_SHORT).show();
        } else if (itemID == R.id.menu_exportPrefs) {
            exportSettings();
        } else if (itemID == R.id.menu_importPrefs) {
            importSettings();
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

        if (data == null)
            return;

        if (resultCode == RESULT_OK) {
            SharedPreferences prefs = Iconify.getAppContext().getSharedPreferences(Iconify.getAppContext().getPackageName(), Context.MODE_PRIVATE);
            switch (requestCode) {
                case REQUESTCODE_IMPORT:
                    AlertDialog alertDialog = new AlertDialog.Builder(Settings.this).create();
                    alertDialog.setTitle("Are you sure?");
                    alertDialog.setMessage("You will loose your current setup and settings.");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                            (dialog, which) -> {
                                dialog.dismiss();
                                try {
                                    PrefConfig.importPrefs(prefs, getContentResolver().openInputStream(data.getData()));
                                    Toast.makeText(Iconify.getAppContext(), "Imported settings successfully", Toast.LENGTH_LONG).show();
                                } catch (Exception e) {
                                    Toast.makeText(Iconify.getAppContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                                }
                            });
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                            (dialog, which) -> dialog.dismiss());
                    alertDialog.show();
                    break;
                case REQUESTCODE_EXPORT:
                    try {
                        PrefConfig.exportPrefs(prefs, getContentResolver().openOutputStream(data.getData()));
                        Toast.makeText(Iconify.getAppContext(), "Saved settings successfully", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(Iconify.getAppContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
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