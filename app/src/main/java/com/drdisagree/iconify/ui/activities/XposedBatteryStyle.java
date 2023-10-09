package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Const.SWITCH_ANIMATION_DELAY;
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
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_HIDE_PERCENTAGE;
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
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.RPrefs;
import com.drdisagree.iconify.databinding.ActivityXposedBatteryStyleBinding;
import com.drdisagree.iconify.databinding.ViewXposedBatteryChargingIconBinding;
import com.drdisagree.iconify.databinding.ViewXposedBatteryColorBinding;
import com.drdisagree.iconify.databinding.ViewXposedBatteryDimensionBinding;
import com.drdisagree.iconify.databinding.ViewXposedBatteryMiscBinding;
import com.drdisagree.iconify.ui.utils.ViewHelper;
import com.drdisagree.iconify.ui.views.RadioDialog;
import com.drdisagree.iconify.utils.SystemUtil;
import com.google.android.material.slider.Slider;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import java.util.Arrays;
import java.util.List;

@SuppressLint("SetTextI18n")
public class XposedBatteryStyle extends BaseActivity implements RadioDialog.RadioDialogListener, ColorPickerDialogListener {

    private static int selectedBatteryStyle = 0, selectedChargingIcon = 0;
    private ActivityXposedBatteryStyleBinding binding;
    private ViewXposedBatteryMiscBinding bindingMiscSettings;
    private ViewXposedBatteryColorBinding bindingCustomColors;
    private ViewXposedBatteryDimensionBinding bindingCustomDimens;
    private ViewXposedBatteryChargingIconBinding bindingChargingIcon;
    private RadioDialog rd_battery_style, rd_charging_icon_style;
    private ColorPickerDialog.Builder fillColorPicker, fillGradColorPicker, chargingFillColorPicker, powersaveFillColorPicker, powersaveIconColorPicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityXposedBatteryStyleBinding.inflate(getLayoutInflater());
        bindingMiscSettings = ViewXposedBatteryMiscBinding.bind(binding.getRoot());
        bindingCustomColors = ViewXposedBatteryColorBinding.bind(binding.getRoot());
        bindingCustomDimens = ViewXposedBatteryDimensionBinding.bind(binding.getRoot());
        bindingChargingIcon = ViewXposedBatteryChargingIconBinding.bind(binding.getRoot());
        setContentView(binding.getRoot());

        // Header
        ViewHelper.setHeader(this, binding.header.toolbar, R.string.activity_title_battery_style);

        // Custom battery style
        rd_battery_style = new RadioDialog(this, 0, RPrefs.getInt(CUSTOM_BATTERY_STYLE, 0));
        rd_battery_style.setRadioDialogListener(this);
        binding.customBatteryStyle.setOnClickListener(v -> rd_battery_style.show(R.string.battery_style_title, R.array.custom_battery_style, binding.selectedCustomBatteryStyle));
        selectedBatteryStyle = rd_battery_style.getSelectedIndex();
        binding.selectedCustomBatteryStyle.setText(Arrays.asList(getResources().getStringArray(R.array.custom_battery_style)).get(selectedBatteryStyle));
        binding.selectedCustomBatteryStyle.setText(getResources().getString(R.string.opt_selected) + ' ' + binding.selectedCustomBatteryStyle.getText().toString().replaceAll(getResources().getString(R.string.opt_selected) + ' ', ""));

        // Apply battery style
        binding.applyBatteryStyle.setOnClickListener(v -> {
            RPrefs.putInt(CUSTOM_BATTERY_STYLE, selectedBatteryStyle);
            new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::handleSystemUIRestart, SWITCH_ANIMATION_DELAY);
        });

        miscSettings();
        customColors();
        customDimension();
        customCharginIcon();
        updateLayoutVisibility();
    }

    private void miscSettings() {
        // Battery width
        final int[] batteryWidth = {RPrefs.getInt(CUSTOM_BATTERY_WIDTH, 20)};
        bindingMiscSettings.batteryWidthOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + batteryWidth[0] + "dp");
        bindingMiscSettings.batteryWidthSeekbar.setValue(RPrefs.getInt(CUSTOM_BATTERY_WIDTH, 20));
        bindingMiscSettings.batteryWidthSeekbar.addOnChangeListener((slider, value, fromUser) -> {
            batteryWidth[0] = (int) slider.getValue();
            bindingMiscSettings.batteryWidthOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + batteryWidth[0] + "dp");
            RPrefs.putInt(CUSTOM_BATTERY_WIDTH, batteryWidth[0]);
        });
        bindingMiscSettings.batteryWidthSeekbar.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                if (selectedBatteryStyle < 3) {
                    new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::handleSystemUIRestart, SWITCH_ANIMATION_DELAY);
                }
            }
        });

        // Battery height
        final int[] batteryHeight = {RPrefs.getInt(CUSTOM_BATTERY_HEIGHT, 20)};
        bindingMiscSettings.batteryHeightOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + batteryHeight[0] + "dp");
        bindingMiscSettings.batteryHeightSeekbar.setValue(RPrefs.getInt(CUSTOM_BATTERY_HEIGHT, 20));
        bindingMiscSettings.batteryHeightSeekbar.addOnChangeListener((slider, value, fromUser) -> {
            batteryHeight[0] = (int) slider.getValue();
            bindingMiscSettings.batteryHeightOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + batteryHeight[0] + "dp");
            RPrefs.putInt(CUSTOM_BATTERY_HEIGHT, batteryHeight[0]);
        });
        bindingMiscSettings.batteryHeightSeekbar.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                if (selectedBatteryStyle < 3) {
                    new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::handleSystemUIRestart, SWITCH_ANIMATION_DELAY);
                }
            }
        });

        // Hide percentage
        bindingMiscSettings.hidePercentage.setChecked(RPrefs.getBoolean(CUSTOM_BATTERY_HIDE_PERCENTAGE, false));
        bindingMiscSettings.hidePercentage.setOnCheckedChangeListener((buttonView, isChecked) -> RPrefs.putBoolean(CUSTOM_BATTERY_HIDE_PERCENTAGE, isChecked));
        bindingMiscSettings.hidePercentageContainer.setOnClickListener(v -> {
            if (bindingMiscSettings.hidePercentage.isEnabled()) {
                bindingMiscSettings.hidePercentage.toggle();
            }
        });

        // Reverse layout
        bindingMiscSettings.reverseLayout.setChecked(RPrefs.getBoolean(CUSTOM_BATTERY_SWAP_PERCENTAGE, false));
        bindingMiscSettings.reverseLayout.setOnCheckedChangeListener((buttonView, isChecked) -> RPrefs.putBoolean(CUSTOM_BATTERY_SWAP_PERCENTAGE, isChecked));
        bindingMiscSettings.reverseLayoutContainer.setOnClickListener(v -> {
            if (bindingMiscSettings.reverseLayout.isEnabled()) {
                bindingMiscSettings.reverseLayout.toggle();
            }
        });

        // Rotate layout
        bindingMiscSettings.rotateLayout.setChecked(RPrefs.getBoolean(CUSTOM_BATTERY_LAYOUT_REVERSE, false));
        bindingMiscSettings.rotateLayout.setOnCheckedChangeListener((buttonView, isChecked) -> RPrefs.putBoolean(CUSTOM_BATTERY_LAYOUT_REVERSE, isChecked));
        bindingMiscSettings.rotateLayoutContainer.setOnClickListener(v -> {
            if (bindingMiscSettings.rotateLayout.isEnabled()) {
                bindingMiscSettings.rotateLayout.toggle();
            }
        });
    }

    private void customColors() {
        // Perimeter alpha
        bindingCustomColors.perimeterAlpha.setChecked(RPrefs.getBoolean(CUSTOM_BATTERY_PERIMETER_ALPHA, false));
        bindingCustomColors.perimeterAlpha.setOnCheckedChangeListener((buttonView, isChecked) -> RPrefs.putBoolean(CUSTOM_BATTERY_PERIMETER_ALPHA, isChecked));
        bindingCustomColors.perimeterAlphaContainer.setOnClickListener(v -> {
            if (bindingCustomColors.perimeterAlpha.isEnabled()) {
                bindingCustomColors.perimeterAlpha.toggle();
            }
        });

        // Fill alpha
        bindingCustomColors.fillAlpha.setChecked(RPrefs.getBoolean(CUSTOM_BATTERY_FILL_ALPHA, false));
        bindingCustomColors.fillAlpha.setOnCheckedChangeListener((buttonView, isChecked) -> RPrefs.putBoolean(CUSTOM_BATTERY_FILL_ALPHA, isChecked));
        bindingCustomColors.fillAlphaContainer.setOnClickListener(v -> {
            if (bindingCustomColors.fillAlpha.isEnabled()) {
                bindingCustomColors.fillAlpha.toggle();
            }
        });

        // Rainbow color
        bindingCustomColors.rainbowColor.setChecked(RPrefs.getBoolean(CUSTOM_BATTERY_RAINBOW_FILL_COLOR, false));
        bindingCustomColors.rainbowColor.setOnCheckedChangeListener((buttonView, isChecked) -> RPrefs.putBoolean(CUSTOM_BATTERY_RAINBOW_FILL_COLOR, isChecked));
        bindingCustomColors.rainbowColorContainer.setOnClickListener(v -> {
            if (bindingCustomColors.rainbowColor.isEnabled()) {
                bindingCustomColors.rainbowColor.toggle();
            }
        });

        // Blend color
        bindingCustomColors.blendedColor.setChecked(RPrefs.getBoolean(CUSTOM_BATTERY_BLEND_COLOR, false));
        bindingCustomColors.blendedColor.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(CUSTOM_BATTERY_BLEND_COLOR, isChecked);
            updateLayoutVisibility();
        });
        bindingCustomColors.blendedColorContainer.setOnClickListener(v -> {
            if (bindingCustomColors.blendedColor.isEnabled()) {
                bindingCustomColors.blendedColor.toggle();
            }
        });

        // Fill color picker
        fillColorPicker = ColorPickerDialog.newBuilder();
        fillColorPicker.setDialogStyle(R.style.ColorPicker)
                .setColor(RPrefs.getInt(CUSTOM_BATTERY_FILL_COLOR, Color.BLACK))
                .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                .setAllowCustom(false)
                .setAllowPresets(true)
                .setDialogId(1)
                .setShowAlphaSlider(false)
                .setShowColorShades(true);
        bindingCustomColors.fillColorContainer.setOnClickListener(v -> fillColorPicker.show(this));

        // Fill gradient color picker
        fillGradColorPicker = ColorPickerDialog.newBuilder();
        fillGradColorPicker.setDialogStyle(R.style.ColorPicker)
                .setColor(RPrefs.getInt(CUSTOM_BATTERY_FILL_GRAD_COLOR, Color.BLACK))
                .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                .setAllowCustom(false)
                .setAllowPresets(true)
                .setDialogId(2)
                .setShowAlphaSlider(false)
                .setShowColorShades(true);
        bindingCustomColors.fillGradientColorContainer.setOnClickListener(v -> fillGradColorPicker.show(this));

        // Charging fill color picker
        chargingFillColorPicker = ColorPickerDialog.newBuilder();
        chargingFillColorPicker.setDialogStyle(R.style.ColorPicker)
                .setColor(RPrefs.getInt(CUSTOM_BATTERY_CHARGING_COLOR, Color.BLACK))
                .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                .setAllowCustom(false)
                .setAllowPresets(true)
                .setDialogId(3)
                .setShowAlphaSlider(false)
                .setShowColorShades(true);
        bindingCustomColors.chargingFillColorContainer.setOnClickListener(v -> chargingFillColorPicker.show(this));

        // Powersave fill color picker
        powersaveFillColorPicker = ColorPickerDialog.newBuilder();
        powersaveFillColorPicker.setDialogStyle(R.style.ColorPicker)
                .setColor(RPrefs.getInt(CUSTOM_BATTERY_POWERSAVE_FILL_COLOR, Color.BLACK))
                .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                .setAllowCustom(false)
                .setAllowPresets(true)
                .setDialogId(4)
                .setShowAlphaSlider(false)
                .setShowColorShades(true);
        bindingCustomColors.powersaveFillColorContainer.setOnClickListener(v -> powersaveFillColorPicker.show(this));

        // Powersave icon color picker
        powersaveIconColorPicker = ColorPickerDialog.newBuilder();
        powersaveIconColorPicker.setDialogStyle(R.style.ColorPicker)
                .setColor(RPrefs.getInt(CUSTOM_BATTERY_POWERSAVE_INDICATOR_COLOR, Color.BLACK))
                .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                .setAllowCustom(false)
                .setAllowPresets(true)
                .setDialogId(5)
                .setShowAlphaSlider(false)
                .setShowColorShades(true);
        bindingCustomColors.powersaveIconColorContainer.setOnClickListener(v -> powersaveIconColorPicker.show(this));

        updateColorPreview(0, Color.BLACK);
    }

    private void customDimension() {
        // Custom dimensions
        bindingCustomDimens.customDimensions.setChecked(RPrefs.getBoolean(CUSTOM_BATTERY_DIMENSION, false));
        bindingCustomDimens.customDimensions.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(CUSTOM_BATTERY_DIMENSION, isChecked);
            updateLayoutVisibility();
        });
        bindingCustomDimens.customDimensionsContainer.setOnClickListener(v -> {
            if (bindingCustomDimens.customDimensions.isEnabled()) {
                bindingCustomDimens.customDimensions.toggle();
            }
        });

        // Battery margin left
        final int[] batteryMarginLeft = {RPrefs.getInt(CUSTOM_BATTERY_MARGIN_LEFT, 4)};
        bindingCustomDimens.batteryMarginLeftOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + batteryMarginLeft[0] + "dp");
        bindingCustomDimens.batteryMarginLeftSeekbar.setValue(RPrefs.getInt(CUSTOM_BATTERY_MARGIN_LEFT, 4));
        bindingCustomDimens.batteryMarginLeftSeekbar.addOnChangeListener((slider, value, fromUser) -> {
            batteryMarginLeft[0] = (int) slider.getValue();
            bindingCustomDimens.batteryMarginLeftOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + batteryMarginLeft[0] + "dp");
            RPrefs.putInt(CUSTOM_BATTERY_MARGIN_LEFT, batteryMarginLeft[0]);
        });

        // Battery margin right
        final int[] batteryMarginRight = {RPrefs.getInt(CUSTOM_BATTERY_MARGIN_RIGHT, 4)};
        bindingCustomDimens.batteryMarginRightOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + batteryMarginRight[0] + "dp");
        bindingCustomDimens.batteryMarginRightSeekbar.setValue(RPrefs.getInt(CUSTOM_BATTERY_MARGIN_RIGHT, 4));
        bindingCustomDimens.batteryMarginRightSeekbar.addOnChangeListener((slider, value, fromUser) -> {
            batteryMarginRight[0] = (int) slider.getValue();
            bindingCustomDimens.batteryMarginRightOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + batteryMarginRight[0] + "dp");
            RPrefs.putInt(CUSTOM_BATTERY_MARGIN_RIGHT, batteryMarginRight[0]);
        });

        // Battery margin top
        final int[] batteryMarginTop = {RPrefs.getInt(CUSTOM_BATTERY_MARGIN_TOP, 0)};
        bindingCustomDimens.batteryMarginTopOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + batteryMarginTop[0] + "dp");
        bindingCustomDimens.batteryMarginTopSeekbar.setValue(RPrefs.getInt(CUSTOM_BATTERY_MARGIN_TOP, 0));
        bindingCustomDimens.batteryMarginTopSeekbar.addOnChangeListener((slider, value, fromUser) -> {
            batteryMarginTop[0] = (int) slider.getValue();
            bindingCustomDimens.batteryMarginTopOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + batteryMarginTop[0] + "dp");
            RPrefs.putInt(CUSTOM_BATTERY_MARGIN_TOP, batteryMarginTop[0]);
        });

        // Battery margin bottom
        final int[] batteryMarginBottom = {RPrefs.getInt(CUSTOM_BATTERY_MARGIN_BOTTOM, 0)};
        bindingCustomDimens.batteryMarginBottomOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + batteryMarginBottom[0] + "dp");
        bindingCustomDimens.batteryMarginBottomSeekbar.setValue(RPrefs.getInt(CUSTOM_BATTERY_MARGIN_BOTTOM, 0));
        bindingCustomDimens.batteryMarginBottomSeekbar.addOnChangeListener((slider, value, fromUser) -> {
            batteryMarginBottom[0] = (int) slider.getValue();
            bindingCustomDimens.batteryMarginBottomOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + batteryMarginBottom[0] + "dp");
            RPrefs.putInt(CUSTOM_BATTERY_MARGIN_BOTTOM, batteryMarginBottom[0]);
        });
    }

    private void customCharginIcon() {
        // Enable charging icon
        bindingChargingIcon.enableChargingIcon.setChecked(RPrefs.getBoolean(CUSTOM_BATTERY_CHARGING_ICON_SWITCH, false));
        bindingChargingIcon.enableChargingIcon.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(CUSTOM_BATTERY_CHARGING_ICON_SWITCH, isChecked);
            updateLayoutVisibility();
        });
        bindingChargingIcon.enableChargingIconContainer.setOnClickListener(v -> {
            if (bindingChargingIcon.enableChargingIcon.isEnabled()) {
                bindingChargingIcon.enableChargingIcon.toggle();
            }
        });

        // Charging icon style
        rd_charging_icon_style = new RadioDialog(this, 1, RPrefs.getInt(CUSTOM_BATTERY_CHARGING_ICON_STYLE, 0));
        rd_charging_icon_style.setRadioDialogListener(this);
        bindingChargingIcon.chargingIconStyleContainer.setOnClickListener(v -> rd_charging_icon_style.show(R.string.charging_icon_style_title, R.array.custom_charging_icon_style, bindingChargingIcon.selectedCustomChargingIconStyle));
        selectedChargingIcon = rd_charging_icon_style.getSelectedIndex();
        bindingChargingIcon.selectedCustomChargingIconStyle.setText(Arrays.asList(getResources().getStringArray(R.array.custom_charging_icon_style)).get(selectedChargingIcon));
        bindingChargingIcon.selectedCustomChargingIconStyle.setText(getResources().getString(R.string.opt_selected) + ' ' + bindingChargingIcon.selectedCustomChargingIconStyle.getText().toString().replaceAll(getResources().getString(R.string.opt_selected) + ' ', ""));

        // Charging icon margin left
        final int[] chargingIconMarginLeft = {RPrefs.getInt(CUSTOM_BATTERY_CHARGING_ICON_MARGIN_LEFT, 1)};
        bindingChargingIcon.chargingIconMarginLeftOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + chargingIconMarginLeft[0] + "dp");
        bindingChargingIcon.chargingIconMarginLeftSeekbar.setValue(RPrefs.getInt(CUSTOM_BATTERY_CHARGING_ICON_MARGIN_LEFT, 1));
        bindingChargingIcon.chargingIconMarginLeftSeekbar.addOnChangeListener((slider, value, fromUser) -> {
            chargingIconMarginLeft[0] = (int) slider.getValue();
            bindingChargingIcon.chargingIconMarginLeftOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + chargingIconMarginLeft[0] + "dp");
            RPrefs.putInt(CUSTOM_BATTERY_CHARGING_ICON_MARGIN_LEFT, chargingIconMarginLeft[0]);
        });

        // Charging icon margin right
        final int[] chargingIconMarginRight = {RPrefs.getInt(CUSTOM_BATTERY_CHARGING_ICON_MARGIN_RIGHT, 0)};
        bindingChargingIcon.chargingIconMarginRightOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + chargingIconMarginRight[0] + "dp");
        bindingChargingIcon.chargingIconMarginRightSeekbar.setValue(RPrefs.getInt(CUSTOM_BATTERY_CHARGING_ICON_MARGIN_RIGHT, 0));
        bindingChargingIcon.chargingIconMarginRightSeekbar.addOnChangeListener((slider, value, fromUser) -> {
            chargingIconMarginRight[0] = (int) slider.getValue();
            bindingChargingIcon.chargingIconMarginRightOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + chargingIconMarginRight[0] + "dp");
            RPrefs.putInt(CUSTOM_BATTERY_CHARGING_ICON_MARGIN_RIGHT, chargingIconMarginRight[0]);
        });

        // Charging icon size
        final int[] chargingIconSize = {RPrefs.getInt(CUSTOM_BATTERY_CHARGING_ICON_WIDTH_HEIGHT, 14)};
        bindingChargingIcon.chargingIconSizeOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + chargingIconSize[0] + "dp");
        bindingChargingIcon.chargingIconSizeSeekbar.setValue(RPrefs.getInt(CUSTOM_BATTERY_CHARGING_ICON_WIDTH_HEIGHT, 14));
        bindingChargingIcon.chargingIconSizeSeekbar.addOnChangeListener((slider, value, fromUser) -> {
            chargingIconSize[0] = (int) slider.getValue();
            bindingChargingIcon.chargingIconSizeOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + chargingIconSize[0] + "dp");
            RPrefs.putInt(CUSTOM_BATTERY_CHARGING_ICON_WIDTH_HEIGHT, chargingIconSize[0]);
        });
    }

    private void updateLayoutVisibility() {
        int selectedIndex = selectedBatteryStyle;

        List<String> battery_styles = Arrays.asList(getResources().getStringArray(R.array.custom_battery_style));

        boolean showAdvancedCustomizations = selectedIndex >= battery_styles.indexOf(getString(R.string.battery_landscape_battery_a)) &&
                selectedIndex <= battery_styles.indexOf(getString(R.string.battery_landscape_battery_o));
        boolean showColorPickers = bindingCustomColors.blendedColor.isChecked();
        boolean showRainbowBattery = battery_styles.indexOf(getString(R.string.battery_landscape_battery_i)) == selectedIndex ||
                battery_styles.indexOf(getString(R.string.battery_landscape_battery_j)) == selectedIndex;
        boolean showCommonCustomizations = selectedIndex != 0;
        boolean showBatteryDimensions = selectedIndex > 2 && bindingCustomDimens.customDimensions.isChecked();
        boolean showPercentage = selectedIndex != BATTERY_STYLE_DEFAULT &&
                selectedIndex != BATTERY_STYLE_DEFAULT_LANDSCAPE &&
                selectedIndex != BATTERY_STYLE_DEFAULT_RLANDSCAPE &&
                selectedIndex != BATTERY_STYLE_LANDSCAPE_IOS_16 &&
                selectedIndex != BATTERY_STYLE_LANDSCAPE_BATTERYL &&
                selectedIndex != BATTERY_STYLE_LANDSCAPE_BATTERYM;
        boolean showChargingIconCustomization = selectedIndex > 2 && bindingChargingIcon.enableChargingIcon.isChecked();

        int visibility_advanced = showAdvancedCustomizations ? View.VISIBLE : View.GONE;
        int visibility_colorpickers = showAdvancedCustomizations && showColorPickers ? View.VISIBLE : View.GONE;
        int visibility_rainbow = showAdvancedCustomizations && showRainbowBattery ? View.VISIBLE : View.GONE;
        int visibility_wh = selectedIndex > 2 ? View.VISIBLE : View.GONE;
        int visibility_dimensions = showBatteryDimensions ? View.VISIBLE : View.GONE;
        int visibility_percentage = showPercentage ? View.VISIBLE : View.GONE;
        int visibility_charging_icon_switch = selectedIndex > 2 ? View.VISIBLE : View.GONE;
        int visibility_charging_icon_cust = showChargingIconCustomization ? View.VISIBLE : View.GONE;

        // Misc settings
        bindingMiscSettings.batteryWidthSeekbar.setEnabled(showCommonCustomizations);
        bindingMiscSettings.batteryHeightSeekbar.setEnabled(showCommonCustomizations);
        bindingMiscSettings.hidePercentageContainer.setVisibility(visibility_percentage);
        bindingMiscSettings.reverseLayoutContainer.setVisibility(visibility_advanced);
        bindingMiscSettings.rotateLayoutContainer.setVisibility(visibility_advanced);

        // Custom colors
        bindingCustomColors.perimeterAlphaContainer.setVisibility(visibility_advanced);
        bindingCustomColors.fillAlphaContainer.setVisibility(visibility_advanced);
        bindingCustomColors.rainbowColorContainer.setVisibility(visibility_rainbow);
        bindingCustomColors.blendedColorContainer.setVisibility(visibility_advanced);
        bindingCustomColors.colorPickers.setVisibility(visibility_colorpickers);

        // Custom dimensions
        bindingCustomDimens.customDimensionsContainer.setVisibility(visibility_wh);
        bindingCustomDimens.batteryMarginLeftContainer.setVisibility(visibility_dimensions);
        bindingCustomDimens.batteryMarginTopContainer.setVisibility(visibility_dimensions);
        bindingCustomDimens.batteryMarginRightContainer.setVisibility(visibility_dimensions);
        bindingCustomDimens.batteryMarginBottomContainer.setVisibility(visibility_dimensions);

        // Custom charging icon
        bindingChargingIcon.enableChargingIconContainer.setVisibility(visibility_charging_icon_switch);
        bindingChargingIcon.chargingIconCustContainer.setVisibility(visibility_charging_icon_cust);
    }

    private void updateColorPreview(int dialogId, int color) {
        View[] views = {
                bindingCustomColors.previewFillColor,
                bindingCustomColors.previewFillGradientColor,
                bindingCustomColors.previewChargingFillColor,
                bindingCustomColors.previewPowersaveFillColor,
                bindingCustomColors.previewPowersaveIconColor};
        int[] colors = {
                RPrefs.getInt(CUSTOM_BATTERY_FILL_COLOR, Color.BLACK),
                RPrefs.getInt(CUSTOM_BATTERY_FILL_GRAD_COLOR, Color.BLACK),
                RPrefs.getInt(CUSTOM_BATTERY_CHARGING_COLOR, Color.BLACK),
                RPrefs.getInt(CUSTOM_BATTERY_POWERSAVE_FILL_COLOR, Color.BLACK),
                RPrefs.getInt(CUSTOM_BATTERY_POWERSAVE_INDICATOR_COLOR, Color.BLACK)
        };

        if (dialogId == 0) {
            for (int i = 0; i < views.length; i++) {
                GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{colors[i], colors[i]});
                gd.setCornerRadius(getResources().getDimension(R.dimen.preview_color_picker_radius) * getResources().getDisplayMetrics().density);
                views[i].setBackground(gd);
            }
        } else {
            GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{color, color});
            gd.setCornerRadius(getResources().getDimension(R.dimen.preview_color_picker_radius) * getResources().getDisplayMetrics().density);
            views[dialogId - 1].setBackground(gd);
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

    @Override
    public void onColorSelected(int dialogId, int color) {
        ColorPickerDialog.Builder[] colorPickers = {
                fillColorPicker,
                fillGradColorPicker,
                chargingFillColorPicker,
                powersaveFillColorPicker,
                powersaveIconColorPicker
        };
        String[] prefKeys = {
                CUSTOM_BATTERY_FILL_COLOR,
                CUSTOM_BATTERY_FILL_GRAD_COLOR,
                CUSTOM_BATTERY_CHARGING_COLOR,
                CUSTOM_BATTERY_POWERSAVE_FILL_COLOR,
                CUSTOM_BATTERY_POWERSAVE_INDICATOR_COLOR
        };

        RPrefs.putInt(prefKeys[dialogId - 1], color);
        colorPickers[dialogId - 1].setDialogStyle(R.style.ColorPicker)
                .setColor(color)
                .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                .setAllowCustom(false)
                .setAllowPresets(true)
                .setDialogId(dialogId)
                .setShowAlphaSlider(false)
                .setShowColorShades(true);
        updateColorPreview(dialogId, color);
    }

    @Override
    public void onDialogDismissed(int dialogId) {
    }

    @Override
    public void onDestroy() {
        rd_battery_style.dismiss();
        rd_charging_icon_style.dismiss();
        super.onDestroy();
    }
}