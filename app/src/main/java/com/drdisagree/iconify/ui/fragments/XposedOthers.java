package com.drdisagree.iconify.ui.fragments;

import static com.drdisagree.iconify.common.Preferences.FIXED_STATUS_ICONS_SIDEMARGIN;
import static com.drdisagree.iconify.common.Preferences.FIXED_STATUS_ICONS_SWITCH;
import static com.drdisagree.iconify.common.Preferences.FIXED_STATUS_ICONS_TOPMARGIN;
import static com.drdisagree.iconify.common.Preferences.HIDE_LOCKSCREEN_CARRIER;
import static com.drdisagree.iconify.common.Preferences.HIDE_LOCKSCREEN_STATUSBAR;
import static com.drdisagree.iconify.common.Preferences.HIDE_STATUS_ICONS_SWITCH;
import static com.drdisagree.iconify.common.Preferences.QSPANEL_HIDE_CARRIER;
import static com.drdisagree.iconify.common.Preferences.QSPANEL_STATUSICONSBG_SWITCH;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.RPrefs;
import com.drdisagree.iconify.databinding.FragmentXposedOthersBinding;
import com.drdisagree.iconify.ui.base.BaseFragment;
import com.drdisagree.iconify.ui.utils.ViewHelper;
import com.google.android.material.slider.Slider;

public class XposedOthers extends BaseFragment {

    private FragmentXposedOthersBinding binding;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentXposedOthersBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Header
        ViewHelper.setHeader(requireContext(), getParentFragmentManager(), binding.header.toolbar, R.string.activity_title_xposed_others);

        // Hide carrier group
        binding.hideQsCarrierGroup.setChecked(RPrefs.getBoolean(QSPANEL_HIDE_CARRIER, false));
        binding.hideQsCarrierGroup.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(QSPANEL_HIDE_CARRIER, isChecked);
        });
        binding.hideQsCarrierGroupContainer.setOnClickListener(v -> binding.hideQsCarrierGroup.toggle());

        // Hide status icons
        binding.hideStatusIcons.setChecked(RPrefs.getBoolean(HIDE_STATUS_ICONS_SWITCH, false));
        binding.hideStatusIcons.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(HIDE_STATUS_ICONS_SWITCH, isChecked);
        });
        binding.hideStatusIconsContainer.setOnClickListener(v -> binding.hideStatusIcons.toggle());

        // Hide lockscreen carrier
        binding.hideLockscreenCarrier.setChecked(RPrefs.getBoolean(HIDE_LOCKSCREEN_CARRIER, false));
        binding.hideLockscreenCarrier.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(HIDE_LOCKSCREEN_CARRIER, isChecked);
        });
        binding.hideLockscreenCarrierContainer.setOnClickListener(v -> binding.hideLockscreenCarrier.toggle());

        // Hide lockscreen statusbar
        binding.hideLockscreenStatusbar.setChecked(RPrefs.getBoolean(HIDE_LOCKSCREEN_STATUSBAR, false));
        binding.hideLockscreenStatusbar.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(HIDE_LOCKSCREEN_STATUSBAR, isChecked);
        });
        binding.hideLockscreenStatusbarContainer.setOnClickListener(v -> binding.hideLockscreenStatusbar.toggle());

        // Fixed status icons
        binding.enableFixedStatusIcons.setChecked(RPrefs.getBoolean(FIXED_STATUS_ICONS_SWITCH, false));
        binding.enableFixedStatusIcons.setOnCheckedChangeListener((buttonView, isChecked) -> {
            binding.statusIconsSideMarginSeekbar.setEnabled(isChecked);
            binding.statusIconsTopMarginSeekbar.setEnabled(isChecked);
            RPrefs.putBoolean(FIXED_STATUS_ICONS_SWITCH, isChecked);
        });
        binding.enableFixedStatusIconsContainer.setOnClickListener(v -> binding.enableFixedStatusIcons.toggle());

        // Status icons top margin
        binding.statusIconsTopMarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + RPrefs.getInt(FIXED_STATUS_ICONS_TOPMARGIN, 8) + "dp");
        binding.statusIconsTopMarginSeekbar.setValue(RPrefs.getInt(FIXED_STATUS_ICONS_TOPMARGIN, 8));
        binding.statusIconsTopMarginSeekbar.setEnabled(
                Build.VERSION.SDK_INT >= 33 ?
                        RPrefs.getBoolean(QSPANEL_STATUSICONSBG_SWITCH, false) || RPrefs.getBoolean(FIXED_STATUS_ICONS_SWITCH, false) :
                        RPrefs.getBoolean(FIXED_STATUS_ICONS_SWITCH, false)
        );
        if (Build.VERSION.SDK_INT >= 33) binding.statusIconsTopMarginSeekbar.setValueTo(200);
        final int[] topMarginStatusIcons = {RPrefs.getInt(FIXED_STATUS_ICONS_TOPMARGIN, 8)};
        binding.statusIconsTopMarginSeekbar.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                topMarginStatusIcons[0] = (int) slider.getValue();
                binding.statusIconsTopMarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + topMarginStatusIcons[0] + "dp");
                RPrefs.putInt(FIXED_STATUS_ICONS_TOPMARGIN, topMarginStatusIcons[0]);
            }
        });

        // Status icons side margin
        binding.statusIconsSideMarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + RPrefs.getInt(FIXED_STATUS_ICONS_SIDEMARGIN, 0) + "dp");
        binding.statusIconsSideMarginSeekbar.setValue(RPrefs.getInt(FIXED_STATUS_ICONS_SIDEMARGIN, 0));
        binding.statusIconsSideMarginSeekbar.setEnabled(RPrefs.getBoolean(FIXED_STATUS_ICONS_SWITCH, false));
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
            }
        });

        return view;
    }
}