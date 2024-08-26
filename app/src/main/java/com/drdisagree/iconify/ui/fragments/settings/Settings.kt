package com.drdisagree.iconify.ui.fragments.settings

import android.content.ComponentName
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.Iconify.Companion.appContextLocale
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Const
import com.drdisagree.iconify.common.Preferences.APP_ICON
import com.drdisagree.iconify.common.Preferences.APP_LANGUAGE
import com.drdisagree.iconify.common.Preferences.APP_THEME
import com.drdisagree.iconify.common.Preferences.EASTER_EGG
import com.drdisagree.iconify.common.Preferences.FIRST_INSTALL
import com.drdisagree.iconify.common.Preferences.ON_HOME_PAGE
import com.drdisagree.iconify.common.Preferences.RESTART_SYSUI_AFTER_BOOT
import com.drdisagree.iconify.common.Resources.MODULE_DIR
import com.drdisagree.iconify.config.RPrefs
import com.drdisagree.iconify.ui.base.ControlledPreferenceFragmentCompat
import com.drdisagree.iconify.ui.preferences.PreferenceCategory
import com.drdisagree.iconify.ui.preferences.PreferenceMenu
import com.drdisagree.iconify.utils.AppUtil.restartApplication
import com.drdisagree.iconify.utils.CacheUtil.clearCache
import com.drdisagree.iconify.utils.SystemUtil.disableBlur
import com.drdisagree.iconify.utils.SystemUtil.disableRestartSystemuiAfterBoot
import com.drdisagree.iconify.utils.SystemUtil.enableRestartSystemuiAfterBoot
import com.drdisagree.iconify.utils.SystemUtil.restartSystemUI
import com.drdisagree.iconify.utils.SystemUtil.saveBootId
import com.drdisagree.iconify.utils.SystemUtil.saveVersionCode
import com.drdisagree.iconify.utils.weather.WeatherConfig
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.topjohnwu.superuser.Shell
import java.util.Date
import java.util.concurrent.Executors

class Settings : ControlledPreferenceFragmentCompat() {

    private var clickTimestamps = LongArray(NUM_CLICKS_REQUIRED)
    private var oldestIndex = 0
    private var nextIndex = 0

    override val title: String
        get() = getString(R.string.settings_title)

    override val backButtonEnabled: Boolean
        get() = true

    override val layoutResource: Int
        get() = R.xml.settings

    override val hasMenu: Boolean
        get() = true

    override fun updateScreen(key: String?) {
        super.updateScreen(key)

        when (key) {
            APP_LANGUAGE -> {
                restartApplication(requireActivity())
            }

            APP_ICON -> {
                val splashActivities = appContextLocale.resources
                    .getStringArray(R.array.app_icon_identifier)
                changeIcon(RPrefs.getString(key, splashActivities[0])!!)
            }

            APP_THEME -> {
                restartApplication(requireActivity())
            }

            RESTART_SYSUI_AFTER_BOOT -> {
                if (RPrefs.getBoolean(key, false)) {
                    enableRestartSystemuiAfterBoot()
                } else {
                    disableRestartSystemuiAfterBoot()
                }
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)

        findPreference<PreferenceCategory>("miscSettings")?.setOnPreferenceClickListener {
            onEasterViewClicked()
            true
        }

        findPreference<PreferenceMenu>("clearAppCache")?.setOnPreferenceClickListener {
            clearCache(appContext)
            Toast.makeText(
                appContext,
                appContextLocale.resources.getString(R.string.toast_clear_cache),
                Toast.LENGTH_SHORT
            ).show()
            true
        }

        findPreference<PreferenceMenu>("disableEverything")?.setOnPreferenceClickListener {
            MaterialAlertDialogBuilder(requireActivity())
                .setCancelable(true)
                .setTitle(requireContext().resources.getString(R.string.import_settings_confirmation_title))
                .setMessage(requireContext().resources.getString(R.string.import_settings_confirmation_desc))
                .setPositiveButton(getString(R.string.positive)) { dialog: DialogInterface, _: Int ->
                    dialog.dismiss()

                    // Show loading dialog
                    loadingDialog?.show(resources.getString(R.string.loading_dialog_wait))

                    Executors.newSingleThreadExecutor().execute {
                        Settings.disableEverything()
                        Handler(Looper.getMainLooper()).postDelayed({

                            // Hide loading dialog
                            loadingDialog?.hide()

                            // Restart SystemUI
                            restartSystemUI()
                        }, 3000)
                    }
                }
                .setNegativeButton(getString(R.string.negative)) { dialog: DialogInterface, _: Int ->
                    dialog.dismiss()
                }
                .show()
            true
        }

        findPreference<PreferenceMenu>("iconifyGitHub")?.setOnPreferenceClickListener {
            startActivity(
                Intent(Intent.ACTION_VIEW).apply {
                    setData(Uri.parse(Const.GITHUB_REPO))
                }
            )
            true
        }

        findPreference<PreferenceMenu>("iconifyTelegram")?.setOnPreferenceClickListener {
            startActivity(
                Intent(Intent.ACTION_VIEW).apply {
                    setData(Uri.parse(Const.TELEGRAM_GROUP))
                }
            )
            true
        }

        findPreference<PreferenceMenu>("iconifyTranslate")?.setOnPreferenceClickListener {
            startActivity(
                Intent(Intent.ACTION_VIEW).apply {
                    setData(Uri.parse(Const.ICONIFY_CROWDIN))
                }
            )
            true
        }
    }

    private fun changeIcon(splash: String) {
        val manager = requireActivity().packageManager
        val splashActivities = appContextLocale.resources
            .getStringArray(R.array.app_icon_identifier)

        for (splashActivity in splashActivities) {
            manager.setComponentEnabledSetting(
                ComponentName(
                    requireActivity(),
                    "com.drdisagree.iconify.$splashActivity"
                ),
                if (splash == splashActivity) {
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                } else {
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                },
                PackageManager.DONT_KILL_APP
            )
        }
    }

    private fun onEasterViewClicked() {
        val timeMillis = Date().time

        if (nextIndex == NUM_CLICKS_REQUIRED - 1 || oldestIndex > 0) {
            val diff = (timeMillis - clickTimestamps[oldestIndex]).toInt()
            if (diff < SECONDS_FOR_CLICKS * 1000) {
                if (!RPrefs.getBoolean(EASTER_EGG)) {
                    RPrefs.putBoolean(EASTER_EGG, true)

                    Toast.makeText(
                        requireContext(),
                        requireContext().resources.getString(R.string.toast_easter_egg),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        requireContext().resources.getString(R.string.toast_easter_egg_activated),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                oldestIndex = 0
                nextIndex = 0
            } else {
                oldestIndex++
            }
        }

        clickTimestamps[nextIndex] = timeMillis
        nextIndex++

        if (nextIndex == NUM_CLICKS_REQUIRED) nextIndex = 0
        if (oldestIndex == NUM_CLICKS_REQUIRED) oldestIndex = 0
    }

    companion object {
        private const val SECONDS_FOR_CLICKS = 3.0
        private const val NUM_CLICKS_REQUIRED = 7

        fun disableEverything() {
            WeatherConfig.clear(appContext)
            RPrefs.clearAllPrefs()
            RPrefs.clearAllPrefs()

            saveBootId
            disableBlur(false)
            saveVersionCode()

            RPrefs.putBoolean(ON_HOME_PAGE, true)
            RPrefs.putBoolean(FIRST_INSTALL, false)

            Shell.cmd(
                "> $MODULE_DIR/system.prop; > $MODULE_DIR/post-exec.sh; for ol in $(cmd overlay list | grep -E '.x.*IconifyComponent' | sed -E 's/^.x..//'); do cmd overlay disable \$ol; done; killall com.android.systemui"
            ).submit()
        }
    }
}
