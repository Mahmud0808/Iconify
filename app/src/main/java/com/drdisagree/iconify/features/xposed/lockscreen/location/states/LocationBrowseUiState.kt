package com.drdisagree.iconify.features.xposed.lockscreen.location.states

import com.drdisagree.iconify.features.xposed.lockscreen.location.models.LocationBrowseItem

data class LocationBrowseUiState(
    val query: String = "",
    val locations: List<LocationBrowseItem> = emptyList(),
    val isLoading: Boolean = false
)