package com.drdisagree.iconify.features.home.iconpack.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.ui.components.extensions.secondaryText
import com.drdisagree.iconify.core.ui.components.others.IconPreviewGrid
import com.drdisagree.iconify.core.ui.components.others.withHaptic
import com.drdisagree.iconify.data.models.IconPackPreview
import com.drdisagree.iconify.features.common.models.UiText
import com.drdisagree.iconify.features.common.models.asString

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun IconPackCard(
    iconPack: IconPackPreview,
    isSelected: Boolean,
    onClick: () -> Unit,
    onActionClick: (IconPackPreview) -> Unit,
    shape: Shape = MaterialTheme.shapes.large
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = if (iconPack.isApplied) MaterialTheme.colorScheme.primaryContainer
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
                        text = iconPack.title.asString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (iconPack.isApplied) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = iconPack.summary.asString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = (if (iconPack.isApplied) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface).secondaryText()
                    )
                }

                IconPreviewGrid(
                    isApplied = iconPack.isApplied,
                    icons = iconPack.icons
                )
            }

            AnimatedVisibility(visible = isSelected) {
                Button(
                    onClick = withHaptic { onActionClick(iconPack) },
                    shapes = ButtonDefaults.shapes(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (iconPack.isApplied)
                            MaterialTheme.colorScheme.errorContainer
                        else
                            MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (iconPack.isApplied)
                            MaterialTheme.colorScheme.onErrorContainer
                        else
                            MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text(
                        stringResource(
                            if (iconPack.isApplied) R.string.btn_disable
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
private fun IconPackPreview() {
    val iconPacks = listOf(
        IconPackPreview(
            id = "1",
            title = UiText.Text("Aurora"),
            summary = UiText.Text("Dual tone linear icon pack"),
            icons = listOf(
                R.drawable.preview_aurora_wifi,
                R.drawable.preview_aurora_signal,
                R.drawable.preview_aurora_airplane,
                R.drawable.preview_aurora_location
            ),
            isApplied = true
        ),
        IconPackPreview(
            id = "2",
            title = UiText.Text("Gradicon"),
            summary = UiText.Text("Gradient shaded filled icon pack"),
            icons = listOf(
                R.drawable.preview_gradicon_wifi,
                R.drawable.preview_gradicon_signal,
                R.drawable.preview_gradicon_airplane,
                R.drawable.preview_gradicon_location
            )
        ),
        IconPackPreview(
            id = "3",
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
        items(iconPacks) { pack ->
            IconPackCard(
                iconPack = pack,
                isSelected = pack.id == "1",
                onClick = {},
                onActionClick = {}
            )
        }
    }
}