package com.drdisagree.iconify.xposed.utils

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.data.common.Const.ACTION_HOOK_CHECK_REQUEST
import com.drdisagree.iconify.data.common.Const.ACTION_HOOK_CHECK_RESULT
import com.drdisagree.iconify.data.common.Const.FRAMEWORK_PACKAGE
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.xposed.HookEntry
import com.drdisagree.iconify.xposed.ModPack
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam

class HookCheck(context: Context) : ModPack(context) {

    private var intentFilter = IntentFilter()
    private var broadcastRegistered = false

    override fun onPreferenceUpdated(vararg key: String) {}

    private fun returnBroadcastResult() {
        Thread {
            mContext.sendBroadcast(
                Intent()
                    .putExtra(
                        "packageName", if (HookEntry.isSystemServer) FRAMEWORK_PACKAGE
                        else SYSTEMUI_PACKAGE
                    )
                    .setPackage(BuildConfig.APPLICATION_ID)
                    .setAction(ACTION_HOOK_CHECK_RESULT)
                    .addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
            )
        }.start()
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onPackageLoaded(packageReadyParam: PackageReadyParam) {
        if (!broadcastRegistered) {
            broadcastRegistered = true

            intentFilter.addAction(ACTION_HOOK_CHECK_REQUEST)

            val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    if (intent.action == ACTION_HOOK_CHECK_REQUEST) {
                        returnBroadcastResult()
                    }
                }
            }

            mContext.registerReceiver(
                broadcastReceiver,
                intentFilter,
                Context.RECEIVER_EXPORTED
            )
        }
    }
}
