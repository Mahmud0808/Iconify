package com.drdisagree.iconify.features.settings.appupdates.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.preferences.PreferenceScreen
import com.drdisagree.iconify.core.preferences.arrayRes
import com.drdisagree.iconify.core.preferences.iconRes
import com.drdisagree.iconify.core.preferences.preferenceScreen
import com.drdisagree.iconify.core.preferences.stringRes
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable
import com.drdisagree.iconify.data.keys.SettingsKey
import com.drdisagree.iconify.features.settings.appupdates.components.AppUpdateContent

val appUpdatesPreferences = preferenceScreen {
    category(title = stringRes(R.string.section_title_schedule)) {
        switch(
            key = SettingsKey.UPDATE_OVER_WIFI,
            icon = iconRes(R.drawable.ic_auto_update_wifi_only),
            title = stringRes(R.string.settings_auto_update_title_wifi_only),
            summary = { stringRes(R.string.settings_auto_update_desc_wifi_only) },
        )

        listPref(
            key = SettingsKey.UPDATE_SCHEDULE,
            icon = iconRes(Icons.Rounded.Schedule),
            title = stringRes(R.string.update_schedule_title),
            entries = arrayRes(R.array.update_schedule_entries),
            entryValues = arrayRes(R.array.update_schedule_values),
            summary = {
                val valStr = it.prefController.getString(it.key)
                when (valStr) {
                    "0" -> stringRes(R.string.update_schedule1)
                    "1" -> stringRes(R.string.update_schedule2)
                    "2" -> stringRes(R.string.update_schedule3)
                    else -> stringRes(R.string.update_schedule4)
                }
            }
        )
    }

    composable("update_content") {
        AppUpdateContent()
    }
}

@Composable
fun AppUpdatesScreen() {
    PreferenceScreen(
        items = appUpdatesPreferences,
        title = stringResource(R.string.settings_app_update_checker_title),
        showBackIcon = true
    )
}

@Preview(showBackground = true)
@Composable
private fun AppUpdatesScreenPreview() {
    PreviewComposable {
        AppUpdatesScreen()
    }
}
