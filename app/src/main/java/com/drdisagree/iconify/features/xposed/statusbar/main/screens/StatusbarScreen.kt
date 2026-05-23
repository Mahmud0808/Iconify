package com.drdisagree.iconify.features.xposed.statusbar.main.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.drdisagree.iconify.R
import com.drdisagree.iconify.app.navigation.NavRoutes
import com.drdisagree.iconify.core.preferences.PrefValue
import com.drdisagree.iconify.core.preferences.PreferenceListener
import com.drdisagree.iconify.core.preferences.PreferenceScreen
import com.drdisagree.iconify.core.preferences.arrayRes
import com.drdisagree.iconify.core.preferences.iconRes
import com.drdisagree.iconify.core.preferences.preferenceScreen
import com.drdisagree.iconify.core.preferences.stringRes
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.features.common.viewmodels.SystemActionViewModel
import kotlin.math.roundToInt

val xposedStatusbarPreferences = preferenceScreen {
    category {
        action(
            key = "battery_style",
            icon = iconRes(R.drawable.ic_colored_battery),
            title = stringRes(R.string.activity_title_battery_style),
            summary = { stringRes(R.string.activity_desc_battery_style) },
            onClick = {
                it.navController.navigate(NavRoutes.MainGraph.Xposed.Statusbar.BatteryStyle) {
                    launchSingleTop = true
                }
            }
        )
    }

    category(title = stringRes(R.string.section_title_clock)) {
        twoTargetSwitch(
            key = XposedKey.STATUSBAR_CLOCK_CHIP,
            title = stringRes(R.string.activity_title_background_chip),
            summary = { stringRes(R.string.activity_desc_background_chip) },
            onClick = {
                it.navController.navigate(NavRoutes.MainGraph.Xposed.Statusbar.ClockChip) {
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
            summary = { stringRes(R.string.statusbar_clock_clickable_desc) },
        )

        switch(
            key = XposedKey.STATUSBAR_CLOCK_TEXT_SIZE_SWITCH,
            title = stringRes(R.string.sb_clock_size_switch_title),
            summary = { stringRes(R.string.sb_clock_size_switch_desc) },
        )

        slider(
            key = XposedKey.STATUSBAR_CLOCK_TEXT_SIZE,
            title = stringRes(R.string.sb_clock_size),
            min = 10f,
            max = 24f,
            steps = 13,
            valueLabel = { "${it.roundToInt()}px" },
            isVisible = { pref -> pref.getBoolean(XposedKey.STATUSBAR_CLOCK_TEXT_SIZE_SWITCH) }
        )
    }

    category(title = stringRes(R.string.section_title_icons)) {
        twoTargetSwitch(
            key = XposedKey.STATUSBAR_LOGO,
            title = stringRes(R.string.status_bar_logo_title),
            summary = { stringRes(R.string.status_bar_logo_desc) },
            onClick = {
                it.navController.navigate(NavRoutes.MainGraph.Xposed.Statusbar.Logo) {
                    launchSingleTop = true
                }
            }
        )

        switch(
            key = XposedKey.COLORED_STATUSBAR_ICON,
            title = stringRes(R.string.colored_statusbar_icon_title),
            summary = { stringRes(R.string.colored_statusbar_icon_desc) },
        )

        switch(
            key = XposedKey.STATUSBAR_SWAP_WIFI_CELLULAR,
            title = stringRes(R.string.sb_swap_wifi_cellular_title),
            summary = { stringRes(R.string.sb_swap_wifi_cellular_desc) },
        )

        switch(
            key = XposedKey.STATUSBAR_SWAP_CELLULAR_NETWORK_TYPE,
            title = stringRes(R.string.sb_swap_cellular_network_type_title),
            summary = { stringRes(R.string.sb_swap_cellular_network_type_desc) },
        )

        switch(
            key = XposedKey.SHOW_4G_INSTEAD_OF_LTE,
            title = stringRes(R.string.sb_show_4g_icon_title),
            summary = { stringRes(R.string.sb_show_4g_icon_desc) },
        )

        slider(
            key = XposedKey.NOTIFICATION_ICONS_LIMIT,
            title = stringRes(R.string.sb_max_notification_icons_title),
            min = -1f,
            max = 15f,
            steps = 14,
            valueLabel = { "${it.roundToInt()}" },
            showDefaultIndicator = true,
            hideDefaultValue = true,
            showResetButton = true
        )
    }

    category(title = stringRes(R.string.section_title_misc)) {
        twoTargetSwitch(
            key = XposedKey.DUAL_STATUSBAR,
            title = stringRes(R.string.dual_status_bar_title),
            summary = { stringRes(R.string.dual_status_bar_desc) },
            onClick = {
                it.navController.navigate(NavRoutes.MainGraph.Xposed.Statusbar.DualStatusbar) {
                    launchSingleTop = true
                }
            }
        )

        switch(
            key = XposedKey.ONGOING_ACTION_CHIP,
            title = stringRes(R.string.ongoing_action_chip_title),
            summary = { stringRes(R.string.ongoing_action_chip_desc) },
        )
    }

    category(title = stringRes(R.string.section_title_others)) {
        switch(
            key = XposedKey.STATUSBAR_LINK_TO_CUSTOM_COLOR,
            title = stringRes(R.string.link_to_custom_color_title),
            summary = { stringRes(R.string.link_to_custom_color_desc) },
        )
    }
}

@Composable
fun XposedStatusbarScreen(
    systemActionViewModel: SystemActionViewModel? = hiltViewModel(),
) {
    PreferenceListener { event ->
        when (event.key) {
            XposedKey.STATUSBAR_CLOCK_CHIP.name -> {
                val newValue = (event.newValue as PrefValue.BoolValue).v
                if (!newValue) {
                    systemActionViewModel?.shouldRestartSystemUI()
                }
            }

            XposedKey.STATUSBAR_CLOCK_POSITION.name,
            XposedKey.STATUSBAR_CLOCK_CLICKABLE.name,
            XposedKey.STATUSBAR_CLOCK_TEXT_SIZE_SWITCH.name,
            XposedKey.STATUSBAR_CLOCK_TEXT_SIZE.name,
            XposedKey.STATUSBAR_LOGO.name,
            XposedKey.COLORED_STATUSBAR_ICON.name,
            XposedKey.STATUSBAR_SWAP_WIFI_CELLULAR.name,
            XposedKey.STATUSBAR_SWAP_CELLULAR_NETWORK_TYPE.name,
            XposedKey.SHOW_4G_INSTEAD_OF_LTE.name,
            XposedKey.NOTIFICATION_ICONS_LIMIT.name,
            XposedKey.DUAL_STATUSBAR.name,
            XposedKey.ONGOING_ACTION_CHIP.name,
            XposedKey.STATUSBAR_LINK_TO_CUSTOM_COLOR.name -> {
                systemActionViewModel?.shouldRestartSystemUI()
            }
        }
    }

    PreferenceScreen(
        items = xposedStatusbarPreferences,
        title = stringResource(R.string.activity_title_statusbar),
        showBackIcon = true
    )
}

@Preview(showBackground = true)
@Composable
private fun XposedStatusbarScreenPreview() {
    PreviewComposable {
        XposedStatusbarScreen(null)
    }
}