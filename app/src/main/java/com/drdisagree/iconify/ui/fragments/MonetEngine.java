package com.drdisagree.iconify.ui.fragments;

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
import static com.drdisagree.iconify.utils.color.ColorSchemeUtil.generateColorPalette;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.databinding.FragmentMonetEngineBinding;
import com.drdisagree.iconify.ui.activities.HomePage;
import com.drdisagree.iconify.ui.base.BaseFragment;
import com.drdisagree.iconify.ui.events.ColorSelectedEvent;
import com.drdisagree.iconify.ui.utils.ViewHelper;
import com.drdisagree.iconify.utils.SystemUtil;
import com.drdisagree.iconify.utils.color.ColorUtil;
import com.drdisagree.iconify.utils.helper.ImportExport;
import com.drdisagree.iconify.utils.overlay.FabricatedUtil;
import com.drdisagree.iconify.utils.overlay.OverlayUtil;
import com.drdisagree.iconify.utils.overlay.manager.MonetEngineManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("deprecation")
@SuppressLint("SetTextI18n")
public class MonetEngine extends BaseFragment {

    private static final int[] colorCodes = {
            0, 10, 50, 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000
    };
    private static String accentPrimary, accentSecondary, selectedStyle;
    private static boolean isSelectedPrimary = false, isSelectedSecondary = false, accurateShades = Prefs.getBoolean(MONET_ACCURATE_SHADES, true);
    private final List<List<List<Object>>> finalPalette = new ArrayList<>();
    private final int[] selectedChild = new int[2];
    int[] monetPrimaryAccentSaturation = new int[]{Prefs.getInt(MONET_PRIMARY_ACCENT_SATURATION, 0)};
    int[] monetSecondaryAccentSaturation = new int[]{Prefs.getInt(MONET_SECONDARY_ACCENT_SATURATION, 0)};
    int[] monetBackgroundSaturation = new int[]{Prefs.getInt(MONET_BACKGROUND_SATURATION, 0)};
    int[] monetBackgroundLightness = new int[]{Prefs.getInt(MONET_BACKGROUND_LIGHTNESS, 0)};
    ActivityResultLauncher<Intent> startExportActivityIntent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result1 -> {
                if (result1.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result1.getData();
                    if (data == null) return;

                    try {
                        ImportExport.exportSettings(Prefs.prefs, Objects.requireNonNull(requireContext().getContentResolver().openOutputStream(Objects.requireNonNull(data.getData()))));
                        Toast.makeText(Iconify.getAppContext(), requireContext().getResources().getString(R.string.toast_export_settings_successfull), Toast.LENGTH_SHORT).show();
                    } catch (Exception exception) {
                        Toast.makeText(Iconify.getAppContext(), requireContext().getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                        Log.e("MonetEngine", "Error exporting settings", exception);
                    }
                }
            });
    private FragmentMonetEngineBinding binding;
    private LinearLayout[] colorTableRows;
    private boolean isDarkMode = SystemUtil.isDarkMode();
    private boolean showApplyButton = false, showDisableButton = false;
    ActivityResultLauncher<Intent> startImportActivityIntent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result2 -> {
                if (result2.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result2.getData();
                    if (data == null) return;

                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle(requireContext().getResources().getString(R.string.import_settings_confirmation_title))
                            .setMessage(requireContext().getResources().getString(R.string.import_settings_confirmation_desc))
                            .setPositiveButton(requireContext().getResources().getString(R.string.btn_positive),
                                    (dialog, which) -> {
                                        dialog.dismiss();

                                        new Handler(Looper.getMainLooper()).post(() -> {
                                            try {
                                                boolean success = importMonetSettings(Prefs.prefs, Objects.requireNonNull(requireContext().getContentResolver().openInputStream(Objects.requireNonNull(data.getData()))));
                                                if (success) {
                                                    Toast.makeText(Iconify.getAppContext(), requireContext().getResources().getString(R.string.toast_import_settings_successfull), Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(Iconify.getAppContext(), requireContext().getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                                                }
                                            } catch (Exception exception) {
                                                Toast.makeText(Iconify.getAppContext(), requireContext().getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                                                Log.e("MonetEngine", "Error importing settings", exception);
                                            }
                                        });
                                    })
                            .setNegativeButton(requireContext().getResources().getString(R.string.btn_negative), (dialog, which) -> dialog.dismiss())
                            .show();
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMonetEngineBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Header
        ViewHelper.setHeader(requireContext(), getParentFragmentManager(), binding.header.toolbar, R.string.activity_title_monet_engine);
        setHasOptionsMenu(true);

        colorTableRows = new LinearLayout[]{
                binding.monetEngine.systemAccent1,
                binding.monetEngine.systemAccent2,
                binding.monetEngine.systemAccent3,
                binding.monetEngine.systemNeutral1,
                binding.monetEngine.systemNeutral2
        };

        isDarkMode = SystemUtil.isDarkMode();
        selectedStyle = Prefs.getString(MONET_STYLE, Iconify.getAppContextLocale().getResources().getString(R.string.monet_tonalspot));

        // Monet Style
        int selectedIndex = Arrays.asList(getResources().getStringArray(R.array.monet_style)).indexOf(selectedStyle);
        binding.monetStyles.setSelectedIndex(selectedIndex);
        binding.monetStyles.setOnItemSelectedListener(index -> {
            selectedStyle = Arrays.asList(getResources().getStringArray(R.array.monet_style)).get(index);
            assignCustomColorsToPalette();
            binding.floatingActionMenu.show();
            showApplyButton = true;
        });

        accentPrimary = Prefs.getString(MONET_PRIMARY_COLOR, String.valueOf(getResources().getColor(isDarkMode ? android.R.color.system_accent1_300 : android.R.color.system_accent1_600, Iconify.getAppContext().getTheme())));
        accentSecondary = Prefs.getString(MONET_SECONDARY_COLOR, String.valueOf(getResources().getColor(isDarkMode ? android.R.color.system_accent3_300 : android.R.color.system_accent3_600, Iconify.getAppContext().getTheme())));

        assignStockColorsToPalette();

        binding.colorAccentPrimary.setColorPickerListener(
                requireActivity(),
                Integer.parseInt(accentPrimary),
                true,
                false,
                true
        );
        binding.colorAccentPrimary.setBeforeColorPickerListener(
                () -> {
                    binding.enableCustomMonet.hide();
                    binding.disableCustomMonet.hide();
                }
        );
        binding.colorAccentPrimary.setOnColorSelectedListener(
                color -> {
                    isSelectedPrimary = true;
                    accentPrimary = String.valueOf(color);
                    binding.floatingActionMenu.show();
                    showApplyButton = true;
                    assignCustomColorsToPalette();
                }
        );

        binding.colorAccentSecondary.setColorPickerListener(
                requireActivity(),
                Integer.parseInt(accentSecondary),
                true,
                false,
                true
        );
        binding.colorAccentSecondary.setBeforeColorPickerListener(
                () -> {
                    binding.enableCustomMonet.hide();
                    binding.disableCustomMonet.hide();
                }
        );
        binding.colorAccentSecondary.setOnColorSelectedListener(
                color -> {
                    isSelectedSecondary = true;
                    accentSecondary = String.valueOf(color);
                    binding.floatingActionMenu.show();
                    showApplyButton = true;
                    assignCustomColorsToPalette();
                }
        );

        // Monet Accurate Shades
        binding.accurateShades.setSwitchChecked(Prefs.getBoolean(MONET_ACCURATE_SHADES, true));
        binding.accurateShades.setSwitchChangeListener((buttonView, isChecked) -> {
            accurateShades = isChecked;
            assignCustomColorsToPalette();
            binding.floatingActionMenu.show();
            binding.enableCustomMonet.hide();
            binding.disableCustomMonet.hide();
            showApplyButton = true;
        });

        // Monet primary accent saturation
        binding.primaryAccentSaturation.setSliderValue(Prefs.getInt(MONET_PRIMARY_ACCENT_SATURATION, 0));

        binding.primaryAccentSaturation.setOnSliderChangeListener((slider, value, fromUser) -> {
            monetPrimaryAccentSaturation[0] = (int) value;
            assignCustomColorsToPalette();
        });

        binding.primaryAccentSaturation.setOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
                binding.enableCustomMonet.hide();
                binding.disableCustomMonet.hide();
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                monetPrimaryAccentSaturation[0] = (int) slider.getValue();
                binding.floatingActionMenu.show();
                showApplyButton = true;
            }
        });

        // Long Click Reset
        binding.primaryAccentSaturation.setResetClickListener(v -> {
            monetPrimaryAccentSaturation[0] = 0;
            assignCustomColorsToPalette();
            binding.floatingActionMenu.show();
            showApplyButton = true;
            return true;
        });

        // Monet secondary accent saturation
        binding.secondaryAccentSaturation.setSliderValue(Prefs.getInt(MONET_SECONDARY_ACCENT_SATURATION, 0));

        binding.secondaryAccentSaturation.setOnSliderChangeListener((slider, value, fromUser) -> {
            monetSecondaryAccentSaturation[0] = (int) value;
            assignCustomColorsToPalette();
        });

        binding.secondaryAccentSaturation.setOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
                binding.enableCustomMonet.hide();
                binding.disableCustomMonet.hide();
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                monetSecondaryAccentSaturation[0] = (int) slider.getValue();
                binding.floatingActionMenu.show();
                showApplyButton = true;
            }
        });

        // Long Click Reset
        binding.secondaryAccentSaturation.setResetClickListener(v -> {
            monetSecondaryAccentSaturation[0] = 0;
            assignCustomColorsToPalette();
            binding.floatingActionMenu.show();
            showApplyButton = true;
            return true;
        });

        // Monet background saturation
        binding.backgroundSaturation.setSliderValue(Prefs.getInt(MONET_BACKGROUND_SATURATION, 0));

        binding.backgroundSaturation.setOnSliderChangeListener((slider, value, fromUser) -> {
            monetBackgroundSaturation[0] = (int) value;
            assignCustomColorsToPalette();
        });

        binding.backgroundSaturation.setOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
                binding.enableCustomMonet.hide();
                binding.disableCustomMonet.hide();
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                monetBackgroundSaturation[0] = (int) slider.getValue();
                binding.floatingActionMenu.show();
                showApplyButton = true;
            }
        });

        // Reset button
        binding.backgroundSaturation.setResetClickListener(v -> {
            monetBackgroundSaturation[0] = 0;
            assignCustomColorsToPalette();
            binding.floatingActionMenu.show();
            binding.enableCustomMonet.hide();
            binding.disableCustomMonet.hide();
            showApplyButton = true;
            return true;
        });

        // Monet background lightness
        binding.backgroundLightness.setSliderValue(Prefs.getInt(MONET_BACKGROUND_LIGHTNESS, 0));

        binding.backgroundLightness.setOnSliderChangeListener((slider, value, fromUser) -> {
            monetBackgroundLightness[0] = (int) value;
            assignCustomColorsToPalette();
        });

        binding.backgroundLightness.setOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
                binding.enableCustomMonet.hide();
                binding.disableCustomMonet.hide();
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                monetBackgroundLightness[0] = (int) slider.getValue();
                binding.floatingActionMenu.show();
                showApplyButton = true;
            }
        });

        // Long Click Reset
        binding.backgroundLightness.setResetClickListener(v -> {
            monetBackgroundLightness[0] = 0;
            assignCustomColorsToPalette();
            binding.floatingActionMenu.show();
            binding.enableCustomMonet.hide();
            binding.disableCustomMonet.hide();
            showApplyButton = true;
            return true;
        });

        // Enable custom colors button
        binding.floatingActionMenu.hide();
        showApplyButton = false;
        binding.enableCustomMonet.setOnClickListener(v -> {
            if (!SystemUtil.hasStoragePermission()) {
                SystemUtil.requestStoragePermission(requireContext());
            } else if (Objects.equals(selectedStyle, STR_NULL)) {
                Toast.makeText(Iconify.getAppContext(), Iconify.getAppContextLocale().getResources().getString(R.string.toast_select_style), Toast.LENGTH_SHORT).show();
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

                new Thread(() -> {
                    try {
                        if (MonetEngineManager.buildOverlay(finalPalette, true)) {
                            hasErroredOut.set(true);
                        }
                    } catch (Exception e) {
                        hasErroredOut.set(true);
                        Log.e("MonetEngine", e.toString());
                    }

                    new Handler(Looper.getMainLooper()).post(() -> {
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
                                Toast.makeText(Iconify.getAppContext(), Iconify.getAppContextLocale().getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();

                                try {
                                    binding.floatingActionMenu.show();
                                    binding.enableCustomMonet.hide();
                                    showApplyButton = false;
                                    showDisableButton = true;
                                } catch (Exception ignored) {
                                }
                            } else
                                Toast.makeText(Iconify.getAppContext(), Iconify.getAppContextLocale().getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                        }, 20);
                    });
                }).start();
            }
        });

        // Disable custom colors button
        if (Prefs.getBoolean(MONET_ENGINE_SWITCH)) {
            binding.floatingActionMenu.show();
            showDisableButton = true;
        } else {
            binding.floatingActionMenu.hide();
            showDisableButton = false;
        }
        binding.disableCustomMonet.setOnClickListener(v -> {
            new Thread(() -> {
                Prefs.putBoolean(MONET_ENGINE_SWITCH, false);
                Prefs.clearPrefs(MONET_PRIMARY_COLOR, MONET_SECONDARY_COLOR);
                OverlayUtil.disableOverlays("IconifyComponentDM.overlay", "IconifyComponentME.overlay");

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    Toast.makeText(Iconify.getAppContext(), Iconify.getAppContextLocale().getResources().getString(R.string.toast_disabled), Toast.LENGTH_SHORT).show();

                    try {
                        if (!showApplyButton) {
                            binding.floatingActionMenu.hide();
                        }
                        binding.disableCustomMonet.hide();
                        showDisableButton = false;
                        isSelectedPrimary = false;
                        isSelectedSecondary = false;
                    } catch (Exception ignored) {
                    }
                }, 2000);
            }).start();
        });

        for (int i = 0; i < colorTableRows.length; i++) {
            for (int j = 0; j < colorTableRows[i].getChildCount(); j++) {
                View child = colorTableRows[i].getChildAt(j);
                int finalI = i;
                int finalJ = j;
                child.setOnClickListener(view1 -> {
                    selectedChild[0] = finalI;
                    selectedChild[1] = finalJ;
                    binding.enableCustomMonet.hide();
                    binding.disableCustomMonet.hide();

                    ((HomePage) requireActivity()).showColorPickerDialog(-1, child.getTag() == null ? Color.WHITE : (Integer) child.getTag(), true, false, true);
                });
            }
        }

        if (!binding.floatingActionMenu.isShown()) {
            binding.enableCustomMonet.hide();
            binding.disableCustomMonet.hide();
        }

        binding.floatingActionMenu.setOnClickListener(view1 -> {
            if (showApplyButton && !binding.enableCustomMonet.isShown()) {
                binding.enableCustomMonet.show();
            } else {
                binding.enableCustomMonet.hide();
            }

            if (showDisableButton && !binding.disableCustomMonet.isShown()) {
                binding.disableCustomMonet.show();
            } else {
                binding.disableCustomMonet.hide();
            }
        });

        return view;
    }

    private void assignStockColorsToPalette() {
        int[][] systemColors = ColorUtil.getSystemColors(requireContext());

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

        for (int i = 0; i < colorTableRows.length; i++) {
            for (int j = 0; j < colorTableRows[i].getChildCount(); j++) {
                colorTableRows[i].getChildAt(j).getBackground().setTint(systemColors[i][j]);
                colorTableRows[i].getChildAt(j).setTag(systemColors[i][j]);

                TextView textView = new TextView(requireContext());
                textView.setText(String.valueOf(colorCodes[j]));
                textView.setRotation(270);
                textView.setTextColor(calculateTextColor(systemColors[i][j]));
                textView.setTextSize(10);
                textView.setAlpha(0.8f);

                ((ViewGroup) colorTableRows[i].getChildAt(j)).addView(textView);
                ((LinearLayout) colorTableRows[i].getChildAt(j)).setGravity(Gravity.CENTER);
            }
        }
    }

    private int calculateTextColor(@ColorInt int color) {
        double darkness = 1 - (0.299 * Color.red(color) +
                0.587 * Color.green(color) +
                0.114 * Color.blue(color)) / 255;

        return darkness < 0.5 ? Color.BLACK : Color.WHITE;
    }

    private void assignCustomColorsToPalette() {
        List<List<Object>> palette = generateColorPalette(requireContext(), selectedStyle, Integer.parseInt(accentPrimary));
        List<List<Object>> palette_night = cloneList(palette);

        if (!Objects.equals(selectedStyle, Iconify.getAppContextLocale().getResources().getString(R.string.monet_monochrome))) {
            // Set primary accent saturation
            for (int i = 0; i <= 1; i++) {
                for (int j = palette.get(i).size() - 2; j >= 1; j--) {
                    int color;

                    if (j == 1)
                        color = ColorUtil.setSaturation(Integer.parseInt(String.valueOf((int) palette.get(i).get(j + 1))), -0.1F);
                    else
                        color = ColorUtil.setSaturation(Integer.parseInt(String.valueOf((int) palette.get(i).get(j))), ((float) monetPrimaryAccentSaturation[0] / 1000.0F) * (Math.min((3.0F - j / 5F), 3.0F)));

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
                    color = ColorUtil.setSaturation(Integer.parseInt(String.valueOf((int) palette.get(i).get(j))), ((float) monetSecondaryAccentSaturation[0] / 1000.0F) * (Math.min((3.0F - j / 5F), 3.0F)));

                palette.get(i).set(j, color);
                palette_night.get(i).set(j, color);
            }
        }

        // Set background saturation
        if (!Objects.equals(selectedStyle, Iconify.getAppContextLocale().getResources().getString(R.string.monet_monochrome))) {
            for (int i = 3; i < palette.size(); i++) {
                for (int j = palette.get(i).size() - 2; j >= 1; j--) {
                    int color;
                    if (j == 1)
                        color = ColorUtil.setSaturation(Integer.parseInt(String.valueOf((int) palette.get(i).get(j + 1))), -0.1F);
                    else
                        color = ColorUtil.setSaturation(Integer.parseInt(String.valueOf((int) palette.get(i).get(j))), ((float) monetBackgroundSaturation[0] / 1000.0F) * (Math.min((3.0F - j / 5F), 3.0F)));

                    palette.get(i).set(j, color);
                    palette_night.get(i).set(j, color);
                }
            }
        }

        // Set background lightness
        for (int i = Objects.equals(selectedStyle, Iconify.getAppContextLocale().getResources().getString(R.string.monet_monochrome)) ? 0 : 3; i < palette.size(); i++) {
            for (int j = 1; j < palette.get(i).size() - 1; j++) {
                int color = ColorUtil.setLightness(Integer.parseInt(String.valueOf((int) palette.get(i).get(j))), (float) monetBackgroundLightness[0] / 1000.0F);

                palette.get(i).set(j, color);
                palette_night.get(i).set(j, color);
            }
        }

        for (int i = 0; i < colorTableRows.length; i++) {
            if (i == 2 && (Prefs.getBoolean(CUSTOM_SECONDARY_COLOR_SWITCH) || isSelectedSecondary) && !Objects.equals(selectedStyle, Iconify.getAppContextLocale().getResources().getString(R.string.monet_monochrome))) {
                Prefs.putBoolean(CUSTOM_SECONDARY_COLOR_SWITCH, true);
                List<List<Object>> secondaryPalette = generateColorPalette(requireContext(), selectedStyle, Integer.parseInt(accentSecondary));

                for (int j = colorTableRows[i].getChildCount() - 1; j >= 0; j--) {
                    int color;

                    if (j == 0 || j == colorTableRows[i].getChildCount() - 1)
                        color = (int) secondaryPalette.get(0).get(j);
                    else if (j == 1)
                        color = ColorUtil.setSaturation(Integer.parseInt(String.valueOf((int) palette.get(i).get(j + 1))), -0.1F);
                    else
                        color = ColorUtil.setSaturation(Integer.parseInt(String.valueOf((int) secondaryPalette.get(0).get(j))), ((float) monetSecondaryAccentSaturation[0] / 1000.0F) * (Math.min((3.0F - j / 5F), 3.0F)));

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

                    colorTableRows[i].getChildAt(j).getBackground().setTint(!isDarkMode ? (int) palette.get(i).get(j) : (int) palette_night.get(i).get(j));
                    colorTableRows[i].getChildAt(j).setTag(!isDarkMode ? palette.get(i).get(j) : palette_night.get(i).get(j));
                    ((TextView) ((ViewGroup) colorTableRows[i].getChildAt(j)).getChildAt(0)).setTextColor(calculateTextColor(!isDarkMode ? (int) palette.get(i).get(j) : (int) palette_night.get(i).get(j)));
                }
            } else {
                for (int j = 0; j < colorTableRows[i].getChildCount(); j++) {
                    try {
                        colorTableRows[i].getChildAt(j).getBackground().setTint(!isDarkMode ? (int) palette.get(i).get(j) : (int) palette_night.get(i).get(j));
                        colorTableRows[i].getChildAt(j).setTag(!isDarkMode ? palette.get(i).get(j) : palette_night.get(i).get(j));
                        ((TextView) ((ViewGroup) colorTableRows[i].getChildAt(j)).getChildAt(0)).setTextColor(calculateTextColor(!isDarkMode ? (int) palette.get(i).get(j) : (int) palette_night.get(i).get(j)));
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
        if (!SystemUtil.hasStoragePermission()) {
            SystemUtil.requestStoragePermission(requireContext());
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

    @SuppressWarnings("unused")
    @Subscribe
    public void onColorSelected(ColorSelectedEvent event) {
        if (event.dialogId() == -1) {
            colorTableRows[selectedChild[0]].getChildAt(selectedChild[1]).getBackground().setTint(event.selectedColor());
            colorTableRows[selectedChild[0]].getChildAt(selectedChild[1]).setTag(event.selectedColor());
            ((TextView) ((ViewGroup) colorTableRows[selectedChild[0]].getChildAt(selectedChild[1])).getChildAt(0)).setTextColor(calculateTextColor(event.selectedColor()));

            finalPalette.get(0).get(selectedChild[0]).set(selectedChild[1], event.selectedColor());
            finalPalette.get(1).get(selectedChild[0]).set(selectedChild[1], event.selectedColor());

            binding.floatingActionMenu.show();
            showApplyButton = true;
        }
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
            if (e.getKey() == null) continue;
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

            status = status && !MonetEngineManager.buildOverlay(palette, true);

            if (status) {
                Prefs.putBoolean(MONET_ENGINE_SWITCH, true);

                if (Prefs.getBoolean("IconifyComponentQSPBD.overlay")) {
                    OverlayUtil.changeOverlayState("IconifyComponentQSPBD.overlay", false, "IconifyComponentQSPBD.overlay", true);
                } else if (Prefs.getBoolean("IconifyComponentQSPBA.overlay")) {
                    OverlayUtil.changeOverlayState("IconifyComponentQSPBA.overlay", false, "IconifyComponentQSPBA.overlay", true);
                }

                Toast.makeText(Iconify.getAppContext(), Iconify.getAppContextLocale().getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                binding.floatingActionMenu.show();
                showApplyButton = false;
                showDisableButton = true;
            } else
                Toast.makeText(Iconify.getAppContext(), Iconify.getAppContextLocale().getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
        } catch (Exception exception) {
            Log.e("ImportSettings", "Error building Monet Engine", exception);
        }

        return status;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.monet_menu, menu);

        super.onCreateOptionsMenu(menu, inflater);
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

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}