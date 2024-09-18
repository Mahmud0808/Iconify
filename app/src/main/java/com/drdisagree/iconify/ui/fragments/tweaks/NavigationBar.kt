package com.drdisagree.iconify.ui.fragments.tweaks

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.annotation.IntRange
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Const.FRAMEWORK_PACKAGE
import com.drdisagree.iconify.common.Const.SWITCH_ANIMATION_DELAY
import com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.common.Preferences.NAVBAR_FULL_SCREEN
import com.drdisagree.iconify.common.Preferences.NAVBAR_GCAM_LAG_FIX
import com.drdisagree.iconify.common.Preferences.NAVBAR_HIDE_PILL
import com.drdisagree.iconify.common.Preferences.NAVBAR_IMMERSIVE_V1
import com.drdisagree.iconify.common.Preferences.NAVBAR_IMMERSIVE_V2
import com.drdisagree.iconify.common.Preferences.NAVBAR_IMMERSIVE_V3
import com.drdisagree.iconify.common.Preferences.NAVBAR_LOW_SENS
import com.drdisagree.iconify.common.Preferences.PILL_SHAPE_SWITCH
import com.drdisagree.iconify.common.References.FABRICATED_PILL_BOTTOM_SPACE
import com.drdisagree.iconify.common.References.FABRICATED_PILL_THICKNESS
import com.drdisagree.iconify.common.References.FABRICATED_PILL_WIDTH
import com.drdisagree.iconify.config.RPrefs.getBoolean
import com.drdisagree.iconify.config.RPrefs.getInt
import com.drdisagree.iconify.config.RPrefs.putBoolean
import com.drdisagree.iconify.config.RPrefs.putInt
import com.drdisagree.iconify.databinding.FragmentNavigationBarBinding
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader
import com.drdisagree.iconify.utils.SystemUtils
import com.drdisagree.iconify.utils.SystemUtils.hasStoragePermission
import com.drdisagree.iconify.utils.SystemUtils.requestStoragePermission
import com.drdisagree.iconify.utils.SystemUtils.restartSystemUI
import com.drdisagree.iconify.utils.overlay.OverlayUtils
import com.drdisagree.iconify.utils.overlay.OverlayUtils.enableOverlay
import com.drdisagree.iconify.utils.overlay.manager.resource.ResourceEntry
import com.drdisagree.iconify.utils.overlay.manager.resource.ResourceManager.buildOverlayWithResource
import com.drdisagree.iconify.utils.overlay.manager.resource.ResourceManager.removeResourceFromOverlay
import com.google.android.material.slider.Slider
import com.topjohnwu.superuser.Shell
import java.util.concurrent.atomic.AtomicBoolean

class NavigationBar : BaseFragment() {

    private lateinit var binding: FragmentNavigationBarBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNavigationBarBinding.inflate(inflater, container, false)
        val view: View = binding.getRoot()

        // Header
        setHeader(
            requireContext(),
            getParentFragmentManager(),
            binding.header.toolbar,
            R.string.activity_title_navigation_bar
        )

        // Switch states
        binding.nbFullscreen.isSwitchChecked = getBoolean(NAVBAR_FULL_SCREEN)
        binding.nbImmersive.isSwitchChecked = getBoolean(NAVBAR_IMMERSIVE_V1)
        binding.nbImmersiveV2.isSwitchChecked = getBoolean(NAVBAR_IMMERSIVE_V2)
        binding.nbImmersiveV3.isSwitchChecked = getBoolean(NAVBAR_IMMERSIVE_V3)
        binding.nbGcamLagFix.isSwitchChecked = getBoolean(NAVBAR_GCAM_LAG_FIX)
        binding.nbLowerSens.isSwitchChecked = getBoolean(NAVBAR_LOW_SENS)
        binding.nbHidePill.isSwitchChecked = getBoolean(NAVBAR_HIDE_PILL)
        binding.nbMonetPill.isSwitchChecked =
            getBoolean("IconifyComponentNBMonetPill.overlay")
        binding.nbHideKbButtons.isSwitchChecked =
            getBoolean("IconifyComponentNBHideKBButton.overlay")

        binding.nbDisableLeftGesture.isSwitchChecked = initializeLeftGestureSwitch()
        binding.nbDisableRightGesture.isSwitchChecked = initializeRightGestureSwitch()

        binding.nbHidePill.setEnabled(!binding.nbFullscreen.isSwitchChecked)
        binding.nbMonetPill.setEnabled(!binding.nbHidePill.isSwitchChecked && !binding.nbFullscreen.isSwitchChecked)

        // Fullscreen
        val nbFullScreenClicked = AtomicBoolean(false)

        binding.nbFullscreen.setSwitchChangeListener { buttonView: CompoundButton, isSwitchChecked: Boolean ->
            if (buttonView.isPressed || nbFullScreenClicked.get()) {
                nbFullScreenClicked.set(false)

                if (!hasStoragePermission()) {
                    requestStoragePermission(requireContext())
                    binding.nbFullscreen.isSwitchChecked = !isSwitchChecked

                    return@setSwitchChangeListener
                }

                binding.nbHidePill.setEnabled(!isSwitchChecked)
                binding.nbMonetPill.setEnabled(!isSwitchChecked && !binding.nbHidePill.isSwitchChecked)

                disableOthers(NAVBAR_FULL_SCREEN)

                if (isSwitchChecked) {
                    binding.pillShape.pillShapeContainer.visibility = View.GONE
                } else {
                    binding.pillShape.pillShapeContainer.visibility = View.VISIBLE
                }

                Handler(Looper.getMainLooper()).postDelayed(
                    { handleFullScreen(isSwitchChecked) },
                    SWITCH_ANIMATION_DELAY
                )
            }
        }
        binding.nbFullscreen.setBeforeSwitchChangeListener {
            nbFullScreenClicked.set(
                true
            )
        }

        // Immersive
        val nbImmersiveClicked = AtomicBoolean(false)

        binding.nbImmersive.setSwitchChangeListener { buttonView: CompoundButton, isSwitchChecked: Boolean ->
            if (buttonView.isPressed || nbImmersiveClicked.get()) {
                nbImmersiveClicked.set(false)

                if (!hasStoragePermission()) {
                    requestStoragePermission(requireContext())
                    binding.nbImmersive.isSwitchChecked = !isSwitchChecked

                    return@setSwitchChangeListener
                }

                disableOthers(NAVBAR_IMMERSIVE_V1)

                Handler(Looper.getMainLooper()).postDelayed(
                    { handleImmersive(isSwitchChecked, 1) },
                    SWITCH_ANIMATION_DELAY
                )
            }
        }
        binding.nbImmersive.setBeforeSwitchChangeListener {
            nbImmersiveClicked.set(
                true
            )
        }

        // Immersive V2
        val nbImmersiveV2Clicked = AtomicBoolean(false)
        binding.nbImmersiveV2.setSwitchChangeListener { buttonView: CompoundButton, isSwitchChecked: Boolean ->
            if (buttonView.isPressed || nbImmersiveV2Clicked.get()) {
                nbImmersiveV2Clicked.set(false)

                if (!hasStoragePermission()) {
                    requestStoragePermission(requireContext())
                    binding.nbImmersiveV2.isSwitchChecked = !isSwitchChecked

                    return@setSwitchChangeListener
                }

                disableOthers(NAVBAR_IMMERSIVE_V2)

                Handler(Looper.getMainLooper()).postDelayed(
                    { handleImmersive(isSwitchChecked, 2) },
                    SWITCH_ANIMATION_DELAY
                )
            }
        }
        binding.nbImmersiveV2.setBeforeSwitchChangeListener {
            nbImmersiveV2Clicked.set(
                true
            )
        }

        // Immersive V3
        val nbImmersiveV3Clicked = AtomicBoolean(false)
        binding.nbImmersiveV3.setSwitchChangeListener { buttonView: CompoundButton, isSwitchChecked: Boolean ->
            if (buttonView.isPressed || nbImmersiveV3Clicked.get()) {
                nbImmersiveV3Clicked.set(false)

                if (!hasStoragePermission()) {
                    requestStoragePermission(requireContext())
                    binding.nbImmersiveV3.isSwitchChecked = !isSwitchChecked

                    return@setSwitchChangeListener
                }

                disableOthers(NAVBAR_IMMERSIVE_V3)

                Handler(Looper.getMainLooper()).postDelayed(
                    { handleImmersive(isSwitchChecked, 3) },
                    SWITCH_ANIMATION_DELAY
                )
            }
        }
        binding.nbImmersiveV3.setBeforeSwitchChangeListener {
            nbImmersiveV3Clicked.set(
                true
            )
        }

        // GCam Lag Fix
        val nbGcamLagFixClicked = AtomicBoolean(false)
        binding.nbGcamLagFix.setSwitchChangeListener { buttonView: CompoundButton, isSwitchChecked: Boolean ->
            if (buttonView.isPressed || nbGcamLagFixClicked.get()) {
                nbGcamLagFixClicked.set(false)

                if (!hasStoragePermission()) {
                    requestStoragePermission(requireContext())
                    binding.nbGcamLagFix.isSwitchChecked = !isSwitchChecked
                    return@setSwitchChangeListener
                }

                putBoolean(NAVBAR_GCAM_LAG_FIX, isSwitchChecked)

                val fullscreen = getBoolean(NAVBAR_FULL_SCREEN)
                val immersive1 = getBoolean(NAVBAR_IMMERSIVE_V1)
                val immersive2 = getBoolean(NAVBAR_IMMERSIVE_V2)
                val immersive3 = getBoolean(NAVBAR_IMMERSIVE_V3)

                Handler(Looper.getMainLooper()).postDelayed({
                    if (fullscreen) {
                        handleFullScreen(true)
                    } else if (immersive1) {
                        handleImmersive(true, 1)
                    } else if (immersive2) {
                        handleImmersive(true, 2)
                    } else if (immersive3) {
                        handleImmersive(true, 3)
                    }
                }, SWITCH_ANIMATION_DELAY)
            }
        }
        binding.nbGcamLagFix.setBeforeSwitchChangeListener {
            nbGcamLagFixClicked.set(
                true
            )
        }

        // Lower Sensitivity
        val nbLowerSensClicked = AtomicBoolean(false)

        binding.nbLowerSens.setSwitchChangeListener { buttonView: CompoundButton, isSwitchChecked: Boolean ->
            if (buttonView.isPressed || nbLowerSensClicked.get()) {
                nbLowerSensClicked.set(false)

                if (!hasStoragePermission()) {
                    requestStoragePermission(requireContext())
                    binding.nbLowerSens.isSwitchChecked = !isSwitchChecked

                    return@setSwitchChangeListener
                }

                Handler(Looper.getMainLooper()).postDelayed(
                    { handleLowSensitivity(isSwitchChecked) },
                    SWITCH_ANIMATION_DELAY
                )
            }
        }
        binding.nbLowerSens.setBeforeSwitchChangeListener {
            nbLowerSensClicked.set(
                true
            )
        }

        // Hide Pill
        val nbHidePillClicked = AtomicBoolean(false)

        binding.nbHidePill.setSwitchChangeListener { buttonView: CompoundButton, isSwitchChecked: Boolean ->
            if (buttonView.isPressed || nbHidePillClicked.get()) {
                nbHidePillClicked.set(false)

                if (!hasStoragePermission()) {
                    requestStoragePermission(requireContext())
                    binding.nbHidePill.isSwitchChecked = !isSwitchChecked

                    return@setSwitchChangeListener
                }

                Handler(Looper.getMainLooper()).postDelayed({
                    handleHidePill(isSwitchChecked)

                    Handler(Looper.getMainLooper()).postDelayed(
                        { SystemUtils.handleSystemUIRestart() },
                        2000
                    )
                }, SWITCH_ANIMATION_DELAY)
            }
        }
        binding.nbHidePill.setBeforeSwitchChangeListener {
            nbHidePillClicked.set(
                true
            )
        }

        // Monet Pill
        binding.nbMonetPill.setSwitchChangeListener { _: CompoundButton?, isSwitchChecked: Boolean ->
            Handler(Looper.getMainLooper()).postDelayed(
                {
                    if (isSwitchChecked) {
                        enableOverlay("IconifyComponentNBMonetPill.overlay")
                        restartSystemUI()
                    } else {
                        OverlayUtils.disableOverlay("IconifyComponentNBMonetPill.overlay")
                        restartSystemUI()
                    }
                }, SWITCH_ANIMATION_DELAY
            )
        }

        // Hide Keyboard Buttons
        binding.nbHideKbButtons.setSwitchChangeListener { _: CompoundButton?, isSwitchChecked: Boolean ->
            Handler(Looper.getMainLooper()).postDelayed(
                {
                    if (isSwitchChecked) {
                        enableOverlay("IconifyComponentNBHideKBButton.overlay")
                    } else {
                        OverlayUtils.disableOverlay("IconifyComponentNBHideKBButton.overlay")
                    }
                }, SWITCH_ANIMATION_DELAY
            )
        }

        // Disable left gesture
        binding.nbDisableLeftGesture.setSwitchChangeListener { _: CompoundButton?, isSwitchChecked: Boolean ->
            Handler(Looper.getMainLooper()).postDelayed(
                {
                    if (isSwitchChecked) {
                        Shell.cmd(
                            "settings put secure back_gesture_inset_scale_left -1 &>/dev/null"
                        ).exec()
                    } else {
                        Shell.cmd(
                            "settings delete secure back_gesture_inset_scale_left &>/dev/null"
                        ).exec()
                    }
                }, SWITCH_ANIMATION_DELAY
            )
        }

        // Disable right gesture
        binding.nbDisableRightGesture.setSwitchChangeListener { _: CompoundButton?, isSwitchChecked: Boolean ->
            Handler(Looper.getMainLooper()).postDelayed(
                {
                    if (isSwitchChecked) {
                        Shell.cmd(
                            "settings put secure back_gesture_inset_scale_right -1 &>/dev/null"
                        ).exec()
                    } else {
                        Shell.cmd(
                            "settings delete secure back_gesture_inset_scale_right &>/dev/null"
                        ).exec()
                    }
                }, SWITCH_ANIMATION_DELAY
            )
        }

        // Pill shape
        binding.pillShape.pillShapeContainer.visibility =
            if (binding.nbFullscreen.isSwitchChecked || binding.nbHidePill.isSwitchChecked) {
                View.GONE
            } else {
                View.VISIBLE
            }

        // Pill width
        val finalPillWidth = intArrayOf(getInt(FABRICATED_PILL_WIDTH, 108))
        binding.pillShape.pillWidth.sliderValue = finalPillWidth[0]
        binding.pillShape.pillWidth.setOnSliderTouchListener(object :
            Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}

            override fun onStopTrackingTouch(slider: Slider) {
                finalPillWidth[0] = slider.value.toInt()
            }
        })

        // Pill thickness
        val finalPillThickness = intArrayOf(getInt(FABRICATED_PILL_THICKNESS, 2))
        binding.pillShape.pillThickness.sliderValue = finalPillThickness[0]
        binding.pillShape.pillThickness.setOnSliderTouchListener(object :
            Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}

            override fun onStopTrackingTouch(slider: Slider) {
                finalPillThickness[0] = slider.value.toInt()
            }
        })

        // Bottom space
        val finalBottomSpace = intArrayOf(getInt(FABRICATED_PILL_BOTTOM_SPACE, 6))
        binding.pillShape.pillBottomSpace.sliderValue = finalBottomSpace[0]
        binding.pillShape.pillBottomSpace.setOnSliderTouchListener(object :
            Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}

            override fun onStopTrackingTouch(slider: Slider) {
                finalBottomSpace[0] = slider.value.toInt()
            }
        })

        // Apply button
        binding.pillShape.pillShapeApply.setOnClickListener {
            if (!hasStoragePermission()) {
                requestStoragePermission(requireContext())

                return@setOnClickListener
            }

            Handler(Looper.getMainLooper()).post {
                putBoolean(PILL_SHAPE_SWITCH, true)

                putInt(FABRICATED_PILL_WIDTH, finalPillWidth[0])
                putInt(FABRICATED_PILL_THICKNESS, finalPillThickness[0])
                putInt(FABRICATED_PILL_BOTTOM_SPACE, finalBottomSpace[0])

                buildOverlayWithResource(
                    ResourceEntry(
                        SYSTEMUI_PACKAGE,
                        "dimen",
                        "navigation_home_handle_width",
                        finalPillWidth[0].toString() + "dp"
                    ),
                    ResourceEntry(
                        SYSTEMUI_PACKAGE,
                        "dimen",
                        "navigation_handle_radius",
                        finalPillThickness[0].toString() + "dp"
                    ),
                    ResourceEntry(
                        SYSTEMUI_PACKAGE,
                        "dimen",
                        "navigation_handle_bottom",
                        finalBottomSpace[0].toString() + "dp"
                    )
                )

                binding.pillShape.pillShapeReset.visibility = View.VISIBLE

                Handler(Looper.getMainLooper()).postDelayed(
                    { restartSystemUI() },
                    SWITCH_ANIMATION_DELAY
                )
            }
        }

        // Reset button
        binding.pillShape.pillShapeReset.visibility =
            if (getBoolean(PILL_SHAPE_SWITCH)) {
                View.VISIBLE
            } else {
                View.GONE
            }
        binding.pillShape.pillShapeReset.setOnClickListener {
            if (!hasStoragePermission()) {
                requestStoragePermission(requireContext())

                return@setOnClickListener
            }

            Handler(Looper.getMainLooper()).post {
                putBoolean(PILL_SHAPE_SWITCH, false)

                removeResourceFromOverlay(
                    ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "navigation_home_handle_width"),
                    ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "navigation_handle_radius"),
                    ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "navigation_handle_bottom")
                )

                binding.pillShape.pillShapeReset.visibility = View.GONE

                Handler(Looper.getMainLooper()).postDelayed(
                    { restartSystemUI() },
                    SWITCH_ANIMATION_DELAY
                )
            }
        }
        return view
    }

    private fun initializeLeftGestureSwitch(): Boolean {
        return try {
            Shell.cmd(
                "settings get secure back_gesture_inset_scale_left"
            ).exec().out[0].toInt() == -1
        } catch (ignored: Exception) {
            false
        }
    }

    private fun initializeRightGestureSwitch(): Boolean {
        return try {
            Shell.cmd(
                "settings get secure back_gesture_inset_scale_right"
            ).exec().out[0].toInt() == -1
        } catch (ignored: Exception) {
            false
        }
    }

    private fun disableOthers(identifier: String) {
        if (identifier != NAVBAR_FULL_SCREEN) {
            putBoolean(NAVBAR_FULL_SCREEN, false)
            binding.nbFullscreen.isSwitchChecked = false
        }

        if (identifier != NAVBAR_IMMERSIVE_V1) {
            putBoolean(NAVBAR_IMMERSIVE_V1, false)
            binding.nbImmersive.isSwitchChecked = false
        }

        if (identifier != NAVBAR_IMMERSIVE_V2) {
            putBoolean(NAVBAR_IMMERSIVE_V2, false)
            binding.nbImmersiveV2.isSwitchChecked = false
        }

        if (identifier != NAVBAR_IMMERSIVE_V3) {
            putBoolean(NAVBAR_IMMERSIVE_V3, false)
            binding.nbImmersiveV3.isSwitchChecked = false
        }
    }

    private fun handleFullScreen(enable: Boolean) {
        putBoolean(NAVBAR_FULL_SCREEN, enable)

        val gcamLagFix = getBoolean(NAVBAR_GCAM_LAG_FIX)

        if (enable) {
            val resource = if (gcamLagFix) "1dp" else "0dp"

            buildOverlayWithResource(
                ResourceEntry(FRAMEWORK_PACKAGE, "bool", "config_imeDrawsImeNavBar", "false"),
                ResourceEntry(FRAMEWORK_PACKAGE, "dimen", "navigation_bar_height", resource),
                ResourceEntry(FRAMEWORK_PACKAGE, "dimen", "navigation_bar_frame_height", resource)
            )
        } else {
            removeResourceFromOverlay(
                ResourceEntry(FRAMEWORK_PACKAGE, "bool", "config_imeDrawsImeNavBar"),
                ResourceEntry(FRAMEWORK_PACKAGE, "dimen", "navigation_bar_height"),
                ResourceEntry(FRAMEWORK_PACKAGE, "dimen", "navigation_bar_frame_height")
            )
        }
    }

    private fun handleImmersive(enable: Boolean, @IntRange(from = 1, to = 3) version: Int) {
        when (version) {
            1 -> {
                putBoolean(NAVBAR_IMMERSIVE_V1, enable)
            }

            2 -> {
                putBoolean(NAVBAR_IMMERSIVE_V2, enable)
            }

            3 -> {
                putBoolean(NAVBAR_IMMERSIVE_V3, enable)
            }
        }

        val gcamLagFix = getBoolean(NAVBAR_GCAM_LAG_FIX)

        if (enable) {
            val resource = if (gcamLagFix) "1dp" else "0dp"
            val frameResource = if (version == 1) "48dp" else if (version == 2) "26dp" else "16dp"

            buildOverlayWithResource(
                ResourceEntry(
                    FRAMEWORK_PACKAGE,
                    "bool",
                    "config_imeDrawsImeNavBar",
                    "false"
                ),
                ResourceEntry(
                    FRAMEWORK_PACKAGE,
                    "dimen",
                    "navigation_bar_height",
                    resource
                ),
                ResourceEntry(
                    FRAMEWORK_PACKAGE,
                    "dimen",
                    "navigation_bar_frame_height",
                    frameResource
                )
            )
        } else {
            removeResourceFromOverlay(
                ResourceEntry(
                    FRAMEWORK_PACKAGE,
                    "bool",
                    "config_imeDrawsImeNavBar"
                ),
                ResourceEntry(
                    FRAMEWORK_PACKAGE,
                    "dimen",
                    "navigation_bar_height"
                ),
                ResourceEntry(
                    FRAMEWORK_PACKAGE,
                    "dimen",
                    "navigation_bar_frame_height"
                )
            )
        }
    }

    private fun handleLowSensitivity(enable: Boolean) {
        putBoolean(NAVBAR_LOW_SENS, enable)
        if (enable) {
            buildOverlayWithResource(
                ResourceEntry(
                    FRAMEWORK_PACKAGE,
                    "dimen",
                    "navigation_bar_gesture_height",
                    "18dp"
                )
            )
        } else {
            removeResourceFromOverlay(
                ResourceEntry(
                    FRAMEWORK_PACKAGE,
                    "dimen",
                    "navigation_bar_gesture_height"
                )
            )
        }
    }

    private fun handleHidePill(enable: Boolean) {
        putBoolean(NAVBAR_HIDE_PILL, enable)
        if (enable) {
            buildOverlayWithResource(
                ResourceEntry(
                    SYSTEMUI_PACKAGE,
                    "dimen",
                    "navigation_handle_radius",
                    "0dp"
                ),
                ResourceEntry(
                    SYSTEMUI_PACKAGE,
                    "dimen",
                    "navigation_home_handle_width",
                    "0dp"
                )
            )
        } else {
            removeResourceFromOverlay(
                ResourceEntry(
                    SYSTEMUI_PACKAGE,
                    "dimen",
                    "navigation_handle_radius"
                ),
                ResourceEntry(
                    SYSTEMUI_PACKAGE,
                    "dimen",
                    "navigation_home_handle_width"
                )
            )
        }
    }
}