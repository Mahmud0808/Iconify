package com.drdisagree.iconify.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.PrefConfig;
import com.drdisagree.iconify.services.ApplyOnBoot;
import com.drdisagree.iconify.utils.FabricatedOverlay;
import com.drdisagree.iconify.utils.OverlayUtils;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.topjohnwu.superuser.Shell;

import java.util.List;
import java.util.Objects;

public class MonetColor extends AppCompatActivity {

    List<String> accurate_sh = Shell.cmd("settings get secure monet_engine_accurate_shades").exec().getOut();
    int shade = initialize_shade();

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.monet_color);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle("Color Engine");
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Link to monet
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch apply_monet_color = findViewById(R.id.apply_monet_color);

        apply_monet_color.setChecked(PrefConfig.loadPrefBool(Iconify.getAppContext(), "IconifyComponentAMC.overlay"));

        apply_monet_color.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                apply_monet_color.setChecked(true);
                Runnable runnable = () -> {
                    OverlayUtils.enableOverlay("IconifyComponentAMC.overlay");
                    PrefConfig.savePrefBool(Iconify.getAppContext(), "IconifyComponentAMC.overlay", true);

                    PrefConfig.savePrefBool(Iconify.getAppContext(), "customColor", false);
                    PrefConfig.savePrefSettings(Iconify.getAppContext(), "colorAccentPrimary", "null");
                    PrefConfig.savePrefSettings(Iconify.getAppContext(), "colorAccentSecondary", "null");
                    FabricatedOverlay.disableOverlay("colorAccentPrimary");
                    FabricatedOverlay.disableOverlay("colorAccentSecondary");
                    findViewById(R.id.page_monet_color).invalidate();
                };
                Thread thread = new Thread(runnable);
                thread.start();
            } else {
                Runnable runnable = () -> {
                    OverlayUtils.disableOverlay("IconifyComponentAMC.overlay");
                    PrefConfig.savePrefBool(Iconify.getAppContext(), "IconifyComponentAMC.overlay", false);

                    PrefConfig.savePrefBool(Iconify.getAppContext(), "customColor", true);
                    PrefConfig.savePrefSettings(Iconify.getAppContext(), "colorAccentPrimary", "null");
                    PrefConfig.savePrefSettings(Iconify.getAppContext(), "colorAccentSecondary", "null");
                    FabricatedOverlay.buildOverlay("android", "colorAccentPrimary", "color", "holo_blue_light", "0xFF50A6D7");
                    FabricatedOverlay.buildOverlay("android", "colorAccentSecondary", "color", "holo_green_light", "0xFF387BFF");
                    FabricatedOverlay.enableOverlay("colorAccentPrimary");
                    FabricatedOverlay.enableOverlay("colorAccentSecondary");
                    ApplyOnBoot.applyColors();
                    findViewById(R.id.page_monet_color).invalidate();
                };
                Thread thread = new Thread(runnable);
                thread.start();
            }
        });

        // Color Picker
        LinearLayout custom_color_picker = findViewById(R.id.custom_color_picker);
        custom_color_picker.setOnClickListener(v -> {
            Intent intent = new Intent(MonetColor.this, ColorPicker.class);
            startActivity(intent);
        });

        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch apply_minimal_qspanel = findViewById(R.id.apply_minimal_qspanel);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch apply_pitch_black_theme = findViewById(R.id.apply_pitch_black_theme);

        // Minimal QsPanel
        apply_minimal_qspanel.setChecked(PrefConfig.loadPrefBool(Iconify.getAppContext(), "IconifyComponentQSST.overlay"));

        apply_minimal_qspanel.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                apply_pitch_black_theme.setChecked(false);
                OverlayUtils.disableOverlay("IconifyComponentQSPB.overlay");
                PrefConfig.savePrefBool(Iconify.getAppContext(), "IconifyComponentQSPB.overlay", false);

                apply_minimal_qspanel.postDelayed(() -> {
                    OverlayUtils.enableOverlay("IconifyComponentQSST.overlay");
                    PrefConfig.savePrefBool(Iconify.getAppContext(), "IconifyComponentQSST.overlay", true);
                }, 1000);
            } else {
                OverlayUtils.disableOverlay("IconifyComponentQSST.overlay");
                PrefConfig.savePrefBool(Iconify.getAppContext(), "IconifyComponentQSST.overlay", false);
            }
        });

        // Pitch Black QsPanel
        apply_pitch_black_theme.setChecked(PrefConfig.loadPrefBool(Iconify.getAppContext(), "IconifyComponentQSPB.overlay"));

        apply_pitch_black_theme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                apply_minimal_qspanel.setChecked(false);
                OverlayUtils.disableOverlay("IconifyComponentQSST.overlay");
                PrefConfig.savePrefBool(Iconify.getAppContext(), "IconifyComponentQSST.overlay", false);

                apply_pitch_black_theme.postDelayed(() -> {
                    OverlayUtils.enableOverlay("IconifyComponentQSPB.overlay");
                    PrefConfig.savePrefBool(Iconify.getAppContext(), "IconifyComponentQSPB.overlay", true);
                }, 1000);
            } else {
                OverlayUtils.disableOverlay("IconifyComponentQSPB.overlay");
                PrefConfig.savePrefBool(Iconify.getAppContext(), "IconifyComponentQSPB.overlay", false);
            }
        });

        // Experimental Options
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch experimental_color = findViewById(R.id.experimental_color);
        LinearLayout experimental_color_options = findViewById(R.id.experimental_color_options);

        if (!PrefConfig.loadPrefBool(this, "experimentalColorOptions")) {
            experimental_color.setChecked(false);
            experimental_color_options.setVisibility(View.GONE);
        } else {
            experimental_color.setChecked(true);
            experimental_color_options.setVisibility(View.VISIBLE);
        }

        experimental_color.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                experimental_color_options.setVisibility(View.VISIBLE);
                PrefConfig.savePrefBool(MonetColor.this, "experimentalColorOptions", true);
            } else {
                experimental_color_options.setVisibility(View.GONE);
                PrefConfig.savePrefBool(MonetColor.this, "experimentalColorOptions", false);
            }
        });

        // Accurate Shades
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enable_accurate_shades = findViewById(R.id.enable_accurate_shades);

        enable_accurate_shades.setChecked(shade != 0);

        enable_accurate_shades.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Shell.cmd("settings put secure monet_engine_accurate_shades 1").exec();
            } else {
                Shell.cmd("settings put secure monet_engine_accurate_shades 0").exec();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private int initialize_shade() {
        int shade = 1;
        try {
            shade = Integer.parseInt(accurate_sh.get(0));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return shade;
    }
}