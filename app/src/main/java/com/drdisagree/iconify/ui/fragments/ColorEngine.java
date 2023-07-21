package com.drdisagree.iconify.ui.fragments;

import static com.drdisagree.iconify.common.Const.FRAGMENT_BACK_BUTTON_DELAY;
import static com.drdisagree.iconify.common.Const.SWITCH_ANIMATION_DELAY;
import static com.drdisagree.iconify.common.Preferences.COLOR_ACCENT_PRIMARY;
import static com.drdisagree.iconify.common.Preferences.COLOR_ACCENT_SECONDARY;
import static com.drdisagree.iconify.common.Preferences.STR_NULL;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.databinding.FragmentColorEngineBinding;
import com.drdisagree.iconify.ui.activities.BasicColors;
import com.drdisagree.iconify.ui.activities.MonetEngine;
import com.drdisagree.iconify.utils.OverlayUtil;

import java.util.Objects;

public class ColorEngine extends BaseFragment {

    private FragmentColorEngineBinding binding;
    private CompoundButton.OnCheckedChangeListener minimalQsListener = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentColorEngineBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Header
        binding.header.collapsingToolbar.setTitle(getResources().getString(R.string.activity_title_color_engine));
        ((AppCompatActivity) requireActivity()).setSupportActionBar(binding.header.toolbar);
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setDisplayShowHomeEnabled(true);
        binding.header.toolbar.setNavigationOnClickListener(view1 -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
            getParentFragmentManager().popBackStack();
        }, FRAGMENT_BACK_BUTTON_DELAY));

        // Basic colors
        binding.basicColors.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), BasicColors.class);
            startActivity(intent);
        });

        // Monet engine
        binding.monetEngine.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), MonetEngine.class);
            startActivity(intent);
        });

        // Apply monet accent and gradient
        binding.applyMonetAccent.setChecked(Prefs.getBoolean("IconifyComponentAMAC.overlay"));
        binding.applyMonetAccent.setOnCheckedChangeListener(monetAccentListener);

        binding.applyMonetGradient.setChecked(Prefs.getBoolean("IconifyComponentAMGC.overlay"));
        binding.applyMonetGradient.setOnCheckedChangeListener(monetGradientListener);

        // Minimal QsPanel
        binding.applyMinimalQspanel.setChecked(Prefs.getBoolean("IconifyComponentQSST.overlay"));
        if (minimalQsListener == null) {
            initializeMinimalQsListener();
        }
        binding.applyMinimalQspanel.setOnCheckedChangeListener(minimalQsListener);

        // Pitch Black Dark
        binding.applyPitchBlackDarkTheme.setChecked(Prefs.getBoolean("IconifyComponentQSPBD.overlay"));
        binding.applyPitchBlackDarkTheme.setOnCheckedChangeListener(pitchBlackDarkListener);

        // Pitch Black Amoled
        binding.applyPitchBlackAmoledTheme.setChecked(Prefs.getBoolean("IconifyComponentQSPBA.overlay"));
        binding.applyPitchBlackAmoledTheme.setOnCheckedChangeListener(pitchBlackAmoledListener);

        // Disable Monet
        binding.disableMonet.setChecked(Prefs.getBoolean("IconifyComponentDM.overlay"));

        binding.disableMonet.setOnCheckedChangeListener((buttonView, isChecked) -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
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
        return OverlayUtil.isOverlayDisabled(OverlayUtil.getEnabledOverlayList(), "IconifyComponentME.overlay");
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