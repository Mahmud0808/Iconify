package com.drdisagree.iconify.core.ui.theme

import android.app.Activity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.drdisagree.iconify.core.common.LocalDarkMode
import com.drdisagree.iconify.core.common.LocalSettings
import com.drdisagree.iconify.data.states.SettingsState
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme

@Composable
private fun ColorScheme.animate(
    spec: AnimationSpec<Color> = tween(durationMillis = 400, easing = FastOutSlowInEasing)
): ColorScheme {
    @Composable
    fun Color.anim() = animateColorAsState(this, spec).value
    return copy(
        primary = primary.anim(),
        onPrimary = onPrimary.anim(),
        primaryContainer = primaryContainer.anim(),
        onPrimaryContainer = onPrimaryContainer.anim(),
        secondary = secondary.anim(),
        onSecondary = onSecondary.anim(),
        secondaryContainer = secondaryContainer.anim(),
        onSecondaryContainer = onSecondaryContainer.anim(),
        tertiary = tertiary.anim(),
        onTertiary = onTertiary.anim(),
        tertiaryContainer = tertiaryContainer.anim(),
        onTertiaryContainer = onTertiaryContainer.anim(),
        error = error.anim(),
        onError = onError.anim(),
        errorContainer = errorContainer.anim(),
        onErrorContainer = onErrorContainer.anim(),
        background = background.anim(),
        onBackground = onBackground.anim(),
        surface = surface.anim(),
        onSurface = onSurface.anim(),
        surfaceVariant = surfaceVariant.anim(),
        onSurfaceVariant = onSurfaceVariant.anim(),
        surfaceTint = surfaceTint.anim(),
        surfaceBright = surfaceBright.anim(),
        surfaceDim = surfaceDim.anim(),
        surfaceContainer = surfaceContainer.anim(),
        surfaceContainerLow = surfaceContainerLow.anim(),
        surfaceContainerLowest = surfaceContainerLowest.anim(),
        surfaceContainerHigh = surfaceContainerHigh.anim(),
        surfaceContainerHighest = surfaceContainerHighest.anim(),
        inverseSurface = inverseSurface.anim(),
        inverseOnSurface = inverseOnSurface.anim(),
        inversePrimary = inversePrimary.anim(),
        outline = outline.anim(),
        outlineVariant = outlineVariant.anim(),
        scrim = scrim.anim(),
    )
}

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun getIsDarkTheme(
    settingsState: SettingsState? = null
): Boolean {
    val settings = settingsState ?: LocalSettings.current
    val themeMode = settings.themeMode

    return when (themeMode) {
        AppCompatDelegate.MODE_NIGHT_YES -> true
        AppCompatDelegate.MODE_NIGHT_NO -> false
        else -> isSystemInDarkTheme()
    }
}

@Composable
fun getColorScheme(
    settingsState: SettingsState? = null
): ColorScheme {
    val context = LocalContext.current
    val settings = settingsState ?: LocalSettings.current
    val isDynamicColor = settings.isDynamicColor
    val seedColor = settings.seedColor
    val themeMode = settings.themeMode
    val paletteStyle = settings.paletteStyle
    val amoledMode = settings.isAmoledTheme
    val contrastLevel = settings.contrastLevel
    val isExpressive = settings.isExpressive

    val isDarkTheme = when (themeMode) {
        AppCompatDelegate.MODE_NIGHT_YES -> true
        AppCompatDelegate.MODE_NIGHT_NO -> false
        else -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        isDynamicColor -> {
            when {
                isDarkTheme -> dynamicDarkColorScheme(context)
                else -> dynamicLightColorScheme(context)
            }
        }

        else -> rememberDynamicColorScheme(
            seedColor = Color(seedColor),
            isDark = isDarkTheme,
            isAmoled = amoledMode,
            contrastLevel = contrastLevel,
            style = paletteStyle,
            specVersion = if (isExpressive) ColorSpec.SpecVersion.SPEC_2025
            else ColorSpec.SpecVersion.SPEC_2021,
        )
    }
    return colorScheme
}

@Composable
fun MyAppTheme(
    darkTheme: Boolean = LocalDarkMode.current,
    content: @Composable () -> Unit
) {
    val view = LocalView.current

    val targetScheme = getColorScheme()
    var hasRenderedOnce by rememberSaveable { mutableStateOf(false) }

    val colorScheme = if (hasRenderedOnce) {
        targetScheme.animate()
    } else {
        targetScheme
    }

    LaunchedEffect(Unit) {
        hasRenderedOnce = true
    }

    if (!view.isInEditMode) {
        val window = (view.context as Activity).window

        SideEffect {
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}