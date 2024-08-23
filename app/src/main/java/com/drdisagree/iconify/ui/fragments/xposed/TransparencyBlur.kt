package com.drdisagree.iconify.ui.fragments.xposed

import com.drdisagree.iconify.R
import com.drdisagree.iconify.ui.base.ControlledPreferenceFragmentCompat
import com.drdisagree.iconify.utils.AppUtil.restartApplication

class TransparencyBlur : ControlledPreferenceFragmentCompat() {

    override val title: String
        get() = getString(R.string.activity_title_transparency_blur)

    override val backButtonEnabled: Boolean
        get() = true

    override val layoutResource: Int
        get() = R.xml.transparency_blur

    override val hasMenu: Boolean
        get() = true

    override fun updateScreen(key: String?) {
        super.updateScreen(key)

        when (key) {
            "IconifyAppLanguage" -> {
                restartApplication(requireActivity())
            }
        }
    }
}
