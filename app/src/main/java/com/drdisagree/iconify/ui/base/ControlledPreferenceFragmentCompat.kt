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
import androidx.preference.PreferenceScreen
import androidx.recyclerview.widget.RecyclerView
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Resources.SHARED_XPREFERENCES
import com.drdisagree.iconify.config.PrefsHelper
import com.drdisagree.iconify.config.RPrefs
import com.drdisagree.iconify.ui.activities.MainActivity.Companion.popCurrentFragment
import com.drdisagree.iconify.ui.activities.MainActivity.Companion.replaceFragment
import com.drdisagree.iconify.ui.activities.MainActivity.Companion.searchConfiguration
import com.drdisagree.iconify.ui.activities.MainActivity.Companion.searchableFragments
import com.drdisagree.iconify.ui.preferences.preferencesearch.SearchPreferenceResult
import com.drdisagree.iconify.utils.helper.LocaleHelper

abstract class ControlledPreferenceFragmentCompat : PreferenceFragmentCompat() {

    private val changeListener =
        OnSharedPreferenceChangeListener { _: SharedPreferences, key: String? ->
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

                        else -> false
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
                    popCurrentFragment()
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
