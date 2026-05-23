package com.drdisagree.iconify.core.ui.components.others

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.unit.Constraints
import kotlin.math.max

@Composable
fun AutoScalingDevicePreview(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val resources = LocalResources.current
    val configuration = LocalConfiguration.current
    val displayMetrics = resources.displayMetrics

    val deviceWidthPx = when (configuration.orientation) {
        Configuration.ORIENTATION_PORTRAIT -> displayMetrics.widthPixels
        else -> displayMetrics.heightPixels
    }
    val deviceHeightPx = when (configuration.orientation) {
        Configuration.ORIENTATION_PORTRAIT -> displayMetrics.heightPixels
        else -> displayMetrics.widthPixels
    }

    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val thisScaleX = constraints.maxWidth.toFloat() / deviceWidthPx
        val thisScaleY = constraints.maxHeight.toFloat() / deviceHeightPx

        val finalScale = max(thisScaleX, thisScaleY)

        val childConstraints = Constraints.fixed(deviceWidthPx, deviceHeightPx)
        val placeables = measurables.map { it.measure(childConstraints) }

        val scaledWidth = (deviceWidthPx * finalScale).toInt()
        val scaledHeight = (deviceHeightPx * finalScale).toInt()

        layout(scaledWidth, scaledHeight) {
            placeables.forEach { placeable ->
                placeable.placeRelativeWithLayer(0, 0) {
                    scaleX = finalScale
                    scaleY = finalScale
                    transformOrigin = TransformOrigin(0f, 0f)
                }
            }
        }
    }
}