package com.drdisagree.iconify.utils.helper

import android.content.Context
import android.os.LocaleList
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Preferences.APP_LANGUAGE
import com.drdisagree.iconify.config.Prefs.getString
import java.util.Arrays
import java.util.Locale

object LocaleHelper {

    @JvmStatic
    fun setLocale(context: Context): Context {
        var localeCode = getString(APP_LANGUAGE, "")

        if (localeCode!!.isEmpty()) {
            val locales = context.resources.configuration.getLocales()
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

            if (localeCode!!.isEmpty()) {
                localeCode = "en-US"
            }
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