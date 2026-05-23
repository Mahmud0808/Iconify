package com.drdisagree.iconify.features.home.iconshape.viewmodels

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
class IconShapeViewModel @Inject constructor() : ViewModel() {

    private val tag = "IconShapeViewModel"

    private val category = "SIS"
    private val prefKey = CustomizationKey.ICON_SHAPE_STYLE
    private val prefDefaultValue = prefKey.default as String

    private val _iconShapeStyle = MutableStateFlow(prefDefaultValue)
    val iconShapeStyle: StateFlow<String> = _iconShapeStyle.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _uiEvent = MutableSharedFlow<ToastUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun refreshState() {
        _iconShapeStyle.value = RPrefs.getString(prefKey)!!
    }

    fun togglePack(packId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (_isLoading.value) return@launch

            _isLoading.value = true

            if (_iconShapeStyle.value == packId) {
                delay(500)
                OverlayUtils.disableOverlay("IconifyComponent$category.overlay")

                delay(1000)
                RPrefs.putString(prefKey, prefDefaultValue)
                _iconShapeStyle.value = prefDefaultValue
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

            delay(1000)
            _isLoading.value = false

            if (!hasError) {
                RPrefs.putString(prefKey, packId)
                _iconShapeStyle.value = packId
                _uiEvent.emit(ToastUiEvent.Applied)
            } else {
                _uiEvent.emit(ToastUiEvent.Error)
            }
        }
    }
}