package com.drdisagree.iconify.features.home.settingsicons.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.ui.components.extensions.secondaryText
import com.drdisagree.iconify.core.ui.components.others.IconPreviewGrid
import com.drdisagree.iconify.core.ui.utils.ItemPosition
import com.drdisagree.iconify.core.ui.utils.cardCorners
import com.drdisagree.iconify.data.models.SettingsIconsPreview
import com.drdisagree.iconify.features.common.models.UiText
import com.drdisagree.iconify.features.common.models.asString

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsIconsCard(
    modifier: Modifier = Modifier,
    settingsIcon: SettingsIconsPreview,
    itemPosition: ItemPosition = ItemPosition.SOLO,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val corners = cardCorners(itemPosition)

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(
            topStart = corners.topStart,
            topEnd = corners.topEnd,
            bottomStart = corners.bottomStart,
            bottomEnd = corners.bottomEnd
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = settingsIcon.title.asString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = settingsIcon.summary.asString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = (if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface).secondaryText()
                    )
                }

                IconPreviewGrid(
                    isApplied = isSelected,
                    icons = settingsIcon.icons,
                    shouldTint = !settingsIcon.title.asString()
                        .contains("bubble", ignoreCase = true)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun IconPackPreview() {
    val iconPacks = listOf(
        SettingsIconsPreview(
            title = UiText.Text("Aurora"),
            summary = UiText.Text("Dual tone linear icon pack"),
            icons = listOf(
                R.drawable.preview_aurora_wifi,
                R.drawable.preview_aurora_signal,
                R.drawable.preview_aurora_airplane,
                R.drawable.preview_aurora_location
            ),
        ),
        SettingsIconsPreview(
            title = UiText.Text("Gradicon"),
            summary = UiText.Text("Gradient shaded filled icon pack"),
            icons = listOf(
                R.drawable.preview_gradicon_wifi,
                R.drawable.preview_gradicon_signal,
                R.drawable.preview_gradicon_airplane,
                R.drawable.preview_gradicon_location
            )
        ),
        SettingsIconsPreview(
            title = UiText.Text("Lorn"),
            summary = UiText.Text("Thick linear icon pack"),
            icons = listOf(
                R.drawable.preview_lorn_wifi,
                R.drawable.preview_lorn_signal,
                R.drawable.preview_lorn_airplane,
                R.drawable.preview_lorn_location
            )
        )
    )

    LazyColumn {
        itemsIndexed(iconPacks) { index, pack ->
            SettingsIconsCard(
                settingsIcon = pack,
                isSelected = index == 0,
                onClick = {},
            )
        }
    }
}