package com.drdisagree.iconify.features.xposed.quicksettings.main.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.drdisagree.iconify.R
import com.drdisagree.iconify.app.navigation.NavRoutes
import com.drdisagree.iconify.core.preferences.PreferenceListener
import com.drdisagree.iconify.core.preferences.PreferenceScreen
import com.drdisagree.iconify.core.preferences.iconRes
import com.drdisagree.iconify.core.preferences.preferenceScreen
import com.drdisagree.iconify.core.preferences.stringRes
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.features.common.viewmodels.SystemActionViewModel
import kotlin.math.roundToInt

val quickSettingsPreferences = preferenceScreen {
    category {
        action(
            key = "xposedTransparencyBlur",
            icon = iconRes(R.drawable.ic_xposed_transparency_blur),
            title = stringRes(R.string.activity_title_transparency_blur),
            summary = { stringRes(R.string.activity_desc_transparency_blur) },
            onClick = {
                it.navController.navigate(NavRoutes.MainGraph.Xposed.QuickSettings.Transparency) {
                    launchSingleTop = true
                }
            }
        )

        action(
            key = "xposedHeaderImage",
            icon = iconRes(R.drawable.ic_xposed_header_image),
            title = stringRes(R.string.activity_title_header_image),
            summary = { stringRes(R.string.activity_desc_header_image) },
            onClick = {
                it.navController.navigate(NavRoutes.MainGraph.Xposed.QuickSettings.HeaderImage) {
                    launchSingleTop = true
                }
            }
        )

        action(
            key = "xposedHeaderClock",
            icon = iconRes(R.drawable.ic_xposed_clock),
            title = stringRes(R.string.activity_title_header_clock),
            summary = { stringRes(R.string.activity_desc_header_clock) },
            onClick = {
                it.navController.navigate(NavRoutes.MainGraph.Xposed.QuickSettings.Clock) {
                    launchSingleTop = true
                }
            }
        )

        action(
            key = "xposedQsGrid",
            icon = iconRes(R.drawable.ic_qs_row_column),
            title = stringRes(R.string.activity_title_qs_row_column),
            summary = { stringRes(R.string.activity_desc_qs_row_column) },
            onClick = {
                it.navController.navigate(NavRoutes.MainGraph.Xposed.QuickSettings.Grid) {
                    launchSingleTop = true
                }
            }
        )

        action(
            key = "xposedThemes",
            icon = iconRes(R.drawable.ic_xposed_themes),
            title = stringRes(R.string.activity_title_themes),
            summary = { stringRes(R.string.activity_desc_themes) },
            onClick = {
                it.navController.navigate(NavRoutes.MainGraph.Xposed.QuickSettings.Themes) {
                    launchSingleTop = true
                }
            }
        )
    }

    category(title = stringRes(R.string.section_title_qs_margin)) {
        twoTargetSwitch(
            key = XposedKey.CUSTOM_QS_MARGINS,
            title = stringRes(R.string.custom_qs_margin_title),
            summary = { stringRes(R.string.custom_qs_margin_desc) },
            onClick = {
                it.navController.navigate(NavRoutes.MainGraph.Xposed.QuickSettings.Margins) {
                    launchSingleTop = true
                }
            }
        )
    }

    category(title = stringRes(R.string.section_title_notifications)) {
        switch(
            key = XposedKey.COLORED_NOTIFICATION_ICON,
            title = stringRes(R.string.colored_qs_notification_title),
            summary = { stringRes(R.string.colored_qs_notification_desc) },
        )

        switch(
            key = XposedKey.COLORED_NOTIFICATION_VIEW,
            title = stringRes(R.string.colorize_notification_view_title),
            summary = { stringRes(R.string.colorize_notification_view_desc) },
        )

        switch(
            key = XposedKey.COLORED_NOTIFICATION_VIEW_ALTERNATIVE,
            title = stringRes(R.string.colorize_notification_alternative_color_generation_title),
            summary = { stringRes(R.string.colorize_notification_alternative_color_generation_desc) },
            isVisible = { pref -> pref.getBoolean(XposedKey.COLORED_NOTIFICATION_VIEW) }
        )
    }

    category(title = stringRes(R.string.section_headsup_title)) {
        switch(
            key = XposedKey.NOTIFICATION_HEADS_UP_BLUR,
            title = stringRes(R.string.notification_headsup_blur_title),
            summary = { stringRes(R.string.notification_headsup_blur_desc) },
        )

        slider(
            key = XposedKey.NOTIFICATION_HEADS_UP_BLUR_RADIUS,
            title = stringRes(R.string.notification_headsup_blur_intensity_title),
            min = 0f,
            max = 100f,
            valueLabel = { "${it.roundToInt()}%" },
            isVisible = { pref -> pref.getBoolean(XposedKey.NOTIFICATION_HEADS_UP_BLUR) }
        )

        slider(
            key = XposedKey.NOTIFICATION_HEADS_UP_TRANSPARENCY,
            title = stringRes(R.string.notification_headsup_blur_transparency_title),
            min = 0f,
            max = 100f,
            valueLabel = { "${it.roundToInt()}%" },
            isVisible = { pref -> pref.getBoolean(XposedKey.NOTIFICATION_HEADS_UP_BLUR) }
        )
    }

    category(title = stringRes(R.string.section_title_media_player)) {
        switch(
            key = XposedKey.COMPACT_MEDIA_PLAYER,
            title = stringRes(R.string.compact_media_player_title),
            summary = { stringRes(R.string.compact_media_player_desc) },
        )

        switch(
            key = XposedKey.BLUR_MEDIA_PLAYER_ARTWORK,
            title = stringRes(R.string.media_player_artwork_blur_title),
            summary = { stringRes(R.string.media_player_artwork_blur_desc) },
        )

        slider(
            key = XposedKey.BLUR_MEDIA_PLAYER_ARTWORK_RADIUS,
            title = stringRes(R.string.media_player_artwork_blur_radius_title),
            min = 0f,
            max = 100f,
            valueLabel = { "${it.roundToInt()}%" },
            isVisible = { pref -> pref.getBoolean(XposedKey.BLUR_MEDIA_PLAYER_ARTWORK) }
        )
    }

    category(title = stringRes(R.string.section_title_qs_elements)) {
        switch(
            key = XposedKey.HIDE_QS_SILENT_TEXT,
            title = stringRes(R.string.hide_qs_silent_text_title),
            summary = { stringRes(R.string.hide_qs_silent_text_desc) },
        )

        switch(
            key = XposedKey.HIDE_QS_FOOTER_BUTTONS,
            title = stringRes(R.string.hide_qs_footer_buttons_title),
            summary = { stringRes(R.string.hide_qs_footer_buttons_desc) },
        )

        switch(
            key = XposedKey.QS_PANEL_HIDE_CARRIER,
            title = stringRes(R.string.hide_carrier_group_title),
            summary = { stringRes(R.string.hide_carrier_group_desc) },
        )

        switch(
            key = XposedKey.HIDE_STATUS_ICONS,
            title = stringRes(R.string.hide_status_icons_title),
            summary = { stringRes(R.string.hide_status_icons_desc) },
        )
    }

    category(title = stringRes(R.string.section_title_others)) {
        switch(
            key = XposedKey.FIX_NOTIFICATION_COLOR,
            title = stringRes(R.string.fix_notification_color_title),
            summary = { stringRes(R.string.fix_notification_color_desc) },
        )

        switch(
            key = XposedKey.FIX_NOTIFICATION_FOOTER_BUTTON_COLOR,
            title = stringRes(R.string.fix_notification_footer_button_color_title),
            summary = { stringRes(R.string.fix_notification_footer_button_color_desc) },
        )

        switch(
            key = XposedKey.FIX_NOTIFICATION_EXPAND_BUTTON_COLOR,
            title = stringRes(R.string.fix_notification_expand_button_color_title),
            summary = { stringRes(R.string.fix_notification_expand_button_color_desc) },
        )
    }
}

@Composable
fun QuickSettingsScreen(
    systemActionViewModel: SystemActionViewModel? = hiltViewModel(),
) {
    PreferenceListener(key = null) { event ->
        when (event.key) {
            XposedKey.CUSTOM_QS_MARGINS.name,
            XposedKey.COLORED_NOTIFICATION_ICON.name,
            XposedKey.COLORED_NOTIFICATION_VIEW.name,
            XposedKey.NOTIFICATION_HEADS_UP_BLUR.name,
            XposedKey.COMPACT_MEDIA_PLAYER.name,
            XposedKey.BLUR_MEDIA_PLAYER_ARTWORK.name,
            XposedKey.HIDE_QS_SILENT_TEXT.name,
            XposedKey.HIDE_QS_FOOTER_BUTTONS.name,
            XposedKey.QS_PANEL_HIDE_CARRIER.name,
            XposedKey.HIDE_STATUS_ICONS.name,
            XposedKey.FIX_NOTIFICATION_COLOR.name,
            XposedKey.FIX_NOTIFICATION_FOOTER_BUTTON_COLOR.name,
            XposedKey.FIX_NOTIFICATION_EXPAND_BUTTON_COLOR.name -> {
                systemActionViewModel?.shouldRestartSystemUI()
            }
        }
    }

    PreferenceScreen(
        items = quickSettingsPreferences,
        title = stringResource(R.string.activity_title_quick_settings),
        showBackIcon = true
    )
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    PreviewComposable {
        QuickSettingsScreen(null)
    }
}