package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Const.FRAMEWORK_PACKAGE;
import static com.drdisagree.iconify.common.Const.SWITCH_ANIMATION_DELAY;
import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.BLACK_QSPANEL;
import static com.drdisagree.iconify.common.Preferences.DUALTONE_QSPANEL;
import static com.drdisagree.iconify.common.Preferences.FLUID_NOTIF_TRANSPARENCY;
import static com.drdisagree.iconify.common.Preferences.FLUID_POWERMENU_TRANSPARENCY;
import static com.drdisagree.iconify.common.Preferences.FLUID_QSPANEL;
import static com.drdisagree.iconify.common.Preferences.HEADER_QQS_TOPMARGIN;
import static com.drdisagree.iconify.common.Preferences.HIDE_QSLABEL_SWITCH;
import static com.drdisagree.iconify.common.Preferences.LIGHT_QSPANEL;
import static com.drdisagree.iconify.common.Preferences.VERTICAL_QSTILE_SWITCH;
import static com.drdisagree.iconify.common.References.FABRICATED_QQS_TOPMARGIN;
import static com.drdisagree.iconify.common.References.FABRICATED_QS_TOPMARGIN;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.config.RPrefs;
import com.drdisagree.iconify.ui.utils.ViewBindingHelpers;
import com.drdisagree.iconify.utils.FabricatedUtil;
import com.drdisagree.iconify.utils.SystemUtil;

public class XposedQuickSettings extends BaseActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xposed_quick_settings);

        // Header
        ViewBindingHelpers.setHeader(this, findViewById(R.id.collapsing_toolbar), findViewById(R.id.toolbar), R.string.activity_title_quick_settings);

        // Declarations
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enable_vertical_tile = findViewById(R.id.enable_vertical_tile);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch hide_tile_label = findViewById(R.id.hide_tile_label);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enable_light_theme = findViewById(R.id.enable_light_theme);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enable_dual_tone = findViewById(R.id.enable_dual_tone);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enable_black_theme = findViewById(R.id.enable_black_theme);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enable_fluid_theme = findViewById(R.id.enable_fluid_theme);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enable_notification_transparency = findViewById(R.id.enable_notification_transparency);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enable_powermenu_transparency = findViewById(R.id.enable_powermenu_transparency);

        // Vertical QS Tile
        enable_vertical_tile.setChecked(RPrefs.getBoolean(VERTICAL_QSTILE_SWITCH, false));
        enable_vertical_tile.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(VERTICAL_QSTILE_SWITCH, isChecked);
            hide_tile_label.setEnabled(isChecked);
            new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::doubleToggleDarkMode, SWITCH_ANIMATION_DELAY);
        });
        hide_tile_label.setEnabled(enable_vertical_tile.isChecked());

        // Hide label for vertical tiles
        hide_tile_label.setChecked(RPrefs.getBoolean(HIDE_QSLABEL_SWITCH, false));
        hide_tile_label.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(HIDE_QSLABEL_SWITCH, isChecked);
            new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::doubleToggleDarkMode, SWITCH_ANIMATION_DELAY);
        });

        // Light Theme
        enable_light_theme.setChecked(RPrefs.getBoolean(LIGHT_QSPANEL, false));
        enable_light_theme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(LIGHT_QSPANEL, isChecked);
            enable_dual_tone.setEnabled(isChecked);
            new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::restartSystemUI, SWITCH_ANIMATION_DELAY);
        });
        enable_dual_tone.setEnabled(enable_light_theme.isChecked());

        // Dual Tone
        enable_dual_tone.setChecked(RPrefs.getBoolean(DUALTONE_QSPANEL, false));
        enable_dual_tone.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(DUALTONE_QSPANEL, isChecked);
            new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::doubleToggleDarkMode, SWITCH_ANIMATION_DELAY);
        });

        // Pixel Black Theme
        enable_black_theme.setChecked(RPrefs.getBoolean(BLACK_QSPANEL, false));
        enable_black_theme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(BLACK_QSPANEL, isChecked);
            new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::restartSystemUI, SWITCH_ANIMATION_DELAY);
        });

        // Fluid QS Theme
        enable_fluid_theme.setChecked(RPrefs.getBoolean(FLUID_QSPANEL, false));
        enable_fluid_theme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(FLUID_QSPANEL, isChecked);
            enable_notification_transparency.setEnabled(isChecked);
            enable_powermenu_transparency.setEnabled(isChecked);
            new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::restartSystemUI, SWITCH_ANIMATION_DELAY);
        });
        enable_notification_transparency.setEnabled(enable_fluid_theme.isChecked());
        enable_powermenu_transparency.setEnabled(enable_fluid_theme.isChecked());

        // Fluid QS Notification Transparency
        enable_notification_transparency.setChecked(RPrefs.getBoolean(FLUID_NOTIF_TRANSPARENCY, false));
        enable_notification_transparency.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(FLUID_NOTIF_TRANSPARENCY, isChecked);
            new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::restartSystemUI, SWITCH_ANIMATION_DELAY);
        });

        // Fluid QS Power Menu Transparency
        enable_powermenu_transparency.setChecked(RPrefs.getBoolean(FLUID_POWERMENU_TRANSPARENCY, false));
        enable_powermenu_transparency.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(FLUID_POWERMENU_TRANSPARENCY, isChecked);
            new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::restartSystemUI, SWITCH_ANIMATION_DELAY);
        });

        // QQS panel top margin slider
        SeekBar qqs_top_margin_seekbar = findViewById(R.id.qqs_top_margin_seekbar);
        TextView qqs_top_margin_output = findViewById(R.id.qqs_top_margin_output);
        qqs_top_margin_output.setText(getResources().getString(R.string.opt_selected) + ' ' + Prefs.getInt(FABRICATED_QQS_TOPMARGIN, 100) + "dp");
        qqs_top_margin_seekbar.setProgress(Prefs.getInt(FABRICATED_QQS_TOPMARGIN, 100));
        final int[] qqsTopMargin = {Prefs.getInt(FABRICATED_QQS_TOPMARGIN, 100)};
        qqs_top_margin_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                qqsTopMargin[0] = progress;
                qqs_top_margin_output.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "dp");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Prefs.putInt(FABRICATED_QQS_TOPMARGIN, qqsTopMargin[0]);
                FabricatedUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, "quick_qs_offset_height", "dimen", "quick_qs_offset_height", qqsTopMargin[0] + "dp");
                FabricatedUtil.buildAndEnableOverlay(SYSTEMUI_PACKAGE, "qqs_layout_margin_top", "dimen", "qqs_layout_margin_top", qqsTopMargin[0] + "dp");
                FabricatedUtil.buildAndEnableOverlay(SYSTEMUI_PACKAGE, "qs_header_row_min_height", "dimen", "qs_header_row_min_height", qqsTopMargin[0] + "dp");
                RPrefs.putInt(HEADER_QQS_TOPMARGIN, qqsTopMargin[0]);
            }
        });

        // QQS Reset button
        ImageView reset_qqs_top_margin = findViewById(R.id.reset_qqs_top_margin);
        boolean reset_qqs_top_margin_visible = Prefs.getBoolean("fabricatedquick_qs_offset_height", false) || Prefs.getBoolean("fabricatedqqs_layout_margin_top", false) || Prefs.getBoolean("fabricatedqs_header_row_min_height", false);
        reset_qqs_top_margin.setVisibility(reset_qqs_top_margin_visible ? View.VISIBLE : View.INVISIBLE);

        reset_qqs_top_margin.setOnLongClickListener(v -> {
            FabricatedUtil.disableOverlay("quick_qs_offset_height");
            FabricatedUtil.disableOverlay("qqs_layout_margin_top");
            FabricatedUtil.disableOverlay("qs_header_row_min_height");
            RPrefs.putInt(HEADER_QQS_TOPMARGIN, -1);
            reset_qqs_top_margin.setVisibility(View.INVISIBLE);
            return true;
        });

        // QS panel top margin slider
        SeekBar qs_top_margin_seekbar = findViewById(R.id.qs_top_margin_seekbar);
        TextView qs_top_margin_output = findViewById(R.id.qs_top_margin_output);
        qs_top_margin_output.setText(getResources().getString(R.string.opt_selected) + ' ' + Prefs.getInt(FABRICATED_QS_TOPMARGIN, 100) + "dp");
        qs_top_margin_seekbar.setProgress(Prefs.getInt(FABRICATED_QS_TOPMARGIN, 100));
        final int[] qsTopMargin = {Prefs.getInt(FABRICATED_QS_TOPMARGIN, 100)};
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
                Prefs.putInt(FABRICATED_QS_TOPMARGIN, qsTopMargin[0]);
                FabricatedUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, "quick_qs_total_height", "dimen", "quick_qs_total_height", qsTopMargin[0] + "dp");
                FabricatedUtil.buildAndEnableOverlay(SYSTEMUI_PACKAGE, "qs_panel_padding_top", "dimen", "qs_panel_padding_top", qsTopMargin[0] + "dp");
                FabricatedUtil.buildAndEnableOverlay(SYSTEMUI_PACKAGE, "qs_panel_padding_top_combined_headers", "dimen", "qs_panel_padding_top_combined_headers", qsTopMargin[0] + "dp");
            }
        });

        // QS Reset button
        ImageView reset_qs_top_margin = findViewById(R.id.reset_qs_top_margin);
        boolean reset_qs_top_margin_visible = Prefs.getBoolean("fabricatedquick_qs_total_height", false) || Prefs.getBoolean("fabricatedqs_panel_padding_top", false) || Prefs.getBoolean("fabricatedqs_panel_padding_top_combined_headers", false);
        reset_qs_top_margin.setVisibility(reset_qs_top_margin_visible ? View.VISIBLE : View.INVISIBLE);

        reset_qs_top_margin.setOnLongClickListener(v -> {
            FabricatedUtil.disableOverlay("quick_qs_total_height");
            FabricatedUtil.disableOverlay("qs_panel_padding_top");
            FabricatedUtil.disableOverlay("qs_panel_padding_top_combined_headers");
            reset_qs_top_margin.setVisibility(View.INVISIBLE);
            return true;
        });
    }
}