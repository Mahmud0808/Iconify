package com.drdisagree.iconify.core.ui.utils

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.drdisagree.iconify.core.common.LocalSettings

/**
 * [animateColorAsState] that respects the global animation toggle: returns the
 * target color directly when animations are disabled.
 */
@Composable
fun animateColorGated(targetValue: Color, label: String = "colorGated"): Color {
    return if (LocalSettings.current.animationsEnabled) {
        animateColorAsState(targetValue = targetValue, label = label).value
    } else {
        targetValue
    }
}

/**
 * [animateDpAsState] that respects the global animation toggle.
 */
@Composable
fun animateDpGated(targetValue: Dp, label: String = "dpGated"): Dp {
    return if (LocalSettings.current.animationsEnabled) {
        animateDpAsState(targetValue = targetValue, label = label).value
    } else {
        targetValue
    }
}
