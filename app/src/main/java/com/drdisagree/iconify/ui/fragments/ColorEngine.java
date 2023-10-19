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
        binding.applyMonetAccent.setChecked(Prefs.getBoolean("IconifyComponentAMAC.overlay"));
        binding.applyMonetAccent.setOnCheckedChangeListener(monetAccentListener);

        binding.applyMonetGradient.setChecked(Prefs.getBoolean("IconifyComponentAMGC.overlay"));
        binding.applyMonetGradient.setOnCheckedChangeListener(monetGradientListener);

        binding.applyMonetAccentContainer.setOnClickListener(v -> binding.applyMonetAccent.toggle());
        binding.applyMonetGradientContainer.setOnClickListener(v -> binding.applyMonetGradient.toggle());

        // Pitch Black Dark
        binding.applyPitchBlackDarkTheme.setChecked(Prefs.getBoolean("IconifyComponentQSPBD.overlay"));
        binding.applyPitchBlackDarkTheme.setOnCheckedChangeListener(pitchBlackDarkListener);
        binding.applyPitchBlackDarkThemeContainer.setOnClickListener(v -> binding.applyPitchBlackDarkTheme.toggle());

        // Pitch Black Amoled
        binding.applyPitchBlackAmoledTheme.setChecked(Prefs.getBoolean("IconifyComponentQSPBA.overlay"));
        binding.applyPitchBlackAmoledTheme.setOnCheckedChangeListener(pitchBlackAmoledListener);
        binding.applyPitchBlackAmoledThemeContainer.setOnClickListener(v -> binding.applyPitchBlackAmoledTheme.toggle());

        // Minimal QsPanel
        binding.applyMinimalQspanel.setChecked(Prefs.getBoolean("IconifyComponentQSST.overlay"));
        if (minimalQsListener == null) {
            initializeMinimalQsListener();
        }
        binding.applyMinimalQspanel.setOnCheckedChangeListener(minimalQsListener);
        binding.applyMinimalQspanelContainer.setOnClickListener(v -> binding.applyMinimalQspanel.toggle());

        // Disable Monet
        binding.disableMonet.setChecked(Prefs.getBoolean("IconifyComponentDM.overlay"));
        binding.disableMonet.setOnCheckedChangeListener((buttonView, isChecked) -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isChecked) {
                OverlayUtil.enableOverlay("IconifyComponentDM.overlay");
            } else {
                OverlayUtil.disableOverlay("IconifyComponentDM.overlay");
            }
        }, SWITCH_ANIMATION_DELAY));
        binding.disableMonetContainer.setOnClickListener(v -> binding.disableMonet.toggle());

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
                binding.applyPitchBlackDarkTheme.setOnCheckedChangeListener(null);
                binding.applyPitchBlackDarkTheme.setChecked(false);
                binding.applyPitchBlackDarkTheme.setOnCheckedChangeListener(pitchBlackDarkListener);

                binding.applyPitchBlackAmoledTheme.setOnCheckedChangeListener(null);
                binding.applyPitchBlackAmoledTheme.setChecked(false);
                binding.applyPitchBlackAmoledTheme.setOnCheckedChangeListener(pitchBlackAmoledListener);
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
                binding.applyMonetGradient.setOnCheckedChangeListener(null);
                binding.applyMonetGradient.setChecked(false);
                binding.applyMonetGradient.setOnCheckedChangeListener(monetGradientListener);
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
            binding.applyMonetAccent.setOnCheckedChangeListener(null);
            binding.applyMonetAccent.setChecked(false);
            binding.applyMonetAccent.setOnCheckedChangeListener(monetAccentListener);
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

                binding.applyMinimalQspanel.setOnCheckedChangeListener(null);
                binding.applyMinimalQspanel.setChecked(false);
                binding.applyMinimalQspanel.setOnCheckedChangeListener(minimalQsListener);

                binding.applyPitchBlackAmoledTheme.setOnCheckedChangeListener(null);
                binding.applyPitchBlackAmoledTheme.setChecked(false);
                binding.applyPitchBlackAmoledTheme.setOnCheckedChangeListener(pitchBlackAmoledListener);
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

            binding.applyMinimalQspanel.setOnCheckedChangeListener(null);
            binding.applyMinimalQspanel.setChecked(false);
            binding.applyMinimalQspanel.setOnCheckedChangeListener(minimalQsListener);

            binding.applyPitchBlackDarkTheme.setOnCheckedChangeListener(null);
            binding.applyPitchBlackDarkTheme.setChecked(false);
            binding.applyPitchBlackDarkTheme.setOnCheckedChangeListener(pitchBlackDarkListener);
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