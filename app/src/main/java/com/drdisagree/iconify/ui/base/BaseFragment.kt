package com.drdisagree.iconify.ui.base

import android.content.Context
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import com.drdisagree.iconify.utils.helper.LocaleHelper

open class BaseFragment : Fragment() {

    override fun onAttach(context: Context) {
        super.onAttach(LocaleHelper.setLocale(context))

        if (activity != null) {
            val window = requireActivity().window
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
    }
}
