package com.drdisagree.iconify.ui.fragments;

import static com.drdisagree.iconify.common.Const.SWITCH_ANIMATION_DELAY;
import static com.drdisagree.iconify.common.Preferences.COLOR_ACCENT_PRIMARY;
import static com.drdisagree.iconify.common.Preferences.COLOR_ACCENT_SECONDARY;
import static com.drdisagree.iconify.common.Preferences.STR_NULL;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.databinding.FragmentColorEngineBinding;
import com.drdisagree.iconify.ui.base.BaseFragment;
import com.drdisagree.iconify.ui.utils.ViewHelper;
import com.drdisagree.iconify.utils.overlay.OverlayUtil;

public class ColorEngine extends BaseFragment {

    private FragmentColorEngineBinding binding;
    private CompoundButton.OnCheckedChangeListener minimalQsListener = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentColorEngineBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Header
        ViewHelper.setHeader(requireContext(), getParentFragmentManager(), binding.header.toolbar, R.string.activity_title_color_engine);

        // Basic colors
        binding.basicColors.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_colorEngine_to_basicColors));

        // Monet engine
        binding.monetEngine.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_colorEngine_to_monetEngine));

        // Apply monet accent and gradient
        binding.monetAccent.setSwitchChecked(Prefs.getBoolean("IconifyComponentAMAC.overlay"));
        binding.monetAccent.setSwitchChangeListener(monetAccentListener);

        binding.monetGradient.setSwitchChecked(Prefs.getBoolean("IconifyComponentAMGC.overlay"));
        binding.monetGradient.setSwitchChangeListener(monetGradientListener);

        // Pitch Black Dark
        binding.pitchBlackDarkTheme.setSwitchChecked(Prefs.getBoolean("IconifyComponentQSPBD.overlay"));
        binding.pitchBlackDarkTheme.setSwitchChangeListener(pitchBlackDarkListener);

        // Pitch Black Amoled
        binding.pitchBlackAmoledTheme.setSwitchChecked(Prefs.getBoolean("IconifyComponentQSPBA.overlay"));
        binding.pitchBlackAmoledTheme.setSwitchChangeListener(pitchBlackAmoledListener);

        // Minimal QsPanel
        binding.minimalQspanel.setSwitchChecked(Prefs.getBoolean("IconifyComponentQSST.overlay"));
        if (minimalQsListener == null) {
            initializeMinimalQsListener();
        }
        binding.minimalQspanel.setSwitchChangeListener(minimalQsListener);

        // Disable Monet
        binding.disableMonet.setSwitchChecked(Prefs.getBoolean("IconifyComponentDM.overlay"));
        binding.disableMonet.setSwitchChangeListener((buttonView, isChecked) -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isChecked) {
                OverlayUtil.enableOverlay("IconifyComponentDM.overlay");
            } else {
                OverlayUtil.disableOverlay("IconifyComponentDM.overlay");
            }
        }, SWITCH_ANIMATION_DELAY));

        return view;
    }

    private void enableMonetAccent() {
        OverlayUtil.enableOverlay("IconifyComponentAMAC.overlay");
        BasicColors.disableAccentColors();
    }

    private void disableMonetAccent() {
        OverlayUtil.disableOverlays("IconifyComponentAMAC.overlay");
    }

    private void enableMonetGradient() {
        OverlayUtil.enableOverlay("IconifyComponentAMGC.overlay");
        BasicColors.disableAccentColors();
    }

    private void disableMonetGradient() {
        OverlayUtil.disableOverlay("IconifyComponentAMGC.overlay");
    }

    private boolean shouldUseDefaultColors() {
        return OverlayUtil.isOverlayDisabled("IconifyComponentME.overlay");
    }

    private void applyDefaultColors() {
        if (shouldUseDefaultColors()) {
            if (Prefs.getString(COLOR_ACCENT_PRIMARY).equals(STR_NULL)) {
                BasicColors.applyDefaultPrimaryColors();
            } else {
                BasicColors.applyPrimaryColors();
            }
            if (Prefs.getString(COLOR_ACCENT_SECONDARY).equals(STR_NULL)) {
                BasicColors.applyDefaultSecondaryColors();
            } else {
                BasicColors.applySecondaryColors();
            }
        }
    }

    private void initializeMinimalQsListener() {
        minimalQsListener = (buttonView, isChecked) -> {
            if (isChecked) {
                binding.pitchBlackDarkTheme.setSwitchChangeListener(null);
                binding.pitchBlackDarkTheme.setSwitchChecked(false);
                binding.pitchBlackDarkTheme.setSwitchChangeListener(pitchBlackDarkListener);

                binding.pitchBlackAmoledTheme.setSwitchChangeListener(null);
                binding.pitchBlackAmoledTheme.setSwitchChecked(false);
                binding.pitchBlackAmoledTheme.setSwitchChangeListener(pitchBlackAmoledListener);
            }

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (isChecked) {
                    OverlayUtil.changeOverlayState("IconifyComponentQSPBD.overlay", false, "IconifyComponentQSPBA.overlay", false, "IconifyComponentQSST.overlay", true);
                } else {
                    OverlayUtil.disableOverlay("IconifyComponentQSST.overlay");
                }
            }, SWITCH_ANIMATION_DELAY);
        };
    }

    CompoundButton.OnCheckedChangeListener monetAccentListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                binding.monetGradient.setSwitchChangeListener(null);
                binding.monetGradient.setSwitchChecked(false);
                binding.monetGradient.setSwitchChangeListener(monetGradientListener);
            }

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (isChecked) {
                    disableMonetGradient();
                    enableMonetAccent();
                } else {
                    disableMonetAccent();
                    applyDefaultColors();
                }
            }, SWITCH_ANIMATION_DELAY);
        }
    };


    CompoundButton.OnCheckedChangeListener monetGradientListener = (buttonView, isChecked) -> {
        if (isChecked) {
            binding.monetAccent.setSwitchChangeListener(null);
            binding.monetAccent.setSwitchChecked(false);
            binding.monetAccent.setSwitchChangeListener(monetAccentListener);
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isChecked) {
                disableMonetAccent();
                enableMonetGradient();
            } else {
                disableMonetGradient();
                applyDefaultColors();
            }
        }, SWITCH_ANIMATION_DELAY);
    };


    CompoundButton.OnCheckedChangeListener pitchBlackDarkListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                if (minimalQsListener == null) {
                    initializeMinimalQsListener();
                }

                binding.minimalQspanel.setSwitchChangeListener(null);
                binding.minimalQspanel.setSwitchChecked(false);
                binding.minimalQspanel.setSwitchChangeListener(minimalQsListener);

                binding.pitchBlackAmoledTheme.setSwitchChangeListener(null);
                binding.pitchBlackAmoledTheme.setSwitchChecked(false);
                binding.pitchBlackAmoledTheme.setSwitchChangeListener(pitchBlackAmoledListener);
            }

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (isChecked) {
                    OverlayUtil.changeOverlayState("IconifyComponentQSST.overlay", false, "IconifyComponentQSPBA.overlay", false, "IconifyComponentQSPBD.overlay", true);
                } else {
                    OverlayUtil.disableOverlay("IconifyComponentQSPBD.overlay");
                }
            }, SWITCH_ANIMATION_DELAY);
        }
    };

    CompoundButton.OnCheckedChangeListener pitchBlackAmoledListener = (buttonView, isChecked) -> {
        if (isChecked) {
            if (minimalQsListener == null) {
                initializeMinimalQsListener();
            }

            binding.minimalQspanel.setSwitchChangeListener(null);
            binding.minimalQspanel.setSwitchChecked(false);
            binding.minimalQspanel.setSwitchChangeListener(minimalQsListener);

            binding.pitchBlackDarkTheme.setSwitchChangeListener(null);
            binding.pitchBlackDarkTheme.setSwitchChecked(false);
            binding.pitchBlackDarkTheme.setSwitchChangeListener(pitchBlackDarkListener);
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isChecked) {
                OverlayUtil.changeOverlayState("IconifyComponentQSST.overlay", false, "IconifyComponentQSPBD.overlay", false, "IconifyComponentQSPBA.overlay", true);
            } else {
                OverlayUtil.disableOverlay("IconifyComponentQSPBA.overlay");
            }
        }, SWITCH_ANIMATION_DELAY);
    };
}