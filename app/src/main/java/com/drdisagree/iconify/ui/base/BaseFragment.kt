package com.drdisagree.iconify.ui.base

import android.content.Context
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import com.drdisagree.iconify.ui.activities.MainActivity.Companion.popCurrentFragment
import com.drdisagree.iconify.ui.activities.MainActivity.Companion.replaceFragment
import com.drdisagree.iconify.ui.activities.MainActivity.Companion.searchableFragments
import com.drdisagree.iconify.ui.preferences.preferencesearch.SearchPreferenceResult
import com.drdisagree.iconify.utils.helper.LocaleHelper

open class BaseFragment : Fragment() {

    override fun onAttach(context: Context) {
        super.onAttach(LocaleHelper.setLocale(context))

        if (activity != null) {
            val window = requireActivity().window
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
    }

    fun onSearchResultClicked(result: SearchPreferenceResult) {
        for (searchableFragment in searchableFragments) {
            if (searchableFragment.xml == result.resourceFile) {
                popCurrentFragment()
                replaceFragment(searchableFragment.fragment)
                SearchPreferenceResult.highlight(searchableFragment.fragment, result.key);
                break
            }
        }
    }
}
