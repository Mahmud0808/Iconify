package com.drdisagree.iconify.ui.fragments.xposed

import com.drdisagree.iconify.R
import com.drdisagree.iconify.data.common.Preferences.COLORED_NOTIFICATION_ALTERNATIVE_SWITCH
import com.drdisagree.iconify.data.common.Preferences.COLORED_NOTIFICATION_ICON_SWITCH
import com.drdisagree.iconify.data.common.Preferences.COLORED_NOTIFICATION_VIEW_SWITCH
import com.drdisagree.iconify.data.common.Preferences.CUSTOM_QS_TEXT_COLOR
import com.drdisagree.iconify.data.common.Preferences.FIX_NOTIFICATION_COLOR
import com.drdisagree.iconify.data.common.Preferences.FIX_NOTIFICATION_FOOTER_BUTTON_COLOR
import com.drdisagree.iconify.data.common.Preferences.FIX_QS_TILE_COLOR
import com.drdisagree.iconify.data.common.Preferences.HIDE_QSLABEL_SWITCH
import com.drdisagree.iconify.data.common.Preferences.HIDE_QS_SILENT_TEXT
import com.drdisagree.iconify.data.common.Preferences.HIDE_STATUS_ICONS_SWITCH
import com.drdisagree.iconify.data.common.Preferences.NOTIFICATION_HEADSUP_BLUR
import com.drdisagree.iconify.data.common.Preferences.NOTIFICATION_HEADSUP_BLUR_RADIUS
import com.drdisagree.iconify.data.common.Preferences.NOTIFICATION_HEADSUP_TRANSPARENCY
import com.drdisagree.iconify.data.common.Preferences.QSPANEL_HIDE_CARRIER
import com.drdisagree.iconify.data.common.Preferences.SELECTED_QS_TEXT_COLOR
import com.drdisagree.iconify.data.common.Preferences.VERTICAL_QSTILE_SWITCH
import com.drdisagree.iconify.ui.activities.MainActivity
import com.drdisagree.iconify.ui.base.ControlledPreferenceFragmentCompat

class QuickSettings : ControlledPreferenceFragmentCompat() {

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
            CUSTOM_QS_TEXT_COLOR,
            SELECTED_QS_TEXT_COLOR,
            HIDE_QSLABEL_SWITCH,
            COLORED_NOTIFICATION_ICON_SWITCH,
            COLORED_NOTIFICATION_VIEW_SWITCH,
            COLORED_NOTIFICATION_ALTERNATIVE_SWITCH,
            NOTIFICATION_HEADSUP_BLUR,
            NOTIFICATION_HEADSUP_BLUR_RADIUS,
            NOTIFICATION_HEADSUP_TRANSPARENCY,
            HIDE_QS_SILENT_TEXT,
            QSPANEL_HIDE_CARRIER,
            HIDE_STATUS_ICONS_SWITCH,
            FIX_QS_TILE_COLOR,
            FIX_NOTIFICATION_COLOR,
            FIX_NOTIFICATION_FOOTER_BUTTON_COLOR -> {
                MainActivity.showOrHidePendingActionButton(
                    activityBinding = (requireActivity() as MainActivity).binding,
                    requiresSystemUiRestart = true
                )
            }
        }
    }
}
