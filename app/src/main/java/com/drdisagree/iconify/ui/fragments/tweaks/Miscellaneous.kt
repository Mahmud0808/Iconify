package com.drdisagree.iconify.ui.fragments.tweaks

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Const.FRAMEWORK_PACKAGE
import com.drdisagree.iconify.common.Const.SWITCH_ANIMATION_DELAY
import com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.common.Preferences.NOTCH_BAR_KILLER_SWITCH
import com.drdisagree.iconify.common.Preferences.PROGRESS_WAVE_ANIMATION_SWITCH
import com.drdisagree.iconify.common.Preferences.TABLET_LANDSCAPE_SWITCH
import com.drdisagree.iconify.common.References.FABRICATED_TABLET_HEADER
import com.drdisagree.iconify.config.RPrefs
import com.drdisagree.iconify.config.RPrefs.getBoolean
import com.drdisagree.iconify.databinding.FragmentMiscellaneousBinding
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader
import com.drdisagree.iconify.utils.SystemUtils.hasStoragePermission
import com.drdisagree.iconify.utils.SystemUtils.requestStoragePermission
import com.drdisagree.iconify.utils.SystemUtils.restartSystemUI
import com.drdisagree.iconify.utils.overlay.FabricatedUtils
import com.drdisagree.iconify.utils.overlay.FabricatedUtils.buildAndEnableOverlay
import com.drdisagree.iconify.utils.overlay.OverlayUtils
import com.drdisagree.iconify.utils.overlay.OverlayUtils.enableOverlay
import com.drdisagree.iconify.utils.overlay.manager.resource.ResourceEntry
import com.drdisagree.iconify.utils.overlay.manager.resource.ResourceManager.buildOverlayWithResource
import com.drdisagree.iconify.utils.overlay.manager.resource.ResourceManager.removeResourceFromOverlay
import java.util.concurrent.atomic.AtomicBoolean

class Miscellaneous : BaseFragment() {

    private lateinit var binding: FragmentMiscellaneousBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMiscellaneousBinding.inflate(inflater, container, false)
        val view: View = binding.getRoot()

        // Header
        setHeader(
            requireContext(),
            getParentFragmentManager(),
            binding.header.toolbar,
            R.string.activity_title_miscellaneous
        )

        // Tablet landscape
        val isTabletLandscapeContainerClicked = AtomicBoolean(false)

        binding.tabletLandscape.isSwitchChecked = getBoolean(TABLET_LANDSCAPE_SWITCH, false)
        binding.tabletLandscape.setSwitchChangeListener { buttonView: CompoundButton, isChecked: Boolean ->
            if (buttonView.isPressed || isTabletLandscapeContainerClicked.get()) {
                isTabletLandscapeContainerClicked.set(false)

                if (!hasStoragePermission()) {
                    requestStoragePermission(requireContext())
                    binding.tabletLandscape.isSwitchChecked = !isChecked

                    return@setSwitchChangeListener
                }

                RPrefs.putBoolean(TABLET_LANDSCAPE_SWITCH, isChecked)

                val resourceEntry1 = ResourceEntry(
                    SYSTEMUI_PACKAGE,
                    "bool",
                    "config_use_split_notification_shade",
                    "true"
                )
                val resourceEntry2 = ResourceEntry(
                    SYSTEMUI_PACKAGE,
                    "bool",
                    "config_skinnyNotifsInLandscape",
                    "false"
                )
                val resourceEntry3 = ResourceEntry(
                    SYSTEMUI_PACKAGE,
                    "bool",
                    "can_use_one_handed_bouncer",
                    "true"
                )
                val resourceEntry4 = ResourceEntry(
                    SYSTEMUI_PACKAGE,
                    "dimen",
                    "notifications_top_padding_split_shade",
                    "40.0dip"
                )
                val resourceEntry5 = ResourceEntry(
                    SYSTEMUI_PACKAGE,
                    "dimen",
                    "split_shade_notifications_scrim_margin_bottom",
                    "14.0dip"
                )
                val resourceEntry6 = ResourceEntry(
                    SYSTEMUI_PACKAGE,
                    "dimen",
                    "qs_header_system_icons_area_height",
                    "0.0dip"
                )
                val resourceEntry7 = ResourceEntry(
                    SYSTEMUI_PACKAGE,
                    "dimen",
                    "qs_panel_padding_top",
                    "0.0dip"
                )
                val resourceEntry8 = ResourceEntry(
                    SYSTEMUI_PACKAGE,
                    "integer",
                    "quick_settings_num_columns",
                    "2"
                )
                val resourceEntry9 = ResourceEntry(
                    SYSTEMUI_PACKAGE,
                    "integer",
                    "quick_qs_panel_max_rows",
                    "2"
                )
                val resourceEntry10 = ResourceEntry(
                    SYSTEMUI_PACKAGE,
                    "integer",
                    "quick_qs_panel_max_tiles",
                    "4"
                )
                val resourceEntry11 = ResourceEntry(
                    FRAMEWORK_PACKAGE,
                    "bool",
                    "config_fillMainBuiltInDisplayCutout",
                    "true"
                )
                val resourceEntry12 = ResourceEntry(
                    FRAMEWORK_PACKAGE,
                    "string",
                    "config_mainBuiltInDisplayCutout",
                    "M 0,0 L 0, 0 C 0,0 0,0 0,0"
                )
                val resourceEntry13 = ResourceEntry(
                    FRAMEWORK_PACKAGE,
                    "string",
                    "config_mainBuiltInDisplayCutoutRectApproximation",
                    "@string/config_mainBuiltInDisplayCutout"
                )

                resourceEntry1.setLandscape(true)
                resourceEntry2.setLandscape(true)
                resourceEntry3.setLandscape(true)
                resourceEntry4.setLandscape(true)
                resourceEntry5.setLandscape(true)
                resourceEntry6.setLandscape(true)
                resourceEntry7.setLandscape(true)
                resourceEntry8.setLandscape(true)
                resourceEntry9.setLandscape(true)
                resourceEntry10.setLandscape(true)
                resourceEntry11.setLandscape(true)
                resourceEntry12.setLandscape(true)
                resourceEntry13.setLandscape(true)

                if (isChecked) {
                    buildOverlayWithResource(
                        requireContext(),
                        resourceEntry1,
                        resourceEntry2,
                        resourceEntry3,
                        resourceEntry4,
                        resourceEntry5,
                        resourceEntry6,
                        resourceEntry7,
                        resourceEntry8,
                        resourceEntry9,
                        resourceEntry10,
                        resourceEntry11,
                        resourceEntry12,
                        resourceEntry13
                    )
                } else {
                    removeResourceFromOverlay(
                        requireContext(),
                        resourceEntry1,
                        resourceEntry2,
                        resourceEntry3,
                        resourceEntry4,
                        resourceEntry5,
                        resourceEntry6,
                        resourceEntry7,
                        resourceEntry8,
                        resourceEntry9,
                        resourceEntry10,
                        resourceEntry11,
                        resourceEntry12,
                        resourceEntry13
                    )
                }
            }
        }
        binding.tabletLandscape.setBeforeSwitchChangeListener {
            isTabletLandscapeContainerClicked.set(true)
        }

        // Notch bar killer
        val isNotchBarKillerContainerClicked = AtomicBoolean(false)

        binding.notchBarKiller.isSwitchChecked = getBoolean(NOTCH_BAR_KILLER_SWITCH)
        binding.notchBarKiller.setSwitchChangeListener { buttonView: CompoundButton, isChecked: Boolean ->
            if (buttonView.isPressed || isNotchBarKillerContainerClicked.get()) {
                isNotchBarKillerContainerClicked.set(false)

                if (!hasStoragePermission()) {
                    requestStoragePermission(requireContext())
                    binding.notchBarKiller.isSwitchChecked = !isChecked

                    return@setSwitchChangeListener
                }

                RPrefs.putBoolean(NOTCH_BAR_KILLER_SWITCH, isChecked)

                if (isChecked) {
                    buildOverlayWithResource(
                        requireContext(),
                        ResourceEntry(
                            FRAMEWORK_PACKAGE,
                            "bool",
                            "config_fillMainBuiltInDisplayCutout",
                            "true"
                        ),
                        ResourceEntry(
                            FRAMEWORK_PACKAGE,
                            "string",
                            "config_mainBuiltInDisplayCutout",
                            "M 0,0 L 0, 0 C 0,0 0,0 0,0"
                        ),
                        ResourceEntry(
                            FRAMEWORK_PACKAGE,
                            "string",
                            "config_mainBuiltInDisplayCutoutRectApproximation",
                            "@string/config_mainBuiltInDisplayCutout"
                        )
                    )
                } else {
                    removeResourceFromOverlay(
                        requireContext(),
                        ResourceEntry(
                            FRAMEWORK_PACKAGE,
                            "bool",
                            "config_fillMainBuiltInDisplayCutout"
                        ),
                        ResourceEntry(
                            FRAMEWORK_PACKAGE,
                            "string",
                            "config_mainBuiltInDisplayCutout"
                        ),
                        ResourceEntry(
                            FRAMEWORK_PACKAGE,
                            "string",
                            "config_mainBuiltInDisplayCutoutRectApproximation"
                        )
                    )
                }
            }
        }
        binding.notchBarKiller.setBeforeSwitchChangeListener {
            isNotchBarKillerContainerClicked.set(true)
        }

        // Tablet header
        binding.tabletHeader.isSwitchChecked = getBoolean(FABRICATED_TABLET_HEADER)
        binding.tabletHeader.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            Handler(Looper.getMainLooper()).postDelayed(
                {
                    RPrefs.putBoolean(FABRICATED_TABLET_HEADER, isChecked)

                    if (isChecked) {
                        buildAndEnableOverlay(
                            SYSTEMUI_PACKAGE,
                            FABRICATED_TABLET_HEADER,
                            "bool",
                            "config_use_large_screen_shade_header",
                            "1"
                        )
                    } else {
                        FabricatedUtils.disableOverlay(FABRICATED_TABLET_HEADER)
                    }
                }, SWITCH_ANIMATION_DELAY
            )
        }

        // Accent privacy chip
        binding.accentPrivacyChip.isSwitchChecked = getBoolean("IconifyComponentPCBG.overlay")
        binding.accentPrivacyChip.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            Handler(Looper.getMainLooper()).postDelayed(
                {
                    if (isChecked) {
                        enableOverlay("IconifyComponentPCBG.overlay")
                    } else {
                        OverlayUtils.disableOverlay("IconifyComponentPCBG.overlay")
                    }

                    restartSystemUI()
                }, SWITCH_ANIMATION_DELAY
            )
        }

        // Progress wave animation
        if (Build.VERSION.SDK_INT < 33) {
            binding.sectionTitleMediaPlayer.visibility = View.GONE
            binding.disableProgressWave.visibility = View.GONE
        }

        val isProgressWaveContainerClicked = AtomicBoolean(false)

        binding.disableProgressWave.isSwitchChecked = getBoolean(PROGRESS_WAVE_ANIMATION_SWITCH)
        binding.disableProgressWave.setSwitchChangeListener { buttonView: CompoundButton, isChecked: Boolean ->
            if (buttonView.isPressed || isProgressWaveContainerClicked.get()) {
                isProgressWaveContainerClicked.set(false)

                if (!hasStoragePermission()) {
                    requestStoragePermission(requireContext())
                    binding.disableProgressWave.isSwitchChecked = !isChecked

                    return@setSwitchChangeListener
                }

                RPrefs.putBoolean(PROGRESS_WAVE_ANIMATION_SWITCH, isChecked)

                if (isChecked) {
                    buildOverlayWithResource(
                        requireContext(),
                        ResourceEntry(
                            SYSTEMUI_PACKAGE,
                            "dimen",
                            "qs_media_seekbar_progress_amplitude",
                            "0dp"
                        ),
                        ResourceEntry(
                            SYSTEMUI_PACKAGE,
                            "dimen",
                            "qs_media_seekbar_progress_phase",
                            "0dp"
                        )
                    )
                } else {
                    removeResourceFromOverlay(
                        requireContext(),
                        ResourceEntry(
                            SYSTEMUI_PACKAGE,
                            "dimen",
                            "qs_media_seekbar_progress_amplitude"
                        ),
                        ResourceEntry(
                            SYSTEMUI_PACKAGE,
                            "dimen",
                            "qs_media_seekbar_progress_phase"
                        )
                    )
                }

                Handler(Looper.getMainLooper()).postDelayed(
                    { restartSystemUI() },
                    SWITCH_ANIMATION_DELAY
                )
            }
        }
        binding.disableProgressWave.setBeforeSwitchChangeListener {
            isProgressWaveContainerClicked.set(true)
        }

        return view
    }
}