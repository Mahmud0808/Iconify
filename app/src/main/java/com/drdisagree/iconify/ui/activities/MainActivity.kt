package com.drdisagree.iconify.ui.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.airbnb.lottie.LottieCompositionFactory
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Preferences
import com.drdisagree.iconify.common.Preferences.FORCE_RELOAD_OVERLAY_STATE
import com.drdisagree.iconify.common.Preferences.FORCE_RELOAD_PACKAGE_NAME
import com.drdisagree.iconify.common.Preferences.MONET_ENGINE_SWITCH
import com.drdisagree.iconify.common.Preferences.ON_HOME_PAGE
import com.drdisagree.iconify.config.Prefs
import com.drdisagree.iconify.config.RPrefs
import com.drdisagree.iconify.databinding.ActivityHomePageBinding
import com.drdisagree.iconify.ui.base.BaseActivity
import com.drdisagree.iconify.ui.events.ColorDismissedEvent
import com.drdisagree.iconify.ui.events.ColorSelectedEvent
import com.drdisagree.iconify.utils.overlay.FabricatedUtil
import com.drdisagree.iconify.utils.overlay.OverlayUtil
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener
import com.topjohnwu.superuser.Shell
import org.greenrobot.eventbus.EventBus

class MainActivity : BaseActivity(), ColorPickerDialogListener {

    private lateinit var binding: ActivityHomePageBinding
    private var selectedFragment: Int? = null
    private var colorPickerDialog: ColorPickerDialog.Builder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(binding.getRoot())

        // Setup navigation
        setupNavigation()
        Prefs.putBoolean(ON_HOME_PAGE, true)

        Thread {
            // Clear lottie cache
            LottieCompositionFactory.clearCache(this)

            // Get list of enabled overlays
            val enabledOverlays = OverlayUtil.enabledOverlayList
            for (overlay in enabledOverlays) {
                Prefs.putBoolean(overlay, true)
            }

            val fabricatedEnabledOverlays = FabricatedUtil.enabledOverlayList
            for (overlay in fabricatedEnabledOverlays) {
                Prefs.putBoolean("fabricated$overlay", true)
            }

            Prefs.putBoolean(
                MONET_ENGINE_SWITCH,
                enabledOverlays.contains("IconifyComponentME.overlay")
            )

            val state =
                Shell.cmd(
                    "[[ $(cmd overlay list | grep -o '\\[x\\] $FORCE_RELOAD_PACKAGE_NAME') ]] && echo 1 || echo 0"
                ).exec().out[0] == "1"
            RPrefs.putBoolean(FORCE_RELOAD_OVERLAY_STATE, state)
        }.start()

        colorPickerDialog = ColorPickerDialog.newBuilder()
    }

    private fun setupNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment?
                ?: return
        val navController = navHostFragment.navController

        if (Preferences.isXposedOnlyMode) {
            navController.setGraph(R.navigation.nav_xposed_menu)
            binding.bottomNavigationView.menu.clear()
            binding.bottomNavigationView.inflateMenu(R.menu.bottom_nav_menu_xposed_only)
        }

        setupWithNavController(binding.bottomNavigationView, navController)

        binding.bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
            setFragment(item.itemId, navController, Preferences.isXposedOnlyMode)
            true
        }
    }

    @SuppressLint("NonConstantResourceId")
    private fun setFragment(itemId: Int, navController: NavController, isXposedOnlyMode: Boolean) {
        val currentDestination = navController.currentDestination ?: return

        if (isXposedOnlyMode) {
            when (itemId) {
                R.id.xposedMenu -> {
                    if (currentDestination.id != itemId) {
                        navController.popBackStack(navController.graph.startDestinationId, false)
                        selectedFragment = itemId
                    }
                }

                R.id.settings -> {
                    if (currentDestination.id != itemId) {
                        navController.popBackStack(navController.graph.startDestinationId, false)
                        findNavController(
                            this,
                            R.id.fragmentContainerView
                        ).navigate(R.id.action_xposedMenu_to_settings2)
                        selectedFragment = itemId
                    }
                }
            }
            return
        }

        when (itemId) {
            R.id.homePage -> {
                if (currentDestination.id != itemId) {
                    navController.popBackStack(navController.graph.startDestinationId, false)
                    selectedFragment = itemId
                }
            }

            R.id.tweaks -> {
                if (currentDestination.id != itemId) {
                    navController.popBackStack(navController.graph.startDestinationId, false)
                    findNavController(
                        this,
                        R.id.fragmentContainerView
                    ).navigate(R.id.action_home2_to_tweaks)
                    selectedFragment = itemId
                }
            }

            R.id.settings -> {
                if (currentDestination.id != itemId) {
                    navController.popBackStack(navController.graph.startDestinationId, false)
                    findNavController(
                        this,
                        R.id.fragmentContainerView
                    ).navigate(R.id.action_home2_to_settings)
                    selectedFragment = itemId
                }
            }
        }
    }

    fun showColorPickerDialog(
        dialogId: Int,
        defaultColor: Int,
        showPresets: Boolean,
        showAlphaSlider: Boolean,
        showColorShades: Boolean
    ) {
        colorPickerDialog!!.setDialogStyle(R.style.ColorPicker)
            .setColor(defaultColor)
            .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
            .setAllowCustom(false)
            .setAllowPresets(showPresets)
            .setDialogId(dialogId)
            .setShowAlphaSlider(showAlphaSlider)
            .setShowColorShades(showColorShades)

        colorPickerDialog!!.show(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        if (selectedFragment != null) outState.putInt(DATA_KEY, selectedFragment!!)
    }

    public override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        selectedFragment = savedInstanceState.getInt(DATA_KEY)
    }

    override fun onColorSelected(dialogId: Int, color: Int) {
        EventBus.getDefault().post(ColorSelectedEvent(dialogId, color))
    }

    override fun onDialogDismissed(dialogId: Int) {
        EventBus.getDefault().post(ColorDismissedEvent(dialogId))
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(this, R.id.fragmentContainerView)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    companion object {
        private const val DATA_KEY = "mDataKey"
    }
}