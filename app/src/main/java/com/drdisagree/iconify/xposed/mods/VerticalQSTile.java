package com.drdisagree.iconify.xposed.mods;

import static com.drdisagree.iconify.common.References.SYSTEM_UI_PACKAGE;
import static com.drdisagree.iconify.common.References.VERTICAL_QSTILE_SWITCH;
import static com.drdisagree.iconify.config.XPrefs.Xprefs;
import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.drdisagree.iconify.utils.SystemUtil;
import com.drdisagree.iconify.xposed.ModPack;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class VerticalQSTile extends ModPack {

    private static final String TAG = "Iconify - VerticalQSTile: ";
    private String rootPackagePath = "";
    private static boolean isVerticalQSTileActive = false;
    private static final String CLASS_QSTILEVIEWIMPL = SYSTEM_UI_PACKAGE + ".qs.tileimpl.QSTileViewImpl";
    private static final String CLASS_FONTSIZEUTILS = SYSTEM_UI_PACKAGE + ".FontSizeUtils";
    private Object mParam = null;
    private static Float QsTilePrimaryTextSize = null, QsTileSecondaryTextSize = null;

    public VerticalQSTile(Context context) {
        super(context);
        if (!listensTo(context.getPackageName())) return;
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;

        isVerticalQSTileActive = Xprefs.getBoolean(VERTICAL_QSTILE_SWITCH, false);

        if (Key.length > 0 && Key[0].equals(VERTICAL_QSTILE_SWITCH))
            SystemUtil.doubleToggleDarkMode();
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

        Class<?> QSTileViewImpl = findClass(CLASS_QSTILEVIEWIMPL, lpparam.classLoader);
        Class<?> FontSizeUtils = findClass(CLASS_FONTSIZEUTILS, lpparam.classLoader);

        hookAllMethods(QSTileViewImpl, "onConfigurationChanged", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) {
                if (!isVerticalQSTileActive)
                    return;

                fixTileLayout(((LinearLayout) param.thisObject), mParam);
            }
        });

        hookAllConstructors(QSTileViewImpl, new XC_MethodHook() {
            @SuppressLint("DiscouragedApi")
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (!isVerticalQSTileActive)
                    return;

                mParam = param.thisObject;

                try {
                    ((LinearLayout) param.thisObject).setGravity(Gravity.CENTER);
                    ((LinearLayout) param.thisObject).setOrientation(LinearLayout.VERTICAL);

                    ((TextView) getObjectField(param.thisObject, "label")).setGravity(Gravity.CENTER_HORIZONTAL);
                    ((TextView) getObjectField(param.thisObject, "secondaryLabel")).setGravity(Gravity.CENTER_HORIZONTAL);

                    LinearLayout newQSTile = new LinearLayout(mContext);
                    newQSTile.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

                    ((LinearLayout) param.thisObject).removeView(((LinearLayout) getObjectField(param.thisObject, "labelContainer")));
                    newQSTile.addView(((LinearLayout) getObjectField(param.thisObject, "labelContainer")));

                    ((LinearLayout) getObjectField(param.thisObject, "labelContainer")).setGravity(Gravity.CENTER_HORIZONTAL);
                    ((LinearLayout) param.thisObject).removeView((View) getObjectField(param.thisObject, "sideView"));

                    fixTileLayout(((LinearLayout) param.thisObject), mParam);

                    ((LinearLayout) param.thisObject).addView(newQSTile);
                } catch (Throwable t) {
                    log(TAG + t.toString());
                }

                if (QsTilePrimaryTextSize == null || QsTileSecondaryTextSize == null) {
                    callStaticMethod(FontSizeUtils, "updateFontSize", mContext.getResources().getIdentifier("qs_tile_text_size", "dimen", mContext.getPackageName()), getObjectField(param.thisObject, "label"));

                    callStaticMethod(FontSizeUtils, "updateFontSize", mContext.getResources().getIdentifier("qs_tile_text_size", "dimen", mContext.getPackageName()), getObjectField(param.thisObject, "secondaryLabel"));

                    TextView PrimaryText = (TextView) getObjectField(param.thisObject, "label");
                    TextView SecondaryText = (TextView) getObjectField(param.thisObject, "secondaryLabel");

                    QsTilePrimaryTextSize = PrimaryText.getTextSize();
                    QsTileSecondaryTextSize = SecondaryText.getTextSize();
                }
            }
        });
    }

    private void fixTileLayout(LinearLayout tile, Object param) {
        Resources mRes = mContext.getResources();
        @SuppressLint("DiscouragedApi") int padding = mRes.getDimensionPixelSize(mRes.getIdentifier("qs_tile_padding", "dimen", mContext.getPackageName()));
        tile.setPadding(padding, padding, padding, padding);
        ((LinearLayout.LayoutParams) ((LinearLayout) getObjectField(tile, "labelContainer")).getLayoutParams()).setMarginStart(0);
        tile.setGravity(Gravity.CENTER);
        tile.setOrientation(LinearLayout.VERTICAL);

        if (param != null) {
            ((TextView) getObjectField(param, "label")).setGravity(Gravity.CENTER_HORIZONTAL);
            ((TextView) getObjectField(param, "secondaryLabel")).setGravity(Gravity.CENTER_HORIZONTAL);
        }
    }
}
