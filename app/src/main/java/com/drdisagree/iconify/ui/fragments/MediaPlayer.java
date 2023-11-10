package com.drdisagree.iconify.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.databinding.FragmentMediaPlayerBinding;
import com.drdisagree.iconify.ui.base.BaseFragment;
import com.drdisagree.iconify.ui.utils.ViewHelper;
import com.drdisagree.iconify.utils.overlay.OverlayUtil;

public class MediaPlayer extends BaseFragment {

    private FragmentMediaPlayerBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMediaPlayerBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Header
        ViewHelper.setHeader(requireContext(), getParentFragmentManager(), binding.header.toolbar, R.string.activity_title_media_player);

        refreshPreview();

        binding.mpAccent.setSwitchChecked(Prefs.getBoolean("IconifyComponentMPA.overlay"));
        binding.mpAccent.setSwitchChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.mpSystem.setSwitchChecked(false);
                binding.mpPitchBlack.setSwitchChecked(false);
                OverlayUtil.changeOverlayState("IconifyComponentMPS.overlay", false, "IconifyComponentMPB.overlay", false, "IconifyComponentMPA.overlay", true);
            } else {
                OverlayUtil.disableOverlay("IconifyComponentMPA.overlay");
            }
            refreshPreview();
        });

        binding.mpSystem.setSwitchChecked(Prefs.getBoolean("IconifyComponentMPS.overlay"));
        binding.mpSystem.setSwitchChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.mpAccent.setSwitchChecked(false);
                binding.mpPitchBlack.setSwitchChecked(false);
                OverlayUtil.changeOverlayState("IconifyComponentMPA.overlay", false, "IconifyComponentMPB.overlay", false, "IconifyComponentMPS.overlay", true);
            } else {
                OverlayUtil.disableOverlay("IconifyComponentMPS.overlay");
            }
            refreshPreview();
        });

        binding.mpPitchBlack.setSwitchChecked(Prefs.getBoolean("IconifyComponentMPB.overlay"));
        binding.mpPitchBlack.setSwitchChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.mpAccent.setSwitchChecked(false);
                binding.mpSystem.setSwitchChecked(false);
                OverlayUtil.changeOverlayState("IconifyComponentMPA.overlay", false, "IconifyComponentMPS.overlay", false, "IconifyComponentMPB.overlay", true);
            } else {
                OverlayUtil.disableOverlay("IconifyComponentMPB.overlay");
            }
            refreshPreview();
        });

        return view;
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