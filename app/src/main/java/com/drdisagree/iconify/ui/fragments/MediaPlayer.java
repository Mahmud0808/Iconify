package com.drdisagree.iconify.ui.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.ui.utils.FragmentHelper;
import com.drdisagree.iconify.utils.OverlayUtil;

public class MediaPlayer extends Fragment {

    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_media_player, container, false);

        // Header
        FragmentHelper.initHeader((AppCompatActivity) requireActivity(), view, R.string.activity_title_media_player, getParentFragmentManager());

        refreshPreview();

        @SuppressLint("UseSwitchCompatOrMaterialCode") android.widget.Switch mp_accent = view.findViewById(R.id.mp_accent);
        @SuppressLint("UseSwitchCompatOrMaterialCode") android.widget.Switch mp_system = view.findViewById(R.id.mp_system);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch mp_pitch_black = view.findViewById(R.id.mp_pitch_black);

        mp_accent.setChecked(Prefs.getBoolean("IconifyComponentMPA.overlay"));

        mp_accent.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                mp_system.setChecked(false);
                mp_pitch_black.setChecked(false);
                OverlayUtil.disableOverlay("IconifyComponentMPS.overlay");
                OverlayUtil.disableOverlay("IconifyComponentMPB.overlay");
                OverlayUtil.enableOverlay("IconifyComponentMPA.overlay");
            } else {
                OverlayUtil.disableOverlay("IconifyComponentMPA.overlay");
            }
            refreshPreview();
        });

        mp_system.setChecked(Prefs.getBoolean("IconifyComponentMPS.overlay"));

        mp_system.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                mp_accent.setChecked(false);
                mp_pitch_black.setChecked(false);
                OverlayUtil.disableOverlay("IconifyComponentMPA.overlay");
                OverlayUtil.disableOverlay("IconifyComponentMPB.overlay");
                OverlayUtil.enableOverlay("IconifyComponentMPS.overlay");
            } else {
                OverlayUtil.disableOverlay("IconifyComponentMPS.overlay");
            }
            refreshPreview();
        });

        mp_pitch_black.setChecked(Prefs.getBoolean("IconifyComponentMPB.overlay"));

        mp_pitch_black.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                mp_accent.setChecked(false);
                mp_system.setChecked(false);
                OverlayUtil.disableOverlay("IconifyComponentMPA.overlay");
                OverlayUtil.disableOverlay("IconifyComponentMPS.overlay");
                OverlayUtil.enableOverlay("IconifyComponentMPB.overlay");
            } else {
                OverlayUtil.disableOverlay("IconifyComponentMPB.overlay");
            }
            refreshPreview();
        });

        return view;
    }

    private void refreshPreview() {
        view.findViewById(R.id.preview_mp_accent).setVisibility(View.GONE);
        view.findViewById(R.id.preview_mp_black).setVisibility(View.GONE);
        view.findViewById(R.id.preview_mp_system).setVisibility(View.GONE);

        if (Prefs.getBoolean("IconifyComponentMPA.overlay"))
            view.findViewById(R.id.preview_mp_accent).setVisibility(View.VISIBLE);
        else if (Prefs.getBoolean("IconifyComponentMPB.overlay"))
            view.findViewById(R.id.preview_mp_black).setVisibility(View.VISIBLE);
        else view.findViewById(R.id.preview_mp_system).setVisibility(View.VISIBLE);
    }
}