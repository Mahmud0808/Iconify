package com.drdisagree.iconify.features.home.tweaks.colornengine.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drdisagree.iconify.core.preferences.PreferenceController
import com.drdisagree.iconify.core.utils.overlay.OverlayUtils
import com.drdisagree.iconify.core.utils.overlay.resource.ResourceEntry
import com.drdisagree.iconify.core.utils.overlay.resource.ResourceManager
import com.drdisagree.iconify.data.common.Const.FRAMEWORK_PACKAGE
import com.drdisagree.iconify.data.events.ToastUiEvent
import com.drdisagree.iconify.data.keys.TweaksKey
import com.drdisagree.iconify.features.home.tweaks.colornengine.models.ColorMode
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

@HiltViewModel
class ColorEngineViewModel @Inject constructor(
    private val prefController: PreferenceController
) : ViewModel() {

    private val tag = "ColorEngineViewModel"

    private val monetAccentOverlayPackageName = "IconifyComponentAMAC.overlay"
    private val monetGradientOverlayPackageName = "IconifyComponentAMGC.overlay"

    private val basicPrimaryColorId = "basic_primary_color"
    private val basicSecondaryColorId = "basic_secondary_color"

    private fun basicPrimaryColorResourceEntries(hex: String) = listOf(
        ResourceEntry(
            FRAMEWORK_PACKAGE,
            "color",
            "holo_blue_light",
            hex
        ),
        ResourceEntry(
            FRAMEWORK_PACKAGE,
            "color",
            "holo_green_light",
            hex
        )
    )

    private fun basicSecondaryColorResourceEntries(hex: String) = listOf(
        ResourceEntry(
            FRAMEWORK_PACKAGE,
            "color",
            "holo_blue_dark",
            hex
        ),
        ResourceEntry(
            FRAMEWORK_PACKAGE,
            "color",
            "holo_green_dark",
            hex
        )
    )

    private val _currentMode = MutableStateFlow(ColorMode.BASIC)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _uiEvent = MutableSharedFlow<ToastUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val operationMutex = Mutex()

    init {
        viewModelScope.launch {
            prefController.setBoolean(
                TweaksKey.MONET_ACCENT,
                OverlayUtils.isOverlayEnabled(monetAccentOverlayPackageName)
            )
            prefController.setBoolean(
                TweaksKey.MONET_GRADIENT,
                OverlayUtils.isOverlayEnabled(monetGradientOverlayPackageName)
            )
            _currentMode.value = when {
                prefController.getBoolean(TweaksKey.MONET_ACCENT) -> ColorMode.MONET_ACCENT
                prefController.getBoolean(TweaksKey.MONET_GRADIENT) -> ColorMode.MONET_GRADIENT
                else -> ColorMode.BASIC
            }
        }
    }

    fun applyPrimaryColor(hex: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (_isLoading.value) return@launch

            _isLoading.value = true

            val entries = basicPrimaryColorResourceEntries(hex)

            val error = ResourceManager.buildOverlayWithResource(
                basicPrimaryColorId,
                *entries.toTypedArray()
            )

            _isLoading.value = false

            if (!error) {
                _uiEvent.emit(ToastUiEvent.Applied)
            } else {
                _uiEvent.emit(ToastUiEvent.Error)
            }
        }
    }

    fun applySecondaryColor(hex: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (_isLoading.value) return@launch

            _isLoading.value = true

            val entries = basicSecondaryColorResourceEntries(hex)

            val error = ResourceManager.buildOverlayWithResource(
                basicSecondaryColorId,
                *entries.toTypedArray()
            )

            _isLoading.value = false

            if (!error) {
                _uiEvent.emit(ToastUiEvent.Applied)
            } else {
                _uiEvent.emit(ToastUiEvent.Error)
            }
        }
    }

    fun setColorMode(newMode: ColorMode) {
        viewModelScope.launch(Dispatchers.IO) {
            operationMutex.withLock {
                if (_currentMode.value == newMode) return@withLock

                _isLoading.value = true

                delay(500)

                val oldMode = _currentMode.value
                var error = false

                if (oldMode == ColorMode.BASIC && newMode != ColorMode.BASIC) {
                    error = disableBasicColors() || error
                }

                when (newMode) {
                    ColorMode.BASIC -> {
                        if (oldMode == ColorMode.MONET_ACCENT) {
                            OverlayUtils.disableOverlay(monetAccentOverlayPackageName)
                        } else if (oldMode == ColorMode.MONET_GRADIENT) {
                            OverlayUtils.disableOverlay(monetGradientOverlayPackageName)
                        }
                        error = applyBasicColors() || error
                    }

                    ColorMode.MONET_ACCENT -> {
                        OverlayUtils.enableOverlayExclusiveInCategory(monetAccentOverlayPackageName)
                    }

                    ColorMode.MONET_GRADIENT -> {
                        OverlayUtils.enableOverlayExclusiveInCategory(monetGradientOverlayPackageName)
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

    private suspend fun applyBasicColors(): Boolean {
        val primaryColor = prefController.getString(TweaksKey.BASIC_COLOR_PRIMARY)
        val secondaryColor = prefController.getString(TweaksKey.BASIC_COLOR_SECONDARY)

        val primaryColorEntries = basicPrimaryColorResourceEntries(primaryColor)
        val secondaryColorEntries = basicSecondaryColorResourceEntries(secondaryColor)

        return try {
            ResourceManager.insertResources(
                overlayId = basicPrimaryColorId,
                resourceEntries = primaryColorEntries.toTypedArray()
            )
            ResourceManager.insertResources(
                overlayId = basicSecondaryColorId,
                resourceEntries = secondaryColorEntries.toTypedArray()
            )
            ResourceManager.triggerDynamicOverlayUpdate(
                packagesToUpdate = (primaryColorEntries + secondaryColorEntries)
                    .map { it.packageName }
                    .distinct()
            )
        } catch (e: Exception) {
            Log.e(tag, "applyBasicColors", e)
            true
        }
    }

    private suspend fun disableBasicColors(): Boolean {
        val primaryColorEntries = basicPrimaryColorResourceEntries("#FFFFFF")
        val secondaryColorEntries = basicSecondaryColorResourceEntries("#FFFFFF")

        return ResourceManager.removeResourceFromOverlay(
            overlayIds = listOf(basicPrimaryColorId, basicSecondaryColorId),
            packagesToUpdate = (primaryColorEntries + secondaryColorEntries)
                .map { it.packageName }
                .distinct()
        )
    }
}