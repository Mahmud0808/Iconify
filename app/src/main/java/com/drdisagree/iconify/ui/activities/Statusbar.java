package com.drdisagree.iconify.ui.activities;

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
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.databinding.ActivityStatusbarBinding;
import com.drdisagree.iconify.ui.utils.ViewHelper;
import com.drdisagree.iconify.utils.overlay.FabricatedUtil;
import com.drdisagree.iconify.utils.overlay.OverlayUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.google.android.material.slider.Slider;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import java.util.Objects;

@SuppressLint("SetTextI18n")
public class Statusbar extends BaseActivity implements ColorPickerDialogListener {

    private static String colorSBTint, selectedStyle;
    private final int[] finalSBLeftPadding = {Prefs.getInt(FABRICATED_SB_LEFT_PADDING, 8)};
    private final int[] finalSBRightPadding = {Prefs.getInt(FABRICATED_SB_RIGHT_PADDING, 8)};
    private final int[] finalSBHeight = {Prefs.getInt(FABRICATED_SB_HEIGHT, 28)};
    private ColorPickerDialog.Builder colorPickerSBTint;
    private ActivityStatusbarBinding binding;

    private final Slider.OnSliderTouchListener sbRightPaddingListener = new Slider.OnSliderTouchListener() {
        @Override
        public void onStartTrackingTouch(@NonNull Slider slider) {
        }

        @Override
        public void onStopTrackingTouch(@NonNull Slider slider) {
            finalSBRightPadding[0] = (int) slider.getValue();
            binding.sbRightPaddingOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + finalSBRightPadding[0] + "dp");
            Prefs.putInt(FABRICATED_SB_RIGHT_PADDING, finalSBRightPadding[0]);
            FabricatedUtil.buildAndEnableOverlay(SYSTEMUI_PACKAGE, FABRICATED_SB_RIGHT_PADDING, "dimen", "status_bar_padding_end", finalSBRightPadding[0] + "dp");
            binding.resetSbRightPadding.setVisibility(View.VISIBLE);
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
        }
    };

    private final Slider.OnSliderTouchListener sbLeftPaddingListener = new Slider.OnSliderTouchListener() {
        @Override
        public void onStartTrackingTouch(@NonNull Slider slider) {
        }

        @Override
        public void onStopTrackingTouch(@NonNull Slider slider) {
            finalSBLeftPadding[0] = (int) slider.getValue();
            binding.sbLeftPaddingOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + finalSBLeftPadding[0] + "dp");
            Prefs.putInt(FABRICATED_SB_LEFT_PADDING, finalSBLeftPadding[0]);
            FabricatedUtil.buildAndEnableOverlay(SYSTEMUI_PACKAGE, FABRICATED_SB_LEFT_PADDING, "dimen", "status_bar_padding_start", finalSBLeftPadding[0] + "dp");
            binding.resetSbLeftPadding.setVisibility(View.VISIBLE);
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
        }
    };

    private final Slider.OnSliderTouchListener sbPortraitHeightListener = new Slider.OnSliderTouchListener() {
        @Override
        public void onStartTrackingTouch(@NonNull Slider slider) {
        }

        @Override
        public void onStopTrackingTouch(@NonNull Slider slider) {
            finalSBHeight[0] = (int) slider.getValue();
            binding.sbHeightOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + finalSBHeight[0] + "dp");
            Prefs.putInt(FABRICATED_SB_HEIGHT, finalSBHeight[0]);
            FabricatedUtil.buildAndEnableOverlays(
                    new Object[]{FRAMEWORK_PACKAGE, FABRICATED_SB_HEIGHT, "dimen", "status_bar_height", finalSBHeight[0] + "dp"},
                    new Object[]{FRAMEWORK_PACKAGE, FABRICATED_SB_HEIGHT + "Default", "dimen", "status_bar_height_default", finalSBHeight[0] + "dp"},
                    new Object[]{FRAMEWORK_PACKAGE, FABRICATED_SB_HEIGHT + "Portrait", "dimen", "status_bar_height_portrait", finalSBHeight[0] + "dp"},
                    new Object[]{FRAMEWORK_PACKAGE, FABRICATED_SB_HEIGHT + "Landscape", "dimen", "status_bar_height_landscape", finalSBHeight[0] + "dp"}
            );
            binding.resetSbHeight.setVisibility(View.VISIBLE);
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
        }
    };

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStatusbarBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Header
        ViewHelper.setHeader(this, binding.header.toolbar, R.string.activity_title_statusbar);

        // Statusbar left padding
        binding.sbLeftPaddingOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + finalSBLeftPadding[0] + "dp");
        binding.sbLeftPaddingSeekbar.setValue(finalSBLeftPadding[0]);
        binding.sbLeftPaddingSeekbar.addOnSliderTouchListener(sbLeftPaddingListener);

        // Reset left padding
        binding.resetSbLeftPadding.setVisibility(Prefs.getBoolean("fabricated" + FABRICATED_SB_LEFT_PADDING, false) ? View.VISIBLE : View.INVISIBLE);

        binding.resetSbLeftPadding.setOnLongClickListener(v -> {
            FabricatedUtil.disableOverlay(FABRICATED_SB_LEFT_PADDING);
            Prefs.putInt(FABRICATED_SB_LEFT_PADDING, 8);
            binding.resetSbLeftPadding.setVisibility(View.INVISIBLE);
            binding.sbLeftPaddingSeekbar.addOnSliderTouchListener(null);
            binding.sbLeftPaddingSeekbar.setValue(8);
            binding.sbLeftPaddingSeekbar.addOnSliderTouchListener(sbLeftPaddingListener);
            binding.sbLeftPaddingOutput.setText(getResources().getString(R.string.opt_selected) + " 8dp");
            return true;
        });

        // Statusbar right padding
        binding.sbRightPaddingOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + finalSBRightPadding[0] + "dp");
        binding.sbRightPaddingSeekbar.setValue(finalSBRightPadding[0]);
        binding.sbRightPaddingSeekbar.addOnSliderTouchListener(sbRightPaddingListener);

        // Reset right padding
        binding.resetSbRightPadding.setVisibility(Prefs.getBoolean("fabricated" + FABRICATED_SB_RIGHT_PADDING, false) ? View.VISIBLE : View.INVISIBLE);

        binding.resetSbRightPadding.setOnLongClickListener(v -> {
            FabricatedUtil.disableOverlay(FABRICATED_SB_RIGHT_PADDING);
            Prefs.putInt(FABRICATED_SB_RIGHT_PADDING, 8);
            binding.resetSbRightPadding.setVisibility(View.INVISIBLE);
            binding.sbRightPaddingSeekbar.addOnSliderTouchListener(null);
            binding.sbRightPaddingSeekbar.setValue(8);
            binding.sbRightPaddingSeekbar.addOnSliderTouchListener(sbRightPaddingListener);
            binding.sbRightPaddingOutput.setText(getResources().getString(R.string.opt_selected) + " 8dp");
            return true;
        });

        // Statusbar height
        binding.sbHeightOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + finalSBHeight[0] + "dp");
        binding.sbHeightSeekbar.setValue(finalSBHeight[0]);
        binding.sbHeightSeekbar.addOnSliderTouchListener(sbPortraitHeightListener);

        // Reset height
        binding.resetSbHeight.setVisibility(Prefs.getBoolean("fabricated" + FABRICATED_SB_HEIGHT, false) ? View.VISIBLE : View.INVISIBLE);

        binding.resetSbHeight.setOnLongClickListener(v -> {
            FabricatedUtil.disableOverlays(FABRICATED_SB_HEIGHT, FABRICATED_SB_HEIGHT + "Default", FABRICATED_SB_HEIGHT + "Portrait", FABRICATED_SB_HEIGHT + "Landscape");
            Prefs.putInt(FABRICATED_SB_HEIGHT, 28);
            binding.resetSbHeight.setVisibility(View.INVISIBLE);
            binding.sbHeightSeekbar.addOnSliderTouchListener(null);
            binding.sbHeightSeekbar.setValue(28);
            binding.sbHeightSeekbar.addOnSliderTouchListener(sbRightPaddingListener);
            binding.sbHeightOutput.setText(getResources().getString(R.string.opt_selected) + " 28dp");
            return true;
        });

        colorSBTint = String.valueOf(getResources().getColor(R.color.colorAccent, getTheme()));

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
                colorPickerSBTint = ColorPickerDialog.newBuilder();
                colorPickerSBTint.setDialogStyle(R.style.ColorPicker).setColor(Integer.parseInt(colorSBTint)).setDialogType(ColorPickerDialog.TYPE_CUSTOM).setAllowCustom(false).setAllowPresets(true).setDialogId(1).setShowAlphaSlider(false).setShowColorShades(true);
                colorPickerSBTint.show(Statusbar.this);
            }
        });
    }

    @Override
    public void onColorSelected(int dialogId, int color) {
        if (dialogId == 1) {
            colorSBTint = String.valueOf(color);
            Prefs.putString(FABRICATED_SB_COLOR_TINT, colorSBTint);
            applySBColor();
            OverlayUtil.disableOverlay("IconifyComponentSBTint.overlay");
            colorPickerSBTint.setDialogStyle(R.style.ColorPicker).setColor(Integer.parseInt(colorSBTint)).setDialogType(ColorPickerDialog.TYPE_CUSTOM).setAllowCustom(false).setAllowPresets(true).setDialogId(1).setShowAlphaSlider(false).setShowColorShades(true);
            Prefs.putString(FABRICATED_SB_COLOR_SOURCE, "Custom");
        }
    }

    @Override
    public void onDialogDismissed(int dialogId) {
        selectedStyle = Prefs.getString(FABRICATED_SB_COLOR_SOURCE);
        if (Objects.equals(selectedStyle, "System"))
            binding.sbTintSystem.setChecked(true);
        else if (Objects.equals(selectedStyle, "Monet"))
            binding.sbTintMonet.setChecked(true);
        else if (Objects.equals(selectedStyle, "Custom"))
            binding.sbTintCustom.setChecked(true);
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
}