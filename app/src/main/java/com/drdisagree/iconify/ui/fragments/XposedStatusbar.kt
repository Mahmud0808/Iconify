package com.drdisagree.iconify.ui.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Const.SWITCH_ANIMATION_DELAY
import com.drdisagree.iconify.common.Preferences.COLORED_STATUSBAR_ICON
import com.drdisagree.iconify.common.Preferences.HIDE_LOCKSCREEN_CARRIER
import com.drdisagree.iconify.common.Preferences.HIDE_LOCKSCREEN_STATUSBAR
import com.drdisagree.iconify.common.Preferences.SB_CLOCK_SIZE
import com.drdisagree.iconify.common.Preferences.SB_CLOCK_SIZE_SWITCH
import com.drdisagree.iconify.config.RPrefs.getBoolean
import com.drdisagree.iconify.config.RPrefs.getInt
import com.drdisagree.iconify.config.RPrefs.putBoolean
import com.drdisagree.iconify.config.RPrefs.putInt
import com.drdisagree.iconify.databinding.FragmentXposedStatusbarBinding
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader
import com.drdisagree.iconify.utils.SystemUtil
import com.google.android.material.slider.Slider

class XposedStatusbar : BaseFragment() {

    private lateinit var binding: FragmentXposedStatusbarBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentXposedStatusbarBinding.inflate(inflater, container, false)
        val view: View = binding.getRoot()

        // Header
        setHeader(
            requireContext(),
            getParentFragmentManager(),
            binding.header.toolbar,
            R.string.activity_title_statusbar
        )

        // SB Clock Size Switch
        binding.sbClockSizeSwitch.isSwitchChecked = getBoolean(SB_CLOCK_SIZE_SWITCH, false)
        binding.sbClockSizeSwitch.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            binding.sbClockSize.visibility = if (isChecked) {
                putBoolean(SB_CLOCK_SIZE_SWITCH, true)
                putInt(SB_CLOCK_SIZE, binding.sbClockSize.sliderValue)
                View.VISIBLE
            } else {
                putInt(SB_CLOCK_SIZE, 14)
                putBoolean(SB_CLOCK_SIZE_SWITCH, false)
                View.GONE
            }
        }

        // SB Clock Size Slider
        binding.sbClockSize.sliderValue = getInt(SB_CLOCK_SIZE, 14)
        binding.sbClockSize.setOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}

            override fun onStopTrackingTouch(slider: Slider) {
                putInt(SB_CLOCK_SIZE, slider.value.toInt())
            }
        })
        binding.sbClockSize.visibility = if (binding.sbClockSizeSwitch.isSwitchChecked) {
            View.VISIBLE
        } else {
            View.GONE
        }

        // Colored statusbar icons
        binding.coloredSbIcon.isSwitchChecked = getBoolean(COLORED_STATUSBAR_ICON, false)
        binding.coloredSbIcon.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            putBoolean(COLORED_STATUSBAR_ICON, isChecked)

            Handler(Looper.getMainLooper()).postDelayed(
                { SystemUtil.handleSystemUIRestart() },
                SWITCH_ANIMATION_DELAY
            )
        }

        // Hide lockscreen carrier
        binding.hideLockscreenCarrier.isSwitchChecked = getBoolean(HIDE_LOCKSCREEN_CARRIER, false)
        binding.hideLockscreenCarrier.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            putBoolean(HIDE_LOCKSCREEN_CARRIER, isChecked)

            Handler(Looper.getMainLooper()).postDelayed(
                { SystemUtil.handleSystemUIRestart() },
                SWITCH_ANIMATION_DELAY
            )
        }

        // Hide lockscreen statusbar
        binding.hideLockscreenStatusbar.isSwitchChecked =
            getBoolean(HIDE_LOCKSCREEN_STATUSBAR, false)
        binding.hideLockscreenStatusbar.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            putBoolean(HIDE_LOCKSCREEN_STATUSBAR, isChecked)

            Handler(Looper.getMainLooper()).postDelayed(
                { SystemUtil.handleSystemUIRestart() },
                SWITCH_ANIMATION_DELAY
            )
        }

        return view
    }
}