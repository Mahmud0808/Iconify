package com.drdisagree.iconify.services;

import static com.drdisagree.iconify.common.Preferences.ON_HOME_PAGE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.drdisagree.iconify.config.Prefs;

import java.util.Objects;

@Deprecated
public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Objects.equals(intent.getAction(), Intent.ACTION_BOOT_COMPLETED) && Prefs.getBoolean(ON_HOME_PAGE)) {
            Log.i(getClass().getSimpleName(), "Starting Background Service...");
            context.startService(new Intent(context, BackgroundService.class));
        }
    }
}
