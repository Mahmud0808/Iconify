package com.drdisagree.iconify.xposed;

import java.util.HashMap;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;

public class HookRes implements IXposedHookInitPackageResources, IXposedHookZygoteInit {

    public final static HashMap<String, XC_InitPackageResources.InitPackageResourcesParam> resparams = new HashMap<>();
    private String MODULE_PATH;

    @Override
    public void initZygote(StartupParam startupParam) {
        MODULE_PATH = startupParam.modulePath;
    }

    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resparam) {
        try {
            resparams.put(resparam.packageName, resparam);
        } catch (Throwable ignored) {
        }
    }


}