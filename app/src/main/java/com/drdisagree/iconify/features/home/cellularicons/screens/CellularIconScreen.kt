package com.drdisagree.iconify.features.home.cellularicons.screens

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.ui.components.dialogs.LoadingDialog
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable
import com.drdisagree.iconify.core.ui.components.others.ToastAppliedEvent
import com.drdisagree.iconify.core.ui.components.others.innerPaddingValues
import com.drdisagree.iconify.core.ui.components.scaffolds.AppScaffold
import com.drdisagree.iconify.data.models.SignalIconPreview
import com.drdisagree.iconify.features.common.models.UiText
import com.drdisagree.iconify.features.home.cellularicons.components.CellularIconCard
import com.drdisagree.iconify.features.home.cellularicons.viewmodels.CellularIconViewModel

private val cellularIconList = listOf(
    Pair(
        "Aquarium",
        listOf(
            R.drawable.preview_aquarium_ic_signal_cellular_1_4_bar,
            R.drawable.preview_aquarium_ic_signal_cellular_2_4_bar,
            R.drawable.preview_aquarium_ic_signal_cellular_3_4_bar,
            R.drawable.preview_aquarium_ic_signal_cellular_4_4_bar
        )
    ),
    Pair(
        "Aurora",
        listOf(
            R.drawable.preview_aurora_ic_signal_cellular_1_4_bar,
            R.drawable.preview_aurora_ic_signal_cellular_2_4_bar,
            R.drawable.preview_aurora_ic_signal_cellular_3_4_bar,
            R.drawable.preview_aurora_ic_signal_cellular_4_4_bar
        )
    ),
    Pair(
        "Bars",
        listOf(
            R.drawable.preview_bars_ic_signal_cellular_1_4_bar,
            R.drawable.preview_bars_ic_signal_cellular_2_4_bar,
            R.drawable.preview_bars_ic_signal_cellular_3_4_bar,
            R.drawable.preview_bars_ic_signal_cellular_4_4_bar
        )
    ),
    Pair(
        "Butterfly",
        listOf(
            R.drawable.preview_butterfly_ic_signal_cellular_1_4_bar,
            R.drawable.preview_butterfly_ic_signal_cellular_2_4_bar,
            R.drawable.preview_butterfly_ic_signal_cellular_3_4_bar,
            R.drawable.preview_butterfly_ic_signal_cellular_4_4_bar
        )
    ),
    Pair(
        "Circle",
        listOf(
            R.drawable.preview_circle_ic_signal_cellular_1_4_bar,
            R.drawable.preview_circle_ic_signal_cellular_2_4_bar,
            R.drawable.preview_circle_ic_signal_cellular_3_4_bar,
            R.drawable.preview_circle_ic_signal_cellular_4_4_bar
        )
    ),
    Pair(
        "Daun",
        listOf(
            R.drawable.preview_daun_ic_signal_cellular_1_4_bar,
            R.drawable.preview_daun_ic_signal_cellular_2_4_bar,
            R.drawable.preview_daun_ic_signal_cellular_3_4_bar,
            R.drawable.preview_daun_ic_signal_cellular_4_4_bar
        )
    ),
    Pair(
        "Dec",
        listOf(
            R.drawable.preview_dec_ic_signal_cellular_1_4_bar,
            R.drawable.preview_dec_ic_signal_cellular_2_4_bar,
            R.drawable.preview_dec_ic_signal_cellular_3_4_bar,
            R.drawable.preview_dec_ic_signal_cellular_4_4_bar
        )
    ),
    Pair(
        "Deep",
        listOf(
            R.drawable.preview_deep_ic_signal_cellular_1_4_bar,
            R.drawable.preview_deep_ic_signal_cellular_2_4_bar,
            R.drawable.preview_deep_ic_signal_cellular_3_4_bar,
            R.drawable.preview_deep_ic_signal_cellular_4_4_bar
        )
    ),
    Pair(
        "Dora",
        listOf(
            R.drawable.preview_dora_ic_signal_cellular_1_4_bar,
            R.drawable.preview_dora_ic_signal_cellular_2_4_bar,
            R.drawable.preview_dora_ic_signal_cellular_3_4_bar,
            R.drawable.preview_dora_ic_signal_cellular_4_4_bar
        )
    ),
    Pair(
        "ZigZag",
        listOf(
            R.drawable.preview_zigzag_ic_signal_cellular_1_4_bar,
            R.drawable.preview_zigzag_ic_signal_cellular_2_4_bar,
            R.drawable.preview_zigzag_ic_signal_cellular_3_4_bar,
            R.drawable.preview_zigzag_ic_signal_cellular_4_4_bar
        )
    ),
    Pair(
        "Equal",
        listOf(
            R.drawable.preview_equal_ic_signal_cellular_1_4_bar,
            R.drawable.preview_equal_ic_signal_cellular_2_4_bar,
            R.drawable.preview_equal_ic_signal_cellular_3_4_bar,
            R.drawable.preview_equal_ic_signal_cellular_4_4_bar
        )
    ),
    Pair(
        "Faint UI",
        listOf(
            R.drawable.preview_faint_ui_ic_signal_cellular_1_4_bar,
            R.drawable.preview_faint_ui_ic_signal_cellular_2_4_bar,
            R.drawable.preview_faint_ui_ic_signal_cellular_3_4_bar,
            R.drawable.preview_faint_ui_ic_signal_cellular_4_4_bar
        )
    ),
    Pair(
        "Fan",
        listOf(
            R.drawable.preview_fan_ic_signal_cellular_1_4_bar,
            R.drawable.preview_fan_ic_signal_cellular_2_4_bar,
            R.drawable.preview_fan_ic_signal_cellular_3_4_bar,
            R.drawable.preview_fan_ic_signal_cellular_4_4_bar
        )
    ),
    Pair(
        "Lorn",
        listOf(
            R.drawable.preview_lorn_ic_signal_cellular_1_4_bar,
            R.drawable.preview_lorn_ic_signal_cellular_2_4_bar,
            R.drawable.preview_lorn_ic_signal_cellular_3_4_bar,
            R.drawable.preview_lorn_ic_signal_cellular_4_4_bar
        )
    ),
    Pair(
        "Gradicon",
        listOf(
            R.drawable.preview_gradicon_ic_signal_cellular_1_4_bar,
            R.drawable.preview_gradicon_ic_signal_cellular_2_4_bar,
            R.drawable.preview_gradicon_ic_signal_cellular_3_4_bar,
            R.drawable.preview_gradicon_ic_signal_cellular_4_4_bar
        )
    ),
    Pair(
        "Huawei",
        listOf(
            R.drawable.preview_huawei_ic_signal_cellular_1_4_bar,
            R.drawable.preview_huawei_ic_signal_cellular_2_4_bar,
            R.drawable.preview_huawei_ic_signal_cellular_3_4_bar,
            R.drawable.preview_huawei_ic_signal_cellular_4_4_bar
        )
    ),
    Pair(
        "Inside",
        listOf(
            R.drawable.preview_inside_ic_signal_cellular_1_4_bar,
            R.drawable.preview_inside_ic_signal_cellular_2_4_bar,
            R.drawable.preview_inside_ic_signal_cellular_3_4_bar,
            R.drawable.preview_inside_ic_signal_cellular_4_4_bar
        )
    ),
    Pair(
        "iOS",
        listOf(
            R.drawable.preview_ios_ic_signal_cellular_1_4_bar,
            R.drawable.preview_ios_ic_signal_cellular_2_4_bar,
            R.drawable.preview_ios_ic_signal_cellular_3_4_bar,
            R.drawable.preview_ios_ic_signal_cellular_4_4_bar
        )
    ),
    Pair(
        "Mini",
        listOf(
            R.drawable.preview_mini_ic_signal_cellular_1_4_bar,
            R.drawable.preview_mini_ic_signal_cellular_2_4_bar,
            R.drawable.preview_mini_ic_signal_cellular_3_4_bar,
            R.drawable.preview_mini_ic_signal_cellular_4_4_bar
        )
    ),
    Pair(
        "Nothing Dot",
        listOf(
            R.drawable.preview_nothing_dot_ic_signal_cellular_1_4_bar,
            R.drawable.preview_nothing_dot_ic_signal_cellular_2_4_bar,
            R.drawable.preview_nothing_dot_ic_signal_cellular_3_4_bar,
            R.drawable.preview_nothing_dot_ic_signal_cellular_4_4_bar
        )
    ),
    Pair(
        "Odin",
        listOf(
            R.drawable.preview_odin_ic_signal_cellular_1_4_bar,
            R.drawable.preview_odin_ic_signal_cellular_2_4_bar,
            R.drawable.preview_odin_ic_signal_cellular_3_4_bar,
            R.drawable.preview_odin_ic_signal_cellular_4_4_bar
        )
    ),
    Pair(
        "Pills",
        listOf(
            R.drawable.preview_pills_ic_signal_cellular_1_4_bar,
            R.drawable.preview_pills_ic_signal_cellular_2_4_bar,
            R.drawable.preview_pills_ic_signal_cellular_3_4_bar,
            R.drawable.preview_pills_ic_signal_cellular_4_4_bar
        )
    ),
    Pair(
        "Plumpy",
        listOf(
            R.drawable.preview_plumpy_ic_signal_cellular_1_4_bar,
            R.drawable.preview_plumpy_ic_signal_cellular_2_4_bar,
            R.drawable.preview_plumpy_ic_signal_cellular_3_4_bar,
            R.drawable.preview_plumpy_ic_signal_cellular_4_4_bar
        )
    ),
    Pair(
        "Rel",
        listOf(
            R.drawable.preview_rel_ic_signal_cellular_1_4_bar,
            R.drawable.preview_rel_ic_signal_cellular_2_4_bar,
            R.drawable.preview_rel_ic_signal_cellular_3_4_bar,
            R.drawable.preview_rel_ic_signal_cellular_4_4_bar
        )
    ),
    Pair(
        "Roman",
        listOf(
            R.drawable.preview_roman_ic_signal_cellular_1_4_bar,
            R.drawable.preview_roman_ic_signal_cellular_2_4_bar,
            R.drawable.preview_roman_ic_signal_cellular_3_4_bar,
            R.drawable.preview_roman_ic_signal_cellular_4_4_bar
        )
    ),
    Pair(
        "Round",
        listOf(
            R.drawable.preview_round_ic_signal_cellular_1_4_bar,
            R.drawable.preview_round_ic_signal_cellular_2_4_bar,
            R.drawable.preview_round_ic_signal_cellular_3_4_bar,
            R.drawable.preview_round_ic_signal_cellular_4_4_bar
        )
    ),
    Pair(
        "Scroll",
        listOf(
            R.drawable.preview_scroll_ic_signal_cellular_1_4_bar,
            R.drawable.preview_scroll_ic_signal_cellular_2_4_bar,
            R.drawable.preview_scroll_ic_signal_cellular_3_4_bar,
            R.drawable.preview_scroll_ic_signal_cellular_4_4_bar
        )
    ),
    Pair(
        "Sea",
        listOf(
            R.drawable.preview_sea_ic_signal_cellular_1_4_bar,
            R.drawable.preview_sea_ic_signal_cellular_2_4_bar,
            R.drawable.preview_sea_ic_signal_cellular_3_4_bar,
            R.drawable.preview_sea_ic_signal_cellular_4_4_bar
        )
    ),
    Pair(
        "Sneaky",
        listOf(
            R.drawable.preview_sneaky_ic_signal_cellular_1_4_bar,
            R.drawable.preview_sneaky_ic_signal_cellular_2_4_bar,
            R.drawable.preview_sneaky_ic_signal_cellular_3_4_bar,
            R.drawable.preview_sneaky_ic_signal_cellular_4_4_bar
        )
    ),
    Pair(
        "Stack",
        listOf(
            R.drawable.preview_stack_ic_signal_cellular_1_4_bar,
            R.drawable.preview_stack_ic_signal_cellular_2_4_bar,
            R.drawable.preview_stack_ic_signal_cellular_3_4_bar,
            R.drawable.preview_stack_ic_signal_cellular_4_4_bar
        )
    ),
    Pair(
        "Stroke",
        listOf(
            R.drawable.preview_stroke_ic_signal_cellular_1_4_bar,
            R.drawable.preview_stroke_ic_signal_cellular_2_4_bar,
            R.drawable.preview_stroke_ic_signal_cellular_3_4_bar,
            R.drawable.preview_stroke_ic_signal_cellular_4_4_bar
        )
    ),
    Pair(
        "Wannui",
        listOf(
            R.drawable.preview_wannui_ic_signal_cellular_1_4_bar,
            R.drawable.preview_wannui_ic_signal_cellular_2_4_bar,
            R.drawable.preview_wannui_ic_signal_cellular_3_4_bar,
            R.drawable.preview_wannui_ic_signal_cellular_4_4_bar
        )
    ),
    Pair(
        "Wavy",
        listOf(
            R.drawable.preview_wavy_ic_signal_cellular_1_4_bar,
            R.drawable.preview_wavy_ic_signal_cellular_2_4_bar,
            R.drawable.preview_wavy_ic_signal_cellular_3_4_bar,
            R.drawable.preview_wavy_ic_signal_cellular_4_4_bar
        )
    ),
    Pair(
        "Windows",
        listOf(
            R.drawable.preview_windows_ic_signal_cellular_1_4_bar,
            R.drawable.preview_windows_ic_signal_cellular_2_4_bar,
            R.drawable.preview_windows_ic_signal_cellular_3_4_bar,
            R.drawable.preview_windows_ic_signal_cellular_4_4_bar
        )
    ),
    Pair(
        "Wing",
        listOf(
            R.drawable.preview_wing_ic_signal_cellular_1_4_bar,
            R.drawable.preview_wing_ic_signal_cellular_2_4_bar,
            R.drawable.preview_wing_ic_signal_cellular_3_4_bar,
            R.drawable.preview_wing_ic_signal_cellular_4_4_bar
        )
    ),
    Pair(
        "Xperia",
        listOf(
            R.drawable.preview_xperia_ic_signal_cellular_1_4_bar,
            R.drawable.preview_xperia_ic_signal_cellular_2_4_bar,
            R.drawable.preview_xperia_ic_signal_cellular_3_4_bar,
            R.drawable.preview_xperia_ic_signal_cellular_4_4_bar
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
fun CellularIconScreen(cellularIconViewModel: CellularIconViewModel = hiltViewModel()) {
    val scrollState = rememberScrollState()
    val cellularIcons by rememberSaveable { mutableStateOf(cellularIconList) }
    val cellularIconStyle by cellularIconViewModel.cellularIconStyle.collectAsStateWithLifecycle()
    val isApplying by cellularIconViewModel.isLoading.collectAsStateWithLifecycle()

    LaunchedEffect(cellularIcons) {
        cellularIconViewModel.refreshState()
    }

    if (isApplying) {
        LoadingDialog()
    }

    ToastAppliedEvent(cellularIconViewModel.uiEvent)

    AppScaffold(
        title = stringResource(R.string.activity_title_cellular_icons),
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
                cellularIcons.forEach { cellularIcons ->
                    val isApplied = cellularIconStyle == cellularIcons.id

                    CellularIconCard(
                        iconPack = cellularIcons.copy(isApplied = isApplied),
                        onClick = {
                            cellularIconViewModel.togglePack(cellularIcons.id)
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Phone", device = Devices.PHONE)
@Composable
private fun CellularIconScreenPhonePreview() {
    PreviewComposable {
        CellularIconScreen()
    }
}

@Preview(showBackground = true, name = "Tablet", device = Devices.TABLET)
@Composable
private fun CellularIconScreenTabletPreview() {
    PreviewComposable {
        CellularIconScreen()
    }
}