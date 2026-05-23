package com.drdisagree.iconify.features.home.tweaks.cornerradius.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drdisagree.iconify.core.utils.overlay.managers.RoundnessManager
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
import java.io.IOException
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class CornerRadiusViewModel @Inject constructor() : ViewModel() {

    private val tag = "CornerRadiusViewModel"

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _uiEvent = MutableSharedFlow<ToastUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun applyCornerRadius(cornerRadius: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            if (_isLoading.value) return@launch

            _isLoading.value = true

            val enable = cornerRadius >= 0
            val defaultValue = (TweaksKey.UI_CORNER_RADIUS.default as Float).roundToInt()

            val error = try {
                RoundnessManager.buildOverlay(
                    cornerRadius = if (enable) cornerRadius else defaultValue,
                    force = true
                )
            } catch (e: IOException) {
                Log.e(tag, e.toString())
                true
            }

            delay(500)
            _isLoading.value = false

            if (!error) {
                _uiEvent.emit(ToastUiEvent.Applied)
            } else {
                _uiEvent.emit(ToastUiEvent.Error)
            }
        }
    }
}