package com.drdisagree.iconify.features.xposed.statusbar.logo.screens

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.common.LocalPreferenceController
import com.drdisagree.iconify.core.preferences.PreferenceListener
import com.drdisagree.iconify.core.preferences.PreferenceScreen
import com.drdisagree.iconify.core.preferences.arrayRes
import com.drdisagree.iconify.core.preferences.preferenceScreen
import com.drdisagree.iconify.core.preferences.stringRes
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable
import com.drdisagree.iconify.core.ui.components.preferences.FilePickerType
import com.drdisagree.iconify.data.common.XposedConst.STATUSBAR_LOGO_FILE
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.features.xposed.statusbar.logo.components.StatusbarLogoBottomSheet
import com.drdisagree.iconify.features.xposed.statusbar.logo.components.rememberStatusbarLogoItems
import com.drdisagree.iconify.helpers.toXposedSharedPath
import kotlinx.coroutines.launch

fun statusbarLogoPreferences(
    selectedLogoLabel: String = "",
    onLogoStyleClick: () -> Unit = {}
) = preferenceScreen {
    category {
        switch(
            key = XposedKey.STATUSBAR_LOGO,
            isMasterSwitch = true,
            title = stringRes(R.string.status_bar_logo_enable_title),
        )
    }

    category {
        listPref(
            key = XposedKey.STATUSBAR_LOGO_POSITION,
            title = stringRes(R.string.status_bar_logo_position_title),
            entries = arrayRes(R.array.status_bar_logo_position_entries),
            entryValues = arrayRes(R.array.status_bar_logo_position_values),
            isEnabled = { it.getBoolean(XposedKey.STATUSBAR_LOGO) },
        )

        action(
            key = "statusbar_logo_style",
            title = stringRes(R.string.status_bar_logo_style_title),
            summary = { _, _ -> stringRes(selectedLogoLabel) },
            onClick = { _, _, _ -> onLogoStyleClick() },
            isEnabled = { it.getBoolean(XposedKey.STATUSBAR_LOGO) },
        )

        filePicker(
            key = XposedKey.STATUSBAR_LOGO_FILE_URI,
            title = stringRes(R.string.status_bar_logo_chooser_title),
            summary = { _, _ -> stringRes(R.string.status_bar_logo_chooser_desc) },
            pickerType = FilePickerType.Image,
            saveFileUri = true,
            onFileSelected = { _, uriString ->
                if (uriString.isNotEmpty()) {
                    uriString.toUri().toXposedSharedPath(STATUSBAR_LOGO_FILE.name)
                }
            },
            isEnabled = { it.getBoolean(XposedKey.STATUSBAR_LOGO) },
            isVisible = { it.getString(XposedKey.STATUSBAR_LOGO_STYLE) in setOf("33") },
        )

        slider(
            key = XposedKey.STATUSBAR_LOGO_SIZE,
            title = stringRes(R.string.status_bar_logo_size_title),
            min = 12f,
            max = 40f,
            valueLabel = { "${it.toInt()}dp" },
            isEnabled = { it.getBoolean(XposedKey.STATUSBAR_LOGO) }
        )

        switch(
            key = XposedKey.STATUSBAR_LOGO_TINT,
            title = stringRes(R.string.status_bar_logo_tint_title),
            summary = { _, _ -> stringRes(R.string.status_bar_logo_tint_desc) },
            isEnabled = { it.getBoolean(XposedKey.STATUSBAR_LOGO) },
            isVisible = { it.getString(XposedKey.STATUSBAR_LOGO_STYLE) in setOf("33") },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusbarLogoScreen() {
    val context = LocalContext.current
    val prefController = LocalPreferenceController.current

    var reloadKey by rememberSaveable { mutableIntStateOf(0) }
    val logoItems = rememberStatusbarLogoItems(context, reloadKey)
    var showLogoStyleSheet by rememberSaveable { mutableStateOf(false) }
    val selectedItemValue by prefController.observe(
        XposedKey.STATUSBAR_LOGO_STYLE.name,
        XposedKey.STATUSBAR_LOGO_STYLE.default as String
    )
    val selectedItemIndex = remember(selectedItemValue, logoItems) {
        logoItems.indexOfFirst { it.value == selectedItemValue }.takeIf { it >= 0 } ?: 0
    }

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    if (showLogoStyleSheet) {
        StatusbarLogoBottomSheet(
            sheetState = sheetState,
            iconPacks = logoItems,
            selectedItemIndex = selectedItemIndex,
            onItemClick = { index ->
                prefController.setString(
                    XposedKey.STATUSBAR_LOGO_STYLE.name,
                    index.toString()
                )

                if (index == logoItems.lastIndex) {
                    scope.launch {
                        sheetState.hide()
                    }
                }
            },
            onDismiss = { showLogoStyleSheet = false }
        )
    }

    PreferenceListener(key = XposedKey.STATUSBAR_LOGO_FILE_URI) {
        reloadKey++
    }

    PreferenceScreen(
        items = statusbarLogoPreferences(
            selectedLogoLabel = logoItems.getOrNull(selectedItemIndex)?.label
                ?: stringResource(R.string.not_available),
            onLogoStyleClick = { showLogoStyleSheet = true }
        ),
        title = stringResource(R.string.status_bar_logo_title),
        showBackIcon = true
    )
}

@Preview(showBackground = true)
@Composable
fun StatusbarLogoScreenPreview() {
    PreviewComposable {
        StatusbarLogoScreen()
    }
}