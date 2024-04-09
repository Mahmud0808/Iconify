package com.drdisagree.iconify.xposed

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
        hookEntry.handleLoadPackage(loadPackageParam)
    }

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        hookRes.initZygote(startupParam)
    }
}
