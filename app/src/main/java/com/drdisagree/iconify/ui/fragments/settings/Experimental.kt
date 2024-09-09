package com.drdisagree.iconify.ui.fragments.settings

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Const.SWITCH_ANIMATION_DELAY
import com.drdisagree.iconify.common.Preferences.HEADER_IMAGE_OVERLAP
import com.drdisagree.iconify.common.Preferences.HIDE_DATA_DISABLED_ICON
import com.drdisagree.iconify.common.Preferences.OP_QS_HEADER_GAP_EXPANDED
import com.drdisagree.iconify.config.RPrefs.getBoolean
import com.drdisagree.iconify.config.RPrefs.getInt
import com.drdisagree.iconify.config.RPrefs.putBoolean
import com.drdisagree.iconify.config.RPrefs.putInt
import com.drdisagree.iconify.databinding.FragmentExperimentalBinding
import com.drdisagree.iconify.ui.activities.MainActivity
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.dialogs.EditTextDialog
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader
import com.drdisagree.iconify.utils.SystemUtils
import com.google.android.material.slider.Slider
import kotlin.random.Random

class Experimental : BaseFragment() {

    private lateinit var binding: FragmentExperimentalBinding
    private lateinit var notificationManager: NotificationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        notificationManager =
            requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    @SuppressLint("InlinedApi")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentExperimentalBinding.inflate(inflater, container, false)
        val root: View = binding.getRoot()

        // Header
        setHeader(
            requireContext(),
            getParentFragmentManager(),
            binding.header.toolbar,
            R.string.activity_title_experimental
        )

        // Header image overlap
        binding.headerImageOverlap.isSwitchChecked = getBoolean(HEADER_IMAGE_OVERLAP, false)
        binding.headerImageOverlap.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            putBoolean(HEADER_IMAGE_OVERLAP, isChecked)

            Handler(Looper.getMainLooper()).postDelayed(
                { SystemUtils.restartSystemUI() },
                SWITCH_ANIMATION_DELAY
            )
        }

        // Hide data disabled icon
        binding.hideDataDisabledIcon.isSwitchChecked = getBoolean(HIDE_DATA_DISABLED_ICON, false)
        binding.hideDataDisabledIcon.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            putBoolean(HIDE_DATA_DISABLED_ICON, isChecked)
        }

        // OP QS Header Gap Expanded
        binding.opQsGapExpanded.apply {
            sliderValue = getInt(OP_QS_HEADER_GAP_EXPANDED, 0)
            setResetClickListener {
                putInt(OP_QS_HEADER_GAP_EXPANDED, 0)

                MainActivity.showOrHidePendingActionButton(
                    activityBinding = (requireActivity() as MainActivity).binding,
                    requiresSystemUiRestart = true
                )

                true
            }
            setOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: Slider) {}

                override fun onStopTrackingTouch(slider: Slider) {
                    putInt(OP_QS_HEADER_GAP_EXPANDED, slider.value.toInt())

                    MainActivity.showOrHidePendingActionButton(
                        activityBinding = (requireActivity() as MainActivity).binding,
                        requiresSystemUiRestart = true
                    )
                }
            })
        }

        // Send notification
        binding.sendNotification.setOnClickListener {
            val permissionGranted = hasNotificationPermission()

            if (!permissionGranted) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                return@setOnClickListener
            }

            showCustomNotificationDialog()
        }

        return root
    }

    private fun showCustomNotificationDialog() {
        EditTextDialog(requireContext(), View.generateViewId()).apply {
            setDialogListener(object : EditTextDialog.EditTextDialogListener {
                override fun onOkPressed(dialogId: Int, newText: String) {
                    if (newText.isNotEmpty()) {
                        sendCustomNotification(requireContext(), newText, notificationManager)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Please enter a message",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            })

            show("Notification Body", "Enter notification message", "Enter message", "")
        }
    }

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { result: Boolean ->
        if (result) {
            showCustomNotificationDialog()
        } else {
            Toast.makeText(
                requireContext(),
                "Permission not granted",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun createChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            "TEST_NOTIFICATION_CHANNEL",
            "Test Notification",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = "This channel is for testing purposes"

        notificationManager.createNotificationChannel(channel)
    }

    private fun sendCustomNotification(
        context: Context,
        message: String,
        notificationManager: NotificationManager
    ) {
        createChannel(notificationManager)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "TEST_NOTIFICATION_CHANNEL")
            .setSmallIcon(R.drawable.ic_launcher_fg)
            .setContentTitle(getString(R.string.derived_app_name))
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(Random.nextInt(), notification)
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}