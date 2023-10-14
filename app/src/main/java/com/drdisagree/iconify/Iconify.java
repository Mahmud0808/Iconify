package com.drdisagree.iconify;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.drdisagree.iconify.services.IRootServiceProvider;
import com.drdisagree.iconify.services.RootServiceProvider;
import com.google.android.material.color.DynamicColors;
import com.topjohnwu.superuser.ipc.RootService;

import java.lang.ref.WeakReference;

public class Iconify extends Application {

    private static WeakReference<Context> contextReference;
    public static IRootServiceProvider mRootServiceProvider;

    public static Context getAppContext() {
        return contextReference.get();
    }

    public void onCreate() {
        super.onCreate();
        contextReference = new WeakReference<>(getApplicationContext());
        startRootService();
        DynamicColors.applyToActivitiesIfAvailable(this);
    }

    private void startRootService() {
        Log.i("Iconify", "Starting RootService...");

        Intent intent = new Intent(this, RootServiceProvider.class);
        ServiceConnection mCoreRootServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mRootServiceProvider = IRootServiceProvider.Stub.asInterface(service);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mRootServiceProvider = null;
            }
        };
        RootService.bind(intent, mCoreRootServiceConnection);
    }
}