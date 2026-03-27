package com.drdisagree.iconify.features.home.iconshape.screens

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
import com.drdisagree.iconify.data.models.IconShapePreview
import com.drdisagree.iconify.data.states.UiText
import com.drdisagree.iconify.features.home.iconshape.components.IconShapeCard
import com.drdisagree.iconify.features.home.iconshape.viewmodels.IconShapeViewModel

private val iconShapeList = listOf(
    Pair(
        R.string.icon_mask_style_round,
        R.drawable.icon_shape_round
    ),
    Pair(
        R.string.icon_mask_style_pebble,
        R.drawable.icon_shape_pebble
    ),
    Pair(
        R.string.icon_mask_style_hexagon,
        R.drawable.icon_shape_rounded_hexagon
    ),
    Pair(
        R.string.icon_mask_style_samsung,
        R.drawable.icon_shape_samsung
    ),
    Pair(
        R.string.icon_mask_style_scroll,
        R.drawable.icon_shape_scroll
    ),
    Pair(
        R.string.icon_mask_style_teardrop,
        R.drawable.icon_shape_teardrops
    ),
    Pair(
        R.string.icon_mask_style_square,
        R.drawable.icon_shape_square
    ),
    Pair(
        R.string.icon_mask_style_rounded_rectangle,
        R.drawable.icon_shape_rounded_rectangle
    ),
    Pair(
        R.string.icon_mask_style_ios,
        R.drawable.icon_shape_ios
    ),
    Pair(
        R.string.icon_mask_style_cloudy,
        R.drawable.icon_shape_cloudy
    ),
    Pair(
        R.string.icon_mask_style_cylinder,
        R.drawable.icon_shape_cylinder
    ),
    Pair(
        R.string.icon_mask_style_flower,
        R.drawable.icon_shape_flower
    ),
    Pair(
        R.string.icon_mask_style_heart,
        R.drawable.icon_shape_heart
    ),
    Pair(
        R.string.icon_mask_style_leaf,
        R.drawable.icon_shape_leaf
    ),
    Pair(
        R.string.icon_mask_style_stretched,
        R.drawable.icon_shape_stretched
    ),
    Pair(
        R.string.icon_mask_style_tapered_rectangle,
        R.drawable.icon_shape_tapered_rectangle
    ),
    Pair(
        R.string.icon_mask_style_vessel,
        R.drawable.icon_shape_vessel
    ),
    Pair(
        R.string.icon_mask_style_rice_rohie_meow,
        R.drawable.icon_shape_rohie_meow
    ),
).mapIndexed { index, (name, icons) ->
    IconShapePreview(
        id = (index + 1).toString(),
        title = UiText.Res(name),
        shape = icons
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconShapeScreen(iconShapeViewModel: IconShapeViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val iconShapes by rememberSaveable { mutableStateOf(iconShapeList) }
    val toastFrameStyle by iconShapeViewModel.iconShapeStyle.collectAsStateWithLifecycle()
    val isApplying by iconShapeViewModel.isLoading.collectAsStateWithLifecycle()

    LaunchedEffect(iconShapes) {
        iconShapeViewModel.refreshState()
    }

    if (isApplying) {
        LoadingDialog()
    }

    LaunchedEffect(Unit) {
        iconShapeViewModel.uiEvent.collect { event ->
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
        title = stringResource(R.string.activity_title_icon_shape),
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
                iconShapes.forEach { iconShape ->
                    val isApplied = toastFrameStyle == iconShape.id

                    IconShapeCard(
                        iconShape = iconShape.copy(isApplied = isApplied),
                        onClick = {
                            iconShapeViewModel.togglePack(iconShape.id)
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Phone", device = Devices.PHONE)
@Composable
fun IconShapeScreenPhonePreview() {
    PreviewComposable {
        IconShapeScreen()
    }
}

@Preview(showBackground = true, name = "Tablet", device = Devices.TABLET)
@Composable
fun IconShapeScreenTabletPreview() {
    PreviewComposable {
        IconShapeScreen()
    }
}