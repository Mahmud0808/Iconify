package com.drdisagree.iconify.core.ui.components.others

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SweepGradientShader
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.rotate
import androidx.compose.ui.graphics.withSave
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AnimatedGradientBorder(
    modifier: Modifier = Modifier,
    borderWidth: Dp = 2.dp,
    shape: Shape = RoundedCornerShape(12.dp),
    gradientColors: List<Color> = listOf(Color.Cyan, Color.Magenta, Color.Yellow, Color.Cyan),
    animationDurationMs: Int = 2000,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "border_rotation")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = animationDurationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gradient_angle"
    )

    Box(
        modifier = modifier.drawWithContent {
            val strokeWidth = borderWidth.toPx()
            val outline = shape.createOutline(size, layoutDirection, this)
            val path = Path().apply { addOutline(outline) }

            drawIntoCanvas { canvas ->
                canvas.withSave {
                    canvas.clipPath(path)

                    val innerPath = Path().apply {
                        addOutline(
                            shape.createOutline(
                                Size(
                                    size.width - strokeWidth * 2,
                                    size.height - strokeWidth * 2
                                ),
                                layoutDirection,
                                this@drawWithContent
                            )
                        )
                        val matrix = Matrix()
                        matrix.translate(strokeWidth, strokeWidth)
                        transform(matrix)
                    }

                    canvas.clipPath(innerPath, ClipOp.Difference)

                    canvas.withSave {
                        canvas.rotate(angle, size.width / 2f, size.height / 2f)

                        val paint = Paint().apply {
                            shader = SweepGradientShader(
                                colors = gradientColors,
                                center = Offset(size.width / 2f, size.height / 2f),
                                colorStops = null
                            )
                        }

                        canvas.drawRect(
                            Rect(
                                -size.width,
                                -size.height,
                                size.width * 2,
                                size.height * 2
                            ),
                            paint
                        )
                    }
                }
            }

            drawContent()
        }
    ) {
        Box(modifier = Modifier.padding(borderWidth)) {
            content()
        }
    }
}