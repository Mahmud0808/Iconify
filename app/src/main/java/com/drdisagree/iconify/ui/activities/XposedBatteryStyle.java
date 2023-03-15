package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Preferences.LANDSCAPE_BATTERY_HEIGHT;
import static com.drdisagree.iconify.common.Preferences.LANDSCAPE_BATTERY_ROTATION;
import static com.drdisagree.iconify.common.Preferences.LANDSCAPE_BATTERY_SWITCH;
import static com.drdisagree.iconify.common.Preferences.LANDSCAPE_BATTERY_WIDTH;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.RPrefs;
import com.drdisagree.iconify.utils.HelperUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class XposedBatteryStyle extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xposed_battery_style);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_battery_style));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Landscape battery
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enable_landscape_battery = findViewById(R.id.enable_landscape_battery);
        enable_landscape_battery.setChecked(RPrefs.getBoolean(LANDSCAPE_BATTERY_SWITCH, false));
        enable_landscape_battery.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(LANDSCAPE_BATTERY_SWITCH, isChecked);
            new Handler().postDelayed(isChecked ? HelperUtil::forceApply : SystemUtil::restartSystemUI, 200);
        });

        // Landscape battery style
        final Spinner landscape_battery_style = findViewById(R.id.landscape_battery_style);
        List<String> lsbattery_styles = new ArrayList<>();
        lsbattery_styles.add("Landscape R");
        lsbattery_styles.add("Landscape L");

        ArrayAdapter<String> lsbattery_styles_adapter = new ArrayAdapter<>(this, R.layout.simple_spinner_item, lsbattery_styles);
        lsbattery_styles_adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        landscape_battery_style.setAdapter(lsbattery_styles_adapter);

        final int[] selectedLandscapeBattery = {RPrefs.getInt(LANDSCAPE_BATTERY_ROTATION, 90) == 90 ? 0 : 1};
        landscape_battery_style.setSelection(selectedLandscapeBattery[0]);
        landscape_battery_style.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedLandscapeBattery[0] = position == 0 ? 90 : 270;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Apply battery style
        Button apply_battery_style = findViewById(R.id.apply_battery_style);
        apply_battery_style.setOnClickListener(v -> {
            RPrefs.putInt(LANDSCAPE_BATTERY_ROTATION, selectedLandscapeBattery[0]);
            if (RPrefs.getBoolean(LANDSCAPE_BATTERY_SWITCH, false)) {
                new Handler().postDelayed(HelperUtil::forceApply, 200);
            }
        });

        // Battery width
        SeekBar battery_width_seekbar = findViewById(R.id.battery_width_seekbar);
        TextView battery_width_output = findViewById(R.id.battery_width_output);
        final int[] batteryWidth = {RPrefs.getInt(LANDSCAPE_BATTERY_WIDTH, 20)};
        battery_width_output.setText(getResources().getString(R.string.opt_selected) + ' ' + batteryWidth[0] + "dp");
        battery_width_seekbar.setProgress(RPrefs.getInt(LANDSCAPE_BATTERY_WIDTH, 20));
        battery_width_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                batteryWidth[0] = progress;
                battery_width_output.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "dp");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                RPrefs.putInt(LANDSCAPE_BATTERY_WIDTH, batteryWidth[0]);
                if (RPrefs.getBoolean(LANDSCAPE_BATTERY_SWITCH, false)) {
                    new Handler().postDelayed(HelperUtil::forceApply, 200);
                }
            }
        });

        // Battery height
        SeekBar battery_height_seekbar = findViewById(R.id.battery_height_seekbar);
        TextView battery_height_output = findViewById(R.id.battery_height_output);
        final int[] batteryHeight = {RPrefs.getInt(LANDSCAPE_BATTERY_HEIGHT, 20)};
        battery_height_output.setText(getResources().getString(R.string.opt_selected) + ' ' + batteryHeight[0] + "dp");
        battery_height_seekbar.setProgress(RPrefs.getInt(LANDSCAPE_BATTERY_HEIGHT, 20));
        battery_height_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                batteryHeight[0] = progress;
                battery_height_output.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "dp");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                RPrefs.putInt(LANDSCAPE_BATTERY_HEIGHT, batteryHeight[0]);
                if (RPrefs.getBoolean(LANDSCAPE_BATTERY_SWITCH, false)) {
                    new Handler().postDelayed(HelperUtil::forceApply, 200);
                }
            }
        });
    }
}