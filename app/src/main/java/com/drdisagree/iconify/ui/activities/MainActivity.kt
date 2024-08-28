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
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.airbnb.lottie.LottieCompositionFactory
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Dynamic
import com.drdisagree.iconify.common.Preferences
import com.drdisagree.iconify.common.Preferences.MONET_ENGINE_SWITCH
import com.drdisagree.iconify.common.Preferences.ON_HOME_PAGE
import com.drdisagree.iconify.common.Resources.searchConfiguration
import com.drdisagree.iconify.common.Resources.searchableFragments
import com.drdisagree.iconify.config.RPrefs
import com.drdisagree.iconify.databinding.ActivityMainBinding
import com.drdisagree.iconify.ui.base.BaseActivity
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.base.ControlledPreferenceFragmentCompat
import com.drdisagree.iconify.ui.events.ColorDismissedEvent
import com.drdisagree.iconify.ui.events.ColorSelectedEvent
import com.drdisagree.iconify.ui.fragments.home.Home
import com.drdisagree.iconify.ui.fragments.settings.Settings
import com.drdisagree.iconify.ui.fragments.tweaks.BasicColors
import com.drdisagree.iconify.ui.fragments.tweaks.ColorEngine
import com.drdisagree.iconify.ui.fragments.tweaks.MonetEngine
import com.drdisagree.iconify.ui.fragments.tweaks.Tweaks
import com.drdisagree.iconify.ui.fragments.xposed.BackgroundChip
import com.drdisagree.iconify.ui.fragments.xposed.ClockChip
import com.drdisagree.iconify.ui.fragments.xposed.LockscreenWidget
import com.drdisagree.iconify.ui.fragments.xposed.WeatherSettings
import com.drdisagree.iconify.ui.fragments.xposed.Xposed
import com.drdisagree.iconify.ui.preferences.preferencesearch.SearchPreferenceFragment
import com.drdisagree.iconify.ui.preferences.preferencesearch.SearchPreferenceResult
import com.drdisagree.iconify.ui.preferences.preferencesearch.SearchPreferenceResultListener
import com.drdisagree.iconify.ui.utils.FragmentHelper.isInGroup
import com.drdisagree.iconify.utils.HapticUtils.weakVibrate
import com.drdisagree.iconify.utils.SystemUtils
import com.drdisagree.iconify.utils.overlay.FabricatedUtils
import com.drdisagree.iconify.utils.overlay.OverlayUtils
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.util.UUID

class MainActivity : BaseActivity(),
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback,
    SearchPreferenceResultListener,
    ColorPickerDialogListener {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
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
        CoroutineScope(Dispatchers.IO).launch {
            // Clear lottie cache
            LottieCompositionFactory.clearCache(this@MainActivity)

            // Get list of enabled overlays
            val enabledOverlays = OverlayUtils.enabledOverlayList
            enabledOverlays.forEach { overlay ->
                RPrefs.putBoolean(overlay, true)
            }

            val fabricatedEnabledOverlays = FabricatedUtils.enabledOverlayList
            fabricatedEnabledOverlays.forEach { overlay ->
                if (!RPrefs.getBoolean("fabricated$overlay", false)) {
                    RPrefs.putBoolean("fabricated$overlay", true)
                }
            }

            RPrefs.putBoolean(
                MONET_ENGINE_SWITCH,
                enabledOverlays.contains("IconifyComponentME.overlay")
            )
        }
    }

    private fun setupFloatingActionButtons() {
        binding.hideAll.hide()
        binding.restartSystemui.hide()
        binding.restartDevice.hide()
        binding.pendingActions.shrink()

        showOrHidePendingActionButton(
            activityBinding = binding,
            requiresSystemUiRestart = Dynamic.requiresSystemUiRestart,
            requiresDeviceRestart = Dynamic.requiresDeviceRestart
        )

        binding.pendingActions.setOnClickListener {
            binding.pendingActions.weakVibrate()
            showOrHideFabButtons()
        }

        binding.hideAll.setOnClickListener {
            Dynamic.requiresSystemUiRestart = false
            Dynamic.requiresDeviceRestart = false

            showOrHidePendingActionButton(
                activityBinding = binding,
                requiresSystemUiRestart = false,
                requiresDeviceRestart = false
            )
        }

        binding.restartSystemui.setOnClickListener {
            Dynamic.requiresSystemUiRestart = false

            showOrHidePendingActionButton(
                activityBinding = binding,
                requiresSystemUiRestart = false,
                requiresDeviceRestart = Dynamic.requiresDeviceRestart
            )

            Handler(Looper.getMainLooper()).postDelayed({
                SystemUtils.restartSystemUI()
            }, 500)
        }

        binding.restartDevice.setOnClickListener {
            Dynamic.requiresDeviceRestart = false

            showOrHidePendingActionButton(
                activityBinding = binding,
                requiresSystemUiRestart = Dynamic.requiresSystemUiRestart,
                requiresDeviceRestart = false
            )

            Handler(Looper.getMainLooper()).postDelayed({
                SystemUtils.restartDevice()
            }, android.R.integer.config_longAnimTime.toLong())
        }
    }

    private fun setupNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (myFragmentManager.backStackEntryCount > 1) {
                    popCurrentFragment()
                } else {
                    handleBackPress()
                }
            }
        })

        if (Preferences.isXposedOnlyMode) {
            binding.bottomNavigationView.menu.clear()
            binding.bottomNavigationView.inflateMenu(R.menu.bottom_nav_menu_xposed_only)
        }

        supportFragmentManager.addOnBackStackChangedListener {
            val fragment = getTopFragment()
            val xposedOnlyMode = Preferences.isXposedOnlyMode

            val homeIndex = 0
            val tweaksIndex = 1
            val xposedIndex = if (!xposedOnlyMode) 2 else 0
            val settingsIndex = if (!xposedOnlyMode) 3 else 1

            when {
                isInGroup(fragment, homeIndex) && !xposedOnlyMode -> {
                    binding.bottomNavigationView.menu.getItem(homeIndex).setChecked(true)
                }

                isInGroup(fragment, tweaksIndex) && !xposedOnlyMode -> {
                    binding.bottomNavigationView.menu.getItem(tweaksIndex).setChecked(true)
                }

                isInGroup(fragment, xposedIndex) -> {
                    binding.bottomNavigationView.menu.getItem(xposedIndex).setChecked(true)
                }

                isInGroup(fragment, settingsIndex) -> {
                    binding.bottomNavigationView.menu.getItem(settingsIndex).setChecked(true)
                }
            }
        }

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            val fragmentTag: String = getTopFragmentTag()

            when (item.itemId) {
                R.id.homePage -> {
                    if (fragmentTag != Home::class.java.simpleName) {
                        replaceFragment(Home())
                        binding.bottomNavigationView.weakVibrate()
                    }
                    return@setOnItemSelectedListener true
                }

                R.id.tweaks -> {
                    if (fragmentTag != Tweaks::class.java.simpleName) {
                        replaceFragment(Tweaks())
                        binding.bottomNavigationView.weakVibrate()
                    }
                    return@setOnItemSelectedListener true
                }

                R.id.xposed -> {
                    if (fragmentTag != Xposed::class.java.simpleName) {
                        replaceFragment(Xposed())
                        binding.bottomNavigationView.weakVibrate()
                    }
                    return@setOnItemSelectedListener true
                }

                R.id.settings -> {
                    if (fragmentTag != Settings::class.java.simpleName) {
                        replaceFragment(Settings())
                        binding.bottomNavigationView.weakVibrate()
                    }
                    return@setOnItemSelectedListener true
                }

                else -> {
                    return@setOnItemSelectedListener false
                }
            }
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

    private fun handleBackPress() {
        val topFragment = getTopFragment()
        val xposedOnlyMode = Preferences.isXposedOnlyMode

        val homeIndex = 0
        val tweaksIndex = 1
        val xposedIndex = if (!xposedOnlyMode) 2 else 0
        val settingsIndex = if (!xposedOnlyMode) 3 else 1

        when {
            topFragment is BasicColors || topFragment is MonetEngine -> {
                clearAndReplaceFragment(ColorEngine())
            }

            topFragment is WeatherSettings -> {
                clearAndReplaceFragment(LockscreenWidget())
            }

            topFragment is ClockChip -> {
                clearAndReplaceFragment(BackgroundChip())
            }

            isInGroup(topFragment, homeIndex) && topFragment !is Home -> {
                clearAndReplaceFragment(Home())
            }

            isInGroup(topFragment, tweaksIndex) && topFragment !is Tweaks -> {
                clearAndReplaceFragment(Tweaks())
            }

            isInGroup(topFragment, xposedIndex) && topFragment !is Xposed -> {
                clearAndReplaceFragment(Xposed())
            }

            isInGroup(topFragment, settingsIndex) && topFragment !is Settings -> {
                clearAndReplaceFragment(Settings())
            }

            (topFragment is Settings || topFragment is Xposed || topFragment is Tweaks) -> {
                clearAndReplaceFragment(Home())
            }

            else -> {
                finish()
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

    private fun getTopFragment(): Fragment {
        val last: Int = supportFragmentManager.fragments.size - 1

        if (last >= 0) {
            return supportFragmentManager.fragments[last]
        }

        return Home()
    }

    private fun getTopFragmentTag(): String {
        return getTopFragment().tag ?: UUID.randomUUID().toString()
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemID = item.itemId

        if (itemID == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }

        return false
    }

    companion object {
        private lateinit var myFragmentManager: FragmentManager
        private var myActionBar: ActionBar? = null
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

            fragmentTransaction.commitAllowingStateLoss()
        }

        private fun clearAndReplaceFragment(fragment: Fragment) {
            while (myFragmentManager.backStackEntryCount > 0) {
                myFragmentManager.popBackStackImmediate()
            }
            replaceFragment(fragment)
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

        fun showOrHidePendingActionButton(
            activityBinding: ActivityMainBinding, // Pass the binding as a parameter
            requiresSystemUiRestart: Boolean = Dynamic.requiresSystemUiRestart,
            requiresDeviceRestart: Boolean = Dynamic.requiresDeviceRestart,
        ) {
            Dynamic.requiresSystemUiRestart =
                requiresSystemUiRestart || Dynamic.requiresSystemUiRestart
            Dynamic.requiresDeviceRestart = requiresDeviceRestart || Dynamic.requiresDeviceRestart

            try {
                with(activityBinding) {
                    if (!Dynamic.requiresSystemUiRestart && !Dynamic.requiresDeviceRestart) {
                        hideAll.hide()
                        hideAllText.fadeOut()
                        restartSystemui.hide()
                        restartSystemuiText.fadeOut()
                        restartDevice.hide()
                        restartDeviceText.fadeOut()
                        pendingActions.hide()
                        pendingActions.shrink()
                    } else {
                        if (hideAll.isShown && Dynamic.requiresSystemUiRestart && !restartSystemui.isShown) {
                            restartSystemui.show()
                            restartSystemuiText.fadeIn()
                        } else if (!Dynamic.requiresSystemUiRestart && restartSystemui.isShown) {
                            restartSystemui.hide()
                            restartSystemuiText.fadeOut()
                        }

                        if (hideAll.isShown && Dynamic.requiresDeviceRestart && !restartDevice.isShown) {
                            restartDevice.show()
                            restartDeviceText.fadeIn()
                        } else if (!Dynamic.requiresDeviceRestart && restartDevice.isShown) {
                            restartDevice.hide()
                            restartDeviceText.fadeOut()
                        }

                        if (!hideAll.isShown) {
                            pendingActions.shrink()
                        } else {
                            pendingActions.extend()
                        }

                        pendingActions.show()
                    }
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
    }
}