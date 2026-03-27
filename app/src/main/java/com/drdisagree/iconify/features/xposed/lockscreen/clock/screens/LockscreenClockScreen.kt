package com.drdisagree.iconify.features.xposed.lockscreen.clock.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.preferences.PreferenceScreen
import com.drdisagree.iconify.core.preferences.preferenceScreen
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable

val lsClockPreferences = preferenceScreen {
}

@Composable
fun LockscreenClockScreen() {
    PreferenceScreen(
        items = lsClockPreferences,
        title = stringResource(R.string.activity_title_lockscreen_clock),
        showBackIcon = true
    )
}

@Preview(showBackground = true)
@Composable
fun LockscreenClockScreenPreview() {
    PreviewComposable {
        LockscreenClockScreen()
    }
}