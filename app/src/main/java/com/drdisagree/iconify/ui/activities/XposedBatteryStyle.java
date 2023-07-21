package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Const.SWITCH_ANIMATION_DELAY;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_HEIGHT;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_MARGIN;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_STYLE;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_WIDTH;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.SeekBar;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.RPrefs;
import com.drdisagree.iconify.databinding.ActivityXposedBatteryStyleBinding;
import com.drdisagree.iconify.ui.utils.ViewBindingHelpers;
import com.drdisagree.iconify.ui.views.RadioDialog;
import com.drdisagree.iconify.utils.SystemUtil;

import java.util.Arrays;

public class XposedBatteryStyle extends BaseActivity implements RadioDialog.RadioDialogListener {

    private ActivityXposedBatteryStyleBinding binding;
    private static int selectedBatteryStyle = 0;
    private RadioDialog rd_battery_style;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityXposedBatteryStyleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Header
        ViewBindingHelpers.setHeader(this, binding.header.collapsingToolbar, binding.header.toolbar, R.string.activity_title_battery_style);

        // Custom battery style
        rd_battery_style = new RadioDialog(this, 0, RPrefs.getInt(CUSTOM_BATTERY_STYLE, 0));
        rd_battery_style.setRadioDialogListener(this);
        binding.customBatteryStyle.setOnClickListener(v -> rd_battery_style.show(R.string.battery_style_title, R.array.custom_battery_style, binding.selectedCustomBatteryStyle));
        selectedBatteryStyle = rd_battery_style.getSelectedIndex();
        binding.selectedCustomBatteryStyle.setText(Arrays.asList(getResources().getStringArray(R.array.custom_battery_style)).get(selectedBatteryStyle));
        binding.selectedCustomBatteryStyle.setText(getResources().getString(R.string.opt_selected) + ' ' + binding.selectedCustomBatteryStyle.getText().toString().replaceAll(getResources().getString(R.string.opt_selected) + ' ', ""));

        // Apply battery style
        binding.applyBatteryStyle.setOnClickListener(v -> {
            RPrefs.putInt(CUSTOM_BATTERY_STYLE, selectedBatteryStyle);
            new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::restartSystemUI, SWITCH_ANIMATION_DELAY);
        });

        // Battery width
        final int[] batteryWidth = {RPrefs.getInt(CUSTOM_BATTERY_WIDTH, 20)};
        binding.batteryWidthOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + batteryWidth[0] + "dp");
        binding.batteryWidthSeekbar.setProgress(RPrefs.getInt(CUSTOM_BATTERY_WIDTH, 20));
        binding.batteryWidthSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                batteryWidth[0] = progress;
                binding.batteryWidthOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "dp");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                RPrefs.putInt(CUSTOM_BATTERY_WIDTH, batteryWidth[0]);
                new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::doubleToggleDarkMode, SWITCH_ANIMATION_DELAY);
            }
        });

        // Battery height
        final int[] batteryHeight = {RPrefs.getInt(CUSTOM_BATTERY_HEIGHT, 20)};
        binding.batteryHeightOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + batteryHeight[0] + "dp");
        binding.batteryHeightSeekbar.setProgress(RPrefs.getInt(CUSTOM_BATTERY_HEIGHT, 20));
        binding.batteryHeightSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                batteryHeight[0] = progress;
                binding.batteryHeightOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "dp");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                RPrefs.putInt(CUSTOM_BATTERY_HEIGHT, batteryHeight[0]);
                new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::doubleToggleDarkMode, SWITCH_ANIMATION_DELAY);
            }
        });

        // Battery margin
        final int[] batteryMargin = {RPrefs.getInt(CUSTOM_BATTERY_MARGIN, 6)};
        binding.batteryMarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + batteryMargin[0] + "dp");
        binding.batteryMarginSeekbar.setProgress(RPrefs.getInt(CUSTOM_BATTERY_MARGIN, 6));
        binding.batteryMarginSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                batteryMargin[0] = progress;
                binding.batteryMarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "dp");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                RPrefs.putInt(CUSTOM_BATTERY_MARGIN, batteryMargin[0]);
                new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::restartSystemUI, SWITCH_ANIMATION_DELAY);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onItemSelected(int dialogId, int selectedIndex) {
        if (dialogId == 0) {
            selectedBatteryStyle = selectedIndex;
            binding.selectedCustomBatteryStyle.setText(getResources().getString(R.string.opt_selected) + ' ' + binding.selectedCustomBatteryStyle.getText().toString().replaceAll(getResources().getString(R.string.opt_selected) + ' ', ""));
        }
    }

    @Override
    public void onDestroy() {
        rd_battery_style.dismiss();
        super.onDestroy();
    }
}