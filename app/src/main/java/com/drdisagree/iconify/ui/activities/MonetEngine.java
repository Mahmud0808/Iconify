package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Preferences.COLOR_ACCENT_PRIMARY;
import static com.drdisagree.iconify.common.Preferences.COLOR_ACCENT_SECONDARY;
import static com.drdisagree.iconify.common.Preferences.COLOR_PIXEL_DARK_BG;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_PRIMARY_COLOR_SWITCH;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_SECONDARY_COLOR_SWITCH;
import static com.drdisagree.iconify.common.Preferences.MONET_ACCENT_SATURATION;
import static com.drdisagree.iconify.common.Preferences.MONET_ACCURATE_SHADES;
import static com.drdisagree.iconify.common.Preferences.MONET_BACKGROUND_LIGHTNESS;
import static com.drdisagree.iconify.common.Preferences.MONET_BACKGROUND_SATURATION;
import static com.drdisagree.iconify.common.Preferences.MONET_ENGINE_SWITCH;
import static com.drdisagree.iconify.common.Preferences.MONET_STYLE;
import static com.drdisagree.iconify.common.Preferences.STR_NULL;
import static com.drdisagree.iconify.common.Preferences.USE_LIGHT_ACCENT;
import static com.drdisagree.iconify.utils.ColorSchemeUtil.GenerateColorPalette;

import android.annotation.SuppressLint;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.overlaymanager.MonetEngineManager;
import com.drdisagree.iconify.utils.ColorUtil;
import com.drdisagree.iconify.utils.FabricatedUtil;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class MonetEngine extends AppCompatActivity implements ColorPickerDialogListener {

    private static String accentPrimary, accentSecondary, selectedStyle;
    private static boolean isSelectedPrimary = false, isSelectedSecondary = false, accurateShades = Prefs.getBoolean(MONET_ACCURATE_SHADES, true);
    int[] monetAccentSaturation = new int[]{Prefs.getInt(MONET_ACCENT_SATURATION, 100)};
    int[] monetBackgroundSaturation = new int[]{Prefs.getInt(MONET_BACKGROUND_SATURATION, 100)};
    int[] monetBackgroundLightness = new int[]{Prefs.getInt(MONET_BACKGROUND_LIGHTNESS, 100)};
    private LinearLayout[] colorTableRows;
    private int[][] systemColors;
    private RadioGroup radioGroup1, radioGroup2;
    private Button enable_custom_monet, disable_custom_monet;
    private ColorPickerDialog.Builder colorPickerDialogPrimary, colorPickerDialogSecondary;
    private List<List<Object>> generatedColorPalette = new ArrayList<>();
    private List<List<Object>> generatedColorPaletteNight = new ArrayList<>();
    private boolean isDarkMode = SystemUtil.isDarkMode();

    @SuppressLint("SetTextI18n")
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

        // Enable/Disable monet button
        enable_custom_monet = findViewById(R.id.enable_custom_monet);
        disable_custom_monet = findViewById(R.id.disable_custom_monet);

        colorTableRows = new LinearLayout[]{findViewById(R.id.monet_engine).findViewById(R.id.system_accent1), findViewById(R.id.monet_engine).findViewById(R.id.system_accent2), findViewById(R.id.monet_engine).findViewById(R.id.system_accent3), findViewById(R.id.monet_engine).findViewById(R.id.system_neutral1), findViewById(R.id.monet_engine).findViewById(R.id.system_neutral2)};
        systemColors = ColorUtil.getSystemColors();

        for (int[] row : systemColors) {
            List<Object> temp = new ArrayList<>();
            for (int col : row) {
                temp.add(col);
            }
            generatedColorPalette.add(temp);
            generatedColorPaletteNight.add(temp);
        }

        isDarkMode = SystemUtil.isDarkMode();
        selectedStyle = Prefs.getString(MONET_STYLE, getResources().getString(R.string.monet_neutral));

        if (Objects.equals(selectedStyle, getResources().getString(R.string.monet_neutral)))
            ((RadioButton) findViewById(R.id.neutral_style)).setChecked(true);
        else if (Objects.equals(selectedStyle, getResources().getString(R.string.monet_monochrome)))
            ((RadioButton) findViewById(R.id.monochrome_style)).setChecked(true);
        else if (Objects.equals(selectedStyle, getResources().getString(R.string.monet_tonalspot)))
            ((RadioButton) findViewById(R.id.tonalspot_style)).setChecked(true);
        else if (Objects.equals(selectedStyle, getResources().getString(R.string.monet_vibrant)))
            ((RadioButton) findViewById(R.id.vibrant_style)).setChecked(true);
        else if (Objects.equals(selectedStyle, getResources().getString(R.string.monet_expressive)))
            ((RadioButton) findViewById(R.id.expressive_style)).setChecked(true);
        else if (Objects.equals(selectedStyle, getResources().getString(R.string.monet_fidelity)))
            ((RadioButton) findViewById(R.id.fidelity_style)).setChecked(true);
        else if (Objects.equals(selectedStyle, getResources().getString(R.string.monet_content)))
            ((RadioButton) findViewById(R.id.content_style)).setChecked(true);
        else {
            Prefs.putBoolean(MONET_ENGINE_SWITCH, false);
            radioGroup1.clearCheck();
            radioGroup2.clearCheck();
        }

        if (!Objects.equals(Prefs.getString(COLOR_ACCENT_PRIMARY), STR_NULL))
            accentPrimary = Prefs.getString(COLOR_ACCENT_PRIMARY);
        else
            accentPrimary = String.valueOf(getResources().getColor(isDarkMode ? android.R.color.system_accent1_300 : (Prefs.getBoolean(USE_LIGHT_ACCENT, false) ? android.R.color.system_accent1_300 : android.R.color.system_accent1_600)));

        if (!Objects.equals(Prefs.getString(COLOR_ACCENT_SECONDARY), STR_NULL))
            accentSecondary = Prefs.getString(COLOR_ACCENT_SECONDARY);
        else
            accentSecondary = String.valueOf(getResources().getColor(isDarkMode ? android.R.color.system_accent3_300 : (Prefs.getBoolean(USE_LIGHT_ACCENT, false) ? android.R.color.system_accent3_300 : android.R.color.system_accent3_600)));

        updatePrimaryColor();
        updateSecondaryColor();

        radioGroup1 = findViewById(R.id.monet_styles1);
        radioGroup2 = findViewById(R.id.monet_styles2);

        radioGroup1.setOnCheckedChangeListener(listener1);
        radioGroup2.setOnCheckedChangeListener(listener2);

        if (Prefs.getBoolean(MONET_ENGINE_SWITCH, false) && !Objects.equals(selectedStyle, STR_NULL))
            assignCustomColorToPalette(GenerateColorPalette(selectedStyle, Integer.parseInt(accentPrimary)));
        else assignStockColorToPalette();

        colorPickerDialogPrimary = ColorPickerDialog.newBuilder();
        colorPickerDialogSecondary = ColorPickerDialog.newBuilder();

        colorPickerDialogPrimary.setDialogStyle(R.style.ColorPicker).setColor(Integer.parseInt(accentPrimary)).setDialogType(ColorPickerDialog.TYPE_CUSTOM).setAllowCustom(false).setAllowPresets(true).setDialogId(1).setShowAlphaSlider(false).setShowColorShades(true);
        colorPickerDialogSecondary.setDialogStyle(R.style.ColorPicker).setColor(Integer.parseInt(accentSecondary)).setDialogType(ColorPickerDialog.TYPE_CUSTOM).setAllowCustom(false).setAllowPresets(true).setDialogId(2).setShowAlphaSlider(false).setShowColorShades(true);

        LinearLayout preview_coloraccentprimary = findViewById(R.id.preview_coloraccentprimary);
        preview_coloraccentprimary.setOnClickListener(v -> colorPickerDialogPrimary.show(MonetEngine.this));

        LinearLayout preview_coloraccentsecondary = findViewById(R.id.preview_coloraccentsecondary);
        preview_coloraccentsecondary.setOnClickListener(v -> colorPickerDialogSecondary.show(MonetEngine.this));

        // Monet Accurate Shades
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch monet_accurate_shades = findViewById(R.id.monet_accurate_shades);
        monet_accurate_shades.setChecked(Prefs.getBoolean(MONET_ACCURATE_SHADES, true));
        monet_accurate_shades.setOnCheckedChangeListener((buttonView, isChecked) -> {
            accurateShades = isChecked;
            assignCustomColorToPalette(GenerateColorPalette(selectedStyle, Integer.parseInt(accentPrimary)));
            enable_custom_monet.setVisibility(View.VISIBLE);
        });

        // Monet accent saturation
        SeekBar monet_accent_saturation_seekbar = findViewById(R.id.monet_accent_saturation_seekbar);
        TextView monet_accent_saturation_output = findViewById(R.id.monet_accent_saturation_output);
        monet_accent_saturation_output.setText(getResources().getString(R.string.opt_selected) + ' ' + (Prefs.getInt(MONET_ACCENT_SATURATION, 100) - 100) + "%");
        monet_accent_saturation_seekbar.setProgress(Prefs.getInt(MONET_ACCENT_SATURATION, 100));

        // Long Click Reset
        ImageView reset_accent_saturation = findViewById(R.id.reset_accent_saturation);
        reset_accent_saturation.setVisibility(Prefs.getInt(MONET_ACCENT_SATURATION, 100) == 100 ? View.INVISIBLE : View.VISIBLE);

        reset_accent_saturation.setOnLongClickListener(v -> {
            monetAccentSaturation[0] = 100;
            monet_accent_saturation_seekbar.setProgress(100);
            assignCustomColorToPalette(GenerateColorPalette(selectedStyle, Integer.parseInt(accentPrimary)));
            reset_accent_saturation.setVisibility(View.INVISIBLE);
            enable_custom_monet.setVisibility(View.VISIBLE);
            return true;
        });

        monet_accent_saturation_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                monetAccentSaturation[0] = progress;
                if (progress == 100) reset_accent_saturation.setVisibility(View.INVISIBLE);
                monet_accent_saturation_output.setText(getResources().getString(R.string.opt_selected) + ' ' + (progress - 100) + "%");
                assignCustomColorToPalette(GenerateColorPalette(selectedStyle, Integer.parseInt(accentPrimary)));
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                enable_custom_monet.setVisibility(View.VISIBLE);
                reset_accent_saturation.setVisibility(monetAccentSaturation[0] == 100 ? View.INVISIBLE : View.VISIBLE);
            }
        });

        // Monet background saturation
        SeekBar monet_background_saturation_seekbar = findViewById(R.id.monet_background_saturation_seekbar);
        TextView monet_background_saturation_output = findViewById(R.id.monet_background_saturation_output);
        monet_background_saturation_output.setText(getResources().getString(R.string.opt_selected) + ' ' + (Prefs.getInt(MONET_BACKGROUND_SATURATION, 100) - 100) + "%");
        monet_background_saturation_seekbar.setProgress(Prefs.getInt(MONET_BACKGROUND_SATURATION, 100));

        // Reset button
        ImageView reset_background_saturation = findViewById(R.id.reset_background_saturation);
        reset_background_saturation.setVisibility(Prefs.getInt(MONET_BACKGROUND_SATURATION, 100) == 100 ? View.INVISIBLE : View.VISIBLE);

        reset_background_saturation.setOnLongClickListener(v -> {
            monetBackgroundSaturation[0] = 100;
            monet_background_saturation_seekbar.setProgress(100);
            assignCustomColorToPalette(GenerateColorPalette(selectedStyle, Integer.parseInt(accentPrimary)));
            reset_background_saturation.setVisibility(View.INVISIBLE);
            enable_custom_monet.setVisibility(View.VISIBLE);
            return true;
        });

        monet_background_saturation_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                monetBackgroundSaturation[0] = progress;
                if (progress == 100) reset_background_saturation.setVisibility(View.INVISIBLE);
                monet_background_saturation_output.setText(getResources().getString(R.string.opt_selected) + ' ' + (progress - 100) + "%");
                assignCustomColorToPalette(GenerateColorPalette(selectedStyle, Integer.parseInt(accentPrimary)));
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                enable_custom_monet.setVisibility(View.VISIBLE);
                reset_background_saturation.setVisibility(monetBackgroundSaturation[0] == 100 ? View.INVISIBLE : View.VISIBLE);
            }
        });

        // Monet background lightness
        SeekBar monet_background_lightness_seekbar = findViewById(R.id.monet_background_lightness_seekbar);
        TextView monet_background_lightness_output = findViewById(R.id.monet_background_lightness_output);
        monet_background_lightness_output.setText(getResources().getString(R.string.opt_selected) + ' ' + (Prefs.getInt(MONET_BACKGROUND_LIGHTNESS, 100) - 100) + "%");
        monet_background_lightness_seekbar.setProgress(Prefs.getInt(MONET_BACKGROUND_LIGHTNESS, 100));

        // Long Click Reset
        ImageView reset_background_lightness = findViewById(R.id.reset_background_lightness);
        reset_background_lightness.setVisibility(Prefs.getInt(MONET_BACKGROUND_LIGHTNESS, 100) == 100 ? View.INVISIBLE : View.VISIBLE);

        reset_background_lightness.setOnLongClickListener(v -> {
            monetBackgroundLightness[0] = 100;
            monet_background_lightness_seekbar.setProgress(100);
            assignCustomColorToPalette(GenerateColorPalette(selectedStyle, Integer.parseInt(accentPrimary)));
            reset_background_lightness.setVisibility(View.INVISIBLE);
            enable_custom_monet.setVisibility(View.VISIBLE);
            return true;
        });

        monet_background_lightness_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                monetBackgroundLightness[0] = progress;
                if (progress == 100) reset_background_lightness.setVisibility(View.INVISIBLE);
                monet_background_lightness_output.setText(getResources().getString(R.string.opt_selected) + ' ' + (progress - 100) + "%");
                assignCustomColorToPalette(GenerateColorPalette(selectedStyle, Integer.parseInt(accentPrimary)));
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                enable_custom_monet.setVisibility(View.VISIBLE);
                reset_background_lightness.setVisibility(monetBackgroundLightness[0] == 100 ? View.INVISIBLE : View.VISIBLE);
            }
        });

        // Enable custom colors button
        enable_custom_monet.setVisibility(View.GONE);
        enable_custom_monet.setOnClickListener(v -> {
            if (!Environment.isExternalStorageManager()) {
                SystemUtil.getStoragePermission(this);
            } else if (Objects.equals(selectedStyle, STR_NULL)) {
                Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_select_style), Toast.LENGTH_SHORT).show();
            } else {
                Prefs.putBoolean(MONET_ACCURATE_SHADES, accurateShades);
                Prefs.putInt(MONET_ACCENT_SATURATION, monetAccentSaturation[0]);
                Prefs.putInt(MONET_BACKGROUND_SATURATION, monetBackgroundSaturation[0]);
                Prefs.putInt(MONET_BACKGROUND_LIGHTNESS, monetBackgroundLightness[0]);

                if (isSelectedPrimary) Prefs.putString(COLOR_ACCENT_PRIMARY, accentPrimary);
                if (isSelectedSecondary) Prefs.putString(COLOR_ACCENT_SECONDARY, accentSecondary);

                disableBasicColors();

                AtomicBoolean hasErroredOut = new AtomicBoolean(false);

                Runnable runnable1 = () -> {
                    try {
                        if (MonetEngineManager.enableOverlay(generatedColorPalette, generatedColorPaletteNight))
                            hasErroredOut.set(true);
                        else Prefs.putString(MONET_STYLE, selectedStyle);
                    } catch (Exception e) {
                        hasErroredOut.set(true);
                        Log.e("MonetEngine", e.toString());
                    }

                    runOnUiThread(() -> {
                        if (!hasErroredOut.get()) {
                            Prefs.putBoolean(MONET_ENGINE_SWITCH, true);
                            if (Prefs.getBoolean("IconifyComponentQSPB.overlay")) {
                                OverlayUtil.disableOverlay("IconifyComponentQSPB.overlay");
                                OverlayUtil.enableOverlay("IconifyComponentQSPB.overlay");
                            }
                        }

                        new Handler().postDelayed(() -> {
                            if (!hasErroredOut.get()) {
                                Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                                enable_custom_monet.setVisibility(View.GONE);
                                disable_custom_monet.setVisibility(View.VISIBLE);
                            } else
                                Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                        }, 20);
                    });
                };
                Thread thread1 = new Thread(runnable1);
                thread1.start();
            }
        });

        // Disable custom colors button
        disable_custom_monet.setVisibility(Prefs.getBoolean(MONET_ENGINE_SWITCH) ? View.VISIBLE : View.GONE);
        disable_custom_monet.setOnClickListener(v -> {
            Runnable runnable2 = () -> {
                Prefs.putBoolean(MONET_ENGINE_SWITCH, false);
                Prefs.putString(COLOR_ACCENT_PRIMARY, STR_NULL);
                Prefs.putString(COLOR_ACCENT_SECONDARY, STR_NULL);
                OverlayUtil.disableOverlay("IconifyComponentDM.overlay");
                OverlayUtil.disableOverlay("IconifyComponentME.overlay");

                runOnUiThread(() -> {
                    new Handler().postDelayed(() -> {
                        Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_disabled), Toast.LENGTH_SHORT).show();
                        disable_custom_monet.setVisibility(View.GONE);
                        isSelectedPrimary = false;
                        isSelectedSecondary = false;
                    }, 2000);
                });
            };
            Thread thread2 = new Thread(runnable2);
            thread2.start();
        });
    }

    @Override
    public void onColorSelected(int dialogId, int color) {
        switch (dialogId) {
            case 1:
                isSelectedPrimary = true;
                accentPrimary = String.valueOf(color);
                updatePrimaryColor();
                enable_custom_monet.setVisibility(View.VISIBLE);
                assignCustomColorToPalette(GenerateColorPalette(selectedStyle, Integer.parseInt(accentPrimary)));
                colorPickerDialogPrimary.setDialogStyle(R.style.ColorPicker).setColor(Integer.parseInt(accentPrimary)).setDialogType(ColorPickerDialog.TYPE_CUSTOM).setAllowCustom(false).setAllowPresets(true).setDialogId(1).setShowAlphaSlider(false).setShowColorShades(true);
                break;
            case 2:
                isSelectedSecondary = true;
                accentSecondary = String.valueOf(color);
                updateSecondaryColor();
                enable_custom_monet.setVisibility(View.VISIBLE);
                assignCustomColorToPalette(GenerateColorPalette(selectedStyle, Integer.parseInt(accentPrimary)));
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

    @SuppressLint("UseCompatLoadingForDrawables")
    private void assignStockColorToPalette() {
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
        List<List<Object>> palette_night = cloneList(palette);

        // Set accent saturation
        if (!Objects.equals(selectedStyle, getResources().getString(R.string.monet_monochrome))) {
            for (int i = 0; i < palette.size() - 2; i++) {
                for (int j = palette.get(i).size() - 2; j >= 1; j--) {
                    int color;

                    if (j == 1)
                        color = ColorUtil.setSaturation(Integer.parseInt(String.valueOf((int) palette.get(i).get(j + 1))), -0.1F);
                    else
                        color = ColorUtil.setSaturation(Integer.parseInt(String.valueOf((int) palette.get(i).get(j))), ((float) (monetAccentSaturation[0] - 100) / 1000.0F) * (Math.min((3.0F - j / 5F), 3.0F)));

                    palette.get(i).set(j, color);
                    palette_night.get(i).set(j, color);

                    if (!accurateShades) {
                        if (i == 0 && j == (Prefs.getBoolean(USE_LIGHT_ACCENT, false) ? 5 : 8))
                            palette.get(i).set(j, Integer.parseInt(accentPrimary));

                        if (i == 0 && j == 5)
                            palette_night.get(i).set(j, Integer.parseInt(accentPrimary));
                    }
                }
            }
        }

        // Set background saturation
        if (!Objects.equals(selectedStyle, getResources().getString(R.string.monet_monochrome))) {
            for (int i = 3; i < palette.size(); i++) {
                for (int j = palette.get(i).size() - 2; j >= 1; j--) {
                    int color;
                    if (j == 1)
                        color = ColorUtil.setSaturation(Integer.parseInt(String.valueOf((int) palette.get(i).get(j + 1))), -0.1F);
                    else
                        color = ColorUtil.setSaturation(Integer.parseInt(String.valueOf((int) palette.get(i).get(j))), ((float) (monetBackgroundSaturation[0] - 100) / 1000.0F) * (Math.min((3.0F - j / 5F), 3.0F)));

                    palette.get(i).set(j, color);
                    palette_night.get(i).set(j, color);
                }
            }
        }

        // Set background lightness
        for (int i = Objects.equals(selectedStyle, getResources().getString(R.string.monet_monochrome)) ? 0 : 3; i < palette.size(); i++) {
            for (int j = 1; j < palette.get(i).size() - 1; j++) {
                int color = ColorUtil.setLightness(Integer.parseInt(String.valueOf((int) palette.get(i).get(j))), (float) (monetBackgroundLightness[0] - 100) / 1000.0F);

                palette.get(i).set(j, color);
                palette_night.get(i).set(j, color);
            }
        }

        for (int i = 0; i < colorTableRows.length; i++) {
            if (i == 2 && (Prefs.getBoolean(CUSTOM_SECONDARY_COLOR_SWITCH) || isSelectedSecondary) && !Objects.equals(selectedStyle, getResources().getString(R.string.monet_monochrome))) {
                Prefs.putBoolean(CUSTOM_SECONDARY_COLOR_SWITCH, true);
                List<List<Object>> secondaryPalette = GenerateColorPalette(selectedStyle, Integer.parseInt(accentSecondary));

                for (int j = colorTableRows[i].getChildCount() - 1; j >= 0; j--) {
                    int color;

                    if (j == 0 || j == colorTableRows[i].getChildCount() - 1)
                        color = (int) secondaryPalette.get(0).get(j);
                    else if (j == 1)
                        color = ColorUtil.setSaturation(Integer.parseInt(String.valueOf((int) palette.get(i).get(j + 1))), -0.1F);
                    else
                        color = ColorUtil.setSaturation(Integer.parseInt(String.valueOf((int) secondaryPalette.get(0).get(j))), ((float) (monetAccentSaturation[0] - 100) / 1000.0F) * (Math.min((3.0F - j / 5F), 3.0F)));

                    palette.get(i).set(j, color);
                    palette_night.get(i).set(j, color);

                    if (!accurateShades) {
                        if (j == (Prefs.getBoolean(USE_LIGHT_ACCENT, false) ? 5 : 8))
                            palette.get(i).set(j, Integer.parseInt(accentSecondary));

                        if (j == 5) palette_night.get(i).set(j, Integer.parseInt(accentSecondary));
                    }

                    GradientDrawable colorbg = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{!isDarkMode ? (int) palette.get(i).get(j) : (int) palette_night.get(i).get(j), !isDarkMode ? (int) palette.get(i).get(j) : (int) palette_night.get(i).get(j)});
                    colorbg.setCornerRadius(8 * getResources().getDisplayMetrics().density);
                    colorTableRows[i].getChildAt(j).setBackgroundDrawable(colorbg);
                }
            } else {
                for (int j = 0; j < colorTableRows[i].getChildCount(); j++) {
                    GradientDrawable colorbg = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{!isDarkMode ? (int) palette.get(i).get(j) : (int) palette_night.get(i).get(j), !isDarkMode ? (int) palette.get(i).get(j) : (int) palette_night.get(i).get(j)});
                    colorbg.setCornerRadius(8 * getResources().getDisplayMetrics().density);
                    colorTableRows[i].getChildAt(j).setBackgroundDrawable(colorbg);
                }
            }
        }

        generatedColorPalette = palette;
        generatedColorPaletteNight = palette_night;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void disableBasicColors() {
        Prefs.clearPref("customMonetColor");
        Prefs.clearPref(CUSTOM_PRIMARY_COLOR_SWITCH);
        Prefs.clearPref(CUSTOM_SECONDARY_COLOR_SWITCH);

        FabricatedUtil.disableOverlay(COLOR_ACCENT_PRIMARY);
        FabricatedUtil.disableOverlay(COLOR_PIXEL_DARK_BG);
        FabricatedUtil.disableOverlay(COLOR_ACCENT_SECONDARY);
    }

    private List<List<Object>> cloneList(final List<List<Object>> src) {
        List<List<Object>> cloned = new ArrayList<>();
        for (List<Object> sublist : src) {
            cloned.add(new ArrayList<>(sublist));
        }
        return cloned;
    }

    private final RadioGroup.OnCheckedChangeListener listener1 = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId != -1) {
                selectedStyle = ((RadioButton) findViewById(checkedId)).getText().toString();
                radioGroup2.setOnCheckedChangeListener(null);
                radioGroup2.clearCheck();
                radioGroup2.setOnCheckedChangeListener(listener2);
                assignCustomColorToPalette(GenerateColorPalette(selectedStyle, Integer.parseInt(accentPrimary)));
                enable_custom_monet.setVisibility(View.VISIBLE);
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
                assignCustomColorToPalette(GenerateColorPalette(selectedStyle, Integer.parseInt(accentPrimary)));
                enable_custom_monet.setVisibility(View.VISIBLE);
            }
        }
    };
}