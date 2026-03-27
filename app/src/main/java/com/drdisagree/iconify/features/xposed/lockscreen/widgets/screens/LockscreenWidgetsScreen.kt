package com.drdisagree.iconify.features.xposed.lockscreen.widgets.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.preferences.PreferenceScreen
import com.drdisagree.iconify.core.preferences.preferenceScreen
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable

val lsWidgetsPreferences = preferenceScreen {
}

@Composable
fun LockscreenWidgetsScreen() {
    PreferenceScreen(
        items = lsWidgetsPreferences,
        title = stringResource(R.string.activity_title_lockscreen_widget),
        showBackIcon = true
    )
}

@Preview(showBackground = true)
@Composable
fun LockscreenWidgetsScreenPreview() {
    PreviewComposable {
        LockscreenWidgetsScreen()
    }
}