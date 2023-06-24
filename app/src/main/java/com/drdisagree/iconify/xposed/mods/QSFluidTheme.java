package com.drdisagree.iconify.xposed.mods;

import static android.content.Context.CONTEXT_IGNORE_SECURITY;
import static com.drdisagree.iconify.BuildConfig.APPLICATION_ID;
import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.FLUID_NOTIF_TRANSPARENCY;
import static com.drdisagree.iconify.common.Preferences.FLUID_QSPANEL;
import static com.drdisagree.iconify.config.XPrefs.Xprefs;
import static com.drdisagree.iconify.xposed.HookRes.resparams;
import static com.drdisagree.iconify.xposed.utils.SettingsLibUtils.getColorAttr;
import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getIntField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.XResources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableWrapper;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.res.ResourcesCompat;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.xposed.HookEntry;
import com.drdisagree.iconify.xposed.ModPack;
import com.drdisagree.iconify.xposed.utils.RoundedCornerProgressDrawable;
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
    private static final float UNAVAILABLE_ALPHA = 0.3f;
    private static final float TILE_ALPHA = 0.2f;
    private static final float INACTIVE_ALPHA = 0.2f;
    private static boolean fluidQsThemeEnabled = false;
    private static boolean fluidNotifEnabled = false;
    private boolean wasDark = getIsDark();
    final Integer[] colorAccent = {wasDark ? mContext.getResources().getColor(mContext.getResources().getIdentifier("android:color/system_accent1_300", "color", listenPackage), mContext.getTheme()) : mContext.getResources().getColor(mContext.getResources().getIdentifier("android:color/system_accent1_600", "color", listenPackage), mContext.getTheme())};
    final Integer[] colorActiveAlpha = {Color.argb((int) (TILE_ALPHA * 255), Color.red(colorAccent[0]), Color.green(colorAccent[0]), Color.blue(colorAccent[0]))};
    final Integer[] colorInactiveAlpha = {wasDark ? Color.parseColor("#0FFFFFFF") : Color.parseColor("#59FFFFFF")};
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
        initResources();
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!lpparam.packageName.equals(listenPackage)) return;

        Class<?> QsPanelClass = findClass(SYSTEMUI_PACKAGE + ".qs.QSPanel", lpparam.classLoader);
        Class<?> QSTileViewImplClass = findClass(SYSTEMUI_PACKAGE + ".qs.tileimpl.QSTileViewImpl", lpparam.classLoader);
        Class<?> QSIconViewImplClass = findClass(SYSTEMUI_PACKAGE + ".qs.tileimpl.QSIconViewImpl", lpparam.classLoader);
        Class<?> CentralSurfacesImplClass = findClass(SYSTEMUI_PACKAGE + ".statusbar.phone.CentralSurfacesImpl", lpparam.classLoader);
        Class<?> BrightnessSliderViewClass = findClass(SYSTEMUI_PACKAGE + ".settings.brightness.BrightnessSliderView", lpparam.classLoader);
        Class<?> BrightnessControllerClass = findClass(SYSTEMUI_PACKAGE + ".settings.brightness.BrightnessController", lpparam.classLoader);
        Class<?> BrightnessMirrorControllerClass = findClass(SYSTEMUI_PACKAGE + ".statusbar.policy.BrightnessMirrorController", lpparam.classLoader);
        Class<?> BrightnessSliderControllerClass = findClass(SYSTEMUI_PACKAGE + ".settings.brightness.BrightnessSliderController", lpparam.classLoader);
        SettingsLibUtils.init(lpparam.classLoader);
        initResources();

        // QS tile color
        hookAllMethods(QSTileViewImplClass, "getBackgroundColorForState", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (!fluidQsThemeEnabled) return;

                if ((int) param.args[0] == STATE_ACTIVE) {
                    param.setResult(colorActiveAlpha[0]);
                } else if ((int) param.args[0] == STATE_INACTIVE) {
                    param.setResult(colorInactiveAlpha[0]);
                } else if ((int) param.args[0] == STATE_UNAVAILABLE) {
                    param.setResult(colorUnavailableAlpha[0]);
                }
            }
        });

        // QS icon color
        hookAllMethods(QSIconViewImplClass, "getIconColorForState", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (!fluidQsThemeEnabled) return;

                if ((int) getObjectField(param.args[1], "state") == STATE_ACTIVE) {
                    param.setResult(colorAccent[0]);
                }
            }
        });

        hookAllMethods(QSIconViewImplClass, "updateIcon", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (!fluidQsThemeEnabled) return;

                if (param.args[0] instanceof ImageView && getIntField(param.args[1], "state") == STATE_ACTIVE) {
                    ((ImageView) param.args[0]).setImageTintList(ColorStateList.valueOf(colorAccent[0]));
                }
            }
        });

        hookAllMethods(QSIconViewImplClass, "setIcon", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (!fluidQsThemeEnabled) return;

                if (param.args[0] instanceof ImageView && getIntField(param.args[1], "state") == STATE_ACTIVE) {
                    setObjectField(param.thisObject, "mTint", colorAccent[0]);
                }
            }
        });

        // Brightness slider and auto brightness color
        hookAllMethods(BrightnessSliderViewClass, "onFinishInflate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (!fluidQsThemeEnabled) return;

                try {
                    ((Drawable) getObjectField(param.thisObject, "mProgressDrawable")).setTint(colorActiveAlpha[0]);

                    LayerDrawable progress = (LayerDrawable) ((SeekBar) getObjectField(param.thisObject, "mSlider")).getProgressDrawable();
                    DrawableWrapper progressSlider = (DrawableWrapper) progress.findDrawableByLayerId(android.R.id.progress);
                    LayerDrawable actualProgressSlider = (LayerDrawable) progressSlider.getDrawable();
                    Drawable mBrightnessIcon = actualProgressSlider.findDrawableByLayerId(mContext.getResources().getIdentifier("slider_icon", "id", mContext.getPackageName()));
                    mBrightnessIcon.setAlpha(0);
                    mBrightnessIcon.setTint(Color.TRANSPARENT);
                } catch (Throwable ignored) {
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
                    log(TAG + throwable);
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

                if ((int) param.args[0] == STATE_ACTIVE) {
                    param.setResult(colorAccent[0]);
                }
            }
        });

        // QS tile secondary label color
        hookAllMethods(QSTileViewImplClass, "getSecondaryLabelColorForState", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (!fluidQsThemeEnabled) return;

                if ((int) param.args[0] == STATE_ACTIVE) {
                    param.setResult(colorAccent[0]);
                }
            }
        });

        // For LineageOS based roms
        hookAllConstructors(QSTileViewImplClass, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (!fluidQsThemeEnabled) return;

                setObjectField(param.thisObject, "colorActive", colorActiveAlpha[0]);
                setObjectField(param.thisObject, "colorInactive", colorInactiveAlpha[0]);
                setObjectField(param.thisObject, "colorUnavailable", colorUnavailableAlpha[0]);
                setObjectField(param.thisObject, "colorLabelActive", colorAccent[0]);
                setObjectField(param.thisObject, "colorSecondaryLabelActive", colorAccent[0]);
            }
        });

        // Initialize colors
        hookAllConstructors(CentralSurfacesImplClass, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                initResources();
            }
        });

        hookAllMethods(CentralSurfacesImplClass, "updateTheme", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                initResources();
            }
        });
    }

    private boolean getIsDark() {
        return (mContext.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_YES) == Configuration.UI_MODE_NIGHT_YES;
    }

    private void initResources() {
        boolean isDark = getIsDark();

        if (isDark != wasDark) {
            wasDark = isDark;
        }

        colorAccent[0] = wasDark ? mContext.getResources().getColor(mContext.getResources().getIdentifier("android:color/system_accent1_300", "color", listenPackage), mContext.getTheme()) : mContext.getResources().getColor(mContext.getResources().getIdentifier("android:color/system_accent1_600", "color", listenPackage), mContext.getTheme());
        colorActiveAlpha[0] = Color.argb((int) (TILE_ALPHA * 255), Color.red(colorAccent[0]), Color.green(colorAccent[0]), Color.blue(colorAccent[0]));
        colorInactiveAlpha[0] = wasDark ? Color.parseColor("#0FFFFFFF") : Color.parseColor("#59FFFFFF");
        colorUnavailableAlpha[0] = wasDark ? Color.parseColor("#08FFFFFF") : Color.parseColor("#33FFFFFF");

        // Replace drawables to match QS style
        XC_InitPackageResources.InitPackageResourcesParam ourResparam = resparams.get(SYSTEMUI_PACKAGE);
        if (ourResparam != null) {
            if (!fluidQsThemeEnabled) return;

            int px2dp2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, mContext.getResources().getDisplayMetrics());
            int px2dp4 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, mContext.getResources().getDisplayMetrics());
            int px2dp20 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, mContext.getResources().getDisplayMetrics());
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
                ourResparam.res.setReplacement(mContext.getPackageName(), "drawable", "brightness_mirror_background", new XResources.DrawableLoader() {
                    @Override
                    public Drawable newDrawable(XResources res, int id) {
                        GradientDrawable gradientDrawable = new GradientDrawable();
                        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
                        gradientDrawable.setColor(Color.TRANSPARENT);
                        gradientDrawable.setCornerRadius(mContext.getResources().getDimensionPixelSize(mContext.getResources().getIdentifier("rounded_slider_background_rounded_corner", "dimen", mContext.getPackageName())));
                        return gradientDrawable;
                    }
                });
            } catch (Throwable ignored) {
            }

            try {
                ourResparam.res.setReplacement(mContext.getPackageName(), "drawable", "brightness_progress_drawable", new XResources.DrawableLoader() {
                    @Override
                    public Drawable newDrawable(XResources res, int id) throws PackageManager.NameNotFoundException {
                        return createBrightnessBackgroundDrawable(mContext);
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

            try {
                ourResparam.res.setReplacement(mContext.getPackageName(), "drawable", "notif_footer_btn_background", new XResources.DrawableLoader() {
                    @Override
                    public Drawable newDrawable(XResources res, int id) {
                        GradientDrawable gradientDrawable = new GradientDrawable();
                        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
                        gradientDrawable.setColor(colorInactiveAlpha[0]);
                        gradientDrawable.setCornerRadius(notifCornerRadius);
                        gradientDrawable.setPadding(px2dp20, 0, px2dp20, 0);
                        return new InsetDrawable(gradientDrawable, 0, px2dp2, 0, px2dp2);
                    }
                });
            } catch (Throwable ignored) {
            }

            @SuppressLint("DiscouragedApi") ColorStateList states = getColorAttr(mContext.getResources().getIdentifier("android:attr/colorControlHighlight", "attr", listenPackage), mContext);

            if (fluidNotifEnabled && states != null) {
                try {
                    ourResparam.res.setReplacement(mContext.getPackageName(), "drawable", "notification_material_bg", new XResources.DrawableLoader() {
                        @Override
                        public Drawable newDrawable(XResources res, int id) {
                            GradientDrawable gradientDrawable = new GradientDrawable();
                            gradientDrawable.setColor(colorInactiveAlpha[0]);
                            return new RippleDrawable(ColorStateList.valueOf(states.getDefaultColor()), gradientDrawable, null);
                        }
                    });
                } catch (Throwable ignored) {
                }

                try {
                    ourResparam.res.setReplacement(mContext.getPackageName(), "drawable", "notification_material_bg_monet", new XResources.DrawableLoader() {
                        @Override
                        public Drawable newDrawable(XResources res, int id) {
                            GradientDrawable gradientDrawable = new GradientDrawable();
                            gradientDrawable.setColor(colorInactiveAlpha[0]);
                            return new RippleDrawable(ColorStateList.valueOf(states.getDefaultColor()), gradientDrawable, null);
                        }
                    });
                } catch (Throwable ignored) {
                }
            }

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

    private LayerDrawable createBrightnessBackgroundDrawable(Context context) throws PackageManager.NameNotFoundException {
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
        backgroundShape.getPaint().setColor(colorInactiveAlpha[0]);

        // Create the progress drawable
        RoundedCornerProgressDrawable progressDrawable = null;
        try {
            progressDrawable = new RoundedCornerProgressDrawable(AppCompatResources.getDrawable(context, res.getIdentifier("brightness_progress_full_drawable", "drawable", context.getPackageName())));
        } catch (Throwable ignored) {
        }

        // Create the start and end drawables
        Resources appRes = context.createPackageContext(APPLICATION_ID, CONTEXT_IGNORE_SECURITY).getResources();
        Drawable startDrawable = ResourcesCompat.getDrawable(appRes, R.drawable.ic_brightness_low, context.getTheme());
        Drawable endDrawable = ResourcesCompat.getDrawable(appRes, R.drawable.ic_brightness_full, context.getTheme());
        if (startDrawable != null && endDrawable != null) {
            startDrawable.setTint(colorAccent[0]);
            endDrawable.setTint(colorAccent[0]);
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

    private static float dpToPx(Context context, int dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName) && !HookEntry.isChildProcess && Build.VERSION.SDK_INT >= 33;
    }
}