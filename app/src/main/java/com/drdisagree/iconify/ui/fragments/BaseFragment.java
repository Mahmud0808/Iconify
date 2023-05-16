package com.drdisagree.iconify.ui.fragments;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.drdisagree.iconify.utils.helpers.LocaleHelper;

public class BaseFragment extends Fragment {

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(LocaleHelper.setLocale(context));
    }
}
