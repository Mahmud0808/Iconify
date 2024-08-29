package com.drdisagree.iconify.ui.fragments.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.drdisagree.iconify.R
import com.drdisagree.iconify.databinding.FragmentNotificationBinding
import com.drdisagree.iconify.ui.adapters.MenuAdapter
import com.drdisagree.iconify.ui.adapters.NotificationAdapter
import com.drdisagree.iconify.ui.adapters.SectionTitleAdapter
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.dialogs.LoadingDialog
import com.drdisagree.iconify.ui.models.MenuModel
import com.drdisagree.iconify.ui.models.NotificationModel
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader

class Notification : BaseFragment() {

    private lateinit var binding: FragmentNotificationBinding
    private var loadingDialog: LoadingDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotificationBinding.inflate(inflater, container, false)
        val view: View = binding.getRoot()

        // Header
        setHeader(
            requireContext(),
            getParentFragmentManager(),
            binding.header.toolbar,
            R.string.activity_title_notification
        )

        // Loading dialog while enabling or disabling pack
        loadingDialog = LoadingDialog(requireContext())

        // RecyclerView
        binding.notificationsContainer.setLayoutManager(LinearLayoutManager(requireContext()))

        val adapter = ConcatAdapter(
            initActivityItems(),
            SectionTitleAdapter(
                requireContext(),
                R.layout.view_section_title_notif,
                R.string.notification_styles
            ),
            initNotificationItems()
        )

        binding.notificationsContainer.setAdapter(adapter)
        binding.notificationsContainer.setHasFixedSize(true)

        return view
    }

    private fun initActivityItems(): MenuAdapter {
        val notificationActivityList = ArrayList<MenuModel>().apply {
            add(
                MenuModel(
                    NotificationPixel(),
                    resources.getString(R.string.activity_title_pixel_variant),
                    resources.getString(R.string.activity_desc_pixel_variant),
                    R.drawable.ic_pixel_device
                )
            )
        }

        return MenuAdapter(
            parentFragmentManager,
            requireContext(),
            notificationActivityList
        )
    }

    private fun initNotificationItems(): NotificationAdapter {
        val notificationList = ArrayList<NotificationModel>().apply {
            add(
                NotificationModel(
                    "Default",
                    R.drawable.notif_default
                )
            )
            add(
                NotificationModel(
                    "Layers",
                    R.drawable.notif_layers
                )
            )
            add(
                NotificationModel(
                    "Thin Outline",
                    R.drawable.notif_thin_outline
                )
            )
            add(
                NotificationModel(
                    "Bottom Outline",
                    R.drawable.notif_bottom_outline
                )
            )
            add(
                NotificationModel(
                    "Neumorph",
                    R.drawable.notif_neumorph
                )
            )
            add(
                NotificationModel(
                    "Stack",
                    R.drawable.notif_stack
                )
            )
            add(
                NotificationModel(
                    "Side Stack",
                    R.drawable.notif_side_stack
                )
            )
            add(
                NotificationModel(
                    "Outline",
                    R.drawable.notif_outline
                )
            )
            add(
                NotificationModel(
                    "Leafy Outline",
                    R.drawable.notif_leafy_outline
                )
            )
            add(
                NotificationModel(
                    "Lighty",
                    R.drawable.notif_lighty
                )
            )
            add(
                NotificationModel(
                    "Neumorph Outline",
                    R.drawable.notif_neumorph_outline
                )
            )
            add(
                NotificationModel(
                    "Cyberponk",
                    R.drawable.notif_cyberponk
                )
            )
            add(
                NotificationModel(
                    "Cyberponk v2",
                    R.drawable.notif_cyberponk_v2
                )
            )
            add(
                NotificationModel(
                    "Thread Line",
                    R.drawable.notif_thread_line
                )
            )
            add(
                NotificationModel(
                    "Faded",
                    R.drawable.notif_faded
                )
            )
            add(
                NotificationModel(
                    "Dumbbell",
                    R.drawable.notif_dumbbell
                )
            )
            add(
                NotificationModel(
                    "Semi Transparent",
                    R.drawable.notif_semi_transparent
                )
            )
            add(
                NotificationModel(
                    "Pitch Black",
                    R.drawable.notif_pitch_black
                )
            )
            add(
                NotificationModel(
                    "Duoline",
                    R.drawable.notif_duoline
                )
            )
            add(
                NotificationModel(
                    "iOS",
                    R.drawable.notif_ios
                )
            )
        }

        return NotificationAdapter(
            requireContext(),
            notificationList,
            loadingDialog!!,
            "NFN"
        )
    }

    override fun onDestroy() {
        loadingDialog?.dismiss()

        super.onDestroy()
    }
}