package com.drdisagree.iconify.ui.fragments.tweaks

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.common.Preferences.LAND_QSTILE_EXPANDED_HEIGHT
import com.drdisagree.iconify.common.Preferences.LAND_QSTILE_NONEXPANDED_HEIGHT
import com.drdisagree.iconify.common.Preferences.PORT_QSTILE_EXPANDED_HEIGHT
import com.drdisagree.iconify.common.Preferences.PORT_QSTILE_NONEXPANDED_HEIGHT
import com.drdisagree.iconify.config.RPrefs
import com.drdisagree.iconify.config.RPrefs.clearPrefs
import com.drdisagree.iconify.databinding.FragmentQsTileSizeBinding
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

class QsTileSize : BaseFragment() {

    private lateinit var binding: FragmentQsTileSizeBinding
    private var loadingDialog: LoadingDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentQsTileSizeBinding.inflate(inflater, container, false)
        val view: View = binding.getRoot()

        // Header
        setHeader(
            requireContext(),
            getParentFragmentManager(),
            binding.header.toolbar,
            R.string.activity_title_qs_tile_size
        )

        // Show loading dialog
        loadingDialog = LoadingDialog(requireContext())

        // Portrait non expanded height
        val portNonExpandedHeight = intArrayOf(RPrefs.getInt(PORT_QSTILE_NONEXPANDED_HEIGHT, 60))
        binding.portNonexpandedHeight.sliderValue = portNonExpandedHeight[0]
        binding.portNonexpandedHeight.setOnSliderTouchListener(object :
            Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}

            override fun onStopTrackingTouch(slider: Slider) {
                portNonExpandedHeight[0] = slider.value.toInt()
            }
        })

        // Reset button
        binding.portNonexpandedHeight.setResetClickListener {
            portNonExpandedHeight[0] = 60
            true
        }

        // Portrait Expanded height
        val portExpandedHeight = intArrayOf(RPrefs.getInt(PORT_QSTILE_EXPANDED_HEIGHT, 80))
        binding.portExpandedHeight.sliderValue = portExpandedHeight[0]
        binding.portExpandedHeight.setOnSliderTouchListener(object :
            Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}

            override fun onStopTrackingTouch(slider: Slider) {
                portExpandedHeight[0] = slider.value.toInt()
            }
        })

        // Reset button
        binding.portExpandedHeight.setResetClickListener {
            portExpandedHeight[0] = 80
            true
        }

        // Landscape non expanded height
        val landNonExpandedHeight = intArrayOf(RPrefs.getInt(LAND_QSTILE_NONEXPANDED_HEIGHT, 60))
        binding.landNonexpandedHeight.sliderValue = landNonExpandedHeight[0]
        binding.landNonexpandedHeight.setOnSliderTouchListener(object :
            Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}

            override fun onStopTrackingTouch(slider: Slider) {
                landNonExpandedHeight[0] = slider.value.toInt()
            }
        })

        // Reset button
        binding.landNonexpandedHeight.setResetClickListener {
            landNonExpandedHeight[0] = 60
            true
        }

        // Landscape Expanded height
        val landExpandedHeight = intArrayOf(RPrefs.getInt(LAND_QSTILE_EXPANDED_HEIGHT, 80))
        binding.landExpandedHeight.sliderValue = landExpandedHeight[0]
        binding.landExpandedHeight.setOnSliderTouchListener(object :
            Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}

            override fun onStopTrackingTouch(slider: Slider) {
                landExpandedHeight[0] = slider.value.toInt()
            }
        })

        // Reset button
        binding.landExpandedHeight.setResetClickListener {
            landExpandedHeight[0] = 80
            true
        }

        // Apply and reset button
        if (isQsTileHeightEnabled) {
            binding.qsTileHeightReset.visibility = View.VISIBLE
        }
        binding.qsTileHeightApply.setOnClickListener {
            if (!hasStoragePermission()) {
                requestStoragePermission(requireContext())
            } else {
                // Show loading dialog
                loadingDialog!!.show(resources.getString(R.string.loading_dialog_wait))

                val qsTileNonExpandedPort = ResourceEntry(
                    SYSTEMUI_PACKAGE,
                    "dimen",
                    "qs_quick_tile_size",
                    portNonExpandedHeight[0].toString() + "dp"
                )
                qsTileNonExpandedPort.setPortrait(true)

                val qsTileExpandedPort = ResourceEntry(
                    SYSTEMUI_PACKAGE,
                    "dimen",
                    "qs_tile_height",
                    portExpandedHeight[0].toString() + "dp"
                )
                qsTileExpandedPort.setPortrait(true)

                val qsTileNonExpandedLand = ResourceEntry(
                    SYSTEMUI_PACKAGE,
                    "dimen",
                    "qs_quick_tile_size",
                    landNonExpandedHeight[0].toString() + "dp"
                )
                qsTileNonExpandedLand.setLandscape(true)

                val qsTileExpandedLand = ResourceEntry(
                    SYSTEMUI_PACKAGE,
                    "dimen",
                    "qs_tile_height",
                    landExpandedHeight[0].toString() + "dp"
                )
                qsTileExpandedLand.setLandscape(true)

                Handler(Looper.getMainLooper()).post {
                    val hasErroredOut = AtomicBoolean(
                        buildOverlayWithResource(
                            qsTileNonExpandedPort,
                            qsTileExpandedPort,
                            qsTileNonExpandedLand,
                            qsTileExpandedLand
                        )
                    )

                    if (!hasErroredOut.get()) {
                        RPrefs.putInt(PORT_QSTILE_NONEXPANDED_HEIGHT, portNonExpandedHeight[0])
                        RPrefs.putInt(PORT_QSTILE_EXPANDED_HEIGHT, portExpandedHeight[0])
                        RPrefs.putInt(LAND_QSTILE_NONEXPANDED_HEIGHT, landNonExpandedHeight[0])
                        RPrefs.putInt(LAND_QSTILE_EXPANDED_HEIGHT, landExpandedHeight[0])
                    }

                    Handler(Looper.getMainLooper()).postDelayed({
                        // Hide loading dialog
                        loadingDialog!!.hide()

                        if (!hasErroredOut.get()) {
                            binding.qsTileHeightReset.visibility = View.VISIBLE
                        }
                    }, 2000)
                }
            }
        }
        binding.qsTileHeightReset.setOnClickListener {
            if (!hasStoragePermission()) {
                requestStoragePermission(requireContext())
            } else {
                // Show loading dialog
                loadingDialog!!.show(resources.getString(R.string.loading_dialog_wait))

                val qsTileNonExpandedPort = ResourceEntry(
                    SYSTEMUI_PACKAGE,
                    "dimen",
                    "qs_quick_tile_size"
                )
                qsTileNonExpandedPort.setPortrait(true)

                val qsTileExpandedPort = ResourceEntry(
                    SYSTEMUI_PACKAGE,
                    "dimen",
                    "qs_tile_height"
                )
                qsTileExpandedPort.setPortrait(true)

                val qsTileNonExpandedLand = ResourceEntry(
                    SYSTEMUI_PACKAGE,
                    "dimen",
                    "qs_quick_tile_size"
                )
                qsTileNonExpandedLand.setLandscape(true)

                val qsTileExpandedLand = ResourceEntry(
                    SYSTEMUI_PACKAGE,
                    "dimen",
                    "qs_tile_height"
                )
                qsTileExpandedLand.setLandscape(true)

                Handler(Looper.getMainLooper()).post {
                    val hasErroredOut = AtomicBoolean(
                        removeResourceFromOverlay(
                            qsTileNonExpandedPort,
                            qsTileExpandedPort,
                            qsTileNonExpandedLand,
                            qsTileExpandedLand
                        )
                    )

                    if (!hasErroredOut.get()) {
                        clearPrefs(
                            PORT_QSTILE_NONEXPANDED_HEIGHT,
                            PORT_QSTILE_EXPANDED_HEIGHT,
                            LAND_QSTILE_NONEXPANDED_HEIGHT,
                            LAND_QSTILE_EXPANDED_HEIGHT
                        )

                        portNonExpandedHeight[0] = 60
                        portExpandedHeight[0] = 80
                        landNonExpandedHeight[0] = 60
                        landExpandedHeight[0] = 80

                        binding.portNonexpandedHeight.resetSlider()
                        binding.portExpandedHeight.resetSlider()
                        binding.landNonexpandedHeight.resetSlider()
                        binding.landExpandedHeight.resetSlider()
                    }

                    Handler(Looper.getMainLooper()).postDelayed({
                        // Hide loading dialog
                        loadingDialog!!.hide()

                        if (!hasErroredOut.get()) {
                            binding.qsTileHeightReset.visibility = View.GONE
                        }
                    }, 2000)
                }
            }
        }

        return view
    }

    private val isQsTileHeightEnabled: Boolean
        get() = RPrefs.getInt(PORT_QSTILE_NONEXPANDED_HEIGHT, 60) != 60 ||
                RPrefs.getInt(PORT_QSTILE_EXPANDED_HEIGHT, 80) != 80 ||
                RPrefs.getInt(LAND_QSTILE_NONEXPANDED_HEIGHT, 60) != 60 ||
                RPrefs.getInt(LAND_QSTILE_EXPANDED_HEIGHT, 80) != 80

    override fun onDestroy() {
        loadingDialog?.dismiss()

        super.onDestroy()
    }
}