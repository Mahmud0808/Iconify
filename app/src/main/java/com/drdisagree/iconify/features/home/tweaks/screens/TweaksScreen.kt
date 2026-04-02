package com.drdisagree.iconify.features.home.tweaks.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.preferences.PreferenceScreen
import com.drdisagree.iconify.core.preferences.iconRes
import com.drdisagree.iconify.core.preferences.preferenceScreen
import com.drdisagree.iconify.core.preferences.stringRes
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable
import com.drdisagree.iconify.core.ui.components.others.showComingSoonToast

val tweaksPreferences = preferenceScreen {
    category {
        action(
            key = "colorEngine",
            icon = iconRes(R.drawable.ic_tweaks_color),
            title = stringRes(R.string.activity_title_color_engine),
            summary = { _, _ -> stringRes(R.string.activity_desc_color_engine) },
            onClick = { context, _, _ -> showComingSoonToast(context) }
        )

        action(
            key = "uiRoundness",
            icon = iconRes(R.drawable.ic_tweaks_roundness),
            title = stringRes(R.string.activity_title_ui_roundness),
            summary = { _, _ -> stringRes(R.string.activity_desc_ui_roundness) },
            onClick = { context, _, _ -> showComingSoonToast(context) }
        )

        //        action(
        //            key = "coloredBattery",
        //            icon = iconRes(R.drawable.ic_colored_battery),
        //            title = stringRes(R.string.activity_title_colored_battery),
        //            summary = { _, _ -> stringRes(R.string.activity_desc_colored_battery) },
        //            onClick = { _, _, _ -> }
        //        )

        action(
            key = "qsRowColumn",
            icon = iconRes(R.drawable.ic_qs_row_column),
            title = stringRes(R.string.activity_title_qs_row_column),
            summary = { _, _ -> stringRes(R.string.activity_desc_qs_row_column) },
            onClick = { context, _, _ -> showComingSoonToast(context) }
        )

        //        action(
        //            key = "qsIconLabel",
        //            icon = iconRes(R.drawable.ic_qs_icon_and_label),
        //            title = stringRes(R.string.activity_title_qs_icon_label),
        //            summary = { _, _ -> stringRes(R.string.activity_desc_qs_icon_label) },
        //            onClick = { _, _, _ -> }
        //        )

        //        action(
        //            key = "qsTileSize",
        //            icon = iconRes(R.drawable.ic_qs_tile_size),
        //            title = stringRes(R.string.activity_title_qs_tile_size),
        //            summary = { _, _ -> stringRes(R.string.activity_desc_qs_tile_size) },
        //            onClick = { _, _, _ -> }
        //        )

        action(
            key = "statusBarTweaks",
            icon = iconRes(R.drawable.ic_tweaks_statusbar),
            title = stringRes(R.string.activity_title_statusbar),
            summary = { _, _ -> stringRes(R.string.activity_desc_statusbar) },
            onClick = { context, _, _ -> showComingSoonToast(context) }
        )

        action(
            key = "navBarTweaks",
            icon = iconRes(R.drawable.ic_tweaks_navbar),
            title = stringRes(R.string.activity_title_navigation_bar),
            summary = { _, _ -> stringRes(R.string.activity_desc_navigation_bar) },
            onClick = { context, _, _ -> showComingSoonToast(context) }
        )

        //        action(
        //            key = "mediaPlayerTweaks",
        //            title = stringRes(R.string.activity_title_media_player),
        //            icon = iconRes(R.drawable.ic_tweaks_media),
        //            summary = { _, _ -> stringRes(R.string.activity_desc_media_player) },
        //            onClick = { _, _, _ -> }
        //        )

        //        action(
        //            key = "volumePanelTweaks",
        //            icon = iconRes(R.drawable.ic_tweaks_volume),
        //            title = stringRes(R.string.activity_title_volume_panel),
        //            summary = { _, _ -> stringRes(R.string.activity_desc_volume_panel) },
        //            onClick = { _, _, _ -> }
        //        )

        action(
            key = "miscellaneousTweaks",
            icon = iconRes(R.drawable.ic_tweaks_miscellaneous),
            title = stringRes(R.string.activity_title_miscellaneous),
            summary = { _, _ -> stringRes(R.string.activity_desc_miscellaneous) },
            onClick = { context, _, _ -> showComingSoonToast(context) }
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
fun TweaksScreenPreview() {
    PreviewComposable {
        TweaksScreen()
    }
}