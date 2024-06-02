package com.drdisagree.iconify.ui.fragments

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Preferences.HEADER_IMAGE_ALPHA
import com.drdisagree.iconify.common.Preferences.HEADER_IMAGE_BOTTOM_FADE_AMOUNT
import com.drdisagree.iconify.common.Preferences.HEADER_IMAGE_HEIGHT
import com.drdisagree.iconify.common.Preferences.HEADER_IMAGE_LANDSCAPE_SWITCH
import com.drdisagree.iconify.common.Preferences.HEADER_IMAGE_SWITCH
import com.drdisagree.iconify.common.Preferences.HEADER_IMAGE_ZOOMTOFIT
import com.drdisagree.iconify.common.Resources.HEADER_IMAGE_DIR
import com.drdisagree.iconify.config.RPrefs.getBoolean
import com.drdisagree.iconify.config.RPrefs.getInt
import com.drdisagree.iconify.config.RPrefs.putBoolean
import com.drdisagree.iconify.config.RPrefs.putInt
import com.drdisagree.iconify.databinding.FragmentXposedHeaderImageBinding
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader
import com.drdisagree.iconify.utils.FileUtil.getRealPath
import com.drdisagree.iconify.utils.FileUtil.moveToIconifyHiddenDir
import com.google.android.material.slider.Slider

class XposedHeaderImage : BaseFragment() {

    private lateinit var binding: FragmentXposedHeaderImageBinding

    private var startActivityIntent = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val path = getRealPath(data)

            if (path != null && moveToIconifyHiddenDir(path, HEADER_IMAGE_DIR)) {
                binding.headerImage.setEnableButtonVisibility(View.VISIBLE)
            } else {
                Toast.makeText(
                    appContext,
                    resources.getString(R.string.toast_rename_file),
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
        binding = FragmentXposedHeaderImageBinding.inflate(inflater, container, false)
        val view: View = binding.getRoot()

        // Header
        setHeader(
            requireContext(),
            getParentFragmentManager(),
            binding.header.toolbar,
            R.string.activity_title_header_image
        )

        // Header image picker
        binding.headerImage.setActivityResultLauncher(startActivityIntent)
        binding.headerImage.setEnableButtonOnClickListener {
            putBoolean(HEADER_IMAGE_SWITCH, false)
            putBoolean(HEADER_IMAGE_SWITCH, true)

            binding.headerImage.setEnableButtonVisibility(View.GONE)
            binding.headerImage.setDisableButtonVisibility(View.VISIBLE)

            updateEnabledState()
        }

        binding.headerImage.setDisableButtonVisibility(
            if (getBoolean(HEADER_IMAGE_SWITCH, false)) {
                View.VISIBLE
            } else {
                View.GONE
            }
        )

        binding.headerImage.setDisableButtonOnClickListener {
            putBoolean(HEADER_IMAGE_SWITCH, false)

            binding.headerImage.setDisableButtonVisibility(View.GONE)

            updateEnabledState()
        }

        // Image height
        binding.headerImageHeight.sliderValue = getInt(HEADER_IMAGE_HEIGHT, 140)
        binding.headerImageHeight.setOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}

            override fun onStopTrackingTouch(slider: Slider) {
                putInt(HEADER_IMAGE_HEIGHT, slider.value.toInt())
            }
        })

        // Image alpha
        binding.headerImageAlpha.sliderValue = getInt(HEADER_IMAGE_ALPHA, 100)
        binding.headerImageAlpha.setOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}

            override fun onStopTrackingTouch(slider: Slider) {
                putInt(HEADER_IMAGE_ALPHA, slider.value.toInt())
            }
        })

        // Image bottom fade amount
        binding.headerImageBottomFade.sliderValue =
            getInt(HEADER_IMAGE_BOTTOM_FADE_AMOUNT, 40)
        binding.headerImageBottomFade.setOnSliderTouchListener(object :
            Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}

            override fun onStopTrackingTouch(slider: Slider) {
                putInt(HEADER_IMAGE_BOTTOM_FADE_AMOUNT, slider.value.toInt())
            }
        })

        // Header image zoom to fit
        binding.zoomToFit.isSwitchChecked = getBoolean(HEADER_IMAGE_ZOOMTOFIT, false)
        binding.zoomToFit.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            putBoolean(HEADER_IMAGE_ZOOMTOFIT, isChecked)
        }

        // Header image hide in landscape
        binding.hideInLandscape.isSwitchChecked =
            getBoolean(HEADER_IMAGE_LANDSCAPE_SWITCH, true)
        binding.hideInLandscape.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            putBoolean(HEADER_IMAGE_LANDSCAPE_SWITCH, isChecked)
        }

        updateEnabledState()

        return view
    }

    private fun updateEnabledState() {
        val enabled = getBoolean(HEADER_IMAGE_SWITCH, false)

        binding.headerImageHeight.setEnabled(enabled)
        binding.headerImageAlpha.setEnabled(enabled)
        binding.headerImageBottomFade.setEnabled(enabled)
        binding.zoomToFit.setEnabled(enabled)
        binding.hideInLandscape.setEnabled(enabled)
    }
}