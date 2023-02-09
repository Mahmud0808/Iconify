package com.drdisagree.iconify.xposed.mods;

import static com.drdisagree.iconify.common.References.HEADER_IMAGE_HEIGHT;
import static com.drdisagree.iconify.common.References.HEADER_IMAGE_SWITCH;
import static com.drdisagree.iconify.common.References.PANEL_TOPMARGIN_SWITCH;
import static com.drdisagree.iconify.common.References.QS_TOPMARGIN;
import static com.drdisagree.iconify.common.References.SYSTEMUI_PACKAGE;
import static com.drdisagree.iconify.config.XPrefs.Xprefs;
import static com.drdisagree.iconify.xposed.HookRes.resparams;

import android.content.Context;
import android.content.res.XResources;
import android.util.TypedValue;

import com.drdisagree.iconify.xposed.ModPack;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class QSPanel extends ModPack implements IXposedHookLoadPackage {

    private static final String TAG = "Iconify - QSPanel: ";
    boolean enabledTopMargin = false;
    boolean showHeaderImage = false;
    int headerImageHeight = 0;
    int qsTopMargin = 16;
    private String rootPackagePath = "";

    public QSPanel(Context context) {
        super(context);
        if (!listensTo(context.getPackageName())) return;
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;

        showHeaderImage = Xprefs.getBoolean(HEADER_IMAGE_SWITCH, false);
        enabledTopMargin = Xprefs.getBoolean(PANEL_TOPMARGIN_SWITCH, false);
        headerImageHeight = Xprefs.getInt(HEADER_IMAGE_HEIGHT, 0);
        qsTopMargin = Xprefs.getInt(QS_TOPMARGIN, 16);

        setPanelTopMargin();
    }

    @Override
    public boolean listensTo(String packageName) {
        return packageName.equals(SYSTEMUI_PACKAGE);
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!lpparam.packageName.equals(SYSTEMUI_PACKAGE))
            return;

        rootPackagePath = lpparam.appInfo.sourceDir;
    }

    private void setPanelTopMargin() {
        XC_InitPackageResources.InitPackageResourcesParam ourResparam = resparams.get(SYSTEMUI_PACKAGE);
        if (ourResparam == null) return;

        if (!enabledTopMargin)
            return;

        ourResparam.res.setReplacement(SYSTEMUI_PACKAGE, "dimen", "qs_panel_padding_top", new XResources.DimensionReplacement(showHeaderImage ? (headerImageHeight + qsTopMargin) : qsTopMargin, TypedValue.COMPLEX_UNIT_DIP));
        ourResparam.res.setReplacement(SYSTEMUI_PACKAGE, "dimen", "qqs_layout_margin_top", new XResources.DimensionReplacement(showHeaderImage ? (headerImageHeight + qsTopMargin) : qsTopMargin, TypedValue.COMPLEX_UNIT_DIP));
    }
}