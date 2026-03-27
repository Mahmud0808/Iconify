package com.drdisagree.iconify.features.home.settingsicons.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable
import com.drdisagree.iconify.core.ui.components.others.innerPaddingValues
import com.drdisagree.iconify.core.ui.components.scaffolds.AppScaffold

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsIconsScreen() {
    val scrollState = rememberScrollState()

    val backgroundOptions = listOf(
        R.string.settings_icons_minimal,
        R.string.settings_icons_filled,
        R.string.settings_icons_outline,
        R.string.settings_icons_neumorph,
    ).map { stringResource(it) }
    val shapeOptions = listOf(
        R.string.settings_icons_circle,
        R.string.settings_icons_squircle,
        R.string.settings_icons_square,
    ).map { stringResource(it) }
    val sizeOptions = listOf(
        R.string.settings_icons_size1,
        R.string.settings_icons_size2,
        R.string.settings_icons_size3,
        R.string.settings_icons_size4,
    ).map { stringResource(it) }
    val colorOptions = listOf(
        R.string.settings_icons_follow_system,
        R.string.settings_icons_system_inverse,
        R.string.settings_icons_monet_accent,
    ).map { stringResource(it) }

    var backgroundSelectedIndex by remember { mutableIntStateOf(0) }
    var shapeSelectedIndex by remember { mutableIntStateOf(0) }
    var sizeSelectedIndex by remember { mutableIntStateOf(0) }
    var colorSelectedIndex by remember { mutableIntStateOf(0) }

    AppScaffold(
        title = stringResource(R.string.activity_title_settings_icons),
        showBackIcon = true,
    ) { innerPadding, _ ->
        val padding = innerPaddingValues(
            innerPadding = innerPadding,
            top = 16.dp
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(padding)
        ) {
            Text(
                text = stringResource(R.string.settings_icons_background),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 4.dp)
            )
            Row(
                Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
            ) {
                backgroundOptions.forEachIndexed { index, label ->
                    ToggleButton(
                        checked = backgroundSelectedIndex == index,
                        onCheckedChange = { backgroundSelectedIndex = index },
                        modifier = Modifier.semantics { role = Role.RadioButton },
                        shapes =
                            when (index) {
                                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                backgroundOptions.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                            },
                    ) {
                        Text(text = label)
                    }
                }
            }
            Text(
                text = stringResource(R.string.settings_icons_shape),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 4.dp)
            )
            Row(
                Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
            ) {
                shapeOptions.forEachIndexed { index, label ->
                    ToggleButton(
                        checked = shapeSelectedIndex == index,
                        onCheckedChange = { shapeSelectedIndex = index },
                        modifier = Modifier.semantics { role = Role.RadioButton },
                        shapes =
                            when (index) {
                                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                shapeOptions.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                            },
                    ) {
                        Text(text = label)
                    }
                }
            }
            Text(
                text = stringResource(R.string.settings_icons_size),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 4.dp)
            )
            Row(
                Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
            ) {
                sizeOptions.forEachIndexed { index, label ->
                    ToggleButton(
                        checked = sizeSelectedIndex == index,
                        onCheckedChange = { sizeSelectedIndex = index },
                        modifier = Modifier.semantics { role = Role.RadioButton },
                        shapes =
                            when (index) {
                                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                sizeOptions.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                            },
                    ) {
                        Text(text = label)
                    }
                }
            }
            Text(
                text = stringResource(R.string.settins_icons_icon_color),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 4.dp)
            )
            Row(
                Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
            ) {
                colorOptions.forEachIndexed { index, label ->
                    ToggleButton(
                        checked = colorSelectedIndex == index,
                        onCheckedChange = { colorSelectedIndex = index },
                        modifier = Modifier.semantics { role = Role.RadioButton },
                        shapes =
                            when (index) {
                                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                colorOptions.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                            },
                    ) {
                        Text(text = label)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Phone", device = Devices.PHONE)
@Composable
fun IconShapeScreenPhonePreview() {
    PreviewComposable {
        SettingsIconsScreen()
    }
}

@Preview(showBackground = true, name = "Tablet", device = Devices.TABLET)
@Composable
fun IconShapeScreenTabletPreview() {
    PreviewComposable {
        SettingsIconsScreen()
    }
}