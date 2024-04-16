package com.drdisagree.iconify.ui.fragments

import android.app.Activity
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.Iconify.Companion.appContextLocale
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Const.SWITCH_ANIMATION_DELAY
import com.drdisagree.iconify.common.Preferences.DEPTH_WALLPAPER_BACKGROUND_MOVEMENT_MULTIPLIER
import com.drdisagree.iconify.common.Preferences.DEPTH_WALLPAPER_CHANGED
import com.drdisagree.iconify.common.Preferences.DEPTH_WALLPAPER_FADE_ANIMATION
import com.drdisagree.iconify.common.Preferences.DEPTH_WALLPAPER_FOREGROUND_MOVEMENT_MULTIPLIER
import com.drdisagree.iconify.common.Preferences.DEPTH_WALLPAPER_PARALLAX_EFFECT
import com.drdisagree.iconify.common.Preferences.DEPTH_WALLPAPER_SWITCH
import com.drdisagree.iconify.common.Resources.DEPTH_WALL_BG_DIR
import com.drdisagree.iconify.common.Resources.DEPTH_WALL_FG_DIR
import com.drdisagree.iconify.config.RPrefs
import com.drdisagree.iconify.config.RPrefs.getBoolean
import com.drdisagree.iconify.config.RPrefs.putBoolean
import com.drdisagree.iconify.databinding.FragmentXposedDepthWallpaperBinding
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader
import com.drdisagree.iconify.utils.FileUtil.getRealPath
import com.drdisagree.iconify.utils.FileUtil.moveToIconifyHiddenDir
import com.drdisagree.iconify.utils.SystemUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider

class XposedDepthWallpaper : BaseFragment() {

    private lateinit var binding: FragmentXposedDepthWallpaperBinding

    private var intentForegroundImage = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val path = getRealPath(data)

            if (path != null && moveToIconifyHiddenDir(path, DEPTH_WALL_FG_DIR)) {
                putBoolean(
                    DEPTH_WALLPAPER_CHANGED,
                    !binding.depthWallpaper.isSwitchChecked
                )
                putBoolean(
                    DEPTH_WALLPAPER_CHANGED,
                    binding.depthWallpaper.isSwitchChecked
                )

                Toast.makeText(
                    appContext,
                    appContextLocale.resources.getString(R.string.toast_selected_successfully),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    appContext,
                    appContextLocale.resources.getString(R.string.toast_rename_file),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private var intentBackgroundImage = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val path = getRealPath(data)

            if (path != null && moveToIconifyHiddenDir(path, DEPTH_WALL_BG_DIR)) {
                putBoolean(
                    DEPTH_WALLPAPER_CHANGED,
                    !binding.depthWallpaper.isSwitchChecked
                )
                putBoolean(
                    DEPTH_WALLPAPER_CHANGED,
                    binding.depthWallpaper.isSwitchChecked
                )

                Toast.makeText(
                    appContext,
                    appContextLocale.resources.getString(R.string.toast_selected_successfully),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    appContext,
                    appContextLocale.resources.getString(R.string.toast_rename_file),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentXposedDepthWallpaperBinding.inflate(inflater, container, false)
        val view: View = binding.getRoot()

        // Header
        setHeader(
            requireContext(),
            getParentFragmentManager(),
            binding.header.toolbar,
            R.string.activity_title_depth_wallpaper
        )

        // Alert dialog
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.attention))
            .setMessage(getString(R.string.depth_wallpaper_alert_msg))
            .setPositiveButton(getString(R.string.understood)) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
            .setCancelable(true)
            .show()

        // Enable depth wallpaper
        binding.depthWallpaper.isSwitchChecked = getBoolean(DEPTH_WALLPAPER_SWITCH, false)
        binding.depthWallpaper.setSwitchChangeListener { _: CompoundButton?, isSwitchChecked: Boolean ->
            putBoolean(DEPTH_WALLPAPER_SWITCH, isSwitchChecked)
            updateEnabledState()

            Handler(Looper.getMainLooper()).postDelayed(
                { SystemUtil.handleSystemUIRestart() },
                SWITCH_ANIMATION_DELAY
            )
        }

        // Foreground image
        binding.foregroundImage.setEnabled(binding.depthWallpaper.isSwitchChecked)
        binding.foregroundImage.setActivityResultLauncher(intentForegroundImage)

        // Background image
        binding.backgroundImage.setEnabled(binding.depthWallpaper.isSwitchChecked)
        binding.backgroundImage.setActivityResultLauncher(intentBackgroundImage)

        // Fade animation
        binding.wallpaperFadeAnimation.setEnabled(binding.depthWallpaper.isSwitchChecked)
        binding.wallpaperFadeAnimation.isSwitchChecked =
            getBoolean(DEPTH_WALLPAPER_FADE_ANIMATION, false)
        binding.wallpaperFadeAnimation.setSwitchChangeListener { _: CompoundButton?, isSwitchChecked: Boolean ->
            putBoolean(
                DEPTH_WALLPAPER_FADE_ANIMATION,
                isSwitchChecked
            )
        }

        // Parallax effect
        binding.parallaxEffect.setEnabled(binding.depthWallpaper.isSwitchChecked)
        binding.parallaxEffect.isSwitchChecked = getBoolean(DEPTH_WALLPAPER_PARALLAX_EFFECT, false)
        binding.parallaxEffect.setSwitchChangeListener { _: CompoundButton?, isSwitchChecked: Boolean ->
            putBoolean(DEPTH_WALLPAPER_PARALLAX_EFFECT, isSwitchChecked)
            updateEnabledState()
        }

        // Background sensitivity
        binding.backgroundSensitivity.sliderValue = RPrefs.getFloat(
            DEPTH_WALLPAPER_BACKGROUND_MOVEMENT_MULTIPLIER,
            1.0f
        ).toInt()
        binding.backgroundSensitivity.setOnSliderTouchListener(object :
            Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}

            override fun onStopTrackingTouch(slider: Slider) {
                RPrefs.putFloat(DEPTH_WALLPAPER_BACKGROUND_MOVEMENT_MULTIPLIER, slider.value)
            }
        })
        binding.backgroundSensitivity.setResetClickListener {
            RPrefs.clearPref(DEPTH_WALLPAPER_BACKGROUND_MOVEMENT_MULTIPLIER)
            true
        }

        // Foreground sensitivity
        binding.foregroundSensitivity.sliderValue = RPrefs.getFloat(
            DEPTH_WALLPAPER_FOREGROUND_MOVEMENT_MULTIPLIER,
            3.0f
        ).toInt()
        binding.foregroundSensitivity.setOnSliderTouchListener(object :
            Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}

            override fun onStopTrackingTouch(slider: Slider) {
                RPrefs.putFloat(DEPTH_WALLPAPER_FOREGROUND_MOVEMENT_MULTIPLIER, slider.value)
            }
        })
        binding.foregroundSensitivity.setResetClickListener {
            RPrefs.clearPref(DEPTH_WALLPAPER_FOREGROUND_MOVEMENT_MULTIPLIER)
            true
        }

        updateEnabledState()

        return view
    }

    private fun updateEnabledState() {
        val isDepthWallpaperEnabled = binding.depthWallpaper.isSwitchChecked

        binding.wallpaperFadeAnimation.setEnabled(isDepthWallpaperEnabled)
        binding.foregroundImage.setEnabled(isDepthWallpaperEnabled)
        binding.backgroundImage.setEnabled(isDepthWallpaperEnabled)
        binding.parallaxEffect.setEnabled(isDepthWallpaperEnabled)

        val isParallaxEffectEnabled = binding.parallaxEffect.isSwitchChecked

        binding.backgroundSensitivity.visibility = if (isParallaxEffectEnabled) {
            View.VISIBLE
        } else {
            View.GONE
        }
        binding.foregroundSensitivity.visibility = if (isParallaxEffectEnabled) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }
}