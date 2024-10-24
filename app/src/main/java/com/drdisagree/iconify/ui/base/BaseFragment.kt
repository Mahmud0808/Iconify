package com.drdisagree.iconify.ui.base

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Dynamic
import com.drdisagree.iconify.common.Resources.searchConfiguration
import com.drdisagree.iconify.common.Resources.searchableFragments
import com.drdisagree.iconify.ui.activities.MainActivity
import com.drdisagree.iconify.ui.activities.MainActivity.Companion.replaceFragment
import com.drdisagree.iconify.ui.dialogs.LoadingDialog
import com.drdisagree.iconify.ui.fragments.settings.Changelog
import com.drdisagree.iconify.ui.fragments.settings.Experimental
import com.drdisagree.iconify.ui.fragments.xposed.LockscreenClockParent
import com.drdisagree.iconify.ui.preferences.preferencesearch.SearchPreferenceResult
import com.drdisagree.iconify.utils.SystemUtils.restartSystemUI
import com.drdisagree.iconify.utils.helper.ImportExport.exportSettings
import com.drdisagree.iconify.utils.helper.ImportExport.handleExportResult
import com.drdisagree.iconify.utils.helper.ImportExport.handleImportResult
import com.drdisagree.iconify.utils.helper.ImportExport.importSettings
import com.drdisagree.iconify.utils.helper.LocaleHelper

abstract class BaseFragment : Fragment() {

    private var loadingDialog: LoadingDialog? = null

    private var startExportActivityIntent = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        handleExportResult(
            result = result,
            context = requireContext(),
            contentResolver = requireContext().contentResolver
        )
    }

    private var startImportActivityIntent = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        handleImportResult(
            result = result,
            fragment = this,
            loadingDialog = loadingDialog!!
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(LocaleHelper.setLocale(context))

        if (activity != null) {
            val window = requireActivity().window
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Initialize loading dialog
        loadingDialog = LoadingDialog(requireActivity())

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.clear()
                menuInflater.inflate(R.menu.default_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_search -> {
                        searchConfiguration.showSearchFragment()
                        true
                    }

                    R.id.menu_changelog -> {
                        replaceFragment(parentFragmentManager, Changelog())
                        true
                    }

                    R.id.menu_export_settings -> {
                        exportSettings(this@BaseFragment, startExportActivityIntent)
                        true
                    }

                    R.id.menu_import_settings -> {
                        importSettings(this@BaseFragment, startImportActivityIntent)
                        true
                    }

                    R.id.restart_systemui -> {
                        Dynamic.requiresSystemUiRestart = false

                        MainActivity.showOrHidePendingActionButton(
                            activityBinding = (requireActivity() as MainActivity).binding,
                            requiresSystemUiRestart = false
                        )

                        Handler(Looper.getMainLooper()).postDelayed({
                            restartSystemUI()
                        }, 300)
                        true
                    }

                    R.id.experimental_features -> {
                        replaceFragment(parentFragmentManager, Experimental())
                        true
                    }

                    else -> {
                        false
                    }
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    open fun onSearchResultClicked(result: SearchPreferenceResult) {
        for (searchableFragment in searchableFragments) {
            if (searchableFragment.xml == result.resourceFile) {
                replaceFragment(parentFragmentManager, searchableFragment.fragment)
                SearchPreferenceResult.highlight(parentFragmentManager as ControlledPreferenceFragmentCompat, result.key);
                break
            }
        }
    }

    override fun onDestroy() {
        loadingDialog?.dismiss()

        super.onDestroy()
    }
}
