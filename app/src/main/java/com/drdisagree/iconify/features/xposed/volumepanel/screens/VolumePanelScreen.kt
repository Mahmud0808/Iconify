package com.drdisagree.iconify.features.xposed.volumepanel.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.preferences.PreferenceScreen
import com.drdisagree.iconify.core.preferences.preferenceScreen
import com.drdisagree.iconify.core.preferences.stringRes
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable
import com.drdisagree.iconify.data.keys.XposedKey

val volumePanelPreferences = preferenceScreen {
    category {
        switch(
            key = XposedKey.VOLUME_PANEL_PERCENTAGE,
            title = stringRes(R.string.volume_percentage_title),
            summary = { _, _ -> stringRes(R.string.volume_percentage_desc) },
        )

        switch(
            key = XposedKey.VOLUME_PANEL_SAFETY_WARNING,
            title = stringRes(R.string.safety_warning_title),
            summary = { _, _ -> stringRes(R.string.safety_warning_desc) },
        )
    }
}

@Composable
fun VolumePanelScreen() {
    PreferenceScreen(
        items = volumePanelPreferences,
        title = stringResource(R.string.activity_title_volume_panel),
        showBackIcon = true
    )
}

@Preview(showBackground = true)
@Composable
fun VolumePanelScreenPreview() {
    PreviewComposable {
        VolumePanelScreen()
    }
}