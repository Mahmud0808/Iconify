package com.drdisagree.iconify.features.home.iconpack.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drdisagree.iconify.core.utils.overlay.OverlayUtils
import com.drdisagree.iconify.data.models.IconPackPreview
import com.drdisagree.iconify.data.repository.SystemActionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IconPackViewModel @Inject constructor(
    private val systemActionRepository: SystemActionRepository
) : ViewModel() {

    private var hasCheckedStatus = false

    private val categories = listOf("IPAS", "IPSUI")

    private val _iconPackStates = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val iconPackStates: StateFlow<Map<String, Boolean>> = _iconPackStates.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun checkAllStatuses(iconPacks: List<IconPackPreview>) {
        if (!hasCheckedStatus) {
            iconPacks.forEach { pack -> checkStatus(pack.id) }
            hasCheckedStatus = true
        }
    }

    fun checkStatus(packId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val isEnabled =
                OverlayUtils.isOverlayEnabled("IconifyComponent${categories[0]}$packId.overlay")
            _iconPackStates.update { it + (packId to isEnabled) }
        }
    }

    fun togglePack(packId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentState = _iconPackStates.value[packId] ?: false
            val currentlyEnabledPack = _iconPackStates.value
                .entries
                .firstOrNull { it.value && it.key != packId }
                ?.key

            _isLoading.value = true
            delay(500)

            if (currentState) {
                OverlayUtils.disableOverlays(
                    *categories.map { category -> "IconifyComponent$category$packId.overlay" }
                        .toTypedArray()
                )
            } else {
                OverlayUtils.enableOverlaysExclusiveInCategory(
                    *categories.map { category -> "IconifyComponent$category$packId.overlay" }
                        .toTypedArray()
                )
            }

            delay(1500)
            _isLoading.value = false
            systemActionRepository.shouldRestartSystemUI()

            val toggledState =
                OverlayUtils.isOverlayEnabled("IconifyComponent${categories[0]}$packId.overlay")
            val previousState = currentlyEnabledPack?.let {
                OverlayUtils.isOverlayEnabled("IconifyComponent${categories[0]}$it.overlay")
            }

            _iconPackStates.update { map ->
                val previousMap = currentlyEnabledPack?.let {
                    mapOf(it to (previousState ?: false))
                } ?: emptyMap()
                map + (packId to toggledState) + previousMap
            }
        }
    }
}