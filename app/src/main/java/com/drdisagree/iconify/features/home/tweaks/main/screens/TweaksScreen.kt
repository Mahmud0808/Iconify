package com.drdisagree.iconify.features.home.tweaks.main.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.drdisagree.iconify.R
import com.drdisagree.iconify.app.navigation.NavRoutes
import com.drdisagree.iconify.core.preferences.PreferenceScreen
import com.drdisagree.iconify.core.preferences.iconRes
import com.drdisagree.iconify.core.preferences.preferenceScreen
import com.drdisagree.iconify.core.preferences.stringRes
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable

val tweaksPreferences = preferenceScreen {
    category {
        action(
            key = "colorEngine",
            icon = iconRes(R.drawable.ic_tweaks_color),
            title = stringRes(R.string.activity_title_color_engine),
            summary = { stringRes(R.string.activity_desc_color_engine) },
            onClick = {
                it.navController.navigate(NavRoutes.MainGraph.Home.More.ColorEngine) {
                    launchSingleTop = true
                }
            }
        )

        action(
            key = "uiRoundness",
            icon = iconRes(R.drawable.ic_tweaks_roundness),
            title = stringRes(R.string.activity_title_ui_roundness),
            summary = { stringRes(R.string.activity_desc_ui_roundness) },
            onClick = {
                it.navController.navigate(NavRoutes.MainGraph.Home.More.UIRoundness) {
                    launchSingleTop = true
                }
            }
        )

        //        action(
        //            key = "coloredBattery",
        //            icon = iconRes(R.drawable.ic_colored_battery),
        //            title = stringRes(R.string.activity_title_colored_battery),
        //            summary = { stringRes(R.string.activity_desc_colored_battery) },
        //            onClick = { }
        //        )

        //        action(
        //            key = "qsIconLabel",
        //            icon = iconRes(R.drawable.ic_qs_icon_and_label),
        //            title = stringRes(R.string.activity_title_qs_icon_label),
        //            summary = { stringRes(R.string.activity_desc_qs_icon_label) },
        //            onClick = { }
        //        )

        //        action(
        //            key = "qsTileSize",
        //            icon = iconRes(R.drawable.ic_qs_tile_size),
        //            title = stringRes(R.string.activity_title_qs_tile_size),
        //            summary = { stringRes(R.string.activity_desc_qs_tile_size) },
        //            onClick = { }
        //        )

        action(
            key = "statusBarTweaks",
            icon = iconRes(R.drawable.ic_tweaks_statusbar),
            title = stringRes(R.string.activity_title_statusbar),
            summary = { stringRes(R.string.activity_desc_statusbar) },
            onClick = {
                it.navController.navigate(NavRoutes.MainGraph.Home.More.StatusBar) {
                    launchSingleTop = true
                }
            }
        )

        action(
            key = "navBarTweaks",
            icon = iconRes(R.drawable.ic_tweaks_navbar),
            title = stringRes(R.string.activity_title_navigation_bar),
            summary = { stringRes(R.string.activity_desc_navigation_bar) },
            onClick = {
                it.navController.navigate(NavRoutes.MainGraph.Home.More.NavigationBar) {
                    launchSingleTop = true
                }
            }
        )

        action(
            key = "mediaPlayerTweaks",
            title = stringRes(R.string.activity_title_media_player),
            icon = iconRes(R.drawable.ic_tweaks_media),
            summary = { stringRes(R.string.activity_desc_media_player) },
            onClick = {
                it.navController.navigate(NavRoutes.MainGraph.Home.More.MediaPlayer) {
                    launchSingleTop = true
                }
            }
        )

        //        action(
        //            key = "volumePanelTweaks",
        //            icon = iconRes(R.drawable.ic_tweaks_volume),
        //            title = stringRes(R.string.activity_title_volume_panel),
        //            summary = { stringRes(R.string.activity_desc_volume_panel) },
        //            onClick = { }
        //        )

        action(
            key = "miscellaneousTweaks",
            icon = iconRes(R.drawable.ic_tweaks_miscellaneous),
            title = stringRes(R.string.activity_title_miscellaneous),
            summary = { stringRes(R.string.activity_desc_miscellaneous) },
            onClick = {
                it.navController.navigate(NavRoutes.MainGraph.Home.More.Miscellaneous) {
                    launchSingleTop = true
                }
            }
        )
    }
}

@Composable
fun TweaksScreen() {
    PreferenceScreen(
        items = tweaksPreferences,
        title = stringResource(R.string.navbar_tweaks),
        showBackIcon = true
    )
}

@Preview(showBackground = true)
@Composable
private fun TweaksScreenPreview() {
    PreviewComposable {
        TweaksScreen()
    }
}