package com.drdisagree.iconify.xposed

import android.app.Instrumentation
import android.content.Context
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.common.Const.FRAMEWORK_PACKAGE
import com.drdisagree.iconify.config.XPrefs
import com.drdisagree.iconify.config.XPrefs.Xprefs
import com.drdisagree.iconify.xposed.utils.BootLoopProtector
import com.drdisagree.iconify.xposed.utils.SystemUtil
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge.hookAllMethods
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import de.robv.android.xposed.XposedHelpers.findClass
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import java.util.concurrent.CompletableFuture

class HookEntry : IXposedHookLoadPackage {

    companion object {
        var isChildProcess = false

        @JvmField
        val runningMods = ArrayList<ModPack>()
    }

    private var mContext: Context? = null
    private val tag = "Iconify - ${this::class.java.simpleName}: "

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
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
                            log(tag + throwable)
                        }
                    }
                })
            }

            else -> {
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

    private fun onXPrefsReady(loadPackageParam: LoadPackageParam) {
        if (BootLoopProtector.isBootLooped(loadPackageParam.packageName)) {
            log("Possible bootloop in ${loadPackageParam.packageName} ; Iconify will not load for now...")
            return
        }

        SystemUtil(mContext!!)

        loadModPacks(loadPackageParam)
    }

    private fun loadModPacks(loadPackageParam: LoadPackageParam) {
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
                log(tag + throwable)
            }
        }
    }

    private fun waitForXprefsLoad(loadPackageParam: LoadPackageParam) {
        while (true) {
            try {
                Xprefs?.getBoolean("LoadTestBooleanValue", false)
                break
            } catch (ignored: Throwable) {
                try {
                    Thread.sleep(1000)
                } catch (ignored1: Throwable) {
                }
            }
        }

        log("Iconify Version: ${BuildConfig.VERSION_NAME}")

        onXPrefsReady(loadPackageParam)
    }
}
