package com.drdisagree.iconify.ui.base;

import android.content.Context;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.Fragment;

import com.drdisagree.iconify.utils.helper.LocaleHelper;

public class BaseFragment extends Fragment {

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(LocaleHelper.setLocale(context));

        if (getActivity() != null) {
            Window window = requireActivity().getWindow();
            WindowCompat.setDecorFitsSystemWindows(window, false);
        }
    }
}
