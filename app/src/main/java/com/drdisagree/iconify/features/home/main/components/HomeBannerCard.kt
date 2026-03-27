package com.drdisagree.iconify.features.home.main.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.innerShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.ui.components.extensions.ShakeConfig
import com.drdisagree.iconify.core.ui.components.extensions.rememberShakeController
import com.drdisagree.iconify.core.ui.components.extensions.secondaryText
import com.drdisagree.iconify.core.ui.components.extensions.shake
import com.drdisagree.iconify.core.ui.components.others.withHaptic
import com.drdisagree.iconify.core.ui.components.texts.AutoResizeableText
import com.materialkolor.ktx.harmonize
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HomeBannerCard(modifier: Modifier = Modifier) {
    val shape = MaterialTheme.shapes.large
    val shakeController = rememberShakeController()
    val infiniteTransition = rememberInfiniteTransition(label = "glow")

    val red400 = Color(0xFFEF5350).harmonize(MaterialTheme.colorScheme.primary)
    val yellow500 = Color(0xFFFFEB3B).harmonize(MaterialTheme.colorScheme.secondary)
    val green200 = Color(0xFFA5D6A7).harmonize(MaterialTheme.colorScheme.tertiary)
    val blue300 = Color(0xFF64B5F6).harmonize(MaterialTheme.colorScheme.tertiary)
    val red200 = Color(0xFFEF9A9A).harmonize(MaterialTheme.colorScheme.primary)
    val yellow200 = Color(0xFFFFF59D).harmonize(MaterialTheme.colorScheme.secondary)
    val blue200 = Color(0xFF90CAF9).harmonize(MaterialTheme.colorScheme.tertiary)

    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "glowAngle"
    )

    val glowBrush = Brush.linearGradient(
        colors = listOf(
            Color.Transparent,
            red400,
            yellow500,
            green200,
            blue300,
        ),
        start = angleToOffset(angle, size = 300f),
        end = angleToOffset(angle + 180f, size = 300f)
    )

    val borderBrush = Brush.linearGradient(
        colors = listOf(
            Color.Transparent,
            red200,
            yellow200,
            green200,
            blue200,
        ),
        start = angleToOffset(angle, size = 300f),
        end = angleToOffset(angle + 180f, size = 300f)
    )

    Card(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = withHaptic {
                    shakeController.shake(
                        ShakeConfig(
                            iterations = 4,
                            intensity = 2_000f,
                            rotateY = 2f,
                            translateX = 5f,
                        )
                    )
                }
            )
            .shake(shakeController)
            .fillMaxWidth()
            .dropShadow(shape = shape) {
                radius = 30f
                color = red200
                brush = glowBrush
            }
            .border(width = 1.dp, shape = shape, brush = borderBrush)
            .innerShadow(shape = shape) {
                radius = 45f
                color = red400
                brush = glowBrush
                alpha = .4f
            }
            .clip(shape),
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_home_card_bg),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter,
                modifier = Modifier.matchParentSize()
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(36.dp),
                verticalArrangement = Arrangement.Center
            ) {
                AutoResizeableText(
                    text = stringResource(id = R.string.home_card_title),
                    maxLines = 1,
                    color = Color.Black,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 30.sp,
                    ),
                    fontSize = 30.sp
                )

                AutoResizeableText(
                    text = stringResource(id = R.string.home_card_subtitle),
                    maxLines = 1,
                    color = Color.Black.secondaryText(),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontSize = 12.sp
                    ),
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }
}

// Converts a degree angle to a gradient Offset within a square of `size`
@Suppress("SameParameterValue")
private fun angleToOffset(angleDeg: Float, size: Float): Offset {
    val rad = Math.toRadians(angleDeg.toDouble())
    val cx = size / 2f
    val cy = size / 2f
    return Offset(
        x = (cx + cx * cos(rad)).toFloat(),
        y = (cy + cy * sin(rad)).toFloat()
    )
}

@Preview(showBackground = true)
@Composable
fun HomeBannerCardPreview() {
    HomeBannerCard()
}