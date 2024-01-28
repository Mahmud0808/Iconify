package com.drdisagree.iconify.xposed.modules;

import static android.service.quicksettings.Tile.STATE_ACTIVE;
import static android.service.quicksettings.Tile.STATE_INACTIVE;
import static android.service.quicksettings.Tile.STATE_UNAVAILABLE;
import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.FLUID_NOTIF_TRANSPARENCY;
import static com.drdisagree.iconify.common.Preferences.FLUID_POWERMENU_TRANSPARENCY;
import static com.drdisagree.iconify.common.Preferences.FLUID_QSPANEL;
import static com.drdisagree.iconify.config.XPrefs.Xprefs;
import static com.drdisagree.iconify.xposed.HookRes.modRes;
import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findClassIfExists;
import static de.robv.android.xposed.XposedHelpers.getIntField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableWrapper;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;

import androidx.core.content.res.ResourcesCompat;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.xposed.ModPack;
import com.drdisagree.iconify.xposed.modules.utils.RoundedCornerProgressDrawable;
import com.drdisagree.iconify.xposed.modules.utils.SettingsLibUtils;
import com.drdisagree.iconify.xposed.modules.utils.ViewHelper;
import com.drdisagree.iconify.xposed.utils.SystemUtil;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

@SuppressLint("DiscouragedApi")
public class QSFluidTheme extends ModPack {

    private static final String TAG = "Iconify - " + QSFluidTheme.class.getSimpleName() + ": ";
    private static final float ACTIVE_ALPHA = 0.2f;
    private static final float INACTIVE_ALPHA = ACTIVE_ALPHA + 0.2f;
    private static final float UNAVAILABLE_ALPHA = INACTIVE_ALPHA - 0.1f;
    private static boolean fluidQsThemeEnabled = false;
    private static boolean fluidNotifEnabled = false;
    private static boolean fluidPowerMenuEnabled = false;
    final Integer[] colorActive = {mContext.getResources().getColor(mContext.getResources().getIdentifier("android:color/system_accent1_400", "color", mContext.getPackageName()), mContext.getTheme())};
    final Integer[] colorActiveAlpha = {Color.argb((int) (ACTIVE_ALPHA * 255), Color.red(colorActive[0]), Color.green(colorActive[0]), Color.blue(colorActive[0]))};
    Integer[] colorInactive = {SettingsLibUtils.getColorAttrDefaultColor(mContext, mContext.getResources().getIdentifier("offStateColor", "attr", mContext.getPackageName()))};
    final Integer[] colorInactiveAlpha = {changeAlpha(colorInactive[0], INACTIVE_ALPHA)};
    private boolean wasDark = SystemUtil.isDarkMode();
    private SeekBar mSlider;

    public QSFluidTheme(Context context) {
        super(context);
    }

    @SuppressWarnings("SameParameterValue")
    private static float dpToPx(Context context, int dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;

        fluidQsThemeEnabled = Xprefs.getBoolean(FLUID_QSPANEL, false);
        fluidNotifEnabled = fluidQsThemeEnabled && Xprefs.getBoolean(FLUID_NOTIF_TRANSPARENCY, false);
        fluidPowerMenuEnabled = fluidQsThemeEnabled && Xprefs.getBoolean(FLUID_POWERMENU_TRANSPARENCY, false);

        initResources();
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        Class<?> QsPanelClass = findClass(SYSTEMUI_PACKAGE + ".qs.QSPanel", loadPackageParam.classLoader);
        Class<?> QSTileViewImplClass = findClass(SYSTEMUI_PACKAGE + ".qs.tileimpl.QSTileViewImpl", loadPackageParam.classLoader);
        Class<?> QSIconViewImplClass = findClass(SYSTEMUI_PACKAGE + ".qs.tileimpl.QSIconViewImpl", loadPackageParam.classLoader);
        Class<?> FooterViewClass = findClass(SYSTEMUI_PACKAGE + ".statusbar.notification.row.FooterView", loadPackageParam.classLoader);
        Class<?> CentralSurfacesImplClass = findClassIfExists(SYSTEMUI_PACKAGE + ".statusbar.phone.CentralSurfacesImpl", loadPackageParam.classLoader);
        Class<?> NotificationExpandButtonClass = findClass("com.android.internal.widget.NotificationExpandButton", loadPackageParam.classLoader);
        Class<?> BrightnessSliderViewClass = findClass(SYSTEMUI_PACKAGE + ".settings.brightness.BrightnessSliderView", loadPackageParam.classLoader);
        Class<?> BrightnessControllerClass = findClass(SYSTEMUI_PACKAGE + ".settings.brightness.BrightnessController", loadPackageParam.classLoader);
        Class<?> BrightnessMirrorControllerClass = findClass(SYSTEMUI_PACKAGE + ".statusbar.policy.BrightnessMirrorController", loadPackageParam.classLoader);
        Class<?> BrightnessSliderControllerClass = findClassIfExists(SYSTEMUI_PACKAGE + ".settings.brightness.BrightnessSliderController", loadPackageParam.classLoader);
        Class<?> ActivatableNotificationViewClass = findClass(SYSTEMUI_PACKAGE + ".statusbar.notification.row.ActivatableNotificationView", loadPackageParam.classLoader);

        // Initialize resources and colors
        hookAllMethods(QSTileViewImplClass, "init", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                initResources();
            }
        });

        if (CentralSurfacesImplClass != null) {
            hookAllConstructors(CentralSurfacesImplClass, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    initResources();
                }
            });

            hookAllMethods(CentralSurfacesImplClass, "updateTheme", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    initResources();
                }
            });
        }

        // QS tile color
        hookAllConstructors(QSTileViewImplClass, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                colorInactive[0] = SettingsLibUtils.getColorAttrDefaultColor(mContext, mContext.getResources().getIdentifier("offStateColor", "attr", mContext.getPackageName()));
                colorInactiveAlpha[0] = changeAlpha(colorInactive[0], INACTIVE_ALPHA);
            }
        });

        hookAllMethods(QSTileViewImplClass, "getBackgroundColorForState", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (!fluidQsThemeEnabled) return;

                try {
                    if ((int) param.args[0] == STATE_ACTIVE) {
                        param.setResult(changeAlpha(colorActive[0], ACTIVE_ALPHA));
                    } else {
                        Integer inactiveColor = (Integer) param.getResult();

                        if (inactiveColor != null) {
                            colorInactive[0] = inactiveColor;
                            colorInactiveAlpha[0] = changeAlpha(inactiveColor, INACTIVE_ALPHA);

                            if ((int) param.args[0] == STATE_INACTIVE) {
                                param.setResult(changeAlpha(inactiveColor, INACTIVE_ALPHA));
                            } else if ((int) param.args[0] == STATE_UNAVAILABLE) {
                                param.setResult(changeAlpha(inactiveColor, UNAVAILABLE_ALPHA));
                            }
                        }
                    }
                } catch (Throwable throwable) {
                    log(TAG + throwable);
                }
            }
        });

        // QS icon color
        hookAllMethods(QSIconViewImplClass, "getIconColorForState", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (!fluidQsThemeEnabled) return;

                try {
                    if ((int) getObjectField(param.args[1], "state") == STATE_ACTIVE) {
                        param.setResult(colorActive[0]);
                    }
                } catch (Throwable ignored) {
                }
            }
        });

        hookAllMethods(QSIconViewImplClass, "updateIcon", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (!fluidQsThemeEnabled) return;

                try {
                    if (param.args[0] instanceof ImageView && getIntField(param.args[1], "state") == STATE_ACTIVE) {
                        ((ImageView) param.args[0]).setImageTintList(ColorStateList.valueOf(colorActive[0]));
                    }
                } catch (Throwable ignored) {
                }
            }
        });

        hookAllMethods(QSIconViewImplClass, "setIcon", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (!fluidQsThemeEnabled) return;

                try {
                    if (param.args[0] instanceof ImageView && getIntField(param.args[1], "state") == STATE_ACTIVE) {
                        setObjectField(param.thisObject, "mTint", colorActive[0]);
                    }
                } catch (Throwable ignored) {
                }
            }
        });

        try {
            Class<?> QSContainerImplClass = findClass(SYSTEMUI_PACKAGE + ".qs.QSContainerImpl", loadPackageParam.classLoader);

            hookAllMethods(QSContainerImplClass, "updateResources", new XC_MethodHook() {
                @SuppressLint("DiscouragedApi")
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (!fluidQsThemeEnabled) return;

                    try {
                        Resources res = mContext.getResources();
                        ViewGroup view = ((ViewGroup) param.thisObject).findViewById(res.getIdentifier("qs_footer_actions", "id", mContext.getPackageName()));
                        view.getBackground().setTint(Color.TRANSPARENT);
                        view.setElevation(0);

                        setAlphaTintedDrawables(view, INACTIVE_ALPHA);

                        try {
                            View security_footer = ((ViewGroup) view.findViewById(res.getIdentifier("security_footers_container", "id", mContext.getPackageName()))).getChildAt(0);
                            security_footer.getBackground().setTint(colorInactive[0]);
                            security_footer.getBackground().setAlpha((int) (INACTIVE_ALPHA * 255));
                        } catch (Throwable ignored) {
                        }

                        try {
                            View multi_user_switch = view.findViewById(res.getIdentifier("multi_user_switch", "id", mContext.getPackageName()));
                            multi_user_switch.getBackground().setTint(colorInactive[0]);
                            multi_user_switch.getBackground().setAlpha((int) (INACTIVE_ALPHA * 255));
                        } catch (Throwable ignored) {
                        }

                        try {
                            ViewGroup pm_button_container = view.findViewById(res.getIdentifier("pm_lite", "id", mContext.getPackageName()));
                            pm_button_container.getBackground().setAlpha((int) (ACTIVE_ALPHA * 255));
                            pm_button_container.getBackground().setTint(colorActive[0]);
                            ((ImageView) pm_button_container.getChildAt(0)).setColorFilter(colorActive[0], PorterDuff.Mode.SRC_IN);
                        } catch (Throwable ignored) {
                            ImageView pm_button = view.findViewById(res.getIdentifier("pm_lite", "id", mContext.getPackageName()));
                            pm_button.getBackground().setAlpha((int) (ACTIVE_ALPHA * 255));
                            pm_button.getBackground().setTint(colorActive[0]);
                            pm_button.setImageTintList(ColorStateList.valueOf(colorActive[0]));
                        }
                    } catch (Throwable ignored) {
                    }
                }
            });
        } catch (Throwable ignored) {
        }

        // Brightness slider and auto brightness color
        hookAllMethods(BrightnessSliderViewClass, "onFinishInflate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                mSlider = (SeekBar) getObjectField(param.thisObject, "mSlider");

                try {
                    if (mSlider != null && fluidQsThemeEnabled) {
                        mSlider.setProgressDrawable(createBrightnessDrawable(mContext));

                        LayerDrawable progress = (LayerDrawable) mSlider.getProgressDrawable();
                        DrawableWrapper progressSlider = (DrawableWrapper) progress.findDrawableByLayerId(android.R.id.progress);

                        try {
                            LayerDrawable actualProgressSlider = (LayerDrawable) progressSlider.getDrawable();
                            Drawable mBrightnessIcon = actualProgressSlider.findDrawableByLayerId(mContext.getResources().getIdentifier("slider_icon", "id", mContext.getPackageName()));
                            mBrightnessIcon.setTintList(ColorStateList.valueOf(Color.TRANSPARENT));
                            mBrightnessIcon.setAlpha(0);
                        } catch (Throwable ignored) {
                        }
                    }
                } catch (Throwable throwable) {
                    log(TAG + throwable);
                }
            }
        });

        hookAllMethods(BrightnessControllerClass, "updateIcon", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (!fluidQsThemeEnabled) return;

                try {
                    ((ImageView) getObjectField(param.thisObject, "mIcon")).setImageTintList(ColorStateList.valueOf(colorActive[0]));
                    ((ImageView) getObjectField(param.thisObject, "mIcon")).setBackgroundTintList(ColorStateList.valueOf(colorActiveAlpha[0]));
                } catch (Throwable throwable) {
                    log(TAG + throwable);
                }
            }
        });

        if (BrightnessSliderControllerClass != null) {
            hookAllConstructors(BrightnessSliderControllerClass, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (!fluidQsThemeEnabled) return;

                    try {
                        ((ImageView) getObjectField(param.thisObject, "mIcon")).setImageTintList(ColorStateList.valueOf(colorActive[0]));
                        ((ImageView) getObjectField(param.thisObject, "mIcon")).setBackgroundTintList(ColorStateList.valueOf(colorActiveAlpha[0]));
                    } catch (Throwable throwable) {
                        try {
                            ((ImageView) getObjectField(param.thisObject, "mIconView")).setImageTintList(ColorStateList.valueOf(colorActive[0]));
                            ((ImageView) getObjectField(param.thisObject, "mIconView")).setBackgroundTintList(ColorStateList.valueOf(colorActiveAlpha[0]));
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
                if (!fluidQsThemeEnabled) return;

                try {
                    ((ImageView) getObjectField(param.thisObject, "mIcon")).setImageTintList(ColorStateList.valueOf(colorActive[0]));
                    ((ImageView) getObjectField(param.thisObject, "mIcon")).setBackgroundTintList(ColorStateList.valueOf(colorActiveAlpha[0]));
                } catch (Throwable throwable) {
                    log(TAG + throwable);
                }
            }
        });

        hookAllMethods(BrightnessMirrorControllerClass, "updateResources", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (!fluidQsThemeEnabled) return;

                try {
                    FrameLayout mBrightnessMirror = (FrameLayout) getObjectField(param.thisObject, "mBrightnessMirror");
                    mBrightnessMirror.getBackground().setAlpha((int) (INACTIVE_ALPHA * 255));
                } catch (Throwable throwable) {
                    log(TAG + throwable);
                }
            }
        });

        hookAllMethods(QsPanelClass, "updateResources", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (!fluidQsThemeEnabled) return;

                try {
                    ((View) getObjectField(param.thisObject, "mAutoBrightnessView")).getBackground().setTint(colorActiveAlpha[0]);
                } catch (Throwable ignored) {
                }
            }
        });

        // QS tile primary label color
        hookAllMethods(QSTileViewImplClass, "getLabelColorForState", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (!fluidQsThemeEnabled) return;

                try {
                    if ((int) param.args[0] == STATE_ACTIVE) {
                        param.setResult(colorActive[0]);
                    }
                } catch (Throwable throwable) {
                    log(TAG + throwable);
                }
            }
        });

        // QS tile secondary label color
        hookAllMethods(QSTileViewImplClass, "getSecondaryLabelColorForState", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (!fluidQsThemeEnabled) return;

                try {
                    if ((int) param.args[0] == STATE_ACTIVE) {
                        param.setResult(colorActive[0]);
                    }
                } catch (Throwable throwable) {
                    log(TAG + throwable);
                }
            }
        });

        hookAllConstructors(QSTileViewImplClass, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (!fluidQsThemeEnabled) return;

                colorInactive[0] = changeAlpha((Integer) getObjectField(param.thisObject, "colorInactive"), 1.0f);
                initResources();

                // For LineageOS based roms
                try {
                    setObjectField(param.thisObject, "colorActive", changeAlpha(colorActive[0], ACTIVE_ALPHA));
                    setObjectField(param.thisObject, "colorInactive", changeAlpha((Integer) getObjectField(param.thisObject, "colorInactive"), INACTIVE_ALPHA));
                    setObjectField(param.thisObject, "colorUnavailable", changeAlpha((Integer) getObjectField(param.thisObject, "colorInactive"), UNAVAILABLE_ALPHA));
                    setObjectField(param.thisObject, "colorLabelActive", colorActive[0]);
                    setObjectField(param.thisObject, "colorSecondaryLabelActive", colorActive[0]);
                } catch (Throwable throwable) {
                    log(TAG + throwable);
                }

                try {
                    if (mSlider != null) {
                        mSlider.setProgressDrawable(createBrightnessDrawable(mContext));

                        LayerDrawable progress = (LayerDrawable) mSlider.getProgressDrawable();
                        DrawableWrapper progressSlider = (DrawableWrapper) progress.findDrawableByLayerId(android.R.id.progress);

                        try {
                            LayerDrawable actualProgressSlider = (LayerDrawable) progressSlider.getDrawable();
                            Drawable mBrightnessIcon = actualProgressSlider.findDrawableByLayerId(mContext.getResources().getIdentifier("slider_icon", "id", mContext.getPackageName()));
                            mBrightnessIcon.setTintList(ColorStateList.valueOf(Color.TRANSPARENT));
                            mBrightnessIcon.setAlpha(0);
                        } catch (Throwable ignored) {
                        }
                    }
                } catch (Throwable throwable) {
                    log(TAG + throwable);
                }
            }
        });

        hookAllMethods(QSTileViewImplClass, "updateResources", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (!fluidQsThemeEnabled) return;

                colorInactive[0] = changeAlpha((Integer) getObjectField(param.thisObject, "colorInactive"), 1.0f);
                initResources();

                try {
                    setObjectField(param.thisObject, "colorActive", changeAlpha(colorActive[0], ACTIVE_ALPHA));
                    setObjectField(param.thisObject, "colorInactive", changeAlpha((Integer) getObjectField(param.thisObject, "colorInactive"), INACTIVE_ALPHA));
                    setObjectField(param.thisObject, "colorUnavailable", changeAlpha((Integer) getObjectField(param.thisObject, "colorInactive"), UNAVAILABLE_ALPHA));
                    setObjectField(param.thisObject, "colorLabelActive", colorActive[0]);
                    setObjectField(param.thisObject, "colorSecondaryLabelActive", colorActive[0]);
                } catch (Throwable throwable) {
                    log(TAG + throwable);
                }
            }
        });

        // Notifications
        hookAllMethods(ActivatableNotificationViewClass, "onFinishInflate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (!fluidQsThemeEnabled || !fluidNotifEnabled) return;

                View mBackgroundNormal = (View) getObjectField(param.thisObject, "mBackgroundNormal");

                if (mBackgroundNormal != null) {
                    mBackgroundNormal.setAlpha(INACTIVE_ALPHA);
                }
            }
        });

        // Notification expand/collapse pill
        hookAllMethods(NotificationExpandButtonClass, "onFinishInflate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (!fluidQsThemeEnabled || !fluidNotifEnabled) return;

                View mPillView = (View) getObjectField(param.thisObject, "mPillView");

                if (mPillView != null) {
                    mPillView.getBackground().setAlpha((int) (INACTIVE_ALPHA * 255));
                }
            }
        });

        // Notification footer buttons
        XC_MethodHook updateNotificationFooterButtons = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (!fluidQsThemeEnabled || !fluidNotifEnabled) return;

                Button mManageButton = (Button) getObjectField(param.thisObject, "mManageButton");
                Button mClearAllButton;

                try {
                    mClearAllButton = (Button) getObjectField(param.thisObject, "mClearAllButton");
                } catch (Throwable ignored) {
                    mClearAllButton = (Button) getObjectField(param.thisObject, "mDismissButton");
                }

                if (mManageButton != null) {
                    mManageButton.getBackground().setAlpha((int) (INACTIVE_ALPHA * 255));
                }

                if (mClearAllButton != null) {
                    mClearAllButton.getBackground().setAlpha((int) (INACTIVE_ALPHA * 255));
                }
            }
        };

        hookAllMethods(FooterViewClass, "onFinishInflate", updateNotificationFooterButtons);
        hookAllMethods(FooterViewClass, "updateColors", updateNotificationFooterButtons);

        // Power menu
        try {
            Class<?> GlobalActionsDialogLiteSinglePressActionClass = findClass(SYSTEMUI_PACKAGE + ".globalactions.GlobalActionsDialogLite$SinglePressAction", loadPackageParam.classLoader);
            Class<?> GlobalActionsLayoutLiteClass = findClass(SYSTEMUI_PACKAGE + ".globalactions.GlobalActionsLayoutLite", loadPackageParam.classLoader);

            // Layout background
            hookAllMethods(GlobalActionsLayoutLiteClass, "onLayout", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (!fluidPowerMenuEnabled) return;

                    ((View) param.thisObject).findViewById(android.R.id.list).getBackground().setAlpha((int) (INACTIVE_ALPHA * 255));
                }
            });

            // Button Color
            hookAllMethods(GlobalActionsDialogLiteSinglePressActionClass, "create", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (!fluidPowerMenuEnabled) return;

                    View itemView = (View) param.getResult();
                    ImageView iconView = itemView.findViewById(android.R.id.icon);

                    iconView.getBackground().setAlpha((int) (INACTIVE_ALPHA * 255));
                }
            });
        } catch (Throwable ignored) {
        }

        // Footer button A12
        try {
            if (Build.VERSION.SDK_INT < 33) {
                Class<?> FooterActionsViewClass = findClass(SYSTEMUI_PACKAGE + ".qs.FooterActionsView", loadPackageParam.classLoader);

                XC_MethodHook updateFooterButtons = new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        ViewGroup parent = (ViewGroup) param.thisObject;
                        int childCount = parent.getChildCount();

                        for (int i = 0; i < childCount; i++) {
                            View childView = parent.getChildAt(i);
                            childView.getBackground().setTint(colorInactive[0]);
                            childView.getBackground().setAlpha((int) (INACTIVE_ALPHA * 255));
                        }
                    }
                };

                hookAllMethods(FooterActionsViewClass, "onFinishInflate", updateFooterButtons);
                hookAllMethods(FooterActionsViewClass, "updateResources", updateFooterButtons);
            }
        } catch (Throwable ignored) {
        }
    }

    private void initResources() {
        boolean isDark = SystemUtil.isDarkMode();

        if (isDark != wasDark) {
            wasDark = isDark;
        }

        colorActive[0] = mContext.getResources().getColor(mContext.getResources().getIdentifier("android:color/system_accent1_400", "color", mContext.getPackageName()), mContext.getTheme());
        colorActiveAlpha[0] = Color.argb((int) (ACTIVE_ALPHA * 255), Color.red(colorActive[0]), Color.green(colorActive[0]), Color.blue(colorActive[0]));
        colorInactiveAlpha[0] = changeAlpha(colorInactive[0], INACTIVE_ALPHA);
    }

    private int changeAlpha(int color, float alpha) {
        return changeAlpha(color, (int) (alpha * 255));
    }

    private int changeAlpha(int color, int alpha) {
        alpha = Math.max(0, Math.min(alpha, 255));

        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);

        return Color.argb(alpha, red, green, blue);
    }

    private LayerDrawable createBrightnessDrawable(Context context) {
        Resources res = context.getResources();
        int cornerRadius = context.getResources().getDimensionPixelSize(res.getIdentifier("rounded_slider_corner_radius", "dimen", context.getPackageName()));
        int height = context.getResources().getDimensionPixelSize(res.getIdentifier("rounded_slider_height", "dimen", context.getPackageName()));
        int startPadding = (int) dpToPx(context, 15);
        int endPadding = (int) dpToPx(context, 15);

        // Create the background shape
        float[] radiusF = new float[8];
        for (int i = 0; i < 8; i++) {
            radiusF[i] = cornerRadius;
        }
        ShapeDrawable backgroundShape = new ShapeDrawable(new RoundRectShape(radiusF, null, null));
        backgroundShape.setIntrinsicHeight(height);
        backgroundShape.getPaint().setColor(changeAlpha(colorInactiveAlpha[0], UNAVAILABLE_ALPHA));

        // Create the progress drawable
        RoundedCornerProgressDrawable progressDrawable = null;
        try {
            progressDrawable = new RoundedCornerProgressDrawable(createBrightnessForegroundDrawable(context));
            progressDrawable.setAlpha((int) (ACTIVE_ALPHA * 255));
            progressDrawable.setTint(colorActive[0]);
        } catch (Throwable ignored) {
        }

        // Create the start and end drawables
        Drawable startDrawable = ResourcesCompat.getDrawable(modRes, R.drawable.ic_brightness_low, context.getTheme());
        Drawable endDrawable = ResourcesCompat.getDrawable(modRes, R.drawable.ic_brightness_full, context.getTheme());
        if (startDrawable != null && endDrawable != null) {
            startDrawable.setTint(colorActive[0]);
            endDrawable.setTint(colorActive[0]);
        }

        // Create the layer drawable
        Drawable[] layers = {backgroundShape, progressDrawable, startDrawable, endDrawable};
        LayerDrawable layerDrawable = new LayerDrawable(layers);
        layerDrawable.setId(0, android.R.id.background);
        layerDrawable.setId(1, android.R.id.progress);
        layerDrawable.setLayerGravity(2, Gravity.START | Gravity.CENTER_VERTICAL);
        layerDrawable.setLayerGravity(3, Gravity.END | Gravity.CENTER_VERTICAL);
        layerDrawable.setLayerInsetStart(2, startPadding);
        layerDrawable.setLayerInsetEnd(3, endPadding);

        return layerDrawable;
    }

    private LayerDrawable createBrightnessForegroundDrawable(Context context) {
        Resources res = context.getResources();

        GradientDrawable rectangleDrawable = new GradientDrawable();
        int cornerRadius = context.getResources().getDimensionPixelSize(res.getIdentifier("rounded_slider_corner_radius", "dimen", context.getPackageName()));
        rectangleDrawable.setCornerRadius(cornerRadius);
        rectangleDrawable.setColor(colorActive[0]);

        LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{rectangleDrawable});
        layerDrawable.setLayerGravity(0, Gravity.FILL_HORIZONTAL | Gravity.CENTER);

        int height = ViewHelper.dp2px(context, 48);
        layerDrawable.setLayerSize(0, layerDrawable.getLayerWidth(0), height);

        return layerDrawable;
    }

    public void setAlphaTintedDrawables(View view, float alpha) {
        setAlphaTintedDrawables(view, (int) (alpha * 255));
    }

    private void setAlphaTintedDrawables(View view, int alpha) {
        if (view instanceof ViewGroup viewGroup) {
            int childCount = viewGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = viewGroup.getChildAt(i);
                setAlphaTintedDrawablesRecursively(child, alpha);
            }
        }
    }

    private void setAlphaTintedDrawablesRecursively(View view, int alpha) {
        Drawable backgroundDrawable = view.getBackground();
        if (backgroundDrawable != null) {
            backgroundDrawable.setTint(colorInactive[0]);
            backgroundDrawable.setAlpha(alpha);
        }

        if (view instanceof ViewGroup viewGroup) {
            int childCount = viewGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = viewGroup.getChildAt(i);
                setAlphaTintedDrawablesRecursively(child, alpha);
            }
        }
    }
}