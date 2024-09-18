package com.drdisagree.iconify.ui.fragments.xposed

import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Preferences.OP_QS_HEADER_EXPANSION_Y
import com.drdisagree.iconify.common.Preferences.OP_QS_HEADER_HIDE_STOCK_MEDIA
import com.drdisagree.iconify.common.Preferences.OP_QS_HEADER_SWITCH
import com.drdisagree.iconify.common.Preferences.OP_QS_HEADER_TOP_MARGIN
import com.drdisagree.iconify.ui.activities.MainActivity
import com.drdisagree.iconify.ui.base.ControlledPreferenceFragmentCompat

class OpQsHeader : ControlledPreferenceFragmentCompat() {

    override val title: String
        get() = getString(R.string.activity_title_op_qs_header)

    override val backButtonEnabled: Boolean
        get() = true

    override val layoutResource: Int
        get() = R.xml.xposed_op_qs_header

    override val hasMenu: Boolean
        get() = true

    override fun updateScreen(key: String?) {
        super.updateScreen(key)

        when (key) {
            OP_QS_HEADER_SWITCH,
            OP_QS_HEADER_HIDE_STOCK_MEDIA,
            OP_QS_HEADER_TOP_MARGIN,
            OP_QS_HEADER_EXPANSION_Y -> {
                MainActivity.showOrHidePendingActionButton(
                    activityBinding = (requireActivity() as MainActivity).binding,
                    requiresSystemUiRestart = true
                )
            }
        }
    }
}
