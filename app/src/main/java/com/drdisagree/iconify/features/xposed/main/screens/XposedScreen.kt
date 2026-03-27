package com.drdisagree.iconify.features.xposed.main.screens

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drdisagree.iconify.R
import com.drdisagree.iconify.app.navigation.NavRoutes
import com.drdisagree.iconify.core.preferences.PreferenceScreen
import com.drdisagree.iconify.core.preferences.iconRes
import com.drdisagree.iconify.core.preferences.preferenceScreen
import com.drdisagree.iconify.core.preferences.stringRes
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable
import com.drdisagree.iconify.core.utils.AppUtils.launchAppThrowError
import com.drdisagree.iconify.core.utils.AppUtils.openUrl
import com.drdisagree.iconify.data.common.Const.PL_ENHANCED_PACKAGE
import com.drdisagree.iconify.data.common.Const.PL_ENHANCED_URL
import com.drdisagree.iconify.features.xposed.main.components.HookCheckCard

fun xposedPreferences(
    onLauncherClick: () -> Unit = {}
) = preferenceScreen {
    composable(key = "xposedHookCheck") {
        val previewMode = LocalInspectionMode.current

        if (!previewMode) {
            HookCheckCard(modifier = Modifier.padding(bottom = 16.dp))
        }
    }

    category {
        action(
            key = "xposedBackgroundChip",
            icon = iconRes(R.drawable.ic_tweaks_statusbar),
            title = stringRes(R.string.activity_title_statusbar),
            summary = { _, _ -> stringRes(R.string.activity_desc_statusbar) },
            onClick = { _, _, nav ->
                nav.navigate(NavRoutes.Xposed.Statusbar.Root) {
                    launchSingleTop = true
                }
            }
        )

        action(
            key = "xposedQuickSettings",
            icon = iconRes(R.drawable.ic_xposed_quick_settings),
            title = stringRes(R.string.activity_title_quick_settings),
            summary = { _, _ -> stringRes(R.string.activity_desc_quick_settings) },
            onClick = { _, _, nav ->
                nav.navigate(NavRoutes.Xposed.QuickSettings.Root) {
                    launchSingleTop = true
                }
            }
        )

        action(
            key = "xposedLockscreen",
            icon = iconRes(R.drawable.ic_xposed_lockscreen),
            title = stringRes(R.string.activity_title_lockscreen),
            summary = { _, _ -> stringRes(R.string.activity_desc_lockscreen) },
            onClick = { _, _, nav ->
                nav.navigate(NavRoutes.Xposed.Lockscreen.Root) {
                    launchSingleTop = true
                }
            }
        )

        action(
            key = "xposedVolumePanel",
            icon = iconRes(R.drawable.ic_tweaks_volume),
            title = stringRes(R.string.activity_title_volume_panel),
            summary = { _, _ -> stringRes(R.string.activity_desc_volume_panel) },
            onClick = { _, _, nav ->
                nav.navigate(NavRoutes.Xposed.VolumePanel) {
                    launchSingleTop = true
                }
            }
        )

        action(
            key = "xposedLauncher",
            icon = iconRes(R.drawable.ic_launcher),
            title = stringRes(R.string.activity_title_xposed_launcher),
            summary = { _, _ -> stringRes(R.string.activity_desc_xposed_launcher) },
            onClick = { _, _, _ -> onLauncherClick() }
        )
    }
}

@Composable
fun XposedScreen() {
    val context = LocalContext.current
    val activity = LocalActivity.current

    PreferenceScreen(
        items = xposedPreferences(
            onLauncherClick = {
                try {
                    launchAppThrowError(activity!!, PL_ENHANCED_PACKAGE)
                } catch (_: Exception) {
                    openUrl(context, PL_ENHANCED_URL)
                }
            }
        ),
        title = stringResource(R.string.navbar_xposed)
    )
}

@Preview(showBackground = true)
@Composable
fun XposedScreenPreview() {
    PreviewComposable {
        XposedScreen()
    }
}