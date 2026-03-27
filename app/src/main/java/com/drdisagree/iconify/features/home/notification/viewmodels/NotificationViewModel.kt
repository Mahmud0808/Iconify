package com.drdisagree.iconify.features.home.notification.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drdisagree.iconify.core.utils.overlay.OverlayUtils
import com.drdisagree.iconify.core.utils.overlay.compilers.OnDemandCompiler
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.config.RPrefs
import com.drdisagree.iconify.data.events.ToastUiEvent
import com.drdisagree.iconify.data.keys.CustomizationKey
import com.drdisagree.iconify.data.repository.SystemActionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val systemActionRepository: SystemActionRepository
) : ViewModel() {

    private val tag = "NotificationViewModel"

    private val category = "NFN"
    private val prefKey = CustomizationKey.NOTIFICATION_STYLE
    private val prefDefaultValue = prefKey.default as String

    private val _notificationStyle = MutableStateFlow(prefDefaultValue)
    val notificationStyle: StateFlow<String> = _notificationStyle.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _uiEvent = MutableSharedFlow<ToastUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun refreshState() {
        _notificationStyle.value = RPrefs.getString(prefKey)!!
    }

    fun togglePack(packId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (_isLoading.value) return@launch

            _isLoading.value = true

            if (_notificationStyle.value == packId) {
                OverlayUtils.disableOverlay("IconifyComponent$category.overlay")

                delay(1000)
                RPrefs.putString(prefKey, prefDefaultValue)
                _notificationStyle.value = prefDefaultValue
                _isLoading.value = false
                systemActionRepository.shouldRestartSystemUI()
                _uiEvent.emit(ToastUiEvent.Disabled)
                return@launch
            }

            val hasError = withContext(Dispatchers.IO) {
                try {
                    OnDemandCompiler.buildOverlay(
                        overlayName = category,
                        style = packId,
                        targetPackage = SYSTEMUI_PACKAGE,
                        force = true
                    )
                } catch (e: IOException) {
                    Log.e(tag, e.toString())
                    true
                }
            }

            if (!hasError) {
                OverlayUtils.enableRoundnessIfDisabled()
            }

            delay(1000)
            _isLoading.value = false
            systemActionRepository.shouldRestartSystemUI()

            if (!hasError) {
                RPrefs.putString(prefKey, packId)
                _notificationStyle.value = packId
                _uiEvent.emit(ToastUiEvent.Applied)
            } else {
                _uiEvent.emit(ToastUiEvent.Error)
            }
        }
    }
}