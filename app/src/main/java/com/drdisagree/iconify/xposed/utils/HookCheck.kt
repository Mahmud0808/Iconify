package com.drdisagree.iconify.xposed.utils

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import com.drdisagree.iconify.common.Const.ACTION_HOOK_CHECK_REQUEST
import com.drdisagree.iconify.common.Const.ACTION_HOOK_CHECK_RESULT
import com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.xposed.ModPack
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

class HookCheck(context: Context?) : ModPack(context!!) {

    private var intentFilter = IntentFilter()
    private var broadcastRegistered = false

    override fun updatePrefs(vararg key: String) {}

    private fun returnBroadcastResult() {
        Thread {
            mContext.sendBroadcast(
                Intent()
                    .setAction(ACTION_HOOK_CHECK_RESULT)
                    .addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
            )
        }.start()
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        if (!broadcastRegistered && loadPackageParam.packageName == SYSTEMUI_PACKAGE) {
            broadcastRegistered = true

            intentFilter.addAction(ACTION_HOOK_CHECK_REQUEST)

            val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    if (intent.action == ACTION_HOOK_CHECK_REQUEST &&
                        loadPackageParam.packageName == SYSTEMUI_PACKAGE
                    ) {
                        returnBroadcastResult()
                    }
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                mContext.registerReceiver(
                    broadcastReceiver,
                    intentFilter,
                    Context.RECEIVER_EXPORTED
                )
            } else {
                mContext.registerReceiver(broadcastReceiver, intentFilter)
            }
        }
    }
}
