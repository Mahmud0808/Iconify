package com.drdisagree.iconify.xposed.mods;

import static com.drdisagree.iconify.common.References.HEADER_CLOCK_SWITCH;
import static com.drdisagree.iconify.common.References.HEADER_IMAGE_ALPHA;
import static com.drdisagree.iconify.common.References.HEADER_IMAGE_HEIGHT;
import static com.drdisagree.iconify.common.References.HEADER_IMAGE_SWITCH;
import static com.drdisagree.iconify.common.References.SYSTEM_UI_PACKAGE;
import static com.drdisagree.iconify.config.XPrefs.Xprefs;
import static com.drdisagree.iconify.xposed.HookRes.resparams;
import static de.robv.android.xposed.XposedBridge.log;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ImageDecoder;
import android.graphics.drawable.AnimatedImageDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.drdisagree.iconify.xposed.ModPack;

import java.io.File;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HeaderImage extends ModPack implements IXposedHookLoadPackage {

    private static final String TAG = "Iconify - HeaderImage: ";
    boolean showHeaderImage = false;
    int imageHeight = 140;
    int headerImageAlpha = 100;
    private String rootPackagePath = "";

    public HeaderImage(Context context) {
        super(context);
        if (!listensTo(context.getPackageName())) return;
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;

        showHeaderImage = Xprefs.getBoolean(HEADER_IMAGE_SWITCH, false);
        headerImageAlpha = Xprefs.getInt(HEADER_IMAGE_ALPHA, 100);
        imageHeight = Xprefs.getInt(HEADER_IMAGE_HEIGHT, 140);

        setHeaderImage();
    }

    @Override
    public boolean listensTo(String packageName) {
        return packageName.equals(SYSTEM_UI_PACKAGE);
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!lpparam.packageName.equals(SYSTEM_UI_PACKAGE))
            return;

        rootPackagePath = lpparam.appInfo.sourceDir;
    }

    private void setHeaderImage() {
        XC_InitPackageResources.InitPackageResourcesParam ourResparam = resparams.get(SYSTEM_UI_PACKAGE);
        if (ourResparam == null) return;

        if (!showHeaderImage)
            return;

        try {
            ourResparam.res.setReplacement(SYSTEM_UI_PACKAGE, "bool", "config_use_large_screen_shade_header", false);
        } catch (Throwable ignored) {
        }

        try {
            ourResparam.res.hookLayout(SYSTEM_UI_PACKAGE, "layout", "quick_status_bar_expanded_header", new XC_LayoutInflated() {
                @SuppressLint({"DiscouragedApi"})
                @Override
                public void handleLayoutInflated(XC_LayoutInflated.LayoutInflatedParam liparam) {
                    @SuppressLint("DiscouragedApi") FrameLayout header = liparam.view.findViewById(liparam.res.getIdentifier("header", "id", SYSTEM_UI_PACKAGE));

                    final ImageView headerImage = new ImageView(mContext);
                    headerImage.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, imageHeight, mContext.getResources().getDisplayMetrics())));
                    ((LinearLayout.LayoutParams) headerImage.getLayoutParams()).setMargins(
                            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -16, mContext.getResources().getDisplayMetrics()),
                            0,
                            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -16, mContext.getResources().getDisplayMetrics()),
                            0);
                    loadGif(headerImage);
                    headerImage.setAlpha((int) (headerImageAlpha / 100.0 * 255.0));
                    header.addView(headerImage, header.getChildCount() - (Xprefs.getBoolean(HEADER_CLOCK_SWITCH, false) ? 2 : 1));

                    log("Header image added successfully.");
                }
            });
        } catch (Throwable t) {
            log(TAG + t);
        }
    }


    private void addOrRemoveProperty(View view, int property, boolean flag) {
        RelativeLayout.LayoutParams layoutParams =
                (RelativeLayout.LayoutParams) view.getLayoutParams();
        if (flag) {
            layoutParams.addRule(property);
        } else {
            layoutParams.removeRule(property);
        }
        view.setLayoutParams(layoutParams);
    }

    private void loadGif(ImageView iv) {
        try {
            ImageDecoder.Source source = ImageDecoder.createSource(new File(Environment.getExternalStorageDirectory() + "/.iconify_files/header_image.png"));

            Drawable drawable = ImageDecoder.decodeDrawable(source);
            iv.setImageDrawable(drawable);
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            iv.setAdjustViewBounds(false);
            iv.setCropToPadding(false);
            iv.setClipToOutline(true);
            // iv.setMinimumHeight(140);
            iv.setMinimumWidth(ViewGroup.LayoutParams.MATCH_PARENT);
            addOrRemoveProperty(iv, RelativeLayout.CENTER_IN_PARENT, true);

            if (drawable instanceof AnimatedImageDrawable) {
                ((AnimatedImageDrawable) drawable).start();
            }

        } catch (Exception e) {
            log(TAG + e);
        }
    }
}
