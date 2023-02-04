package com.drdisagree.iconify.ui.activity;

import static android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION;
import static com.drdisagree.iconify.utils.ColorSchemeUtil.GenerateColorPalette;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.utils.ColorUtil;
import com.drdisagree.iconify.utils.MonetCompilerUtil;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class MonetEngine extends AppCompatActivity implements ColorPickerDialogListener {

    private LinearLayout[] colorTableRows;
    private int[][] systemColors;
    private RadioGroup radioGroup1, radioGroup2;
    private Button enable_custom_monet, disable_custom_monet;
    private static String accentPrimary, accentSecondary, selectedStyle;
    private static boolean isSelectedPrimary = false, isSelectedSecondary = false;
    public static List<String> EnabledOverlays = OverlayUtil.getEnabledOverlayList();
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

        colorTableRows = new LinearLayout[]{findViewById(R.id.color_table).findViewById(R.id.system_accent1), findViewById(R.id.color_table).findViewById(R.id.system_accent2), findViewById(R.id.color_table).findViewById(R.id.system_accent3), findViewById(R.id.color_table).findViewById(R.id.system_neutral1), findViewById(R.id.color_table).findViewById(R.id.system_neutral2)};
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

        selectedStyle = Prefs.getString("customMonetStyle").equals("null") ? "Neutral" : Prefs.getString("customMonetStyle");

        radioGroup1 = findViewById(R.id.monet_styles1);
        radioGroup2 = findViewById(R.id.monet_styles2);

        radioGroup1.clearCheck();
        radioGroup2.clearCheck();

        switch (selectedStyle) {
            case "Neutral":
                ((RadioButton) findViewById(R.id.neutral_style)).setChecked(true);
                break;
            case "Monochrome":
                ((RadioButton) findViewById(R.id.monochrome_style)).setChecked(true);
                break;
            case "Tonal Spot":
                ((RadioButton) findViewById(R.id.tonalspot_style)).setChecked(true);
                break;
            case "Vibrant":
                ((RadioButton) findViewById(R.id.vibrant_style)).setChecked(true);
                break;
            case "Expressive":
                ((RadioButton) findViewById(R.id.expressive_style)).setChecked(true);
                break;
            case "Fidelity":
                ((RadioButton) findViewById(R.id.fidelity_style)).setChecked(true);
                break;
            case "Content":
                ((RadioButton) findViewById(R.id.content_style)).setChecked(true);
                break;
        }

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

        if (Prefs.getBoolean("customMonet"))
            assignCustomColorToPalette(GenerateColorPalette(selectedStyle, Integer.parseInt(accentPrimary)));
        else {
            selectedStyle = "null";
            radioGroup1.clearCheck();
            radioGroup2.clearCheck();
            assignStockColorToPalette();
        }

        colorPickerDialogPrimary = ColorPickerDialog.newBuilder();
        colorPickerDialogSecondary = ColorPickerDialog.newBuilder();

        colorPickerDialogPrimary.setDialogStyle(R.style.ColorPicker).setColor(Integer.parseInt(accentPrimary)).setDialogType(ColorPickerDialog.TYPE_CUSTOM).setAllowCustom(false).setAllowPresets(true).setDialogId(1).setShowAlphaSlider(false).setShowColorShades(true);
        colorPickerDialogSecondary.setDialogStyle(R.style.ColorPicker).setColor(Integer.parseInt(accentSecondary)).setDialogType(ColorPickerDialog.TYPE_CUSTOM).setAllowCustom(false).setAllowPresets(true).setDialogId(2).setShowAlphaSlider(false).setShowColorShades(true);

        LinearLayout preview_coloraccentprimary = findViewById(R.id.preview_coloraccentprimary);
        preview_coloraccentprimary.setOnClickListener(v -> colorPickerDialogPrimary.show(MonetEngine.this));

        LinearLayout preview_coloraccentsecondary = findViewById(R.id.preview_coloraccentsecondary);
        preview_coloraccentsecondary.setOnClickListener(v -> colorPickerDialogSecondary.show(MonetEngine.this));

        // Monet accent saturation
        SeekBar monet_accent_saturation_seekbar = findViewById(R.id.monet_accent_saturation_seekbar);
        monet_accent_saturation_seekbar.setPadding(0, 0, 0, 0);
        TextView monet_accent_saturation_output = findViewById(R.id.monet_accent_saturation_output);
        monet_accent_saturation_output.setText(getResources().getString(R.string.opt_selected) + ' ' + (Prefs.getInt("monetAccentSaturation", 100) - 100) + "%");
        monet_accent_saturation_seekbar.setProgress(Prefs.getInt("monetAccentSaturation", 100));
        final int[] monetAccentSaturation = {Prefs.getInt("monetAccentSaturation", 100)};
        monet_accent_saturation_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                monetAccentSaturation[0] = progress;
                monet_accent_saturation_output.setText(getResources().getString(R.string.opt_selected) + ' ' + (progress - 100) + "%");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Prefs.putInt("monetAccentSaturation", monetAccentSaturation[0]);
                assignCustomColorToPalette(GenerateColorPalette(selectedStyle, Integer.parseInt(accentPrimary)));
                enable_custom_monet.setVisibility(View.VISIBLE);
            }
        });

        // Monet background saturation
        SeekBar monet_background_saturation_seekbar = findViewById(R.id.monet_background_saturation_seekbar);
        monet_background_saturation_seekbar.setPadding(0, 0, 0, 0);
        TextView monet_background_saturation_output = findViewById(R.id.monet_background_saturation_output);
        monet_background_saturation_output.setText(getResources().getString(R.string.opt_selected) + ' ' + (Prefs.getInt("monetBackgroundSaturation", 100) - 100) + "%");
        monet_background_saturation_seekbar.setProgress(Prefs.getInt("monetBackgroundSaturation", 100));
        final int[] monetBackgroundSaturation = {Prefs.getInt("monetBackgroundSaturation", 100)};
        monet_background_saturation_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                monetBackgroundSaturation[0] = progress;
                monet_background_saturation_output.setText(getResources().getString(R.string.opt_selected) + ' ' + (progress - 100) + "%");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Prefs.putInt("monetBackgroundSaturation", monetBackgroundSaturation[0]);
                assignCustomColorToPalette(GenerateColorPalette(selectedStyle, Integer.parseInt(accentPrimary)));
                enable_custom_monet.setVisibility(View.VISIBLE);
            }
        });

        // Monet lightness
        SeekBar monet_lightness_seekbar = findViewById(R.id.monet_lightness_seekbar);
        monet_lightness_seekbar.setPadding(0, 0, 0, 0);
        TextView monet_lightness_output = findViewById(R.id.monet_lightness_output);
        monet_lightness_output.setText(getResources().getString(R.string.opt_selected) + ' ' + (Prefs.getInt("monetLightness", 100) - 100) + "%");
        monet_lightness_seekbar.setProgress(Prefs.getInt("monetLightness", 100));
        final int[] monetLightness = {Prefs.getInt("monetLightness", 100)};
        monet_lightness_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                monetLightness[0] = progress;
                monet_lightness_output.setText(getResources().getString(R.string.opt_selected) + ' ' + (progress - 100) + "%");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Prefs.putInt("monetLightness", monetLightness[0]);
                assignCustomColorToPalette(GenerateColorPalette(selectedStyle, Integer.parseInt(accentPrimary)));
                enable_custom_monet.setVisibility(View.VISIBLE);
            }
        });

        // Enable custom colors button
        enable_custom_monet.setVisibility(View.GONE);
        enable_custom_monet.setOnClickListener(v -> {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent();
                intent.setAction(ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", Iconify.getAppContext().getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            } else if (Objects.equals(selectedStyle, "null")) {
                Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_select_style), Toast.LENGTH_SHORT).show();
            } else {
                if (isSelectedPrimary) Prefs.putString("colorAccentPrimary", accentPrimary);
                if (isSelectedSecondary) Prefs.putString("colorAccentSecondary", accentSecondary);
                AtomicBoolean hasErroredOut = new AtomicBoolean(false);

                Runnable runnable1 = () -> {
                    try {
                        if (applyCustomMonet())
                            hasErroredOut.set(true);
                        else
                            Prefs.putString("customMonetStyle", selectedStyle);
                    } catch (Exception e) {
                        hasErroredOut.set(true);
                    }

                    runOnUiThread(() -> {
                        if (!hasErroredOut.get())
                            Prefs.putBoolean("customMonet", true);

                        new Handler().postDelayed(() -> {
                            if (!hasErroredOut.get()) {
                                Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                                enable_custom_monet.setVisibility(View.GONE);
                                disable_custom_monet.setVisibility(View.VISIBLE);
                            }
                            else
                                Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                        }, 2000);
                    });
                };
                Thread thread1 = new Thread(runnable1);
                thread1.start();
            }
        });

        // Disable custom colors button
        disable_custom_monet.setVisibility(Prefs.getBoolean("customMonet") ? View.VISIBLE : View.GONE);
        disable_custom_monet.setOnClickListener(v -> {
            Runnable runnable2 = () -> {
                Prefs.putBoolean("customMonet", false);
                Prefs.putString("colorAccentPrimary", "null");
                Prefs.putString("colorAccentSecondary", "null");
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
        if (!Objects.equals(selectedStyle, "Monochrome")) {
            for (int i = 0; i < palette.size() - 2; i++) {
                for (int j = 1; j < palette.get(i).size() - 1; j++) {
                    int color;
                    if (j == 1)
                        color = ColorUtil.setSaturation(Integer.parseInt(String.valueOf((int) palette.get(i).get(j + 1))), ((float) (Prefs.getInt("monetAccentSaturation", 100) - 100) / 1000.0F) * (Math.max((1.5F - j / 8F), 1.5F)));
                    else
                        color = ColorUtil.setSaturation(Integer.parseInt(String.valueOf((int) palette.get(i).get(j))), ((float) (Prefs.getInt("monetAccentSaturation", 100) - 100) / 1000.0F) * (Math.max((3.0F - j / 5F), 3.0F)));

                    palette.get(i).set(j, color);
                }
            }
        }

        // Set background saturation
        if (!Objects.equals(selectedStyle, "Monochrome")) {
            for (int i = 3; i < palette.size(); i++) {
                for (int j = 1; j < palette.get(i).size() - 1; j++) {
                    int color;
                    if (j == 1)
                        color = ColorUtil.setSaturation(Integer.parseInt(String.valueOf((int) palette.get(i).get(j + 1))), ((float) (Prefs.getInt("monetBackgroundSaturation", 100) - 100) / 1000.0F) * (Math.max((1.5F - j / 8F), 1.5F)));
                    else
                        color = ColorUtil.setSaturation(Integer.parseInt(String.valueOf((int) palette.get(i).get(j))), ((float) (Prefs.getInt("monetBackgroundSaturation", 100) - 100) / 1000.0F) * (Math.max((3.0F - j / 5F), 3.0F)));

                    palette.get(i).set(j, color);
                }
            }
        }

        // Set lightness
        for (int i = Objects.equals(selectedStyle, "Monochrome") ? 0 : 3; i < palette.size(); i++) {
            for (int j = 1; j < palette.get(i).size() - 1; j++) {
                int color = ColorUtil.setLightness(Integer.parseInt(String.valueOf((int) palette.get(i).get(j))), (float) (Prefs.getInt("monetLightness", 100) - 100) / 1000.0F);

                palette.get(i).set(j, color);
            }
        }

        for (int i = 0; i < colorTableRows.length; i++) {
            if (i == 2 && (Prefs.getBoolean("customSecondaryColor") || isSelectedSecondary) && !Objects.equals(selectedStyle, "Monochrome")) {
                Prefs.putBoolean("customSecondaryColor", true);
                List<List<Object>> secondaryPalette = GenerateColorPalette(selectedStyle, Integer.parseInt(accentSecondary));

                for (int j = 0; j < colorTableRows[i].getChildCount(); j++) {
                    if (j == 0 || j == colorTableRows[i].getChildCount() - 1)
                        palette.get(i).set(j, secondaryPalette.get(0).get(j));
                    else if (j == 1)
                        palette.get(i).set(j, ColorUtil.setSaturation(Integer.parseInt(String.valueOf((int) secondaryPalette.get(0).get(j + 1))), ((float) (Prefs.getInt("monetAccentSaturation", 100) - 100) / 1000.0F) * (Math.max((1.5F - j / 8F), 1.5F))));
                    else
                        palette.get(i).set(j, ColorUtil.setSaturation(Integer.parseInt(String.valueOf((int) secondaryPalette.get(0).get(j))), ((float) (Prefs.getInt("monetAccentSaturation", 100) - 100) / 1000.0F) * (Math.max((3.0F - j / 5F), 3.0F))));

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

        resources.append("</resources>\n");

        return MonetCompilerUtil.buildMonetPalette(resources.toString());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}