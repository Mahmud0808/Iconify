package com.drdisagree.iconify.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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

        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch mp_accent = findViewById(R.id.mp_accent);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch mp_system = findViewById(R.id.mp_system);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch mp_pitch_black = findViewById(R.id.mp_pitch_black);

        mp_accent.setChecked(PrefConfig.loadPrefBool(getApplicationContext(), "IconifyComponentMPA.overlay"));

        mp_accent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mp_system.setChecked(false);
                    mp_pitch_black.setChecked(false);
                    OverlayUtils.disableOverlay("IconifyComponentMPS.overlay");
                    PrefConfig.savePrefBool(getApplicationContext(), "IconifyComponentMPS.overlay", false);
                    OverlayUtils.disableOverlay("IconifyComponentMPB.overlay");
                    PrefConfig.savePrefBool(getApplicationContext(), "IconifyComponentMPB.overlay", false);
                    OverlayUtils.enableOverlay("IconifyComponentMPA.overlay");
                    PrefConfig.savePrefBool(getApplicationContext(), "IconifyComponentMPA.overlay", true);
                } else {
                    OverlayUtils.disableOverlay("IconifyComponentMPA.overlay");
                    PrefConfig.savePrefBool(getApplicationContext(), "IconifyComponentMPA.overlay", false);
                }
            }
        });

        mp_system.setChecked(PrefConfig.loadPrefBool(getApplicationContext(), "IconifyComponentMPS.overlay"));

        mp_system.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mp_accent.setChecked(false);
                    mp_pitch_black.setChecked(false);
                    OverlayUtils.disableOverlay("IconifyComponentMPA.overlay");
                    PrefConfig.savePrefBool(getApplicationContext(), "IconifyComponentMPA.overlay", false);
                    OverlayUtils.disableOverlay("IconifyComponentMPB.overlay");
                    PrefConfig.savePrefBool(getApplicationContext(), "IconifyComponentMPB.overlay", false);
                    OverlayUtils.enableOverlay("IconifyComponentMPS.overlay");
                    PrefConfig.savePrefBool(getApplicationContext(), "IconifyComponentMPS.overlay", true);
                } else {
                    OverlayUtils.disableOverlay("IconifyComponentMPS.overlay");
                    PrefConfig.savePrefBool(getApplicationContext(), "IconifyComponentMPS.overlay", false);
                }
            }
        });

        mp_pitch_black.setChecked(PrefConfig.loadPrefBool(getApplicationContext(), "IconifyComponentMPB.overlay"));

        mp_pitch_black.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mp_accent.setChecked(false);
                    mp_system.setChecked(false);
                    OverlayUtils.disableOverlay("IconifyComponentMPA.overlay");
                    PrefConfig.savePrefBool(getApplicationContext(), "IconifyComponentMPA.overlay", false);
                    OverlayUtils.disableOverlay("IconifyComponentMPS.overlay");
                    PrefConfig.savePrefBool(getApplicationContext(), "IconifyComponentMPS.overlay", false);
                    OverlayUtils.enableOverlay("IconifyComponentMPB.overlay");
                    PrefConfig.savePrefBool(getApplicationContext(), "IconifyComponentMPB.overlay", true);
                } else {
                    OverlayUtils.disableOverlay("IconifyComponentMPB.overlay");
                    PrefConfig.savePrefBool(getApplicationContext(), "IconifyComponentMPB.overlay", false);
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