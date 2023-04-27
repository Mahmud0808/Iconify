package com.drdisagree.iconify.ui.fragments;

import static com.drdisagree.iconify.common.Preferences.APP_LANGUAGE;

import android.content.Context;
import android.content.res.Configuration;
import android.os.LocaleList;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.utils.helpers.LocaleHelper;

import java.util.Locale;

public class BaseFragment extends Fragment {

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(LocaleHelper.setLocale(context));
    }
}
