package com.drdisagree.iconify.helpers

import android.content.Context
import android.os.LocaleList
import com.drdisagree.iconify.R
import com.drdisagree.iconify.data.common.Preferences.APP_LANGUAGE
import com.drdisagree.iconify.data.config.RPrefs.getString
import com.drdisagree.iconify.data.config.RPrefs.putString
import java.util.Locale

object LocaleHelper {

    fun setLocale(context: Context): Context {
        var localeCode = getString(APP_LANGUAGE)

        if (localeCode.isNullOrEmpty()) {
            val locales = context.resources.configuration.locales
            val localeCodes = listOf(*context.resources.getStringArray(R.array.locale_code))

            for (i in 0 until locales.size()) {
                val languageCode = locales[i].language
                val countryCode = locales[i].country
                val languageFormat = "$languageCode-$countryCode"

                if (localeCodes.contains(languageFormat)) {
                    localeCode = languageFormat
                    break
                }
            }

            if (localeCode.isNullOrEmpty()) {
                localeCode = "en-US"
            }

            putString(APP_LANGUAGE, localeCode)
        }

        val locale = Locale.forLanguageTag(localeCode)
        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        val localeList = LocaleList(locale)
        LocaleList.setDefault(localeList)
        configuration.setLocales(localeList)

        return context.createConfigurationContext(configuration)
    }
}