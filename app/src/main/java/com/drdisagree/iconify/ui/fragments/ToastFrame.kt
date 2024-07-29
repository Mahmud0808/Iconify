package com.drdisagree.iconify.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.Iconify.Companion.appContextLocale
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Const.FRAMEWORK_PACKAGE
import com.drdisagree.iconify.common.Preferences.SELECTED_TOAST_FRAME
import com.drdisagree.iconify.config.Prefs
import com.drdisagree.iconify.config.Prefs.getInt
import com.drdisagree.iconify.databinding.FragmentToastFrameBinding
import com.drdisagree.iconify.ui.adapters.IconPackAdapter
import com.drdisagree.iconify.ui.adapters.ToastAdapter
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.dialogs.LoadingDialog
import com.drdisagree.iconify.ui.models.IconPackModel
import com.drdisagree.iconify.ui.models.ToastModel
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader
import com.drdisagree.iconify.utils.SystemUtil.hasStoragePermission
import com.drdisagree.iconify.utils.SystemUtil.requestStoragePermission
import com.drdisagree.iconify.utils.overlay.OverlayUtil
import com.drdisagree.iconify.utils.overlay.compiler.OnDemandCompiler.buildOverlay
import com.drdisagree.iconify.utils.overlay.manager.IconPackManager
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

class ToastFrame : BaseFragment() {

    private lateinit var binding: FragmentToastFrameBinding
    private var loadingDialog: LoadingDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentToastFrameBinding.inflate(inflater, container, false)
        val view: View = binding.getRoot()

        // Header
        setHeader(
            requireContext(),
            getParentFragmentManager(),
            binding.header.toolbar,
            R.string.activity_title_toast_frame
        )

        // Loading dialog while enabling or disabling pack
        loadingDialog = LoadingDialog(requireContext())

        // Toast Frame style
        binding.toastStylesContainer.setLayoutManager(GridLayoutManager(requireContext(), 2))
        binding.toastStylesContainer.setAdapter(initToastFrameItems())
        binding.toastStylesContainer.setHasFixedSize(true)

        return view
    }

    private fun initToastFrameItems(): ToastAdapter {
        val selectedStyle = getInt(SELECTED_TOAST_FRAME, -1)
        val toastFrameStyle = ArrayList<ToastModel>().apply {
            add(ToastModel(R.drawable.toast_frame_style_1, R.string.style_0))
            add(ToastModel(R.drawable.toast_frame_style_1, R.string.style_1))
            add(ToastModel(R.drawable.toast_frame_style_2, R.string.style_2))
            add(ToastModel(R.drawable.toast_frame_style_3, R.string.style_3))
            add(ToastModel(R.drawable.toast_frame_style_4, R.string.style_4))
            add(ToastModel(R.drawable.toast_frame_style_5, R.string.style_5))
            add(ToastModel(R.drawable.toast_frame_style_6, R.string.style_6))
            add(ToastModel(R.drawable.toast_frame_style_7, R.string.style_7))
            add(ToastModel(R.drawable.toast_frame_style_8, R.string.style_8))
            add(ToastModel(R.drawable.toast_frame_style_9, R.string.style_9))
            add(ToastModel(R.drawable.toast_frame_style_10, R.string.style_10))
            add(ToastModel(R.drawable.toast_frame_style_11, R.string.style_11))
        }

        return ToastAdapter(
            requireContext(),
            toastFrameStyle,
            onToastClick
        )
    }

    private val onToastClick = object : ToastAdapter.OnToastClick {
        override fun onToastClick(position: Int, item: ToastModel) {
            // Show loading dialog
            loadingDialog!!.show(appContextLocale.resources.getString(R.string.loading_dialog_wait))

            Thread {
                val hasErroredOut = AtomicBoolean(false)

                try {
                    hasErroredOut.set(
                        buildOverlay(
                            "TSTFRM",
                            position,
                            FRAMEWORK_PACKAGE,
                            true
                        )
                    )
                } catch (e: IOException) {
                    hasErroredOut.set(true)
                    Log.e("ToastFrame", e.toString())
                }

                if (!hasErroredOut.get()) {
                    Prefs.putInt(SELECTED_TOAST_FRAME, position)
                    val ad = binding.toastStylesContainer.adapter as ToastAdapter
                    ad.notifyChange()
                }

                Handler(Looper.getMainLooper()).postDelayed({
                    // Hide loading dialog
                    loadingDialog!!.hide()

                    if (!hasErroredOut.get()) {
                        Toast.makeText(
                            appContext,
                            appContextLocale.resources.getString(R.string.toast_applied),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            appContext,
                            appContextLocale.resources.getString(R.string.toast_error),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }, 3000)
            }.start()
        }
    }

    override fun onDestroy() {
        loadingDialog?.dismiss()

        super.onDestroy()
    }
}