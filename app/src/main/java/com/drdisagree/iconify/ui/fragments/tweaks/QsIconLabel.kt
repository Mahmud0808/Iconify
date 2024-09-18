package com.drdisagree.iconify.ui.fragments.tweaks

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Const.SWITCH_ANIMATION_DELAY
import com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.common.Dynamic.isAtleastA14
import com.drdisagree.iconify.common.Preferences.QS_HIDE_LABEL_SWITCH
import com.drdisagree.iconify.common.Preferences.QS_TEXT_COLOR_VARIANT
import com.drdisagree.iconify.common.Preferences.QS_TEXT_COLOR_VARIANT_NORMAL
import com.drdisagree.iconify.common.Preferences.QS_TEXT_COLOR_VARIANT_PIXEL
import com.drdisagree.iconify.common.References.FABRICATED_QS_ICON_SIZE
import com.drdisagree.iconify.common.References.FABRICATED_QS_MOVE_ICON
import com.drdisagree.iconify.common.References.FABRICATED_QS_TEXT_SIZE
import com.drdisagree.iconify.config.RPrefs.getBoolean
import com.drdisagree.iconify.config.RPrefs.getString
import com.drdisagree.iconify.config.RPrefs.putBoolean
import com.drdisagree.iconify.config.RPrefs.putString
import com.drdisagree.iconify.databinding.FragmentQsIconLabelBinding
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader
import com.drdisagree.iconify.utils.SystemUtils.hasStoragePermission
import com.drdisagree.iconify.utils.SystemUtils.requestStoragePermission
import com.drdisagree.iconify.utils.overlay.OverlayUtils
import com.drdisagree.iconify.utils.overlay.OverlayUtils.changeOverlayState
import com.drdisagree.iconify.utils.overlay.OverlayUtils.disableOverlays
import com.drdisagree.iconify.utils.overlay.OverlayUtils.enableOverlay
import com.drdisagree.iconify.utils.overlay.manager.resource.ResourceEntry
import com.drdisagree.iconify.utils.overlay.manager.resource.ResourceManager.buildOverlayWithResource
import com.drdisagree.iconify.utils.overlay.manager.resource.ResourceManager.removeResourceFromOverlay
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.slider.Slider
import java.util.concurrent.atomic.AtomicBoolean

class QsIconLabel : BaseFragment() {

    private lateinit var binding: FragmentQsIconLabelBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentQsIconLabelBinding.inflate(inflater, container, false)
        val view: View = binding.getRoot()

        // Header
        setHeader(
            requireContext(),
            getParentFragmentManager(),
            binding.header.toolbar,
            R.string.activity_title_qs_icon_label
        )

        // Text Size
        val finalTextSize = intArrayOf(14)
        if (getString(FABRICATED_QS_TEXT_SIZE) != null) {
            finalTextSize[0] = getString(FABRICATED_QS_TEXT_SIZE)!!.toInt()
            binding.textSize.sliderValue = finalTextSize[0]
        }

        // Reset button
        binding.textSize.setResetClickListener {
            finalTextSize[0] = 14
            putString(FABRICATED_QS_TEXT_SIZE, finalTextSize[0].toString())

            removeResourceFromOverlay(
                requireContext(),
                ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "qs_tile_text_size")
            )

            true
        }
        binding.textSize.setOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}

            override fun onStopTrackingTouch(slider: Slider) {
                finalTextSize[0] = slider.value.toInt()
                putString(FABRICATED_QS_TEXT_SIZE, finalTextSize[0].toString())

                buildOverlayWithResource(
                    requireContext(),
                    ResourceEntry(
                        SYSTEMUI_PACKAGE,
                        "dimen",
                        "qs_tile_text_size",
                        finalTextSize[0].toString() + "sp"
                    )
                )
            }
        })

        // Icon Size
        val finalIconSize = intArrayOf(20)
        if (getString(FABRICATED_QS_ICON_SIZE) != null) {
            finalIconSize[0] = getString(FABRICATED_QS_ICON_SIZE)!!.toInt()
            binding.iconSize.sliderValue = finalIconSize[0]
        }

        // Reset button
        binding.iconSize.setResetClickListener {
            finalIconSize[0] = 20
            putString(FABRICATED_QS_ICON_SIZE, finalIconSize[0].toString())

            removeResourceFromOverlay(
                requireContext(),
                ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "qs_icon_size")
            )

            true
        }
        binding.iconSize.setOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}

            override fun onStopTrackingTouch(slider: Slider) {
                finalIconSize[0] = slider.value.toInt()
                putString(FABRICATED_QS_ICON_SIZE, finalIconSize[0].toString())

                buildOverlayWithResource(
                    requireContext(),
                    ResourceEntry(
                        SYSTEMUI_PACKAGE,
                        "dimen",
                        "qs_icon_size",
                        finalIconSize[0].toString() + "sp"
                    )
                )
            }
        })

        // Hide text size if hide label is enabled
        if (getBoolean(QS_HIDE_LABEL_SWITCH, false)) {
            binding.textSize.visibility = View.GONE
        }

        // QS Text Color
        if (isNormalVariantActive) {
            binding.toggleButtonTextColor.check(R.id.textColorNormal)
        } else if (isPixelVariantActive) {
            binding.toggleButtonTextColor.check(R.id.textColorPixel)
        }

        selectedVariant =
            if (binding.toggleButtonTextColor.checkedButtonId == R.id.textColorNormal) {
                QS_TEXT_COLOR_VARIANT_NORMAL
            } else {
                QS_TEXT_COLOR_VARIANT_PIXEL
            }

        binding.toggleButtonTextColor.addOnButtonCheckedListener { _: MaterialButtonToggleGroup?, checkedId: Int, _: Boolean ->
            if (checkedId == R.id.textColorNormal) {
                if (selectedVariant != QS_TEXT_COLOR_VARIANT_NORMAL) {
                    putString(QS_TEXT_COLOR_VARIANT, QS_TEXT_COLOR_VARIANT_NORMAL)
                    selectedVariant = QS_TEXT_COLOR_VARIANT_NORMAL

                    if (getBoolean(QSPT1_OVERLAY)) changeOverlayState(
                        QSPT1_OVERLAY,
                        false,
                        QSNT1_OVERLAY,
                        true
                    )

                    if (getBoolean(QSPT2_OVERLAY)) changeOverlayState(
                        QSPT2_OVERLAY,
                        false,
                        QSNT2_OVERLAY,
                        true
                    )

                    if (getBoolean(QSPT3_OVERLAY)) changeOverlayState(
                        QSPT3_OVERLAY,
                        false,
                        QSNT3_OVERLAY,
                        true
                    )

                    if (getBoolean(QSPT4_OVERLAY)) changeOverlayState(
                        QSPT4_OVERLAY,
                        false,
                        QSNT4_OVERLAY,
                        true
                    )

                    handleCommonOverlay()
                }
            } else if (checkedId == R.id.textColorPixel) {
                if (selectedVariant != QS_TEXT_COLOR_VARIANT_PIXEL) {
                    putString(QS_TEXT_COLOR_VARIANT, QS_TEXT_COLOR_VARIANT_PIXEL)
                    selectedVariant = QS_TEXT_COLOR_VARIANT_PIXEL

                    if (getBoolean(QSNT1_OVERLAY)) changeOverlayState(
                        QSNT1_OVERLAY,
                        false,
                        QSPT1_OVERLAY,
                        true
                    )

                    if (getBoolean(QSNT2_OVERLAY)) changeOverlayState(
                        QSNT2_OVERLAY,
                        false,
                        QSPT2_OVERLAY,
                        true
                    )

                    if (getBoolean(QSNT3_OVERLAY)) changeOverlayState(
                        QSNT3_OVERLAY,
                        false,
                        QSPT3_OVERLAY,
                        true
                    )

                    if (getBoolean(QSNT4_OVERLAY)) changeOverlayState(
                        QSNT4_OVERLAY,
                        false,
                        QSPT4_OVERLAY,
                        true
                    )

                    handleCommonOverlay()
                }
            }
        }

        binding.labelWhite.isSwitchChecked =
            getBoolean(replaceVariant("IconifyComponentQST1.overlay"))
        binding.labelWhite.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            if (isChecked && isAtleastA14) {
                Toast.makeText(
                    requireContext(),
                    R.string.toast_use_from_xposed_menu,
                    Toast.LENGTH_SHORT
                ).show()

                return@setSwitchChangeListener
            }

            Handler(Looper.getMainLooper()).postDelayed({
                if (isChecked) {
                    binding.labelWhiteV2.isSwitchChecked = false
                    binding.labelSystem.isSwitchChecked = false
                    binding.labelSystemV2.isSwitchChecked = false
                    binding.labelFixTextColor.isSwitchChecked = false

                    disableOverlays(
                        *replaceVariant(
                            "IconifyComponentQST2.overlay",
                            "IconifyComponentQST3.overlay",
                            "IconifyComponentQST4.overlay",
                            "IconifyComponentQST5.overlay"
                        )
                    )

                    enableOverlay(replaceVariant("IconifyComponentQST1.overlay"))
                } else {
                    OverlayUtils.disableOverlay(replaceVariant("IconifyComponentQST1.overlay"))
                }

                handleCommonOverlay()
            }, SWITCH_ANIMATION_DELAY)
        }

        binding.labelWhiteV2.isSwitchChecked =
            getBoolean(replaceVariant("IconifyComponentQST2.overlay"))
        binding.labelWhiteV2.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            if (isChecked && isAtleastA14) {
                Toast.makeText(
                    requireContext(),
                    R.string.toast_use_from_xposed_menu,
                    Toast.LENGTH_SHORT
                ).show()

                return@setSwitchChangeListener
            }

            Handler(Looper.getMainLooper()).postDelayed({
                if (isChecked) {
                    binding.labelWhite.isSwitchChecked = false
                    binding.labelSystem.isSwitchChecked = false
                    binding.labelSystemV2.isSwitchChecked = false
                    binding.labelFixTextColor.isSwitchChecked = false

                    disableOverlays(
                        *replaceVariant(
                            "IconifyComponentQST1.overlay",
                            "IconifyComponentQST3.overlay",
                            "IconifyComponentQST4.overlay",
                            "IconifyComponentQST5.overlay"
                        )
                    )

                    enableOverlay(replaceVariant("IconifyComponentQST2.overlay"))
                } else {
                    OverlayUtils.disableOverlay(replaceVariant("IconifyComponentQST2.overlay"))
                }

                handleCommonOverlay()
            }, SWITCH_ANIMATION_DELAY)
        }

        binding.labelSystem.isSwitchChecked =
            getBoolean(replaceVariant("IconifyComponentQST3.overlay"))
        binding.labelSystem.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            if (isChecked && isAtleastA14) {
                Toast.makeText(
                    requireContext(),
                    R.string.toast_use_from_xposed_menu,
                    Toast.LENGTH_SHORT
                ).show()

                return@setSwitchChangeListener
            }

            Handler(Looper.getMainLooper()).postDelayed({
                if (isChecked) {
                    binding.labelWhite.isSwitchChecked = false
                    binding.labelWhiteV2.isSwitchChecked = false
                    binding.labelSystemV2.isSwitchChecked = false
                    binding.labelFixTextColor.isSwitchChecked = false

                    disableOverlays(
                        *replaceVariant(
                            "IconifyComponentQST1.overlay",
                            "IconifyComponentQST2.overlay",
                            "IconifyComponentQST4.overlay",
                            "IconifyComponentQST5.overlay"
                        )
                    )

                    enableOverlay(replaceVariant("IconifyComponentQST3.overlay"))
                } else {
                    OverlayUtils.disableOverlay(replaceVariant("IconifyComponentQST3.overlay"))
                }

                handleCommonOverlay()
            }, SWITCH_ANIMATION_DELAY)
        }

        binding.labelSystemV2.isSwitchChecked =
            getBoolean(replaceVariant("IconifyComponentQST4.overlay"))
        binding.labelSystemV2.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            if (isChecked && isAtleastA14) {
                Toast.makeText(
                    requireContext(),
                    R.string.toast_use_from_xposed_menu,
                    Toast.LENGTH_SHORT
                ).show()

                return@setSwitchChangeListener
            }
            Handler(Looper.getMainLooper()).postDelayed({
                if (isChecked) {
                    binding.labelWhite.isSwitchChecked = false
                    binding.labelWhiteV2.isSwitchChecked = false
                    binding.labelSystem.isSwitchChecked = false
                    binding.labelFixTextColor.isSwitchChecked = false

                    disableOverlays(
                        *replaceVariant(
                            "IconifyComponentQST1.overlay",
                            "IconifyComponentQST2.overlay",
                            "IconifyComponentQST3.overlay",
                            "IconifyComponentQST5.overlay"
                        )
                    )

                    enableOverlay(replaceVariant("IconifyComponentQST4.overlay"))
                } else {
                    OverlayUtils.disableOverlay(replaceVariant("IconifyComponentQST4.overlay"))
                }

                handleCommonOverlay()
            }, SWITCH_ANIMATION_DELAY)
        }

        binding.labelFixTextColor.isSwitchChecked =
            getBoolean("IconifyComponentQST5.overlay")
        binding.labelFixTextColor.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            if (isChecked && isAtleastA14) {
                Toast.makeText(
                    requireContext(),
                    R.string.toast_use_from_xposed_menu,
                    Toast.LENGTH_SHORT
                ).show()

                return@setSwitchChangeListener
            }

            Handler(Looper.getMainLooper()).postDelayed({
                if (isChecked) {
                    binding.labelWhite.isSwitchChecked = false
                    binding.labelWhiteV2.isSwitchChecked = false
                    binding.labelSystem.isSwitchChecked = false
                    binding.labelSystemV2.isSwitchChecked = false

                    disableOverlays(
                        *replaceVariant(
                            "IconifyComponentQST1.overlay",
                            "IconifyComponentQST2.overlay",
                            "IconifyComponentQST3.overlay",
                            "IconifyComponentQST4.overlay"
                        )
                    )

                    enableOverlay(replaceVariant("IconifyComponentQST5.overlay"))
                } else {
                    OverlayUtils.disableOverlay(replaceVariant("IconifyComponentQST5.overlay"))
                }

                handleCommonOverlay()
            }, SWITCH_ANIMATION_DELAY)
        }

        // Hide Label
        val isHideLabelContainerClicked = AtomicBoolean(false)

        binding.hideLabel.isSwitchChecked = getBoolean(QS_HIDE_LABEL_SWITCH, false)
        binding.hideLabel.setSwitchChangeListener { buttonView: CompoundButton, isChecked: Boolean ->
            if (!hasStoragePermission()) {
                isHideLabelContainerClicked.set(false)
                requestStoragePermission(requireContext())
                binding.hideLabel.isSwitchChecked = !isChecked
            } else if (buttonView.isPressed || isHideLabelContainerClicked.get()) {
                isHideLabelContainerClicked.set(false)
                putBoolean(QS_HIDE_LABEL_SWITCH, isChecked)

                if (isChecked) {
                    buildOverlayWithResource(
                        requireContext(),
                        ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "qs_tile_text_size", "0sp"),
                        ResourceEntry(
                            SYSTEMUI_PACKAGE,
                            "dimen",
                            "qs_label_container_margin",
                            "-120dp"
                        )
                    )

                    binding.textSize.visibility = View.GONE
                } else {
                    removeResourceFromOverlay(
                        requireContext(),
                        ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "qs_tile_text_size"),
                        ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "qs_label_container_margin")
                    )

                    binding.textSize.visibility = View.VISIBLE
                }
            }
        }
        binding.hideLabel.setBeforeSwitchChangeListener {
            isHideLabelContainerClicked.set(true)
        }

        // Move Icon
        val finalMoveIcon = intArrayOf(16)
        if (getString(FABRICATED_QS_MOVE_ICON) != null) {
            finalMoveIcon[0] = getString(FABRICATED_QS_MOVE_ICON)!!.toInt()
            binding.moveIcon.sliderValue = finalMoveIcon[0]
        }

        // Reset button
        binding.moveIcon.setResetClickListener {
            finalMoveIcon[0] = 16
            putString(FABRICATED_QS_MOVE_ICON, finalMoveIcon[0].toString())

            removeResourceFromOverlay(
                requireContext(),
                ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "qs_tile_start_padding")
            )

            true
        }
        binding.moveIcon.setOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}

            override fun onStopTrackingTouch(slider: Slider) {
                finalMoveIcon[0] = slider.value.toInt()
                putString(FABRICATED_QS_MOVE_ICON, finalMoveIcon[0].toString())

                buildOverlayWithResource(
                    requireContext(),
                    ResourceEntry(
                        SYSTEMUI_PACKAGE,
                        "dimen",
                        "qs_tile_start_padding",
                        finalMoveIcon[0].toString() + "dp"
                    )
                )
            }
        })

        return view
    }

    private fun replaceVariant(vararg args: String): Array<String?> {
        val newArgs = arrayOfNulls<String>(args.size)

        for (i in args.indices) {
            if (args[i].contains("QST5")) continue

            newArgs[i] = args[i].replace(
                "QST",
                if (selectedVariant == QS_TEXT_COLOR_VARIANT_NORMAL) "QSNT" else "QSPT"
            )
        }

        return newArgs
    }

    private fun replaceVariant(arg: String): String {
        return if (arg.contains("QST5")) arg else arg.replace(
            "QST",
            if (selectedVariant == QS_TEXT_COLOR_VARIANT_NORMAL) "QSNT" else "QSPT"
        )
    }

    private val isNormalVariantActive: Boolean
        get() = getBoolean(QSNT1_OVERLAY) ||
                getBoolean(QSNT2_OVERLAY) ||
                getBoolean(QSNT3_OVERLAY) ||
                getBoolean(QSNT4_OVERLAY) || getString(QS_TEXT_COLOR_VARIANT) == QS_TEXT_COLOR_VARIANT_NORMAL

    private val isPixelVariantActive: Boolean
        get() = getBoolean(QSPT1_OVERLAY) ||
                getBoolean(QSPT2_OVERLAY) ||
                getBoolean(QSPT3_OVERLAY) ||
                getBoolean(QSPT4_OVERLAY) || getString(QS_TEXT_COLOR_VARIANT) == QS_TEXT_COLOR_VARIANT_PIXEL

    private fun handleCommonOverlay() {
        changeOverlayState(
            QSNPT_OVERLAY,
            getBoolean(QSNT1_OVERLAY) ||
                    getBoolean(QSNT2_OVERLAY) ||
                    getBoolean(QSNT3_OVERLAY) ||
                    getBoolean(QSNT4_OVERLAY) ||
                    getBoolean(QSPT1_OVERLAY) ||
                    getBoolean(QSPT2_OVERLAY) ||
                    getBoolean(QSPT3_OVERLAY) ||
                    getBoolean(QSPT4_OVERLAY)
        )
    }

    companion object {
        private var selectedVariant: String? = null
        const val QSNPT_OVERLAY = "IconifyComponentQSNPT.overlay"
        const val QSNT1_OVERLAY = "IconifyComponentQSNT1.overlay"
        const val QSNT2_OVERLAY = "IconifyComponentQSNT2.overlay"
        const val QSNT3_OVERLAY = "IconifyComponentQSNT3.overlay"
        const val QSNT4_OVERLAY = "IconifyComponentQSNT4.overlay"
        const val QSPT1_OVERLAY = "IconifyComponentQSPT1.overlay"
        const val QSPT2_OVERLAY = "IconifyComponentQSPT2.overlay"
        const val QSPT3_OVERLAY = "IconifyComponentQSPT3.overlay"
        const val QSPT4_OVERLAY = "IconifyComponentQSPT4.overlay"
    }
}