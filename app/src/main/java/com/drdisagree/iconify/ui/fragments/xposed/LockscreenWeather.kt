package com.drdisagree.iconify.ui.fragments.xposed

import com.drdisagree.iconify.Iconify.Companion.appContextLocale
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Preferences.WEATHER_OWM_KEY
import com.drdisagree.iconify.common.Preferences.WEATHER_PROVIDER
import com.drdisagree.iconify.common.Preferences.WEATHER_SWITCH
import com.drdisagree.iconify.common.Preferences.WEATHER_YANDEX_KEY
import com.drdisagree.iconify.config.RPrefs
import com.drdisagree.iconify.ui.activities.MainActivity
import com.drdisagree.iconify.ui.base.WeatherPreferenceFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class LockscreenWeather : WeatherPreferenceFragment() {

    override val title: String
        get() = getString(R.string.activity_title_lockscreen_weather)

    override val backButtonEnabled: Boolean
        get() = true

    override val layoutResource: Int
        get() = R.xml.xposed_lockscreen_weather

    override val hasMenu: Boolean
        get() = true

    override fun getMainSwitchKey(): String {
        return WEATHER_SWITCH
    }

    override fun updateScreen(key: String?) {
        super.updateScreen(key)

        when (key) {
            WEATHER_SWITCH -> {
                MainActivity.showOrHidePendingActionButton(
                    activityBinding = (requireActivity() as MainActivity).binding,
                    requiresSystemUiRestart = true
                )
            }
        }
    }
}