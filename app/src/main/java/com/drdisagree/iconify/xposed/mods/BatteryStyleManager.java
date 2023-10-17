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
import static com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_LANDSCAPE_BATTERYA;
import static com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_LANDSCAPE_BATTERYB;
import static com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_LANDSCAPE_BATTERYC;
import static com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_LANDSCAPE_BATTERYD;
import static com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_LANDSCAPE_BATTERYE;
import static com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_LANDSCAPE_BATTERYF;
import static com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_LANDSCAPE_BATTERYG;
import static com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_LANDSCAPE_BATTERYH;
import static com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_LANDSCAPE_BATTERYI;
import static com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_LANDSCAPE_BATTERYJ;
import static com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_LANDSCAPE_BATTERYK;
import static com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_LANDSCAPE_BATTERYL;
import static com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_LANDSCAPE_BATTERYM;
import static com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_LANDSCAPE_BATTERYN;
import static com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_LANDSCAPE_BATTERYO;
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
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_BLEND_COLOR;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_CHARGING_COLOR;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_CHARGING_ICON_MARGIN_LEFT;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_CHARGING_ICON_MARGIN_RIGHT;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_CHARGING_ICON_STYLE;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_CHARGING_ICON_SWITCH;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_CHARGING_ICON_WIDTH_HEIGHT;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_DIMENSION;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_FILL_ALPHA;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_FILL_COLOR;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_FILL_GRAD_COLOR;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_HEIGHT;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_HIDE_PERCENTAGE;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_LAYOUT_REVERSE;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_MARGIN_BOTTOM;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_MARGIN_LEFT;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_MARGIN_RIGHT;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_MARGIN_TOP;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_PERIMETER_ALPHA;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_POWERSAVE_FILL_COLOR;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_POWERSAVE_INDICATOR_COLOR;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_RAINBOW_FILL_COLOR;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_STYLE;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_SWAP_PERCENTAGE;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_WIDTH;
import static com.drdisagree.iconify.common.Preferences.ICONIFY_CHARGING_ICON_TAG;
import static com.drdisagree.iconify.config.XPrefs.Xprefs;
import static com.drdisagree.iconify.xposed.HookRes.modRes;
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

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XResources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.xposed.ModPack;
import com.drdisagree.iconify.xposed.mods.batterystyles.BatteryDrawable;
import com.drdisagree.iconify.xposed.mods.batterystyles.LandscapeBattery;
import com.drdisagree.iconify.xposed.mods.batterystyles.LandscapeBatteryA;
import com.drdisagree.iconify.xposed.mods.batterystyles.LandscapeBatteryB;
import com.drdisagree.iconify.xposed.mods.batterystyles.LandscapeBatteryC;
import com.drdisagree.iconify.xposed.mods.batterystyles.LandscapeBatteryColorOS;
import com.drdisagree.iconify.xposed.mods.batterystyles.LandscapeBatteryD;
import com.drdisagree.iconify.xposed.mods.batterystyles.LandscapeBatteryE;
import com.drdisagree.iconify.xposed.mods.batterystyles.LandscapeBatteryF;
import com.drdisagree.iconify.xposed.mods.batterystyles.LandscapeBatteryG;
import com.drdisagree.iconify.xposed.mods.batterystyles.LandscapeBatteryH;
import com.drdisagree.iconify.xposed.mods.batterystyles.LandscapeBatteryI;
import com.drdisagree.iconify.xposed.mods.batterystyles.LandscapeBatteryJ;
import com.drdisagree.iconify.xposed.mods.batterystyles.LandscapeBatteryK;
import com.drdisagree.iconify.xposed.mods.batterystyles.LandscapeBatteryL;
import com.drdisagree.iconify.xposed.mods.batterystyles.LandscapeBatteryM;
import com.drdisagree.iconify.xposed.mods.batterystyles.LandscapeBatteryMIUIPill;
import com.drdisagree.iconify.xposed.mods.batterystyles.LandscapeBatteryN;
import com.drdisagree.iconify.xposed.mods.batterystyles.LandscapeBatteryO;
import com.drdisagree.iconify.xposed.mods.batterystyles.LandscapeBatterySmiley;
import com.drdisagree.iconify.xposed.mods.batterystyles.LandscapeBatteryStyleA;
import com.drdisagree.iconify.xposed.mods.batterystyles.LandscapeBatteryStyleB;
import com.drdisagree.iconify.xposed.mods.batterystyles.LandscapeBatteryiOS15;
import com.drdisagree.iconify.xposed.mods.batterystyles.LandscapeBatteryiOS16;
import com.drdisagree.iconify.xposed.mods.batterystyles.PortraitBatteryAiroo;
import com.drdisagree.iconify.xposed.mods.batterystyles.PortraitBatteryCapsule;
import com.drdisagree.iconify.xposed.mods.batterystyles.PortraitBatteryLorn;
import com.drdisagree.iconify.xposed.mods.batterystyles.PortraitBatteryMx;
import com.drdisagree.iconify.xposed.mods.batterystyles.PortraitBatteryOrigami;
import com.drdisagree.iconify.xposed.mods.batterystyles.RLandscapeBattery;
import com.drdisagree.iconify.xposed.mods.batterystyles.RLandscapeBatteryColorOS;
import com.drdisagree.iconify.xposed.mods.batterystyles.RLandscapeBatteryStyleA;
import com.drdisagree.iconify.xposed.mods.batterystyles.RLandscapeBatteryStyleB;
import com.drdisagree.iconify.xposed.utils.SettingsLibUtils;
import com.drdisagree.iconify.xposed.utils.ViewHelper;

import java.util.ArrayList;
import java.util.Objects;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

@SuppressWarnings({"DiscouragedApi", "unused", "FieldCanBeLocal"})
public class BatteryStyleManager extends ModPack {

    private static final String TAG = "Iconify - " + BatteryStyleManager.class.getSimpleName() + ": ";
    private static final ArrayList<View> batteryViews = new ArrayList<>();
    private static final int BatteryIconOpacity = 100;
    private static int BatteryStyle = 0;
    private static boolean mShowPercentInside = false;
    private boolean DefaultLandscapeBatteryEnabled = false;
    private static int mBatteryRotation = 0;
    private static boolean CustomBatteryEnabled = false;
    private static int mBatteryScaleWidth = 20;
    private static int mBatteryScaleHeight = 20;
    private int frameColor = Color.WHITE;
    private Object BatteryController = null;
    private XC_MethodHook.MethodHookParam BatteryMeterViewParam;
    private boolean mBatteryLayoutReverse = false;
    private static boolean mBatteryCustomDimension = false;
    private static int mBatteryMarginLeft = 0;
    private static int mBatteryMarginTop = 0;
    private static int mBatteryMarginRight = 0;
    private static int mBatteryMarginBottom = 0;
    private boolean mScaledPerimeterAlpha = false;
    private boolean mScaledFillAlpha = false;
    private boolean mRainbowFillColor = false;
    private boolean mCustomBlendColor = false;
    private int mCustomChargingColor = Color.BLACK;
    private int mCustomFillColor = Color.BLACK;
    private int mCustomFillGradColor = Color.BLACK;
    private int mCustomPowerSaveColor = Color.BLACK;
    private int mCustomPowerSaveFillColor = Color.BLACK;
    private boolean mSwapPercentage = false;
    private ImageView mChargingIconView;
    private boolean mChargingIconSwitch = false;
    private int mChargingIconStyle = 0;
    private int mChargingIconML = 1;
    private int mChargingIconMR = 0;
    private int mChargingIconWH = 14;
    private boolean mIsChargingImpl = false;
    private boolean mIsCharging = false;

    public BatteryStyleManager(Context context) {
        super(context);
    }

    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;

        int batteryStyle = Xprefs.getInt(CUSTOM_BATTERY_STYLE, 0);

        DefaultLandscapeBatteryEnabled = batteryStyle == BATTERY_STYLE_DEFAULT_LANDSCAPE ||
                batteryStyle == BATTERY_STYLE_DEFAULT_RLANDSCAPE;
        CustomBatteryEnabled = batteryStyle != BATTERY_STYLE_DEFAULT &&
                batteryStyle != BATTERY_STYLE_DEFAULT_LANDSCAPE &&
                batteryStyle != BATTERY_STYLE_DEFAULT_RLANDSCAPE;

        if (DefaultLandscapeBatteryEnabled) {
            if (batteryStyle == BATTERY_STYLE_DEFAULT_RLANDSCAPE) {
                mBatteryRotation = 90;
            } else {
                mBatteryRotation = 270;
            }
        } else {
            mBatteryRotation = 0;
        }

        mShowPercentInside = batteryStyle == BATTERY_STYLE_LANDSCAPE_IOS_16 ||
                batteryStyle == BATTERY_STYLE_LANDSCAPE_BATTERYL ||
                batteryStyle == BATTERY_STYLE_LANDSCAPE_BATTERYM ||
                Xprefs.getBoolean(CUSTOM_BATTERY_HIDE_PERCENTAGE, false);

        mBatteryLayoutReverse = Xprefs.getBoolean(CUSTOM_BATTERY_LAYOUT_REVERSE, false);
        mBatteryCustomDimension = Xprefs.getBoolean(CUSTOM_BATTERY_DIMENSION, false);
        mBatteryScaleWidth = Xprefs.getInt(CUSTOM_BATTERY_WIDTH, 20);
        mBatteryScaleHeight = Xprefs.getInt(CUSTOM_BATTERY_HEIGHT, 20);
        mScaledPerimeterAlpha = Xprefs.getBoolean(CUSTOM_BATTERY_PERIMETER_ALPHA, false);
        mScaledFillAlpha = Xprefs.getBoolean(CUSTOM_BATTERY_FILL_ALPHA, false);
        mRainbowFillColor = Xprefs.getBoolean(CUSTOM_BATTERY_RAINBOW_FILL_COLOR, false);
        mCustomBlendColor = Xprefs.getBoolean(CUSTOM_BATTERY_BLEND_COLOR, false);
        mCustomChargingColor = Xprefs.getInt(CUSTOM_BATTERY_CHARGING_COLOR, Color.BLACK);
        mCustomFillColor = Xprefs.getInt(CUSTOM_BATTERY_FILL_COLOR, Color.BLACK);
        mCustomFillGradColor = Xprefs.getInt(CUSTOM_BATTERY_FILL_GRAD_COLOR, Color.BLACK);
        mCustomPowerSaveColor = Xprefs.getInt(CUSTOM_BATTERY_POWERSAVE_INDICATOR_COLOR, Color.BLACK);
        mCustomPowerSaveFillColor = Xprefs.getInt(CUSTOM_BATTERY_POWERSAVE_FILL_COLOR, Color.BLACK);
        mSwapPercentage = Xprefs.getBoolean(CUSTOM_BATTERY_SWAP_PERCENTAGE, false);
        mChargingIconSwitch = Xprefs.getBoolean(CUSTOM_BATTERY_CHARGING_ICON_SWITCH, false);
        mChargingIconStyle = Xprefs.getInt(CUSTOM_BATTERY_CHARGING_ICON_STYLE, 0);
        mChargingIconML = Xprefs.getInt(CUSTOM_BATTERY_CHARGING_ICON_MARGIN_LEFT, 1);
        mChargingIconMR = Xprefs.getInt(CUSTOM_BATTERY_CHARGING_ICON_MARGIN_RIGHT, 0);
        mChargingIconWH = Xprefs.getInt(CUSTOM_BATTERY_CHARGING_ICON_WIDTH_HEIGHT, 14);
        mBatteryMarginLeft = ViewHelper.dp2px(mContext, Xprefs.getInt(CUSTOM_BATTERY_MARGIN_LEFT, 4));
        mBatteryMarginTop = ViewHelper.dp2px(mContext, Xprefs.getInt(CUSTOM_BATTERY_MARGIN_TOP, 0));
        mBatteryMarginRight = ViewHelper.dp2px(mContext, Xprefs.getInt(CUSTOM_BATTERY_MARGIN_RIGHT, 4));
        mBatteryMarginBottom = ViewHelper.dp2px(mContext, Xprefs.getInt(CUSTOM_BATTERY_MARGIN_BOTTOM, 0));

        if (BatteryStyle != batteryStyle) {
            BatteryStyle = batteryStyle;

            for (View view : batteryViews) {
                ImageView mBatteryIconView = (ImageView) getObjectField(view, "mBatteryIconView");
                if (mBatteryIconView != null) {
                    updateBatteryRotation(mBatteryIconView);
                    updateFlipper(mBatteryIconView.getParent());
                }

                TextView mBatteryPercentView = (TextView) getObjectField(view, "mBatteryPercentView");
                if (mBatteryPercentView != null) {
                    mBatteryPercentView.setVisibility(mShowPercentInside ? View.GONE : View.VISIBLE);
                }

                boolean mCharging = isBatteryCharging(view);
                int mLevel = (int) getObjectField(view, "mLevel");

                if (CustomBatteryEnabled) {
                    BatteryDrawable mBatteryDrawable = getNewBatteryDrawable(mContext);

                    if (mBatteryDrawable != null) {
                        if (mBatteryIconView != null) {
                            mBatteryIconView.setImageDrawable(mBatteryDrawable);
                        }

                        setAdditionalInstanceField(view, "mBatteryDrawable", mBatteryDrawable);
                        mBatteryDrawable.setBatteryLevel(mLevel);
                        mBatteryDrawable.setChargingEnabled(mCharging);
                        updateCustomizeBatteryDrawable(mBatteryDrawable);
                    }
                }
            }
        }

        refreshBatteryIcons();

        if (Key.length > 0 && (Objects.equals(Key[0], CUSTOM_BATTERY_WIDTH) || Objects.equals(Key[0], CUSTOM_BATTERY_HEIGHT))) {
            setDefaultBatteryDimens();
        }

        if (Key.length > 0 && (Objects.equals(Key[0], CUSTOM_BATTERY_STYLE) ||
                Objects.equals(Key[0], CUSTOM_BATTERY_HIDE_PERCENTAGE) ||
                Objects.equals(Key[0], CUSTOM_BATTERY_LAYOUT_REVERSE) ||
                Objects.equals(Key[0], CUSTOM_BATTERY_DIMENSION) ||
                Objects.equals(Key[0], CUSTOM_BATTERY_WIDTH) ||
                Objects.equals(Key[0], CUSTOM_BATTERY_HEIGHT) ||
                Objects.equals(Key[0], CUSTOM_BATTERY_PERIMETER_ALPHA) ||
                Objects.equals(Key[0], CUSTOM_BATTERY_FILL_ALPHA) ||
                Objects.equals(Key[0], CUSTOM_BATTERY_RAINBOW_FILL_COLOR) ||
                Objects.equals(Key[0], CUSTOM_BATTERY_BLEND_COLOR) ||
                Objects.equals(Key[0], CUSTOM_BATTERY_CHARGING_COLOR) ||
                Objects.equals(Key[0], CUSTOM_BATTERY_FILL_COLOR) ||
                Objects.equals(Key[0], CUSTOM_BATTERY_FILL_GRAD_COLOR) ||
                Objects.equals(Key[0], CUSTOM_BATTERY_POWERSAVE_INDICATOR_COLOR) ||
                Objects.equals(Key[0], CUSTOM_BATTERY_POWERSAVE_FILL_COLOR) ||
                Objects.equals(Key[0], CUSTOM_BATTERY_SWAP_PERCENTAGE) ||
                Objects.equals(Key[0], CUSTOM_BATTERY_CHARGING_ICON_SWITCH) ||
                Objects.equals(Key[0], CUSTOM_BATTERY_CHARGING_ICON_STYLE) ||
                Objects.equals(Key[0], CUSTOM_BATTERY_CHARGING_ICON_MARGIN_LEFT) ||
                Objects.equals(Key[0], CUSTOM_BATTERY_CHARGING_ICON_MARGIN_RIGHT) ||
                Objects.equals(Key[0], CUSTOM_BATTERY_CHARGING_ICON_WIDTH_HEIGHT) ||
                Objects.equals(Key[0], CUSTOM_BATTERY_MARGIN_LEFT) ||
                Objects.equals(Key[0], CUSTOM_BATTERY_MARGIN_TOP) ||
                Objects.equals(Key[0], CUSTOM_BATTERY_MARGIN_RIGHT) ||
                Objects.equals(Key[0], CUSTOM_BATTERY_MARGIN_BOTTOM))
        ) {
            if (BatteryMeterViewParam != null) {
                updateSettings(BatteryMeterViewParam);
            }
        }
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        Class<?> BatteryControllerImplClass = findClass(SYSTEMUI_PACKAGE + ".statusbar.policy.BatteryControllerImpl", lpparam.classLoader);
        Class<?> BatteryMeterViewClass = findClassIfExists(SYSTEMUI_PACKAGE + ".battery.BatteryMeterView", lpparam.classLoader);
        if (BatteryMeterViewClass == null) {
            BatteryMeterViewClass = findClass(SYSTEMUI_PACKAGE + ".BatteryMeterView", lpparam.classLoader);
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
                    if (!CustomBatteryEnabled) return;

                    for (View view : batteryViews) {
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
                    int mLevel = getIntField(param.thisObject, "mLevel");
                    mIsChargingImpl = getBooleanField(param.thisObject, "mPluggedIn")
                            || getBooleanField(param.thisObject, "mCharging")
                            || getBooleanField(param.thisObject, "mWirelessCharging");
                    boolean mPowerSave = getBooleanField(param.thisObject, "mPowerSave");

                    if (!CustomBatteryEnabled) return;

                    refreshBatteryData(mLevel, mIsChargingImpl, mPowerSave);
                    // refreshing twice to avoid a bug where the battery icon updates incorrectly
                    refreshBatteryData(mLevel, mIsChargingImpl, mPowerSave);
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
                public void onViewAttachedToWindow(@NonNull View v) {
                    batteryViews.add(v);
                    new Thread(() -> {
                        try {
                            if (BatteryController != null) {
                                Thread.sleep(500);
                                callMethod(BatteryController, "fireBatteryLevelChanged");
                            }
                        } catch (Throwable ignored) {
                        }
                    }).start();
                }

                @Override
                public void onViewDetachedFromWindow(@NonNull View v) {
                    batteryViews.remove(v);
                }
            };

            findAndHookConstructor(BatteryMeterViewClass, Context.class, AttributeSet.class, int.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (BatteryMeterViewParam == null) {
                        BatteryMeterViewParam = param;
                    }

                    final int[] styleableBatteryMeterView = new int[]{
                            mContext.getResources().getIdentifier("frameColor", "attr", mContext.getPackageName()),
                            mContext.getResources().getIdentifier("textAppearance", "attr", mContext.getPackageName())
                    };
                    TypedArray atts = mContext.obtainStyledAttributes((AttributeSet) param.args[1],
                            styleableBatteryMeterView,
                            (int) param.args[2],
                            0);
                    frameColor = atts.getColor(
                            mContext.getResources().getIdentifier("BatteryMeterView_frameColor", "styleable", mContext.getPackageName()),
                            mContext.getColor(mContext.getResources().getIdentifier("meter_background_color", "color", mContext.getPackageName()))
                    );
                    atts.recycle();

                    ((View) param.thisObject).addOnAttachStateChangeListener(listener);

                    ImageView mBatteryIconView = initBatteryIfNull(param, (ImageView) getObjectField(param.thisObject, "mBatteryIconView"));

                    if (CustomBatteryEnabled || BatteryStyle == BATTERY_STYLE_DEFAULT_LANDSCAPE || BatteryStyle == BATTERY_STYLE_DEFAULT_RLANDSCAPE) {
                        updateBatteryRotation(mBatteryIconView);
                        updateFlipper(mBatteryIconView.getParent());
                    }

                    if (!CustomBatteryEnabled) return;

                    BatteryDrawable mBatteryDrawable = getNewBatteryDrawable(mContext);

                    if (mBatteryDrawable != null) {
                        setAdditionalInstanceField(param.thisObject, "mBatteryDrawable", mBatteryDrawable);
                        mBatteryIconView.setImageDrawable(mBatteryDrawable);
                        setObjectField(param.thisObject, "mBatteryIconView", mBatteryIconView);
                    }

                    boolean mCharging = isBatteryCharging(param.thisObject);
                    updateChargingIconView(param.thisObject, mCharging);
                    updateSettings(param);

                    if (BatteryController != null) {
                        callMethod(BatteryController, "fireBatteryLevelChanged");
                    }
                }
            });
        } catch (Throwable throwable) {
            log(TAG + throwable);
        }

        try {
            findAndHookMethod(BatteryMeterViewClass, "updateColors", int.class, int.class, int.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (BatteryMeterViewParam == null) {
                        BatteryMeterViewParam = param;
                    }

                    if (!CustomBatteryEnabled) return;

                    BatteryDrawable mBatteryDrawable = (BatteryDrawable) getAdditionalInstanceField(param.thisObject, "mBatteryDrawable");
                    if (mBatteryDrawable != null) {
                        mBatteryDrawable.setColors((int) param.args[0], (int) param.args[1], (int) param.args[2]);
                    }

                    ImageView mChargingIconView = ((LinearLayout) param.thisObject).findViewWithTag(ICONIFY_CHARGING_ICON_TAG);
                    if (mChargingIconView != null) {
                        mChargingIconView.setImageTintList(ColorStateList.valueOf((int) param.args[2]));
                    }
                }
            });
        } catch (Throwable throwable) {
            log(TAG + throwable);
        }

        try {
            Class<?> ShadeHeaderControllerClass = findClassIfExists(SYSTEMUI_PACKAGE + ".shade.ShadeHeaderController", lpparam.classLoader);
            if (ShadeHeaderControllerClass == null)
                ShadeHeaderControllerClass = findClass(SYSTEMUI_PACKAGE + ".shade.LargeScreenShadeHeaderController", lpparam.classLoader);

            hookAllMethods(ShadeHeaderControllerClass, "onInit", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    try {
                        Object configurationControllerListener = getObjectField(param.thisObject, "configurationControllerListener");

                        hookAllMethods(configurationControllerListener.getClass(), "onConfigChanged", new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam methodHookParam) {
                                if (!CustomBatteryEnabled) return;

                                updateBatteryResources(param);
                            }
                        });

                        if (!CustomBatteryEnabled) return;

                        updateBatteryResources(param);
                    } catch (Throwable throwable) {
                        log(TAG + throwable);
                    }
                }
            });
        } catch (Throwable ignored) {
        }

        try {
            if (CustomBatteryEnabled) {
                hookAllMethods(BatteryMeterViewClass, "scaleBatteryMeterViews", new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam methodHookParam) {
                        refreshBatteryIcons();
                        return null;
                    }
                });
            }
        } catch (Throwable ignored) {
        }

        try {
            hookAllMethods(BatteryMeterViewClass, "onBatteryLevelChanged", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (BatteryMeterViewParam == null) {
                        BatteryMeterViewParam = param;
                    }

                    mIsCharging = (boolean) param.args[1];
                }
            });
        } catch (Throwable throwable) {
            log(TAG + throwable);
        }

        try {
            hookAllMethods(BatteryMeterViewClass, "setPercentShowMode", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (BatteryMeterViewParam == null) {
                        BatteryMeterViewParam = param;
                    }

                    if ((CustomBatteryEnabled || DefaultLandscapeBatteryEnabled) && mShowPercentInside) {
                        param.setResult(2);
                    }
                }
            });

            hookAllMethods(BatteryMeterViewClass, "updateShowPercent", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (BatteryMeterViewParam == null) {
                        BatteryMeterViewParam = param;
                    }

                    TextView mBatteryPercentView = (TextView) getObjectField(param.thisObject, "mBatteryPercentView");
                    if (mBatteryPercentView != null) {
                        mBatteryPercentView.setVisibility(mShowPercentInside ? View.GONE : View.VISIBLE);
                    }
                }
            });
        } catch (Throwable throwable) {
            log(TAG + throwable);
        }

        removeBatteryMeterViewMethods(BatteryMeterViewClass);
        setDefaultBatteryDimens();
    }

    private void refreshBatteryData(int mLevel, boolean mIsCharging, boolean mPowerSave) {
        for (View view : batteryViews) {
            try {
                view.post(() -> {
                    BatteryDrawable mBatteryDrawable = (BatteryDrawable) getAdditionalInstanceField(view, "mBatteryDrawable");
                    if (mBatteryDrawable != null) {
                        mBatteryDrawable.setBatteryLevel(mLevel);
                        mBatteryDrawable.setChargingEnabled(mIsCharging);
                        mBatteryDrawable.setPowerSavingEnabled(mPowerSave);
                        updateCustomizeBatteryDrawable(mBatteryDrawable);
                    }

                    TextView mBatteryPercentView = (TextView) getObjectField(view, "mBatteryPercentView");
                    if (mBatteryPercentView != null) {
                        mBatteryPercentView.setVisibility(mShowPercentInside ? View.GONE : View.VISIBLE);
                    }

                    scaleBatteryMeterViews(view);
                    updateChargingIconView(view, mIsCharging);
                });
            } catch (Throwable ignored) {
            }
        }
    }

    private void updateBatteryResources(XC_MethodHook.MethodHookParam param) {
        try {
            View header = (View) getObjectField(param.thisObject, "header");
            int textColorPrimary = SettingsLibUtils.getColorAttrDefaultColor(header.getContext(), android.R.attr.textColorPrimary);
            int textColorSecondary = SettingsLibUtils.getColorAttrDefaultColor(header.getContext(), android.R.attr.textColorSecondary);
            LinearLayout batteryIcon = (LinearLayout) getObjectField(param.thisObject, "batteryIcon");

            if (getObjectField(param.thisObject, "iconManager") != null) {
                callMethod(getObjectField(param.thisObject, "iconManager"), "setTint", textColorPrimary);
            }
            callMethod(batteryIcon, "updateColors", textColorPrimary, textColorSecondary, textColorPrimary);
            scaleBatteryMeterViews(batteryIcon);
        } catch (Throwable throwable) {
            log(TAG + throwable);
        }
    }

    private void refreshBatteryIcons() {
        for (View view : batteryViews) {
            ImageView mBatteryIconView = (ImageView) getObjectField(view, "mBatteryIconView");
            if (mBatteryIconView != null) {
                updateBatteryRotation(mBatteryIconView);
                updateFlipper(mBatteryIconView.getParent());
            }

            TextView mBatteryPercentView = (TextView) getObjectField(view, "mBatteryPercentView");
            if (mBatteryPercentView != null) {
                mBatteryPercentView.setVisibility(mShowPercentInside ? View.GONE : View.VISIBLE);
            }

            if (CustomBatteryEnabled) {
                scaleBatteryMeterViews(mBatteryIconView);

                try {
                    BatteryDrawable mBatteryDrawable = (BatteryDrawable) getAdditionalInstanceField(view, "mBatteryDrawable");
                    mBatteryDrawable.setShowPercentEnabled(mShowPercentInside);
                    mBatteryDrawable.setAlpha(Math.round(BatteryIconOpacity * 2.55f));
                    updateCustomizeBatteryDrawable(mBatteryDrawable);
                } catch (Throwable ignored) {
                }
            }

            boolean mCharging = isBatteryCharging(view);
            updateChargingIconView(view, mCharging);
        }
    }

    private boolean isBatteryCharging(Object thisObject) {
        boolean mCharging = mIsCharging;
        boolean mIsIncompatibleCharging = false;

        try {
            mCharging = (boolean) getObjectField(thisObject, "mCharging");
        } catch (Throwable ignored) {
            try {
                mIsIncompatibleCharging = (boolean) getObjectField(thisObject, "mIsIncompatibleCharging");
            } catch (Throwable throwable) {
                log(TAG + throwable);
            }
        }
        return mCharging && !mIsIncompatibleCharging;
    }

    public static void scaleBatteryMeterViews(@NonNull Object thisObject) {
        ImageView mBatteryIconView = (ImageView) getObjectField(thisObject, "mBatteryIconView");
        scaleBatteryMeterViews(mBatteryIconView);
    }

    public static void scaleBatteryMeterViews(@Nullable ImageView mBatteryIconView) {
        if (mBatteryIconView == null) {
            return;
        }

        try {
            Context context = mBatteryIconView.getContext();
            Resources res = context.getResources();

            TypedValue typedValue = new TypedValue();

            res.getValue(res.getIdentifier("status_bar_icon_scale_factor", "dimen", context.getPackageName()), typedValue, true);
            float iconScaleFactor = typedValue.getFloat();

            int batteryWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mBatteryScaleWidth, mBatteryIconView.getContext().getResources().getDisplayMetrics());
            int batteryHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mBatteryScaleHeight, mBatteryIconView.getContext().getResources().getDisplayMetrics());

            LinearLayout.LayoutParams scaledLayoutParams = (LinearLayout.LayoutParams) mBatteryIconView.getLayoutParams();
            scaledLayoutParams.width = (int) (batteryWidth * iconScaleFactor);
            scaledLayoutParams.height = (int) (batteryHeight * iconScaleFactor);
            if (mBatteryCustomDimension) {
                scaledLayoutParams.setMargins(mBatteryMarginLeft, mBatteryMarginTop, mBatteryMarginRight, mBatteryMarginBottom);
            } else {
                scaledLayoutParams.setMargins(0, 0, 0, context.getResources().getDimensionPixelOffset(context.getResources().getIdentifier("battery_margin_bottom", "dimen", context.getPackageName())));
            }

            mBatteryIconView.setLayoutParams(scaledLayoutParams);
        } catch (Throwable throwable) {
            log(TAG + throwable);
        }
    }

    private ImageView initBatteryIfNull(XC_MethodHook.MethodHookParam param, ImageView mBatteryIconView) {
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

            TypedValue typedValue = new TypedValue();

            mContext.getResources().getValue(mContext.getResources().getIdentifier("status_bar_icon_scale_factor", "dimen", mContext.getPackageName()), typedValue, true);
            float iconScaleFactor = typedValue.getFloat();

            int batteryWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mBatteryScaleWidth, mBatteryIconView.getContext().getResources().getDisplayMetrics());
            int batteryHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mBatteryScaleHeight, mBatteryIconView.getContext().getResources().getDisplayMetrics());

            final ViewGroup.MarginLayoutParams mlp = new ViewGroup.MarginLayoutParams((int) (batteryWidth * iconScaleFactor), (int) (batteryHeight * iconScaleFactor));
            mlp.setMargins(0, 0, 0, mContext.getResources().getDimensionPixelOffset(mContext.getResources().getIdentifier("battery_margin_bottom", "dimen", mContext.getPackageName())));
            setObjectField(param.thisObject, "mBatteryIconView", mBatteryIconView);
            callMethod(param.thisObject, "addView", mBatteryIconView, mlp);
        }

        return mBatteryIconView;
    }

    private BatteryDrawable getNewBatteryDrawable(Context context) {
        BatteryDrawable mBatteryDrawable = switch (BatteryStyle) {
            case BATTERY_STYLE_CUSTOM_RLANDSCAPE -> new RLandscapeBattery(context, frameColor);
            case BATTERY_STYLE_CUSTOM_LANDSCAPE -> new LandscapeBattery(context, frameColor);
            case BATTERY_STYLE_PORTRAIT_CAPSULE -> new PortraitBatteryCapsule(context, frameColor);
            case BATTERY_STYLE_PORTRAIT_LORN -> new PortraitBatteryLorn(context, frameColor);
            case BATTERY_STYLE_PORTRAIT_MX -> new PortraitBatteryMx(context, frameColor);
            case BATTERY_STYLE_PORTRAIT_AIROO -> new PortraitBatteryAiroo(context, frameColor);
            case BATTERY_STYLE_RLANDSCAPE_STYLE_A ->
                    new RLandscapeBatteryStyleA(context, frameColor);
            case BATTERY_STYLE_LANDSCAPE_STYLE_A -> new LandscapeBatteryStyleA(context, frameColor);
            case BATTERY_STYLE_RLANDSCAPE_STYLE_B ->
                    new RLandscapeBatteryStyleB(context, frameColor);
            case BATTERY_STYLE_LANDSCAPE_STYLE_B -> new LandscapeBatteryStyleB(context, frameColor);
            case BATTERY_STYLE_LANDSCAPE_IOS_15 -> new LandscapeBatteryiOS15(context, frameColor);
            case BATTERY_STYLE_LANDSCAPE_IOS_16 -> new LandscapeBatteryiOS16(context, frameColor);
            case BATTERY_STYLE_PORTRAIT_ORIGAMI -> new PortraitBatteryOrigami(context, frameColor);
            case BATTERY_STYLE_LANDSCAPE_SMILEY -> new LandscapeBatterySmiley(context, frameColor);
            case BATTERY_STYLE_LANDSCAPE_MIUI_PILL ->
                    new LandscapeBatteryMIUIPill(context, frameColor);
            case BATTERY_STYLE_LANDSCAPE_COLOROS ->
                    new LandscapeBatteryColorOS(context, frameColor);
            case BATTERY_STYLE_RLANDSCAPE_COLOROS ->
                    new RLandscapeBatteryColorOS(context, frameColor);
            case BATTERY_STYLE_LANDSCAPE_BATTERYA -> new LandscapeBatteryA(context, frameColor);
            case BATTERY_STYLE_LANDSCAPE_BATTERYB -> new LandscapeBatteryB(context, frameColor);
            case BATTERY_STYLE_LANDSCAPE_BATTERYC -> new LandscapeBatteryC(context, frameColor);
            case BATTERY_STYLE_LANDSCAPE_BATTERYD -> new LandscapeBatteryD(context, frameColor);
            case BATTERY_STYLE_LANDSCAPE_BATTERYE -> new LandscapeBatteryE(context, frameColor);
            case BATTERY_STYLE_LANDSCAPE_BATTERYF -> new LandscapeBatteryF(context, frameColor);
            case BATTERY_STYLE_LANDSCAPE_BATTERYG -> new LandscapeBatteryG(context, frameColor);
            case BATTERY_STYLE_LANDSCAPE_BATTERYH -> new LandscapeBatteryH(context, frameColor);
            case BATTERY_STYLE_LANDSCAPE_BATTERYI -> new LandscapeBatteryI(context, frameColor);
            case BATTERY_STYLE_LANDSCAPE_BATTERYJ -> new LandscapeBatteryJ(context, frameColor);
            case BATTERY_STYLE_LANDSCAPE_BATTERYK -> new LandscapeBatteryK(context, frameColor);
            case BATTERY_STYLE_LANDSCAPE_BATTERYL -> new LandscapeBatteryL(context, frameColor);
            case BATTERY_STYLE_LANDSCAPE_BATTERYM -> new LandscapeBatteryM(context, frameColor);
            case BATTERY_STYLE_LANDSCAPE_BATTERYN -> new LandscapeBatteryN(context, frameColor);
            case BATTERY_STYLE_LANDSCAPE_BATTERYO -> new LandscapeBatteryO(context, frameColor);
            default -> null;
        };

        if (mBatteryDrawable != null) {
            mBatteryDrawable.setShowPercentEnabled(mShowPercentInside);
            mBatteryDrawable.setAlpha(Math.round(BatteryIconOpacity * 2.55f));
        }

        return mBatteryDrawable;
    }

    private void setDefaultBatteryDimens() {
        XC_InitPackageResources.InitPackageResourcesParam ourResparam = resparams.get(SYSTEMUI_PACKAGE);
        if (ourResparam == null) return;

        if (DefaultLandscapeBatteryEnabled) {
            ourResparam.res.setReplacement(SYSTEMUI_PACKAGE, "dimen", "status_bar_battery_icon_width", new XResources.DimensionReplacement(mBatteryScaleWidth, TypedValue.COMPLEX_UNIT_DIP));
            ourResparam.res.setReplacement(SYSTEMUI_PACKAGE, "dimen", "status_bar_battery_icon_height", new XResources.DimensionReplacement(mBatteryScaleHeight, TypedValue.COMPLEX_UNIT_DIP));
            ourResparam.res.setReplacement(SYSTEMUI_PACKAGE, "dimen", "signal_cluster_battery_padding", new XResources.DimensionReplacement(4, TypedValue.COMPLEX_UNIT_DIP));
        } else if (CustomBatteryEnabled) {
            ourResparam.res.setReplacement(SYSTEMUI_PACKAGE, "dimen", "signal_cluster_battery_padding", new XResources.DimensionReplacement(3, TypedValue.COMPLEX_UNIT_DIP));
        }
    }

    private void removeBatteryMeterViewMethods(Class<?> BatteryMeterViewClass) {
        if (CustomBatteryEnabled) {
            String[] methodNames = {"updateDrawable", "updateBatteryStyle", "updateSettings", "updateVisibility"};
            XC_MethodReplacement methodReplacement = new XC_MethodReplacement() {
                @Override
                protected Object replaceHookedMethod(MethodHookParam methodHookParam) {
                    return null;
                }
            };

            for (String methodName : methodNames) {
                try {
                    hookAllMethods(BatteryMeterViewClass, methodName, methodReplacement);
                } catch (Throwable ignored) {
                }
            }
        }
    }

    private void updateChargingIconView() {
        for (View view : batteryViews) {
            updateChargingIconView(view, mIsChargingImpl);
        }
    }

    private void updateChargingIconView(Object thisObject) {
        updateChargingIconView(thisObject, mIsChargingImpl);
    }

    private void updateChargingIconView(Object thisObject, boolean mCharging) {
        ImageView mChargingIconView = ((LinearLayout) thisObject).findViewWithTag(ICONIFY_CHARGING_ICON_TAG);

        if (mChargingIconView == null) {
            mChargingIconView = new ImageView(mContext);
            mChargingIconView.setTag(ICONIFY_CHARGING_ICON_TAG);
            ((ViewGroup) thisObject).addView(mChargingIconView, 1);
        }

        Drawable drawable = switch (mChargingIconStyle) {
            case 0 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_bold, mContext.getTheme());
            case 1 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_asus, mContext.getTheme());
            case 2 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_buddy, mContext.getTheme());
            case 3 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_evplug, mContext.getTheme());
            case 4 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_idc, mContext.getTheme());
            case 5 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_ios, mContext.getTheme());
            case 6 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_koplak, mContext.getTheme());
            case 7 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_miui, mContext.getTheme());
            case 8 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_mmk, mContext.getTheme());
            case 9 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_moto, mContext.getTheme());
            case 10 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_nokia, mContext.getTheme());
            case 11 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_plug, mContext.getTheme());
            case 12 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_powercable, mContext.getTheme());
            case 13 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_powercord, mContext.getTheme());
            case 14 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_powerstation, mContext.getTheme());
            case 15 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_realme, mContext.getTheme());
            case 16 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_soak, mContext.getTheme());
            case 17 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_stres, mContext.getTheme());
            case 18 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_strip, mContext.getTheme());
            case 19 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_usbcable, mContext.getTheme());
            case 20 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_xiaomi, mContext.getTheme());
            default -> null;
        };

        if (drawable != null && drawable != mChargingIconView.getDrawable()) {
            mChargingIconView.setImageDrawable(drawable);
        }

        int left = ViewHelper.dp2px(mContext, mChargingIconML);
        int right = ViewHelper.dp2px(mContext, mChargingIconMR);
        int size = ViewHelper.dp2px(mContext, mChargingIconWH);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
        lp.setMargins(left, 0, right, mContext.getResources().getDimensionPixelSize(mContext.getResources().getIdentifier("battery_margin_bottom", "dimen", mContext.getPackageName())));
        mChargingIconView.setLayoutParams(lp);

        mChargingIconView.setVisibility(mCharging && mChargingIconSwitch ? View.VISIBLE : View.GONE);
    }

    private void updateSettings(XC_MethodHook.MethodHookParam param) {
        updateCustomizeBatteryDrawable(param.thisObject);
        updateChargingIconView(param.thisObject);
        updateBatteryRotation(param.thisObject);
        updateFlipper(param.thisObject);
        updateChargingIconView();
    }

    private void updateFlipper(Object thisObject) {
        LinearLayout batteryView = (LinearLayout) thisObject;
        batteryView.setOrientation(LinearLayout.HORIZONTAL);
        batteryView.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        batteryView.setLayoutDirection(mSwapPercentage ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);
    }

    private void updateBatteryRotation(Object thisObject) {
        View mBatteryIconView = (View) getObjectField(thisObject, "mBatteryIconView");
        updateBatteryRotation(mBatteryIconView);
    }

    private void updateBatteryRotation(View mBatteryIconView) {
        mBatteryIconView.setRotation(!DefaultLandscapeBatteryEnabled && mBatteryLayoutReverse ? 180 : mBatteryRotation);
    }

    private void updateCustomizeBatteryDrawable(Object thisObject) {
        if (!CustomBatteryEnabled) return;

        BatteryDrawable mBatteryDrawable = (BatteryDrawable) getAdditionalInstanceField(thisObject, "mBatteryDrawable");
        updateCustomizeBatteryDrawable(mBatteryDrawable);
    }

    private void updateCustomizeBatteryDrawable(BatteryDrawable mBatteryDrawable) {
        if (!CustomBatteryEnabled) return;

        mBatteryDrawable.customizeBatteryDrawable(
                mBatteryLayoutReverse,
                mScaledPerimeterAlpha,
                mScaledFillAlpha,
                mCustomBlendColor,
                mRainbowFillColor,
                mCustomFillColor,
                mCustomFillGradColor,
                mCustomChargingColor,
                mCustomPowerSaveColor,
                mCustomPowerSaveFillColor,
                mChargingIconSwitch
        );
    }
}