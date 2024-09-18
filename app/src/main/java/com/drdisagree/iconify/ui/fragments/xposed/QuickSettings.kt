package com.drdisagree.iconify.ui.fragments.xposed

import android.os.Bundle
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Preferences.CUSTOM_QS_MARGIN
import com.drdisagree.iconify.common.Preferences.HIDE_QSLABEL_SWITCH
import com.drdisagree.iconify.common.Preferences.HIDE_QS_ON_LOCKSCREEN
import com.drdisagree.iconify.common.Preferences.HIDE_QS_SILENT_TEXT
import com.drdisagree.iconify.common.Preferences.QQS_TOPMARGIN
import com.drdisagree.iconify.common.Preferences.QS_TEXT_ALWAYS_WHITE
import com.drdisagree.iconify.common.Preferences.QS_TEXT_FOLLOW_ACCENT
import com.drdisagree.iconify.common.Preferences.QS_TOPMARGIN
import com.drdisagree.iconify.common.Preferences.VERTICAL_QSTILE_SWITCH
import com.drdisagree.iconify.config.RPrefs.getBoolean
import com.drdisagree.iconify.config.RPrefs.putBoolean
import com.drdisagree.iconify.ui.activities.MainActivity
import com.drdisagree.iconify.ui.base.ControlledPreferenceFragmentCompat
import com.drdisagree.iconify.ui.preferences.SwitchPreference

class QuickSettings : ControlledPreferenceFragmentCompat() {

    private var alwaysWhitePreference: SwitchPreference? = null
    private var followAccentPreference: SwitchPreference? = null

    override val title: String
        get() = getString(R.string.activity_title_quick_settings)

    override val backButtonEnabled: Boolean
        get() = true

    override val layoutResource: Int
        get() = R.xml.xposed_quick_settings

    override val hasMenu: Boolean
        get() = true

    override fun updateScreen(key: String?) {
        super.updateScreen(key)

        when (key) {
            VERTICAL_QSTILE_SWITCH,
            HIDE_QSLABEL_SWITCH,
            CUSTOM_QS_MARGIN,
            QQS_TOPMARGIN,
            QS_TOPMARGIN,
            HIDE_QS_ON_LOCKSCREEN,
            HIDE_QS_SILENT_TEXT -> {
                MainActivity.showOrHidePendingActionButton(
                    activityBinding = (requireActivity() as MainActivity).binding,
                    requiresSystemUiRestart = true
                )
            }

            QS_TEXT_ALWAYS_WHITE -> {
                if (getBoolean(key)) {
                    putBoolean(QS_TEXT_FOLLOW_ACCENT, false)
                    followAccentPreference?.isChecked = false
                }
                MainActivity.showOrHidePendingActionButton(
                    activityBinding = (requireActivity() as MainActivity).binding,
                    requiresSystemUiRestart = true
                )
            }

            QS_TEXT_FOLLOW_ACCENT -> {
                if (getBoolean(key)) {
                    putBoolean(QS_TEXT_ALWAYS_WHITE, false)
                    alwaysWhitePreference?.isChecked = false
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

        alwaysWhitePreference = findPreference(QS_TEXT_ALWAYS_WHITE)
        followAccentPreference = findPreference(QS_TEXT_FOLLOW_ACCENT)
    }
}
