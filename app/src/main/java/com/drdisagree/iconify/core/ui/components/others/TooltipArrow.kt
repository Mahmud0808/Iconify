package com.drdisagree.iconify.core.ui.components.others

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

class TooltipArrowShape(
    private val arrowCornerRadius: Dp = 4.dp
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val centerX = size.width / 2f
        val arrowCorner = with(density) { arrowCornerRadius.toPx() }

        val path = Path().apply {
            moveTo(centerX - size.width / 2f, size.height)
            quadraticTo(
                centerX - arrowCorner / 2f, 0f,
                centerX, 0f
            )
            quadraticTo(
                centerX + arrowCorner / 2f, 0f,
                centerX + size.width / 2f, size.height
            )
            close()
        }

        return Outline.Generic(path)
    }
}

@Composable
fun TooltipArrow(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    arrowWidth: Dp = 12.dp,
    arrowHeight: Dp = 6.dp,
    arrowCornerRadius: Dp = 4.dp,
    onClick: (() -> Unit)? = null,
    interactionSource: MutableInteractionSource? = null
) {
    val density = LocalDensity.current
    val shape = remember(arrowCornerRadius) { TooltipArrowShape(arrowCornerRadius) }
    val resolvedInteractionSource = interactionSource ?: remember { MutableInteractionSource() }

    Canvas(
        modifier = modifier
            .offset(y = 0.5.dp)
            .size(width = arrowWidth, height = arrowHeight + 1.dp)
            .clip(shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        onClick = onClick,
                        interactionSource = resolvedInteractionSource,
                        indication = ripple()
                    )
                } else if (interactionSource != null) Modifier.indication(
                    interactionSource = resolvedInteractionSource,
                    indication = ripple()
                ) else Modifier
            )
    ) {
        val w = size.width
        val h = size.height
        val centerX = w / 2f

        val arrowW = size.width
        val arrowH = size.height
        val arrowCorner = with(density) { arrowCornerRadius.toPx() }

        val path = Path().apply {
            moveTo(centerX - arrowW / 2f, arrowH)
            quadraticTo(
                centerX - arrowCorner / 2f, 0f,
                centerX, 0f
            )
            quadraticTo(
                centerX + arrowCorner / 2f, 0f,
                centerX + arrowW / 2f, arrowH
            )
            close()
        }

        drawPath(path, color = backgroundColor)
    }
}


@Preview
@Composable
fun TooltipArrowPreview() {
    TooltipArrow()
}