package com.drdisagree.iconify.ui.fragments;

import static com.drdisagree.iconify.common.Const.FRAMEWORK_PACKAGE;
import static com.drdisagree.iconify.common.Const.SWITCH_ANIMATION_DELAY;
import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.COLORED_BATTERY_CHECK;
import static com.drdisagree.iconify.common.Preferences.COLORED_BATTERY_SWITCH;
import static com.drdisagree.iconify.common.Preferences.STR_NULL;
import static com.drdisagree.iconify.common.References.FABRICATED_BATTERY_COLOR_BG;
import static com.drdisagree.iconify.common.References.FABRICATED_BATTERY_COLOR_FG;
import static com.drdisagree.iconify.common.References.FABRICATED_COLORED_BATTERY;
import static com.drdisagree.iconify.utils.color.ColorUtil.colorToSpecialHex;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.databinding.FragmentColoredBatteryBinding;
import com.drdisagree.iconify.ui.activities.HomePage;
import com.drdisagree.iconify.ui.base.BaseFragment;
import com.drdisagree.iconify.ui.events.ColorSelectedEvent;
import com.drdisagree.iconify.ui.utils.ViewHelper;
import com.drdisagree.iconify.utils.overlay.FabricatedUtil;
import com.drdisagree.iconify.utils.overlay.OverlayUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Objects;

public class ColoredBattery extends BaseFragment {

    private static String colorBackground, colorFilled;
    private FragmentColoredBatteryBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentColoredBatteryBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Header
        ViewHelper.setHeader(requireContext(), getParentFragmentManager(), binding.header.toolbar, R.string.activity_title_colored_battery);

        // Enable colored battery
        binding.enableColoredBattery.setChecked(Prefs.getString(COLORED_BATTERY_CHECK, STR_NULL).equals(STR_NULL) ? (OverlayUtil.isOverlayEnabled("IconifyComponentIPSUI2.overlay") || OverlayUtil.isOverlayEnabled("IconifyComponentIPSUI4.overlay")) : Prefs.getBoolean(COLORED_BATTERY_SWITCH));
        binding.enableColoredBattery.setOnCheckedChangeListener((buttonView, isChecked) -> {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (isChecked) {
                    Prefs.putString(COLORED_BATTERY_CHECK, "On");
                    FabricatedUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, FABRICATED_COLORED_BATTERY, "bool", "config_batterymeterDualTone", "1");
                } else {
                    Prefs.putString(COLORED_BATTERY_CHECK, "Off");
                    FabricatedUtil.disableOverlay(FABRICATED_COLORED_BATTERY);
                    FabricatedUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, FABRICATED_COLORED_BATTERY, "bool", "config_batterymeterDualTone", "0");

                    if (!Objects.equals(Prefs.getString(FABRICATED_BATTERY_COLOR_BG), STR_NULL))
                        FabricatedUtil.disableOverlay(FABRICATED_BATTERY_COLOR_BG);

                    if (!Objects.equals(Prefs.getString(FABRICATED_BATTERY_COLOR_FG), STR_NULL))
                        FabricatedUtil.disableOverlay(FABRICATED_BATTERY_COLOR_FG);
                }

                if (OverlayUtil.isOverlayEnabled("IconifyComponentIPSUI2.overlay"))
                    Toast.makeText(Iconify.getAppContext(), Iconify.getAppContextLocale().getResources().getString(R.string.toast_lorn_colored_battery), Toast.LENGTH_SHORT).show();
                else if (OverlayUtil.isOverlayEnabled("IconifyComponentIPSUI4.overlay"))
                    Toast.makeText(Iconify.getAppContext(), Iconify.getAppContextLocale().getResources().getString(R.string.toast_plumpy_colored_battery), Toast.LENGTH_SHORT).show();

                Prefs.putBoolean(COLORED_BATTERY_SWITCH, isChecked);
            }, SWITCH_ANIMATION_DELAY);
        });
        binding.enableColoredBatteryContainer.setOnClickListener(v -> binding.enableColoredBattery.toggle());

        if (!Objects.equals(Prefs.getString(FABRICATED_BATTERY_COLOR_BG), STR_NULL))
            colorBackground = Prefs.getString(FABRICATED_BATTERY_COLOR_BG);
        else colorBackground = String.valueOf(Color.parseColor("#FFF0F0F0"));

        if (!Objects.equals(Prefs.getString(FABRICATED_BATTERY_COLOR_FG), STR_NULL))
            colorFilled = Prefs.getString(FABRICATED_BATTERY_COLOR_FG);
        else colorFilled = String.valueOf(Color.parseColor("#FFF0F0F0"));

        // Battery background and filled color
        binding.batteryBackgroundColor.setOnClickListener(v -> ((HomePage) requireActivity()).showColorPickerDialog(1, Integer.parseInt(colorBackground), true, false, true));
        binding.batteryFilledColor.setOnClickListener(v -> ((HomePage) requireActivity()).showColorPickerDialog(2, Integer.parseInt(colorBackground), true, false, true));

        updateColorPreview();

        return view;
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onColorSelected(ColorSelectedEvent event) {
        switch (event.dialogId()) {
            case 1 -> {
                colorBackground = String.valueOf(event.selectedColor());
                Prefs.putString(FABRICATED_BATTERY_COLOR_BG, colorBackground);
                updateColorPreview();
                FabricatedUtil.buildAndEnableOverlay(SYSTEMUI_PACKAGE, FABRICATED_BATTERY_COLOR_BG, "color", "light_mode_icon_color_dual_tone_background", colorToSpecialHex(Integer.parseInt(colorBackground)));
            }
            case 2 -> {
                colorFilled = String.valueOf(event.selectedColor());
                Prefs.putString(FABRICATED_BATTERY_COLOR_FG, colorFilled);
                updateColorPreview();
                FabricatedUtil.buildAndEnableOverlay(SYSTEMUI_PACKAGE, FABRICATED_BATTERY_COLOR_FG, "color", "light_mode_icon_color_dual_tone_fill", colorToSpecialHex(Integer.parseInt(colorFilled)));
            }
        }
    }

    private void updateColorPreview() {
        GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{Integer.parseInt(colorBackground), Integer.parseInt(colorBackground)});
        gd.setCornerRadius(Iconify.getAppContextLocale().getResources().getDimension(R.dimen.preview_color_picker_radius) * Iconify.getAppContextLocale().getResources().getDisplayMetrics().density);
        binding.previewColorPickerBackground.setBackground(gd);

        gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{Integer.parseInt(colorFilled), Integer.parseInt(colorFilled)});
        gd.setCornerRadius(Iconify.getAppContextLocale().getResources().getDimension(R.dimen.preview_color_picker_radius) * Iconify.getAppContextLocale().getResources().getDisplayMetrics().density);
        binding.previewColorPickerFill.setBackground(gd);
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