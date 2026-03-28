package com.drdisagree.iconify.features.home.main.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
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

@Composable
fun HomeBannerCard(modifier: Modifier = Modifier) {
    val shape = MaterialTheme.shapes.large
    val shakeController = rememberShakeController()
    val interactionSource = remember { MutableInteractionSource() }

    val isPressed by interactionSource.collectIsPressedAsState()

    var touchX by remember { mutableFloatStateOf(0.5f) }
    var touchY by remember { mutableFloatStateOf(0.5f) }

    val edgeThreshold = 0.3f
    val nearLeft = touchX < edgeThreshold
    val nearRight = touchX > 1f - edgeThreshold
    val nearTop = touchY < edgeThreshold
    val nearBottom = touchY > 1f - edgeThreshold
    val isCorner = (nearLeft || nearRight) && (nearTop || nearBottom)
    val isSide = (nearLeft || nearRight || nearTop || nearBottom) && !isCorner
    val maxTilt = 6f

    val tiltX by animateFloatAsState(
        targetValue = if (isPressed) -(touchY - 0.5f) * 2f * maxTilt else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "tiltX"
    )

    val tiltY by animateFloatAsState(
        targetValue = if (isPressed) (touchX - 0.5f) * 2f * maxTilt else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "tiltY"
    )
    val scaleTarget = when {
        !isPressed -> 1f
        isCorner -> 0.97f
        isSide -> 0.98f
        else -> 0.99f
    }
    val scale by animateFloatAsState(
        targetValue = scaleTarget,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )

    val onTapAction = withHaptic {
        shakeController.shake(
            ShakeConfig(
                iterations = 4,
                intensity = 2_000f,
                rotateY = 2f,
                translateX = 5f,
            )
        )
    }

    Card(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        // Update position
                        touchX = (offset.x / size.width).coerceIn(0f, 1f)
                        touchY = (offset.y / size.height).coerceIn(0f, 1f)

                        // Emit press to interactionSource
                        val press = PressInteraction.Press(offset)
                        interactionSource.emit(press)

                        val released = tryAwaitRelease()

                        if (released) {
                            interactionSource.emit(PressInteraction.Release(press))
                        } else {
                            interactionSource.emit(PressInteraction.Cancel(press))
                        }
                    },
                    onTap = { onTapAction() }
                )
            }
            .shake(shakeController)
            .fillMaxWidth()
            .graphicsLayer {
                rotationX = tiltX
                rotationY = tiltY
                scaleX = scale
                scaleY = scale
                cameraDistance = 14f * density

                // Shift toward pressed corner instead of changing pivot
                // so the held corner goes "inward" without clipping opposite side
                val shiftAmount = 6f
                translationX = (touchX - 0.5f) * shiftAmount
                translationY = (touchY - 0.5f) * shiftAmount

                transformOrigin = TransformOrigin.Center
            },
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
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
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 30.sp),
                    fontSize = 30.sp
                )
                AutoResizeableText(
                    text = stringResource(id = R.string.home_card_subtitle),
                    maxLines = 1,
                    color = Color.Black.secondaryText(),
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 12.sp),
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeBannerCardPreview() {
    HomeBannerCard()
}