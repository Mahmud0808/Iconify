package com.drdisagree.iconify.ui;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.graphics.drawable.GradientDrawable;
import android.graphics.text.LineBreaker;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.PrefConfig;
import com.drdisagree.iconify.installer.RadiusInstaller;
import com.drdisagree.iconify.utils.FabricatedOverlay;
import com.drdisagree.iconify.utils.OverlayUtils;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.topjohnwu.superuser.Shell;

import java.util.List;
import java.util.Objects;

public class Extras extends AppCompatActivity {

    public static void disableEverything() {
        List<String> overlays = OverlayUtils.getEnabledOverlayList();
        List<String> fabricatedOverlays = FabricatedOverlay.getEnabledOverlayList();

        for (String overlay : overlays) {
            OverlayUtils.disableOverlay(overlay);
            PrefConfig.savePrefBool(Iconify.getAppContext(), overlay, false);
        }

        for (String fabricatedOverlay : fabricatedOverlays) {
            FabricatedOverlay.disableOverlay(fabricatedOverlay);
            PrefConfig.savePrefBool(Iconify.getAppContext(), fabricatedOverlay, false);
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
        setContentView(R.layout.extras);

        // Spinner
        LinearLayout spinner = findViewById(R.id.progressBar_Extras);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle("Extras");
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Corner Radius

        LinearLayout qstile_preview1 = findViewById(R.id.qs_tile_preview1);
        LinearLayout qstile_preview2 = findViewById(R.id.qs_tile_preview2);
        LinearLayout qstile_preview3 = findViewById(R.id.qs_tile_preview3);
        LinearLayout qstile_preview4 = findViewById(R.id.qs_tile_preview4);

        GradientDrawable drawable1 = (GradientDrawable) qstile_preview1.getBackground();
        GradientDrawable drawable2 = (GradientDrawable) qstile_preview2.getBackground();
        GradientDrawable drawable3 = (GradientDrawable) qstile_preview3.getBackground();
        GradientDrawable drawable4 = (GradientDrawable) qstile_preview4.getBackground();

        SeekBar corner_radius_seekbar = findViewById(R.id.corner_radius_seekbar);
        TextView corner_radius_output = findViewById(R.id.corner_radius_output);

        corner_radius_seekbar.setPadding(0, 0, 0, 0);
        final int[] finalUiCornerRadius = {16};
        if (!PrefConfig.loadPrefSettings(Iconify.getAppContext(), "cornerRadius").equals("null"))
            finalUiCornerRadius[0] = Integer.parseInt(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "cornerRadius"));

        if (!PrefConfig.loadPrefSettings(Iconify.getAppContext(), "cornerRadius").equals("null")) {
            if ((Integer.parseInt(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "cornerRadius")) + 8) == 24) {
                corner_radius_output.setText("Selected: " + (Integer.parseInt(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "cornerRadius")) + 8) + "dp (Default)");
            } else {
                corner_radius_output.setText("Selected: " + (Integer.parseInt(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "cornerRadius")) + 8) + "dp");
            }
            drawable1.setCornerRadius((Integer.parseInt(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "cornerRadius"))  + 8) * getResources().getDisplayMetrics().density);
            drawable2.setCornerRadius((Integer.parseInt(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "cornerRadius"))  + 8) * getResources().getDisplayMetrics().density);
            drawable3.setCornerRadius((Integer.parseInt(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "cornerRadius"))  + 8) * getResources().getDisplayMetrics().density);
            drawable4.setCornerRadius((Integer.parseInt(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "cornerRadius"))  + 8) * getResources().getDisplayMetrics().density);
            finalUiCornerRadius[0] = Integer.parseInt(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "cornerRadius"));
            corner_radius_seekbar.setProgress(finalUiCornerRadius[0]);
        } else {
            drawable1.setCornerRadius(24 * getResources().getDisplayMetrics().density);
            drawable2.setCornerRadius(24 * getResources().getDisplayMetrics().density);
            drawable3.setCornerRadius(24 * getResources().getDisplayMetrics().density);
            drawable4.setCornerRadius(24 * getResources().getDisplayMetrics().density);
        }

        corner_radius_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                ;
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                finalUiCornerRadius[0] = progress;
                if (progress + 8 == 24)
                    corner_radius_output.setText("Selected: " + (progress + 8) + "dp (Default)");
                else
                    corner_radius_output.setText("Selected: " + (progress + 8) + "dp");
                drawable1.setCornerRadius((finalUiCornerRadius[0] + 8) * getResources().getDisplayMetrics().density);
                drawable2.setCornerRadius((finalUiCornerRadius[0] + 8) * getResources().getDisplayMetrics().density);
                drawable3.setCornerRadius((finalUiCornerRadius[0] + 8) * getResources().getDisplayMetrics().density);
                drawable4.setCornerRadius((finalUiCornerRadius[0] + 8) * getResources().getDisplayMetrics().density);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                ;
            }
        });

        Button apply_radius = findViewById(R.id.apply_radius);
        apply_radius.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show spinner
                spinner.setVisibility(View.VISIBLE);
                // Block touch
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                spinner.postDelayed(new Runnable() {
                    public void run() {
                        RadiusInstaller.install_pack(finalUiCornerRadius[0]);
                        PrefConfig.savePrefSettings(Iconify.getAppContext(), "cornerRadius", String.valueOf(finalUiCornerRadius[0]));
                        // Hide spinner
                        spinner.setVisibility(View.GONE);
                        // Unblock touch
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    }
                }, 1000);
            }
        });

        // Disable Everything
        TextView list_title_disableEverything = findViewById(R.id.list_title_disableEverything);
        TextView list_desc_disableEverything = findViewById(R.id.list_desc_disableEverything);
        Button button_disableEverything = findViewById(R.id.button_disableEverything);

        list_title_disableEverything.setText("Disable Everything");
        list_desc_disableEverything.setText("Disable all the applied UI, colors and miscellaneous changes done by Iconify.");
        list_desc_disableEverything.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);

        button_disableEverything.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show spinner
                spinner.setVisibility(View.VISIBLE);
                // Block touch
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                spinner.postDelayed(new Runnable() {
                    public void run() {
                        disableEverything();
                        // Hide spinner
                        spinner.setVisibility(View.GONE);
                        // Unblock touch
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    }
                }, 1000);
            }
        });

        // Restart SystemUI
        TextView list_title_restartSysui = findViewById(R.id.list_title_restartSysui);
        TextView list_desc_restartSysui = findViewById(R.id.list_desc_restartSysui);
        Button button_restartSysui = findViewById(R.id.button_restartSysui);

        list_title_restartSysui.setText("Restart SystemUI");
        list_desc_restartSysui.setText("Sometimes some of the options might get applied but not visible until a SystemUI reboot. In that case you can use this option to restart SystemUI.");
        list_desc_restartSysui.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);

        button_restartSysui.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show spinner
                spinner.setVisibility(View.VISIBLE);
                // Block touch
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                spinner.postDelayed(new Runnable() {
                    public void run() {
                        // Hide spinner
                        spinner.setVisibility(View.GONE);
                        // Unblock touch
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        // Restarting sysui
                        Shell.cmd("killall com.android.systemui").exec();
                    }
                }, 1000);
            }
        });

        // Change orientation in landscape / portrait mode
        int orientation = this.getResources().getConfiguration().orientation;
        LinearLayout qs_tile_preview_orientation = findViewById(R.id.qs_tile_preview_orientation);
        if (orientation == Configuration.ORIENTATION_LANDSCAPE)
            qs_tile_preview_orientation.setOrientation(LinearLayout.HORIZONTAL);
        else
            qs_tile_preview_orientation.setOrientation(LinearLayout.VERTICAL);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Change orientation in landscape / portrait mode
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LinearLayout qs_tile_preview_orientation = findViewById(R.id.qs_tile_preview_orientation);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
            qs_tile_preview_orientation.setOrientation(LinearLayout.HORIZONTAL);
        else
            qs_tile_preview_orientation.setOrientation(LinearLayout.VERTICAL);
    }
}