package com.drdisagree.iconify.features.home.main.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.common.LocalDarkMode
import com.drdisagree.iconify.core.ui.components.extensions.ShakeConfig
import com.drdisagree.iconify.core.ui.components.extensions.rememberShakeController
import com.drdisagree.iconify.core.ui.components.extensions.secondaryText
import com.drdisagree.iconify.core.ui.components.extensions.shake
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable
import com.drdisagree.iconify.core.ui.components.others.withHaptic
import com.drdisagree.iconify.core.ui.components.texts.AutoResizeableText
import kotlin.math.abs

@Composable
fun HomeBannerCard(modifier: Modifier = Modifier) {
    val darkMode = LocalDarkMode.current

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

    val isVerticalSide = isSide && (nearTop || nearBottom)
    val isHorizontalSide = isSide && (nearLeft || nearRight)
    val isCenter = !isSide && !isCorner

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
        else -> 0.96f
    }
    val scale by animateFloatAsState(
        targetValue = scaleTarget,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    val shapeCorner by animateDpAsState(
        targetValue = if (isPressed && !isCorner && !isSide) 48.dp else 28.dp,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = Spring.StiffnessLow
        ),
        label = "shape"
    )
    val onTapAction = withHaptic {
        when {
            isCenter || !isPressed -> {}

            isVerticalSide -> {
                shakeController.shake(
                    ShakeConfig(
                        iterations = 4,
                        intensity = 2_000f,
                        translateY = 8f,
                        rotateY = 0f,
                        rotateX = 2f
                    )
                )
            }

            else -> {
                shakeController.shake(
                    ShakeConfig(
                        iterations = 4,
                        intensity = 2_000f,
                        translateX = 8f,
                        rotateY = 2f,
                        rotateX = 0f
                    )
                )
            }
        }
    }

    Card(
        modifier = modifier
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val press = PressInteraction.Press(down.position)
                    interactionSource.tryEmit(press) // Safe in pointerInput

                    var released = false
                    var cancelled = false
                    var isScrolling = false
                    val startX = down.position.x
                    val startY = down.position.y
                    val scrollThreshold = viewConfiguration.touchSlop

                    touchX = (startX / size.width).coerceIn(0f, 1f)
                    touchY = (startY / size.height).coerceIn(0f, 1f)

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull() ?: break
                        val dx = change.position.x - startX
                        val dy = change.position.y - startY

                        if (!isScrolling && (abs(dy) > scrollThreshold || abs(dx) > scrollThreshold)) {
                            isScrolling = true
                            interactionSource.tryEmit(PressInteraction.Cancel(press))
                            cancelled = true
                        }

                        if (change.changedToUp()) {
                            if (!isScrolling) {
                                interactionSource.tryEmit(PressInteraction.Release(press))
                                onTapAction()
                            }
                            released = true
                            break
                        }
                    }

                    if (!released && !cancelled) {
                        interactionSource.tryEmit(PressInteraction.Cancel(press))
                    }
                }
            }
            .shake(shakeController)
            .fillMaxWidth()
            .heightIn(max = 320.dp)
            .aspectRatio(3f)
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
        shape = RoundedCornerShape(shapeCorner),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            val painter = rememberAsyncImagePainter(
                model = if (!darkMode) R.drawable.img_home_card_bg
                else R.drawable.img_home_card_bg_night
            )

            Image(
                painter = painter,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter,
                modifier = Modifier.matchParentSize(),
                colorFilter = ColorFilter.tint(
                    color = MaterialTheme.colorScheme.primary,
                    blendMode = BlendMode.Softlight
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 36.dp),
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
private fun HomeBannerCardPreview() {
    PreviewComposable {
        HomeBannerCard()
    }
}