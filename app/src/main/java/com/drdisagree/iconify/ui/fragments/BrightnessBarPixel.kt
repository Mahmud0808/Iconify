package com.drdisagree.iconify.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.drdisagree.iconify.R
import com.drdisagree.iconify.databinding.FragmentBrightnessBarPixelBinding
import com.drdisagree.iconify.ui.adapters.BrightnessBarAdapter
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.dialogs.LoadingDialog
import com.drdisagree.iconify.ui.models.BrightnessBarModel
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader

class BrightnessBarPixel : BaseFragment() {

    private lateinit var binding: FragmentBrightnessBarPixelBinding
    private var loadingDialog: LoadingDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBrightnessBarPixelBinding.inflate(inflater, container, false)
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
        binding.brightnessBarPixelContainer.setLayoutManager(LinearLayoutManager(requireContext()))
        binding.brightnessBarPixelContainer.setAdapter(initQsShapeItems())
        binding.brightnessBarPixelContainer.setHasFixedSize(true)

        return view
    }

    private fun initQsShapeItems(): BrightnessBarAdapter {
        val bbList = ArrayList<BrightnessBarModel>().apply {
            add(
                BrightnessBarModel(
                    "Rounded Clip",
                    R.drawable.bb_roundedclip_pixel,
                    R.drawable.auto_bb_roundedclip_pixel
                )
            )
            add(
                BrightnessBarModel(
                    "Rounded Bar",
                    R.drawable.bb_rounded_pixel,
                    R.drawable.auto_bb_rounded_pixel
                )
            )
            add(
                BrightnessBarModel(
                    "Double Layer",
                    R.drawable.bb_double_layer_pixel,
                    R.drawable.auto_bb_double_layer_pixel
                )
            )
            add(
                BrightnessBarModel(
                    "Shaded Layer",
                    R.drawable.bb_shaded_layer_pixel,
                    R.drawable.auto_bb_shaded_layer_pixel
                )
            )
            add(
                BrightnessBarModel(
                    "Outline",
                    R.drawable.bb_outline_pixel,
                    R.drawable.auto_bb_outline_pixel
                )
            )
            add(
                BrightnessBarModel(
                    "Leafy Outline",
                    R.drawable.bb_leafy_outline_pixel,
                    R.drawable.auto_bb_leafy_outline_pixel
                )
            )
            add(
                BrightnessBarModel(
                    "Neumorph",
                    R.drawable.bb_neumorph_pixel,
                    R.drawable.auto_bb_neumorph_pixel
                )
            )
            add(
                BrightnessBarModel(
                    "Inline",
                    R.drawable.bb_inline_pixel,
                    R.drawable.auto_bb_rounded_pixel
                )
            )
            add(
                BrightnessBarModel(
                    "Neumorph Outline",
                    R.drawable.bb_neumorph_outline_pixel,
                    R.drawable.auto_bb_neumorph_outline_pixel
                )
            )
            add(
                BrightnessBarModel(
                    "Neumorph Thumb",
                    R.drawable.bb_neumorph_thumb_pixel,
                    R.drawable.auto_bb_neumorph_thumb_pixel
                )
            )
            add(
                BrightnessBarModel(
                    "Blocky Thumb",
                    R.drawable.bb_blocky_thumb_pixel,
                    R.drawable.auto_bb_blocky_thumb_pixel
                )
            )
            add(
                BrightnessBarModel(
                    "Comet Thumb",
                    R.drawable.bb_comet_thumb_pixel,
                    R.drawable.auto_bb_comet_thumb_pixel
                )
            )
            add(
                BrightnessBarModel(
                    "Minimal Thumb",
                    R.drawable.bb_minimal_thumb_pixel,
                    R.drawable.auto_bb_minimal_thumb_pixel
                )
            )
            add(
                BrightnessBarModel(
                    "Old School Thumb",
                    R.drawable.bb_oldschool_thumb_pixel,
                    R.drawable.auto_bb_oldschool_thumb_pixel
                )
            )
            add(
                BrightnessBarModel(
                    "Gradient Thumb",
                    R.drawable.bb_gradient_thumb_pixel,
                    R.drawable.auto_bb_gradient_thumb_pixel
                )
            )
            add(
                BrightnessBarModel(
                    "Lighty",
                    R.drawable.bb_lighty_pixel,
                    R.drawable.auto_bb_lighty_pixel
                )
            )
            add(
                BrightnessBarModel(
                    "Semi Transparent",
                    R.drawable.bb_semi_transparent_pixel,
                    R.drawable.auto_bb_semi_transparent_pixel
                )
            )
            add(
                BrightnessBarModel(
                    "Thin Outline",
                    R.drawable.bb_thin_outline_pixel,
                    R.drawable.auto_bb_thin_outline_pixel
                )
            )
            add(
                BrightnessBarModel(
                    "Purfect",
                    R.drawable.bb_purfect_pixel,
                    R.drawable.auto_bb_purfect_pixel
                )
            )
            add(
                BrightnessBarModel(
                    "Translucent Outline",
                    R.drawable.bb_translucent_outline_pixel,
                    R.drawable.auto_bb_translucent_outline_pixel
                )
            )
        }

        return BrightnessBarAdapter(
            requireContext(),
            bbList,
            loadingDialog!!,
            "BBP"
        )
    }

    override fun onDestroy() {
        loadingDialog?.dismiss()

        super.onDestroy()
    }
}