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

public class VolumePanel extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.volume_panel);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle("Volume Panel");
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch thin_bg = findViewById(R.id.thin_bg);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch thick_bg = findViewById(R.id.thick_bg);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch no_bg = findViewById(R.id.no_bg);

        thin_bg.setChecked(PrefConfig.loadPrefBool(Iconify.getAppContext(), "IconifyComponentVPBG1.overlay"));

        thin_bg.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    thick_bg.setChecked(false);
                    no_bg.setChecked(false);
                    OverlayUtils.disableOverlay("IconifyComponentVPBG2.overlay");
                    OverlayUtils.disableOverlay("IconifyComponentVPBG3.overlay");
                    OverlayUtils.enableOverlay("IconifyComponentVPBG1.overlay");
                } else {
                    OverlayUtils.disableOverlay("IconifyComponentVPBG1.overlay");
                }
            }
        });

        thick_bg.setChecked(PrefConfig.loadPrefBool(Iconify.getAppContext(), "IconifyComponentVPBG2.overlay"));

        thick_bg.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    thin_bg.setChecked(false);
                    no_bg.setChecked(false);
                    OverlayUtils.disableOverlay("IconifyComponentVPBG1.overlay");
                    OverlayUtils.disableOverlay("IconifyComponentVPBG3.overlay");
                    OverlayUtils.enableOverlay("IconifyComponentVPBG2.overlay");
                } else {
                    OverlayUtils.disableOverlay("IconifyComponentVPBG2.overlay");
                }
            }
        });

        no_bg.setChecked(PrefConfig.loadPrefBool(Iconify.getAppContext(), "IconifyComponentVPBG3.overlay"));

        no_bg.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    thin_bg.setChecked(false);
                    thick_bg.setChecked(false);
                    OverlayUtils.disableOverlay("IconifyComponentVPBG1.overlay");
                    OverlayUtils.disableOverlay("IconifyComponentVPBG2.overlay");
                    OverlayUtils.enableOverlay("IconifyComponentVPBG3.overlay");
                } else {
                    OverlayUtils.disableOverlay("IconifyComponentVPBG3.overlay");
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