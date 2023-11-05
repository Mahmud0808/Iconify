package com.drdisagree.iconify.utils.helper;

import static com.drdisagree.iconify.common.Preferences.APP_LANGUAGE;

import android.content.Context;
import android.content.res.Configuration;
import android.os.LocaleList;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class LocaleHelper {

    public static Context setLocale(Context context) {
        String localeCode = Prefs.getString(APP_LANGUAGE, "en-US");

        LocaleList locales = context.getResources().getConfiguration().getLocales();
        List<String> locale_codes = Arrays.asList(context.getResources().getStringArray(R.array.locale_code));
        for (int i = 0; i < locales.size(); i++) {
            String languageCode = locales.get(i).getLanguage();
            String countryCode = locales.get(i).getCountry();
            String languageFormat = languageCode + "-" + countryCode;

            if (locale_codes.contains(languageFormat)) {
                localeCode = languageFormat;
                break;
            }
        }

        Locale locale = Locale.forLanguageTag(localeCode);

        Configuration configuration = context.getResources().getConfiguration();
        configuration.setLocale(locale);
        configuration.setLayoutDirection(locale);

        LocaleList localeList = new LocaleList(locale);
        LocaleList.setDefault(localeList);
        configuration.setLocales(localeList);

        return context.createConfigurationContext(configuration);
    }
}