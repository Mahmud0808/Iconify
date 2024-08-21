package com.drdisagree.iconify.ui.activities

import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.Navigation.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
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
import com.drdisagree.iconify.ui.fragments.home.Home
import com.drdisagree.iconify.ui.fragments.settings.Settings
import com.drdisagree.iconify.ui.fragments.tweaks.Tweaks
import com.drdisagree.iconify.ui.preferences.preferencesearch.SearchPreferenceResult
import com.drdisagree.iconify.ui.preferences.preferencesearch.SearchPreferenceResultListener
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

    private lateinit var binding: ActivityHomePageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(binding.getRoot())

        colorPickerDialog = ColorPickerDialog.newBuilder()
        myFragmentManager = supportFragmentManager
        myActionBar = supportActionBar

        setupNavigation()
        Prefs.putBoolean(ON_HOME_PAGE, true)

        if (savedInstanceState == null) {
            replaceFragment(if (!Preferences.isXposedOnlyMode) Home() else Tweaks())
        }

        initData()
    }

    private fun initData() {
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
    }

    private fun setupNavigation() {
        if (Preferences.isXposedOnlyMode) {
            binding.bottomNavigationView.menu.clear()
            binding.bottomNavigationView.inflateMenu(R.menu.bottom_nav_menu_xposed_only)
        }

        supportFragmentManager.addOnBackStackChangedListener {
            val tag = getTopFragmentTag()
            val xposedOnlyMode = Preferences.isXposedOnlyMode

            val homeIndex = 0
            val tweaksIndex = if (xposedOnlyMode) 0 else 1
            val settingsIndex = if (xposedOnlyMode) 1 else 2

            when (tag) {
                Home::class.java.simpleName -> {
                    selectedFragment = R.id.homePage
                    binding.bottomNavigationView.menu.getItem(homeIndex).setChecked(true)
                }

                Tweaks::class.java.simpleName -> {
                    selectedFragment = R.id.tweaks
                    binding.bottomNavigationView.menu.getItem(tweaksIndex).setChecked(true)
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
        super.onSaveInstanceState(outState)

        if (selectedFragment != null) {
            outState.putInt(DATA_KEY, selectedFragment!!)
        }
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

    @Suppress("deprecation")
    override fun onPreferenceStartFragment(
        caller: PreferenceFragmentCompat,
        pref: Preference
    ): Boolean {
        val args = pref.extras
        val fragment = supportFragmentManager.fragmentFactory.instantiate(
            classLoader, pref.fragment!!
        )
        fragment.arguments = args
        fragment.setTargetFragment(caller, 0)

        replaceFragment(fragment)
        return true
    }

    override fun onSearchResultClicked(result: SearchPreferenceResult) {
        Handler(mainLooper).post { Tweaks().onSearchResultClicked(result) }
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
        private const val DATA_KEY = "mDataKey"
        private lateinit var myFragmentManager: FragmentManager
        private var myActionBar: ActionBar? = null
        private var selectedFragment: Int? = null
        private lateinit var colorPickerDialog: ColorPickerDialog.Builder
        val prefsList: List<Array<Any>> = ArrayList()

        @JvmStatic
        fun replaceFragment(fragment: Fragment) {
            val fragmentTag = fragment.javaClass.simpleName
            val fragmentTransaction: FragmentTransaction = myFragmentManager.beginTransaction()

            fragmentTransaction.setCustomAnimations(
                R.anim.fragment_fade_in,
                R.anim.fragment_fade_out,
                R.anim.fragment_fade_in,
                R.anim.fragment_fade_out
            )

            fragmentTransaction.replace(R.id.fragmentContainerView, fragment, fragmentTag)

            if (fragmentTag == Home::class.java.simpleName ||
                (fragmentTag == Tweaks::class.java.simpleName && Preferences.isXposedOnlyMode)
            ) {
                myFragmentManager.popBackStack(
                    null,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                )
            } else if (fragmentTag == Tweaks::class.java.simpleName ||
                fragmentTag == Settings::class.java.simpleName
            ) {
                myFragmentManager.popBackStack(
                    null,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                )
                fragmentTransaction.addToBackStack(fragmentTag)
            } else {
                fragmentTransaction.addToBackStack(fragmentTag)
            }

            fragmentTransaction.commit()
        }

        @JvmStatic
        fun backButtonEnabled() {
            myActionBar?.setDisplayHomeAsUpEnabled(true)
            myActionBar?.setDisplayShowHomeEnabled(true)
        }

        @JvmStatic
        fun backButtonDisabled() {
            myActionBar?.setDisplayHomeAsUpEnabled(false)
            myActionBar?.setDisplayShowHomeEnabled(false)
        }
    }
}