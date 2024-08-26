package com.drdisagree.iconify.ui.fragments.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.drdisagree.iconify.R
import com.drdisagree.iconify.databinding.FragmentIconPackBinding
import com.drdisagree.iconify.ui.adapters.IconPackAdapter
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.dialogs.LoadingDialog
import com.drdisagree.iconify.ui.models.IconPackModel
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader
import com.drdisagree.iconify.utils.overlay.manager.IconPackManager

class IconPack : BaseFragment() {

    private lateinit var binding: FragmentIconPackBinding
    private var loadingDialog: LoadingDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentIconPackBinding.inflate(inflater, container, false)
        val view: View = binding.getRoot()

        // Header
        setHeader(
            requireContext(),
            getParentFragmentManager(),
            binding.header.toolbar,
            R.string.activity_title_icon_pack
        )

        // Loading dialog while enabling or disabling pack
        loadingDialog = LoadingDialog(requireContext())

        // RecyclerView
        binding.iconPackContainer.setLayoutManager(LinearLayoutManager(requireContext()))

        binding.iconPackContainer.setAdapter(initIconPackItems())
        binding.iconPackContainer.setHasFixedSize(true)

        return view
    }

    private fun initIconPackItems(): IconPackAdapter {
        val iconPackList = ArrayList<IconPackModel>().apply {
            add(
                IconPackModel(
                    "Aurora",
                    R.string.iconpack_aurora_desc,
                    R.drawable.preview_aurora_wifi,
                    R.drawable.preview_aurora_signal,
                    R.drawable.preview_aurora_airplane,
                    R.drawable.preview_aurora_location
                )
            )
            add(
                IconPackModel(
                    "Gradicon",
                    R.string.iconpack_gradicon_desc,
                    R.drawable.preview_gradicon_wifi,
                    R.drawable.preview_gradicon_signal,
                    R.drawable.preview_gradicon_airplane,
                    R.drawable.preview_gradicon_location
                )
            )
            add(
                IconPackModel(
                    "Lorn",
                    R.string.iconpack_lorn_desc,
                    R.drawable.preview_lorn_wifi,
                    R.drawable.preview_lorn_signal,
                    R.drawable.preview_lorn_airplane,
                    R.drawable.preview_lorn_location
                )
            )
            add(
                IconPackModel(
                    "Plumpy",
                    R.string.iconpack_plumpy_desc,
                    R.drawable.preview_plumpy_wifi,
                    R.drawable.preview_plumpy_signal,
                    R.drawable.preview_plumpy_airplane,
                    R.drawable.preview_plumpy_location
                )
            )
            add(
                IconPackModel(
                    "Acherus",
                    R.string.iconpack_acherus_desc,
                    R.drawable.preview_acherus_wifi,
                    R.drawable.preview_acherus_signal,
                    R.drawable.preview_acherus_airplane,
                    R.drawable.preview_acherus_location
                )
            )
            add(
                IconPackModel(
                    "Circular",
                    R.string.iconpack_circular_desc,
                    R.drawable.preview_circular_wifi,
                    R.drawable.preview_circular_signal,
                    R.drawable.preview_circular_airplane,
                    R.drawable.preview_circular_location
                )
            )
            add(
                IconPackModel(
                    "Filled",
                    R.string.iconpack_filled_desc,
                    R.drawable.preview_filled_wifi,
                    R.drawable.preview_filled_signal,
                    R.drawable.preview_filled_airplane,
                    R.drawable.preview_filled_location
                )
            )
            add(
                IconPackModel(
                    "Kai",
                    R.string.iconpack_kai_desc,
                    R.drawable.preview_kai_wifi,
                    R.drawable.preview_kai_signal,
                    R.drawable.preview_kai_airplane,
                    R.drawable.preview_kai_location
                )
            )
            add(
                IconPackModel(
                    "OOS",
                    R.string.iconpack_oos_desc,
                    R.drawable.preview_oos_wifi,
                    R.drawable.preview_oos_signal,
                    R.drawable.preview_oos_airplane,
                    R.drawable.preview_oos_location
                )
            )
            add(
                IconPackModel(
                    "Outline",
                    R.string.iconpack_outline_desc,
                    R.drawable.preview_outline_wifi,
                    R.drawable.preview_outline_signal,
                    R.drawable.preview_outline_airplane,
                    R.drawable.preview_outline_location
                )
            )
            add(
                IconPackModel(
                    "PUI",
                    R.string.iconpack_pui_desc,
                    R.drawable.preview_pui_wifi,
                    R.drawable.preview_pui_signal,
                    R.drawable.preview_pui_airplane,
                    R.drawable.preview_pui_location
                )
            )
            add(
                IconPackModel(
                    "Rounded",
                    R.string.iconpack_rounded_desc,
                    R.drawable.preview_rounded_wifi,
                    R.drawable.preview_rounded_signal,
                    R.drawable.preview_rounded_airplane,
                    R.drawable.preview_rounded_location
                )
            )
            add(
                IconPackModel(
                    "Sam",
                    R.string.iconpack_sam_desc,
                    R.drawable.preview_sam_wifi,
                    R.drawable.preview_sam_signal,
                    R.drawable.preview_sam_airplane,
                    R.drawable.preview_sam_location
                )
            )
            add(
                IconPackModel(
                    "Victor",
                    R.string.iconpack_victor_desc,
                    R.drawable.preview_victor_wifi,
                    R.drawable.preview_victor_signal,
                    R.drawable.preview_victor_airplane,
                    R.drawable.preview_victor_location
                )
            )
        }

        return IconPackAdapter(
            requireContext(),
            iconPackList,
            loadingDialog!!,
            "IPSUI",
            onButtonClick
        )
    }

    private val onButtonClick = object : IconPackAdapter.OnButtonClick {

        override fun onEnableClick(position: Int, item: IconPackModel) {
            IconPackManager.enableOverlay(index = position + 1, "IPAS", "IPSUI")
        }

        override fun onDisableClick(position: Int, item: IconPackModel) {
            IconPackManager.disableOverlay(index = position + 1, "IPAS", "IPSUI")
        }
    }

    override fun onDestroy() {
        loadingDialog?.dismiss()

        super.onDestroy()
    }
}