package com.drdisagree.iconify.ui.fragments.xposed

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Preferences
import com.drdisagree.iconify.common.Preferences.XPOSED_HOOK_CHECK
import com.drdisagree.iconify.ui.base.ControlledPreferenceFragmentCompat
import com.drdisagree.iconify.ui.preferences.HookCheckPreference

class Xposed : ControlledPreferenceFragmentCompat() {

    private var hookCheckPreference: HookCheckPreference? = null

    override val title: String
        get() = getString(R.string.navbar_xposed)

    override val backButtonEnabled: Boolean
        get() = !Preferences.isXposedOnlyMode

    override val layoutResource: Int
        get() = R.xml.xposed

    override val hasMenu: Boolean
        get() = true

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)

        findPreference<HookCheckPreference>(XPOSED_HOOK_CHECK)?.apply {
            hookCheckPreference = this

            setOnPreferenceClickListener {
                try {
                    val intent = Intent(Intent.ACTION_MAIN)
                    intent.setComponent(
                        ComponentName(
                            "org.lsposed.manager",
                            "org.lsposed.manager.ui.activities.MainActivity"
                        )
                    )
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                } catch (ignored: Exception) {
                }
                true
            }

            initializeHookCheck()
        }
    }

    override fun onResume() {
        super.onResume()

        hookCheckPreference?.isHooked = false
        hookCheckPreference?.initializeHookCheck()
    }
}
