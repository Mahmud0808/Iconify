package com.drdisagree.iconify.ui.base

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
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
import android.widget.Toast
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
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.Iconify.Companion.appContextLocale
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Resources.SHARED_XPREFERENCES
import com.drdisagree.iconify.config.PrefsHelper
import com.drdisagree.iconify.config.RPrefs
import com.drdisagree.iconify.ui.activities.MainActivity.Companion.popCurrentFragment
import com.drdisagree.iconify.ui.activities.MainActivity.Companion.replaceFragment
import com.drdisagree.iconify.ui.activities.MainActivity.Companion.searchConfiguration
import com.drdisagree.iconify.ui.activities.MainActivity.Companion.searchableFragments
import com.drdisagree.iconify.ui.dialogs.LoadingDialog
import com.drdisagree.iconify.ui.fragments.Changelog
import com.drdisagree.iconify.ui.preferences.preferencesearch.SearchPreferenceResult
import com.drdisagree.iconify.utils.SystemUtil.hasStoragePermission
import com.drdisagree.iconify.utils.SystemUtil.requestStoragePermission
import com.drdisagree.iconify.utils.SystemUtil.restartSystemUI
import com.drdisagree.iconify.utils.helper.ImportExport.exportSettings
import com.drdisagree.iconify.utils.helper.ImportExport.importSettings
import com.drdisagree.iconify.utils.helper.LocaleHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.concurrent.Executors

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

    abstract val hasMenu: Boolean

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
                            importExportSettings(true)
                            true
                        }

                        R.id.menu_import_settings -> {
                            importExportSettings(false)
                            true
                        }

                        R.id.restart_systemui -> {
                            Handler(Looper.getMainLooper()).postDelayed(
                                { restartSystemUI() },
                                300
                            )
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

    private fun importExportSettings(export: Boolean) {
        if (!hasStoragePermission()) {
            requestStoragePermission(requireContext())
        } else {
            val fileIntent = Intent()
            fileIntent.setAction(if (export) Intent.ACTION_CREATE_DOCUMENT else Intent.ACTION_GET_CONTENT)
            fileIntent.setType("*/*")
            fileIntent.putExtra(Intent.EXTRA_TITLE, "configs" + ".iconify")

            if (export) {
                startExportActivityIntent.launch(fileIntent)
            } else {
                startImportActivityIntent.launch(fileIntent)
            }
        }
    }

    private var startExportActivityIntent = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result1: ActivityResult ->
        if (result1.resultCode == Activity.RESULT_OK) {
            val data = result1.data ?: return@registerForActivityResult

            Executors.newSingleThreadExecutor().execute {
                try {
                    exportSettings(
                        RPrefs.getPrefs,
                        appContext.contentResolver.openOutputStream(data.data!!)!!
                    )

                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(
                            appContext,
                            appContextLocale.resources.getString(R.string.toast_export_settings_successfull),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (exception: Exception) {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(
                            appContext,
                            appContextLocale.resources.getString(R.string.toast_error),
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("Settings", "Error exporting settings", exception)
                    }
                }
            }
        }
    }

    private var startImportActivityIntent = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result2: ActivityResult ->
        if (result2.resultCode == Activity.RESULT_OK) {
            val data = result2.data ?: return@registerForActivityResult
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(requireContext().resources.getString(R.string.import_settings_confirmation_title))
                .setMessage(requireContext().resources.getString(R.string.import_settings_confirmation_desc))
                .setPositiveButton(
                    requireContext().resources.getString(R.string.btn_positive)
                ) { dialog: DialogInterface, _: Int ->
                    dialog.dismiss()
                    loadingDialog!!.show(resources.getString(R.string.loading_dialog_wait))
                    Executors.newSingleThreadExecutor().execute {
                        try {
                            val success = importSettings(
                                RPrefs.getPrefs,
                                appContext.contentResolver.openInputStream(data.data!!)!!,
                                true
                            )
                            Handler(Looper.getMainLooper()).post {
                                loadingDialog!!.hide()
                                if (success) {
                                    Toast.makeText(
                                        appContext,
                                        appContext.resources.getString(R.string.toast_import_settings_successfull),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        appContext,
                                        appContext.resources.getString(R.string.toast_error),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } catch (exception: Exception) {
                            Handler(Looper.getMainLooper()).post {
                                Toast.makeText(
                                    appContext,
                                    appContext.resources.getString(R.string.toast_error),
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.e("Settings", "Error importing settings", exception)
                            }
                        }
                    }
                }
                .setNegativeButton(requireContext().resources.getString(R.string.btn_negative)) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
                .show()
        }
    }

    companion object {
        private val TAG = ControlledPreferenceFragmentCompat::class.java.simpleName
    }
}
