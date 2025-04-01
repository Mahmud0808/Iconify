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
import com.drdisagree.iconify.data.common.Preferences.STATUSBAR_LOGO_CUSTOM
import com.drdisagree.iconify.data.common.Preferences.STATUSBAR_LOGO_STYLE
import com.drdisagree.iconify.data.common.XposedConst.STATUSBAR_LOGO_FILE
import com.drdisagree.iconify.data.config.RPrefs
import com.drdisagree.iconify.data.config.RPrefs.getBoolean
import com.drdisagree.iconify.data.config.RPrefs.putBoolean
import com.drdisagree.iconify.ui.base.ControlledPreferenceFragmentCompat
import com.drdisagree.iconify.ui.preferences.BottomSheetListPreference
import com.drdisagree.iconify.ui.utils.ViewHelper.getStatusbarLogoDrawables
import com.drdisagree.iconify.utils.FileUtils.getRealPath
import com.drdisagree.iconify.utils.FileUtils.launchFilePicker
import com.drdisagree.iconify.utils.FileUtils.moveToIconifyHiddenDir

class StatusbarLogo : ControlledPreferenceFragmentCompat() {

    override val title: String
        get() = getString(R.string.status_bar_logo_title)

    override val backButtonEnabled: Boolean
        get() = true

    override val layoutResource: Int
        get() = R.xml.xposed_statusbar_logo

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
                    moveToIconifyHiddenDir(path, STATUSBAR_LOGO_FILE.absolutePath)
                ) {
                    putBoolean(STATUSBAR_LOGO_CUSTOM, !getBoolean(STATUSBAR_LOGO_CUSTOM))

                    Toast.makeText(
                        appContext,
                        appContextLocale.resources.getString(R.string.toast_applied),
                        Toast.LENGTH_SHORT
                    ).show()

                    findPreference<BottomSheetListPreference>(STATUSBAR_LOGO_STYLE)?.apply {
                        setDrawables(getStatusbarLogoDrawables(requireContext()))
                        getAdapter()!!.notifyItemChanged(
                            listOf<String>(
                                *resources.getStringArray(R.array.status_bar_logo_style_entries)
                            ).indexOf(resources.getString(R.string.status_bar_logo_style_custom))
                        )
                    }
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

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)

        findPreference<BottomSheetListPreference>(STATUSBAR_LOGO_STYLE)?.apply {
            createDefaultAdapter(
                getStatusbarLogoDrawables(requireContext()),
                object : BottomSheetListPreference.OnItemClickListener {
                    override fun onItemClick(position: Int) {
                        if (listOf<String>(
                                *resources.getStringArray(R.array.status_bar_logo_style_entries)
                            )[RPrefs.getString(key, "0")!!.toInt()]
                            == resources.getString(R.string.status_bar_logo_style_custom)
                        ) {
                            launchFilePicker(appContext, "image", startActivityIntent)
                        }
                    }
                }
            )
        }
    }
}
