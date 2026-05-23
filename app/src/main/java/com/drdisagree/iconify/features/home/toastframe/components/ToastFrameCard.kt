package com.drdisagree.iconify.features.home.toastframe.components

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.ui.components.others.TooltipArrow
import com.drdisagree.iconify.core.ui.utils.CARD_CORNER_LARGE
import com.drdisagree.iconify.core.ui.utils.rememberXmlPainter
import com.drdisagree.iconify.data.models.ToastFramePreview
import com.drdisagree.iconify.features.common.models.UiText
import com.drdisagree.iconify.features.common.models.asString

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ToastFrameCard(
    toastFrame: ToastFramePreview,
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
                .wrapContentSize()
                .clickable(
                    onClick = onClick,
                    interactionSource = interactionSource,
                    indication = null
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier
                    .wrapContentSize()
                    .paint(
                        painter = rememberXmlPainter(toastFrame.toastStyle),
                        contentScale = ContentScale.FillBounds
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.toast_message),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }

            Column(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .wrapContentSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                TooltipArrow(
                    backgroundColor = if (toastFrame.isApplied) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    },
                    interactionSource = interactionSource
                )
                Box(
                    modifier = Modifier
                        .wrapContentWidth()
                        .clip(MaterialTheme.shapes.large)
                        .background(
                            if (toastFrame.isApplied) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHigh
                            }
                        )
                        .indication(
                            interactionSource = interactionSource,
                            indication = ripple()
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = toastFrame.title.asString(),
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .wrapContentWidth()
                            .basicMarquee(iterations = Int.MAX_VALUE),
                        color = if (toastFrame.isApplied) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ToastFrameCardPreview() {
    val iconPacks = listOf(
        ToastFramePreview(
            id = "1",
            title = UiText.Text("Style 1"),
            toastStyle = R.drawable.toast_frame_style_1,
            isApplied = true
        ),
        ToastFramePreview(
            id = "2",
            title = UiText.Text("Style 2"),
            toastStyle = R.drawable.toast_frame_style_2,
        ),
        ToastFramePreview(
            id = "3",
            title = UiText.Text("Style 3"),
            toastStyle = R.drawable.toast_frame_style_3,
        )
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(16.dp)
    ) {
        items(iconPacks) { toast ->
            ToastFrameCard(
                toastFrame = toast,
                onClick = {}
            )
        }
    }
}