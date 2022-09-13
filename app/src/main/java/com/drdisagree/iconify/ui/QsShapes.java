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

import java.util.List;
import java.util.Objects;

public class QsShapes extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qs_shapes);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle("QS Panel Tiles");
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        List<String> enabledOverlays = OverlayUtils.getEnabledOverlayList();

        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch label_white = findViewById(R.id.label_white);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch label_systemInverse = findViewById(R.id.label_systemInverse);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch label_systemInverseV2 = findViewById(R.id.label_systemInverseV2);

        label_white.setChecked(PrefConfig.loadPrefBool(getApplicationContext(), "IconifyComponentQST1.overlay"));

        label_white.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    label_systemInverse.setChecked(false);
                    label_systemInverseV2.setChecked(false);
                    OverlayUtils.disableOverlay("IconifyComponentQST2.overlay");
                    PrefConfig.savePrefBool(getApplicationContext(), "IconifyComponentQST2.overlay", false);
                    OverlayUtils.disableOverlay("IconifyComponentQST3.overlay");
                    PrefConfig.savePrefBool(getApplicationContext(), "IconifyComponentQST3.overlay", false);
                    OverlayUtils.enableOverlay("IconifyComponentQST1.overlay");
                    PrefConfig.savePrefBool(getApplicationContext(), "IconifyComponentQST1.overlay", true);
                } else {
                    OverlayUtils.disableOverlay("IconifyComponentQST1.overlay");
                    PrefConfig.savePrefBool(getApplicationContext(), "IconifyComponentQST1.overlay", false);
                }
            }
        });

        label_systemInverse.setChecked(PrefConfig.loadPrefBool(getApplicationContext(), "IconifyComponentQST2.overlay"));

        label_systemInverse.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    label_white.setChecked(false);
                    label_systemInverseV2.setChecked(false);
                    OverlayUtils.disableOverlay("IconifyComponentQST1.overlay");
                    PrefConfig.savePrefBool(getApplicationContext(), "IconifyComponentQST1.overlay", false);
                    OverlayUtils.disableOverlay("IconifyComponentQST3.overlay");
                    PrefConfig.savePrefBool(getApplicationContext(), "IconifyComponentQST3.overlay", false);
                    OverlayUtils.enableOverlay("IconifyComponentQST2.overlay");
                    PrefConfig.savePrefBool(getApplicationContext(), "IconifyComponentQST2.overlay", true);
                } else {
                    OverlayUtils.disableOverlay("IconifyComponentQST2.overlay");
                    PrefConfig.savePrefBool(getApplicationContext(), "IconifyComponentQST2.overlay", false);
                }
            }
        });

        label_systemInverseV2.setChecked(PrefConfig.loadPrefBool(getApplicationContext(), "IconifyComponentQST3.overlay"));

        label_systemInverseV2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    label_white.setChecked(false);
                    label_systemInverse.setChecked(false);
                    OverlayUtils.disableOverlay("IconifyComponentQST1.overlay");
                    PrefConfig.savePrefBool(getApplicationContext(), "IconifyComponentQST1.overlay", false);
                    OverlayUtils.disableOverlay("IconifyComponentQST2.overlay");
                    PrefConfig.savePrefBool(getApplicationContext(), "IconifyComponentQST2.overlay", false);
                    OverlayUtils.enableOverlay("IconifyComponentQST3.overlay");
                    PrefConfig.savePrefBool(getApplicationContext(), "IconifyComponentQST3.overlay", true);
                } else {
                    OverlayUtils.disableOverlay("IconifyComponentQST3.overlay");
                    PrefConfig.savePrefBool(getApplicationContext(), "IconifyComponentQST3.overlay", false);
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