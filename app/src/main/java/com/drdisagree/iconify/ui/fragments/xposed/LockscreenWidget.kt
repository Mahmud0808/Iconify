package com.drdisagree.iconify.ui.fragments.xposed

import android.os.Bundle
import android.text.TextUtils
import androidx.preference.Preference
import com.drdisagree.iconify.R
import com.drdisagree.iconify.data.common.Preferences.EXTRA_WIDGET_1_KEY
import com.drdisagree.iconify.data.common.Preferences.EXTRA_WIDGET_2_KEY
import com.drdisagree.iconify.data.common.Preferences.EXTRA_WIDGET_3_KEY
import com.drdisagree.iconify.data.common.Preferences.EXTRA_WIDGET_4_KEY
import com.drdisagree.iconify.data.common.Preferences.LOCKSCREEN_WIDGETS
import com.drdisagree.iconify.data.common.Preferences.LOCKSCREEN_WIDGETS_DEVICE_WIDGET
import com.drdisagree.iconify.data.common.Preferences.LOCKSCREEN_WIDGETS_ENABLED
import com.drdisagree.iconify.data.common.Preferences.LOCKSCREEN_WIDGETS_EXTRAS
import com.drdisagree.iconify.data.common.Preferences.MAIN_WIDGET_1_KEY
import com.drdisagree.iconify.data.common.Preferences.MAIN_WIDGET_2_KEY
import com.drdisagree.iconify.data.config.RPrefs
import com.drdisagree.iconify.services.WeatherScheduler
import com.drdisagree.iconify.ui.activities.MainActivity
import com.drdisagree.iconify.ui.base.ControlledPreferenceFragmentCompat
import com.drdisagree.iconify.utils.OmniJawsClient
import com.drdisagree.iconify.utils.weather.WeatherConfig
import java.util.stream.Collectors

class LockscreenWidget : ControlledPreferenceFragmentCompat() {

    private lateinit var mWeatherClient: OmniJawsClient

    private val widgetKeysMap: MutableMap<Preference, String?> = HashMap()
    private val initialWidgetKeysMap: MutableMap<Preference, String?> = HashMap()

    private lateinit var mMainWidget1: Preference
    private lateinit var mMainWidget2: Preference
    private lateinit var mExtraWidget1: Preference
    private lateinit var mExtraWidget2: Preference
    private lateinit var mExtraWidget3: Preference
    private lateinit var mExtraWidget4: Preference
    private lateinit var mDeviceInfoWidgetPref: Preference

    private var mWidgetPreferences: List<Preference>? = null

    override val title: String
        get() = getString(R.string.activity_title_lockscreen_widget)

    override val backButtonEnabled: Boolean
        get() = true

    override val layoutResource: Int
        get() = R.xml.xposed_lockscreen_widget

    override val hasMenu: Boolean
        get() = true

    override fun updateScreen(key: String?) {
        super.updateScreen(key)

        if (key == null) return

        saveInitialPreferences()

        var mainWidgetsList: List<String> = listOf(
            RPrefs.getString(MAIN_WIDGET_1_KEY, "none")!!,
            RPrefs.getString(MAIN_WIDGET_2_KEY, "none")!!
        )
        var extraWidgetsList: List<String> = listOf(
            RPrefs.getString(EXTRA_WIDGET_1_KEY, "none")!!,
            RPrefs.getString(EXTRA_WIDGET_2_KEY, "none")!!,
            RPrefs.getString(EXTRA_WIDGET_3_KEY, "none")!!,
            RPrefs.getString(EXTRA_WIDGET_4_KEY, "none")!!
        )

        mainWidgetsList = replaceEmptyWithNone(mainWidgetsList)
        extraWidgetsList = replaceEmptyWithNone(extraWidgetsList)

        val mainWidgets = TextUtils.join(",", mainWidgetsList)
        val extraWidgets = TextUtils.join(",", extraWidgetsList)

        val wasWeatherEnabled: Boolean = WeatherConfig.isEnabled(requireContext())

        RPrefs.putString(LOCKSCREEN_WIDGETS, mainWidgets)
        RPrefs.putString(LOCKSCREEN_WIDGETS_EXTRAS, extraWidgets)

        val weatherEnabled =
            mainWidgets.contains("weather") || extraWidgets.contains("weather")

        if (weatherEnabled && wasWeatherEnabled && mWeatherClient.mCachedInfo != null) {
            if (System.currentTimeMillis() - mWeatherClient.mCachedInfo!!.timeStamp > 3600000) {
                WeatherScheduler.scheduleUpdateNow(requireContext())
            }
        } else if (weatherEnabled) {
            WeatherScheduler.scheduleUpdates(requireContext())
            WeatherScheduler.scheduleUpdateNow(requireContext())
        }

        when (key) {
            LOCKSCREEN_WIDGETS_ENABLED -> {
                MainActivity.showOrHidePendingActionButton(
                    activityBinding = (requireActivity() as MainActivity).binding,
                    requiresSystemUiRestart = true
                )
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)

        mWeatherClient = OmniJawsClient(requireContext())
        mWeatherClient.queryWeather()

        mMainWidget1 = findPreference(MAIN_WIDGET_1_KEY)!!
        mMainWidget2 = findPreference(MAIN_WIDGET_2_KEY)!!
        mExtraWidget1 = findPreference(EXTRA_WIDGET_1_KEY)!!
        mExtraWidget2 = findPreference(EXTRA_WIDGET_2_KEY)!!
        mExtraWidget3 = findPreference(EXTRA_WIDGET_3_KEY)!!
        mExtraWidget4 = findPreference(EXTRA_WIDGET_4_KEY)!!
        mDeviceInfoWidgetPref = findPreference(LOCKSCREEN_WIDGETS_DEVICE_WIDGET)!!

        mWidgetPreferences = listOf(
            mMainWidget1,
            mMainWidget2,
            mExtraWidget1,
            mExtraWidget2,
            mExtraWidget3,
            mExtraWidget4,
            mDeviceInfoWidgetPref
        )
    }

    private fun replaceEmptyWithNone(inputList: List<String>): MutableList<String> {
        return inputList.stream()
            .map { s: String? -> if (TextUtils.isEmpty(s)) "none" else s }
            .collect(Collectors.toList())
    }

    private fun saveInitialPreferences() {
        initialWidgetKeysMap.clear()

        for (widgetPref in mWidgetPreferences!!) {
            initialWidgetKeysMap[widgetPref] = widgetKeysMap[widgetPref]
        }
    }
}