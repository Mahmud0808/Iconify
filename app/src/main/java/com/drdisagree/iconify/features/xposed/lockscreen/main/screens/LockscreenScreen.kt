package com.drdisagree.iconify.features.xposed.lockscreen.main.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.drdisagree.iconify.R
import com.drdisagree.iconify.app.navigation.NavRoutes
import com.drdisagree.iconify.core.preferences.PreferenceListener
import com.drdisagree.iconify.core.preferences.PreferenceScreen
import com.drdisagree.iconify.core.preferences.iconRes
import com.drdisagree.iconify.core.preferences.preferenceScreen
import com.drdisagree.iconify.core.preferences.stringRes
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.features.common.viewmodels.SystemActionViewModel
import kotlin.math.roundToInt

val lockscreenPreferences = preferenceScreen {
    category {
        action(
            key = "xposedLockscreenClock",
            icon = iconRes(R.drawable.ic_xposed_clock),
            title = stringRes(R.string.activity_title_lockscreen_clock),
            summary = { stringRes(R.string.activity_desc_lockscreen_clock) },
            onClick = {
                it.navController.navigate(NavRoutes.MainGraph.Xposed.Lockscreen.Clock) {
                    launchSingleTop = true
                }
            }
        )

        action(
            key = "xposedLockscreenWeather",
            icon = iconRes(R.drawable.ic_xposed_lockscreen_weather),
            title = stringRes(R.string.activity_title_lockscreen_weather),
            summary = { stringRes(R.string.activity_desc_lockscreen_weather) },
            onClick = {
                it.navController.navigate(NavRoutes.MainGraph.Xposed.Lockscreen.Weather) {
                    launchSingleTop = true
                }
            }
        )

        action(
            key = "xposedLockscreenWidgets",
            icon = iconRes(R.drawable.ic_xposed_lockscreen_widgets),
            title = stringRes(R.string.activity_title_lockscreen_widget),
            summary = { stringRes(R.string.activity_desc_lockscreen_widget) },
            onClick = {
                it.navController.navigate(NavRoutes.MainGraph.Xposed.Lockscreen.Widgets.Root) {
                    launchSingleTop = true
                }
            }
        )

        action(
            key = "xposedDepthWallpaper",
            icon = iconRes(R.drawable.ic_xposed_depth_wallpaper),
            title = stringRes(R.string.activity_title_depth_wallpaper),
            summary = { stringRes(R.string.activity_desc_depth_wallpaper) },
            onClick = {
                it.navController.navigate(NavRoutes.MainGraph.Xposed.Lockscreen.DepthWallpaper) {
                    launchSingleTop = true
                }
            }
        )

        action(
            key = "xposedAlbumArt",
            icon = iconRes(R.drawable.ic_xposed_album_art),
            title = stringRes(R.string.activity_title_lockscreen_album_art),
            summary = { stringRes(R.string.media_art_summary) },
            onClick = {
                it.navController.navigate(NavRoutes.MainGraph.Xposed.Lockscreen.MediaAlbumArt) {
                    launchSingleTop = true
                }
            }
        )

        action(
            key = "xposedLockscreenVisualizer",
            icon = iconRes(R.drawable.ic_volume_eq),
            title = stringRes(R.string.activity_title_lockscreen_visualizer),
            summary = { stringRes(R.string.activity_desc_lockscreen_visualizer) },
            onClick = {
                it.navController.navigate(NavRoutes.MainGraph.Xposed.Lockscreen.Visualizer) {
                    launchSingleTop = true
                }
            }
        )
    }

    category(title = stringRes(R.string.settings_section_title_general)) {
        switch(
            key = XposedKey.LOCKSCREEN_WALLPAPER_BLUR,
            title = stringRes(R.string.ls_wallpaper_blur_title),
            summary = { stringRes(R.string.ls_wallpaper_blur_desc) },
        )

        slider(
            key = XposedKey.LOCKSCREEN_WALLPAPER_BLUR_RADIUS,
            title = stringRes(R.string.ls_wallpaper_blur_radius_title),
            min = 0f,
            max = 100f,
            valueLabel = { "${it.roundToInt()}%" },
            isVisible = { pref -> pref.getBoolean(XposedKey.LOCKSCREEN_WALLPAPER_BLUR) }
        )

        switch(
            key = XposedKey.HIDE_LOCKSCREEN_LOCK_ICON,
            title = stringRes(R.string.hide_ls_lock_icon_title),
            summary = { stringRes(R.string.hide_ls_lock_icon_desc) },
        )

        switch(
            key = XposedKey.HIDE_LOCKSCREEN_CARRIER,
            title = stringRes(R.string.hide_ls_carrier_title),
            summary = { stringRes(R.string.hide_ls_carrier_desc) },
        )

        switch(
            key = XposedKey.HIDE_LOCKSCREEN_STATUSBAR,
            title = stringRes(R.string.hide_ls_statusbar_title),
            summary = { stringRes(R.string.hide_ls_statusbar_desc) },
        )

    }
}

@Composable
fun LockscreenScreen(
    systemActionViewModel: SystemActionViewModel? = hiltViewModel(),
) {
    PreferenceListener { event ->
        when (event.key) {
            XposedKey.LOCKSCREEN_WALLPAPER_BLUR.name,
            XposedKey.HIDE_LOCKSCREEN_LOCK_ICON.name,
            XposedKey.HIDE_LOCKSCREEN_CARRIER.name,
            XposedKey.HIDE_LOCKSCREEN_STATUSBAR.name -> {
                systemActionViewModel?.shouldRestartSystemUI()
            }
        }
    }

    PreferenceScreen(
        items = lockscreenPreferences,
        title = stringResource(R.string.activity_title_lockscreen),
        showBackIcon = true
    )
}

@Preview(showBackground = true)
@Composable
private fun LockscreenScreenPreview() {
    PreviewComposable {
        LockscreenScreen(null)
    }
}
