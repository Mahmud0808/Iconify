package com.drdisagree.iconify.features.home.settingsicons.screens

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.drdisagree.iconify.core.ui.components.others.ToastAppliedEvent
import com.drdisagree.iconify.core.ui.components.others.innerPaddingValues
import com.drdisagree.iconify.core.ui.components.others.withHaptic
import com.drdisagree.iconify.core.ui.components.scaffolds.AppScaffold
import com.drdisagree.iconify.core.ui.utils.CARD_ITEM_SPACING
import com.drdisagree.iconify.core.ui.utils.ItemPosition
import com.drdisagree.iconify.data.models.SettingsIconsPreview
import com.drdisagree.iconify.features.common.models.UiText
import com.drdisagree.iconify.features.home.settingsicons.components.SettingsIconsCard
import com.drdisagree.iconify.features.home.settingsicons.components.SettingsOptionsCard
import com.drdisagree.iconify.features.home.settingsicons.viewmodels.SettingsIconsViewModel

private data class SettingsIconsPack(
    val title: String,
    @param:StringRes val summary: Int,
    val icons: List<Int>,
)

private val settingsIconsList = listOf(
    SettingsIconsPack(
        title = "Aurora",
        summary = R.string.iconpack_aurora_desc,
        icons = listOf(
            R.drawable.preview_aurora_wifi,
            R.drawable.preview_aurora_signal,
            R.drawable.preview_aurora_airplane,
            R.drawable.preview_aurora_location
        )
    ),
    SettingsIconsPack(
        title = "Gradicon",
        summary = R.string.iconpack_gradicon_desc,
        icons = listOf(
            R.drawable.preview_gradicon_wifi,
            R.drawable.preview_gradicon_signal,
            R.drawable.preview_gradicon_airplane,
            R.drawable.preview_gradicon_location
        )
    ),
    SettingsIconsPack(
        title = "Lorn",
        summary = R.string.iconpack_lorn_desc,
        icons = listOf(
            R.drawable.preview_lorn_wifi,
            R.drawable.preview_lorn_signal,
            R.drawable.preview_lorn_airplane,
            R.drawable.preview_lorn_location
        )
    ),
    SettingsIconsPack(
        title = "Plumpy",
        summary = R.string.iconpack_plumpy_desc,
        icons = listOf(
            R.drawable.preview_plumpy_wifi,
            R.drawable.preview_plumpy_signal,
            R.drawable.preview_plumpy_airplane,
            R.drawable.preview_plumpy_location
        )
    ),
    SettingsIconsPack(
        title = "Bubble v1",
        summary = R.string.settings_iconpack_bubble_v1,
        icons = listOf(
            R.drawable.preview_settings_icon_bubble_v1_1,
            R.drawable.preview_settings_icon_bubble_v1_2,
            R.drawable.preview_settings_icon_bubble_v1_3,
            R.drawable.preview_settings_icon_bubble_v1_4
        )
    ),
    SettingsIconsPack(
        title = "Bubble v2",
        summary = R.string.settings_iconpack_bubble_v2,
        icons = listOf(
            R.drawable.preview_settings_icon_bubble_v2_1,
            R.drawable.preview_settings_icon_bubble_v2_2,
            R.drawable.preview_settings_icon_bubble_v2_3,
            R.drawable.preview_settings_icon_bubble_v2_4
        )
    ),
).map { pack ->
    SettingsIconsPreview(
        title = UiText.Text(pack.title),
        summary = UiText.Res(pack.summary),
        icons = pack.icons
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsIconsScreen(settingsIconsViewModel: SettingsIconsViewModel = hiltViewModel()) {
    val scrollState = rememberScrollState()
    val settingsIcons by rememberSaveable { mutableStateOf(settingsIconsList) }
    val settingsIconsStyle by settingsIconsViewModel.settingsIconsStyle.collectAsStateWithLifecycle()
    val isApplying by settingsIconsViewModel.isLoading.collectAsStateWithLifecycle()

    var backgroundSelectedIndex by remember { mutableIntStateOf(-1) }
    var shapeSelectedIndex by remember { mutableIntStateOf(-1) }
    var sizeSelectedIndex by remember { mutableIntStateOf(-1) }
    var colorSelectedIndex by remember { mutableIntStateOf(-1) }
    var iconPackSelectedIndex by remember { mutableIntStateOf(-1) }

    LaunchedEffect(settingsIcons) {
        settingsIconsViewModel.refreshState()
    }

    if (isApplying) {
        LoadingDialog()
    }

    LaunchedEffect(settingsIconsStyle) {
        val splitStyle = settingsIconsStyle.split(",")

        backgroundSelectedIndex = splitStyle[0].toInt()
        shapeSelectedIndex = splitStyle[1].toInt()
        sizeSelectedIndex = splitStyle[2].toInt()
        colorSelectedIndex = splitStyle[3].toInt()
        iconPackSelectedIndex = splitStyle[4].toInt()
    }

    ToastAppliedEvent(settingsIconsViewModel.uiEvent)

    AppScaffold(
        title = stringResource(R.string.activity_title_settings_icons),
        showBackIcon = true,
    ) { innerPadding, _ ->
        val padding = innerPaddingValues(
            innerPadding = innerPadding,
            top = 16.dp,
            start = 16.dp,
            end = 16.dp
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(padding)
        ) {
            SettingsOptionsCard(
                modifier = Modifier.padding(bottom = CARD_ITEM_SPACING),
                title = stringResource(R.string.settings_icons_background),
                itemPosition = ItemPosition.FIRST,
                buttonLabels = listOf(
                    R.string.settings_icons_minimal,
                    R.string.settings_icons_filled,
                    R.string.settings_icons_outline,
                    R.string.settings_icons_neumorph,
                ).map { stringResource(it) },
                selectedIndex = backgroundSelectedIndex,
                onItemSelected = { backgroundSelectedIndex = it }
            )
            SettingsOptionsCard(
                modifier = Modifier.padding(bottom = CARD_ITEM_SPACING),
                title = stringResource(R.string.settings_icons_shape),
                itemPosition = ItemPosition.MIDDLE,
                buttonLabels = listOf(
                    R.string.settings_icons_circle,
                    R.string.settings_icons_squircle,
                    R.string.settings_icons_square,
                ).map { stringResource(it) },
                selectedIndex = shapeSelectedIndex,
                onItemSelected = { shapeSelectedIndex = it }
            )
            SettingsOptionsCard(
                modifier = Modifier.padding(bottom = CARD_ITEM_SPACING),
                title = stringResource(R.string.settings_icons_size),
                itemPosition = ItemPosition.MIDDLE,
                buttonLabels = listOf(
                    R.string.settings_icons_size1,
                    R.string.settings_icons_size2,
                    R.string.settings_icons_size3,
                    R.string.settings_icons_size4,
                ).map { stringResource(it) },
                selectedIndex = sizeSelectedIndex,
                onItemSelected = { sizeSelectedIndex = it }
            )
            SettingsOptionsCard(
                title = stringResource(R.string.settins_icons_icon_color),
                itemPosition = ItemPosition.LAST,
                buttonLabels = listOf(
                    R.string.settings_icons_follow_system,
                    R.string.settings_icons_system_inverse,
                    R.string.settings_icons_monet_accent,
                ).map { stringResource(it) },
                selectedIndex = colorSelectedIndex,
                onItemSelected = { colorSelectedIndex = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            val currentSelectedStyle = remember {
                derivedStateOf {
                    "$backgroundSelectedIndex,$shapeSelectedIndex,$sizeSelectedIndex,$colorSelectedIndex,$iconPackSelectedIndex"
                }
            }

            val lastIndex = settingsIcons.lastIndex
            settingsIcons.forEachIndexed { index, settingsIcon ->
                SettingsIconsCard(
                    settingsIcon = settingsIcon,
                    isSelected = index == iconPackSelectedIndex,
                    onClick = { iconPackSelectedIndex = index },
                    itemPosition = when (index) {
                        0 -> ItemPosition.FIRST
                        lastIndex -> ItemPosition.LAST
                        else -> ItemPosition.MIDDLE
                    },
                    modifier = Modifier.padding(
                        bottom = if (index == lastIndex) 0.dp else CARD_ITEM_SPACING
                    )
                )
            }

            val showApplyButton = remember {
                derivedStateOf {
                    currentSelectedStyle.value != settingsIconsViewModel.prefDefaultValue
                            && currentSelectedStyle.value != settingsIconsStyle
                            && backgroundSelectedIndex != -1
                            && shapeSelectedIndex != -1
                            && sizeSelectedIndex != -1
                            && colorSelectedIndex != -1
                            && iconPackSelectedIndex != -1
                }
            }

            Button(
                enabled = showApplyButton.value,
                onClick = withHaptic {
                    settingsIconsViewModel.applyStyle(
                        backgroundStyle = backgroundSelectedIndex,
                        backgroundShape = shapeSelectedIndex,
                        iconSize = sizeSelectedIndex,
                        iconColor = colorSelectedIndex,
                        iconSet = iconPackSelectedIndex
                    )
                },
                shapes = ButtonDefaults.shapes(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text(stringResource(R.string.btn_apply))
            }

            val showDisableButton = remember {
                derivedStateOf {
                    settingsIconsStyle != settingsIconsViewModel.prefDefaultValue
                }
            }

            AnimatedVisibility(visible = showDisableButton.value) {
                Button(
                    onClick = withHaptic { settingsIconsViewModel.disableStyle() },
                    shapes = ButtonDefaults.shapes(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text(stringResource(R.string.btn_disable))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun IconShapeScreenPreview() {
    PreviewComposable {
        SettingsIconsScreen()
    }
}