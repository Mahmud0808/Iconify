package com.drdisagree.iconify.xposed.utils

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.os.UserManager
import com.drdisagree.iconify.xposed.utils.BootLoopProtector.resetCounter
import com.topjohnwu.superuser.Shell
import de.robv.android.xposed.XposedBridge.log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.annotations.Contract
import java.util.concurrent.CountDownLatch
import javax.annotation.Nullable


class SystemUtils(var mContext: Context) {

    init {
        instance = this
    }

    private val isDark: Boolean
        get() = mContext.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_YES ==
                Configuration.UI_MODE_NIGHT_YES

    private fun getUserManager(): UserManager? {
        if (mUserManager == null) {
            val latch = CountDownLatch(1)

            Handler(Looper.getMainLooper()).post {
                try {
                    mUserManager = mContext.getSystemService(Context.USER_SERVICE) as UserManager
                } catch (throwable: Throwable) {
                    log(TAG + throwable)
                } finally {
                    latch.countDown()
                }
            }

            try {
                latch.await()
            } catch (e: InterruptedException) {
                log(TAG + e.message)
            }
        }

        return mUserManager
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        var instance: SystemUtils? = null

        private var darkSwitching = false
        private var mUserManager: UserManager? = null

        val isDarkMode: Boolean get() = instance?.isDark ?: false

        @Nullable
        @get:Contract(pure = true)
        val UserManager: UserManager? get() = instance?.getUserManager()

        fun sleep(millis: Int) {
            try {
                Thread.sleep(millis.toLong())
            } catch (ignored: Throwable) {
            }
        }

        fun doubleToggleDarkMode() {
            val isDark = isDarkMode

            CoroutineScope(Dispatchers.Default).launch {
                try {
                    while (darkSwitching) {
                        delay(100)
                    }

                    darkSwitching = true

                    Shell.cmd("cmd uimode night ${if (isDark) "no" else "yes"}").exec()
                    delay(1000)
                    Shell.cmd("cmd uimode night ${if (isDark) "yes" else "no"}").exec()
                    delay(500)

                    darkSwitching = false
                } catch (ignored: Exception) {
                }
            }
        }

        fun <Method> killSelf() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                resetCounter(Process.myProcessName())
            } else {
                resetCounter(Application.getProcessName())
            }
            Process.killProcess(Process.myPid())
        }

        private val TAG = "Iconify - ${this::class.java.simpleName}: "
    }
}
