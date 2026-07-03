package com.drdisagree.iconify.services.providers

import android.app.Activity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Density
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.drdisagree.iconify.core.common.LocalColorScheme
import com.drdisagree.iconify.core.common.LocalDarkMode
import com.drdisagree.iconify.core.common.LocalHazeState
import com.drdisagree.iconify.core.common.LocalLayerBackdrop
import com.drdisagree.iconify.core.common.LocalNavController
import com.drdisagree.iconify.core.common.LocalSettings
import com.drdisagree.iconify.core.common.LocalStrongHaptic
import com.drdisagree.iconify.core.common.LocalWeakHaptic
import com.drdisagree.iconify.core.common.LocalWindowSizeClass
import com.drdisagree.iconify.core.utils.HapticUtils.strongHaptic
import com.drdisagree.iconify.core.utils.HapticUtils.weakHaptic
import com.drdisagree.iconify.core.common.SettingsHolder
import com.drdisagree.iconify.data.keys.SettingsKey
import com.drdisagree.iconify.features.common.viewmodels.SettingsViewModel
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun AppProviders(
    activity: Activity,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    val context = LocalContext.current
    val baseDensity = LocalDensity.current

    val navController = rememberNavController()
    val windowSizeClass = calculateWindowSizeClass(activity)

    val uiScale by settingsViewModel.floatState(SettingsKey.UI_SCALE)
    val textScale by settingsViewModel.floatState(SettingsKey.TEXT_SCALE)

    val settingsHolder = remember { SettingsHolder() }

    // Collect preference flows straight into the snapshot holder. Each flow
    // emits its current value on start, and every property write invalidates
    // only the composables that read that specific property.
    LaunchedEffect(settingsViewModel) {
        launch {
            settingsViewModel.getStringFlow(SettingsKey.THEME_MODE)
                .collect { settingsHolder.themeMode = it.toInt() }
        }
        launch {
            settingsViewModel.getBooleanFlow(SettingsKey.EXPRESSIVE_COLORS)
                .collect { settingsHolder.isExpressive = it }
        }
        launch {
            settingsViewModel.getBooleanFlow(SettingsKey.AMOLED_THEME)
                .collect { settingsHolder.isAmoledTheme = it }
        }
        launch {
            settingsViewModel.getStringFlow(SettingsKey.SEED_COLOR)
                .collect { settingsHolder.seedColor = it.toLong() }
        }
        launch {
            settingsViewModel.getStringFlow(SettingsKey.PALETTE_STYLE)
                .collect { settingsHolder.paletteStyle = PaletteStyle.valueOf(it) }
        }
        launch {
            settingsViewModel.getBooleanFlow(SettingsKey.DYNAMIC_COLORS)
                .collect { settingsHolder.isDynamicColor = it }
        }
        launch {
            settingsViewModel.getStringFlow(SettingsKey.CONTRAST_LEVEL)
                .collect { settingsHolder.contrastLevel = it.toDouble() }
        }
        launch {
            settingsViewModel.getBooleanFlow(SettingsKey.HAPTICS_AND_VIBRATION)
                .collect { settingsHolder.isHapticEnabled = it }
        }
        launch {
            settingsViewModel.getBooleanFlow(SettingsKey.FLOATING_BOTTOM_BAR)
                .collect { settingsHolder.floatingBottomBar = it }
        }
        launch {
            settingsViewModel.getBooleanFlow(SettingsKey.BLUR_EFFECT)
                .collect { settingsHolder.blurEffect = it }
        }
        launch {
            settingsViewModel.getBooleanFlow(SettingsKey.ANIMATIONS)
                .collect { settingsHolder.animationsEnabled = it }
        }
        launch {
            settingsViewModel.getIntFlow(SettingsKey.OVERLAY_VERSION_CODE)
                .collect { settingsHolder.overlayVersionCode = it }
        }
        launch {
            settingsViewModel.getBooleanFlow(SettingsKey.XPOSED_ONLY_MODE)
                .collect { settingsHolder.isXposedOnlyMode = it }
        }
        launch {
            settingsViewModel.getBooleanFlow(SettingsKey.PLAYGROUND_UNLOCKED)
                .collect { settingsHolder.isPlaygroundUnlocked = it }
        }
        launch {
            settingsViewModel.isLoaded.collect { settingsHolder.isLoaded = it }
        }
    }

    val isDarkTheme = when (settingsHolder.themeMode) {
        AppCompatDelegate.MODE_NIGHT_YES -> true
        AppCompatDelegate.MODE_NIGHT_NO -> false
        else -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        settingsHolder.isDynamicColor -> when {
            isDarkTheme -> dynamicDarkColorScheme(context)
            else -> dynamicLightColorScheme(context)
        }

        else -> rememberDynamicColorScheme(
            seedColor = Color(settingsHolder.seedColor),
            isDark = isDarkTheme,
            isAmoled = settingsHolder.isAmoledTheme,
            contrastLevel = settingsHolder.contrastLevel,
            style = settingsHolder.paletteStyle,
            specVersion = if (settingsHolder.isExpressive) ColorSpec.SpecVersion.SPEC_2025
            else ColorSpec.SpecVersion.SPEC_2021,
        )
    }

    val animatedSurface by animateColorAsState(
        targetValue = colorScheme.surface,
        label = "backdropSurface"
    )

    val hazeState = remember { HazeState() }
    val backdrop = rememberLayerBackdrop {
        drawRect(animatedSurface)
        drawContent()
    }

    val scaledDensity = remember(uiScale, textScale, baseDensity) {
        Density(
            density = baseDensity.density * uiScale,
            fontScale = baseDensity.fontScale * uiScale * textScale
        )
    }

    // Haptic enablement is read inside the lambdas at invocation time, so
    // toggling the setting doesn't recompose anything.
    val weakHaptic = remember(settingsHolder, view) {
        {
            if (settingsHolder.isHapticEnabled) {
                view.weakHaptic()
            }
        }
    }

    val strongHaptic = remember(settingsHolder, view) {
        {
            if (settingsHolder.isHapticEnabled) {
                view.strongHaptic()
            }
        }
    }

    ProvideSharedPreferencesController {
        CompositionLocalProvider(
            LocalHazeState provides hazeState,
            LocalLayerBackdrop provides backdrop,
            LocalNavController provides navController,
            LocalSettings provides settingsHolder,
            LocalColorScheme provides colorScheme,
            LocalWeakHaptic provides weakHaptic,
            LocalStrongHaptic provides strongHaptic,
            LocalDarkMode provides isDarkTheme,
            LocalWindowSizeClass provides windowSizeClass,
            LocalDensity provides scaledDensity,
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsViewModel.floatState(key: SettingsKey): androidx.compose.runtime.State<Float> {
    return getFloatFlow(key).collectAsState(initial = key.default as Float)
}