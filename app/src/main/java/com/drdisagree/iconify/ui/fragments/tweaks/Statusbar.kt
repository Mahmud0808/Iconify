package com.drdisagree.iconify.ui.fragments.tweaks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.R
import com.drdisagree.iconify.data.common.Const.FRAMEWORK_PACKAGE
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.common.References.FABRICATED_SB_COLOR_SOURCE
import com.drdisagree.iconify.data.common.References.FABRICATED_SB_COLOR_TINT
import com.drdisagree.iconify.data.common.References.FABRICATED_SB_HEIGHT
import com.drdisagree.iconify.data.common.References.FABRICATED_SB_LEFT_PADDING
import com.drdisagree.iconify.data.common.References.FABRICATED_SB_RIGHT_PADDING
import com.drdisagree.iconify.data.config.RPrefs
import com.drdisagree.iconify.data.config.RPrefs.putString
import com.drdisagree.iconify.data.events.ColorDismissedEvent
import com.drdisagree.iconify.data.events.ColorSelectedEvent
import com.drdisagree.iconify.databinding.FragmentStatusbarBinding
import com.drdisagree.iconify.ui.activities.MainActivity
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader
import com.drdisagree.iconify.utils.color.ColorUtils.colorToSpecialHex
import com.drdisagree.iconify.utils.overlay.FabricatedUtils.buildAndEnableOverlays
import com.drdisagree.iconify.utils.overlay.FabricatedUtils.disableOverlays
import com.drdisagree.iconify.utils.overlay.OverlayUtils
import com.drdisagree.iconify.utils.overlay.OverlayUtils.enableOverlay
import com.drdisagree.iconify.utils.overlay.manager.resource.ResourceEntry
import com.drdisagree.iconify.utils.overlay.manager.resource.ResourceManager.buildOverlayWithResource
import com.drdisagree.iconify.utils.overlay.manager.resource.ResourceManager.removeResourceFromOverlay
import com.google.android.material.slider.Slider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe


class Statusbar : BaseFragment() {

    private lateinit var binding: FragmentStatusbarBinding
    private val finalSBLeftPadding = intArrayOf(RPrefs.getInt(FABRICATED_SB_LEFT_PADDING, 8))
    private val finalSBRightPadding = intArrayOf(RPrefs.getInt(FABRICATED_SB_RIGHT_PADDING, 8))
    private val finalSBHeight = intArrayOf(RPrefs.getInt(FABRICATED_SB_HEIGHT, 28))

    private val sbLeftPaddingListener: Slider.OnSliderTouchListener =
        object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}

            override fun onStopTrackingTouch(slider: Slider) {
                finalSBLeftPadding[0] = slider.value.toInt()
                RPrefs.putInt(FABRICATED_SB_LEFT_PADDING, finalSBLeftPadding[0])

                CoroutineScope(Dispatchers.IO).launch {
                    buildOverlayWithResource(
                        ResourceEntry(
                            SYSTEMUI_PACKAGE,
                            "dimen",
                            "status_bar_padding_start",
                            finalSBLeftPadding[0].toString() + "dp"
                        )
                    )
                }
            }
        }

    private val sbRightPaddingListener: Slider.OnSliderTouchListener =
        object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}

            override fun onStopTrackingTouch(slider: Slider) {
                finalSBRightPadding[0] = slider.value.toInt()
                RPrefs.putInt(FABRICATED_SB_RIGHT_PADDING, finalSBRightPadding[0])

                CoroutineScope(Dispatchers.IO).launch {
                    buildOverlayWithResource(
                        ResourceEntry(
                            SYSTEMUI_PACKAGE,
                            "dimen",
                            "status_bar_padding_end",
                            finalSBRightPadding[0].toString() + "dp"
                        )
                    )
                }
            }
        }

    private val sbHeightListener: Slider.OnSliderTouchListener =
        object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}

            override fun onStopTrackingTouch(slider: Slider) {
                finalSBHeight[0] = slider.value.toInt()
                RPrefs.putInt(FABRICATED_SB_HEIGHT, finalSBHeight[0])

                CoroutineScope(Dispatchers.IO).launch {
                    buildOverlayWithResource(
                        ResourceEntry(
                            SYSTEMUI_PACKAGE,
                            "dimen",
                            "status_bar_height",
                            finalSBHeight[0].toString() + "dp"
                        ),
                        ResourceEntry(
                            FRAMEWORK_PACKAGE,
                            "dimen",
                            "status_bar_height",
                            finalSBHeight[0].toString() + "dp"
                        ),
                        ResourceEntry(
                            FRAMEWORK_PACKAGE,
                            "dimen",
                            "status_bar_height_default",
                            finalSBHeight[0].toString() + "dp"
                        ),
                        ResourceEntry(
                            FRAMEWORK_PACKAGE,
                            "dimen",
                            "status_bar_height_portrait",
                            finalSBHeight[0].toString() + "dp"
                        ),
                        ResourceEntry(
                            FRAMEWORK_PACKAGE,
                            "dimen",
                            "status_bar_height_landscape",
                            finalSBHeight[0].toString() + "dp"
                        )
                    )
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStatusbarBinding.inflate(inflater, container, false)
        val view: View = binding.getRoot()

        // Header
        setHeader(
            requireContext(),
            getParentFragmentManager(),
            binding.header.toolbar,
            R.string.activity_title_statusbar
        )

        // Statusbar left padding
        binding.sbLeftPadding.sliderValue = finalSBLeftPadding[0]
        binding.sbLeftPadding.setOnSliderTouchListener(sbLeftPaddingListener)

        // Reset left padding
        binding.sbLeftPadding.setResetClickListener {
            RPrefs.putInt(FABRICATED_SB_LEFT_PADDING, 8)

            CoroutineScope(Dispatchers.IO).launch {
                removeResourceFromOverlay(
                    ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "status_bar_padding_start")
                )
            }

            true
        }

        // Statusbar right padding
        binding.sbRightPadding.sliderValue = finalSBRightPadding[0]
        binding.sbRightPadding.setOnSliderTouchListener(sbRightPaddingListener)

        // Reset right padding
        binding.sbRightPadding.setResetClickListener {
            RPrefs.putInt(FABRICATED_SB_RIGHT_PADDING, 8)

            CoroutineScope(Dispatchers.IO).launch {
                removeResourceFromOverlay(
                    ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "status_bar_padding_end")
                )
            }

            true
        }

        // Statusbar height
        binding.sbHeight.sliderValue = finalSBHeight[0]
        binding.sbHeight.setOnSliderTouchListener(sbHeightListener)

        // Reset height
        binding.sbHeight.setResetClickListener {
            RPrefs.putInt(FABRICATED_SB_HEIGHT, 28)

            CoroutineScope(Dispatchers.IO).launch {
                removeResourceFromOverlay(
                    ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "status_bar_height"),
                    ResourceEntry(FRAMEWORK_PACKAGE, "dimen", "status_bar_height"),
                    ResourceEntry(FRAMEWORK_PACKAGE, "dimen", "status_bar_height_default"),
                    ResourceEntry(FRAMEWORK_PACKAGE, "dimen", "status_bar_height_portrait"),
                    ResourceEntry(FRAMEWORK_PACKAGE, "dimen", "status_bar_height_landscape")
                )
            }

            true
        }
        colorSBTint = resources.getColor(R.color.colorAccent, appContext.theme).toString()

        //set current chosen style
        selectedStyle = RPrefs.getString(FABRICATED_SB_COLOR_SOURCE)
        when {
            selectedStyle == "Monet" || RPrefs.getBoolean("IconifyComponentSBTint.overlay") -> {
                binding.sbTintMonet.setChecked(true)
                putString(FABRICATED_SB_COLOR_SOURCE, "Monet")
            }

            selectedStyle == "System" -> {
                binding.sbTintSystem.setChecked(true)
            }

            selectedStyle == "Custom" -> {
                binding.sbTintCustom.setChecked(
                    true
                )
            }
        }

        // Statusbar color source select
        binding.sbTintSourceSelector.setOnCheckedChangeListener { _: RadioGroup?, checkedId: Int ->
            when (checkedId) {
                R.id.sb_tint_system -> {
                    if (selectedStyle != "System") {
                        putString(FABRICATED_SB_COLOR_SOURCE, "System")
                        resetSBColor()
                    }
                }

                R.id.sb_tint_monet -> {
                    if (selectedStyle != "Monet") {
                        enableOverlay("IconifyComponentSBTint.overlay")
                        putString(FABRICATED_SB_COLOR_SOURCE, "Monet")
                        MainActivity.showOrHidePendingActionButton(
                            activityBinding = (requireActivity() as MainActivity).binding,
                            requiresSystemUiRestart = true
                        )
                    }
                }

                R.id.sb_tint_custom -> {
                    (requireActivity() as MainActivity).showColorPickerDialog(
                        dialogId = 1,
                        defaultColor = colorSBTint!!.toInt(),
                        showPresets = true,
                        showAlphaSlider = false,
                        showColorShades = true
                    )
                }
            }
        }

        return view
    }

    @Suppress("unused")
    @Subscribe
    fun onColorSelected(event: ColorSelectedEvent) {
        if (event.dialogId == 1) {
            colorSBTint = event.selectedColor.toString()
            putString(FABRICATED_SB_COLOR_TINT, colorSBTint)

            applySBColor()

            putString(FABRICATED_SB_COLOR_SOURCE, "Custom")

            OverlayUtils.disableOverlay("IconifyComponentSBTint.overlay")
        }
    }

    @Suppress("unused")
    @Subscribe
    fun onDialogDismissed(event: ColorDismissedEvent) {
        if (event.dialogId == 1) {
            selectedStyle = RPrefs.getString(FABRICATED_SB_COLOR_SOURCE)
            when (selectedStyle) {
                "System" -> binding.sbTintSystem.setChecked(true)
                "Monet" -> binding.sbTintMonet.setChecked(true)
                "Custom" -> binding.sbTintCustom.setChecked(true)
            }
        }
    }

    private fun applySBColor() {
        buildAndEnableOverlays(
            arrayOf(
                SYSTEMUI_PACKAGE,
                "colorSBTint1",
                "color",
                "dark_mode_icon_color_dual_tone_fill",
                colorToSpecialHex(colorSBTint!!.toInt())
            ), arrayOf(
                SYSTEMUI_PACKAGE,
                "colorSBTint2",
                "color",
                "dark_mode_icon_color_single_tone",
                colorToSpecialHex(colorSBTint!!.toInt())
            ), arrayOf(
                SYSTEMUI_PACKAGE,
                "colorSBTint3",
                "color",
                "dark_mode_qs_icon_color_dual_tone_fill",
                colorToSpecialHex(colorSBTint!!.toInt())
            ), arrayOf(
                SYSTEMUI_PACKAGE,
                "colorSBTint4",
                "color",
                "dark_mode_qs_icon_color_single_tone",
                colorToSpecialHex(colorSBTint!!.toInt())
            ), arrayOf(
                SYSTEMUI_PACKAGE,
                "colorSBTint5",
                "color",
                "light_mode_icon_color_dual_tone_fill",
                colorToSpecialHex(colorSBTint!!.toInt())
            ), arrayOf(
                SYSTEMUI_PACKAGE,
                "colorSBTint6",
                "color",
                "light_mode_icon_color_single_tone",
                colorToSpecialHex(colorSBTint!!.toInt())
            ), arrayOf(
                SYSTEMUI_PACKAGE,
                "colorSBTint7",
                "color",
                "status_bar_clock_color",
                colorToSpecialHex(colorSBTint!!.toInt())
            )
        )

        MainActivity.showOrHidePendingActionButton(
            activityBinding = (requireActivity() as MainActivity).binding,
            requiresSystemUiRestart = true
        )
    }

    private fun resetSBColor() {
        disableOverlays(
            "colorSBTint1",
            "colorSBTint2",
            "colorSBTint3",
            "colorSBTint4",
            "colorSBTint5",
            "colorSBTint6",
            "colorSBTint7"
        )

        OverlayUtils.disableOverlay("IconifyComponentSBTint.overlay")

        MainActivity.showOrHidePendingActionButton(
            activityBinding = (requireActivity() as MainActivity).binding,
            requiresSystemUiRestart = true
        )
    }

    override fun onStart() {
        super.onStart()

        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()

        EventBus.getDefault().unregister(this)
    }

    companion object {
        private var colorSBTint: String? = null
        private var selectedStyle: String? = null
    }
}