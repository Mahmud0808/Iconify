package com.drdisagree.iconify.ui.fragments;

import static com.drdisagree.iconify.common.Const.FRAMEWORK_PACKAGE;
import static com.drdisagree.iconify.common.Const.SWITCH_ANIMATION_DELAY;
import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;
import static com.drdisagree.iconify.common.References.FABRICATED_SB_COLOR_SOURCE;
import static com.drdisagree.iconify.common.References.FABRICATED_SB_COLOR_TINT;
import static com.drdisagree.iconify.common.References.FABRICATED_SB_HEIGHT;
import static com.drdisagree.iconify.common.References.FABRICATED_SB_LEFT_PADDING;
import static com.drdisagree.iconify.common.References.FABRICATED_SB_RIGHT_PADDING;
import static com.drdisagree.iconify.utils.color.ColorUtil.colorToSpecialHex;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.databinding.FragmentStatusbarBinding;
import com.drdisagree.iconify.ui.activities.MainActivity;
import com.drdisagree.iconify.ui.base.BaseFragment;
import com.drdisagree.iconify.ui.events.ColorDismissedEvent;
import com.drdisagree.iconify.ui.events.ColorSelectedEvent;
import com.drdisagree.iconify.ui.utils.ViewHelper;
import com.drdisagree.iconify.utils.SystemUtil;
import com.drdisagree.iconify.utils.overlay.FabricatedUtil;
import com.drdisagree.iconify.utils.overlay.OverlayUtil;
import com.drdisagree.iconify.utils.overlay.manager.resource.ResourceEntry;
import com.drdisagree.iconify.utils.overlay.manager.resource.ResourceManager;
import com.google.android.material.slider.Slider;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Objects;

@SuppressLint("SetTextI18n")
public class Statusbar extends BaseFragment {

    private static String colorSBTint, selectedStyle;
    private final int[] finalSBLeftPadding = {Prefs.getInt(FABRICATED_SB_LEFT_PADDING, 8)};
    private final int[] finalSBRightPadding = {Prefs.getInt(FABRICATED_SB_RIGHT_PADDING, 8)};
    private final int[] finalSBHeight = {Prefs.getInt(FABRICATED_SB_HEIGHT, 28)};
    private final Slider.OnSliderTouchListener sbLeftPaddingListener = new Slider.OnSliderTouchListener() {
        @Override
        public void onStartTrackingTouch(@NonNull Slider slider) {
        }

        @Override
        public void onStopTrackingTouch(@NonNull Slider slider) {
            finalSBLeftPadding[0] = (int) slider.getValue();
            Prefs.putInt(FABRICATED_SB_LEFT_PADDING, finalSBLeftPadding[0]);
            ResourceManager.buildOverlayWithResource(
                    requireContext(),
                    new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "status_bar_padding_start", finalSBLeftPadding[0] + "dp")
            );
        }
    };
    private final Slider.OnSliderTouchListener sbRightPaddingListener = new Slider.OnSliderTouchListener() {
        @Override
        public void onStartTrackingTouch(@NonNull Slider slider) {
        }

        @Override
        public void onStopTrackingTouch(@NonNull Slider slider) {
            finalSBRightPadding[0] = (int) slider.getValue();
            Prefs.putInt(FABRICATED_SB_RIGHT_PADDING, finalSBRightPadding[0]);
            ResourceManager.buildOverlayWithResource(
                    requireContext(),
                    new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "status_bar_padding_end", finalSBRightPadding[0] + "dp")
            );
        }
    };
    private final Slider.OnSliderTouchListener sbHeightListener = new Slider.OnSliderTouchListener() {
        @Override
        public void onStartTrackingTouch(@NonNull Slider slider) {
        }

        @Override
        public void onStopTrackingTouch(@NonNull Slider slider) {
            finalSBHeight[0] = (int) slider.getValue();
            Prefs.putInt(FABRICATED_SB_HEIGHT, finalSBHeight[0]);
            ResourceManager.buildOverlayWithResource(
                    requireContext(),
                    new ResourceEntry(FRAMEWORK_PACKAGE, "dimen", "status_bar_height", finalSBHeight[0] + "dp"),
                    new ResourceEntry(FRAMEWORK_PACKAGE, "dimen", "status_bar_height_default", finalSBHeight[0] + "dp"),
                    new ResourceEntry(FRAMEWORK_PACKAGE, "dimen", "status_bar_height_portrait", finalSBHeight[0] + "dp"),
                    new ResourceEntry(FRAMEWORK_PACKAGE, "dimen", "status_bar_height_landscape", finalSBHeight[0] + "dp")
            );
        }
    };
    private FragmentStatusbarBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStatusbarBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Header
        ViewHelper.setHeader(requireContext(), getParentFragmentManager(), binding.header.toolbar, R.string.activity_title_statusbar);

        // Statusbar left padding
        binding.sbLeftPadding.setSliderValue(finalSBLeftPadding[0]);
        binding.sbLeftPadding.setOnSliderTouchListener(sbLeftPaddingListener);

        // Reset left padding
        binding.sbLeftPadding.setResetClickListener(v -> {
            Prefs.putInt(FABRICATED_SB_LEFT_PADDING, 8);
            ResourceManager.removeResourceFromOverlay(
                    requireContext(),
                    new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "status_bar_padding_start")
            );
            return true;
        });

        // Statusbar right padding
        binding.sbRightPadding.setSliderValue(finalSBRightPadding[0]);
        binding.sbRightPadding.setOnSliderTouchListener(sbRightPaddingListener);

        // Reset right padding
        binding.sbRightPadding.setResetClickListener(v -> {
            Prefs.putInt(FABRICATED_SB_RIGHT_PADDING, 8);
            ResourceManager.removeResourceFromOverlay(
                    requireContext(),
                    new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "status_bar_padding_end")
            );
            return true;
        });

        // Statusbar height
        binding.sbHeight.setSliderValue(finalSBHeight[0]);
        binding.sbHeight.setOnSliderTouchListener(sbHeightListener);

        // Reset height
        binding.sbHeight.setResetClickListener(v -> {
            Prefs.putInt(FABRICATED_SB_HEIGHT, 28);
            ResourceManager.removeResourceFromOverlay(
                    requireContext(),
                    new ResourceEntry(FRAMEWORK_PACKAGE, "dimen", "status_bar_height"),
                    new ResourceEntry(FRAMEWORK_PACKAGE, "dimen", "status_bar_height_default"),
                    new ResourceEntry(FRAMEWORK_PACKAGE, "dimen", "status_bar_height_portrait"),
                    new ResourceEntry(FRAMEWORK_PACKAGE, "dimen", "status_bar_height_landscape")
            );
            return true;
        });

        colorSBTint = String.valueOf(getResources().getColor(R.color.colorAccent, Iconify.getAppContext().getTheme()));

        //set current choosen style
        selectedStyle = Prefs.getString(FABRICATED_SB_COLOR_SOURCE);

        if (Objects.equals(selectedStyle, "Monet") || Prefs.getBoolean("IconifyComponentSBTint.overlay")) {
            binding.sbTintMonet.setChecked(true);
            Prefs.putString(FABRICATED_SB_COLOR_SOURCE, "Monet");
        } else if (Objects.equals(selectedStyle, "System"))
            binding.sbTintSystem.setChecked(true);
        else if (Objects.equals(selectedStyle, "Custom"))
            binding.sbTintCustom.setChecked(true);

        // Statusbar color source select
        binding.sbTintSourceSelector.setOnCheckedChangeListener((group, checkedId) -> {
            if (Objects.equals(checkedId, R.id.sb_tint_system)) {
                if (!Objects.equals(selectedStyle, "System")) {
                    Prefs.putString(FABRICATED_SB_COLOR_SOURCE, "System");
                    resetSBColor();
                }
            } else if (Objects.equals(checkedId, R.id.sb_tint_monet)) {
                if (!Objects.equals(selectedStyle, "Monet")) {
                    OverlayUtil.enableOverlay("IconifyComponentSBTint.overlay");
                    Prefs.putString(FABRICATED_SB_COLOR_SOURCE, "Monet");
                    new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::restartSystemUI, SWITCH_ANIMATION_DELAY);
                }
            } else if (Objects.equals(checkedId, R.id.sb_tint_custom)) {
                ((MainActivity) requireActivity()).showColorPickerDialog(1, Integer.parseInt(colorSBTint), true, false, true);
            }
        });

        return view;
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onColorSelected(ColorSelectedEvent event) {
        if (event.dialogId() == 1) {
            colorSBTint = String.valueOf(event.selectedColor());
            Prefs.putString(FABRICATED_SB_COLOR_TINT, colorSBTint);
            applySBColor();
            Prefs.putString(FABRICATED_SB_COLOR_SOURCE, "Custom");
            OverlayUtil.disableOverlay("IconifyComponentSBTint.overlay");
        }
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onDialogDismissed(ColorDismissedEvent event) {
        if (event.dialogId() == 1) {
            selectedStyle = Prefs.getString(FABRICATED_SB_COLOR_SOURCE);
            if (Objects.equals(selectedStyle, "System"))
                binding.sbTintSystem.setChecked(true);
            else if (Objects.equals(selectedStyle, "Monet"))
                binding.sbTintMonet.setChecked(true);
            else if (Objects.equals(selectedStyle, "Custom"))
                binding.sbTintCustom.setChecked(true);
        }
    }

    private void applySBColor() {
        FabricatedUtil.buildAndEnableOverlays(new Object[]{SYSTEMUI_PACKAGE, "colorSBTint1", "color", "dark_mode_icon_color_dual_tone_fill", colorToSpecialHex(Integer.parseInt(colorSBTint))},
                new Object[]{SYSTEMUI_PACKAGE, "colorSBTint2", "color", "dark_mode_icon_color_single_tone", colorToSpecialHex(Integer.parseInt(colorSBTint))},
                new Object[]{SYSTEMUI_PACKAGE, "colorSBTint3", "color", "dark_mode_qs_icon_color_dual_tone_fill", colorToSpecialHex(Integer.parseInt(colorSBTint))},
                new Object[]{SYSTEMUI_PACKAGE, "colorSBTint4", "color", "dark_mode_qs_icon_color_single_tone", colorToSpecialHex(Integer.parseInt(colorSBTint))},
                new Object[]{SYSTEMUI_PACKAGE, "colorSBTint5", "color", "light_mode_icon_color_dual_tone_fill", colorToSpecialHex(Integer.parseInt(colorSBTint))},
                new Object[]{SYSTEMUI_PACKAGE, "colorSBTint6", "color", "light_mode_icon_color_single_tone", colorToSpecialHex(Integer.parseInt(colorSBTint))},
                new Object[]{SYSTEMUI_PACKAGE, "colorSBTint7", "color", "status_bar_clock_color", colorToSpecialHex(Integer.parseInt(colorSBTint))}
        );

        new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::restartSystemUI, 1000);
    }

    private void resetSBColor() {
        FabricatedUtil.disableOverlays("colorSBTint1", "colorSBTint2", "colorSBTint3", "colorSBTint4", "colorSBTint5", "colorSBTint6", "colorSBTint7");
        OverlayUtil.disableOverlay("IconifyComponentSBTint.overlay");

        new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::restartSystemUI, 1000);
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