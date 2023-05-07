package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Const.SWITCH_ANIMATION_DELAY;
import static com.drdisagree.iconify.common.Preferences.BLACK_QSPANEL;
import static com.drdisagree.iconify.common.Preferences.DUALTONE_QSPANEL;
import static com.drdisagree.iconify.common.Preferences.FLUID_QSPANEL;
import static com.drdisagree.iconify.common.Preferences.HIDE_QSLABEL_SWITCH;
import static com.drdisagree.iconify.common.Preferences.LIGHT_QSPANEL;
import static com.drdisagree.iconify.common.Preferences.PANEL_TOPMARGIN_SWITCH;
import static com.drdisagree.iconify.common.Preferences.QS_TOPMARGIN;
import static com.drdisagree.iconify.common.Preferences.VERTICAL_QSTILE_SWITCH;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.RPrefs;
import com.drdisagree.iconify.ui.utils.ViewBindingHelpers;
import com.drdisagree.iconify.utils.HelperUtil;
import com.drdisagree.iconify.utils.SystemUtil;

public class XposedQuickSettings extends BaseActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xposed_quick_settings);

        // Header
        ViewBindingHelpers.setHeader(this, findViewById(R.id.collapsing_toolbar), findViewById(R.id.toolbar), R.string.activity_title_quick_settings);

        // Vertical QS Tile
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enable_vertical_tile = findViewById(R.id.enable_vertical_tile);
        enable_vertical_tile.setChecked(RPrefs.getBoolean(VERTICAL_QSTILE_SWITCH, false));
        enable_vertical_tile.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(VERTICAL_QSTILE_SWITCH, isChecked);
            new Handler().postDelayed(HelperUtil::forceApply, SWITCH_ANIMATION_DELAY);
        });

        // Hide label for vertical tiles
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch hide_tile_label = findViewById(R.id.hide_tile_label);
        hide_tile_label.setChecked(RPrefs.getBoolean(HIDE_QSLABEL_SWITCH, false));
        hide_tile_label.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(HIDE_QSLABEL_SWITCH, isChecked);
            new Handler().postDelayed(HelperUtil::forceApply, SWITCH_ANIMATION_DELAY);
        });

        // Light Theme
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enable_light_theme = findViewById(R.id.enable_light_theme);
        enable_light_theme.setChecked(RPrefs.getBoolean(LIGHT_QSPANEL, false));
        enable_light_theme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(LIGHT_QSPANEL, isChecked);
            new Handler().postDelayed(SystemUtil::restartSystemUI, SWITCH_ANIMATION_DELAY);
        });

        // Dual Tone
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enable_dual_tone = findViewById(R.id.enable_dual_tone);
        enable_dual_tone.setChecked(RPrefs.getBoolean(DUALTONE_QSPANEL, false));
        enable_dual_tone.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(DUALTONE_QSPANEL, isChecked);
            new Handler().postDelayed(HelperUtil::forceApply, SWITCH_ANIMATION_DELAY);
        });

        // Pixel Black Theme
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enable_black_theme = findViewById(R.id.enable_black_theme);
        enable_black_theme.setChecked(RPrefs.getBoolean(BLACK_QSPANEL, false));
        enable_black_theme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(BLACK_QSPANEL, isChecked);
            new Handler().postDelayed(SystemUtil::restartSystemUI, SWITCH_ANIMATION_DELAY);
        });

        // Fluid QS Theme
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enable_fluid_theme = findViewById(R.id.enable_fluid_theme);
        enable_fluid_theme.setChecked(RPrefs.getBoolean(FLUID_QSPANEL, false));
        enable_fluid_theme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(FLUID_QSPANEL, isChecked);
            new Handler().postDelayed(SystemUtil::restartSystemUI, SWITCH_ANIMATION_DELAY);
        });

        // QS panel top margin switch
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enable_panel_top_margin = findViewById(R.id.enable_panel_top_margin);
        enable_panel_top_margin.setChecked(RPrefs.getBoolean(PANEL_TOPMARGIN_SWITCH, false));
        enable_panel_top_margin.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(PANEL_TOPMARGIN_SWITCH, isChecked);
            if (isChecked)
                new Handler().postDelayed(HelperUtil::forceApply, SWITCH_ANIMATION_DELAY);
            else new Handler().postDelayed(SystemUtil::restartSystemUI, SWITCH_ANIMATION_DELAY);
        });

        // QS panel top margin slider
        SeekBar qs_top_margin_seekbar = findViewById(R.id.qs_top_margin_seekbar);
        TextView qs_top_margin_output = findViewById(R.id.qs_top_margin_output);
        qs_top_margin_output.setText(getResources().getString(R.string.opt_selected) + ' ' + RPrefs.getInt(QS_TOPMARGIN, 0) + "dp");
        qs_top_margin_seekbar.setProgress(RPrefs.getInt(QS_TOPMARGIN, 0));
        final int[] qsTopMargin = {RPrefs.getInt(QS_TOPMARGIN, 0)};
        qs_top_margin_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                qsTopMargin[0] = progress;
                qs_top_margin_output.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "dp");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                RPrefs.putInt(QS_TOPMARGIN, qsTopMargin[0]);
                if (RPrefs.getBoolean(PANEL_TOPMARGIN_SWITCH, false)) {
                    new Handler().postDelayed(HelperUtil::forceApply, SWITCH_ANIMATION_DELAY);
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