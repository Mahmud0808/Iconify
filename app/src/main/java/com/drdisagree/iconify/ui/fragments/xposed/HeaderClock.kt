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
import com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_FONT_SWITCH
import com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_STYLE
import com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_SWITCH
import com.drdisagree.iconify.common.Resources.HEADER_CLOCK_FONT_DIR
import com.drdisagree.iconify.common.Resources.HEADER_CLOCK_LAYOUT
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

class HeaderClock : ControlledPreferenceFragmentCompat() {

    override val title: String
        get() = getString(R.string.activity_title_header_clock)

    override val backButtonEnabled: Boolean
        get() = true

    override val layoutResource: Int
        get() = R.xml.xposed_header_clock

    override val hasMenu: Boolean
        get() = true

    private var startActivityIntent = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val path = getRealPath(data)

            if (path != null && moveToIconifyHiddenDir(path, HEADER_CLOCK_FONT_DIR)) {
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

        findPreference<FilePickerPreference>("xposed_headerclockfontpicker")?.apply {
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
            maxIndex++
        }

        for (i in 0 until maxIndex) {
            headerClock.add(
                ClockModel(
                    if (i == 0) {
                        requireContext().getString(R.string.clock_none)
                    } else {
                        requireContext().getString(R.string.clock_style_name, i)
                    },
                    requireContext()
                        .resources
                        .getIdentifier(
                            HEADER_CLOCK_LAYOUT + i,
                            "layout",
                            BuildConfig.APPLICATION_ID
                        )
                )
            )
        }

        return ClockPreviewAdapter(
            requireContext(),
            headerClock,
            HEADER_CLOCK_SWITCH,
            HEADER_CLOCK_STYLE
        )
    }
}
