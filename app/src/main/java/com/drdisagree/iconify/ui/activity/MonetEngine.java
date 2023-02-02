package com.drdisagree.iconify.ui.activity;

import static com.drdisagree.iconify.utils.ColorSchemeUtil.GenerateColorPalette;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import java.util.List;
import java.util.Objects;

public class MonetEngine extends AppCompatActivity implements ColorPickerDialogListener {

    LinearLayout[] colorTableRows;
    private int[][] systemColors;
    RadioGroup radioGroup1, radioGroup2;
    private static String accentPrimary, accentSecondary, selectedStyle;
    private static boolean isSelectedPrimary = false, isSelectedSecondary = false;
    ColorPickerDialog.Builder colorPickerDialogPrimary, colorPickerDialogSecondary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monet_engine);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_monet_engine));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        colorTableRows = new LinearLayout[]{
                findViewById(R.id.color_table).findViewById(R.id.system_accent1),
                findViewById(R.id.color_table).findViewById(R.id.system_accent2),
                findViewById(R.id.color_table).findViewById(R.id.system_accent3),
                findViewById(R.id.color_table).findViewById(R.id.system_neutral1),
                findViewById(R.id.color_table).findViewById(R.id.system_neutral2)
        };

        getSystemColors();
        assignColorToPalette();

        selectedStyle = "Neutral";

        radioGroup1 = findViewById(R.id.monet_styles1);
        radioGroup2 = findViewById(R.id.monet_styles2);

        radioGroup1.clearCheck();
        radioGroup2.clearCheck();
        ((RadioButton) findViewById(R.id.neutral_style)).setChecked(true);

        radioGroup1.setOnCheckedChangeListener(listener1);
        radioGroup2.setOnCheckedChangeListener(listener2);

        if (!Objects.equals(Prefs.getString("colorAccentPrimary"), "null"))
            accentPrimary = Prefs.getString("colorAccentPrimary");
        else if (!Prefs.getBoolean("IconifyComponentAMAC.overlay") && !Prefs.getBoolean("IconifyComponentAMGC.overlay"))
            accentPrimary = String.valueOf(Color.parseColor("#FF50A6D7"));
        else
            accentPrimary = String.valueOf(getResources().getColor(android.R.color.system_accent1_200));

        if (!Objects.equals(Prefs.getString("colorAccentSecondary"), "null"))
            accentSecondary = Prefs.getString("colorAccentSecondary");
        else if (!Prefs.getBoolean("IconifyComponentAMAC.overlay") && !Prefs.getBoolean("IconifyComponentAMGC.overlay"))
            accentSecondary = String.valueOf(Color.parseColor("#FF387BFF"));
        else if (Prefs.getBoolean("IconifyComponentAMAC.overlay"))
            accentSecondary = String.valueOf(getResources().getColor(android.R.color.system_accent1_200));
        else
            accentSecondary = String.valueOf(getResources().getColor(android.R.color.system_accent3_200));

        colorPickerDialogPrimary = ColorPickerDialog.newBuilder();
        colorPickerDialogSecondary = ColorPickerDialog.newBuilder();

        colorPickerDialogPrimary.setDialogStyle(R.style.ColorPicker).setColor(Integer.parseInt(accentPrimary)).setDialogType(ColorPickerDialog.TYPE_CUSTOM).setAllowCustom(false).setAllowPresets(true).setDialogId(1).setShowAlphaSlider(false).setShowColorShades(true);
        colorPickerDialogSecondary.setDialogStyle(R.style.ColorPicker).setColor(Integer.parseInt(accentSecondary)).setDialogType(ColorPickerDialog.TYPE_CUSTOM).setAllowCustom(false).setAllowPresets(true).setDialogId(2).setShowAlphaSlider(false).setShowColorShades(true);

        LinearLayout preview_coloraccentprimary = findViewById(R.id.preview_coloraccentprimary);
        preview_coloraccentprimary.setOnClickListener(v -> colorPickerDialogPrimary.show(MonetEngine.this));

        LinearLayout preview_coloraccentsecondary = findViewById(R.id.preview_coloraccentsecondary);
        preview_coloraccentsecondary.setOnClickListener(v -> colorPickerDialogSecondary.show(MonetEngine.this));

        Button monet_content = findViewById(R.id.monet_content);
        monet_content.setOnClickListener(v -> assignCustomColorToPalette(GenerateColorPalette(selectedStyle, Integer.parseInt(accentPrimary))));
    }

    private final RadioGroup.OnCheckedChangeListener listener1 = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId != -1) {
                selectedStyle = ((RadioButton) findViewById(checkedId)).getText().toString();
                radioGroup2.setOnCheckedChangeListener(null);
                radioGroup2.clearCheck();
                radioGroup2.setOnCheckedChangeListener(listener2);
            }
        }
    };

    private final RadioGroup.OnCheckedChangeListener listener2 = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId != -1) {
                selectedStyle = ((RadioButton) findViewById(checkedId)).getText().toString();
                radioGroup1.setOnCheckedChangeListener(null);
                radioGroup1.clearCheck();
                radioGroup1.setOnCheckedChangeListener(listener1);
            }
        }
    };

    @Override
    public void onColorSelected(int dialogId, int color) {
        switch (dialogId) {
            case 1:
                isSelectedPrimary = true;
                accentPrimary = String.valueOf(color);
                updatePrimaryColor();
                colorPickerDialogPrimary.setDialogStyle(R.style.ColorPicker).setColor(Integer.parseInt(accentPrimary)).setDialogType(ColorPickerDialog.TYPE_CUSTOM).setAllowCustom(false).setAllowPresets(true).setDialogId(1).setShowAlphaSlider(false).setShowColorShades(true);
                break;
            case 2:
                isSelectedSecondary = true;
                accentSecondary = String.valueOf(color);
                updateSecondaryColor();
                colorPickerDialogSecondary.setDialogStyle(R.style.ColorPicker).setColor(Integer.parseInt(accentSecondary)).setDialogType(ColorPickerDialog.TYPE_CUSTOM).setAllowCustom(false).setAllowPresets(true).setDialogId(2).setShowAlphaSlider(false).setShowColorShades(true);
                break;
        }
    }

    @Override
    public void onDialogDismissed(int dialogId) {
    }

    private void updatePrimaryColor() {
        View preview_color_picker_primary = findViewById(R.id.preview_color_picker_primary);
        GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{Integer.parseInt(accentPrimary), Integer.parseInt(accentPrimary)});
        gd.setCornerRadius(24 * getResources().getDisplayMetrics().density);
        preview_color_picker_primary.setBackgroundDrawable(gd);
    }

    private void updateSecondaryColor() {
        View preview_color_picker_secondary = findViewById(R.id.preview_color_picker_secondary);
        GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{Integer.parseInt(accentSecondary), Integer.parseInt(accentSecondary)});
        gd.setCornerRadius(24 * getResources().getDisplayMetrics().density);
        preview_color_picker_secondary.setBackgroundDrawable(gd);
    }

    private void getSystemColors() {
        systemColors = new int[][]{
                new int[]{
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_primary100),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_primary99),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_primary95),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_primary90),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_primary80),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_primary70),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_primary60),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_primary50),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_primary40),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_primary30),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_primary20),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_primary10),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_primary0)
                },

                new int[]{
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary100),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary99),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary95),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary90),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary80),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary70),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary60),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary50),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary40),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary30),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary20),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary10),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary0)
                },

                new int[]{
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary100),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary99),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary95),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary90),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary80),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary70),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary60),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary50),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary40),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary30),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary20),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary10),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary0)
                },

                new int[]{
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral100),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral99),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral95),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral90),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral80),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral70),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral60),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral50),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral40),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral30),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral20),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral10),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral0)
                },

                new int[]{
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant100),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant99),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant95),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant90),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant80),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant70),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant60),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant50),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant40),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant30),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant20),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant10),
                        getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant0)
                }};
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void assignColorToPalette() {
        for (int i = 0; i < colorTableRows.length; i++) {
            for (int j = 0; j < colorTableRows[i].getChildCount(); j++) {
                GradientDrawable colorbg = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{systemColors[i][j], systemColors[i][j]});
                colorbg.setCornerRadius(8 * getResources().getDisplayMetrics().density);
                colorTableRows[i].getChildAt(j).setBackgroundDrawable(colorbg);
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void assignCustomColorToPalette(List<List<Object>> palette) {
        for (int i = 0; i < colorTableRows.length; i++) {
            for (int j = 0; j < colorTableRows[i].getChildCount(); j++) {
                GradientDrawable colorbg = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{(int) palette.get(i).get(j), (int) palette.get(i).get(j)});
                colorbg.setCornerRadius(8 * getResources().getDisplayMetrics().density);
                colorTableRows[i].getChildAt(j).setBackgroundDrawable(colorbg);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}