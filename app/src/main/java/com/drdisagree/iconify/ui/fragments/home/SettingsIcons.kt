package com.drdisagree.iconify.ui.fragments.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Preferences.SELECTED_SETTINGS_ICONS_BG
import com.drdisagree.iconify.common.Preferences.SELECTED_SETTINGS_ICONS_COLOR
import com.drdisagree.iconify.common.Preferences.SELECTED_SETTINGS_ICONS_SET
import com.drdisagree.iconify.common.Preferences.SELECTED_SETTINGS_ICONS_SHAPE
import com.drdisagree.iconify.common.Preferences.SELECTED_SETTINGS_ICONS_SIZE
import com.drdisagree.iconify.config.RPrefs
import com.drdisagree.iconify.config.RPrefs.clearPrefs
import com.drdisagree.iconify.databinding.FragmentSettingsIconsBinding
import com.drdisagree.iconify.ui.dialogs.LoadingDialog
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader
import com.drdisagree.iconify.utils.SystemUtils.hasStoragePermission
import com.drdisagree.iconify.utils.SystemUtils.requestStoragePermission
import com.drdisagree.iconify.utils.overlay.OverlayUtils
import com.drdisagree.iconify.utils.overlay.OverlayUtils.enableOverlays
import com.drdisagree.iconify.utils.overlay.manager.SettingsIconResourceManager.buildOverlay
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

class SettingsIcons : Fragment() {

    private lateinit var binding: FragmentSettingsIconsBinding
    private var loadingDialog: LoadingDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsIconsBinding.inflate(inflater, container, false)
        val view: View = binding.getRoot()

        // Header
        setHeader(
            requireContext(),
            getParentFragmentManager(),
            binding.header.toolbar,
            R.string.activity_title_settings_icons
        )

        // Loading dialog while enabling or disabling pack
        loadingDialog = LoadingDialog(requireContext())

        // Retrieve previously saved preferences
        selectedIcon = RPrefs.getInt(SELECTED_SETTINGS_ICONS_SET, 1)

        // Background style
        binding.bgStyle.setSelectedIndex(RPrefs.getInt(SELECTED_SETTINGS_ICONS_BG, 1) - 1)
        binding.bgStyle.setOnItemSelectedListener { index: Int ->
            selectedBackground = index + 1
        }
        selectedBackground = binding.bgStyle.getSelectedIndex() + 1

        // Background Shape
        binding.bgShape.setSelectedIndex(RPrefs.getInt(SELECTED_SETTINGS_ICONS_SHAPE, 1) - 1)
        binding.bgShape.setOnItemSelectedListener { index: Int ->
            selectedShape = index + 1
        }
        selectedShape = binding.bgShape.getSelectedIndex() + 1

        // Icon Size
        binding.iconSize.setSelectedIndex(RPrefs.getInt(SELECTED_SETTINGS_ICONS_SIZE, 1) - 1)
        binding.iconSize.setOnItemSelectedListener { index: Int ->
            selectedSize = index + 1
        }
        selectedSize = binding.iconSize.getSelectedIndex() + 1

        // Icon color
        binding.iconColor.setSelectedIndex(RPrefs.getInt(SELECTED_SETTINGS_ICONS_COLOR, 1) - 1)
        binding.iconColor.setOnItemSelectedListener { index: Int ->
            selectedIconColor = index + 1
        }
        selectedIconColor = binding.iconColor.getSelectedIndex() + 1

        // Icon Pack list items
        addItem(initIconPackList())

        for (i in 0 until binding.iconPacksList.childCount) {
            val child = binding.iconPacksList.getChildAt(i)
                .findViewById<RelativeLayout>(R.id.icon_pack_child)

            if ((child.findViewById<View>(R.id.iconpack_title) as TextView).getText() === "Bubble" ||
                (child.findViewById<View>(R.id.iconpack_title) as TextView).getText() === "Bubble v2"
            ) {
                (child.findViewById<View>(R.id.iconpack_preview1) as ImageView).setColorFilter(0)
                (child.findViewById<View>(R.id.iconpack_preview2) as ImageView).setColorFilter(0)
                (child.findViewById<View>(R.id.iconpack_preview3) as ImageView).setColorFilter(0)
                (child.findViewById<View>(R.id.iconpack_preview4) as ImageView).setColorFilter(0)
            }
        }

        // Enable onClick event
        for (i in 0 until binding.iconPacksList.childCount) {
            enableOnClickListener(
                binding.iconPacksList.getChildAt(i).findViewById(R.id.icon_pack_child), i
            )
        }

        refreshBackground()

        // Enable and disable button
        if (RPrefs.getBoolean("IconifyComponentSIP1.overlay")) {
            binding.disableSettingsIcons.visibility = View.VISIBLE
        }
        binding.enableSettingsIcons.setOnClickListener {
            if (!hasStoragePermission()) {
                requestStoragePermission(requireContext())
            } else {
                // Show loading dialog
                loadingDialog!!.show(resources.getString(R.string.loading_dialog_wait))

                val hasErroredOut = AtomicBoolean(false)

                Thread {
                    try {
                        hasErroredOut.set(
                            buildOverlay(
                                selectedIcon,
                                selectedBackground,
                                selectedShape,
                                selectedSize,
                                selectedIconColor,
                                true
                            )
                        )
                    } catch (e: IOException) {
                        hasErroredOut.set(true)
                        Log.e("SettingsIcons", e.toString())
                    }

                    Handler(Looper.getMainLooper()).post {
                        if (!hasErroredOut.get()) {
                            RPrefs.putInt(SELECTED_SETTINGS_ICONS_SET, selectedIcon)
                            RPrefs.putInt(SELECTED_SETTINGS_ICONS_BG, selectedBackground)
                            RPrefs.putInt(SELECTED_SETTINGS_ICONS_SHAPE, selectedShape)
                            RPrefs.putInt(SELECTED_SETTINGS_ICONS_SIZE, selectedSize)
                            RPrefs.putInt(SELECTED_SETTINGS_ICONS_COLOR, selectedIconColor)

                            binding.disableSettingsIcons.visibility = View.VISIBLE
                            binding.iconPacksList.getChildAt(selectedIcon - 1)
                                .findViewById<View>(R.id.icon_selected).visibility = View.VISIBLE

                            enableOverlays(
                                "IconifyComponentCR1.overlay",
                                "IconifyComponentCR2.overlay"
                            )
                        }

                        Handler(Looper.getMainLooper()).postDelayed({
                            // Hide loading dialog
                            loadingDialog!!.hide()

                            if (hasErroredOut.get()) Toast.makeText(
                                appContext,
                                resources.getString(R.string.toast_error),
                                Toast.LENGTH_SHORT
                            ).show() else Toast.makeText(
                                appContext,
                                resources.getString(R.string.toast_applied),
                                Toast.LENGTH_SHORT
                            ).show()
                        }, 10)
                    }
                }.start()
            }
        }
        binding.disableSettingsIcons.setOnClickListener {
            binding.iconPacksList.getChildAt(RPrefs.getInt(SELECTED_SETTINGS_ICONS_SET, 1) - 1)
                .findViewById<View>(R.id.icon_selected).visibility = View.INVISIBLE

            clearPrefs(
                SELECTED_SETTINGS_ICONS_SET,
                SELECTED_SETTINGS_ICONS_BG,
                SELECTED_SETTINGS_ICONS_COLOR
            )

            binding.disableSettingsIcons.visibility = View.GONE

            OverlayUtils.disableOverlays(
                "IconifyComponentSIP1.overlay",
                "IconifyComponentSIP2.overlay",
                "IconifyComponentSIP3.overlay"
            )
        }

        return view
    }

    private fun initIconPackList(): ArrayList<Array<Any>> {
        val iconPackList = ArrayList<Array<Any>>().apply {
            add(
                arrayOf(
                    "Aurora",
                    R.string.iconpack_aurora_desc,
                    R.drawable.preview_aurora_wifi,
                    R.drawable.preview_aurora_signal,
                    R.drawable.preview_aurora_airplane,
                    R.drawable.preview_aurora_location
                )
            )
            add(
                arrayOf(
                    "Gradicon",
                    R.string.iconpack_gradicon_desc,
                    R.drawable.preview_gradicon_wifi,
                    R.drawable.preview_gradicon_signal,
                    R.drawable.preview_gradicon_airplane,
                    R.drawable.preview_gradicon_location
                )
            )
            add(
                arrayOf(
                    "Lorn",
                    R.string.iconpack_lorn_desc,
                    R.drawable.preview_lorn_wifi,
                    R.drawable.preview_lorn_signal,
                    R.drawable.preview_lorn_airplane,
                    R.drawable.preview_lorn_location
                )
            )
            add(
                arrayOf(
                    "Plumpy",
                    R.string.iconpack_plumpy_desc,
                    R.drawable.preview_plumpy_wifi,
                    R.drawable.preview_plumpy_signal,
                    R.drawable.preview_plumpy_airplane,
                    R.drawable.preview_plumpy_location
                )
            )
            add(
                arrayOf(
                    "Bubble v1",
                    R.string.settings_iconpack_bubble_v1,
                    R.drawable.preview_bubble_v1_1,
                    R.drawable.preview_bubble_v1_2,
                    R.drawable.preview_bubble_v1_3,
                    R.drawable.preview_bubble_v1_4
                )
            )
            add(
                arrayOf(
                    "Bubble v2",
                    R.string.settings_iconpack_bubble_v2,
                    R.drawable.preview_bubble_v2_1,
                    R.drawable.preview_bubble_v2_2,
                    R.drawable.preview_bubble_v2_3,
                    R.drawable.preview_bubble_v2_4
                )
            )
        }

        return iconPackList
    }

    // Function to check for layout changes
    private fun refreshLayout(layout: RelativeLayout) {
        for (i in 0 until binding.iconPacksList.childCount) {
            val child = binding.iconPacksList.getChildAt(i)
                .findViewById<RelativeLayout>(R.id.icon_pack_child)

            itemSelected(
                child,
                child === layout,
                i + 1
            )
        }
    }

    // Function to check for bg drawable changes
    private fun refreshBackground() {
        for (i in 0 until binding.iconPacksList.childCount) {
            val child = binding.iconPacksList.getChildAt(i)
                .findViewById<RelativeLayout>(R.id.icon_pack_child)

            itemSelected(
                child,
                RPrefs.getInt(SELECTED_SETTINGS_ICONS_SET, 1) == i + 1,
                i + 1
            )
        }
    }

    // Function for onClick events
    private fun enableOnClickListener(layout: RelativeLayout, index: Int) {
        // Set onClick operation for options in list
        layout.setOnClickListener {
            refreshLayout(layout)
            selectedIcon = index + 1
        }
    }

    // Function to add new item in list
    private fun addItem(pack: ArrayList<Array<Any>>) {
        for (i in pack.indices) {
            val list = LayoutInflater.from(requireContext())
                .inflate(R.layout.view_list_option_settings_icons, binding.iconPacksList, false)

            val name = list.findViewById<TextView>(R.id.iconpack_title)
            name.text = pack[i][0] as String

            val description = list.findViewById<TextView>(R.id.iconpack_desc)
            description.text = resources.getString(pack[i][1] as Int)

            val ic1 = list.findViewById<ImageView>(R.id.iconpack_preview1)
            ic1.setImageResource(pack[i][2] as Int)

            val ic2 = list.findViewById<ImageView>(R.id.iconpack_preview2)
            ic2.setImageResource(pack[i][3] as Int)

            val ic3 = list.findViewById<ImageView>(R.id.iconpack_preview3)
            ic3.setImageResource(pack[i][4] as Int)

            val ic4 = list.findViewById<ImageView>(R.id.iconpack_preview4)
            ic4.setImageResource(pack[i][5] as Int)

            binding.iconPacksList.addView(list)
        }
    }

    private fun itemSelected(parent: View, state: Boolean, selectedIndex: Int) {
        if (state) {
            parent.background =
                ContextCompat.getDrawable(appContext, R.drawable.container_selected)
            (parent.findViewById<View>(R.id.iconpack_title) as TextView).setTextColor(
                ContextCompat.getColor(
                    appContext, R.color.colorAccent
                )
            )
            (parent.findViewById<View>(R.id.iconpack_desc) as TextView).setTextColor(
                ContextCompat.getColor(
                    appContext, R.color.colorAccent
                )
            )
            parent.findViewById<View>(R.id.icon_selected).visibility =
                if (RPrefs.getBoolean("IconifyComponentSIP1.overlay") &&
                    RPrefs.getInt(SELECTED_SETTINGS_ICONS_SET, 1) == selectedIndex
                ) View.VISIBLE else View.INVISIBLE
            parent.findViewById<View>(R.id.iconpack_desc).setAlpha(0.8f)
        } else {
            parent.background =
                ContextCompat.getDrawable(appContext, R.drawable.item_background_material)
            (parent.findViewById<View>(R.id.iconpack_title) as TextView).setTextColor(
                ContextCompat.getColor(
                    appContext, R.color.text_color_primary
                )
            )
            (parent.findViewById<View>(R.id.iconpack_desc) as TextView).setTextColor(
                ContextCompat.getColor(
                    appContext, R.color.text_color_secondary
                )
            )
            parent.findViewById<View>(R.id.icon_selected).visibility = View.INVISIBLE
            parent.findViewById<View>(R.id.iconpack_desc).setAlpha(1f)
        }
    }

    override fun onDestroy() {
        loadingDialog?.dismiss()

        super.onDestroy()
    }

    companion object {
        private var selectedBackground = 1
        private var selectedShape = 1
        private var selectedSize = 1
        private var selectedIconColor = 1
        private var selectedIcon = 1
    }
}