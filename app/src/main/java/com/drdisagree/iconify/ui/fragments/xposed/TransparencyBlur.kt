package com.drdisagree.iconify.ui.fragments.xposed

import android.os.Bundle
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Preferences.AGGRESSIVE_QSPANEL_BLUR_SWITCH
import com.drdisagree.iconify.common.Preferences.BLUR_RADIUS_VALUE
import com.drdisagree.iconify.common.Preferences.LOCKSCREEN_SHADE_SWITCH
import com.drdisagree.iconify.common.Preferences.NOTIF_TRANSPARENCY_SWITCH
import com.drdisagree.iconify.common.Preferences.QSPANEL_BLUR_SWITCH
import com.drdisagree.iconify.common.Preferences.QS_TRANSPARENCY_SWITCH
import com.drdisagree.iconify.config.RPrefs.getBoolean
import com.drdisagree.iconify.config.RPrefs.putBoolean
import com.drdisagree.iconify.ui.activities.MainActivity
import com.drdisagree.iconify.ui.base.ControlledPreferenceFragmentCompat
import com.drdisagree.iconify.ui.preferences.SwitchPreference
import com.drdisagree.iconify.utils.SystemUtil.disableBlur
import com.drdisagree.iconify.utils.SystemUtil.enableBlur
import com.drdisagree.iconify.utils.SystemUtil.isBlurEnabled

class TransparencyBlur : ControlledPreferenceFragmentCompat() {

    override val title: String
        get() = getString(R.string.activity_title_transparency_blur)

    override val backButtonEnabled: Boolean
        get() = true

    override val layoutResource: Int
        get() = R.xml.xposed_transparency_blur

    override val hasMenu: Boolean
        get() = true

    override fun updateScreen(key: String?) {
        super.updateScreen(key)

        when (key) {
            QS_TRANSPARENCY_SWITCH -> {
                if (getBoolean(key)) {
                    putBoolean(NOTIF_TRANSPARENCY_SWITCH, false)
                }
                MainActivity.showOrHidePendingActionButton(requiresSystemUiRestart = true)
            }

            NOTIF_TRANSPARENCY_SWITCH -> {
                if (getBoolean(key)) {
                    putBoolean(QS_TRANSPARENCY_SWITCH, false)
                }
                MainActivity.showOrHidePendingActionButton(requiresSystemUiRestart = true)
            }

            LOCKSCREEN_SHADE_SWITCH -> {
                MainActivity.showOrHidePendingActionButton(requiresSystemUiRestart = true)
            }

            QSPANEL_BLUR_SWITCH -> {
                if (getBoolean(key)) {
                    enableBlur(force = false)
                } else {
                    putBoolean(AGGRESSIVE_QSPANEL_BLUR_SWITCH, false)
                    disableBlur(force = false)
                }
                MainActivity.showOrHidePendingActionButton(requiresDeviceRestart = true)
            }

            AGGRESSIVE_QSPANEL_BLUR_SWITCH -> {
                if (getBoolean(key)) {
                    enableBlur(force = true)
                } else {
                    disableBlur(force = true)
                }
                MainActivity.showOrHidePendingActionButton(requiresDeviceRestart = true)
            }

            BLUR_RADIUS_VALUE -> {
                MainActivity.showOrHidePendingActionButton(requiresSystemUiRestart = true)
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)

        findPreference<SwitchPreference>(QSPANEL_BLUR_SWITCH)?.isChecked =
            isBlurEnabled(force = false)

        findPreference<SwitchPreference>(AGGRESSIVE_QSPANEL_BLUR_SWITCH)?.isChecked =
            isBlurEnabled(force = true)
    }
}
