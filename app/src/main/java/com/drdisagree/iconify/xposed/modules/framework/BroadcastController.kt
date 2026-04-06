package com.drdisagree.iconify.xposed.modules.framework

import android.content.Context
import android.content.Intent
import com.drdisagree.iconify.data.common.Const.BROADCAST_ACTIONS
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.hookMethod
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam

class BroadcastController(context: Context) : ModPack(context) {

    override fun onPreferenceUpdated(vararg key: String) {}

    override fun onPackageLoaded(packageReadyParam: PackageReadyParam) {
        val broadcastControllerClass = findClass(
            "com.android.server.am.BroadcastController",
            "com.android.server.am.ActivityManagerService"
        )

        broadcastControllerClass
            .hookMethod("checkBroadcastFromSystem")
            .runBefore { param ->
                val intent = param.args.firstOrNull { it is Intent } as? Intent
                    ?: return@runBefore
                val action = intent.action

                if (action in BROADCAST_ACTIONS) {
                    param.result = null
                }
            }
    }
}