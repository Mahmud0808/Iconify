package com.drdisagree.iconify.core.common

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController
import com.drdisagree.iconify.core.preferences.PreferenceController
import com.kyant.backdrop.backdrops.LayerBackdrop
import dev.chrisbanes.haze.HazeState

val LocalWeakHaptic = compositionLocalOf { {} }

val LocalStrongHaptic = compositionLocalOf { {} }

val LocalDarkMode = compositionLocalOf<Boolean> {
    error("No dark mode provided")
}

val LocalPreferenceController = staticCompositionLocalOf<PreferenceController> {
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

val LocalInnerPadding = compositionLocalOf<PaddingValues> {
    error("No PaddingValues provided")
}

val LocalWindowSizeClass = compositionLocalOf<WindowSizeClass> {
    error("No WindowSizeClass provided")
}

val LocalColorScheme = compositionLocalOf<ColorScheme> {
    error("No ColorScheme provided ")
}

val LocalSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope?> { null }

val LocalNavAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }
