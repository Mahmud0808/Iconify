package com.drdisagree.iconify.features.home.toastframe.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drdisagree.iconify.core.utils.overlay.OverlayUtils
import com.drdisagree.iconify.core.utils.overlay.compilers.OnDemandCompiler
import com.drdisagree.iconify.data.common.Const.FRAMEWORK_PACKAGE
import com.drdisagree.iconify.data.config.RPrefs
import com.drdisagree.iconify.data.events.ToastUiEvent
import com.drdisagree.iconify.data.keys.CustomizationKey
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
class ToastFrameViewModel @Inject constructor() : ViewModel() {

    private val tag = "ToastFrameViewModel"

    private val category = "TSTFRM"
    private val prefKey = CustomizationKey.TOAST_FRAME_STYLE
    private val prefDefaultValue = prefKey.default as String

    private val _toastFrameStyle = MutableStateFlow(prefDefaultValue)
    val toastFrameStyle: StateFlow<String> = _toastFrameStyle.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _uiEvent = MutableSharedFlow<ToastUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun refreshState() {
        _toastFrameStyle.value = RPrefs.getString(prefKey)!!
    }

    fun togglePack(packId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (_isLoading.value) return@launch

            _isLoading.value = true

            if (_toastFrameStyle.value == packId) {
                delay(500)
                OverlayUtils.disableOverlay("IconifyComponent$category.overlay")

                delay(1000)
                RPrefs.putString(prefKey, prefDefaultValue)
                _toastFrameStyle.value = prefDefaultValue
                _isLoading.value = false
                _uiEvent.emit(ToastUiEvent.Disabled)
                return@launch
            }

            val hasError = withContext(Dispatchers.IO) {
                try {
                    OnDemandCompiler.buildOverlay(
                        overlayName = category,
                        style = packId,
                        targetPackage = FRAMEWORK_PACKAGE,
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

            if (!hasError) {
                RPrefs.putString(prefKey, packId)
                _toastFrameStyle.value = packId
                _uiEvent.emit(ToastUiEvent.Applied)
            } else {
                _uiEvent.emit(ToastUiEvent.Error)
            }
        }
    }
}