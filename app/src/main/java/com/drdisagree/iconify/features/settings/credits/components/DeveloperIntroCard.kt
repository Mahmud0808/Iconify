package com.drdisagree.iconify.features.settings.credits.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Coffee
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.common.LocalPreferenceController
import com.drdisagree.iconify.core.common.LocalSettings
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable
import com.drdisagree.iconify.core.ui.utils.CARD_CORNER_LARGE
import com.drdisagree.iconify.data.keys.SettingsKey
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DeveloperIntroCard() {
    val context = LocalContext.current
    val settings = LocalSettings.current
    val uriHandler = LocalUriHandler.current
    val preferenceController = LocalPreferenceController.current
    val clickCount = remember { mutableIntStateOf(0) }
    val lastClickTime = remember { mutableLongStateOf(0L) }

    val gradient = Brush.linearGradient(
        colorStops = arrayOf(
            0.0f to MaterialTheme.colorScheme.primaryContainer,
            1.0f to MaterialTheme.colorScheme.secondaryContainer,
        )
    )
    val onContainer = MaterialTheme.colorScheme.onPrimaryContainer

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(DynamicWavyShape())
            .background(gradient),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
            ) {
                val showFallbackImage = remember { mutableStateOf(true) }

                AsyncImage(
                    model = "https://avatars.githubusercontent.com/u/29881338",
                    contentDescription = "Developer avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (settings.isPlaygroundUnlocked) return@clickable

                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastClickTime.longValue < 500) {
                                clickCount.intValue++
                            } else {
                                clickCount.intValue = 1
                            }
                            lastClickTime.longValue = currentTime

                            if (clickCount.intValue >= 7) {
                                preferenceController.setBoolean(
                                    SettingsKey.PLAYGROUND_UNLOCKED,
                                    true
                                )

                                Toast.makeText(
                                    context,
                                    "Unlocked playground",
                                    Toast.LENGTH_SHORT
                                ).show()

                                clickCount.intValue = 0
                            }
                        },
                    onState = { state ->
                        showFallbackImage.value = state is AsyncImagePainter.State.Error ||
                                state is AsyncImagePainter.State.Loading
                    }
                )

                if (showFallbackImage.value) {
                    Icon(
                        painter = painterResource(R.drawable.ic_user),
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "DrDisagree",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = onContainer,
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
                    .padding(horizontal = 14.dp, vertical = 4.dp),
            ) {
                Text(
                    text = stringResource(R.string.lead_developer),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.developer_intro_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = onContainer.copy(alpha = 0.75f),
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = { uriHandler.openUri("https://buymeacoffee.com/drdisagree") },
                    modifier = Modifier.weight(1f),
                    shapes = ButtonDefaults.shapes(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Coffee,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.buy_a_coffee),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                    )
                }
                OutlinedButton(
                    onClick = { uriHandler.openUri("https://github.com/Mahmud0808") },
                    modifier = Modifier.weight(1f),
                    shapes = ButtonDefaults.shapes(),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.secondary
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Code,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.info_github_title),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                    )
                }
            }
        }
    }
}

private class DynamicWavyShape(
    private val waveLength: Dp = 40.dp,
    private val waveHeight: Dp = 4.dp,
    private val cornerRadius: Dp = CARD_CORNER_LARGE
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val hPx = with(density) { waveHeight.toPx() }
        val rPx = with(density) { cornerRadius.toPx() }
        val wavelengthPx = with(density) { waveLength.toPx() }

        val path = Path().apply {
            moveTo(rPx, 0f)
            drawInwardWave(
                this,
                rPx,
                0f,
                size.width - rPx,
                0f,
                isHorizontal = true,
                hPx,
                wavelengthPx,
                direction = 1f
            )

            arcTo(Rect(size.width - rPx * 2, 0f, size.width, rPx * 2), 270f, 90f, false)

            drawInwardWave(
                this,
                size.width,
                rPx,
                size.width,
                size.height - rPx,
                isHorizontal = false,
                hPx,
                wavelengthPx,
                direction = -1f
            )

            arcTo(
                Rect(size.width - rPx * 2, size.height - rPx * 2, size.width, size.height),
                0f,
                90f,
                false
            )

            drawInwardWave(
                this,
                size.width - rPx,
                size.height,
                rPx,
                size.height,
                isHorizontal = true,
                hPx,
                wavelengthPx,
                direction = -1f
            )

            arcTo(Rect(0f, size.height - rPx * 2, rPx * 2, size.height), 90f, 90f, false)

            drawInwardWave(
                this,
                0f,
                size.height - rPx,
                0f,
                rPx,
                isHorizontal = false,
                hPx,
                wavelengthPx,
                direction = 1f
            )

            arcTo(Rect(0f, 0f, rPx * 2, rPx * 2), 180f, 90f, false)

            close()
        }
        return Outline.Generic(path)
    }

    private fun drawInwardWave(
        path: Path,
        startX: Float, startY: Float,
        endX: Float, endY: Float,
        isHorizontal: Boolean,
        hPx: Float,
        wavelengthPx: Float,
        direction: Float
    ) {
        val length = if (isHorizontal) abs(endX - startX) else abs(endY - startY)
        val count = (length / wavelengthPx).roundToInt().coerceAtLeast(1)
        val steps = (count * 15)

        for (i in 0..steps) {
            val t = i.toFloat() / steps
            val x = startX + (endX - startX) * t
            val y = startY + (endY - startY) * t

            val waveFactor = (1f - cos(t * count * 2 * PI).toFloat()) / 2f
            val offset = waveFactor * hPx * direction

            if (isHorizontal) {
                path.lineTo(x, y + offset)
            } else {
                path.lineTo(x + offset, y)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DeveloperIntroCardPreview() {
    PreviewComposable {
        DeveloperIntroCard()
    }
}