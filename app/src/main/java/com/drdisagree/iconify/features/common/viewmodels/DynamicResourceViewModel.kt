package com.drdisagree.iconify.features.common.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drdisagree.iconify.data.entity.DynamicResourceEntity
import com.drdisagree.iconify.data.repository.DynamicResourceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DynamicResourceViewModel @Inject constructor(
    private val repository: DynamicResourceRepository
) : ViewModel() {

    private val _resolvedResources = MutableStateFlow<List<DynamicResourceEntity>>(emptyList())
    val resolvedResources: StateFlow<List<DynamicResourceEntity>> = _resolvedResources

    private val _allRawResources = MutableStateFlow<List<DynamicResourceEntity>>(emptyList())
    val allRawResources: StateFlow<List<DynamicResourceEntity>> = _allRawResources

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<Throwable?>(null)
    val error: StateFlow<Throwable?> = _error

    fun insertResources(resources: List<DynamicResourceEntity>) = launchCatching {
        repository.insertResources(resources)
        refreshResolved()
    }

    fun deleteOverlay(overlayId: String) = launchCatching {
        repository.deleteOverlay(overlayId)
        refreshResolved()
    }

    fun deleteOverlays(vararg overlayIds: String) = launchCatching {
        repository.deleteOverlays(*overlayIds)
        refreshResolved()
    }

    fun clearAllResources() = launchCatching {
        repository.clearAllResources()
        refreshResolved()
    }

    fun refreshResolved() = launchCatching {
        _resolvedResources.value = repository.getAllLatestResources()
    }

    fun refreshRaw() = launchCatching {
        _allRawResources.value = repository.getAllResources()
    }

    fun loadResourcesForPackage(packageName: String) = launchCatching {
        _allRawResources.value =
            repository.getResourcesForPackage(packageName)
    }

    fun clearError() {
        _error.value = null
    }

    private inline fun launchCatching(
        crossinline block: suspend () -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                block()
            } catch (t: Throwable) {
                _error.value = t
            } finally {
                _isLoading.value = false
            }
        }
    }
}