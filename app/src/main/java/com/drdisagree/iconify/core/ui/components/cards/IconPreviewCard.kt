package com.drdisagree.iconify.core.ui.components.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.drdisagree.iconify.core.ui.components.others.IconPreviewGrid
import com.drdisagree.iconify.core.ui.utils.CARD_CORNER_LARGE
import com.drdisagree.iconify.core.ui.utils.CARD_CORNER_SMALL

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun IconPreviewCard(
    title: String,
    isApplied: Boolean,
    icons: List<Int>,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Card(
        shape = RoundedCornerShape(CARD_CORNER_LARGE),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Layout(
            content = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.clickable(
                        onClick = onClick,
                        interactionSource = interactionSource,
                        indication = null
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(
                                RoundedCornerShape(
                                    topStart = CARD_CORNER_LARGE,
                                    topEnd = CARD_CORNER_LARGE,
                                    bottomStart = CARD_CORNER_SMALL,
                                    bottomEnd = CARD_CORNER_SMALL
                                )
                            )
                            .background(
                                if (isApplied) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceContainerHigh
                            )
                            .indication(
                                interactionSource = interactionSource,
                                indication = ripple()
                            )
                            .border(
                                2.dp,
                                if (isApplied) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceContainerHigh,
                                RoundedCornerShape(
                                    topStart = CARD_CORNER_LARGE,
                                    topEnd = CARD_CORNER_LARGE,
                                    bottomStart = CARD_CORNER_SMALL,
                                    bottomEnd = CARD_CORNER_SMALL
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        IconPreviewGrid(
                            modifier = Modifier.padding(12.dp),
                            isApplied = isApplied,
                            icons = icons
                        )
                    }

                    Box(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .fillMaxWidth()
                            .clip(
                                RoundedCornerShape(
                                    topStart = CARD_CORNER_SMALL,
                                    topEnd = CARD_CORNER_SMALL,
                                    bottomStart = CARD_CORNER_LARGE,
                                    bottomEnd = CARD_CORNER_LARGE
                                )
                            )
                            .background(
                                if (isApplied) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerHigh
                                }
                            )
                            .indication(
                                interactionSource = interactionSource,
                                indication = ripple()
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .basicMarquee(iterations = Int.MAX_VALUE),
                            color = if (isApplied) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                        )
                    }
                }
            }
        ) { measurables, constraints ->
            val measurable = measurables.first()

            // Ask for intrinsic height (legal, no measurement)
            val intrinsicHeight = measurable.maxIntrinsicHeight(constraints.maxWidth)

            // Use that height as width
            val width = intrinsicHeight.coerceIn(constraints.minWidth, constraints.maxWidth)

            // Measure ONCE with final constraints
            val placeable = measurable.measure(
                constraints.copy(
                    minWidth = width,
                    maxWidth = width
                )
            )

            layout(width, placeable.height) {
                placeable.place(0, 0)
            }
        }
    }
}