package com.drdisagree.iconify.ui.preferences

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.widget.ImageView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.drdisagree.iconify.Iconify.Companion.appContextLocale
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Const.ACTION_HOOK_CHECK_REQUEST
import com.drdisagree.iconify.common.Const.ACTION_HOOK_CHECK_RESULT
import com.drdisagree.iconify.common.Preferences
import com.drdisagree.iconify.config.RPrefs
import com.drdisagree.iconify.utils.extension.ObservableVariable
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class HookCheckPreference(context: Context, attrs: AttributeSet?) : Preference(context, attrs) {

    private val handler = Handler(Looper.getMainLooper())
    private val delayedHandler = Handler(Looper.getMainLooper())
    private val intentFilterHookedSystemUI = IntentFilter()
    private var isHookSuccessful = false
    var isHooked: Boolean = false

    init {
        intentFilterHookedSystemUI.addAction(ACTION_HOOK_CHECK_RESULT)

        isXposedHooked.setOnChangeListener { newValue ->
            newValue?.let {
                try {
                    delayedHandler.removeCallbacks(delayedHookCheck)
                } catch (ignored: Exception) {
                }

                isHooked = it

                if (RPrefs.getBoolean("hookCheckPreference") != it) {
                    RPrefs.putBoolean("xposedHookCheck", it)
                }
            }
        }
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        holder.itemView.setOnClickListener {
            try {
                context.startActivity(
                    Intent(Intent.ACTION_MAIN).apply {
                        setComponent(
                            ComponentName(
                                "org.lsposed.manager",
                                "org.lsposed.manager.ui.activity.MainActivity"
                            )
                        )
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                )
            } catch (ignored: Exception) {
            }
        }

        holder.itemView.findViewById<ImageView>(R.id.info_icon).setOnClickListener {
            MaterialAlertDialogBuilder(context)
                .setTitle(context.resources.getString(R.string.attention))
                .setMessage(
                    buildString {
                        append(
                            (if (Preferences.isXposedOnlyMode) {
                                appContextLocale.resources.getString(
                                    R.string.xposed_only_desc
                                ) + "\n\n"
                            } else {
                                ""
                            })
                        )
                        append(appContextLocale.resources.getString(R.string.lsposed_warn))
                    }
                )
                .setPositiveButton(context.resources.getString(R.string.understood)) { dialog: DialogInterface, _: Int ->
                    dialog.dismiss()
                }
                .setCancelable(true)
                .show()
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    fun initializeHookCheck() {
        delayedHandler.postDelayed(delayedHookCheck, 1000)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(
                receiverHookedSystemUI,
                intentFilterHookedSystemUI,
                Context.RECEIVER_EXPORTED
            )
        } else {
            context.registerReceiver(
                receiverHookedSystemUI,
                intentFilterHookedSystemUI
            )
        }

        handler.post(checkSystemUIHooked)
    }

    private val delayedHookCheck: Runnable = Runnable {
        if (!isHooked) {
            if (RPrefs.getBoolean("xposedHookCheck")) {
                RPrefs.putBoolean("xposedHookCheck", false)
            }
        }
    }

    private val checkSystemUIHooked: Runnable = object : Runnable {
        override fun run() {
            checkXposedHooked()
//            handler.postDelayed(this, 1000) // repeat check every 1 second
        }
    }

    private val receiverHookedSystemUI: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_HOOK_CHECK_RESULT) {
                isHookSuccessful = true
                isXposedHooked.setValue(true)
            }
        }
    }

    private fun checkXposedHooked() {
        isHookSuccessful = false

        object : CountDownTimer(1600, 800) {
            override fun onTick(millisUntilFinished: Long) {
                if (isHookSuccessful) {
                    cancel()
                }
            }

            override fun onFinish() {
                if (!isHookSuccessful) {
                    isXposedHooked.setValue(false)
                }
            }
        }.start()

        Thread {
            try {
                context.sendBroadcast(Intent().setAction(ACTION_HOOK_CHECK_REQUEST))
            } catch (ignored: Exception) {
            }
        }.start()
    }

    override fun onDetached() {
        super.onDetached()
        try {
            handler.removeCallbacks(checkSystemUIHooked)
            delayedHandler.removeCallbacks(delayedHookCheck)
            context.unregisterReceiver(receiverHookedSystemUI)
        } catch (ignored: Exception) {
        }
    }

    companion object {
        val isXposedHooked = ObservableVariable<Boolean>()
    }
}
