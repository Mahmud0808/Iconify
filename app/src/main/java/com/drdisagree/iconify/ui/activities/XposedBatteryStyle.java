package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Const.SWITCH_ANIMATION_DELAY;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_HEIGHT;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_MARGIN;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_STYLE;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_WIDTH;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.RPrefs;
import com.drdisagree.iconify.ui.utils.ViewBindingHelpers;
import com.drdisagree.iconify.ui.views.RadioDialog;
import com.drdisagree.iconify.utils.HelperUtil;
import com.drdisagree.iconify.utils.SystemUtil;

import java.util.Arrays;

public class XposedBatteryStyle extends BaseActivity implements RadioDialog.RadioDialogListener {

    private static int selectedBatteryStyle = 0;
    RadioDialog rd_battery_style;
    TextView selected_custom_battery_style;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xposed_battery_style);

        // Header
        ViewBindingHelpers.setHeader(this, findViewById(R.id.collapsing_toolbar), findViewById(R.id.toolbar), R.string.activity_title_battery_style);

        // Custom battery style
        LinearLayout custom_battery_style = findViewById(R.id.custom_battery_style);
        selected_custom_battery_style = findViewById(R.id.selected_custom_battery_style);
        rd_battery_style = new RadioDialog(this, 0, RPrefs.getInt(CUSTOM_BATTERY_STYLE, 0));
        rd_battery_style.setRadioDialogListener(this);
        custom_battery_style.setOnClickListener(v -> rd_battery_style.show(R.string.battery_style_title, R.array.custom_battery_style, selected_custom_battery_style));
        selectedBatteryStyle = rd_battery_style.getSelectedIndex();
        selected_custom_battery_style.setText(Arrays.asList(getResources().getStringArray(R.array.custom_battery_style)).get(selectedBatteryStyle));
        selected_custom_battery_style.setText(getResources().getString(R.string.opt_selected) + ' ' + selected_custom_battery_style.getText().toString().replaceAll(getResources().getString(R.string.opt_selected) + ' ', ""));

        // Apply battery style
        Button apply_battery_style = findViewById(R.id.apply_battery_style);
        apply_battery_style.setOnClickListener(v -> {
            RPrefs.putInt(CUSTOM_BATTERY_STYLE, selectedBatteryStyle);
            new Handler().postDelayed(SystemUtil::restartSystemUI, SWITCH_ANIMATION_DELAY);
        });

        // Battery width
        SeekBar battery_width_seekbar = findViewById(R.id.battery_width_seekbar);
        TextView battery_width_output = findViewById(R.id.battery_width_output);
        final int[] batteryWidth = {RPrefs.getInt(CUSTOM_BATTERY_WIDTH, 20)};
        battery_width_output.setText(getResources().getString(R.string.opt_selected) + ' ' + batteryWidth[0] + "dp");
        battery_width_seekbar.setProgress(RPrefs.getInt(CUSTOM_BATTERY_WIDTH, 20));
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
                RPrefs.putInt(CUSTOM_BATTERY_WIDTH, batteryWidth[0]);
                new Handler().postDelayed(HelperUtil::forceApply, SWITCH_ANIMATION_DELAY);
            }
        });

        // Battery height
        SeekBar battery_height_seekbar = findViewById(R.id.battery_height_seekbar);
        TextView battery_height_output = findViewById(R.id.battery_height_output);
        final int[] batteryHeight = {RPrefs.getInt(CUSTOM_BATTERY_HEIGHT, 20)};
        battery_height_output.setText(getResources().getString(R.string.opt_selected) + ' ' + batteryHeight[0] + "dp");
        battery_height_seekbar.setProgress(RPrefs.getInt(CUSTOM_BATTERY_HEIGHT, 20));
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
                RPrefs.putInt(CUSTOM_BATTERY_HEIGHT, batteryHeight[0]);
                new Handler().postDelayed(HelperUtil::forceApply, SWITCH_ANIMATION_DELAY);
            }
        });

        // Battery margin
        SeekBar battery_margin_seekbar = findViewById(R.id.battery_margin_seekbar);
        TextView battery_margin_output = findViewById(R.id.battery_margin_output);
        final int[] batteryMargin = {RPrefs.getInt(CUSTOM_BATTERY_MARGIN, 6)};
        battery_margin_output.setText(getResources().getString(R.string.opt_selected) + ' ' + batteryMargin[0] + "dp");
        battery_margin_seekbar.setProgress(RPrefs.getInt(CUSTOM_BATTERY_MARGIN, 6));
        battery_margin_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                batteryMargin[0] = progress;
                battery_margin_output.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "dp");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                RPrefs.putInt(CUSTOM_BATTERY_MARGIN, batteryMargin[0]);
                new Handler().postDelayed(SystemUtil::restartSystemUI, SWITCH_ANIMATION_DELAY);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onItemSelected(int dialogId, int selectedIndex) {
        if (dialogId == 0) {
            selectedBatteryStyle = selectedIndex;
            selected_custom_battery_style.setText(getResources().getString(R.string.opt_selected) + ' ' + selected_custom_battery_style.getText().toString().replaceAll(getResources().getString(R.string.opt_selected) + ' ', ""));
        }
    }

    @Override
    public void onDestroy() {
        rd_battery_style.dismiss();
        super.onDestroy();
    }
}