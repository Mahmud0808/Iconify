package com.drdisagree.iconify.core.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController
import com.drdisagree.iconify.core.preferences.PreferenceController
import com.drdisagree.iconify.data.models.AppSeedColors
import com.kyant.backdrop.backdrops.LayerBackdrop

val LocalWeakHaptic = staticCompositionLocalOf { {} }

val LocalStrongHaptic = staticCompositionLocalOf { {} }

val LocalDarkMode = staticCompositionLocalOf<Boolean> {
    error("No dark mode provided")
}

val LocalSeedColor = staticCompositionLocalOf<Long> {
    error("No seed color provided")
}

val LocalTonalPalette = staticCompositionLocalOf<List<AppSeedColors>> {
    error("No tonal palette provided")
}

val LocalPreferenceController = compositionLocalOf<PreferenceController> {
    error("No PreferenceController provided. Wrap your UI in ProvidePreferenceController { }.")
}

val LocalLayerBackdrop = staticCompositionLocalOf<LayerBackdrop> {
    error("No LayerBackdrop provided")
}

val LocalNavController = staticCompositionLocalOf<NavHostController> {
    error("No NavController provided")
}

val LocalInnerPadding = staticCompositionLocalOf<PaddingValues> {
    error("No PaddingValues provided")
}

val LocalWindowSizeClass = staticCompositionLocalOf<WindowSizeClass> {
    error("No WindowSizeClass provided")
}