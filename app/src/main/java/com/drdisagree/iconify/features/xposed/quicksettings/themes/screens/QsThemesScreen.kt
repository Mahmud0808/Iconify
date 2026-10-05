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

        switch(
            key = XposedKey.ACTIVE_QS_TILE_GRADIENT,
            title = stringRes(R.string.active_tile_gradient_title),
            summary = { stringRes(R.string.active_tile_gradient_desc) },
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_QS_THEME) }
        )

        colorPicker(
            key = XposedKey.ACTIVE_QS_TILE_GRADIENT_END_COLOR,
            title = stringRes(R.string.active_tile_gradient_end_color),
            isEnabled = {
                it.getBoolean(XposedKey.CUSTOM_QS_THEME) &&
                        it.getBoolean(XposedKey.ACTIVE_QS_TILE_GRADIENT)
            }
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

    category(title = stringRes(R.string.section_title_brightness_slider_colors)) {
        colorPicker(
            key = XposedKey.BRIGHTNESS_SLIDER_ACTIVE_COLOR,
            title = stringRes(R.string.brightness_slider_active_color),
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_QS_THEME) }
        )

        colorPicker(
            key = XposedKey.BRIGHTNESS_SLIDER_INACTIVE_COLOR,
            title = stringRes(R.string.brightness_slider_inactive_color),
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_QS_THEME) }
        )

        colorPicker(
            key = XposedKey.BRIGHTNESS_SLIDER_THUMB_COLOR,
            title = stringRes(R.string.brightness_slider_thumb_color),
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_QS_THEME) }
        )

        switch(
            key = XposedKey.BRIGHTNESS_SLIDER_GRADIENT,
            title = stringRes(R.string.brightness_slider_gradient_title),
            summary = { stringRes(R.string.brightness_slider_gradient_desc) },
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_QS_THEME) }
        )

        colorPicker(
            key = XposedKey.BRIGHTNESS_SLIDER_GRADIENT_END_COLOR,
            title = stringRes(R.string.brightness_slider_gradient_end_color),
            isEnabled = {
                it.getBoolean(XposedKey.CUSTOM_QS_THEME) &&
                        it.getBoolean(XposedKey.BRIGHTNESS_SLIDER_GRADIENT)
            }
        )
    }

    category(title = stringRes(R.string.section_title_qs_footer_colors)) {
        colorPicker(
            key = XposedKey.QS_FOOTER_INACTIVE_BUTTON_BACKGROUND_COLOR,
            title = stringRes(R.string.qs_footer_inactive_button_background_color),
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_QS_THEME) }
        )

        colorPicker(
            key = XposedKey.QS_FOOTER_INACTIVE_BUTTON_ICON_COLOR,
            title = stringRes(R.string.qs_footer_inactive_button_icon_color),
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_QS_THEME) }
        )

        colorPicker(
            key = XposedKey.QS_FOOTER_ACTIVE_BUTTON_BACKGROUND_COLOR,
            title = stringRes(R.string.qs_footer_active_button_background_color),
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_QS_THEME) }
        )

        colorPicker(
            key = XposedKey.QS_FOOTER_ACTIVE_BUTTON_ICON_COLOR,
            title = stringRes(R.string.qs_footer_active_button_icon_color),
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_QS_THEME) }
        )

        colorPicker(
            key = XposedKey.QS_FOOTER_CHIP_BACKGROUND_COLOR,
            title = stringRes(R.string.qs_footer_chip_background_color),
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_QS_THEME) }
        )

        colorPicker(
            key = XposedKey.QS_FOOTER_CHIP_CONTENT_COLOR,
            title = stringRes(R.string.qs_footer_chip_content_color),
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_QS_THEME) }
        )

        switch(
            key = XposedKey.QS_FOOTER_GRADIENT,
            title = stringRes(R.string.qs_footer_gradient_title),
            summary = { stringRes(R.string.qs_footer_gradient_desc) },
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_QS_THEME) }
        )

        colorPicker(
            key = XposedKey.QS_FOOTER_GRADIENT_END_COLOR,
            title = stringRes(R.string.qs_footer_gradient_end_color),
            isEnabled = {
                it.getBoolean(XposedKey.CUSTOM_QS_THEME) &&
                        it.getBoolean(XposedKey.QS_FOOTER_GRADIENT)
            }
        )
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
private fun QsThemesScreenPreview() {
    PreviewComposable {
        QsThemesScreen()
    }
}