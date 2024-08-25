package com.drdisagree.iconify.ui.fragments.xposed

import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Preferences.COLORED_STATUSBAR_ICON
import com.drdisagree.iconify.common.Preferences.HIDE_LOCKSCREEN_CARRIER
import com.drdisagree.iconify.common.Preferences.HIDE_LOCKSCREEN_STATUSBAR
import com.drdisagree.iconify.ui.activities.MainActivity
import com.drdisagree.iconify.ui.base.ControlledPreferenceFragmentCompat

class Statusbar : ControlledPreferenceFragmentCompat() {

    override val title: String
        get() = getString(R.string.activity_title_statusbar)

    override val backButtonEnabled: Boolean
        get() = true

    override val layoutResource: Int
        get() = R.xml.xposed_statusbar

    override val hasMenu: Boolean
        get() = true

    override fun updateScreen(key: String?) {
        super.updateScreen(key)

        when (key) {
            COLORED_STATUSBAR_ICON,
            HIDE_LOCKSCREEN_CARRIER,
            HIDE_LOCKSCREEN_STATUSBAR -> {
                MainActivity.showOrHidePendingActionButton(requiresSystemUiRestart = true)
            }
        }
    }
}
