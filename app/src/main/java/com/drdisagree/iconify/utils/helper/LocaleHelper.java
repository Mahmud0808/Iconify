package com.drdisagree.iconify.utils.helper;

import static com.drdisagree.iconify.common.Preferences.APP_LANGUAGE;
import static com.drdisagree.iconify.common.Resources.SharedPref;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.LocaleList;
import android.util.Log;

import java.util.Locale;

public class LocaleHelper {

    private static final String TAG = LocaleHelper.class.getSimpleName();

    public static Context setLocale(Context context) {
        Context deviceContext = context.createDeviceProtectedStorageContext();
        if (!deviceContext.moveSharedPreferencesFrom(context, SharedPref)) {
            Log.w(TAG, "Failed to migrate shared preferences.");
        }

        SharedPreferences prefs = deviceContext.getSharedPreferences(SharedPref, Context.MODE_PRIVATE);
        String localeCode = prefs.getString(APP_LANGUAGE, "");

        if (!localeCode.isEmpty()) {
            Locale locale = Locale.forLanguageTag(localeCode);

            Configuration configuration = deviceContext.getResources().getConfiguration();
            configuration.setLocale(locale);
            configuration.setLayoutDirection(locale);

            LocaleList localeList = new LocaleList(locale);
            LocaleList.setDefault(localeList);
            configuration.setLocales(localeList);

            deviceContext = deviceContext.createConfigurationContext(configuration);
        }

        return deviceContext;
    }
}