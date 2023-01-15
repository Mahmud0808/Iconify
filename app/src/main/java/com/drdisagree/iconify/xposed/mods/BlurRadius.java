package com.drdisagree.iconify.xposed.mods;

import static com.drdisagree.iconify.common.References.QSBLUR_RADIUS;
import static com.drdisagree.iconify.common.References.QSBLUR_SWITCH;
import static com.drdisagree.iconify.common.References.SYSTEM_UI_PACKAGE;
import static com.drdisagree.iconify.config.XPrefs.Xprefs;
import static com.drdisagree.iconify.xposed.HookRes.resparams;
import static de.robv.android.xposed.XposedBridge.log;

import android.content.Context;
import android.content.res.XResources;
import android.util.TypedValue;

import com.drdisagree.iconify.xposed.ModPack;

import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class BlurRadius extends ModPack {

    private static final String TAG = "Iconify - BlurRadius";
    private String rootPackagePath = "";
    int BlurRadius = 23;

    public BlurRadius(Context context) {
        super(context);
        if (!listensTo(context.getPackageName())) return;
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;
        BlurRadius = Xprefs.getInt(QSBLUR_RADIUS, 23);
        setResources();
    }

    @Override
    public boolean listensTo(String packageName) {
        return packageName.equals(SYSTEM_UI_PACKAGE);
    }

    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpParam) {
        if (!lpParam.packageName.equals(SYSTEM_UI_PACKAGE))
            return;

        rootPackagePath = lpParam.appInfo.sourceDir;
    }

    private void setResources() {
        XC_InitPackageResources.InitPackageResourcesParam ourResparam = resparams.get(SYSTEM_UI_PACKAGE);
        if (ourResparam == null) return;

        try {
            ourResparam.res.setReplacement(SYSTEM_UI_PACKAGE, "dimen", "max_window_blur_radius", new XResources.DimensionReplacement(BlurRadius, TypedValue.COMPLEX_UNIT_PX));
        } catch (Exception e) {
            log("Failed to replace dimen: " + e);
        }
        log("Blur Radius: " + BlurRadius + "px\nBlur isActive: " + Xprefs.getBoolean(QSBLUR_SWITCH, false));
    }
}
