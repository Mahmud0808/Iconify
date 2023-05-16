package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Const.FRAMEWORK_PACKAGE;
import static com.drdisagree.iconify.common.Const.SWITCH_ANIMATION_DELAY;
import static com.drdisagree.iconify.common.Preferences.FIXED_STATUS_ICONS_SIDEMARGIN;
import static com.drdisagree.iconify.common.Preferences.FIXED_STATUS_ICONS_SWITCH;
import static com.drdisagree.iconify.common.Preferences.FIXED_STATUS_ICONS_TOPMARGIN;
import static com.drdisagree.iconify.common.Preferences.HIDE_LOCKSCREEN_CARRIER;
import static com.drdisagree.iconify.common.Preferences.HIDE_LOCKSCREEN_STATUSBAR;
import static com.drdisagree.iconify.common.Preferences.HIDE_STATUS_ICONS_SWITCH;
import static com.drdisagree.iconify.common.Preferences.QSPANEL_HIDE_CARRIER;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.RPrefs;
import com.drdisagree.iconify.ui.utils.ViewBindingHelpers;
import com.drdisagree.iconify.utils.FabricatedUtil;
import com.drdisagree.iconify.utils.HelperUtil;
import com.drdisagree.iconify.utils.SystemUtil;

public class XposedOthers extends BaseActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xposed_others);

        // Header
        ViewBindingHelpers.setHeader(this, findViewById(R.id.collapsing_toolbar), findViewById(R.id.toolbar), R.string.activity_title_xposed_others);

        // Hide carrier group
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch hide_qs_carrier_group = findViewById(R.id.hide_qs_carrier_group);
        hide_qs_carrier_group.setChecked(RPrefs.getBoolean(QSPANEL_HIDE_CARRIER, false));
        hide_qs_carrier_group.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(QSPANEL_HIDE_CARRIER, isChecked);

            if (Build.VERSION.SDK_INT >= 33) {
                SystemUtil.restartSystemUI();
            } else {
                HelperUtil.forceApply();
            }
        });

        // Hide status icons
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch hide_status_icons = findViewById(R.id.hide_status_icons);
        hide_status_icons.setChecked(RPrefs.getBoolean(HIDE_STATUS_ICONS_SWITCH, false));
        hide_status_icons.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(HIDE_STATUS_ICONS_SWITCH, isChecked);

            if (Build.VERSION.SDK_INT >= 33) {
                SystemUtil.restartSystemUI();
            } else {
                HelperUtil.forceApply();
            }
        });

        // Hide lockscreen carrier
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch hide_lockscreen_carrier = findViewById(R.id.hide_lockscreen_carrier);
        hide_lockscreen_carrier.setChecked(RPrefs.getBoolean(HIDE_LOCKSCREEN_CARRIER, false));
        hide_lockscreen_carrier.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(HIDE_LOCKSCREEN_CARRIER, isChecked);
            new Handler().postDelayed(SystemUtil::restartSystemUI, SWITCH_ANIMATION_DELAY);
        });

        // Hide lockscreen statusbar
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch hide_lockscreen_statusbar = findViewById(R.id.hide_lockscreen_statusbar);
        hide_lockscreen_statusbar.setChecked(RPrefs.getBoolean(HIDE_LOCKSCREEN_STATUSBAR, false));
        hide_lockscreen_statusbar.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(HIDE_LOCKSCREEN_STATUSBAR, isChecked);
            new Handler().postDelayed(SystemUtil::restartSystemUI, SWITCH_ANIMATION_DELAY);
        });

        // Fixed status icons
        if (Build.VERSION.SDK_INT >= 33) {
            findViewById(R.id.status_icons_container).setVisibility(View.GONE);
            RPrefs.putBoolean(FIXED_STATUS_ICONS_SWITCH, false);
        }

        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enable_fixed_status_icons = findViewById(R.id.enable_fixed_status_icons);
        enable_fixed_status_icons.setChecked(RPrefs.getBoolean(FIXED_STATUS_ICONS_SWITCH, false));
        enable_fixed_status_icons.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(FIXED_STATUS_ICONS_SWITCH, isChecked);
            if (!isChecked) FabricatedUtil.disableOverlay("quickQsOffsetHeight");
            else if (RPrefs.getInt(FIXED_STATUS_ICONS_TOPMARGIN, 0) > 32)
                FabricatedUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, "quickQsOffsetHeight", "dimen", "quick_qs_offset_height", (48 + RPrefs.getInt(FIXED_STATUS_ICONS_TOPMARGIN, 0)) + "dp");

            new Handler().postDelayed(HelperUtil::forceApply, SWITCH_ANIMATION_DELAY);
        });

        // Status icons top margin
        SeekBar status_icons_top_margin_seekbar = findViewById(R.id.status_icons_top_margin_seekbar);
        TextView status_icons_top_margin_output = findViewById(R.id.status_icons_top_margin_output);
        status_icons_top_margin_output.setText(getResources().getString(R.string.opt_selected) + ' ' + RPrefs.getInt(FIXED_STATUS_ICONS_TOPMARGIN, 0) + "dp");
        status_icons_top_margin_seekbar.setProgress(RPrefs.getInt(FIXED_STATUS_ICONS_TOPMARGIN, 0));
        final int[] topMarginStatusIcons = {RPrefs.getInt(FIXED_STATUS_ICONS_TOPMARGIN, 0)};
        status_icons_top_margin_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                topMarginStatusIcons[0] = progress;
                status_icons_top_margin_output.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "dp");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                RPrefs.putInt(FIXED_STATUS_ICONS_TOPMARGIN, topMarginStatusIcons[0]);
                if (RPrefs.getBoolean(FIXED_STATUS_ICONS_SWITCH, false)) {
                    FabricatedUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, "quickQsOffsetHeight", "dimen", "quick_qs_offset_height", (48 + topMarginStatusIcons[0]) + "dp");
                    new Handler().postDelayed(HelperUtil::forceApply, SWITCH_ANIMATION_DELAY);
                }
            }
        });

        // Status icons side margin
        SeekBar status_icons_side_margin_seekbar = findViewById(R.id.status_icons_side_margin_seekbar);
        TextView status_icons_side_margin_output = findViewById(R.id.status_icons_side_margin_output);
        status_icons_side_margin_output.setText(getResources().getString(R.string.opt_selected) + ' ' + RPrefs.getInt(FIXED_STATUS_ICONS_SIDEMARGIN, 0) + "dp");
        status_icons_side_margin_seekbar.setProgress(RPrefs.getInt(FIXED_STATUS_ICONS_SIDEMARGIN, 0));
        final int[] sideMarginStatusIcons = {RPrefs.getInt(FIXED_STATUS_ICONS_SIDEMARGIN, 0)};
        status_icons_side_margin_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sideMarginStatusIcons[0] = progress;
                status_icons_side_margin_output.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "dp");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                RPrefs.putInt(FIXED_STATUS_ICONS_SIDEMARGIN, sideMarginStatusIcons[0]);
                if (RPrefs.getBoolean(FIXED_STATUS_ICONS_SWITCH, false)) {
                    new Handler().postDelayed(HelperUtil::forceApply, SWITCH_ANIMATION_DELAY);
                }
            }
        });
    }
}