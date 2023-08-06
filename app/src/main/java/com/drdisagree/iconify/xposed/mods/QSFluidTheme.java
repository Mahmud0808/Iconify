package com.drdisagree.iconify.xposed.mods;

import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.FLUID_NOTIF_TRANSPARENCY;
import static com.drdisagree.iconify.common.Preferences.FLUID_POWERMENU_TRANSPARENCY;
import static com.drdisagree.iconify.common.Preferences.FLUID_QSPANEL;
import static com.drdisagree.iconify.config.XPrefs.Xprefs;
import static com.drdisagree.iconify.xposed.HookRes.resparams;
import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getIntField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.XResources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableWrapper;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.drdisagree.iconify.xposed.HookEntry;
import com.drdisagree.iconify.xposed.ModPack;
import com.drdisagree.iconify.xposed.utils.SettingsLibUtils;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

@SuppressLint("DiscouragedApi")
public class QSFluidTheme extends ModPack {

    public static final String listenPackage = SYSTEMUI_PACKAGE;
    private static final String TAG = "Iconify - QSFluidTheme: ";
    private static final int STATE_UNAVAILABLE = 0;
    private static final int STATE_INACTIVE = 1;
    private static final int STATE_ACTIVE = 2;
    private static final float UNAVAILABLE_ALPHA = 0.2f;
    private static final float ACTIVE_ALPHA = 0.3f;
    private static final float INACTIVE_ALPHA = 0.3f;
    private static boolean fluidQsThemeEnabled = false;
    private static boolean fluidNotifEnabled = false;
    private static boolean fluidPowerMenuEnabled = false;
    private static XC_MethodHook.MethodHookParam customParam = null;
    private boolean wasDark = getIsDark();
    final Integer[] colorInactive = {null};
    final Integer[] colorAccent = {mContext.getResources().getColor(mContext.getResources().getIdentifier("android:color/system_accent1_300", "color", listenPackage), mContext.getTheme())};
    final Integer[] colorActiveAlpha = {Color.argb((int) (ACTIVE_ALPHA * 255), Color.red(colorAccent[0]), Color.green(colorAccent[0]), Color.blue(colorAccent[0]))};
    final Integer[] colorInactiveAlpha = {wasDark ? Color.parseColor("#0FFFFFFF") : Color.parseColor("#59FFFFFF")};
    final Integer[] colorInactiveQsAlpha = colorInactiveAlpha;
    final Integer[] colorUnavailableAlpha = {wasDark ? Color.parseColor("#08FFFFFF") : Color.parseColor("#33FFFFFF")};

    public QSFluidTheme(Context context) {
        super(context);
        if (!listensTo(context.getPackageName())) return;

        wasDark = getIsDark();
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;

        fluidQsThemeEnabled = Xprefs.getBoolean(FLUID_QSPANEL, false);
        fluidNotifEnabled = Xprefs.getBoolean(FLUID_NOTIF_TRANSPARENCY, false);
        fluidPowerMenuEnabled = Xprefs.getBoolean(FLUID_POWERMENU_TRANSPARENCY, false);

        boolean isDark = getIsDark();
        if (isDark != wasDark) {
            wasDark = isDark;
        }

        if (customParam != null) {
            initColors(customParam);
        }
        initResources();
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!lpparam.packageName.equals(listenPackage)) return;

        Class<?> QsPanelClass = findClass(SYSTEMUI_PACKAGE + ".qs.QSPanel", lpparam.classLoader);
        Class<?> QSTileViewImplClass = findClass(SYSTEMUI_PACKAGE + ".qs.tileimpl.QSTileViewImpl", lpparam.classLoader);
        Class<?> QSIconViewImplClass = findClass(SYSTEMUI_PACKAGE + ".qs.tileimpl.QSIconViewImpl", lpparam.classLoader);
        Class<?> FooterViewClass = findClass(SYSTEMUI_PACKAGE + ".statusbar.notification.row.FooterView", lpparam.classLoader);
        Class<?> BrightnessSliderViewClass = findClass(SYSTEMUI_PACKAGE + ".settings.brightness.BrightnessSliderView", lpparam.classLoader);
        Class<?> BrightnessControllerClass = findClass(SYSTEMUI_PACKAGE + ".settings.brightness.BrightnessController", lpparam.classLoader);
        Class<?> BrightnessMirrorControllerClass = findClass(SYSTEMUI_PACKAGE + ".statusbar.policy.BrightnessMirrorController", lpparam.classLoader);
        Class<?> BrightnessSliderControllerClass = findClass(SYSTEMUI_PACKAGE + ".settings.brightness.BrightnessSliderController", lpparam.classLoader);
        Class<?> ActivatableNotificationViewClass = findClass(SYSTEMUI_PACKAGE + ".statusbar.notification.row.ActivatableNotificationView", lpparam.classLoader);
        SettingsLibUtils.init(lpparam.classLoader);

        // Initialize resources and colors
        hookAllMethods(QSTileViewImplClass, "init", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                customParam = param;
                if (!fluidQsThemeEnabled) return;

                initColors(param);
                initResources();
            }
        });

        // QS tile color
        hookAllMethods(QSTileViewImplClass, "getBackgroundColorForState", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                customParam = param;
                if (!fluidQsThemeEnabled) return;

                try {
                    if ((int) param.args[0] == STATE_ACTIVE) {
                        param.setResult(colorActiveAlpha[0]);
                    } else if ((int) param.args[0] == STATE_INACTIVE) {
                        param.setResult(colorInactiveQsAlpha[0]);
                    } else if ((int) param.args[0] == STATE_UNAVAILABLE) {
                        param.setResult(colorUnavailableAlpha[0]);
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
                        param.setResult(colorAccent[0]);
                    }
                } catch (Throwable throwable) {
                    log(TAG + throwable);
                }
            }
        });

        hookAllMethods(QSIconViewImplClass, "updateIcon", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (!fluidQsThemeEnabled) return;

                try {
                    if (param.args[0] instanceof ImageView && getIntField(param.args[1], "state") == STATE_ACTIVE) {
                        ((ImageView) param.args[0]).setImageTintList(ColorStateList.valueOf(colorAccent[0]));
                    }
                } catch (Throwable throwable) {
                    log(TAG + throwable);
                }
            }
        });

        hookAllMethods(QSIconViewImplClass, "setIcon", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (!fluidQsThemeEnabled) return;

                try {
                    if (param.args[0] instanceof ImageView && getIntField(param.args[1], "state") == STATE_ACTIVE) {
                        setObjectField(param.thisObject, "mTint", colorAccent[0]);
                    }
                } catch (Throwable throwable) {
                    log(TAG + throwable);
                }
            }
        });

        // Brightness slider and auto brightness color
        hookAllMethods(BrightnessSliderViewClass, "onFinishInflate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (!fluidQsThemeEnabled) return;

                try {
                    SeekBar mSlider = (SeekBar) getObjectField(param.thisObject, "mSlider");
                    LayerDrawable progress = (LayerDrawable) mSlider.getProgressDrawable();

                    GradientDrawable backgroundSlider = (GradientDrawable) progress.findDrawableByLayerId(android.R.id.background);
                    backgroundSlider.setAlpha((int) ((INACTIVE_ALPHA + 0.2f) * 255));

                    DrawableWrapper progressSlider = (DrawableWrapper) progress.findDrawableByLayerId(android.R.id.progress);
                    LayerDrawable actualProgressSlider = (LayerDrawable) progressSlider.getDrawable();
                    actualProgressSlider.setTint(colorAccent[0]);
                    actualProgressSlider.setAlpha((int) (ACTIVE_ALPHA * 255));

                    Drawable mBrightnessIcon = actualProgressSlider.findDrawableByLayerId(mContext.getResources().getIdentifier("slider_icon", "id", mContext.getPackageName()));
                    mBrightnessIcon.setTint(colorAccent[0]);
                    mBrightnessIcon.setAlpha(255);
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
                    ((ImageView) getObjectField(param.thisObject, "mIcon")).setImageTintList(ColorStateList.valueOf(colorAccent[0]));
                    ((ImageView) getObjectField(param.thisObject, "mIcon")).setBackgroundTintList(ColorStateList.valueOf(colorActiveAlpha[0]));
                } catch (Throwable throwable) {
                    log(TAG + throwable);
                }
            }
        });

        hookAllConstructors(BrightnessSliderControllerClass, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (!fluidQsThemeEnabled) return;

                try {
                    ((ImageView) getObjectField(param.thisObject, "mIcon")).setImageTintList(ColorStateList.valueOf(colorAccent[0]));
                    ((ImageView) getObjectField(param.thisObject, "mIcon")).setBackgroundTintList(ColorStateList.valueOf(colorActiveAlpha[0]));
                } catch (Throwable throwable) {
                    try {
                        ((ImageView) getObjectField(param.thisObject, "mIconView")).setImageTintList(ColorStateList.valueOf(colorAccent[0]));
                        ((ImageView) getObjectField(param.thisObject, "mIconView")).setBackgroundTintList(ColorStateList.valueOf(colorActiveAlpha[0]));
                    } catch (Throwable throwable1) {
                        log(TAG + throwable1);
                    }
                }
            }
        });

        hookAllMethods(BrightnessMirrorControllerClass, "updateIcon", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (!fluidQsThemeEnabled) return;

                try {
                    ((ImageView) getObjectField(param.thisObject, "mIcon")).setImageTintList(ColorStateList.valueOf(colorAccent[0]));
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
                customParam = param;
                if (!fluidQsThemeEnabled) return;

                try {
                    if ((int) param.args[0] == STATE_ACTIVE) {
                        param.setResult(colorAccent[0]);
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
                customParam = param;
                if (!fluidQsThemeEnabled) return;

                try {
                    if ((int) param.args[0] == STATE_ACTIVE) {
                        param.setResult(colorAccent[0]);
                    }
                } catch (Throwable throwable) {
                    log(TAG + throwable);
                }
            }
        });

        // For LineageOS based roms
        hookAllMethods(QSTileViewImplClass, "updateResources", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                customParam = param;
                if (!fluidQsThemeEnabled) return;

                initColors(param);
                initResources();

                try {
                    setObjectField(param.thisObject, "colorActive", colorActiveAlpha[0]);
                    setObjectField(param.thisObject, "colorInactive", colorInactiveQsAlpha[0]);
                    setObjectField(param.thisObject, "colorUnavailable", colorUnavailableAlpha[0]);
                    setObjectField(param.thisObject, "colorLabelActive", colorAccent[0]);
                    setObjectField(param.thisObject, "colorSecondaryLabelActive", colorAccent[0]);
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

        // Notification footer buttons
        hookAllMethods(FooterViewClass, "updateColors", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (!fluidQsThemeEnabled || !fluidNotifEnabled) return;

                Button mManageButton = (Button) getObjectField(param.thisObject, "mManageButton");
                Button mClearAllButton = (Button) getObjectField(param.thisObject, "mClearAllButton");

                if (mManageButton != null) {
                    mManageButton.getBackground().setAlpha((int) (INACTIVE_ALPHA * 255));
                }

                if (mClearAllButton != null) {
                    mClearAllButton.getBackground().setAlpha((int) (INACTIVE_ALPHA * 255));
                }
            }
        });
    }

    private boolean getIsDark() {
        return (mContext.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_YES) == Configuration.UI_MODE_NIGHT_YES;
    }

    private void initColors(XC_MethodHook.MethodHookParam param) {
        try {
            colorInactive[0] = (Integer) getObjectField(param.thisObject, "colorInactive");
            colorAccent[0] = mContext.getResources().getColor(mContext.getResources().getIdentifier("android:color/system_accent1_300", "color", mContext.getPackageName()), mContext.getTheme());
            colorActiveAlpha[0] = Color.argb((int) (ACTIVE_ALPHA * 255), Color.red(colorAccent[0]), Color.green(colorAccent[0]), Color.blue(colorAccent[0]));
            colorInactiveAlpha[0] = changeAlpha(colorInactive[0], INACTIVE_ALPHA);
            colorInactiveQsAlpha[0] = changeAlpha(colorInactive[0], INACTIVE_ALPHA + 0.2f);
            colorUnavailableAlpha[0] = changeAlpha(colorInactive[0], UNAVAILABLE_ALPHA);
        } catch (Throwable ignored) {
        }
    }

    private void initResources() {
        // Replace drawables to match QS style
        XC_InitPackageResources.InitPackageResourcesParam ourResparam = resparams.get(SYSTEMUI_PACKAGE);

        if (ourResparam == null || !fluidQsThemeEnabled) return;

        int px2dp2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, mContext.getResources().getDisplayMetrics());
        int px2dp4 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, mContext.getResources().getDisplayMetrics());
        int notifCornerRadius = mContext.getResources().getDimensionPixelSize(mContext.getResources().getIdentifier("notification_corner_radius", "dimen", mContext.getPackageName()));

        try {
            ourResparam.res.setReplacement(mContext.getPackageName(), "drawable", "qs_footer_actions_background", new XResources.DrawableLoader() {
                @Override
                public Drawable newDrawable(XResources res, int id) {
                    return new ColorDrawable(Color.TRANSPARENT);
                }
            });
        } catch (Throwable ignored) {
        }

        try {
            ourResparam.res.setReplacement(mContext.getPackageName(), "drawable", "qs_footer_action_circle", new XResources.DrawableLoader() {
                @Override
                public Drawable newDrawable(XResources res, int id) {
                    GradientDrawable gradientDrawable = new GradientDrawable();
                    gradientDrawable.setShape(GradientDrawable.RECTANGLE);
                    gradientDrawable.setColor(colorInactiveAlpha[0]);
                    gradientDrawable.setCornerRadius(notifCornerRadius);
                    return new InsetDrawable(gradientDrawable, px2dp4, px2dp4, px2dp4, px2dp4);
                }
            });
        } catch (Throwable ignored) {
        }

        try {
            ourResparam.res.setReplacement(mContext.getPackageName(), "drawable", "qs_footer_action_circle_color", new XResources.DrawableLoader() {
                @Override
                public Drawable newDrawable(XResources res, int id) {
                    GradientDrawable gradientDrawable = new GradientDrawable();
                    gradientDrawable.setShape(GradientDrawable.RECTANGLE);
                    gradientDrawable.setColor(colorActiveAlpha[0]);
                    gradientDrawable.setCornerRadius(notifCornerRadius);
                    return new InsetDrawable(gradientDrawable, px2dp4, px2dp4, px2dp4, px2dp4);
                }
            });
        } catch (Throwable ignored) {
        }

        try {
            ourResparam.res.setReplacement(mContext.getPackageName(), "drawable", "qs_footer_action_chip_background", new XResources.DrawableLoader() {
                @Override
                public Drawable newDrawable(XResources res, int id) {
                    GradientDrawable gradientDrawable = new GradientDrawable();
                    gradientDrawable.setShape(GradientDrawable.RECTANGLE);
                    gradientDrawable.setColor(colorInactiveAlpha[0]);
                    gradientDrawable.setCornerRadius(notifCornerRadius);
                    return new InsetDrawable(gradientDrawable, 0, px2dp2, 0, px2dp2);
                }
            });
        } catch (Throwable ignored) {
        }

        try {
            ourResparam.res.setReplacement(mContext.getPackageName(), "drawable", "qs_security_footer_background", new XResources.DrawableLoader() {
                @Override
                public Drawable newDrawable(XResources res, int id) {
                    GradientDrawable gradientDrawable = new GradientDrawable();
                    gradientDrawable.setShape(GradientDrawable.RECTANGLE);
                    gradientDrawable.setColor(colorInactiveAlpha[0]);
                    gradientDrawable.setCornerRadius(notifCornerRadius);
                    return new InsetDrawable(gradientDrawable, 0, px2dp4, 0, px2dp4);
                }
            });
        } catch (Throwable ignored) {
        }

        if (fluidPowerMenuEnabled) {
            try {
                ourResparam.res.setReplacement(mContext.getPackageName(), "drawable", "global_actions_lite_background", new XResources.DrawableLoader() {
                    @Override
                    public Drawable newDrawable(XResources res, int id) {
                        GradientDrawable gradientDrawable = new GradientDrawable();
                        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
                        gradientDrawable.setColor(colorInactiveAlpha[0]);
                        gradientDrawable.setCornerRadius(notifCornerRadius);
                        return gradientDrawable;
                    }
                });
            } catch (Throwable ignored) {
            }

            try {
                ourResparam.res.setReplacement(mContext.getPackageName(), "drawable", "global_actions_lite_button", new XResources.DrawableLoader() {
                    @Override
                    public Drawable newDrawable(XResources res, int id) {
                        GradientDrawable gradientDrawable = new GradientDrawable();
                        gradientDrawable.setShape(GradientDrawable.OVAL);
                        gradientDrawable.setColor(colorInactiveAlpha[0]);
                        return gradientDrawable;
                    }
                });
            } catch (Throwable ignored) {
            }
        }
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

    private static float dpToPx(Context context, int dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName) && !HookEntry.isChildProcess && Build.VERSION.SDK_INT >= 33;
    }
}