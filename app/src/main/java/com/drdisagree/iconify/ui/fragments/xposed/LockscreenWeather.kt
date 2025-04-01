package com.drdisagree.iconify.ui.fragments.xposed

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.Iconify.Companion.appContextLocale
import com.drdisagree.iconify.R
import com.drdisagree.iconify.data.common.Preferences.WEATHER_SWITCH
import com.drdisagree.iconify.data.common.Preferences.WEATHER_TRIGGER_UPDATE
import com.drdisagree.iconify.data.common.XposedConst.LOCKSCREEN_WEATHER_FONT_FILE
import com.drdisagree.iconify.data.config.RPrefs.getBoolean
import com.drdisagree.iconify.data.config.RPrefs.putBoolean
import com.drdisagree.iconify.ui.activities.MainActivity
import com.drdisagree.iconify.ui.base.WeatherPreferenceFragment
import com.drdisagree.iconify.ui.preferences.FilePickerPreference
import com.drdisagree.iconify.utils.FileUtils.getRealPath
import com.drdisagree.iconify.utils.FileUtils.launchFilePicker
import com.drdisagree.iconify.utils.FileUtils.moveToIconifyHiddenDir

class LockscreenWeather : WeatherPreferenceFragment() {

    override val title: String
        get() = getString(R.string.activity_title_lockscreen_weather)

    override val backButtonEnabled: Boolean
        get() = true

    override val layoutResource: Int
        get() = R.xml.xposed_lockscreen_weather

    override val hasMenu: Boolean
        get() = true

    private lateinit var startActivityIntent: ActivityResultLauncher<Intent?>

    override fun getMainSwitchKey(): String {
        return WEATHER_SWITCH
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        startActivityIntent = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val path = getRealPath(data)

                if (path != null &&
                    moveToIconifyHiddenDir(path, LOCKSCREEN_WEATHER_FONT_FILE.absolutePath)
                ) {
                    putBoolean(WEATHER_TRIGGER_UPDATE, !getBoolean(WEATHER_TRIGGER_UPDATE))

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
            WEATHER_SWITCH -> {
                MainActivity.showOrHidePendingActionButton(
                    activityBinding = (requireActivity() as MainActivity).binding,
                    requiresSystemUiRestart = true
                )
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)

        findPreference<FilePickerPreference>("xposed_lockscreenweatherfontpicker")?.apply {
            setOnButtonClick {
                launchFilePicker(context, "font", startActivityIntent)
            }
        }
    }
}