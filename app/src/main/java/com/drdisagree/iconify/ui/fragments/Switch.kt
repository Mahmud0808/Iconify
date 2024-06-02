package com.drdisagree.iconify.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.drdisagree.iconify.R
import com.drdisagree.iconify.databinding.FragmentSwitchBinding
import com.drdisagree.iconify.ui.adapters.SwitchAdapter
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.dialogs.LoadingDialog
import com.drdisagree.iconify.ui.models.SwitchModel
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader

class Switch : BaseFragment() {

    private lateinit var binding: FragmentSwitchBinding
    private var loadingDialog: LoadingDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSwitchBinding.inflate(inflater, container, false)
        val view: View = binding.getRoot()

        // Header
        setHeader(
            requireContext(),
            getParentFragmentManager(),
            binding.header.toolbar,
            R.string.activity_title_switch
        )

        // Loading dialog while enabling or disabling pack
        loadingDialog = LoadingDialog(requireContext())

        // RecyclerView
        binding.switchContainer.setLayoutManager(LinearLayoutManager(requireContext()))
        binding.switchContainer.setAdapter(initSwitchItems())
        binding.switchContainer.setHasFixedSize(true)

        return view
    }

    private fun initSwitchItems(): SwitchAdapter {
        val switchList = ArrayList<SwitchModel>().apply {
            add(
                SwitchModel(
                    "Minimal Switch",
                    R.drawable.switch_minimal_track,
                    R.drawable.switch_minimal_thumb
                )
            )
            add(
                SwitchModel(
                    "Material Switch",
                    R.drawable.switch_material_track,
                    R.drawable.switch_material_thumb
                )
            )
            add(
                SwitchModel(
                    "Realme Switch",
                    R.drawable.switch_realme_track,
                    R.drawable.switch_realme_thumb
                )
            )
            add(
                SwitchModel(
                    "iOS Switch",
                    R.drawable.switch_ios_track,
                    R.drawable.switch_ios_thumb
                )
            )
            add(
                SwitchModel(
                    "Outline Switch",
                    R.drawable.switch_outline_track,
                    R.drawable.switch_outline_thumb
                )
            )
            add(
                SwitchModel(
                    "Neumorph Switch",
                    R.drawable.switch_neumorph_track,
                    R.drawable.switch_neumorph_thumb
                )
            )
            add(
                SwitchModel(
                    "Emoji Switch",
                    R.drawable.switch_emoji_track,
                    R.drawable.switch_emoji_thumb
                )
            )
            add(
                SwitchModel(
                    "Tiny Switch",
                    R.drawable.switch_tiny_track,
                    R.drawable.switch_tiny_thumb
                )
            )
            add(
                SwitchModel(
                    "Shaded Switch",
                    R.drawable.switch_shaded_track,
                    R.drawable.switch_shaded_thumb
                )
            )
            add(
                SwitchModel(
                    "Foggy Switch",
                    R.drawable.switch_foggy_track,
                    R.drawable.switch_foggy_thumb
                )
            )
            add(
                SwitchModel(
                    "Checkmark Switch",
                    R.drawable.switch_checkmark_track,
                    R.drawable.switch_checkmark_thumb
                )
            )
        }

        return SwitchAdapter(
            requireContext(),
            switchList,
            loadingDialog!!
        )
    }

    override fun onDestroy() {
        loadingDialog?.dismiss()

        super.onDestroy()
    }
}