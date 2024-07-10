package com.drdisagree.iconify.ui.fragments

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.drdisagree.iconify.Iconify
import com.drdisagree.iconify.R
import com.drdisagree.iconify.config.Prefs.putBoolean
import com.drdisagree.iconify.databinding.FragmentIconPackBinding
import com.drdisagree.iconify.ui.adapters.IconPackAdapter
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.dialogs.LoadingDialog
import com.drdisagree.iconify.ui.models.IconPackModel
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader
import com.drdisagree.iconify.utils.overlay.OverlayUtil
import com.drdisagree.iconify.utils.overlay.OverlayUtil.checkEnabledOverlay
import com.drdisagree.iconify.utils.overlay.OverlayUtil.getDrawableFromOverlay
import com.drdisagree.iconify.utils.overlay.OverlayUtil.getOverlayForComponent
import com.drdisagree.iconify.utils.overlay.OverlayUtil.getStringFromOverlay
import java.io.IOException
import java.io.InputStream

class SignalIcons : BaseFragment() {

    private lateinit var binding: FragmentIconPackBinding
    private var loadingDialog: LoadingDialog? = null
    val signalIcons: java.util.ArrayList<IconPackModel> = ArrayList()

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
            R.string.activity_title_signal_icons
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
        val pack = getOverlayForComponent("SGIC")
        for (i in pack.indices) {
            if (!pack[i].contains("]")) continue
            val pkgName = pack[i].split("]")[1].trim()
            val name = getStringFromOverlay(Iconify.appContext, pkgName, "theme_name") ?: "Unknown"
            Log.d("SignalIcons", "initIconPackItems: $pkgName | $name")
            signalIcons.add(
                IconPackModel(
                    name,
                    pkgName,
                    getDrawableFromOverlay(Iconify.appContext, pkgName, "ic_signal_cellular_1_4_bar"),
                    getDrawableFromOverlay(Iconify.appContext, pkgName, "ic_signal_cellular_2_4_bar"),
                    getDrawableFromOverlay(Iconify.appContext, pkgName, "ic_signal_cellular_3_4_bar"),
                    getDrawableFromOverlay(Iconify.appContext, pkgName, "ic_signal_cellular_4_4_bar"),
                    pack[i].contains("[x]")
                )
            )
        }
        signalIcons.sortBy { it.name }

        return IconPackAdapter(
            requireContext(),
            signalIcons,
            loadingDialog!!,
            "SGIC",
            onButtonClick
        )
    }

    private val onButtonClick = object : IconPackAdapter.OnButtonClick {

        override fun onEnableClick(position: Int, item: IconPackModel) {
            disableIcons()
            putBoolean(item.packageName, true)
            val pkgName = checkEnabledOverlay("IPAS")
            if (pkgName.isNotEmpty()) {
                OverlayUtil.disableOverlay(pkgName)
            }
            item.packageName?.let { OverlayUtil.enableOverlay(it) }
            if (pkgName.isNotEmpty()) {
                OverlayUtil.enableOverlay(pkgName, "high")
                // Force Signal Icons
                item.packageName?.let { OverlayUtil.enableOverlay(it) }
                // get and set wifi icon
                val wifiPack = checkEnabledOverlay("WIFI")
                if (wifiPack.isNotEmpty()) {
                    OverlayUtil.enableOverlay(wifiPack)
                }
            }
        }

        override fun onDisableClick(position: Int, item: IconPackModel) {
            disableIcons()
        }
    }

    private fun disableIcons() {
        for (i in signalIcons.indices) {
            signalIcons[i].isEnabled = false
            putBoolean(signalIcons[i].packageName, false)
            signalIcons[i].packageName?.let { OverlayUtil.disableOverlay(it)}
        }
    }

    override fun onDestroy() {
        loadingDialog?.dismiss()

        super.onDestroy()
    }
}