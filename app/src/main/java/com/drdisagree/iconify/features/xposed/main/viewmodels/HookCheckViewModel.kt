package com.drdisagree.iconify.features.xposed.main.viewmodels

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel
import com.drdisagree.iconify.R
import com.drdisagree.iconify.data.common.Const.ACTION_HOOK_CHECK_REQUEST
import com.drdisagree.iconify.data.common.Const.ACTION_HOOK_CHECK_RESULT
import com.drdisagree.iconify.data.common.Preferences.XPOSED_HOOK_CHECK
import com.drdisagree.iconify.data.config.RPrefs
import com.drdisagree.iconify.features.xposed.main.states.HookCheckUiState
import com.drdisagree.iconify.xposed.utils.BootLoopProtector.PACKAGE_STRIKE_KEY_KEY
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class HookCheckViewModel @Inject constructor(
    @param:ApplicationContext val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(HookCheckUiState())
    val uiState: StateFlow<HookCheckUiState> = _uiState.asStateFlow()

    private val handler = Handler(Looper.getMainLooper())
    private val delayedHandler = Handler(Looper.getMainLooper())
    private var isHookSuccessful = false

    private val hookPackages: Array<String> =
        context.resources.getStringArray(R.array.module_scope)

    private val intentFilterHookedSystemUI = IntentFilter().apply {
        addAction(ACTION_HOOK_CHECK_RESULT)
    }

    private val delayedHookCheck = Runnable {
        if (!_uiState.value.isHooked) {
            RPrefs.putBoolean(XPOSED_HOOK_CHECK, false)

            var bootlooped = false
            for (packageName in hookPackages) {
                val strikeKey = "$PACKAGE_STRIKE_KEY_KEY$packageName"
                if (RPrefs.getInt(strikeKey, 0) >= 3) {
                    bootlooped = true
                    break
                }
            }

            _uiState.update {
                it.copy(
                    isHooked = false,
                    hasBootlooped = bootlooped,
                    hookCheckCompleted = true
                )
            }
        }
    }

    private val checkSystemUIHooked = Runnable {
        checkXposedHooked()
    }

    private val receiverHookedSystemUI = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_HOOK_CHECK_RESULT) {
                isHookSuccessful = true

                try {
                    delayedHandler.removeCallbacks(delayedHookCheck)
                } catch (_: Exception) {
                }

                RPrefs.putBoolean(XPOSED_HOOK_CHECK, true)
                _uiState.update { it.copy(isHooked = true, hookCheckCompleted = true) }
            }
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    fun initializeHookCheck() {
        val lastKnownHooked = RPrefs.getBoolean(XPOSED_HOOK_CHECK, false)
        _uiState.update {
            it.copy(
                isHooked = lastKnownHooked,
                hookCheckCompleted = false
            )
        }

        delayedHandler.postDelayed(delayedHookCheck, 1000)

        context.registerReceiver(
            receiverHookedSystemUI,
            intentFilterHookedSystemUI,
            Context.RECEIVER_EXPORTED
        )

        handler.post(checkSystemUIHooked)
    }

    private fun checkXposedHooked() {
        isHookSuccessful = false

        object : CountDownTimer(1600, 800) {
            override fun onTick(millisUntilFinished: Long) {
                if (isHookSuccessful) cancel()
            }

            override fun onFinish() {
                if (!isHookSuccessful) {
                    try {
                        delayedHandler.removeCallbacks(delayedHookCheck)
                    } catch (_: Exception) {
                    }

                    RPrefs.putBoolean(XPOSED_HOOK_CHECK, false)
                    _uiState.update { it.copy(isHooked = false, hookCheckCompleted = true) }
                }
            }
        }.start()

        Thread {
            try {
                context.sendBroadcast(Intent().setAction(ACTION_HOOK_CHECK_REQUEST))
            } catch (_: Exception) {
            }
        }.start()
    }

    override fun onCleared() {
        super.onCleared()
        try {
            handler.removeCallbacks(checkSystemUIHooked)
            delayedHandler.removeCallbacks(delayedHookCheck)
            context.unregisterReceiver(receiverHookedSystemUI)
        } catch (_: Exception) {
        }
    }
}