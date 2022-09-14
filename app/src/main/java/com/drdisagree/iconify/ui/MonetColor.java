package com.drdisagree.iconify.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.PrefConfig;
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

        // Apply Accent or Gradient
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch apply_monet_accent = findViewById(R.id.apply_monet_accent);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch apply_monet_gradient = findViewById(R.id.apply_monet_gradient);

        // Apply Accent
        apply_monet_accent.setChecked(PrefConfig.loadPrefBool(getApplicationContext(), "IconifyComponentAMA.overlay"));

        apply_monet_accent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    apply_monet_gradient.setChecked(false);
                    OverlayUtils.disableOverlay("IconifyComponentAMG.overlay");
                    PrefConfig.savePrefBool(getApplicationContext(), "IconifyComponentAMG.overlay", false);
                    OverlayUtils.enableOverlay("IconifyComponentAMA.overlay");
                    PrefConfig.savePrefBool(getApplicationContext(), "IconifyComponentAMA.overlay", true);
                } else {
                    OverlayUtils.disableOverlay("IconifyComponentAMA.overlay");
                    PrefConfig.savePrefBool(getApplicationContext(), "IconifyComponentAMA.overlay", false);
                }
            }
        });

        // Apply Gradient
        apply_monet_gradient.setChecked(PrefConfig.loadPrefBool(getApplicationContext(), "IconifyComponentAMG.overlay"));

        apply_monet_gradient.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    apply_monet_accent.setChecked(false);
                    OverlayUtils.disableOverlay("IconifyComponentAMA.overlay");
                    PrefConfig.savePrefBool(getApplicationContext(), "IconifyComponentAMA.overlay", false);
                    OverlayUtils.enableOverlay("IconifyComponentAMG.overlay");
                    PrefConfig.savePrefBool(getApplicationContext(), "IconifyComponentAMG.overlay", true);
                } else {
                    OverlayUtils.disableOverlay("IconifyComponentAMG.overlay");
                    PrefConfig.savePrefBool(getApplicationContext(), "IconifyComponentAMG.overlay", false);
                }
            }
        });

        // Color Picker
        TextView color_picker_desc = findViewById(R.id.color_picker_desc);
        if (PrefConfig.loadPrefBool(this, "IconifyComponentAMA.overlay"))
            color_picker_desc.setText("Pick your desired accent color.");
        else if (PrefConfig.loadPrefBool(this, "IconifyComponentAMG.overlay"))
            color_picker_desc.setText("Pick your desired gradient colors.");

        LinearLayout custom_color_picker = findViewById(R.id.custom_color_picker);
        custom_color_picker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MonetColor.this, ColorPicker.class);
                startActivity(intent);
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