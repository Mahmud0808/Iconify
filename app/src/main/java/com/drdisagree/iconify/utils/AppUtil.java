package com.drdisagree.iconify.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.topjohnwu.superuser.Shell;

public class AppUtil {

    public static boolean isAppInstalled(String packageName) {
        PackageManager pm = Iconify.Companion.getAppContext().getPackageManager();
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return pm.getApplicationInfo(packageName, 0).enabled;
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        return false;
    }

    public static boolean isAppInstalledRoot(String packageName) {
        return Shell.cmd("res=$(pm path " + packageName + "); if [ ! -z \"$res\" ]; then echo \"installed\"; else echo \"not found\"; fi").exec().getOut().get(0).contains("installed");
    }

    public static int getAppUid(String packageName) {
        PackageManager pm = Iconify.Companion.getAppContext().getPackageManager();
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return pm.getApplicationInfo(packageName, 0).uid;
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        return 0;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public static Drawable getAppIcon(String packageName) {
        Drawable appIcon = ContextCompat.getDrawable(Iconify.Companion.getAppContext(), R.drawable.ic_android);
        try {
            appIcon = Iconify.Companion.getAppContext().getPackageManager().getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        return appIcon;
    }

    public static String getAppName(String packageName) {
        final PackageManager pm = Iconify.Companion.getAppContext().getApplicationContext().getPackageManager();
        ApplicationInfo ai = null;
        try {
            ai = pm.getApplicationInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        return (String) (ai == null ? "Unavailable" : pm.getApplicationLabel(ai));
    }

    public static void launchApp(Activity activity, String packageName) {
        Intent launchIntent = Iconify.Companion.getAppContext().getPackageManager().getLaunchIntentForPackage(packageName);
        if (launchIntent != null) {
            activity.startActivity(launchIntent);
        } else {
            Toast.makeText(Iconify.Companion.getAppContext(), Iconify.Companion.getAppContext().getResources().getString(R.string.app_not_found), Toast.LENGTH_SHORT).show();
        }
    }

    public static String[] getSplitLocations(String packageName) {
        try {
            String[] splitLocations = Iconify.Companion.getAppContext().getPackageManager().getApplicationInfo(packageName, 0).splitSourceDirs;
            if (splitLocations == null) {
                splitLocations = new String[]{Iconify.Companion.getAppContext().getPackageManager().getApplicationInfo(packageName, 0).sourceDir};
            }
            return splitLocations;
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        return new String[0];
    }

    public static boolean isLsposedInstalled() {
        return RootUtil.fileExists("/data/adb/lspd/manager.apk") || RootUtil.fileExists("/data/adb/modules/*lsposed*/manager.apk");
    }

    public static void restartApplication(Activity activity) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = activity.getIntent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.finish();
            activity.startActivity(intent);
        }, 600);
    }
}
