package com.drdisagree.iconify.features.home.tweaks.mediaplayer.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drdisagree.iconify.core.utils.overlay.resource.ResourceEntry
import com.drdisagree.iconify.core.utils.overlay.resource.ResourceManager
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.events.ToastUiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MediaPlayerViewModel @Inject constructor() : ViewModel() {

    private val tag = "MediaPlayerViewModel"

    private val progressWaveId = "media_progress_wave"

    private val progressWaveResourceEntries = listOf(
        ResourceEntry(
            SYSTEMUI_PACKAGE,
            "dimen",
            "qs_media_seekbar_progress_amplitude",
            "0dp"
        ),
        ResourceEntry(
            SYSTEMUI_PACKAGE,
            "dimen",
            "qs_media_seekbar_progress_phase",
            "0dp"
        )
    )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _uiEvent = MutableSharedFlow<ToastUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun toggleDisableProgressWave(enable: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (_isLoading.value) return@launch

            _isLoading.value = true

            val entries = progressWaveResourceEntries

            val error = if (enable) {
                ResourceManager.buildOverlayWithResource(
                    progressWaveId,
                    *entries.toTypedArray()
                )
            } else {
                ResourceManager.removeResourceFromOverlay(
                    overlayIds = listOf(progressWaveId),
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
}