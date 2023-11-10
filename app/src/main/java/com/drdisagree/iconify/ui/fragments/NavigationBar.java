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
        binding.nbFullscreen.setSwitchChecked(Prefs.getBoolean(NAVBAR_FULL_SCREEN));
        binding.nbImmersive.setSwitchChecked(Prefs.getBoolean(NAVBAR_IMMERSIVE_V1));
        binding.nbImmersiveV2.setSwitchChecked(Prefs.getBoolean(NAVBAR_IMMERSIVE_V2));
        binding.nbImmersiveV3.setSwitchChecked(Prefs.getBoolean(NAVBAR_IMMERSIVE_V3));
        binding.nbGcamLagFix.setSwitchChecked(Prefs.getBoolean(NAVBAR_GCAM_LAG_FIX));
        binding.nbLowerSens.setSwitchChecked(Prefs.getBoolean(NAVBAR_LOW_SENS));
        binding.nbHidePill.setSwitchChecked(Prefs.getBoolean(NAVBAR_HIDE_PILL));
        binding.nbMonetPill.setSwitchChecked(Prefs.getBoolean("IconifyComponentNBMonetPill.overlay"));
        binding.nbHideKbButtons.setSwitchChecked(Prefs.getBoolean("IconifyComponentNBHideKBButton.overlay"));
        binding.nbDisableLeftGesture.setSwitchChecked(initialize_left_gesture_switch());
        binding.nbDisableRightGesture.setSwitchChecked(initialize_right_gesture_switch());
        binding.nbHidePill.setEnabled(!binding.nbFullscreen.isSwitchChecked());
        binding.nbMonetPill.setEnabled(!binding.nbHidePill.isSwitchChecked() && !binding.nbFullscreen.isSwitchChecked());

        // Fullscreen
        AtomicBoolean nbFullScreenClicked = new AtomicBoolean(false);
        binding.nbFullscreen.setSwitchChangeListener((buttonView, isSwitchChecked) -> {
            if (buttonView.isPressed() || nbFullScreenClicked.get()) {
                nbFullScreenClicked.set(false);

                if (!SystemUtil.hasStoragePermission()) {
                    SystemUtil.requestStoragePermission(requireContext());
                    binding.nbFullscreen.setSwitchChecked(!isSwitchChecked);
                    return;
                }

                binding.nbHidePill.setEnabled(!isSwitchChecked);
                binding.nbMonetPill.setEnabled(!isSwitchChecked && !binding.nbHidePill.isSwitchChecked());
                disableOthers(NAVBAR_FULL_SCREEN);

                if (isSwitchChecked) {
                    binding.pillShape.pillShapeContainer.setVisibility(View.GONE);
                } else {
                    binding.pillShape.pillShapeContainer.setVisibility(View.VISIBLE);
                }

                new Handler(Looper.getMainLooper()).postDelayed(() -> handleFullScreen(isSwitchChecked), SWITCH_ANIMATION_DELAY);
            }
        });
        binding.nbFullscreen.setBeforeSwitchChangeListener(() -> nbFullScreenClicked.set(true));

        // Immersive
        AtomicBoolean nbImmersiveClicked = new AtomicBoolean(false);
        binding.nbImmersive.setSwitchChangeListener((buttonView, isSwitchChecked) -> {
            if (buttonView.isPressed() || nbImmersiveClicked.get()) {
                nbImmersiveClicked.set(false);

                if (!SystemUtil.hasStoragePermission()) {
                    SystemUtil.requestStoragePermission(requireContext());
                    binding.nbImmersive.setSwitchChecked(!isSwitchChecked);
                    return;
                }

                disableOthers(NAVBAR_IMMERSIVE_V1);

                new Handler(Looper.getMainLooper()).postDelayed(() -> handleImmersive(isSwitchChecked, 1), SWITCH_ANIMATION_DELAY);
            }
        });
        binding.nbImmersive.setBeforeSwitchChangeListener(() -> nbImmersiveClicked.set(true));

        // Immersive V2
        AtomicBoolean nbImmersiveV2Clicked = new AtomicBoolean(false);
        binding.nbImmersiveV2.setSwitchChangeListener((buttonView, isSwitchChecked) -> {
            if (buttonView.isPressed() || nbImmersiveV2Clicked.get()) {
                nbImmersiveV2Clicked.set(false);

                if (!SystemUtil.hasStoragePermission()) {
                    SystemUtil.requestStoragePermission(requireContext());
                    binding.nbImmersiveV2.setSwitchChecked(!isSwitchChecked);
                    return;
                }

                disableOthers(NAVBAR_IMMERSIVE_V2);

                new Handler(Looper.getMainLooper()).postDelayed(() -> handleImmersive(isSwitchChecked, 2), SWITCH_ANIMATION_DELAY);
            }
        });
        binding.nbImmersiveV2.setBeforeSwitchChangeListener(() -> nbImmersiveV2Clicked.set(true));

        // Immersive V3
        AtomicBoolean nbImmersiveV3Clicked = new AtomicBoolean(false);
        binding.nbImmersiveV3.setSwitchChangeListener((buttonView, isSwitchChecked) -> {
            if (buttonView.isPressed() || nbImmersiveV3Clicked.get()) {
                nbImmersiveV3Clicked.set(false);

                if (!SystemUtil.hasStoragePermission()) {
                    SystemUtil.requestStoragePermission(requireContext());
                    binding.nbImmersiveV3.setSwitchChecked(!isSwitchChecked);
                    return;
                }

                disableOthers(NAVBAR_IMMERSIVE_V3);

                new Handler(Looper.getMainLooper()).postDelayed(() -> handleImmersive(isSwitchChecked, 3), SWITCH_ANIMATION_DELAY);
            }
        });
        binding.nbImmersiveV3.setBeforeSwitchChangeListener(() -> nbImmersiveV3Clicked.set(true));

        // GCam Lag Fix
        AtomicBoolean nbGcamLagFixClicked = new AtomicBoolean(false);
        binding.nbGcamLagFix.setSwitchChangeListener((buttonView, isSwitchChecked) -> {
            if (buttonView.isPressed() || nbGcamLagFixClicked.get()) {
                nbGcamLagFixClicked.set(false);

                if (!SystemUtil.hasStoragePermission()) {
                    SystemUtil.requestStoragePermission(requireContext());
                    binding.nbGcamLagFix.setSwitchChecked(!isSwitchChecked);
                    return;
                }

                Prefs.putBoolean(NAVBAR_GCAM_LAG_FIX, isSwitchChecked);
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
        binding.nbGcamLagFix.setBeforeSwitchChangeListener(() -> nbGcamLagFixClicked.set(true));

        // Lower Sensitivity
        AtomicBoolean nbLowerSensClicked = new AtomicBoolean(false);
        binding.nbLowerSens.setSwitchChangeListener((buttonView, isSwitchChecked) -> {
            if (buttonView.isPressed() || nbLowerSensClicked.get()) {
                nbLowerSensClicked.set(false);

                if (!SystemUtil.hasStoragePermission()) {
                    SystemUtil.requestStoragePermission(requireContext());
                    binding.nbLowerSens.setSwitchChecked(!isSwitchChecked);
                    return;
                }

                new Handler(Looper.getMainLooper()).postDelayed(() -> handleLowSensitivity(isSwitchChecked), SWITCH_ANIMATION_DELAY);
            }
        });
        binding.nbLowerSens.setBeforeSwitchChangeListener(() -> nbLowerSensClicked.set(true));

        // Hide Pill
        AtomicBoolean nbHidePillClicked = new AtomicBoolean(false);
        binding.nbHidePill.setSwitchChangeListener((buttonView, isSwitchChecked) -> {
            if (buttonView.isPressed() || nbHidePillClicked.get()) {
                nbHidePillClicked.set(false);

                if (!SystemUtil.hasStoragePermission()) {
                    SystemUtil.requestStoragePermission(requireContext());
                    binding.nbHidePill.setSwitchChecked(!isSwitchChecked);
                    return;
                }

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    handleHidePill(isSwitchChecked);

                    new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::handleSystemUIRestart, 2000);
                }, SWITCH_ANIMATION_DELAY);
            }
        });
        binding.nbHidePill.setBeforeSwitchChangeListener(() -> nbHidePillClicked.set(true));

        // Monet Pill
        binding.nbMonetPill.setSwitchChangeListener((buttonView, isSwitchChecked) -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isSwitchChecked) {
                OverlayUtil.enableOverlay("IconifyComponentNBMonetPill.overlay");
                SystemUtil.restartSystemUI();
            } else {
                OverlayUtil.disableOverlay("IconifyComponentNBMonetPill.overlay");
                SystemUtil.restartSystemUI();
            }
        }, SWITCH_ANIMATION_DELAY));

        // Hide Keyboard Buttons
        binding.nbHideKbButtons.setSwitchChangeListener((buttonView, isSwitchChecked) -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isSwitchChecked) {
                OverlayUtil.enableOverlay("IconifyComponentNBHideKBButton.overlay");
            } else {
                OverlayUtil.disableOverlay("IconifyComponentNBHideKBButton.overlay");
            }
        }, SWITCH_ANIMATION_DELAY));

        // Disable left gesture
        binding.nbDisableLeftGesture.setSwitchChangeListener((buttonView, isSwitchChecked) -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isSwitchChecked) {
                Shell.cmd("settings put secure back_gesture_inset_scale_left -1 &>/dev/null").exec();
            } else {
                Shell.cmd("settings delete secure back_gesture_inset_scale_left &>/dev/null").exec();
            }
        }, SWITCH_ANIMATION_DELAY));

        // Disable right gesture
        binding.nbDisableRightGesture.setSwitchChangeListener((buttonView, isSwitchChecked) -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isSwitchChecked) {
                Shell.cmd("settings put secure back_gesture_inset_scale_right -1 &>/dev/null").exec();
            } else {
                Shell.cmd("settings delete secure back_gesture_inset_scale_right &>/dev/null").exec();
            }
        }, SWITCH_ANIMATION_DELAY));

        // Pill shape
        binding.pillShape.pillShapeContainer.setVisibility((binding.nbFullscreen.isSwitchChecked() || binding.nbHidePill.isSwitchChecked()) ? View.GONE : View.VISIBLE);

        // Pill width
        final int[] finalPillWidth = {Prefs.getInt(FABRICATED_PILL_WIDTH, 108)};
        binding.pillShape.pillWidth.setSliderValue(finalPillWidth[0]);
        binding.pillShape.pillWidth.setOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                finalPillWidth[0] = (int) slider.getValue();
            }
        });

        // Pill thickness
        final int[] finalPillThickness = {Prefs.getInt(FABRICATED_PILL_THICKNESS, 2)};
        binding.pillShape.pillThickness.setSliderValue(finalPillThickness[0]);
        binding.pillShape.pillThickness.setOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                finalPillThickness[0] = (int) slider.getValue();
            }
        });

        // Bottom space
        final int[] finalBottomSpace = {Prefs.getInt(FABRICATED_PILL_BOTTOM_SPACE, 6)};
        binding.pillShape.pillBottomSpace.setSliderValue(finalBottomSpace[0]);
        binding.pillShape.pillBottomSpace.setOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                finalBottomSpace[0] = (int) slider.getValue();
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
            Prefs.putBoolean(NAVBAR_FULL_SCREEN, false);
            binding.nbFullscreen.setSwitchChecked(false);
        }
        if (!Objects.equals(identifier, NAVBAR_IMMERSIVE_V1)) {
            Prefs.putBoolean(NAVBAR_IMMERSIVE_V1, false);
            binding.nbImmersive.setSwitchChecked(false);
        }
        if (!Objects.equals(identifier, NAVBAR_IMMERSIVE_V2)) {
            Prefs.putBoolean(NAVBAR_IMMERSIVE_V2, false);
            binding.nbImmersiveV2.setSwitchChecked(false);
        }
        if (!Objects.equals(identifier, NAVBAR_IMMERSIVE_V3)) {
            Prefs.putBoolean(NAVBAR_IMMERSIVE_V3, false);
            binding.nbImmersiveV3.setSwitchChecked(false);
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