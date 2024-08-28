package com.drdisagree.iconify.ui.fragments.tweaks

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Const.FRAMEWORK_PACKAGE
import com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.common.Preferences.LAND_QQS_TOP_MARGIN
import com.drdisagree.iconify.common.Preferences.LAND_QS_TOP_MARGIN
import com.drdisagree.iconify.common.Preferences.PORT_QQS_TOP_MARGIN
import com.drdisagree.iconify.common.Preferences.PORT_QS_TOP_MARGIN
import com.drdisagree.iconify.config.RPrefs.clearPrefs
import com.drdisagree.iconify.config.RPrefs.getInt
import com.drdisagree.iconify.config.RPrefs.putInt
import com.drdisagree.iconify.databinding.FragmentQsPanelMarginBinding
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.dialogs.LoadingDialog
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader
import com.drdisagree.iconify.utils.SystemUtils.hasStoragePermission
import com.drdisagree.iconify.utils.SystemUtils.requestStoragePermission
import com.drdisagree.iconify.utils.overlay.manager.resource.ResourceEntry
import com.drdisagree.iconify.utils.overlay.manager.resource.ResourceManager.buildOverlayWithResource
import com.drdisagree.iconify.utils.overlay.manager.resource.ResourceManager.removeResourceFromOverlay
import com.google.android.material.slider.Slider
import java.util.concurrent.atomic.AtomicBoolean

class QsPanelMargin : BaseFragment() {

    private lateinit var binding: FragmentQsPanelMarginBinding
    private var loadingDialog: LoadingDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentQsPanelMarginBinding.inflate(inflater, container, false)
        val view: View = binding.getRoot()

        // Header
        setHeader(
            requireContext(),
            getParentFragmentManager(),
            binding.header.toolbar,
            R.string.activity_title_qs_panel_margin
        )

        // Show loading dialog
        loadingDialog = LoadingDialog(requireContext())

        // Portrait qqs margin
        val portQqsMargin = intArrayOf(getInt(PORT_QQS_TOP_MARGIN, 100))
        binding.portQqsTopMargin.sliderValue = portQqsMargin[0]
        binding.portQqsTopMargin.setOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}

            override fun onStopTrackingTouch(slider: Slider) {
                portQqsMargin[0] = slider.value.toInt()
            }
        })

        // Portrait qs margin
        val portQsMargin = intArrayOf(getInt(PORT_QS_TOP_MARGIN, 100))
        binding.portQsTopMargin.sliderValue = portQsMargin[0]
        binding.portQsTopMargin.setOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}

            override fun onStopTrackingTouch(slider: Slider) {
                portQsMargin[0] = slider.value.toInt()
            }
        })

        // Landscape qqs margin
        val landQqsMargin = intArrayOf(getInt(LAND_QQS_TOP_MARGIN, 100))
        binding.landQqsTopMargin.sliderValue = landQqsMargin[0]
        binding.landQqsTopMargin.setOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}

            override fun onStopTrackingTouch(slider: Slider) {
                landQqsMargin[0] = slider.value.toInt()
            }
        })

        // Landscape qs margin
        val landQsMargin = intArrayOf(getInt(LAND_QS_TOP_MARGIN, 100))
        binding.landQsTopMargin.sliderValue = landQsMargin[0]
        binding.landQsTopMargin.setOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}

            override fun onStopTrackingTouch(slider: Slider) {
                landQsMargin[0] = slider.value.toInt()
            }
        })

        // Apply and reset button
        if (isQsMarginEnabled) {
            binding.qsMarginReset.visibility = View.VISIBLE
        }
        binding.qsMarginApply.setOnClickListener {
            if (!hasStoragePermission()) {
                requestStoragePermission(requireContext())
            } else {
                // Show loading dialog
                loadingDialog!!.show(resources.getString(R.string.loading_dialog_wait))

                // Framework portrait
                val qqsMarginPortF = ResourceEntry(
                    FRAMEWORK_PACKAGE,
                    "dimen",
                    "quick_qs_offset_height",
                    portQqsMargin[0].toString() + "dp"
                )
                qqsMarginPortF.setPortrait(true)

                val qsMarginPortF = ResourceEntry(
                    FRAMEWORK_PACKAGE,
                    "dimen",
                    "quick_qs_total_height",
                    portQsMargin[0].toString() + "dp"
                )
                qsMarginPortF.setPortrait(true)

                // Framework landscape
                val qqsMarginLandF = ResourceEntry(
                    FRAMEWORK_PACKAGE,
                    "dimen",
                    "quick_qs_offset_height",
                    landQqsMargin[0].toString() + "dp"
                )
                qqsMarginLandF.setLandscape(true)

                val qsMarginLandF = ResourceEntry(
                    FRAMEWORK_PACKAGE,
                    "dimen",
                    "quick_qs_total_height",
                    landQsMargin[0].toString() + "dp"
                )
                qsMarginLandF.setLandscape(true)

                // SystemUI portrait
                val qqsMarginPortS1 = ResourceEntry(
                    SYSTEMUI_PACKAGE,
                    "dimen",
                    "qqs_layout_margin_top",
                    portQqsMargin[0].toString() + "dp"
                )
                qqsMarginPortS1.setPortrait(true)

                val qqsMarginPortS2 = ResourceEntry(
                    SYSTEMUI_PACKAGE,
                    "dimen",
                    "qs_header_row_min_height",
                    portQqsMargin[0].toString() + "dp"
                )
                qqsMarginPortS2.setPortrait(true)

                val qsMarginPortS1 = ResourceEntry(
                    SYSTEMUI_PACKAGE,
                    "dimen",
                    "qs_panel_padding_top",
                    portQsMargin[0].toString() + "dp"
                )
                qsMarginPortS1.setPortrait(true)

                val qsMarginPortS2 = ResourceEntry(
                    SYSTEMUI_PACKAGE,
                    "dimen",
                    "qs_panel_padding_top_combined_headers",
                    portQsMargin[0].toString() + "dp"
                )
                qsMarginPortS2.setPortrait(true)

                // SystemUI landscape
                val qqsMarginLandS1 = ResourceEntry(
                    SYSTEMUI_PACKAGE,
                    "dimen",
                    "qqs_layout_margin_top",
                    landQqsMargin[0].toString() + "dp"
                )
                qqsMarginLandS1.setLandscape(true)

                val qqsMarginLandS2 = ResourceEntry(
                    SYSTEMUI_PACKAGE,
                    "dimen",
                    "qs_header_row_min_height",
                    landQqsMargin[0].toString() + "dp"
                )
                qqsMarginLandS2.setLandscape(true)

                val qsMarginLandS1 = ResourceEntry(
                    SYSTEMUI_PACKAGE,
                    "dimen",
                    "qs_panel_padding_top",
                    landQsMargin[0].toString() + "dp"
                )
                qsMarginLandS1.setLandscape(true)

                val qsMarginLandS2 = ResourceEntry(
                    SYSTEMUI_PACKAGE,
                    "dimen",
                    "qs_panel_padding_top_combined_headers",
                    landQsMargin[0].toString() + "dp"
                )
                qsMarginLandS2.setLandscape(true)

                Handler(Looper.getMainLooper()).post {
                    val hasErroredOut = AtomicBoolean(
                        buildOverlayWithResource(
                            qqsMarginPortF,
                            qsMarginPortF,
                            qqsMarginLandF,
                            qsMarginLandF,
                            qqsMarginPortS1,
                            qqsMarginPortS2,
                            qsMarginPortS1,
                            qsMarginPortS2,
                            qqsMarginLandS1,
                            qqsMarginLandS2,
                            qsMarginLandS1,
                            qsMarginLandS2
                        )
                    )

                    if (!hasErroredOut.get()) {
                        putInt(PORT_QQS_TOP_MARGIN, portQqsMargin[0])
                        putInt(PORT_QS_TOP_MARGIN, portQsMargin[0])
                        putInt(LAND_QQS_TOP_MARGIN, landQqsMargin[0])
                        putInt(LAND_QS_TOP_MARGIN, landQsMargin[0])
                    }

                    Handler(Looper.getMainLooper()).postDelayed({
                        // Hide loading dialog
                        loadingDialog!!.hide()

                        if (!hasErroredOut.get()) {
                            binding.qsMarginReset.visibility = View.VISIBLE
                        }
                    }, 2000)
                }
            }
        }
        binding.qsMarginReset.setOnClickListener {
            if (!hasStoragePermission()) {
                requestStoragePermission(requireContext())
            } else {
                // Show loading dialog
                loadingDialog!!.show(resources.getString(R.string.loading_dialog_wait))

                // Framework portrait
                val qqsMarginPortF = ResourceEntry(
                    FRAMEWORK_PACKAGE,
                    "dimen",
                    "quick_qs_offset_height"
                )
                qqsMarginPortF.setPortrait(true)

                val qsMarginPortF = ResourceEntry(
                    FRAMEWORK_PACKAGE,
                    "dimen",
                    "quick_qs_total_height"
                )
                qsMarginPortF.setPortrait(true)

                // Framework landscape
                val qqsMarginLandF = ResourceEntry(
                    FRAMEWORK_PACKAGE,
                    "dimen",
                    "quick_qs_offset_height"
                )
                qqsMarginLandF.setLandscape(true)

                val qsMarginLandF = ResourceEntry(
                    FRAMEWORK_PACKAGE,
                    "dimen",
                    "quick_qs_total_height"
                )
                qsMarginLandF.setLandscape(true)

                // SystemUI portrait
                val qqsMarginPortS1 =
                    ResourceEntry(
                        SYSTEMUI_PACKAGE,
                        "dimen",
                        "qqs_layout_margin_top"
                    )
                qqsMarginPortS1.setPortrait(true)

                val qqsMarginPortS2 =
                    ResourceEntry(
                        SYSTEMUI_PACKAGE,
                        "dimen",
                        "qs_header_row_min_height"
                    )
                qqsMarginPortS2.setPortrait(true)

                val qsMarginPortS1 =
                    ResourceEntry(
                        SYSTEMUI_PACKAGE,
                        "dimen",
                        "qs_panel_padding_top"
                    )
                qsMarginPortS1.setPortrait(true)

                val qsMarginPortS2 = ResourceEntry(
                    SYSTEMUI_PACKAGE,
                    "dimen",
                    "qs_panel_padding_top_combined_headers"
                )
                qsMarginPortS2.setPortrait(true)

                // SystemUI landscape
                val qqsMarginLandS1 =
                    ResourceEntry(
                        SYSTEMUI_PACKAGE,
                        "dimen",
                        "qqs_layout_margin_top"
                    )
                qqsMarginLandS1.setLandscape(true)

                val qqsMarginLandS2 =
                    ResourceEntry(
                        SYSTEMUI_PACKAGE,
                        "dimen",
                        "qs_header_row_min_height"
                    )
                qqsMarginLandS2.setLandscape(true)

                val qsMarginLandS1 =
                    ResourceEntry(
                        SYSTEMUI_PACKAGE,
                        "dimen",
                        "qs_panel_padding_top"
                    )
                qsMarginLandS1.setLandscape(true)

                val qsMarginLandS2 = ResourceEntry(
                    SYSTEMUI_PACKAGE,
                    "dimen",
                    "qs_panel_padding_top_combined_headers"
                )
                qsMarginLandS2.setLandscape(true)

                Handler(Looper.getMainLooper()).post {
                    val hasErroredOut = AtomicBoolean(
                        removeResourceFromOverlay(
                            qqsMarginPortF, qsMarginPortF, qqsMarginLandF, qsMarginLandF,
                            qqsMarginPortS1, qqsMarginPortS2, qsMarginPortS1, qsMarginPortS2,
                            qqsMarginLandS1, qqsMarginLandS2, qsMarginLandS1, qsMarginLandS2
                        )
                    )

                    if (!hasErroredOut.get()) {
                        clearPrefs(
                            PORT_QQS_TOP_MARGIN,
                            PORT_QS_TOP_MARGIN,
                            LAND_QQS_TOP_MARGIN,
                            LAND_QS_TOP_MARGIN
                        )

                        portQqsMargin[0] = 100
                        portQsMargin[0] = 100
                        landQqsMargin[0] = 100
                        landQsMargin[0] = 100

                        binding.portQqsTopMargin.resetSlider()
                        binding.portQsTopMargin.resetSlider()
                        binding.landQqsTopMargin.resetSlider()
                        binding.landQsTopMargin.resetSlider()
                    }

                    Handler(Looper.getMainLooper()).postDelayed({
                        // Hide loading dialog
                        loadingDialog!!.hide()

                        if (!hasErroredOut.get()) {
                            binding.qsMarginReset.visibility = View.GONE
                        }
                    }, 2000)
                }
            }
        }

        return view
    }

    private val isQsMarginEnabled: Boolean
        get() = getInt(PORT_QQS_TOP_MARGIN, 100) != 100 ||
                getInt(PORT_QS_TOP_MARGIN, 100) != 100 ||
                getInt(LAND_QQS_TOP_MARGIN, 100) != 100 ||
                getInt(LAND_QS_TOP_MARGIN, 100) != 100

    override fun onDestroy() {
        loadingDialog?.dismiss()

        super.onDestroy()
    }
}