package com.drdisagree.iconify.ui.fragments.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.R
import com.drdisagree.iconify.databinding.FragmentIconPackBinding
import com.drdisagree.iconify.ui.adapters.IconPackAdapter
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.dialogs.LoadingDialog
import com.drdisagree.iconify.ui.models.IconPackModel
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader
import com.drdisagree.iconify.utils.overlay.manager.SignalIconManager
import java.util.Locale

class CellularIcons : BaseFragment() {

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
            R.string.activity_title_cellular_icons
        )

        // Loading dialog while enabling or disabling pack
        loadingDialog = LoadingDialog(requireContext())

        // RecyclerView
        binding.iconPackContainer.setLayoutManager(LinearLayoutManager(requireContext()))

        binding.iconPackContainer.setAdapter(initIconPackItems())
        binding.iconPackContainer.setHasFixedSize(true)

        return view
    }

    @Suppress("DiscouragedApi")
    private fun initIconPackItems(): IconPackAdapter {
        val iconPackList = ArrayList<IconPackModel>().apply {
            val themes = listOf(
                "Aquarium", "Aurora", "Bars", "Butterfly", "Circle", "Daun",
                "Dec", "Deep", "Dora", "ZigZag", "Equal", "Faint UI",
                "Fan", "Lorn", "Gradicon", "Huawei", "Inside", "iOS",
                "Mini", "Nothing Dot", "Odin", "Pills", "Plumpy", "Rel",
                "Roman", "Round", "Scroll", "Sea", "Sneaky", "Stack",
                "Stroke", "Wannui", "Wavy", "Windows", "Wing", "Xperia"
            )

            val packageName = BuildConfig.APPLICATION_ID

            themes.forEachIndexed { _, themeName ->
                val themeResourceName = themeName.replace(" ", "_").lowercase(Locale.ROOT)

                add(
                    IconPackModel(
                        themeName,
                        0,
                        resources.getIdentifier(
                            "preview_${themeResourceName}_ic_signal_cellular_1_4_bar",
                            "drawable",
                            packageName
                        ),
                        resources.getIdentifier(
                            "preview_${themeResourceName}_ic_signal_cellular_2_4_bar",
                            "drawable",
                            packageName
                        ),
                        resources.getIdentifier(
                            "preview_${themeResourceName}_ic_signal_cellular_2_4_bar",
                            "drawable",
                            packageName
                        ),
                        resources.getIdentifier(
                            "preview_${themeResourceName}_ic_signal_cellular_4_4_bar",
                            "drawable",
                            packageName
                        )
                    )
                )
            }
        }

        return IconPackAdapter(
            requireContext(),
            iconPackList,
            loadingDialog!!,
            "SGIC",
            onButtonClick
        )
    }

    private val onButtonClick = object : IconPackAdapter.OnButtonClick {

        override fun onEnableClick(position: Int, item: IconPackModel) {
            SignalIconManager.enableOverlay(n = position + 1, "SGIC")
        }

        override fun onDisableClick(position: Int, item: IconPackModel) {
            SignalIconManager.disableOverlay(n = position + 1, "SGIC")
        }
    }

    override fun onDestroy() {
        loadingDialog?.dismiss()

        super.onDestroy()
    }
}