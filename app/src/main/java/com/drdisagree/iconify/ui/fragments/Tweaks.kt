package com.drdisagree.iconify.ui.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation.findNavController
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.R
import com.drdisagree.iconify.databinding.FragmentTweaksBinding
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader
import com.drdisagree.iconify.ui.widgets.MenuWidget
import com.drdisagree.iconify.utils.AppUtil.isLsposedInstalled

class Tweaks : BaseFragment() {

    private lateinit var binding: FragmentTweaksBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTweaksBinding.inflate(inflater, container, false)
        val view: View = binding.getRoot()

        // Header
        setHeader(
            requireContext(),
            getParentFragmentManager(),
            binding.header.toolbar,
            R.string.navbar_tweaks
        )

        addItem(initTweaksItemList(view))

        return view
    }

    private fun initTweaksItemList(view: View): ArrayList<Array<Any>> {
        val tweaksList = ArrayList<Array<Any>>().apply {
            add(
                arrayOf(
                    R.id.action_tweaks_to_colorEngine,
                    resources.getString(R.string.activity_title_color_engine),
                    resources.getString(R.string.activity_desc_color_engine),
                    R.drawable.ic_tweaks_color
                )
            )
            add(
                arrayOf(
                    R.id.action_tweaks_to_uiRoundness,
                    resources.getString(R.string.activity_title_ui_roundness),
                    resources.getString(R.string.activity_desc_ui_roundness),
                    R.drawable.ic_tweaks_roundness
                )
            )
            add(
                arrayOf(
                    R.id.action_tweaks_to_qsRowColumn,
                    resources.getString(R.string.activity_title_qs_row_column),
                    resources.getString(R.string.activity_desc_qs_row_column),
                    R.drawable.ic_qs_row_column
                )
            )
            add(
                arrayOf(
                    R.id.action_tweaks_to_qsIconLabel,
                    resources.getString(R.string.activity_title_qs_icon_label),
                    resources.getString(R.string.activity_desc_qs_icon_label),
                    R.drawable.ic_qs_icon_and_label
                )
            )
            add(
                arrayOf(
                    R.id.action_tweaks_to_qsTileSize,
                    resources.getString(R.string.activity_title_qs_tile_size),
                    resources.getString(R.string.activity_desc_qs_tile_size),
                    R.drawable.ic_qs_tile_size
                )
            )
            add(
                arrayOf(
                    R.id.action_tweaks_to_qsPanelMargin,
                    resources.getString(R.string.activity_title_qs_panel_margin),
                    resources.getString(R.string.activity_desc_qs_panel_margin),
                    R.drawable.ic_qs_top_margin
                )
            )
            add(
                arrayOf(
                    R.id.action_tweaks_to_statusbar,
                    resources.getString(R.string.activity_title_statusbar),
                    resources.getString(R.string.activity_desc_statusbar),
                    R.drawable.ic_tweaks_statusbar
                )
            )
            add(
                arrayOf(
                    R.id.action_tweaks_to_navigationBar,
                    resources.getString(R.string.activity_title_navigation_bar),
                    resources.getString(R.string.activity_desc_navigation_bar),
                    R.drawable.ic_tweaks_navbar
                )
            )
            add(
                arrayOf(
                    R.id.action_tweaks_to_mediaPlayer,
                    resources.getString(R.string.activity_title_media_player),
                    resources.getString(R.string.activity_desc_media_player),
                    R.drawable.ic_tweaks_media
                )
            )
            add(
                arrayOf(
                    R.id.action_tweaks_to_volumePanel,
                    resources.getString(R.string.activity_title_volume_panel),
                    resources.getString(R.string.activity_desc_volume_panel),
                    R.drawable.ic_tweaks_volume
                )
            )
            add(
                arrayOf(
                    R.id.action_tweaks_to_miscellaneous,
                    resources.getString(R.string.activity_title_miscellaneous),
                    resources.getString(R.string.activity_desc_miscellaneous),
                    R.drawable.ic_tweaks_miscellaneous
                )
            )
            add(
                arrayOf(
                    View.OnClickListener {
                        // Check if LSPosed is installed or not
                        if (!isLsposedInstalled) {
                            Toast.makeText(
                                appContext,
                                resources.getString(R.string.toast_lsposed_not_found),
                                Toast.LENGTH_SHORT
                            ).show()
                            return@OnClickListener
                        }
                        findNavController(view).navigate(R.id.action_tweaks_to_nav_xposed_menu)
                    },
                    resources.getString(R.string.activity_title_xposed_menu),
                    resources.getString(R.string.activity_desc_xposed_menu),
                    R.drawable.ic_tweaks_xposed_menu
                )
            )
        }

        return tweaksList
    }

    // Function to add new item in list
    private fun addItem(pack: ArrayList<Array<Any>>) {
        for (i in pack.indices) {
            val menu = MenuWidget(requireActivity())

            menu.setTitle(pack[i][1] as String)
            menu.setSummary(pack[i][2] as String)
            menu.setIcon(pack[i][3] as Int)
            menu.setEndArrowVisibility(View.VISIBLE)

            if (pack[i][0] is View.OnClickListener) {
                menu.setOnClickListener(pack[i][0] as View.OnClickListener)
            } else if (pack[i][0] is Int) {
                menu.setOnClickListener {
                    findNavController(
                        binding.getRoot()
                    ).navigate((pack[i][0] as Int))
                }
            }

            if (pack[i][1] == resources.getString(R.string.activity_title_media_player) && Build.VERSION.SDK_INT >= 33) {
                menu.visibility = View.GONE
            }

            binding.tweaksList.addView(menu)
        }
    }
}