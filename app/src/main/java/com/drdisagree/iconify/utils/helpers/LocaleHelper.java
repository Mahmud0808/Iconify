package com.drdisagree.iconify.utils.helpers;

import static com.drdisagree.iconify.common.Preferences.APP_LANGUAGE;

import android.content.Context;
import android.content.res.Configuration;
import android.os.LocaleList;

import com.drdisagree.iconify.config.Prefs;

import java.util.Locale;

public class LocaleHelper {

    public static Context setLocale(Context context) {
        String localeCode = Prefs.getString(APP_LANGUAGE, "");

        if (!localeCode.isEmpty()) {
            Locale locale = Locale.forLanguageTag(localeCode);

            Configuration configuration = context.getResources().getConfiguration();
            configuration.setLocale(locale);
            configuration.setLayoutDirection(locale);

            LocaleList localeList = new LocaleList(locale);
            LocaleList.setDefault(localeList);
            configuration.setLocales(localeList);

            context = context.createConfigurationContext(configuration);
        }

        return context;
    }
}