package com.drdisagree.iconify.ui.activity;

import static com.drdisagree.iconify.common.References.FRAMEWORK_PACKAGE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.utils.FabricatedOverlayUtil;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.List;
import java.util.Objects;

public class ColorEngine extends AppCompatActivity {

    public static List<String> FabricatedEnabledOverlays = FabricatedOverlayUtil.getEnabledOverlayList();

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

        apply_monet_color.setChecked(Prefs.getBoolean("IconifyComponentAMC.overlay"));

        apply_monet_color.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                apply_monet_color.setChecked(true);

                OverlayUtil.enableOverlay("IconifyComponentAMC.overlay");

                if (!Objects.equals(Prefs.getString("colorAccentPrimary"), "null")) {
                    ColorPicker.applyPrimaryColors();
                } else {
                    FabricatedOverlayUtil.disableOverlay("colorAccentPrimary");
                    FabricatedOverlayUtil.disableOverlay("colorAccentPrimary1");
                    FabricatedOverlayUtil.disableOverlay("colorAccentPrimary2");
                    FabricatedOverlayUtil.disableOverlay("colorAccentPrimary3");
                    FabricatedOverlayUtil.disableOverlay("colorAccentPrimary4");
                    FabricatedOverlayUtil.disableOverlay("colorAccentPrimary5");
                    FabricatedOverlayUtil.disableOverlay("colorAccentPrimary6");
                }

                if (!Objects.equals(Prefs.getString("colorAccentSecondary"), "null")) {
                    ColorPicker.applySecondaryColors();
                } else {
                    FabricatedOverlayUtil.disableOverlay("colorAccentSecondary");
                    FabricatedOverlayUtil.disableOverlay("colorAccentSecondary1");
                    FabricatedOverlayUtil.disableOverlay("colorAccentSecondary2");
                    FabricatedOverlayUtil.disableOverlay("colorAccentSecondary3");
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

                    if (Prefs.getString("colorAccentPrimary").equals("null")) {
                        FabricatedOverlayUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, "colorAccentPrimary", "color", "holo_blue_light", "0xFF50A6D7");
                        FabricatedOverlayUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, "colorAccentPrimaryDark", "color", "holo_blue_dark", "0xFF122530");
                    }

                    if (Prefs.getString("colorAccentSecondary").equals("null")) {
                        FabricatedOverlayUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, "colorAccentSecondary", "color", "holo_green_light", "0xFF387BFF");
                    }

                    OverlayUtil.disableOverlay("IconifyComponentAMC.overlay");
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
        apply_minimal_qspanel.setChecked(Prefs.getBoolean("IconifyComponentQSST.overlay"));

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
        apply_pitch_black_theme.setChecked(Prefs.getBoolean("IconifyComponentQSPB.overlay"));

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
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}