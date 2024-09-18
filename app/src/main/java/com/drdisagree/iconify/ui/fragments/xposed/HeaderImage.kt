package com.drdisagree.iconify.ui.fragments.xposed

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.Iconify.Companion.appContextLocale
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Preferences.HEADER_IMAGE_SWITCH
import com.drdisagree.iconify.common.Resources.HEADER_IMAGE_DIR
import com.drdisagree.iconify.config.RPrefs.putBoolean
import com.drdisagree.iconify.ui.base.ControlledPreferenceFragmentCompat
import com.drdisagree.iconify.ui.preferences.FilePickerPreference
import com.drdisagree.iconify.utils.FileUtils.getRealPath
import com.drdisagree.iconify.utils.FileUtils.launchFilePicker
import com.drdisagree.iconify.utils.FileUtils.moveToIconifyHiddenDir

class HeaderImage : ControlledPreferenceFragmentCompat() {

    override val title: String
        get() = getString(R.string.activity_title_header_image)

    override val backButtonEnabled: Boolean
        get() = true

    override val layoutResource: Int
        get() = R.xml.xposed_header_image

    override val hasMenu: Boolean
        get() = true

    private var startActivityIntent = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val path = getRealPath(data)

            if (path != null && moveToIconifyHiddenDir(path, HEADER_IMAGE_DIR)) {
                putBoolean(HEADER_IMAGE_SWITCH, false)
                putBoolean(HEADER_IMAGE_SWITCH, true)

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

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)

        findPreference<FilePickerPreference>("xposed_headerimagepicker")?.apply {
            setOnButtonClick {
                launchFilePicker(context, "image", startActivityIntent)
            }
        }
    }
}
