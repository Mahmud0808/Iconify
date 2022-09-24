package com.drdisagree.iconify.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.PrefConfig;
import com.drdisagree.iconify.utils.OverlayUtils;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.Objects;

public class MediaPlayer extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.media_player);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle("Media Player");
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        refreshPreview();

        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch mp_accent = findViewById(R.id.mp_accent);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch mp_system = findViewById(R.id.mp_system);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch mp_pitch_black = findViewById(R.id.mp_pitch_black);

        mp_accent.setChecked(PrefConfig.loadPrefBool(Iconify.getAppContext(), "IconifyComponentMPA.overlay"));

        mp_accent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mp_system.setChecked(false);
                    mp_pitch_black.setChecked(false);
                    OverlayUtils.disableOverlay("IconifyComponentMPS.overlay");
                    PrefConfig.savePrefBool(Iconify.getAppContext(), "IconifyComponentMPS.overlay", false);
                    OverlayUtils.disableOverlay("IconifyComponentMPB.overlay");
                    PrefConfig.savePrefBool(Iconify.getAppContext(), "IconifyComponentMPB.overlay", false);
                    OverlayUtils.enableOverlay("IconifyComponentMPA.overlay");
                    PrefConfig.savePrefBool(Iconify.getAppContext(), "IconifyComponentMPA.overlay", true);
                } else {
                    OverlayUtils.disableOverlay("IconifyComponentMPA.overlay");
                    PrefConfig.savePrefBool(Iconify.getAppContext(), "IconifyComponentMPA.overlay", false);
                }
                refreshPreview();
            }
        });

        mp_system.setChecked(PrefConfig.loadPrefBool(Iconify.getAppContext(), "IconifyComponentMPS.overlay"));

        mp_system.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mp_accent.setChecked(false);
                    mp_pitch_black.setChecked(false);
                    OverlayUtils.disableOverlay("IconifyComponentMPA.overlay");
                    PrefConfig.savePrefBool(Iconify.getAppContext(), "IconifyComponentMPA.overlay", false);
                    OverlayUtils.disableOverlay("IconifyComponentMPB.overlay");
                    PrefConfig.savePrefBool(Iconify.getAppContext(), "IconifyComponentMPB.overlay", false);
                    OverlayUtils.enableOverlay("IconifyComponentMPS.overlay");
                    PrefConfig.savePrefBool(Iconify.getAppContext(), "IconifyComponentMPS.overlay", true);
                } else {
                    OverlayUtils.disableOverlay("IconifyComponentMPS.overlay");
                    PrefConfig.savePrefBool(Iconify.getAppContext(), "IconifyComponentMPS.overlay", false);
                }
                refreshPreview();
            }
        });

        mp_pitch_black.setChecked(PrefConfig.loadPrefBool(Iconify.getAppContext(), "IconifyComponentMPB.overlay"));

        mp_pitch_black.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mp_accent.setChecked(false);
                    mp_system.setChecked(false);
                    OverlayUtils.disableOverlay("IconifyComponentMPA.overlay");
                    PrefConfig.savePrefBool(Iconify.getAppContext(), "IconifyComponentMPA.overlay", false);
                    OverlayUtils.disableOverlay("IconifyComponentMPS.overlay");
                    PrefConfig.savePrefBool(Iconify.getAppContext(), "IconifyComponentMPS.overlay", false);
                    OverlayUtils.enableOverlay("IconifyComponentMPB.overlay");
                    PrefConfig.savePrefBool(Iconify.getAppContext(), "IconifyComponentMPB.overlay", true);
                } else {
                    OverlayUtils.disableOverlay("IconifyComponentMPB.overlay");
                    PrefConfig.savePrefBool(Iconify.getAppContext(), "IconifyComponentMPB.overlay", false);
                }
                refreshPreview();
            }
        });
    }

    private void refreshPreview() {
        ImageView preview_accent = findViewById(R.id.media_player_preview_accent);
        ImageView preview_system = findViewById(R.id.media_player_preview_system);
        ImageView preview_black = findViewById(R.id.media_player_preview_black);

        if (PrefConfig.loadPrefBool(Iconify.getAppContext(), "IconifyComponentMPA.overlay")) {
            preview_accent.setVisibility(View.VISIBLE);
            preview_system.setVisibility(View.GONE);
            preview_black.setVisibility(View.GONE);
        } else if (PrefConfig.loadPrefBool(Iconify.getAppContext(), "IconifyComponentMPB.overlay")) {
            preview_black.setVisibility(View.VISIBLE);
            preview_accent.setVisibility(View.GONE);
            preview_system.setVisibility(View.GONE);
        } else {
            preview_system.setVisibility(View.VISIBLE);
            preview_accent.setVisibility(View.GONE);
            preview_black.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}