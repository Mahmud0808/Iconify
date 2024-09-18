package com.drdisagree.iconify.ui.fragments.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.Iconify.Companion.appContextLocale
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Const.FRAMEWORK_PACKAGE
import com.drdisagree.iconify.common.Preferences.SELECTED_TOAST_FRAME
import com.drdisagree.iconify.config.RPrefs
import com.drdisagree.iconify.config.RPrefs.getInt
import com.drdisagree.iconify.databinding.FragmentToastFrameBinding
import com.drdisagree.iconify.ui.adapters.ToastAdapter
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.dialogs.LoadingDialog
import com.drdisagree.iconify.ui.models.ToastModel
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader
import com.drdisagree.iconify.utils.SystemUtils.hasStoragePermission
import com.drdisagree.iconify.utils.SystemUtils.requestStoragePermission
import com.drdisagree.iconify.utils.overlay.OverlayUtils
import com.drdisagree.iconify.utils.overlay.compiler.OnDemandCompiler.buildOverlay
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
        val gridLayout = GridLayoutManager(requireContext(), 2)
        gridLayout.spanSizeLookup = object : SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val lastIndex = binding.toastStylesContainer.adapter?.itemCount?.minus(1) ?: 0

                return if (position == lastIndex && lastIndex % gridLayout.spanCount == 0) {
                    2
                } else {
                    1
                }
            }
        }
        binding.toastStylesContainer.setLayoutManager(gridLayout)
        binding.toastStylesContainer.setAdapter(initToastFrameItems())
        binding.toastStylesContainer.setHasFixedSize(true)

        return view
    }

    private fun initToastFrameItems(): ToastAdapter {
        val selectedStyle = getInt(SELECTED_TOAST_FRAME, -1)
        val toastFrameStyle = ArrayList<ToastModel>().apply {
            add(
                ToastModel(
                    R.drawable.toast_frame_style_1,
                    appContextLocale.resources.getString(R.string.style_0)
                )
            )
            add(
                ToastModel(
                    R.drawable.toast_frame_style_1,
                    String.format(appContextLocale.resources.getString(R.string.style), 1)
                )
            )
            add(
                ToastModel(
                    R.drawable.toast_frame_style_2,
                    String.format(appContextLocale.resources.getString(R.string.style), 2)
                )
            )
            add(
                ToastModel(
                    R.drawable.toast_frame_style_3,
                    String.format(appContextLocale.resources.getString(R.string.style), 3)
                )
            )
            add(
                ToastModel(
                    R.drawable.toast_frame_style_4,
                    String.format(appContextLocale.resources.getString(R.string.style), 4)
                )
            )
            add(
                ToastModel(
                    R.drawable.toast_frame_style_5,
                    String.format(appContextLocale.resources.getString(R.string.style), 5)
                )
            )
            add(
                ToastModel(
                    R.drawable.toast_frame_style_6,
                    String.format(appContextLocale.resources.getString(R.string.style), 6)
                )
            )
            add(
                ToastModel(
                    R.drawable.toast_frame_style_7,
                    String.format(appContextLocale.resources.getString(R.string.style), 7)
                )
            )
            add(
                ToastModel(
                    R.drawable.toast_frame_style_8,
                    String.format(appContextLocale.resources.getString(R.string.style), 8)
                )
            )
            add(
                ToastModel(
                    R.drawable.toast_frame_style_9,
                    String.format(appContextLocale.resources.getString(R.string.style), 9)
                )
            )
            add(
                ToastModel(
                    R.drawable.toast_frame_style_10,
                    String.format(appContextLocale.resources.getString(R.string.style), 10)
                )
            )
            add(
                ToastModel(
                    R.drawable.toast_frame_style_11,
                    String.format(appContextLocale.resources.getString(R.string.style), 11)
                )
            )
            add(
                ToastModel(
                    R.drawable.toast_frame_style_12,
                    String.format(appContextLocale.resources.getString(R.string.style), 12)
                )
            )
        }

        return ToastAdapter(
            requireContext(),
            toastFrameStyle,
            onToastClick
        )
    }

    private val onToastClick = object : ToastAdapter.OnToastClick {
        override fun onToastClick(position: Int, item: ToastModel) {

            if (!hasStoragePermission()) {
                requestStoragePermission(appContext)
                return
            }

            if (position == 0) {
                RPrefs.putInt(SELECTED_TOAST_FRAME, -1)
                OverlayUtils.disableOverlay("IconifyComponentTSTFRM.overlay")
                Toast.makeText(
                    appContext,
                    appContextLocale.resources.getString(R.string.toast_disabled),
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

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
                    RPrefs.putInt(SELECTED_TOAST_FRAME, position)
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