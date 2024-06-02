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
import com.drdisagree.iconify.common.Preferences.AGGRESSIVE_QSPANEL_BLUR_SWITCH
import com.drdisagree.iconify.common.Preferences.BLUR_RADIUS_VALUE
import com.drdisagree.iconify.common.Preferences.LOCKSCREEN_SHADE_SWITCH
import com.drdisagree.iconify.common.Preferences.NOTIF_TRANSPARENCY_SWITCH
import com.drdisagree.iconify.common.Preferences.QSALPHA_LEVEL
import com.drdisagree.iconify.common.Preferences.QSPANEL_BLUR_SWITCH
import com.drdisagree.iconify.common.Preferences.QS_TRANSPARENCY_SWITCH
import com.drdisagree.iconify.config.RPrefs.getBoolean
import com.drdisagree.iconify.config.RPrefs.getInt
import com.drdisagree.iconify.config.RPrefs.putBoolean
import com.drdisagree.iconify.config.RPrefs.putInt
import com.drdisagree.iconify.databinding.FragmentXposedTransparencyBlurBinding
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader
import com.drdisagree.iconify.utils.SystemUtil
import com.drdisagree.iconify.utils.SystemUtil.disableBlur
import com.drdisagree.iconify.utils.SystemUtil.enableBlur
import com.drdisagree.iconify.utils.SystemUtil.isBlurEnabled
import com.google.android.material.slider.Slider

class XposedTransparencyBlur : BaseFragment() {

    private lateinit var binding: FragmentXposedTransparencyBlurBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentXposedTransparencyBlurBinding.inflate(inflater, container, false)
        val view: View = binding.getRoot()

        // Header
        setHeader(
            requireContext(),
            getParentFragmentManager(),
            binding.header.toolbar,
            R.string.activity_title_transparency_blur
        )

        // Qs Panel & Notification Shade Transparency
        binding.transparentQsPanel.isSwitchChecked = getBoolean(QS_TRANSPARENCY_SWITCH, false)
        binding.transparentQsPanel.setSwitchChangeListener(qsTransparencyListener)
        binding.transparentNotifShade.isSwitchChecked = getBoolean(NOTIF_TRANSPARENCY_SWITCH, false)
        binding.transparentNotifShade.setSwitchChangeListener(notifTransparencyListener)
        binding.keepLockscreenShade.setEnabled(binding.transparentQsPanel.isSwitchChecked || binding.transparentNotifShade.isSwitchChecked)
        binding.keepLockscreenShade.isSwitchChecked = getBoolean(LOCKSCREEN_SHADE_SWITCH, false)
        binding.keepLockscreenShade.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            putBoolean(LOCKSCREEN_SHADE_SWITCH, isChecked)

            Handler(Looper.getMainLooper()).postDelayed(
                { SystemUtil.handleSystemUIRestart() },
                SWITCH_ANIMATION_DELAY
            )
        }

        // Transparency Alpha
        binding.transparencySlider.setEnabled(binding.transparentQsPanel.isSwitchChecked || binding.transparentNotifShade.isSwitchChecked)
        binding.transparencySlider.sliderValue = getInt(QSALPHA_LEVEL, 60)
        binding.transparencySlider.setOnSliderTouchListener(object :
            Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}

            override fun onStopTrackingTouch(slider: Slider) {
                putInt(QSALPHA_LEVEL, slider.value.toInt())
            }
        })
        binding.transparencySlider.setResetClickListener {
            putInt(QSALPHA_LEVEL, 60)

            true
        }

        // Qs Panel Blur Enabler
        putBoolean(QSPANEL_BLUR_SWITCH, isBlurEnabled(false))
        binding.blur.isSwitchChecked = getBoolean(QSPANEL_BLUR_SWITCH, false)
        binding.blur.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            putBoolean(QSPANEL_BLUR_SWITCH, isChecked)

            if (isChecked) {
                enableBlur(false)
            } else {
                binding.aggressiveBlur.isSwitchChecked = false
                disableBlur(false)
            }

            binding.aggressiveBlur.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        // Aggressive Qs Panel Blur Enabler
        putBoolean(AGGRESSIVE_QSPANEL_BLUR_SWITCH, isBlurEnabled(true))
        binding.aggressiveBlur.visibility =
            if (binding.blur.isSwitchChecked) {
                View.VISIBLE
            } else {
                View.GONE
            }
        binding.aggressiveBlur.isSwitchChecked = getBoolean(AGGRESSIVE_QSPANEL_BLUR_SWITCH, false)
        binding.aggressiveBlur.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            putBoolean(AGGRESSIVE_QSPANEL_BLUR_SWITCH, isChecked)

            if (isChecked) {
                enableBlur(true)
            } else {
                disableBlur(true)
            }
        }

        // Blur Intensity
        binding.blurIntensity.sliderValue = getInt(BLUR_RADIUS_VALUE, 23)
        binding.blurIntensity.setOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}

            override fun onStopTrackingTouch(slider: Slider) {
                putInt(BLUR_RADIUS_VALUE, slider.value.toInt())

                Handler(Looper.getMainLooper()).postDelayed(
                    { SystemUtil.handleSystemUIRestart() },
                    SWITCH_ANIMATION_DELAY
                )
            }
        })
        binding.blurIntensity.setResetClickListener {
            putInt(BLUR_RADIUS_VALUE, 23)

            Handler(Looper.getMainLooper()).postDelayed(
                { SystemUtil.handleSystemUIRestart() },
                SWITCH_ANIMATION_DELAY
            )

            true
        }

        return view
    }

    private var qsTransparencyListener: CompoundButton.OnCheckedChangeListener =
        CompoundButton.OnCheckedChangeListener { _, isChecked ->
            putBoolean(QS_TRANSPARENCY_SWITCH, isChecked)

            if (isChecked) {
                putBoolean(NOTIF_TRANSPARENCY_SWITCH, false)
                binding.transparentNotifShade.setSwitchChangeListener(null)
                binding.transparentNotifShade.isSwitchChecked = false
                binding.transparentNotifShade.setSwitchChangeListener(notifTransparencyListener)
            }

            binding.keepLockscreenShade.setEnabled(binding.transparentQsPanel.isSwitchChecked || binding.transparentNotifShade.isSwitchChecked)
            binding.transparencySlider.setEnabled(binding.transparentQsPanel.isSwitchChecked || binding.transparentNotifShade.isSwitchChecked)

            Handler(Looper.getMainLooper()).postDelayed(
                { SystemUtil.handleSystemUIRestart() },
                SWITCH_ANIMATION_DELAY
            )
        }

    private var notifTransparencyListener: CompoundButton.OnCheckedChangeListener =
        CompoundButton.OnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            putBoolean(NOTIF_TRANSPARENCY_SWITCH, isChecked)

            if (isChecked) {
                putBoolean(QS_TRANSPARENCY_SWITCH, false)
                binding.transparentQsPanel.setSwitchChangeListener(null)
                binding.transparentQsPanel.isSwitchChecked = false
                binding.transparentQsPanel.setSwitchChangeListener(qsTransparencyListener)
            }

            binding.keepLockscreenShade.setEnabled(binding.transparentQsPanel.isSwitchChecked || binding.transparentNotifShade.isSwitchChecked)
            binding.transparencySlider.setEnabled(binding.transparentQsPanel.isSwitchChecked || binding.transparentNotifShade.isSwitchChecked)

            Handler(Looper.getMainLooper()).postDelayed(
                { SystemUtil.handleSystemUIRestart() },
                SWITCH_ANIMATION_DELAY
            )
        }
}
