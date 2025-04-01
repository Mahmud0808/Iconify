package com.drdisagree.iconify.ui.fragments.xposed

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.Iconify.Companion.appContextLocale
import com.drdisagree.iconify.R
import com.drdisagree.iconify.data.common.Preferences.HEADER_CLOCK_FONT_PICKER
import com.drdisagree.iconify.data.common.Preferences.HEADER_CLOCK_FONT_SWITCH
import com.drdisagree.iconify.data.common.Preferences.HEADER_CLOCK_STYLE
import com.drdisagree.iconify.data.common.Preferences.HEADER_CLOCK_SWITCH
import com.drdisagree.iconify.data.common.Resources.HEADER_CLOCK_LAYOUT
import com.drdisagree.iconify.data.common.XposedConst.HEADER_CLOCK_FONT_FILE
import com.drdisagree.iconify.data.config.RPrefs.putBoolean
import com.drdisagree.iconify.data.models.ClockModel
import com.drdisagree.iconify.ui.activities.MainActivity
import com.drdisagree.iconify.ui.adapters.ClockPreviewAdapter
import com.drdisagree.iconify.ui.base.ControlledPreferenceFragmentCompat
import com.drdisagree.iconify.ui.preferences.FilePickerPreference
import com.drdisagree.iconify.ui.preferences.RecyclerPreference
import com.drdisagree.iconify.utils.FileUtils.getRealPath
import com.drdisagree.iconify.utils.FileUtils.launchFilePicker
import com.drdisagree.iconify.utils.FileUtils.moveToIconifyHiddenDir

class HeaderClock : ControlledPreferenceFragmentCompat() {

    override val title: String
        get() = getString(R.string.activity_title_header_clock)

    override val backButtonEnabled: Boolean
        get() = true

    override val layoutResource: Int
        get() = R.xml.xposed_header_clock

    override val hasMenu: Boolean
        get() = true

    private lateinit var startActivityIntent: ActivityResultLauncher<Intent?>

    override fun onAttach(context: Context) {
        super.onAttach(context)

        startActivityIntent = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val path = getRealPath(data)

                if (path != null &&
                    moveToIconifyHiddenDir(path, HEADER_CLOCK_FONT_FILE.absolutePath)
                ) {
                    putBoolean(HEADER_CLOCK_FONT_SWITCH, false)
                    putBoolean(HEADER_CLOCK_FONT_SWITCH, true)

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
            HEADER_CLOCK_SWITCH -> {
                MainActivity.showOrHidePendingActionButton(
                    activityBinding = (requireActivity() as MainActivity).binding,
                    requiresSystemUiRestart = true
                )
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)

        findPreference<RecyclerPreference>(HEADER_CLOCK_STYLE)?.apply {
            setAdapter(initHeaderClockStyles())
            setPreference(HEADER_CLOCK_STYLE, 0)
        }

        findPreference<FilePickerPreference>(HEADER_CLOCK_FONT_PICKER)?.apply {
            setOnButtonClick {
                launchFilePicker(context, "font", startActivityIntent)
            }
        }
    }

    @SuppressLint("DiscouragedApi")
    private fun initHeaderClockStyles(): ClockPreviewAdapter {
        val headerClock = ArrayList<ClockModel>()
        var maxIndex = 0

        while (requireContext()
                .resources
                .getIdentifier(
                    HEADER_CLOCK_LAYOUT + maxIndex,
                    "layout",
                    BuildConfig.APPLICATION_ID
                ) != 0
        ) {
            headerClock.add(
                ClockModel(
                    if (maxIndex == 0) {
                        requireContext().getString(R.string.clock_none)
                    } else {
                        requireContext().getString(R.string.clock_style_name, maxIndex)
                    },
                    requireContext()
                        .resources
                        .getIdentifier(
                            HEADER_CLOCK_LAYOUT + maxIndex,
                            "layout",
                            BuildConfig.APPLICATION_ID
                        )
                )
            )
            maxIndex++
        }

        return ClockPreviewAdapter(
            requireContext(),
            headerClock,
            HEADER_CLOCK_SWITCH,
            HEADER_CLOCK_STYLE
        )
    }
}
