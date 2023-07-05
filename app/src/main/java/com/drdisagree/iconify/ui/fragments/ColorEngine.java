package com.drdisagree.iconify.ui.fragments;

import static com.drdisagree.iconify.common.Const.FRAGMENT_BACK_BUTTON_DELAY;
import static com.drdisagree.iconify.common.Const.FRAMEWORK_PACKAGE;
import static com.drdisagree.iconify.common.Const.SWITCH_ANIMATION_DELAY;
import static com.drdisagree.iconify.common.Preferences.COLOR_ACCENT_PRIMARY;
import static com.drdisagree.iconify.common.Preferences.COLOR_ACCENT_SECONDARY;
import static com.drdisagree.iconify.common.Preferences.STR_NULL;
import static com.drdisagree.iconify.common.Preferences.USE_LIGHT_ACCENT;
import static com.drdisagree.iconify.common.References.ICONIFY_COLOR_ACCENT_PRIMARY;
import static com.drdisagree.iconify.common.References.ICONIFY_COLOR_ACCENT_SECONDARY;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.ui.activities.BasicColors;
import com.drdisagree.iconify.ui.activities.MonetEngine;
import com.drdisagree.iconify.utils.FabricatedUtil;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.List;
import java.util.Objects;

public class ColorEngine extends BaseFragment {

    public static List<String> EnabledOverlays = OverlayUtil.getEnabledOverlayList();
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_color_engine, container, false);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = view.findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_color_engine));
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(view1 -> new Handler().postDelayed(() -> {
            getParentFragmentManager().popBackStack();
        }, FRAGMENT_BACK_BUTTON_DELAY));

        // Basic colors
        LinearLayout basic_colors = view.findViewById(R.id.basic_colors);
        basic_colors.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), BasicColors.class);
            startActivity(intent);
        });

        // Monet engine
        LinearLayout monet_engine = view.findViewById(R.id.monet_engine);
        monet_engine.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), MonetEngine.class);
            startActivity(intent);
        });

        // Apply monet accent and gradient
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch apply_monet_accent = view.findViewById(R.id.apply_monet_accent);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch apply_monet_gradient = view.findViewById(R.id.apply_monet_gradient);

        apply_monet_accent.setChecked(Prefs.getBoolean("IconifyComponentAMAC.overlay") || Prefs.getBoolean("IconifyComponentAMACL.overlay"));
        apply_monet_accent.setOnCheckedChangeListener(monetAccentListener);

        apply_monet_gradient.setChecked(Prefs.getBoolean("IconifyComponentAMGC.overlay") || Prefs.getBoolean("IconifyComponentAMGCL.overlay"));
        apply_monet_gradient.setOnCheckedChangeListener(monetGradientListener);

        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch apply_minimal_qspanel = view.findViewById(R.id.apply_minimal_qspanel);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch apply_pitch_black_dark_theme = view.findViewById(R.id.apply_pitch_black_dark_theme);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch apply_pitch_black_amoled_theme = view.findViewById(R.id.apply_pitch_black_amoled_theme);

        // Minimal QsPanel
        apply_minimal_qspanel.setChecked(Prefs.getBoolean("IconifyComponentQSST.overlay"));
        if (minimalQsListener == null) {
            initializeMinimalQsListener();
        }
        apply_minimal_qspanel.setOnCheckedChangeListener(minimalQsListener);

        // Pitch Black Dark
        apply_pitch_black_dark_theme.setChecked(Prefs.getBoolean("IconifyComponentQSPBD.overlay"));
        apply_pitch_black_dark_theme.setOnCheckedChangeListener(pitchBlackDarkListener);

        // Pitch Black Amoled
        apply_pitch_black_amoled_theme.setChecked(Prefs.getBoolean("IconifyComponentQSPBA.overlay"));
        apply_pitch_black_amoled_theme.setOnCheckedChangeListener(pitchBlackAmoledListener);

        // Disable Monet
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch disable_monet = view.findViewById(R.id.disable_monet);
        disable_monet.setChecked(Prefs.getBoolean("IconifyComponentDM.overlay"));

        disable_monet.setOnCheckedChangeListener((buttonView, isChecked) -> new Handler().postDelayed(() -> {
            if (isChecked) {
                OverlayUtil.enableOverlay("IconifyComponentDM.overlay");
            } else {
                OverlayUtil.disableOverlay("IconifyComponentDM.overlay");
            }
        }, SWITCH_ANIMATION_DELAY));

        return view;
    }

    private void enableMonetAccent() {
        if (Prefs.getBoolean(USE_LIGHT_ACCENT, false)) {
            OverlayUtil.changeOverlayState("IconifyComponentAMAC.overlay", false, "IconifyComponentAMACL.overlay", true);
        } else {
            OverlayUtil.changeOverlayState("IconifyComponentAMACL.overlay", false, "IconifyComponentAMAC.overlay", true);
        }

        if (!Objects.equals(Prefs.getString(COLOR_ACCENT_PRIMARY), STR_NULL)) {
            BasicColors.applyPrimaryColors();
        } else {
            FabricatedUtil.disableOverlay(COLOR_ACCENT_PRIMARY);
        }

        if (!Objects.equals(Prefs.getString(COLOR_ACCENT_SECONDARY), STR_NULL)) {
            BasicColors.applySecondaryColors();
        } else {
            FabricatedUtil.disableOverlay(COLOR_ACCENT_SECONDARY);
        }
    }

    private void disableMonetAccent() {
        if (!((Switch) view.findViewById(R.id.apply_monet_gradient)).isChecked() && OverlayUtil.isOverlayDisabled(EnabledOverlays, "IconifyComponentME.overlay")) {
            if (Prefs.getString(COLOR_ACCENT_PRIMARY).equals(STR_NULL)) {
                FabricatedUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, COLOR_ACCENT_PRIMARY, "color", "holo_blue_light", ICONIFY_COLOR_ACCENT_PRIMARY);
            }

            if (Prefs.getString(COLOR_ACCENT_SECONDARY).equals(STR_NULL)) {
                FabricatedUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, COLOR_ACCENT_SECONDARY, "color", "holo_green_light", ICONIFY_COLOR_ACCENT_SECONDARY);
            }
        }

        OverlayUtil.changeOverlayState("IconifyComponentAMAC.overlay", false, "IconifyComponentAMACL.overlay", false);
    }

    private void enableMonetGradient() {
        if (Prefs.getBoolean(USE_LIGHT_ACCENT, false)) {
            OverlayUtil.changeOverlayState("IconifyComponentAMGC.overlay", false, "IconifyComponentAMGCL.overlay", true);
        } else {
            OverlayUtil.changeOverlayState("IconifyComponentAMGCL.overlay", false, "IconifyComponentAMGC.overlay", true);
        }

        if (!Objects.equals(Prefs.getString(COLOR_ACCENT_PRIMARY), STR_NULL)) {
            BasicColors.applyPrimaryColors();
        } else {
            FabricatedUtil.disableOverlay(COLOR_ACCENT_PRIMARY);
        }

        if (!Objects.equals(Prefs.getString(COLOR_ACCENT_SECONDARY), STR_NULL)) {
            BasicColors.applySecondaryColors();
        } else {
            FabricatedUtil.disableOverlay(COLOR_ACCENT_SECONDARY);
        }
    }

    private void disableMonetGradient() {
        if (!((Switch) view.findViewById(R.id.apply_monet_accent)).isChecked() && OverlayUtil.isOverlayDisabled(EnabledOverlays, "IconifyComponentME.overlay")) {
            if (Prefs.getString(COLOR_ACCENT_PRIMARY).equals(STR_NULL)) {
                FabricatedUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, COLOR_ACCENT_PRIMARY, "color", "holo_blue_light", ICONIFY_COLOR_ACCENT_PRIMARY);
            }

            if (Prefs.getString(COLOR_ACCENT_SECONDARY).equals(STR_NULL)) {
                FabricatedUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, COLOR_ACCENT_SECONDARY, "color", "holo_green_light", ICONIFY_COLOR_ACCENT_SECONDARY);
            }
        }

        OverlayUtil.changeOverlayState("IconifyComponentAMGC.overlay", false, "IconifyComponentAMGCL.overlay", false);
    }

    CompoundButton.OnCheckedChangeListener monetAccentListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                ((Switch) view.findViewById(R.id.apply_monet_gradient)).setOnCheckedChangeListener(null);
                ((Switch) view.findViewById(R.id.apply_monet_gradient)).setChecked(false);
                ((Switch) view.findViewById(R.id.apply_monet_gradient)).setOnCheckedChangeListener(monetGradientListener);
            }

            new Handler().postDelayed(() -> {
                if (isChecked) {
                    disableMonetGradient();
                    enableMonetAccent();
                } else {
                    disableMonetAccent();
                }
            }, SWITCH_ANIMATION_DELAY);
        }
    };

    CompoundButton.OnCheckedChangeListener monetGradientListener = (buttonView, isChecked) -> {
        if (isChecked) {
            ((Switch) view.findViewById(R.id.apply_monet_accent)).setOnCheckedChangeListener(null);
            ((Switch) view.findViewById(R.id.apply_monet_accent)).setChecked(false);
            ((Switch) view.findViewById(R.id.apply_monet_accent)).setOnCheckedChangeListener(monetAccentListener);
        }

        new Handler().postDelayed(() -> {
            if (isChecked) {
                disableMonetAccent();
                enableMonetGradient();
            } else {
                disableMonetGradient();
            }
        }, SWITCH_ANIMATION_DELAY);
    };

    CompoundButton.OnCheckedChangeListener minimalQsListener = null;

    CompoundButton.OnCheckedChangeListener pitchBlackDarkListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                if (minimalQsListener == null) {
                    initializeMinimalQsListener();
                }

                ((Switch) view.findViewById(R.id.apply_minimal_qspanel)).setOnCheckedChangeListener(null);
                ((Switch) view.findViewById(R.id.apply_minimal_qspanel)).setChecked(false);
                ((Switch) view.findViewById(R.id.apply_minimal_qspanel)).setOnCheckedChangeListener(minimalQsListener);

                ((Switch) view.findViewById(R.id.apply_pitch_black_amoled_theme)).setOnCheckedChangeListener(null);
                ((Switch) view.findViewById(R.id.apply_pitch_black_amoled_theme)).setChecked(false);
                ((Switch) view.findViewById(R.id.apply_pitch_black_amoled_theme)).setOnCheckedChangeListener(pitchBlackAmoledListener);
            }

            new Handler().postDelayed(() -> {
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

            ((Switch) view.findViewById(R.id.apply_minimal_qspanel)).setOnCheckedChangeListener(null);
            ((Switch) view.findViewById(R.id.apply_minimal_qspanel)).setChecked(false);
            ((Switch) view.findViewById(R.id.apply_minimal_qspanel)).setOnCheckedChangeListener(minimalQsListener);

            ((Switch) view.findViewById(R.id.apply_pitch_black_dark_theme)).setOnCheckedChangeListener(null);
            ((Switch) view.findViewById(R.id.apply_pitch_black_dark_theme)).setChecked(false);
            ((Switch) view.findViewById(R.id.apply_pitch_black_dark_theme)).setOnCheckedChangeListener(pitchBlackDarkListener);
        }

        new Handler().postDelayed(() -> {
            if (isChecked) {
                OverlayUtil.changeOverlayState("IconifyComponentQSST.overlay", false, "IconifyComponentQSPBD.overlay", false, "IconifyComponentQSPBA.overlay", true);
            } else {
                OverlayUtil.disableOverlay("IconifyComponentQSPBA.overlay");
            }
        }, SWITCH_ANIMATION_DELAY);
    };

    private void initializeMinimalQsListener() {
        minimalQsListener = (buttonView, isChecked) -> {
            if (isChecked) {
                ((Switch) view.findViewById(R.id.apply_pitch_black_dark_theme)).setOnCheckedChangeListener(null);
                ((Switch) view.findViewById(R.id.apply_pitch_black_dark_theme)).setChecked(false);
                ((Switch) view.findViewById(R.id.apply_pitch_black_dark_theme)).setOnCheckedChangeListener(pitchBlackDarkListener);

                ((Switch) view.findViewById(R.id.apply_pitch_black_amoled_theme)).setOnCheckedChangeListener(null);
                ((Switch) view.findViewById(R.id.apply_pitch_black_amoled_theme)).setChecked(false);
                ((Switch) view.findViewById(R.id.apply_pitch_black_amoled_theme)).setOnCheckedChangeListener(pitchBlackAmoledListener);
            }

            new Handler().postDelayed(() -> {
                if (isChecked) {
                    OverlayUtil.changeOverlayState("IconifyComponentQSPBD.overlay", false, "IconifyComponentQSPBA.overlay", false, "IconifyComponentQSST.overlay", true);
                } else {
                    OverlayUtil.disableOverlay("IconifyComponentQSST.overlay");
                }
            }, SWITCH_ANIMATION_DELAY);
        };
    }
}