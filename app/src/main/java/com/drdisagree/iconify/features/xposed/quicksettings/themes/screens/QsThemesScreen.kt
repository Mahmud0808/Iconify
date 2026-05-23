package com.drdisagree.iconify.features.xposed.quicksettings.themes.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.preferences.PreferenceListener
import com.drdisagree.iconify.core.preferences.PreferenceScreen
import com.drdisagree.iconify.core.preferences.preferenceScreen
import com.drdisagree.iconify.core.preferences.stringRes
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.features.common.viewmodels.SystemActionViewModel

val qsThemesPreferences = preferenceScreen {
    category {
        switch(
            key = XposedKey.CUSTOM_QS_THEME,
            isMasterSwitch = true,
            title = stringRes(R.string.quick_settings_theme),
        )
    }

    category(title = stringRes(R.string.section_title_active_tile_colors)) {
        colorPicker(
            key = XposedKey.ACTIVE_QS_TILE_BACKGROUND_COLOR,
            title = stringRes(R.string.tile_background_color),
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_QS_THEME) }
        )

        colorPicker(
            key = XposedKey.ACTIVE_QS_TILE_ICON_COLOR,
            title = stringRes(R.string.icon_color),
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_QS_THEME) }
        )

        colorPicker(
            key = XposedKey.ACTIVE_QS_TILE_ICON_BACKGROUND_COLOR,
            title = stringRes(R.string.icon_background_color),
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_QS_THEME) }
        )

        colorPicker(
            key = XposedKey.ACTIVE_QS_TILE_LABEL_COLOR,
            title = stringRes(R.string.label_color),
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_QS_THEME) }
        )

        colorPicker(
            key = XposedKey.ACTIVE_QS_TILE_SECONDARY_LABEL_COLOR,
            title = stringRes(R.string.secondary_label_color),
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_QS_THEME) }
        )
    }

    category(title = stringRes(R.string.section_title_inactive_tile_colors)) {
        colorPicker(
            key = XposedKey.INACTIVE_QS_TILE_BACKGROUND_COLOR,
            title = stringRes(R.string.tile_background_color),
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_QS_THEME) }
        )

        colorPicker(
            key = XposedKey.INACTIVE_QS_TILE_ICON_COLOR,
            title = stringRes(R.string.icon_color),
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_QS_THEME) }
        )

        colorPicker(
            key = XposedKey.INACTIVE_QS_TILE_ICON_BACKGROUND_COLOR,
            title = stringRes(R.string.icon_background_color),
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_QS_THEME) }
        )

        colorPicker(
            key = XposedKey.INACTIVE_QS_TILE_LABEL_COLOR,
            title = stringRes(R.string.label_color),
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_QS_THEME) }
        )

        colorPicker(
            key = XposedKey.INACTIVE_QS_TILE_SECONDARY_LABEL_COLOR,
            title = stringRes(R.string.secondary_label_color),
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_QS_THEME) }
        )
    }

    category(title = stringRes(R.string.section_title_disabled_tile_colors)) {
        colorPicker(
            key = XposedKey.UNAVAILABLE_QS_TILE_BACKGROUND_COLOR,
            title = stringRes(R.string.tile_background_color),
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_QS_THEME) }
        )

        colorPicker(
            key = XposedKey.UNAVAILABLE_QS_TILE_ICON_COLOR,
            title = stringRes(R.string.icon_color),
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_QS_THEME) }
        )

        colorPicker(
            key = XposedKey.UNAVAILABLE_QS_TILE_ICON_BACKGROUND_COLOR,
            title = stringRes(R.string.icon_background_color),
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_QS_THEME) }
        )

        colorPicker(
            key = XposedKey.UNAVAILABLE_QS_TILE_LABEL_COLOR,
            title = stringRes(R.string.label_color),
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_QS_THEME) }
        )

        colorPicker(
            key = XposedKey.UNAVAILABLE_QS_TILE_SECONDARY_LABEL_COLOR,
            title = stringRes(R.string.secondary_label_color),
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_QS_THEME) }
        )
    }
}

@Composable
fun QsThemesScreen(
    systemActionViewModel: SystemActionViewModel? = hiltViewModel(),
) {
    PreferenceListener(key = null) { event ->
        when (event.key) {
            XposedKey.CUSTOM_QS_THEME.name,
            XposedKey.ACTIVE_QS_TILE_BACKGROUND_COLOR.name,
            XposedKey.ACTIVE_QS_TILE_ICON_COLOR.name,
            XposedKey.ACTIVE_QS_TILE_ICON_BACKGROUND_COLOR.name,
            XposedKey.ACTIVE_QS_TILE_LABEL_COLOR.name,
            XposedKey.ACTIVE_QS_TILE_SECONDARY_LABEL_COLOR.name,
            XposedKey.INACTIVE_QS_TILE_BACKGROUND_COLOR.name,
            XposedKey.INACTIVE_QS_TILE_ICON_COLOR.name,
            XposedKey.INACTIVE_QS_TILE_ICON_BACKGROUND_COLOR.name,
            XposedKey.INACTIVE_QS_TILE_LABEL_COLOR.name,
            XposedKey.INACTIVE_QS_TILE_SECONDARY_LABEL_COLOR.name,
            XposedKey.UNAVAILABLE_QS_TILE_BACKGROUND_COLOR.name,
            XposedKey.UNAVAILABLE_QS_TILE_ICON_COLOR.name,
            XposedKey.UNAVAILABLE_QS_TILE_ICON_BACKGROUND_COLOR.name,
            XposedKey.UNAVAILABLE_QS_TILE_LABEL_COLOR.name,
            XposedKey.UNAVAILABLE_QS_TILE_SECONDARY_LABEL_COLOR.name,
                -> {
                systemActionViewModel?.shouldRestartSystemUI()
            }
        }
    }

    PreferenceScreen(
        items = qsThemesPreferences,
        title = stringResource(R.string.activity_title_themes),
        showBackIcon = true
    )
}

@Preview(showBackground = true)
@Composable
private fun QsThemesScreenPreview() {
    PreviewComposable {
        QsThemesScreen(null)
    }
}