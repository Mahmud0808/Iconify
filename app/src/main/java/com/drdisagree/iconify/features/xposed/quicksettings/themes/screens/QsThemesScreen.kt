package com.drdisagree.iconify.features.xposed.quicksettings.themes.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.preferences.PreferenceScreen
import com.drdisagree.iconify.core.preferences.preferenceScreen
import com.drdisagree.iconify.core.preferences.stringRes
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable
import com.drdisagree.iconify.data.keys.XposedKey

val qsThemesPreferences = preferenceScreen {
    category {
        switch(
            key = XposedKey.CUSTOM_QS_THEME,
            isMasterSwitch = true,
            title = stringRes("Quick settings theme"),
            summary = { _, _ -> stringRes("Enable custom QS theme") }
        )
    }

    category {
        colorPicker(
            key = XposedKey.QS_TILE_BACKGROUND_COLOR,
            title = stringRes("Tile background color"),
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_QS_THEME) }
        )

        colorPicker(
            key = XposedKey.QS_TILE_ICON_COLOR,
            title = stringRes("Icon color"),
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_QS_THEME) }
        )

        colorPicker(
            key = XposedKey.QS_TILE_ICON_BACKGROUND_COLOR,
            title = stringRes("Icon background color"),
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_QS_THEME) }
        )

        colorPicker(
            key = XposedKey.QS_TILE_LABEL_COLOR,
            title = stringRes("Label color"),
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_QS_THEME) }
        )

        colorPicker(
            key = XposedKey.QS_TILE_SECONDARY_LABEL_COLOR,
            title = stringRes("Secondary label color"),
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_QS_THEME) }
        )

        //        filePicker(
        //            key = "file_path",
        //            title = stringRes("Image picker test"),
        //            pickerType = FilePickerType.Image,
        //            onFileSelected = { pref, uriString ->
        //                if (uriString.isNotEmpty()) {
        //                    val path = uriString.toUri().toXposedSharedPath(HEADER_IMAGE_FILE.name)
        //                    pref.get("file_path2", path)
        //                } else {
        //                    pref.set("file_path2", PrefValue.None)
        //                }
        //            },
        //            isEnabled = { it.getBoolean("customQsTheme", false) }
        //        )
    }
}

@Composable
fun QsThemesScreen() {
    PreferenceScreen(
        items = qsThemesPreferences,
        title = stringResource(R.string.activity_title_themes),
        showBackIcon = true
    )
}

@Preview(showBackground = true)
@Composable
fun QsThemesScreenPreview() {
    PreviewComposable {
        QsThemesScreen()
    }
}