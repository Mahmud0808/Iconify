package com.drdisagree.iconify.core.common

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.drdisagree.iconify.data.models.AppSeedColors
import com.materialkolor.Contrast
import com.materialkolor.PaletteStyle

/**
 * Snapshot-state settings holder. Each property is individually observable, so a
 * composable that reads e.g. [blurEffect] recomposes only when that property
 * changes — not when any other setting does. Provided via [LocalSettings] as a
 * static CompositionLocal: the holder reference never changes, granularity comes
 * entirely from the per-property snapshot state.
 */
@Stable
class SettingsHolder {
    var themeMode by mutableIntStateOf(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    var isDynamicColor by mutableStateOf(true)
    var seedColor by mutableLongStateOf(AppSeedColors.Blue.seedColor.primaryColor)
    var paletteStyle by mutableStateOf(PaletteStyle.TonalSpot)
    var isExpressive by mutableStateOf(false)
    var isAmoledTheme by mutableStateOf(false)
    var contrastLevel by mutableDoubleStateOf(Contrast.Default.value)
    var isHapticEnabled by mutableStateOf(true)
    var floatingBottomBar by mutableStateOf(true)
    var blurEffect by mutableStateOf(true)
    var animationsEnabled by mutableStateOf(true)
    var overlayVersionCode by mutableIntStateOf(0)
    var isXposedOnlyMode by mutableStateOf(false)
    var isPlaygroundUnlocked by mutableStateOf(false)
    var isLoaded by mutableStateOf(false)
}

val defaultSettings = SettingsHolder()

val LocalSettings = staticCompositionLocalOf { defaultSettings }
