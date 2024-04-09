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
import com.drdisagree.iconify.common.Preferences.BLACK_QSPANEL
import com.drdisagree.iconify.common.Preferences.DUALTONE_QSPANEL
import com.drdisagree.iconify.common.Preferences.FIX_NOTIFICATION_COLOR
import com.drdisagree.iconify.common.Preferences.FIX_QS_TILE_COLOR
import com.drdisagree.iconify.common.Preferences.FLUID_NOTIF_TRANSPARENCY
import com.drdisagree.iconify.common.Preferences.FLUID_POWERMENU_TRANSPARENCY
import com.drdisagree.iconify.common.Preferences.FLUID_QSPANEL
import com.drdisagree.iconify.common.Preferences.LIGHT_QSPANEL
import com.drdisagree.iconify.config.RPrefs.getBoolean
import com.drdisagree.iconify.config.RPrefs.putBoolean
import com.drdisagree.iconify.databinding.FragmentXposedThemesBinding
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader
import com.drdisagree.iconify.utils.SystemUtil

class XposedThemes : BaseFragment() {

    private lateinit var binding: FragmentXposedThemesBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentXposedThemesBinding.inflate(inflater, container, false)
        val view: View = binding.getRoot()

        // Header
        setHeader(
            requireContext(),
            getParentFragmentManager(),
            binding.header.toolbar,
            R.string.activity_title_themes
        )

        // Light Theme
        binding.lightTheme.isSwitchChecked = getBoolean(LIGHT_QSPANEL, false)
        binding.lightTheme.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            putBoolean(LIGHT_QSPANEL, isChecked)

            binding.dualTone.setEnabled(isChecked)

            Handler(Looper.getMainLooper()).postDelayed(
                { SystemUtil.handleSystemUIRestart() },
                SWITCH_ANIMATION_DELAY
            )
        }
        binding.dualTone.setEnabled(binding.lightTheme.isSwitchChecked)

        // Dual Tone
        binding.dualTone.isSwitchChecked = getBoolean(DUALTONE_QSPANEL, false)
        binding.dualTone.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            putBoolean(DUALTONE_QSPANEL, isChecked)
        }

        // Pixel Black Theme
        binding.blackTheme.isSwitchChecked = getBoolean(BLACK_QSPANEL, false)
        binding.blackTheme.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            putBoolean(BLACK_QSPANEL, isChecked)

            Handler(Looper.getMainLooper()).postDelayed(
                { SystemUtil.handleSystemUIRestart() },
                SWITCH_ANIMATION_DELAY
            )
        }

        // Fluid QS Theme
        binding.fluidQsTheme.isSwitchChecked = getBoolean(FLUID_QSPANEL, false)
        binding.fluidQsTheme.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            putBoolean(FLUID_QSPANEL, isChecked)

            binding.fluidNotifTheme.setEnabled(isChecked)
            binding.fluidPowermenuTheme.setEnabled(isChecked)

            Handler(Looper.getMainLooper()).postDelayed(
                { SystemUtil.handleSystemUIRestart() },
                SWITCH_ANIMATION_DELAY
            )
        }
        binding.fluidNotifTheme.setEnabled(binding.fluidQsTheme.isSwitchChecked)
        binding.fluidPowermenuTheme.setEnabled(binding.fluidQsTheme.isSwitchChecked)

        // Fluid QS Notification Transparency
        binding.fluidNotifTheme.isSwitchChecked = getBoolean(FLUID_NOTIF_TRANSPARENCY, false)
        binding.fluidNotifTheme.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            putBoolean(FLUID_NOTIF_TRANSPARENCY, isChecked)

            Handler(Looper.getMainLooper()).postDelayed(
                { SystemUtil.handleSystemUIRestart() },
                SWITCH_ANIMATION_DELAY
            )
        }

        // Fluid QS Power Menu Transparency
        binding.fluidPowermenuTheme.isSwitchChecked =
            getBoolean(FLUID_POWERMENU_TRANSPARENCY, false)
        binding.fluidPowermenuTheme.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            putBoolean(FLUID_POWERMENU_TRANSPARENCY, isChecked)

            Handler(Looper.getMainLooper()).postDelayed(
                { SystemUtil.handleSystemUIRestart() },
                SWITCH_ANIMATION_DELAY
            )
        }

        // Others section
        binding.sectionOthers.visibility =
            if (Build.VERSION.SDK_INT >= 34) {
                View.VISIBLE
            } else {
                View.GONE
            }

        // Fix qs tile color
        binding.fixQsTileColor.visibility =
            if (Build.VERSION.SDK_INT >= 34) {
                View.VISIBLE
            } else {
                View.GONE
            }
        binding.fixQsTileColor.isSwitchChecked = getBoolean(FIX_QS_TILE_COLOR, true)
        binding.fixQsTileColor.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            putBoolean(FIX_QS_TILE_COLOR, isChecked)

            Handler(Looper.getMainLooper()).postDelayed(
                { SystemUtil.handleSystemUIRestart() },
                SWITCH_ANIMATION_DELAY
            )
        }

        // Fix notification color
        binding.fixNotificationColor.visibility =
            if (Build.VERSION.SDK_INT >= 34) {
                View.VISIBLE
            } else {
                View.GONE
            }
        binding.fixNotificationColor.isSwitchChecked = getBoolean(FIX_NOTIFICATION_COLOR, true)
        binding.fixNotificationColor.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            putBoolean(FIX_NOTIFICATION_COLOR, isChecked)

            Handler(Looper.getMainLooper()).postDelayed(
                { SystemUtil.handleSystemUIRestart() },
                SWITCH_ANIMATION_DELAY
            )
        }

        return view
    }
}