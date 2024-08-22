package com.drdisagree.iconify.ui.fragments.tweaks

import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Preferences
import com.drdisagree.iconify.ui.base.ControlledPreferenceFragmentCompat

class Tweaks : ControlledPreferenceFragmentCompat() {

    override val title: String
        get() = getString(R.string.navbar_tweaks)

    override val backButtonEnabled: Boolean
        get() = !Preferences.isXposedOnlyMode

    override val layoutResource: Int
        get() = R.xml.tweaks

    override val hasMenu: Boolean
        get() = true

    override val scopes: Array<String>?
        get() = null
}
