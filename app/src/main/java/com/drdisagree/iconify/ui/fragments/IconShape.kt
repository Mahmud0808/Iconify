package com.drdisagree.iconify.ui.fragments

import android.annotation.SuppressLint
import android.content.res.ColorStateList
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
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.Iconify.Companion.appContextLocale
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Const.FRAMEWORK_PACKAGE
import com.drdisagree.iconify.common.Preferences.SELECTED_ICON_SHAPE
import com.drdisagree.iconify.config.Prefs
import com.drdisagree.iconify.databinding.FragmentIconShapeBinding
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.dialogs.LoadingDialog
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader
import com.drdisagree.iconify.utils.SystemUtil.hasStoragePermission
import com.drdisagree.iconify.utils.SystemUtil.requestStoragePermission
import com.drdisagree.iconify.utils.overlay.OverlayUtil
import com.drdisagree.iconify.utils.overlay.compiler.OnDemandCompiler.buildOverlay
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

class IconShape : BaseFragment() {

    private lateinit var binding: FragmentIconShapeBinding
    private var loadingDialog: LoadingDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentIconShapeBinding.inflate(inflater, container, false)
        val view: View = binding.getRoot()

        // Header
        setHeader(
            requireContext(),
            getParentFragmentManager(),
            binding.header.toolbar,
            R.string.activity_title_icon_shape
        )

        // Loading dialog while enabling or disabling pack
        loadingDialog = LoadingDialog(requireContext())

        // Icon masking shape list
        addItem(initIconShapeList())
        refreshBackground()

        return view
    }

    private fun initIconShapeList(): ArrayList<Array<Any>> {
        val iconShapePreviewStyles = ArrayList<Array<Any>>().apply {
            add(
                arrayOf(
                    R.drawable.icon_shape_none,
                    R.string.icon_mask_style_none
                )
            )
            add(
                arrayOf(
                    R.drawable.icon_shape_pebble,
                    R.string.icon_mask_style_pebble
                )
            )
            add(
                arrayOf(
                    R.drawable.icon_shape_rounded_hexagon,
                    R.string.icon_mask_style_hexagon
                )
            )
            add(
                arrayOf(
                    R.drawable.icon_shape_samsung,
                    R.string.icon_mask_style_samsung
                )
            )
            add(
                arrayOf(
                    R.drawable.icon_shape_scroll,
                    R.string.icon_mask_style_scroll
                )
            )
            add(
                arrayOf(
                    R.drawable.icon_shape_teardrops,
                    R.string.icon_mask_style_teardrop
                )
            )
            add(
                arrayOf(
                    R.drawable.icon_shape_square,
                    R.string.icon_mask_style_square
                )
            )
            add(
                arrayOf(
                    R.drawable.icon_shape_rounded_rectangle,
                    R.string.icon_mask_style_rounded_rectangle
                )
            )
            add(
                arrayOf(
                    R.drawable.icon_shape_ios,
                    R.string.icon_mask_style_ios
                )
            )
            add(
                arrayOf(
                    R.drawable.icon_shape_cloudy,
                    R.string.icon_mask_style_cloudy
                )
            )
            add(
                arrayOf(
                    R.drawable.icon_shape_cylinder,
                    R.string.icon_mask_style_cylinder
                )
            )
            add(
                arrayOf(
                    R.drawable.icon_shape_flower,
                    R.string.icon_mask_style_flower
                )
            )
            add(
                arrayOf(
                    R.drawable.icon_shape_heart,
                    R.string.icon_mask_style_heart
                )
            )
            add(
                arrayOf(
                    R.drawable.icon_shape_leaf,
                    R.string.icon_mask_style_leaf
                )
            )
            add(
                arrayOf(
                    R.drawable.icon_shape_stretched,
                    R.string.icon_mask_style_stretched
                )
            )
            add(
                arrayOf(
                    R.drawable.icon_shape_tapered_rectangle,
                    R.string.icon_mask_style_tapered_rectangle
                )
            )
            add(
                arrayOf(
                    R.drawable.icon_shape_vessel,
                    R.string.icon_mask_style_vessel
                )
            )
            add(
                arrayOf(
                    R.drawable.icon_shape_rohie_meow,
                    R.string.icon_mask_style_rice_rohie_meow
                )
            )
            add(
                arrayOf(
                    R.drawable.icon_shape_force_round,
                    R.string.icon_mask_style_force_round
                )
            )
        }

        return iconShapePreviewStyles
    }

    // Function to add new item in list
    @SuppressLint("UseCompatLoadingForDrawables")
    private fun addItem(pack: ArrayList<Array<Any>>) {
        @ColorInt val colorBackground = appContextLocale.resources.getColor(
            R.color.colorBackground,
            appContext.theme
        )

        for (i in pack.indices) {
            val list = LayoutInflater.from(requireContext())
                .inflate(
                    R.layout.view_icon_shape,
                    binding.iconShapePreviewContainer,
                    false
                )

            val iconContainerBg = list.findViewById<LinearLayout>(R.id.mask_shape_bg)
            val iconContainerFg = list.findViewById<LinearLayout>(R.id.mask_shape_fg)

            iconContainerBg.background = ContextCompat.getDrawable(appContext, pack[i][0] as Int)
            iconContainerFg.background = ContextCompat.getDrawable(appContext, pack[i][0] as Int)
            iconContainerFg.setBackgroundTintList(ColorStateList.valueOf(colorBackground))

            val styleName = list.findViewById<TextView>(R.id.shape_name)
            styleName.text = resources.getString(pack[i][1] as Int)

            list.setOnClickListener {
                if (i == 0) {
                    Prefs.putInt(SELECTED_ICON_SHAPE, i)
                    OverlayUtil.disableOverlay("IconifyComponentSIS.overlay")

                    Toast.makeText(
                        appContext,
                        resources.getString(R.string.toast_disabled),
                        Toast.LENGTH_SHORT
                    ).show()

                    refreshBackground()
                } else {
                    if (!hasStoragePermission()) {
                        requestStoragePermission(
                            requireContext()
                        )
                    } else {
                        // Show loading dialog
                        loadingDialog!!.show(resources.getString(R.string.loading_dialog_wait))

                        Thread {
                            val hasErroredOut = AtomicBoolean(false)

                            try {
                                hasErroredOut.set(
                                    buildOverlay(
                                        "SIS",
                                        i,
                                        FRAMEWORK_PACKAGE,
                                        true
                                    )
                                )
                            } catch (e: IOException) {
                                hasErroredOut.set(true)
                                Log.e("IconShape", e.toString())
                            }

                            if (!hasErroredOut.get()) {
                                Prefs.putInt(SELECTED_ICON_SHAPE, i)
                                refreshBackground()
                            }

                            Handler(Looper.getMainLooper()).postDelayed({
                                // Hide loading dialog
                                loadingDialog!!.hide()
                                if (!hasErroredOut.get()) {
                                    Toast.makeText(
                                        appContext,
                                        appContextLocale.resources
                                            .getString(R.string.toast_applied),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        appContext,
                                        appContextLocale.resources
                                            .getString(R.string.toast_error),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }, 3000)
                        }.start()
                    }
                }
            }

            binding.iconShapePreviewContainer.addView(list)
        }
    }

    // Function to check for bg drawable changes
    private fun refreshBackground() {
        @ColorInt val colorSuccess = appContextLocale.resources.getColor(
            R.color.colorSuccess,
            appContext.theme
        )
        @ColorInt val textColorSecondary = appContextLocale.resources.getColor(
            R.color.textColorSecondary,
            appContext.theme
        )

        for (i in 0 until binding.iconShapePreviewContainer.childCount) {
            val child = binding.iconShapePreviewContainer.getChildAt(i)
                .findViewById<LinearLayout>(R.id.list_item_shape)

            val title = child.findViewById<TextView>(R.id.shape_name)
            val iconContainerBg = child.findViewById<LinearLayout>(R.id.mask_shape_bg)

            if (i == Prefs.getInt(SELECTED_ICON_SHAPE, 0)) {
                iconContainerBg.setBackgroundTintList(ColorStateList.valueOf(colorSuccess))
                title.setTextColor(colorSuccess)
            } else {
                iconContainerBg.setBackgroundTintList(ColorStateList.valueOf(textColorSecondary))
                title.setTextColor(textColorSecondary)
            }
        }
    }

    override fun onDestroy() {
        loadingDialog?.dismiss()

        super.onDestroy()
    }
}