package com.drdisagree.iconify.data.states

import com.materialkolor.PaletteStyle

data class SettingsState(
    val themeMode: Int,
    val isDynamicColor: Boolean,
    val seedColor: Long,
    val paletteStyle: PaletteStyle,
    val isExpressive: Boolean,
    val isAmoledTheme: Boolean,
    val contrastLevel: Double,
    val isHapticEnabled: Boolean,
    val floatingBottomBar: Boolean,
    val blurEffect: Boolean,
    val overlayVersionCode: Int,
    val isXposedOnlyMode: Boolean,
    val isPlaygroundUnlocked: Boolean,
    val isLoaded: Boolean,
)