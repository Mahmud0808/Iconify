package com.drdisagree.iconify.xposed.mods;

import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.DEPTH_WALLPAPER_SWITCH;
import static com.drdisagree.iconify.config.XPrefs.Xprefs;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.ImageDecoder;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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

                // Create a new layout for the indication text views
                LinearLayout mIndicationView = new LinearLayout(mContext);
                LinearLayout.LayoutParams mIndicationViewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                int bottomMargin = mContext.getResources().getDimensionPixelSize(mContext.getResources().getIdentifier("keyguard_indication_margin_bottom", "dimen", mContext.getPackageName()));
                mIndicationViewParams.setMargins(0, 0, 0, bottomMargin);
                mIndicationViewParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                mIndicationView.setOrientation(LinearLayout.VERTICAL);
                mIndicationView.setLayoutParams(mIndicationViewParams);

                // Add the indication text views to the new layout
                TextView mTopIndicationView = container.findViewById(mContext.getResources().getIdentifier("keyguard_indication_text", "id", mContext.getPackageName()));
                TextView mLockScreenIndicationView = container.findViewById(mContext.getResources().getIdentifier("keyguard_indication_text_bottom", "id", mContext.getPackageName()));

                ((ViewGroup) mTopIndicationView.getParent()).removeView(mTopIndicationView);
                ((ViewGroup) mLockScreenIndicationView.getParent()).removeView(mLockScreenIndicationView);
                mIndicationView.addView(mTopIndicationView);
                mIndicationView.addView(mLockScreenIndicationView);
                container.addView(mIndicationView);

                // Get the depth wallpaper layout
                String depth_wall_tag = "iconify_depth_wallpaper";
                mDepthWallpaperLayout = container.findViewWithTag(depth_wall_tag);

                // Create the depth wallpaper layout if it doesn't exist
                if (mDepthWallpaperLayout == null) {
                    mDepthWallpaperLayout = new FrameLayout(mContext);
                    mDepthWallpaperLayout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    mDepthWallpaperLayout.setTag(depth_wall_tag);

                    FrameLayout mIndicationArea = new FrameLayout(mContext);
                    FrameLayout.LayoutParams mIndicationAreaParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    mIndicationArea.setLayoutParams(mIndicationAreaParams);

                    mIndicationArea.addView(mDepthWallpaperLayout, 0);
                    container.addView(mIndicationArea, 0);
                }

                mDepthWallpaperBackground = new ImageView(mContext);
                mDepthWallpaperForeground = new ImageView(mContext);

                mDepthWallpaperBackground.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                mDepthWallpaperForeground.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

                mDepthWallpaperLayout.addView(mDepthWallpaperBackground, 0);
                mDepthWallpaperLayout.addView(mDepthWallpaperForeground);

                // Fix the bottom shortcuts pushing the wallpaper
                ImageView startButton = view.findViewById(mContext.getResources().getIdentifier("start_button", "id", mContext.getPackageName()));
                ImageView endButton = view.findViewById(mContext.getResources().getIdentifier("end_button", "id", mContext.getPackageName()));
                int offset = mContext.getResources().getDimensionPixelSize(mContext.getResources().getIdentifier("keyguard_affordance_fixed_height", "dimen", mContext.getPackageName()))
                        + mContext.getResources().getDimensionPixelSize(mContext.getResources().getIdentifier("keyguard_affordance_horizontal_offset", "dimen", mContext.getPackageName()));

                startButton.getViewTreeObserver().addOnGlobalLayoutListener(() -> ((ViewGroup.MarginLayoutParams) mIndicationView.getLayoutParams()).setMarginStart(startButton.getVisibility() == View.VISIBLE ? offset : 0));
                endButton.getViewTreeObserver().addOnGlobalLayoutListener(() -> ((ViewGroup.MarginLayoutParams) mIndicationView.getLayoutParams()).setMarginEnd(endButton.getVisibility() == View.VISIBLE ? offset : 0));

                updateWallpaper();
            }
        });

        hookAllMethods(KeyguardBottomAreaViewClass, "onConfigurationChanged", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                updateWallpaper();
            }
        });

        Class<?> NotificationPanelViewControllerClass = findClass(SYSTEMUI_PACKAGE + ".shade.NotificationPanelViewController", lpparam.classLoader);

        hookAllMethods(NotificationPanelViewControllerClass, "onFinishInflate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (!showDepthWallpaper) return;

                View mView = (View) getObjectField(param.thisObject, "mView");
                View keyguardBottomArea = mView.findViewById(mContext.getResources().getIdentifier("keyguard_bottom_area", "id", mContext.getPackageName()));
                ViewGroup parent = (ViewGroup) keyguardBottomArea.getParent();
                parent.removeView(keyguardBottomArea);
                parent.addView(keyguardBottomArea, 0);
            }
        });

        hookAllMethods(Resources.class, "getDimensionPixelOffset", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                int resId = mContext.getResources().getIdentifier("keyguard_indication_area_padding", "dimen", mContext.getPackageName());

                if (showDepthWallpaper && param.args[0].equals(resId)) {
                    param.setResult(0);
                }
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