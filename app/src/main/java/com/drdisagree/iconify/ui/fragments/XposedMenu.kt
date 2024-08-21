package com.drdisagree.iconify.ui.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
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
import androidx.navigation.Navigation.findNavController
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.Iconify.Companion.appContextLocale
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Const.ACTION_HOOK_CHECK_REQUEST
import com.drdisagree.iconify.common.Const.ACTION_HOOK_CHECK_RESULT
import com.drdisagree.iconify.common.Preferences
import com.drdisagree.iconify.common.Preferences.SHOW_XPOSED_WARN
import com.drdisagree.iconify.config.Prefs
import com.drdisagree.iconify.config.RPrefs
import com.drdisagree.iconify.databinding.FragmentXposedMenuBinding
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.widgets.MenuWidget
import com.drdisagree.iconify.utils.SystemUtil
import com.drdisagree.iconify.utils.SystemUtil.disableBlur
import com.drdisagree.iconify.utils.SystemUtil.hasStoragePermission
import com.drdisagree.iconify.utils.SystemUtil.requestStoragePermission
import com.drdisagree.iconify.utils.SystemUtil.restartSystemUI
import com.drdisagree.iconify.utils.extension.ObservableVariable
import com.drdisagree.iconify.utils.helper.ImportExport.exportSettings
import com.drdisagree.iconify.utils.helper.ImportExport.importSettings
import com.drdisagree.iconify.utils.overlay.FabricatedUtil.disableOverlays
import com.drdisagree.iconify.utils.overlay.OverlayUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.concurrent.Executors

class XposedMenu : BaseFragment() {

    private lateinit var binding: FragmentXposedMenuBinding

    private val handler = Handler(Looper.getMainLooper())
    private var intentFilterHookedSystemUI = IntentFilter()
    private var isHookSuccessful = false

    private val checkSystemUIHooked: Runnable = object : Runnable {
        override fun run() {
            checkXposedHooked()
            handler.postDelayed(this, 1000)
        }
    }

    private val receiverHookedSystemui: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_HOOK_CHECK_RESULT) {
                isHookSuccessful = true
                isXposedHooked.setValue(true)
            }
        }
    }

    private var startExportActivityIntent = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result1: ActivityResult ->
        if (result1.resultCode == Activity.RESULT_OK) {
            val data = result1.data ?: return@registerForActivityResult

            try {
                exportSettings(
                    RPrefs.prefs,
                    requireContext().contentResolver.openOutputStream(data.data!!)!!
                )

                Toast.makeText(
                    requireContext(),
                    requireContext().resources.getString(R.string.toast_export_settings_successfull),
                    Toast.LENGTH_SHORT
                ).show()
            } catch (exception: Exception) {
                Toast.makeText(
                    requireContext(),
                    requireContext().resources.getString(R.string.toast_error),
                    Toast.LENGTH_SHORT
                ).show()

                Log.e("Settings", "Error exporting settings", exception)
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
                .setPositiveButton(requireContext().resources.getString(R.string.btn_positive)) { dialog: DialogInterface, _: Int ->
                    dialog.dismiss()

                    Executors.newSingleThreadExecutor().execute {
                        try {
                            val success = importSettings(
                                RPrefs.prefs,
                                appContext.contentResolver.openInputStream(data.data!!)!!,
                                false
                            )

                            if (success) {
                                Handler(Looper.getMainLooper()).post {
                                    Toast.makeText(
                                        appContext,
                                        appContext.resources.getString(R.string.toast_import_settings_successfull),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                restartSystemUI()
                            } else {
                                Handler(Looper.getMainLooper()).post {
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

                                Log.e("Settings", "Error exporting settings", exception)
                            }
                        }
                    }
                }
                .setNegativeButton(requireContext().resources.getString(R.string.btn_negative)) { dialog: DialogInterface, _: Int ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Suppress("deprecation")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentXposedMenuBinding.inflate(inflater, container, false)
        val view: View = binding.getRoot()

        // Header
        binding.header.toolbar.setTitle(resources.getString(R.string.activity_title_xposed_menu))
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.header.toolbar)
        setHasOptionsMenu(true)

        if (!Preferences.isXposedOnlyMode) {
            (requireActivity() as AppCompatActivity).supportActionBar
                ?.setDisplayHomeAsUpEnabled(true)
            (requireActivity() as AppCompatActivity).supportActionBar
                ?.setDisplayShowHomeEnabled(true)
            binding.header.toolbar.setNavigationOnClickListener {
                findNavController(view).popBackStack()
            }
        }

        // Xposed hook check
        intentFilterHookedSystemUI.addAction(ACTION_HOOK_CHECK_RESULT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(
                receiverHookedSystemui,
                intentFilterHookedSystemUI,
                Context.RECEIVER_EXPORTED
            )
        } else {
            requireContext().registerReceiver(
                receiverHookedSystemui,
                intentFilterHookedSystemUI
            )
        }
        binding.xposedHookCheck.container.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_MAIN)
                intent.setComponent(
                    ComponentName(
                        "org.lsposed.manager",
                        "org.lsposed.manager.ui.activities.MainActivity"
                    )
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } catch (ignored: Exception) {
            }
        }
        isXposedHooked.setOnChangeListener { newValue: Boolean? ->
            try {
                if (newValue == true) {
                    if (binding.xposedHookCheck.container.visibility != View.GONE) {
                        binding.xposedHookCheck.container.visibility = View.GONE
                    }
                } else {
                    if (binding.xposedHookCheck.container.visibility != View.VISIBLE) {
                        binding.xposedHookCheck.container.visibility = View.VISIBLE
                    }
                }
            } catch (ignored: Exception) {
            }
        }
        isXposedHooked.setValue(false)
        handler.post(checkSystemUIHooked)

        // Xposed menu list items
        addItem(initXposedMenuListItems())

        return view
    }

    private fun initXposedMenuListItems(): ArrayList<Array<Any>> {
        val xposedMenu = ArrayList<Array<Any>>().apply {
            add(
                arrayOf(
                    R.id.action_xposedMenu2_to_xposedTransparencyBlur,
                    appContextLocale.resources.getString(R.string.activity_title_transparency_blur),
                    appContextLocale.resources.getString(R.string.activity_desc_transparency_blur),
                    R.drawable.ic_xposed_transparency_blur
                )
            )
            add(
                arrayOf(
                    R.id.action_xposedMenu2_to_xposedQuickSettings,
                    appContextLocale.resources.getString(R.string.activity_title_quick_settings),
                    appContextLocale.resources.getString(R.string.activity_desc_quick_settings),
                    R.drawable.ic_xposed_quick_settings
                )
            )
            add(
                arrayOf(
                    R.id.action_xposedMenu2_to_xposedThemes,
                    appContextLocale.resources.getString(R.string.activity_title_themes),
                    appContextLocale.resources.getString(R.string.activity_desc_themes),
                    R.drawable.ic_xposed_themes
                )
            )
            add(
                arrayOf(
                    R.id.action_xposedMenu2_to_xposedBatteryStyle,
                    appContextLocale.resources.getString(R.string.activity_title_battery_style),
                    appContextLocale.resources.getString(R.string.activity_desc_battery_style),
                    R.drawable.ic_colored_battery
                )
            )
            add(
                arrayOf(
                    R.id.action_xposedMenu_to_xposedStatusbar,
                    resources.getString(R.string.activity_title_statusbar),
                    resources.getString(R.string.activity_desc_statusbar),
                    R.drawable.ic_tweaks_statusbar
                )
            )
            add(
                arrayOf(
                    R.id.action_xposedMenu_to_xposedVolumePanel,
                    appContextLocale.resources.getString(R.string.activity_title_volume_panel),
                    appContextLocale.resources.getString(R.string.activity_desc_volume_panel),
                    R.drawable.ic_tweaks_volume
                )
            )
            add(
                arrayOf(
                    R.id.action_xposedMenu2_to_xposedHeaderImage,
                    appContextLocale.resources.getString(R.string.activity_title_header_image),
                    appContextLocale.resources.getString(R.string.activity_desc_header_image),
                    R.drawable.ic_xposed_header_image
                )
            )
            add(
                arrayOf(
                    R.id.action_xposedMenu2_to_xposedHeaderClock,
                    appContextLocale.resources.getString(R.string.activity_title_header_clock),
                    appContextLocale.resources.getString(R.string.activity_desc_header_clock),
                    R.drawable.ic_xposed_header_clock
                )
            )
            add(
                arrayOf(
                    R.id.action_xposedMenu2_to_xposedLockscreenClock,
                    appContextLocale.resources.getString(R.string.activity_title_lockscreen_clock),
                    appContextLocale.resources.getString(R.string.activity_desc_lockscreen_clock),
                    R.drawable.ic_xposed_lockscreen
                )
            )
            add(
                arrayOf(
                    R.id.action_xposedMenu2_to_xposedLockscreenWeather,
                    appContextLocale.resources.getString(R.string.activity_title_lockscreen_weather),
                    appContextLocale.resources.getString(R.string.activity_desc_lockscreen_weather),
                    R.drawable.ic_xposed_lockscreen_weather
                )
            )
            add(
                arrayOf(
                    R.id.action_xposedMenu2_to_xposedDepthWallpaper,
                    appContextLocale.resources.getString(R.string.activity_title_depth_wallpaper),
                    appContextLocale.resources.getString(R.string.activity_desc_depth_wallpaper),
                    R.drawable.ic_xposed_depth_wallpaper
                )
            )
            add(
                arrayOf(
                    R.id.action_xposedMenu2_to_xposedBackgroundChip,
                    appContextLocale.resources.getString(R.string.activity_title_background_chip),
                    appContextLocale.resources.getString(R.string.activity_desc_background_chip),
                    R.drawable.ic_xposed_background_chip
                )
            )
            add(
                arrayOf(
                    R.id.action_xposedMenu2_to_xposedOthers,
                    appContextLocale.resources.getString(R.string.activity_title_xposed_others),
                    appContextLocale.resources.getString(R.string.activity_desc_xposed_others),
                    R.drawable.ic_xposed_misc
                )
            )
        }

        return xposedMenu
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (Prefs.getBoolean(SHOW_XPOSED_WARN, true)) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(requireContext().resources.getString(R.string.attention))
                .setMessage(
                    buildString {
                        append(
                            (if (Preferences.isXposedOnlyMode) {
                                appContextLocale.resources.getString(
                                    R.string.xposed_only_desc
                                ) + "\n\n"
                            } else {
                                ""
                            })
                        )
                        append(appContextLocale.resources.getString(R.string.lsposed_warn))
                    }
                )
                .setPositiveButton(requireContext().resources.getString(R.string.understood)) { dialog: DialogInterface, _: Int ->
                    dialog.dismiss()
                }
                .setNegativeButton(requireContext().resources.getString(R.string.dont_show_again)) { dialog: DialogInterface, _: Int ->
                    dialog.dismiss()

                    Prefs.putBoolean(SHOW_XPOSED_WARN, false)
                }
                .setCancelable(true)
                .show()
        }
    }

    @Deprecated("Deprecated in Java")
    @Suppress("deprecation")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.xposed_menu, menu)

        super.onCreateOptionsMenu(menu, inflater)
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("NonConstantResourceId")
    @Suppress("deprecation")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                findNavController(binding.getRoot()).popBackStack()
                return true
            }

            R.id.menu_export_settings -> importExportSettings(true)
            R.id.menu_import_settings -> importExportSettings(false)
            R.id.menu_reset_settings -> resetSettings()
            R.id.restart_systemui -> Handler(Looper.getMainLooper()).postDelayed(
                { SystemUtil.restartSystemUI() },
                300
            )
        }

        return super.onOptionsItemSelected(item)
    }

    // Function to add new item in list
    private fun addItem(pack: ArrayList<Array<Any>>) {
        for (i in pack.indices) {
            val menu = MenuWidget(requireActivity())

            menu.setTitle(pack[i][1] as String)
            menu.setSummary(pack[i][2] as String)
            menu.setIcon(pack[i][3] as Int)
            menu.setEndArrowVisibility(View.VISIBLE)
            menu.setOnClickListener {
                findNavController(menu).navigate(
                    (pack[i][0] as Int)
                )
            }

            binding.xposedList.addView(menu)
        }
    }

    private fun importExportSettings(export: Boolean) {
        if (!hasStoragePermission()) {
            requestStoragePermission(requireContext())
        } else {
            val fileIntent = Intent()
            fileIntent.setAction(if (export) Intent.ACTION_CREATE_DOCUMENT else Intent.ACTION_GET_CONTENT)
            fileIntent.setType("*/*")
            fileIntent.putExtra(Intent.EXTRA_TITLE, "xposed_configs" + ".iconify")

            if (export) {
                startExportActivityIntent.launch(fileIntent)
            } else {
                startImportActivityIntent.launch(fileIntent)
            }
        }
    }

    private fun resetSettings() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(requireContext().resources.getString(R.string.import_settings_confirmation_title))
            .setMessage(requireContext().resources.getString(R.string.import_settings_confirmation_desc))
            .setPositiveButton(
                requireContext().resources.getString(R.string.btn_positive)
            ) { dialog: DialogInterface, _: Int ->
                dialog.dismiss()

                Handler(Looper.getMainLooper()).post {
                    try {
                        RPrefs.clearAllPrefs()

                        disableBlur(false)

                        disableOverlays(
                            "quick_qs_offset_height",
                            "qqs_layout_margin_top",
                            "qs_header_row_min_height",
                            "quick_qs_total_height",
                            "qs_panel_padding_top",
                            "qs_panel_padding_top_combined_headers"
                        )

                        OverlayUtil.disableOverlays(
                            "IconifyComponentQSLT.overlay",
                            "IconifyComponentQSDT.overlay"
                        )

                        restartSystemUI()
                    } catch (exception: Exception) {
                        Toast.makeText(
                            requireContext(),
                            requireContext().resources.getString(R.string.toast_error),
                            Toast.LENGTH_SHORT
                        ).show()

                        Log.e("Settings", "Error importing settings", exception)
                    }
                }
            }
            .setNegativeButton(requireContext().resources.getString(R.string.btn_negative)) { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
            }
            .show()
    }

    private fun checkXposedHooked() {

        isHookSuccessful = false

        object : CountDownTimer(1600, 800) {
            override fun onTick(millisUntilFinished: Long) {
                if (isHookSuccessful) {
                    cancel()
                }
            }

            override fun onFinish() {
                if (!isHookSuccessful) {
                    isXposedHooked.setValue(false)
                }
            }
        }.start()

        Thread {
            try {
                requireContext().sendBroadcast(Intent().setAction(ACTION_HOOK_CHECK_REQUEST))
            } catch (ignored: Exception) {
            }
        }.start()
    }

    override fun onDestroy() {
        try {
            handler.removeCallbacks(checkSystemUIHooked)
        } catch (ignored: Exception) {
        }

        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()

        try {
            isXposedHooked.notifyChanged()
            handler.post(checkSystemUIHooked)
        } catch (ignored: Exception) {
        }
    }

    override fun onPause() {
        try {
            handler.removeCallbacks(checkSystemUIHooked)
        } catch (ignored: Exception) {
        }

        super.onPause()
    }

    companion object {
        private val isXposedHooked = ObservableVariable<Boolean>()
    }
}