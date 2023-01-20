package com.drdisagree.iconify.ui.activity;

import static com.drdisagree.iconify.common.References.HIDE_QSLABEL_SWITCH;
import static com.drdisagree.iconify.common.References.STATUSBAR_CLOCKBG;
import static com.drdisagree.iconify.common.References.VERTICAL_QSTILE_SWITCH;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Switch;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.RemotePrefs;
import com.drdisagree.iconify.utils.SystemUtil;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.Objects;

public class XPosedMenu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xposed_menu);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_xposed_menu));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Clock Background Chip
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enable_clock_bg_chip = findViewById(R.id.enable_clock_bg_chip);
        enable_clock_bg_chip.setChecked(RemotePrefs.getBoolean(STATUSBAR_CLOCKBG, false));
        enable_clock_bg_chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RemotePrefs.putBoolean(STATUSBAR_CLOCKBG, isChecked);
            new Handler().postDelayed(SystemUtil::restartSystemUI, 200);
        });

        // Vertical QS Tile
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enable_vertical_tile = findViewById(R.id.enable_vertical_tile);
        enable_vertical_tile.setChecked(RemotePrefs.getBoolean(VERTICAL_QSTILE_SWITCH, false));
        enable_vertical_tile.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RemotePrefs.putBoolean(VERTICAL_QSTILE_SWITCH, isChecked);
        });

        // Hide label for vertical tiles
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch hide_tile_label = findViewById(R.id.hide_tile_label);
        hide_tile_label.setChecked(RemotePrefs.getBoolean(HIDE_QSLABEL_SWITCH, false));
        hide_tile_label.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RemotePrefs.putBoolean(HIDE_QSLABEL_SWITCH, isChecked);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}