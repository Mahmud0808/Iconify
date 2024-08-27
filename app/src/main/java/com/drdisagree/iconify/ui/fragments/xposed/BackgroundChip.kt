package com.drdisagree.iconify.ui.fragments.xposed

import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Preferences.CHIP_STATUSBAR_CLOCK_SWITCH
import com.drdisagree.iconify.common.Preferences.CHIP_STATUS_ICONS_SWITCH
import com.drdisagree.iconify.config.RPrefs.getBoolean
import com.drdisagree.iconify.ui.activities.MainActivity
import com.drdisagree.iconify.ui.base.ControlledPreferenceFragmentCompat

class BackgroundChip : ControlledPreferenceFragmentCompat() {

    override val title: String
        get() = getString(R.string.activity_title_background_chip)

    override val backButtonEnabled: Boolean
        get() = true

    override val layoutResource: Int
        get() = R.xml.xposed_background_chip

    override val hasMenu: Boolean
        get() = true

    override fun updateScreen(key: String?) {
        super.updateScreen(key)

        when (key) {
            CHIP_STATUSBAR_CLOCK_SWITCH -> {
                if (!getBoolean(key)) {
                    MainActivity.showOrHidePendingActionButton(requiresSystemUiRestart = true)
                }
            }

            CHIP_STATUS_ICONS_SWITCH -> {
                MainActivity.showOrHidePendingActionButton(requiresSystemUiRestart = true)
            }
        }
    }
}
