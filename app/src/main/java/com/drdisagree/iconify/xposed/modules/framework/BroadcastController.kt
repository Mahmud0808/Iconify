package com.drdisagree.iconify.xposed.modules.framework

import android.content.Context
import android.content.Intent
import com.drdisagree.iconify.data.common.Const.BROADCAST_ACTIONS
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

class BroadcastController(context: Context) : ModPack(context) {

    override fun updatePrefs(vararg key: String) {}

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
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