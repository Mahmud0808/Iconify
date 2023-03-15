package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Const.FRAMEWORK_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.FIXED_STATUS_ICONS_SIDEMARGIN;
import static com.drdisagree.iconify.common.Preferences.FIXED_STATUS_ICONS_SWITCH;
import static com.drdisagree.iconify.common.Preferences.FIXED_STATUS_ICONS_TOPMARGIN;
import static com.drdisagree.iconify.common.Preferences.HIDE_STATUS_ICONS_SWITCH;
import static com.drdisagree.iconify.common.Preferences.QSPANEL_HIDE_CARRIER;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.RPrefs;
import com.drdisagree.iconify.utils.FabricatedUtil;
import com.drdisagree.iconify.utils.HelperUtil;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.Objects;

public class XposedOthers extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xposed_others);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_xposed_others));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Hide carrier group
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch hide_qs_carrier_group = findViewById(R.id.hide_qs_carrier_group);
        hide_qs_carrier_group.setChecked(RPrefs.getBoolean(QSPANEL_HIDE_CARRIER, false));
        hide_qs_carrier_group.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(QSPANEL_HIDE_CARRIER, isChecked);
            new Handler().postDelayed(HelperUtil::forceApply, 200);
        });

        // Hide status icons
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch hide_status_icons = findViewById(R.id.hide_status_icons);
        hide_status_icons.setChecked(RPrefs.getBoolean(HIDE_STATUS_ICONS_SWITCH, false));
        hide_status_icons.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(HIDE_STATUS_ICONS_SWITCH, isChecked);
            new Handler().postDelayed(HelperUtil::forceApply, 200);
        });

        // Fixed status icons
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enable_fixed_status_icons = findViewById(R.id.enable_fixed_status_icons);
        enable_fixed_status_icons.setChecked(RPrefs.getBoolean(FIXED_STATUS_ICONS_SWITCH, false));
        enable_fixed_status_icons.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(FIXED_STATUS_ICONS_SWITCH, isChecked);
            if (!isChecked) FabricatedUtil.disableOverlay("quickQsOffsetHeight");
            else if (RPrefs.getInt(FIXED_STATUS_ICONS_TOPMARGIN, 0) > 32)
                FabricatedUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, "quickQsOffsetHeight", "dimen", "quick_qs_offset_height", (48 + RPrefs.getInt(FIXED_STATUS_ICONS_TOPMARGIN, 0)) + "dp");

            new Handler().postDelayed(HelperUtil::forceApply, 200);
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
                    new Handler().postDelayed(HelperUtil::forceApply, 200);
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
                    new Handler().postDelayed(HelperUtil::forceApply, 200);
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