package com.drdisagree.iconify.xposed

import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.ComposeToolkit
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook
import de.robv.android.xposed.IXposedHookInitPackageResources
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.callbacks.XC_InitPackageResources
import de.robv.android.xposed.callbacks.XC_LoadPackage

class InitHook : IXposedHookZygoteInit, IXposedHookInitPackageResources, IXposedHookLoadPackage {

    private val hookRes = HookRes()
    private val hookEntry = HookEntry()

    override fun handleInitPackageResources(initPackageResourcesParam: XC_InitPackageResources.InitPackageResourcesParam) {
        hookRes.handleInitPackageResources(initPackageResourcesParam)
    }

    override fun handleLoadPackage(loadPackageParam: XC_LoadPackage.LoadPackageParam) {
        XposedHook.init(loadPackageParam)
        if (loadPackageParam.packageName == SYSTEMUI_PACKAGE) {
            ComposeToolkit.init(loadPackageParam.classLoader)
        }
        hookEntry.handleLoadPackage(loadPackageParam)
    }

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        hookRes.initZygote(startupParam)
    }
}
