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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import com.drdisagree.iconify.data.keys.SettingsKey
import com.drdisagree.iconify.data.states.SettingsState
import com.drdisagree.iconify.features.common.viewmodels.SettingsViewModel
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme
import dev.chrisbanes.haze.HazeState

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

    val themeMode by settingsViewModel.stringState(SettingsKey.THEME_MODE)
    val isHapticEnabled by settingsViewModel.booleanState(SettingsKey.HAPTICS_AND_VIBRATION)
    val seedColor by settingsViewModel.stringState(SettingsKey.SEED_COLOR)
    val isDynamicColor by settingsViewModel.booleanState(SettingsKey.DYNAMIC_COLORS)
    val paletteStyle by settingsViewModel.stringState(SettingsKey.PALETTE_STYLE)
    val isExpressive by settingsViewModel.booleanState(SettingsKey.EXPRESSIVE_COLORS)
    val isAmoledTheme by settingsViewModel.booleanState(SettingsKey.AMOLED_THEME)
    val contrastLevel by settingsViewModel.stringState(SettingsKey.CONTRAST_LEVEL)
    val floatingBottomBar by settingsViewModel.booleanState(SettingsKey.FLOATING_BOTTOM_BAR)
    val blurEffect by settingsViewModel.booleanState(SettingsKey.BLUR_EFFECT)
    val overlayVersionCode by settingsViewModel.intState(SettingsKey.OVERLAY_VERSION_CODE)
    val isXposedOnlyMode by settingsViewModel.booleanState(SettingsKey.XPOSED_ONLY_MODE)
    val isPlaygroundUnlocked by settingsViewModel.booleanState(SettingsKey.PLAYGROUND_UNLOCKED)
    val uiScale by settingsViewModel.floatState(SettingsKey.UI_SCALE)
    val textScale by settingsViewModel.floatState(SettingsKey.TEXT_SCALE)
    val isSettingsLoaded by settingsViewModel.isLoaded.collectAsState()

    val state by remember {
        derivedStateOf {
            SettingsState(
                themeMode = themeMode.toInt(),
                isExpressive = isExpressive,
                isAmoledTheme = isAmoledTheme,
                seedColor = seedColor.toLong(),
                paletteStyle = PaletteStyle.valueOf(paletteStyle),
                isDynamicColor = isDynamicColor,
                contrastLevel = contrastLevel.toDouble(),
                isHapticEnabled = isHapticEnabled,
                floatingBottomBar = floatingBottomBar,
                blurEffect = blurEffect,
                overlayVersionCode = overlayVersionCode,
                isXposedOnlyMode = isXposedOnlyMode,
                isPlaygroundUnlocked = isPlaygroundUnlocked,
                isLoaded = isSettingsLoaded,
            )
        }
    }

    val isDarkTheme = when (themeMode.toInt()) {
        AppCompatDelegate.MODE_NIGHT_YES -> true
        AppCompatDelegate.MODE_NIGHT_NO -> false
        else -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        isDynamicColor -> when {
            isDarkTheme -> dynamicDarkColorScheme(context)
            else -> dynamicLightColorScheme(context)
        }

        else -> rememberDynamicColorScheme(
            seedColor = Color(seedColor.toLong()),
            isDark = isDarkTheme,
            isAmoled = isAmoledTheme,
            contrastLevel = contrastLevel.toDouble(),
            style = PaletteStyle.valueOf(paletteStyle),
            specVersion = if (isExpressive) ColorSpec.SpecVersion.SPEC_2025
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

    val weakHaptic = remember(isHapticEnabled, view) {
        {
            if (isHapticEnabled) {
                view.weakHaptic()
            }
        }
    }

    val strongHaptic = remember(isHapticEnabled, view) {
        {
            if (isHapticEnabled) {
                view.strongHaptic()
            }
        }
    }

    ProvideSharedPreferencesController {
        CompositionLocalProvider(
            LocalHazeState provides hazeState,
            LocalLayerBackdrop provides backdrop,
            LocalNavController provides navController,
            LocalSettings provides state,
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
private fun SettingsViewModel.booleanState(key: SettingsKey): androidx.compose.runtime.State<Boolean> {
    return getBooleanFlow(key).collectAsState(initial = key.default as Boolean)
}

@Composable
private fun SettingsViewModel.intState(key: SettingsKey): androidx.compose.runtime.State<Int> {
    return getIntFlow(key).collectAsState(initial = key.default as Int)
}

@Composable
private fun SettingsViewModel.floatState(key: SettingsKey): androidx.compose.runtime.State<Float> {
    return getFloatFlow(key).collectAsState(initial = key.default as Float)
}

@Composable
private fun SettingsViewModel.stringState(key: SettingsKey): androidx.compose.runtime.State<String> {
    return getStringFlow(key).collectAsState(initial = key.default as String)
}