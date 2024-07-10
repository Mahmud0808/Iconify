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

class WiFiIcons : BaseFragment() {

    private lateinit var binding: FragmentIconPackBinding
    private var loadingDialog: LoadingDialog? = null
    val wifiIcons: java.util.ArrayList<IconPackModel> = ArrayList()

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
            R.string.activity_title_wifi_icons
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
        val pack = getOverlayForComponent("WIFI")
        for (i in pack.indices) {
            if (!pack[i].contains("]")) continue
            val pkgName = pack[i].split("]")[1].trim()
            val name = getStringFromOverlay(Iconify.appContext, pkgName, "theme_name") ?: "Unknown"
            Log.d("WifiIcons", "initIconPackItems: $pkgName | $name")
            wifiIcons.add(
                IconPackModel(
                    name,
                    pkgName,
                    getDrawableFromOverlay(Iconify.appContext, pkgName, "ic_wifi_signal_1"),
                    getDrawableFromOverlay(Iconify.appContext, pkgName, "ic_wifi_signal_2"),
                    getDrawableFromOverlay(Iconify.appContext, pkgName, "ic_wifi_signal_3"),
                    getDrawableFromOverlay(Iconify.appContext, pkgName, "ic_wifi_signal_4"),
                    pack[i].contains("[x]")
                )
            )
        }
        wifiIcons.sortBy { it.name }

        return IconPackAdapter(
            requireContext(),
            wifiIcons,
            loadingDialog!!,
            "WIFI",
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
                // Force WiFi Icons
                item.packageName?.let { OverlayUtil.enableOverlay(it) }
                // get and set signal icon
                val signalPack = checkEnabledOverlay("SGIC")
                if (signalPack.isNotEmpty()) {
                    OverlayUtil.enableOverlay(signalPack)
                }
            }
        }

        override fun onDisableClick(position: Int, item: IconPackModel) {
            disableIcons()
        }
    }

    private fun disableIcons() {
        for (i in wifiIcons.indices) {
            wifiIcons[i].isEnabled = false
            putBoolean(wifiIcons[i].packageName, false)
            wifiIcons[i].packageName?.let { OverlayUtil.disableOverlay(it) }
        }
    }

    override fun onDestroy() {
        loadingDialog?.dismiss()

        super.onDestroy()
    }
}