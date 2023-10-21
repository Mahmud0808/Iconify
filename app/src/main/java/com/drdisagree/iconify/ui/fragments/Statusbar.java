package com.drdisagree.iconify.ui.fragments;

import static com.drdisagree.iconify.common.Const.TRANSITION_DELAY;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.databinding.FragmentStatusbarBinding;
import com.drdisagree.iconify.ui.activities.HomePage;
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
    private FragmentStatusbarBinding binding;
    private final Slider.OnSliderTouchListener sbLeftPaddingListener = new Slider.OnSliderTouchListener() {
        @Override
        public void onStartTrackingTouch(@NonNull Slider slider) {
        }

        @Override
        public void onStopTrackingTouch(@NonNull Slider slider) {
            finalSBLeftPadding[0] = (int) slider.getValue();
            binding.sbLeftPaddingOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + finalSBLeftPadding[0] + "dp");
            Prefs.putInt(FABRICATED_SB_LEFT_PADDING, finalSBLeftPadding[0]);
            if (!SystemUtil.hasStoragePermission()) {
                SystemUtil.requestStoragePermission(requireContext());
            } else {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    try {
                        ResourceManager.createResource(new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "status_bar_padding_start", finalSBLeftPadding[0] + "dp"));
                        Toast.makeText(Iconify.getAppContext(), Iconify.getAppContextLocale().getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(Iconify.getAppContext(), Iconify.getAppContextLocale().getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                        Log.e("Iconify", e.toString());
                    }
                }, TRANSITION_DELAY);
            }
            binding.resetSbLeftPadding.setVisibility(View.VISIBLE);
        }
    };
    private final Slider.OnSliderTouchListener sbRightPaddingListener = new Slider.OnSliderTouchListener() {
        @Override
        public void onStartTrackingTouch(@NonNull Slider slider) {
        }

        @Override
        public void onStopTrackingTouch(@NonNull Slider slider) {
            finalSBRightPadding[0] = (int) slider.getValue();
            binding.sbRightPaddingOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + finalSBRightPadding[0] + "dp");
            Prefs.putInt(FABRICATED_SB_RIGHT_PADDING, finalSBRightPadding[0]);
            if (!SystemUtil.hasStoragePermission()) {
                SystemUtil.requestStoragePermission(requireContext());
            } else {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    try {
                        ResourceManager.createResource(new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "status_bar_padding_end", finalSBRightPadding[0] + "dp"));
                        Toast.makeText(Iconify.getAppContext(), Iconify.getAppContextLocale().getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(Iconify.getAppContext(), Iconify.getAppContextLocale().getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                        Log.e("Iconify", e.toString());
                    }
                }, TRANSITION_DELAY);
            }
            binding.resetSbRightPadding.setVisibility(View.VISIBLE);
        }
    };
    private final Slider.OnSliderTouchListener sbHeightListener = new Slider.OnSliderTouchListener() {
        @Override
        public void onStartTrackingTouch(@NonNull Slider slider) {
        }

        @Override
        public void onStopTrackingTouch(@NonNull Slider slider) {
            finalSBHeight[0] = (int) slider.getValue();
            binding.sbHeightOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + finalSBHeight[0] + "dp");
            Prefs.putInt(FABRICATED_SB_HEIGHT, finalSBHeight[0]);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                try {
                    ResourceManager.createResource(
                            new ResourceEntry(FRAMEWORK_PACKAGE, "dimen", "status_bar_height", finalSBHeight[0] + "dp"),
                            new ResourceEntry(FRAMEWORK_PACKAGE, "dimen", "status_bar_height_default", finalSBHeight[0] + "dp"),
                            new ResourceEntry(FRAMEWORK_PACKAGE, "dimen", "status_bar_height_portrait", finalSBHeight[0] + "dp"),
                            new ResourceEntry(FRAMEWORK_PACKAGE, "dimen", "status_bar_height_landscape", finalSBHeight[0] + "dp")
                    );
                    Toast.makeText(Iconify.getAppContext(), Iconify.getAppContextLocale().getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(Iconify.getAppContext(), Iconify.getAppContextLocale().getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                    Log.e("Iconify", e.toString());
                }
            }, TRANSITION_DELAY);
            binding.resetSbHeight.setVisibility(View.VISIBLE);
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStatusbarBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Header
        ViewHelper.setHeader(requireContext(), getParentFragmentManager(), binding.header.toolbar, R.string.activity_title_statusbar);

        // Statusbar left padding
        binding.sbLeftPaddingOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + finalSBLeftPadding[0] + "dp");
        binding.sbLeftPaddingSeekbar.setValue(finalSBLeftPadding[0]);
        binding.sbLeftPaddingSeekbar.addOnSliderTouchListener(sbLeftPaddingListener);

        // Reset left padding
        binding.resetSbLeftPadding.setVisibility(Prefs.getInt(FABRICATED_SB_LEFT_PADDING, 8) != 8 ? View.VISIBLE : View.INVISIBLE);

        binding.resetSbLeftPadding.setOnLongClickListener(v -> {
            Prefs.putInt(FABRICATED_SB_LEFT_PADDING, 8);
            if (!SystemUtil.hasStoragePermission()) {
                SystemUtil.requestStoragePermission(requireContext());
            } else {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    try {
                        ResourceManager.removeResource(new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "status_bar_padding_start", "8dp"));
                        Toast.makeText(Iconify.getAppContext(), Iconify.getAppContextLocale().getResources().getString(R.string.toast_reset), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(Iconify.getAppContext(), Iconify.getAppContextLocale().getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                        Log.e("Iconify", e.toString());
                    }
                }, TRANSITION_DELAY);
            }
            binding.resetSbLeftPadding.setVisibility(View.INVISIBLE);
            binding.sbLeftPaddingSeekbar.removeOnSliderTouchListener(sbLeftPaddingListener);
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
        binding.resetSbRightPadding.setVisibility(Prefs.getInt(FABRICATED_SB_RIGHT_PADDING, 8) != 8 ? View.VISIBLE : View.INVISIBLE);

        binding.resetSbRightPadding.setOnLongClickListener(v -> {
            Prefs.putInt(FABRICATED_SB_RIGHT_PADDING, 8);
            if (!SystemUtil.hasStoragePermission()) {
                SystemUtil.requestStoragePermission(requireContext());
            } else {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    try {
                        ResourceManager.removeResource(new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "status_bar_padding_end", "8dp"));
                        Toast.makeText(Iconify.getAppContext(), Iconify.getAppContextLocale().getResources().getString(R.string.toast_reset), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(Iconify.getAppContext(), Iconify.getAppContextLocale().getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                        Log.e("Iconify", e.toString());
                    }
                }, TRANSITION_DELAY);
            }
            binding.resetSbRightPadding.setVisibility(View.INVISIBLE);
            binding.sbRightPaddingSeekbar.removeOnSliderTouchListener(sbRightPaddingListener);
            binding.sbRightPaddingSeekbar.setValue(8);
            binding.sbRightPaddingSeekbar.addOnSliderTouchListener(sbRightPaddingListener);
            binding.sbRightPaddingOutput.setText(getResources().getString(R.string.opt_selected) + " 8dp");
            return true;
        });

        // Statusbar height
        binding.sbHeightOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + finalSBHeight[0] + "dp");
        binding.sbHeightSeekbar.setValue(finalSBHeight[0]);
        binding.sbHeightSeekbar.addOnSliderTouchListener(sbHeightListener);

        // Reset height
        binding.resetSbHeight.setVisibility(Prefs.getInt(FABRICATED_SB_HEIGHT, 28) != 28 ? View.VISIBLE : View.INVISIBLE);

        binding.resetSbHeight.setOnLongClickListener(v -> {
            Prefs.putInt(FABRICATED_SB_HEIGHT, 28);
            if (!SystemUtil.hasStoragePermission()) {
                SystemUtil.requestStoragePermission(requireContext());
            } else {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    try {
                        ResourceManager.removeResource(
                                new ResourceEntry(FRAMEWORK_PACKAGE, "dimen", "status_bar_height", "28dp"),
                                new ResourceEntry(FRAMEWORK_PACKAGE, "dimen", "status_bar_height_default", "28dp"),
                                new ResourceEntry(FRAMEWORK_PACKAGE, "dimen", "status_bar_height_portrait", "28dp"),
                                new ResourceEntry(FRAMEWORK_PACKAGE, "dimen", "status_bar_height_landscape", "28dp")
                        );
                        Toast.makeText(Iconify.getAppContext(), Iconify.getAppContextLocale().getResources().getString(R.string.toast_reset), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(Iconify.getAppContext(), Iconify.getAppContextLocale().getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                        Log.e("Iconify", e.toString());
                    }
                }, TRANSITION_DELAY);
            }
            binding.resetSbHeight.setVisibility(View.INVISIBLE);
            binding.sbHeightSeekbar.removeOnSliderTouchListener(sbHeightListener);
            binding.sbHeightSeekbar.setValue(28);
            binding.sbHeightSeekbar.addOnSliderTouchListener(sbHeightListener);
            binding.sbHeightOutput.setText(getResources().getString(R.string.opt_selected) + " 28dp");
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
                ((HomePage) requireActivity()).showColorPickerDialog(1, Integer.parseInt(colorSBTint), true, false, true);
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