package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Const.SWITCH_ANIMATION_DELAY;
import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;
import static com.drdisagree.iconify.common.References.FABRICATED_MUSIC_AMPLITUDE;
import static com.drdisagree.iconify.common.References.FABRICATED_MUSIC_PHASE;
import static com.drdisagree.iconify.common.References.FABRICATED_TABLET_HEADER;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.databinding.ActivityMiscellaneousBinding;
import com.drdisagree.iconify.ui.utils.ViewHelper;
import com.drdisagree.iconify.utils.FabricatedUtil;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.drdisagree.iconify.utils.SystemUtil;

public class Miscellaneous extends BaseActivity {

    private ActivityMiscellaneousBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMiscellaneousBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Header
        ViewHelper.setHeader(this, binding.header.toolbar, R.string.activity_title_miscellaneous);

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

        binding.disableProgressWave.setChecked(FabricatedUtil.isOverlayEnabled(FABRICATED_MUSIC_AMPLITUDE));
        binding.disableProgressWave.setOnCheckedChangeListener((buttonView, isChecked) -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isChecked) {
                FabricatedUtil.buildAndEnableOverlays(
                        new Object[]{SYSTEMUI_PACKAGE, FABRICATED_MUSIC_AMPLITUDE, "dimen", "qs_media_seekbar_progress_amplitude", "0dp"},
                        new Object[]{SYSTEMUI_PACKAGE, FABRICATED_MUSIC_PHASE, "dimen", "qs_media_seekbar_progress_phase", "0dp"}
                );
            } else {
                FabricatedUtil.disableOverlays(FABRICATED_MUSIC_AMPLITUDE, FABRICATED_MUSIC_PHASE);
            }
            SystemUtil.restartSystemUI();
        }, SWITCH_ANIMATION_DELAY));
        binding.disableProgressWaveContainer.setOnClickListener(v -> binding.disableProgressWave.toggle());
    }
}