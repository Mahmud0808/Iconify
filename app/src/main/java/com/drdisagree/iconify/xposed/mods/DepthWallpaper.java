package com.drdisagree.iconify.xposed.mods;

import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.DEPTH_WALLPAPER_SWITCH;
import static com.drdisagree.iconify.config.XPrefs.Xprefs;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.findClass;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ImageDecoder;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.drdisagree.iconify.xposed.ModPack;

import java.io.File;
import java.util.Objects;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

@SuppressLint("DiscouragedApi")
public class DepthWallpaper extends ModPack {

    private static final String TAG = "Iconify - " + DepthWallpaper.class.getSimpleName() + ": ";
    private boolean showDepthWallpaper = false;
    private FrameLayout mDepthWallpaperLayout = null;
    private ImageView mDepthWallpaperBackground = null;
    private ImageView mDepthWallpaperForeground = null;

    public DepthWallpaper(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;

        showDepthWallpaper = Xprefs.getBoolean(DEPTH_WALLPAPER_SWITCH, false);

        if (Key.length > 0 && Objects.equals(Key[0], DEPTH_WALLPAPER_SWITCH)) {
            updateWallpaper();
        }
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        Class<?> KeyguardBottomAreaViewClass = findClass(SYSTEMUI_PACKAGE + ".statusbar.phone.KeyguardBottomAreaView", lpparam.classLoader);

        hookAllMethods(KeyguardBottomAreaViewClass, "onFinishInflate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (!showDepthWallpaper) return;

                View view = (View) param.thisObject;
                ViewGroup container = view.findViewById(mContext.getResources().getIdentifier("keyguard_indication_area", "id", mContext.getPackageName()));

                container.setClipChildren(false);
                container.setClipToPadding(false);
                container.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                container.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                ((ViewGroup.MarginLayoutParams) container.getLayoutParams()).bottomMargin = 0;

                // Get the depth wallpaper layout
                String depth_wall_tag = "iconify_depth_wallpaper";
                mDepthWallpaperLayout = container.findViewWithTag(depth_wall_tag);

                // Create the depth wallpaper layout if it doesn't exist
                if (mDepthWallpaperLayout == null) {
                    mDepthWallpaperLayout = new FrameLayout(mContext);
                    mDepthWallpaperLayout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    mDepthWallpaperLayout.setTag(depth_wall_tag);

                    container.addView(mDepthWallpaperLayout, 0);
                }

                mDepthWallpaperBackground = new ImageView(mContext);
                mDepthWallpaperForeground = new ImageView(mContext);

                mDepthWallpaperBackground.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                mDepthWallpaperForeground.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

                mDepthWallpaperLayout.addView(mDepthWallpaperBackground, 0);
                mDepthWallpaperLayout.addView(mDepthWallpaperForeground);

                updateWallpaper();
            }
        });

        hookAllMethods(KeyguardBottomAreaViewClass, "onConfigurationChanged", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                updateWallpaper();
            }
        });
    }

    private void updateWallpaper() {
        if (mDepthWallpaperLayout == null) return;

        if (!showDepthWallpaper) {
            mDepthWallpaperLayout.setVisibility(View.GONE);
            return;
        }

        try {
            ImageDecoder.Source backgroundImg = ImageDecoder.createSource(new File(Environment.getExternalStorageDirectory() + "/.iconify_files/depth_wallpaper_bg.png"));
            ImageDecoder.Source foregroundImg = ImageDecoder.createSource(new File(Environment.getExternalStorageDirectory() + "/.iconify_files/depth_wallpaper_fg.png"));

            Drawable backgroundDrawable = ImageDecoder.decodeDrawable(backgroundImg);
            Drawable foregroundDrawable = ImageDecoder.decodeDrawable(foregroundImg);

            mDepthWallpaperBackground.setImageDrawable(backgroundDrawable);
            mDepthWallpaperBackground.setClipToOutline(true);
            mDepthWallpaperBackground.setScaleType(ImageView.ScaleType.CENTER_CROP);
            mDepthWallpaperBackground.setScaleX(1.1f);
            mDepthWallpaperBackground.setScaleY(1.1f);

            mDepthWallpaperForeground.setImageDrawable(foregroundDrawable);
            mDepthWallpaperForeground.setClipToOutline(true);
            mDepthWallpaperForeground.setScaleType(ImageView.ScaleType.CENTER_CROP);
            mDepthWallpaperForeground.setScaleX(1.1f);
            mDepthWallpaperForeground.setScaleY(1.1f);

            mDepthWallpaperLayout.setVisibility(View.VISIBLE);
        } catch (Throwable ignored) {
        }
    }
}