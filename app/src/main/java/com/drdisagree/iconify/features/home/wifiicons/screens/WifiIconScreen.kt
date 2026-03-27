package com.drdisagree.iconify.features.home.wifiicons.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.ui.components.dialogs.LoadingDialog
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable
import com.drdisagree.iconify.core.ui.components.others.innerPaddingValues
import com.drdisagree.iconify.core.ui.components.scaffolds.AppScaffold
import com.drdisagree.iconify.data.events.ToastUiEvent
import com.drdisagree.iconify.data.models.SignalIconPreview
import com.drdisagree.iconify.data.states.UiText
import com.drdisagree.iconify.features.home.wifiicons.components.WifiIconCard
import com.drdisagree.iconify.features.home.wifiicons.viewmodels.WifiIconViewModel

private val wifiIconList = listOf(
    Pair(
        "Aurora",
        listOf(
            R.drawable.preview_aurora_ic_wifi_signal_1,
            R.drawable.preview_aurora_ic_wifi_signal_2,
            R.drawable.preview_aurora_ic_wifi_signal_3,
            R.drawable.preview_aurora_ic_wifi_signal_4
        )
    ),
    Pair(
        "Bars",
        listOf(
            R.drawable.preview_bars_ic_wifi_signal_1,
            R.drawable.preview_bars_ic_wifi_signal_2,
            R.drawable.preview_bars_ic_wifi_signal_3,
            R.drawable.preview_bars_ic_wifi_signal_4
        )
    ),
    Pair(
        "Dora",
        listOf(
            R.drawable.preview_dora_ic_wifi_signal_1,
            R.drawable.preview_dora_ic_wifi_signal_2,
            R.drawable.preview_dora_ic_wifi_signal_3,
            R.drawable.preview_dora_ic_wifi_signal_4
        )
    ),
    Pair(
        "Faint UI",
        listOf(
            R.drawable.preview_faint_ui_ic_wifi_signal_1,
            R.drawable.preview_faint_ui_ic_wifi_signal_2,
            R.drawable.preview_faint_ui_ic_wifi_signal_3,
            R.drawable.preview_faint_ui_ic_wifi_signal_4
        )
    ),
    Pair(
        "Lorn",
        listOf(
            R.drawable.preview_lorn_ic_wifi_signal_1,
            R.drawable.preview_lorn_ic_wifi_signal_2,
            R.drawable.preview_lorn_ic_wifi_signal_3,
            R.drawable.preview_lorn_ic_wifi_signal_4
        )
    ),
    Pair(
        "Gradicon",
        listOf(
            R.drawable.preview_gradicon_ic_wifi_signal_1,
            R.drawable.preview_gradicon_ic_wifi_signal_2,
            R.drawable.preview_gradicon_ic_wifi_signal_3,
            R.drawable.preview_gradicon_ic_wifi_signal_4
        )
    ),
    Pair(
        "Inside",
        listOf(
            R.drawable.preview_inside_ic_wifi_signal_1,
            R.drawable.preview_inside_ic_wifi_signal_2,
            R.drawable.preview_inside_ic_wifi_signal_3,
            R.drawable.preview_inside_ic_wifi_signal_4
        )
    ),
    Pair(
        "Nothing Dot",
        listOf(
            R.drawable.preview_nothing_dot_ic_wifi_signal_1,
            R.drawable.preview_nothing_dot_ic_wifi_signal_2,
            R.drawable.preview_nothing_dot_ic_wifi_signal_3,
            R.drawable.preview_nothing_dot_ic_wifi_signal_4
        )
    ),
    Pair(
        "Plumpy",
        listOf(
            R.drawable.preview_plumpy_ic_wifi_signal_1,
            R.drawable.preview_plumpy_ic_wifi_signal_2,
            R.drawable.preview_plumpy_ic_wifi_signal_3,
            R.drawable.preview_plumpy_ic_wifi_signal_4
        )
    ),
    Pair(
        "Round",
        listOf(
            R.drawable.preview_round_ic_wifi_signal_1,
            R.drawable.preview_round_ic_wifi_signal_2,
            R.drawable.preview_round_ic_wifi_signal_3,
            R.drawable.preview_round_ic_wifi_signal_4
        )
    ),
    Pair(
        "Sneaky",
        listOf(
            R.drawable.preview_sneaky_ic_wifi_signal_1,
            R.drawable.preview_sneaky_ic_wifi_signal_2,
            R.drawable.preview_sneaky_ic_wifi_signal_3,
            R.drawable.preview_sneaky_ic_wifi_signal_4
        )
    ),
    Pair(
        "Stroke",
        listOf(
            R.drawable.preview_stroke_ic_wifi_signal_1,
            R.drawable.preview_stroke_ic_wifi_signal_2,
            R.drawable.preview_stroke_ic_wifi_signal_3,
            R.drawable.preview_stroke_ic_wifi_signal_4
        )
    ),
    Pair(
        "Wavy",
        listOf(
            R.drawable.preview_wavy_ic_wifi_signal_1,
            R.drawable.preview_wavy_ic_wifi_signal_2,
            R.drawable.preview_wavy_ic_wifi_signal_3,
            R.drawable.preview_wavy_ic_wifi_signal_4
        )
    ),
    Pair(
        "Weed",
        listOf(
            R.drawable.preview_weed_ic_wifi_signal_1,
            R.drawable.preview_weed_ic_wifi_signal_2,
            R.drawable.preview_weed_ic_wifi_signal_3,
            R.drawable.preview_weed_ic_wifi_signal_4
        )
    ),
    Pair(
        "Xperia",
        listOf(
            R.drawable.preview_xperia_ic_wifi_signal_1,
            R.drawable.preview_xperia_ic_wifi_signal_2,
            R.drawable.preview_xperia_ic_wifi_signal_3,
            R.drawable.preview_xperia_ic_wifi_signal_4
        )
    ),
    Pair(
        "ZigZag",
        listOf(
            R.drawable.preview_zigzag_ic_wifi_signal_1,
            R.drawable.preview_zigzag_ic_wifi_signal_2,
            R.drawable.preview_zigzag_ic_wifi_signal_3,
            R.drawable.preview_zigzag_ic_wifi_signal_4
        )
    )
).mapIndexed { index, (name, icons) ->
    SignalIconPreview(
        id = (index + 1).toString(),
        title = UiText.Text(name),
        icons = icons
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WifiIconScreen(wifiIconViewModel: WifiIconViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val wifiIcons by rememberSaveable { mutableStateOf(wifiIconList) }
    val wifiIconStyle by wifiIconViewModel.wifiIconStyle.collectAsStateWithLifecycle()
    val isApplying by wifiIconViewModel.isLoading.collectAsStateWithLifecycle()

    LaunchedEffect(wifiIcons) {
        wifiIconViewModel.refreshState()
    }

    if (isApplying) {
        LoadingDialog()
    }

    LaunchedEffect(Unit) {
        wifiIconViewModel.uiEvent.collect { event ->
            when (event) {
                ToastUiEvent.Applied -> Toast.makeText(
                    context,
                    R.string.toast_applied,
                    Toast.LENGTH_SHORT
                ).show()

                ToastUiEvent.Disabled -> Toast.makeText(
                    context,
                    R.string.toast_disabled,
                    Toast.LENGTH_SHORT
                ).show()

                ToastUiEvent.Error -> Toast.makeText(
                    context,
                    R.string.toast_error,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    AppScaffold(
        title = stringResource(R.string.activity_title_wifi_icons),
        showBackIcon = true,
    ) { innerPadding, _ ->
        val padding = innerPaddingValues(
            innerPadding = innerPadding,
            start = 16.dp,
            end = 16.dp,
            top = 16.dp
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(padding)
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                wifiIcons.forEach { wifiIcons ->
                    val isApplied = wifiIconStyle == wifiIcons.id

                    WifiIconCard(
                        iconPack = wifiIcons.copy(isApplied = isApplied),
                        onClick = {
                            wifiIconViewModel.togglePack(wifiIcons.id)
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Phone", device = Devices.PHONE)
@Composable
fun WifiIconScreenPhonePreview() {
    PreviewComposable {
        WifiIconScreen()
    }
}

@Preview(showBackground = true, name = "Tablet", device = Devices.TABLET)
@Composable
fun WifiIconScreenTabletPreview() {
    PreviewComposable {
        WifiIconScreen()
    }
}