package com.drdisagree.iconify.xposed

import android.app.Instrumentation
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.R
import com.drdisagree.iconify.data.common.Const.FRAMEWORK_PACKAGE
import com.drdisagree.iconify.xposed.core.PackageParam
import com.drdisagree.iconify.xposed.core.XposedRuntime
import com.drdisagree.iconify.xposed.core.hook.hookMethod
import com.drdisagree.iconify.xposed.core.log.log
import com.drdisagree.iconify.xposed.core.reflect.findClass
import com.drdisagree.iconify.xposed.utils.BootLoopProtector
import com.drdisagree.iconify.xposed.utils.SystemUtils
import com.drdisagree.iconify.xposed.utils.XPrefs
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import com.drdisagree.iconify.xposed.utils.XPrefs.XprefsIsInitialized
import java.lang.reflect.InvocationTargetException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean

class HookEntry {

    private lateinit var mContext: Context

    fun handleLoadPackage(packageParam: PackageParam) {
        isChildProcess = packageParam.processName.contains(":")

        when (packageParam.packageName) {
            FRAMEWORK_PACKAGE -> {
                val phoneWindowManagerClass =
                    findClass("com.android.server.policy.PhoneWindowManager")

                phoneWindowManagerClass
                    .hookMethod("init")
                    .runBefore { chain ->
                        try {
                            if (!::mContext.isInitialized) {
                                mContext = chain.args[0] as Context

                                attachContext()

                                CompletableFuture.runAsync { waitForXprefsLoad(packageParam) }
                            }
                        } catch (throwable: Throwable) {
                            log(this@HookEntry, throwable)
                        }
                    }
            }

            else -> {
                Instrumentation::class.java
                    .hookMethod("newApplication")
                    .runAfter { chain, _ ->
                        try {
                            if (!::mContext.isInitialized) {
                                mContext = chain.args[chain.args.size - 1] as Context

                                attachContext()

                                waitForXprefsLoad(packageParam)
                            }
                        } catch (throwable: Throwable) {
                            log(this@HookEntry, throwable)
                        }
                    }
            }
        }
    }

    private fun attachContext() {
        HookRes.modRes = mContext.createPackageContext(
            BuildConfig.APPLICATION_ID,
            Context.CONTEXT_IGNORE_SECURITY
        ).resources

        XposedRuntime.attachContext(mContext)
        RootProxyClient.init(mContext)
        XPrefs.init(mContext)
    }

    private fun onXPrefsReady(packageParam: PackageParam) {
        if (BootLoopProtector.isBootLooped(packageParam.packageName)) {
            log("Possible crash in ${packageParam.packageName} ; Iconify will not load for now...")
            return
        }

        SystemUtils(mContext)

        loadModPacks(packageParam)
    }

    private fun loadModPacks(packageParam: PackageParam) {
        if (HookRes.modRes
                .getStringArray(R.array.root_requirement)
                .toList()
                .contains(packageParam.packageName)
        ) {
            RootProxyClient.forceConnect()
        }

        for (mod in EntryList.getEntries(packageParam.packageName)) {
            try {
                val modInstance = mod.getConstructor(Context::class.java).newInstance(mContext)

                if (XprefsIsInitialized) {
                    try {
                        modInstance.updatePrefs()
                    } catch (throwable: Throwable) {
                        log(this@HookEntry, "Failed to update prefs in ${mod.name}")
                        log(this@HookEntry, throwable)
                    }
                }

                modInstance.handleLoadPackage(packageParam)
                runningMods.add(modInstance)
            } catch (invocationTargetException: InvocationTargetException) {
                log(this@HookEntry, "Start Error Dump - Occurred in ${mod.name}")
                log(this@HookEntry, invocationTargetException.cause)
            } catch (throwable: Throwable) {
                log(this@HookEntry, "Start Error Dump - Occurred in ${mod.name}")
                log(this@HookEntry, throwable)
            }
        }
    }

    private fun waitForXprefsLoad(packageParam: PackageParam) {
        val deadline = SystemClock.uptimeMillis() + MAX_PREFS_WAIT_MS

        while (SystemClock.uptimeMillis() < deadline) {
            if (Xprefs.isProviderReady()) {
                onPrefsAvailable(packageParam)
                return
            }
            SystemUtils.sleep(1000)
        }

        log(
            "Prefs provider not responding after ${MAX_PREFS_WAIT_MS / 1000}s; " +
                    "continuing ${packageParam.packageName} startup without Iconify " +
                    "and waiting for the provider in background"
        )

        val loaded = AtomicBoolean(false)
        Xprefs.addRecoveryListener {
            if (loaded.compareAndSet(false, true)) {
                Handler(Looper.getMainLooper()).post { onPrefsAvailable(packageParam) }
            }
        }

        if (Xprefs.isProviderReady() && loaded.compareAndSet(false, true)) {
            onPrefsAvailable(packageParam)
        }
    }

    private fun onPrefsAvailable(packageParam: PackageParam) {
        log("Iconify Version: ${BuildConfig.VERSION_NAME}")
        log("Hooked ${packageParam.packageName}")

        onXPrefsReady(packageParam)
    }

    companion object {
        private const val MAX_PREFS_WAIT_MS = 30_000L

        val runningMods = ArrayList<ModPack>()
        var isChildProcess = false
    }
}
