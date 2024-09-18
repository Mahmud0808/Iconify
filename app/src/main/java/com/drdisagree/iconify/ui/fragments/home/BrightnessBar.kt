package com.drdisagree.iconify.ui.fragments.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.drdisagree.iconify.R
import com.drdisagree.iconify.databinding.FragmentBrightnessBarBinding
import com.drdisagree.iconify.ui.adapters.BrightnessBarAdapter
import com.drdisagree.iconify.ui.adapters.MenuAdapter
import com.drdisagree.iconify.ui.adapters.SectionTitleAdapter
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.dialogs.LoadingDialog
import com.drdisagree.iconify.ui.models.BrightnessBarModel
import com.drdisagree.iconify.ui.models.MenuModel
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader

class BrightnessBar : BaseFragment() {

    private lateinit var binding: FragmentBrightnessBarBinding
    private var loadingDialog: LoadingDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBrightnessBarBinding.inflate(inflater, container, false)
        val view: View = binding.getRoot()

        // Header
        setHeader(
            requireContext(),
            getParentFragmentManager(),
            binding.header.toolbar,
            R.string.activity_title_brightness_bar
        )

        // Loading dialog while enabling or disabling pack
        loadingDialog = LoadingDialog(requireContext())

        // RecyclerView
        binding.brightnessBarContainer.setLayoutManager(LinearLayoutManager(requireContext()))

        val adapter = ConcatAdapter(
            initActivityItems(),
            SectionTitleAdapter(
                requireContext(),
                R.layout.view_section_title,
                R.string.brightness_bar_styles
            ),
            initBrightnessBarItems()
        )

        binding.brightnessBarContainer.setAdapter(adapter)
        binding.brightnessBarContainer.setHasFixedSize(true)

        return view
    }

    private fun initActivityItems(): MenuAdapter {
        val brightnessBarActivityList = ArrayList<MenuModel>().apply {
            add(
                MenuModel(
                    BrightnessBarPixel(),
                    resources.getString(R.string.activity_title_pixel_variant),
                    resources.getString(R.string.activity_desc_pixel_variant),
                    R.drawable.ic_pixel_device
                )
            )
        }

        return MenuAdapter(
            parentFragmentManager,
            requireContext(),
            brightnessBarActivityList
        )
    }

    private fun initBrightnessBarItems(): BrightnessBarAdapter {
        val bbList = ArrayList<BrightnessBarModel>().apply {
            add(
                BrightnessBarModel(
                    "Rounded Clip",
                    R.drawable.bb_roundedclip,
                    R.drawable.auto_bb_roundedclip,
                    false
                )
            )
            add(
                BrightnessBarModel(
                    "Rounded Bar",
                    R.drawable.bb_rounded,
                    R.drawable.auto_bb_rounded,
                    false
                )
            )
            add(
                BrightnessBarModel(
                    "Double Layer",
                    R.drawable.bb_double_layer,
                    R.drawable.auto_bb_double_layer,
                    false
                )
            )
            add(
                BrightnessBarModel(
                    "Shaded Layer",
                    R.drawable.bb_shaded_layer,
                    R.drawable.auto_bb_shaded_layer,
                    false
                )
            )
            add(
                BrightnessBarModel(
                    "Outline",
                    R.drawable.bb_outline,
                    R.drawable.auto_bb_outline,
                    true
                )
            )
            add(
                BrightnessBarModel(
                    "Leafy Outline",
                    R.drawable.bb_leafy_outline,
                    R.drawable.auto_bb_leafy_outline,
                    true
                )
            )
            add(
                BrightnessBarModel(
                    "Neumorph",
                    R.drawable.bb_neumorph,
                    R.drawable.auto_bb_neumorph,
                    false
                )
            )
            add(
                BrightnessBarModel(
                    "Inline",
                    R.drawable.bb_inline,
                    R.drawable.auto_bb_rounded,
                    false
                )
            )
            add(
                BrightnessBarModel(
                    "Neumorph Outline",
                    R.drawable.bb_neumorph_outline,
                    R.drawable.auto_bb_neumorph_outline,
                    true
                )
            )
            add(
                BrightnessBarModel(
                    "Neumorph Thumb",
                    R.drawable.bb_neumorph_thumb,
                    R.drawable.auto_bb_neumorph_thumb,
                    false
                )
            )
            add(
                BrightnessBarModel(
                    "Blocky Thumb",
                    R.drawable.bb_blocky_thumb,
                    R.drawable.auto_bb_blocky_thumb,
                    false
                )
            )
            add(
                BrightnessBarModel(
                    "Comet Thumb",
                    R.drawable.bb_comet_thumb,
                    R.drawable.auto_bb_comet_thumb,
                    false
                )
            )
            add(
                BrightnessBarModel(
                    "Minimal Thumb",
                    R.drawable.bb_minimal_thumb,
                    R.drawable.auto_bb_minimal_thumb,
                    false
                )
            )
            add(
                BrightnessBarModel(
                    "Old School Thumb",
                    R.drawable.bb_oldschool_thumb,
                    R.drawable.auto_bb_oldschool_thumb,
                    false
                )
            )
            add(
                BrightnessBarModel(
                    "Gradient Thumb",
                    R.drawable.bb_gradient_thumb,
                    R.drawable.auto_bb_gradient_thumb,
                    false
                )
            )
            add(
                BrightnessBarModel(
                    "Lighty",
                    R.drawable.bb_lighty,
                    R.drawable.auto_bb_lighty,
                    true
                )
            )
            add(
                BrightnessBarModel(
                    "Semi Transparent",
                    R.drawable.bb_semi_transparent,
                    R.drawable.auto_bb_semi_transparent,
                    false
                )
            )
            add(
                BrightnessBarModel(
                    "Thin Outline",
                    R.drawable.bb_thin_outline,
                    R.drawable.auto_bb_thin_outline,
                    true
                )
            )
            add(
                BrightnessBarModel(
                    "Purfect",
                    R.drawable.bb_purfect,
                    R.drawable.auto_bb_purfect,
                    false
                )
            )
            add(
                BrightnessBarModel(
                    "Translucent Outline",
                    R.drawable.bb_translucent_outline,
                    R.drawable.auto_bb_translucent_outline,
                    true
                )
            )
        }

        return BrightnessBarAdapter(
            requireContext(),
            bbList,
            loadingDialog!!,
            "BBN"
        )
    }

    override fun onDestroy() {
        loadingDialog?.dismiss()

        super.onDestroy()
    }
}