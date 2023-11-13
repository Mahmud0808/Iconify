package com.drdisagree.iconify.ui.fragments;

import static com.drdisagree.iconify.common.Const.SWITCH_ANIMATION_DELAY;
import static com.drdisagree.iconify.common.Preferences.AGGRESSIVE_QSPANEL_BLUR_SWITCH;
import static com.drdisagree.iconify.common.Preferences.BLUR_RADIUS_VALUE;
import static com.drdisagree.iconify.common.Preferences.NOTIF_TRANSPARENCY_SWITCH;
import static com.drdisagree.iconify.common.Preferences.QSALPHA_LEVEL;
import static com.drdisagree.iconify.common.Preferences.QSPANEL_BLUR_SWITCH;
import static com.drdisagree.iconify.common.Preferences.QS_TRANSPARENCY_SWITCH;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.RPrefs;
import com.drdisagree.iconify.databinding.FragmentXposedTransparencyBlurBinding;
import com.drdisagree.iconify.ui.base.BaseFragment;
import com.drdisagree.iconify.ui.utils.ViewHelper;
import com.drdisagree.iconify.utils.SystemUtil;
import com.google.android.material.slider.Slider;

public class XposedTransparencyBlur extends BaseFragment {

    private FragmentXposedTransparencyBlurBinding binding;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentXposedTransparencyBlurBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Header
        ViewHelper.setHeader(requireContext(), getParentFragmentManager(), binding.header.toolbar, R.string.activity_title_transparency_blur);

        // Qs Panel & Notification Shade Transparency
        binding.transparentQsPanel.setSwitchChecked(RPrefs.getBoolean(QS_TRANSPARENCY_SWITCH, false));
        binding.transparentQsPanel.setSwitchChangeListener(qsTransparencyListener);

        binding.transparentNotifShade.setSwitchChecked(RPrefs.getBoolean(NOTIF_TRANSPARENCY_SWITCH, false));
        binding.transparentNotifShade.setSwitchChangeListener(notifTransparencyListener);

        // Tansparency Alpha
        binding.transparencySlider.setEnabled(binding.transparentQsPanel.isSwitchChecked() || binding.transparentNotifShade.isSwitchChecked());
        binding.transparencySlider.setSliderValue(RPrefs.getInt(QSALPHA_LEVEL, 60));
        binding.transparencySlider.setOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                RPrefs.putInt(QSALPHA_LEVEL, (int) slider.getValue());
            }
        });
        binding.transparencySlider.setResetClickListener(v -> {
            RPrefs.putInt(QSALPHA_LEVEL, 60);
            return true;
        });

        // Qs Panel Blur Enabler
        RPrefs.putBoolean(QSPANEL_BLUR_SWITCH, SystemUtil.isBlurEnabled(false));
        binding.blur.setSwitchChecked(RPrefs.getBoolean(QSPANEL_BLUR_SWITCH, false));
        binding.blur.setSwitchChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(QSPANEL_BLUR_SWITCH, isChecked);
            if (isChecked) {
                SystemUtil.enableBlur(false);
            } else {
                binding.aggressiveBlur.setSwitchChecked(false);
                SystemUtil.disableBlur(false);
            }
            binding.aggressiveBlur.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        // Aggressive Qs Panel Blur Enabler
        RPrefs.putBoolean(AGGRESSIVE_QSPANEL_BLUR_SWITCH, SystemUtil.isBlurEnabled(true));
        binding.aggressiveBlur.setVisibility(binding.blur.isSwitchChecked() ? View.VISIBLE : View.GONE);
        binding.aggressiveBlur.setSwitchChecked(RPrefs.getBoolean(AGGRESSIVE_QSPANEL_BLUR_SWITCH, false));
        binding.aggressiveBlur.setSwitchChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(AGGRESSIVE_QSPANEL_BLUR_SWITCH, isChecked);
            if (isChecked) {
                SystemUtil.enableBlur(true);
            } else {
                SystemUtil.disableBlur(true);
            }
        });

        // Blur Intensity
        binding.blurIntensity.setSliderValue(RPrefs.getInt(BLUR_RADIUS_VALUE, 23));
        binding.blurIntensity.setOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                RPrefs.putInt(BLUR_RADIUS_VALUE, (int) slider.getValue());
                new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::handleSystemUIRestart, SWITCH_ANIMATION_DELAY);
            }
        });
        binding.blurIntensity.setResetClickListener(v -> {
            RPrefs.putInt(BLUR_RADIUS_VALUE, 23);
            new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::handleSystemUIRestart, SWITCH_ANIMATION_DELAY);
            return true;
        });

        return view;
    }

    CompoundButton.OnCheckedChangeListener qsTransparencyListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            RPrefs.putBoolean(QS_TRANSPARENCY_SWITCH, isChecked);

            if (isChecked) {
                RPrefs.putBoolean(NOTIF_TRANSPARENCY_SWITCH, false);
                binding.transparentNotifShade.setSwitchChangeListener(null);
                binding.transparentNotifShade.setSwitchChecked(false);
                binding.transparentNotifShade.setSwitchChangeListener(notifTransparencyListener);
            }

            binding.transparencySlider.setEnabled(binding.transparentQsPanel.isSwitchChecked() || binding.transparentNotifShade.isSwitchChecked());

            new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::handleSystemUIRestart, SWITCH_ANIMATION_DELAY);
        }
    };

    CompoundButton.OnCheckedChangeListener notifTransparencyListener = (buttonView, isChecked) -> {
        RPrefs.putBoolean(NOTIF_TRANSPARENCY_SWITCH, isChecked);

        if (isChecked) {
            RPrefs.putBoolean(QS_TRANSPARENCY_SWITCH, false);
            binding.transparentQsPanel.setSwitchChangeListener(null);
            binding.transparentQsPanel.setSwitchChecked(false);
            binding.transparentQsPanel.setSwitchChangeListener(qsTransparencyListener);
        }

        binding.transparencySlider.setEnabled(binding.transparentQsPanel.isSwitchChecked() || binding.transparentNotifShade.isSwitchChecked());

        new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::handleSystemUIRestart, SWITCH_ANIMATION_DELAY);
    };
}