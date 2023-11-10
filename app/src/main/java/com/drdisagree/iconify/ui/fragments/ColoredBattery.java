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
import com.drdisagree.iconify.ui.base.BaseFragment;
import com.drdisagree.iconify.ui.utils.ViewHelper;
import com.drdisagree.iconify.utils.overlay.FabricatedUtil;
import com.drdisagree.iconify.utils.overlay.OverlayUtil;

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
        binding.enableColoredBattery.setSwitchChecked(Prefs.getString(COLORED_BATTERY_CHECK, STR_NULL).equals(STR_NULL) ? (OverlayUtil.isOverlayEnabled("IconifyComponentIPSUI2.overlay") || OverlayUtil.isOverlayEnabled("IconifyComponentIPSUI4.overlay")) : Prefs.getBoolean(COLORED_BATTERY_SWITCH));
        binding.enableColoredBattery.setSwitchChangeListener((buttonView, isChecked) -> {
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

        if (!Objects.equals(Prefs.getString(FABRICATED_BATTERY_COLOR_BG), STR_NULL)) {
            colorBackground = Prefs.getString(FABRICATED_BATTERY_COLOR_BG);
        } else {
            colorBackground = String.valueOf(Color.parseColor("#FFF0F0F0"));
        }

        if (!Objects.equals(Prefs.getString(FABRICATED_BATTERY_COLOR_FG), STR_NULL)) {
            colorFilled = Prefs.getString(FABRICATED_BATTERY_COLOR_FG);
        } else {
            colorFilled = String.valueOf(Color.parseColor("#FFF0F0F0"));
        }

        // Battery background color
        binding.batteryBackgroundColor.setColorPickerListener(
                requireActivity(),
                Integer.parseInt(colorBackground),
                true,
                false,
                true
        );
        binding.batteryBackgroundColor.setOnColorSelectedListener(
                color -> {
                    colorBackground = String.valueOf(color);
                    Prefs.putString(FABRICATED_BATTERY_COLOR_BG, colorBackground);
                    FabricatedUtil.buildAndEnableOverlay(SYSTEMUI_PACKAGE, FABRICATED_BATTERY_COLOR_BG, "color", "light_mode_icon_color_dual_tone_background", colorToSpecialHex(Integer.parseInt(colorBackground)));
                }
        );

        // Battery filled color
        binding.batteryFilledColor.setColorPickerListener(
                requireActivity(),
                Integer.parseInt(colorFilled),
                true,
                false,
                true
        );
        binding.batteryFilledColor.setOnColorSelectedListener(
                color -> {
                    colorFilled = String.valueOf(color);
                    Prefs.putString(FABRICATED_BATTERY_COLOR_FG, colorFilled);
                    FabricatedUtil.buildAndEnableOverlay(SYSTEMUI_PACKAGE, FABRICATED_BATTERY_COLOR_FG, "color", "light_mode_icon_color_dual_tone_fill", colorToSpecialHex(Integer.parseInt(colorFilled)));
                }
        );

        return view;
    }
}