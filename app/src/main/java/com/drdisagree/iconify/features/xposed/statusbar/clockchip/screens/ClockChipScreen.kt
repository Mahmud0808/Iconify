package com.drdisagree.iconify.features.xposed.statusbar.clockchip.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.preferences.PreferenceScreen
import com.drdisagree.iconify.core.preferences.preferenceScreen
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable

val clockChipPreferences = preferenceScreen {
}

@Composable
fun ClockChipScreen() {
    PreferenceScreen(
        items = clockChipPreferences,
        title = stringResource(R.string.activity_title_background_chip),
        showBackIcon = true
    )
}

@Preview(showBackground = true)
@Composable
fun ClockChipScreenPreview() {
    PreviewComposable {
        ClockChipScreen()
    }
}