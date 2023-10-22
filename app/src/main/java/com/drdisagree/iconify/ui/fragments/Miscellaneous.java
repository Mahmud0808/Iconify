package com.drdisagree.iconify.ui.fragments;

import static com.drdisagree.iconify.common.Const.SWITCH_ANIMATION_DELAY;
import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.PROGRESS_WAVE_ANIMATION_SWITCH;
import static com.drdisagree.iconify.common.References.FABRICATED_TABLET_HEADER;

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

        binding.enableTabletLandscape.setChecked(Prefs.getBoolean("IconifyComponentBQS.overlay", false));
        binding.enableTabletLandscape.setOnCheckedChangeListener((buttonView, isChecked) -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isChecked) {
                OverlayUtil.enableOverlay("IconifyComponentBQS.overlay");
            } else {
                OverlayUtil.disableOverlay("IconifyComponentBQS.overlay");
            }
        }, SWITCH_ANIMATION_DELAY));
        binding.tabletLandscape.setOnClickListener(v -> binding.enableTabletLandscape.toggle());

        binding.enableNotchBarKiller.setChecked(Prefs.getBoolean("IconifyComponentNBK.overlay", false));
        binding.enableNotchBarKiller.setOnCheckedChangeListener((buttonView, isChecked) -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isChecked) {
                OverlayUtil.enableOverlay("IconifyComponentNBK.overlay");
            } else {
                OverlayUtil.disableOverlay("IconifyComponentNBK.overlay");
            }
        }, SWITCH_ANIMATION_DELAY));
        binding.notchBarKiller.setOnClickListener(v -> binding.enableNotchBarKiller.toggle());

        binding.enableTabletHeader.setChecked(Prefs.getBoolean(FABRICATED_TABLET_HEADER, false));
        binding.enableTabletHeader.setOnCheckedChangeListener((buttonView, isChecked) -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Prefs.putBoolean(FABRICATED_TABLET_HEADER, isChecked);
            if (isChecked) {
                FabricatedUtil.buildAndEnableOverlay(SYSTEMUI_PACKAGE, FABRICATED_TABLET_HEADER, "bool", "config_use_large_screen_shade_header", "1");
            } else {
                FabricatedUtil.disableOverlay(FABRICATED_TABLET_HEADER);
            }
        }, SWITCH_ANIMATION_DELAY));
        binding.tabletHeader.setOnClickListener(v -> binding.enableTabletHeader.toggle());

        binding.enableAccentPrivacyChip.setChecked(Prefs.getBoolean("IconifyComponentPCBG.overlay", false));
        binding.enableAccentPrivacyChip.setOnCheckedChangeListener((buttonView, isChecked) -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isChecked) {
                OverlayUtil.enableOverlay("IconifyComponentPCBG.overlay");
            } else {
                OverlayUtil.disableOverlay("IconifyComponentPCBG.overlay");
            }
            SystemUtil.restartSystemUI();
        }, SWITCH_ANIMATION_DELAY));
        binding.accentPrivacyChip.setOnClickListener(v -> binding.enableAccentPrivacyChip.toggle());

        AtomicBoolean isProgressWaveContainerClicked = new AtomicBoolean(false);
        binding.disableProgressWave.setChecked(Prefs.getBoolean(PROGRESS_WAVE_ANIMATION_SWITCH, false));
        binding.disableProgressWave.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!SystemUtil.hasStoragePermission()) {
                isProgressWaveContainerClicked.set(false);
                SystemUtil.requestStoragePermission(requireContext());
                binding.disableProgressWave.setChecked(!isChecked);
            } else if (buttonView.isPressed() || isProgressWaveContainerClicked.get()) {
                isProgressWaveContainerClicked.set(false);
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
        binding.disableProgressWaveContainer.setOnClickListener(v -> {
            isProgressWaveContainerClicked.set(true);
            binding.disableProgressWave.toggle();
        });

        return view;
    }
}