package com.drdisagree.iconify.features.xposed.quicksettings.headerimage.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.preferences.PreferenceScreen
import com.drdisagree.iconify.core.preferences.preferenceScreen
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable

val headerImagePreferences = preferenceScreen {
}

@Composable
fun HeaderImageScreen() {
    PreferenceScreen(
        items = headerImagePreferences,
        title = stringResource(R.string.activity_title_header_image),
        showBackIcon = true
    )
}

@Preview(showBackground = true)
@Composable
fun HeaderImageScreenPreview() {
    PreviewComposable {
        HeaderImageScreen()
    }
}