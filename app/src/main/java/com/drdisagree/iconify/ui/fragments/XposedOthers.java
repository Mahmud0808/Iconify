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
        binding.hideQsCarrierGroup.setSwitchChecked(RPrefs.getBoolean(QSPANEL_HIDE_CARRIER, false));
        binding.hideQsCarrierGroup.setSwitchChangeListener((buttonView, isChecked) -> RPrefs.putBoolean(QSPANEL_HIDE_CARRIER, isChecked));

        // Hide status icons
        binding.hideStatusIcons.setSwitchChecked(RPrefs.getBoolean(HIDE_STATUS_ICONS_SWITCH, false));
        binding.hideStatusIcons.setSwitchChangeListener((buttonView, isChecked) -> RPrefs.putBoolean(HIDE_STATUS_ICONS_SWITCH, isChecked));

        // Hide lockscreen carrier
        binding.hideLockscreenCarrier.setSwitchChecked(RPrefs.getBoolean(HIDE_LOCKSCREEN_CARRIER, false));
        binding.hideLockscreenCarrier.setSwitchChangeListener((buttonView, isChecked) -> RPrefs.putBoolean(HIDE_LOCKSCREEN_CARRIER, isChecked));

        // Hide lockscreen statusbar
        binding.hideLockscreenStatusbar.setSwitchChecked(RPrefs.getBoolean(HIDE_LOCKSCREEN_STATUSBAR, false));
        binding.hideLockscreenStatusbar.setSwitchChangeListener((buttonView, isChecked) -> RPrefs.putBoolean(HIDE_LOCKSCREEN_STATUSBAR, isChecked));

        // Fixed status icons
        binding.fixedStatusIcons.setSwitchChecked(RPrefs.getBoolean(FIXED_STATUS_ICONS_SWITCH, false));
        binding.fixedStatusIcons.setSwitchChangeListener((buttonView, isChecked) -> {
            binding.statusIconsTopMargin.setEnabled(isChecked);
            binding.statusIconsSideMargin.setEnabled(isChecked);
            RPrefs.putBoolean(FIXED_STATUS_ICONS_SWITCH, isChecked);
        });

        // Status icons top margin
        if (Build.VERSION.SDK_INT >= 33) {
            binding.statusIconsTopMargin.setSliderValueTo(250);
        }
        binding.statusIconsTopMargin.setSliderValue(RPrefs.getInt(FIXED_STATUS_ICONS_TOPMARGIN, 8));
        binding.statusIconsTopMargin.setEnabled(
                Build.VERSION.SDK_INT >= 33 ?
                        RPrefs.getBoolean(QSPANEL_STATUSICONSBG_SWITCH, false) ||
                                RPrefs.getBoolean(FIXED_STATUS_ICONS_SWITCH, false) :
                        RPrefs.getBoolean(FIXED_STATUS_ICONS_SWITCH, false)
        );
        binding.statusIconsTopMargin.setOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                RPrefs.putInt(FIXED_STATUS_ICONS_TOPMARGIN, (int) slider.getValue());
            }
        });

        // Status icons side margin
        binding.statusIconsSideMargin.setSliderValue(RPrefs.getInt(FIXED_STATUS_ICONS_SIDEMARGIN, 0));
        binding.statusIconsSideMargin.setEnabled(RPrefs.getBoolean(FIXED_STATUS_ICONS_SWITCH, false));
        binding.statusIconsSideMargin.setOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                RPrefs.putInt(FIXED_STATUS_ICONS_SIDEMARGIN, (int) slider.getValue());
            }
        });

        return view;
    }
}