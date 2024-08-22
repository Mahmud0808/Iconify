package com.drdisagree.iconify.config

import android.annotation.SuppressLint
import android.content.Context
import androidx.preference.Preference
import androidx.preference.PreferenceGroup
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.R
import com.drdisagree.iconify.ui.preferences.SliderPreference

object PrefsHelper {

    @JvmStatic
    val getPrefs: RPrefs
        get() = mPrefs

    private val mPrefs: RPrefs by lazy {
        RPrefs.instance
    }

    @JvmStatic
    fun isVisible(key: String?): Boolean {
        return when (key) {
            "experimentalFeatures" -> mPrefs.getBoolean(
                "iconify_easter_egg",
                false
            )

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
            return String.format("%.2f", mPrefs.getSliderFloat(key, 0f))
        }
        if (key.contains("Switch")) {
            return fragmentCompat.getString(
                if (mPrefs.getBoolean(
                        key,
                        false
                    )
                ) android.R.string.ok else android.R.string.cancel
            )
        }
        if (key.contains("List")) {
            return mPrefs.getString(key, "")
        }
        if (key.contains("EditText")) {
            return mPrefs.getString(key, "")
        }
        if (key.contains("MultiSelect")) {
            return mPrefs.getStringSet(key, emptySet()).toString()
        }

        return when (key) {
            "IconifyAppLanguage" -> {
                val currentLanguageCode =
                    listOf<String?>(*fragmentCompat.resources.getStringArray(R.array.locale_code))
                        .indexOf(
                            mPrefs.getString(
                                "IconifyAppLanguage",
                                fragmentCompat.resources.configuration.locales[0].language
                            )
                        )
                val selectedLanguageCode = if (currentLanguageCode < 0) listOf<String>(
                    *fragmentCompat.resources.getStringArray(R.array.locale_code)
                ).indexOf("en") else currentLanguageCode

                return listOf<String>(*fragmentCompat.resources.getStringArray(R.array.locale_name))[selectedLanguageCode]
            }

            else -> null
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
}
