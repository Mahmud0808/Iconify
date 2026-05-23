package com.drdisagree.iconify.features.settings.main.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.R
import com.drdisagree.iconify.app.navigation.NavRoutes
import com.drdisagree.iconify.core.common.LocalPreferenceController
import com.drdisagree.iconify.core.preferences.PrefValue
import com.drdisagree.iconify.core.preferences.PreferenceListener
import com.drdisagree.iconify.core.preferences.PreferenceScreen
import com.drdisagree.iconify.core.preferences.iconRes
import com.drdisagree.iconify.core.preferences.preferenceScreen
import com.drdisagree.iconify.core.preferences.stringRes
import com.drdisagree.iconify.core.ui.components.dialogs.LoadingDialog
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable
import com.drdisagree.iconify.core.ui.components.others.withHaptic
import com.drdisagree.iconify.core.utils.AppUtils
import com.drdisagree.iconify.core.utils.AppUtils.openUrl
import com.drdisagree.iconify.core.utils.CacheUtils
import com.drdisagree.iconify.core.utils.SystemUtils
import com.drdisagree.iconify.core.utils.SystemUtils.disableBlur
import com.drdisagree.iconify.core.utils.weather.WeatherConfig
import com.drdisagree.iconify.data.common.Const.GITHUB_REPO
import com.drdisagree.iconify.data.common.Const.ICONIFY_CROWDIN
import com.drdisagree.iconify.data.common.Const.TELEGRAM_GROUP
import com.drdisagree.iconify.data.common.Resources.MODULE_DIR
import com.drdisagree.iconify.data.keys.SettingsKey
import com.drdisagree.iconify.features.common.viewmodels.DynamicResourceViewModel
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun settingsPreferences(
    onDisableEverything: () -> Unit = {},
) = preferenceScreen {
    category(title = stringRes(R.string.section_title_appearance)) {
        action(
            key = "look_and_feel",
            icon = iconRes(R.drawable.ic_app_icon),
            title = stringRes(R.string.look_and_feel_title),
            summary = { stringRes(R.string.look_and_feel_desc) },
            onClick = {
                it.navController.navigate(NavRoutes.MainGraph.Settings.LookAndFeel) {
                    launchSingleTop = true
                }
            }
        )

        action(
            key = "app_language",
            icon = iconRes(R.drawable.ic_language),
            title = stringRes(R.string.settings_app_language),
            summary = { stringRes(R.string.settings_app_language_desc) },
            onClick = {
                it.context.startActivity(
                    Intent(Settings.ACTION_APP_LOCALE_SETTINGS).apply {
                        data = Uri.fromParts("package", it.context.packageName, null)
                    }
                )
            }
        )

        switch(
            key = SettingsKey.HAPTICS_AND_VIBRATION,
            icon = iconRes(R.drawable.ic_vibrate),
            title = stringRes(R.string.settings_vibrate_ui_card_title),
            summary = { stringRes(R.string.settings_vibrate_ui_page_card_desc) },
            isEnabled = { ctrl -> ctrl.getBoolean("notifications_enabled", true) },
        )
    }

    category(title = stringRes(R.string.settings_section_title_miscellaneous)) {
        switch(
            key = SettingsKey.RESTART_SYSTEMUI_AFTER_BOOT,
            icon = iconRes(R.drawable.ic_restart_systemui_after_boot),
            title = stringRes(R.string.settings_restart_systemui_after_boot_title),
            summary = { stringRes(R.string.settings_restart_systemui_after_boot_desc) },
        )

        action(
            key = "clearAppCache",
            icon = iconRes(R.drawable.ic_clear_cache),
            title = stringRes(R.string.settings_clear_app_cache_title),
            summary = { stringRes(R.string.settings_clear_app_cache_desc) },
            onClick = {
                CacheUtils.clearCache(it.context)

                Toast.makeText(
                    it.context,
                    it.context.getString(R.string.toast_clear_cache),
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        action(
            key = "disableEverything",
            icon = iconRes(R.drawable.ic_disable_everything),
            title = stringRes(R.string.settings_disable_everything_title),
            summary = { stringRes(R.string.settings_disable_everything_desc) },
            onClick = { onDisableEverything() }
        )
    }

    category(title = stringRes(R.string.settings_section_title_updates)) {
        action(
            key = "app_update_checker",
            icon = iconRes(R.drawable.ic_check_update),
            title = stringRes(R.string.settings_check_for_update),
            summary = { stringRes(BuildConfig.VERSION_NAME.removePrefix("v")) },
            onClick = {
                it.navController.navigate(NavRoutes.MainGraph.Settings.AppUpdates) {
                    launchSingleTop = true
                }
            }
        )

        switch(
            key = SettingsKey.AUTO_UPDATE,
            icon = iconRes(R.drawable.ic_auto_update),
            title = stringRes(R.string.settings_auto_update_title),
            summary = { stringRes(R.string.settings_auto_update_desc) },
        )
    }

    category(title = stringRes(R.string.settings_section_title_about)) {
        action(
            key = "iconifyGitHub",
            icon = iconRes(R.drawable.ic_github),
            title = stringRes(R.string.settings_github_repository_title),
            summary = { stringRes(R.string.settings_github_repository_desc) },
            onClick = { openUrl(it.context, GITHUB_REPO) }
        )

        action(
            key = "iconifyTelegram",
            icon = iconRes(R.drawable.ic_telegram),
            title = stringRes(R.string.settings_telegram_group_title),
            summary = { stringRes(R.string.settings_telegram_group_desc) },
            onClick = { openUrl(it.context, TELEGRAM_GROUP) }
        )

        action(
            key = "iconifyTranslate",
            icon = iconRes(R.drawable.ic_translate),
            title = stringRes(R.string.settings_translate_title),
            summary = { stringRes(R.string.settings_translate_desc) },
            onClick = { openUrl(it.context, ICONIFY_CROWDIN) }
        )

        action(
            key = "iconifyCredits",
            icon = iconRes(R.drawable.ic_credits),
            title = stringRes(R.string.settings_credits_title),
            summary = { stringRes(R.string.settings_credits_desc) },
            onClick = {
                it.navController.navigate(NavRoutes.MainGraph.Settings.Credits) {
                    launchSingleTop = true
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    dynamicResourceViewModel: DynamicResourceViewModel? = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val prefController = LocalPreferenceController.current
    val coroutineScope = rememberCoroutineScope()
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var showLoading by rememberSaveable { mutableStateOf(false) }

    if (showLoading) {
        LoadingDialog()
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.import_settings_confirmation_title)
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.import_settings_confirmation_desc)
                )
            },
            dismissButton = {
                OutlinedButton(
                    shapes = ButtonDefaults.shapes(),
                    onClick = withHaptic { showDialog = false }
                ) { Text(stringResource(android.R.string.cancel)) }
            },
            confirmButton = {
                Button(
                    shapes = ButtonDefaults.shapes(),
                    onClick = withHaptic {
                    showDialog = false
                    showLoading = true

                    coroutineScope.launch {
                        withContext(Dispatchers.IO) {
                            // Clear weather configs
                            WeatherConfig.clear(context)

                            // Clear shared preferences
                            prefController.reset()

                            // Clear dynamic resource database
                            dynamicResourceViewModel?.clearAllResources()

                            disableBlur(false)

                            prefController.setInt(
                                SettingsKey.OVERLAY_VERSION_CODE,
                                BuildConfig.OVERLAY_VERSION_CODE
                            )
                            prefController.setBoolean(
                                SettingsKey.ON_HOME_PAGE,
                                true
                            )
                            prefController.setBoolean(
                                SettingsKey.FIRST_INSTALL,
                                false
                            )

                            Shell.cmd(
                                $$"> $$MODULE_DIR/system.prop; > $$MODULE_DIR/post-exec.sh; for ol in $(cmd overlay list | grep -E '.x.*IconifyComponent' | sed -E 's/^.x..//'); do cmd overlay disable $ol; done"
                            ).submit()

                            delay(3000)
                            showLoading = false

                            SystemUtils.restartSystemUI()
                            activity?.let { AppUtils.restartApplication(it) }
                        }
                    }
                    }) { Text(stringResource(R.string.ok)) }
            }
        )
    }

    PreferenceListener(key = SettingsKey.RESTART_SYSTEMUI_AFTER_BOOT) {
        if ((it.newValue as PrefValue.BoolValue).v) {
            SystemUtils.enableRestartSystemuiAfterBoot()
        } else {
            SystemUtils.disableRestartSystemuiAfterBoot()
        }
    }

    PreferenceScreen(
        items = settingsPreferences(onDisableEverything = { showDialog = true }),
        title = stringResource(R.string.activity_title_settings),
        showActionIcon = true
    )
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    PreviewComposable {
        SettingsScreen(null)
    }
}