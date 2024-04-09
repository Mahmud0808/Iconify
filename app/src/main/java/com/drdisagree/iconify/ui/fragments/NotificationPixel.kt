package com.drdisagree.iconify.ui.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Dynamic.isAtleastA14
import com.drdisagree.iconify.common.Preferences.SHOW_NOTIFICATION_PIXEL_WARN
import com.drdisagree.iconify.config.Prefs
import com.drdisagree.iconify.databinding.FragmentNotificationPixelBinding
import com.drdisagree.iconify.ui.adapters.NotificationAdapter
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.dialogs.LoadingDialog
import com.drdisagree.iconify.ui.models.NotificationModel
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader
import com.google.android.material.dialog.MaterialAlertDialogBuilder

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isAtleastA14 && Prefs.getBoolean(SHOW_NOTIFICATION_PIXEL_WARN, true)) {
            try {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.attention)
                    .setMessage(R.string.requires_lsposed_for_a14)
                    .setPositiveButton(requireContext().resources.getString(R.string.understood)) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
                    .setNegativeButton(requireContext().resources.getString(R.string.dont_show_again)) { dialog: DialogInterface, _: Int ->
                        dialog.dismiss()
                        Prefs.putBoolean(SHOW_NOTIFICATION_PIXEL_WARN, false)
                    }
                    .setCancelable(true)
                    .show()
            } catch (ignored: Exception) {
            }
        }
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