package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Const.FRAMEWORK_PACKAGE;
import static com.drdisagree.iconify.common.Const.SWITCH_ANIMATION_DELAY;
import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.HEADER_QQS_TOPMARGIN;
import static com.drdisagree.iconify.common.Preferences.HIDE_QSLABEL_SWITCH;
import static com.drdisagree.iconify.common.Preferences.VERTICAL_QSTILE_SWITCH;
import static com.drdisagree.iconify.common.References.FABRICATED_QQS_TOPMARGIN;
import static com.drdisagree.iconify.common.References.FABRICATED_QS_TOPMARGIN;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.config.RPrefs;
import com.drdisagree.iconify.databinding.ActivityXposedQuickSettingsBinding;
import com.drdisagree.iconify.ui.utils.ViewHelper;
import com.drdisagree.iconify.utils.overlay.FabricatedUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.google.android.material.slider.Slider;

public class XposedQuickSettings extends BaseActivity {

    private ActivityXposedQuickSettingsBinding binding;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityXposedQuickSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Header
        ViewHelper.setHeader(this, binding.header.toolbar, R.string.activity_title_quick_settings);

        // Vertical QS Tile
        binding.enableVerticalTile.setChecked(RPrefs.getBoolean(VERTICAL_QSTILE_SWITCH, false));
        binding.enableVerticalTile.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(VERTICAL_QSTILE_SWITCH, isChecked);
            binding.hideTileLabel.setEnabled(isChecked);
            new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::doubleToggleDarkMode, SWITCH_ANIMATION_DELAY);
        });
        binding.verticalTileContainer.setOnClickListener(v -> binding.enableVerticalTile.toggle());
        binding.hideTileLabel.setEnabled(binding.enableVerticalTile.isChecked());

        // Hide label for vertical tiles
        binding.hideTileLabel.setChecked(RPrefs.getBoolean(HIDE_QSLABEL_SWITCH, false));
        binding.hideTileLabel.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(HIDE_QSLABEL_SWITCH, isChecked);
            new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::doubleToggleDarkMode, SWITCH_ANIMATION_DELAY);
        });
        binding.hideTileLabelContainer.setOnClickListener(v -> {
            if (binding.hideTileLabel.isEnabled())
                binding.hideTileLabel.toggle();
        });

        // QQS panel top margin slider
        binding.qqsTopMarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + Prefs.getInt(FABRICATED_QQS_TOPMARGIN, 100) + "dp");
        binding.qqsTopMarginSeekbar.setValue(Prefs.getInt(FABRICATED_QQS_TOPMARGIN, 100));
        final int[] qqsTopMargin = {Prefs.getInt(FABRICATED_QQS_TOPMARGIN, 100)};
        binding.qqsTopMarginSeekbar.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                qqsTopMargin[0] = (int) slider.getValue();
                binding.qqsTopMarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + qqsTopMargin[0] + "dp");
                binding.resetQqsTopMargin.setVisibility(View.VISIBLE);
                Prefs.putInt(FABRICATED_QQS_TOPMARGIN, qqsTopMargin[0]);
                FabricatedUtil.buildAndEnableOverlays(
                        new Object[]{FRAMEWORK_PACKAGE, "quick_qs_offset_height", "dimen", "quick_qs_offset_height", qqsTopMargin[0] + "dp"},
                        new Object[]{SYSTEMUI_PACKAGE, "qqs_layout_margin_top", "dimen", "qqs_layout_margin_top", qqsTopMargin[0] + "dp"},
                        new Object[]{SYSTEMUI_PACKAGE, "qs_header_row_min_height", "dimen", "qs_header_row_min_height", qqsTopMargin[0] + "dp"}
                );
                RPrefs.putInt(HEADER_QQS_TOPMARGIN, qqsTopMargin[0]);
            }
        });

        // QQS Reset button
        boolean reset_qqs_top_margin_visible = Prefs.getBoolean("fabricatedquick_qs_offset_height", false) || Prefs.getBoolean("fabricatedqqs_layout_margin_top", false) || Prefs.getBoolean("fabricatedqs_header_row_min_height", false);
        binding.resetQqsTopMargin.setVisibility(reset_qqs_top_margin_visible ? View.VISIBLE : View.INVISIBLE);

        binding.resetQqsTopMargin.setOnLongClickListener(v -> {
            FabricatedUtil.disableOverlays("quick_qs_offset_height", "qqs_layout_margin_top", "qs_header_row_min_height");
            RPrefs.putInt(HEADER_QQS_TOPMARGIN, -1);
            binding.resetQqsTopMargin.setVisibility(View.INVISIBLE);
            return true;
        });

        // QS panel top margin slider
        binding.qsTopMarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + Prefs.getInt(FABRICATED_QS_TOPMARGIN, 100) + "dp");
        binding.qsTopMarginSeekbar.setValue(Prefs.getInt(FABRICATED_QS_TOPMARGIN, 100));
        final int[] qsTopMargin = {Prefs.getInt(FABRICATED_QS_TOPMARGIN, 100)};
        binding.qsTopMarginSeekbar.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                qsTopMargin[0] = (int) slider.getValue();
                binding.qsTopMarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + qsTopMargin[0] + "dp");
                binding.resetQsTopMargin.setVisibility(View.VISIBLE);
                Prefs.putInt(FABRICATED_QS_TOPMARGIN, qsTopMargin[0]);
                FabricatedUtil.buildAndEnableOverlays(
                        new Object[]{FRAMEWORK_PACKAGE, "quick_qs_total_height", "dimen", "quick_qs_total_height", qsTopMargin[0] + "dp"},
                        new Object[]{SYSTEMUI_PACKAGE, "qs_panel_padding_top", "dimen", "qs_panel_padding_top", qsTopMargin[0] + "dp"},
                        new Object[]{SYSTEMUI_PACKAGE, "qs_panel_padding_top_combined_headers", "dimen", "qs_panel_padding_top_combined_headers", qsTopMargin[0] + "dp"}
                );
            }
        });

        // QS Reset button
        boolean reset_qs_top_margin_visible = Prefs.getBoolean("fabricatedquick_qs_total_height", false) || Prefs.getBoolean("fabricatedqs_panel_padding_top", false) || Prefs.getBoolean("fabricatedqs_panel_padding_top_combined_headers", false);
        binding.resetQsTopMargin.setVisibility(reset_qs_top_margin_visible ? View.VISIBLE : View.INVISIBLE);

        binding.resetQsTopMargin.setOnLongClickListener(v -> {
            FabricatedUtil.disableOverlays("quick_qs_total_height", "qs_panel_padding_top", "qs_panel_padding_top_combined_headers");
            binding.resetQsTopMargin.setVisibility(View.INVISIBLE);
            return true;
        });
    }
}