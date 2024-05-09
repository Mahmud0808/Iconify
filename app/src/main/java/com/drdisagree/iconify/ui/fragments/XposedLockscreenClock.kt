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
import com.drdisagree.iconify.common.Preferences.LSCLOCK_BOTTOMMARGIN
import com.drdisagree.iconify.common.Preferences.LSCLOCK_COLOR_CODE_ACCENT1
import com.drdisagree.iconify.common.Preferences.LSCLOCK_COLOR_CODE_ACCENT2
import com.drdisagree.iconify.common.Preferences.LSCLOCK_COLOR_CODE_ACCENT3
import com.drdisagree.iconify.common.Preferences.LSCLOCK_COLOR_CODE_TEXT1
import com.drdisagree.iconify.common.Preferences.LSCLOCK_COLOR_CODE_TEXT2
import com.drdisagree.iconify.common.Preferences.LSCLOCK_COLOR_SWITCH
import com.drdisagree.iconify.common.Preferences.LSCLOCK_FONT_LINEHEIGHT
import com.drdisagree.iconify.common.Preferences.LSCLOCK_FONT_SWITCH
import com.drdisagree.iconify.common.Preferences.LSCLOCK_FONT_TEXT_SCALING
import com.drdisagree.iconify.common.Preferences.LSCLOCK_STYLE
import com.drdisagree.iconify.common.Preferences.LSCLOCK_SWITCH
import com.drdisagree.iconify.common.Preferences.LSCLOCK_TOPMARGIN
import com.drdisagree.iconify.common.Resources.LOCKSCREEN_CLOCK_LAYOUT
import com.drdisagree.iconify.common.Resources.LSCLOCK_FONT_DIR
import com.drdisagree.iconify.config.RPrefs.clearPref
import com.drdisagree.iconify.config.RPrefs.clearPrefs
import com.drdisagree.iconify.config.RPrefs.getBoolean
import com.drdisagree.iconify.config.RPrefs.getInt
import com.drdisagree.iconify.config.RPrefs.putBoolean
import com.drdisagree.iconify.config.RPrefs.putInt
import com.drdisagree.iconify.databinding.FragmentXposedLockscreenClockBinding
import com.drdisagree.iconify.ui.adapters.ClockPreviewAdapter
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.models.ClockModel
import com.drdisagree.iconify.ui.utils.CarouselLayoutManager
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader
import com.drdisagree.iconify.utils.FileUtil.getRealPath
import com.drdisagree.iconify.utils.FileUtil.moveToIconifyHiddenDir
import com.drdisagree.iconify.utils.SystemUtil
import com.google.android.material.slider.Slider

class XposedLockscreenClock : BaseFragment() {

    private lateinit var binding: FragmentXposedLockscreenClockBinding
    private var totalClocks: Int = 1

    private var startActivityIntent = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val path = getRealPath(data)

            if (path != null && moveToIconifyHiddenDir(path, LSCLOCK_FONT_DIR)) {
                binding.lockscreenClockFont.setEnableButtonVisibility(View.VISIBLE)
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
        binding = FragmentXposedLockscreenClockBinding.inflate(inflater, container, false)
        val view: View = binding.getRoot()

        // Header
        setHeader(
            requireContext(),
            getParentFragmentManager(),
            binding.header.toolbar,
            R.string.activity_title_lockscreen_clock
        )

        // Enable lockscreen clock
        binding.enableLockscreenClock.isSwitchChecked = getBoolean(LSCLOCK_SWITCH, false)
        binding.enableLockscreenClock.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            putBoolean(LSCLOCK_SWITCH, isChecked)

            updateEnabled(isChecked)

            Handler(Looper.getMainLooper()).postDelayed(
                { SystemUtil.handleSystemUIRestart() },
                SWITCH_ANIMATION_DELAY
            )
        }

        updateEnabled(getBoolean(LSCLOCK_SWITCH, false))

        // Lockscreen clock style
        val snapHelper: SnapHelper = LinearSnapHelper()
        val carouselLayoutManager = CarouselLayoutManager(
            requireContext(),
            RecyclerView.HORIZONTAL,
            false
        )
        carouselLayoutManager.setMinifyDistance(0.8f)

        binding.rvLockscreenClockPreview.setLayoutManager(carouselLayoutManager)
        binding.rvLockscreenClockPreview.setAdapter(initLockscreenClockStyles())
        binding.rvLockscreenClockPreview.setHasFixedSize(true)
        snapHelper.attachToRecyclerView(binding.rvLockscreenClockPreview)

        // if index exceeds limit, set to highest available
        var lsClockStyle = getInt(LSCLOCK_STYLE, 0)
        if (lsClockStyle >= totalClocks) {
            lsClockStyle = totalClocks - 1
            putInt(LSCLOCK_STYLE, lsClockStyle)
        }
        binding.rvLockscreenClockPreview.scrollToPosition(lsClockStyle)

        // Lockscreen clock font picker
        binding.lockscreenClockFont.setActivityResultLauncher(startActivityIntent)
        binding.lockscreenClockFont.setDisableButtonVisibility(
            if (getBoolean(
                    LSCLOCK_FONT_SWITCH,
                    false
                )
            ) View.VISIBLE else View.GONE
        )
        binding.lockscreenClockFont.setEnableButtonOnClickListener {
            putBoolean(LSCLOCK_FONT_SWITCH, false)
            putBoolean(LSCLOCK_FONT_SWITCH, true)

            binding.lockscreenClockFont.setEnableButtonVisibility(View.GONE)
            binding.lockscreenClockFont.setDisableButtonVisibility(View.VISIBLE)
        }
        binding.lockscreenClockFont.setDisableButtonOnClickListener {
            putBoolean(LSCLOCK_FONT_SWITCH, false)

            binding.lockscreenClockFont.setDisableButtonVisibility(View.GONE)
        }

        // Custom clock color
        binding.lsClockCustomColor.isSwitchChecked =
            getBoolean(LSCLOCK_COLOR_SWITCH, false)
        binding.lsClockCustomColor.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            putBoolean(LSCLOCK_COLOR_SWITCH, isChecked)

            clearPrefs(
                LSCLOCK_COLOR_CODE_ACCENT1,
                LSCLOCK_COLOR_CODE_ACCENT2,
                LSCLOCK_COLOR_CODE_ACCENT3,
                LSCLOCK_COLOR_CODE_TEXT1,
                LSCLOCK_COLOR_CODE_TEXT2
            )

            binding.lsClockColorPicker.visibility = if (isChecked) View.VISIBLE else View.GONE

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
        binding.lsClockColorPicker.visibility =
            if (getBoolean(LSCLOCK_COLOR_SWITCH, false)) View.VISIBLE else View.GONE

        // Clock color picker accent 1
        binding.colorPickerAccent1.setColorPickerListener(
            activity = requireActivity(),
            defaultColor = getInt(
                LSCLOCK_COLOR_CODE_ACCENT1,
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

            putInt(LSCLOCK_COLOR_CODE_ACCENT1, color)
        }

        // Clock color picker accent 2
        binding.colorPickerAccent2.setColorPickerListener(
            activity = requireActivity(),
            defaultColor = getInt(
                LSCLOCK_COLOR_CODE_ACCENT2,
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

            putInt(LSCLOCK_COLOR_CODE_ACCENT2, color)
        }

        // Clock color picker accent 3
        binding.colorPickerAccent3.setColorPickerListener(
            activity = requireActivity(),
            defaultColor = getInt(
                LSCLOCK_COLOR_CODE_ACCENT3,
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

            putInt(LSCLOCK_COLOR_CODE_ACCENT3, color)
        }

        // Clock color picker text 1
        binding.colorPickerText1.setColorPickerListener(
            activity = requireActivity(),
            defaultColor = getInt(
                LSCLOCK_COLOR_CODE_TEXT1,
                Color.WHITE
            ),
            showPresets = true,
            showAlphaSlider = true,
            showColorShades = true
        )
        binding.colorPickerText1.setOnColorSelectedListener { color: Int ->
            binding.colorPickerText1.previewColor = color

            putInt(LSCLOCK_COLOR_CODE_TEXT1, color)
        }

        // Clock color picker text 2
        binding.colorPickerText2.setColorPickerListener(
            activity = requireActivity(),
            defaultColor = getInt(
                LSCLOCK_COLOR_CODE_TEXT2,
                Color.BLACK
            ),
            showPresets = true,
            showAlphaSlider = true,
            showColorShades = true
        )
        binding.colorPickerText2.setOnColorSelectedListener { color: Int ->
            binding.colorPickerText2.previewColor = color

            putInt(LSCLOCK_COLOR_CODE_TEXT2, color)
        }

        // Line height
        binding.lsclockLineHeight.sliderValue = getInt(LSCLOCK_FONT_LINEHEIGHT, 0)
        binding.lsclockLineHeight.setOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}

            override fun onStopTrackingTouch(slider: Slider) {
                putInt(LSCLOCK_FONT_LINEHEIGHT, slider.value.toInt())
            }
        })
        binding.lsclockLineHeight.setResetClickListener {
            clearPref(LSCLOCK_FONT_LINEHEIGHT)

            true
        }

        // Text Scaling
        binding.lsClockTextscaling.sliderValue = getInt(LSCLOCK_FONT_TEXT_SCALING, 10)
        binding.lsClockTextscaling.setOnSliderTouchListener(object :
            Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}
            override fun onStopTrackingTouch(slider: Slider) {
                putInt(LSCLOCK_FONT_TEXT_SCALING, slider.value.toInt())
            }
        })
        binding.lsClockTextscaling.setResetClickListener {
            clearPref(LSCLOCK_FONT_TEXT_SCALING)

            true
        }

        // Top margin
        binding.lsclockTopMargin.sliderValue = getInt(LSCLOCK_TOPMARGIN, 100)
        binding.lsclockTopMargin.setOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}

            override fun onStopTrackingTouch(slider: Slider) {
                putInt(LSCLOCK_TOPMARGIN, slider.value.toInt())
            }
        })

        // Bottom margin
        binding.lsclockBottomMargin.sliderValue = getInt(LSCLOCK_BOTTOMMARGIN, 40)
        binding.lsclockBottomMargin.setOnSliderTouchListener(object :
            Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}

            override fun onStopTrackingTouch(slider: Slider) {
                putInt(LSCLOCK_BOTTOMMARGIN, slider.value.toInt())
            }
        })

        return view
    }

    @SuppressLint("DiscouragedApi")
    private fun initLockscreenClockStyles(): ClockPreviewAdapter {
        val lsClock = ArrayList<ClockModel>()
        var maxIndex = 0

        while (requireContext()
                .resources
                .getIdentifier(
                    LOCKSCREEN_CLOCK_LAYOUT + maxIndex,
                    "layout",
                    BuildConfig.APPLICATION_ID
                ) != 0
        ) {
            maxIndex++
        }

        totalClocks = maxIndex

        for (i in 0 until maxIndex) {
            lsClock.add(
                ClockModel(
                    if (i == 0) {
                        requireContext().getString(R.string.clock_none)
                    } else {
                        requireContext().getString(R.string.clock_style_name, i)
                    },
                    requireContext()
                        .resources
                        .getIdentifier(
                            LOCKSCREEN_CLOCK_LAYOUT + i,
                            "layout",
                            BuildConfig.APPLICATION_ID
                        )
                )
            )
        }

        return ClockPreviewAdapter(
            requireContext(),
            lsClock,
            LSCLOCK_SWITCH,
            LSCLOCK_STYLE
        )
    }

    private fun updateEnabled(enabled: Boolean) {
        binding.lockscreenClockFont.setEnabled(enabled)
        binding.lsClockCustomColor.setEnabled(enabled)
        binding.colorPickerAccent1.setEnabled(enabled)
        binding.colorPickerAccent2.setEnabled(enabled)
        binding.colorPickerAccent3.setEnabled(enabled)
        binding.colorPickerText1.setEnabled(enabled)
        binding.colorPickerText2.setEnabled(enabled)
        binding.lsclockLineHeight.setEnabled(enabled)
        binding.lsClockTextscaling.setEnabled(enabled)
        binding.lsclockTopMargin.setEnabled(enabled)
        binding.lsclockBottomMargin.setEnabled(enabled)
    }
}