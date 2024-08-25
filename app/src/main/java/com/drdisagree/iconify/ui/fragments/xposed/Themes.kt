package com.drdisagree.iconify.ui.fragments.xposed

import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Preferences.BLACK_QSPANEL
import com.drdisagree.iconify.common.Preferences.FIX_NOTIFICATION_COLOR
import com.drdisagree.iconify.common.Preferences.FIX_NOTIFICATION_FOOTER_BUTTON_COLOR
import com.drdisagree.iconify.common.Preferences.FIX_QS_TILE_COLOR
import com.drdisagree.iconify.common.Preferences.FLUID_NOTIF_TRANSPARENCY
import com.drdisagree.iconify.common.Preferences.FLUID_POWERMENU_TRANSPARENCY
import com.drdisagree.iconify.common.Preferences.FLUID_QSPANEL
import com.drdisagree.iconify.common.Preferences.LIGHT_QSPANEL
import com.drdisagree.iconify.ui.activities.MainActivity
import com.drdisagree.iconify.ui.base.ControlledPreferenceFragmentCompat

class Themes : ControlledPreferenceFragmentCompat() {

    override val title: String
        get() = getString(R.string.activity_title_themes)

    override val backButtonEnabled: Boolean
        get() = true

    override val layoutResource: Int
        get() = R.xml.xposed_themes

    override val hasMenu: Boolean
        get() = true

    override fun updateScreen(key: String?) {
        super.updateScreen(key)

        when (key) {
            LIGHT_QSPANEL,
            BLACK_QSPANEL,
            FLUID_QSPANEL,
            FLUID_NOTIF_TRANSPARENCY,
            FLUID_POWERMENU_TRANSPARENCY,
            FIX_QS_TILE_COLOR,
            FIX_NOTIFICATION_COLOR,
            FIX_NOTIFICATION_FOOTER_BUTTON_COLOR -> {
                MainActivity.showOrHidePendingActionButton(requiresSystemUiRestart = true)
            }
        }
    }
}
