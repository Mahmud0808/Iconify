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
import com.drdisagree.iconify.common.Preferences.DEPTH_WALLPAPER_CHANGED
import com.drdisagree.iconify.common.Preferences.DEPTH_WALLPAPER_FADE_ANIMATION
import com.drdisagree.iconify.common.Preferences.DEPTH_WALLPAPER_SWITCH
import com.drdisagree.iconify.common.Resources.DEPTH_WALL_BG_DIR
import com.drdisagree.iconify.common.Resources.DEPTH_WALL_FG_DIR
import com.drdisagree.iconify.config.RPrefs.getBoolean
import com.drdisagree.iconify.config.RPrefs.putBoolean
import com.drdisagree.iconify.databinding.FragmentXposedDepthWallpaperBinding
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader
import com.drdisagree.iconify.utils.FileUtil.getRealPath
import com.drdisagree.iconify.utils.FileUtil.moveToIconifyHiddenDir
import com.drdisagree.iconify.utils.SystemUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder

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
            binding.wallpaperFadeAnimation.setEnabled(isSwitchChecked)
            binding.foregroundImage.setEnabled(isSwitchChecked)
            binding.backgroundImage.setEnabled(isSwitchChecked)

            Handler(Looper.getMainLooper()).postDelayed(
                { SystemUtil.handleSystemUIRestart() },
                SWITCH_ANIMATION_DELAY
            )
        }

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

        // Foreground image
        binding.foregroundImage.setEnabled(binding.depthWallpaper.isSwitchChecked)
        binding.foregroundImage.setActivityResultLauncher(intentForegroundImage)

        // Background image
        binding.backgroundImage.setEnabled(binding.depthWallpaper.isSwitchChecked)
        binding.backgroundImage.setActivityResultLauncher(intentBackgroundImage)

        return view
    }
}