package com.drdisagree.iconify.core.ui.components.others

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
fun DecorativeShape(
    size: Int,
    shape: Shape,
    color: Color,
    scale: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .graphicsLayer {
                rotationZ = 15f
                scaleX = scale
                scaleY = scale
                this.shape = shape
                clip = true
            }
            .background(color)
    )
}