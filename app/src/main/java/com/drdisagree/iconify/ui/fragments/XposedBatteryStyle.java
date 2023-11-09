package com.drdisagree.iconify.ui.fragments;

import static com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_DEFAULT;
import static com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_DEFAULT_LANDSCAPE;
import static com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_DEFAULT_RLANDSCAPE;
import static com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_LANDSCAPE_BATTERYL;
import static com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_LANDSCAPE_BATTERYM;
import static com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_LANDSCAPE_IOS_16;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_BLEND_COLOR;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_CHARGING_COLOR;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_CHARGING_ICON_MARGIN_LEFT;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_CHARGING_ICON_MARGIN_RIGHT;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_CHARGING_ICON_STYLE;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_CHARGING_ICON_SWITCH;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_CHARGING_ICON_WIDTH_HEIGHT;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_DIMENSION;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_FILL_ALPHA;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_FILL_COLOR;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_FILL_GRAD_COLOR;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_HEIGHT;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_HIDE_BATTERY;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_HIDE_PERCENTAGE;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_INSIDE_PERCENTAGE;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_LAYOUT_REVERSE;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_MARGIN_BOTTOM;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_MARGIN_LEFT;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_MARGIN_RIGHT;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_MARGIN_TOP;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_PERIMETER_ALPHA;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_POWERSAVE_FILL_COLOR;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_POWERSAVE_INDICATOR_COLOR;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_RAINBOW_FILL_COLOR;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_STYLE;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_SWAP_PERCENTAGE;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_WIDTH;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.RPrefs;
import com.drdisagree.iconify.databinding.FragmentXposedBatteryStyleBinding;
import com.drdisagree.iconify.databinding.ViewXposedBatteryChargingIconBinding;
import com.drdisagree.iconify.databinding.ViewXposedBatteryColorBinding;
import com.drdisagree.iconify.databinding.ViewXposedBatteryDimensionBinding;
import com.drdisagree.iconify.databinding.ViewXposedBatteryMiscBinding;
import com.drdisagree.iconify.ui.base.BaseFragment;
import com.drdisagree.iconify.ui.dialogs.RadioDialog;
import com.drdisagree.iconify.ui.events.ColorSelectedEvent;
import com.drdisagree.iconify.ui.utils.ViewHelper;
import com.drdisagree.iconify.ui.widgets.ColorPickerWidget;
import com.drdisagree.iconify.xposed.modules.utils.Helpers;
import com.google.android.material.slider.Slider;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Arrays;
import java.util.List;

@SuppressLint("SetTextI18n")
public class XposedBatteryStyle extends BaseFragment implements RadioDialog.RadioDialogListener {

    private static int selectedBatteryStyle = 0, selectedChargingIcon = 0;
    private FragmentXposedBatteryStyleBinding binding;
    private ViewXposedBatteryMiscBinding bindingMiscSettings;
    private ViewXposedBatteryColorBinding bindingCustomColors;
    private ViewXposedBatteryDimensionBinding bindingCustomDimens;
    private ViewXposedBatteryChargingIconBinding bindingChargingIcon;
    private RadioDialog rd_battery_style, rd_charging_icon_style;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentXposedBatteryStyleBinding.inflate(inflater, container, false);
        bindingMiscSettings = ViewXposedBatteryMiscBinding.bind(binding.getRoot());
        bindingCustomColors = ViewXposedBatteryColorBinding.bind(binding.getRoot());
        bindingCustomDimens = ViewXposedBatteryDimensionBinding.bind(binding.getRoot());
        bindingChargingIcon = ViewXposedBatteryChargingIconBinding.bind(binding.getRoot());
        View view = binding.getRoot();

        // Header
        ViewHelper.setHeader(requireContext(), getParentFragmentManager(), binding.header.toolbar, R.string.activity_title_battery_style);

        // Custom battery style
        rd_battery_style = new RadioDialog(requireContext(), 0, RPrefs.getInt(CUSTOM_BATTERY_STYLE, 0));
        rd_battery_style.setRadioDialogListener(this);
        binding.customBatteryStyle.setOnClickListener(v -> rd_battery_style.show(R.string.battery_style_title, R.array.custom_battery_style, binding.selectedCustomBatteryStyle));
        selectedBatteryStyle = rd_battery_style.getSelectedIndex();
        binding.selectedCustomBatteryStyle.setText(Arrays.asList(getResources().getStringArray(R.array.custom_battery_style)).get(selectedBatteryStyle));
        binding.selectedCustomBatteryStyle.setText(getResources().getString(R.string.opt_selected) + ' ' + binding.selectedCustomBatteryStyle.getText().toString().replaceAll(getResources().getString(R.string.opt_selected) + ' ', ""));

        // Apply battery style
        binding.applyBatteryStyle.setOnClickListener(v -> {
            RPrefs.putInt(CUSTOM_BATTERY_STYLE, selectedBatteryStyle);
        });

        miscSettings();
        customColors();
        customDimension();
        customCharginIcon();
        updateLayoutVisibility();

        return view;
    }

    private void miscSettings() {
        // Battery width
        bindingMiscSettings.batteryWidth.setSliderValue(RPrefs.getInt(CUSTOM_BATTERY_WIDTH, 20));
        bindingMiscSettings.batteryWidth.setOnSliderChangeListener((slider, value, fromUser) -> {
            RPrefs.putInt(CUSTOM_BATTERY_WIDTH, (int) slider.getValue());
        });
        bindingMiscSettings.batteryWidth.setOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                if (selectedBatteryStyle < 3) {
                    Helpers.forceReloadUI(getContext());
                }
            }
        });

        // Battery height
        bindingMiscSettings.batteryHeight.setSliderValue(RPrefs.getInt(CUSTOM_BATTERY_HEIGHT, 20));
        bindingMiscSettings.batteryHeight.setOnSliderChangeListener((slider, value, fromUser) -> {
            RPrefs.putInt(CUSTOM_BATTERY_HEIGHT, (int) slider.getValue());
        });
        bindingMiscSettings.batteryHeight.setOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                if (selectedBatteryStyle < 3) {
                    Helpers.forceReloadUI(getContext());
                }
            }
        });

        // Hide percentage
        bindingMiscSettings.hidePercentage.setSwitchChecked(RPrefs.getBoolean(CUSTOM_BATTERY_HIDE_PERCENTAGE, false));
        bindingMiscSettings.hidePercentage.setSwitchChangeListener((buttonView, isSwitchChecked) -> {
            RPrefs.putBoolean(CUSTOM_BATTERY_HIDE_PERCENTAGE, isSwitchChecked);
            updateLayoutVisibility();
        });

        // Inside percentage
        bindingMiscSettings.insidePercentage.setSwitchChecked(RPrefs.getBoolean(CUSTOM_BATTERY_INSIDE_PERCENTAGE, false));
        bindingMiscSettings.insidePercentage.setSwitchChangeListener((buttonView, isSwitchChecked) -> RPrefs.putBoolean(CUSTOM_BATTERY_INSIDE_PERCENTAGE, isSwitchChecked));

        // Hide battery
        bindingMiscSettings.hideBattery.setSwitchChecked(RPrefs.getBoolean(CUSTOM_BATTERY_HIDE_BATTERY, false));
        bindingMiscSettings.hideBattery.setSwitchChangeListener((buttonView, isSwitchChecked) -> {
            RPrefs.putBoolean(CUSTOM_BATTERY_HIDE_BATTERY, isSwitchChecked);
            updateLayoutVisibility();
        });

        // Reverse layout
        bindingMiscSettings.reverseLayout.setSwitchChecked(RPrefs.getBoolean(CUSTOM_BATTERY_SWAP_PERCENTAGE, false));
        bindingMiscSettings.reverseLayout.setSwitchChangeListener((buttonView, isSwitchChecked) -> RPrefs.putBoolean(CUSTOM_BATTERY_SWAP_PERCENTAGE, isSwitchChecked));

        // Rotate layout
        bindingMiscSettings.rotateLayout.setSwitchChecked(RPrefs.getBoolean(CUSTOM_BATTERY_LAYOUT_REVERSE, false));
        bindingMiscSettings.rotateLayout.setSwitchChangeListener((buttonView, isSwitchChecked) -> RPrefs.putBoolean(CUSTOM_BATTERY_LAYOUT_REVERSE, isSwitchChecked));
    }

    private void customColors() {
        // Perimeter alpha
        bindingCustomColors.perimeterAlpha.setSwitchChecked(RPrefs.getBoolean(CUSTOM_BATTERY_PERIMETER_ALPHA, false));
        bindingCustomColors.perimeterAlpha.setSwitchChangeListener((buttonView, isSwitchChecked) -> RPrefs.putBoolean(CUSTOM_BATTERY_PERIMETER_ALPHA, isSwitchChecked));

        // Fill alpha
        bindingCustomColors.fillAlpha.setSwitchChecked(RPrefs.getBoolean(CUSTOM_BATTERY_FILL_ALPHA, false));
        bindingCustomColors.fillAlpha.setSwitchChangeListener((buttonView, isSwitchChecked) -> RPrefs.putBoolean(CUSTOM_BATTERY_FILL_ALPHA, isSwitchChecked));

        // Rainbow color
        bindingCustomColors.rainbowColor.setSwitchChecked(RPrefs.getBoolean(CUSTOM_BATTERY_RAINBOW_FILL_COLOR, false));
        bindingCustomColors.rainbowColor.setSwitchChangeListener((buttonView, isSwitchChecked) -> RPrefs.putBoolean(CUSTOM_BATTERY_RAINBOW_FILL_COLOR, isSwitchChecked));

        // Blend color
        bindingCustomColors.blendColor.setSwitchChecked(RPrefs.getBoolean(CUSTOM_BATTERY_BLEND_COLOR, false));
        bindingCustomColors.blendColor.setSwitchChangeListener((buttonView, isSwitchChecked) -> {
            RPrefs.putBoolean(CUSTOM_BATTERY_BLEND_COLOR, isSwitchChecked);
            updateLayoutVisibility();
        });

        // Fill color picker
        bindingCustomColors.fillColor.setColorPickerListener(
                requireActivity(),
                1,
                RPrefs.getInt(CUSTOM_BATTERY_FILL_COLOR, Color.BLACK),
                true,
                false,
                true
        );

        // Fill gradient color picker
        bindingCustomColors.fillGradientColor.setColorPickerListener(
                requireActivity(),
                2,
                RPrefs.getInt(CUSTOM_BATTERY_FILL_GRAD_COLOR, Color.BLACK),
                true,
                false,
                true
        );

        // Charging fill color picker
        bindingCustomColors.chargingFillColor.setColorPickerListener(
                requireActivity(),
                3,
                RPrefs.getInt(CUSTOM_BATTERY_CHARGING_COLOR, Color.BLACK),
                true,
                false,
                true
        );

        // Powersave fill color picker
        bindingCustomColors.powersaveFillColor.setColorPickerListener(
                requireActivity(),
                4,
                RPrefs.getInt(CUSTOM_BATTERY_POWERSAVE_FILL_COLOR, Color.BLACK),
                true,
                false,
                true
        );

        // Powersave icon color picker
        bindingCustomColors.powersaveIconColor.setColorPickerListener(
                requireActivity(),
                5,
                RPrefs.getInt(CUSTOM_BATTERY_POWERSAVE_INDICATOR_COLOR, Color.BLACK),
                true,
                false,
                true
        );

        updateColorPreview(0, Color.BLACK);
    }

    private void customDimension() {
        // Custom dimensions
        bindingCustomDimens.customDimensions.setSwitchChecked(RPrefs.getBoolean(CUSTOM_BATTERY_DIMENSION, false));
        bindingCustomDimens.customDimensions.setSwitchChangeListener((buttonView, isSwitchChecked) -> {
            RPrefs.putBoolean(CUSTOM_BATTERY_DIMENSION, isSwitchChecked);
            updateLayoutVisibility();
        });

        // Battery margin left
        bindingCustomDimens.batteryMarginLeft.setSliderValue(RPrefs.getInt(CUSTOM_BATTERY_MARGIN_LEFT, 4));
        bindingCustomDimens.batteryMarginLeft.setOnSliderChangeListener((slider, value, fromUser) -> {
            RPrefs.putInt(CUSTOM_BATTERY_MARGIN_LEFT, (int) slider.getValue());
        });

        // Battery margin right
        bindingCustomDimens.batteryMarginRight.setSliderValue(RPrefs.getInt(CUSTOM_BATTERY_MARGIN_RIGHT, 4));
        bindingCustomDimens.batteryMarginRight.setOnSliderChangeListener((slider, value, fromUser) -> {
            RPrefs.putInt(CUSTOM_BATTERY_MARGIN_RIGHT, (int) slider.getValue());
        });

        // Battery margin top
        bindingCustomDimens.batteryMarginTop.setSliderValue(RPrefs.getInt(CUSTOM_BATTERY_MARGIN_TOP, 0));
        bindingCustomDimens.batteryMarginTop.setOnSliderChangeListener((slider, value, fromUser) -> {
            RPrefs.putInt(CUSTOM_BATTERY_MARGIN_TOP, (int) slider.getValue());
        });

        // Battery margin bottom
        bindingCustomDimens.batteryMarginBottom.setSliderValue(RPrefs.getInt(CUSTOM_BATTERY_MARGIN_BOTTOM, 0));
        bindingCustomDimens.batteryMarginBottom.setOnSliderChangeListener((slider, value, fromUser) -> {
            RPrefs.putInt(CUSTOM_BATTERY_MARGIN_BOTTOM, (int) slider.getValue());
        });
    }

    private void customCharginIcon() {
        // Enable charging icon
        bindingChargingIcon.enableChargingIcon.setSwitchChecked(RPrefs.getBoolean(CUSTOM_BATTERY_CHARGING_ICON_SWITCH, false));
        bindingChargingIcon.enableChargingIcon.setSwitchChangeListener((buttonView, isSwitchChecked) -> {
            RPrefs.putBoolean(CUSTOM_BATTERY_CHARGING_ICON_SWITCH, isSwitchChecked);
            updateLayoutVisibility();
        });

        // Charging icon style
        rd_charging_icon_style = new RadioDialog(requireContext(), 1, RPrefs.getInt(CUSTOM_BATTERY_CHARGING_ICON_STYLE, 0));
        rd_charging_icon_style.setRadioDialogListener(this);
        bindingChargingIcon.chargingIconStyleContainer.setOnClickListener(v -> rd_charging_icon_style.show(R.string.charging_icon_style_title, R.array.custom_charging_icon_style, bindingChargingIcon.selectedCustomChargingIconStyle));
        selectedChargingIcon = rd_charging_icon_style.getSelectedIndex();
        bindingChargingIcon.selectedCustomChargingIconStyle.setText(Arrays.asList(getResources().getStringArray(R.array.custom_charging_icon_style)).get(selectedChargingIcon));
        bindingChargingIcon.selectedCustomChargingIconStyle.setText(getResources().getString(R.string.opt_selected) + ' ' + bindingChargingIcon.selectedCustomChargingIconStyle.getText().toString().replaceAll(getResources().getString(R.string.opt_selected) + ' ', ""));

        // Charging icon margin left
        bindingChargingIcon.chargingIconMarginLeft.setSliderValue(RPrefs.getInt(CUSTOM_BATTERY_CHARGING_ICON_MARGIN_LEFT, 1));
        bindingChargingIcon.chargingIconMarginLeft.setOnSliderChangeListener((slider, value, fromUser) -> {
            RPrefs.putInt(CUSTOM_BATTERY_CHARGING_ICON_MARGIN_LEFT, (int) slider.getValue());
        });

        // Charging icon margin right
        bindingChargingIcon.chargingIconMarginRight.setSliderValue(RPrefs.getInt(CUSTOM_BATTERY_CHARGING_ICON_MARGIN_RIGHT, 0));
        bindingChargingIcon.chargingIconMarginRight.setOnSliderChangeListener((slider, value, fromUser) -> {
            RPrefs.putInt(CUSTOM_BATTERY_CHARGING_ICON_MARGIN_RIGHT, (int) slider.getValue());
        });

        // Charging icon size
        bindingChargingIcon.chargingIconSize.setSliderValue(RPrefs.getInt(CUSTOM_BATTERY_CHARGING_ICON_WIDTH_HEIGHT, 14));
        bindingChargingIcon.chargingIconSize.setOnSliderChangeListener((slider, value, fromUser) -> {
            RPrefs.putInt(CUSTOM_BATTERY_CHARGING_ICON_WIDTH_HEIGHT, (int) slider.getValue());
        });
    }

    private void updateLayoutVisibility() {
        int selectedIndex = selectedBatteryStyle;

        List<String> battery_styles = Arrays.asList(getResources().getStringArray(R.array.custom_battery_style));

        boolean showAdvancedCustomizations = selectedIndex >= battery_styles.indexOf(getString(R.string.battery_landscape_battery_a)) &&
                selectedIndex <= battery_styles.indexOf(getString(R.string.battery_landscape_battery_o));
        boolean showColorPickers = bindingCustomColors.blendColor.isSwitchChecked();
        boolean showRainbowBattery = battery_styles.indexOf(getString(R.string.battery_landscape_battery_i)) == selectedIndex ||
                battery_styles.indexOf(getString(R.string.battery_landscape_battery_j)) == selectedIndex;
        boolean showCommonCustomizations = selectedIndex != 0;
        boolean showBatteryDimensions = selectedIndex > 2 && bindingCustomDimens.customDimensions.isSwitchChecked();
        boolean showPercentage = selectedIndex != BATTERY_STYLE_DEFAULT &&
                selectedIndex != BATTERY_STYLE_DEFAULT_LANDSCAPE &&
                selectedIndex != BATTERY_STYLE_DEFAULT_RLANDSCAPE &&
                selectedIndex != BATTERY_STYLE_LANDSCAPE_IOS_16 &&
                selectedIndex != BATTERY_STYLE_LANDSCAPE_BATTERYL &&
                selectedIndex != BATTERY_STYLE_LANDSCAPE_BATTERYM;
        boolean showInsidePercentage = showPercentage && !bindingMiscSettings.hidePercentage.isSwitchChecked();
        boolean showChargingIconCustomization = selectedIndex > 2 && bindingChargingIcon.enableChargingIcon.isSwitchChecked();
        boolean showReverseLayout = selectedIndex > 2 && showInsidePercentage;

        int visibility_advanced = showAdvancedCustomizations ? View.VISIBLE : View.GONE;
        int visibility_colorpickers = showAdvancedCustomizations && showColorPickers ? View.VISIBLE : View.GONE;
        int visibility_rainbow = showAdvancedCustomizations && showRainbowBattery ? View.VISIBLE : View.GONE;
        int visibility_wh = selectedIndex > 2 ? View.VISIBLE : View.GONE;
        int visibility_dimensions = showBatteryDimensions ? View.VISIBLE : View.GONE;
        int visibility_percentage = showPercentage ? View.VISIBLE : View.GONE;
        int visibility_inside_percentage = showInsidePercentage ? View.VISIBLE : View.GONE;
        int visibility_reverse_layout = showReverseLayout ? View.VISIBLE : View.GONE;
        int visibility_charging_icon_switch = selectedIndex > 2 ? View.VISIBLE : View.GONE;
        int visibility_charging_icon_customization = showChargingIconCustomization ? View.VISIBLE : View.GONE;

        // Misc settings
        bindingMiscSettings.batteryWidth.setEnabled(showCommonCustomizations);
        bindingMiscSettings.batteryHeight.setEnabled(showCommonCustomizations);
        bindingMiscSettings.hidePercentage.setVisibility(visibility_percentage);
        bindingMiscSettings.insidePercentage.setVisibility(visibility_inside_percentage);
        bindingMiscSettings.hideBattery.setVisibility(visibility_charging_icon_switch);
        bindingMiscSettings.reverseLayout.setVisibility(visibility_reverse_layout);
        bindingMiscSettings.rotateLayout.setVisibility(visibility_advanced);

        // Custom colors
        bindingCustomColors.perimeterAlpha.setVisibility(visibility_advanced);
        bindingCustomColors.fillAlpha.setVisibility(visibility_advanced);
        bindingCustomColors.rainbowColor.setVisibility(visibility_rainbow);
        bindingCustomColors.blendColor.setVisibility(visibility_advanced);
        bindingCustomColors.colorPickers.setVisibility(visibility_colorpickers);

        // Custom dimensions
        bindingCustomDimens.customDimensions.setVisibility(visibility_wh);
        bindingCustomDimens.batteryMarginLeft.setVisibility(visibility_dimensions);
        bindingCustomDimens.batteryMarginTop.setVisibility(visibility_dimensions);
        bindingCustomDimens.batteryMarginRight.setVisibility(visibility_dimensions);
        bindingCustomDimens.batteryMarginBottom.setVisibility(visibility_dimensions);

        // Custom charging icon
        bindingChargingIcon.enableChargingIcon.setVisibility(visibility_charging_icon_switch);
        bindingChargingIcon.chargingIconCustContainer.setVisibility(visibility_charging_icon_customization);
    }

    private void updateColorPreview(int dialogId, int color) {
        View[] views = {
                bindingCustomColors.fillColor,
                bindingCustomColors.fillGradientColor,
                bindingCustomColors.chargingFillColor,
                bindingCustomColors.powersaveFillColor,
                bindingCustomColors.powersaveIconColor};
        int[] colors = {
                RPrefs.getInt(CUSTOM_BATTERY_FILL_COLOR, Color.BLACK),
                RPrefs.getInt(CUSTOM_BATTERY_FILL_GRAD_COLOR, Color.BLACK),
                RPrefs.getInt(CUSTOM_BATTERY_CHARGING_COLOR, Color.BLACK),
                RPrefs.getInt(CUSTOM_BATTERY_POWERSAVE_FILL_COLOR, Color.BLACK),
                RPrefs.getInt(CUSTOM_BATTERY_POWERSAVE_INDICATOR_COLOR, Color.BLACK)
        };

        if (dialogId == 0) {
            for (int i = 0; i < views.length; i++) {
                ((ColorPickerWidget) views[i]).setPreviewColor(colors[i]);
            }
        } else {
            ((ColorPickerWidget) views[dialogId - 1]).setPreviewColor(color);
        }
    }

    @Override
    public void onItemSelected(int dialogId, int selectedIndex) {
        switch (dialogId) {
            case 0 -> {
                selectedBatteryStyle = selectedIndex;
                binding.selectedCustomBatteryStyle.setText(getResources().getString(R.string.opt_selected) + ' ' + binding.selectedCustomBatteryStyle.getText().toString().replaceAll(getResources().getString(R.string.opt_selected) + ' ', ""));
            }
            case 1 -> {
                selectedChargingIcon = selectedIndex;
                bindingChargingIcon.selectedCustomChargingIconStyle.setText(getResources().getString(R.string.opt_selected) + ' ' + bindingChargingIcon.selectedCustomChargingIconStyle.getText().toString().replaceAll(getResources().getString(R.string.opt_selected) + ' ', ""));
                RPrefs.putInt(CUSTOM_BATTERY_CHARGING_ICON_STYLE, selectedIndex);
            }
        }
        updateLayoutVisibility();
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onColorSelected(ColorSelectedEvent event) {
        String[] prefKeys = {
                CUSTOM_BATTERY_FILL_COLOR,
                CUSTOM_BATTERY_FILL_GRAD_COLOR,
                CUSTOM_BATTERY_CHARGING_COLOR,
                CUSTOM_BATTERY_POWERSAVE_FILL_COLOR,
                CUSTOM_BATTERY_POWERSAVE_INDICATOR_COLOR
        };

        RPrefs.putInt(prefKeys[event.dialogId() - 1], event.selectedColor());
        updateColorPreview(event.dialogId(), event.selectedColor());
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

    @Override
    public void onDestroy() {
        rd_battery_style.dismiss();
        rd_charging_icon_style.dismiss();
        super.onDestroy();
    }
}