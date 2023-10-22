package com.drdisagree.iconify.ui.fragments;

import static com.drdisagree.iconify.common.Const.FRAMEWORK_PACKAGE;
import static com.drdisagree.iconify.common.Const.SWITCH_ANIMATION_DELAY;
import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.NAVBAR_FULL_SCREEN;
import static com.drdisagree.iconify.common.Preferences.NAVBAR_GCAM_LAG_FIX;
import static com.drdisagree.iconify.common.Preferences.NAVBAR_HIDE_PILL;
import static com.drdisagree.iconify.common.Preferences.NAVBAR_IMMERSIVE_V1;
import static com.drdisagree.iconify.common.Preferences.NAVBAR_IMMERSIVE_V2;
import static com.drdisagree.iconify.common.Preferences.NAVBAR_IMMERSIVE_V3;
import static com.drdisagree.iconify.common.Preferences.NAVBAR_LOW_SENS;
import static com.drdisagree.iconify.common.Preferences.PILL_SHAPE_SWITCH;
import static com.drdisagree.iconify.common.References.FABRICATED_PILL_BOTTOM_SPACE;
import static com.drdisagree.iconify.common.References.FABRICATED_PILL_THICKNESS;
import static com.drdisagree.iconify.common.References.FABRICATED_PILL_WIDTH;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.databinding.FragmentNavigationBarBinding;
import com.drdisagree.iconify.ui.base.BaseFragment;
import com.drdisagree.iconify.ui.utils.ViewHelper;
import com.drdisagree.iconify.utils.SystemUtil;
import com.drdisagree.iconify.utils.overlay.OverlayUtil;
import com.drdisagree.iconify.utils.overlay.manager.resource.ResourceEntry;
import com.drdisagree.iconify.utils.overlay.manager.resource.ResourceManager;
import com.google.android.material.slider.Slider;
import com.topjohnwu.superuser.Shell;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class NavigationBar extends BaseFragment {

    private FragmentNavigationBarBinding binding;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNavigationBarBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Header
        ViewHelper.setHeader(requireContext(), getParentFragmentManager(), binding.header.toolbar, R.string.activity_title_navigation_bar);

        // Switch states
        binding.nbFullscreen.setChecked(Prefs.getBoolean(NAVBAR_FULL_SCREEN));
        binding.nbImmersive.setChecked(Prefs.getBoolean(NAVBAR_IMMERSIVE_V1));
        binding.nbImmersivev2.setChecked(Prefs.getBoolean(NAVBAR_IMMERSIVE_V2));
        binding.nbImmersivev3.setChecked(Prefs.getBoolean(NAVBAR_IMMERSIVE_V3));
        binding.nbGcamLagFix.setChecked(Prefs.getBoolean(NAVBAR_GCAM_LAG_FIX));
        binding.nbLowerSens.setChecked(Prefs.getBoolean(NAVBAR_LOW_SENS));
        binding.nbHidePill.setChecked(Prefs.getBoolean(NAVBAR_HIDE_PILL));
        binding.nbMonetPill.setChecked(Prefs.getBoolean("IconifyComponentNBMonetPill.overlay"));
        binding.nbHideKbButtons.setChecked(Prefs.getBoolean("IconifyComponentNBHideKBButton.overlay"));
        binding.nbDisableLeftGesture.setChecked(initialize_left_gesture_switch());
        binding.nbDisableRightGesture.setChecked(initialize_right_gesture_switch());
        binding.nbHidePill.setEnabled(!binding.nbFullscreen.isChecked());
        binding.nbMonetPill.setEnabled(!binding.nbHidePill.isChecked() && !binding.nbFullscreen.isChecked());

        // Fullscreen
        AtomicBoolean nbFullScreenClicked = new AtomicBoolean(false);
        binding.nbFullscreen.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed() || nbFullScreenClicked.get()) {
                nbFullScreenClicked.set(false);

                if (!SystemUtil.hasStoragePermission()) {
                    SystemUtil.requestStoragePermission(requireContext());
                    binding.nbFullscreen.setChecked(!isChecked);
                    return;
                }

                binding.nbHidePill.setEnabled(!isChecked);
                binding.nbMonetPill.setEnabled(!isChecked && !binding.nbHidePill.isChecked());
                disableOthers(NAVBAR_FULL_SCREEN);

                if (isChecked) {
                    binding.pillShape.pillShapeContainer.setVisibility(View.GONE);
                } else {
                    binding.pillShape.pillShapeContainer.setVisibility(View.VISIBLE);
                }

                new Handler(Looper.getMainLooper()).postDelayed(() -> handleFullScreen(isChecked), SWITCH_ANIMATION_DELAY);
            }
        });
        binding.nbFullscreenContainer.setOnClickListener(v -> {
            nbFullScreenClicked.set(true);
            binding.nbFullscreen.toggle();
        });

        // Immersive
        AtomicBoolean nbImmersiveClicked = new AtomicBoolean(false);
        binding.nbImmersive.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed() || nbImmersiveClicked.get()) {
                nbImmersiveClicked.set(false);

                if (!SystemUtil.hasStoragePermission()) {
                    SystemUtil.requestStoragePermission(requireContext());
                    binding.nbImmersive.setChecked(!isChecked);
                    return;
                }

                disableOthers(NAVBAR_IMMERSIVE_V1);

                new Handler(Looper.getMainLooper()).postDelayed(() -> handleImmersive(isChecked, 1), SWITCH_ANIMATION_DELAY);
            }
        });
        binding.nbImmersiveContainer.setOnClickListener(v -> {
            nbImmersiveClicked.set(true);
            binding.nbImmersive.toggle();
        });

        // Immersive v2
        AtomicBoolean nbImmersivev2Clicked = new AtomicBoolean(false);
        binding.nbImmersivev2.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed() || nbImmersivev2Clicked.get()) {
                nbImmersivev2Clicked.set(false);

                if (!SystemUtil.hasStoragePermission()) {
                    SystemUtil.requestStoragePermission(requireContext());
                    binding.nbImmersivev2.setChecked(!isChecked);
                    return;
                }

                disableOthers(NAVBAR_IMMERSIVE_V2);

                new Handler(Looper.getMainLooper()).postDelayed(() -> handleImmersive(isChecked, 2), SWITCH_ANIMATION_DELAY);
            }
        });
        binding.nbImmersivev2Container.setOnClickListener(v -> {
            nbImmersivev2Clicked.set(true);
            binding.nbImmersivev2.toggle();
        });

        // Immersive v3
        AtomicBoolean nbImmersivev3Clicked = new AtomicBoolean(false);
        binding.nbImmersivev3.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed() || nbImmersivev3Clicked.get()) {
                nbImmersivev3Clicked.set(false);

                if (!SystemUtil.hasStoragePermission()) {
                    SystemUtil.requestStoragePermission(requireContext());
                    binding.nbImmersivev3.setChecked(!isChecked);
                    return;
                }

                disableOthers(NAVBAR_IMMERSIVE_V3);

                new Handler(Looper.getMainLooper()).postDelayed(() -> handleImmersive(isChecked, 3), SWITCH_ANIMATION_DELAY);
            }
        });
        binding.nbImmersivev3Container.setOnClickListener(v -> {
            nbImmersivev3Clicked.set(true);
            binding.nbImmersivev3.toggle();
        });

        // GCam Lag Fix
        AtomicBoolean nbGcamLagFixClicked = new AtomicBoolean(false);
        binding.nbGcamLagFix.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed() || nbGcamLagFixClicked.get()) {
                nbGcamLagFixClicked.set(false);

                if (!SystemUtil.hasStoragePermission()) {
                    SystemUtil.requestStoragePermission(requireContext());
                    binding.nbGcamLagFix.setChecked(!isChecked);
                    return;
                }

                Prefs.putBoolean(NAVBAR_GCAM_LAG_FIX, isChecked);
                boolean fullscreen = Prefs.getBoolean(NAVBAR_FULL_SCREEN);
                boolean immersive1 = Prefs.getBoolean(NAVBAR_IMMERSIVE_V1);
                boolean immersive2 = Prefs.getBoolean(NAVBAR_IMMERSIVE_V2);
                boolean immersive3 = Prefs.getBoolean(NAVBAR_IMMERSIVE_V3);

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (fullscreen) {
                        handleFullScreen(true);
                    } else if (immersive1) {
                        handleImmersive(true, 1);
                    } else if (immersive2) {
                        handleImmersive(true, 2);
                    } else if (immersive3) {
                        handleImmersive(true, 3);
                    }
                }, SWITCH_ANIMATION_DELAY);
            }
        });
        binding.nbGcamLagFixContainer.setOnClickListener(v -> {
            nbGcamLagFixClicked.set(true);
            binding.nbGcamLagFix.toggle();
        });

        // Lower Sensitivity
        AtomicBoolean nbLowerSensClicked = new AtomicBoolean(false);
        binding.nbLowerSens.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed() || nbLowerSensClicked.get()) {
                nbLowerSensClicked.set(false);

                if (!SystemUtil.hasStoragePermission()) {
                    SystemUtil.requestStoragePermission(requireContext());
                    binding.nbLowerSens.setChecked(!isChecked);
                    return;
                }

                new Handler(Looper.getMainLooper()).postDelayed(() -> handleLowSensitivity(isChecked), SWITCH_ANIMATION_DELAY);
            }
        });
        binding.nbLowerSensContainer.setOnClickListener(v -> {
            nbLowerSensClicked.set(true);
            binding.nbLowerSens.toggle();
        });

        // Hide Pill
        AtomicBoolean nbHidePillClicked = new AtomicBoolean(false);
        binding.nbHidePill.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed() || nbHidePillClicked.get()) {
                nbHidePillClicked.set(false);

                if (!SystemUtil.hasStoragePermission()) {
                    SystemUtil.requestStoragePermission(requireContext());
                    binding.nbHidePill.setChecked(!isChecked);
                    return;
                }

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    handleHidePill(isChecked);

                    new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::handleSystemUIRestart, 2000);
                }, SWITCH_ANIMATION_DELAY);
            }
        });
        binding.nbHidePillContainer.setOnClickListener(v -> {
            nbHidePillClicked.set(true);
            binding.nbHidePill.toggle();
        });

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
            if (!SystemUtil.hasStoragePermission()) {
                SystemUtil.requestStoragePermission(requireContext());
                return;
            }

            new Handler(Looper.getMainLooper()).post(() -> {
                Prefs.putBoolean(PILL_SHAPE_SWITCH, true);
                Prefs.putInt(FABRICATED_PILL_WIDTH, finalPillWidth[0]);
                Prefs.putInt(FABRICATED_PILL_THICKNESS, finalPillThickness[0]);
                Prefs.putInt(FABRICATED_PILL_BOTTOM_SPACE, finalBottomSpace[0]);

                ResourceManager.buildOverlayWithResource(
                        new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "navigation_home_handle_width", finalPillWidth[0] + "dp"),
                        new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "navigation_handle_radius", finalPillThickness[0] + "dp"),
                        new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "navigation_handle_bottom", finalBottomSpace[0] + "dp")
                );

                binding.pillShape.pillShapeReset.setVisibility(View.VISIBLE);

                new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::restartSystemUI, SWITCH_ANIMATION_DELAY);
            });
        });

        // Reset button
        binding.pillShape.pillShapeReset.setVisibility(Prefs.getBoolean(PILL_SHAPE_SWITCH) ? View.VISIBLE : View.GONE);
        binding.pillShape.pillShapeReset.setOnClickListener(v -> {
            if (!SystemUtil.hasStoragePermission()) {
                SystemUtil.requestStoragePermission(requireContext());
                return;
            }

            new Handler(Looper.getMainLooper()).post(() -> {
                Prefs.putBoolean(PILL_SHAPE_SWITCH, false);

                ResourceManager.removeResourceFromOverlay(
                        new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "navigation_home_handle_width"),
                        new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "navigation_handle_radius"),
                        new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "navigation_handle_bottom")
                );

                binding.pillShape.pillShapeReset.setVisibility(View.GONE);

                new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::restartSystemUI, SWITCH_ANIMATION_DELAY);
            });
        });

        return view;
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

    private void disableOthers(String identifier) {
        if (!Objects.equals(identifier, NAVBAR_FULL_SCREEN)) {
            binding.nbFullscreen.setChecked(false);
        }
        if (!Objects.equals(identifier, NAVBAR_IMMERSIVE_V1)) {
            binding.nbImmersive.setChecked(false);
        }
        if (!Objects.equals(identifier, NAVBAR_IMMERSIVE_V2)) {
            binding.nbImmersivev2.setChecked(false);
        }
        if (!Objects.equals(identifier, NAVBAR_IMMERSIVE_V3)) {
            binding.nbImmersivev3.setChecked(false);
        }
    }

    private void handleFullScreen(boolean enable) {
        Prefs.putBoolean(NAVBAR_FULL_SCREEN, enable);
        boolean gcamLagFix = Prefs.getBoolean(NAVBAR_GCAM_LAG_FIX);

        if (enable) {
            String resource = gcamLagFix ? "1dp" : "0dp";
            ResourceManager.buildOverlayWithResource(
                    new ResourceEntry(FRAMEWORK_PACKAGE, "bool", "config_imeDrawsImeNavBar", "false"),
                    new ResourceEntry(FRAMEWORK_PACKAGE, "dimen", "navigation_bar_height", resource),
                    new ResourceEntry(FRAMEWORK_PACKAGE, "dimen", "navigation_bar_frame_height", resource)
            );
        } else {
            ResourceManager.removeResourceFromOverlay(
                    new ResourceEntry(FRAMEWORK_PACKAGE, "bool", "config_imeDrawsImeNavBar"),
                    new ResourceEntry(FRAMEWORK_PACKAGE, "dimen", "navigation_bar_height"),
                    new ResourceEntry(FRAMEWORK_PACKAGE, "dimen", "navigation_bar_frame_height")
            );
        }
    }

    private void handleImmersive(boolean enable, @IntRange(from = 1, to = 3) int version) {
        if (version == 1) {
            Prefs.putBoolean(NAVBAR_IMMERSIVE_V1, enable);
        } else if (version == 2) {
            Prefs.putBoolean(NAVBAR_IMMERSIVE_V2, enable);
        } else if (version == 3) {
            Prefs.putBoolean(NAVBAR_IMMERSIVE_V3, enable);
        }

        boolean gcamLagFix = Prefs.getBoolean(NAVBAR_GCAM_LAG_FIX);

        if (enable) {
            String resource = gcamLagFix ? "1dp" : "0dp";
            String frameResource = version == 1 ? "48dp" : version == 2 ? "26dp" : "16dp";
            ResourceManager.buildOverlayWithResource(
                    new ResourceEntry(FRAMEWORK_PACKAGE, "bool", "config_imeDrawsImeNavBar", "false"),
                    new ResourceEntry(FRAMEWORK_PACKAGE, "dimen", "navigation_bar_height", resource),
                    new ResourceEntry(FRAMEWORK_PACKAGE, "dimen", "navigation_bar_frame_height", frameResource)
            );
        } else {
            ResourceManager.removeResourceFromOverlay(
                    new ResourceEntry(FRAMEWORK_PACKAGE, "bool", "config_imeDrawsImeNavBar"),
                    new ResourceEntry(FRAMEWORK_PACKAGE, "dimen", "navigation_bar_height"),
                    new ResourceEntry(FRAMEWORK_PACKAGE, "dimen", "navigation_bar_frame_height")
            );
        }
    }

    private void handleLowSensitivity(boolean enable) {
        Prefs.putBoolean(NAVBAR_LOW_SENS, enable);

        if (enable) {
            ResourceManager.buildOverlayWithResource(
                    new ResourceEntry(FRAMEWORK_PACKAGE, "dimen", "navigation_bar_gesture_height", "18dp")
            );
        } else {
            ResourceManager.removeResourceFromOverlay(
                    new ResourceEntry(FRAMEWORK_PACKAGE, "dimen", "navigation_bar_gesture_height")
            );
        }
    }

    private void handleHidePill(boolean enable) {
        Prefs.putBoolean(NAVBAR_HIDE_PILL, enable);

        if (enable) {
            ResourceManager.buildOverlayWithResource(
                    new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "navigation_handle_radius", "0dp"),
                    new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "navigation_home_handle_width", "0dp")
            );
        } else {
            ResourceManager.removeResourceFromOverlay(
                    new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "navigation_handle_radius"),
                    new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "navigation_home_handle_width")
            );
        }
    }
}