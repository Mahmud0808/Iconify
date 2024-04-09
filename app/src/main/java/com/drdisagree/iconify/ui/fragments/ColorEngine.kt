package com.drdisagree.iconify.ui.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.navigation.Navigation.findNavController
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Const.SWITCH_ANIMATION_DELAY
import com.drdisagree.iconify.common.Preferences.COLOR_ACCENT_PRIMARY
import com.drdisagree.iconify.common.Preferences.COLOR_ACCENT_SECONDARY
import com.drdisagree.iconify.common.Preferences.STR_NULL
import com.drdisagree.iconify.config.Prefs.getBoolean
import com.drdisagree.iconify.config.Prefs.getString
import com.drdisagree.iconify.databinding.FragmentColorEngineBinding
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader
import com.drdisagree.iconify.utils.overlay.OverlayUtil
import com.drdisagree.iconify.utils.overlay.OverlayUtil.changeOverlayState
import com.drdisagree.iconify.utils.overlay.OverlayUtil.enableOverlay
import com.drdisagree.iconify.utils.overlay.OverlayUtil.isOverlayDisabled

class ColorEngine : BaseFragment() {

    private lateinit var binding: FragmentColorEngineBinding
    private var minimalQsListener: CompoundButton.OnCheckedChangeListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentColorEngineBinding.inflate(inflater, container, false)
        val view: View = binding.getRoot()

        // Header
        setHeader(
            requireContext(),
            getParentFragmentManager(),
            binding.header.toolbar,
            R.string.activity_title_color_engine
        )

        // Basic colors
        binding.basicColors.setOnClickListener {
            findNavController(view).navigate(R.id.action_colorEngine_to_basicColors)
        }

        // Monet engine
        binding.monetEngine.setOnClickListener {
            findNavController(view).navigate(R.id.action_colorEngine_to_monetEngine)
        }

        // Apply monet accent and gradient
        binding.monetAccent.isSwitchChecked = getBoolean("IconifyComponentAMAC.overlay")
        binding.monetAccent.setSwitchChangeListener(monetAccentListener)

        binding.monetGradient.isSwitchChecked = getBoolean("IconifyComponentAMGC.overlay")
        binding.monetGradient.setSwitchChangeListener(monetGradientListener)

        // Pitch Black Dark
        binding.pitchBlackDarkTheme.isSwitchChecked = getBoolean("IconifyComponentQSPBD.overlay")
        binding.pitchBlackDarkTheme.setSwitchChangeListener(pitchBlackDarkListener)

        // Pitch Black Amoled
        binding.pitchBlackAmoledTheme.isSwitchChecked = getBoolean("IconifyComponentQSPBA.overlay")
        binding.pitchBlackAmoledTheme.setSwitchChangeListener(pitchBlackAmoledListener)

        // Minimal QsPanel
        binding.minimalQspanel.isSwitchChecked = getBoolean("IconifyComponentQSST.overlay")
        if (minimalQsListener == null) {
            initializeMinimalQsListener()
        }
        binding.minimalQspanel.setSwitchChangeListener(minimalQsListener)

        // Disable Monet
        binding.disableMonet.isSwitchChecked = getBoolean("IconifyComponentDM.overlay")
        binding.disableMonet.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            Handler(Looper.getMainLooper()).postDelayed(
                {
                    if (isChecked) {
                        enableOverlay("IconifyComponentDM.overlay")
                    } else {
                        OverlayUtil.disableOverlay("IconifyComponentDM.overlay")
                    }
                }, SWITCH_ANIMATION_DELAY
            )
        }
        
        return view
    }

    private fun enableMonetAccent() {
        enableOverlay("IconifyComponentAMAC.overlay")
        BasicColors.disableAccentColors()
    }

    private fun disableMonetAccent() {
        OverlayUtil.disableOverlays("IconifyComponentAMAC.overlay")
    }

    private fun enableMonetGradient() {
        enableOverlay("IconifyComponentAMGC.overlay")
        BasicColors.disableAccentColors()
    }

    private fun disableMonetGradient() {
        OverlayUtil.disableOverlay("IconifyComponentAMGC.overlay")
    }

    private fun shouldUseDefaultColors(): Boolean {
        return isOverlayDisabled("IconifyComponentME.overlay")
    }

    private fun applyDefaultColors() {
        if (shouldUseDefaultColors()) {
            if (getString(COLOR_ACCENT_PRIMARY) == STR_NULL) {
                BasicColors.applyDefaultPrimaryColors()
            } else {
                BasicColors.applyPrimaryColors()
            }

            if (getString(COLOR_ACCENT_SECONDARY) == STR_NULL) {
                BasicColors.applyDefaultSecondaryColors()
            } else {
                BasicColors.applySecondaryColors()
            }
        }
    }

    private fun initializeMinimalQsListener() {
        minimalQsListener =
            CompoundButton.OnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
                if (isChecked) {
                    binding.pitchBlackDarkTheme.setSwitchChangeListener(null)
                    binding.pitchBlackDarkTheme.isSwitchChecked = false
                    binding.pitchBlackDarkTheme.setSwitchChangeListener(pitchBlackDarkListener)
                    binding.pitchBlackAmoledTheme.setSwitchChangeListener(null)
                    binding.pitchBlackAmoledTheme.isSwitchChecked = false
                    binding.pitchBlackAmoledTheme.setSwitchChangeListener(pitchBlackAmoledListener)
                }

                Handler(Looper.getMainLooper()).postDelayed({
                    if (isChecked) {
                        changeOverlayState(
                            "IconifyComponentQSPBD.overlay",
                            false,
                            "IconifyComponentQSPBA.overlay",
                            false,
                            "IconifyComponentQSST.overlay",
                            true
                        )
                    } else {
                        OverlayUtil.disableOverlay("IconifyComponentQSST.overlay")
                    }
                }, SWITCH_ANIMATION_DELAY)
            }
    }

    private var monetAccentListener: CompoundButton.OnCheckedChangeListener =
        CompoundButton.OnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.monetGradient.setSwitchChangeListener(null)
                binding.monetGradient.isSwitchChecked = false
                binding.monetGradient.setSwitchChangeListener(monetGradientListener)
            }

            Handler(Looper.getMainLooper()).postDelayed({
                if (isChecked) {
                    disableMonetGradient()
                    enableMonetAccent()
                } else {
                    disableMonetAccent()
                    applyDefaultColors()
                }
            }, SWITCH_ANIMATION_DELAY)
        }
    private var monetGradientListener: CompoundButton.OnCheckedChangeListener =
        CompoundButton.OnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            if (isChecked) {
                binding.monetAccent.setSwitchChangeListener(null)
                binding.monetAccent.isSwitchChecked = false
                binding.monetAccent.setSwitchChangeListener(monetAccentListener)
            }

            Handler(Looper.getMainLooper()).postDelayed({
                if (isChecked) {
                    disableMonetAccent()
                    enableMonetGradient()
                } else {
                    disableMonetGradient()
                    applyDefaultColors()
                }
            }, SWITCH_ANIMATION_DELAY)
        }
    private var pitchBlackDarkListener: CompoundButton.OnCheckedChangeListener =
        CompoundButton.OnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (minimalQsListener == null) {
                    initializeMinimalQsListener()
                }

                binding.minimalQspanel.setSwitchChangeListener(null)
                binding.minimalQspanel.isSwitchChecked = false
                binding.minimalQspanel.setSwitchChangeListener(minimalQsListener)

                binding.pitchBlackAmoledTheme.setSwitchChangeListener(null)
                binding.pitchBlackAmoledTheme.isSwitchChecked = false
                binding.pitchBlackAmoledTheme.setSwitchChangeListener(pitchBlackAmoledListener)
            }

            Handler(Looper.getMainLooper()).postDelayed({
                if (isChecked) {
                    changeOverlayState(
                        "IconifyComponentQSST.overlay",
                        false,
                        "IconifyComponentQSPBA.overlay",
                        false,
                        "IconifyComponentQSPBD.overlay",
                        true
                    )
                } else {
                    OverlayUtil.disableOverlay("IconifyComponentQSPBD.overlay")
                }
            }, SWITCH_ANIMATION_DELAY)
        }
    private var pitchBlackAmoledListener: CompoundButton.OnCheckedChangeListener =
        CompoundButton.OnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            if (isChecked) {
                if (minimalQsListener == null) {
                    initializeMinimalQsListener()
                }

                binding.minimalQspanel.setSwitchChangeListener(null)
                binding.minimalQspanel.isSwitchChecked = false
                binding.minimalQspanel.setSwitchChangeListener(minimalQsListener)

                binding.pitchBlackDarkTheme.setSwitchChangeListener(null)
                binding.pitchBlackDarkTheme.isSwitchChecked = false
                binding.pitchBlackDarkTheme.setSwitchChangeListener(pitchBlackDarkListener)
            }

            Handler(Looper.getMainLooper()).postDelayed({
                if (isChecked) {
                    changeOverlayState(
                        "IconifyComponentQSST.overlay",
                        false,
                        "IconifyComponentQSPBD.overlay",
                        false,
                        "IconifyComponentQSPBA.overlay",
                        true
                    )
                } else {
                    OverlayUtil.disableOverlay("IconifyComponentQSPBA.overlay")
                }
            }, SWITCH_ANIMATION_DELAY)
        }
}