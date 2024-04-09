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
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.Iconify.Companion.appContextLocale
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Const.FRAMEWORK_PACKAGE
import com.drdisagree.iconify.common.Preferences.SELECTED_TOAST_FRAME
import com.drdisagree.iconify.config.Prefs
import com.drdisagree.iconify.databinding.FragmentToastFrameBinding
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.dialogs.LoadingDialog
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader
import com.drdisagree.iconify.utils.SystemUtil.hasStoragePermission
import com.drdisagree.iconify.utils.SystemUtil.requestStoragePermission
import com.drdisagree.iconify.utils.overlay.OverlayUtil
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
        addItem(initToastFrameList())

        refreshBackground()

        return view
    }

    private fun initToastFrameList(): ArrayList<Array<Any>> {
        val toastFrameStyle = ArrayList<Array<Any>>().apply {
            add(arrayOf(R.drawable.toast_frame_style_1, R.string.style_0))
            add(arrayOf(R.drawable.toast_frame_style_1, R.string.style_1))
            add(arrayOf(R.drawable.toast_frame_style_2, R.string.style_2))
            add(arrayOf(R.drawable.toast_frame_style_3, R.string.style_3))
            add(arrayOf(R.drawable.toast_frame_style_4, R.string.style_4))
            add(arrayOf(R.drawable.toast_frame_style_5, R.string.style_5))
            add(arrayOf(R.drawable.toast_frame_style_6, R.string.style_6))
            add(arrayOf(R.drawable.toast_frame_style_7, R.string.style_7))
            add(arrayOf(R.drawable.toast_frame_style_8, R.string.style_8))
            add(arrayOf(R.drawable.toast_frame_style_9, R.string.style_9))
            add(arrayOf(R.drawable.toast_frame_style_10, R.string.style_10))
            add(arrayOf(R.drawable.toast_frame_style_11, R.string.style_11))
        }

        return toastFrameStyle
    }

    // Function to add new item in list
    @SuppressLint("UseCompatLoadingForDrawables")
    private fun addItem(pack: ArrayList<Array<Any>>) {
        for (i in pack.indices) {
            val list = LayoutInflater.from(requireContext())
                .inflate(R.layout.view_toast_frame, binding.toastFrameContainer, false)

            val toastContainer = list.findViewById<LinearLayout>(R.id.toast_container)
            toastContainer.background = ContextCompat.getDrawable(appContext, pack[i][0] as Int)

            val styleName = list.findViewById<TextView>(R.id.style_name)
            styleName.text = appContextLocale.resources.getString(pack[i][1] as Int)

            list.setOnClickListener {
                if (i == 0) {
                    Prefs.putInt(SELECTED_TOAST_FRAME, -1)

                    OverlayUtil.disableOverlay("IconifyComponentTSTFRM.overlay")

                    Toast.makeText(
                        appContext,
                        appContextLocale.resources
                            .getString(R.string.toast_disabled),
                        Toast.LENGTH_SHORT
                    ).show()

                    return@setOnClickListener
                }

                if (!hasStoragePermission()) {
                    requestStoragePermission(requireContext())
                } else {
                    // Show loading dialog
                    loadingDialog!!.show(appContextLocale.resources.getString(R.string.loading_dialog_wait))

                    Thread {
                        val hasErroredOut = AtomicBoolean(false)

                        try {
                            hasErroredOut.set(
                                buildOverlay(
                                    "TSTFRM",
                                    i,
                                    FRAMEWORK_PACKAGE,
                                    true
                                )
                            )
                        } catch (e: IOException) {
                            hasErroredOut.set(true)
                            Log.e("ToastFrame", e.toString())
                        }

                        if (!hasErroredOut.get()) {
                            Prefs.putInt(SELECTED_TOAST_FRAME, i)
                            refreshBackground()
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

            binding.toastFrameContainer.addView(list)
        }
    }

    // Function to check for bg drawable changes
    private fun refreshBackground() {
        var selected = false
        for (i in 0 until binding.toastFrameContainer.childCount) {
            val child = binding.toastFrameContainer.getChildAt(i)
                .findViewById<LinearLayout>(R.id.list_item_toast)

            val title = child.findViewById<TextView>(R.id.style_name)

            if (i == Prefs.getInt(SELECTED_TOAST_FRAME, -1)) {
                selected = true
                title.setTextColor(
                    appContextLocale.resources.getColor(
                        R.color.colorAccent,
                        appContext.theme
                    )
                )
            } else {
                title.setTextColor(
                    appContextLocale.resources.getColor(
                        R.color.textColorSecondary,
                        appContext.theme
                    )
                )
            }
        }

        if (!selected) {
            val child = binding.toastFrameContainer.getChildAt(0)
                .findViewById<LinearLayout>(R.id.list_item_toast)

            val title = child.findViewById<TextView>(R.id.style_name)

            title.setTextColor(
                appContextLocale.resources.getColor(
                    R.color.colorAccent,
                    appContext.theme
                )
            )
        }
    }

    override fun onDestroy() {
        loadingDialog?.dismiss()

        super.onDestroy()
    }
}