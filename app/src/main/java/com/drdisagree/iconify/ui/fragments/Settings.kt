package com.drdisagree.iconify.ui.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation.findNavController
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.Iconify.Companion.appContextLocale
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Const
import com.drdisagree.iconify.common.Const.FRAGMENT_BACK_BUTTON_DELAY
import com.drdisagree.iconify.common.Preferences
import com.drdisagree.iconify.common.Preferences.APP_ICON
import com.drdisagree.iconify.common.Preferences.APP_LANGUAGE
import com.drdisagree.iconify.common.Preferences.APP_THEME
import com.drdisagree.iconify.common.Preferences.AUTO_UPDATE
import com.drdisagree.iconify.common.Preferences.EASTER_EGG
import com.drdisagree.iconify.common.Preferences.FIRST_INSTALL
import com.drdisagree.iconify.common.Preferences.ON_HOME_PAGE
import com.drdisagree.iconify.common.Preferences.RESTART_SYSUI_AFTER_BOOT
import com.drdisagree.iconify.common.Preferences.RESTART_SYSUI_BEHAVIOR_EXT
import com.drdisagree.iconify.common.Preferences.SHOW_HOME_CARD
import com.drdisagree.iconify.common.Preferences.SHOW_XPOSED_WARN
import com.drdisagree.iconify.common.Preferences.UPDATE_OVER_WIFI
import com.drdisagree.iconify.common.Resources.MODULE_DIR
import com.drdisagree.iconify.config.RPrefs
import com.drdisagree.iconify.config.RPrefs.putString
import com.drdisagree.iconify.databinding.FragmentSettingsBinding
import com.drdisagree.iconify.services.UpdateScheduler.scheduleUpdates
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.dialogs.LoadingDialog
import com.drdisagree.iconify.utils.AppUtil.restartApplication
import com.drdisagree.iconify.utils.CacheUtil.clearCache
import com.drdisagree.iconify.utils.SystemUtil.disableBlur
import com.drdisagree.iconify.utils.SystemUtil.disableRestartSystemuiAfterBoot
import com.drdisagree.iconify.utils.SystemUtil.enableRestartSystemuiAfterBoot
import com.drdisagree.iconify.utils.SystemUtil.hasStoragePermission
import com.drdisagree.iconify.utils.SystemUtil.requestStoragePermission
import com.drdisagree.iconify.utils.SystemUtil.restartSystemUI
import com.drdisagree.iconify.utils.SystemUtil.saveBootId
import com.drdisagree.iconify.utils.SystemUtil.saveVersionCode
import com.drdisagree.iconify.utils.helper.ImportExport.exportSettings
import com.drdisagree.iconify.utils.helper.ImportExport.importSettings
import com.drdisagree.iconify.utils.weather.WeatherConfig
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.topjohnwu.superuser.Shell
import java.lang.Integer.parseInt
import java.util.Date
import java.util.concurrent.Executors

class Settings : BaseFragment() {

    private lateinit var binding: FragmentSettingsBinding
    private var loadingDialog: LoadingDialog? = null
    private var clickTimestamps = LongArray(NUM_CLICKS_REQUIRED)
    private var oldestIndex = 0
    private var nextIndex = 0

    private var startExportActivityIntent = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result1: ActivityResult ->
        if (result1.resultCode == Activity.RESULT_OK) {
            val data = result1.data ?: return@registerForActivityResult

            Executors.newSingleThreadExecutor().execute {
                try {
                    exportSettings(
                        RPrefs.getPrefs,
                        appContext.contentResolver.openOutputStream(data.data!!)!!
                    )

                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(
                            appContext,
                            appContextLocale.resources.getString(R.string.toast_export_settings_successfull),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (exception: Exception) {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(
                            appContext,
                            appContextLocale.resources.getString(R.string.toast_error),
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("Settings", "Error exporting settings", exception)
                    }
                }
            }
        }
    }

    private var startImportActivityIntent = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result2: ActivityResult ->
        if (result2.resultCode == Activity.RESULT_OK) {
            val data = result2.data ?: return@registerForActivityResult
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(requireContext().resources.getString(R.string.import_settings_confirmation_title))
                .setMessage(requireContext().resources.getString(R.string.import_settings_confirmation_desc))
                .setPositiveButton(
                    requireContext().resources.getString(R.string.btn_positive)
                ) { dialog: DialogInterface, _: Int ->
                    dialog.dismiss()
                    loadingDialog!!.show(resources.getString(R.string.loading_dialog_wait))
                    Executors.newSingleThreadExecutor().execute {
                        try {
                            val success = importSettings(
                                RPrefs.getPrefs,
                                appContext.contentResolver.openInputStream(data.data!!)!!,
                                true
                            )
                            Handler(Looper.getMainLooper()).post {
                                loadingDialog!!.hide()
                                if (success) {
                                    Toast.makeText(
                                        appContext,
                                        appContext.resources.getString(R.string.toast_import_settings_successfull),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        appContext,
                                        appContext.resources.getString(R.string.toast_error),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } catch (exception: Exception) {
                            Handler(Looper.getMainLooper()).post {
                                Toast.makeText(
                                    appContext,
                                    appContext.resources.getString(R.string.toast_error),
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.e("Settings", "Error importing settings", exception)
                            }
                        }
                    }
                }
                .setNegativeButton(requireContext().resources.getString(R.string.btn_negative)) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
                .show()
        }
    }

    @Suppress("deprecation")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val view: View = binding.getRoot()

        // Header
        binding.header.toolbar.setTitle(resources.getString(R.string.activity_title_settings))
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.header.toolbar)
        setHasOptionsMenu(true)
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.header.toolbar.setNavigationOnClickListener {
            Handler(Looper.getMainLooper()).postDelayed(
                { getParentFragmentManager().popBackStack() }, FRAGMENT_BACK_BUTTON_DELAY.toLong()
            )
        }

        // Show loading dialog
        loadingDialog = LoadingDialog(requireActivity())

        // Language
        var currentLanguage =
            listOf(*resources.getStringArray(R.array.locale_code)).indexOf("en-US")
        val locales = resources.configuration.getLocales()
        val localeCodes = listOf(*resources.getStringArray(R.array.locale_code))

        for (i in 0 until locales.size()) {
            val languageCode = locales[i].language
            val countryCode = locales[i].country
            val languageFormat = "$languageCode-$countryCode"

            if (localeCodes.contains(RPrefs.getString(APP_LANGUAGE, languageFormat))) {
                currentLanguage =
                    localeCodes.indexOf(RPrefs.getString(APP_LANGUAGE, languageFormat))
                break
            }
        }

        binding.settingsGeneral.appLanguage.setSelectedIndex(currentLanguage)
        binding.settingsGeneral.appLanguage.setOnItemSelectedListener { index: Int ->
            putString(
                APP_LANGUAGE,
                listOf<String>(*resources.getStringArray(R.array.locale_code))[index]
            )

            restartApplication(requireActivity())
        }

        // App Icon
        binding.settingsGeneral.appIcon.setSelectedIndex(RPrefs.getInt(APP_ICON, 0))
        binding.settingsGeneral.appIcon.setOnItemSelectedListener { index: Int ->
            RPrefs.putInt(APP_ICON, index)
            val splashActivities =
                appContextLocale.resources.getStringArray(R.array.app_icon_identifier)

            changeIcon(splashActivities[index])
        }

        // App Theme
        binding.settingsGeneral.appTheme.setSelectedIndex(
            parseInt(
                RPrefs.getString(
                    APP_THEME,
                    "2"
                )!!
            )
        )
        binding.settingsGeneral.appTheme.setOnItemSelectedListener { index: Int ->
            RPrefs.putString(APP_THEME, index.toString())

            restartApplication(requireActivity())
        }

        // Check for update
        binding.settingsUpdate.checkUpdate.setSummary(
            resources.getString(
                R.string.settings_current_version,
                BuildConfig.VERSION_NAME
            )
        )
        binding.settingsUpdate.checkUpdate.setOnClickListener {
            findNavController(view).navigate(
                R.id.action_settings_to_appUpdates
            )
        }

        // Auto update
        binding.settingsUpdate.autoUpdate.isSwitchChecked = RPrefs.getBoolean(AUTO_UPDATE, true)
        binding.settingsUpdate.autoUpdate.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            RPrefs.putBoolean(AUTO_UPDATE, isChecked)
            scheduleUpdates(requireContext().applicationContext)
            binding.settingsUpdate.autoUpdateWifiOnly.setEnabled(isChecked)
        }

        // Check over wifi only
        binding.settingsUpdate.autoUpdateWifiOnly.setEnabled(binding.settingsUpdate.autoUpdate.isSwitchChecked)
        binding.settingsUpdate.autoUpdateWifiOnly.isSwitchChecked =
            RPrefs.getBoolean(UPDATE_OVER_WIFI, true)
        binding.settingsUpdate.autoUpdateWifiOnly.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            RPrefs.putBoolean(UPDATE_OVER_WIFI, isChecked)
        }

        // Show xposed warn
        binding.settingsXposed.hideWarnMessage.isSwitchChecked =
            RPrefs.getBoolean(SHOW_XPOSED_WARN, true)
        binding.settingsXposed.hideWarnMessage.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            RPrefs.putBoolean(SHOW_XPOSED_WARN, isChecked)
        }

        // Restart systemui behavior
        binding.settingsXposed.modApplyingMethod.setSelectedIndex(
            RPrefs.getInt(RESTART_SYSUI_BEHAVIOR_EXT, 0)
        )
        binding.settingsXposed.modApplyingMethod.setOnItemSelectedListener { index: Int ->
            RPrefs.putInt(RESTART_SYSUI_BEHAVIOR_EXT, index)
        }

        // Restart systemui after boot
        binding.settingsMisc.restartSysuiAfterBoot.isSwitchChecked =
            RPrefs.getBoolean(RESTART_SYSUI_AFTER_BOOT, false)
        binding.settingsMisc.restartSysuiAfterBoot.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            RPrefs.putBoolean(RESTART_SYSUI_AFTER_BOOT, isChecked)
            if (isChecked) {
                enableRestartSystemuiAfterBoot()
            } else {
                disableRestartSystemuiAfterBoot()
            }
        }

        // Home page card
        binding.settingsMisc.homePageCard.isSwitchChecked = RPrefs.getBoolean(SHOW_HOME_CARD, true)
        binding.settingsMisc.homePageCard.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            RPrefs.putBoolean(SHOW_HOME_CARD, isChecked)
        }
        binding.settingsMisc.homePageCard.visibility =
            if (Preferences.isXposedOnlyMode) {
                View.GONE
            } else {
                View.VISIBLE
            }

        // Clear App Cache
        binding.settingsMisc.clearCache.setOnClickListener {
            clearCache(appContext)

            Toast.makeText(
                appContext,
                appContextLocale.resources.getString(R.string.toast_clear_cache),
                Toast.LENGTH_SHORT
            ).show()
        }

        // Experimental features
        binding.settingsMisc.settingsMiscTitle.setOnClickListener { onEasterViewClicked() }
        binding.settingsMisc.experimentalFeatures.setOnClickListener {
            findNavController(
                view
            ).navigate(R.id.action_settings_to_experimental)
        }
        binding.settingsMisc.experimentalFeatures.visibility =
            if (RPrefs.getBoolean(EASTER_EGG)) {
                View.VISIBLE
            } else {
                View.GONE
            }

        // Disable Everything
        binding.settingsMisc.buttonDisableEverything.setOnClickListener {
            MaterialAlertDialogBuilder(requireActivity())
                .setCancelable(true)
                .setTitle(requireContext().resources.getString(R.string.import_settings_confirmation_title))
                .setMessage(requireContext().resources.getString(R.string.import_settings_confirmation_desc))
                .setPositiveButton(getString(R.string.positive)) { dialog: DialogInterface, _: Int ->
                    dialog.dismiss()

                    // Show loading dialog
                    loadingDialog!!.show(resources.getString(R.string.loading_dialog_wait))
                    Executors.newSingleThreadExecutor().execute {
                        disableEverything()
                        Handler(Looper.getMainLooper()).postDelayed({

                            // Hide loading dialog
                            loadingDialog!!.hide()

                            // Restart SystemUI
                            restartSystemUI()
                        }, 3000)
                    }
                }
                .setNegativeButton(getString(R.string.negative)) { dialog: DialogInterface, _: Int ->
                    dialog.dismiss()
                }
                .show()
        }

        binding.settingsMisc.buttonDisableEverything.visibility =
            if (Preferences.isXposedOnlyMode) {
                View.GONE
            } else {
                View.VISIBLE
            }

        // Github repository
        binding.settingsAbout.githubRepository.setOnClickListener {
            startActivity(
                Intent(Intent.ACTION_VIEW).setData(Uri.parse(Const.GITHUB_REPO))
            )
        }

        // Telegram group
        binding.settingsAbout.telegramGroup.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setData(Uri.parse(Const.TELEGRAM_GROUP))
            startActivity(intent)
        }

        // Credits
        binding.settingsAbout.credits.setOnClickListener {
            findNavController(view).navigate(
                R.id.action_settings_to_credits2
            )
        }

        return view
    }

    override fun onDestroy() {
        loadingDialog?.hide()

        super.onDestroy()
    }

    @Suppress("deprecation")
    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.settings_menu, menu)

        super.onCreateOptionsMenu(menu, inflater)
    }

    @Suppress("deprecation")
    @Deprecated("Deprecated in Java")
    @SuppressLint("NonConstantResourceId")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_changelog -> findNavController(requireView()).navigate(R.id.action_settings_to_changelog2)
            R.id.menu_export_settings -> importExportSettings(true)
            R.id.menu_import_settings -> importExportSettings(false)
            R.id.restart_systemui -> Handler(Looper.getMainLooper()).postDelayed(
                { restartSystemUI() },
                300
            )
        }

        return super.onOptionsItemSelected(item)
    }

    private fun importExportSettings(export: Boolean) {
        if (!hasStoragePermission()) {
            requestStoragePermission(requireContext())
        } else {
            val fileIntent = Intent()
            fileIntent.setAction(if (export) Intent.ACTION_CREATE_DOCUMENT else Intent.ACTION_GET_CONTENT)
            fileIntent.setType("*/*")
            fileIntent.putExtra(Intent.EXTRA_TITLE, "configs" + ".iconify")

            if (export) {
                startExportActivityIntent.launch(fileIntent)
            } else {
                startImportActivityIntent.launch(fileIntent)
            }
        }
    }

    private fun changeIcon(splash: String) {
        val manager = requireActivity().packageManager
        val splashActivities =
            appContextLocale.resources.getStringArray(R.array.app_icon_identifier)
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

                    binding.settingsMisc.experimentalFeatures.visibility = View.VISIBLE

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