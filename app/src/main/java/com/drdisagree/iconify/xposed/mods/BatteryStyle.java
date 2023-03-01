package com.drdisagree.iconify.xposed.mods;

import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.LANDSCAPE_BATTERY_HEIGHT;
import static com.drdisagree.iconify.common.Preferences.LANDSCAPE_BATTERY_ROTATION;
import static com.drdisagree.iconify.common.Preferences.LANDSCAPE_BATTERY_SWITCH;
import static com.drdisagree.iconify.common.Preferences.LANDSCAPE_BATTERY_WIDTH;
import static com.drdisagree.iconify.config.XPrefs.Xprefs;
import static com.drdisagree.iconify.xposed.HookRes.resparams;
import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

import android.content.Context;
import android.content.res.XResources;
import android.util.TypedValue;
import android.widget.ImageView;

import com.drdisagree.iconify.xposed.ModPack;

import java.util.Objects;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class BatteryStyle extends ModPack implements IXposedHookLoadPackage {

    private static final String TAG = "Iconify - BatteryStyle: ";
    private static final String BatteryMeterViewClass = SYSTEMUI_PACKAGE + ".battery.BatteryMeterView";
    boolean enableLandscapeBattery = false;
    int landscapeBatteryRotation = 90;
    int landscapeBatteryWidth = 20;
    int landscapeBatteryHeight = 20;
    ImageView mBatteryIconView = null;

    public BatteryStyle(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;

        enableLandscapeBattery = Xprefs.getBoolean(LANDSCAPE_BATTERY_SWITCH, false);
        landscapeBatteryRotation = Xprefs.getInt(LANDSCAPE_BATTERY_ROTATION, 90);
        landscapeBatteryWidth = Xprefs.getInt(LANDSCAPE_BATTERY_WIDTH, 20);
        landscapeBatteryHeight = Xprefs.getInt(LANDSCAPE_BATTERY_HEIGHT, 20);

        if (Key.length > 0) {
            if (Objects.equals(Key[0], LANDSCAPE_BATTERY_SWITCH) || Objects.equals(Key[0], LANDSCAPE_BATTERY_ROTATION) || Objects.equals(Key[0], LANDSCAPE_BATTERY_WIDTH) || Objects.equals(Key[0], LANDSCAPE_BATTERY_HEIGHT))
                setLandscapeBatterySize();
        }
    }

    @Override
    public boolean listensTo(String packageName) {
        return packageName.equals(SYSTEMUI_PACKAGE);
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!lpparam.packageName.equals(SYSTEMUI_PACKAGE)) return;

        final Class<?> BatteryMeterView = findClass(BatteryMeterViewClass, lpparam.classLoader);

        try {
            hookAllConstructors(BatteryMeterView, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (!enableLandscapeBattery) return;
                    mBatteryIconView = (ImageView) getObjectField(param.thisObject, "mBatteryIconView");
                    mBatteryIconView.setRotation(landscapeBatteryRotation);
                }
            });
        } catch (Throwable ignored) {
        }
        setLandscapeBatterySize();
    }

    private void setLandscapeBatterySize() {
        XC_InitPackageResources.InitPackageResourcesParam ourResparam = resparams.get(SYSTEMUI_PACKAGE);
        if (ourResparam == null) return;

        if (enableLandscapeBattery) {
            ourResparam.res.setReplacement(SYSTEMUI_PACKAGE, "dimen", "status_bar_battery_icon_width", new XResources.DimensionReplacement(landscapeBatteryWidth, TypedValue.COMPLEX_UNIT_DIP));
            ourResparam.res.setReplacement(SYSTEMUI_PACKAGE, "dimen", "status_bar_battery_icon_height", new XResources.DimensionReplacement(landscapeBatteryHeight, TypedValue.COMPLEX_UNIT_DIP));
        }
    }
}
