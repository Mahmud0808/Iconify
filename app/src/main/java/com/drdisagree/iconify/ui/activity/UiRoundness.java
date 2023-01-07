package com.drdisagree.iconify.ui.activity;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
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
import com.drdisagree.iconify.ui.fragment.LoadingDialog;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.Objects;

public class UiRoundness extends AppCompatActivity {

    LoadingDialog loadingDialog;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ui_roundness);

        // Show loading dialog
        loadingDialog = new LoadingDialog(this);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_ui_roundness));
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
                corner_radius_output.setText(getResources().getString(R.string.opt_selected) + ' ' + (Integer.parseInt(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "cornerRadius")) + 8) + "dp " + getResources().getString(R.string.opt_default));
            } else {
                corner_radius_output.setText(getResources().getString(R.string.opt_selected) + ' ' + (Integer.parseInt(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "cornerRadius")) + 8) + "dp");
            }
            drawable1.setCornerRadius((Integer.parseInt(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "cornerRadius")) + 8) * getResources().getDisplayMetrics().density);
            drawable2.setCornerRadius((Integer.parseInt(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "cornerRadius")) + 8) * getResources().getDisplayMetrics().density);
            drawable3.setCornerRadius((Integer.parseInt(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "cornerRadius")) + 8) * getResources().getDisplayMetrics().density);
            drawable4.setCornerRadius((Integer.parseInt(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "cornerRadius")) + 8) * getResources().getDisplayMetrics().density);
            finalUiCornerRadius[0] = Integer.parseInt(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "cornerRadius"));
            corner_radius_seekbar.setProgress(finalUiCornerRadius[0]);
        } else {
            corner_radius_output.setText(getResources().getString(R.string.opt_selected) + " 24dp " + getResources().getString(R.string.opt_default));
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
                    corner_radius_output.setText(getResources().getString(R.string.opt_selected) + (progress + 8) + "dp " + getResources().getString(R.string.opt_default));
                else
                    corner_radius_output.setText(getResources().getString(R.string.opt_selected) + (progress + 8) + "dp");
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
        apply_radius.setOnClickListener(v -> {
            // Show loading dialog
            loadingDialog.show(getResources().getString(R.string.loading_dialog_wait));

            Runnable runnable = () -> {
                RadiusInstaller.install_pack(finalUiCornerRadius[0]);

                runOnUiThread(() -> {
                    PrefConfig.savePrefSettings(Iconify.getAppContext(), "cornerRadius", String.valueOf(finalUiCornerRadius[0]));

                    new Handler().postDelayed(() -> {
                        // Hide loading dialog
                        loadingDialog.hide();

                        Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                    }, 2000);
                });
            };
            Thread thread = new Thread(runnable);
            thread.start();
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

    @Override
    public void onDestroy() {
        loadingDialog.hide();
        super.onDestroy();
    }
}