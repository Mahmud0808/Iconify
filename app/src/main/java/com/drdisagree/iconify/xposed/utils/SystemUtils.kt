package com.drdisagree.iconify.xposed.utils

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Process
import com.drdisagree.iconify.xposed.RootProxyClient.enqueueProxyCommand
import com.drdisagree.iconify.xposed.utils.BootLoopProtector.resetCounter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class SystemUtils(var mContext: Context) {

    init {
        instance = this
    }

    private val isDark: Boolean
        get() = mContext.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_YES ==
                Configuration.UI_MODE_NIGHT_YES

    companion object {
        @SuppressLint("StaticFieldLeak")
        var instance: SystemUtils? = null

        private var darkSwitching = false

        val isDarkMode: Boolean get() = instance?.isDark ?: false

        fun sleep(millis: Int) {
            try {
                Thread.sleep(millis.toLong())
            } catch (_: Throwable) {
            }
        }

        fun doubleToggleDarkMode() {
            val isDark = isDarkMode

            CoroutineScope(Dispatchers.Default).launch {
                try {
                    while (darkSwitching) {
                        delay(100.milliseconds)
                    }

                    darkSwitching = true

                    enqueueProxyCommand { proxy ->
                        proxy.runCommand("cmd uimode night ${if (isDark) "no" else "yes"}")
                    }
                    delay(1000.milliseconds)
                    enqueueProxyCommand { proxy ->
                        proxy.runCommand("cmd uimode night ${if (isDark) "yes" else "no"}")
                    }
                    delay(500.milliseconds)

                    darkSwitching = false
                } catch (_: Exception) {
                }
            }
        }

        fun sleepDevice() {
            enqueueProxyCommand { proxy ->
                proxy.runCommand("input keyevent 223")
            }
        }

        fun <Method> killSelf() {
            resetCounter(Process.myProcessName())
            Process.killProcess(Process.myPid())
        }
    }
}
