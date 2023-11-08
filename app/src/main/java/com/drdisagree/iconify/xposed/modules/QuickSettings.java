package com.drdisagree.iconify.xposed.modules;

/* Modified from AOSPMods
 * https://github.com/siavash79/AOSPMods/blob/canary/app/src/main/java/sh/siava/AOSPMods/systemui/QSTileGrid.java
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
import static com.drdisagree.iconify.common.Preferences.FIX_NOTIFICATION_COLOR;
import static com.drdisagree.iconify.common.Preferences.HIDE_QSLABEL_SWITCH;
import static com.drdisagree.iconify.common.Preferences.QQS_TOPMARGIN;
import static com.drdisagree.iconify.common.Preferences.QS_TOPMARGIN;
import static com.drdisagree.iconify.common.Preferences.VERTICAL_QSTILE_SWITCH;
import static com.drdisagree.iconify.config.XPrefs.Xprefs;
import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.drdisagree.iconify.xposed.ModPack;
import com.drdisagree.iconify.xposed.modules.utils.Helpers;

import java.util.Objects;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class QuickSettings extends ModPack {

    private static final String TAG = "Iconify - " + QuickSettings.class.getSimpleName() + ": ";
    private static boolean isVerticalQSTileActive = false;
    private static boolean isHideLabelActive = false;
    private static Float QsTilePrimaryTextSize = null;
    private static Float QsTileSecondaryTextSize = null;
    private static boolean qqsTopMarginEnabled = false;
    private static boolean qsTopMarginEnabled = false;
    private boolean fixNotificationColor = false;
    private int qqsTopMargin = 100;
    private int qsTopMargin = 100;
    private Object mParam = null;

    public QuickSettings(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;

        isVerticalQSTileActive = Xprefs.getBoolean(VERTICAL_QSTILE_SWITCH, false);
        isHideLabelActive = Xprefs.getBoolean(HIDE_QSLABEL_SWITCH, false);

        qqsTopMarginEnabled = Xprefs.getInt(QQS_TOPMARGIN, -1) != -1;
        qsTopMarginEnabled = Xprefs.getInt(QS_TOPMARGIN, -1) != -1;

        qqsTopMargin = Xprefs.getInt(QQS_TOPMARGIN, 100);
        qsTopMargin = Xprefs.getInt(QS_TOPMARGIN, 100);

        fixNotificationColor = Xprefs.getBoolean(FIX_NOTIFICATION_COLOR, false);

        if (Key.length > 0 && (Objects.equals(Key[0], VERTICAL_QSTILE_SWITCH) ||
                Objects.equals(Key[0], HIDE_QSLABEL_SWITCH) ||
                Objects.equals(Key[0], QQS_TOPMARGIN) ||
                Objects.equals(Key[0], QS_TOPMARGIN) ||
                Objects.equals(Key[0], FIX_NOTIFICATION_COLOR))
        ) {
            Helpers.forceReloadUI(mContext);
        }
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        setVerticalTiles(loadPackageParam);
        setQsMargin(loadPackageParam);
        fixNotificationColorA14(loadPackageParam);
    }

    private void setVerticalTiles(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        Class<?> QSTileViewImpl = findClass(SYSTEMUI_PACKAGE + ".qs.tileimpl.QSTileViewImpl", loadPackageParam.classLoader);
        Class<?> FontSizeUtils = findClass(SYSTEMUI_PACKAGE + ".FontSizeUtils", loadPackageParam.classLoader);

        hookAllConstructors(QSTileViewImpl, new XC_MethodHook() {
            @SuppressLint("DiscouragedApi")
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (!isVerticalQSTileActive) return;

                mParam = param.thisObject;

                try {
                    ((LinearLayout) param.thisObject).setGravity(Gravity.CENTER);
                    ((LinearLayout) param.thisObject).setOrientation(LinearLayout.VERTICAL);
                    ((TextView) getObjectField(param.thisObject, "label")).setGravity(Gravity.CENTER_HORIZONTAL);
                    ((TextView) getObjectField(param.thisObject, "secondaryLabel")).setGravity(Gravity.CENTER_HORIZONTAL);
                    ((LinearLayout) getObjectField(param.thisObject, "labelContainer")).setLayoutParams(new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.MATCH_PARENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT));

                    ((View) getObjectField(param.thisObject, "sideView")).setVisibility(View.GONE);
                    ((LinearLayout) param.thisObject).removeView(((LinearLayout) getObjectField(param.thisObject, "labelContainer")));

                    if (!isHideLabelActive) {
                        ((LinearLayout) getObjectField(param.thisObject, "labelContainer")).setGravity(Gravity.CENTER_HORIZONTAL);
                        ((LinearLayout) param.thisObject).addView((LinearLayout) getObjectField(param.thisObject, "labelContainer"));
                    }

                    fixTileLayout(((LinearLayout) param.thisObject), mParam);

                    if (QsTilePrimaryTextSize == null || QsTileSecondaryTextSize == null) {
                        try {
                            callStaticMethod(FontSizeUtils, "updateFontSize", mContext.getResources().getIdentifier("qs_tile_text_size", "dimen", mContext.getPackageName()), getObjectField(param.thisObject, "label"));
                            callStaticMethod(FontSizeUtils, "updateFontSize", mContext.getResources().getIdentifier("qs_tile_text_size", "dimen", mContext.getPackageName()), getObjectField(param.thisObject, "secondaryLabel"));
                        } catch (Throwable ignored) {
                        }

                        TextView PrimaryText = (TextView) getObjectField(param.thisObject, "label");
                        TextView SecondaryText = (TextView) getObjectField(param.thisObject, "secondaryLabel");

                        QsTilePrimaryTextSize = PrimaryText.getTextSize();
                        QsTileSecondaryTextSize = SecondaryText.getTextSize();
                    }
                } catch (Throwable throwable) {
                    log(TAG + throwable);
                }
            }
        });

        hookAllMethods(QSTileViewImpl, "onConfigurationChanged", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) {
                if (!isVerticalQSTileActive) return;

                fixTileLayout(((LinearLayout) param.thisObject), mParam);
            }
        });
    }

    private void setQsMargin(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        hookAllMethods(Resources.class, "getDimensionPixelSize", new XC_MethodHook() {
            @SuppressLint("DiscouragedApi")
            @Override
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) {
                if (qqsTopMarginEnabled) {
                    String[] qqsHeaderResNames = {
                            "qs_header_system_icons_area_height",
                            "qqs_layout_margin_top",
                            "qs_header_row_min_height",
                            "large_screen_shade_header_min_height"
                    };

                    for (String resName : qqsHeaderResNames) {
                        try {
                            int resId = mContext.getResources()
                                    .getIdentifier(resName, "dimen", mContext.getPackageName());
                            if (param.args[0].equals(resId)) {
                                param.setResult(qqsTopMargin);
                            }
                        } catch (Throwable ignored) {
                        }
                    }
                }

                if (qsTopMarginEnabled) {
                    String[] qsHeaderResNames = {
                            "qs_panel_padding_top",
                            "qs_panel_padding_top_combined_headers"
                    };

                    for (String resName : qsHeaderResNames) {
                        try {
                            int resId = mContext.getResources()
                                    .getIdentifier(resName, "dimen", mContext.getPackageName());
                            if (param.args[0].equals(resId)) {
                                param.setResult(qsTopMargin);
                            }
                        } catch (Throwable ignored) {
                        }
                    }
                }
            }
        });

        try {
            final Class<?> QuickStatusBarHeader = findClass(SYSTEMUI_PACKAGE + ".qs.QuickStatusBarHeader", loadPackageParam.classLoader);

            hookAllMethods(QuickStatusBarHeader, "updateResources", new XC_MethodHook() {
                @SuppressLint("DiscouragedApi")
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (!qqsTopMarginEnabled) return;

                    if (Build.VERSION.SDK_INT >= 33) {
                        try {
                            Resources res = mContext.getResources();

                            ViewGroup.MarginLayoutParams qqsLP = (ViewGroup.MarginLayoutParams) callMethod(getObjectField(param.thisObject, "mHeaderQsPanel"), "getLayoutParams");
                            qqsLP.topMargin = mContext.getResources().getDimensionPixelSize(res.getIdentifier("qqs_layout_margin_top", "dimen", mContext.getPackageName()));
                            callMethod(getObjectField(param.thisObject, "mHeaderQsPanel"), "setLayoutParams", qqsLP);
                        } catch (Throwable throwable) {
                            log(TAG + throwable);
                        }
                    }
                }
            });
        } catch (Throwable throwable) {
            log(TAG + throwable);
        }
    }

    private void fixNotificationColorA14(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        if (Build.VERSION.SDK_INT < 34) return;

        try {
            Class<?> ActivatableNotificationViewClass = findClass(SYSTEMUI_PACKAGE + ".statusbar.notification.row.ActivatableNotificationView", loadPackageParam.classLoader);
            Class<?> NotificationBackgroundViewClass = findClass(SYSTEMUI_PACKAGE + ".statusbar.notification.row.NotificationBackgroundView", loadPackageParam.classLoader);
            Class<?> FooterViewClass = findClass(SYSTEMUI_PACKAGE + ".statusbar.notification.row.FooterView", loadPackageParam.classLoader);

            hookAllMethods(ActivatableNotificationViewClass, "setBackgroundTintColor", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (!fixNotificationColor) return;

                    int color = (int) param.args[0];
                    View notificationBackgroundView = (View) getObjectField(param.thisObject, "mBackgroundNormal");

                    setObjectField(param.thisObject, "mCurrentBackgroundTint", color);
                    callMethod(getObjectField(notificationBackgroundView, "mBackground"), "clearColorFilter");
                    setObjectField(notificationBackgroundView, "mTintColor", 0);
                    notificationBackgroundView.invalidate();
                }
            });

            hookAllMethods(NotificationBackgroundViewClass, "setCustomBackground$1", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (!fixNotificationColor) return;

                    setObjectField(param.thisObject, "mTintColor", 0);
                }
            });

            hookAllMethods(FooterViewClass, "updateColors", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (!fixNotificationColor) return;

                    Button mClearAllButton = (Button) getObjectField(param.thisObject, "mClearAllButton");
                    Button mManageButton = (Button) getObjectField(param.thisObject, "mManageButton");

                    mClearAllButton.getBackground().clearColorFilter();
                    mManageButton.getBackground().clearColorFilter();

                    mClearAllButton.invalidate();
                    mManageButton.invalidate();
                }
            });
        } catch (Throwable throwable) {
            log(TAG + throwable);
        }
    }

    private void fixTileLayout(LinearLayout tile, Object param) {
        Resources mRes = mContext.getResources();
        @SuppressLint("DiscouragedApi") int padding = mRes.getDimensionPixelSize(mRes.getIdentifier("qs_tile_padding", "dimen", mContext.getPackageName()));
        tile.setPadding(padding, padding, padding, padding);
        tile.setGravity(Gravity.CENTER);
        tile.setOrientation(LinearLayout.VERTICAL);

        if (!isHideLabelActive) {
            try {
                ((ViewGroup.MarginLayoutParams) ((LinearLayout) getObjectField(tile, "labelContainer")).getLayoutParams()).setMarginStart(0);
                ((ViewGroup.MarginLayoutParams) ((LinearLayout) getObjectField(tile, "labelContainer")).getLayoutParams()).topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, mContext.getResources().getDisplayMetrics());
            } catch (Throwable throwable) {
                log(TAG + throwable);
            }
        }

        if (param != null) {
            ((TextView) getObjectField(param, "label")).setGravity(Gravity.CENTER_HORIZONTAL);
            ((TextView) getObjectField(param, "secondaryLabel")).setGravity(Gravity.CENTER_HORIZONTAL);
        }
    }
}
