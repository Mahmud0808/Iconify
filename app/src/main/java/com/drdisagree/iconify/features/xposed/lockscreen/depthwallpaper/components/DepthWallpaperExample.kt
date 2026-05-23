package com.drdisagree.iconify.features.xposed.lockscreen.depthwallpaper.components

import android.annotation.SuppressLint
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.ui.components.extensions.secondaryText
import kotlin.math.roundToInt

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun DepthWallpaperExample(
    modifier: Modifier = Modifier,
    leftImage: Painter,
    rightImage: Painter,
    leftName: String = "Alice",
    rightName: String = "Bob",
    imageWidth: Dp = 130.dp,
    gap: Dp = 48.dp,
) {
    val density = LocalDensity.current

    val idleDurationMs = 800
    val moveDurationMs = 900
    val mergedPauseMs = 400
    val fadeOutDurationMs = 500

    val infiniteTransition = rememberInfiniteTransition(label = "merge_loop")

    val totalCycle = idleDurationMs + moveDurationMs + mergedPauseMs + fadeOutDurationMs

    val cycleProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = totalCycle, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cycle_progress"
    )

    val idleFraction = idleDurationMs.toFloat() / totalCycle
    val moveFraction = moveDurationMs.toFloat() / totalCycle
    val pauseFraction = mergedPauseMs.toFloat() / totalCycle

    val moveProgress: Float = when {
        cycleProgress < idleFraction -> 0f
        cycleProgress < idleFraction + moveFraction ->
            (cycleProgress - idleFraction) / moveFraction

        else -> 1f
    }

    val fadeOutStart = idleFraction + moveFraction + pauseFraction
    val fadeProgress: Float = when {
        cycleProgress < fadeOutStart -> 0f
        else -> ((cycleProgress - fadeOutStart) / (1f - fadeOutStart)).coerceIn(0f, 1f)
    }

    val easedMove = FastOutSlowInEasing.transform(moveProgress)
    val easedFade = FastOutLinearInEasing.transform(fadeProgress)

    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        val halfImagePx = with(density) { (imageWidth / 2).toPx() }
        val halfGapPx = with(density) { (gap / 2).toPx() }

        val travelPx = halfImagePx + halfGapPx

        val leftOffsetPx = (easedMove * travelPx).roundToInt()
        val rightOffsetPx = (easedMove * travelPx).roundToInt()

        val nameFadeStart = 0.2f
        val nameFadeEnd = 0.4f
        val nameAlpha = when {
            moveProgress < nameFadeStart -> 1f
            moveProgress > nameFadeEnd -> 0f
            else -> 1f - ((moveProgress - nameFadeStart) / (nameFadeEnd - nameFadeStart))
        }.coerceIn(0f, 1f)

        val imageAlpha = (1f - easedFade).coerceIn(0f, 1f)

        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            ImageCard(
                painter = leftImage,
                name = leftName,
                imageWidth = imageWidth,
                imageAlpha = imageAlpha,
                nameAlpha = nameAlpha * imageAlpha,
                offset = IntOffset(x = leftOffsetPx, y = 0),
                zIndex = 2f
            )

            Spacer(modifier = Modifier.width(gap))

            ImageCard(
                painter = rightImage,
                name = rightName,
                imageWidth = imageWidth,
                imageAlpha = imageAlpha,
                nameAlpha = nameAlpha * imageAlpha,
                offset = IntOffset(x = -rightOffsetPx, y = 0),
                zIndex = 1f
            )
        }
    }
}

@Composable
private fun ImageCard(
    painter: Painter,
    name: String,
    imageWidth: Dp,
    imageAlpha: Float,
    nameAlpha: Float,
    offset: IntOffset,
    zIndex: Float,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .offset { offset }
            .zIndex(zIndex)
    ) {
        Image(
            painter = painter,
            contentDescription = name,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .width(imageWidth)
                .clip(MaterialTheme.shapes.medium)
                .alpha(imageAlpha)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = name,
            color = MaterialTheme.colorScheme.onSurface.secondaryText(),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .widthIn(max = imageWidth * 0.8f)
                .alpha(nameAlpha)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DepthWallpaperExamplePreview() {
    DepthWallpaperExample(
        leftImage = painterResource(id = R.drawable.img_depth_wallpaper_example_fg),
        rightImage = painterResource(id = R.drawable.img_depth_wallpaper_example_bg),
        leftName = "Foreground",
        rightName = "Background",
        imageWidth = 120.dp,
    )
}