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

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.PrefConfig;
import com.drdisagree.iconify.services.ApplyOnBoot;
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

        // Apply Accent
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch apply_monet_color = findViewById(R.id.apply_monet_color);

        apply_monet_color.setChecked(PrefConfig.loadPrefBool(getApplicationContext(), "IconifyComponentAMC.overlay"));

        apply_monet_color.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    apply_monet_color.setChecked(true);
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            OverlayUtils.enableOverlay("IconifyComponentAMC.overlay");
                            PrefConfig.savePrefBool(getApplicationContext(), "IconifyComponentAMC.overlay", true);
                            ApplyOnBoot.applyColors();
                        }
                    };
                    Thread thread = new Thread(runnable);
                    thread.start();
                } else {
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            OverlayUtils.disableOverlay("IconifyComponentAMC.overlay");
                            PrefConfig.savePrefBool(getApplicationContext(), "IconifyComponentAMC.overlay", false);
                            OverlayUtils.disableOverlay("com.android.shell:colorAccentPrimary");
                            PrefConfig.savePrefBool(getApplicationContext(), "fabricatecolorAccentPrimary", false);
                            PrefConfig.savePrefSettings(getApplicationContext(), "colorAccentPrimary", "null");
                            OverlayUtils.disableOverlay("com.android.shell:colorAccentSecondary");
                            PrefConfig.savePrefBool(getApplicationContext(), "fabricatecolorAccentSecondary", false);
                            PrefConfig.savePrefSettings(getApplicationContext(), "colorAccentSecondary", "null");
                        }
                    };
                    Thread thread = new Thread(runnable);
                    thread.start();
                }
            }
        });

        // Color Picker
        LinearLayout custom_color_picker = findViewById(R.id.custom_color_picker);
        if (PrefConfig.loadPrefBool(getApplicationContext(), "IconifyComponentAMC.overlay"))
            custom_color_picker.setVisibility(View.GONE);
        else
            custom_color_picker.setVisibility(View.VISIBLE);
        custom_color_picker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MonetColor.this, ColorPicker.class);
                startActivity(intent);
            }
        });

        // Minimal QsPanel
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch apply_minimal_qspanel = findViewById(R.id.apply_minimal_qspanel);
        apply_minimal_qspanel.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    OverlayUtils.enableOverlay("IconifyComponentQSST.overlay");
                } else {
                    OverlayUtils.disableOverlay("IconifyComponentQSST.overlay");
                }
            }
        });

        // Pitch Black QsPanel
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch apply_pitch_black_theme = findViewById(R.id.apply_pitch_black_theme);
        apply_pitch_black_theme.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    OverlayUtils.enableOverlay("IconifyComponentQSPB.overlay");
                } else {
                    OverlayUtils.disableOverlay("IconifyComponentQSPB.overlay");
                }
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

        experimental_color.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    experimental_color_options.setVisibility(View.VISIBLE);
                    PrefConfig.savePrefBool(MonetColor.this, "experimentalColorOptions", true);
                } else {
                    experimental_color_options.setVisibility(View.GONE);
                    PrefConfig.savePrefBool(MonetColor.this, "experimentalColorOptions", false);
                }
            }
        });

        // Accurate Shades
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enable_accurate_shades = findViewById(R.id.enable_accurate_shades);

        enable_accurate_shades.setChecked(shade != 0);

        enable_accurate_shades.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Shell.cmd("settings put secure monet_engine_accurate_shades 1").exec();
                } else {
                    Shell.cmd("settings put secure monet_engine_accurate_shades 0").exec();
                }
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