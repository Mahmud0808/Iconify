package com.drdisagree.iconify.features.settings.main.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
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
import com.drdisagree.iconify.core.utils.SystemUtils.saveBootId
import com.drdisagree.iconify.data.common.Const.GITHUB_REPO
import com.drdisagree.iconify.data.common.Const.ICONIFY_CROWDIN
import com.drdisagree.iconify.data.common.Const.TELEGRAM_GROUP
import com.drdisagree.iconify.data.common.Resources.MODULE_DIR
import com.drdisagree.iconify.data.keys.SettingsKey
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun settingsPreferences(
    onDisableEverything: () -> Unit = {},
) = preferenceScreen {
    category(title = "Appearance") {
        action(
            key = "look_and_feel",
            icon = iconRes(R.drawable.ic_app_icon),
            title = stringRes("Look & Feel"),
            summary = { _, _ -> stringRes("Theme, contrast and colors") },
            onClick = { _, _, nav ->
                nav.navigate(NavRoutes.Settings.LookAndFeel) {
                    launchSingleTop = true
                }
            }
        )

        action(
            key = "app_language",
            icon = iconRes(R.drawable.ic_language),
            title = stringRes(R.string.settings_app_language),
            summary = { _, _ -> stringRes("Choose your app language") },
            onClick = { context, _, _ ->
                context.startActivity(
                    Intent(Settings.ACTION_APP_LOCALE_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                )
            }
        )

        switch(
            key = SettingsKey.HAPTICS_AND_VIBRATION,
            icon = iconRes(R.drawable.ic_vibrate),
            title = stringRes(R.string.settings_vibrate_ui_card_title),
            summary = { _, _ -> stringRes(R.string.settings_vibrate_ui_page_card_desc) },
            isEnabled = { ctrl -> ctrl.getBoolean("notifications_enabled", true) },
        )
    }

    category(title = stringRes(R.string.settings_section_title_miscellaneous)) {
        switch(
            key = SettingsKey.RESTART_SYSTEMUI_AFTER_BOOT,
            icon = iconRes(R.drawable.ic_restart_systemui_after_boot),
            title = stringRes(R.string.settings_restart_systemui_after_boot_title),
            summary = { _, _ -> stringRes(R.string.settings_restart_systemui_after_boot_desc) },
        )

        action(
            key = "clearAppCache",
            icon = iconRes(R.drawable.ic_clear_cache),
            title = stringRes(R.string.settings_clear_app_cache_title),
            summary = { _, _ -> stringRes(R.string.settings_clear_app_cache_desc) },
            onClick = { context, _, _ ->
                CacheUtils.clearCache(context)

                Toast.makeText(
                    context,
                    context.getString(R.string.toast_clear_cache),
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        action(
            key = "disableEverything",
            icon = iconRes(R.drawable.ic_disable_everything),
            title = stringRes(R.string.settings_disable_everything_title),
            summary = { _, _ -> stringRes(R.string.settings_disable_everything_desc) },
            onClick = { _, _, _ -> onDisableEverything() }
        )
    }

    category(title = stringRes(R.string.settings_section_title_about)) {
        action(
            key = "iconifyGitHub",
            icon = iconRes(R.drawable.ic_github),
            title = stringRes(R.string.settings_github_repository_title),
            summary = { _, _ -> stringRes(R.string.settings_github_repository_desc) },
            onClick = { context, _, _ -> openUrl(context, GITHUB_REPO) }
        )

        action(
            key = "iconifyTelegram",
            icon = iconRes(R.drawable.ic_telegram),
            title = stringRes(R.string.settings_telegram_group_title),
            summary = { _, _ -> stringRes(R.string.settings_telegram_group_desc) },
            onClick = { context, _, _ -> openUrl(context, TELEGRAM_GROUP) }
        )

        action(
            key = "iconifyTranslate",
            icon = iconRes(R.drawable.ic_translate),
            title = stringRes(R.string.settings_translate_title),
            summary = { _, _ -> stringRes(R.string.settings_translate_desc) },
            onClick = { context, _, _ -> openUrl(context, ICONIFY_CROWDIN) }
        )
    }
}

@Composable
fun SettingsScreen() {
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
                TextButton(onClick = withHaptic { showDialog = false }) { Text("Cancel") }
            },
            confirmButton = {
                TextButton(onClick = withHaptic {
                    showDialog = false
                    showLoading = true

                    coroutineScope.launch {
                        withContext(Dispatchers.IO) {
                            //                    WeatherConfig.clear(context)

                            // Clear shared preferences
                            prefController.reset()

                            // Clear dynamic resource database
                            //                    DynamicResourceRepository(
                            //                        DynamicResourceDatabase.getInstance().dynamicResourceDao()
                            //                    ).apply {
                            //                        deleteResources(getAllResources())
                            //                    }

                            saveBootId
                            disableBlur(false)

                            prefController.setInt(
                                SettingsKey.SAVED_VERSION_CODE,
                                BuildConfig.VERSION_CODE
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
                }) { Text("OK") }
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
        title = stringResource(R.string.activity_title_settings)
    )
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    PreviewComposable {
        SettingsScreen()
    }
}