package com.drdisagree.iconify.utils.helper;

import static com.drdisagree.iconify.common.Preferences.APP_LANGUAGE;
import static com.drdisagree.iconify.common.Resources.SharedPref;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.LocaleList;

import java.util.Locale;

public class LocaleHelper {

    public static Context setLocale(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SharedPref, Context.MODE_PRIVATE);
        String localeCode = prefs.getString(APP_LANGUAGE, "");

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