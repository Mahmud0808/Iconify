package com.drdisagree.iconify.ui.activities;

import android.os.Bundle;
import android.view.View;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.databinding.ActivityMediaPlayerBinding;
import com.drdisagree.iconify.ui.utils.ViewBindingHelpers;
import com.drdisagree.iconify.utils.OverlayUtil;

public class MediaPlayer extends BaseActivity {

    private ActivityMediaPlayerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMediaPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Header
        ViewBindingHelpers.setHeader(this, binding.header.toolbar, R.string.activity_title_media_player);

        refreshPreview();

        binding.mpAccent.setChecked(Prefs.getBoolean("IconifyComponentMPA.overlay"));

        binding.mpAccent.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.mpSystem.setChecked(false);
                binding.mpPitchBlack.setChecked(false);
                OverlayUtil.changeOverlayState("IconifyComponentMPS.overlay", false, "IconifyComponentMPB.overlay", false, "IconifyComponentMPA.overlay", true);
            } else {
                OverlayUtil.disableOverlay("IconifyComponentMPA.overlay");
            }
            refreshPreview();
        });

        binding.mpSystem.setChecked(Prefs.getBoolean("IconifyComponentMPS.overlay"));

        binding.mpSystem.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.mpAccent.setChecked(false);
                binding.mpPitchBlack.setChecked(false);
                OverlayUtil.changeOverlayState("IconifyComponentMPA.overlay", false, "IconifyComponentMPB.overlay", false, "IconifyComponentMPS.overlay", true);
            } else {
                OverlayUtil.disableOverlay("IconifyComponentMPS.overlay");
            }
            refreshPreview();
        });

        binding.mpPitchBlack.setChecked(Prefs.getBoolean("IconifyComponentMPB.overlay"));

        binding.mpPitchBlack.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.mpAccent.setChecked(false);
                binding.mpSystem.setChecked(false);
                OverlayUtil.changeOverlayState("IconifyComponentMPA.overlay", false, "IconifyComponentMPS.overlay", false, "IconifyComponentMPB.overlay", true);
            } else {
                OverlayUtil.disableOverlay("IconifyComponentMPB.overlay");
            }
            refreshPreview();
        });
    }

    private void refreshPreview() {
        binding.mpAccentPreview.previewMpAccent.setVisibility(View.GONE);
        binding.mpPitchBlackPreview.previewMpBlack.setVisibility(View.GONE);
        binding.mpSystemPreview.previewMpSystem.setVisibility(View.GONE);

        if (Prefs.getBoolean("IconifyComponentMPA.overlay"))
            binding.mpAccentPreview.previewMpAccent.setVisibility(View.VISIBLE);
        else if (Prefs.getBoolean("IconifyComponentMPB.overlay"))
            binding.mpPitchBlackPreview.previewMpBlack.setVisibility(View.VISIBLE);
        else
            binding.mpSystemPreview.previewMpSystem.setVisibility(View.VISIBLE);
    }
}