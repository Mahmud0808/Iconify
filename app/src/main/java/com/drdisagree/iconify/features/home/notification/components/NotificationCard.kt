package com.drdisagree.iconify.features.home.notification.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.ui.components.extensions.secondaryText
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable
import com.drdisagree.iconify.core.ui.components.others.withHaptic
import com.drdisagree.iconify.core.ui.utils.rememberXmlPainter
import com.drdisagree.iconify.data.models.NotificationPreview
import com.drdisagree.iconify.features.common.models.UiText
import com.drdisagree.iconify.features.common.models.asString

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NotificationCard(
    notification: NotificationPreview,
    isSelected: Boolean,
    onClick: () -> Unit,
    onActionClick: (NotificationPreview) -> Unit
) {
    Card(
        modifier = Modifier
            .widthIn(max = 480.dp)
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .paint(
                    painter = rememberXmlPainter(notification.notificationStyle),
                    contentScale = ContentScale.FillBounds
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "Icon",
                        modifier = Modifier.requiredSize(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                ) {
                    AnimatedVisibility(visible = isSelected) {
                        Text(
                            text = stringResource(id = R.string.app_name),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.secondaryText()
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = notification.title.asString(),
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (notification.isApplied) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        )
                        if (notification.isApplied) {
                            Icon(
                                painter = painterResource(R.drawable.ic_tick),
                                contentDescription = "Tick",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(start = 6.dp)
                                    .size(18.dp)
                            )
                        }
                    }
                    AnimatedVisibility(visible = isSelected) {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Text(
                        text = stringResource(R.string.notif_preview_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.secondaryText()
                    )
                }
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .width(24.dp)
                        .height(16.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = isSelected,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_expand_arrow),
                            contentDescription = "Collapse",
                            modifier = Modifier
                                .size(14.dp)
                                .graphicsLayer {
                                    rotationZ = 180f
                                },
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    androidx.compose.animation.AnimatedVisibility(
                        visible = !isSelected,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_expand_arrow),
                            contentDescription = "Expand",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            AnimatedVisibility(visible = isSelected) {
                Button(
                    onClick = withHaptic { onActionClick(notification) },
                    shapes = ButtonDefaults.shapes(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (notification.isApplied)
                            MaterialTheme.colorScheme.errorContainer
                        else
                            MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (notification.isApplied)
                            MaterialTheme.colorScheme.onErrorContainer
                        else
                            MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text(
                        stringResource(
                            if (notification.isApplied) R.string.btn_disable
                            else R.string.btn_apply
                        )
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NotificationCardPreview() {
    val notifications = listOf(
        NotificationPreview(
            id = "1",
            title = UiText.Text("Default"),
            notificationStyle = R.drawable.preview_notification_default,
            isApplied = true
        ),
        NotificationPreview(
            id = "2",
            title = UiText.Text("Layers"),
            notificationStyle = R.drawable.preview_notification_layers
        ),
        NotificationPreview(
            id = "3",
            title = UiText.Text("Thin Outline"),
            notificationStyle = R.drawable.preview_notification_thin_outline
        )
    )

    PreviewComposable {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(notifications) { pack ->
                NotificationCard(
                    notification = pack,
                    isSelected = pack.id == "1",
                    onClick = {},
                    onActionClick = {}
                )
            }
        }
    }
}