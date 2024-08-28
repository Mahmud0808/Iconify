package com.drdisagree.iconify.ui.fragments.settings

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Const.SWITCH_ANIMATION_DELAY
import com.drdisagree.iconify.common.Preferences.HEADER_IMAGE_OVERLAP
import com.drdisagree.iconify.common.Preferences.HIDE_DATA_DISABLED_ICON
import com.drdisagree.iconify.config.RPrefs.getBoolean
import com.drdisagree.iconify.config.RPrefs.putBoolean
import com.drdisagree.iconify.databinding.FragmentExperimentalBinding
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader
import com.drdisagree.iconify.utils.SystemUtils

class Experimental : BaseFragment() {

    private lateinit var binding: FragmentExperimentalBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentExperimentalBinding.inflate(inflater, container, false)
        val root: View = binding.getRoot()

        // Header
        setHeader(
            requireContext(),
            getParentFragmentManager(),
            binding.header.toolbar,
            R.string.activity_title_experimental
        )

        // Header image overlap
        binding.headerImageOverlap.isSwitchChecked = getBoolean(HEADER_IMAGE_OVERLAP, false)
        binding.headerImageOverlap.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            putBoolean(HEADER_IMAGE_OVERLAP, isChecked)

            Handler(Looper.getMainLooper()).postDelayed(
                { SystemUtils.restartSystemUI() },
                SWITCH_ANIMATION_DELAY
            )
        }

        // Hide data disabled icon
        binding.hideDataDisabledIcon.isSwitchChecked = getBoolean(HIDE_DATA_DISABLED_ICON, false)
        binding.hideDataDisabledIcon.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            putBoolean(HIDE_DATA_DISABLED_ICON, isChecked)
        }
        return root
    }
}