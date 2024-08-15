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
import com.drdisagree.iconify.common.Preferences.HIDE_QSLABEL_SWITCH
import com.drdisagree.iconify.common.Preferences.HIDE_QS_FOOTER_BUTTONS
import com.drdisagree.iconify.common.Preferences.HIDE_QS_ON_LOCKSCREEN
import com.drdisagree.iconify.common.Preferences.HIDE_QS_SILENT_TEXT
import com.drdisagree.iconify.common.Preferences.QQS_TOPMARGIN
import com.drdisagree.iconify.common.Preferences.QS_TEXT_ALWAYS_WHITE
import com.drdisagree.iconify.common.Preferences.QS_TEXT_FOLLOW_ACCENT
import com.drdisagree.iconify.common.Preferences.QS_TOPMARGIN
import com.drdisagree.iconify.common.Preferences.VERTICAL_QSTILE_SWITCH
import com.drdisagree.iconify.config.RPrefs.clearPref
import com.drdisagree.iconify.config.RPrefs.getBoolean
import com.drdisagree.iconify.config.RPrefs.getInt
import com.drdisagree.iconify.config.RPrefs.putBoolean
import com.drdisagree.iconify.config.RPrefs.putInt
import com.drdisagree.iconify.databinding.FragmentXposedQuickSettingsBinding
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader
import com.drdisagree.iconify.utils.SystemUtil
import com.google.android.material.slider.Slider

class XposedQuickSettings : BaseFragment() {

    private lateinit var binding: FragmentXposedQuickSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentXposedQuickSettingsBinding.inflate(inflater, container, false)
        val view: View = binding.getRoot()

        // Header
        setHeader(
            requireContext(),
            getParentFragmentManager(),
            binding.header.toolbar,
            R.string.activity_title_quick_settings
        )

        // Vertical QS Tile
        binding.verticalTile.isSwitchChecked = getBoolean(VERTICAL_QSTILE_SWITCH, false)
        binding.verticalTile.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            putBoolean(VERTICAL_QSTILE_SWITCH, isChecked)

            binding.hideTileLabel.setEnabled(isChecked)

            Handler(Looper.getMainLooper()).postDelayed(
                { SystemUtil.handleSystemUIRestart() },
                SWITCH_ANIMATION_DELAY
            )
        }

        // Hide label for vertical tiles
        binding.hideTileLabel.setEnabled(binding.verticalTile.isSwitchChecked)
        binding.hideTileLabel.isSwitchChecked = getBoolean(HIDE_QSLABEL_SWITCH, false)
        binding.hideTileLabel.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            putBoolean(HIDE_QSLABEL_SWITCH, isChecked)

            Handler(Looper.getMainLooper()).postDelayed(
                { SystemUtil.handleSystemUIRestart() },
                SWITCH_ANIMATION_DELAY
            )
        }

        // QQS panel top margin slider
        binding.qqsTopMargin.sliderValue = getInt(QQS_TOPMARGIN, 100)
        binding.qqsTopMargin.setOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}

            override fun onStopTrackingTouch(slider: Slider) {
                putInt(QQS_TOPMARGIN, slider.value.toInt())

                Handler(Looper.getMainLooper()).postDelayed(
                    { SystemUtil.handleSystemUIRestart() },
                    SWITCH_ANIMATION_DELAY
                )
            }
        })
        binding.qqsTopMargin.setResetClickListener {
            clearPref(QQS_TOPMARGIN)

            Handler(Looper.getMainLooper()).postDelayed(
                { SystemUtil.handleSystemUIRestart() },
                SWITCH_ANIMATION_DELAY
            )

            true
        }

        // QS panel top margin slider
        binding.qsTopMargin.sliderValue = getInt(QS_TOPMARGIN, 100)
        binding.qsTopMargin.setOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}

            override fun onStopTrackingTouch(slider: Slider) {
                putInt(QS_TOPMARGIN, slider.value.toInt())

                Handler(Looper.getMainLooper()).postDelayed(
                    { SystemUtil.handleSystemUIRestart() },
                    SWITCH_ANIMATION_DELAY
                )
            }
        })
        binding.qsTopMargin.setResetClickListener {
            clearPref(QS_TOPMARGIN)

            Handler(Looper.getMainLooper()).postDelayed(
                { SystemUtil.handleSystemUIRestart() },
                SWITCH_ANIMATION_DELAY
            )

            true
        }

        // QS text always white
        binding.labelWhite.isSwitchChecked = getBoolean(QS_TEXT_ALWAYS_WHITE, false)
        binding.labelWhite.setSwitchChangeListener(qsTextWhiteListener)

        // QS text follow accent
        binding.labelAccent.isSwitchChecked = getBoolean(QS_TEXT_FOLLOW_ACCENT, false)
        binding.labelAccent.setSwitchChangeListener(qsTextAccentListener)

        // Hide QS on lockscreen
        binding.hideQsOnLockscreen.isSwitchChecked = getBoolean(HIDE_QS_ON_LOCKSCREEN, false)
        binding.hideQsOnLockscreen.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            putBoolean(HIDE_QS_ON_LOCKSCREEN, isChecked)

            Handler(Looper.getMainLooper()).postDelayed(
                { SystemUtil.handleSystemUIRestart() },
                SWITCH_ANIMATION_DELAY
            )
        }

        // Hide silent text
        binding.hideSilentText.isSwitchChecked = getBoolean(HIDE_QS_SILENT_TEXT, false)
        binding.hideSilentText.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            putBoolean(HIDE_QS_SILENT_TEXT, isChecked)

            Handler(Looper.getMainLooper()).postDelayed(
                { SystemUtil.handleSystemUIRestart() },
                SWITCH_ANIMATION_DELAY
            )
        }

        // Hide footer buttons
        binding.hideFooterButtons.isSwitchChecked = getBoolean(HIDE_QS_FOOTER_BUTTONS, false)
        binding.hideFooterButtons.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            putBoolean(HIDE_QS_FOOTER_BUTTONS, isChecked)
        }

        return view
    }

    private var qsTextWhiteListener: CompoundButton.OnCheckedChangeListener =
        CompoundButton.OnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                putBoolean(QS_TEXT_FOLLOW_ACCENT, false)

                binding.labelAccent.setSwitchChangeListener(null)
                binding.labelAccent.isSwitchChecked = false
                binding.labelAccent.setSwitchChangeListener(qsTextAccentListener)
            }

            putBoolean(QS_TEXT_ALWAYS_WHITE, isChecked)

            Handler(Looper.getMainLooper()).postDelayed(
                { SystemUtil.handleSystemUIRestart() },
                SWITCH_ANIMATION_DELAY
            )
        }

    private var qsTextAccentListener: CompoundButton.OnCheckedChangeListener =
        CompoundButton.OnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            if (isChecked) {
                putBoolean(QS_TEXT_ALWAYS_WHITE, false)

                binding.labelWhite.setSwitchChangeListener(null)
                binding.labelWhite.isSwitchChecked = false
                binding.labelWhite.setSwitchChangeListener(qsTextWhiteListener)
            }

            putBoolean(QS_TEXT_FOLLOW_ACCENT, isChecked)

            Handler(Looper.getMainLooper()).postDelayed(
                { SystemUtil.handleSystemUIRestart() },
                SWITCH_ANIMATION_DELAY
            )
        }
}