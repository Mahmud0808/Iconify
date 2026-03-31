package com.drdisagree.iconify.core.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController
import com.drdisagree.iconify.core.preferences.PreferenceController
import com.kyant.backdrop.backdrops.LayerBackdrop
import dev.chrisbanes.haze.HazeState

val LocalWeakHaptic = staticCompositionLocalOf { {} }

val LocalStrongHaptic = staticCompositionLocalOf { {} }

val LocalDarkMode = staticCompositionLocalOf<Boolean> {
    error("No dark mode provided")
}

val LocalPreferenceController = compositionLocalOf<PreferenceController> {
    error("No PreferenceController provided. Wrap your UI in ProvidePreferenceController { }.")
}

val LocalLayerBackdrop = staticCompositionLocalOf<LayerBackdrop> {
    error("No LayerBackdrop provided")
}

val LocalHazeState = staticCompositionLocalOf<HazeState> {
    error("No HazeState provided")
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