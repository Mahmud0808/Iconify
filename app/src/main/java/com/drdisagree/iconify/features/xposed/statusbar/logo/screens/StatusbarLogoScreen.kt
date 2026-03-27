package com.drdisagree.iconify.features.xposed.statusbar.logo.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.preferences.PreferenceScreen
import com.drdisagree.iconify.core.preferences.preferenceScreen
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable

val statusbarLogoPreferences = preferenceScreen {
}

@Composable
fun StatusbarLogoScreen() {
    PreferenceScreen(
        items = statusbarLogoPreferences,
        title = stringResource(R.string.status_bar_logo_title),
        showBackIcon = true
    )
}

@Preview(showBackground = true)
@Composable
fun StatusbarLogoScreenPreview() {
    PreviewComposable {
        StatusbarLogoScreen()
    }
}