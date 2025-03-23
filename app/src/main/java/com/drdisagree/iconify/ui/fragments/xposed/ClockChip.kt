package com.drdisagree.iconify.ui.fragments.xposed

import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.CompoundButton
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import com.drdisagree.iconify.R
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUSBAR_CLOCK_ACCENT
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUSBAR_CLOCK_END_COLOR
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUSBAR_CLOCK_GRADIENT_DIRECTION
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUSBAR_CLOCK_PADDING_BOTTOM
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUSBAR_CLOCK_PADDING_LEFT
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUSBAR_CLOCK_PADDING_RIGHT
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUSBAR_CLOCK_PADDING_TOP
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUSBAR_CLOCK_RADIUS_BOTTOM_LEFT
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUSBAR_CLOCK_RADIUS_BOTTOM_RIGHT
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUSBAR_CLOCK_RADIUS_TOP_LEFT
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUSBAR_CLOCK_RADIUS_TOP_RIGHT
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUSBAR_CLOCK_START_COLOR
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUSBAR_CLOCK_STROKE_ACCENT
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUSBAR_CLOCK_STROKE_COLOR
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUSBAR_CLOCK_STROKE_DASH
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUSBAR_CLOCK_STROKE_DASH_GAP
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUSBAR_CLOCK_STROKE_DASH_WIDTH
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUSBAR_CLOCK_STROKE_SWITCH
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUSBAR_CLOCK_STROKE_WIDTH
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUSBAR_CLOCK_STYLE_CHANGED
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUSBAR_CLOCK_TEXT_COLOR_CODE
import com.drdisagree.iconify.data.common.Preferences.CHIP_STATUSBAR_CLOCK_TEXT_COLOR_OPTION
import com.drdisagree.iconify.data.config.RPrefs
import com.drdisagree.iconify.data.config.RPrefs.getBoolean
import com.drdisagree.iconify.data.config.RPrefs.getInt
import com.drdisagree.iconify.databinding.FragmentXposedClockChipBinding
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.toPx
import com.drdisagree.iconify.xposed.modules.extras.views.ChipDrawable
import com.drdisagree.iconify.xposed.modules.extras.views.ChipDrawable.GradientDirection.Companion.toIndex
import com.google.android.material.slider.Slider
import eightbitlab.com.blurview.RenderEffectBlur

class ClockChip : BaseFragment() {

    private lateinit var binding: FragmentXposedClockChipBinding

    private var customTextColorIndex: Int = getInt(CHIP_STATUSBAR_CLOCK_TEXT_COLOR_OPTION, 0)
    private var customTextColor: Int = getInt(CHIP_STATUSBAR_CLOCK_TEXT_COLOR_CODE, Color.WHITE)
    private var accentFillEnabled: Boolean = getBoolean(CHIP_STATUSBAR_CLOCK_ACCENT, true)
    private var startColor: Int = getInt(CHIP_STATUSBAR_CLOCK_START_COLOR, Color.RED)
    private var endColor: Int = getInt(CHIP_STATUSBAR_CLOCK_END_COLOR, Color.BLUE)
    private var gradientDirection: ChipDrawable.GradientDirection =
        ChipDrawable.GradientDirection.fromIndex(
            getInt(
                CHIP_STATUSBAR_CLOCK_GRADIENT_DIRECTION,
                ChipDrawable.GradientDirection.LEFT_RIGHT.toIndex()
            )
        )
    private var padding: IntArray = intArrayOf(
        getInt(CHIP_STATUSBAR_CLOCK_PADDING_LEFT, 8),
        getInt(CHIP_STATUSBAR_CLOCK_PADDING_TOP, 4),
        getInt(CHIP_STATUSBAR_CLOCK_PADDING_RIGHT, 8),
        getInt(CHIP_STATUSBAR_CLOCK_PADDING_BOTTOM, 4)
    )
    private var strokeEnabled: Boolean = getBoolean(CHIP_STATUSBAR_CLOCK_STROKE_SWITCH)
    private var strokeWidth: Int = getInt(CHIP_STATUSBAR_CLOCK_STROKE_WIDTH, 2)
    private var accentBorderEnabled: Boolean = getBoolean(CHIP_STATUSBAR_CLOCK_STROKE_ACCENT, true)
    private var strokeColor: Int = getInt(CHIP_STATUSBAR_CLOCK_STROKE_COLOR, Color.GREEN)
    private var dashedBorderEnabled: Boolean = getBoolean(CHIP_STATUSBAR_CLOCK_STROKE_DASH)
    private var dashWidth: Int = getInt(CHIP_STATUSBAR_CLOCK_STROKE_DASH_WIDTH, 4)
    private var dashGap: Int = getInt(CHIP_STATUSBAR_CLOCK_STROKE_DASH_GAP, 4)
    private var cornerRadii = floatArrayOf(
        getInt(CHIP_STATUSBAR_CLOCK_RADIUS_TOP_LEFT, 28).toFloat(),
        getInt(CHIP_STATUSBAR_CLOCK_RADIUS_TOP_LEFT, 28).toFloat(),
        getInt(CHIP_STATUSBAR_CLOCK_RADIUS_TOP_RIGHT, 28).toFloat(),
        getInt(CHIP_STATUSBAR_CLOCK_RADIUS_TOP_RIGHT, 28).toFloat(),
        getInt(CHIP_STATUSBAR_CLOCK_RADIUS_BOTTOM_RIGHT, 28).toFloat(),
        getInt(CHIP_STATUSBAR_CLOCK_RADIUS_BOTTOM_RIGHT, 28).toFloat(),
        getInt(CHIP_STATUSBAR_CLOCK_RADIUS_BOTTOM_LEFT, 28).toFloat(),
        getInt(CHIP_STATUSBAR_CLOCK_RADIUS_BOTTOM_LEFT, 28).toFloat(),
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentXposedClockChipBinding.inflate(inflater, container, false)

        // Header
        setHeader(
            requireContext(),
            getParentFragmentManager(),
            binding.header.toolbar,
            R.string.activity_title_clock_chip
        )

        binding.header.appBarLayout.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.header.appBarLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)

                val params = binding.blurView.layoutParams as CoordinatorLayout.LayoutParams
                params.topMargin = binding.header.appBarLayout.height
                binding.blurView.layoutParams = params

                binding.linearLayout.setPadding(
                    binding.linearLayout.paddingLeft,
                    binding.blurView.height,
                    binding.linearLayout.paddingRight,
                    binding.linearLayout.paddingBottom
                )
            }
        })

        val windowBackground: Drawable? = requireActivity().window.decorView.background
        binding.blurView.setupWith(binding.root, RenderEffectBlur())
            .setFrameClearDrawable(windowBackground)
            .setBlurRadius(8f)

        binding.clockTextColor.setSelectedIndex(getInt(CHIP_STATUSBAR_CLOCK_TEXT_COLOR_OPTION, 0))
        binding.clockTextColor.setOnItemSelectedListener { index: Int ->
            customTextColorIndex = index
            updateVisibility()
        }

        binding.clockTextColorPicker.apply {
            setColorPickerListener(
                activity = requireActivity(),
                defaultColor = customTextColor,
                showPresets = true,
                showAlphaSlider = true,
                showColorShades = true
            )
            setOnColorSelectedListener { color: Int ->
                customTextColor = color
                updateVisibility()
            }
        }

        binding.accentFillColor.isSwitchChecked = accentFillEnabled
        binding.accentFillColor.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            accentFillEnabled = isChecked
            updateVisibility()
        }

        binding.gradientDirection.setSelectedIndex(gradientDirection.toIndex())
        binding.gradientDirection.setOnItemSelectedListener { index: Int ->
            gradientDirection = ChipDrawable.GradientDirection.entries.toTypedArray()[index]
            updateVisibility()
        }

        binding.fillStartColor.apply {
            setColorPickerListener(
                activity = requireActivity(),
                defaultColor = startColor,
                showPresets = true,
                showAlphaSlider = true,
                showColorShades = true
            )
            setOnColorSelectedListener { color: Int ->
                startColor = color
                updateVisibility()
            }
        }

        binding.fillEndColor.apply {
            setColorPickerListener(
                activity = requireActivity(),
                defaultColor = endColor,
                showPresets = true,
                showAlphaSlider = true,
                showColorShades = true
            )
            setOnColorSelectedListener { color: Int ->
                endColor = color
                updateVisibility()
            }
        }

        binding.enableBorder.isSwitchChecked = strokeEnabled
        binding.enableBorder.setSwitchChangeListener { _: CompoundButton?, enabled: Boolean ->
            strokeEnabled = enabled
            updateVisibility()
        }

        binding.accentBorderColor.isSwitchChecked = accentBorderEnabled
        binding.accentBorderColor.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            accentBorderEnabled = isChecked
            updateVisibility()
        }

        binding.borderColor.apply {
            setColorPickerListener(
                activity = requireActivity(),
                defaultColor = strokeColor,
                showPresets = true,
                showAlphaSlider = true,
                showColorShades = true
            )
            setOnColorSelectedListener { color: Int ->
                strokeColor = color
                updateVisibility()
            }
        }

        binding.borderThickness.sliderValue = strokeWidth
        binding.borderThickness.setOnSliderChangeListener { _: Slider?, value: Float, _: Boolean ->
            strokeWidth = value.toInt()
            updateVisibility()
        }

        binding.dashedBorder.isSwitchChecked = dashedBorderEnabled
        binding.dashedBorder.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            dashedBorderEnabled = isChecked
            updateVisibility()
        }

        binding.dashWidth.sliderValue = dashWidth
        binding.dashWidth.setOnSliderChangeListener { _: Slider?, value: Float, _: Boolean ->
            dashWidth = value.toInt()
            updateVisibility()
        }

        binding.dashGap.sliderValue = dashGap
        binding.dashGap.setOnSliderChangeListener { _: Slider?, value: Float, _: Boolean ->
            dashGap = value.toInt()
            updateVisibility()
        }

        binding.paddingLeft.sliderValue = padding[0]
        binding.paddingLeft.setOnSliderChangeListener { _: Slider?, value: Float, _: Boolean ->
            padding[0] = value.toInt()
            updateVisibility()
        }

        binding.paddingRight.sliderValue = padding[2]
        binding.paddingRight.setOnSliderChangeListener { _: Slider?, value: Float, _: Boolean ->
            padding[2] = value.toInt()
            updateVisibility()
        }

        binding.paddingTop.sliderValue = padding[1]
        binding.paddingTop.setOnSliderChangeListener { _: Slider?, value: Float, _: Boolean ->
            padding[1] = value.toInt()
            updateVisibility()
        }

        binding.paddingBottom.sliderValue = padding[3]
        binding.paddingBottom.setOnSliderChangeListener { _: Slider?, value: Float, _: Boolean ->
            padding[3] = value.toInt()
            updateVisibility()
        }

        binding.cornerRadiusTopLeft.sliderValue = cornerRadii[0].toInt()
        binding.cornerRadiusTopLeft.setOnSliderChangeListener { _: Slider?, value: Float, _: Boolean ->
            cornerRadii[0] = value
            cornerRadii[1] = value
            updateVisibility()
        }

        binding.cornerRadiusTopRight.sliderValue = cornerRadii[2].toInt()
        binding.cornerRadiusTopRight.setOnSliderChangeListener { _: Slider?, value: Float, _: Boolean ->
            cornerRadii[2] = value
            cornerRadii[3] = value
            updateVisibility()
        }

        binding.cornerRadiusBottomLeft.sliderValue = cornerRadii[6].toInt()
        binding.cornerRadiusBottomLeft.setOnSliderChangeListener { _: Slider?, value: Float, _: Boolean ->
            cornerRadii[6] = value
            cornerRadii[7] = value
            updateVisibility()
        }

        binding.cornerRadiusBottomRight.sliderValue = cornerRadii[4].toInt()
        binding.cornerRadiusBottomRight.setOnSliderChangeListener { _: Slider?, value: Float, _: Boolean ->
            cornerRadii[4] = value
            cornerRadii[5] = value
            updateVisibility()
        }

        binding.btnApply.setOnClickListener {
            RPrefs.apply {
                putInt(CHIP_STATUSBAR_CLOCK_TEXT_COLOR_OPTION, customTextColorIndex)
                putInt(CHIP_STATUSBAR_CLOCK_TEXT_COLOR_CODE, customTextColor)
                putBoolean(CHIP_STATUSBAR_CLOCK_ACCENT, accentFillEnabled)
                putInt(CHIP_STATUSBAR_CLOCK_START_COLOR, startColor)
                putInt(CHIP_STATUSBAR_CLOCK_END_COLOR, endColor)
                putInt(CHIP_STATUSBAR_CLOCK_GRADIENT_DIRECTION, gradientDirection.toIndex())
                putInt(CHIP_STATUSBAR_CLOCK_PADDING_LEFT, padding[0])
                putInt(CHIP_STATUSBAR_CLOCK_PADDING_TOP, padding[1])
                putInt(CHIP_STATUSBAR_CLOCK_PADDING_RIGHT, padding[2])
                putInt(CHIP_STATUSBAR_CLOCK_PADDING_BOTTOM, padding[3])
                putBoolean(CHIP_STATUSBAR_CLOCK_STROKE_SWITCH, strokeEnabled)
                putInt(CHIP_STATUSBAR_CLOCK_STROKE_WIDTH, strokeWidth)
                putBoolean(CHIP_STATUSBAR_CLOCK_STROKE_ACCENT, accentBorderEnabled)
                putInt(CHIP_STATUSBAR_CLOCK_STROKE_COLOR, strokeColor)
                putBoolean(CHIP_STATUSBAR_CLOCK_STROKE_DASH, dashedBorderEnabled)
                putInt(CHIP_STATUSBAR_CLOCK_STROKE_DASH_WIDTH, dashWidth)
                putInt(CHIP_STATUSBAR_CLOCK_STROKE_DASH_GAP, dashGap)
                putInt(CHIP_STATUSBAR_CLOCK_RADIUS_TOP_LEFT, cornerRadii[0].toInt())
                putInt(CHIP_STATUSBAR_CLOCK_RADIUS_TOP_LEFT, cornerRadii[0].toInt())
                putInt(CHIP_STATUSBAR_CLOCK_RADIUS_TOP_RIGHT, cornerRadii[2].toInt())
                putInt(CHIP_STATUSBAR_CLOCK_RADIUS_TOP_RIGHT, cornerRadii[2].toInt())
                putInt(CHIP_STATUSBAR_CLOCK_RADIUS_BOTTOM_RIGHT, cornerRadii[4].toInt())
                putInt(CHIP_STATUSBAR_CLOCK_RADIUS_BOTTOM_RIGHT, cornerRadii[4].toInt())
                putInt(CHIP_STATUSBAR_CLOCK_RADIUS_BOTTOM_LEFT, cornerRadii[6].toInt())
                putInt(CHIP_STATUSBAR_CLOCK_RADIUS_BOTTOM_LEFT, cornerRadii[6].toInt())

                putBoolean(
                    CHIP_STATUSBAR_CLOCK_STYLE_CHANGED,
                    !getBoolean(CHIP_STATUSBAR_CLOCK_STYLE_CHANGED)
                )
            }
        }

        updateVisibility()

        return binding.getRoot()
    }

    private fun updateVisibility() {
        val textColorPicker = if (customTextColorIndex == 2) View.VISIBLE else View.GONE

        binding.clockTextColorPicker.visibility = textColorPicker

        val accentFillEnabled =
            if (binding.accentFillColor.isSwitchChecked) View.GONE else View.VISIBLE

        binding.gradientDirection.visibility = accentFillEnabled
        binding.fillStartColor.visibility = accentFillEnabled
        binding.fillEndColor.visibility = accentFillEnabled

        val borderEnabled = if (binding.enableBorder.isSwitchChecked) View.VISIBLE else View.GONE

        binding.accentBorderColor.visibility = borderEnabled
        binding.borderThickness.visibility = borderEnabled
        binding.dashedBorder.visibility = borderEnabled

        val accentBorderColor =
            if (binding.accentBorderColor.isSwitchChecked || !binding.enableBorder.isSwitchChecked) View.GONE else View.VISIBLE

        binding.borderColor.visibility = accentBorderColor

        val dashedBorderEnabled = if (binding.dashedBorder.isSwitchChecked) {
            if (!binding.enableBorder.isSwitchChecked) View.GONE else View.VISIBLE
        } else View.GONE

        binding.dashWidth.visibility = dashedBorderEnabled
        binding.dashGap.visibility = dashedBorderEnabled

        val isDarkMode =
            requireContext().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_YES ==
                    Configuration.UI_MODE_NIGHT_YES

        when (customTextColorIndex) {
            0 -> {
                binding.previewClock.paint.xfermode = null
                binding.previewClock.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        if (isDarkMode) {
                            R.color.white
                        } else {
                            R.color.black
                        }
                    )
                )
            }

            1 -> {
                binding.previewClock.paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
            }

            2 -> {
                binding.previewClock.paint.xfermode = null
                binding.previewClock.setTextColor(customTextColor)
            }
        }

        binding.previewClock.setPadding(
            requireContext().toPx(padding[0]),
            requireContext().toPx(padding[1]),
            requireContext().toPx(padding[2]),
            requireContext().toPx(padding[3])
        )

        binding.previewClock.background = ChipDrawable.createChipDrawable(
            context = requireContext(),
            accentFill = this.accentFillEnabled,
            startColor = startColor,
            endColor = endColor,
            gradientDirection = gradientDirection,
            padding = intArrayOf(0, 0, 0, 0),
            strokeEnabled = strokeEnabled,
            accentStroke = accentBorderEnabled,
            strokeWidth = strokeWidth,
            strokeColor = strokeColor,
            dashedBorderEnabled = this.dashedBorderEnabled,
            dashWidth = dashWidth,
            dashGap = dashGap,
            cornerRadii = cornerRadii
        )
    }
}