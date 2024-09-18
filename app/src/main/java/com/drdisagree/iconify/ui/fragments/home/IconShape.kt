package com.drdisagree.iconify.ui.fragments.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.Iconify.Companion.appContextLocale
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Const.FRAMEWORK_PACKAGE
import com.drdisagree.iconify.common.Preferences.SELECTED_ICON_SHAPE
import com.drdisagree.iconify.config.RPrefs
import com.drdisagree.iconify.databinding.FragmentIconShapeBinding
import com.drdisagree.iconify.ui.adapters.IconShapeAdapter
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.dialogs.LoadingDialog
import com.drdisagree.iconify.ui.models.IconShapeModel
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader
import com.drdisagree.iconify.utils.SystemUtils.hasStoragePermission
import com.drdisagree.iconify.utils.SystemUtils.requestStoragePermission
import com.drdisagree.iconify.utils.overlay.OverlayUtils
import com.drdisagree.iconify.utils.overlay.compiler.OnDemandCompiler.buildOverlay
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

class IconShape : BaseFragment() {

    private lateinit var binding: FragmentIconShapeBinding
    private var loadingDialog: LoadingDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentIconShapeBinding.inflate(inflater, container, false)
        val view: View = binding.getRoot()

        // Header
        setHeader(
            requireContext(),
            getParentFragmentManager(),
            binding.header.toolbar,
            R.string.activity_title_icon_shape
        )

        // Loading dialog while enabling or disabling pack
        loadingDialog = LoadingDialog(requireContext())

        // Icon masking shape list
        val gridLayoutManager = GridLayoutManager(appContext, 3) // 3 columns
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val totalItemCount = binding.iconShapeContainer.adapter?.itemCount ?: return 1
                val spanCount = gridLayoutManager.spanCount

                // Calculate the number of items in the last row
                val itemsInLastRow = totalItemCount % spanCount
                return if (position >= totalItemCount - itemsInLastRow) {
                    // Adjust span size for the last row
                    when (itemsInLastRow) {
                        1 -> spanCount // 1 item spans all columns
                        2 -> spanCount / 2 // 2 items span half of the columns each
                        else -> 1 // Default span size (1 column each)
                    }
                } else {
                    1 // Default span size (1 column each)
                }
            }
        }
        binding.iconShapeContainer.layoutManager = gridLayoutManager
        binding.iconShapeContainer.adapter = initIconShapeList()
        binding.iconShapeContainer.setHasFixedSize(true)

        return view
    }

    private fun initIconShapeList(): IconShapeAdapter {
        val iconShapePreviewStyles = ArrayList<IconShapeModel>().apply {
            add(
                IconShapeModel(
                    R.drawable.icon_shape_none,
                    R.string.icon_mask_style_none
                )
            )
            add(
                IconShapeModel(
                    R.drawable.icon_shape_pebble,
                    R.string.icon_mask_style_pebble
                )
            )
            add(
                IconShapeModel(
                    R.drawable.icon_shape_rounded_hexagon,
                    R.string.icon_mask_style_hexagon
                )
            )
            add(
                IconShapeModel(
                    R.drawable.icon_shape_samsung,
                    R.string.icon_mask_style_samsung
                )
            )
            add(
                IconShapeModel(
                    R.drawable.icon_shape_scroll,
                    R.string.icon_mask_style_scroll
                )
            )
            add(
                IconShapeModel(
                    R.drawable.icon_shape_teardrops,
                    R.string.icon_mask_style_teardrop
                )
            )
            add(
                IconShapeModel(
                    R.drawable.icon_shape_square,
                    R.string.icon_mask_style_square
                )
            )
            add(
                IconShapeModel(
                    R.drawable.icon_shape_rounded_rectangle,
                    R.string.icon_mask_style_rounded_rectangle
                )
            )
            add(
                IconShapeModel(
                    R.drawable.icon_shape_ios,
                    R.string.icon_mask_style_ios
                )
            )
            add(
                IconShapeModel(
                    R.drawable.icon_shape_cloudy,
                    R.string.icon_mask_style_cloudy
                )
            )
            add(
                IconShapeModel(
                    R.drawable.icon_shape_cylinder,
                    R.string.icon_mask_style_cylinder
                )
            )
            add(
                IconShapeModel(
                    R.drawable.icon_shape_flower,
                    R.string.icon_mask_style_flower
                )
            )
            add(
                IconShapeModel(
                    R.drawable.icon_shape_heart,
                    R.string.icon_mask_style_heart
                )
            )
            add(
                IconShapeModel(
                    R.drawable.icon_shape_leaf,
                    R.string.icon_mask_style_leaf
                )
            )
            add(
                IconShapeModel(
                    R.drawable.icon_shape_stretched,
                    R.string.icon_mask_style_stretched
                )
            )
            add(
                IconShapeModel(
                    R.drawable.icon_shape_tapered_rectangle,
                    R.string.icon_mask_style_tapered_rectangle
                )
            )
            add(
                IconShapeModel(
                    R.drawable.icon_shape_vessel,
                    R.string.icon_mask_style_vessel
                )
            )
            add(
                IconShapeModel(
                    R.drawable.icon_shape_rohie_meow,
                    R.string.icon_mask_style_rice_rohie_meow
                )
            )
            add(
                IconShapeModel(
                    R.drawable.icon_shape_force_round,
                    R.string.icon_mask_style_force_round
                )
            )
        }

        return IconShapeAdapter(
            appContext,
            iconShapePreviewStyles,
            onShapeClick
        )
    }

    private val onShapeClick = object : IconShapeAdapter.OnShapeClick {
        override fun onShapeClick(position: Int, item: IconShapeModel) {

            if (position == 0) {
                RPrefs.putInt(SELECTED_ICON_SHAPE, 0)
                OverlayUtils.disableOverlay("IconifyComponentSIS.overlay")

                Toast.makeText(
                    appContext,
                    resources.getString(R.string.toast_disabled),
                    Toast.LENGTH_SHORT
                ).show()
                loadingDialog!!.hide()
                return
            }

            if (!hasStoragePermission()) {
                requestStoragePermission(
                    requireContext()
                )
            } else {
                // Show loading dialog
                loadingDialog!!.show(resources.getString(R.string.loading_dialog_wait))

                Thread {
                    val hasErroredOut = AtomicBoolean(false)

                    try {
                        hasErroredOut.set(
                            buildOverlay(
                                "SIS",
                                position,
                                FRAMEWORK_PACKAGE,
                                true
                            )
                        )
                    } catch (e: IOException) {
                        hasErroredOut.set(true)
                        Log.e("IconShape", e.toString())
                    }

                    if (!hasErroredOut.get()) {
                        RPrefs.putInt(SELECTED_ICON_SHAPE, position)
                        refreshAdapter()
                    }

                    Handler(Looper.getMainLooper()).postDelayed({
                        // Hide loading dialog
                        loadingDialog!!.hide()
                        if (!hasErroredOut.get()) {
                            Toast.makeText(
                                appContext,
                                appContextLocale.resources
                                    .getString(R.string.toast_applied),
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                appContext,
                                appContextLocale.resources
                                    .getString(R.string.toast_error),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }, 3000)
                }.start()
            }
        }
    }

    private fun refreshAdapter() {
        val ad = binding.iconShapeContainer.adapter as IconShapeAdapter
        ad.notifyChange()
    }

    override fun onDestroy() {
        loadingDialog?.dismiss()

        super.onDestroy()
    }
}