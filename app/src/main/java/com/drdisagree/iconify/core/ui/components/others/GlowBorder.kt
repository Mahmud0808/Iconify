package com.drdisagree.iconify.core.ui.components.others

import android.annotation.SuppressLint
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.withSaveLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.materialkolor.ktx.harmonize
import kotlin.math.pow
import kotlin.math.sqrt

@Composable
fun GlowBorder(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    content: @Composable () -> Unit
) {
    val red400 = Color(0xFFEF5350).harmonize(MaterialTheme.colorScheme.primary)
    val yellow500 = Color(0xFFFFEB3B).harmonize(MaterialTheme.colorScheme.primary)
    val green200 = Color(0xFFA5D6A7).harmonize(MaterialTheme.colorScheme.primary)
    val blue300 = Color(0xFF64B5F6).harmonize(MaterialTheme.colorScheme.primary)

    val red200 = Color(0xFFEF9A9A).harmonize(MaterialTheme.colorScheme.primary)
    val yellow200 = Color(0xFFFFF59D).harmonize(MaterialTheme.colorScheme.primary)
    val blue200 = Color(0xFF90CAF9).harmonize(MaterialTheme.colorScheme.primary)

    val rotation by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 3000,
                easing = LinearEasing,
            )
        )
    )

    Box(
        modifier = modifier.clip(shape),
        contentAlignment = Alignment.Center
    ) {
        @SuppressLint("UnusedBoxWithConstraintsScope")
        BoxWithConstraints {
            MaskBox(
                modifier = Modifier
                    .matchParentSize()
                    .blur(20.dp),
                overlay = {
                    rotate(
                        degrees = rotation
                    ) {
                        drawCircle(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Transparent,
                                    red400,
                                    yellow500,
                                    green200,
                                    blue300,
                                    Color.Transparent,
                                )
                            ),
                            radius = diagonal(
                                size.width.toDouble(),
                                size.height.toDouble()
                            ).toFloat(),
                        )
                    }
                },
                content = {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .dropShadow(
                                shape = shape,
                            ) {
                                alpha = .5f
                                radius = 40f
                                color = Color.White
                            }
                            .dropShadow(
                                shape = shape,
                            ) {
                                alpha = .2f
                                spread = 50f
                                radius = 400f
                                color = Color.White
                            }
                            .border(
                                width = 1.dp,
                                color = Color.White,
                                shape = shape,
                            )
                    )

                }
            )

            MaskBox(
                modifier = Modifier
                    .zIndex(10f)
                    .matchParentSize()
                    .blur(20.dp),
                overlay = {
                    rotate(
                        degrees = rotation
                    ) {
                        drawCircle(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Transparent,
                                    red200,
                                    yellow200,
                                    green200,
                                    blue200,
                                    Color.Transparent,
                                )
                            ),
                            radius = diagonal(
                                size.width.toDouble(),
                                size.height.toDouble()
                            ).toFloat(),
                        )
                    }

                },
                content = {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .border(
                                width = 1.dp,
                                color = Color.White,
                                shape = shape,
                            )
                    )
                }
            )

            Box(
                modifier = Modifier
                    .padding(2.dp)
                    .zIndex(25f)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun MaskBox(
    modifier: Modifier = Modifier,
    overlay: DrawScope.() -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    val graphicsLayer = rememberGraphicsLayer()
    Box(
        modifier
            .drawWithContent {
                graphicsLayer.record {
                    this@drawWithContent.drawContent()
                }
                graphicsLayer.blendMode = BlendMode.DstIn
                layer(
                    bounds = Rect(
                        center = center,
                        radius = size.width * size.height,
                    )
                ) {
                    overlay()
                    drawLayer(graphicsLayer)
                }
            },
        content = content,
    )
}

private fun DrawScope.layer(
    bounds: Rect = Rect(size.center, size.width * size.height),
    block: DrawScope.() -> Unit
) = drawIntoCanvas { canvas ->
    canvas.withSaveLayer(
        bounds = bounds,
        paint = Paint(),
    ) { block() }
}

private fun diagonal(width: Double, height: Double): Double {
    return sqrt(width.pow(2) + height.pow(2))
}