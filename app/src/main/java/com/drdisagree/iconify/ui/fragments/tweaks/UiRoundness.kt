package com.drdisagree.iconify.ui.fragments.tweaks

import android.content.res.Configuration
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.Iconify.Companion.appContextLocale
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Preferences.UI_CORNER_RADIUS
import com.drdisagree.iconify.config.RPrefs
import com.drdisagree.iconify.databinding.FragmentUiRoundnessBinding
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.dialogs.LoadingDialog
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader
import com.drdisagree.iconify.utils.SystemUtils.hasStoragePermission
import com.drdisagree.iconify.utils.SystemUtils.requestStoragePermission
import com.drdisagree.iconify.utils.overlay.manager.RoundnessManager.buildOverlay
import com.google.android.material.slider.Slider
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

class UiRoundness : BaseFragment() {

    private lateinit var binding: FragmentUiRoundnessBinding
    private var loadingDialog: LoadingDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUiRoundnessBinding.inflate(inflater, container, false)
        val view: View = binding.getRoot()

        // Header
        setHeader(
            requireContext(),
            getParentFragmentManager(),
            binding.header.toolbar,
            R.string.activity_title_ui_roundness
        )

        // Show loading dialog
        loadingDialog = LoadingDialog(requireContext())

        // Header
        setHeader(
            requireContext(),
            binding.header.toolbar,
            R.string.activity_title_ui_roundness
        )

        // Corner Radius
        val drawables = arrayOf(
            binding.qsTilePreview1.background as GradientDrawable,
            binding.qsTilePreview2.background as GradientDrawable,
            binding.qsTilePreview3.background as GradientDrawable,
            binding.qsTilePreview4.background as GradientDrawable,
            binding.brightnessBarBg.background as GradientDrawable,
            binding.brightnessBarFg.background as GradientDrawable,
            binding.autoBrightness.background as GradientDrawable
        )

        val finalUiCornerRadius = intArrayOf(RPrefs.getInt(UI_CORNER_RADIUS, 28))

        if (finalUiCornerRadius[0] == 28) {
            binding.cornerRadiusOutput.text =
                resources.getString(
                    R.string.opt_selected3,
                    finalUiCornerRadius[0].toString(),
                    "dp",
                    appContextLocale.resources.getString(R.string.opt_default)
                )
        } else {
            binding.cornerRadiusOutput.text =
                resources.getString(
                    R.string.opt_selected2,
                    finalUiCornerRadius[0].toString(),
                    "dp"
                )
        }

        for (drawable in drawables) {
            drawable.setCornerRadius(finalUiCornerRadius[0] * appContextLocale.resources.displayMetrics.density)
        }

        binding.cornerRadiusSeekbar.value = finalUiCornerRadius[0].toFloat()

        binding.cornerRadiusSeekbar.addOnSliderTouchListener(object :
            Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}

            override fun onStopTrackingTouch(slider: Slider) {
                finalUiCornerRadius[0] = slider.value.toInt()

                if (finalUiCornerRadius[0] == 28) {
                    binding.cornerRadiusOutput.text =
                        resources.getString(
                            R.string.opt_selected3,
                            finalUiCornerRadius[0].toString(),
                            "dp",
                            appContextLocale.resources.getString(R.string.opt_default)
                        )
                } else {
                    binding.cornerRadiusOutput.text =
                        resources.getString(
                            R.string.opt_selected2,
                            finalUiCornerRadius[0].toString(),
                            "dp"
                        )
                }
            }
        })

        binding.cornerRadiusSeekbar.addOnChangeListener(Slider.OnChangeListener { _: Slider?, value: Float, _: Boolean ->
            for (drawable in drawables) {
                drawable.setCornerRadius(value * appContextLocale.resources.displayMetrics.density)
            }
        })

        binding.applyRadius.setOnClickListener {
            if (!hasStoragePermission()) {
                requestStoragePermission(requireContext())
            } else {
                // Show loading dialog
                loadingDialog!!.show(resources.getString(R.string.loading_dialog_wait))

                val hasErroredOut = AtomicBoolean(false)

                Thread {
                    try {
                        hasErroredOut.set(buildOverlay(finalUiCornerRadius[0], true))
                    } catch (e: IOException) {
                        hasErroredOut.set(true)
                        Log.e("UiRoundness", e.toString())
                    }

                    Handler(Looper.getMainLooper()).post {
                        if (!hasErroredOut.get()) {
                            RPrefs.putInt(UI_CORNER_RADIUS, finalUiCornerRadius[0])
                            RPrefs.putInt(UI_CORNER_RADIUS, finalUiCornerRadius[0])
                        }

                        Handler(Looper.getMainLooper()).postDelayed({
                            // Hide loading dialog
                            loadingDialog!!.hide()

                            if (hasErroredOut.get()) Toast.makeText(
                                appContext,
                                appContextLocale.resources.getString(R.string.toast_error),
                                Toast.LENGTH_SHORT
                            ).show() else Toast.makeText(
                                appContext,
                                appContextLocale.resources.getString(R.string.toast_applied),
                                Toast.LENGTH_SHORT
                            ).show()
                        }, 2000)
                    }
                }.start()
            }
        }

        // Change orientation in landscape / portrait mode
        val orientation = this.resources.configuration.orientation

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.qsTileOrientation.orientation = LinearLayout.HORIZONTAL
        } else {
            binding.qsTileOrientation.orientation = LinearLayout.VERTICAL
        }

        return view
    }

    // Change tile orientation in landscape / portrait mode
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.qsTileOrientation.orientation = LinearLayout.HORIZONTAL
        } else {
            binding.qsTileOrientation.orientation = LinearLayout.VERTICAL
        }
    }

    override fun onDestroy() {
        loadingDialog?.dismiss()

        super.onDestroy()
    }
}