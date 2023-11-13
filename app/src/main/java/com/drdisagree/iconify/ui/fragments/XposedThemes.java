package com.drdisagree.iconify.ui.fragments;

import static com.drdisagree.iconify.common.Const.SWITCH_ANIMATION_DELAY;
import static com.drdisagree.iconify.common.Preferences.BLACK_QSPANEL;
import static com.drdisagree.iconify.common.Preferences.DUALTONE_QSPANEL;
import static com.drdisagree.iconify.common.Preferences.FIX_NOTIFICATION_COLOR;
import static com.drdisagree.iconify.common.Preferences.FLUID_NOTIF_TRANSPARENCY;
import static com.drdisagree.iconify.common.Preferences.FLUID_POWERMENU_TRANSPARENCY;
import static com.drdisagree.iconify.common.Preferences.FLUID_QSPANEL;
import static com.drdisagree.iconify.common.Preferences.LIGHT_QSPANEL;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.RPrefs;
import com.drdisagree.iconify.databinding.FragmentXposedThemesBinding;
import com.drdisagree.iconify.ui.base.BaseFragment;
import com.drdisagree.iconify.ui.utils.ViewHelper;
import com.drdisagree.iconify.utils.SystemUtil;

public class XposedThemes extends BaseFragment {

    private FragmentXposedThemesBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentXposedThemesBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Header
        ViewHelper.setHeader(requireContext(), getParentFragmentManager(), binding.header.toolbar, R.string.activity_title_themes);

        // Light Theme
        binding.lightTheme.setSwitchChecked(RPrefs.getBoolean(LIGHT_QSPANEL, false));
        binding.lightTheme.setSwitchChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(LIGHT_QSPANEL, isChecked);
            binding.dualTone.setEnabled(isChecked);
            new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::handleSystemUIRestart, SWITCH_ANIMATION_DELAY);
        });
        binding.dualTone.setEnabled(binding.lightTheme.isSwitchChecked());

        // Dual Tone
        binding.dualTone.setSwitchChecked(RPrefs.getBoolean(DUALTONE_QSPANEL, false));
        binding.dualTone.setSwitchChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(DUALTONE_QSPANEL, isChecked);
        });

        // Pixel Black Theme
        binding.blackTheme.setSwitchChecked(RPrefs.getBoolean(BLACK_QSPANEL, false));
        binding.blackTheme.setSwitchChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(BLACK_QSPANEL, isChecked);
            new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::handleSystemUIRestart, SWITCH_ANIMATION_DELAY);
        });

        // Fluid QS Theme
        binding.fluidQsTheme.setSwitchChecked(RPrefs.getBoolean(FLUID_QSPANEL, false));
        binding.fluidQsTheme.setSwitchChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(FLUID_QSPANEL, isChecked);
            binding.fluidNotifTheme.setEnabled(isChecked);
            binding.fluidPowermenuTheme.setEnabled(isChecked);
            new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::handleSystemUIRestart, SWITCH_ANIMATION_DELAY);
        });
        binding.fluidNotifTheme.setEnabled(binding.fluidQsTheme.isSwitchChecked());
        binding.fluidPowermenuTheme.setEnabled(binding.fluidQsTheme.isSwitchChecked());

        // Fluid QS Notification Transparency
        binding.fluidNotifTheme.setSwitchChecked(RPrefs.getBoolean(FLUID_NOTIF_TRANSPARENCY, false));
        binding.fluidNotifTheme.setSwitchChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(FLUID_NOTIF_TRANSPARENCY, isChecked);
            new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::handleSystemUIRestart, SWITCH_ANIMATION_DELAY);
        });

        // Fluid QS Power Menu Transparency
        binding.fluidPowermenuTheme.setSwitchChecked(RPrefs.getBoolean(FLUID_POWERMENU_TRANSPARENCY, false));
        binding.fluidPowermenuTheme.setSwitchChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(FLUID_POWERMENU_TRANSPARENCY, isChecked);
            new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::handleSystemUIRestart, SWITCH_ANIMATION_DELAY);
        });

        // Fix notification color
        binding.sectionOthers.setVisibility(Build.VERSION.SDK_INT >= 34 ? View.VISIBLE : View.GONE);
        binding.fixNotificationColor.setVisibility(Build.VERSION.SDK_INT >= 34 ? View.VISIBLE : View.GONE);

        binding.fixNotificationColor.setSwitchChecked(RPrefs.getBoolean(FIX_NOTIFICATION_COLOR, true));
        binding.fixNotificationColor.setSwitchChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(FIX_NOTIFICATION_COLOR, isChecked);
            new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::handleSystemUIRestart, SWITCH_ANIMATION_DELAY);
        });

        return view;
    }
}