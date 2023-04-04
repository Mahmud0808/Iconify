package com.drdisagree.iconify.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.topjohnwu.superuser.Shell;

public class AppUtil {
    public static boolean isAppInstalled(String packageName) {
        PackageManager pm = Iconify.getAppContext().getPackageManager();
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

    @SuppressLint("UseCompatLoadingForDrawables")
    public static Drawable getAppIcon(String packageName) {
        Drawable appIcon = Iconify.getAppContext().getResources().getDrawable(R.drawable.ic_android);
        try {
            appIcon = Iconify.getAppContext().getPackageManager().getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        return appIcon;
    }

    public static String getAppName(String packageName) {
        final PackageManager pm = Iconify.getAppContext().getApplicationContext().getPackageManager();
        ApplicationInfo ai = null;
        try {
            ai = pm.getApplicationInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        return (String) (ai == null ? "Unavailable" : pm.getApplicationLabel(ai));
    }

    public static void launchApp(Activity activity, String packageName) {
        Intent launchIntent = Iconify.getAppContext().getPackageManager().getLaunchIntentForPackage(packageName);
        if (launchIntent != null) {
            activity.startActivity(launchIntent);
        } else {
            Toast.makeText(Iconify.getAppContext(), Iconify.getAppContext().getResources().getString(R.string.app_not_found), Toast.LENGTH_SHORT).show();
        }
    }

    public static String[] getSplitLocations(String packageName) {
        try {
            return new String[]{Iconify.getAppContext().getPackageManager().getApplicationInfo(packageName, 0).sourceDir};
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        return new String[0];
    }

    public static boolean isLsposedInstalled() {
        return RootUtil.fileExists("/data/adb/lspd/manager.apk");
    }
}
