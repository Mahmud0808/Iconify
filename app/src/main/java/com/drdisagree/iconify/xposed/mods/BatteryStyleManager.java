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
import static com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_LANDSCAPE_COLOROS;
import static com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_LANDSCAPE_IOS_15;
import static com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_LANDSCAPE_IOS_16;
import static com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_LANDSCAPE_MIUI_PILL;
import static com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_LANDSCAPE_SMILEY;
import static com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_LANDSCAPE_STYLE_A;
import static com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_LANDSCAPE_STYLE_B;
import static com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_PORTRAIT_AIROO;
import static com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_PORTRAIT_CAPSULE;
import static com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_PORTRAIT_LORN;
import static com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_PORTRAIT_MX;
import static com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_PORTRAIT_ORIGAMI;
import static com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_RLANDSCAPE_COLOROS;
import static com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_RLANDSCAPE_STYLE_A;
import static com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_RLANDSCAPE_STYLE_B;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_HEIGHT;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_MARGIN;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_STYLE;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_WIDTH;
import static com.drdisagree.iconify.config.XPrefs.Xprefs;
import static com.drdisagree.iconify.xposed.HookRes.resparams;
import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedBridge.log;
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
import com.drdisagree.iconify.xposed.mods.batterystyles.BatteryDrawable;
import com.drdisagree.iconify.xposed.mods.batterystyles.LandscapeBatteryDrawable;
import com.drdisagree.iconify.xposed.mods.batterystyles.LandscapeBatteryDrawableColorOS;
import com.drdisagree.iconify.xposed.mods.batterystyles.LandscapeBatteryDrawableMIUIPill;
import com.drdisagree.iconify.xposed.mods.batterystyles.LandscapeBatteryDrawableSmiley;
import com.drdisagree.iconify.xposed.mods.batterystyles.LandscapeBatteryDrawableStyleA;
import com.drdisagree.iconify.xposed.mods.batterystyles.LandscapeBatteryDrawableStyleB;
import com.drdisagree.iconify.xposed.mods.batterystyles.LandscapeBatteryDrawableiOS15;
import com.drdisagree.iconify.xposed.mods.batterystyles.LandscapeBatteryDrawableiOS16;
import com.drdisagree.iconify.xposed.mods.batterystyles.LandscapeRBatteryDrawable;
import com.drdisagree.iconify.xposed.mods.batterystyles.LandscapeRBatteryDrawableColorOS;
import com.drdisagree.iconify.xposed.mods.batterystyles.LandscapeRBatteryDrawableStyleA;
import com.drdisagree.iconify.xposed.mods.batterystyles.LandscapeRBatteryDrawableStyleB;
import com.drdisagree.iconify.xposed.mods.batterystyles.PortraitBatteryDrawableAiroo;
import com.drdisagree.iconify.xposed.mods.batterystyles.PortraitBatteryDrawableCapsule;
import com.drdisagree.iconify.xposed.mods.batterystyles.PortraitBatteryDrawableLorn;
import com.drdisagree.iconify.xposed.mods.batterystyles.PortraitBatteryDrawableMx;
import com.drdisagree.iconify.xposed.mods.batterystyles.PortraitBatteryDrawableOrigami;
import com.drdisagree.iconify.xposed.utils.SettingsLibUtils;

import java.util.ArrayList;
import java.util.Objects;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class BatteryStyleManager extends ModPack {

    public static final String listenPackage = SYSTEMUI_PACKAGE;
    private static final String TAG = "Iconify - BatteryStyleManager: ";
    private static final ArrayList<Object> batteryViews = new ArrayList<>();
    private static final int BatteryIconOpacity = 100;
    public static int BatteryStyle = 0;
    public static boolean showPercentInside = false;
    public static int scaleFactor = 100;
    public static int batteryRotation = 0;
    private static boolean customBatteryEnabled = false;
    private static int customBatteryWidth = 20;
    private static int customBatteryHeight = 20;
    private int frameColor;
    private Object BatteryController = null;
    private int customBatteryMargin = 6;

    public BatteryStyleManager(Context context) {
        super(context);
    }

    private static void refreshBatteryIcons() {
        try {
            for (Object view : batteryViews) {
                ImageView mBatteryIconView = (ImageView) getObjectField(view, "mBatteryIconView");
                if (mBatteryIconView != null) {
                    mBatteryIconView.setRotation(batteryRotation);
                }

                if (customBatteryEnabled) {
                    scale(mBatteryIconView);
                    try {
                        BatteryDrawable drawable = (BatteryDrawable) getAdditionalInstanceField(view, "mBatteryDrawable");
                        drawable.setShowPercentEnabled(showPercentInside);
                        drawable.setAlpha(Math.round(BatteryIconOpacity * 2.55f));
                        drawable.invalidateSelf();
                    } catch (Throwable ignored) {
                    }
                }
            }
        } catch (Throwable throwable) {
            log(TAG + throwable);
        }
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

        try {
            Context context = mBatteryIconView.getContext();
            Resources res = context.getResources();

            TypedValue typedValue = new TypedValue();

            res.getValue(res.getIdentifier("status_bar_icon_scale_factor", "dimen", context.getPackageName()), typedValue, true);
            float iconScaleFactor = typedValue.getFloat() * (scaleFactor / 100f);

            int batteryHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, customBatteryHeight, mBatteryIconView.getContext().getResources().getDisplayMetrics());
            int batteryWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, customBatteryWidth, mBatteryIconView.getContext().getResources().getDisplayMetrics());

            ViewGroup.LayoutParams scaledLayoutParams = mBatteryIconView.getLayoutParams();
            scaledLayoutParams.height = (int) (batteryHeight * iconScaleFactor);
            scaledLayoutParams.width = (int) (batteryWidth * iconScaleFactor);

            mBatteryIconView.setLayoutParams(scaledLayoutParams);
        } catch (Throwable throwable) {
            log(TAG + throwable);
        }
    }

    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;

        int batteryStyle = Xprefs.getInt(CUSTOM_BATTERY_STYLE, 0);
        customBatteryEnabled = batteryStyle != BATTERY_STYLE_DEFAULT && batteryStyle != BATTERY_STYLE_DEFAULT_LANDSCAPE && batteryStyle != BATTERY_STYLE_DEFAULT_RLANDSCAPE;
        customBatteryWidth = Xprefs.getInt(CUSTOM_BATTERY_WIDTH, 20);
        customBatteryHeight = Xprefs.getInt(CUSTOM_BATTERY_HEIGHT, 20);
        customBatteryMargin = Xprefs.getInt(CUSTOM_BATTERY_MARGIN, 6);

        if (batteryStyle == BATTERY_STYLE_DEFAULT_RLANDSCAPE) {
            batteryRotation = 90;
        } else if (batteryStyle == BATTERY_STYLE_DEFAULT_LANDSCAPE) {
            batteryRotation = 270;
        } else {
            batteryRotation = 0;
        }

        showPercentInside = batteryStyle == BATTERY_STYLE_LANDSCAPE_IOS_16;

        if (BatteryStyle != batteryStyle) {
            BatteryStyle = batteryStyle;
            try {
                for (Object view : batteryViews) {
                    ImageView mBatteryIconView = (ImageView) getObjectField(view, "mBatteryIconView");
                    if (mBatteryIconView != null) {
                        mBatteryIconView.setRotation(batteryRotation);
                    }

                    boolean mCharging = (boolean) getObjectField(view, "mCharging");
                    int mLevel = (int) getObjectField(view, "mLevel");

                    if (customBatteryEnabled) {
                        BatteryDrawable newDrawable = getNewDrawable(mContext);
                        if (newDrawable != null) {
                            if (mBatteryIconView != null) {
                                mBatteryIconView.setImageDrawable(newDrawable);
                            }
                            setAdditionalInstanceField(view, "mBatteryDrawable", newDrawable);
                            newDrawable.setBatteryLevel(mLevel);
                            newDrawable.setChargingEnabled(mCharging);
                        }
                    }
                }
            } catch (Throwable throwable) {
                log(TAG + throwable);
            }
        }

        refreshBatteryIcons();

        if (Key.length > 0 && (Objects.equals(Key[0], CUSTOM_BATTERY_WIDTH) || Objects.equals(Key[0], CUSTOM_BATTERY_HEIGHT) || Objects.equals(Key[0], CUSTOM_BATTERY_MARGIN))) {
            setCustomBatteryDimens();
        }
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!lpparam.packageName.equals(listenPackage)) return;

        Class<?> BatteryControllerImplClass = findClass("com.android.systemui.statusbar.policy.BatteryControllerImpl", lpparam.classLoader);
        Class<?> BatteryMeterViewClass = findClassIfExists("com.android.systemui.battery.BatteryMeterView", lpparam.classLoader);
        if (BatteryMeterViewClass == null) {
            BatteryMeterViewClass = findClass("com.android.systemui.BatteryMeterView", lpparam.classLoader);
        }
        SettingsLibUtils.init(lpparam.classLoader);

        try {
            findAndHookConstructor("com.android.settingslib.graph.ThemedBatteryDrawable", lpparam.classLoader, Context.class, int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    frameColor = (int) param.args[1];
                }
            });
        } catch (Throwable throwable) {
            log(TAG + throwable);
        }

        try {
            hookAllConstructors(BatteryControllerImplClass, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    BatteryController = param.thisObject;
                }
            });
        } catch (Throwable throwable) {
            log(TAG + throwable);
        }

        try {
            hookAllMethods(BatteryControllerImplClass, "fireBatteryUnknownStateChanged", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (!customBatteryEnabled) return;

                    for (Object view : batteryViews) {
                        BatteryDrawable mBatteryDrawable = (BatteryDrawable) getAdditionalInstanceField(view, "mBatteryDrawable");
                        callMethod(view, "setImageDrawable", mBatteryDrawable);
                    }
                }
            });
        } catch (Throwable throwable) {
            log(TAG + throwable);
        }

        try {
            XC_MethodHook batteryDataRefreshHook = new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    int level = getIntField(param.thisObject, "mLevel");
                    boolean charging = getBooleanField(param.thisObject, "mPluggedIn") || getBooleanField(param.thisObject, "mCharging") || getBooleanField(param.thisObject, "mWirelessCharging");
                    boolean powerSave = getBooleanField(param.thisObject, "mPowerSave");

                    if (!customBatteryEnabled) return;

                    try {
                        for (Object view : batteryViews) {
                            ((View) view).post(() -> {
                                BatteryDrawable drawable = (BatteryDrawable) getAdditionalInstanceField(view, "mBatteryDrawable");
                                if (drawable != null) {
                                    drawable.setBatteryLevel(level);
                                    drawable.setChargingEnabled(charging);
                                    drawable.setPowerSavingEnabled(powerSave);
                                }
                                scale(view);
                            });
                        }
                    } catch (Throwable ignored) {
                    }
                }
            };

            hookAllMethods(BatteryControllerImplClass, "fireBatteryLevelChanged", batteryDataRefreshHook);
            hookAllMethods(BatteryControllerImplClass, "firePowerSaveChanged", batteryDataRefreshHook);
        } catch (Throwable throwable) {
            log(TAG + throwable);
        }

        try {
            View.OnAttachStateChangeListener listener = new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View v) {
                    batteryViews.add(v);
                    new Thread(() -> {
                        try {
                            Thread.sleep(500);
                            if (BatteryController != null) {
                                callMethod(BatteryController, "fireBatteryLevelChanged");
                            }
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

                    ImageView mBatteryIconView = (ImageView) getObjectField(param.thisObject, "mBatteryIconView");
                    initBatteryIfNull(param, mBatteryIconView);
                    mBatteryIconView = (ImageView) getObjectField(param.thisObject, "mBatteryIconView");

                    if (customBatteryEnabled || BatteryStyle == BATTERY_STYLE_DEFAULT_LANDSCAPE || BatteryStyle == BATTERY_STYLE_DEFAULT_RLANDSCAPE) {
                        mBatteryIconView.setRotation(batteryRotation);
                    }

                    if (!customBatteryEnabled) return;

                    BatteryDrawable mBatteryDrawable = getNewDrawable(mContext);
                    if (mBatteryDrawable != null) {
                        setAdditionalInstanceField(param.thisObject, "mBatteryDrawable", mBatteryDrawable);
                        mBatteryIconView.setImageDrawable(mBatteryDrawable);
                        setObjectField(param.thisObject, "mBatteryIconView", mBatteryIconView);
                    }

                    if (BatteryController != null) {
                        callMethod(BatteryController, "fireBatteryLevelChanged");
                    }

                    hidePercentage(param);
                }
            });
        } catch (Throwable throwable) {
            log(TAG + throwable);
        }

        try {
            findAndHookMethod(BatteryMeterViewClass, "updateColors", int.class, int.class, int.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (!customBatteryEnabled) return;

                    BatteryDrawable mBatteryDrawable = (BatteryDrawable) getAdditionalInstanceField(param.thisObject, "mBatteryDrawable");
                    if (mBatteryDrawable != null) {
                        mBatteryDrawable.setColors((int) param.args[0], (int) param.args[1], (int) param.args[2]);
                    }

                    hidePercentage(param);
                }
            });
        } catch (Throwable throwable) {
            log(TAG + throwable);
        }

        if (customBatteryEnabled) {
            try {
                hookAllMethods(BatteryMeterViewClass, "updateDrawable", new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam methodHookParam) {
                        return null;
                    }
                });

                hookAllMethods(BatteryMeterViewClass, "updateBatteryStyle", new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam methodHookParam) {
                        return null;
                    }
                });
            } catch (Throwable ignored) {
            }
        }

        setCustomBatteryDimens();
    }

    @SuppressLint("DiscouragedApi")
    private void initBatteryIfNull(XC_MethodHook.MethodHookParam param, ImageView mBatteryIconView) {
        if (mBatteryIconView == null) {
            mBatteryIconView = new ImageView(mContext);
            try {
                mBatteryIconView.setImageDrawable((Drawable) getObjectField(param.thisObject, "mAccessorizedDrawable"));
            } catch (Throwable throwable) {
                try {
                    mBatteryIconView.setImageDrawable((Drawable) getObjectField(param.thisObject, "mThemedDrawable"));
                } catch (Throwable throwable1) {
                    mBatteryIconView.setImageDrawable((Drawable) getObjectField(param.thisObject, "mDrawable"));
                }
            }
            final ViewGroup.MarginLayoutParams mlp = new ViewGroup.MarginLayoutParams(mContext.getResources().getDimensionPixelSize(mContext.getResources().getIdentifier("status_bar_battery_icon_width", "dimen", mContext.getPackageName())), mContext.getResources().getDimensionPixelSize(mContext.getResources().getIdentifier("status_bar_battery_icon_height", "dimen", mContext.getPackageName())));
            mlp.setMargins(0, 0, 0, mContext.getResources().getDimensionPixelOffset(mContext.getResources().getIdentifier("battery_margin_bottom", "dimen", mContext.getPackageName())));
            setObjectField(param.thisObject, "mBatteryIconView", mBatteryIconView);
            callMethod(param.thisObject, "addView", mBatteryIconView, mlp);
        }
    }

    private BatteryDrawable getNewDrawable(Context context) {
        BatteryDrawable mBatteryDrawable = null;
        switch (BatteryStyle) {
            case BATTERY_STYLE_DEFAULT:
            case BATTERY_STYLE_DEFAULT_RLANDSCAPE:
            case BATTERY_STYLE_DEFAULT_LANDSCAPE:
                break;
            case BATTERY_STYLE_CUSTOM_RLANDSCAPE:
                mBatteryDrawable = new LandscapeRBatteryDrawable(context, frameColor);
                break;
            case BATTERY_STYLE_CUSTOM_LANDSCAPE:
                mBatteryDrawable = new LandscapeBatteryDrawable(context, frameColor);
                break;
            case BATTERY_STYLE_PORTRAIT_CAPSULE:
                mBatteryDrawable = new PortraitBatteryDrawableCapsule(context, frameColor);
                break;
            case BATTERY_STYLE_PORTRAIT_LORN:
                mBatteryDrawable = new PortraitBatteryDrawableLorn(context, frameColor);
                break;
            case BATTERY_STYLE_PORTRAIT_MX:
                mBatteryDrawable = new PortraitBatteryDrawableMx(context, frameColor);
                break;
            case BATTERY_STYLE_PORTRAIT_AIROO:
                mBatteryDrawable = new PortraitBatteryDrawableAiroo(context, frameColor);
                break;
            case BATTERY_STYLE_RLANDSCAPE_STYLE_A:
                mBatteryDrawable = new LandscapeRBatteryDrawableStyleA(context, frameColor);
                break;
            case BATTERY_STYLE_LANDSCAPE_STYLE_A:
                mBatteryDrawable = new LandscapeBatteryDrawableStyleA(context, frameColor);
                break;
            case BATTERY_STYLE_RLANDSCAPE_STYLE_B:
                mBatteryDrawable = new LandscapeRBatteryDrawableStyleB(context, frameColor);
                break;
            case BATTERY_STYLE_LANDSCAPE_STYLE_B:
                mBatteryDrawable = new LandscapeBatteryDrawableStyleB(context, frameColor);
                break;
            case BATTERY_STYLE_LANDSCAPE_IOS_15:
                mBatteryDrawable = new LandscapeBatteryDrawableiOS15(context, frameColor);
                break;
            case BATTERY_STYLE_LANDSCAPE_IOS_16:
                mBatteryDrawable = new LandscapeBatteryDrawableiOS16(context, frameColor);
                break;
            case BATTERY_STYLE_PORTRAIT_ORIGAMI:
                mBatteryDrawable = new PortraitBatteryDrawableOrigami(context, frameColor);
                break;
            case BATTERY_STYLE_LANDSCAPE_SMILEY:
                mBatteryDrawable = new LandscapeBatteryDrawableSmiley(context, frameColor);
                break;
            case BATTERY_STYLE_LANDSCAPE_MIUI_PILL:
                mBatteryDrawable = new LandscapeBatteryDrawableMIUIPill(context, frameColor);
                break;
            case BATTERY_STYLE_LANDSCAPE_COLOROS:
                mBatteryDrawable = new LandscapeBatteryDrawableColorOS(context, frameColor);
                break;
            case BATTERY_STYLE_RLANDSCAPE_COLOROS:
                mBatteryDrawable = new LandscapeRBatteryDrawableColorOS(context, frameColor);
                break;
        }

        if (mBatteryDrawable != null) {
            mBatteryDrawable.setShowPercentEnabled(showPercentInside);
            mBatteryDrawable.setAlpha(Math.round(BatteryIconOpacity * 2.55f));
        }

        return mBatteryDrawable;
    }

    private void hidePercentage(XC_MethodHook.MethodHookParam param) {
        if (showPercentInside) {
            setObjectField(param.thisObject, "mShowPercentMode", 2);
            callMethod(param.thisObject, "updateShowPercent");
            callMethod(param.thisObject, "updatePercentText");
            try {
                callMethod(param.thisObject, "removeView", getObjectField(param.thisObject, "mBatteryPercentView"));
                setObjectField(param.thisObject, "mBatteryPercentView", null);
            } catch (Throwable ignored) {
            }
        }
    }

    private void setCustomBatteryDimens() {
        XC_InitPackageResources.InitPackageResourcesParam ourResparam = resparams.get(SYSTEMUI_PACKAGE);
        if (ourResparam == null) return;

        if (BatteryStyle != BATTERY_STYLE_DEFAULT) {
            ourResparam.res.setReplacement(SYSTEMUI_PACKAGE, "dimen", "status_bar_battery_icon_width", new XResources.DimensionReplacement(customBatteryWidth, TypedValue.COMPLEX_UNIT_DIP));
            ourResparam.res.setReplacement(SYSTEMUI_PACKAGE, "dimen", "status_bar_battery_icon_height", new XResources.DimensionReplacement(customBatteryHeight, TypedValue.COMPLEX_UNIT_DIP));
            ourResparam.res.setReplacement(SYSTEMUI_PACKAGE, "dimen", "signal_cluster_battery_padding", new XResources.DimensionReplacement(customBatteryMargin, TypedValue.COMPLEX_UNIT_DIP));
        }
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName) && !HookEntry.isChildProcess;
    }
}