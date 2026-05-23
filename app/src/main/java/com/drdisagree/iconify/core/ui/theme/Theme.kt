package com.drdisagree.iconify.core.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
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
import com.materialkolor.ktx.animateColorScheme

@Composable
fun MyAppTheme(
    darkTheme: Boolean = LocalDarkMode.current,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    val targetScheme = LocalColorScheme.current

    var animate by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        animate = true
    }

    val colorScheme = if (animate) {
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}