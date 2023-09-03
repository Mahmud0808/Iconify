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
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.RPrefs;
import com.drdisagree.iconify.databinding.ActivityXposedOthersBinding;
import com.drdisagree.iconify.ui.utils.ViewBindingHelpers;
import com.drdisagree.iconify.utils.FabricatedUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.google.android.material.slider.Slider;

public class XposedOthers extends BaseActivity {

    private ActivityXposedOthersBinding binding;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityXposedOthersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Header
        ViewBindingHelpers.setHeader(this, binding.header.toolbar, R.string.activity_title_xposed_others);

        // Hide carrier group
        binding.hideQsCarrierGroup.setChecked(RPrefs.getBoolean(QSPANEL_HIDE_CARRIER, false));
        binding.hideQsCarrierGroup.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(QSPANEL_HIDE_CARRIER, isChecked);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (Build.VERSION.SDK_INT >= 33) {
                    SystemUtil.handleSystemUIRestart();
                } else {
                    SystemUtil.doubleToggleDarkMode();
                }
            }, SWITCH_ANIMATION_DELAY);
        });
        binding.hideQsCarrierGroupContainer.setOnClickListener(v -> binding.hideQsCarrierGroup.toggle());

        // Hide status icons
        binding.hideStatusIcons.setChecked(RPrefs.getBoolean(HIDE_STATUS_ICONS_SWITCH, false));
        binding.hideStatusIcons.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(HIDE_STATUS_ICONS_SWITCH, isChecked);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (Build.VERSION.SDK_INT >= 33) {
                    SystemUtil.handleSystemUIRestart();
                } else {
                    SystemUtil.doubleToggleDarkMode();
                }
            }, SWITCH_ANIMATION_DELAY);
        });
        binding.hideStatusIconsContainer.setOnClickListener(v -> binding.hideStatusIcons.toggle());

        // Hide lockscreen carrier
        binding.hideLockscreenCarrier.setChecked(RPrefs.getBoolean(HIDE_LOCKSCREEN_CARRIER, false));
        binding.hideLockscreenCarrier.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(HIDE_LOCKSCREEN_CARRIER, isChecked);
            new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::handleSystemUIRestart, SWITCH_ANIMATION_DELAY);
        });
        binding.hideLockscreenCarrierContainer.setOnClickListener(v -> binding.hideLockscreenCarrier.toggle());

        // Hide lockscreen statusbar
        binding.hideLockscreenStatusbar.setChecked(RPrefs.getBoolean(HIDE_LOCKSCREEN_STATUSBAR, false));
        binding.hideLockscreenStatusbar.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(HIDE_LOCKSCREEN_STATUSBAR, isChecked);
            new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::handleSystemUIRestart, SWITCH_ANIMATION_DELAY);
        });
        binding.hideLockscreenStatusbarContainer.setOnClickListener(v -> binding.hideLockscreenStatusbar.toggle());

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
        binding.statusIconsTopMarginSeekbar.setValue(RPrefs.getInt(FIXED_STATUS_ICONS_TOPMARGIN, 0));
        final int[] topMarginStatusIcons = {RPrefs.getInt(FIXED_STATUS_ICONS_TOPMARGIN, 0)};
        binding.statusIconsTopMarginSeekbar.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                topMarginStatusIcons[0] = (int) slider.getValue();
                binding.statusIconsTopMarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + topMarginStatusIcons[0] + "dp");
                RPrefs.putInt(FIXED_STATUS_ICONS_TOPMARGIN, topMarginStatusIcons[0]);
                if (RPrefs.getBoolean(FIXED_STATUS_ICONS_SWITCH, false)) {
                    FabricatedUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, "quickQsOffsetHeight", "dimen", "quick_qs_offset_height", (48 + topMarginStatusIcons[0]) + "dp");
                    new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::doubleToggleDarkMode, SWITCH_ANIMATION_DELAY);
                }
            }
        });

        // Status icons side margin
        binding.statusIconsSideMarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + RPrefs.getInt(FIXED_STATUS_ICONS_SIDEMARGIN, 0) + "dp");
        binding.statusIconsSideMarginSeekbar.setValue(RPrefs.getInt(FIXED_STATUS_ICONS_SIDEMARGIN, 0));
        final int[] sideMarginStatusIcons = {RPrefs.getInt(FIXED_STATUS_ICONS_SIDEMARGIN, 0)};
        binding.statusIconsSideMarginSeekbar.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                sideMarginStatusIcons[0] = (int) slider.getValue();
                binding.statusIconsSideMarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + sideMarginStatusIcons[0] + "dp");
                RPrefs.putInt(FIXED_STATUS_ICONS_SIDEMARGIN, sideMarginStatusIcons[0]);
                if (RPrefs.getBoolean(FIXED_STATUS_ICONS_SWITCH, false)) {
                    new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::doubleToggleDarkMode, SWITCH_ANIMATION_DELAY);
                }
            }
        });
    }
}