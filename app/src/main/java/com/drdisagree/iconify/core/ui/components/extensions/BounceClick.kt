package com.drdisagree.iconify.core.ui.components.extensions

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import com.drdisagree.iconify.core.common.LocalSettings

/**
 * Scales the component down slightly while pressed and springs back on release.
 * Purely additive: it never consumes pointer events, so it composes with any
 * clickable/toggleable placed on the same chain. Returns the receiver unchanged
 * when the global animation toggle is off.
 *
 * The animated scale is read inside [graphicsLayer], so the press animation
 * runs entirely in the draw phase without recomposing the component.
 */
@Composable
fun Modifier.bounceClick(pressedScale: Float = 0.96f): Modifier {
    val animationsEnabled = LocalSettings.current.animationsEnabled
    if (!animationsEnabled) return this

    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bounceScale"
    )

    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false)
                pressed = true
                waitForUpOrCancellation()
                pressed = false
            }
        }
}
