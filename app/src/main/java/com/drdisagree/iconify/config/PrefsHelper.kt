package com.drdisagree.iconify.config

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.preference.Preference
import androidx.preference.PreferenceGroup
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Preferences.AGGRESSIVE_QSPANEL_BLUR_SWITCH
import com.drdisagree.iconify.common.Preferences.APP_LANGUAGE
import com.drdisagree.iconify.common.Preferences.AUTO_UPDATE
import com.drdisagree.iconify.common.Preferences.BLUR_RADIUS_VALUE
import com.drdisagree.iconify.common.Preferences.CUSTOM_QS_MARGIN
import com.drdisagree.iconify.common.Preferences.EASTER_EGG
import com.drdisagree.iconify.common.Preferences.EXPERIMENTAL_FEATURES
import com.drdisagree.iconify.common.Preferences.FIXED_STATUS_ICONS_SIDEMARGIN
import com.drdisagree.iconify.common.Preferences.FIXED_STATUS_ICONS_SWITCH
import com.drdisagree.iconify.common.Preferences.FIXED_STATUS_ICONS_TOPMARGIN
import com.drdisagree.iconify.common.Preferences.HIDE_QSLABEL_SWITCH
import com.drdisagree.iconify.common.Preferences.LOCKSCREEN_SHADE_SWITCH
import com.drdisagree.iconify.common.Preferences.NEW_UPDATE_FOUND
import com.drdisagree.iconify.common.Preferences.NOTIF_TRANSPARENCY_SWITCH
import com.drdisagree.iconify.common.Preferences.QQS_TOPMARGIN
import com.drdisagree.iconify.common.Preferences.QSALPHA_LEVEL
import com.drdisagree.iconify.common.Preferences.QSPANEL_BLUR_SWITCH
import com.drdisagree.iconify.common.Preferences.QSPANEL_STATUSICONSBG_SWITCH
import com.drdisagree.iconify.common.Preferences.QS_TOPMARGIN
import com.drdisagree.iconify.common.Preferences.QS_TRANSPARENCY_SWITCH
import com.drdisagree.iconify.common.Preferences.SB_CLOCK_SIZE
import com.drdisagree.iconify.common.Preferences.SB_CLOCK_SIZE_SWITCH
import com.drdisagree.iconify.common.Preferences.SHOW_HOME_CARD
import com.drdisagree.iconify.common.Preferences.UPDATE_OVER_WIFI
import com.drdisagree.iconify.common.Preferences.VERTICAL_QSTILE_SWITCH
import com.drdisagree.iconify.common.Preferences.XPOSED_HOOK_CHECK
import com.drdisagree.iconify.common.Resources.shouldShowRebootDialog
import com.drdisagree.iconify.config.RPrefs.getBoolean
import com.drdisagree.iconify.ui.preferences.SliderPreference

object PrefsHelper {

    fun isVisible(key: String?): Boolean {
        return when (key) {
            UPDATE_OVER_WIFI -> getBoolean(AUTO_UPDATE, true)

            EXPERIMENTAL_FEATURES -> getBoolean(EASTER_EGG)

            "iconifyHomeCard" -> getBoolean(SHOW_HOME_CARD, true)

            "rebootReminder" -> shouldShowRebootDialog()

            "newUpdate" -> getBoolean(NEW_UPDATE_FOUND)

            XPOSED_HOOK_CHECK -> !getBoolean(key)

            LOCKSCREEN_SHADE_SWITCH,
            QSALPHA_LEVEL -> getBoolean(QS_TRANSPARENCY_SWITCH) ||
                    getBoolean(NOTIF_TRANSPARENCY_SWITCH)

            AGGRESSIVE_QSPANEL_BLUR_SWITCH -> getBoolean(QSPANEL_BLUR_SWITCH)

            HIDE_QSLABEL_SWITCH -> getBoolean(VERTICAL_QSTILE_SWITCH)

            QQS_TOPMARGIN,
            QS_TOPMARGIN -> getBoolean(CUSTOM_QS_MARGIN)

            SB_CLOCK_SIZE -> getBoolean(SB_CLOCK_SIZE_SWITCH)

            FIXED_STATUS_ICONS_TOPMARGIN -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                getBoolean(QSPANEL_STATUSICONSBG_SWITCH) ||
                        getBoolean(FIXED_STATUS_ICONS_SWITCH)
            } else {
                getBoolean(FIXED_STATUS_ICONS_SWITCH)
            }

            FIXED_STATUS_ICONS_SIDEMARGIN -> getBoolean(FIXED_STATUS_ICONS_SWITCH)

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
        if (key.endsWith("Slider")) {
            return String.format("%.2f", RPrefs.getSliderFloat(key, 0f))
        }
        if (key.endsWith("List")) {
            return RPrefs.getString(key, "")
        }
        if (key.endsWith("EditText")) {
            return RPrefs.getString(key, "")
        }
        if (key.endsWith("MultiSelect")) {
            return RPrefs.getStringSet(key, emptySet()).toString()
        }

        return when (key) {
            APP_LANGUAGE -> {
                val currentLanguageCode =
                    listOf<String?>(*fragmentCompat.resources.getStringArray(R.array.locale_code))
                        .indexOf(
                            RPrefs.getString(
                                APP_LANGUAGE,
                                fragmentCompat.resources.configuration.locales[0].language
                            )
                        )
                val selectedLanguageCode = if (currentLanguageCode < 0) listOf<String>(
                    *fragmentCompat.resources.getStringArray(R.array.locale_code)
                ).indexOf("en-US") else currentLanguageCode

                return listOf<String>(*fragmentCompat.resources.getStringArray(R.array.locale_name))[selectedLanguageCode]
            }

            "checkForUpdatePref" -> BuildConfig.VERSION_NAME

            QSALPHA_LEVEL -> "${RPrefs.getSliderInt(key, 60)}%"

            BLUR_RADIUS_VALUE -> "${RPrefs.getSliderInt(key, 23)}px"

            QQS_TOPMARGIN,
            QS_TOPMARGIN -> "${RPrefs.getSliderInt(key, 100)}dp"

            SB_CLOCK_SIZE -> "${RPrefs.getSliderInt(key, 14)}px"

            FIXED_STATUS_ICONS_TOPMARGIN -> "${RPrefs.getSliderInt(key, 8)}dp"

            FIXED_STATUS_ICONS_SIDEMARGIN -> "${RPrefs.getSliderInt(key, 0)}dp"

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
