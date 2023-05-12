package com.drdisagree.iconify.xposed.mods;

/* Modified from AOSPMods
 * https://github.com/siavash79/AOSPMods/blob/canary/app/src/main/java/sh/siava/AOSPMods/systemui/BatteryStyleManager.java
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see [http://www.gnu.org/licenses/].
 */

import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_CUSTOM_LANDSCAPE;
import static com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_CUSTOM_RLANDSCAPE;
import static com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_DEFAULT;
import static com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_DEFAULT_LANDSCAPE;
import static com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_DEFAULT_RLANDSCAPE;
import static com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_LANDSCAPE_BUDDY;
import static com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_LANDSCAPE_LINE;
import static com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_LANDSCAPE_MUSKU;
import static com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_LANDSCAPE_PILL;
import static com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_LANDSCAPE_SIGNAL;
import static com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_LANDSCAPE_STYLE_A;
import static com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_LANDSCAPE_STYLE_B;
import static com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_RLANDSCAPE_STYLE_A;
import static com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_RLANDSCAPE_STYLE_B;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_HEIGHT;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_STYLE;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_WIDTH;
import static com.drdisagree.iconify.config.XPrefs.Xprefs;
import static com.drdisagree.iconify.xposed.HookRes.resparams;
import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findClassIfExists;
import static de.robv.android.xposed.XposedHelpers.getAdditionalInstanceField;
import static de.robv.android.xposed.XposedHelpers.getBooleanField;
import static de.robv.android.xposed.XposedHelpers.getIntField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setAdditionalInstanceField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.XResources;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.drdisagree.iconify.xposed.HookEntry;
import com.drdisagree.iconify.xposed.ModPack;
import com.drdisagree.iconify.xposed.mods.batterystyles.LandscapeBatteryDrawable;
import com.drdisagree.iconify.xposed.mods.batterystyles.LandscapeBatteryDrawableBuddy;
import com.drdisagree.iconify.xposed.mods.batterystyles.LandscapeBatteryDrawableLine;
import com.drdisagree.iconify.xposed.mods.batterystyles.LandscapeBatteryDrawableMusku;
import com.drdisagree.iconify.xposed.mods.batterystyles.LandscapeBatteryDrawablePill;
import com.drdisagree.iconify.xposed.mods.batterystyles.LandscapeBatteryDrawableSignal;
import com.drdisagree.iconify.xposed.mods.batterystyles.LandscapeBatteryDrawableStyleA;
import com.drdisagree.iconify.xposed.mods.batterystyles.LandscapeBatteryDrawableStyleB;
import com.drdisagree.iconify.xposed.mods.batterystyles.RLandscapeBatteryDrawable;
import com.drdisagree.iconify.xposed.mods.batterystyles.RLandscapeBatteryDrawableStyleA;
import com.drdisagree.iconify.xposed.mods.batterystyles.RLandscapeBatteryDrawableStyleB;
import com.drdisagree.iconify.xposed.utils.SettingsLibUtils;

import java.util.ArrayList;
import java.util.Objects;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class BatteryStyleManager extends ModPack {

    private static final String TAG = "Iconify - BatteryStyleManager: ";
    public static final String listenPackage = SYSTEMUI_PACKAGE;
    private static boolean customBatteryEnabled = false;
    private int frameColor;
    public static int BatteryStyle = 0;
    public static boolean ShowPercent = false;
    public static int scaleFactor = 100;
    public static int batteryRotation = 0;
    private static int BatteryIconOpacity = 100;
    private Object BatteryController;
    private int landscapeBatteryWidth = 20;
    private int landscapeBatteryHeight = 20;
    private static final ArrayList<Object> batteryViews = new ArrayList<>();

    public BatteryStyleManager(Context context) {
        super(context);
    }

    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;

        int batteryStyle = Xprefs.getInt(CUSTOM_BATTERY_STYLE, 0);
        customBatteryEnabled = batteryStyle != BATTERY_STYLE_DEFAULT;
        landscapeBatteryWidth = Xprefs.getInt(CUSTOM_BATTERY_WIDTH, 20);
        landscapeBatteryHeight = Xprefs.getInt(CUSTOM_BATTERY_HEIGHT, 20);

        if (batteryStyle == 1) {
            batteryRotation = 90;
        } else if (batteryStyle == 2) {
            batteryRotation = 270;
        } else {
            batteryRotation = 0;
        }

        if (BatteryStyle != batteryStyle) {
            BatteryStyle = batteryStyle;
            for (Object view : batteryViews) {
                ImageView mBatteryIconView = (ImageView) getObjectField(view, "mBatteryIconView");
                boolean mCharging = (boolean) getObjectField(view, "mCharging");
                int mLevel = (int) getObjectField(view, "mLevel");

                if (customBatteryEnabled) {
                    LandscapeBatteryDrawableMusku newDrawable = new LandscapeBatteryDrawableMusku(mContext, frameColor);
                    mBatteryIconView.setImageDrawable(newDrawable);
                    setAdditionalInstanceField(view, "mBatteryDrawable", newDrawable);
                    newDrawable.setBatteryLevel(mLevel);
                    newDrawable.setCharging(mCharging);
                } else {
                    try {
                        mBatteryIconView.setImageDrawable((Drawable) getObjectField(view, "mDrawable"));
                    } catch (Throwable ignored) { //PE+ !
                        mBatteryIconView.setImageDrawable((Drawable) getObjectField(view, "mThemedDrawable"));
                    }
                }
            }
        }

        refreshBatteryIcons();

        if (Key.length > 0) {
            if (Objects.equals(Key[0], CUSTOM_BATTERY_WIDTH) || Objects.equals(Key[0], CUSTOM_BATTERY_HEIGHT))
                setCustomBatterySize();
        }
    }

    private static void refreshBatteryIcons() {
        for (Object view : batteryViews) {
            ImageView mBatteryIconView = (ImageView) getObjectField(view, "mBatteryIconView");
            mBatteryIconView.setRotation(batteryRotation);
            scale(mBatteryIconView);
            try {
                switch (BatteryStyle) {
                    case BATTERY_STYLE_DEFAULT:
                        mBatteryIconView.setRotation(0);
                        break;
                    case BATTERY_STYLE_DEFAULT_RLANDSCAPE:
                        mBatteryIconView.setRotation(90);
                        break;
                    case BATTERY_STYLE_DEFAULT_LANDSCAPE:
                        mBatteryIconView.setRotation(270);
                        break;
                    case BATTERY_STYLE_CUSTOM_RLANDSCAPE:
                        RLandscapeBatteryDrawable drawable3 = (RLandscapeBatteryDrawable) getAdditionalInstanceField(view, "mBatteryDrawable");
                        drawable3.setShowPercent(ShowPercent);
                        drawable3.setAlpha(Math.round(BatteryIconOpacity * 2.55f));
                        drawable3.invalidateSelf();
                        break;
                    case BATTERY_STYLE_CUSTOM_LANDSCAPE:
                        LandscapeBatteryDrawable drawable4 = (LandscapeBatteryDrawable) getAdditionalInstanceField(view, "mBatteryDrawable");
                        drawable4.setShowPercent(ShowPercent);
                        drawable4.setAlpha(Math.round(BatteryIconOpacity * 2.55f));
                        drawable4.invalidateSelf();
                        break;
                    case BATTERY_STYLE_LANDSCAPE_BUDDY:
                        LandscapeBatteryDrawableBuddy drawable5 = (LandscapeBatteryDrawableBuddy) getAdditionalInstanceField(view, "mBatteryDrawable");
                        drawable5.setShowPercent(ShowPercent);
                        drawable5.setAlpha(Math.round(BatteryIconOpacity * 2.55f));
                        drawable5.invalidateSelf();
                        break;
                    case BATTERY_STYLE_LANDSCAPE_LINE:
                        LandscapeBatteryDrawableLine drawable6 = (LandscapeBatteryDrawableLine) getAdditionalInstanceField(view, "mBatteryDrawable");
                        drawable6.setShowPercent(ShowPercent);
                        drawable6.setAlpha(Math.round(BatteryIconOpacity * 2.55f));
                        drawable6.invalidateSelf();
                        break;
                    case BATTERY_STYLE_LANDSCAPE_MUSKU:
                        LandscapeBatteryDrawableMusku drawable7 = (LandscapeBatteryDrawableMusku) getAdditionalInstanceField(view, "mBatteryDrawable");
                        drawable7.setShowPercent(ShowPercent);
                        drawable7.setAlpha(Math.round(BatteryIconOpacity * 2.55f));
                        drawable7.invalidateSelf();
                        break;
                    case BATTERY_STYLE_LANDSCAPE_PILL:
                        LandscapeBatteryDrawablePill drawable8 = (LandscapeBatteryDrawablePill) getAdditionalInstanceField(view, "mBatteryDrawable");
                        drawable8.setShowPercent(ShowPercent);
                        drawable8.setAlpha(Math.round(BatteryIconOpacity * 2.55f));
                        drawable8.invalidateSelf();
                        break;
                    case BATTERY_STYLE_LANDSCAPE_SIGNAL:
                        LandscapeBatteryDrawableSignal drawable9 = (LandscapeBatteryDrawableSignal) getAdditionalInstanceField(view, "mBatteryDrawable");
                        drawable9.setShowPercent(ShowPercent);
                        drawable9.setAlpha(Math.round(BatteryIconOpacity * 2.55f));
                        drawable9.invalidateSelf();
                        break;
                    case BATTERY_STYLE_RLANDSCAPE_STYLE_A:
                        RLandscapeBatteryDrawableStyleA drawable10 = (RLandscapeBatteryDrawableStyleA) getAdditionalInstanceField(view, "mBatteryDrawable");
                        drawable10.setShowPercent(ShowPercent);
                        drawable10.setAlpha(Math.round(BatteryIconOpacity * 2.55f));
                        drawable10.invalidateSelf();
                        break;
                    case BATTERY_STYLE_LANDSCAPE_STYLE_A:
                        LandscapeBatteryDrawableStyleA drawable11 = (LandscapeBatteryDrawableStyleA) getAdditionalInstanceField(view, "mBatteryDrawable");
                        drawable11.setShowPercent(ShowPercent);
                        drawable11.setAlpha(Math.round(BatteryIconOpacity * 2.55f));
                        drawable11.invalidateSelf();
                        break;
                    case BATTERY_STYLE_RLANDSCAPE_STYLE_B:
                        RLandscapeBatteryDrawableStyleB drawable12 = (RLandscapeBatteryDrawableStyleB) getAdditionalInstanceField(view, "mBatteryDrawable");
                        drawable12.setShowPercent(ShowPercent);
                        drawable12.setAlpha(Math.round(BatteryIconOpacity * 2.55f));
                        drawable12.invalidateSelf();
                        break;
                    case BATTERY_STYLE_LANDSCAPE_STYLE_B:
                        LandscapeBatteryDrawableStyleB drawable13 = (LandscapeBatteryDrawableStyleB) getAdditionalInstanceField(view, "mBatteryDrawable");
                        drawable13.setShowPercent(ShowPercent);
                        drawable13.setAlpha(Math.round(BatteryIconOpacity * 2.55f));
                        drawable13.invalidateSelf();
                        break;
                }
            } catch (Throwable ignored) {
            }
        }
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!lpparam.packageName.equals(listenPackage)) return;

        Class<?> BatteryControllerImplClass = findClass("com.android.systemui.statusbar.policy.BatteryControllerImpl", lpparam.classLoader);
        Class<?> BatteryMeterViewClass = findClassIfExists("com.android.systemui.battery.BatteryMeterView", lpparam.classLoader);
        SettingsLibUtils.init(lpparam.classLoader);

        findAndHookConstructor("com.android.settingslib.graph.ThemedBatteryDrawable", lpparam.classLoader, Context.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                frameColor = (int) param.args[1];
            }
        });

        hookAllConstructors(BatteryControllerImplClass, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                BatteryController = param.thisObject;
            }
        });

        hookAllMethods(BatteryControllerImplClass, "fireBatteryUnknownStateChanged", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (!customBatteryEnabled) return;

                for (Object view : batteryViews) {
                    try {
                        switch (BatteryStyle) {
                            case BATTERY_STYLE_DEFAULT:
                            case BATTERY_STYLE_DEFAULT_RLANDSCAPE:
                            case BATTERY_STYLE_DEFAULT_LANDSCAPE:
                                break;
                            case BATTERY_STYLE_CUSTOM_RLANDSCAPE:
                                RLandscapeBatteryDrawable drawable3 = (RLandscapeBatteryDrawable) getAdditionalInstanceField(view, "mBatteryDrawable");
                                callMethod(view, "setImageDrawable", drawable3);
                                break;
                            case BATTERY_STYLE_CUSTOM_LANDSCAPE:
                                LandscapeBatteryDrawable drawable4 = (LandscapeBatteryDrawable) getAdditionalInstanceField(view, "mBatteryDrawable");
                                callMethod(view, "setImageDrawable", drawable4);
                                break;
                            case BATTERY_STYLE_LANDSCAPE_BUDDY:
                                LandscapeBatteryDrawableBuddy drawable5 = (LandscapeBatteryDrawableBuddy) getAdditionalInstanceField(view, "mBatteryDrawable");
                                callMethod(view, "setImageDrawable", drawable5);
                                break;
                            case BATTERY_STYLE_LANDSCAPE_LINE:
                                LandscapeBatteryDrawableLine drawable6 = (LandscapeBatteryDrawableLine) getAdditionalInstanceField(view, "mBatteryDrawable");
                                callMethod(view, "setImageDrawable", drawable6);
                                break;
                            case BATTERY_STYLE_LANDSCAPE_MUSKU:
                                LandscapeBatteryDrawableMusku drawable7 = (LandscapeBatteryDrawableMusku) getAdditionalInstanceField(view, "mBatteryDrawable");
                                callMethod(view, "setImageDrawable", drawable7);
                                break;
                            case BATTERY_STYLE_LANDSCAPE_PILL:
                                LandscapeBatteryDrawablePill drawable8 = (LandscapeBatteryDrawablePill) getAdditionalInstanceField(view, "mBatteryDrawable");
                                callMethod(view, "setImageDrawable", drawable8);
                                break;
                            case BATTERY_STYLE_LANDSCAPE_SIGNAL:
                                LandscapeBatteryDrawableSignal drawable9 = (LandscapeBatteryDrawableSignal) getAdditionalInstanceField(view, "mBatteryDrawable");
                                callMethod(view, "setImageDrawable", drawable9);
                                break;
                            case BATTERY_STYLE_RLANDSCAPE_STYLE_A:
                                RLandscapeBatteryDrawableStyleA drawable10 = (RLandscapeBatteryDrawableStyleA) getAdditionalInstanceField(view, "mBatteryDrawable");
                                callMethod(view, "setImageDrawable", drawable10);
                                break;
                            case BATTERY_STYLE_LANDSCAPE_STYLE_A:
                                LandscapeBatteryDrawableStyleA drawable11 = (LandscapeBatteryDrawableStyleA) getAdditionalInstanceField(view, "mBatteryDrawable");
                                callMethod(view, "setImageDrawable", drawable11);
                                break;
                            case BATTERY_STYLE_RLANDSCAPE_STYLE_B:
                                RLandscapeBatteryDrawableStyleB drawable12 = (RLandscapeBatteryDrawableStyleB) getAdditionalInstanceField(view, "mBatteryDrawable");
                                callMethod(view, "setImageDrawable", drawable12);
                                break;
                            case BATTERY_STYLE_LANDSCAPE_STYLE_B:
                                LandscapeBatteryDrawableStyleB drawable13 = (LandscapeBatteryDrawableStyleB) getAdditionalInstanceField(view, "mBatteryDrawable");
                                callMethod(view, "setImageDrawable", drawable13);
                                break;
                        }
                    } catch (Throwable ignored) {
                    }
                }
            }
        });

        XC_MethodHook batteryDataRefreshHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                int level = getIntField(param.thisObject, "mLevel");
                boolean charging = getBooleanField(param.thisObject, "mPluggedIn") || getBooleanField(param.thisObject, "mCharging") || getBooleanField(param.thisObject, "mWirelessCharging");
                boolean powerSave = getBooleanField(param.thisObject, "mPowerSave");

                if (!customBatteryEnabled) return;

                for (Object view : batteryViews) {
                    ((View) view).post(() -> {
                        try {
                            switch (BatteryStyle) {
                                case BATTERY_STYLE_DEFAULT:
                                case BATTERY_STYLE_DEFAULT_RLANDSCAPE:
                                case BATTERY_STYLE_DEFAULT_LANDSCAPE:
                                    break;
                                case BATTERY_STYLE_CUSTOM_RLANDSCAPE:
                                    RLandscapeBatteryDrawable drawable3 = (RLandscapeBatteryDrawable) getAdditionalInstanceField(view, "mBatteryDrawable");
                                    drawable3.setBatteryLevel(level);
                                    drawable3.setCharging(charging);
                                    drawable3.setPowerSaveEnabled(powerSave);
                                    break;
                                case BATTERY_STYLE_CUSTOM_LANDSCAPE:
                                    LandscapeBatteryDrawable drawable4 = (LandscapeBatteryDrawable) getAdditionalInstanceField(view, "mBatteryDrawable");
                                    drawable4.setBatteryLevel(level);
                                    drawable4.setCharging(charging);
                                    drawable4.setPowerSaveEnabled(powerSave);
                                    break;
                                case BATTERY_STYLE_LANDSCAPE_BUDDY:
                                    LandscapeBatteryDrawableBuddy drawable5 = (LandscapeBatteryDrawableBuddy) getAdditionalInstanceField(view, "mBatteryDrawable");
                                    drawable5.setBatteryLevel(level);
                                    drawable5.setCharging(charging);
                                    drawable5.setPowerSaveEnabled(powerSave);
                                    break;
                                case BATTERY_STYLE_LANDSCAPE_LINE:
                                    LandscapeBatteryDrawableLine drawable6 = (LandscapeBatteryDrawableLine) getAdditionalInstanceField(view, "mBatteryDrawable");
                                    drawable6.setBatteryLevel(level);
                                    drawable6.setCharging(charging);
                                    drawable6.setPowerSaveEnabled(powerSave);
                                    break;
                                case BATTERY_STYLE_LANDSCAPE_MUSKU:
                                    LandscapeBatteryDrawableMusku drawable7 = (LandscapeBatteryDrawableMusku) getAdditionalInstanceField(view, "mBatteryDrawable");
                                    drawable7.setBatteryLevel(level);
                                    drawable7.setCharging(charging);
                                    drawable7.setPowerSaveEnabled(powerSave);
                                    break;
                                case BATTERY_STYLE_LANDSCAPE_PILL:
                                    LandscapeBatteryDrawablePill drawable8 = (LandscapeBatteryDrawablePill) getAdditionalInstanceField(view, "mBatteryDrawable");
                                    drawable8.setBatteryLevel(level);
                                    drawable8.setCharging(charging);
                                    drawable8.setPowerSaveEnabled(powerSave);
                                    break;
                                case BATTERY_STYLE_LANDSCAPE_SIGNAL:
                                    LandscapeBatteryDrawableSignal drawable9 = (LandscapeBatteryDrawableSignal) getAdditionalInstanceField(view, "mBatteryDrawable");
                                    drawable9.setBatteryLevel(level);
                                    drawable9.setCharging(charging);
                                    drawable9.setPowerSaveEnabled(powerSave);
                                    break;
                                case BATTERY_STYLE_RLANDSCAPE_STYLE_A:
                                    RLandscapeBatteryDrawableStyleA drawable10 = (RLandscapeBatteryDrawableStyleA) getAdditionalInstanceField(view, "mBatteryDrawable");
                                    drawable10.setBatteryLevel(level);
                                    drawable10.setCharging(charging);
                                    drawable10.setPowerSaveEnabled(powerSave);
                                    break;
                                case BATTERY_STYLE_LANDSCAPE_STYLE_A:
                                    LandscapeBatteryDrawableStyleA drawable11 = (LandscapeBatteryDrawableStyleA) getAdditionalInstanceField(view, "mBatteryDrawable");
                                    drawable11.setBatteryLevel(level);
                                    drawable11.setCharging(charging);
                                    drawable11.setPowerSaveEnabled(powerSave);
                                    break;
                                case BATTERY_STYLE_RLANDSCAPE_STYLE_B:
                                    RLandscapeBatteryDrawableStyleB drawable12 = (RLandscapeBatteryDrawableStyleB) getAdditionalInstanceField(view, "mBatteryDrawable");
                                    drawable12.setBatteryLevel(level);
                                    drawable12.setCharging(charging);
                                    drawable12.setPowerSaveEnabled(powerSave);
                                    break;
                                case BATTERY_STYLE_LANDSCAPE_STYLE_B:
                                    LandscapeBatteryDrawableStyleB drawable13 = (LandscapeBatteryDrawableStyleB) getAdditionalInstanceField(view, "mBatteryDrawable");
                                    drawable13.setBatteryLevel(level);
                                    drawable13.setCharging(charging);
                                    drawable13.setPowerSaveEnabled(powerSave);
                                    break;
                            }
                        } catch (Throwable ignored) {
                        }
                        scale(view);
                    });
                }
            }
        };

        hookAllMethods(BatteryControllerImplClass, "fireBatteryLevelChanged", batteryDataRefreshHook);
        hookAllMethods(BatteryControllerImplClass, "firePowerSaveChanged", batteryDataRefreshHook);

        View.OnAttachStateChangeListener listener = new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                batteryViews.add(v);
                new Thread(() -> {
                    try {
                        Thread.sleep(500);
                        callMethod(BatteryController, "fireBatteryLevelChanged");
                    } catch (Throwable ignored) {
                    }
                }).start();
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                batteryViews.remove(v);
            }
        };

        hookAllConstructors(BatteryMeterViewClass, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                ((View) param.thisObject).addOnAttachStateChangeListener(listener);

                if (!customBatteryEnabled) return;

                ImageView mBatteryIconView = (ImageView) getObjectField(param.thisObject, "mBatteryIconView");
                mBatteryIconView.setRotation(batteryRotation);

                switch (BatteryStyle) {
                    case BATTERY_STYLE_DEFAULT:
                    case BATTERY_STYLE_DEFAULT_RLANDSCAPE:
                    case BATTERY_STYLE_DEFAULT_LANDSCAPE:
                        break;
                    case BATTERY_STYLE_CUSTOM_RLANDSCAPE:
                        RLandscapeBatteryDrawable drawable3 = new RLandscapeBatteryDrawable(mContext, frameColor);
                        setAdditionalInstanceField(param.thisObject, "mBatteryDrawable", drawable3);
                        mBatteryIconView.setImageDrawable(drawable3);
                        break;
                    case BATTERY_STYLE_CUSTOM_LANDSCAPE:
                        LandscapeBatteryDrawable drawable4 = new LandscapeBatteryDrawable(mContext, frameColor);
                        setAdditionalInstanceField(param.thisObject, "mBatteryDrawable", drawable4);
                        mBatteryIconView.setImageDrawable(drawable4);
                        break;
                    case BATTERY_STYLE_LANDSCAPE_BUDDY:
                        LandscapeBatteryDrawableBuddy drawable5 = new LandscapeBatteryDrawableBuddy(mContext, frameColor);
                        setAdditionalInstanceField(param.thisObject, "mBatteryDrawable", drawable5);
                        mBatteryIconView.setImageDrawable(drawable5);
                        break;
                    case BATTERY_STYLE_LANDSCAPE_LINE:
                        LandscapeBatteryDrawableLine drawable6 = new LandscapeBatteryDrawableLine(mContext, frameColor);
                        setAdditionalInstanceField(param.thisObject, "mBatteryDrawable", drawable6);
                        mBatteryIconView.setImageDrawable(drawable6);
                        break;
                    case BATTERY_STYLE_LANDSCAPE_MUSKU:
                        LandscapeBatteryDrawableMusku drawable7 = new LandscapeBatteryDrawableMusku(mContext, frameColor);
                        setAdditionalInstanceField(param.thisObject, "mBatteryDrawable", drawable7);
                        mBatteryIconView.setImageDrawable(drawable7);
                        break;
                    case BATTERY_STYLE_LANDSCAPE_PILL:
                        LandscapeBatteryDrawablePill drawable8 = new LandscapeBatteryDrawablePill(mContext, frameColor);
                        setAdditionalInstanceField(param.thisObject, "mBatteryDrawable", drawable8);
                        mBatteryIconView.setImageDrawable(drawable8);
                        break;
                    case BATTERY_STYLE_LANDSCAPE_SIGNAL:
                        LandscapeBatteryDrawableSignal drawable9 = new LandscapeBatteryDrawableSignal(mContext, frameColor);
                        setAdditionalInstanceField(param.thisObject, "mBatteryDrawable", drawable9);
                        mBatteryIconView.setImageDrawable(drawable9);
                        break;
                    case BATTERY_STYLE_RLANDSCAPE_STYLE_A:
                        RLandscapeBatteryDrawableStyleA drawable10 = new RLandscapeBatteryDrawableStyleA(mContext, frameColor);
                        setAdditionalInstanceField(param.thisObject, "mBatteryDrawable", drawable10);
                        mBatteryIconView.setImageDrawable(drawable10);
                        break;
                    case BATTERY_STYLE_LANDSCAPE_STYLE_A:
                        LandscapeBatteryDrawableStyleA drawable11 = new LandscapeBatteryDrawableStyleA(mContext, frameColor);
                        setAdditionalInstanceField(param.thisObject, "mBatteryDrawable", drawable11);
                        mBatteryIconView.setImageDrawable(drawable11);
                        break;
                    case BATTERY_STYLE_RLANDSCAPE_STYLE_B:
                        RLandscapeBatteryDrawableStyleB drawable12 = new RLandscapeBatteryDrawableStyleB(mContext, frameColor);
                        setAdditionalInstanceField(param.thisObject, "mBatteryDrawable", drawable12);
                        mBatteryIconView.setImageDrawable(drawable12);
                        break;
                    case BATTERY_STYLE_LANDSCAPE_STYLE_B:
                        LandscapeBatteryDrawableStyleB drawable13 = new LandscapeBatteryDrawableStyleB(mContext, frameColor);
                        setAdditionalInstanceField(param.thisObject, "mBatteryDrawable", drawable13);
                        mBatteryIconView.setImageDrawable(drawable13);
                        break;
                }

                setObjectField(param.thisObject, "mBatteryIconView", mBatteryIconView);

                callMethod(BatteryController, "fireBatteryLevelChanged");
            }
        });

        findAndHookMethod(BatteryMeterViewClass, "updateColors", int.class, int.class, int.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (!customBatteryEnabled) return;

                try {
                    switch (BatteryStyle) {
                        case BATTERY_STYLE_DEFAULT:
                        case BATTERY_STYLE_DEFAULT_RLANDSCAPE:
                        case BATTERY_STYLE_DEFAULT_LANDSCAPE:
                            break;
                        case BATTERY_STYLE_CUSTOM_RLANDSCAPE:
                            RLandscapeBatteryDrawable drawable3 = (RLandscapeBatteryDrawable) getAdditionalInstanceField(param.thisObject, "mBatteryDrawable");
                            if (drawable3 != null) {
                                drawable3.setColors((int) param.args[0], (int) param.args[1], (int) param.args[2]);
                            }
                            break;
                        case BATTERY_STYLE_CUSTOM_LANDSCAPE:
                            LandscapeBatteryDrawable drawable4 = (LandscapeBatteryDrawable) getAdditionalInstanceField(param.thisObject, "mBatteryDrawable");
                            if (drawable4 != null) {
                                drawable4.setColors((int) param.args[0], (int) param.args[1], (int) param.args[2]);
                            }
                            break;
                        case BATTERY_STYLE_LANDSCAPE_BUDDY:
                            LandscapeBatteryDrawableBuddy drawable5 = (LandscapeBatteryDrawableBuddy) getAdditionalInstanceField(param.thisObject, "mBatteryDrawable");
                            if (drawable5 != null) {
                                drawable5.setColors((int) param.args[0], (int) param.args[1], (int) param.args[2]);
                            }
                            break;
                        case BATTERY_STYLE_LANDSCAPE_LINE:
                            LandscapeBatteryDrawableLine drawable6 = (LandscapeBatteryDrawableLine) getAdditionalInstanceField(param.thisObject, "mBatteryDrawable");
                            if (drawable6 != null) {
                                drawable6.setColors((int) param.args[0], (int) param.args[1], (int) param.args[2]);
                            }
                            break;
                        case BATTERY_STYLE_LANDSCAPE_MUSKU:
                            LandscapeBatteryDrawableMusku drawable7 = (LandscapeBatteryDrawableMusku) getAdditionalInstanceField(param.thisObject, "mBatteryDrawable");
                            if (drawable7 != null) {
                                drawable7.setColors((int) param.args[0], (int) param.args[1], (int) param.args[2]);
                            }
                            break;
                        case BATTERY_STYLE_LANDSCAPE_PILL:
                            LandscapeBatteryDrawablePill drawable8 = (LandscapeBatteryDrawablePill) getAdditionalInstanceField(param.thisObject, "mBatteryDrawable");
                            if (drawable8 != null) {
                                drawable8.setColors((int) param.args[0], (int) param.args[1], (int) param.args[2]);
                            }
                            break;
                        case BATTERY_STYLE_LANDSCAPE_SIGNAL:
                            LandscapeBatteryDrawableSignal drawable9 = (LandscapeBatteryDrawableSignal) getAdditionalInstanceField(param.thisObject, "mBatteryDrawable");
                            if (drawable9 != null) {
                                drawable9.setColors((int) param.args[0], (int) param.args[1], (int) param.args[2]);
                            }
                            break;
                        case BATTERY_STYLE_RLANDSCAPE_STYLE_A:
                            RLandscapeBatteryDrawableStyleA drawable10 = (RLandscapeBatteryDrawableStyleA) getAdditionalInstanceField(param.thisObject, "mBatteryDrawable");
                            if (drawable10 != null) {
                                drawable10.setColors((int) param.args[0], (int) param.args[1], (int) param.args[2]);
                            }
                            break;
                        case BATTERY_STYLE_LANDSCAPE_STYLE_A:
                            LandscapeBatteryDrawableStyleA drawable11 = (LandscapeBatteryDrawableStyleA) getAdditionalInstanceField(param.thisObject, "mBatteryDrawable");
                            if (drawable11 != null) {
                                drawable11.setColors((int) param.args[0], (int) param.args[1], (int) param.args[2]);
                            }
                            break;
                        case BATTERY_STYLE_RLANDSCAPE_STYLE_B:
                            RLandscapeBatteryDrawableStyleB drawable12 = (RLandscapeBatteryDrawableStyleB) getAdditionalInstanceField(param.thisObject, "mBatteryDrawable");
                            if (drawable12 != null) {
                                drawable12.setColors((int) param.args[0], (int) param.args[1], (int) param.args[2]);
                            }
                            break;
                        case BATTERY_STYLE_LANDSCAPE_STYLE_B:
                            LandscapeBatteryDrawableStyleB drawable13 = (LandscapeBatteryDrawableStyleB) getAdditionalInstanceField(param.thisObject, "mBatteryDrawable");
                            if (drawable13 != null) {
                                drawable13.setColors((int) param.args[0], (int) param.args[1], (int) param.args[2]);
                            }
                            break;
                    }
                } catch (Throwable ignored) {
                }
            }
        });

        setCustomBatterySize();
    }

    public static void scale(Object thisObject) {
        ImageView mBatteryIconView = (ImageView) getObjectField(thisObject, "mBatteryIconView");
        scale(mBatteryIconView);
    }

    @SuppressLint("DiscouragedApi")
    public static void scale(ImageView mBatteryIconView) {
        if (mBatteryIconView == null) {
            return;
        }

        Context context = mBatteryIconView.getContext();
        Resources res = context.getResources();

        TypedValue typedValue = new TypedValue();

        res.getValue(res.getIdentifier("status_bar_icon_scale_factor", "dimen", context.getPackageName()), typedValue, true);
        float iconScaleFactor = typedValue.getFloat() * (scaleFactor / 100f);

        int batteryHeight = res.getDimensionPixelSize(res.getIdentifier("status_bar_battery_icon_height", "dimen", context.getPackageName()));
        int batteryWidth = res.getDimensionPixelSize(res.getIdentifier((customBatteryEnabled) ? "status_bar_battery_icon_height" : "status_bar_battery_icon_width", "dimen", context.getPackageName()));

        ViewGroup.LayoutParams scaledLayoutParams = mBatteryIconView.getLayoutParams();
        scaledLayoutParams.height = (int) (batteryHeight * iconScaleFactor);
        scaledLayoutParams.width = (int) (batteryWidth * iconScaleFactor);

        mBatteryIconView.setLayoutParams(scaledLayoutParams);
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName) && !HookEntry.isChildProcess;
    }

    private void setCustomBatterySize() {
        XC_InitPackageResources.InitPackageResourcesParam ourResparam = resparams.get(SYSTEMUI_PACKAGE);
        if (ourResparam == null) return;

        if (customBatteryEnabled) {
            ourResparam.res.setReplacement(SYSTEMUI_PACKAGE, "dimen", "status_bar_battery_icon_width", new XResources.DimensionReplacement(landscapeBatteryWidth, TypedValue.COMPLEX_UNIT_DIP));
            ourResparam.res.setReplacement(SYSTEMUI_PACKAGE, "dimen", "status_bar_battery_icon_height", new XResources.DimensionReplacement(landscapeBatteryHeight, TypedValue.COMPLEX_UNIT_DIP));
        }
    }
}