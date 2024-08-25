package com.drdisagree.iconify.ui.activities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.Navigation.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.airbnb.lottie.LottieCompositionFactory
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Dynamic
import com.drdisagree.iconify.common.Preferences
import com.drdisagree.iconify.common.Preferences.FORCE_RELOAD_OVERLAY_STATE
import com.drdisagree.iconify.common.Preferences.FORCE_RELOAD_PACKAGE_NAME
import com.drdisagree.iconify.common.Preferences.MONET_ENGINE_SWITCH
import com.drdisagree.iconify.common.Preferences.ON_HOME_PAGE
import com.drdisagree.iconify.config.RPrefs
import com.drdisagree.iconify.databinding.ActivityHomePageBinding
import com.drdisagree.iconify.ui.base.BaseActivity
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.base.ControlledPreferenceFragmentCompat
import com.drdisagree.iconify.ui.events.ColorDismissedEvent
import com.drdisagree.iconify.ui.events.ColorSelectedEvent
import com.drdisagree.iconify.ui.fragments.home.Home
import com.drdisagree.iconify.ui.fragments.settings.Settings
import com.drdisagree.iconify.ui.fragments.tweaks.Tweaks
import com.drdisagree.iconify.ui.fragments.xposed.HeaderClock
import com.drdisagree.iconify.ui.fragments.xposed.LockscreenClock
import com.drdisagree.iconify.ui.fragments.xposed.Others
import com.drdisagree.iconify.ui.fragments.xposed.QuickSettings
import com.drdisagree.iconify.ui.fragments.xposed.Statusbar
import com.drdisagree.iconify.ui.fragments.xposed.TransparencyBlur
import com.drdisagree.iconify.ui.fragments.xposed.VolumePanel
import com.drdisagree.iconify.ui.fragments.xposed.Xposed
import com.drdisagree.iconify.ui.models.SearchPreferenceItem
import com.drdisagree.iconify.ui.preferences.preferencesearch.SearchConfiguration
import com.drdisagree.iconify.ui.preferences.preferencesearch.SearchPreferenceFragment
import com.drdisagree.iconify.ui.preferences.preferencesearch.SearchPreferenceResult
import com.drdisagree.iconify.ui.preferences.preferencesearch.SearchPreferenceResultListener
import com.drdisagree.iconify.utils.SystemUtil
import com.drdisagree.iconify.utils.overlay.FabricatedUtil
import com.drdisagree.iconify.utils.overlay.OverlayUtil
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener
import com.topjohnwu.superuser.Shell
import org.greenrobot.eventbus.EventBus
import java.util.UUID

class MainActivity : BaseActivity(),
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback,
    SearchPreferenceResultListener,
    ColorPickerDialogListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(binding.getRoot())

        colorPickerDialog = ColorPickerDialog.newBuilder()
        myFragmentManager = supportFragmentManager
        myActionBar = supportActionBar

        setupNavigation()
        setupSearchConfiguration()
        RPrefs.putBoolean(ON_HOME_PAGE, true)

        if (savedInstanceState == null) {
            replaceFragment(if (!Preferences.isXposedOnlyMode) Home() else Xposed())
        }

        initData()

        setupFloatingActionButtons()
    }

    private fun initData() {
        Thread {
            // Clear lottie cache
            LottieCompositionFactory.clearCache(this)

            // Get list of enabled overlays
            val enabledOverlays = OverlayUtil.enabledOverlayList
            for (overlay in enabledOverlays) {
                if (!RPrefs.getBoolean(overlay, false)) {
                    RPrefs.putBoolean(overlay, true)
                }
            }

            val fabricatedEnabledOverlays = FabricatedUtil.enabledOverlayList
            for (overlay in fabricatedEnabledOverlays) {
                if (!RPrefs.getBoolean("fabricated$overlay", false)) {
                    RPrefs.putBoolean("fabricated$overlay", true)
                }
            }

            if (enabledOverlays.contains("IconifyComponentME.overlay") &&
                !RPrefs.getBoolean(MONET_ENGINE_SWITCH, false)
            ) {
                RPrefs.putBoolean(
                    MONET_ENGINE_SWITCH,
                    true
                )
            } else if (!enabledOverlays.contains("IconifyComponentME.overlay") &&
                RPrefs.getBoolean(MONET_ENGINE_SWITCH, false)
            ) {
                RPrefs.putBoolean(
                    MONET_ENGINE_SWITCH,
                    false
                )
            }

            val state = Shell.cmd(
                "[[ $(cmd overlay list | grep -o '\\[x\\] $FORCE_RELOAD_PACKAGE_NAME') ]] && echo 1 || echo 0"
            ).exec().out[0] == "1"

            if (state != RPrefs.getBoolean(FORCE_RELOAD_OVERLAY_STATE, false)) {
                RPrefs.putBoolean(FORCE_RELOAD_OVERLAY_STATE, state)
            }
        }.start()
    }

    private fun setupFloatingActionButtons() {
        binding.hideAll.hide()
        binding.restartSystemui.hide()
        binding.restartDevice.hide()
        binding.pendingActions.shrink()

        showOrHidePendingActionButton(
            Dynamic.requiresSystemUiRestart,
            Dynamic.requiresDeviceRestart
        )

        binding.pendingActions.setOnClickListener {
            showOrHideFabButtons()
        }

        binding.hideAll.setOnClickListener {
            Dynamic.requiresSystemUiRestart = false
            Dynamic.requiresDeviceRestart = false

            showOrHidePendingActionButton(
                requiresSystemUiRestart = false,
                requiresDeviceRestart = false
            )
        }

        binding.restartSystemui.setOnClickListener {
            Dynamic.requiresSystemUiRestart = false

            showOrHidePendingActionButton(
                requiresSystemUiRestart = false,
                requiresDeviceRestart = Dynamic.requiresDeviceRestart
            )

            Handler(Looper.getMainLooper()).postDelayed({
                SystemUtil.restartSystemUI()
            }, android.R.integer.config_longAnimTime.toLong())
        }

        binding.restartDevice.setOnClickListener {
            Dynamic.requiresDeviceRestart = false

            showOrHidePendingActionButton(
                requiresSystemUiRestart = Dynamic.requiresSystemUiRestart,
                requiresDeviceRestart = false
            )

            Handler(Looper.getMainLooper()).postDelayed({
                SystemUtil.restartDevice()
            }, android.R.integer.config_longAnimTime.toLong())
        }
    }

    private fun setupNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (myFragmentManager.backStackEntryCount > 1) {
                    popCurrentFragment()
                } else {
                    finish()
                }
            }
        })

        if (Preferences.isXposedOnlyMode) {
            binding.bottomNavigationView.menu.clear()
            binding.bottomNavigationView.inflateMenu(R.menu.bottom_nav_menu_xposed_only)
        }

        supportFragmentManager.addOnBackStackChangedListener {
            val tag = getTopFragmentTag()
            val xposedOnlyMode = Preferences.isXposedOnlyMode

            val homeIndex = 0
            val tweaksIndex = 1
            val xposedIndex = if (!xposedOnlyMode) 2 else 0
            val settingsIndex = if (!xposedOnlyMode) 3 else 1

            when (tag) {
                Home::class.java.simpleName -> {
                    selectedFragment = R.id.homePage
                    binding.bottomNavigationView.menu.getItem(homeIndex).setChecked(true)
                }

                Tweaks::class.java.simpleName -> {
                    selectedFragment = R.id.tweaks
                    binding.bottomNavigationView.menu.getItem(tweaksIndex).setChecked(true)
                }

                Xposed::class.java.simpleName -> {
                    selectedFragment = R.id.xposed
                    binding.bottomNavigationView.menu.getItem(xposedIndex).setChecked(true)
                }

                Settings::class.java.simpleName -> {
                    selectedFragment = R.id.settings
                    binding.bottomNavigationView.menu.getItem(settingsIndex).setChecked(true)
                }
            }
        }

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            val fragmentTag: String = getTopFragmentTag()

            when (item.itemId) {
                R.id.homePage -> {
                    if (fragmentTag != Home::class.java.simpleName) {
                        selectedFragment = R.id.homePage
                        replaceFragment(Home())
                    }
                    return@setOnItemSelectedListener true
                }

                R.id.tweaks -> {
                    if (fragmentTag != Tweaks::class.java.simpleName) {
                        selectedFragment = R.id.tweaks
                        replaceFragment(Tweaks())
                    }
                    return@setOnItemSelectedListener true
                }

                R.id.xposed -> {
                    if (fragmentTag != Xposed::class.java.simpleName) {
                        selectedFragment = R.id.xposed
                        replaceFragment(Xposed())
                    }
                    return@setOnItemSelectedListener true
                }

                R.id.settings -> {
                    if (fragmentTag != Settings::class.java.simpleName) {
                        selectedFragment = R.id.settings
                        replaceFragment(Settings())
                    }
                    return@setOnItemSelectedListener true
                }

                else -> {
                    return@setOnItemSelectedListener true
                }
            }
        }
    }

    private fun setupSearchConfiguration() {
        searchConfiguration.apply {
            setActivity(this@MainActivity)
            setFragmentContainerViewId(R.id.fragmentContainerView)

            searchableFragments.forEach {
                index(it.xml).addBreadcrumb(resources.getString(it.title))
            }

            setBreadcrumbsEnabled(true)
            setHistoryEnabled(true)
            setFuzzySearchEnabled(false)
        }
    }

    private fun getTopFragmentTag(): String {
        var fragment = UUID.randomUUID().toString()

        val last: Int = supportFragmentManager.fragments.size - 1

        if (last >= 0) {
            when (val topFragment = supportFragmentManager.fragments[last]) {
                is Home -> {
                    fragment = Home::class.java.simpleName
                }

                is Tweaks -> {
                    fragment = Tweaks::class.java.simpleName
                }

                is Settings -> {
                    fragment = Settings::class.java.simpleName
                }

                else -> {
                    fragment = topFragment.tag ?: UUID.randomUUID().toString()
                }
            }
        }

        return fragment
    }

    fun showColorPickerDialog(
        dialogId: Int,
        defaultColor: Int,
        showPresets: Boolean,
        showAlphaSlider: Boolean,
        showColorShades: Boolean
    ) {
        colorPickerDialog
            .setColor(defaultColor)
            .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
            .setAllowCustom(false)
            .setAllowPresets(showPresets)
            .setDialogId(dialogId)
            .setShowAlphaSlider(showAlphaSlider)
            .setShowColorShades(showColorShades)

        colorPickerDialog.show(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (selectedFragment != null) {
            outState.putInt(SELECTED_FRAGMENT_KEY, selectedFragment!!)
        }

        outState.putBoolean(REQUIRE_SYSTEMUI_RESTART_KEY, Dynamic.requiresSystemUiRestart)
        outState.putBoolean(REQUIRE_DEVICE_RESTART_KEY, Dynamic.requiresDeviceRestart)

        super.onSaveInstanceState(outState)
    }

    public override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        selectedFragment = savedInstanceState.getInt(SELECTED_FRAGMENT_KEY)
        Dynamic.requiresSystemUiRestart =
            savedInstanceState.getBoolean(REQUIRE_SYSTEMUI_RESTART_KEY)
        Dynamic.requiresDeviceRestart = savedInstanceState.getBoolean(REQUIRE_DEVICE_RESTART_KEY)

        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onColorSelected(dialogId: Int, color: Int) {
        EventBus.getDefault().post(ColorSelectedEvent(dialogId, color))
    }

    override fun onDialogDismissed(dialogId: Int) {
        EventBus.getDefault().post(ColorDismissedEvent(dialogId))
    }

    @Suppress("deprecation")
    override fun onPreferenceStartFragment(
        caller: PreferenceFragmentCompat,
        pref: Preference
    ): Boolean {
        replaceFragment(
            supportFragmentManager.fragmentFactory.instantiate(
                classLoader, pref.fragment!!
            ).apply {
                arguments = pref.extras
                setTargetFragment(caller, 0)
            }
        )
        return true
    }

    override fun onSearchResultClicked(result: SearchPreferenceResult) {
        Handler(mainLooper).post {
            val lastFragment = getLastFragment(excludeSearchFragment = true)

            (lastFragment as? BaseFragment)?.onSearchResultClicked(result)
                ?: (lastFragment as? ControlledPreferenceFragmentCompat)
                    ?.onSearchResultClicked(result)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(this, R.id.fragmentContainerView)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemID = item.itemId

        if (itemID == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }

        return false
    }

    companion object {
        @Suppress("StaticFieldLeak")
        private lateinit var binding: ActivityHomePageBinding
        private const val SELECTED_FRAGMENT_KEY = "mSelectedFragmentKey"
        private const val REQUIRE_SYSTEMUI_RESTART_KEY = "mSystemUiRestartKey"
        private const val REQUIRE_DEVICE_RESTART_KEY = "mDeviceRestartKey"
        private lateinit var myFragmentManager: FragmentManager
        private var myActionBar: ActionBar? = null
        private var selectedFragment: Int? = null
        private lateinit var colorPickerDialog: ColorPickerDialog.Builder

        fun replaceFragment(fragment: Fragment) {
            val fragmentTag = fragment.javaClass.simpleName
            var currentFragment = myFragmentManager.findFragmentById(R.id.fragmentContainerView)

            if (currentFragment != null &&
                currentFragment.javaClass.simpleName == SearchPreferenceFragment::class.java.simpleName
            ) {
                myFragmentManager.popBackStack()
                currentFragment = myFragmentManager.findFragmentById(R.id.fragmentContainerView)
            }

            if (currentFragment != null &&
                currentFragment.javaClass.simpleName == fragmentTag
            ) {
                popCurrentFragment()
            }

            try {
                myFragmentManager.popBackStackImmediate(
                    fragmentTag,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                )
            } catch (ignored: IllegalStateException) {
            }

            val fragmentTransaction: FragmentTransaction = myFragmentManager.beginTransaction()

            fragmentTransaction.setCustomAnimations(
                R.anim.fragment_fade_in,
                R.anim.fragment_fade_out,
                R.anim.fragment_fade_in,
                R.anim.fragment_fade_out
            )

            fragmentTransaction.replace(R.id.fragmentContainerView, fragment, fragmentTag)

            when {
                fragmentTag == Home::class.java.simpleName ||
                        fragmentTag == Tweaks::class.java.simpleName ||
                        fragmentTag == Xposed::class.java.simpleName ||
                        fragmentTag == Settings::class.java.simpleName -> {
                    myFragmentManager.popBackStack(Home::class.java.simpleName, 0)
                    fragmentTransaction.addToBackStack(fragmentTag)
                }

                else -> {
                    fragmentTransaction.addToBackStack(fragmentTag)
                }
            }

            fragmentTransaction.commit()
        }

        private fun getLastFragment(excludeSearchFragment: Boolean = false): Fragment? {
            val index = myFragmentManager.backStackEntryCount - 1
            var backEntry = myFragmentManager.getBackStackEntryAt(index)
            var fragment = myFragmentManager.findFragmentByTag(backEntry.name)

            if (excludeSearchFragment && fragment is SearchPreferenceFragment) {
                backEntry = myFragmentManager.getBackStackEntryAt(index - 1)
                fragment = myFragmentManager.findFragmentByTag(backEntry.name)
            }

            return fragment
        }

        fun popCurrentFragment() {
            myFragmentManager.popBackStack()
        }

        @JvmStatic
        fun showOrHidePendingActionButton(
            requiresSystemUiRestart: Boolean = false,
            requiresDeviceRestart: Boolean = false,
        ) {
            Dynamic.requiresSystemUiRestart =
                requiresSystemUiRestart || Dynamic.requiresSystemUiRestart
            Dynamic.requiresDeviceRestart = requiresDeviceRestart || Dynamic.requiresDeviceRestart

            try {
                if (!Dynamic.requiresSystemUiRestart && !Dynamic.requiresDeviceRestart) {
                    binding.hideAll.hide()
                    binding.hideAllText.fadeOut()
                    binding.restartSystemui.hide()
                    binding.restartSystemuiText.fadeOut()
                    binding.restartDevice.hide()
                    binding.restartDeviceText.fadeOut()
                    binding.pendingActions.hide()
                    binding.pendingActions.shrink()
                } else {
                    if (binding.hideAll.isShown && Dynamic.requiresSystemUiRestart && !binding.restartSystemui.isShown) {
                        binding.restartSystemui.show()
                        binding.restartSystemuiText.fadeIn()
                    } else if (!Dynamic.requiresSystemUiRestart && binding.restartSystemui.isShown) {
                        binding.restartSystemui.hide()
                        binding.restartSystemuiText.fadeOut()
                    }

                    if (binding.hideAll.isShown && Dynamic.requiresDeviceRestart && !binding.restartDevice.isShown) {
                        binding.restartDevice.show()
                        binding.restartDeviceText.fadeIn()
                    } else if (!Dynamic.requiresDeviceRestart && binding.restartDevice.isShown) {
                        binding.restartDevice.hide()
                        binding.restartDeviceText.fadeOut()
                    }

                    if (!binding.hideAll.isShown) {
                        binding.pendingActions.shrink()
                    } else {
                        binding.pendingActions.extend()
                    }

                    binding.pendingActions.show()
                }
            } catch (_: Exception) {
            }
        }

        private fun showOrHideFabButtons() {
            try {
                val pendingActionsShown = binding.pendingActions.isShown
                var isAnyButtonShown: Boolean

                if (!binding.hideAll.isShown && pendingActionsShown) {
                    binding.hideAll.show()
                    binding.hideAllText.fadeIn()
                    isAnyButtonShown = true
                } else {
                    binding.hideAll.hide()
                    binding.hideAllText.fadeOut()
                    isAnyButtonShown = false
                }

                if (!binding.restartSystemui.isShown && Dynamic.requiresSystemUiRestart && pendingActionsShown) {
                    binding.restartSystemui.show()
                    binding.restartSystemuiText.fadeIn()
                    isAnyButtonShown = true
                } else {
                    binding.restartSystemui.hide()
                    binding.restartSystemuiText.fadeOut()
                    isAnyButtonShown = isAnyButtonShown || false
                }

                if (!binding.restartDevice.isShown && Dynamic.requiresDeviceRestart && pendingActionsShown) {
                    binding.restartDevice.show()
                    binding.restartDeviceText.fadeIn()
                    isAnyButtonShown = true
                } else {
                    binding.restartDevice.hide()
                    binding.restartDeviceText.fadeOut()
                    isAnyButtonShown = isAnyButtonShown || false
                }

                if (isAnyButtonShown) {
                    binding.pendingActions.extend()
                } else {
                    binding.pendingActions.shrink()
                }
            } catch (_: Exception) {
            }
        }

        private fun View.fadeIn(duration: Long = 300) {
            this.apply {
                alpha = 0f
                visibility = View.VISIBLE
                animate()
                    .alpha(1f)
                    .setDuration(duration)
                    .setListener(null)
            }
        }

        private fun View.fadeOut(duration: Long = 300) {
            this.apply {
                animate()
                    .alpha(0f)
                    .setDuration(duration)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            visibility = View.GONE
                        }
                    })
            }
        }

        val searchConfiguration = SearchConfiguration()

        val searchableFragments = arrayOf(
            SearchPreferenceItem(
                R.xml.home_page,
                R.string.navbar_home,
                Home()
            ),
            SearchPreferenceItem(
                R.xml.tweaks,
                R.string.navbar_tweaks,
                Tweaks()
            ),
            SearchPreferenceItem(
                R.xml.xposed,
                R.string.navbar_xposed,
                Xposed()
            ),
            SearchPreferenceItem(
                R.xml.settings,
                R.string.navbar_settings,
                Settings()
            ),
            SearchPreferenceItem(
                R.xml.xposed_transparency_blur,
                R.string.activity_title_transparency_blur,
                TransparencyBlur()
            ),
            SearchPreferenceItem(
                R.xml.xposed_quick_settings,
                R.string.activity_title_quick_settings,
                QuickSettings()
            ),
            SearchPreferenceItem(
                R.xml.xposed_statusbar,
                R.string.activity_title_statusbar,
                Statusbar()
            ),
            SearchPreferenceItem(
                R.xml.xposed_volume_panel,
                R.string.activity_title_volume_panel,
                VolumePanel()
            ),
            SearchPreferenceItem(
                R.xml.xposed_header_clock,
                R.string.activity_title_header_clock,
                HeaderClock()
            ),
            SearchPreferenceItem(
                R.xml.xposed_lockscreen_clock,
                R.string.activity_title_lockscreen_clock,
                LockscreenClock()
            ),
            SearchPreferenceItem(
                R.xml.xposed_others,
                R.string.activity_title_xposed_others,
                Others()
            ),
        )
    }
}