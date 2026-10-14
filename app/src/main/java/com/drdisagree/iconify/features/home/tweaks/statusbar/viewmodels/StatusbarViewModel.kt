package com.drdisagree.iconify.features.home.tweaks.statusbar.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drdisagree.iconify.core.preferences.PreferenceController
import com.drdisagree.iconify.core.utils.overlay.resource.ResourceEntry
import com.drdisagree.iconify.core.utils.overlay.resource.ResourceManager
import com.drdisagree.iconify.data.common.Const.FRAMEWORK_PACKAGE
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.events.ToastUiEvent
import com.drdisagree.iconify.data.keys.TweaksKey
import com.drdisagree.iconify.data.keys.XposedKey
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatusbarViewModel @Inject constructor(
    private val prefController: PreferenceController
) : ViewModel() {

    private val tag = "StatusbarViewModel"

    private val statusbarIconColorId = "status_bar_icon_color"
    private val statusbarStartPaddingId = "status_bar_padding_start"
    private val statusbarEndPaddingId = "status_bar_padding_end"
    private val statusbarHeightId = "status_bar_height"
    private val signalIconSizeId = "signal_icon_size_increase"
    private val wifiIconSizeId = "wifi_icon_size_increase"
    private val networkTypeSizeId = "network_type_size_increase"

    private fun statusbarIconColorResourceEntries(color: String) = listOf(
        ResourceEntry(
            SYSTEMUI_PACKAGE,
            "color",
            "dark_mode_icon_color_dual_tone_fill",
            color
        ),
        ResourceEntry(
            SYSTEMUI_PACKAGE,
            "color",
            "dark_mode_icon_color_single_tone",
            color
        ),
        ResourceEntry(
            SYSTEMUI_PACKAGE,
            "color",
            "dark_mode_qs_icon_color_dual_tone_fill",
            color
        ),
        ResourceEntry(
            SYSTEMUI_PACKAGE,
            "color",
            "dark_mode_qs_icon_color_single_tone",
            color
        ),
        ResourceEntry(
            SYSTEMUI_PACKAGE,
            "color",
            "light_mode_icon_color_dual_tone_fill",
            color
        ),
        ResourceEntry(
            SYSTEMUI_PACKAGE,
            "color",
            "light_mode_icon_color_single_tone",
            color
        ),
        ResourceEntry(
            SYSTEMUI_PACKAGE,
            "color",
            "status_bar_clock_color",
            color
        ),
    )

    private fun statusbarStartPaddingResourceEntries(padding: Int) = listOf(
        ResourceEntry(
            SYSTEMUI_PACKAGE,
            "dimen",
            "status_bar_padding_start",
            padding.toString() + "dp"
        )
    )

    private fun statusbarEndPaddingResourceEntries(padding: Int) = listOf(
        ResourceEntry(
            SYSTEMUI_PACKAGE,
            "dimen",
            "status_bar_padding_end",
            padding.toString() + "dp"
        )
    )

    private val signalIconSizeResourceEntries = listOf(
        ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "status_bar_mobile_signal_size_updated", "15sp"),
        ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "status_bar_mobile_container_height_updated", "15sp")
    )

    private val wifiIconSizeResourceEntries = listOf(
        ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "status_bar_wifi_signal_height_updated", "15sp"),
        ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "status_bar_composable_wifi_icon_height", "15sp")
    )

    private val networkTypeSizeResourceEntries = listOf(
        ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "status_bar_mobile_type_size_updated", "15sp")
    )

    private fun statusbarHeightResourceEntries(height: Int) = listOf(
        ResourceEntry(
            SYSTEMUI_PACKAGE,
            "dimen",
            "status_bar_height",
            height.toString() + "dp"
        ),
        ResourceEntry(
            FRAMEWORK_PACKAGE,
            "dimen",
            "status_bar_height",
            height.toString() + "dp"
        ),
        ResourceEntry(
            FRAMEWORK_PACKAGE,
            "dimen",
            "status_bar_height_default",
            height.toString() + "dp"
        ),
        ResourceEntry(
            FRAMEWORK_PACKAGE,
            "dimen",
            "status_bar_height_portrait",
            height.toString() + "dp"
        ),
        ResourceEntry(
            FRAMEWORK_PACKAGE,
            "dimen",
            "status_bar_height_landscape",
            height.toString() + "dp"
        )
    )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _uiEvent = MutableSharedFlow<ToastUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun updateStatusbarTintMode() {
        viewModelScope.launch(Dispatchers.IO) {
            if (_isLoading.value) return@launch

            _isLoading.value = true

            val tintMode = prefController.getString(TweaksKey.STATUSBAR_TINT_MODE)
            val colorCode = prefController.getString(TweaksKey.STATUSBAR_TINT_CUSTOM_COLOR_CODE)

            val enable = tintMode != "0"
            val color = when (tintMode) {
                "1" -> "@*android:color/holo_blue_light"
                "2" -> colorCode
                else -> "#FFFFFF"
            }

            val entries = statusbarIconColorResourceEntries(color)

            val error = if (enable) {
                ResourceManager.buildOverlayWithResource(
                    statusbarIconColorId,
                    *entries.toTypedArray()
                )
            } else {
                ResourceManager.removeResourceFromOverlay(
                    overlayIds = listOf(statusbarIconColorId),
                    packagesToUpdate = entries.map { it.packageName }.distinct()
                )
            }

            delay(500)
            prefController.setBoolean(
                XposedKey.STATUSBAR_CUSTOM_COLOR_CHANGED,
                !prefController.getBoolean(XposedKey.STATUSBAR_CUSTOM_COLOR_CHANGED)
            )

            _isLoading.value = false

            if (!error) {
                _uiEvent.emit(ToastUiEvent.Applied)
            } else {
                _uiEvent.emit(ToastUiEvent.Error)
            }
        }
    }

    fun applyStatusbarStartPadding(padding: Int, onSuccess: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            if (_isLoading.value) return@launch

            _isLoading.value = true

            val entries = statusbarStartPaddingResourceEntries(padding)

            val error = if (padding >= 0) {
                ResourceManager.buildOverlayWithResource(
                    statusbarStartPaddingId,
                    *entries.toTypedArray()
                )
            } else {
                ResourceManager.removeResourceFromOverlay(
                    overlayIds = listOf(statusbarStartPaddingId),
                    packagesToUpdate = entries.map { it.packageName }.distinct()
                )
            }

            _isLoading.value = false

            if (!error) {
                _uiEvent.emit(ToastUiEvent.Applied)
                delay(500)
                onSuccess()
            } else {
                _uiEvent.emit(ToastUiEvent.Error)
            }
        }
    }

    fun applyStatusbarEndPadding(padding: Int, onSuccess: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            if (_isLoading.value) return@launch

            _isLoading.value = true

            val entries = statusbarEndPaddingResourceEntries(padding)

            val error = if (padding >= 0) {
                ResourceManager.buildOverlayWithResource(
                    statusbarEndPaddingId,
                    *entries.toTypedArray()
                )
            } else {
                ResourceManager.removeResourceFromOverlay(
                    overlayIds = listOf(statusbarEndPaddingId),
                    packagesToUpdate = entries.map { it.packageName }.distinct()
                )
            }

            _isLoading.value = false

            if (!error) {
                _uiEvent.emit(ToastUiEvent.Applied)
                delay(500)
                onSuccess()
            } else {
                _uiEvent.emit(ToastUiEvent.Error)
            }
        }
    }

    fun applyStatusbarHeight(height: Int, onSuccess: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            if (_isLoading.value) return@launch

            _isLoading.value = true

            val entries = statusbarHeightResourceEntries(height)

            val error = if (height >= 0) {
                ResourceManager.buildOverlayWithResource(
                    statusbarHeightId,
                    *entries.toTypedArray()
                )
            } else {
                ResourceManager.removeResourceFromOverlay(
                    overlayIds = listOf(statusbarHeightId),
                    packagesToUpdate = entries.map { it.packageName }.distinct()
                )
            }

            _isLoading.value = false

            if (!error) {
                _uiEvent.emit(ToastUiEvent.Applied)
                delay(500)
                onSuccess()
            } else {
                _uiEvent.emit(ToastUiEvent.Error)
            }
        }
    }

    fun applySignalIconSizeIncrease(enabled: Boolean, onSuccess: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            if (_isLoading.value) return@launch

            _isLoading.value = true

            val error = if (enabled) {
                ResourceManager.buildOverlayWithResource(
                    signalIconSizeId,
                    *signalIconSizeResourceEntries.toTypedArray()
                )
            } else {
                ResourceManager.removeResourceFromOverlay(
                    overlayIds = listOf(signalIconSizeId),
                    packagesToUpdate = signalIconSizeResourceEntries.map { it.packageName }.distinct()
                )
            }

            _isLoading.value = false

            if (!error) {
                _uiEvent.emit(ToastUiEvent.Applied)
                delay(500)
                onSuccess()
            } else {
                _uiEvent.emit(ToastUiEvent.Error)
            }
        }
    }

    fun applyWifiIconSizeIncrease(enabled: Boolean, onSuccess: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            if (_isLoading.value) return@launch

            _isLoading.value = true

            val error = if (enabled) {
                ResourceManager.buildOverlayWithResource(
                    wifiIconSizeId,
                    *wifiIconSizeResourceEntries.toTypedArray()
                )
            } else {
                ResourceManager.removeResourceFromOverlay(
                    overlayIds = listOf(wifiIconSizeId),
                    packagesToUpdate = wifiIconSizeResourceEntries.map { it.packageName }.distinct()
                )
            }

            _isLoading.value = false

            if (!error) {
                _uiEvent.emit(ToastUiEvent.Applied)
                delay(500)
                onSuccess()
            } else {
                _uiEvent.emit(ToastUiEvent.Error)
            }
        }
    }

    fun applyNetworkTypeSizeIncrease(enabled: Boolean, onSuccess: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            if (_isLoading.value) return@launch

            _isLoading.value = true

            val error = if (enabled) {
                ResourceManager.buildOverlayWithResource(
                    networkTypeSizeId,
                    *networkTypeSizeResourceEntries.toTypedArray()
                )
            } else {
                ResourceManager.removeResourceFromOverlay(
                    overlayIds = listOf(networkTypeSizeId),
                    packagesToUpdate = networkTypeSizeResourceEntries.map { it.packageName }.distinct()
                )
            }

            _isLoading.value = false

            if (!error) {
                _uiEvent.emit(ToastUiEvent.Applied)
                delay(500)
                onSuccess()
            } else {
                _uiEvent.emit(ToastUiEvent.Error)
            }
        }
    }
}