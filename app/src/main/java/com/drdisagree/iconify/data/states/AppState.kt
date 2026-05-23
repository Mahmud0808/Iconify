package com.drdisagree.iconify.data.states

sealed class AppState {
    data object Loading : AppState()
    data class Ready(val skipOnboarding: Boolean) : AppState()
}