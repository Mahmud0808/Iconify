package com.drdisagree.iconify.ui.fragments.xposed

import android.os.Build
import android.os.Bundle
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Preferences.FIXED_STATUS_ICONS_SWITCH
import com.drdisagree.iconify.common.Preferences.FIXED_STATUS_ICONS_TOPMARGIN
import com.drdisagree.iconify.common.Preferences.HIDE_LOCKSCREEN_LOCK_ICON
import com.drdisagree.iconify.common.Preferences.HIDE_STATUS_ICONS_SWITCH
import com.drdisagree.iconify.common.Preferences.QSPANEL_HIDE_CARRIER
import com.drdisagree.iconify.ui.activities.MainActivity
import com.drdisagree.iconify.ui.base.ControlledPreferenceFragmentCompat
import com.drdisagree.iconify.ui.preferences.SliderPreference

class Others : ControlledPreferenceFragmentCompat() {

    override val title: String
        get() = getString(R.string.activity_title_xposed_others)

    override val backButtonEnabled: Boolean
        get() = true

    override val layoutResource: Int
        get() = R.xml.xposed_others

    override val hasMenu: Boolean
        get() = true

    override fun updateScreen(key: String?) {
        super.updateScreen(key)

        when (key) {
            QSPANEL_HIDE_CARRIER,
            HIDE_STATUS_ICONS_SWITCH,
            HIDE_LOCKSCREEN_LOCK_ICON,
            FIXED_STATUS_ICONS_SWITCH -> {
                MainActivity.showOrHidePendingActionButton(requiresSystemUiRestart = true)
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            findPreference<SliderPreference>(FIXED_STATUS_ICONS_TOPMARGIN)?.setMax(250f)
        }
    }
}
