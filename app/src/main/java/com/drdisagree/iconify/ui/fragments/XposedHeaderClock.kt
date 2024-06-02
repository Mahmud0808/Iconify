package com.drdisagree.iconify.ui.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
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
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.Iconify.Companion.appContextLocale
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Const.SWITCH_ANIMATION_DELAY
import com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_CENTERED
import com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_COLOR_CODE_ACCENT1
import com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_COLOR_CODE_ACCENT2
import com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_COLOR_CODE_ACCENT3
import com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_COLOR_CODE_TEXT1
import com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_COLOR_CODE_TEXT2
import com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_COLOR_SWITCH
import com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_FONT_SWITCH
import com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_FONT_TEXT_SCALING
import com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_LANDSCAPE_SWITCH
import com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_SIDEMARGIN
import com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_STYLE
import com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_SWITCH
import com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_TOPMARGIN
import com.drdisagree.iconify.common.Resources.HEADER_CLOCK_FONT_DIR
import com.drdisagree.iconify.common.Resources.HEADER_CLOCK_LAYOUT
import com.drdisagree.iconify.config.RPrefs.clearPref
import com.drdisagree.iconify.config.RPrefs.clearPrefs
import com.drdisagree.iconify.config.RPrefs.getBoolean
import com.drdisagree.iconify.config.RPrefs.getInt
import com.drdisagree.iconify.config.RPrefs.putBoolean
import com.drdisagree.iconify.config.RPrefs.putInt
import com.drdisagree.iconify.databinding.FragmentXposedHeaderClockBinding
import com.drdisagree.iconify.ui.adapters.ClockPreviewAdapter
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.models.ClockModel
import com.drdisagree.iconify.ui.utils.CarouselLayoutManager
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader
import com.drdisagree.iconify.utils.FileUtil.getRealPath
import com.drdisagree.iconify.utils.FileUtil.moveToIconifyHiddenDir
import com.drdisagree.iconify.utils.SystemUtil
import com.google.android.material.slider.Slider

class XposedHeaderClock : BaseFragment() {

    private lateinit var binding: FragmentXposedHeaderClockBinding
    private var totalClocks: Int = 1

    private var startActivityIntent = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val path = getRealPath(data)

            if (path != null && moveToIconifyHiddenDir(path, HEADER_CLOCK_FONT_DIR)) {
                binding.headerClockFont.setEnableButtonVisibility(View.VISIBLE)
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
        binding = FragmentXposedHeaderClockBinding.inflate(inflater, container, false)
        val view: View = binding.getRoot()

        // Header
        setHeader(
            requireContext(),
            getParentFragmentManager(),
            binding.header.toolbar,
            R.string.activity_title_header_clock
        )

        // Enable header clock
        binding.enableHeaderClock.isSwitchChecked = getBoolean(HEADER_CLOCK_SWITCH, false)
        binding.enableHeaderClock.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            putBoolean(HEADER_CLOCK_SWITCH, isChecked)
            updateEnabled(isChecked)

            Handler(Looper.getMainLooper()).postDelayed(
                { SystemUtil.handleSystemUIRestart() },
                SWITCH_ANIMATION_DELAY
            )
        }
        updateEnabled(getBoolean(HEADER_CLOCK_SWITCH, false))

        // Header clock style
        val snapHelper: SnapHelper = LinearSnapHelper()
        val carouselLayoutManager = CarouselLayoutManager(
            requireContext(),
            RecyclerView.HORIZONTAL,
            false
        )
        carouselLayoutManager.setMinifyDistance(0.8f)

        binding.rvHeaderClockPreview.setLayoutManager(carouselLayoutManager)
        binding.rvHeaderClockPreview.setAdapter(initHeaderClockStyles())
        binding.rvHeaderClockPreview.setHasFixedSize(true)
        snapHelper.attachToRecyclerView(binding.rvHeaderClockPreview)

        // if index exceeds limit, set to highest available
        var headerClockStyle = getInt(HEADER_CLOCK_STYLE, 0)
        if (headerClockStyle >= totalClocks) {
            headerClockStyle = totalClocks - 1
            putInt(HEADER_CLOCK_STYLE, headerClockStyle)
        }
        binding.rvHeaderClockPreview.scrollToPosition(headerClockStyle)

        // Lockscreen clock font picker
        binding.headerClockFont.setActivityResultLauncher(startActivityIntent)
        binding.headerClockFont.setDisableButtonVisibility(
            if (getBoolean(
                    HEADER_CLOCK_FONT_SWITCH,
                    false
                )
            ) View.VISIBLE else View.GONE
        )
        binding.headerClockFont.setEnableButtonOnClickListener {
            putBoolean(HEADER_CLOCK_FONT_SWITCH, false)
            putBoolean(HEADER_CLOCK_FONT_SWITCH, true)

            binding.headerClockFont.setEnableButtonVisibility(View.GONE)
            binding.headerClockFont.setDisableButtonVisibility(View.VISIBLE)
        }
        binding.headerClockFont.setDisableButtonOnClickListener {
            putBoolean(HEADER_CLOCK_FONT_SWITCH, false)

            binding.headerClockFont.setDisableButtonVisibility(View.GONE)
        }

        // Custom clock color
        binding.headerClockCustomColor.isSwitchChecked =
            getBoolean(HEADER_CLOCK_COLOR_SWITCH, false)
        binding.headerClockCustomColor.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            putBoolean(HEADER_CLOCK_COLOR_SWITCH, isChecked)

            clearPrefs(
                HEADER_CLOCK_COLOR_CODE_ACCENT1,
                HEADER_CLOCK_COLOR_CODE_ACCENT2,
                HEADER_CLOCK_COLOR_CODE_ACCENT3,
                HEADER_CLOCK_COLOR_CODE_TEXT1,
                HEADER_CLOCK_COLOR_CODE_TEXT2
            )

            binding.headerClockColorPicker.visibility = if (isChecked) View.VISIBLE else View.GONE

            if (!isChecked) {
                binding.colorPickerAccent1.previewColor = ContextCompat.getColor(
                    requireContext(),
                    android.R.color.system_accent1_300
                )
                binding.colorPickerAccent2.previewColor = ContextCompat.getColor(
                    requireContext(),
                    android.R.color.system_accent2_300
                )
                binding.colorPickerAccent3.previewColor = ContextCompat.getColor(
                    requireContext(),
                    android.R.color.system_accent3_300
                )

                binding.colorPickerText1.previewColor = Color.WHITE
                binding.colorPickerText2.previewColor = Color.BLACK
            }
        }
        binding.headerClockColorPicker.visibility =
            if (getBoolean(HEADER_CLOCK_COLOR_SWITCH, false)) {
                View.VISIBLE
            } else {
                View.GONE
            }

        // Clock color picker accent 1
        binding.colorPickerAccent1.setColorPickerListener(
            activity = requireActivity(),
            defaultColor = getInt(
                HEADER_CLOCK_COLOR_CODE_ACCENT1,
                ContextCompat.getColor(
                    requireContext(),
                    android.R.color.system_accent1_300
                )
            ),
            showPresets = true,
            showAlphaSlider = true,
            showColorShades = true
        )
        binding.colorPickerAccent1.setOnColorSelectedListener { color: Int ->
            binding.colorPickerAccent1.previewColor = color

            putInt(HEADER_CLOCK_COLOR_CODE_ACCENT1, color)
        }

        // Clock color picker accent 2
        binding.colorPickerAccent2.setColorPickerListener(
            activity = requireActivity(),
            defaultColor = getInt(
                HEADER_CLOCK_COLOR_CODE_ACCENT2,
                ContextCompat.getColor(
                    requireContext(),
                    android.R.color.system_accent2_300
                )
            ),
            showPresets = true,
            showAlphaSlider = true,
            showColorShades = true
        )
        binding.colorPickerAccent2.setOnColorSelectedListener { color: Int ->
            binding.colorPickerAccent2.previewColor = color

            putInt(HEADER_CLOCK_COLOR_CODE_ACCENT2, color)
        }

        // Clock color picker accent 3
        binding.colorPickerAccent3.setColorPickerListener(
            activity = requireActivity(),
            defaultColor = getInt(
                HEADER_CLOCK_COLOR_CODE_ACCENT3,
                ContextCompat.getColor(
                    requireContext(),
                    android.R.color.system_accent3_300
                )
            ),
            showPresets = true,
            showAlphaSlider = true,
            showColorShades = true
        )
        binding.colorPickerAccent3.setOnColorSelectedListener { color: Int ->
            binding.colorPickerAccent3.previewColor = color

            putInt(HEADER_CLOCK_COLOR_CODE_ACCENT3, color)
        }

        // Clock color picker text 1
        binding.colorPickerText1.setColorPickerListener(
            activity = requireActivity(),
            defaultColor = getInt(
                HEADER_CLOCK_COLOR_CODE_TEXT1,
                Color.WHITE
            ),
            showPresets = true,
            showAlphaSlider = true,
            showColorShades = true
        )
        binding.colorPickerText1.setOnColorSelectedListener { color: Int ->
            binding.colorPickerText1.previewColor = color

            putInt(HEADER_CLOCK_COLOR_CODE_TEXT1, color)
        }

        // Clock color picker text 2
        binding.colorPickerText2.setColorPickerListener(
            activity = requireActivity(),
            defaultColor = getInt(
                HEADER_CLOCK_COLOR_CODE_TEXT2,
                Color.BLACK
            ),
            showPresets = true,
            showAlphaSlider = true,
            showColorShades = true
        )
        binding.colorPickerText2.setOnColorSelectedListener { color: Int ->
            binding.colorPickerText2.previewColor = color

            putInt(HEADER_CLOCK_COLOR_CODE_TEXT2, color)
        }

        // Text Scaling
        binding.headerClockTextscaling.sliderValue =
            getInt(HEADER_CLOCK_FONT_TEXT_SCALING, 10)
        binding.headerClockTextscaling.setOnSliderTouchListener(object :
            Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}

            override fun onStopTrackingTouch(slider: Slider) {
                putInt(HEADER_CLOCK_FONT_TEXT_SCALING, slider.value.toInt())
            }
        })
        binding.headerClockTextscaling.setResetClickListener {
            clearPref(HEADER_CLOCK_FONT_TEXT_SCALING)

            true
        }

        // Header clock side margin
        binding.headerClockSideMargin.sliderValue = getInt(HEADER_CLOCK_SIDEMARGIN, 0)
        binding.headerClockSideMargin.setOnSliderTouchListener(object :
            Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}

            override fun onStopTrackingTouch(slider: Slider) {
                putInt(HEADER_CLOCK_SIDEMARGIN, slider.value.toInt())
            }
        })

        // Header clock top margin
        binding.headerClockTopMargin.sliderValue = getInt(HEADER_CLOCK_TOPMARGIN, 8)
        binding.headerClockTopMargin.setOnSliderTouchListener(object :
            Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}

            override fun onStopTrackingTouch(slider: Slider) {
                putInt(HEADER_CLOCK_TOPMARGIN, slider.value.toInt())
            }
        })

        // Center clock
        binding.centerClock.isSwitchChecked = getBoolean(HEADER_CLOCK_CENTERED, false)
        binding.centerClock.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            putBoolean(
                HEADER_CLOCK_CENTERED,
                isChecked
            )
        }

        // Hide in landscape
        binding.hideHeaderClockLandscape.isSwitchChecked =
            getBoolean(HEADER_CLOCK_LANDSCAPE_SWITCH, true)
        binding.hideHeaderClockLandscape.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            putBoolean(
                HEADER_CLOCK_LANDSCAPE_SWITCH,
                isChecked
            )
        }

        return view
    }

    @SuppressLint("DiscouragedApi")
    private fun initHeaderClockStyles(): ClockPreviewAdapter {
        val headerClock = ArrayList<ClockModel>()
        var maxIndex = 0

        while (requireContext()
                .resources
                .getIdentifier(
                    HEADER_CLOCK_LAYOUT + maxIndex,
                    "layout",
                    BuildConfig.APPLICATION_ID
                ) != 0
        ) {
            maxIndex++
        }

        totalClocks = maxIndex

        for (i in 0 until maxIndex) {
            headerClock.add(
                ClockModel(
                    if (i == 0) {
                        requireContext().getString(R.string.clock_none)
                    } else {
                        requireContext().getString(R.string.clock_style_name, i)
                    },
                    requireContext()
                        .resources
                        .getIdentifier(
                            HEADER_CLOCK_LAYOUT + i,
                            "layout",
                            BuildConfig.APPLICATION_ID
                        )
                )
            )
        }

        return ClockPreviewAdapter(
            requireContext(),
            headerClock,
            HEADER_CLOCK_SWITCH,
            HEADER_CLOCK_STYLE
        )
    }

    private fun updateEnabled(enabled: Boolean) {
        binding.headerClockFont.setEnabled(enabled)
        binding.headerClockCustomColor.setEnabled(enabled)
        binding.colorPickerAccent1.setEnabled(enabled)
        binding.colorPickerAccent2.setEnabled(enabled)
        binding.colorPickerAccent3.setEnabled(enabled)
        binding.colorPickerText1.setEnabled(enabled)
        binding.colorPickerText2.setEnabled(enabled)
        binding.headerClockTextscaling.setEnabled(enabled)
        binding.headerClockSideMargin.setEnabled(enabled)
        binding.headerClockTopMargin.setEnabled(enabled)
        binding.centerClock.setEnabled(enabled)
        binding.hideHeaderClockLandscape.setEnabled(enabled)
    }
}