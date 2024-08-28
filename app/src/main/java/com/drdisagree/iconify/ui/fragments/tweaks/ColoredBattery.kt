package com.drdisagree.iconify.ui.fragments.tweaks

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.Iconify.Companion.appContextLocale
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Const.FRAMEWORK_PACKAGE
import com.drdisagree.iconify.common.Const.SWITCH_ANIMATION_DELAY
import com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.common.Preferences.COLORED_BATTERY_CHECK
import com.drdisagree.iconify.common.Preferences.COLORED_BATTERY_SWITCH
import com.drdisagree.iconify.common.References.FABRICATED_BATTERY_COLOR_BG
import com.drdisagree.iconify.common.References.FABRICATED_BATTERY_COLOR_FG
import com.drdisagree.iconify.common.References.FABRICATED_COLORED_BATTERY
import com.drdisagree.iconify.config.RPrefs
import com.drdisagree.iconify.config.RPrefs.putString
import com.drdisagree.iconify.databinding.FragmentColoredBatteryBinding
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader
import com.drdisagree.iconify.utils.color.ColorUtils.colorToSpecialHex
import com.drdisagree.iconify.utils.overlay.FabricatedUtils
import com.drdisagree.iconify.utils.overlay.FabricatedUtils.buildAndEnableOverlay
import com.drdisagree.iconify.utils.overlay.OverlayUtils.isOverlayEnabled

class ColoredBattery : BaseFragment() {

    private lateinit var binding: FragmentColoredBatteryBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentColoredBatteryBinding.inflate(inflater, container, false)
        val view: View = binding.getRoot()

        // Header
        setHeader(
            requireContext(),
            getParentFragmentManager(),
            binding.header.toolbar,
            R.string.activity_title_colored_battery
        )

        // Enable colored battery
        binding.enableColoredBattery.isSwitchChecked =
            if (RPrefs.getString(COLORED_BATTERY_CHECK) == null) {
                isOverlayEnabled("IconifyComponentIPSUI2.overlay") ||
                        isOverlayEnabled("IconifyComponentIPSUI4.overlay")
            } else {
                RPrefs.getBoolean(COLORED_BATTERY_SWITCH)
            }

        binding.enableColoredBattery.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            Handler(Looper.getMainLooper()).postDelayed(
                {
                    if (isChecked) {
                        putString(COLORED_BATTERY_CHECK, "On")
                        buildAndEnableOverlay(
                            FRAMEWORK_PACKAGE,
                            FABRICATED_COLORED_BATTERY,
                            "bool",
                            "config_batterymeterDualTone",
                            "1"
                        )
                    } else {
                        putString(COLORED_BATTERY_CHECK, "Off")
                        FabricatedUtils.disableOverlay(FABRICATED_COLORED_BATTERY)
                        buildAndEnableOverlay(
                            FRAMEWORK_PACKAGE,
                            FABRICATED_COLORED_BATTERY,
                            "bool",
                            "config_batterymeterDualTone",
                            "0"
                        )

                        if (RPrefs.getString(FABRICATED_BATTERY_COLOR_BG) != null) FabricatedUtils.disableOverlay(
                            FABRICATED_BATTERY_COLOR_BG
                        )

                        if (RPrefs.getString(FABRICATED_BATTERY_COLOR_FG) != null) FabricatedUtils.disableOverlay(
                            FABRICATED_BATTERY_COLOR_FG
                        )
                    }

                    if (isOverlayEnabled("IconifyComponentIPSUI2.overlay")) {
                        Toast.makeText(
                            appContext,
                            appContextLocale.resources.getString(R.string.toast_lorn_colored_battery),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else if (isOverlayEnabled("IconifyComponentIPSUI4.overlay")) {
                        Toast.makeText(
                            appContext,
                            appContextLocale.resources.getString(R.string.toast_plumpy_colored_battery),
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    RPrefs.putBoolean(COLORED_BATTERY_SWITCH, isChecked)
                }, SWITCH_ANIMATION_DELAY
            )
        }

        colorBackground = if (RPrefs.getString(FABRICATED_BATTERY_COLOR_BG) != null) {
            RPrefs.getString(FABRICATED_BATTERY_COLOR_BG)
        } else {
            (-0xf0f10).toString()
        }
        colorFilled = if (RPrefs.getString(FABRICATED_BATTERY_COLOR_FG) != null) {
            RPrefs.getString(FABRICATED_BATTERY_COLOR_FG)
        } else {
            (-0xf0f10).toString()
        }

        // Battery background color
        binding.batteryBackgroundColor.setColorPickerListener(
            activity = requireActivity(),
            defaultColor = colorBackground!!.toInt(),
            showPresets = true,
            showAlphaSlider = false,
            showColorShades = true
        )
        binding.batteryBackgroundColor.setOnColorSelectedListener { color: Int ->
            colorBackground = color.toString()
            putString(FABRICATED_BATTERY_COLOR_BG, colorBackground)

            buildAndEnableOverlay(
                SYSTEMUI_PACKAGE,
                FABRICATED_BATTERY_COLOR_BG,
                "color",
                "light_mode_icon_color_dual_tone_background",
                colorToSpecialHex(colorBackground!!.toInt())
            )
        }

        // Battery filled color
        binding.batteryFilledColor.setColorPickerListener(
            activity = requireActivity(), defaultColor = colorFilled!!.toInt(),
            showPresets = true,
            showAlphaSlider = false,
            showColorShades = true
        )
        binding.batteryFilledColor.setOnColorSelectedListener { color: Int ->
            colorFilled = color.toString()
            putString(FABRICATED_BATTERY_COLOR_FG, colorFilled)

            buildAndEnableOverlay(
                SYSTEMUI_PACKAGE,
                FABRICATED_BATTERY_COLOR_FG,
                "color",
                "light_mode_icon_color_dual_tone_fill",
                colorToSpecialHex(colorFilled!!.toInt())
            )
        }

        return view
    }

    companion object {
        private var colorBackground: String? = null
        private var colorFilled: String? = null
    }
}