package com.drdisagree.iconify.ui.fragments.xposed

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.Iconify.Companion.appContextLocale
import com.drdisagree.iconify.R
import com.drdisagree.iconify.data.common.Const.RESET_LOCKSCREEN_CLOCK_COMMAND
import com.drdisagree.iconify.data.common.Dynamic.isAtleastA14
import com.drdisagree.iconify.data.common.Preferences.LSCLOCK_FONT_PICKER
import com.drdisagree.iconify.data.common.Preferences.LSCLOCK_FONT_SWITCH
import com.drdisagree.iconify.data.common.Preferences.LSCLOCK_IMAGE_PICKER1
import com.drdisagree.iconify.data.common.Preferences.LSCLOCK_IMAGE_PICKER2
import com.drdisagree.iconify.data.common.Preferences.LSCLOCK_IMAGE_SWITCH
import com.drdisagree.iconify.data.common.Preferences.LSCLOCK_MOVE_NOTIFICATION_ICONS
import com.drdisagree.iconify.data.common.Preferences.LSCLOCK_SWITCH
import com.drdisagree.iconify.data.common.XposedConst.LSCLOCK_FONT_FILE
import com.drdisagree.iconify.data.common.XposedConst.LSCLOCK_IMAGE1_FILE
import com.drdisagree.iconify.data.common.XposedConst.LSCLOCK_IMAGE2_FILE
import com.drdisagree.iconify.data.config.RPrefs.getBoolean
import com.drdisagree.iconify.data.config.RPrefs.putBoolean
import com.drdisagree.iconify.ui.activities.MainActivity
import com.drdisagree.iconify.ui.base.ControlledPreferenceFragmentCompat
import com.drdisagree.iconify.ui.preferences.FilePickerPreference
import com.drdisagree.iconify.utils.FileUtils.getRealPath
import com.drdisagree.iconify.utils.FileUtils.launchFilePicker
import com.drdisagree.iconify.utils.FileUtils.moveToIconifyHiddenDir
import com.topjohnwu.superuser.Shell

class LockscreenClock : ControlledPreferenceFragmentCompat() {

    override val title: String
        get() = getString(R.string.activity_title_lockscreen_clock)

    override val backButtonEnabled: Boolean
        get() = true

    override val layoutResource: Int
        get() = R.xml.xposed_lockscreen_clock

    override val themeResource: Int
        get() = R.style.PrefsThemeNoToolbar

    override val hasMenu: Boolean
        get() = true

    private lateinit var startActivityIntent: ActivityResultLauncher<Intent?>
    private var copyToDirectory: String? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        startActivityIntent = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val path = getRealPath(data)
                val directory = copyToDirectory ?: return@registerForActivityResult
                Log.d("LockscreenClock", "Directory: $directory")

                if (path != null && moveToIconifyHiddenDir(path, directory)) {
                    if (directory == LSCLOCK_FONT_FILE.absolutePath) {
                        putBoolean(LSCLOCK_FONT_SWITCH, false)
                        putBoolean(LSCLOCK_FONT_SWITCH, true)
                    } else if (directory == LSCLOCK_IMAGE1_FILE.absolutePath ||
                        directory == LSCLOCK_IMAGE2_FILE.absolutePath
                    ) {
                        putBoolean(LSCLOCK_IMAGE_SWITCH, false)
                        putBoolean(LSCLOCK_IMAGE_SWITCH, true)
                    }

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
            LSCLOCK_SWITCH -> {
                if (getBoolean(key) && isAtleastA14) {
                    Shell.cmd(RESET_LOCKSCREEN_CLOCK_COMMAND).exec()
                }

                MainActivity.showOrHidePendingActionButton(
                    activityBinding = (requireActivity() as MainActivity).binding,
                    requiresSystemUiRestart = true
                )
            }

            LSCLOCK_MOVE_NOTIFICATION_ICONS -> {
                MainActivity.showOrHidePendingActionButton(
                    activityBinding = (requireActivity() as MainActivity).binding,
                    requiresSystemUiRestart = true
                )
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)

        findPreference<FilePickerPreference>(LSCLOCK_FONT_PICKER)?.apply {
            setOnButtonClick {
                copyToDirectory = LSCLOCK_FONT_FILE.absolutePath
                launchFilePicker(context, "font", startActivityIntent)
            }
        }

        findPreference<FilePickerPreference>(LSCLOCK_IMAGE_PICKER1)?.apply {
            setOnButtonClick {
                copyToDirectory = LSCLOCK_IMAGE1_FILE.absolutePath
                launchFilePicker(context, "image", startActivityIntent)
            }
        }

        findPreference<FilePickerPreference>(LSCLOCK_IMAGE_PICKER2)?.apply {
            setOnButtonClick {
                copyToDirectory = LSCLOCK_IMAGE2_FILE.absolutePath
                launchFilePicker(context, "image", startActivityIntent)
            }
        }
    }
}
