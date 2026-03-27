package com.drdisagree.iconify.core.common

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.compositionLocalOf
import com.drdisagree.iconify.data.models.AppSeedColors
import com.drdisagree.iconify.data.states.SettingsState
import com.materialkolor.Contrast
import com.materialkolor.PaletteStyle

val defaultSettings = SettingsState(
    themeMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
    isDynamicColor = true,
    seedColor = AppSeedColors.Blue.seedColor.primaryColor,
    paletteStyle = PaletteStyle.TonalSpot,
    isExpressive = false,
    isAmoledTheme = false,
    contrastLevel = Contrast.Default.value,
    isHapticEnabled = true,
    floatingBottomBar = true,
    blurEffect = true,
    savedVersionCode = 0,
    isLoaded = false,
)

val LocalSettings = compositionLocalOf { defaultSettings }