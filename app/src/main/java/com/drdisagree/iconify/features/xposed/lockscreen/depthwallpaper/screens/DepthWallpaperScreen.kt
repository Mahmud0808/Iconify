package com.drdisagree.iconify.features.xposed.lockscreen.depthwallpaper.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.preferences.PreferenceScreen
import com.drdisagree.iconify.core.preferences.preferenceScreen
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable

val depthWallpaperPreferences = preferenceScreen {
}

@Composable
fun DepthWallpaperScreen() {
    PreferenceScreen(
        items = depthWallpaperPreferences,
        title = stringResource(R.string.activity_title_depth_wallpaper),
        showBackIcon = true
    )
}

@Preview(showBackground = true)
@Composable
fun DepthWallpaperScreenPreview() {
    PreviewComposable {
        DepthWallpaperScreen()
    }
}