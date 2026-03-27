package com.drdisagree.iconify.features.home.cellularicons.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.ui.components.cards.IconPreviewCard
import com.drdisagree.iconify.data.models.SignalIconPreview
import com.drdisagree.iconify.data.states.asString
import com.drdisagree.iconify.data.states.UiText

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CellularIconCard(
    iconPack: SignalIconPreview,
    onClick: () -> Unit
) {
    IconPreviewCard(
        title = iconPack.title.asString(),
        isApplied = iconPack.isApplied,
        icons = iconPack.icons,
        onClick = onClick
    )
}

@Preview(showBackground = true)
@Composable
fun CellularIconCardPreview() {
    val iconPacks = listOf(
        SignalIconPreview(
            id = "1",
            title = UiText.Text("Aurora"),
            icons = listOf(
                R.drawable.preview_aurora_wifi,
                R.drawable.preview_aurora_signal,
                R.drawable.preview_aurora_airplane,
                R.drawable.preview_aurora_location
            ),
            isApplied = true
        ),
        SignalIconPreview(
            id = "2",
            title = UiText.Text("Gradicon"),
            icons = listOf(
                R.drawable.preview_gradicon_wifi,
                R.drawable.preview_gradicon_signal,
                R.drawable.preview_gradicon_airplane,
                R.drawable.preview_gradicon_location
            )
        ),
        SignalIconPreview(
            id = "3",
            title = UiText.Text("Lorn"),
            icons = listOf(
                R.drawable.preview_lorn_wifi,
                R.drawable.preview_lorn_signal,
                R.drawable.preview_lorn_airplane,
                R.drawable.preview_lorn_location
            )
        )
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(16.dp)
    ) {
        items(iconPacks) { pack ->
            CellularIconCard(
                iconPack = pack,
                onClick = {}
            )
        }
    }
}