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

import static android.service.quicksettings.Tile.STATE_ACTIVE;
import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.FIX_NOTIFICATION_COLOR;
import static com.drdisagree.iconify.common.Preferences.FIX_QS_TILE_COLOR;
import static com.drdisagree.iconify.common.Preferences.HIDE_QSLABEL_SWITCH;
import static com.drdisagree.iconify.common.Preferences.HIDE_QS_FOOTER_BUTTONS;
import static com.drdisagree.iconify.common.Preferences.HIDE_QS_SILENT_TEXT;
import static com.drdisagree.iconify.common.Preferences.QQS_TOPMARGIN;
import static com.drdisagree.iconify.common.Preferences.QS_TEXT_ALWAYS_WHITE;
import static com.drdisagree.iconify.common.Preferences.QS_TEXT_FOLLOW_ACCENT;
import static com.drdisagree.iconify.common.Preferences.QS_TOPMARGIN;
import static com.drdisagree.iconify.common.Preferences.VERTICAL_QSTILE_SWITCH;
import static com.drdisagree.iconify.config.XPrefs.Xprefs;
import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findClassIfExists;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.core.content.res.ResourcesCompat;

import com.drdisagree.iconify.xposed.ModPack;
import com.drdisagree.iconify.xposed.modules.utils.Helpers;

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
    private boolean fixQsTileColor = true;
    private boolean fixNotificationColor = true;
    private boolean qsTextAlwaysWhite = false;
    private boolean qsTextFollowAccent = false;
    private boolean hideFooterButtons = false;
    private boolean hideSilentText = false;
    private int qqsTopMargin = 100;
    private int qsTopMargin = 100;
    private Object mParam = null;
    private ViewGroup mFooterButtonsContainer = null;
    private ViewTreeObserver.OnDrawListener mFooterButtonsOnDrawListener = null;
    private ViewGroup mSilentTextContainer = null;
    private ViewTreeObserver.OnDrawListener mSilentTextOnDrawListener = null;

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

        fixQsTileColor = Build.VERSION.SDK_INT >= 34 &&
                Xprefs.getBoolean(FIX_QS_TILE_COLOR, true);
        fixNotificationColor = Build.VERSION.SDK_INT >= 34 &&
                Xprefs.getBoolean(FIX_NOTIFICATION_COLOR, true);

        qsTextAlwaysWhite = Xprefs.getBoolean(QS_TEXT_ALWAYS_WHITE, false);
        qsTextFollowAccent = Xprefs.getBoolean(QS_TEXT_FOLLOW_ACCENT, false);

        hideSilentText = Xprefs.getBoolean(HIDE_QS_SILENT_TEXT, false);
        hideFooterButtons = Xprefs.getBoolean(HIDE_QS_FOOTER_BUTTONS, false);

        triggerQsElementVisibility();
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        setVerticalTiles(loadPackageParam);
        setQsMargin(loadPackageParam);
        fixQsTileAndLabelColorA14(loadPackageParam);
        fixNotificationColorA14(loadPackageParam);
        manageQsElementVisibility(loadPackageParam);
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
                                param.setResult((int) (qqsTopMargin * mContext.getResources().getDisplayMetrics().density));
                            }
                        } catch (Throwable ignored) {
                        }
                    }
                }

                if (qsTopMarginEnabled) {
                    String[] qsHeaderResNames = {
                            "qs_panel_padding_top",
                            "qs_panel_padding_top_combined_headers",
                            "qs_header_height"
                    };

                    for (String resName : qsHeaderResNames) {
                        try {
                            int resId = mContext.getResources()
                                    .getIdentifier(resName, "dimen", mContext.getPackageName());
                            if (param.args[0].equals(resId)) {
                                param.setResult((int) (qsTopMargin * mContext.getResources().getDisplayMetrics().density));
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

    private void fixQsTileAndLabelColorA14(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        try {
            Class<?> QSTileViewImplClass = findClass(SYSTEMUI_PACKAGE + ".qs.tileimpl.QSTileViewImpl", loadPackageParam.classLoader);

            XC_MethodHook removeQsTileTint = new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (fixQsTileColor && Build.VERSION.SDK_INT >= 34) {
                        try {
                            setObjectField(param.thisObject, "colorActive", Color.WHITE);
                            setObjectField(param.thisObject, "colorInactive", Color.TRANSPARENT);
                            setObjectField(param.thisObject, "colorUnavailable", Color.TRANSPARENT);
                        } catch (Throwable throwable) {
                            log(TAG + throwable);
                        }
                    }
                }
            };

            hookAllConstructors(QSTileViewImplClass, removeQsTileTint);
            hookAllMethods(QSTileViewImplClass, "updateResources", removeQsTileTint);

            hookAllConstructors(QSTileViewImplClass, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (!qsTextAlwaysWhite && !qsTextFollowAccent) return;

                    @ColorInt int color = getQsIconLabelColor();
                    @ColorInt int colorAlpha = (color & 0xFFFFFF) | (Math.round(Color.alpha(color) * 0.8f) << 24);

                    setObjectField(param.thisObject, "colorLabelActive", color);
                    setObjectField(param.thisObject, "colorSecondaryLabelActive", colorAlpha);
                }
            });

            hookAllMethods(QSTileViewImplClass, "getLabelColorForState", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (isQsIconLabelStateActive(param, 0)) {
                        param.setResult(getQsIconLabelColor());
                    }
                }
            });

            hookAllMethods(QSTileViewImplClass, "getSecondaryLabelColorForState", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (isQsIconLabelStateActive(param, 0)) {
                        @ColorInt int color = getQsIconLabelColor();
                        @ColorInt int colorAlpha = (color & 0xFFFFFF) | (Math.round(Color.alpha(color) * 0.8f) << 24);
                        param.setResult(colorAlpha);
                    }
                }
            });
        } catch (Throwable throwable) {
            log(TAG + throwable);
        }

        try {
            Class<?> QSIconViewImplClass = findClass(SYSTEMUI_PACKAGE + ".qs.tileimpl.QSIconViewImpl", loadPackageParam.classLoader);

            hookAllMethods(QSIconViewImplClass, "getIconColorForState", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (isQsIconLabelStateActive(param, 1)) {
                        param.setResult(getQsIconLabelColor());
                    }
                }
            });
            try {
                hookAllMethods(QSIconViewImplClass, "updateIcon", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        if (isQsIconLabelStateActive(param, 1)) {
                            ImageView mIcon = (ImageView) param.args[0];
                            mIcon.setImageTintList(ColorStateList.valueOf(getQsIconLabelColor()));
                        }
                    }
                });
            } catch (Throwable ignored) {
            }
        } catch (Throwable throwable) {
            log(TAG + throwable);
        }

        try {
            Class<?> QSContainerImplClass = findClass(SYSTEMUI_PACKAGE + ".qs.QSContainerImpl", loadPackageParam.classLoader);

            hookAllMethods(QSContainerImplClass, "updateResources", new XC_MethodHook() {
                @SuppressLint("DiscouragedApi")
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (!qsTextAlwaysWhite && !qsTextFollowAccent) return;

                    try {
                        Resources res = mContext.getResources();
                        ViewGroup view = ((ViewGroup) param.thisObject).findViewById(res.getIdentifier("qs_footer_actions", "id", mContext.getPackageName()));
                        @ColorInt int color = getQsIconLabelColor();

                        try {
                            ViewGroup pm_button_container = view.findViewById(res.getIdentifier("pm_lite", "id", mContext.getPackageName()));
                            ((ImageView) pm_button_container.getChildAt(0)).setColorFilter(color, PorterDuff.Mode.SRC_IN);
                        } catch (Throwable ignored) {
                            ImageView pm_button = view.findViewById(res.getIdentifier("pm_lite", "id", mContext.getPackageName()));
                            pm_button.setImageTintList(ColorStateList.valueOf(color));
                        }
                    } catch (Throwable ignored) {
                    }
                }
            });
        } catch (Throwable ignored) {
        }

        try { // Compose implementation of QS Footer actions
            Class<?> FooterActionsButtonViewModelClass = findClass(SYSTEMUI_PACKAGE + ".qs.footer.ui.viewmodel.FooterActionsButtonViewModel", loadPackageParam.classLoader);

            hookAllConstructors(FooterActionsButtonViewModelClass, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (!qsTextAlwaysWhite && !qsTextFollowAccent) return;

                    if (mContext.getResources().getResourceName((Integer) param.args[0]).split("/")[1].equals("pm_lite")) {
                        param.args[2] = getQsIconLabelColor();
                    }
                }
            });
        } catch (Throwable ignored) {
        }

        // Auto brightness icon color
        Class<?> BrightnessControllerClass = findClass(SYSTEMUI_PACKAGE + ".settings.brightness.BrightnessController", loadPackageParam.classLoader);
        Class<?> BrightnessMirrorControllerClass = findClass(SYSTEMUI_PACKAGE + ".statusbar.policy.BrightnessMirrorController", loadPackageParam.classLoader);
        Class<?> BrightnessSliderControllerClass = findClassIfExists(SYSTEMUI_PACKAGE + ".settings.brightness.BrightnessSliderController", loadPackageParam.classLoader);

        hookAllMethods(BrightnessControllerClass, "updateIcon", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (!qsTextAlwaysWhite && !qsTextFollowAccent) return;

                @ColorInt int color = getQsIconLabelColor();

                try {
                    ((ImageView) getObjectField(param.thisObject, "mIcon")).setImageTintList(ColorStateList.valueOf(color));
                } catch (Throwable throwable) {
                    log(TAG + throwable);
                }
            }
        });

        if (BrightnessSliderControllerClass != null) {
            hookAllConstructors(BrightnessSliderControllerClass, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (!qsTextAlwaysWhite && !qsTextFollowAccent) return;

                    @ColorInt int color = getQsIconLabelColor();

                    try {
                        ((ImageView) getObjectField(param.thisObject, "mIcon")).setImageTintList(ColorStateList.valueOf(color));
                    } catch (Throwable throwable) {
                        try {
                            ((ImageView) getObjectField(param.thisObject, "mIconView")).setImageTintList(ColorStateList.valueOf(color));
                        } catch (Throwable ignored) {
                        }
                    }
                }
            });
        } else {
            log(TAG + "Not a crash... BrightnessSliderController class not found.");
        }

        hookAllMethods(BrightnessMirrorControllerClass, "updateIcon", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (!qsTextAlwaysWhite && !qsTextFollowAccent) return;

                @ColorInt int color = getQsIconLabelColor();

                try {
                    ((ImageView) getObjectField(param.thisObject, "mIcon")).setImageTintList(ColorStateList.valueOf(color));
                } catch (Throwable throwable) {
                    log(TAG + throwable);
                }
            }
        });
    }

    private void fixNotificationColorA14(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        if (Build.VERSION.SDK_INT < 34) return;

        try {
            Class<?> ActivatableNotificationViewClass = findClass(SYSTEMUI_PACKAGE + ".statusbar.notification.row.ActivatableNotificationView", loadPackageParam.classLoader);
            Class<?> NotificationBackgroundViewClass = findClass(SYSTEMUI_PACKAGE + ".statusbar.notification.row.NotificationBackgroundView", loadPackageParam.classLoader);
            Class<?> FooterViewClass = findClass(SYSTEMUI_PACKAGE + ".statusbar.notification.row.FooterView", loadPackageParam.classLoader);

            XC_MethodHook removeNotificationTint = new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (!fixNotificationColor) return;

                    View notificationBackgroundView = (View) getObjectField(param.thisObject, "mBackgroundNormal");

                    try {
                        setObjectField(param.thisObject, "mCurrentBackgroundTint", (int) param.args[0]);
                    } catch (Throwable ignored) {
                    }

                    callMethod(getObjectField(notificationBackgroundView, "mBackground"), "clearColorFilter");
                    setObjectField(notificationBackgroundView, "mTintColor", 0);
                    notificationBackgroundView.invalidate();
                }
            };

            hookAllMethods(ActivatableNotificationViewClass, "setBackgroundTintColor", removeNotificationTint);
            hookAllMethods(ActivatableNotificationViewClass, "updateBackgroundColors", removeNotificationTint);
            hookAllMethods(ActivatableNotificationViewClass, "updateBackgroundTint", removeNotificationTint);

            XC_MethodHook replaceTintColor = new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (!fixNotificationColor) return;

                    setObjectField(param.thisObject, "mTintColor", 0);
                }
            };

            try {
                hookAllMethods(NotificationBackgroundViewClass, "setCustomBackground$1", replaceTintColor);
            } catch (Throwable ignored) {
            }

            try {
                hookAllMethods(NotificationBackgroundViewClass, "setCustomBackground", replaceTintColor);
            } catch (Throwable ignored) {
            }

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

    private void manageQsElementVisibility(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        try {
            final Class<?> FooterViewClass = findClass(SYSTEMUI_PACKAGE + ".statusbar.notification.row.FooterView", loadPackageParam.classLoader);

            hookAllMethods(FooterViewClass, "onFinishInflate", new XC_MethodHook() {
                @SuppressLint("DiscouragedApi")
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    View view = (View) param.thisObject;
                    Integer resId1 = mContext.getResources().getIdentifier("manage_text", "id", mContext.getPackageName());
                    Integer resId2 = mContext.getResources().getIdentifier("dismiss_text", "id", mContext.getPackageName());

                    if (resId1 != null) {
                        mFooterButtonsContainer = (ViewGroup) view.findViewById(resId1).getParent();
                    } else if (resId2 != null) {
                        mFooterButtonsContainer = (ViewGroup) view.findViewById(resId2).getParent();
                    }

                    triggerQsElementVisibility();
                }
            });
        } catch (Throwable throwable) {
            log(TAG + throwable);
        }

        try {
            final Class<?> SectionHeaderViewClass = findClass(SYSTEMUI_PACKAGE + ".statusbar.notification.stack.SectionHeaderView", loadPackageParam.classLoader);

            hookAllMethods(SectionHeaderViewClass, "onFinishInflate", new XC_MethodHook() {
                @SuppressLint("DiscouragedApi")
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    mSilentTextContainer = (ViewGroup) param.thisObject;

                    triggerQsElementVisibility();
                }
            });
        } catch (Throwable throwable) {
            log(TAG + throwable);
        }
    }

    private boolean isQsIconLabelStateActive(XC_MethodHook.MethodHookParam param, int stateIndex) {
        if (param == null || param.args == null) return false;
        if (!qsTextAlwaysWhite && !qsTextFollowAccent) return false;

        boolean isActiveState = false;

        try {
            isActiveState = (int) getObjectField(param.args[stateIndex], "state") == STATE_ACTIVE;
        } catch (Throwable throwable) {
            try {
                isActiveState = (int) param.args[stateIndex] == STATE_ACTIVE;
            } catch (Throwable throwable1) {
                try {
                    isActiveState = (boolean) param.args[stateIndex];
                } catch (Throwable throwable2) {
                    log(TAG + throwable2);
                }
            }
        }

        return isActiveState;
    }

    private @ColorInt int getQsIconLabelColor() {
        try {
            if (qsTextAlwaysWhite) {
                return Color.WHITE;
            } else if (qsTextFollowAccent) {
                return ResourcesCompat.getColor(
                        mContext.getResources(),
                        Helpers.isPixelVariant() ?
                                android.R.color.holo_green_light :
                                android.R.color.holo_blue_light,
                        mContext.getTheme()
                );
            }
        } catch (Throwable throwable) {
            log(TAG + throwable);
        }
        return Color.WHITE;
    }

    private void triggerQsElementVisibility() {
        if (mFooterButtonsContainer != null) {
            if (mFooterButtonsOnDrawListener == null) {
                mFooterButtonsOnDrawListener = () -> mFooterButtonsContainer.setVisibility(View.INVISIBLE);
            }

            try {
                if (hideFooterButtons) {
                    mFooterButtonsContainer.setVisibility(View.INVISIBLE);
                    mFooterButtonsContainer.getViewTreeObserver().addOnDrawListener(mFooterButtonsOnDrawListener);
                } else {
                    mFooterButtonsContainer.getViewTreeObserver().removeOnDrawListener(mFooterButtonsOnDrawListener);
                    mFooterButtonsContainer.setVisibility(View.VISIBLE);
                }
            } catch (Throwable ignored) {
            }
        }

        if (mSilentTextContainer != null) {
            if (mSilentTextOnDrawListener == null) {
                mSilentTextOnDrawListener = () -> mSilentTextContainer.setVisibility(View.GONE);
            }

            try {
                if (hideSilentText) {
                    mSilentTextContainer.setVisibility(View.GONE);
                    mSilentTextContainer.getViewTreeObserver().addOnDrawListener(mSilentTextOnDrawListener);
                } else {
                    mSilentTextContainer.getViewTreeObserver().removeOnDrawListener(mSilentTextOnDrawListener);
                    mSilentTextContainer.setVisibility(View.VISIBLE);
                }
            } catch (Throwable ignored) {
            }
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
