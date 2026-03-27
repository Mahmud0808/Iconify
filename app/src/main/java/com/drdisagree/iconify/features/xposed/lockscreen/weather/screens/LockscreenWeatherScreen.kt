package com.drdisagree.iconify.features.xposed.lockscreen.weather.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.preferences.PreferenceScreen
import com.drdisagree.iconify.core.preferences.preferenceScreen
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable

val lsWeatherPreferences = preferenceScreen {
}

@Composable
fun LockscreenWeatherScreen() {
    PreferenceScreen(
        items = lsWeatherPreferences,
        title = stringResource(R.string.activity_title_lockscreen_weather),
        showBackIcon = true
    )
}

@Preview(showBackground = true)
@Composable
fun LockscreenWeatherScreenPreview() {
    PreviewComposable {
        LockscreenWeatherScreen()
    }
}