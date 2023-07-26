package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Preferences.COLOR_ACCENT_PRIMARY;
import static com.drdisagree.iconify.common.Preferences.COLOR_ACCENT_PRIMARY_LIGHT;
import static com.drdisagree.iconify.common.Preferences.COLOR_ACCENT_SECONDARY;
import static com.drdisagree.iconify.common.Preferences.COLOR_ACCENT_SECONDARY_LIGHT;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_ACCENT;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_PRIMARY_COLOR_SWITCH;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_SECONDARY_COLOR_SWITCH;
import static com.drdisagree.iconify.common.Preferences.MONET_ACCURATE_SHADES;
import static com.drdisagree.iconify.common.Preferences.MONET_BACKGROUND_LIGHTNESS;
import static com.drdisagree.iconify.common.Preferences.MONET_BACKGROUND_SATURATION;
import static com.drdisagree.iconify.common.Preferences.MONET_ENGINE_SWITCH;
import static com.drdisagree.iconify.common.Preferences.MONET_PRIMARY_ACCENT_SATURATION;
import static com.drdisagree.iconify.common.Preferences.MONET_PRIMARY_COLOR;
import static com.drdisagree.iconify.common.Preferences.MONET_SECONDARY_ACCENT_SATURATION;
import static com.drdisagree.iconify.common.Preferences.MONET_SECONDARY_COLOR;
import static com.drdisagree.iconify.common.Preferences.MONET_STYLE;
import static com.drdisagree.iconify.common.Preferences.STR_NULL;
import static com.drdisagree.iconify.utils.ColorSchemeUtil.generateColorPalette;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.databinding.ActivityMonetEngineBinding;
import com.drdisagree.iconify.overlaymanager.MonetEngineManager;
import com.drdisagree.iconify.ui.utils.ViewBindingHelpers;
import com.drdisagree.iconify.ui.views.RadioDialog;
import com.drdisagree.iconify.utils.ColorUtil;
import com.drdisagree.iconify.utils.FabricatedUtil;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.drdisagree.iconify.utils.helpers.ImportExport;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class MonetEngine extends BaseActivity implements ColorPickerDialogListener, RadioDialog.RadioDialogListener {

    private static String accentPrimary, accentSecondary, selectedStyle;
    private static boolean isSelectedPrimary = false, isSelectedSecondary = false, accurateShades = Prefs.getBoolean(MONET_ACCURATE_SHADES, true);
    private final List<List<List<Object>>> finalPalette = new ArrayList<>();
    private final int[] selectedChild = new int[2];
    int[] monetPrimaryAccentSaturation = new int[]{Prefs.getInt(MONET_PRIMARY_ACCENT_SATURATION, 100)};
    int[] monetSecondaryAccentSaturation = new int[]{Prefs.getInt(MONET_SECONDARY_ACCENT_SATURATION, 100)};
    int[] monetBackgroundSaturation = new int[]{Prefs.getInt(MONET_BACKGROUND_SATURATION, 100)};
    int[] monetBackgroundLightness = new int[]{Prefs.getInt(MONET_BACKGROUND_LIGHTNESS, 100)};
    private ActivityMonetEngineBinding binding;
    private LinearLayout[] colorTableRows;
    private int[][] systemColors;
    private ColorPickerDialog.Builder colorPickerDialogPrimary, colorPickerDialogSecondary, colorPickerDialogCustom;
    private boolean isDarkMode = SystemUtil.isDarkMode();
    private RadioDialog rd_monet_style;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMonetEngineBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Header
        ViewBindingHelpers.setHeader(this, binding.header.collapsingToolbar, binding.header.toolbar, R.string.activity_title_monet_engine);

        colorTableRows = new LinearLayout[]{
                binding.monetEngine.systemAccent1,
                binding.monetEngine.systemAccent2,
                binding.monetEngine.systemAccent3,
                binding.monetEngine.systemNeutral1,
                binding.monetEngine.systemNeutral2
        };
        systemColors = ColorUtil.getSystemColors(MonetEngine.this);

        List<List<Object>> temp = new ArrayList<>();
        for (int[] row : systemColors) {
            List<Object> temp2 = new ArrayList<>();
            for (int col : row) {
                temp2.add(col);
            }
            temp.add(temp2);
        }
        finalPalette.clear();
        finalPalette.add(temp);
        finalPalette.add(temp);

        isDarkMode = SystemUtil.isDarkMode();
        selectedStyle = Prefs.getString(MONET_STYLE, getResources().getString(R.string.monet_neutral));

        // Monet Style
        int selectedIndex = Arrays.asList(getResources().getStringArray(R.array.monet_style)).indexOf(selectedStyle);
        rd_monet_style = new RadioDialog(this, 0, selectedIndex);
        rd_monet_style.setRadioDialogListener(this);
        binding.monetStyles.setOnClickListener(v -> rd_monet_style.show(R.string.monet_style_title, R.array.monet_style, binding.selectedMonetStyle));
        binding.selectedMonetStyle.setText(getResources().getString(R.string.opt_selected) + " " + Arrays.asList(getResources().getStringArray(R.array.monet_style)).get(rd_monet_style.getSelectedIndex()));

        accentPrimary = Prefs.getString(MONET_PRIMARY_COLOR, String.valueOf(getResources().getColor(isDarkMode ? android.R.color.system_accent1_300 : android.R.color.system_accent1_600, getTheme())));
        accentSecondary = Prefs.getString(MONET_SECONDARY_COLOR, String.valueOf(getResources().getColor(isDarkMode ? android.R.color.system_accent3_300 : android.R.color.system_accent3_600, getTheme())));

        updatePrimaryColor();
        updateSecondaryColor();

        assignStockColorToPalette();

        colorPickerDialogPrimary = ColorPickerDialog.newBuilder();
        colorPickerDialogSecondary = ColorPickerDialog.newBuilder();
        colorPickerDialogCustom = ColorPickerDialog.newBuilder();

        colorPickerDialogPrimary.setDialogStyle(R.style.ColorPicker).setColor(Integer.parseInt(accentPrimary)).setDialogType(ColorPickerDialog.TYPE_CUSTOM).setAllowCustom(false).setAllowPresets(true).setDialogId(1).setShowAlphaSlider(false).setShowColorShades(true);
        colorPickerDialogSecondary.setDialogStyle(R.style.ColorPicker).setColor(Integer.parseInt(accentSecondary)).setDialogType(ColorPickerDialog.TYPE_CUSTOM).setAllowCustom(false).setAllowPresets(true).setDialogId(2).setShowAlphaSlider(false).setShowColorShades(true);

        binding.previewColoraccentprimary.setOnClickListener(v -> colorPickerDialogPrimary.show(MonetEngine.this));

        binding.previewColoraccentsecondary.setOnClickListener(v -> colorPickerDialogSecondary.show(MonetEngine.this));

        // Monet Accurate Shades
        binding.monetAccurateShades.setChecked(Prefs.getBoolean(MONET_ACCURATE_SHADES, true));
        binding.monetAccurateShades.setOnCheckedChangeListener((buttonView, isChecked) -> {
            accurateShades = isChecked;
            assignCustomColorToPalette();
            binding.enableCustomMonet.setVisibility(View.VISIBLE);
        });

        // Monet primary accent saturation
        binding.monetPrimaryAccentSaturationOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + (Prefs.getInt(MONET_PRIMARY_ACCENT_SATURATION, 100) - 100) + "%");
        binding.monetPrimaryAccentSaturationSeekbar.setProgress(Prefs.getInt(MONET_PRIMARY_ACCENT_SATURATION, 100));

        // Long Click Reset
        binding.resetPrimaryAccentSaturation.setVisibility(Prefs.getInt(MONET_PRIMARY_ACCENT_SATURATION, 100) == 100 ? View.INVISIBLE : View.VISIBLE);

        binding.resetPrimaryAccentSaturation.setOnLongClickListener(v -> {
            monetPrimaryAccentSaturation[0] = 100;
            binding.monetPrimaryAccentSaturationSeekbar.setProgress(100);
            assignCustomColorToPalette();
            binding.resetPrimaryAccentSaturation.setVisibility(View.INVISIBLE);
            binding.enableCustomMonet.setVisibility(View.VISIBLE);
            return true;
        });

        binding.monetPrimaryAccentSaturationSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                monetPrimaryAccentSaturation[0] = progress;
                if (progress == 100)
                    binding.resetPrimaryAccentSaturation.setVisibility(View.INVISIBLE);
                binding.monetPrimaryAccentSaturationOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + (progress - 100) + "%");
                assignCustomColorToPalette();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                binding.enableCustomMonet.setVisibility(View.VISIBLE);
                binding.resetPrimaryAccentSaturation.setVisibility(monetPrimaryAccentSaturation[0] == 100 ? View.INVISIBLE : View.VISIBLE);
            }
        });

        // Monet secondary accent saturation
        binding.monetSecondaryAccentSaturationOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + (Prefs.getInt(MONET_SECONDARY_ACCENT_SATURATION, 100) - 100) + "%");
        binding.monetSecondaryAccentSaturationSeekbar.setProgress(Prefs.getInt(MONET_SECONDARY_ACCENT_SATURATION, 100));

        // Long Click Reset
        binding.resetSecondaryAccentSaturation.setVisibility(Prefs.getInt(MONET_SECONDARY_ACCENT_SATURATION, 100) == 100 ? View.INVISIBLE : View.VISIBLE);

        binding.resetSecondaryAccentSaturation.setOnLongClickListener(v -> {
            monetSecondaryAccentSaturation[0] = 100;
            binding.monetSecondaryAccentSaturationSeekbar.setProgress(100);
            assignCustomColorToPalette();
            binding.resetSecondaryAccentSaturation.setVisibility(View.INVISIBLE);
            binding.enableCustomMonet.setVisibility(View.VISIBLE);
            return true;
        });

        binding.monetSecondaryAccentSaturationSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                monetSecondaryAccentSaturation[0] = progress;
                if (progress == 100)
                    binding.resetSecondaryAccentSaturation.setVisibility(View.INVISIBLE);
                binding.monetSecondaryAccentSaturationOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + (progress - 100) + "%");
                assignCustomColorToPalette();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                binding.enableCustomMonet.setVisibility(View.VISIBLE);
                binding.resetSecondaryAccentSaturation.setVisibility(monetSecondaryAccentSaturation[0] == 100 ? View.INVISIBLE : View.VISIBLE);
            }
        });

        // Monet background saturation
        binding.monetBackgroundSaturationOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + (Prefs.getInt(MONET_BACKGROUND_SATURATION, 100) - 100) + "%");
        binding.monetBackgroundSaturationSeekbar.setProgress(Prefs.getInt(MONET_BACKGROUND_SATURATION, 100));

        // Reset button
        binding.resetBackgroundSaturation.setVisibility(Prefs.getInt(MONET_BACKGROUND_SATURATION, 100) == 100 ? View.INVISIBLE : View.VISIBLE);

        binding.resetBackgroundSaturation.setOnLongClickListener(v -> {
            monetBackgroundSaturation[0] = 100;
            binding.monetBackgroundSaturationSeekbar.setProgress(100);
            assignCustomColorToPalette();
            binding.resetBackgroundSaturation.setVisibility(View.INVISIBLE);
            binding.enableCustomMonet.setVisibility(View.VISIBLE);
            return true;
        });

        binding.monetBackgroundSaturationSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                monetBackgroundSaturation[0] = progress;
                if (progress == 100)
                    binding.resetBackgroundSaturation.setVisibility(View.INVISIBLE);
                binding.monetBackgroundSaturationOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + (progress - 100) + "%");
                assignCustomColorToPalette();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                binding.enableCustomMonet.setVisibility(View.VISIBLE);
                binding.resetBackgroundSaturation.setVisibility(monetBackgroundSaturation[0] == 100 ? View.INVISIBLE : View.VISIBLE);
            }
        });

        // Monet background lightness
        binding.monetBackgroundLightnessOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + (Prefs.getInt(MONET_BACKGROUND_LIGHTNESS, 100) - 100) + "%");
        binding.monetBackgroundLightnessSeekbar.setProgress(Prefs.getInt(MONET_BACKGROUND_LIGHTNESS, 100));

        // Long Click Reset
        binding.resetBackgroundLightness.setVisibility(Prefs.getInt(MONET_BACKGROUND_LIGHTNESS, 100) == 100 ? View.INVISIBLE : View.VISIBLE);

        binding.resetBackgroundLightness.setOnLongClickListener(v -> {
            monetBackgroundLightness[0] = 100;
            binding.monetBackgroundLightnessSeekbar.setProgress(100);
            assignCustomColorToPalette();
            binding.resetBackgroundLightness.setVisibility(View.INVISIBLE);
            binding.enableCustomMonet.setVisibility(View.VISIBLE);
            return true;
        });

        binding.monetBackgroundLightnessSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                monetBackgroundLightness[0] = progress;
                if (progress == 100) binding.resetBackgroundLightness.setVisibility(View.INVISIBLE);
                binding.monetBackgroundLightnessOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + (progress - 100) + "%");
                assignCustomColorToPalette();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                binding.enableCustomMonet.setVisibility(View.VISIBLE);
                binding.resetBackgroundLightness.setVisibility(monetBackgroundLightness[0] == 100 ? View.INVISIBLE : View.VISIBLE);
            }
        });

        // Enable custom colors button
        binding.enableCustomMonet.setVisibility(View.GONE);
        binding.enableCustomMonet.setOnClickListener(v -> {
            if (!Environment.isExternalStorageManager()) {
                SystemUtil.getStoragePermission(this);
            } else if (Objects.equals(selectedStyle, STR_NULL)) {
                Toast.makeText(MonetEngine.this, getResources().getString(R.string.toast_select_style), Toast.LENGTH_SHORT).show();
            } else {
                Prefs.putBoolean(MONET_ACCURATE_SHADES, accurateShades);
                if (isSelectedPrimary) Prefs.putString(MONET_PRIMARY_COLOR, accentPrimary);
                if (isSelectedSecondary) Prefs.putString(MONET_SECONDARY_COLOR, accentSecondary);
                Prefs.putString(MONET_STYLE, selectedStyle);
                Prefs.putInt(MONET_PRIMARY_ACCENT_SATURATION, monetPrimaryAccentSaturation[0]);
                Prefs.putInt(MONET_SECONDARY_ACCENT_SATURATION, monetSecondaryAccentSaturation[0]);
                Prefs.putInt(MONET_BACKGROUND_SATURATION, monetBackgroundSaturation[0]);
                Prefs.putInt(MONET_BACKGROUND_LIGHTNESS, monetBackgroundLightness[0]);

                disableBasicColors();

                AtomicBoolean hasErroredOut = new AtomicBoolean(false);

                Runnable runnable1 = () -> {
                    try {
                        if (MonetEngineManager.enableOverlay(finalPalette, true)) {
                            hasErroredOut.set(true);
                        }
                    } catch (Exception e) {
                        hasErroredOut.set(true);
                        Log.e("MonetEngine", e.toString());
                    }

                    runOnUiThread(() -> {
                        if (!hasErroredOut.get()) {
                            Prefs.putBoolean(MONET_ENGINE_SWITCH, true);
                            if (Prefs.getBoolean("IconifyComponentQSPBD.overlay")) {
                                OverlayUtil.changeOverlayState("IconifyComponentQSPBD.overlay", false, "IconifyComponentQSPBD.overlay", true);
                            } else if (Prefs.getBoolean("IconifyComponentQSPBA.overlay")) {
                                OverlayUtil.changeOverlayState("IconifyComponentQSPBA.overlay", false, "IconifyComponentQSPBA.overlay", true);
                            }
                        }

                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            if (!hasErroredOut.get()) {
                                Toast.makeText(MonetEngine.this, getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                                binding.enableCustomMonet.setVisibility(View.GONE);
                                binding.disableCustomMonet.setVisibility(View.VISIBLE);
                            } else
                                Toast.makeText(MonetEngine.this, getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                        }, 20);
                    });
                };
                Thread thread1 = new Thread(runnable1);
                thread1.start();
            }
        });

        // Disable custom colors button
        binding.disableCustomMonet.setVisibility(Prefs.getBoolean(MONET_ENGINE_SWITCH) ? View.VISIBLE : View.GONE);
        binding.disableCustomMonet.setOnClickListener(v -> {
            Runnable runnable2 = () -> {
                Prefs.putBoolean(MONET_ENGINE_SWITCH, false);
                Prefs.clearPrefs(MONET_PRIMARY_COLOR, MONET_SECONDARY_COLOR);
                OverlayUtil.disableOverlays("IconifyComponentDM.overlay", "IconifyComponentME.overlay");

                runOnUiThread(() -> {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        Toast.makeText(MonetEngine.this, getResources().getString(R.string.toast_disabled), Toast.LENGTH_SHORT).show();
                        binding.disableCustomMonet.setVisibility(View.GONE);
                        isSelectedPrimary = false;
                        isSelectedSecondary = false;
                    }, 2000);
                });
            };
            Thread thread2 = new Thread(runnable2);
            thread2.start();
        });

        for (int i = 0; i < colorTableRows.length; i++) {
            for (int j = 0; j < colorTableRows[i].getChildCount(); j++) {
                View child = colorTableRows[i].getChildAt(j);
                int finalI = i;
                int finalJ = j;
                child.setOnClickListener(view -> {
                    selectedChild[0] = finalI;
                    selectedChild[1] = finalJ;
                    int[] color = ((GradientDrawable) child.getBackground()).getColors();
                    colorPickerDialogCustom.setDialogStyle(R.style.ColorPicker).setColor(color[0]).setDialogType(ColorPickerDialog.TYPE_CUSTOM).setAllowCustom(false).setAllowPresets(true).setDialogId(3).setShowAlphaSlider(false).setShowColorShades(true).show(MonetEngine.this);
                });
            }
        }
    }

    private void updatePrimaryColor() {
        GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{Integer.parseInt(accentPrimary), Integer.parseInt(accentPrimary)});
        gd.setCornerRadius(getResources().getDimension(R.dimen.preview_color_picker_radius) * getResources().getDisplayMetrics().density);
        binding.previewColorPickerPrimary.setBackground(gd);
    }

    private void updateSecondaryColor() {
        GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{Integer.parseInt(accentSecondary), Integer.parseInt(accentSecondary)});
        gd.setCornerRadius(getResources().getDimension(R.dimen.preview_color_picker_radius) * getResources().getDisplayMetrics().density);
        binding.previewColorPickerSecondary.setBackground(gd);
    }

    private void assignStockColorToPalette() {
        for (int i = 0; i < colorTableRows.length; i++) {
            for (int j = 0; j < colorTableRows[i].getChildCount(); j++) {
                GradientDrawable colorbg = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{systemColors[i][j], systemColors[i][j]});
                colorbg.setCornerRadius(8 * getResources().getDisplayMetrics().density);
                colorTableRows[i].getChildAt(j).setBackground(colorbg);
            }
        }
    }

    private void assignCustomColorToPalette() {
        List<List<Object>> palette = generateColorPalette(MonetEngine.this, selectedStyle, Integer.parseInt(accentPrimary));
        List<List<Object>> palette_night = cloneList(palette);

        if (!Objects.equals(selectedStyle, getResources().getString(R.string.monet_monochrome))) {
            // Set primary accent saturation
            for (int i = 0; i <= 1; i++) {
                for (int j = palette.get(i).size() - 2; j >= 1; j--) {
                    int color;

                    if (j == 1)
                        color = ColorUtil.setSaturation(Integer.parseInt(String.valueOf((int) palette.get(i).get(j + 1))), -0.1F);
                    else
                        color = ColorUtil.setSaturation(Integer.parseInt(String.valueOf((int) palette.get(i).get(j))), ((float) (monetPrimaryAccentSaturation[0] - 100) / 1000.0F) * (Math.min((3.0F - j / 5F), 3.0F)));

                    palette.get(i).set(j, color);
                    palette_night.get(i).set(j, color);

                    if (!accurateShades) {
                        if (i == 0 && j == 8)
                            palette.get(i).set(j, Integer.parseInt(accentPrimary));

                        if (i == 0 && j == 5)
                            palette_night.get(i).set(j, Integer.parseInt(accentPrimary));
                    }
                }
            }

            // Set secondary accent saturation
            int i = 2;
            for (int j = palette.get(i).size() - 2; j >= 1; j--) {
                int color;

                if (j == 1)
                    color = ColorUtil.setSaturation(Integer.parseInt(String.valueOf((int) palette.get(i).get(j + 1))), -0.1F);
                else
                    color = ColorUtil.setSaturation(Integer.parseInt(String.valueOf((int) palette.get(i).get(j))), ((float) (monetSecondaryAccentSaturation[0] - 100) / 1000.0F) * (Math.min((3.0F - j / 5F), 3.0F)));

                palette.get(i).set(j, color);
                palette_night.get(i).set(j, color);
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
                List<List<Object>> secondaryPalette = generateColorPalette(MonetEngine.this, selectedStyle, Integer.parseInt(accentSecondary));

                for (int j = colorTableRows[i].getChildCount() - 1; j >= 0; j--) {
                    int color;

                    if (j == 0 || j == colorTableRows[i].getChildCount() - 1)
                        color = (int) secondaryPalette.get(0).get(j);
                    else if (j == 1)
                        color = ColorUtil.setSaturation(Integer.parseInt(String.valueOf((int) palette.get(i).get(j + 1))), -0.1F);
                    else
                        color = ColorUtil.setSaturation(Integer.parseInt(String.valueOf((int) secondaryPalette.get(0).get(j))), ((float) (monetSecondaryAccentSaturation[0] - 100) / 1000.0F) * (Math.min((3.0F - j / 5F), 3.0F)));

                    palette.get(i).set(j, color);
                    palette_night.get(i).set(j, color);

                    if (!accurateShades) {
                        if (j == 8) {
                            palette.get(i).set(j, Integer.parseInt(accentSecondary));
                        }
                        if (j == 5) {
                            palette_night.get(i).set(j, Integer.parseInt(accentSecondary));
                        }
                    }

                    GradientDrawable colorbg = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{!isDarkMode ? (int) palette.get(i).get(j) : (int) palette_night.get(i).get(j), !isDarkMode ? (int) palette.get(i).get(j) : (int) palette_night.get(i).get(j)});
                    colorbg.setCornerRadius(8 * getResources().getDisplayMetrics().density);
                    colorTableRows[i].getChildAt(j).setBackground(colorbg);
                }
            } else {
                for (int j = 0; j < colorTableRows[i].getChildCount(); j++) {
                    try {
                        GradientDrawable colorbg = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{!isDarkMode ? (int) palette.get(i).get(j) : (int) palette_night.get(i).get(j), !isDarkMode ? (int) palette.get(i).get(j) : (int) palette_night.get(i).get(j)});
                        colorbg.setCornerRadius(8 * getResources().getDisplayMetrics().density);
                        colorTableRows[i].getChildAt(j).setBackground(colorbg);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        finalPalette.clear();
        finalPalette.add(palette);
        finalPalette.add(palette_night);
    }

    private void disableBasicColors() {
        Prefs.clearPrefs(CUSTOM_ACCENT, COLOR_ACCENT_PRIMARY, COLOR_ACCENT_SECONDARY, CUSTOM_PRIMARY_COLOR_SWITCH, CUSTOM_SECONDARY_COLOR_SWITCH);

        FabricatedUtil.disableOverlays(COLOR_ACCENT_PRIMARY, COLOR_ACCENT_PRIMARY_LIGHT, COLOR_ACCENT_SECONDARY, COLOR_ACCENT_SECONDARY_LIGHT);
    }

    private List<List<Object>> cloneList(final List<List<Object>> src) {
        List<List<Object>> cloned = new ArrayList<>();
        for (List<Object> sublist : src) {
            cloned.add(new ArrayList<>(sublist));
        }
        return cloned;
    }

    private void importExportSettings(boolean export) {
        if (!Environment.isExternalStorageManager()) {
            SystemUtil.getStoragePermission(MonetEngine.this);
        } else {
            Intent fileIntent = new Intent();
            fileIntent.setAction(export ? Intent.ACTION_CREATE_DOCUMENT : Intent.ACTION_GET_CONTENT);
            fileIntent.setType("*/*");
            fileIntent.putExtra(Intent.EXTRA_TITLE, "monet_configs" + ".iconify");
            if (export) {
                startExportActivityIntent.launch(fileIntent);
            } else {
                startImportActivityIntent.launch(fileIntent);
            }
        }
    }

    ActivityResultLauncher<Intent> startExportActivityIntent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result1 -> {
                if (result1.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result1.getData();
                    if (data == null) return;

                    try {
                        ImportExport.exportSettings(Prefs.prefs, MonetEngine.this.getContentResolver().openOutputStream(data.getData()));
                        Toast.makeText(MonetEngine.this, MonetEngine.this.getResources().getString(R.string.toast_export_settings_successfull), Toast.LENGTH_SHORT).show();
                    } catch (Exception exception) {
                        Toast.makeText(MonetEngine.this, MonetEngine.this.getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                        Log.e("MonetEngine", "Error exporting settings", exception);
                    }
                }
            });
    ActivityResultLauncher<Intent> startImportActivityIntent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result2 -> {
                if (result2.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result2.getData();
                    if (data == null) return;

                    AlertDialog alertDialog = new AlertDialog.Builder(MonetEngine.this).create();
                    alertDialog.setTitle(MonetEngine.this.getResources().getString(R.string.import_settings_confirmation_title));
                    alertDialog.setMessage(MonetEngine.this.getResources().getString(R.string.import_settings_confirmation_desc));
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, MonetEngine.this.getResources().getString(R.string.btn_positive),
                            (dialog, which) -> {
                                dialog.dismiss();
                                new Handler(Looper.getMainLooper()).post(() -> {
                                    try {
                                        boolean success = importMonetSettings(Prefs.prefs, MonetEngine.this.getContentResolver().openInputStream(data.getData()));
                                        if (success) {
                                            Toast.makeText(MonetEngine.this, MonetEngine.this.getResources().getString(R.string.toast_import_settings_successfull), Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(MonetEngine.this, MonetEngine.this.getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (Exception exception) {
                                        Toast.makeText(MonetEngine.this, MonetEngine.this.getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                                        Log.e("MonetEngine", "Error importing settings", exception);
                                    }
                                });
                            });
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, MonetEngine.this.getResources().getString(R.string.btn_negative),
                            (dialog, which) -> dialog.dismiss());
                    alertDialog.show();
                }
            });


    @Override
    public void onColorSelected(int dialogId, int color) {
        switch (dialogId) {
            case 1:
                isSelectedPrimary = true;
                accentPrimary = String.valueOf(color);
                updatePrimaryColor();
                binding.enableCustomMonet.setVisibility(View.VISIBLE);
                assignCustomColorToPalette();
                colorPickerDialogPrimary.setDialogStyle(R.style.ColorPicker).setColor(Integer.parseInt(accentPrimary)).setDialogType(ColorPickerDialog.TYPE_CUSTOM).setAllowCustom(false).setAllowPresets(true).setDialogId(1).setShowAlphaSlider(false).setShowColorShades(true);
                break;
            case 2:
                isSelectedSecondary = true;
                accentSecondary = String.valueOf(color);
                updateSecondaryColor();
                binding.enableCustomMonet.setVisibility(View.VISIBLE);
                assignCustomColorToPalette();
                colorPickerDialogSecondary.setDialogStyle(R.style.ColorPicker).setColor(Integer.parseInt(accentSecondary)).setDialogType(ColorPickerDialog.TYPE_CUSTOM).setAllowCustom(false).setAllowPresets(true).setDialogId(2).setShowAlphaSlider(false).setShowColorShades(true);
                break;
            case 3:
                GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{color, color});
                gd.setCornerRadius(8 * getResources().getDisplayMetrics().density);
                colorTableRows[selectedChild[0]].getChildAt(selectedChild[1]).setBackground(gd);
                finalPalette.get(0).get(selectedChild[0]).set(selectedChild[1], color);
                finalPalette.get(1).get(selectedChild[0]).set(selectedChild[1], color);
                binding.enableCustomMonet.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void onDialogDismissed(int dialogId) {
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onItemSelected(int dialogId, int selectedIndex) {
        selectedStyle = Arrays.asList(getResources().getStringArray(R.array.monet_style)).get(selectedIndex);
        assignCustomColorToPalette();
        binding.enableCustomMonet.setVisibility(View.VISIBLE);
        binding.selectedMonetStyle.setText(getResources().getString(R.string.opt_selected) + " " + Arrays.asList(getResources().getStringArray(R.array.monet_style)).get(selectedIndex));
    }

    @Override
    protected void onDestroy() {
        if (rd_monet_style != null)
            rd_monet_style.dismiss();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.monet_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemID = item.getItemId();

        if (itemID == R.id.menu_export_settings) {
            importExportSettings(true);
        } else if (itemID == R.id.menu_import_settings) {
            importExportSettings(false);
        }

        return super.onOptionsItemSelected(item);
    }


    @SuppressWarnings("unchecked")
    public boolean importMonetSettings(SharedPreferences sharedPreferences, final @NonNull InputStream inputStream) throws IOException {
        ObjectInputStream objectInputStream = null;
        Map<String, Object> map;
        try {
            objectInputStream = new ObjectInputStream(inputStream);
            map = (Map<String, Object>) objectInputStream.readObject();
        } catch (Exception exception) {
            Log.e("ImportSettings", "Error deserializing preferences", exception);
            return false;
        } finally {
            if (objectInputStream != null) {
                objectInputStream.close();
            }
            inputStream.close();
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();

        for (Map.Entry<String, Object> e : map.entrySet()) {
            if (e.getValue() instanceof Boolean && (e.getKey().contains(MONET_ENGINE_SWITCH) ||
                    e.getKey().contains(MONET_ACCURATE_SHADES)
            )) {
                editor.putBoolean(e.getKey(), (Boolean) e.getValue());
            } else if (e.getValue() instanceof String && (e.getKey().endsWith("_day") ||
                    e.getKey().endsWith("_night") ||
                    e.getKey().contains(MONET_STYLE) ||
                    e.getKey().contains(MONET_PRIMARY_COLOR) ||
                    e.getKey().contains(MONET_SECONDARY_COLOR)
            )) {
                editor.putString(e.getKey(), (String) e.getValue());
            } else if (e.getValue() instanceof Integer && (e.getKey().contains(MONET_PRIMARY_ACCENT_SATURATION) ||
                    e.getKey().contains(MONET_SECONDARY_ACCENT_SATURATION) ||
                    e.getKey().contains(MONET_BACKGROUND_SATURATION) ||
                    e.getKey().contains(MONET_BACKGROUND_LIGHTNESS)
            )) {
                editor.putInt(e.getKey(), (int) e.getValue());
            }
        }

        boolean status = editor.commit();

        try {
            String[][] colors = ColorUtil.getColorNames();
            List<List<List<Object>>> palette = new ArrayList<>();
            String[] statNames = new String[]{"_day", "_night"};

            for (String stat : statNames) {
                List<List<Object>> temp = new ArrayList<>();
                for (String[] types : colors) {
                    List<Object> tmp = new ArrayList<>();
                    for (String color : types) {
                        tmp.add(Integer.parseInt(Objects.requireNonNull(map.get(color + stat)).toString()));
                    }
                    temp.add(tmp);
                }
                palette.add(temp);
            }

            status = status && !MonetEngineManager.enableOverlay(palette, true);

            if (status) {
                Prefs.putBoolean(MONET_ENGINE_SWITCH, true);

                if (Prefs.getBoolean("IconifyComponentQSPBD.overlay")) {
                    OverlayUtil.changeOverlayState("IconifyComponentQSPBD.overlay", false, "IconifyComponentQSPBD.overlay", true);
                } else if (Prefs.getBoolean("IconifyComponentQSPBA.overlay")) {
                    OverlayUtil.changeOverlayState("IconifyComponentQSPBA.overlay", false, "IconifyComponentQSPBA.overlay", true);
                }

                Toast.makeText(MonetEngine.this, getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                binding.enableCustomMonet.setVisibility(View.GONE);
                binding.disableCustomMonet.setVisibility(View.VISIBLE);
            } else
                Toast.makeText(MonetEngine.this, getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
        } catch (Exception exception) {
            Log.e("ImportSettings", "Error building Monet Engine", exception);
        }

        return status;
    }
}