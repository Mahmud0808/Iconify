package com.drdisagree.iconify.xposed

import android.app.Instrumentation
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.RemoteException
import android.os.SystemClock
import android.os.UserManager
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.R
import com.drdisagree.iconify.data.common.Const.FRAMEWORK_PACKAGE
import com.drdisagree.iconify.services.providers.IRootProviderProxy
import com.drdisagree.iconify.services.providers.RootProviderProxy
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.ResourceHookManager
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.log
import com.drdisagree.iconify.xposed.utils.BootLoopProtector
import com.drdisagree.iconify.xposed.utils.SystemUtils
import com.drdisagree.iconify.xposed.utils.XPrefs
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import com.drdisagree.iconify.xposed.utils.XPrefs.XprefsIsInitialized
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference
import java.lang.reflect.InvocationTargetException
import java.util.LinkedList
import java.util.Queue
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration.Companion.milliseconds

class HookEntry : ServiceConnection {

    private lateinit var mContext: Context

    init {
        instance = this
    }

    fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        isChildProcess = try {
            loadPackageParam.processName.contains(":")
        } catch (_: Throwable) {
            false
        }

        when (loadPackageParam.packageName) {
            FRAMEWORK_PACKAGE -> {
                val phoneWindowManagerClass =
                    findClass("com.android.server.policy.PhoneWindowManager")

                phoneWindowManagerClass
                    .hookMethod("init")
                    .runBefore { param ->
                        try {
                            if (!::mContext.isInitialized) {
                                mContext = param.args[0] as Context

                                HookRes.modRes = mContext.createPackageContext(
                                    BuildConfig.APPLICATION_ID,
                                    Context.CONTEXT_IGNORE_SECURITY
                                ).resources

                                XPrefs.init(mContext)
                                ResourceHookManager.init(mContext)

                                CompletableFuture.runAsync { waitForXprefsLoad(loadPackageParam) }
                            }
                        } catch (throwable: Throwable) {
                            log(this@HookEntry, throwable)
                        }
                    }
            }

            else -> {
                Instrumentation::class.java
                    .hookMethod("newApplication")
                    .runAfter { param ->
                        try {
                            if (!::mContext.isInitialized) {
                                mContext = param.args[param.args.size - 1] as Context

                                HookRes.modRes = mContext.createPackageContext(
                                    BuildConfig.APPLICATION_ID,
                                    Context.CONTEXT_IGNORE_SECURITY
                                ).resources

                                XPrefs.init(mContext)
                                ResourceHookManager.init(mContext)

                                waitForXprefsLoad(loadPackageParam)
                            }
                        } catch (throwable: Throwable) {
                            log(this@HookEntry, throwable)
                        }
                    }
            }
        }
    }

    private fun onXPrefsReady(loadPackageParam: LoadPackageParam) {
        if (BootLoopProtector.isBootLooped(loadPackageParam.packageName)) {
            log("Possible crash in ${loadPackageParam.packageName} ; Iconify will not load for now...")
            return
        }

        SystemUtils(mContext)

        loadModPacks(loadPackageParam)
    }

    private fun loadModPacks(loadPackageParam: LoadPackageParam) {
        if (HookRes.modRes
                .getStringArray(R.array.root_requirement)
                .toList()
                .contains(loadPackageParam.packageName)
        ) {
            forceConnectRootService()
        }

        for (mod in EntryList.getEntries(loadPackageParam.packageName)) {
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

                modInstance.handleLoadPackage(loadPackageParam)
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

    private fun waitForXprefsLoad(loadPackageParam: LoadPackageParam) {
        val deadline = SystemClock.uptimeMillis() + MAX_PREFS_WAIT_MS

        while (SystemClock.uptimeMillis() < deadline) {
            if (Xprefs.isProviderReady()) {
                onPrefsAvailable(loadPackageParam)
                return
            }
            SystemUtils.sleep(1000)
        }

        log(
            "Prefs provider not responding after ${MAX_PREFS_WAIT_MS / 1000}s; " +
                    "continuing ${loadPackageParam.packageName} startup without Iconify " +
                    "and waiting for the provider in background"
        )

        val loaded = AtomicBoolean(false)
        Xprefs.addRecoveryListener {
            if (loaded.compareAndSet(false, true)) {
                Handler(Looper.getMainLooper()).post { onPrefsAvailable(loadPackageParam) }
            }
        }

        if (Xprefs.isProviderReady() && loaded.compareAndSet(false, true)) {
            onPrefsAvailable(loadPackageParam)
        }
    }

    private fun onPrefsAvailable(loadPackageParam: LoadPackageParam) {
        log("Iconify Version: ${BuildConfig.VERSION_NAME}")
        log("Hooked ${loadPackageParam.packageName}")

        onXPrefsReady(loadPackageParam)
    }

    private fun forceConnectRootService() {
        CoroutineScope(Dispatchers.Main).launch {
            val mUserManager = mContext.getSystemService(Context.USER_SERVICE) as UserManager?

            withContext(Dispatchers.IO) {
                while (mUserManager == null || !mUserManager.isUserUnlocked) {
                    // device is still CE encrypted
                    delay(2000.milliseconds)
                }

                delay(5000.milliseconds) // wait for the unlocked account to settle down a bit

                while (rootProxyIPC == null) {
                    connectRootService()
                    delay(5000.milliseconds)
                }
            }
        }
    }

    private fun connectRootService() {
        try {
            val intent = Intent().apply {
                component = ComponentName(
                    BuildConfig.APPLICATION_ID,
                    RootProviderProxy::class.qualifiedName!!
                )
            }

            mContext.bindService(
                intent,
                instance!!,
                Context.BIND_AUTO_CREATE or Context.BIND_ADJUST_WITH_ACTIVITY
            )
        } catch (throwable: Throwable) {
            log(this@HookEntry, throwable)
        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        rootProxyIPC = IRootProviderProxy.Stub.asInterface(service)

        synchronized(proxyQueue) {
            while (!proxyQueue.isEmpty()) {
                try {
                    proxyQueue.poll()!!.run(rootProxyIPC!!)
                } catch (_: Throwable) {
                }
            }
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        rootProxyIPC = null
        forceConnectRootService()
    }

    fun interface ProxyRunnable {
        @Throws(RemoteException::class)
        fun run(proxy: IRootProviderProxy)
    }

    companion object {
        private var _instance: WeakReference<HookEntry>? = null
        private var instance: HookEntry?
            get() = _instance?.get()
            set(value) {
                _instance = value?.let { WeakReference(it) }
            }

        private const val MAX_PREFS_WAIT_MS = 30_000L

        val runningMods = ArrayList<ModPack>()
        var isChildProcess = false

        private var rootProxyIPC: IRootProviderProxy? = null
        private val proxyQueue: Queue<ProxyRunnable> = LinkedList()

        fun enqueueProxyCommand(runnable: ProxyRunnable) {
            rootProxyIPC?.let {
                try {
                    runnable.run(it)
                } catch (_: RemoteException) {
                }
            } ?: run {
                synchronized(proxyQueue) {
                    proxyQueue.add(runnable)
                }

                instance!!.forceConnectRootService()
            }
        }
    }
}
