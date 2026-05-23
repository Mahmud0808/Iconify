package com.drdisagree.iconify.features.home.iconshape.components

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.ui.components.extensions.secondaryText
import com.drdisagree.iconify.core.ui.components.others.TooltipArrow
import com.drdisagree.iconify.core.ui.utils.CARD_CORNER_LARGE
import com.drdisagree.iconify.core.ui.utils.rememberXmlPainter
import com.drdisagree.iconify.data.models.IconShapePreview
import com.drdisagree.iconify.features.common.models.UiText
import com.drdisagree.iconify.features.common.models.asString

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun IconShapeCard(
    iconShape: IconShapePreview,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Card(
        shape = RoundedCornerShape(CARD_CORNER_LARGE),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .width(78.dp)
                .clickable(
                    onClick = onClick,
                    interactionSource = interactionSource,
                    indication = null
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .paint(
                        painter = rememberXmlPainter(iconShape.shape),
                        contentScale = ContentScale.FillBounds,
                        colorFilter = ColorFilter.tint(
                            if (iconShape.isApplied) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface.secondaryText()
                            }
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .paint(
                            painter = rememberXmlPainter(iconShape.shape),
                            contentScale = ContentScale.FillBounds,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.surface)
                        )
                )
            }

            val containerColor = if (iconShape.isApplied) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
            }
            var contentHeightPx by remember { mutableIntStateOf(0) }
            val density = LocalDensity.current

            Column(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .wrapContentSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                TooltipArrow(
                    backgroundColor = containerColor,
                    interactionSource = interactionSource
                )
                Box(
                    modifier = Modifier
                        .wrapContentSize()
                        .clip(MaterialTheme.shapes.large)
                        .background(containerColor)
                        .indication(
                            interactionSource = interactionSource,
                            indication = ripple()
                        )
                        .padding(vertical = 4.dp)
                        .onGloballyPositioned { coordinates ->
                            contentHeightPx = coordinates.size.height
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = iconShape.title.asString(),
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .basicMarquee(iterations = Int.MAX_VALUE)
                            .padding(horizontal = 8.dp),
                        color = if (iconShape.isApplied) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    )
                    Box(
                        modifier = Modifier
                            .width(8.dp)
                            .height(with(density) { contentHeightPx.toDp() })
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        containerColor,
                                        containerColor.copy(alpha = 0.7f),
                                        containerColor.copy(alpha = 0f)
                                    )
                                )
                            )
                            .align(Alignment.CenterStart)
                    )
                    Box(
                        modifier = Modifier
                            .width(8.dp)
                            .height(with(density) { contentHeightPx.toDp() })
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        containerColor.copy(alpha = 0f),
                                        containerColor.copy(alpha = 0.7f),
                                        containerColor
                                    )
                                )
                            )
                            .align(Alignment.CenterEnd)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun IconShapeCardPreview() {
    val iconShapes = listOf(
        IconShapePreview(
            id = "1",
            title = UiText.Text("Round"),
            shape = R.drawable.icon_shape_round,
            isApplied = true
        ),
        IconShapePreview(
            id = "2",
            title = UiText.Text("Pebble"),
            shape = R.drawable.icon_shape_pebble
        ),
        IconShapePreview(
            id = "3",
            title = UiText.Text("Hexagon"),
            shape = R.drawable.icon_shape_rounded_hexagon
        )
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(16.dp)
    ) {
        items(iconShapes) { iconShape ->
            IconShapeCard(
                iconShape = iconShape,
                onClick = {}
            )
        }
    }
}