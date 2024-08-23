package com.drdisagree.iconify.config

import android.annotation.SuppressLint
import android.content.Context
import androidx.preference.Preference
import androidx.preference.PreferenceGroup
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Resources.shouldShowRebootDialog
import com.drdisagree.iconify.config.RPrefs.getBoolean
import com.drdisagree.iconify.ui.preferences.SliderPreference

object PrefsHelper {

    fun isVisible(key: String?): Boolean {
        return when (key) {
            "IconifyUpdateOverWifi" -> getBoolean("IconifyAutoUpdate", true)

            "experimentalFeatures" -> getBoolean("iconify_easter_egg")

            "iconifyHomeCard" -> getBoolean("IconifyShowHomeCard", true)

            "rebootReminder" -> shouldShowRebootDialog()

            "newUpdate" -> getBoolean("newUpdateFound")

            "xposedHookCheck" -> !getBoolean(key)

            "xposed_lockscreen_shade",
            "xposed_qsalpha" -> getBoolean("xposed_qstransparency") ||
                    getBoolean("xposed_notiftransparency")

            "aggressiveQsBlurSwitch" -> getBoolean("qsBlurSwitch")

            else -> true
        }
    }

    fun isEnabled(key: String): Boolean {
        return when (key) {
            else -> true
        }
    }

    @SuppressLint("DefaultLocale")
    fun getSummary(fragmentCompat: Context, key: String): String? {
        if (key.contains("Slider")) {
            return String.format("%.2f", RPrefs.getSliderFloat(key, 0f))
        }
        if (key.contains("Switch")) {
            return fragmentCompat.getString(
                if (getBoolean(key, false)) {
                    android.R.string.ok
                } else {
                    android.R.string.cancel
                }
            )
        }
        if (key.contains("List")) {
            return RPrefs.getString(key, "")
        }
        if (key.contains("EditText")) {
            return RPrefs.getString(key, "")
        }
        if (key.contains("MultiSelect")) {
            return RPrefs.getStringSet(key, emptySet()).toString()
        }

        return when (key) {
            "IconifyAppLanguage" -> {
                val currentLanguageCode =
                    listOf<String?>(*fragmentCompat.resources.getStringArray(R.array.locale_code))
                        .indexOf(
                            RPrefs.getString(
                                "IconifyAppLanguage",
                                fragmentCompat.resources.configuration.locales[0].language
                            )
                        )
                val selectedLanguageCode = if (currentLanguageCode < 0) listOf<String>(
                    *fragmentCompat.resources.getStringArray(R.array.locale_code)
                ).indexOf("en-US") else currentLanguageCode

                return listOf<String>(*fragmentCompat.resources.getStringArray(R.array.locale_name))[selectedLanguageCode]
            }

            "checkForUpdatePref" -> BuildConfig.VERSION_NAME

            "xposed_qsalpha" -> "${RPrefs.getSliderInt(key, 60)}%"

            "xposed_blurradiusvalue" -> "${RPrefs.getSliderInt(key, 23)}px"

            else -> null
        }
    }

    fun setupAllPreferences(group: PreferenceGroup) {
        var i = 0
        while (true) {
            try {
                val thisPreference = group.getPreference(i)

                setupPreference(thisPreference)

                if (thisPreference is PreferenceGroup) {
                    setupAllPreferences(thisPreference)
                }
            } catch (ignored: Throwable) {
                break
            }
            i++
        }
    }

    private fun setupPreference(preference: Preference) {
        try {
            val key = preference.key

            preference.isVisible = isVisible(key)
            preference.isEnabled = isEnabled(key)

            getSummary(preference.context, key)?.let {
                preference.summary = it
            }

            if (preference is SliderPreference) {
                preference.slider.setLabelFormatter { value: Float ->
                    if (value == preference.defaultValue[0]) return@setLabelFormatter appContext.getString(
                        R.string.opt_default
                    )
                    else return@setLabelFormatter Math.round(value).toString()
                }
            }
        } catch (ignored: Throwable) {
        }
    }
}
