package com.drdisagree.iconify.xposed.mods;

import static android.content.Context.RECEIVER_EXPORTED;
import static com.drdisagree.iconify.common.Const.ACTION_HOOK_CHECK_REQUEST;
import static com.drdisagree.iconify.common.Const.ACTION_HOOK_CHECK_RESULT;
import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import com.drdisagree.iconify.xposed.ModPack;

import java.util.Objects;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookCheck extends ModPack {

    private static final String TAG = "Iconify - " + HookCheck.class.getSimpleName() + ": ";
    IntentFilter intentFilter = new IntentFilter();
    boolean broadcastRegistered = false;

    public HookCheck(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
    }

    private void returnBroadcastResult() {
        new Thread(() -> mContext.sendBroadcast(new Intent()
                .setAction(ACTION_HOOK_CHECK_RESULT)
                .addFlags(Intent.FLAG_RECEIVER_FOREGROUND))).start();
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!broadcastRegistered && lpparam.packageName.equals(SYSTEMUI_PACKAGE)) {
            broadcastRegistered = true;
            intentFilter.addAction(ACTION_HOOK_CHECK_REQUEST);

            BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (Objects.equals(intent.getAction(), ACTION_HOOK_CHECK_REQUEST) && lpparam.packageName.equals(SYSTEMUI_PACKAGE)) {
                        returnBroadcastResult();
                    }
                }
            };

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                mContext.registerReceiver(broadcastReceiver, intentFilter, RECEIVER_EXPORTED);
            } else {
                mContext.registerReceiver(broadcastReceiver, intentFilter);
            }
        }
    }
}
