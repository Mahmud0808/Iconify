package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Const.SWITCH_ANIMATION_DELAY;
import static com.drdisagree.iconify.common.Preferences.BLACK_QSPANEL;
import static com.drdisagree.iconify.common.Preferences.DUALTONE_QSPANEL;
import static com.drdisagree.iconify.common.Preferences.FLUID_NOTIF_TRANSPARENCY;
import static com.drdisagree.iconify.common.Preferences.FLUID_POWERMENU_TRANSPARENCY;
import static com.drdisagree.iconify.common.Preferences.FLUID_QSPANEL;
import static com.drdisagree.iconify.common.Preferences.LIGHT_QSPANEL;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.RPrefs;
import com.drdisagree.iconify.databinding.ActivityXposedQuickSettingsBinding;
import com.drdisagree.iconify.databinding.ActivityXposedThemesBinding;
import com.drdisagree.iconify.ui.utils.ViewHelper;
import com.drdisagree.iconify.utils.SystemUtil;

public class XposedThemes extends AppCompatActivity {

    private ActivityXposedThemesBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityXposedThemesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Header
        ViewHelper.setHeader(this, binding.header.toolbar, R.string.activity_title_themes);

        // Light Theme
        binding.enableLightTheme.setChecked(RPrefs.getBoolean(LIGHT_QSPANEL, false));
        binding.enableLightTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(LIGHT_QSPANEL, isChecked);
            binding.enableDualTone.setEnabled(isChecked);
            new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::handleSystemUIRestart, SWITCH_ANIMATION_DELAY);
        });
        binding.lightThemeContainer.setOnClickListener(v -> {
            if (binding.enableLightTheme.isEnabled())
                binding.enableLightTheme.toggle();
        });
        binding.enableDualTone.setEnabled(binding.enableLightTheme.isChecked());

        // Dual Tone
        binding.enableDualTone.setChecked(RPrefs.getBoolean(DUALTONE_QSPANEL, false));
        binding.enableDualTone.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(DUALTONE_QSPANEL, isChecked);
            new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::doubleToggleDarkMode, SWITCH_ANIMATION_DELAY);
        });

        binding.dualToneContainer.setOnClickListener(v -> {
            if (binding.enableDualTone.isEnabled())
                binding.enableDualTone.toggle();
        });

        // Pixel Black Theme
        binding.enableBlackTheme.setChecked(RPrefs.getBoolean(BLACK_QSPANEL, false));
        binding.enableBlackTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(BLACK_QSPANEL, isChecked);
            new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::handleSystemUIRestart, SWITCH_ANIMATION_DELAY);
        });
        binding.blackThemeContainer.setOnClickListener(v -> {
            if (binding.enableBlackTheme.isEnabled())
                binding.enableBlackTheme.toggle();
        });

        // Fluid QS Theme
        binding.enableFluidTheme.setChecked(RPrefs.getBoolean(FLUID_QSPANEL, false));
        binding.enableFluidTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(FLUID_QSPANEL, isChecked);
            binding.enableNotificationTransparency.setEnabled(isChecked);
            binding.enablePowermenuTransparency.setEnabled(isChecked);
            new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::handleSystemUIRestart, SWITCH_ANIMATION_DELAY);
        });
        binding.fluidThemeContainer.setOnClickListener(v -> {
            if (binding.enableFluidTheme.isEnabled())
                binding.enableFluidTheme.toggle();
        });
        binding.enableNotificationTransparency.setEnabled(binding.enableFluidTheme.isChecked());
        binding.enablePowermenuTransparency.setEnabled(binding.enableFluidTheme.isChecked());

        // Fluid QS Notification Transparency
        binding.enableNotificationTransparency.setChecked(RPrefs.getBoolean(FLUID_NOTIF_TRANSPARENCY, false));
        binding.enableNotificationTransparency.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(FLUID_NOTIF_TRANSPARENCY, isChecked);
            new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::handleSystemUIRestart, SWITCH_ANIMATION_DELAY);
        });
        binding.notificationTransparencyContainer.setOnClickListener(v -> {
            if (binding.enableNotificationTransparency.isEnabled())
                binding.enableNotificationTransparency.toggle();
        });

        // Fluid QS Power Menu Transparency
        binding.enablePowermenuTransparency.setChecked(RPrefs.getBoolean(FLUID_POWERMENU_TRANSPARENCY, false));
        binding.enablePowermenuTransparency.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(FLUID_POWERMENU_TRANSPARENCY, isChecked);
            new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::handleSystemUIRestart, SWITCH_ANIMATION_DELAY);
        });
        binding.powermenuTransparencyContainer.setOnClickListener(v -> {
            if (binding.enablePowermenuTransparency.isEnabled())
                binding.enablePowermenuTransparency.toggle();
        });
    }
}