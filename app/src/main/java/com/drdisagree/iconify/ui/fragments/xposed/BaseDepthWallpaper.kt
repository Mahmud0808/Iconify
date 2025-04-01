package com.drdisagree.iconify.ui.fragments.xposed

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.Iconify.Companion.appContextLocale
import com.drdisagree.iconify.R
import com.drdisagree.iconify.data.common.Const.AI_PLUGIN_PACKAGE
import com.drdisagree.iconify.data.common.Const.AI_PLUGIN_URL
import com.drdisagree.iconify.data.common.Preferences.DEPTH_WALLPAPER_AI_MODE
import com.drdisagree.iconify.data.common.Preferences.DEPTH_WALLPAPER_AI_STATUS
import com.drdisagree.iconify.data.common.Preferences.DEPTH_WALLPAPER_CHANGED
import com.drdisagree.iconify.data.common.Preferences.DEPTH_WALLPAPER_SWITCH
import com.drdisagree.iconify.data.common.XposedConst.DEPTH_WALL_BG_FILE
import com.drdisagree.iconify.data.common.XposedConst.DEPTH_WALL_FG_FILE
import com.drdisagree.iconify.data.config.RPrefs
import com.drdisagree.iconify.data.config.RPrefs.putBoolean
import com.drdisagree.iconify.ui.activities.MainActivity
import com.drdisagree.iconify.ui.base.ControlledPreferenceFragmentCompat
import com.drdisagree.iconify.ui.preferences.FilePickerPreference
import com.drdisagree.iconify.ui.preferences.PreferenceMenu
import com.drdisagree.iconify.utils.AppUtils
import com.drdisagree.iconify.utils.FileUtils.getRealPath
import com.drdisagree.iconify.utils.FileUtils.launchFilePicker
import com.drdisagree.iconify.utils.FileUtils.moveToIconifyHiddenDir

abstract class BaseDepthWallpaper : ControlledPreferenceFragmentCompat() {

    override val title: String
        get() = getString(R.string.activity_title_depth_wallpaper)

    override val backButtonEnabled: Boolean
        get() = true

    override val layoutResource: Int
        get() = R.xml.xposed_depth_wallpaper

    override val hasMenu: Boolean
        get() = true

    private lateinit var startActivityIntentForBackgroundImage: ActivityResultLauncher<Intent?>
    private lateinit var startActivityIntentForForegroundImage: ActivityResultLauncher<Intent?>

    abstract fun PreferenceMenu.setMLKitStatus()

    override fun onAttach(context: Context) {
        super.onAttach(context)

        startActivityIntentForBackgroundImage = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val path = getRealPath(data)

                if (path != null &&
                    moveToIconifyHiddenDir(path, DEPTH_WALL_BG_FILE.absolutePath)
                ) {
                    putBoolean(DEPTH_WALLPAPER_CHANGED, false)
                    putBoolean(DEPTH_WALLPAPER_CHANGED, true)

                    Toast.makeText(
                        appContext,
                        appContextLocale.resources.getString(R.string.toast_applied),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        appContext,
                        appContextLocale.resources.getString(R.string.toast_rename_file),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        startActivityIntentForForegroundImage = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val path = getRealPath(data)

                if (path != null &&
                    moveToIconifyHiddenDir(path, DEPTH_WALL_FG_FILE.absolutePath)
                ) {
                    putBoolean(DEPTH_WALLPAPER_CHANGED, false)
                    putBoolean(DEPTH_WALLPAPER_CHANGED, true)

                    Toast.makeText(
                        appContext,
                        appContextLocale.resources.getString(R.string.toast_applied),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        appContext,
                        appContextLocale.resources.getString(R.string.toast_rename_file),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun updateScreen(key: String?) {
        super.updateScreen(key)

        when (key) {
            DEPTH_WALLPAPER_SWITCH -> {
                MainActivity.showOrHidePendingActionButton(
                    activityBinding = (requireActivity() as MainActivity).binding,
                    requiresSystemUiRestart = true
                )
            }

            DEPTH_WALLPAPER_AI_MODE -> {
                checkAiStatus()
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)

        checkAiStatus()

        findPreference<FilePickerPreference>("xposed_depthwallpaperbgimagepicker")?.apply {
            setOnButtonClick {
                launchFilePicker(context, "image", startActivityIntentForBackgroundImage)
            }
        }

        findPreference<FilePickerPreference>("xposed_depthwallpaperfgimagepicker")?.apply {
            setOnButtonClick {
                launchFilePicker(context, "image", startActivityIntentForForegroundImage)
            }
        }
    }

    private fun checkAiStatus() {
        findPreference<PreferenceMenu>(DEPTH_WALLPAPER_AI_STATUS)?.apply {
            if (RPrefs.getString(DEPTH_WALLPAPER_AI_MODE, "0") == "0") {
                setMLKitStatus()
            } else {
                setShowArrow(true)
                if (AppUtils.isAppInstalled(AI_PLUGIN_PACKAGE)) {
                    setSummary(getString(R.string.depth_wallpaper_ai_status_plugin_installed))
                    setOnPreferenceClickListener {
                        startActivity(
                            requireContext()
                                .packageManager
                                .getLaunchIntentForPackage(AI_PLUGIN_PACKAGE)!!
                        )
                        true
                    }
                } else {
                    setSummary(getString(R.string.depth_wallpaper_ai_status_plugin_not_installed))
                    setOnPreferenceClickListener {
                        try {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(AI_PLUGIN_URL)))
                            true
                        } catch (_: Exception) {
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.toast_error),
                                Toast.LENGTH_SHORT
                            ).show()
                            false
                        }
                    }
                }
            }
        }
    }
}
