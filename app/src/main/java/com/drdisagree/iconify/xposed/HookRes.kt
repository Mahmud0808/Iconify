package com.drdisagree.iconify.xposed

import android.content.res.Resources
import de.robv.android.xposed.IXposedHookInitPackageResources
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.callbacks.XC_InitPackageResources

class HookRes : IXposedHookInitPackageResources, IXposedHookZygoteInit {

    private lateinit var modulePath: String

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        modulePath = startupParam.modulePath
    }

    override fun handleInitPackageResources(resparam: XC_InitPackageResources.InitPackageResourcesParam) {
        resParams[resparam.packageName] = resparam
    }

    companion object {
        var modRes: Resources? = null
        val resParams = HashMap<String, XC_InitPackageResources.InitPackageResourcesParam>()
    }
}