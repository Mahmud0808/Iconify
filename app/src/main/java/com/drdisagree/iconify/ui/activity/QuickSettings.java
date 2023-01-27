package com.drdisagree.iconify.ui.activity;

import static com.drdisagree.iconify.common.References.CHIP_QSCLOCK_STYLE;
import static com.drdisagree.iconify.common.References.CHIP_QSDATE_STYLE;
import static com.drdisagree.iconify.common.References.CHIP_QSSTATUSICONS_STYLE;
import static com.drdisagree.iconify.common.References.HIDE_QSLABEL_SWITCH;
import static com.drdisagree.iconify.common.References.QSALPHA_LEVEL;
import static com.drdisagree.iconify.common.References.QSPANEL_CLOCKBG_SWITCH;
import static com.drdisagree.iconify.common.References.QSPANEL_DATEBG_SWITCH;
import static com.drdisagree.iconify.common.References.QSPANEL_HIDE_CARRIER;
import static com.drdisagree.iconify.common.References.QSPANEL_STATUSICONSBG_SWITCH;
import static com.drdisagree.iconify.common.References.QSTRANSPARENCY_SWITCH;
import static com.drdisagree.iconify.common.References.STATUSBAR_CLOCKBG_SWITCH;
import static com.drdisagree.iconify.common.References.VERTICAL_QSTILE_SWITCH;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.RemotePrefs;
import com.drdisagree.iconify.utils.SystemUtil;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class QuickSettings extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_settings);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_quicksettings));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Qs Panel Transparency
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enable_qs_transparency = findViewById(R.id.enable_qs_transparency);
        enable_qs_transparency.setChecked(RemotePrefs.getBoolean(QSTRANSPARENCY_SWITCH, false));
        enable_qs_transparency.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RemotePrefs.putBoolean(QSTRANSPARENCY_SWITCH, isChecked);
            // Restart SystemUI
            new Handler().postDelayed(SystemUtil::restartSystemUI, 200);
        });

        SeekBar transparency_seekbar = findViewById(R.id.transparency_seekbar);
        transparency_seekbar.setPadding(0, 0, 0, 0);
        TextView transparency_output = findViewById(R.id.transparency_output);
        final int[] transparency = {RemotePrefs.getInt(QSALPHA_LEVEL, 60)};
        transparency_output.setText(getResources().getString(R.string.opt_selected) + ' ' + transparency[0] + "%");
        transparency_seekbar.setProgress(transparency[0]);
        transparency_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                transparency[0] = progress;
                transparency_output.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "%");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                RemotePrefs.putInt(QSALPHA_LEVEL, transparency[0]);
            }
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

        // Clock Background Chip
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enable_clock_bg_chip = findViewById(R.id.enable_clock_bg_chip);
        enable_clock_bg_chip.setChecked(RemotePrefs.getBoolean(STATUSBAR_CLOCKBG_SWITCH, false));
        enable_clock_bg_chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RemotePrefs.putBoolean(STATUSBAR_CLOCKBG_SWITCH, isChecked);
            new Handler().postDelayed(SystemUtil::restartSystemUI, 200);
        });

        // Hide carrier group
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch hide_qs_carrier_group = findViewById(R.id.hide_qs_carrier_group);
        hide_qs_carrier_group.setChecked(RemotePrefs.getBoolean(QSPANEL_HIDE_CARRIER, false));
        hide_qs_carrier_group.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RemotePrefs.putBoolean(QSPANEL_HIDE_CARRIER, isChecked);
            new Handler().postDelayed(SystemUtil::restartSystemUI, 200);
        });

        // QS clock chip
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enable_qs_clock_chip = findViewById(R.id.enable_qs_clock_chip);
        enable_qs_clock_chip.setChecked(RemotePrefs.getBoolean(QSPANEL_CLOCKBG_SWITCH, false));
        enable_qs_clock_chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RemotePrefs.putBoolean(QSPANEL_CLOCKBG_SWITCH, isChecked);
            new Handler().postDelayed(SystemUtil::restartSystemUI, 200);
        });

        // QS clock chip style
        final Spinner qs_clock_chip_style = findViewById(R.id.qs_clock_chip_style);
        List<String> qs_clock_chip_styles = new ArrayList<>();
        qs_clock_chip_styles.add("Style 1");
        qs_clock_chip_styles.add("Style 2");
        qs_clock_chip_styles.add("Style 3");
        qs_clock_chip_styles.add("Style 4");
        qs_clock_chip_styles.add("Style 5");

        ArrayAdapter<String> qs_clock_chip_styles_adapter = new ArrayAdapter<>(this, R.layout.simple_spinner_item, qs_clock_chip_styles);
        qs_clock_chip_styles_adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        qs_clock_chip_style.setAdapter(qs_clock_chip_styles_adapter);

        qs_clock_chip_style.setSelection(RemotePrefs.getInt(CHIP_QSCLOCK_STYLE, 0));
        qs_clock_chip_style.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                RemotePrefs.putInt(CHIP_QSCLOCK_STYLE, position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // QS date chip
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enable_qs_date_chip = findViewById(R.id.enable_qs_date_chip);
        enable_qs_date_chip.setChecked(RemotePrefs.getBoolean(QSPANEL_DATEBG_SWITCH, false));
        enable_qs_date_chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RemotePrefs.putBoolean(QSPANEL_DATEBG_SWITCH, isChecked);
            new Handler().postDelayed(SystemUtil::restartSystemUI, 200);
        });

        // QS date chip style
        final Spinner qs_date_chip_style = findViewById(R.id.qs_date_chip_style);
        List<String> qs_date_chip_styles = new ArrayList<>();
        qs_date_chip_styles.add("Style 1");
        qs_date_chip_styles.add("Style 2");
        qs_date_chip_styles.add("Style 3");
        qs_date_chip_styles.add("Style 4");
        qs_date_chip_styles.add("Style 5");

        ArrayAdapter<String> qs_date_chip_styles_adapter = new ArrayAdapter<>(this, R.layout.simple_spinner_item, qs_date_chip_styles);
        qs_date_chip_styles_adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        qs_date_chip_style.setAdapter(qs_date_chip_styles_adapter);

        qs_date_chip_style.setSelection(RemotePrefs.getInt(CHIP_QSDATE_STYLE, 0));
        qs_date_chip_style.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                RemotePrefs.putInt(CHIP_QSDATE_STYLE, position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Status icons chip
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enable_status_icons_chip = findViewById(R.id.enable_status_icons_chip);
        enable_status_icons_chip.setChecked(RemotePrefs.getBoolean(QSPANEL_STATUSICONSBG_SWITCH, false));
        enable_status_icons_chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RemotePrefs.putBoolean(QSPANEL_STATUSICONSBG_SWITCH, isChecked);
            new Handler().postDelayed(SystemUtil::restartSystemUI, 200);
        });

        // Status icons chip style
        final Spinner status_icons_chip_style = findViewById(R.id.status_icons_chip_style);
        List<String> status_icons_chip_styles = new ArrayList<>();
        status_icons_chip_styles.add("Style 1");
        status_icons_chip_styles.add("Style 2");
        status_icons_chip_styles.add("Style 3");
        status_icons_chip_styles.add("Style 4");
        status_icons_chip_styles.add("Style 5");

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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}