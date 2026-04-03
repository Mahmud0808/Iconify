package com.drdisagree.iconify.features.xposed.statusbar.main.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.drdisagree.iconify.R
import com.drdisagree.iconify.app.navigation.NavRoutes
import com.drdisagree.iconify.core.preferences.PreferenceScreen
import com.drdisagree.iconify.core.preferences.arrayRes
import com.drdisagree.iconify.core.preferences.preferenceScreen
import com.drdisagree.iconify.core.preferences.stringRes
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable
import com.drdisagree.iconify.data.keys.XposedKey

val statusbarPreferences = preferenceScreen {
    category(title = stringRes(R.string.section_title_clock)) {
        twoTargetSwitch(
            key = XposedKey.STATUSBAR_CLOCK_CHIP,
            title = stringRes(R.string.activity_title_background_chip),
            summary = { _, _ -> stringRes(R.string.activity_desc_background_chip) },
            onClick = { _, _, nav ->
                nav.navigate(NavRoutes.Xposed.Statusbar.ClockChip) {
                    launchSingleTop = true
                }
            }
        )

        listPref(
            key = XposedKey.STATUSBAR_CLOCK_POSITION,
            title = stringRes(R.string.sb_clock_position_title),
            entries = arrayRes(R.array.status_bar_clock_position_entries),
            entryValues = arrayRes(R.array.status_bar_clock_position_values),
        )

        switch(
            key = XposedKey.STATUSBAR_CLOCK_CLICKABLE,
            title = stringRes(R.string.statusbar_clock_clickable_title),
            summary = { _, _ -> stringRes(R.string.statusbar_clock_clickable_desc) },
        )

        switch(
            key = XposedKey.STATUSBAR_CLOCK_TEXT_SIZE_SWITCH,
            title = stringRes(R.string.sb_clock_size_switch_title),
            summary = { _, _ -> stringRes(R.string.sb_clock_size_switch_desc) },
        )

        slider(
            key = XposedKey.STATUSBAR_CLOCK_TEXT_SIZE,
            title = stringRes(R.string.sb_clock_size),
            min = 10f,
            max = 24f,
            steps = 13,
            valueLabel = { "${it.toInt()}px" },
            isVisible = { pref -> pref.getBoolean(XposedKey.STATUSBAR_CLOCK_TEXT_SIZE_SWITCH) }
        )

        switch(
            key = XposedKey.HIDE_BATTERY_VIEW,
            title = stringRes(R.string.hide_battery_title),
            summary = { _, _-> stringRes("Hide battery icon from statusbar") },
        )
    }

    category(title = stringRes(R.string.section_title_icons)) {
        twoTargetSwitch(
            key = XposedKey.STATUSBAR_LOGO,
            title = stringRes(R.string.status_bar_logo_title),
            summary = { _, _ -> stringRes(R.string.status_bar_logo_desc) },
            onClick = { _, _, nav ->
                nav.navigate(NavRoutes.Xposed.Statusbar.Logo) {
                    launchSingleTop = true
                }
            }
        )

        switch(
            key = XposedKey.COLORED_STATUSBAR_ICON,
            title = stringRes(R.string.colored_statusbar_icon_title),
            summary = { _, _ -> stringRes(R.string.colored_statusbar_icon_desc) },
        )

        switch(
            key = XposedKey.STATUSBAR_SWAP_WIFI_CELLULAR,
            title = stringRes(R.string.sb_swap_wifi_cellular_title),
            summary = { _, _ -> stringRes(R.string.sb_swap_wifi_cellular_desc) },
        )

        switch(
            key = XposedKey.STATUSBAR_SWAP_CELLULAR_NETWORK_TYPE,
            title = stringRes(R.string.sb_swap_cellular_network_type_title),
            summary = { _, _ -> stringRes(R.string.sb_swap_cellular_network_type_desc) },
        )

        switch(
            key = XposedKey.SHOW_4G_INSTEAD_OF_LTE,
            title = stringRes(R.string.sb_show_4g_icon_title),
            summary = { _, _ -> stringRes(R.string.sb_show_4g_icon_desc) },
        )

        slider(
            key = XposedKey.NOTIFICATION_ICONS_LIMIT,
            title = stringRes(R.string.sb_max_notification_icons_title),
            min = -1f,
            max = 15f,
            steps = 14,
            valueLabel = { "${it.toInt()}" },
            showDefaultIndicator = true,
            hideDefaultValue = true,
            showResetButton = true
        )
    }

    category(title = stringRes(R.string.section_title_misc)) {
        twoTargetSwitch(
            key = XposedKey.DUAL_STATUSBAR,
            title = stringRes(R.string.dual_status_bar_title),
            summary = { _, _ -> stringRes(R.string.dual_status_bar_desc) },
            onClick = { _, _, nav ->
                nav.navigate(NavRoutes.Xposed.Statusbar.DualStatusbar) {
                    launchSingleTop = true
                }
            }
        )

        switch(
            key = XposedKey.ONGOING_ACTION_CHIP,
            title = stringRes(R.string.ongoing_action_chip_title),
            summary = { _, _ -> stringRes(R.string.ongoing_action_chip_desc) },
        )
    }
}

@Composable
fun StatusbarScreen() {
    PreferenceScreen(
        items = statusbarPreferences,
        title = stringResource(R.string.activity_title_statusbar),
        showBackIcon = true
    )
}

@Preview(showBackground = true)
@Composable
fun StatusbarScreenPreview() {
    PreviewComposable {
        StatusbarScreen()
    }
}