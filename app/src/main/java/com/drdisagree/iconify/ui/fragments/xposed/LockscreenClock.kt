package com.drdisagree.iconify.ui.fragments.xposed

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.Iconify.Companion.appContextLocale
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Dynamic.isAtleastA14
import com.drdisagree.iconify.common.Preferences.LSCLOCK_FONT_SWITCH
import com.drdisagree.iconify.common.Preferences.LSCLOCK_STYLE
import com.drdisagree.iconify.common.Preferences.LSCLOCK_SWITCH
import com.drdisagree.iconify.common.Resources.LOCKSCREEN_CLOCK_LAYOUT
import com.drdisagree.iconify.common.Resources.LSCLOCK_FONT_DIR
import com.drdisagree.iconify.config.RPrefs.getBoolean
import com.drdisagree.iconify.config.RPrefs.putBoolean
import com.drdisagree.iconify.ui.activities.MainActivity
import com.drdisagree.iconify.ui.adapters.ClockPreviewAdapter
import com.drdisagree.iconify.ui.base.ControlledPreferenceFragmentCompat
import com.drdisagree.iconify.ui.models.ClockModel
import com.drdisagree.iconify.ui.preferences.FilePickerPreference
import com.drdisagree.iconify.ui.preferences.RecyclerPreference
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

    override val hasMenu: Boolean
        get() = true

    private var startActivityIntent = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val path = getRealPath(data)

            if (path != null && moveToIconifyHiddenDir(path, LSCLOCK_FONT_DIR)) {
                putBoolean(LSCLOCK_FONT_SWITCH, false)
                putBoolean(LSCLOCK_FONT_SWITCH, true)

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

    override fun updateScreen(key: String?) {
        super.updateScreen(key)

        when (key) {
            LSCLOCK_SWITCH -> {
                if (getBoolean(key) && isAtleastA14) {
                    Shell.cmd(
                        "settings put secure lock_screen_custom_clock_face default"
                    ).exec()
                }

                MainActivity.showOrHidePendingActionButton(
                    activityBinding = (requireActivity() as MainActivity).binding,
                    requiresSystemUiRestart = true
                )
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)

        findPreference<RecyclerPreference>(LSCLOCK_STYLE)?.apply {
            setAdapter(initLockscreenClockStyles())
            setPreference(LSCLOCK_STYLE, 0)
        }

        findPreference<FilePickerPreference>("xposed_lockscreenclockfontpicker")?.apply {
            setOnButtonClick {
                launchFilePicker(context, "font", startActivityIntent)
            }
        }
    }

    @SuppressLint("DiscouragedApi")
    private fun initLockscreenClockStyles(): ClockPreviewAdapter {
        val lsClock = ArrayList<ClockModel>()
        var maxIndex = 0

        while (requireContext()
                .resources
                .getIdentifier(
                    LOCKSCREEN_CLOCK_LAYOUT + maxIndex,
                    "layout",
                    BuildConfig.APPLICATION_ID
                ) != 0
        ) {
            maxIndex++
        }

        for (i in 0 until maxIndex) {
            lsClock.add(
                ClockModel(
                    if (i == 0) {
                        requireContext().getString(R.string.clock_none)
                    } else {
                        requireContext().getString(R.string.clock_style_name, i)
                    },
                    requireContext()
                        .resources
                        .getIdentifier(
                            LOCKSCREEN_CLOCK_LAYOUT + i,
                            "layout",
                            BuildConfig.APPLICATION_ID
                        )
                )
            )
        }

        return ClockPreviewAdapter(
            requireContext(),
            lsClock,
            LSCLOCK_SWITCH,
            LSCLOCK_STYLE
        )
    }
}
