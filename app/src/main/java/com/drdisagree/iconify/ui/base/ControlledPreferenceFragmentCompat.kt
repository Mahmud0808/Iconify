package com.drdisagree.iconify.ui.base

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import androidx.recyclerview.widget.RecyclerView
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Dynamic
import com.drdisagree.iconify.common.Resources.SHARED_XPREFERENCES
import com.drdisagree.iconify.common.Resources.searchConfiguration
import com.drdisagree.iconify.common.Resources.searchableFragments
import com.drdisagree.iconify.config.PrefsHelper
import com.drdisagree.iconify.config.RPrefs
import com.drdisagree.iconify.ui.activities.MainActivity
import com.drdisagree.iconify.ui.activities.MainActivity.Companion.popCurrentFragment
import com.drdisagree.iconify.ui.activities.MainActivity.Companion.replaceFragment
import com.drdisagree.iconify.ui.dialogs.LoadingDialog
import com.drdisagree.iconify.ui.fragments.settings.Changelog
import com.drdisagree.iconify.ui.preferences.preferencesearch.SearchPreferenceResult
import com.drdisagree.iconify.utils.SystemUtils.restartSystemUI
import com.drdisagree.iconify.utils.helper.ImportExport.exportSettings
import com.drdisagree.iconify.utils.helper.ImportExport.handleExportResult
import com.drdisagree.iconify.utils.helper.ImportExport.handleImportResult
import com.drdisagree.iconify.utils.helper.ImportExport.importSettings
import com.drdisagree.iconify.utils.helper.LocaleHelper

abstract class ControlledPreferenceFragmentCompat : PreferenceFragmentCompat() {

    var loadingDialog: LoadingDialog? = null

    private val changeListener =
        OnSharedPreferenceChangeListener { _: SharedPreferences, key: String? ->
            updateScreen(
                key
            )
        }

    abstract val title: String

    abstract val backButtonEnabled: Boolean

    abstract val layoutResource: Int

    open val themeResource: Int
        get() = R.style.PrefsThemeToolbar

    abstract val hasMenu: Boolean

    open val menuResource: Int
        get() = R.menu.default_menu

    private var mView: View? = null

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

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.setStorageDeviceProtected()
        preferenceManager.sharedPreferencesName = SHARED_XPREFERENCES
        preferenceManager.sharedPreferencesMode = Context.MODE_PRIVATE

        try {
            setPreferencesFromResource(layoutResource, rootKey)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load preference from resource", e)
        }
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
    ): View {
        inflater.context.setTheme(themeResource)

        // Initialize loading dialog
        loadingDialog = LoadingDialog(requireActivity())

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mView = view

        if (hasMenu) {
            val menuHost: MenuHost = requireActivity()
            menuHost.addMenuProvider(object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menu.clear()
                    menuInflater.inflate(menuResource, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return when (menuItem.itemId) {
                        R.id.action_search -> {
                            searchConfiguration.showSearchFragment()
                            true
                        }

                        R.id.menu_changelog -> {
                            replaceFragment(Changelog())
                            true
                        }

                        R.id.menu_export_settings -> {
                            exportSettings(
                                this@ControlledPreferenceFragmentCompat,
                                startExportActivityIntent
                            )
                            true
                        }

                        R.id.menu_import_settings -> {
                            importSettings(
                                this@ControlledPreferenceFragmentCompat,
                                startImportActivityIntent
                            )
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

                        else -> {
                            false
                        }
                    }
                }
            }, viewLifecycleOwner, Lifecycle.State.RESUMED)
        }
    }

    public override fun onCreateAdapter(preferenceScreen: PreferenceScreen): RecyclerView.Adapter<*> {
        RPrefs.registerOnSharedPreferenceChangeListener(changeListener)

        updateScreen(null)

        return super.onCreateAdapter(preferenceScreen)
    }

    fun onSearchResultClicked(result: SearchPreferenceResult) {
        if (result.resourceFile == layoutResource) {
            popCurrentFragment()
            SearchPreferenceResult.highlight(this, result.key)
        } else {
            for (searchableFragment in searchableFragments) {
                if (searchableFragment.xml == result.resourceFile) {
                    replaceFragment(searchableFragment.fragment)
                    SearchPreferenceResult.highlight(searchableFragment.fragment, result.key);
                    break
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (context != null) {
            mView?.findViewById<Toolbar?>(R.id.toolbar)?.let {
                (context as AppCompatActivity).setSupportActionBar(it)
                it.title = title
            }

            ((context as AppCompatActivity).supportActionBar)?.setDisplayHomeAsUpEnabled(
                backButtonEnabled
            )
        }
    }

    override fun onDestroy() {
        loadingDialog?.hide()

        RPrefs.unregisterOnSharedPreferenceChangeListener(changeListener)

        super.onDestroy()
    }

    open fun updateScreen(key: String?) {
        PrefsHelper.setupAllPreferences(this.preferenceScreen)
    }

    override fun setDivider(divider: Drawable?) {
        super.setDivider(ColorDrawable(Color.TRANSPARENT))
    }

    override fun setDividerHeight(height: Int) {
        super.setDividerHeight(0)
    }

    companion object {
        private val TAG = ControlledPreferenceFragmentCompat::class.java.simpleName
    }
}
