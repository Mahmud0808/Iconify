package com.drdisagree.iconify.ui.fragments.tweaks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import com.drdisagree.iconify.R
import com.drdisagree.iconify.config.RPrefs
import com.drdisagree.iconify.databinding.FragmentMediaPlayerBinding
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader
import com.drdisagree.iconify.utils.overlay.OverlayUtils
import com.drdisagree.iconify.utils.overlay.OverlayUtils.changeOverlayState

class MediaPlayer : BaseFragment() {

    private lateinit var binding: FragmentMediaPlayerBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMediaPlayerBinding.inflate(inflater, container, false)
        val view: View = binding.getRoot()

        // Header
        setHeader(
            requireContext(),
            getParentFragmentManager(),
            binding.header.toolbar,
            R.string.activity_title_media_player
        )

        refreshPreview()

        binding.mpAccent.isSwitchChecked = RPrefs.getBoolean("IconifyComponentMPA.overlay")
        binding.mpAccent.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            if (isChecked) {
                binding.mpSystem.isSwitchChecked = false
                binding.mpPitchBlack.isSwitchChecked = false

                changeOverlayState(
                    "IconifyComponentMPS.overlay",
                    false,
                    "IconifyComponentMPB.overlay",
                    false,
                    "IconifyComponentMPA.overlay",
                    true
                )
            } else {
                OverlayUtils.disableOverlay("IconifyComponentMPA.overlay")
            }

            refreshPreview()
        }

        binding.mpSystem.isSwitchChecked = RPrefs.getBoolean("IconifyComponentMPS.overlay")
        binding.mpSystem.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            if (isChecked) {
                binding.mpAccent.isSwitchChecked = false
                binding.mpPitchBlack.isSwitchChecked = false

                changeOverlayState(
                    "IconifyComponentMPA.overlay",
                    false,
                    "IconifyComponentMPB.overlay",
                    false,
                    "IconifyComponentMPS.overlay",
                    true
                )
            } else {
                OverlayUtils.disableOverlay("IconifyComponentMPS.overlay")
            }

            refreshPreview()
        }

        binding.mpPitchBlack.isSwitchChecked = RPrefs.getBoolean("IconifyComponentMPB.overlay")
        binding.mpPitchBlack.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            if (isChecked) {
                binding.mpAccent.isSwitchChecked = false
                binding.mpSystem.isSwitchChecked = false

                changeOverlayState(
                    "IconifyComponentMPA.overlay",
                    false,
                    "IconifyComponentMPS.overlay",
                    false,
                    "IconifyComponentMPB.overlay",
                    true
                )
            } else {
                OverlayUtils.disableOverlay("IconifyComponentMPB.overlay")
            }

            refreshPreview()
        }

        return view
    }

    private fun refreshPreview() {
        binding.mpAccentPreview.previewMpAccent.visibility = View.GONE
        binding.mpPitchBlackPreview.previewMpBlack.visibility = View.GONE
        binding.mpSystemPreview.previewMpSystem.visibility = View.GONE

        when {
            RPrefs.getBoolean("IconifyComponentMPA.overlay") -> {
                binding.mpAccentPreview.previewMpAccent.visibility = View.VISIBLE
            }

            RPrefs.getBoolean("IconifyComponentMPB.overlay") -> {
                binding.mpPitchBlackPreview.previewMpBlack.visibility = View.VISIBLE
            }

            else -> {
                binding.mpSystemPreview.previewMpSystem.visibility = View.VISIBLE
            }
        }
    }
}