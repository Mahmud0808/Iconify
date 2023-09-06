package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Const.SWITCH_ANIMATION_DELAY;
import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;
import static com.drdisagree.iconify.common.References.FABRICATED_PILL_BOTTOM_SPACE;
import static com.drdisagree.iconify.common.References.FABRICATED_PILL_SHAPE_SWITCH;
import static com.drdisagree.iconify.common.References.FABRICATED_PILL_THICKNESS;
import static com.drdisagree.iconify.common.References.FABRICATED_PILL_WIDTH;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.databinding.ActivityNavigationBarBinding;
import com.drdisagree.iconify.ui.utils.ViewHelper;
import com.drdisagree.iconify.utils.FabricatedUtil;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.google.android.material.slider.Slider;
import com.topjohnwu.superuser.Shell;

import java.util.Objects;

public class NavigationBar extends BaseActivity {

    private ActivityNavigationBarBinding binding;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNavigationBarBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Header
        ViewHelper.setHeader(this, binding.header.toolbar, R.string.activity_title_navigation_bar);

        // Switch states
        binding.nbFullscreen.setChecked(Prefs.getBoolean("IconifyComponentNBFullScreen.overlay"));
        binding.nbImmersive.setChecked(Prefs.getBoolean("IconifyComponentNBImmersive.overlay"));
        binding.nbImmersivev2.setChecked(Prefs.getBoolean("IconifyComponentNBImmersiveSmall.overlay"));
        binding.nbImmersivev3.setChecked(Prefs.getBoolean("IconifyComponentNBImmersiveSmaller.overlay"));
        binding.nbLowerSens.setChecked(Prefs.getBoolean("IconifyComponentNBLowSens.overlay"));
        binding.nbHidePill.setChecked(Prefs.getBoolean("IconifyComponentNBHidePill.overlay"));
        binding.nbMonetPill.setChecked(Prefs.getBoolean("IconifyComponentNBMonetPill.overlay"));
        binding.nbHideKbButtons.setChecked(Prefs.getBoolean("IconifyComponentNBHideKBButton.overlay"));
        binding.nbDisableLeftGesture.setChecked(initialize_left_gesture_switch());
        binding.nbDisableRightGesture.setChecked(initialize_right_gesture_switch());
        binding.nbHidePill.setEnabled(!binding.nbFullscreen.isChecked());
        binding.nbMonetPill.setEnabled(!binding.nbHidePill.isChecked() && !binding.nbFullscreen.isChecked());

        // Fullscreen
        binding.nbFullscreen.setOnCheckedChangeListener((buttonView, isChecked) -> {
            binding.nbHidePill.setEnabled(!isChecked);
            binding.nbMonetPill.setEnabled(!isChecked && !binding.nbHidePill.isChecked());

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (isChecked) {
                    disableOthers("IconifyComponentNBFullScreen.overlay");
                    OverlayUtil.enableOverlay("IconifyComponentNBFullScreen.overlay");
                    binding.pillShape.pillShapeContainer.setVisibility(View.GONE);
                } else {
                    OverlayUtil.disableOverlay("IconifyComponentNBFullScreen.overlay");
                    binding.pillShape.pillShapeContainer.setVisibility(View.VISIBLE);
                }
            }, SWITCH_ANIMATION_DELAY);
        });
        binding.nbFullscreenContainer.setOnClickListener(v -> binding.nbFullscreen.toggle());

        // Immersive
        binding.nbImmersive.setOnCheckedChangeListener((buttonView, isChecked) -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isChecked) {
                disableOthers("IconifyComponentNBImmersive.overlay");
                OverlayUtil.enableOverlay("IconifyComponentNBImmersive.overlay");
            } else {
                OverlayUtil.disableOverlay("IconifyComponentNBImmersive.overlay");
            }
        }, SWITCH_ANIMATION_DELAY));
        binding.nbImmersiveContainer.setOnClickListener(v -> binding.nbImmersive.toggle());

        // Immersive v2
        binding.nbImmersivev2.setOnCheckedChangeListener((buttonView, isChecked) -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isChecked) {
                disableOthers("IconifyComponentNBImmersiveSmall.overlay");
                OverlayUtil.enableOverlay("IconifyComponentNBImmersiveSmall.overlay");
            } else {
                OverlayUtil.disableOverlay("IconifyComponentNBImmersiveSmall.overlay");
            }
        }, SWITCH_ANIMATION_DELAY));
        binding.nbImmersivev2Container.setOnClickListener(v -> binding.nbImmersivev2.toggle());

        // Immersive v3
        binding.nbImmersivev3.setOnCheckedChangeListener((buttonView, isChecked) -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isChecked) {
                disableOthers("IconifyComponentNBImmersiveSmaller.overlay");
                OverlayUtil.enableOverlay("IconifyComponentNBImmersiveSmaller.overlay");
            } else {
                OverlayUtil.disableOverlay("IconifyComponentNBImmersiveSmaller.overlay");
            }
        }, SWITCH_ANIMATION_DELAY));
        binding.nbImmersivev3Container.setOnClickListener(v -> binding.nbImmersivev3.toggle());

        // Lower Sensitivity
        binding.nbHidePill.setOnCheckedChangeListener((buttonView, isChecked) -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isChecked) {
                OverlayUtil.enableOverlay("IconifyComponentNBLowSens.overlay");
            } else {
                OverlayUtil.disableOverlay("IconifyComponentNBLowSens.overlay");
            }
        }, SWITCH_ANIMATION_DELAY));
        binding.nbLowerSensContainer.setOnClickListener(v -> binding.nbLowerSens.toggle());

        // Hide Pill
        binding.nbHidePill.setOnCheckedChangeListener((buttonView, isChecked) -> {
            binding.nbMonetPill.setEnabled(!isChecked && !binding.nbFullscreen.isChecked());

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (isChecked) {
                    OverlayUtil.enableOverlay("IconifyComponentNBHidePill.overlay");
                    binding.pillShape.pillShapeContainer.setVisibility(View.GONE);
                    SystemUtil.restartSystemUI();
                } else {
                    OverlayUtil.disableOverlay("IconifyComponentNBHidePill.overlay");
                    binding.pillShape.pillShapeContainer.setVisibility(View.VISIBLE);
                    SystemUtil.restartSystemUI();
                }
            }, SWITCH_ANIMATION_DELAY);
        });
        binding.nbHidePillContainer.setOnClickListener(v -> binding.nbHidePill.toggle());

        // Monet Pill
        binding.nbMonetPill.setOnCheckedChangeListener((buttonView, isChecked) -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isChecked) {
                OverlayUtil.enableOverlay("IconifyComponentNBMonetPill.overlay");
                SystemUtil.restartSystemUI();
            } else {
                OverlayUtil.disableOverlay("IconifyComponentNBMonetPill.overlay");
                SystemUtil.restartSystemUI();
            }
        }, SWITCH_ANIMATION_DELAY));
        binding.nbMonetPillContainer.setOnClickListener(v -> binding.nbMonetPill.toggle());

        // Hide Keyboard Buttons
        binding.nbHideKbButtons.setOnCheckedChangeListener((buttonView, isChecked) -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isChecked) {
                OverlayUtil.enableOverlay("IconifyComponentNBHideKBButton.overlay");
            } else {
                OverlayUtil.disableOverlay("IconifyComponentNBHideKBButton.overlay");
            }
        }, SWITCH_ANIMATION_DELAY));
        binding.nbHideKbButtonsContainer.setOnClickListener(v -> binding.nbHideKbButtons.toggle());

        // Disable left gesture
        binding.nbDisableLeftGesture.setOnCheckedChangeListener((buttonView, isChecked) -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isChecked) {
                Shell.cmd("settings put secure back_gesture_inset_scale_left -1 &>/dev/null").exec();
            } else {
                Shell.cmd("settings delete secure back_gesture_inset_scale_left &>/dev/null").exec();
            }
        }, SWITCH_ANIMATION_DELAY));
        binding.nbDisableLeftGestureContainer.setOnClickListener(v -> binding.nbDisableLeftGesture.toggle());

        // Disable right gesture
        binding.nbDisableRightGesture.setOnCheckedChangeListener((buttonView, isChecked) -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isChecked) {
                Shell.cmd("settings put secure back_gesture_inset_scale_right -1 &>/dev/null").exec();
            } else {
                Shell.cmd("settings delete secure back_gesture_inset_scale_right &>/dev/null").exec();
            }
        }, SWITCH_ANIMATION_DELAY));
        binding.nbDisableRightGestureContainer.setOnClickListener(v -> binding.nbDisableRightGesture.toggle());

        // Pill shape
        binding.pillShape.pillShapeContainer.setVisibility((binding.nbFullscreen.isChecked() || binding.nbHidePill.isChecked()) ? View.GONE : View.VISIBLE);

        // Pill width
        final int[] finalPillWidth = {Prefs.getInt(FABRICATED_PILL_WIDTH, 108)};

        binding.pillShape.pillWidthOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + finalPillWidth[0] + "dp");
        binding.pillShape.pillWidthSeekbar.setValue(finalPillWidth[0]);
        binding.pillShape.pillWidthSeekbar.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                finalPillWidth[0] = (int) slider.getValue();
                binding.pillShape.pillWidthOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + finalPillWidth[0] + "dp");
            }
        });

        // Pill thickness
        final int[] finalPillThickness = {Prefs.getInt(FABRICATED_PILL_THICKNESS, 2)};

        binding.pillShape.pillThicknessOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + finalPillThickness[0] + "dp");
        binding.pillShape.pillThicknessSeekbar.setValue(finalPillThickness[0]);
        binding.pillShape.pillThicknessSeekbar.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                finalPillThickness[0] = (int) slider.getValue();
                binding.pillShape.pillThicknessOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + finalPillThickness[0] + "dp");
            }
        });

        // Bottom space
        final int[] finalBottomSpace = {Prefs.getInt(FABRICATED_PILL_BOTTOM_SPACE, 6)};

        binding.pillShape.bottomSpaceOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + finalBottomSpace[0] + "dp");
        binding.pillShape.bottomSpaceSeekbar.setValue(finalBottomSpace[0]);
        binding.pillShape.bottomSpaceSeekbar.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                finalBottomSpace[0] = (int) slider.getValue();
                binding.pillShape.bottomSpaceOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + finalBottomSpace[0] + "dp");
            }
        });

        // Apply button
        binding.pillShape.pillShapeApply.setOnClickListener(v -> {
            Prefs.putBoolean(FABRICATED_PILL_SHAPE_SWITCH, true);
            Prefs.putInt(FABRICATED_PILL_WIDTH, finalPillWidth[0]);
            Prefs.putInt(FABRICATED_PILL_THICKNESS, finalPillThickness[0]);
            Prefs.putInt(FABRICATED_PILL_BOTTOM_SPACE, finalBottomSpace[0]);

            FabricatedUtil.buildAndEnableOverlays(
                    new Object[]{SYSTEMUI_PACKAGE, FABRICATED_PILL_WIDTH, "dimen", "navigation_home_handle_width", finalPillWidth[0] + "dp"},
                    new Object[]{SYSTEMUI_PACKAGE, FABRICATED_PILL_THICKNESS, "dimen", "navigation_handle_radius", finalPillThickness[0] + "dp"},
                    new Object[]{SYSTEMUI_PACKAGE, FABRICATED_PILL_BOTTOM_SPACE, "dimen", "navigation_handle_bottom", finalBottomSpace[0] + "dp"}
            );

            binding.pillShape.pillShapeReset.setVisibility(View.VISIBLE);
            SystemUtil.restartSystemUI();
        });

        // Reset button
        binding.pillShape.pillShapeReset.setVisibility(Prefs.getBoolean(FABRICATED_PILL_SHAPE_SWITCH) ? View.VISIBLE : View.GONE);
        binding.pillShape.pillShapeReset.setOnClickListener(v -> {
            Prefs.putBoolean(FABRICATED_PILL_SHAPE_SWITCH, false);

            FabricatedUtil.disableOverlays(FABRICATED_PILL_WIDTH, FABRICATED_PILL_THICKNESS, FABRICATED_PILL_BOTTOM_SPACE);

            binding.pillShape.pillShapeReset.setVisibility(View.GONE);
            SystemUtil.restartSystemUI();
        });
    }

    private boolean initialize_left_gesture_switch() {
        try {
            return Integer.parseInt(Shell.cmd("settings get secure back_gesture_inset_scale_left").exec().getOut().get(0)) == -1;
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean initialize_right_gesture_switch() {
        try {
            return Integer.parseInt(Shell.cmd("settings get secure back_gesture_inset_scale_right").exec().getOut().get(0)) == -1;
        } catch (Exception ignored) {
            return false;
        }
    }

    private void disableOthers(String pkgName) {
        if (!Objects.equals(pkgName, "IconifyComponentNBFullScreen.overlay")) {
            binding.nbFullscreen.setChecked(false);
            OverlayUtil.disableOverlay("IconifyComponentNBFullScreen.overlay");
        }
        if (!Objects.equals(pkgName, "IconifyComponentNBImmersive.overlay")) {
            binding.nbImmersive.setChecked(false);
            OverlayUtil.disableOverlay("IconifyComponentNBImmersive.overlay");
        }
        if (!Objects.equals(pkgName, "IconifyComponentNBImmersiveSmall.overlay")) {
            binding.nbImmersivev2.setChecked(false);
            OverlayUtil.disableOverlay("IconifyComponentNBImmersiveSmall.overlay");
        }
        if (!Objects.equals(pkgName, "IconifyComponentNBImmersiveSmaller.overlay")) {
            binding.nbImmersivev3.setChecked(false);
            OverlayUtil.disableOverlay("IconifyComponentNBImmersiveSmaller.overlay");
        }
    }
}