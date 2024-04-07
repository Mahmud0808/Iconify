package com.drdisagree.iconify.xposed.modules

import android.content.Context
import android.os.UserHandle
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.xposed.ModPack
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge.hookAllConstructors
import de.robv.android.xposed.XposedBridge.hookAllMethods
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.findClass
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

class IconUpdater(context: Context?) : ModPack(context!!) {

    private var launcherModel: Any? = null

    override fun updatePrefs(vararg key: String) {}

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        try {
            val launcherModelClass = findClass(
                "com.android.launcher3.LauncherModel",
                loadPackageParam.classLoader
            )
            val baseDraggingActivityClass = findClass(
                "com.android.launcher3.BaseDraggingActivity",
                loadPackageParam.classLoader
            )

            hookAllConstructors(launcherModelClass, object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    launcherModel = param.thisObject
                }
            })

            hookAllMethods(baseDraggingActivityClass, "onResume", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    try {
                        if (launcherModel != null) {
                            callMethod(
                                launcherModel,
                                "onAppIconChanged",
                                BuildConfig.APPLICATION_ID,
                                UserHandle.getUserHandleForUid(0)
                            )
                        }
                    } catch (throwable: Throwable) {
                        log(TAG + throwable)
                    }
                }
            })
        } catch (ignored: Throwable) {
        }
    }

    companion object {
        private val TAG = "Iconify - ${IconUpdater::class.java.simpleName}: "
    }
}