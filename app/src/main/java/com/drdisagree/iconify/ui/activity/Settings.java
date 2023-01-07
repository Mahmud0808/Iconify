package com.drdisagree.iconify.ui.activity;

import android.annotation.SuppressLint;
import android.graphics.text.LineBreaker;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.PrefConfig;
import com.drdisagree.iconify.ui.fragment.LoadingDialog;
import com.drdisagree.iconify.utils.FabricatedOverlay;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.topjohnwu.superuser.Shell;

import java.util.List;
import java.util.Objects;

public class Settings extends AppCompatActivity {

    LoadingDialog loadingDialog;

    public static void disableEverything() {
        List<String> overlays = OverlayUtil.getEnabledOverlayList();
        List<String> fabricatedOverlays = FabricatedOverlay.getEnabledOverlayList();

        for (String overlay : overlays) {
            OverlayUtil.disableOverlay(overlay);
            PrefConfig.clearPref(Iconify.getAppContext(), overlay);
            PrefConfig.clearPref(Iconify.getAppContext(), "cornerRadius");
            PrefConfig.clearPref(Iconify.getAppContext(), "qsTextSize");
            PrefConfig.clearPref(Iconify.getAppContext(), "qsIconSize");
            PrefConfig.clearPref(Iconify.getAppContext(), "qsMoveIcon");
        }

        for (String fabricatedOverlay : fabricatedOverlays) {
            FabricatedOverlay.disableOverlay(fabricatedOverlay);
            PrefConfig.clearPref(Iconify.getAppContext(), fabricatedOverlay);
            PrefConfig.clearPref(Iconify.getAppContext(), "fabricatedqsRowColumn");
            PrefConfig.clearPref(Iconify.getAppContext(), "customColor");
            PrefConfig.clearPref(Iconify.getAppContext(), "colorAccentPrimary");
            PrefConfig.clearPref(Iconify.getAppContext(), "colorAccentSecondary");
            PrefConfig.clearPref(Iconify.getAppContext(), "fabricatedqsTextSize");
            PrefConfig.clearPref(Iconify.getAppContext(), "fabricatedqsIconSize");
            PrefConfig.clearPref(Iconify.getAppContext(), "fabricatedqsMoveIcon");
        }

        PrefConfig.savePrefSettings(Iconify.getAppContext(), "colorAccentPrimary", "null");
        PrefConfig.savePrefSettings(Iconify.getAppContext(), "colorAccentSecondary", "null");
        PrefConfig.savePrefSettings(Iconify.getAppContext(), "dialogCornerRadius", "null");
        PrefConfig.savePrefSettings(Iconify.getAppContext(), "insetCornerRadius2", "null");
        PrefConfig.savePrefSettings(Iconify.getAppContext(), "insetCornerRadius4", "null");
        PrefConfig.savePrefBool(Iconify.getAppContext(), "fabricatedcolorAccentPrimary", false);
        PrefConfig.savePrefBool(Iconify.getAppContext(), "fabricatedcolorAccentSecondary", false);
        PrefConfig.savePrefBool(Iconify.getAppContext(), "fabricatedcornerRadius", false);
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
    public void onDestroy() {
        loadingDialog.hide();
        super.onDestroy();
    }
}