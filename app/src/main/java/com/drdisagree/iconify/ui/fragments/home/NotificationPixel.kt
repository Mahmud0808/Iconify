package com.drdisagree.iconify.ui.fragments.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.drdisagree.iconify.R
import com.drdisagree.iconify.databinding.FragmentNotificationPixelBinding
import com.drdisagree.iconify.ui.adapters.NotificationAdapter
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.dialogs.LoadingDialog
import com.drdisagree.iconify.ui.models.NotificationModel
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader

class NotificationPixel : BaseFragment() {

    private lateinit var binding: FragmentNotificationPixelBinding
    private var loadingDialog: LoadingDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotificationPixelBinding.inflate(inflater, container, false)
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
        binding.notificationsPixelContainer.setLayoutManager(LinearLayoutManager(requireContext()))
        binding.notificationsPixelContainer.setAdapter(initNotifItems())
        binding.notificationsPixelContainer.setHasFixedSize(true)

        return view
    }

    private fun initNotifItems(): NotificationAdapter {
        val notifList = ArrayList<NotificationModel>().apply {
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
            notifList,
            loadingDialog!!,
            "NFP"
        )
    }

    override fun onDestroy() {
        loadingDialog?.dismiss()

        super.onDestroy()
    }
}