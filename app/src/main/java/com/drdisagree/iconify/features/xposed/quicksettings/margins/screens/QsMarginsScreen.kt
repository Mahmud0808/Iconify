package com.drdisagree.iconify.features.xposed.quicksettings.margins.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.preferences.PreferenceScreen
import com.drdisagree.iconify.core.preferences.preferenceScreen
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable

val qsMarginsPreferences = preferenceScreen {
}

@Composable
fun QsMarginsScreen() {
    PreferenceScreen(
        items = qsMarginsPreferences,
        title = stringResource(R.string.activity_title_qs_panel_margin),
        showBackIcon = true
    )
}

@Preview(showBackground = true)
@Composable
fun QsMarginsScreenPreview() {
    PreviewComposable {
        QsMarginsScreen()
    }
}