package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Const.FRAMEWORK_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.COLOR_ACCENT_PRIMARY;
import static com.drdisagree.iconify.common.Preferences.COLOR_ACCENT_PRIMARY_LIGHT;
import static com.drdisagree.iconify.common.Preferences.COLOR_ACCENT_SECONDARY;
import static com.drdisagree.iconify.common.Preferences.COLOR_ACCENT_SECONDARY_LIGHT;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_ACCENT;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_PRIMARY_COLOR_SWITCH;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_SECONDARY_COLOR_SWITCH;
import static com.drdisagree.iconify.common.Preferences.STR_NULL;
import static com.drdisagree.iconify.common.References.ICONIFY_COLOR_ACCENT_PRIMARY;
import static com.drdisagree.iconify.common.References.ICONIFY_COLOR_ACCENT_SECONDARY;
import static com.drdisagree.iconify.utils.ColorUtil.colorToSpecialHex;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.databinding.ActivityBasicColorsBinding;
import com.drdisagree.iconify.ui.utils.ViewBindingHelpers;
import com.drdisagree.iconify.utils.FabricatedUtil;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import java.util.List;
import java.util.Objects;

public class BasicColors extends BaseActivity implements ColorPickerDialogListener {

    public static List<String> EnabledOverlays = OverlayUtil.getEnabledOverlayList();
    private static boolean isSelectedPrimary = false, isSelectedSecondary = false;
    private static String accentPrimary, accentSecondary;
    private ActivityBasicColorsBinding binding;
    private ColorPickerDialog.Builder colorPickerDialogPrimary, colorPickerDialogSecondary;

    public static void applyPrimaryColors() {
        FabricatedUtil.buildAndEnableOverlays(
                new Object[]{FRAMEWORK_PACKAGE, COLOR_ACCENT_PRIMARY, "color", "holo_blue_light", colorToSpecialHex(Integer.parseInt(Prefs.getString(COLOR_ACCENT_PRIMARY)))},
                new Object[]{FRAMEWORK_PACKAGE, COLOR_ACCENT_PRIMARY_LIGHT, "color", "holo_blue_dark", colorToSpecialHex(Integer.parseInt(Prefs.getString(COLOR_ACCENT_PRIMARY)))}
        );
    }

    public static void applySecondaryColors() {
        FabricatedUtil.buildAndEnableOverlays(
                new Object[]{FRAMEWORK_PACKAGE, COLOR_ACCENT_SECONDARY, "color", "holo_green_light", colorToSpecialHex(Integer.parseInt(Prefs.getString(COLOR_ACCENT_SECONDARY)))},
                new Object[]{FRAMEWORK_PACKAGE, COLOR_ACCENT_SECONDARY_LIGHT, "color", "holo_green_dark", colorToSpecialHex(Integer.parseInt(Prefs.getString(COLOR_ACCENT_SECONDARY)))}
        );
    }

    public static void disableAccentColors() {
        Prefs.putBoolean(CUSTOM_ACCENT, false);
        Prefs.clearPref(CUSTOM_PRIMARY_COLOR_SWITCH);
        Prefs.clearPref(CUSTOM_SECONDARY_COLOR_SWITCH);
        Prefs.clearPref(COLOR_ACCENT_PRIMARY);
        Prefs.clearPref(COLOR_ACCENT_PRIMARY_LIGHT);
        Prefs.clearPref(COLOR_ACCENT_SECONDARY);
        Prefs.clearPref(COLOR_ACCENT_SECONDARY_LIGHT);

        FabricatedUtil.disableOverlays(COLOR_ACCENT_PRIMARY, COLOR_ACCENT_PRIMARY_LIGHT, COLOR_ACCENT_SECONDARY, COLOR_ACCENT_SECONDARY_LIGHT);
    }

    public static void applyDefaultColors() {
        applyDefaultPrimaryColors();
        applyDefaultSecondaryColors();
    }

    public static void applyDefaultPrimaryColors() {
        FabricatedUtil.buildAndEnableOverlays(
                new Object[]{FRAMEWORK_PACKAGE, COLOR_ACCENT_PRIMARY, "color", "holo_blue_light", ICONIFY_COLOR_ACCENT_PRIMARY},
                new Object[]{FRAMEWORK_PACKAGE, COLOR_ACCENT_PRIMARY_LIGHT, "color", "holo_blue_dark", ICONIFY_COLOR_ACCENT_PRIMARY}
        );
    }

    public static void applyDefaultSecondaryColors() {
        FabricatedUtil.buildAndEnableOverlays(
                new Object[]{FRAMEWORK_PACKAGE, COLOR_ACCENT_SECONDARY, "color", "holo_green_light", ICONIFY_COLOR_ACCENT_SECONDARY},
                new Object[]{FRAMEWORK_PACKAGE, COLOR_ACCENT_SECONDARY_LIGHT, "color", "holo_green_dark", ICONIFY_COLOR_ACCENT_SECONDARY}
        );
    }

    private static boolean shouldUseDefaultColors() {
        return OverlayUtil.isOverlayDisabled(EnabledOverlays, "IconifyComponentAMAC.overlay") && OverlayUtil.isOverlayDisabled(EnabledOverlays, "IconifyComponentAMGC.overlay") && OverlayUtil.isOverlayDisabled(EnabledOverlays, "IconifyComponentME.overlay");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBasicColorsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Header
        ViewBindingHelpers.setHeader(this, binding.header.collapsingToolbar, binding.header.toolbar, R.string.activity_title_basic_colors);

        if (!Objects.equals(Prefs.getString(COLOR_ACCENT_PRIMARY), STR_NULL))
            accentPrimary = Prefs.getString(COLOR_ACCENT_PRIMARY);
        else
            accentPrimary = String.valueOf(getResources().getColor(android.R.color.holo_blue_light, getTheme()));

        if (!Objects.equals(Prefs.getString(COLOR_ACCENT_SECONDARY), STR_NULL))
            accentSecondary = Prefs.getString(COLOR_ACCENT_SECONDARY);
        else
            accentSecondary = String.valueOf(getResources().getColor(android.R.color.holo_green_light, getTheme()));

        updatePrimaryColor();
        updateSecondaryColor();

        // Primary and Secondary color
        colorPickerDialogPrimary = ColorPickerDialog.newBuilder();
        colorPickerDialogSecondary = ColorPickerDialog.newBuilder();

        colorPickerDialogPrimary.setDialogStyle(R.style.ColorPicker).setColor(Integer.parseInt(accentPrimary)).setDialogType(ColorPickerDialog.TYPE_CUSTOM).setAllowCustom(false).setAllowPresets(true).setDialogId(1).setShowAlphaSlider(false).setShowColorShades(true);
        colorPickerDialogSecondary.setDialogStyle(R.style.ColorPicker).setColor(Integer.parseInt(accentSecondary)).setDialogType(ColorPickerDialog.TYPE_CUSTOM).setAllowCustom(false).setAllowPresets(true).setDialogId(2).setShowAlphaSlider(false).setShowColorShades(true);

        binding.previewColoraccentprimary.setOnClickListener(v -> colorPickerDialogPrimary.show(BasicColors.this));

        binding.previewColoraccentsecondary.setOnClickListener(v -> colorPickerDialogSecondary.show(BasicColors.this));

        // Enable custom colors
        binding.enableCustomColor.setOnClickListener(v -> {
            binding.enableCustomColor.setVisibility(View.GONE);
            Runnable runnable = () -> {
                applyMonetColors();

                runOnUiThread(() -> {
                    Prefs.putBoolean(CUSTOM_ACCENT, true);

                    new Handler(Looper.getMainLooper()).postDelayed(() -> Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show(), 2000);
                });
            };
            Thread thread = new Thread(runnable);
            thread.start();
        });

        // Disable custom colors
        binding.disableCustomColor.setVisibility(Prefs.getBoolean(CUSTOM_ACCENT, false) ? View.VISIBLE : View.GONE);

        binding.disableCustomColor.setOnClickListener(v -> {
            binding.disableCustomColor.setVisibility(View.GONE);
            Runnable runnable = () -> {
                disableAccentColors();
                if (shouldUseDefaultColors()) {
                    applyDefaultColors();
                }

                runOnUiThread(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_disabled), Toast.LENGTH_SHORT).show(), 2000));
            };
            Thread thread = new Thread(runnable);
            thread.start();
        });
    }

    @Override
    public void onColorSelected(int dialogId, int color) {
        switch (dialogId) {
            case 1:
                isSelectedPrimary = true;
                accentPrimary = String.valueOf(color);
                updatePrimaryColor();
                binding.enableCustomColor.setVisibility(View.VISIBLE);
                Prefs.putBoolean(CUSTOM_PRIMARY_COLOR_SWITCH, true);
                colorPickerDialogPrimary.setDialogStyle(R.style.ColorPicker).setColor(Integer.parseInt(accentPrimary)).setDialogType(ColorPickerDialog.TYPE_CUSTOM).setAllowCustom(false).setAllowPresets(true).setDialogId(1).setShowAlphaSlider(false).setShowColorShades(true);
                break;
            case 2:
                isSelectedSecondary = true;
                accentSecondary = String.valueOf(color);
                updateSecondaryColor();
                binding.enableCustomColor.setVisibility(View.VISIBLE);
                Prefs.putBoolean(CUSTOM_SECONDARY_COLOR_SWITCH, true);
                colorPickerDialogSecondary.setDialogStyle(R.style.ColorPicker).setColor(Integer.parseInt(accentSecondary)).setDialogType(ColorPickerDialog.TYPE_CUSTOM).setAllowCustom(false).setAllowPresets(true).setDialogId(2).setShowAlphaSlider(false).setShowColorShades(true);
                break;
        }
    }

    private void updatePrimaryColor() {
        GradientDrawable gd;
        gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{Integer.parseInt(accentPrimary), Integer.parseInt(accentPrimary)});
        gd.setCornerRadius(getResources().getDimension(R.dimen.preview_color_picker_radius) * getResources().getDisplayMetrics().density);
        binding.previewColorPickerPrimary.setBackground(gd);

        gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{Integer.parseInt(accentPrimary), Integer.parseInt(accentSecondary)});
        gd.setCornerRadius(24 * getResources().getDisplayMetrics().density);
        binding.colorPreviewLarge.setBackground(gd);
    }

    private void updateSecondaryColor() {
        GradientDrawable gd;
        gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{Integer.parseInt(accentSecondary), Integer.parseInt(accentSecondary)});
        gd.setCornerRadius(getResources().getDimension(R.dimen.preview_color_picker_radius) * getResources().getDisplayMetrics().density);
        binding.previewColorPickerSecondary.setBackground(gd);

        gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{Integer.parseInt(accentPrimary), Integer.parseInt(accentSecondary)});
        gd.setCornerRadius(24 * getResources().getDisplayMetrics().density);
        binding.colorPreviewLarge.setBackground(gd);
    }

    private void applyMonetColors() {
        Prefs.putBoolean(CUSTOM_ACCENT, true);

        if (isSelectedPrimary) {
            Prefs.putString(COLOR_ACCENT_PRIMARY, accentPrimary);
            Prefs.putString(COLOR_ACCENT_PRIMARY_LIGHT, accentPrimary);
        }

        if (isSelectedSecondary) {
            Prefs.putString(COLOR_ACCENT_SECONDARY, accentSecondary);
            Prefs.putString(COLOR_ACCENT_SECONDARY_LIGHT, accentSecondary);
        }

        if (isSelectedPrimary) applyPrimaryColors();

        if (isSelectedSecondary) applySecondaryColors();
    }

    @Override
    public void onDialogDismissed(int dialogId) {
    }
}