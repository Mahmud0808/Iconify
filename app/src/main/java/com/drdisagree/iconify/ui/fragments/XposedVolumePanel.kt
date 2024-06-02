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
import com.drdisagree.iconify.common.Preferences.VOLUME_PANEL_PERCENTAGE
import com.drdisagree.iconify.common.Preferences.VOLUME_PANEL_SAFETY_WARNING
import com.drdisagree.iconify.config.RPrefs.getBoolean
import com.drdisagree.iconify.config.RPrefs.putBoolean
import com.drdisagree.iconify.databinding.FragmentXposedVolumePanelBinding
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader
import com.drdisagree.iconify.utils.SystemUtil

class XposedVolumePanel : BaseFragment() {

    private lateinit var binding: FragmentXposedVolumePanelBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentXposedVolumePanelBinding.inflate(inflater, container, false)
        val view: View = binding.getRoot()

        // Header
        setHeader(
            requireContext(),
            getParentFragmentManager(),
            binding.header.toolbar,
            R.string.activity_title_volume_panel
        )

        // Volume percentage
        binding.volumePercentage.isSwitchChecked = getBoolean(VOLUME_PANEL_PERCENTAGE, false)
        binding.volumePercentage.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            putBoolean(VOLUME_PANEL_PERCENTAGE, isChecked)

            Handler(Looper.getMainLooper()).postDelayed(
                { SystemUtil.handleSystemUIRestart() },
                SWITCH_ANIMATION_DELAY
            )
        }

        // Safety warning
        binding.safetyWarning.isSwitchChecked = getBoolean(VOLUME_PANEL_SAFETY_WARNING, true)
        binding.safetyWarning.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            putBoolean(VOLUME_PANEL_SAFETY_WARNING, isChecked)
        }

        return view
    }
}