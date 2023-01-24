package com.drdisagree.iconify.xposed.mods;

import static com.drdisagree.iconify.common.References.HEADER_IMAGE_ALPHA;
import static com.drdisagree.iconify.common.References.HEADER_IMAGE_HEIGHT;
import static com.drdisagree.iconify.common.References.HEADER_IMAGE_SWITCH;
import static com.drdisagree.iconify.common.References.HIDE_STATUS_ICONS_SWITCH;
import static com.drdisagree.iconify.common.References.SYSTEM_UI_PACKAGE;
import static com.drdisagree.iconify.config.XPrefs.Xprefs;
import static com.drdisagree.iconify.xposed.HookRes.resparams;
import static de.robv.android.xposed.XposedBridge.log;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.XResources;
import android.graphics.ImageDecoder;
import android.graphics.drawable.AnimatedImageDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.drdisagree.iconify.xposed.ModPack;

import java.io.File;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HeaderImage extends ModPack implements IXposedHookLoadPackage {

    private static final String TAG = "Iconify - HeaderImage: ";
    boolean showHeaderImage = false;
    int headerImageHeight = 108;
    boolean hideStatusIcons = false;
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
        headerImageHeight = Xprefs.getInt(HEADER_IMAGE_HEIGHT, 108);
        headerImageAlpha = Xprefs.getInt(HEADER_IMAGE_ALPHA, 100);
        hideStatusIcons = Xprefs.getBoolean(HIDE_STATUS_ICONS_SWITCH, false);

        setHeaderImage();
        setStatusIcons();
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
            ourResparam.res.hookLayout(SYSTEM_UI_PACKAGE, "layout", "quick_status_bar_expanded_header", new XC_LayoutInflated() {
                @SuppressLint({"DiscouragedApi"})
                @Override
                public void handleLayoutInflated(XC_LayoutInflated.LayoutInflatedParam liparam) {
                    @SuppressLint("DiscouragedApi") FrameLayout header = liparam.view.findViewById(liparam.res.getIdentifier("header", "id", SYSTEM_UI_PACKAGE));

                    final ImageView headerImage = new ImageView(mContext);
                    headerImage.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, headerImageHeight, mContext.getResources().getDisplayMetrics())));
                    ((LinearLayout.LayoutParams) headerImage.getLayoutParams()).setMargins((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -16, mContext.getResources().getDisplayMetrics()), 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -16, mContext.getResources().getDisplayMetrics()), 0);
                    loadGif(headerImage);
                    headerImage.setAlpha((int) (headerImageAlpha / 100.0 * 255.0));
                    header.addView(headerImage, header.getChildCount() - 1);

                    ourResparam.res.setReplacement(SYSTEM_UI_PACKAGE, "dimen", "qs_panel_padding_top", new XResources.DimensionReplacement(headerImageHeight - 30, TypedValue.COMPLEX_UNIT_DIP));
                    ourResparam.res.setReplacement(SYSTEM_UI_PACKAGE, "dimen", "qqs_layout_margin_top", new XResources.DimensionReplacement(headerImageHeight - 30, TypedValue.COMPLEX_UNIT_DIP));

                    log("Header image added successfully.");
                }
            });
        } catch (Throwable t) {
            log(TAG + t);
        }
    }

    private void loadGif(ImageView iv) {
        try {
            ImageDecoder.Source source = ImageDecoder.createSource(new File(Environment.getExternalStorageDirectory() + "/.iconify_headerimg/header_image.png"));

            Drawable drawable = ImageDecoder.decodeDrawable(source);
            iv.setImageDrawable(drawable);

            if (drawable instanceof AnimatedImageDrawable) {
                ((AnimatedImageDrawable) drawable).start();
            }

        } catch (Exception e) {
            log(TAG + e);
        }
    }

    private void setStatusIcons() {
        XC_InitPackageResources.InitPackageResourcesParam ourResparam = resparams.get(SYSTEM_UI_PACKAGE);
        if (ourResparam == null) return;

        if (!hideStatusIcons)
            return;

        try {
            ourResparam.res.hookLayout(SYSTEM_UI_PACKAGE, "layout", "quick_qs_status_icons", new XC_LayoutInflated() {
                @SuppressLint({"DiscouragedApi"})
                @Override
                public void handleLayoutInflated(XC_LayoutInflated.LayoutInflatedParam liparam) {
                    @SuppressLint("DiscouragedApi") TextView clock = liparam.view.findViewById(liparam.res.getIdentifier("clock", "id", SYSTEM_UI_PACKAGE));
                    clock.getLayoutParams().height = 0;
                    clock.getLayoutParams().width = 0;

                    @SuppressLint("DiscouragedApi") TextView date_clock = liparam.view.findViewById(liparam.res.getIdentifier("date_clock", "id", SYSTEM_UI_PACKAGE));
                    date_clock.getLayoutParams().height = 0;
                    date_clock.getLayoutParams().width = 0;

                    @SuppressLint("DiscouragedApi") LinearLayout carrier_group = liparam.view.findViewById(liparam.res.getIdentifier("carrier_group", "id", SYSTEM_UI_PACKAGE));
                    carrier_group.getLayoutParams().height = 0;
                    carrier_group.getLayoutParams().width = 0;

                    @SuppressLint("DiscouragedApi") LinearLayout statusIcons = liparam.view.findViewById(liparam.res.getIdentifier("statusIcons", "id", SYSTEM_UI_PACKAGE));
                    statusIcons.getLayoutParams().height = 0;
                    statusIcons.getLayoutParams().width = 0;

                    @SuppressLint("DiscouragedApi") LinearLayout batteryRemainingIcon = liparam.view.findViewById(liparam.res.getIdentifier("batteryRemainingIcon", "id", SYSTEM_UI_PACKAGE));
                    batteryRemainingIcon.getLayoutParams().height = 0;
                    batteryRemainingIcon.getLayoutParams().width = 0;

                    log("Header image added successfully.");
                }
            });
        } catch (Throwable t) {
            log(TAG + t);
        }
    }
}