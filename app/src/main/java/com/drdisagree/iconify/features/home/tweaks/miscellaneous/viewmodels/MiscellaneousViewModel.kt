package com.drdisagree.iconify.features.home.tweaks.miscellaneous.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drdisagree.iconify.core.preferences.PreferenceController
import com.drdisagree.iconify.core.utils.overlay.OverlayUtils
import com.drdisagree.iconify.core.utils.overlay.resource.ResourceEntry
import com.drdisagree.iconify.core.utils.overlay.resource.ResourceManager
import com.drdisagree.iconify.data.common.Const.FRAMEWORK_PACKAGE
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.events.ToastUiEvent
import com.drdisagree.iconify.data.keys.TweaksKey
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
class MiscellaneousViewModel @Inject constructor(
    private val prefController: PreferenceController
) : ViewModel() {

    private val tag = "MiscellaneousViewModel"

    private val accentPrivacyChipOverlayPackageName = "IconifyComponentPCBG.overlay"

    private val tabletLandscapeId = "tablet_qs_landscape"
    private val notchBarKillerId = "notch_bar_killer"
    private val tabletHeaderId = "tablet_qs_header"

    private val tabletLandscapeResourceEntries = listOf(
        ResourceEntry(
            SYSTEMUI_PACKAGE,
            "bool",
            "config_use_split_notification_shade",
            "true"
        ),
        ResourceEntry(
            SYSTEMUI_PACKAGE,
            "bool",
            "config_skinnyNotifsInLandscape",
            "false"
        ),
        ResourceEntry(
            SYSTEMUI_PACKAGE,
            "bool",
            "can_use_one_handed_bouncer",
            "true"
        ),
        ResourceEntry(
            SYSTEMUI_PACKAGE,
            "dimen",
            "notifications_top_padding_split_shade",
            "40.0dip"
        ),
        ResourceEntry(
            SYSTEMUI_PACKAGE,
            "dimen",
            "split_shade_notifications_scrim_margin_bottom",
            "14.0dip"
        ),
        ResourceEntry(
            SYSTEMUI_PACKAGE,
            "dimen",
            "qs_header_system_icons_area_height",
            "0.0dip"
        ),
        ResourceEntry(
            SYSTEMUI_PACKAGE,
            "dimen",
            "qs_panel_padding_top",
            "0.0dip"
        ),
        ResourceEntry(
            SYSTEMUI_PACKAGE,
            "integer",
            "quick_settings_num_columns",
            "2"
        ),
        ResourceEntry(
            SYSTEMUI_PACKAGE,
            "integer",
            "quick_qs_panel_max_rows",
            "2"
        ),
        ResourceEntry(
            SYSTEMUI_PACKAGE,
            "integer",
            "quick_qs_panel_max_tiles",
            "4"
        ),
        ResourceEntry(
            FRAMEWORK_PACKAGE,
            "bool",
            "config_fillMainBuiltInDisplayCutout",
            "false"
        ),
        ResourceEntry(
            FRAMEWORK_PACKAGE,
            "bool",
            "config_maskMainBuiltInDisplayCutout",
            "true"
        ),
        ResourceEntry(
            FRAMEWORK_PACKAGE,
            "string",
            "config_mainBuiltInDisplayCutout",
            "M 0,0 L 0, 0 C 0,0 0,0 0,0"
        ),
        ResourceEntry(
            FRAMEWORK_PACKAGE,
            "string",
            "config_mainBuiltInDisplayCutoutRectApproximation",
            "@string/config_mainBuiltInDisplayCutout"
        )
    )

    private val tabletHeaderResourceEntries = listOf(
        ResourceEntry(
            SYSTEMUI_PACKAGE,
            "bool",
            "config_use_large_screen_shade_header",
            "true"
        )
    )

    private val notchBarKillerResourceEntries = listOf(
        ResourceEntry(
            FRAMEWORK_PACKAGE,
            "bool",
            "config_fillMainBuiltInDisplayCutout",
            "false"
        ),
        ResourceEntry(
            FRAMEWORK_PACKAGE,
            "bool",
            "config_maskMainBuiltInDisplayCutout",
            "true"
        ),
        ResourceEntry(
            FRAMEWORK_PACKAGE,
            "string",
            "config_mainBuiltInDisplayCutout",
            "M 0,0 L 0, 0 C 0,0 0,0 0,0"
        ),
        ResourceEntry(
            FRAMEWORK_PACKAGE,
            "string",
            "config_mainBuiltInDisplayCutoutRectApproximation",
            "@string/config_mainBuiltInDisplayCutout"
        )
    )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _uiEvent = MutableSharedFlow<ToastUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            prefController.setBoolean(
                TweaksKey.ACCENT_PRIVACY_CHIP,
                OverlayUtils.isOverlayEnabled(accentPrivacyChipOverlayPackageName)
            )
        }
    }

    fun toggleTabletLandscape(enable: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (_isLoading.value) return@launch

            _isLoading.value = true

            val entries = tabletLandscapeResourceEntries

            entries.forEach { entry ->
                entry.apply {
                    isPortrait = false
                    isLandscape = true
                }
            }

            val error = if (enable) {
                ResourceManager.buildOverlayWithResource(
                    tabletLandscapeId,
                    *entries.toTypedArray()
                )
            } else {
                ResourceManager.removeResourceFromOverlay(
                    overlayIds = listOf(tabletLandscapeId),
                    packagesToUpdate = entries.map { it.packageName }.distinct()
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

    fun toggleTabletHeader(enable: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (_isLoading.value) return@launch

            _isLoading.value = true

            val entries = tabletHeaderResourceEntries

            val error = if (enable) {
                ResourceManager.buildOverlayWithResource(
                    tabletHeaderId,
                    *entries.toTypedArray()
                )
            } else {
                ResourceManager.removeResourceFromOverlay(
                    overlayIds = listOf(tabletHeaderId),
                    packagesToUpdate = entries.map { it.packageName }.distinct()
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

    fun toggleNotchBarKiller(enable: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (_isLoading.value) return@launch

            _isLoading.value = true

            val entries = notchBarKillerResourceEntries

            val error = if (enable) {
                ResourceManager.buildOverlayWithResource(
                    notchBarKillerId,
                    *entries.toTypedArray()
                )
            } else {
                ResourceManager.removeResourceFromOverlay(
                    overlayIds = listOf(notchBarKillerId),
                    packagesToUpdate = entries.map { it.packageName }.distinct()
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

    fun toggleAccentPrivacyChip(enable: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (_isLoading.value) return@launch

            _isLoading.value = true

            if (enable) {
                OverlayUtils.enableOverlay(accentPrivacyChipOverlayPackageName)
            } else {
                OverlayUtils.disableOverlay(accentPrivacyChipOverlayPackageName)
            }

            delay(500)
            _isLoading.value = false

            _uiEvent.emit(ToastUiEvent.Applied)
        }
    }
}