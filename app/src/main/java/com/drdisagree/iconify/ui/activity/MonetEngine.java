package com.drdisagree.iconify.ui.activity;

import static com.drdisagree.iconify.common.References.COLOR_ACCENT_PRIMARY;
import static com.drdisagree.iconify.common.References.COLOR_ACCENT_SECONDARY;
import static com.drdisagree.iconify.common.References.COLOR_PIXEL_DARK_BG;
import static com.drdisagree.iconify.common.References.CUSTOM_PRIMARY_COLOR_SWITCH;
import static com.drdisagree.iconify.common.References.CUSTOM_SECONDARY_COLOR_SWITCH;
import static com.drdisagree.iconify.common.References.MONET_ACCENT_SATURATION;
import static com.drdisagree.iconify.common.References.MONET_ACCURATE_SHADES;
import static com.drdisagree.iconify.common.References.MONET_BACKGROUND_LIGHTNESS;
import static com.drdisagree.iconify.common.References.MONET_BACKGROUND_SATURATION;
import static com.drdisagree.iconify.common.References.MONET_ENGINE_SWITCH;
import static com.drdisagree.iconify.common.References.MONET_STYLE;
import static com.drdisagree.iconify.common.References.STR_NULL;
import static com.drdisagree.iconify.utils.ColorSchemeUtil.GenerateColorPalette;

import android.annotation.SuppressLint;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
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
import com.drdisagree.iconify.utils.ColorUtil;
import com.drdisagree.iconify.utils.FabricatedOverlayUtil;
import com.drdisagree.iconify.utils.MonetCompilerUtil;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class MonetEngine extends AppCompatActivity implements ColorPickerDialogListener {

    private static String accentPrimary, accentSecondary, selectedStyle;
    private static boolean isSelectedPrimary = false, isSelectedSecondary = false, accurateShades = true;
    int[] monetAccentSaturation = new int[]{Prefs.getInt(MONET_ACCENT_SATURATION, 100)};
    int[] monetBackgroundSaturation = new int[]{Prefs.getInt(MONET_BACKGROUND_SATURATION, 100)};
    int[] monetBackgroundLightness = new int[]{Prefs.getInt(MONET_BACKGROUND_LIGHTNESS, 100)};
    private LinearLayout[] colorTableRows;
    private int[][] systemColors;
    private RadioGroup radioGroup1, radioGroup2;
    private Button enable_custom_monet, disable_custom_monet;
    private ColorPickerDialog.Builder colorPickerDialogPrimary, colorPickerDialogSecondary;
    private List<List<Object>> generatedColorPalette = new ArrayList<>();

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

        Runnable runnable = () -> {
            for (int[] row : systemColors) {
                List<Object> temp = new ArrayList<>();
                for (int col : row) {
                    temp.add(col);
                }
                generatedColorPalette.add(temp);
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();

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
            accentPrimary = String.valueOf(getResources().getColor(android.R.color.system_accent1_200));

        if (!Objects.equals(Prefs.getString(COLOR_ACCENT_SECONDARY), STR_NULL))
            accentSecondary = Prefs.getString(COLOR_ACCENT_SECONDARY);
        else
            accentSecondary = String.valueOf(getResources().getColor(android.R.color.system_accent3_200));

        updatePrimaryColor();
        updateSecondaryColor();

        radioGroup1 = findViewById(R.id.monet_styles1);
        radioGroup2 = findViewById(R.id.monet_styles2);

        radioGroup1.setOnCheckedChangeListener(listener1);
        radioGroup2.setOnCheckedChangeListener(listener2);

        if (Prefs.getBoolean(MONET_ENGINE_SWITCH) && !Objects.equals(selectedStyle, STR_NULL))
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
        accurateShades = Prefs.getBoolean(MONET_ACCURATE_SHADES, true);
        monet_accurate_shades.setOnCheckedChangeListener((buttonView, isChecked) -> {
            accurateShades = isChecked;
            assignCustomColorToPalette(GenerateColorPalette(selectedStyle, Integer.parseInt(accentPrimary)));
            enable_custom_monet.setVisibility(View.VISIBLE);
        });

        // Monet accent saturation
        SeekBar monet_accent_saturation_seekbar = findViewById(R.id.monet_accent_saturation_seekbar);
        monet_accent_saturation_seekbar.setPadding(0, 0, 0, 0);
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
                if (progress == 100)
                    reset_accent_saturation.setVisibility(View.INVISIBLE);
                monet_accent_saturation_output.setText(getResources().getString(R.string.opt_selected) + ' ' + (progress - 100) + "%");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                assignCustomColorToPalette(GenerateColorPalette(selectedStyle, Integer.parseInt(accentPrimary)));
                enable_custom_monet.setVisibility(View.VISIBLE);
                reset_accent_saturation.setVisibility(monetAccentSaturation[0] == 100 ? View.INVISIBLE : View.VISIBLE);
            }
        });

        // Monet background saturation
        SeekBar monet_background_saturation_seekbar = findViewById(R.id.monet_background_saturation_seekbar);
        monet_background_saturation_seekbar.setPadding(0, 0, 0, 0);
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
                if (progress == 100)
                    reset_background_saturation.setVisibility(View.INVISIBLE);
                monet_background_saturation_output.setText(getResources().getString(R.string.opt_selected) + ' ' + (progress - 100) + "%");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                assignCustomColorToPalette(GenerateColorPalette(selectedStyle, Integer.parseInt(accentPrimary)));
                enable_custom_monet.setVisibility(View.VISIBLE);
                reset_background_saturation.setVisibility(monetBackgroundSaturation[0] == 100 ? View.INVISIBLE : View.VISIBLE);
            }
        });

        // Monet background lightness
        SeekBar monet_background_lightness_seekbar = findViewById(R.id.monet_background_lightness_seekbar);
        monet_background_lightness_seekbar.setPadding(0, 0, 0, 0);
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
                if (progress == 100)
                    reset_background_lightness.setVisibility(View.INVISIBLE);
                monet_background_lightness_output.setText(getResources().getString(R.string.opt_selected) + ' ' + (progress - 100) + "%");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                assignCustomColorToPalette(GenerateColorPalette(selectedStyle, Integer.parseInt(accentPrimary)));
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
                        if (applyCustomMonet()) hasErroredOut.set(true);
                        else Prefs.putString(MONET_STYLE, selectedStyle);
                    } catch (Exception e) {
                        hasErroredOut.set(true);
                    }

                    runOnUiThread(() -> {
                        if (!hasErroredOut.get()) Prefs.putBoolean(MONET_ENGINE_SWITCH, true);

                        new Handler().postDelayed(() -> {
                            if (!hasErroredOut.get()) {
                                Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                                enable_custom_monet.setVisibility(View.GONE);
                                disable_custom_monet.setVisibility(View.VISIBLE);
                            } else
                                Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                        }, 2000);
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
        // Set accent saturation
        if (!Objects.equals(selectedStyle, getResources().getString(R.string.monet_monochrome))) {
            for (int i = 0; i < palette.size() - 2; i++) {
                for (int j = palette.get(i).size() - 2; j >= 1; j--) {
                    int color;
                    if (j == 1)
                        color = ColorUtil.setSaturation(Integer.parseInt(String.valueOf((int) palette.get(i).get(j + 1))), -0.1F);
                    else
                        color = ColorUtil.setSaturation(Integer.parseInt(String.valueOf((int) palette.get(i).get(j))), ((float) (monetAccentSaturation[0] - 100) / 1000.0F) * (Math.min((3.0F - j / 5F), 3.0F)));

                    if (!accurateShades) {
                        if (i == 0 && j == 4)
                            color = Integer.parseInt(accentPrimary);
                        else if (i == 2 && j == 4)
                            color = Integer.parseInt(String.valueOf((int) palette.get(i).get(6)));
                    }

                    palette.get(i).set(j, color);
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
                }
            }
        }

        // Set lightness
        for (int i = Objects.equals(selectedStyle, getResources().getString(R.string.monet_monochrome)) ? 0 : 3; i < palette.size(); i++) {
            for (int j = 1; j < palette.get(i).size() - 1; j++) {
                int color = ColorUtil.setLightness(Integer.parseInt(String.valueOf((int) palette.get(i).get(j))), (float) (monetBackgroundLightness[0] - 100) / 1000.0F);

                palette.get(i).set(j, color);
            }
        }

        for (int i = 0; i < colorTableRows.length; i++) {
            if (i == 2 && (Prefs.getBoolean(CUSTOM_SECONDARY_COLOR_SWITCH) || isSelectedSecondary) && !Objects.equals(selectedStyle, getResources().getString(R.string.monet_monochrome))) {
                Prefs.putBoolean(CUSTOM_SECONDARY_COLOR_SWITCH, true);
                List<List<Object>> secondaryPalette = GenerateColorPalette(selectedStyle, Integer.parseInt(accentSecondary));

                for (int j = colorTableRows[i].getChildCount() - 1; j >= 0; j--) {
                    if (j == 0 || j == colorTableRows[i].getChildCount() - 1)
                        palette.get(i).set(j, secondaryPalette.get(0).get(j));
                    else if (j == 1)
                        palette.get(i).set(j, ColorUtil.setSaturation(Integer.parseInt(String.valueOf((int) palette.get(i).get(j + 1))), -0.1F));
                    else
                        palette.get(i).set(j, ColorUtil.setSaturation(Integer.parseInt(String.valueOf((int) secondaryPalette.get(0).get(j))), ((float) (monetAccentSaturation[0] - 100) / 1000.0F) * (Math.min((3.0F - j / 5F), 3.0F))));

                    if (!accurateShades) {
                        if (j == 4)
                            palette.get(i).set(j, Integer.parseInt(accentSecondary));
                    }

                    GradientDrawable colorbg = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{(int) palette.get(i).get(j), (int) palette.get(i).get(j)});
                    colorbg.setCornerRadius(8 * getResources().getDisplayMetrics().density);
                    colorTableRows[i].getChildAt(j).setBackgroundDrawable(colorbg);
                }
            } else {
                for (int j = 0; j < colorTableRows[i].getChildCount(); j++) {
                    GradientDrawable colorbg = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{(int) palette.get(i).get(j), (int) palette.get(i).get(j)});
                    colorbg.setCornerRadius(8 * getResources().getDisplayMetrics().density);
                    colorTableRows[i].getChildAt(j).setBackgroundDrawable(colorbg);
                }
            }
        }

        generatedColorPalette = palette;
    }

    private boolean applyCustomMonet() throws IOException {
        String[][] colors = ColorUtil.getColorNames();

        StringBuilder resources = new StringBuilder("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<resources>\n");

        for (int i = 0; i < colors.length; i++) {
            for (int j = 0; j < colors[i].length; j++) {
                resources.append("    <color name=\"").append(colors[i][j]).append("\">").append(ColorUtil.ColorToHex((int) generatedColorPalette.get(i).get(j), false, true)).append("</color>\n");
            }
        }
        resources.append("    <color name=\"holo_blue_light\">").append(ColorUtil.ColorToHex((int) generatedColorPalette.get(0).get(4), false, true)).append("</color>\n");
        resources.append("    <color name=\"holo_green_light\">").append(ColorUtil.ColorToHex((int) generatedColorPalette.get(2).get(4), false, true)).append("</color>\n");
        resources.append("    <color name=\"holo_blue_dark\">").append(ColorUtil.ColorToHex((int) generatedColorPalette.get(1).get(10), false, true)).append("</color>\n");
        resources.append("<color name=\"accent_device_default\">@*android:color/holo_blue_light</color>\n" +
                "    <color name=\"accent_device_default_dark\">@*android:color/holo_blue_light</color>\n" +
                "    <color name=\"accent_device_default_light\">@*android:color/holo_blue_light</color>\n" +
                "    <color name=\"accent_material_dark\">@*android:color/holo_blue_light</color>\n" +
                "    <color name=\"accent_material_light\">@*android:color/holo_blue_light</color>\n" +
                "    <color name=\"accent_primary_device_default\">@*android:color/holo_blue_light</color>\n" +
                "    <color name=\"accent_secondary_device_default\">@*android:color/system_accent2_200</color>\n" +
                "    <color name=\"accent_tertiary_device_default\">@*android:color/system_accent3_200</color>\n" +
                "    <color name=\"autofill_background_material_dark\">@*android:color/background_dark</color>\n" +
                "    <color name=\"background_dark\">@*android:color/system_neutral1_900</color>\n" +
                "    <color name=\"background_light\">@*android:color/system_neutral1_50</color>\n" +
                "    <color name=\"background_device_default_dark\">@*android:color/background_dark</color>\n" +
                "    <color name=\"background_device_default_light\">@*android:color/background_light</color>\n" +
                "    <color name=\"background_floating_device_default_dark\">@*android:color/background_dark</color>\n" +
                "    <color name=\"background_floating_device_default_light\">@*android:color/background_light</color>\n" +
                "    <color name=\"background_floating_material_dark\">@*android:color/background_dark</color>\n" +
                "    <color name=\"background_floating_material_light\">@*android:color/background_light</color>\n" +
                "    <color name=\"background_leanback_dark\">@*android:color/background_dark</color>\n" +
                "    <color name=\"background_material_dark\">@*android:color/background_dark</color>\n" +
                "    <color name=\"background_material_light\">@*android:color/background_light</color>\n" +
                "    <color name=\"BottomBarBackground\">@*android:color/background_material_light</color>\n" +
                "    <color name=\"button_material_dark\">@*android:color/background_dark</color>\n" +
                "    <color name=\"floating_popup_divider_dark\">@*android:color/background_dark</color>\n" +
                "    <color name=\"GM2_grey_800\">@*android:color/background_dark</color>\n" +
                "    <color name=\"holo_light_button_normal\">@*android:color/background_material_light</color>\n" +
                "    <color name=\"holo_primary_dark\">@*android:color/background_material_dark</color>\n" +
                "    <color name=\"list_divider_color_dark\">@*android:color/background_dark</color>\n" +
                "    <color name=\"material_blue_grey_800\">@*android:color/background_dark</color>\n" +
                "    <color name=\"material_blue_grey_900\">@*android:color/background_dark</color>\n" +
                "    <color name=\"material_blue_grey_950\">@*android:color/background_dark</color>\n" +
                "    <color name=\"material_grey_800\">@*android:color/background_dark</color>\n" +
                "    <color name=\"material_grey_850\">@*android:color/background_dark</color>\n" +
                "    <color name=\"material_grey_900\">@*android:color/background_dark</color>\n" +
                "    <color name=\"primary_dark_device_default_dark\">@*android:color/background_dark</color>\n" +
                "    <color name=\"primary_dark_device_default_settings\">@*android:color/background_dark</color>\n" +
                "    <color name=\"primary_dark_material_dark\">@*android:color/background_dark</color>\n" +
                "    <color name=\"primary_dark_material_settings\">@*android:color/background_dark</color>\n" +
                "    <color name=\"primary_device_default_dark\">@*android:color/background_dark</color>\n" +
                "    <color name=\"primary_device_default_settings\">@*android:color/background_dark</color>\n" +
                "    <color name=\"primary_material_dark\">@*android:color/background_dark</color>\n" +
                "    <color name=\"primary_material_settings\">@*android:color/background_dark</color>\n");
        resources.append("</resources>\n");

        return MonetCompilerUtil.buildMonetPalette(resources.toString());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
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

    private void disableBasicColors() {
        Prefs.clearPref("customMonetColor");
        Prefs.clearPref(CUSTOM_PRIMARY_COLOR_SWITCH);
        Prefs.clearPref(CUSTOM_SECONDARY_COLOR_SWITCH);

        FabricatedOverlayUtil.disableOverlay(COLOR_ACCENT_PRIMARY);
        FabricatedOverlayUtil.disableOverlay(COLOR_PIXEL_DARK_BG);
        FabricatedOverlayUtil.disableOverlay(COLOR_ACCENT_SECONDARY);
    }
}