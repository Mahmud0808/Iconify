package com.drdisagree.iconify.xposed.mods;

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
import static com.drdisagree.iconify.common.Preferences.HEADER_QQS_TOPMARGIN;
import static com.drdisagree.iconify.common.Preferences.HIDE_QSLABEL_SWITCH;
import static com.drdisagree.iconify.common.Preferences.VERTICAL_QSTILE_SWITCH;
import static com.drdisagree.iconify.config.XPrefs.Xprefs;
import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.drdisagree.iconify.xposed.ModPack;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class QuickSettings extends ModPack {

    private static final String TAG = "Iconify - QuickSettings: ";
    private static final String QuickStatusBarHeaderClass = SYSTEMUI_PACKAGE + ".qs.QuickStatusBarHeader";
    private static final String CLASS_QSTILEVIEWIMPL = SYSTEMUI_PACKAGE + ".qs.tileimpl.QSTileViewImpl";
    private static final String CLASS_FONTSIZEUTILS = SYSTEMUI_PACKAGE + ".FontSizeUtils";
    private static int qqsHeaderSize = -1;
    private static boolean headerSizeFixActive = false;
    private static boolean isVerticalQSTileActive = false;
    private static boolean isHideLabelActive = false;
    private static Float QsTilePrimaryTextSize = null, QsTileSecondaryTextSize = null;
    private Object mParam = null;

    public QuickSettings(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;

        qqsHeaderSize = Xprefs.getInt(HEADER_QQS_TOPMARGIN, -1);
        headerSizeFixActive = qqsHeaderSize != -1;

        isVerticalQSTileActive = Xprefs.getBoolean(VERTICAL_QSTILE_SWITCH, false);
        isHideLabelActive = Xprefs.getBoolean(HIDE_QSLABEL_SWITCH, false);
    }

    @Override
    public boolean listensTo(String packageName) {
        return packageName.equals(SYSTEMUI_PACKAGE);
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!lpparam.packageName.equals(SYSTEMUI_PACKAGE)) return;

        Class<?> QSTileViewImpl = findClass(CLASS_QSTILEVIEWIMPL, lpparam.classLoader);
        Class<?> FontSizeUtils = findClass(CLASS_FONTSIZEUTILS, lpparam.classLoader);

        hookAllMethods(QSTileViewImpl, "onConfigurationChanged", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) {
                if (!isVerticalQSTileActive) return;

                fixTileLayout(((LinearLayout) param.thisObject), mParam);
            }
        });

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

        final Class<?> QuickStatusBarHeader = findClass(QuickStatusBarHeaderClass, lpparam.classLoader);

        try {
            hookAllMethods(QuickStatusBarHeader, "updateResources", new XC_MethodHook() {
                @SuppressLint("DiscouragedApi")
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (!headerSizeFixActive) return;

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
