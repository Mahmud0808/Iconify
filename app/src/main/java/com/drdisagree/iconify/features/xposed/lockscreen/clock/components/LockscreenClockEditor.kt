package com.drdisagree.iconify.features.xposed.lockscreen.clock.components

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.common.LocalPreferenceController
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable
import com.drdisagree.iconify.data.keys.XposedKey
import kotlin.math.roundToInt

private const val MIN_SCALE = 0.5f
private const val MAX_SCALE = 2.5f
private const val MAX_OFFSET_X = 300f
private const val MAX_TOP_MARGIN = 600f

/**
 * iPhone style lock screen clock editor.
 *
 * Tap the phone to start editing, then drag the clock to move it and pinch to resize it.
 * Changes are written to the same preferences the sliders below use, once the finger
 * is lifted, so the lock screen picks them up like any other change.
 */
@Composable
fun LockscreenClockEditor() {
    val context = LocalContext.current
    val density = LocalDensity.current
    val prefController = LocalPreferenceController.current

    val isClockEnabled by prefController.observe(XposedKey.CUSTOM_LOCKSCREEN_CLOCK.name, false)
    val clockStyle by prefController.observe(XposedKey.LSCLOCK_STYLE.name, 0)
    val prefOffsetX by prefController.observe(XposedKey.LSCLOCK_OFFSET_X.name, 0f)
    val prefTopMargin by prefController.observe(XposedKey.LSCLOCK_TOP_MARGIN.name, 100f)
    val prefScale by prefController.observe(XposedKey.LSCLOCK_TEXT_SCALE.name, 1f)

    var offsetX by remember { mutableFloatStateOf(prefOffsetX) }
    var topMargin by remember { mutableFloatStateOf(prefTopMargin) }
    var scale by remember { mutableFloatStateOf(prefScale) }
    var isEditing by remember { mutableStateOf(false) }
    var isGesturing by remember { mutableStateOf(false) }

    // Keep the editor in sync when a slider below changes the same values
    LaunchedEffect(prefOffsetX, prefTopMargin, prefScale) {
        if (isGesturing) return@LaunchedEffect
        offsetX = prefOffsetX
        topMargin = prefTopMargin
        scale = prefScale
    }

    // Layout of the selected clock style, 0 means "no clock" and has no layout
    val layoutId = remember(clockStyle) {
        context.resources.getIdentifier(
            "preview_lockscreen_clock_$clockStyle",
            "layout",
            context.packageName
        )
    }

    // The phone is drawn at a fixed size, while the clock is laid out at the real
    // screen size in dp and scaled down, so the margins mean the same thing as on device
    val displayMetrics = context.resources.displayMetrics
    val deviceWidthDp = displayMetrics.widthPixels / displayMetrics.density
    val deviceHeightDp = displayMetrics.heightPixels / displayMetrics.density
    val phoneWidthDp = 220f
    val phoneHeightDp = phoneWidthDp * deviceHeightDp / deviceWidthDp
    val previewScale = phoneWidthDp / deviceWidthDp

    val borderColor = if (isEditing) MaterialTheme.colorScheme.primary else Color.Transparent

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.lockscreen_clock_editor_title),
            style = MaterialTheme.typography.titleMedium
        )

        Box(
            modifier = Modifier
                .size(phoneWidthDp.dp, phoneHeightDp.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF2B2F4A), Color(0xFF0E0F1A))
                    )
                )
                .border(BorderStroke(2.dp, borderColor), RoundedCornerShape(24.dp))
                .clickable(enabled = isClockEnabled) { isEditing = !isEditing }
                .pointerInput(isEditing, isClockEnabled) {
                    if (!isEditing || !isClockEnabled) return@pointerInput

                    // Pixels of the phone box to dp on the real screen
                    val pxToDp = 1f / (density.density * previewScale)

                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        isGesturing = true

                        do {
                            val event = awaitPointerEvent()
                            val pan = event.calculatePan()
                            val zoom = event.calculateZoom()

                            if (event.changes.any { it.positionChanged() }) {
                                offsetX = (offsetX + pan.x * pxToDp)
                                    .coerceIn(-MAX_OFFSET_X, MAX_OFFSET_X)
                                topMargin = (topMargin + pan.y * pxToDp)
                                    .coerceIn(0f, MAX_TOP_MARGIN)
                                scale = (scale * zoom).coerceIn(MIN_SCALE, MAX_SCALE)

                                event.changes.forEach { change ->
                                    if (change.positionChanged()) change.consume()
                                }
                            }
                        } while (event.changes.any { it.pressed })

                        isGesturing = false

                        // Whole dp and one decimal of scale, same as the sliders
                        prefController.setFloat(XposedKey.LSCLOCK_OFFSET_X, offsetX.roundToInt().toFloat())
                        prefController.setFloat(XposedKey.LSCLOCK_TOP_MARGIN, topMargin.roundToInt().toFloat())
                        prefController.setFloat(XposedKey.LSCLOCK_TEXT_SCALE, (scale * 10f).roundToInt() / 10f)
                    }
                }
                .clipToBounds()
        ) {
            if (layoutId != 0) {
                // Real screen sized canvas, scaled down to fit the phone box
                Box(
                    modifier = Modifier
                        .requiredSize(deviceWidthDp.dp, deviceHeightDp.dp)
                        .graphicsLayer {
                            scaleX = previewScale
                            scaleY = previewScale
                            transformOrigin = TransformOrigin(0f, 0f)
                        }
                ) {
                    AndroidView(
                        factory = { ctx ->
                            FrameLayout(ctx).apply {
                                addView(
                                    LayoutInflater.from(ctx).inflate(layoutId, this, false),
                                    FrameLayout.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT,
                                        Gravity.CENTER_HORIZONTAL
                                    )
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset {
                                IntOffset(
                                    x = (offsetX * density.density).roundToInt(),
                                    y = (topMargin * density.density).roundToInt()
                                )
                            }
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                transformOrigin = TransformOrigin(0.5f, 0f)
                            }
                    )
                }
            }
        }

        Text(
            text = stringResource(R.string.lockscreen_clock_editor_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        TextButton(
            enabled = isClockEnabled,
            onClick = {
                offsetX = XposedKey.LSCLOCK_OFFSET_X.default as Float
                topMargin = XposedKey.LSCLOCK_TOP_MARGIN.default as Float
                scale = XposedKey.LSCLOCK_TEXT_SCALE.default as Float

                prefController.setFloat(XposedKey.LSCLOCK_OFFSET_X, offsetX)
                prefController.setFloat(XposedKey.LSCLOCK_TOP_MARGIN, topMargin)
                prefController.setFloat(XposedKey.LSCLOCK_TEXT_SCALE, scale)
            }
        ) {
            Text(stringResource(R.string.lockscreen_clock_editor_reset))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LockscreenClockEditorPreview() {
    PreviewComposable {
        LockscreenClockEditor()
    }
}
