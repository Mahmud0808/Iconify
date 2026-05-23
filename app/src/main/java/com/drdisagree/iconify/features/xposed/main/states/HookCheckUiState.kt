package com.drdisagree.iconify.features.xposed.main.states

data class HookCheckUiState(
    val isHooked: Boolean = false,
    val hasBootlooped: Boolean = false,
    val hookCheckCompleted: Boolean = false
)