package com.drdisagree.iconify.features.home.cellularicons.viewmodels

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
class CellularIconViewModel @Inject constructor(
    private val systemActionRepository: SystemActionRepository
) : ViewModel() {

    private val tag = "CellularIconViewModel"

    private val category = "SGIC"
    private val otherCategory = "WIFI"
    private val prefKey = CustomizationKey.CELLULAR_ICON_STYLE
    private val prefDefaultValue = prefKey.default as String

    private val _cellularIconStyle = MutableStateFlow(prefDefaultValue)
    val cellularIconStyle: StateFlow<String> = _cellularIconStyle.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _uiEvent = MutableSharedFlow<ToastUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun refreshState() {
        _cellularIconStyle.value = RPrefs.getString(prefKey)!!
    }

    fun togglePack(packId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (_isLoading.value) return@launch

            _isLoading.value = true

            if (_cellularIconStyle.value == packId) {
                OverlayUtils.disableOverlay("IconifyComponent$category.overlay")

                delay(1000)
                RPrefs.putString(prefKey, prefDefaultValue)
                _cellularIconStyle.value = prefDefaultValue
                _isLoading.value = false
                systemActionRepository.shouldRestartSystemUI()
                _uiEvent.emit(ToastUiEvent.Disabled)
                return@launch
            }

            val iconPackPkgName = OverlayUtils.checkEnabledOverlay("IPSUI")
            val iconPackApplied = iconPackPkgName.isNotEmpty()

            if (iconPackApplied) {
                OverlayUtils.disableOverlay(iconPackPkgName)
            }

            val wifiPack = "IconifyComponent$otherCategory.overlay"
            val wifiPackApplied = OverlayUtils.isOverlayEnabled(wifiPack)

            if (iconPackApplied) {
                OverlayUtils.enableOverlayExclusiveInCategory(iconPackPkgName, "low")
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

            if (wifiPackApplied) {
                OverlayUtils.enableOverlay(wifiPack)
            }

            delay(1000)
            _isLoading.value = false
            systemActionRepository.shouldRestartSystemUI()

            if (!hasError) {
                RPrefs.putString(prefKey, packId)
                _cellularIconStyle.value = packId
                _uiEvent.emit(ToastUiEvent.Applied)
            } else {
                _uiEvent.emit(ToastUiEvent.Error)
            }
        }
    }
}