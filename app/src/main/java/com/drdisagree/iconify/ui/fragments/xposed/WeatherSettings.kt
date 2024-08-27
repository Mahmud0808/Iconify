package com.drdisagree.iconify.ui.fragments.xposed

import com.drdisagree.iconify.Iconify.Companion.appContextLocale
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Preferences.WEATHER_OWM_KEY
import com.drdisagree.iconify.common.Preferences.WEATHER_PROVIDER
import com.drdisagree.iconify.config.RPrefs
import com.drdisagree.iconify.ui.base.WeatherPreferenceFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class WeatherSettings : WeatherPreferenceFragment() {

    override fun getMainSwitchKey(): String {
        return ""
    }

    override val title: String
        get() = getString(R.string.activity_title_xposed_weather_settings)

    override val backButtonEnabled: Boolean
        get() = true

    override val layoutResource: Int
        get() = R.xml.xposed_weather_settings

    override val hasMenu: Boolean
        get() = true

    private fun showOwnKeyDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(appContextLocale.getString(R.string.weather_provider_owm_key_title))
            .setMessage(appContextLocale.getString(R.string.weather_provider_owm_key_message))
            .setCancelable(false)
            .setPositiveButton(appContextLocale.getString(R.string.understood), null)
            .create()
            .show()
    }

    override fun updateScreen(key: String?) {
        super.updateScreen(key)

        when (key) {
            WEATHER_PROVIDER -> {
                if (RPrefs.getString(WEATHER_PROVIDER) == "1" && RPrefs.getString(WEATHER_OWM_KEY)
                        .isNullOrEmpty()
                ) {
                    showOwnKeyDialog()
                }
            }
        }
    }
}
