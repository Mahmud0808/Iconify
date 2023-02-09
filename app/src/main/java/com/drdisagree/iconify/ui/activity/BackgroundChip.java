package com.drdisagree.iconify.ui.activity;

import static com.drdisagree.iconify.common.References.CHIP_QSSTATUSICONS_STYLE;
import static com.drdisagree.iconify.common.References.QSPANEL_STATUSICONSBG_SWITCH;
import static com.drdisagree.iconify.common.References.STATUSBAR_CLOCKBG_SWITCH;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.RemotePrefs;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BackgroundChip extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_background_chip);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_background_chip));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Clock Background Chip
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enable_clock_bg_chip = findViewById(R.id.enable_clock_bg_chip);
        enable_clock_bg_chip.setChecked(RemotePrefs.getBoolean(STATUSBAR_CLOCKBG_SWITCH, false));
        enable_clock_bg_chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RemotePrefs.putBoolean(STATUSBAR_CLOCKBG_SWITCH, isChecked);
            new Handler().postDelayed(SystemUtil::restartSystemUI, 200);
        });

        // Status icons chip
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enable_status_icons_chip = findViewById(R.id.enable_status_icons_chip);
        enable_status_icons_chip.setChecked(RemotePrefs.getBoolean(QSPANEL_STATUSICONSBG_SWITCH, false));
        enable_status_icons_chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RemotePrefs.putBoolean(QSPANEL_STATUSICONSBG_SWITCH, isChecked);
            new Handler().postDelayed(() -> {
                OverlayUtil.enableOverlay("IconifyComponentIXCC.overlay");
                SystemUtil.restartSystemUI();
            }, 200);
        });

        // Status icons chip style
        final Spinner status_icons_chip_style = findViewById(R.id.status_icons_chip_style);
        List<String> status_icons_chip_styles = new ArrayList<>();
        status_icons_chip_styles.add(getResources().getString(R.string.style_1));
        status_icons_chip_styles.add(getResources().getString(R.string.style_2));
        status_icons_chip_styles.add(getResources().getString(R.string.style_3));
        status_icons_chip_styles.add(getResources().getString(R.string.style_4));
        status_icons_chip_styles.add(getResources().getString(R.string.style_5));

        ArrayAdapter<String> status_icons_chip_styles_adapter = new ArrayAdapter<>(this, R.layout.simple_spinner_item, status_icons_chip_styles);
        status_icons_chip_styles_adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        status_icons_chip_style.setAdapter(status_icons_chip_styles_adapter);

        status_icons_chip_style.setSelection(RemotePrefs.getInt(CHIP_QSSTATUSICONS_STYLE, 0));
        status_icons_chip_style.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                RemotePrefs.putInt(CHIP_QSSTATUSICONS_STYLE, position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
}