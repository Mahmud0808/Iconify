package com.drdisagree.iconify.xposed

import android.app.Instrumentation
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.Resources
import android.os.IBinder
import android.os.RemoteException
import android.os.UserManager
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.R
import com.drdisagree.iconify.services.providers.IRootProviderProxy
import com.drdisagree.iconify.services.providers.RootProviderProxy
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.ResourceHookManager
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.hookMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.log
import com.drdisagree.iconify.xposed.utils.BootLoopProtector
import com.drdisagree.iconify.xposed.utils.SystemUtils
import com.drdisagree.iconify.xposed.utils.XPrefs
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import com.drdisagree.iconify.xposed.utils.XPrefs.XprefsIsInitialized
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam
import io.github.libxposed.api.XposedModuleInterface.SystemServerStartingParam
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

class HookEntry : XposedModule(), ServiceConnection {

    private lateinit var mContext: Context

    init {
        instance = this
    }

    override fun onModuleLoaded(moduleLoadedParam: ModuleLoadedParam) {
        processName = moduleLoadedParam.processName
        isSystemServer = moduleLoadedParam.isSystemServer
    }

    override fun onSystemServerStarting(systemServerStartingParam: SystemServerStartingParam) {
        XposedHook.setFrameworkClassLoader(systemServerStartingParam.classLoader)
    }

    override fun onPackageReady(packageReadyParam: PackageReadyParam) {
        XposedHook.setXposedInterface(this)

        when {
            isSystemServer -> {
                val phoneWindowManagerClass =
                    findClass("com.android.server.policy.PhoneWindowManager")

                phoneWindowManagerClass
                    .hookMethod("init")
                    .runBefore { param ->
                        try {
                            if (!::mContext.isInitialized) {
                                mContext = param.args[0] as Context

                                moduleResources = mContext.createPackageContext(
                                    BuildConfig.APPLICATION_ID,
                                    Context.CONTEXT_IGNORE_SECURITY
                                ).resources

                                XPrefs.init(mContext)
                                ResourceHookManager.init(mContext)

                                CompletableFuture.runAsync { waitForXprefsLoad(packageReadyParam) }
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
                        isChildProcess = !packageReadyParam.isFirstPackage

                        try {
                            if (!::mContext.isInitialized) {
                                mContext = param.args[param.args.size - 1] as Context

                                moduleResources = mContext.createPackageContext(
                                    BuildConfig.APPLICATION_ID,
                                    Context.CONTEXT_IGNORE_SECURITY
                                ).resources

                                XPrefs.init(mContext)
                                ResourceHookManager.init(mContext)

                                waitForXprefsLoad(packageReadyParam)
                            }
                        } catch (throwable: Throwable) {
                            log(this@HookEntry, throwable)
                        }
                    }
            }
        }
    }

    private fun waitForXprefsLoad(packageReadyParam: PackageReadyParam) {
        while (true) {
            try {
                Xprefs.getBoolean("LoadTestBooleanValue", false)
                break
            } catch (_: Throwable) {
                SystemUtils.sleep(1000);
            }
        }

        log("Iconify Version: ${BuildConfig.VERSION_NAME}")
        log("Hooked ${packageReadyParam.packageName}")

        onXPrefsReady(packageReadyParam)
    }

    private fun onXPrefsReady(packageReadyParam: PackageReadyParam) {
        if (BootLoopProtector.isBootLooped(packageReadyParam.packageName)) {
            log("Possible crash in ${packageReadyParam.packageName} ; Iconify will not load for now...")
            return
        }

        SystemUtils(mContext)
        loadModPacks(packageReadyParam)
        XPrefs.onContentProviderLoaded()
    }

    private fun loadModPacks(packageReadyParam: PackageReadyParam) {
        XposedHook.setDefaultClassLoader(packageReadyParam.classLoader)

        if (moduleResources
                .getStringArray(R.array.root_requirement)
                .toList()
                .contains(packageReadyParam.packageName)
        ) {
            forceConnectRootService()
        }

        EntryList.getEntries(packageReadyParam.packageName).forEach { mod ->
            try {
                val modInstance = mod.getConstructor(Context::class.java).newInstance(mContext)

                if (XprefsIsInitialized) {
                    try {
                        modInstance.onPreferenceUpdated()
                    } catch (throwable: Throwable) {
                        log(this@HookEntry, "Failed to update prefs in ${mod.name}")
                        log(this@HookEntry, throwable)
                    }
                }

                modInstance.onPackageLoaded(packageReadyParam)
                runningMods.add(modInstance)
            } catch (invocationTargetException: InvocationTargetException) {
                log(
                    this@HookEntry,
                    "Start Error Dump - Occurred in ${mod.name}",
                    invocationTargetException.cause
                )
            } catch (throwable: Throwable) {
                log(
                    this@HookEntry,
                    "Start Error Dump - Occurred in ${mod.name}",
                    throwable
                )
            }
        }
    }

    private fun forceConnectRootService() {
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                val mUserManager = withContext(Dispatchers.Main) {
                    mContext.getSystemService(Context.USER_SERVICE) as? UserManager
                }

                if (mUserManager != null && mUserManager.isUserUnlocked) break
                // device is still CE encrypted
                delay(2_000)
            }

            // wait for the unlocked account to settle down a bit
            delay(5_000)

            while (rootProxyIPC == null) {
                connectRootService()
                delay(5_000)
            }
        }
    }

    private fun connectRootService() {
        try {
            val intent = Intent().apply {
                component = ComponentName(
                    BuildConfig.APPLICATION_ID,
                    RootProviderProxy.Companion::class.java.name
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

        private lateinit var _instance: WeakReference<HookEntry>
        private var instance: HookEntry?
            get() = _instance.get()
            set(value) {
                _instance = WeakReference(value)
            }

        val runningMods = ArrayList<ModPack>()
        var isChildProcess = false

        var processName: String = ""
        var isSystemServer: Boolean = false

        lateinit var moduleResources: Resources

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
