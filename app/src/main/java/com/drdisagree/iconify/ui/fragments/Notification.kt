package com.drdisagree.iconify.ui.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Dynamic.isAtleastA14
import com.drdisagree.iconify.common.Preferences.SHOW_NOTIFICATION_NORMAL_WARN
import com.drdisagree.iconify.config.Prefs
import com.drdisagree.iconify.databinding.FragmentNotificationBinding
import com.drdisagree.iconify.ui.adapters.MenuAdapter
import com.drdisagree.iconify.ui.adapters.NotificationAdapter
import com.drdisagree.iconify.ui.adapters.SectionTitleAdapter
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.dialogs.LoadingDialog
import com.drdisagree.iconify.ui.models.MenuModel
import com.drdisagree.iconify.ui.models.NotificationModel
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader
import com.google.android.material.dialog.MaterialAlertDialogBuilder

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
            initNotifItems()
        )

        binding.notificationsContainer.setAdapter(adapter)
        binding.notificationsContainer.setHasFixedSize(true)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isAtleastA14 && Prefs.getBoolean(SHOW_NOTIFICATION_NORMAL_WARN, true)) {
            try {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.attention)
                    .setMessage(R.string.requires_lsposed_for_a14)
                    .setPositiveButton(requireContext().resources.getString(R.string.understood)) { dialog: DialogInterface, _: Int ->
                        dialog.dismiss()
                    }
                    .setNegativeButton(requireContext().resources.getString(R.string.dont_show_again)) { dialog: DialogInterface, _: Int ->
                        dialog.dismiss()
                        Prefs.putBoolean(SHOW_NOTIFICATION_NORMAL_WARN, false)
                    }
                    .setCancelable(true)
                    .show()
            } catch (ignored: Exception) {
            }
        }
    }

    private fun initActivityItems(): MenuAdapter {
        val notifActivityList = ArrayList<MenuModel>().apply {
            add(
                MenuModel(
                    R.id.action_notification_to_notificationPixel,
                    resources.getString(R.string.activity_title_pixel_variant),
                    resources.getString(R.string.activity_desc_pixel_variant),
                    R.drawable.ic_pixel_device
                )
            )
        }

        return MenuAdapter(
            requireContext(),
            notifActivityList
        )
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
            "NFN"
        )
    }

    override fun onDestroy() {
        loadingDialog?.dismiss()

        super.onDestroy()
    }
}