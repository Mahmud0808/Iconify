package com.drdisagree.iconify.ui.fragments

import android.app.Activity
import android.os.Build
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
import com.drdisagree.iconify.common.Preferences.CUSTOM_DEPTH_WALLPAPER_SWITCH
import com.drdisagree.iconify.common.Preferences.DEPTH_WALLPAPER_BACKGROUND_MOVEMENT_MULTIPLIER
import com.drdisagree.iconify.common.Preferences.DEPTH_WALLPAPER_CHANGED
import com.drdisagree.iconify.common.Preferences.DEPTH_WALLPAPER_FADE_ANIMATION
import com.drdisagree.iconify.common.Preferences.DEPTH_WALLPAPER_FOREGROUND_ALPHA
import com.drdisagree.iconify.common.Preferences.DEPTH_WALLPAPER_FOREGROUND_MOVEMENT_MULTIPLIER
import com.drdisagree.iconify.common.Preferences.DEPTH_WALLPAPER_ON_AOD
import com.drdisagree.iconify.common.Preferences.DEPTH_WALLPAPER_PARALLAX_EFFECT
import com.drdisagree.iconify.common.Preferences.DEPTH_WALLPAPER_SWITCH
import com.drdisagree.iconify.common.Preferences.UNZOOM_DEPTH_WALLPAPER
import com.drdisagree.iconify.common.Resources.DEPTH_WALL_BG_DIR
import com.drdisagree.iconify.common.Resources.DEPTH_WALL_FG_DIR
import com.drdisagree.iconify.config.RPrefs
import com.drdisagree.iconify.config.RPrefs.getBoolean
import com.drdisagree.iconify.config.RPrefs.getInt
import com.drdisagree.iconify.config.RPrefs.putBoolean
import com.drdisagree.iconify.config.RPrefs.putInt
import com.drdisagree.iconify.databinding.FragmentXposedDepthWallpaperBinding
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader
import com.drdisagree.iconify.utils.FileUtil.getRealPath
import com.drdisagree.iconify.utils.FileUtil.moveToIconifyHiddenDir
import com.drdisagree.iconify.utils.SystemUtil
import com.drdisagree.iconify.xposed.modules.utils.BitmapSubjectSegmenter
import com.google.android.gms.common.moduleinstall.ModuleAvailabilityResponse
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

        // Enable depth wallpaper
        binding.depthWallpaper.isSwitchChecked = getBoolean(DEPTH_WALLPAPER_SWITCH, false)
        binding.depthWallpaper.setSwitchChangeListener { _: CompoundButton?, isSwitchChecked: Boolean ->
            putBoolean(DEPTH_WALLPAPER_SWITCH, isSwitchChecked)
            updateEnabledState()

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU) {
                Handler(Looper.getMainLooper()).postDelayed(
                    { SystemUtil.handleSystemUIRestart() },
                    SWITCH_ANIMATION_DELAY
                )
            }
        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU) {
            binding.depthWallpaper.setSummary(
                getString(
                    R.string.enable_depth_wallpaper_desc,
                    getString(R.string.use_custom_lockscreen_clock)
                )
            )
        } else {
            BitmapSubjectSegmenter(requireContext())
                .checkModelAvailability { moduleAvailabilityResponse: ModuleAvailabilityResponse? ->
                    binding.depthWallpaper.setSummary(
                        getString(
                            R.string.enable_depth_wallpaper_desc,
                            getString(
                                if (moduleAvailabilityResponse?.areModulesAvailable() == true) {
                                    R.string.depth_wallpaper_model_ready
                                } else {
                                    R.string.depth_wallpaper_model_not_available
                                }
                            )
                        )
                    )
                }
        }

        // Custom depth wallpaper
        binding.customDepthWallpaper.visibility =
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU) {
                View.GONE
            } else {
                View.VISIBLE
            }
        binding.customDepthWallpaper.isSwitchChecked = getBoolean(
            CUSTOM_DEPTH_WALLPAPER_SWITCH,
            false
        )
        binding.customDepthWallpaper.setSwitchChangeListener { _: CompoundButton?, isSwitchChecked: Boolean ->
            putBoolean(CUSTOM_DEPTH_WALLPAPER_SWITCH, isSwitchChecked)
            updateEnabledState()
        }

        // Foreground image
        binding.foregroundImage.setEnabled(binding.depthWallpaper.isSwitchChecked)
        binding.foregroundImage.setActivityResultLauncher(intentForegroundImage)

        // Background image
        binding.backgroundImage.setEnabled(binding.depthWallpaper.isSwitchChecked)
        binding.backgroundImage.setActivityResultLauncher(intentBackgroundImage)

        // Foreground alpha
        binding.foregroundAlpha.setEnabled(binding.depthWallpaper.isSwitchChecked)
        binding.foregroundAlpha.sliderValue = getInt(DEPTH_WALLPAPER_FOREGROUND_ALPHA, 80)
        binding.foregroundAlpha.setOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}

            override fun onStopTrackingTouch(slider: Slider) {
                putInt(DEPTH_WALLPAPER_FOREGROUND_ALPHA, slider.value.toInt())
            }
        })

        // Show on AOD
        binding.depthWallpaperOnAod.visibility =
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU) {
                View.GONE
            } else {
                View.VISIBLE
            }
        binding.depthWallpaperOnAod.setEnabled(binding.depthWallpaper.isSwitchChecked)
        binding.depthWallpaperOnAod.isSwitchChecked = getBoolean(DEPTH_WALLPAPER_ON_AOD, true)
        binding.depthWallpaperOnAod.setSwitchChangeListener { _: CompoundButton?, isSwitchChecked: Boolean ->
            putBoolean(DEPTH_WALLPAPER_ON_AOD, isSwitchChecked)
        }

        // Fade animation
        binding.wallpaperFadeAnimation.visibility =
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU) {
                View.VISIBLE
            } else {
                View.GONE
            }
        binding.wallpaperFadeAnimation.setEnabled(binding.depthWallpaper.isSwitchChecked)
        binding.wallpaperFadeAnimation.isSwitchChecked =
            getBoolean(DEPTH_WALLPAPER_FADE_ANIMATION, false)
        binding.wallpaperFadeAnimation.setSwitchChangeListener { _: CompoundButton?, isSwitchChecked: Boolean ->
            putBoolean(DEPTH_WALLPAPER_FADE_ANIMATION, isSwitchChecked)
        }

        // Parallax effect
        binding.parallaxEffect.visibility =
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU) {
                View.VISIBLE
            } else {
                View.GONE
            }
        binding.parallaxEffect.setEnabled(binding.depthWallpaper.isSwitchChecked)
        binding.parallaxEffect.isSwitchChecked = getBoolean(DEPTH_WALLPAPER_PARALLAX_EFFECT, false)
        binding.parallaxEffect.setSwitchChangeListener { _: CompoundButton?, isSwitchChecked: Boolean ->
            putBoolean(DEPTH_WALLPAPER_PARALLAX_EFFECT, isSwitchChecked)
            updateEnabledState()
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

        // Unzoom depth wallpaper
        binding.unzoomDepthWallpaper.isSwitchChecked = getBoolean(UNZOOM_DEPTH_WALLPAPER, false)
        binding.unzoomDepthWallpaper.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            putBoolean(UNZOOM_DEPTH_WALLPAPER, isChecked)

            Handler(Looper.getMainLooper()).postDelayed(
                { SystemUtil.restartSystemUI() },
                SWITCH_ANIMATION_DELAY
            )
        }

        updateEnabledState()

        return view
    }

    private fun updateEnabledState() {
        val isDepthWallpaperEnabled = binding.depthWallpaper.isSwitchChecked

        binding.customDepthWallpaper.setEnabled(isDepthWallpaperEnabled)
        binding.depthWallpaperOnAod.setEnabled(isDepthWallpaperEnabled)
        binding.wallpaperFadeAnimation.setEnabled(isDepthWallpaperEnabled)
        binding.foregroundImage.setEnabled(isDepthWallpaperEnabled)
        binding.backgroundImage.setEnabled(isDepthWallpaperEnabled)
        binding.foregroundAlpha.setEnabled(isDepthWallpaperEnabled)
        binding.parallaxEffect.setEnabled(isDepthWallpaperEnabled)
        binding.foregroundSensitivity.setEnabled(isDepthWallpaperEnabled)
        binding.backgroundSensitivity.setEnabled(isDepthWallpaperEnabled)
        binding.unzoomDepthWallpaper.setEnabled(isDepthWallpaperEnabled)

        val isBelowA14Feature = Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU

        binding.wallpaperFadeAnimation.visibility = if (isBelowA14Feature) {
            View.VISIBLE
        } else {
            View.GONE
        }
        binding.parallaxEffect.visibility = if (isBelowA14Feature) {
            View.VISIBLE
        } else {
            View.GONE
        }
        binding.unzoomDepthWallpaper.visibility = if (isBelowA14Feature) {
            View.VISIBLE
        } else {
            View.GONE
        }
        binding.depthWallpaperOnAod.visibility = if (isBelowA14Feature) {
            View.GONE
        } else {
            View.VISIBLE
        }

        val isParallaxEffectEnabled = binding.parallaxEffect.isSwitchChecked
                && isBelowA14Feature

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

        val isImagePickersEnabled = binding.customDepthWallpaper.isSwitchChecked
                || isBelowA14Feature

        binding.foregroundImage.visibility = if (isImagePickersEnabled) {
            View.VISIBLE
        } else {
            View.GONE
        }
        binding.backgroundImage.visibility = if (isImagePickersEnabled) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }
}