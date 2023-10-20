package com.drdisagree.iconify.ui.fragments;

import static com.drdisagree.iconify.common.Const.FRAMEWORK_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.COLOR_ACCENT_PRIMARY;
import static com.drdisagree.iconify.common.Preferences.COLOR_ACCENT_PRIMARY_LIGHT;
import static com.drdisagree.iconify.common.Preferences.COLOR_ACCENT_SECONDARY;
import static com.drdisagree.iconify.common.Preferences.COLOR_ACCENT_SECONDARY_LIGHT;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_ACCENT;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_PRIMARY_COLOR_SWITCH;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_SECONDARY_COLOR_SWITCH;
import static com.drdisagree.iconify.common.Preferences.STR_NULL;
import static com.drdisagree.iconify.common.References.ICONIFY_COLOR_ACCENT_PRIMARY;
import static com.drdisagree.iconify.common.References.ICONIFY_COLOR_ACCENT_SECONDARY;
import static com.drdisagree.iconify.utils.color.ColorUtil.colorToSpecialHex;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.databinding.FragmentBasicColorsBinding;
import com.drdisagree.iconify.ui.activities.HomePage;
import com.drdisagree.iconify.ui.base.BaseFragment;
import com.drdisagree.iconify.ui.events.ColorSelectedEvent;
import com.drdisagree.iconify.ui.utils.ViewHelper;
import com.drdisagree.iconify.utils.overlay.FabricatedUtil;
import com.drdisagree.iconify.utils.overlay.OverlayUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Objects;

public class BasicColors extends BaseFragment {

    private static boolean isSelectedPrimary = false, isSelectedSecondary = false;
    private static String accentPrimary, accentSecondary;
    private FragmentBasicColorsBinding binding;

    public static void applyPrimaryColors() {
        FabricatedUtil.buildAndEnableOverlays(
                new Object[]{FRAMEWORK_PACKAGE, COLOR_ACCENT_PRIMARY, "color", "holo_blue_light", colorToSpecialHex(Integer.parseInt(Prefs.getString(COLOR_ACCENT_PRIMARY)))},
                new Object[]{FRAMEWORK_PACKAGE, COLOR_ACCENT_PRIMARY_LIGHT, "color", "holo_green_light", colorToSpecialHex(Integer.parseInt(Prefs.getString(COLOR_ACCENT_PRIMARY)))}
        );
    }

    public static void applySecondaryColors() {
        FabricatedUtil.buildAndEnableOverlays(
                new Object[]{FRAMEWORK_PACKAGE, COLOR_ACCENT_SECONDARY, "color", "holo_blue_dark", colorToSpecialHex(Integer.parseInt(Prefs.getString(COLOR_ACCENT_SECONDARY)))},
                new Object[]{FRAMEWORK_PACKAGE, COLOR_ACCENT_SECONDARY_LIGHT, "color", "holo_green_dark", colorToSpecialHex(Integer.parseInt(Prefs.getString(COLOR_ACCENT_SECONDARY)))}
        );
    }

    public static void disableAccentColors() {
        Prefs.putBoolean(CUSTOM_ACCENT, false);
        Prefs.clearPrefs(CUSTOM_PRIMARY_COLOR_SWITCH, CUSTOM_SECONDARY_COLOR_SWITCH, COLOR_ACCENT_PRIMARY, COLOR_ACCENT_PRIMARY_LIGHT, COLOR_ACCENT_SECONDARY, COLOR_ACCENT_SECONDARY_LIGHT);

        FabricatedUtil.disableOverlays(COLOR_ACCENT_PRIMARY, COLOR_ACCENT_PRIMARY_LIGHT, COLOR_ACCENT_SECONDARY, COLOR_ACCENT_SECONDARY_LIGHT);
    }

    public static void applyDefaultColors() {
        applyDefaultPrimaryColors();
        applyDefaultSecondaryColors();
    }

    public static void applyDefaultPrimaryColors() {
        FabricatedUtil.buildAndEnableOverlays(
                new Object[]{FRAMEWORK_PACKAGE, COLOR_ACCENT_PRIMARY, "color", "holo_blue_light", ICONIFY_COLOR_ACCENT_PRIMARY},
                new Object[]{FRAMEWORK_PACKAGE, COLOR_ACCENT_PRIMARY_LIGHT, "color", "holo_green_light", ICONIFY_COLOR_ACCENT_PRIMARY}
        );
    }

    public static void applyDefaultSecondaryColors() {
        FabricatedUtil.buildAndEnableOverlays(
                new Object[]{FRAMEWORK_PACKAGE, COLOR_ACCENT_SECONDARY, "color", "holo_blue_dark", ICONIFY_COLOR_ACCENT_SECONDARY},
                new Object[]{FRAMEWORK_PACKAGE, COLOR_ACCENT_SECONDARY_LIGHT, "color", "holo_green_dark", ICONIFY_COLOR_ACCENT_SECONDARY}
        );
    }

    private static boolean shouldUseDefaultColors() {
        return OverlayUtil.isOverlayDisabled("IconifyComponentAMAC.overlay") && OverlayUtil.isOverlayDisabled("IconifyComponentAMGC.overlay") && OverlayUtil.isOverlayDisabled("IconifyComponentME.overlay");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBasicColorsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Header
        ViewHelper.setHeader(requireContext(), getParentFragmentManager(), binding.header.toolbar, R.string.activity_title_basic_colors);

        if (!Objects.equals(Prefs.getString(COLOR_ACCENT_PRIMARY), STR_NULL))
            accentPrimary = Prefs.getString(COLOR_ACCENT_PRIMARY);
        else
            accentPrimary = String.valueOf(getResources().getColor(android.R.color.holo_blue_light, requireActivity().getTheme()));

        if (!Objects.equals(Prefs.getString(COLOR_ACCENT_SECONDARY), STR_NULL))
            accentSecondary = Prefs.getString(COLOR_ACCENT_SECONDARY);
        else
            accentSecondary = String.valueOf(getResources().getColor(android.R.color.holo_blue_dark, requireActivity().getTheme()));

        updatePrimaryColor();
        updateSecondaryColor();

        // Primary and Secondary color
        binding.previewColoraccentprimary.setOnClickListener(v -> ((HomePage) requireActivity()).showColorPickerDialog(1, Integer.parseInt(accentPrimary), true, false, true));
        binding.previewColoraccentsecondary.setOnClickListener(v -> ((HomePage) requireActivity()).showColorPickerDialog(2, Integer.parseInt(accentSecondary), true, false, true));

        // Enable custom colors
        binding.enableCustomColor.setOnClickListener(v -> {
            binding.enableCustomColor.setVisibility(View.GONE);
            refreshVisibility();

            Runnable runnable = () -> {
                applyMonetColors();

                if (getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        Prefs.putBoolean(CUSTOM_ACCENT, true);

                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            if (getContext() != null) {
                                Toast.makeText(requireContext(), Iconify.getAppContextLocale().getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                            }
                        }, 2000);
                    });
                }
            };
            Thread thread = new Thread(runnable);
            thread.start();
        });

        // Disable custom colors
        binding.disableCustomColor.setVisibility(Prefs.getBoolean(CUSTOM_ACCENT, false) ? View.VISIBLE : View.GONE);

        binding.disableCustomColor.setOnClickListener(v -> {
            binding.disableCustomColor.setVisibility(View.GONE);
            refreshVisibility();

            Runnable runnable = () -> {
                disableAccentColors();
                if (shouldUseDefaultColors()) {
                    applyDefaultColors();
                }

                if (getActivity() != null) {
                    requireActivity().runOnUiThread(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (getContext() != null) {
                            Toast.makeText(requireContext(), Iconify.getAppContextLocale().getResources().getString(R.string.toast_disabled), Toast.LENGTH_SHORT).show();
                        }
                    }, 2000));
                }
            };
            Thread thread = new Thread(runnable);
            thread.start();
        });

        refreshVisibility();

        return view;
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onColorSelected(ColorSelectedEvent event) {
        switch (event.dialogId()) {
            case 1 -> {
                isSelectedPrimary = true;
                accentPrimary = String.valueOf(event.selectedColor());
                updatePrimaryColor();
                binding.enableCustomColor.setVisibility(View.VISIBLE);
                Prefs.putBoolean(CUSTOM_PRIMARY_COLOR_SWITCH, true);
            }
            case 2 -> {
                isSelectedSecondary = true;
                accentSecondary = String.valueOf(event.selectedColor());
                updateSecondaryColor();
                binding.enableCustomColor.setVisibility(View.VISIBLE);
                Prefs.putBoolean(CUSTOM_SECONDARY_COLOR_SWITCH, true);
            }
        }

        refreshVisibility();
    }

    private void refreshVisibility() {
        if (binding.enableCustomColor.getVisibility() == View.VISIBLE || binding.disableCustomColor.getVisibility() == View.VISIBLE) {
            binding.buttonContainer.setVisibility(View.VISIBLE);
        } else {
            binding.buttonContainer.setVisibility(View.GONE);
        }
    }

    private void updatePrimaryColor() {
        GradientDrawable gd;
        gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{Integer.parseInt(accentPrimary), Integer.parseInt(accentPrimary)});
        gd.setCornerRadius(getResources().getDimension(R.dimen.preview_color_picker_radius) * Iconify.getAppContextLocale().getResources().getDisplayMetrics().density);
        binding.previewColorPickerPrimary.setBackground(gd);

        gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{Integer.parseInt(accentPrimary), Integer.parseInt(accentSecondary)});
        gd.setCornerRadius(24 * Iconify.getAppContextLocale().getResources().getDisplayMetrics().density);
        binding.colorPreviewLarge.setBackground(gd);
    }

    private void updateSecondaryColor() {
        GradientDrawable gd;
        gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{Integer.parseInt(accentSecondary), Integer.parseInt(accentSecondary)});
        gd.setCornerRadius(getResources().getDimension(R.dimen.preview_color_picker_radius) * Iconify.getAppContextLocale().getResources().getDisplayMetrics().density);
        binding.previewColorPickerSecondary.setBackground(gd);

        gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{Integer.parseInt(accentPrimary), Integer.parseInt(accentSecondary)});
        gd.setCornerRadius(24 * Iconify.getAppContextLocale().getResources().getDisplayMetrics().density);
        binding.colorPreviewLarge.setBackground(gd);
    }

    private void applyMonetColors() {
        Prefs.putBoolean(CUSTOM_ACCENT, true);

        if (isSelectedPrimary) {
            Prefs.putString(COLOR_ACCENT_PRIMARY, accentPrimary);
            Prefs.putString(COLOR_ACCENT_PRIMARY_LIGHT, accentPrimary);
        }

        if (isSelectedSecondary) {
            Prefs.putString(COLOR_ACCENT_SECONDARY, accentSecondary);
            Prefs.putString(COLOR_ACCENT_SECONDARY_LIGHT, accentSecondary);
        }

        if (isSelectedPrimary) applyPrimaryColors();

        if (isSelectedSecondary) applySecondaryColors();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}