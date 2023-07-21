package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Const.FRAMEWORK_PACKAGE;
import static com.drdisagree.iconify.common.Const.SWITCH_ANIMATION_DELAY;
import static com.drdisagree.iconify.common.Preferences.FIXED_STATUS_ICONS_SIDEMARGIN;
import static com.drdisagree.iconify.common.Preferences.FIXED_STATUS_ICONS_SWITCH;
import static com.drdisagree.iconify.common.Preferences.FIXED_STATUS_ICONS_TOPMARGIN;
import static com.drdisagree.iconify.common.Preferences.HIDE_DATA_DISABLED_ICON;
import static com.drdisagree.iconify.common.Preferences.HIDE_LOCKSCREEN_CARRIER;
import static com.drdisagree.iconify.common.Preferences.HIDE_LOCKSCREEN_STATUSBAR;
import static com.drdisagree.iconify.common.Preferences.HIDE_STATUS_ICONS_SWITCH;
import static com.drdisagree.iconify.common.Preferences.QSPANEL_HIDE_CARRIER;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.SeekBar;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.RPrefs;
import com.drdisagree.iconify.databinding.ActivityXposedOthersBinding;
import com.drdisagree.iconify.ui.utils.ViewBindingHelpers;
import com.drdisagree.iconify.utils.FabricatedUtil;
import com.drdisagree.iconify.utils.SystemUtil;

public class XposedOthers extends BaseActivity {

    private ActivityXposedOthersBinding binding;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityXposedOthersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Header
        ViewBindingHelpers.setHeader(this, binding.header.collapsingToolbar, binding.header.toolbar, R.string.activity_title_xposed_others);

        // Hide carrier group
        binding.hideQsCarrierGroup.setChecked(RPrefs.getBoolean(QSPANEL_HIDE_CARRIER, false));
        binding.hideQsCarrierGroup.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(QSPANEL_HIDE_CARRIER, isChecked);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (Build.VERSION.SDK_INT >= 33) {
                    SystemUtil.restartSystemUI();
                } else {
                    SystemUtil.doubleToggleDarkMode();
                }
            }, SWITCH_ANIMATION_DELAY);
        });

        // Hide status icons
        binding.hideStatusIcons.setChecked(RPrefs.getBoolean(HIDE_STATUS_ICONS_SWITCH, false));
        binding.hideStatusIcons.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(HIDE_STATUS_ICONS_SWITCH, isChecked);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (Build.VERSION.SDK_INT >= 33) {
                    SystemUtil.restartSystemUI();
                } else {
                    SystemUtil.doubleToggleDarkMode();
                }
            }, SWITCH_ANIMATION_DELAY);
        });

        // Hide data disabled icon
        binding.hideDataDisabledIcon.setChecked(RPrefs.getBoolean(HIDE_DATA_DISABLED_ICON, false));
        binding.hideDataDisabledIcon.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(HIDE_DATA_DISABLED_ICON, isChecked);
        });

        // Hide lockscreen carrier
        binding.hideLockscreenCarrier.setChecked(RPrefs.getBoolean(HIDE_LOCKSCREEN_CARRIER, false));
        binding.hideLockscreenCarrier.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(HIDE_LOCKSCREEN_CARRIER, isChecked);
            new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::restartSystemUI, SWITCH_ANIMATION_DELAY);
        });

        // Hide lockscreen statusbar
        binding.hideLockscreenStatusbar.setChecked(RPrefs.getBoolean(HIDE_LOCKSCREEN_STATUSBAR, false));
        binding.hideLockscreenStatusbar.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(HIDE_LOCKSCREEN_STATUSBAR, isChecked);
            new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::restartSystemUI, SWITCH_ANIMATION_DELAY);
        });

        // Fixed status icons
        if (Build.VERSION.SDK_INT >= 33) {
            binding.statusIconsContainer.setVisibility(View.GONE);
            RPrefs.putBoolean(FIXED_STATUS_ICONS_SWITCH, false);
        }

        binding.enableFixedStatusIcons.setChecked(RPrefs.getBoolean(FIXED_STATUS_ICONS_SWITCH, false));
        binding.enableFixedStatusIcons.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(FIXED_STATUS_ICONS_SWITCH, isChecked);
            if (!isChecked) FabricatedUtil.disableOverlay("quickQsOffsetHeight");
            else if (RPrefs.getInt(FIXED_STATUS_ICONS_TOPMARGIN, 0) > 32)
                FabricatedUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, "quickQsOffsetHeight", "dimen", "quick_qs_offset_height", (48 + RPrefs.getInt(FIXED_STATUS_ICONS_TOPMARGIN, 0)) + "dp");

            new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::doubleToggleDarkMode, SWITCH_ANIMATION_DELAY);
        });

        // Status icons top margin
        binding.statusIconsTopMarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + RPrefs.getInt(FIXED_STATUS_ICONS_TOPMARGIN, 0) + "dp");
        binding.statusIconsTopMarginSeekbar.setProgress(RPrefs.getInt(FIXED_STATUS_ICONS_TOPMARGIN, 0));
        final int[] topMarginStatusIcons = {RPrefs.getInt(FIXED_STATUS_ICONS_TOPMARGIN, 0)};
        binding.statusIconsTopMarginSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                topMarginStatusIcons[0] = progress;
                binding.statusIconsTopMarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "dp");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                RPrefs.putInt(FIXED_STATUS_ICONS_TOPMARGIN, topMarginStatusIcons[0]);
                if (RPrefs.getBoolean(FIXED_STATUS_ICONS_SWITCH, false)) {
                    FabricatedUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, "quickQsOffsetHeight", "dimen", "quick_qs_offset_height", (48 + topMarginStatusIcons[0]) + "dp");
                    new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::doubleToggleDarkMode, SWITCH_ANIMATION_DELAY);
                }
            }
        });

        // Status icons side margin
        binding.statusIconsSideMarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + RPrefs.getInt(FIXED_STATUS_ICONS_SIDEMARGIN, 0) + "dp");
        binding.statusIconsSideMarginSeekbar.setProgress(RPrefs.getInt(FIXED_STATUS_ICONS_SIDEMARGIN, 0));
        final int[] sideMarginStatusIcons = {RPrefs.getInt(FIXED_STATUS_ICONS_SIDEMARGIN, 0)};
        binding.statusIconsSideMarginSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sideMarginStatusIcons[0] = progress;
                binding.statusIconsSideMarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "dp");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                RPrefs.putInt(FIXED_STATUS_ICONS_SIDEMARGIN, sideMarginStatusIcons[0]);
                if (RPrefs.getBoolean(FIXED_STATUS_ICONS_SWITCH, false)) {
                    new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::doubleToggleDarkMode, SWITCH_ANIMATION_DELAY);
                }
            }
        });
    }
}