package com.drdisagree.iconify.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.common.References;
import com.drdisagree.iconify.config.PrefConfig;
import com.drdisagree.iconify.services.ApplyOnBoot;
import com.drdisagree.iconify.utils.FabricatedOverlayUtil;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.topjohnwu.superuser.Shell;

import java.util.List;
import java.util.Objects;

public class ColorEngine extends AppCompatActivity {

    public static List<String> FabricatedEnabledOverlays = FabricatedOverlayUtil.getEnabledOverlayList();
    List<String> accurate_sh = Shell.cmd("settings get secure monet_engine_accurate_shades").exec().getOut();
    int shade = initialize_shade();

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_engine);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_color_engine));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Link to monet
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch apply_monet_color = findViewById(R.id.apply_monet_color);

        apply_monet_color.setChecked(PrefConfig.loadPrefBool("IconifyComponentAMC.overlay"));

        apply_monet_color.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                apply_monet_color.setChecked(true);

                OverlayUtil.enableOverlay("IconifyComponentAMC.overlay");

                if (!Objects.equals(PrefConfig.loadPrefSettings("colorAccentPrimary"), "null")) {
                    ColorPicker.applyPrimaryColors();
                }

                if (!Objects.equals(PrefConfig.loadPrefSettings("colorAccentSecondary"), "null")) {
                    ColorPicker.applySecondaryColors();
                }

                apply_monet_color.postDelayed(() -> {
                    findViewById(R.id.page_color_engine).invalidate();
                }, 1000);
            } else {
                Runnable runnable = () -> {
                    FabricatedOverlayUtil.disableOverlay("colorAccentPrimary1");
                    FabricatedOverlayUtil.disableOverlay("colorAccentPrimary2");
                    FabricatedOverlayUtil.disableOverlay("colorAccentPrimary3");
                    FabricatedOverlayUtil.disableOverlay("colorAccentPrimary4");
                    FabricatedOverlayUtil.disableOverlay("colorAccentPrimary5");
                    FabricatedOverlayUtil.disableOverlay("colorAccentPrimary6");
                    FabricatedOverlayUtil.disableOverlay("colorAccentSecondary1");
                    FabricatedOverlayUtil.disableOverlay("colorAccentSecondary2");
                    FabricatedOverlayUtil.disableOverlay("colorAccentSecondary3");

                    if (FabricatedOverlayUtil.isOverlayDisabled(FabricatedEnabledOverlays, "colorAccentPrimary")) {
                        FabricatedOverlayUtil.buildAndEnableOverlay("android", "colorAccentPrimary", "color", "holo_blue_light", "0xFF50A6D7");
                        FabricatedOverlayUtil.buildAndEnableOverlay("android", "colorAccentPrimaryDark", "color", "holo_blue_dark", "0xFF122530");
                    }

                    if (FabricatedOverlayUtil.isOverlayDisabled(FabricatedEnabledOverlays, "colorAccentSecondary")) {
                        FabricatedOverlayUtil.buildAndEnableOverlay("android", "colorAccentSecondary", "color", "holo_green_light", "0xFF387BFF");
                    }

                    OverlayUtil.disableOverlay("IconifyComponentAMC.overlay");
                    ApplyOnBoot.applyColors();
                };
                Thread thread = new Thread(runnable);
                thread.start();

                apply_monet_color.postDelayed(() -> {
                    findViewById(R.id.page_color_engine).invalidate();
                }, 1000);
            }
        });

        // Color Picker
        LinearLayout custom_color_picker = findViewById(R.id.custom_color_picker);
        custom_color_picker.setOnClickListener(v -> {
            Intent intent = new Intent(ColorEngine.this, ColorPicker.class);
            startActivity(intent);
        });

        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch apply_minimal_qspanel = findViewById(R.id.apply_minimal_qspanel);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch apply_pitch_black_theme = findViewById(R.id.apply_pitch_black_theme);

        // Minimal QsPanel
        apply_minimal_qspanel.setChecked(PrefConfig.loadPrefBool("IconifyComponentQSST.overlay"));

        apply_minimal_qspanel.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                apply_pitch_black_theme.setChecked(false);
                OverlayUtil.disableOverlay("IconifyComponentQSPB.overlay");

                apply_minimal_qspanel.postDelayed(() -> {
                    OverlayUtil.enableOverlay("IconifyComponentQSST.overlay");
                }, 200);
            } else {
                OverlayUtil.disableOverlay("IconifyComponentQSST.overlay");
            }
        });

        // Pitch Black QsPanel
        apply_pitch_black_theme.setChecked(PrefConfig.loadPrefBool("IconifyComponentQSPB.overlay"));

        apply_pitch_black_theme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                apply_minimal_qspanel.setChecked(false);
                OverlayUtil.disableOverlay("IconifyComponentQSST.overlay");

                apply_pitch_black_theme.postDelayed(() -> {
                    OverlayUtil.enableOverlay("IconifyComponentQSPB.overlay");
                }, 200);
            } else {
                OverlayUtil.disableOverlay("IconifyComponentQSPB.overlay");
            }
        });

        // Experimental Options
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch experimental_color = findViewById(R.id.experimental_color);
        LinearLayout experimental_color_options = findViewById(R.id.experimental_color_options);

        if (!PrefConfig.loadPrefBool("experimentalColorOptions")) {
            experimental_color.setChecked(false);
            experimental_color_options.setVisibility(View.GONE);
        } else {
            experimental_color.setChecked(true);
            experimental_color_options.setVisibility(View.VISIBLE);
        }

        experimental_color.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                experimental_color_options.setVisibility(View.VISIBLE);
                PrefConfig.savePrefBool("experimentalColorOptions", true);
            } else {
                experimental_color_options.setVisibility(View.GONE);
                PrefConfig.savePrefBool("experimentalColorOptions", false);
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