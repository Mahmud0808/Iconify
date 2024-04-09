package com.drdisagree.iconify.ui.fragments

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Dynamic.isAtleastA14
import com.drdisagree.iconify.common.Preferences.SHOW_QS_TILE_PIXEL_WARN
import com.drdisagree.iconify.config.Prefs
import com.drdisagree.iconify.databinding.FragmentQsPanelTilePixelBinding
import com.drdisagree.iconify.ui.adapters.QsShapeAdapter
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.dialogs.LoadingDialog
import com.drdisagree.iconify.ui.models.QsShapeModel
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class QsPanelTilePixel : BaseFragment() {

    private lateinit var binding: FragmentQsPanelTilePixelBinding
    private var loadingDialog: LoadingDialog? = null
    private var adapter: QsShapeAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentQsPanelTilePixelBinding.inflate(inflater, container, false)
        val view: View = binding.getRoot()

        // Header
        setHeader(
            requireContext(),
            getParentFragmentManager(),
            binding.header.toolbar,
            R.string.activity_title_qs_shape
        )

        // Loading dialog while enabling or disabling pack
        loadingDialog = LoadingDialog(requireContext())

        // RecyclerView
        binding.qsShapesPixelContainer.setLayoutManager(LinearLayoutManager(requireContext()))
        adapter = initQsShapeItems()
        binding.qsShapesPixelContainer.setAdapter(adapter)
        binding.qsShapesPixelContainer.setHasFixedSize(true)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isAtleastA14 && Prefs.getBoolean(SHOW_QS_TILE_PIXEL_WARN, true)) {
            try {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.attention)
                    .setMessage(R.string.requires_lsposed_for_a14)
                    .setPositiveButton(requireContext().resources.getString(R.string.understood)) { dialog: DialogInterface, _: Int ->
                        dialog.dismiss()
                    }
                    .setNegativeButton(requireContext().resources.getString(R.string.dont_show_again)) { dialog: DialogInterface, _: Int ->
                        dialog.dismiss()
                        Prefs.putBoolean(SHOW_QS_TILE_PIXEL_WARN, false)
                    }
                    .setCancelable(true)
                    .show()
            } catch (ignored: Exception) {
            }
        }
    }

    private fun initQsShapeItems(): QsShapeAdapter {
        val qsShapeList = ArrayList<QsShapeModel>().apply {
            add(
                QsShapeModel(
                    "Default",
                    R.drawable.qs_shape_default_enabled_pixel,
                    R.drawable.qs_shape_default_disabled_pixel
                )
            )
            add(
                QsShapeModel(
                    "Double Layer",
                    R.drawable.qs_shape_doublelayer_enabled_pixel,
                    R.drawable.qs_shape_doublelayer_disabled_pixel
                )
            )
            add(
                QsShapeModel(
                    "Shaded Layer",
                    R.drawable.qs_shape_shadedlayer_enabled_pixel,
                    R.drawable.qs_shape_shadedlayer_disabled_pixel
                )
            )
            add(
                QsShapeModel(
                    "Outline",
                    R.drawable.qs_shape_outline_enabled_pixel,
                    R.drawable.qs_shape_outline_disabled_pixel,
                    true
                )
            )
            add(
                QsShapeModel(
                    "Leafy Outline",
                    R.drawable.qs_shape_leafy_outline_enabled_pixel,
                    R.drawable.qs_shape_leafy_outline_disabled_pixel,
                    true
                )
            )
            add(
                QsShapeModel(
                    "Neumorph",
                    R.drawable.qs_shape_neumorph_enabled_pixel,
                    R.drawable.qs_shape_neumorph_disabled_pixel
                )
            )
            add(
                QsShapeModel(
                    "Surround",
                    R.drawable.qs_shape_surround_enabled_pixel,
                    R.drawable.qs_shape_surround_disabled_pixel,
                    4,
                    22
                )
            )
            add(
                QsShapeModel(
                    "Bookmark",
                    R.drawable.qs_shape_bookmark_enabled_pixel,
                    R.drawable.qs_shape_bookmark_disabled_pixel,
                    4,
                    26
                )
            )
            add(
                QsShapeModel(
                    "Neumorph Outline",
                    R.drawable.qs_shape_neumorph_outline_enabled_pixel,
                    R.drawable.qs_shape_neumorph_outline_disabled_pixel,
                    true
                )
            )
            add(
                QsShapeModel(
                    "Reflected",
                    R.drawable.qs_shape_reflected_enabled_pixel,
                    R.drawable.qs_shape_reflected_disabled_pixel,
                    true
                )
            )
            add(
                QsShapeModel(
                    "Reflected Fill",
                    R.drawable.qs_shape_reflected_fill_enabled_pixel,
                    R.drawable.qs_shape_reflected_fill_disabled_pixel
                )
            )
            add(
                QsShapeModel(
                    "Divided",
                    R.drawable.qs_shape_divided_enabled_pixel,
                    R.drawable.qs_shape_divided_disabled_pixel,
                    4,
                    22
                )
            )
            add(
                QsShapeModel(
                    "Lighty",
                    R.drawable.qs_shape_lighty_enabled_pixel,
                    R.drawable.qs_shape_lighty_disabled_pixel,
                    true
                )
            )
            add(
                QsShapeModel(
                    "Bottom Outline",
                    R.drawable.qs_shape_bottom_outline_enabled_pixel,
                    R.drawable.qs_shape_bottom_outline_disabled_pixel
                )
            )
            add(
                QsShapeModel(
                    "Cyberponk",
                    R.drawable.qs_shape_cyberponk_enabled_pixel,
                    R.drawable.qs_shape_cyberponk_disabled_pixel
                )
            )
            add(
                QsShapeModel(
                    "Cyberponk v2",
                    R.drawable.qs_shape_cyberponk_v2_enabled_pixel,
                    R.drawable.qs_shape_cyberponk_v2_disabled_pixel,
                    true
                )
            )
            add(
                QsShapeModel(
                    "Semi Transparent",
                    R.drawable.qs_shape_semi_transparent_enabled_pixel,
                    R.drawable.qs_shape_semi_transparent_disabled_pixel,
                    true
                )
            )
            add(
                QsShapeModel(
                    "Thin Outline",
                    R.drawable.qs_shape_thin_outline_enabled_pixel,
                    R.drawable.qs_shape_thin_outline_disabled_pixel,
                    true
                )
            )
            add(
                QsShapeModel(
                    "Purfect",
                    R.drawable.qs_shape_purfect_enabled_pixel,
                    R.drawable.qs_shape_purfect_disabled_pixel
                )
            )
            add(
                QsShapeModel(
                    "Translucent Outline",
                    R.drawable.qs_shape_translucent_outline_enabled_pixel,
                    R.drawable.qs_shape_translucent_outline_disabled_pixel,
                    true
                )
            )
        }

        return QsShapeAdapter(requireContext(), qsShapeList, loadingDialog!!, "QSSP")
    }

    // Change orientation in landscape / portrait mode
    @SuppressLint("NotifyDataSetChanged")
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        adapter!!.notifyDataSetChanged()
    }

    override fun onDestroy() {
        loadingDialog?.dismiss()

        super.onDestroy()
    }
}