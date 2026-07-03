package com.drdisagree.iconify.core.ui.components.extensions

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Soft, tinted drop shadow drawn behind the component. Unlike Material
 * elevation this allows a colored ambient glow, giving cards and floating
 * elements an elevated, glassy feel. Purely decorative and static — not
 * gated by the animation toggle.
 */
fun Modifier.softShadow(
    color: Color,
    blurRadius: Dp = 20.dp,
    offsetY: Dp = 6.dp,
    cornerRadius: Dp = 28.dp,
    alpha: Float = 0.25f,
): Modifier = drawBehind {
    if (blurRadius <= 0.dp || alpha <= 0f) return@drawBehind

    drawIntoCanvas { canvas ->
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()
        frameworkPaint.color = android.graphics.Color.TRANSPARENT
        frameworkPaint.setShadowLayer(
            blurRadius.toPx(),
            0f,
            offsetY.toPx(),
            color.copy(alpha = alpha).toArgb()
        )
        canvas.drawRoundRect(
            left = 0f,
            top = 0f,
            right = size.width,
            bottom = size.height,
            radiusX = cornerRadius.toPx(),
            radiusY = cornerRadius.toPx(),
            paint = paint
        )
    }
}
