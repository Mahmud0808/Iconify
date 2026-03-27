package com.drdisagree.iconify.features.home.iconpack.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.ui.components.dialogs.LoadingDialog
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable
import com.drdisagree.iconify.core.ui.components.others.innerPaddingValues
import com.drdisagree.iconify.core.ui.components.scaffolds.AppScaffold
import com.drdisagree.iconify.core.ui.utils.CARD_ITEM_SPACING
import com.drdisagree.iconify.core.ui.utils.cardCorners
import com.drdisagree.iconify.core.ui.utils.resolvePosition
import com.drdisagree.iconify.data.models.IconPackPreview
import com.drdisagree.iconify.data.states.UiText
import com.drdisagree.iconify.features.home.iconpack.components.IconPackCard
import com.drdisagree.iconify.features.home.iconpack.viewmodels.IconPackViewModel

private val iconPackList = listOf(
    Triple(
        "Aurora",
        R.string.iconpack_aurora_desc,
        listOf(
            R.drawable.preview_aurora_wifi,
            R.drawable.preview_aurora_signal,
            R.drawable.preview_aurora_airplane,
            R.drawable.preview_aurora_location
        )
    ),
    Triple(
        "Gradicon",
        R.string.iconpack_gradicon_desc,
        listOf(
            R.drawable.preview_gradicon_wifi,
            R.drawable.preview_gradicon_signal,
            R.drawable.preview_gradicon_airplane,
            R.drawable.preview_gradicon_location
        )
    ),
    Triple(
        "Lorn",
        R.string.iconpack_lorn_desc,
        listOf(
            R.drawable.preview_lorn_wifi,
            R.drawable.preview_lorn_signal,
            R.drawable.preview_lorn_airplane,
            R.drawable.preview_lorn_location
        )
    ),
    Triple(
        "Plumpy",
        R.string.iconpack_plumpy_desc,
        listOf(
            R.drawable.preview_plumpy_wifi,
            R.drawable.preview_plumpy_signal,
            R.drawable.preview_plumpy_airplane,
            R.drawable.preview_plumpy_location
        )
    ),
    Triple(
        "Acherus",
        R.string.iconpack_acherus_desc,
        listOf(
            R.drawable.preview_acherus_wifi,
            R.drawable.preview_acherus_signal,
            R.drawable.preview_acherus_airplane,
            R.drawable.preview_acherus_location
        )
    ),
    Triple(
        "Circular",
        R.string.iconpack_circular_desc,
        listOf(
            R.drawable.preview_circular_wifi,
            R.drawable.preview_circular_signal,
            R.drawable.preview_circular_airplane,
            R.drawable.preview_circular_location
        )
    ),
    Triple(
        "Filled",
        R.string.iconpack_filled_desc,
        listOf(
            R.drawable.preview_filled_wifi,
            R.drawable.preview_filled_signal,
            R.drawable.preview_filled_airplane,
            R.drawable.preview_filled_location
        )
    ),
    Triple(
        "Kai",
        R.string.iconpack_kai_desc,
        listOf(
            R.drawable.preview_kai_wifi,
            R.drawable.preview_kai_signal,
            R.drawable.preview_kai_airplane,
            R.drawable.preview_kai_location
        )
    ),
    Triple(
        "OOS",
        R.string.iconpack_oos_desc,
        listOf(
            R.drawable.preview_oos_wifi,
            R.drawable.preview_oos_signal,
            R.drawable.preview_oos_airplane,
            R.drawable.preview_oos_location
        )
    ),
    Triple(
        "Outline",
        R.string.iconpack_outline_desc,
        listOf(
            R.drawable.preview_outline_wifi,
            R.drawable.preview_outline_signal,
            R.drawable.preview_outline_airplane,
            R.drawable.preview_outline_location
        )
    ),
    Triple(
        "PUI",
        R.string.iconpack_pui_desc,
        listOf(
            R.drawable.preview_pui_wifi,
            R.drawable.preview_pui_signal,
            R.drawable.preview_pui_airplane,
            R.drawable.preview_pui_location
        )
    ),
    Triple(
        "Rounded",
        R.string.iconpack_rounded_desc,
        listOf(
            R.drawable.preview_rounded_wifi,
            R.drawable.preview_rounded_signal,
            R.drawable.preview_rounded_airplane,
            R.drawable.preview_rounded_location
        )
    ),
    Triple(
        "Sam",
        R.string.iconpack_sam_desc,
        listOf(
            R.drawable.preview_sam_wifi,
            R.drawable.preview_sam_signal,
            R.drawable.preview_sam_airplane,
            R.drawable.preview_sam_location
        )
    ),
    Triple(
        "Victor",
        R.string.iconpack_victor_desc,
        listOf(
            R.drawable.preview_victor_wifi,
            R.drawable.preview_victor_signal,
            R.drawable.preview_victor_airplane,
            R.drawable.preview_victor_location
        )
    )
).mapIndexed { index, (name, descRes, icons) ->
    IconPackPreview(
        id = (index + 1).toString(),
        title = UiText.Text(name),
        summary = UiText.Res(descRes),
        icons = icons
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconPackScreen(iconPackViewModel: IconPackViewModel = hiltViewModel()) {
    val iconPacks by rememberSaveable { mutableStateOf(iconPackList) }
    val overlayStates by iconPackViewModel.iconPackStates.collectAsStateWithLifecycle()
    var expandedPackId by rememberSaveable { mutableStateOf<String?>(null) }
    val isApplying by iconPackViewModel.isLoading.collectAsStateWithLifecycle()

    val indices by rememberSaveable(iconPacks) { mutableStateOf(iconPacks.indices.toList()) }

    LaunchedEffect(iconPacks) {
        iconPackViewModel.checkAllStatuses(iconPacks)
    }

    if (isApplying) {
        LoadingDialog()
    }

    AppScaffold(
        title = stringResource(R.string.activity_title_icon_pack),
        showBackIcon = true,
    ) { innerPadding, _ ->
        val padding = innerPaddingValues(
            innerPadding = innerPadding,
            start = 16.dp,
            end = 16.dp,
            top = 16.dp
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(CARD_ITEM_SPACING),
            contentPadding = padding,
        ) {
            itemsIndexed(iconPacks) { index, pack ->
                val isApplied = overlayStates[pack.id] ?: false
                val position = resolvePosition(indices, index)
                val corners = cardCorners(position)

                IconPackCard(
                    iconPack = pack.copy(isApplied = isApplied),
                    isSelected = expandedPackId == pack.id,
                    onClick = {
                        expandedPackId = if (expandedPackId == pack.id) null else pack.id
                    },
                    onActionClick = {
                        iconPackViewModel.togglePack(pack.id)
                    },
                    shape = RoundedCornerShape(
                        topStart = corners.topStart,
                        topEnd = corners.topEnd,
                        bottomStart = corners.bottomStart,
                        bottomEnd = corners.bottomEnd
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun IconPackScreenPreview() {
    PreviewComposable {
        IconPackScreen()
    }
}