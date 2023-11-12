package com.drdisagree.iconify.ui.fragments;

import static com.drdisagree.iconify.common.Const.FRAMEWORK_PACKAGE;
import static com.drdisagree.iconify.common.Const.SWITCH_ANIMATION_DELAY;
import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.NOTCH_BAR_KILLER_SWITCH;
import static com.drdisagree.iconify.common.Preferences.PROGRESS_WAVE_ANIMATION_SWITCH;
import static com.drdisagree.iconify.common.Preferences.TABLET_LANDSCAPE_SWITCH;
import static com.drdisagree.iconify.common.References.FABRICATED_TABLET_HEADER;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.databinding.FragmentMiscellaneousBinding;
import com.drdisagree.iconify.ui.base.BaseFragment;
import com.drdisagree.iconify.ui.utils.ViewHelper;
import com.drdisagree.iconify.utils.SystemUtil;
import com.drdisagree.iconify.utils.overlay.FabricatedUtil;
import com.drdisagree.iconify.utils.overlay.OverlayUtil;
import com.drdisagree.iconify.utils.overlay.manager.resource.ResourceEntry;
import com.drdisagree.iconify.utils.overlay.manager.resource.ResourceManager;

import java.util.concurrent.atomic.AtomicBoolean;

public class Miscellaneous extends BaseFragment {

    private FragmentMiscellaneousBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMiscellaneousBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Header
        ViewHelper.setHeader(requireContext(), getParentFragmentManager(), binding.header.toolbar, R.string.activity_title_miscellaneous);

        // Tablet landscape
        AtomicBoolean isTabletLandscapeContainerClicked = new AtomicBoolean(false);
        binding.tabletLandscape.setSwitchChecked(Prefs.getBoolean(TABLET_LANDSCAPE_SWITCH, false));
        binding.tabletLandscape.setSwitchChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed() || isTabletLandscapeContainerClicked.get()) {
                isTabletLandscapeContainerClicked.set(false);

                if (!SystemUtil.hasStoragePermission()) {
                    SystemUtil.requestStoragePermission(requireContext());
                    binding.tabletLandscape.setSwitchChecked(!isChecked);
                    return;
                }

                Prefs.putBoolean(TABLET_LANDSCAPE_SWITCH, isChecked);

                ResourceEntry resourceEntry1 = new ResourceEntry(SYSTEMUI_PACKAGE, "bool", "config_use_split_notification_shade", "true");
                ResourceEntry resourceEntry2 = new ResourceEntry(SYSTEMUI_PACKAGE, "bool", "config_skinnyNotifsInLandscape", "false");
                ResourceEntry resourceEntry3 = new ResourceEntry(SYSTEMUI_PACKAGE, "bool", "can_use_one_handed_bouncer", "true");
                ResourceEntry resourceEntry4 = new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "notifications_top_padding_split_shade", "40.0dip");
                ResourceEntry resourceEntry5 = new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "split_shade_notifications_scrim_margin_bottom", "14.0dip");
                ResourceEntry resourceEntry6 = new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "qs_header_system_icons_area_height", "0.0dip");
                ResourceEntry resourceEntry7 = new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "qs_panel_padding_top", "0.0dip");
                ResourceEntry resourceEntry8 = new ResourceEntry(SYSTEMUI_PACKAGE, "integer", "quick_settings_num_columns", "2");
                ResourceEntry resourceEntry9 = new ResourceEntry(SYSTEMUI_PACKAGE, "integer", "quick_qs_panel_max_rows", "2");
                ResourceEntry resourceEntry10 = new ResourceEntry(SYSTEMUI_PACKAGE, "integer", "quick_qs_panel_max_tiles", "4");
                ResourceEntry resourceEntry11 = new ResourceEntry(FRAMEWORK_PACKAGE, "bool", "config_fillMainBuiltInDisplayCutout", "true");
                ResourceEntry resourceEntry12 = new ResourceEntry(FRAMEWORK_PACKAGE, "string", "config_mainBuiltInDisplayCutout", "M 0,0 L 0, 0 C 0,0 0,0 0,0");
                ResourceEntry resourceEntry13 = new ResourceEntry(FRAMEWORK_PACKAGE, "string", "config_mainBuiltInDisplayCutoutRectApproximation", "@string/config_mainBuiltInDisplayCutout");

                resourceEntry1.setLandscape(true);
                resourceEntry2.setLandscape(true);
                resourceEntry3.setLandscape(true);
                resourceEntry4.setLandscape(true);
                resourceEntry5.setLandscape(true);
                resourceEntry6.setLandscape(true);
                resourceEntry7.setLandscape(true);
                resourceEntry8.setLandscape(true);
                resourceEntry9.setLandscape(true);
                resourceEntry10.setLandscape(true);
                resourceEntry11.setLandscape(true);
                resourceEntry12.setLandscape(true);
                resourceEntry13.setLandscape(true);

                if (isChecked) {
                    ResourceManager.buildOverlayWithResource(
                            requireContext(),
                            resourceEntry1, resourceEntry2, resourceEntry3, resourceEntry4,
                            resourceEntry5, resourceEntry6, resourceEntry7, resourceEntry8,
                            resourceEntry9, resourceEntry10, resourceEntry11, resourceEntry12,
                            resourceEntry13
                    );
                } else {
                    ResourceManager.removeResourceFromOverlay(
                            requireContext(),
                            resourceEntry1, resourceEntry2, resourceEntry3, resourceEntry4,
                            resourceEntry5, resourceEntry6, resourceEntry7, resourceEntry8,
                            resourceEntry9, resourceEntry10, resourceEntry11, resourceEntry12,
                            resourceEntry13
                    );
                }
            }
        });
        binding.tabletLandscape.setBeforeSwitchChangeListener(() -> isTabletLandscapeContainerClicked.set(true));

        // Notch bar killer
        AtomicBoolean isNotchBarKillerContainerClicked = new AtomicBoolean(false);
        binding.notchBarKiller.setSwitchChecked(Prefs.getBoolean(NOTCH_BAR_KILLER_SWITCH, false));
        binding.notchBarKiller.setSwitchChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed() || isNotchBarKillerContainerClicked.get()) {
                isNotchBarKillerContainerClicked.set(false);

                if (!SystemUtil.hasStoragePermission()) {
                    SystemUtil.requestStoragePermission(requireContext());
                    binding.notchBarKiller.setSwitchChecked(!isChecked);
                    return;
                }

                Prefs.putBoolean(NOTCH_BAR_KILLER_SWITCH, isChecked);

                if (isChecked) {
                    ResourceManager.buildOverlayWithResource(
                            requireContext(),
                            new ResourceEntry(FRAMEWORK_PACKAGE, "bool", "config_fillMainBuiltInDisplayCutout", "true"),
                            new ResourceEntry(FRAMEWORK_PACKAGE, "string", "config_mainBuiltInDisplayCutout", "M 0,0 L 0, 0 C 0,0 0,0 0,0"),
                            new ResourceEntry(FRAMEWORK_PACKAGE, "string", "config_mainBuiltInDisplayCutoutRectApproximation", "@string/config_mainBuiltInDisplayCutout")
                    );
                } else {
                    ResourceManager.removeResourceFromOverlay(
                            requireContext(),
                            new ResourceEntry(FRAMEWORK_PACKAGE, "bool", "config_fillMainBuiltInDisplayCutout"),
                            new ResourceEntry(FRAMEWORK_PACKAGE, "string", "config_mainBuiltInDisplayCutout"),
                            new ResourceEntry(FRAMEWORK_PACKAGE, "string", "config_mainBuiltInDisplayCutoutRectApproximation")
                    );
                }
            }
        });
        binding.notchBarKiller.setBeforeSwitchChangeListener(() -> isNotchBarKillerContainerClicked.set(true));

        // Tablet header
        binding.tabletHeader.setSwitchChecked(Prefs.getBoolean(FABRICATED_TABLET_HEADER, false));
        binding.tabletHeader.setSwitchChangeListener((buttonView, isChecked) -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Prefs.putBoolean(FABRICATED_TABLET_HEADER, isChecked);
            if (isChecked) {
                FabricatedUtil.buildAndEnableOverlay(SYSTEMUI_PACKAGE, FABRICATED_TABLET_HEADER, "bool", "config_use_large_screen_shade_header", "1");
            } else {
                FabricatedUtil.disableOverlay(FABRICATED_TABLET_HEADER);
            }
        }, SWITCH_ANIMATION_DELAY));

        // Accent privacy chip
        binding.accentPrivacyChip.setSwitchChecked(Prefs.getBoolean("IconifyComponentPCBG.overlay", false));
        binding.accentPrivacyChip.setSwitchChangeListener((buttonView, isChecked) -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isChecked) {
                OverlayUtil.enableOverlay("IconifyComponentPCBG.overlay");
            } else {
                OverlayUtil.disableOverlay("IconifyComponentPCBG.overlay");
            }
            SystemUtil.restartSystemUI();
        }, SWITCH_ANIMATION_DELAY));

        // Progress wave animation
        if (Build.VERSION.SDK_INT < 33) {
            binding.sectionTitleMediaPlayer.setVisibility(View.GONE);
            binding.disableProgressWave.setVisibility(View.GONE);
        }

        AtomicBoolean isProgressWaveContainerClicked = new AtomicBoolean(false);
        binding.disableProgressWave.setSwitchChecked(Prefs.getBoolean(PROGRESS_WAVE_ANIMATION_SWITCH, false));
        binding.disableProgressWave.setSwitchChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed() || isProgressWaveContainerClicked.get()) {
                isProgressWaveContainerClicked.set(false);

                if (!SystemUtil.hasStoragePermission()) {
                    SystemUtil.requestStoragePermission(requireContext());
                    binding.disableProgressWave.setSwitchChecked(!isChecked);
                    return;
                }

                Prefs.putBoolean(PROGRESS_WAVE_ANIMATION_SWITCH, isChecked);

                if (isChecked) {
                    ResourceManager.buildOverlayWithResource(
                            requireContext(),
                            new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "qs_media_seekbar_progress_amplitude", "0dp"),
                            new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "qs_media_seekbar_progress_phase", "0dp")
                    );
                } else {
                    ResourceManager.removeResourceFromOverlay(
                            requireContext(),
                            new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "qs_media_seekbar_progress_amplitude"),
                            new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "qs_media_seekbar_progress_phase")
                    );
                }

                new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::restartSystemUI, SWITCH_ANIMATION_DELAY);
            }
        });
        binding.disableProgressWave.setBeforeSwitchChangeListener(() -> isProgressWaveContainerClicked.set(true));

        return view;
    }
}