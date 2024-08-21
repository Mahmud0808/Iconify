package com.drdisagree.iconify.ui.base

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.PreferenceScreen
import androidx.recyclerview.widget.RecyclerView
import com.drdisagree.iconify.R
import com.drdisagree.iconify.config.ExtendedPrefs
import com.drdisagree.iconify.config.PreferenceHelper
import com.drdisagree.iconify.ui.preferences.preferencesearch.SearchConfiguration
import com.drdisagree.iconify.utils.helper.LocaleHelper

abstract class ControlledPreferenceFragmentCompat : PreferenceFragmentCompat() {

    private var mPreferences: ExtendedPrefs? = null

    private val changeListener =
        OnSharedPreferenceChangeListener { _: SharedPreferences?, key: String? ->
            updateScreen(
                key
            )
        }

    abstract val title: String

    abstract val backButtonEnabled: Boolean

    abstract val layoutResource: Int

    abstract val hasMenu: Boolean

    abstract val scopes: Array<String>?

    open val themeResource: Int
        get() = R.style.PrefsThemeToolbar

    open val menuResource: Int
        get() = R.menu.default_menu

    private var mView: View? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.setStorageDeviceProtected()
        try {
            setPreferencesFromResource(layoutResource, rootKey)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load preferences", e)
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
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mView = view

        val searchConfiguration = SearchConfiguration().apply {
            setActivity(requireActivity() as AppCompatActivity)
            setFragmentContainerViewId(R.id.fragmentContainerView)
            setBreadcrumbsEnabled(true)
            setHistoryEnabled(true)
            setFuzzySearchEnabled(false)
        }

        if (hasMenu) {
            val menuHost: MenuHost = requireActivity()
            menuHost.addMenuProvider(object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(menuResource, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return when (menuItem.itemId) {
                        R.id.action_search -> {
                            searchConfiguration.showSearchFragment()
                            true
                        }

                        else -> false
                    }
                }
            }, viewLifecycleOwner, Lifecycle.State.RESUMED)
        }
    }

    public override fun onCreateAdapter(preferenceScreen: PreferenceScreen): RecyclerView.Adapter<*> {
        mPreferences = ExtendedPrefs.from(
            PreferenceManager.getDefaultSharedPreferences(
                requireContext().createDeviceProtectedStorageContext()
            )
        )

        mPreferences!!.registerOnSharedPreferenceChangeListener(changeListener)

        updateScreen(null)

        return super.onCreateAdapter(preferenceScreen)
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
        if (mPreferences != null) {
            mPreferences!!.unregisterOnSharedPreferenceChangeListener(changeListener)
        }
        super.onDestroy()
    }

    open fun updateScreen(key: String?) {
        PreferenceHelper.setupAllPreferences(this.preferenceScreen)
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
