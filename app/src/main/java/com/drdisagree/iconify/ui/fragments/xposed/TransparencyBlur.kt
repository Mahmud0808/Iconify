package com.drdisagree.iconify.ui.fragments.xposed

import android.os.Bundle
import com.drdisagree.iconify.R
import com.drdisagree.iconify.config.RPrefs.getBoolean
import com.drdisagree.iconify.config.RPrefs.putBoolean
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
        get() = R.xml.transparency_blur

    override val hasMenu: Boolean
        get() = true

    override fun updateScreen(key: String?) {
        super.updateScreen(key)

        when (key) {
            "xposed_qstransparency" -> {
                if (getBoolean(key)) {
                    putBoolean("xposed_notiftransparency", false)
                }
            }

            "xposed_notiftransparency" -> {
                if (getBoolean(key)) {
                    putBoolean("xposed_qstransparency", false)
                }
            }

            "qsBlurSwitch" -> {
                if (getBoolean(key)) {
                    enableBlur(force = false)
                } else {
                    putBoolean("aggressiveQsBlurSwitch", false)
                    disableBlur(force = false)
                }
            }

            "aggressiveQsBlurSwitch" -> {
                if (getBoolean(key)) {
                    enableBlur(force = true)
                } else {
                    disableBlur(force = true)
                }
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)

        findPreference<SwitchPreference>("qsBlurSwitch")?.isChecked = isBlurEnabled(force = false)

        findPreference<SwitchPreference>("aggressiveQsBlurSwitch")?.isChecked =
            isBlurEnabled(force = true)
    }
}
