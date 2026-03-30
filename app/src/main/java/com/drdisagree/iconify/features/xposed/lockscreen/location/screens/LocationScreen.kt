package com.drdisagree.iconify.features.xposed.lockscreen.location.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.preferences.PreferenceScreen
import com.drdisagree.iconify.core.preferences.preferenceScreen
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable

val locationPreferences = preferenceScreen {
}

@Composable
fun LocationScreen() {
    PreferenceScreen(
        items = locationPreferences,
        title = stringResource(R.string.custom_location_title),
        showBackIcon = true
    )
}

@Preview(showBackground = true)
@Composable
fun LocationScreenPreview() {
    PreviewComposable {
        LocationScreen()
    }
}