package com.drdisagree.iconify.features.home.tweaks.colornengine.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drdisagree.iconify.core.ui.utils.CARD_CORNER_LARGE

@Composable
fun ColorEngineGradient() {
    val shape = RoundedCornerShape(CARD_CORNER_LARGE)
    val primaryColor = colorResource(android.R.color.holo_blue_light)
    val secondaryColor = colorResource(android.R.color.holo_blue_dark)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(primaryColor, secondaryColor)
                ),
                shape
            )
    )
}

@Preview
@Composable
private fun ColorEngineGradientPreview() {
    ColorEngineGradient()
}