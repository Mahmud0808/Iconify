package com.drdisagree.iconify.features.home.wifiicons.components

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
import com.drdisagree.iconify.data.states.UiText
import com.drdisagree.iconify.data.states.asString

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WifiIconCard(
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
fun WifiIconCardPreview() {
    val iconPacks = listOf(
        SignalIconPreview(
            id = "1",
            title = UiText.Text("Aurora"),
            icons = listOf(
                R.drawable.preview_aurora_ic_wifi_signal_1,
                R.drawable.preview_aurora_ic_wifi_signal_2,
                R.drawable.preview_aurora_ic_wifi_signal_3,
                R.drawable.preview_aurora_ic_wifi_signal_4
            ),
            isApplied = true
        ),
        SignalIconPreview(
            id = "2",
            title = UiText.Text("Bars"),
            icons = listOf(
                R.drawable.preview_bars_ic_wifi_signal_1,
                R.drawable.preview_bars_ic_wifi_signal_2,
                R.drawable.preview_bars_ic_wifi_signal_3,
                R.drawable.preview_bars_ic_wifi_signal_4
            )
        ),
        SignalIconPreview(
            id = "3",
            title = UiText.Text("Dora"),
            icons = listOf(
                R.drawable.preview_dora_ic_wifi_signal_1,
                R.drawable.preview_dora_ic_wifi_signal_2,
                R.drawable.preview_dora_ic_wifi_signal_3,
                R.drawable.preview_dora_ic_wifi_signal_4
            )
        )
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(16.dp)
    ) {
        items(iconPacks) { pack ->
            WifiIconCard(
                iconPack = pack,
                onClick = {}
            )
        }
    }
}