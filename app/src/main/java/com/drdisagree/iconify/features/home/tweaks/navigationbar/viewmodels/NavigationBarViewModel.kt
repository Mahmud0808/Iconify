package com.drdisagree.iconify.features.home.tweaks.navigationbar.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drdisagree.iconify.core.preferences.PreferenceController
import com.drdisagree.iconify.core.utils.AppUtils
import com.drdisagree.iconify.core.utils.overlay.OverlayUtils
import com.drdisagree.iconify.core.utils.overlay.resource.ResourceEntry
import com.drdisagree.iconify.core.utils.overlay.resource.ResourceManager
import com.drdisagree.iconify.data.common.Const.FRAMEWORK_PACKAGE
import com.drdisagree.iconify.data.common.Const.LAUNCHER3_PACKAGE
import com.drdisagree.iconify.data.common.Const.PIXEL_LAUNCHER_PACKAGE
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.events.ToastUiEvent
import com.drdisagree.iconify.data.keys.TweaksKey
import com.drdisagree.iconify.features.home.tweaks.navigationbar.models.DisplayMode
import com.topjohnwu.superuser.Shell
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class NavigationBarViewModel @Inject constructor(
    private val prefController: PreferenceController
) : ViewModel() {

    private val tag = "NavigationBarViewModel"

    private val navigationBarMonetPillOverlayPackageName1 = "IconifyComponentNBMP1.overlay"
    private val navigationBarMonetPillOverlayPackageName2 = "IconifyComponentNBMP2.overlay"

    private val navigationBarDisplayModeFullScreenId = "navigation_bar_display_mode_full_screen"
    private val navigationBarDisplayModeImmersiveId = "navigation_bar_display_mode_immersive"
    private val navigationBarHideKeyboardButtonsId = "navigation_bar_hide_keyboard_buttons"
    private val navigationBarLowerSensitivityId = "navigation_bar_lower_sensitivity"
    private val navigationBarHidePillId = "navigation_bar_hide_pill"
    private val navigationBarPillAppearanceId = "navigation_bar_pill_appearance"

    private fun displayModeFullScreenResourceEntries(
        barHeight: String,
        frameHeight: String
    ) = listOf(
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
            barHeight
        ),
        ResourceEntry(
            FRAMEWORK_PACKAGE,
            "dimen",
            "navigation_bar_frame_height",
            frameHeight
        ),
        ResourceEntry(
            FRAMEWORK_PACKAGE,
            "dimen",
            "navigation_bar_width",
            "0dp"
        ),
        ResourceEntry(
            FRAMEWORK_PACKAGE,
            "dimen",
            "navigation_bar_height_portrait",
            barHeight
        ),
        ResourceEntry(
            FRAMEWORK_PACKAGE,
            "dimen",
            "navigation_bar_height_landscape",
            barHeight
        ),
        ResourceEntry(
            FRAMEWORK_PACKAGE,
            "dimen",
            "navigation_bar_frame_height_landscape",
            frameHeight
        ),
        ResourceEntry(
            FRAMEWORK_PACKAGE,
            "bool",
            "config_allowSeamlessRotationDespiteNavBarMoving",
            "true"
        ),
        ResourceEntry(
            FRAMEWORK_PACKAGE,
            "bool",
            "config_navBarAlwaysShowOnSideEdgeGesture",
            "true"
        ),
        ResourceEntry(
            FRAMEWORK_PACKAGE,
            "bool",
            "config_navBarCanMove",
            "false"
        ),
        ResourceEntry(
            FRAMEWORK_PACKAGE,
            "bool",
            "config_navBarTapThrough",
            "true"
        ),
        ResourceEntry(
            FRAMEWORK_PACKAGE,
            "dimen",
            "config_backGestureInset",
            "24dp"
        ),
        ResourceEntry(
            FRAMEWORK_PACKAGE,
            "dimen",
            "navigation_bar_gesture_height",
            "24dp"
        ),
        ResourceEntry(
            PIXEL_LAUNCHER_PACKAGE,
            "dimen",
            "taskbar_stashed_handle_height",
            "0dp"
        ),
        ResourceEntry(
            LAUNCHER3_PACKAGE,
            "dimen",
            "taskbar_stashed_handle_height",
            "0dp"
        )
    )

    private fun displayModeImmersiveResourceEntries(
        barHeight: String,
        frameHeight: String
    ) = listOf(
        ResourceEntry(
            FRAMEWORK_PACKAGE,
            "dimen",
            "navigation_bar_height",
            barHeight
        ),
        ResourceEntry(
            FRAMEWORK_PACKAGE,
            "dimen",
            "navigation_bar_frame_height",
            frameHeight
        ),
        ResourceEntry(
            FRAMEWORK_PACKAGE,
            "dimen",
            "navigation_bar_width",
            "0dp"
        ),
        ResourceEntry(
            FRAMEWORK_PACKAGE,
            "dimen",
            "navigation_bar_height_portrait",
            barHeight
        ),
        ResourceEntry(
            FRAMEWORK_PACKAGE,
            "dimen",
            "navigation_bar_height_landscape",
            barHeight
        ),
        ResourceEntry(
            FRAMEWORK_PACKAGE,
            "dimen",
            "navigation_bar_frame_height_landscape",
            frameHeight
        ),
        ResourceEntry(
            FRAMEWORK_PACKAGE,
            "bool",
            "config_allowSeamlessRotationDespiteNavBarMoving",
            "true"
        ),
        ResourceEntry(
            FRAMEWORK_PACKAGE,
            "bool",
            "config_navBarAlwaysShowOnSideEdgeGesture",
            "true"
        ),
        ResourceEntry(
            FRAMEWORK_PACKAGE,
            "bool",
            "config_navBarCanMove",
            "false"
        ),
        ResourceEntry(
            FRAMEWORK_PACKAGE,
            "bool",
            "config_navBarTapThrough",
            "true"
        ),
        ResourceEntry(
            FRAMEWORK_PACKAGE,
            "dimen",
            "config_backGestureInset",
            "24dp"
        ),
        ResourceEntry(
            FRAMEWORK_PACKAGE,
            "dimen",
            "navigation_bar_gesture_height",
            "24dp"
        ),
        ResourceEntry(
            PIXEL_LAUNCHER_PACKAGE,
            "dimen",
            "taskbar_stashed_handle_height",
            "0dp"
        ),
        ResourceEntry(
            LAUNCHER3_PACKAGE,
            "dimen",
            "taskbar_stashed_handle_height",
            "0dp"
        )
    )

    private val hideKeyboardButtonsResourceEntries = listOf(
        ResourceEntry(
            SYSTEMUI_PACKAGE,
            "string",
            "config_navBarLayoutHandle",
            ";home_handle;"
        ).apply {
            isPortrait = true
            isLandscape = true
        },
        ResourceEntry(
            SYSTEMUI_PACKAGE,
            "string",
            "config_navBarLayout",
            ""
        ).apply {
            isPortrait = true
            isLandscape = true
        },
        ResourceEntry(
            FRAMEWORK_PACKAGE,
            "bool",
            "config_imeDrawsImeNavBar",
            "false"
        )
    )

    private val lowerSensitivityResourceEntries = listOf(
        ResourceEntry(
            FRAMEWORK_PACKAGE,
            "dimen",
            "navigation_bar_gesture_height",
            "12dp"
        )
    )

    private val hidePillResourceEntries = listOf(
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
        ),
        ResourceEntry(
            SYSTEMUI_PACKAGE,
            "dimen",
            "navigation_handle_horizontal_margin",
            "0dp"
        ),
        ResourceEntry(
            SYSTEMUI_PACKAGE,
            "dimen",
            "navigation_handle_sample_horizontal_margin",
            "0dp"
        ),
        ResourceEntry(
            SYSTEMUI_PACKAGE,
            "dimen",
            "navigation_home_handle_width_land",
            "0dp"
        ),
        ResourceEntry(
            PIXEL_LAUNCHER_PACKAGE,
            "dimen",
            "transient_taskbar_stashed_height",
            "0.2dp"
        ),
        ResourceEntry(
            PIXEL_LAUNCHER_PACKAGE,
            "dimen",
            "taskbar_from_nav_threshold",
            "10dp"
        ),
        ResourceEntry(
            PIXEL_LAUNCHER_PACKAGE,
            "dimen",
            "taskbar_stashed_size",
            "0.2dp"
        ),
        ResourceEntry(
            PIXEL_LAUNCHER_PACKAGE,
            "dimen",
            "taskbar_suw_insets",
            "0.1dp"
        ),
        ResourceEntry(
            LAUNCHER3_PACKAGE,
            "dimen",
            "transient_taskbar_stashed_height",
            "0.2dp"
        ),
        ResourceEntry(
            LAUNCHER3_PACKAGE,
            "dimen",
            "taskbar_from_nav_threshold",
            "10dp"
        ),
        ResourceEntry(
            LAUNCHER3_PACKAGE,
            "dimen",
            "taskbar_stashed_size",
            "0.2dp"
        ),
        ResourceEntry(
            LAUNCHER3_PACKAGE,
            "dimen",
            "taskbar_suw_insets",
            "0.1dp"
        ),
        ResourceEntry(
            PIXEL_LAUNCHER_PACKAGE,
            "dimen",
            "taskbar_nav_buttons_size",
            "0dp"
        ),
        ResourceEntry(
            PIXEL_LAUNCHER_PACKAGE,
            "dimen",
            "taskbar_stashed_handle_height",
            "0dp"
        ),
        ResourceEntry(
            LAUNCHER3_PACKAGE,
            "dimen",
            "taskbar_nav_buttons_size",
            "0dp"
        ),
        ResourceEntry(
            LAUNCHER3_PACKAGE,
            "dimen",
            "taskbar_stashed_handle_height",
            "0dp"
        )
    )

    private fun getPillAppearanceResourceEntries(
        pillWidth: Int,
        pillThickness: Int
    ) = listOf(
        ResourceEntry(
            PIXEL_LAUNCHER_PACKAGE,
            "dimen",
            "taskbar_stashed_handle_width",
            pillWidth.toString() + "dp"
        ),
        ResourceEntry(
            PIXEL_LAUNCHER_PACKAGE,
            "dimen",
            "taskbar_stashed_small_screen",
            pillWidth.toString() + "dp"
        ),
        ResourceEntry(
            PIXEL_LAUNCHER_PACKAGE,
            "dimen",
            "taskbar_stashed_handle_height",
            pillThickness.toString() + "dp"
        ),
        ResourceEntry(
            LAUNCHER3_PACKAGE,
            "dimen",
            "taskbar_stashed_handle_width",
            pillWidth.toString() + "dp"
        ),
        ResourceEntry(
            LAUNCHER3_PACKAGE,
            "dimen",
            "taskbar_stashed_small_screen",
            pillWidth.toString() + "dp"
        ),
        ResourceEntry(
            LAUNCHER3_PACKAGE,
            "dimen",
            "taskbar_stashed_handle_height",
            pillThickness.toString() + "dp"
        ),
    )

    private val _currentMode = MutableStateFlow(DisplayMode.NONE)
    private val _currentGcamLagFix = MutableStateFlow(false)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _uiEvent = MutableSharedFlow<ToastUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val operationMutex = Mutex()

    init {
        viewModelScope.launch {
            _currentMode.value = getCurrentMode()
            _currentGcamLagFix.value = getCurrentGcamLagFix()
        }

        viewModelScope.launch {
            prefController.setBoolean(
                TweaksKey.NAVIGATION_BAR_DISABLE_LEFT_GESTURE,
                try {
                    Shell.cmd(
                        "settings get secure back_gesture_inset_scale_left"
                    ).exec().out[0].toInt() == -1
                } catch (_: Exception) {
                    false
                }
            )
            prefController.setBoolean(
                TweaksKey.NAVIGATION_BAR_DISABLE_RIGHT_GESTURE,
                try {
                    Shell.cmd(
                        "settings get secure back_gesture_inset_scale_right"
                    ).exec().out[0].toInt() == -1
                } catch (_: Exception) {
                    false
                }
            )
        }
    }

    private fun getCurrentMode(): DisplayMode {
        return when {
            prefController.getBoolean(TweaksKey.NAVIGATION_BAR_DISPLAY_MODE_FULL_SCREEN) -> DisplayMode.FULL_SCREEN
            prefController.getBoolean(TweaksKey.NAVIGATION_BAR_DISPLAY_MODE_IMMERSIVE) -> DisplayMode.IMMERSIVE
            else -> DisplayMode.NONE
        }
    }

    private fun getCurrentGcamLagFix(): Boolean {
        return prefController.getBoolean(TweaksKey.NAVIGATION_BAR_DISPLAY_MODE_GCAM_LAG_FIX)
    }

    fun updateDisplayMode() {
        viewModelScope.launch(Dispatchers.IO) {
            operationMutex.withLock {
                val newMode = getCurrentMode()
                val newGcamLagFix = getCurrentGcamLagFix()

                if (_currentMode.value == newMode && _currentGcamLagFix.value == newGcamLagFix) return@withLock

                _isLoading.value = true

                delay(500)

                val oldMode = _currentMode.value
                val gcamLagFix =
                    prefController.getBoolean(TweaksKey.NAVIGATION_BAR_DISPLAY_MODE_GCAM_LAG_FIX)
                val immersiveVersion = 1
                val barHeight = if (gcamLagFix) "0.3dp" else "0dp"
                val frameHeight = when (immersiveVersion) {
                    1 -> "48dp"
                    2 -> "26dp"
                    else -> "16dp"
                }

                val error = when (newMode) {
                    DisplayMode.NONE -> {
                        if (oldMode == DisplayMode.FULL_SCREEN) {
                            ResourceManager.removeResourceFromOverlay(
                                overlayIds = listOf(navigationBarDisplayModeFullScreenId),
                                packagesToUpdate = displayModeFullScreenResourceEntries(
                                    barHeight,
                                    frameHeight
                                )
                                    .map { it.packageName }
                                    .distinct()
                            )
                        } else {
                            ResourceManager.removeResourceFromOverlay(
                                overlayIds = listOf(navigationBarDisplayModeImmersiveId),
                                packagesToUpdate = displayModeImmersiveResourceEntries(
                                    barHeight,
                                    frameHeight
                                )
                                    .map { it.packageName }
                                    .distinct()
                            )
                        }
                    }

                    DisplayMode.FULL_SCREEN -> {
                        ResourceManager.buildOverlayWithResource(
                            navigationBarDisplayModeFullScreenId,
                            *displayModeFullScreenResourceEntries(
                                barHeight,
                                frameHeight
                            ).toTypedArray()
                        )
                    }

                    DisplayMode.IMMERSIVE -> {
                        ResourceManager.buildOverlayWithResource(
                            navigationBarDisplayModeImmersiveId,
                            *displayModeImmersiveResourceEntries(
                                barHeight,
                                frameHeight
                            ).toTypedArray()
                        )
                    }
                }

                _currentMode.value = newMode

                delay(300)
                _isLoading.value = false

                if (!error) {
                    _uiEvent.emit(ToastUiEvent.Applied)
                } else {
                    _uiEvent.emit(ToastUiEvent.Error)
                }
            }
        }
    }

    fun applyHideKeyboardButtons(enable: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (_isLoading.value) return@launch

            _isLoading.value = true

            val error = if (enable) {
                ResourceManager.buildOverlayWithResource(
                    navigationBarHideKeyboardButtonsId,
                    *hideKeyboardButtonsResourceEntries.toTypedArray()
                )
            } else {
                ResourceManager.removeResourceFromOverlay(
                    overlayIds = listOf(navigationBarHideKeyboardButtonsId),
                    packagesToUpdate = hideKeyboardButtonsResourceEntries
                        .map { it.packageName }
                        .distinct()
                )
            }

            _isLoading.value = false

            if (!error) {
                _uiEvent.emit(ToastUiEvent.Applied)
            } else {
                _uiEvent.emit(ToastUiEvent.Error)
            }
        }
    }

    fun applyLowerSensitivity(enable: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (_isLoading.value) return@launch

            _isLoading.value = true

            val error = if (enable) {
                ResourceManager.buildOverlayWithResource(
                    navigationBarLowerSensitivityId,
                    *lowerSensitivityResourceEntries.toTypedArray()
                )
            } else {
                ResourceManager.removeResourceFromOverlay(
                    overlayIds = listOf(navigationBarLowerSensitivityId),
                    packagesToUpdate = lowerSensitivityResourceEntries
                        .map { it.packageName }
                        .distinct()
                )
            }

            _isLoading.value = false

            if (!error) {
                _uiEvent.emit(ToastUiEvent.Applied)
            } else {
                _uiEvent.emit(ToastUiEvent.Error)
            }
        }
    }

    fun applyDisableLeftGesture(enable: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (enable) {
                Shell.cmd(
                    "settings put secure back_gesture_inset_scale_left -1 &>/dev/null"
                ).exec()
            } else {
                Shell.cmd(
                    "settings delete secure back_gesture_inset_scale_left &>/dev/null"
                ).exec()
            }
        }
    }

    fun applyDisableRightGesture(enable: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (enable) {
                Shell.cmd(
                    "settings put secure back_gesture_inset_scale_right -1 &>/dev/null"
                ).exec()
            } else {
                Shell.cmd(
                    "settings delete secure back_gesture_inset_scale_right &>/dev/null"
                ).exec()
            }
        }
    }

    fun applyHidePill(enable: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (_isLoading.value) return@launch

            _isLoading.value = true

            val error = if (enable) {
                ResourceManager.buildOverlayWithResource(
                    navigationBarHidePillId,
                    *hidePillResourceEntries.toTypedArray()
                )
            } else {
                ResourceManager.removeResourceFromOverlay(
                    overlayIds = listOf(navigationBarHidePillId),
                    packagesToUpdate = hidePillResourceEntries
                        .map { it.packageName }
                        .distinct()
                )
            }

            _isLoading.value = false

            if (!error) {
                _uiEvent.emit(ToastUiEvent.Applied)
                restartLauncher()
            } else {
                _uiEvent.emit(ToastUiEvent.Error)
            }
        }
    }

    fun applyMonetPill(enable: Boolean) {
        viewModelScope.launch {
            if (enable) {
                OverlayUtils.enableOverlays(
                    navigationBarMonetPillOverlayPackageName1,
                    navigationBarMonetPillOverlayPackageName2
                )
            } else {
                OverlayUtils.disableOverlays(
                    navigationBarMonetPillOverlayPackageName1,
                    navigationBarMonetPillOverlayPackageName2
                )
            }
            restartLauncher()
        }
    }

    fun applyPillAppearance(width: Float, thickness: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            if (_isLoading.value) return@launch

            _isLoading.value = true

            val enable = width >= 0 && thickness >= 0

            val error = if (enable) {
                ResourceManager.buildOverlayWithResource(
                    navigationBarPillAppearanceId,
                    *getPillAppearanceResourceEntries(
                        width.roundToInt(),
                        thickness.roundToInt()
                    ).toTypedArray()
                )
            } else {
                ResourceManager.removeResourceFromOverlay(
                    overlayIds = listOf(navigationBarPillAppearanceId),
                    packagesToUpdate = getPillAppearanceResourceEntries(
                        width.roundToInt(),
                        thickness.roundToInt()
                    )
                        .map { it.packageName }
                        .distinct()
                )
            }

            _isLoading.value = false

            if (!error) {
                _uiEvent.emit(ToastUiEvent.Applied)
                restartLauncher()
            } else {
                _uiEvent.emit(ToastUiEvent.Error)
            }
        }
    }

    private fun restartLauncher() {
        if (AppUtils.isAppInstalled(PIXEL_LAUNCHER_PACKAGE)) {
            Shell.cmd("killall $PIXEL_LAUNCHER_PACKAGE").exec()
        }
        if (AppUtils.isAppInstalled(LAUNCHER3_PACKAGE)) {
            Shell.cmd("killall $LAUNCHER3_PACKAGE").exec()
        }
    }
}