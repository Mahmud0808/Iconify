package com.drdisagree.iconify.features.home.settingsicons.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drdisagree.iconify.core.utils.overlay.OverlayUtils
import com.drdisagree.iconify.core.utils.overlay.managers.SettingsIconResourceManager
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
class SettingsIconsViewModel @Inject constructor() : ViewModel() {

    private val tag = "SettingsIconsViewModel"

    private val categories = listOf("SIP1", "SIP2", "SIP3")
    private val prefKey = CustomizationKey.SETTINGS_ICONS_STYLE
    val prefDefaultValue = prefKey.default as String

    private val _settingsIconsStyle = MutableStateFlow(prefDefaultValue)
    val settingsIconsStyle: StateFlow<String> = _settingsIconsStyle.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _uiEvent = MutableSharedFlow<ToastUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun refreshState() {
        _settingsIconsStyle.value = RPrefs.getString(prefKey)!!
    }

    fun applyStyle(
        backgroundStyle: Int,
        backgroundShape: Int,
        iconSize: Int,
        iconColor: Int,
        iconSet: Int,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (_isLoading.value) return@launch

            _isLoading.value = true

            val selectedStyle = "$backgroundStyle,$backgroundShape,$iconSize,$iconColor,$iconSet"

            val hasError = withContext(Dispatchers.IO) {
                try {
                    SettingsIconResourceManager.buildOverlay(
                        backgroundStyle = backgroundStyle + 1,
                        backgroundShape = backgroundShape + 1,
                        iconSize = iconSize + 1,
                        iconColor = iconColor + 1,
                        iconSet = iconSet + 1,
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
                RPrefs.putString(prefKey, selectedStyle)
                _settingsIconsStyle.value = selectedStyle
                _uiEvent.emit(ToastUiEvent.Applied)
            } else {
                _uiEvent.emit(ToastUiEvent.Error)
            }
        }
    }

    fun disableStyle() {
        viewModelScope.launch(Dispatchers.IO) {
            if (_isLoading.value) return@launch

            _isLoading.value = true

            delay(500)
            OverlayUtils.disableOverlays(
                *categories.map { "IconifyComponent${it}.overlay" }.toTypedArray()
            )
            delay(1000)
            RPrefs.putString(prefKey, prefDefaultValue)
            _settingsIconsStyle.value = prefDefaultValue
            _isLoading.value = false
            _uiEvent.emit(ToastUiEvent.Disabled)
        }
    }
}