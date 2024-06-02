package com.drdisagree.iconify.ui.fragments

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Const.SWITCH_ANIMATION_DELAY
import com.drdisagree.iconify.common.Preferences.FIXED_STATUS_ICONS_SIDEMARGIN
import com.drdisagree.iconify.common.Preferences.FIXED_STATUS_ICONS_SWITCH
import com.drdisagree.iconify.common.Preferences.FIXED_STATUS_ICONS_TOPMARGIN
import com.drdisagree.iconify.common.Preferences.HIDE_LOCKSCREEN_CARRIER
import com.drdisagree.iconify.common.Preferences.HIDE_LOCKSCREEN_LOCK_ICON
import com.drdisagree.iconify.common.Preferences.HIDE_LOCKSCREEN_STATUSBAR
import com.drdisagree.iconify.common.Preferences.HIDE_STATUS_ICONS_SWITCH
import com.drdisagree.iconify.common.Preferences.QSPANEL_HIDE_CARRIER
import com.drdisagree.iconify.common.Preferences.QSPANEL_STATUSICONSBG_SWITCH
import com.drdisagree.iconify.config.RPrefs.getBoolean
import com.drdisagree.iconify.config.RPrefs.getInt
import com.drdisagree.iconify.config.RPrefs.putBoolean
import com.drdisagree.iconify.config.RPrefs.putInt
import com.drdisagree.iconify.databinding.FragmentXposedOthersBinding
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader
import com.drdisagree.iconify.utils.SystemUtil
import com.google.android.material.slider.Slider

class XposedOthers : BaseFragment() {

    private lateinit var binding: FragmentXposedOthersBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentXposedOthersBinding.inflate(inflater, container, false)
        val view: View = binding.getRoot()

        // Header
        setHeader(
            requireContext(),
            getParentFragmentManager(),
            binding.header.toolbar,
            R.string.activity_title_xposed_others
        )

        // Hide carrier group
        binding.hideQsCarrierGroup.isSwitchChecked = getBoolean(QSPANEL_HIDE_CARRIER, false)
        binding.hideQsCarrierGroup.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            putBoolean(QSPANEL_HIDE_CARRIER, isChecked)

            Handler(Looper.getMainLooper()).postDelayed(
                { SystemUtil.handleSystemUIRestart() },
                SWITCH_ANIMATION_DELAY
            )
        }

        // Hide status icons
        binding.hideStatusIcons.isSwitchChecked = getBoolean(HIDE_STATUS_ICONS_SWITCH, false)
        binding.hideStatusIcons.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            putBoolean(HIDE_STATUS_ICONS_SWITCH, isChecked)

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

        // Hide lockscreen lock icon
        binding.hideLockscreenLockIcon.isSwitchChecked =
            getBoolean(HIDE_LOCKSCREEN_LOCK_ICON, false)
        binding.hideLockscreenLockIcon.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            putBoolean(HIDE_LOCKSCREEN_LOCK_ICON, isChecked)

            Handler(Looper.getMainLooper()).postDelayed(
                { SystemUtil.handleSystemUIRestart() },
                SWITCH_ANIMATION_DELAY
            )
        }

        // Fixed status icons
        binding.fixedStatusIcons.isSwitchChecked = getBoolean(FIXED_STATUS_ICONS_SWITCH, false)
        binding.fixedStatusIcons.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            binding.statusIconsTopMargin.setEnabled(isChecked)
            binding.statusIconsSideMargin.setEnabled(isChecked)

            putBoolean(FIXED_STATUS_ICONS_SWITCH, isChecked)

            Handler(Looper.getMainLooper()).postDelayed(
                { SystemUtil.handleSystemUIRestart() },
                SWITCH_ANIMATION_DELAY
            )
        }

        // Status icons top margin
        if (Build.VERSION.SDK_INT >= 33) {
            binding.statusIconsTopMargin.setSliderValueTo(250)
        }
        binding.statusIconsTopMargin.sliderValue = getInt(FIXED_STATUS_ICONS_TOPMARGIN, 8)
        binding.statusIconsTopMargin.setEnabled(
            if (Build.VERSION.SDK_INT >= 33) {
                getBoolean(QSPANEL_STATUSICONSBG_SWITCH, false) ||
                        getBoolean(FIXED_STATUS_ICONS_SWITCH, false)
            } else {
                getBoolean(FIXED_STATUS_ICONS_SWITCH, false)
            }
        )
        binding.statusIconsTopMargin.setOnSliderTouchListener(object :
            Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}

            override fun onStopTrackingTouch(slider: Slider) {
                putInt(FIXED_STATUS_ICONS_TOPMARGIN, slider.value.toInt())
            }
        })

        // Status icons side margin
        binding.statusIconsSideMargin.sliderValue = getInt(FIXED_STATUS_ICONS_SIDEMARGIN, 0)
        binding.statusIconsSideMargin.setEnabled(
            getBoolean(FIXED_STATUS_ICONS_SWITCH, false)
        )
        binding.statusIconsSideMargin.setOnSliderTouchListener(object :
            Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}

            override fun onStopTrackingTouch(slider: Slider) {
                putInt(FIXED_STATUS_ICONS_SIDEMARGIN, slider.value.toInt())
            }
        })
        return view
    }
}