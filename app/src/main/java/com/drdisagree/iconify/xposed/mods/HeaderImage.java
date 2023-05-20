package com.drdisagree.iconify.xposed.mods;

import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.HEADER_IMAGE_ALPHA;
import static com.drdisagree.iconify.common.Preferences.HEADER_IMAGE_HEIGHT;
import static com.drdisagree.iconify.common.Preferences.HEADER_IMAGE_LANDSCAPE_SWITCH;
import static com.drdisagree.iconify.common.Preferences.HEADER_IMAGE_SWITCH;
import static com.drdisagree.iconify.common.Preferences.HEADER_IMAGE_ZOOMTOFIT;
import static com.drdisagree.iconify.config.XPrefs.Xprefs;
import static com.drdisagree.iconify.xposed.HookRes.resparams;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getIntField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.ImageDecoder;
import android.graphics.drawable.AnimatedImageDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.drdisagree.iconify.xposed.ModPack;

import java.io.File;
import java.util.Objects;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HeaderImage extends ModPack implements IXposedHookLoadPackage {

    private static final String TAG = "Iconify - XposedHeaderImage: ";
    private static final String QuickStatusBarHeaderClass = SYSTEMUI_PACKAGE + ".qs.QuickStatusBarHeader";
    boolean showHeaderImage = false;
    int imageHeight = 140;
    int headerImageAlpha = 100;
    boolean zoomToFit = false;
    boolean hideLandscapeHeaderImage = true;
    LinearLayout mQsHeaderLayout = null;
    ImageView mQsHeaderImageView = null;
    private Object lpparamCustom = null;

    public HeaderImage(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;

        showHeaderImage = Xprefs.getBoolean(HEADER_IMAGE_SWITCH, false);
        headerImageAlpha = Xprefs.getInt(HEADER_IMAGE_ALPHA, 100);
        imageHeight = Xprefs.getInt(HEADER_IMAGE_HEIGHT, 140);
        zoomToFit = Xprefs.getBoolean(HEADER_IMAGE_ZOOMTOFIT, false);
        hideLandscapeHeaderImage = Xprefs.getBoolean(HEADER_IMAGE_LANDSCAPE_SWITCH, true);

        if (Key.length > 0 && (Objects.equals(Key[0], HEADER_IMAGE_SWITCH) || Objects.equals(Key[0], HEADER_IMAGE_LANDSCAPE_SWITCH) || Objects.equals(Key[0], HEADER_IMAGE_ALPHA) || Objects.equals(Key[0], HEADER_IMAGE_HEIGHT) || Objects.equals(Key[0], HEADER_IMAGE_ZOOMTOFIT))) {
            if (lpparamCustom != null) {
                callMethod(lpparamCustom, "updateResources");
            }
        }
    }

    @Override
    public boolean listensTo(String packageName) {
        return packageName.equals(SYSTEMUI_PACKAGE);
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!lpparam.packageName.equals(SYSTEMUI_PACKAGE)) return;

        addHeaderImageLayout();

        final Class<?> QuickStatusBarHeader = findClass(QuickStatusBarHeaderClass, lpparam.classLoader);

        try {
            hookAllMethods(QuickStatusBarHeader, "updateResources", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    lpparamCustom = param.thisObject;
                    updateQSHeaderImage();
                }
            });

            hookAllMethods(QuickStatusBarHeader, "onMeasure", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    View mDatePrivacyView = (View) getObjectField(param.thisObject, "mDatePrivacyView");
                    int mTopViewMeasureHeight = getIntField(param.thisObject, "mTopViewMeasureHeight");

                    if ((int) callMethod(mDatePrivacyView, "getMeasuredHeight") != mTopViewMeasureHeight)
                        callMethod(param.thisObject, "updateAnimators");
                }
            });
        } catch (Throwable throwable) {
            log(TAG + throwable);
        }
    }

    private void updateQSHeaderImage() {
        if (mQsHeaderLayout == null || mQsHeaderImageView == null) return;

        if (!showHeaderImage) {
            mQsHeaderLayout.setVisibility(View.GONE);
            return;
        }

        loadImageOrGif(mQsHeaderImageView);
        mQsHeaderLayout.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, imageHeight, mContext.getResources().getDisplayMetrics());

        Configuration config = mContext.getResources().getConfiguration();
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE && hideLandscapeHeaderImage) {
            mQsHeaderLayout.setVisibility(View.GONE);
        } else {
            mQsHeaderLayout.setVisibility(View.VISIBLE);
        }
    }

    private void addHeaderImageLayout() {
        XC_InitPackageResources.InitPackageResourcesParam ourResparam = resparams.get(SYSTEMUI_PACKAGE);
        if (ourResparam == null) return;

        try {
            ourResparam.res.hookLayout(SYSTEMUI_PACKAGE, "layout", "quick_status_bar_expanded_header", new XC_LayoutInflated() {
                @SuppressLint({"DiscouragedApi"})
                @Override
                public void handleLayoutInflated(XC_LayoutInflated.LayoutInflatedParam liparam) {
                    FrameLayout header = liparam.view.findViewById(liparam.res.getIdentifier("header", "id", mContext.getPackageName()));

                    mQsHeaderLayout = new LinearLayout(mContext);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, imageHeight, mContext.getResources().getDisplayMetrics()));
                    layoutParams.leftMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -16, mContext.getResources().getDisplayMetrics());
                    layoutParams.rightMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -16, mContext.getResources().getDisplayMetrics());
                    mQsHeaderLayout.setLayoutParams(layoutParams);
                    mQsHeaderLayout.setVisibility(View.GONE);

                    mQsHeaderImageView = new ImageView(mContext);
                    mQsHeaderImageView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    mQsHeaderImageView.setAlpha((int) (headerImageAlpha / 100.0 * 255.0));

                    mQsHeaderLayout.addView(mQsHeaderImageView);
                    header.addView(mQsHeaderLayout, 0);
                }
            });
        } catch (Throwable throwable) {
            log(TAG + throwable);
        }
    }

    private void addOrRemoveProperty(View view, int property, boolean flag) {
        try {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
            if (flag) {
                layoutParams.addRule(property);
            } else {
                layoutParams.removeRule(property);
            }
            view.setLayoutParams(layoutParams);
        } catch (Throwable throwable) {
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
            if (flag) {
                layoutParams.gravity = property;
            } else {
                layoutParams.gravity = Gravity.NO_GRAVITY;
            }
            view.setLayoutParams(layoutParams);
        }
    }

    private void loadImageOrGif(ImageView iv) {
        try {
            ImageDecoder.Source source = ImageDecoder.createSource(new File(Environment.getExternalStorageDirectory() + "/.iconify_files/header_image.png"));

            Drawable drawable = ImageDecoder.decodeDrawable(source);
            iv.setImageDrawable(drawable);
            if (!zoomToFit) iv.setScaleType(ImageView.ScaleType.FIT_XY);
            else {
                iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                iv.setAdjustViewBounds(false);
                iv.setCropToPadding(false);
                iv.setClipToOutline(true);
                iv.setMinimumWidth(ViewGroup.LayoutParams.MATCH_PARENT);
                addOrRemoveProperty(iv, RelativeLayout.CENTER_IN_PARENT, true);
            }

            if (drawable instanceof AnimatedImageDrawable) {
                ((AnimatedImageDrawable) drawable).start();
            }

        } catch (Throwable ignored) {
        }
    }
}