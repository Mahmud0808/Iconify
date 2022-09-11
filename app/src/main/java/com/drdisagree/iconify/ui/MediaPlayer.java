package com.drdisagree.iconify.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.utils.OverlayUtils;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.List;
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

        List<String> enabledOverlays = OverlayUtils.getEnabledOverlayList();

        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch mp_accent = findViewById(R.id.mp_accent);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch mp_system = findViewById(R.id.mp_system);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch mp_pitch_black = findViewById(R.id.mp_pitch_black);

        if (OverlayUtils.isOverlayEnabled(enabledOverlays, "IconifyComponentMPA.overlay"))
            mp_accent.setChecked(true);
        else
            mp_accent.setChecked(false);

        mp_accent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mp_system.setChecked(false);
                    mp_pitch_black.setChecked(false);
                    OverlayUtils.enableOverlay(enabledOverlays, "IconifyComponentMPA.overlay");
                    OverlayUtils.disableOverlay("IconifyComponentMPS.overlay");
                    OverlayUtils.disableOverlay("IconifyComponentMPB.overlay");
                } else {
                    OverlayUtils.disableOverlay("IconifyComponentMPA.overlay");
                }
            }
        });

        if (OverlayUtils.isOverlayEnabled(enabledOverlays, "IconifyComponentMPS.overlay"))
            mp_system.setChecked(true);
        else
            mp_system.setChecked(false);

        mp_system.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mp_accent.setChecked(false);
                    mp_pitch_black.setChecked(false);
                    OverlayUtils.enableOverlay(enabledOverlays, "IconifyComponentMPS.overlay");
                    OverlayUtils.disableOverlay("IconifyComponentMPA.overlay");
                    OverlayUtils.disableOverlay("IconifyComponentMPB.overlay");
                } else {
                    OverlayUtils.disableOverlay("IconifyComponentMPS.overlay");
                }
            }
        });

        if (OverlayUtils.isOverlayEnabled(enabledOverlays, "IconifyComponentMPB.overlay"))
            mp_pitch_black.setChecked(true);
        else
            mp_pitch_black.setChecked(false);

        mp_pitch_black.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mp_accent.setChecked(false);
                    mp_system.setChecked(false);
                    OverlayUtils.enableOverlay(enabledOverlays, "IconifyComponentMPB.overlay");
                    OverlayUtils.disableOverlay("IconifyComponentMPA.overlay");
                    OverlayUtils.disableOverlay("IconifyComponentMPS.overlay");
                } else {
                    OverlayUtils.disableOverlay("IconifyComponentMPB.overlay");
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}