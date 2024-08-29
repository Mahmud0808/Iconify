package com.drdisagree.iconify.ui.fragments.home

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.drdisagree.iconify.R
import com.drdisagree.iconify.databinding.FragmentQsPanelTileBinding
import com.drdisagree.iconify.ui.adapters.MenuAdapter
import com.drdisagree.iconify.ui.adapters.QsShapeAdapter
import com.drdisagree.iconify.ui.adapters.SectionTitleAdapter
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.dialogs.LoadingDialog
import com.drdisagree.iconify.ui.models.MenuModel
import com.drdisagree.iconify.ui.models.QsShapeModel
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader

class QsPanelTile : BaseFragment() {

    private lateinit var binding: FragmentQsPanelTileBinding
    private var loadingDialog: LoadingDialog? = null
    private var adapter: ConcatAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentQsPanelTileBinding.inflate(inflater, container, false)
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
        binding.qsShapesContainer.setLayoutManager(LinearLayoutManager(requireContext()))

        adapter = ConcatAdapter(
            initActivityItems(),
            SectionTitleAdapter(
                requireContext(),
                R.layout.view_section_title,
                R.string.qspanel_tile_styles
            ),
            initQsShapeItems()
        )

        binding.qsShapesContainer.setAdapter(adapter)
        binding.qsShapesContainer.setHasFixedSize(true)

        return view
    }

    private fun initActivityItems(): MenuAdapter {
        val qsShapeActivityList = ArrayList<MenuModel>().apply {
            add(
                MenuModel(
                    QsPanelTilePixel(),
                    resources.getString(R.string.activity_title_pixel_variant),
                    resources.getString(R.string.activity_desc_pixel_variant),
                    R.drawable.ic_pixel_device
                )
            )
        }

        return MenuAdapter(
            parentFragmentManager,
            requireContext(),
            qsShapeActivityList
        )
    }

    private fun initQsShapeItems(): QsShapeAdapter {
        val qsShapeList = ArrayList<QsShapeModel>().apply {
            add(
                QsShapeModel(
                    "Default",
                    R.drawable.qs_shape_default_enabled,
                    R.drawable.qs_shape_default_disabled,
                    false
                )
            )
            add(
                QsShapeModel(
                    "Double Layer",
                    R.drawable.qs_shape_doublelayer_enabled,
                    R.drawable.qs_shape_doublelayer_disabled,
                    false
                )
            )
            add(
                QsShapeModel(
                    "Shaded Layer",
                    R.drawable.qs_shape_shadedlayer_enabled,
                    R.drawable.qs_shape_shadedlayer_disabled,
                    false
                )
            )
            add(
                QsShapeModel(
                    "Outline",
                    R.drawable.qs_shape_outline_enabled,
                    R.drawable.qs_shape_outline_disabled,
                    true
                )
            )
            add(
                QsShapeModel(
                    "Leafy Outline",
                    R.drawable.qs_shape_leafy_outline_enabled,
                    R.drawable.qs_shape_leafy_outline_disabled,
                    true
                )
            )
            add(
                QsShapeModel(
                    "Neumorph",
                    R.drawable.qs_shape_neumorph_enabled,
                    R.drawable.qs_shape_neumorph_disabled,
                    false
                )
            )
            add(
                QsShapeModel(
                    "Surround",
                    R.drawable.qs_shape_surround_enabled,
                    R.drawable.qs_shape_surround_disabled,
                    false,
                    4,
                    22
                )
            )
            add(
                QsShapeModel(
                    "Bookmark",
                    R.drawable.qs_shape_bookmark_enabled,
                    R.drawable.qs_shape_bookmark_disabled,
                    false,
                    4,
                    26
                )
            )
            add(
                QsShapeModel(
                    "Neumorph Outline",
                    R.drawable.qs_shape_neumorph_outline_enabled,
                    R.drawable.qs_shape_neumorph_outline_disabled,
                    true
                )
            )
            add(
                QsShapeModel(
                    "Reflected",
                    R.drawable.qs_shape_reflected_enabled,
                    R.drawable.qs_shape_reflected_disabled,
                    true
                )
            )
            add(
                QsShapeModel(
                    "Reflected Fill",
                    R.drawable.qs_shape_reflected_fill_enabled,
                    R.drawable.qs_shape_reflected_fill_disabled,
                    false
                )
            )
            add(
                QsShapeModel(
                    "Divided",
                    R.drawable.qs_shape_divided_enabled,
                    R.drawable.qs_shape_divided_disabled,
                    false,
                    4,
                    22
                )
            )
            add(
                QsShapeModel(
                    "Lighty",
                    R.drawable.qs_shape_lighty_enabled,
                    R.drawable.qs_shape_lighty_disabled,
                    true
                )
            )
            add(
                QsShapeModel(
                    "Bottom Outline",
                    R.drawable.qs_shape_bottom_outline_enabled,
                    R.drawable.qs_shape_bottom_outline_disabled,
                    false
                )
            )
            add(
                QsShapeModel(
                    "Cyberponk",
                    R.drawable.qs_shape_cyberponk_enabled,
                    R.drawable.qs_shape_cyberponk_disabled,
                    false
                )
            )
            add(
                QsShapeModel(
                    "Cyberponk v2",
                    R.drawable.qs_shape_cyberponk_v2_enabled,
                    R.drawable.qs_shape_cyberponk_v2_disabled,
                    true
                )
            )
            add(
                QsShapeModel(
                    "Semi Transparent",
                    R.drawable.qs_shape_semi_transparent_enabled,
                    R.drawable.qs_shape_semi_transparent_disabled,
                    false
                )
            )
            add(
                QsShapeModel(
                    "Thin Outline",
                    R.drawable.qs_shape_thin_outline_enabled,
                    R.drawable.qs_shape_thin_outline_disabled,
                    true
                )
            )
            add(
                QsShapeModel(
                    "Purfect",
                    R.drawable.qs_shape_purfect_enabled,
                    R.drawable.qs_shape_purfect_disabled,
                    false
                )
            )
            add(
                QsShapeModel(
                    "Translucent Outline",
                    R.drawable.qs_shape_translucent_outline_enabled,
                    R.drawable.qs_shape_translucent_outline_disabled,
                    true
                )
            )
        }

        return QsShapeAdapter(
            requireContext(),
            qsShapeList,
            loadingDialog!!,
            "QSSN"
        )
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