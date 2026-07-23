package com.drdisagree.iconify.core.ui.theme

import android.app.Activity
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.drdisagree.iconify.core.common.LocalColorScheme
import com.drdisagree.iconify.core.common.LocalDarkMode
import com.drdisagree.iconify.core.common.LocalSettings
import com.materialkolor.ktx.animateColorScheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MyAppTheme(
    darkTheme: Boolean = LocalDarkMode.current,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    val settings = LocalSettings.current
    val targetScheme = LocalColorScheme.current

    var animate by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        animate = true
    }

    val colorScheme = if (animate && settings.animationsEnabled) {
        animateColorScheme(colorScheme = targetScheme)
    } else {
        targetScheme
    }

    if (!view.isInEditMode) {
        val window = (view.context as Activity).window

        SideEffect {
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        motionScheme = if (settings.isExpressive) {
            MotionScheme.expressive()
        } else {
            MotionScheme.standard()
        },
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
