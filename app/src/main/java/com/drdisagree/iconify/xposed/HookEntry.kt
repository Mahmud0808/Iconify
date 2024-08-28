package com.drdisagree.iconify.xposed

import android.annotation.SuppressLint
import android.app.Instrumentation
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.RemoteException
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.IRootProviderProxy
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Const.FRAMEWORK_PACKAGE
import com.drdisagree.iconify.xposed.utils.BootLoopProtector
import com.drdisagree.iconify.xposed.utils.SystemUtils
import com.drdisagree.iconify.xposed.utils.XPrefs
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge.hookAllMethods
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import de.robv.android.xposed.XposedHelpers.findClass
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.LinkedList
import java.util.Queue
import java.util.concurrent.CompletableFuture

class HookEntry : ServiceConnection {

    private var mContext: Context? = null

    init {
        instance = this
    }

    fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        isChildProcess = try {
            loadPackageParam.processName.contains(":")
        } catch (ignored: Throwable) {
            false
        }

        when (loadPackageParam.packageName) {
            FRAMEWORK_PACKAGE -> {
                val phoneWindowManagerClass = findClass(
                    "com.android.server.policy.PhoneWindowManager",
                    loadPackageParam.classLoader
                )

                hookAllMethods(phoneWindowManagerClass, "init", object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        try {
                            if (mContext == null) {
                                mContext = param.args[0] as Context

                                HookRes.modRes = mContext!!.createPackageContext(
                                    BuildConfig.APPLICATION_ID,
                                    Context.CONTEXT_IGNORE_SECURITY
                                ).resources

                                XPrefs.init(mContext!!)

                                CompletableFuture.runAsync { waitForXprefsLoad(loadPackageParam) }
                            }
                        } catch (throwable: Throwable) {
                            log(TAG + throwable)
                        }
                    }
                })
            }

            else -> {
                if (!isChildProcess) {
                    findAndHookMethod(
                        Instrumentation::class.java,
                        "newApplication",
                        ClassLoader::class.java,
                        String::class.java,
                        Context::class.java,
                        object : XC_MethodHook() {
                            override fun afterHookedMethod(param: MethodHookParam) {
                                try {
                                    if (mContext == null) {
                                        mContext = param.args[2] as Context

                                        HookRes.modRes = mContext!!.createPackageContext(
                                            BuildConfig.APPLICATION_ID,
                                            Context.CONTEXT_IGNORE_SECURITY
                                        ).resources

                                        XPrefs.init(mContext!!)

                                        waitForXprefsLoad(loadPackageParam)
                                    }
                                } catch (throwable: Throwable) {
                                    log(throwable)
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    private fun onXPrefsReady(loadPackageParam: LoadPackageParam) {
        if (!isChildProcess && BootLoopProtector.isBootLooped(loadPackageParam.packageName)) {
            log("Possible bootloop in ${loadPackageParam.packageName} ; Iconify will not load for now...")
            return
        }

        SystemUtils(mContext!!)

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
                val instance = mod.getConstructor(Context::class.java).newInstance(mContext)

                try {
                    instance.updatePrefs()
                } catch (ignored: Throwable) {
                }

                instance.handleLoadPackage(loadPackageParam)
                runningMods.add(instance)
            } catch (throwable: Throwable) {
                log("Start Error Dump - Occurred in ${mod.name}")
                log(TAG + throwable)
            }
        }
    }

    private fun waitForXprefsLoad(loadPackageParam: LoadPackageParam) {
        while (true) {
            try {
                Xprefs.getBoolean("LoadTestBooleanValue", false)
                break
            } catch (ignored: Throwable) {
                SystemUtils.sleep(1000);
            }
        }

        log("Iconify Version: ${BuildConfig.VERSION_NAME}")

        onXPrefsReady(loadPackageParam)
    }

    private fun forceConnectRootService() {
        CoroutineScope(Dispatchers.IO).launch {
            while (SystemUtils.UserManager == null || !SystemUtils.UserManager!!.isUserUnlocked) {
                // device is still CE encrypted
                delay(2000)
            }

            delay(5000) // wait for the unlocked account to settle down a bit

            while (rootProxyIPC == null) {
                connectRootService()
                delay(5000)
            }
        }
    }

    private fun connectRootService() {
        try {
            val intent = Intent().apply {
                setComponent(
                    ComponentName(
                        BuildConfig.APPLICATION_ID,
                        "${
                            BuildConfig.APPLICATION_ID.replace(
                                ".debug",
                                ""
                            )
                        }.services.RootProviderProxy"
                    )
                )
            }

            mContext!!.bindService(
                intent,
                instance!!,
                Context.BIND_AUTO_CREATE or Context.BIND_ADJUST_WITH_ACTIVITY
            )
        } catch (throwable: Throwable) {
            log(TAG + throwable)
        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        rootProxyIPC = IRootProviderProxy.Stub.asInterface(service)

        synchronized(proxyQueue) {
            while (!proxyQueue.isEmpty()) {
                try {
                    proxyQueue.poll()!!.run(rootProxyIPC)
                } catch (ignored: Throwable) {
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
        fun run(proxy: IRootProviderProxy?)
    }

    companion object {
        private val TAG = "Iconify - ${HookEntry::class.java.simpleName}: "

        @SuppressLint("StaticFieldLeak")
        var instance: HookEntry? = null
        val runningMods = ArrayList<ModPack>()
        var isChildProcess = false
        var rootProxyIPC: IRootProviderProxy? = null
        val proxyQueue: Queue<ProxyRunnable> = LinkedList()

        fun enqueueProxyCommand(runnable: ProxyRunnable) {
            rootProxyIPC?.let {
                try {
                    runnable.run(it)
                } catch (ignored: RemoteException) {
                }
            } ?: run {
                synchronized(proxyQueue) {
                    proxyQueue.add(runnable)
                }

                instance!!.forceConnectRootService()
            }
        }

        fun enableOverlay(packageName: String) {
            enqueueProxyCommand { proxy ->
                proxy?.enableOverlay(packageName)
            }
        }

        fun enableOverlays(vararg packageNames: String) {
            enqueueProxyCommand { proxy ->
                packageNames.forEach { packageName ->
                    proxy?.enableOverlay(packageName)
                }
            }
        }

        fun disableOverlay(packageName: String) {
            enqueueProxyCommand { proxy ->
                proxy?.disableOverlay(packageName)
            }
        }

        fun disableOverlays(vararg packageNames: String) {
            enqueueProxyCommand { proxy ->
                packageNames.forEach { packageName ->
                    proxy?.disableOverlay(packageName)
                }
            }
        }
    }
}
