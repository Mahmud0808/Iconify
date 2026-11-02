package com.drdisagree.iconify.xposed

import com.drdisagree.iconify.data.common.Const.FRAMEWORK_PACKAGE
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.xposed.core.PackageParam
import com.drdisagree.iconify.xposed.core.XposedRuntime
import com.drdisagree.iconify.xposed.core.compose.ComposeToolkit
import com.drdisagree.iconify.xposed.core.log.log
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam
import io.github.libxposed.api.XposedModuleInterface.SystemServerStartingParam

class IconifyModule : XposedModule() {

    private var hookEntry: HookEntry? = null

    override fun onModuleLoaded(param: ModuleLoadedParam) {
        XposedRuntime.attach(this, param.processName)
    }

    override fun onSystemServerStarting(param: SystemServerStartingParam) {
        dispatch(
            PackageParam(
                packageName = FRAMEWORK_PACKAGE,
                processName = XposedRuntime.processName,
                classLoader = param.classLoader,
                appInfo = null,
                isFirstPackage = true
            )
        )
    }

    override fun onPackageReady(param: PackageReadyParam) {
        if (!param.isFirstPackage || param.packageName != SYSTEMUI_PACKAGE) return

        dispatch(
            PackageParam(
                packageName = param.packageName,
                processName = XposedRuntime.processName,
                classLoader = param.classLoader,
                appInfo = param.applicationInfo,
                isFirstPackage = true
            )
        )
    }

    private fun dispatch(packageParam: PackageParam) {
        if (hookEntry != null) return

        try {
            XposedRuntime.onPackageReady(packageParam)

            if (packageParam.packageName == SYSTEMUI_PACKAGE) {
                ComposeToolkit.init(packageParam.classLoader)
            }

            hookEntry = HookEntry().also { it.handleLoadPackage(packageParam) }
        } catch (throwable: Throwable) {
            log(this, throwable)
        }
    }
}
