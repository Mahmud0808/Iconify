package com.drdisagree.iconify.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Const.SWITCH_ANIMATION_DELAY
import com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_CIRCLE
import com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_DEFAULT
import com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_DEFAULT_LANDSCAPE
import com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_DEFAULT_RLANDSCAPE
import com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_DOTTED_CIRCLE
import com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_FILLED_CIRCLE
import com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_LANDSCAPE_BATTERYL
import com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_LANDSCAPE_BATTERYM
import com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_LANDSCAPE_IOS_16
import com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_BLEND_COLOR
import com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_CHARGING_COLOR
import com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_CHARGING_ICON_MARGIN_LEFT
import com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_CHARGING_ICON_MARGIN_RIGHT
import com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_CHARGING_ICON_STYLE
import com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_CHARGING_ICON_SWITCH
import com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_CHARGING_ICON_WIDTH_HEIGHT
import com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_DIMENSION
import com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_FILL_ALPHA
import com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_FILL_COLOR
import com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_FILL_GRAD_COLOR
import com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_HEIGHT
import com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_HIDE_BATTERY
import com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_HIDE_PERCENTAGE
import com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_INSIDE_PERCENTAGE
import com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_LAYOUT_REVERSE
import com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_MARGIN_BOTTOM
import com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_MARGIN_LEFT
import com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_MARGIN_RIGHT
import com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_MARGIN_TOP
import com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_PERIMETER_ALPHA
import com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_POWERSAVE_FILL_COLOR
import com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_POWERSAVE_INDICATOR_COLOR
import com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_RAINBOW_FILL_COLOR
import com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_STYLE
import com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_SWAP_PERCENTAGE
import com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_WIDTH
import com.drdisagree.iconify.config.RPrefs.getBoolean
import com.drdisagree.iconify.config.RPrefs.getInt
import com.drdisagree.iconify.config.RPrefs.putBoolean
import com.drdisagree.iconify.config.RPrefs.putInt
import com.drdisagree.iconify.databinding.FragmentXposedBatteryStyleBinding
import com.drdisagree.iconify.databinding.ViewXposedBatteryChargingIconBinding
import com.drdisagree.iconify.databinding.ViewXposedBatteryColorBinding
import com.drdisagree.iconify.databinding.ViewXposedBatteryDimensionBinding
import com.drdisagree.iconify.databinding.ViewXposedBatteryMiscBinding
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.utils.ViewHelper.getBatteryDrawables
import com.drdisagree.iconify.ui.utils.ViewHelper.getChargingIcons
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader
import com.drdisagree.iconify.utils.SystemUtil
import com.google.android.material.slider.Slider


class XposedBatteryStyle : BaseFragment() {

    private lateinit var binding: FragmentXposedBatteryStyleBinding
    private lateinit var bindingMiscSettings: ViewXposedBatteryMiscBinding
    private lateinit var bindingCustomColors: ViewXposedBatteryColorBinding
    private lateinit var bindingCustomDimens: ViewXposedBatteryDimensionBinding
    private lateinit var bindingChargingIcon: ViewXposedBatteryChargingIconBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentXposedBatteryStyleBinding.inflate(inflater, container, false)
        bindingMiscSettings = ViewXposedBatteryMiscBinding.bind(binding.getRoot())
        bindingCustomColors = ViewXposedBatteryColorBinding.bind(binding.getRoot())
        bindingCustomDimens = ViewXposedBatteryDimensionBinding.bind(binding.getRoot())
        bindingChargingIcon = ViewXposedBatteryChargingIconBinding.bind(binding.getRoot())
        val view: View = binding.getRoot()

        // Header
        setHeader(
            requireContext(),
            getParentFragmentManager(),
            binding.header.toolbar,
            R.string.activity_title_battery_style
        )

        // Custom battery style
        binding.customBatteryStyle.setSelectedIndex(getInt(CUSTOM_BATTERY_STYLE, 0))
        binding.customBatteryStyle.setDrawable(getBatteryDrawables(requireContext()))
        binding.customBatteryStyle.setOnItemClickListener { index: Int ->
            selectedBatteryStyle = index
            binding.customBatteryStyle.setCurrentValue(index.toString())
            updateLayoutVisibility()
        }
        selectedBatteryStyle = getInt(CUSTOM_BATTERY_STYLE, 0)

        // Apply battery style
        binding.applyBatteryStyle.setOnClickListener {
            putInt(CUSTOM_BATTERY_STYLE, selectedBatteryStyle)
            Handler(Looper.getMainLooper()).postDelayed(
                { SystemUtil.handleSystemUIRestart() },
                SWITCH_ANIMATION_DELAY
            )
        }

        miscSettings()
        customColors()
        customDimension()
        customChargingIcon()
        updateLayoutVisibility()

        return view
    }

    private fun miscSettings() {
        // Battery width
        bindingMiscSettings.batteryWidth.sliderValue = getInt(CUSTOM_BATTERY_WIDTH, 20)
        bindingMiscSettings.batteryWidth.setOnSliderChangeListener { slider: Slider, _: Float, _: Boolean ->
            putInt(
                CUSTOM_BATTERY_WIDTH,
                slider.value.toInt()
            )
        }
        bindingMiscSettings.batteryWidth.setOnSliderTouchListener(object :
            Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}

            override fun onStopTrackingTouch(slider: Slider) {
                if (selectedBatteryStyle < 3) {
                    Handler(Looper.getMainLooper()).postDelayed(
                        { SystemUtil.handleSystemUIRestart() },
                        SWITCH_ANIMATION_DELAY
                    )
                }
            }
        })

        // Battery height
        bindingMiscSettings.batteryHeight.sliderValue = getInt(CUSTOM_BATTERY_HEIGHT, 20)
        bindingMiscSettings.batteryHeight.setOnSliderChangeListener { slider: Slider, _: Float, _: Boolean ->
            putInt(
                CUSTOM_BATTERY_HEIGHT,
                slider.value.toInt()
            )
        }
        bindingMiscSettings.batteryHeight.setOnSliderTouchListener(object :
            Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}

            override fun onStopTrackingTouch(slider: Slider) {
                if (selectedBatteryStyle < 3) {
                    Handler(Looper.getMainLooper()).postDelayed(
                        { SystemUtil.handleSystemUIRestart() },
                        SWITCH_ANIMATION_DELAY
                    )
                }
            }
        })

        // Hide percentage
        bindingMiscSettings.hidePercentage.isSwitchChecked =
            getBoolean(CUSTOM_BATTERY_HIDE_PERCENTAGE, false)
        bindingMiscSettings.hidePercentage.setSwitchChangeListener { _: CompoundButton?, isSwitchChecked: Boolean ->
            putBoolean(CUSTOM_BATTERY_HIDE_PERCENTAGE, isSwitchChecked)
            updateLayoutVisibility()
        }

        // Inside percentage
        bindingMiscSettings.insidePercentage.isSwitchChecked =
            getBoolean(CUSTOM_BATTERY_INSIDE_PERCENTAGE, false)
        bindingMiscSettings.insidePercentage.setSwitchChangeListener { _: CompoundButton?, isSwitchChecked: Boolean ->
            putBoolean(
                CUSTOM_BATTERY_INSIDE_PERCENTAGE,
                isSwitchChecked
            )
        }

        // Hide battery
        bindingMiscSettings.hideBattery.isSwitchChecked =
            getBoolean(CUSTOM_BATTERY_HIDE_BATTERY, false)
        bindingMiscSettings.hideBattery.setSwitchChangeListener { _: CompoundButton?, isSwitchChecked: Boolean ->
            putBoolean(CUSTOM_BATTERY_HIDE_BATTERY, isSwitchChecked)
            updateLayoutVisibility()
        }

        // Reverse layout
        bindingMiscSettings.reverseLayout.isSwitchChecked =
            getBoolean(CUSTOM_BATTERY_SWAP_PERCENTAGE, false)
        bindingMiscSettings.reverseLayout.setSwitchChangeListener { _: CompoundButton?, isSwitchChecked: Boolean ->
            putBoolean(
                CUSTOM_BATTERY_SWAP_PERCENTAGE,
                isSwitchChecked
            )
        }

        // Rotate layout
        bindingMiscSettings.rotateLayout.isSwitchChecked =
            getBoolean(CUSTOM_BATTERY_LAYOUT_REVERSE, false)
        bindingMiscSettings.rotateLayout.setSwitchChangeListener { _: CompoundButton?, isSwitchChecked: Boolean ->
            putBoolean(
                CUSTOM_BATTERY_LAYOUT_REVERSE,
                isSwitchChecked
            )
        }
    }

    private fun customColors() {
        // Perimeter alpha
        bindingCustomColors.perimeterAlpha.isSwitchChecked =
            getBoolean(CUSTOM_BATTERY_PERIMETER_ALPHA, false)
        bindingCustomColors.perimeterAlpha.setSwitchChangeListener { _: CompoundButton?, isSwitchChecked: Boolean ->
            putBoolean(
                CUSTOM_BATTERY_PERIMETER_ALPHA,
                isSwitchChecked
            )
        }

        // Fill alpha
        bindingCustomColors.fillAlpha.isSwitchChecked =
            getBoolean(CUSTOM_BATTERY_FILL_ALPHA, false)
        bindingCustomColors.fillAlpha.setSwitchChangeListener { _: CompoundButton?, isSwitchChecked: Boolean ->
            putBoolean(
                CUSTOM_BATTERY_FILL_ALPHA,
                isSwitchChecked
            )
        }

        // Rainbow color
        bindingCustomColors.rainbowColor.isSwitchChecked =
            getBoolean(CUSTOM_BATTERY_RAINBOW_FILL_COLOR, false)
        bindingCustomColors.rainbowColor.setSwitchChangeListener { _: CompoundButton?, isSwitchChecked: Boolean ->
            putBoolean(
                CUSTOM_BATTERY_RAINBOW_FILL_COLOR,
                isSwitchChecked
            )
        }

        // Blend color
        bindingCustomColors.blendColor.isSwitchChecked =
            getBoolean(CUSTOM_BATTERY_BLEND_COLOR, false)
        bindingCustomColors.blendColor.setSwitchChangeListener { _: CompoundButton?, isSwitchChecked: Boolean ->
            putBoolean(CUSTOM_BATTERY_BLEND_COLOR, isSwitchChecked)
            updateLayoutVisibility()
        }

        // Fill color picker
        bindingCustomColors.fillColor.setColorPickerListener(
            activity = requireActivity(),
            defaultColor = getInt(CUSTOM_BATTERY_FILL_COLOR, Color.BLACK),
            showPresets = true,
            showAlphaSlider = false,
            showColorShades = true
        )
        bindingCustomColors.fillColor.setOnColorSelectedListener { color: Int ->
            putInt(
                CUSTOM_BATTERY_FILL_COLOR,
                color
            )
        }

        // Fill gradient color picker
        bindingCustomColors.fillGradientColor.setColorPickerListener(
            activity = requireActivity(),
            defaultColor = getInt(CUSTOM_BATTERY_FILL_GRAD_COLOR, Color.BLACK),
            showPresets = true,
            showAlphaSlider = false,
            showColorShades = true
        )
        bindingCustomColors.fillGradientColor.setOnColorSelectedListener { color: Int ->
            putInt(
                CUSTOM_BATTERY_FILL_GRAD_COLOR,
                color
            )
        }

        // Charging fill color picker
        bindingCustomColors.chargingFillColor.setColorPickerListener(
            activity = requireActivity(),
            defaultColor = getInt(CUSTOM_BATTERY_CHARGING_COLOR, Color.BLACK),
            showPresets = true,
            showAlphaSlider = false,
            showColorShades = true
        )
        bindingCustomColors.chargingFillColor.setOnColorSelectedListener { color: Int ->
            putInt(
                CUSTOM_BATTERY_CHARGING_COLOR,
                color
            )
        }

        // Power save fill color picker
        bindingCustomColors.powersaveFillColor.setColorPickerListener(
            activity = requireActivity(),
            defaultColor = getInt(CUSTOM_BATTERY_POWERSAVE_FILL_COLOR, Color.BLACK),
            showPresets = true,
            showAlphaSlider = false,
            showColorShades = true
        )
        bindingCustomColors.powersaveFillColor.setOnColorSelectedListener { color: Int ->
            putInt(
                CUSTOM_BATTERY_POWERSAVE_FILL_COLOR,
                color
            )
        }

        // Power save icon color picker
        bindingCustomColors.powersaveIconColor.setColorPickerListener(
            activity = requireActivity(),
            defaultColor = getInt(CUSTOM_BATTERY_POWERSAVE_INDICATOR_COLOR, Color.BLACK),
            showPresets = true,
            showAlphaSlider = false,
            showColorShades = true
        )
        bindingCustomColors.powersaveIconColor.setOnColorSelectedListener { color: Int ->
            putInt(
                CUSTOM_BATTERY_POWERSAVE_INDICATOR_COLOR,
                color
            )
        }
    }

    private fun customDimension() {
        // Custom dimensions
        bindingCustomDimens.customDimensions.isSwitchChecked =
            getBoolean(CUSTOM_BATTERY_DIMENSION, false)
        bindingCustomDimens.customDimensions.setSwitchChangeListener { _: CompoundButton?, isSwitchChecked: Boolean ->
            putBoolean(CUSTOM_BATTERY_DIMENSION, isSwitchChecked)
            updateLayoutVisibility()
        }

        // Battery margin left
        bindingCustomDimens.batteryMarginLeft.sliderValue =
            getInt(CUSTOM_BATTERY_MARGIN_LEFT, 4)
        bindingCustomDimens.batteryMarginLeft.setOnSliderChangeListener { slider: Slider, _: Float, _: Boolean ->
            putInt(
                CUSTOM_BATTERY_MARGIN_LEFT,
                slider.value.toInt()
            )
        }

        // Battery margin right
        bindingCustomDimens.batteryMarginRight.sliderValue =
            getInt(CUSTOM_BATTERY_MARGIN_RIGHT, 4)
        bindingCustomDimens.batteryMarginRight.setOnSliderChangeListener { slider: Slider, _: Float, _: Boolean ->
            putInt(
                CUSTOM_BATTERY_MARGIN_RIGHT,
                slider.value.toInt()
            )
        }

        // Battery margin top
        bindingCustomDimens.batteryMarginTop.sliderValue =
            getInt(CUSTOM_BATTERY_MARGIN_TOP, 0)
        bindingCustomDimens.batteryMarginTop.setOnSliderChangeListener { slider: Slider, _: Float, _: Boolean ->
            putInt(
                CUSTOM_BATTERY_MARGIN_TOP,
                slider.value.toInt()
            )
        }

        // Battery margin bottom
        bindingCustomDimens.batteryMarginBottom.sliderValue =
            getInt(CUSTOM_BATTERY_MARGIN_BOTTOM, 0)
        bindingCustomDimens.batteryMarginBottom.setOnSliderChangeListener { slider: Slider, _: Float, _: Boolean ->
            putInt(
                CUSTOM_BATTERY_MARGIN_BOTTOM,
                slider.value.toInt()
            )
        }
    }

    private fun customChargingIcon() {
        // Enable charging icon
        bindingChargingIcon.enableChargingIcon.isSwitchChecked =
            getBoolean(CUSTOM_BATTERY_CHARGING_ICON_SWITCH, false)
        bindingChargingIcon.enableChargingIcon.setSwitchChangeListener { _: CompoundButton?, isSwitchChecked: Boolean ->
            putBoolean(CUSTOM_BATTERY_CHARGING_ICON_SWITCH, isSwitchChecked)
            updateLayoutVisibility()
        }

        // Charging icon style
        bindingChargingIcon.chargingIconStyle.setSelectedIndex(
            getInt(
                CUSTOM_BATTERY_CHARGING_ICON_STYLE,
                0
            )
        )
        bindingChargingIcon.chargingIconStyle.setDrawable(getChargingIcons(requireContext()))
        bindingChargingIcon.chargingIconStyle.setCurrentValue(
            getInt(
                CUSTOM_BATTERY_CHARGING_ICON_STYLE,
                0
            ).toString()
        )
        bindingChargingIcon.chargingIconStyle.setOnItemClickListener { index: Int ->
            bindingChargingIcon.chargingIconStyle.setCurrentValue(index.toString())
            putInt(CUSTOM_BATTERY_CHARGING_ICON_STYLE, index)
        }

        // Charging icon margin left
        bindingChargingIcon.chargingIconMarginLeft.sliderValue =
            getInt(CUSTOM_BATTERY_CHARGING_ICON_MARGIN_LEFT, 1)
        bindingChargingIcon.chargingIconMarginLeft.setOnSliderChangeListener { slider: Slider, _: Float, _: Boolean ->
            putInt(
                CUSTOM_BATTERY_CHARGING_ICON_MARGIN_LEFT,
                slider.value.toInt()
            )
        }

        // Charging icon margin right
        bindingChargingIcon.chargingIconMarginRight.sliderValue =
            getInt(CUSTOM_BATTERY_CHARGING_ICON_MARGIN_RIGHT, 0)
        bindingChargingIcon.chargingIconMarginRight.setOnSliderChangeListener { slider: Slider, _: Float, _: Boolean ->
            putInt(
                CUSTOM_BATTERY_CHARGING_ICON_MARGIN_RIGHT,
                slider.value.toInt()
            )
        }

        // Charging icon size
        bindingChargingIcon.chargingIconSize.sliderValue =
            getInt(CUSTOM_BATTERY_CHARGING_ICON_WIDTH_HEIGHT, 14)
        bindingChargingIcon.chargingIconSize.setOnSliderChangeListener { slider: Slider, _: Float, _: Boolean ->
            putInt(
                CUSTOM_BATTERY_CHARGING_ICON_WIDTH_HEIGHT,
                slider.value.toInt()
            )
        }
    }

    private fun updateLayoutVisibility() {
        val selectedIndex = selectedBatteryStyle
        val batteryStyles = listOf(*resources.getStringArray(R.array.custom_battery_style))
        val showAdvancedCustomizations =
            selectedIndex >= batteryStyles.indexOf(getString(R.string.battery_landscape_battery_a)) &&
                    selectedIndex <= batteryStyles.indexOf(getString(R.string.battery_landscape_battery_o))
        val showColorPickers = bindingCustomColors.blendColor.isSwitchChecked
        val showRainbowBattery =
            batteryStyles.indexOf(getString(R.string.battery_landscape_battery_i)) == selectedIndex ||
                    batteryStyles.indexOf(getString(R.string.battery_landscape_battery_j)) == selectedIndex
        val showCommonCustomizations = selectedIndex != 0
        val showBatteryDimensions =
            selectedIndex > 2 && bindingCustomDimens.customDimensions.isSwitchChecked
        val showPercentage =
            selectedIndex != BATTERY_STYLE_DEFAULT &&
                    selectedIndex != BATTERY_STYLE_DEFAULT_LANDSCAPE &&
                    selectedIndex != BATTERY_STYLE_DEFAULT_RLANDSCAPE &&
                    selectedIndex != BATTERY_STYLE_LANDSCAPE_IOS_16 &&
                    selectedIndex != BATTERY_STYLE_LANDSCAPE_BATTERYL &&
                    selectedIndex != BATTERY_STYLE_LANDSCAPE_BATTERYM
        val showInsidePercentage =
            showPercentage && !bindingMiscSettings.hidePercentage.isSwitchChecked
        val showChargingIconCustomization =
            selectedIndex > 2 && bindingChargingIcon.enableChargingIcon.isSwitchChecked
        val showReverseLayout = selectedIndex > 2 && showInsidePercentage
        val circleBattery = selectedIndex == BATTERY_STYLE_CIRCLE ||
                selectedIndex == BATTERY_STYLE_DOTTED_CIRCLE ||
                selectedIndex == BATTERY_STYLE_FILLED_CIRCLE
        val visibilityAdvanced = if (showAdvancedCustomizations) View.VISIBLE else View.GONE
        val visibilityBlendColor =
            if (showAdvancedCustomizations || circleBattery) View.VISIBLE else View.GONE
        val visibilityColorPickers =
            if ((showAdvancedCustomizations || circleBattery) && showColorPickers) View.VISIBLE else View.GONE
        val visibilityRainbow =
            if ((showAdvancedCustomizations || circleBattery) && showRainbowBattery) View.VISIBLE else View.GONE
        val visibilityWh = if (selectedIndex > 2) View.VISIBLE else View.GONE
        val visibilityDimensions = if (showBatteryDimensions) View.VISIBLE else View.GONE
        val visibilityPercentage = if (showPercentage) View.VISIBLE else View.GONE
        val visibilityInsidePercentage = if (showInsidePercentage) View.VISIBLE else View.GONE
        val visibilityReverseLayout = if (showReverseLayout) View.VISIBLE else View.GONE
        val visibilityChargingIconSwitch = if (selectedIndex > 2) View.VISIBLE else View.GONE
        val visibilityChargingIconCustomization =
            if (showChargingIconCustomization) View.VISIBLE else View.GONE

        // Misc settings
        bindingMiscSettings.batteryWidth.setEnabled(showCommonCustomizations)
        bindingMiscSettings.batteryHeight.setEnabled(showCommonCustomizations)
        bindingMiscSettings.hidePercentage.visibility = visibilityPercentage
        bindingMiscSettings.insidePercentage.visibility = visibilityInsidePercentage
        bindingMiscSettings.hideBattery.visibility = visibilityChargingIconSwitch
        bindingMiscSettings.reverseLayout.visibility = visibilityReverseLayout
        bindingMiscSettings.rotateLayout.visibility = visibilityAdvanced

        // Custom colors
        bindingCustomColors.perimeterAlpha.visibility = visibilityAdvanced
        bindingCustomColors.fillAlpha.visibility = visibilityAdvanced
        bindingCustomColors.rainbowColor.visibility = visibilityRainbow
        bindingCustomColors.blendColor.visibility = visibilityBlendColor
        bindingCustomColors.colorPickers.visibility = visibilityColorPickers

        // Custom dimensions
        bindingCustomDimens.customDimensions.visibility = visibilityWh
        bindingCustomDimens.batteryMarginLeft.visibility = visibilityDimensions
        bindingCustomDimens.batteryMarginTop.visibility = visibilityDimensions
        bindingCustomDimens.batteryMarginRight.visibility = visibilityDimensions
        bindingCustomDimens.batteryMarginBottom.visibility = visibilityDimensions

        // Custom charging icon
        bindingChargingIcon.enableChargingIcon.visibility = visibilityChargingIconSwitch
        bindingChargingIcon.chargingIconCustContainer.visibility =
            visibilityChargingIconCustomization
    }

    companion object {
        private var selectedBatteryStyle = 0
    }
}