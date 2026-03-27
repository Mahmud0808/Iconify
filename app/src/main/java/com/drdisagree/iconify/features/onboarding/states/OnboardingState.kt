package com.drdisagree.iconify.features.onboarding.states

sealed interface InstallationState {
    object Idle : InstallationState
    object Installing : InstallationState
    data class Progressing(val step: Int, val descRes: Int, val replace: Boolean) : InstallationState
    object Reboot : InstallationState
    object Success : InstallationState
}

sealed class InstallationEvent {
    data class Error(val titleRes: Int, val descRes: Int) : InstallationEvent()
    data class Toast(val messageRes: Int) : InstallationEvent()
}