package com.drdisagree.iconify.xposed;

import android.content.res.XModuleResources;

import java.util.HashMap;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;

public class ResourceManager implements IXposedHookInitPackageResources, IXposedHookZygoteInit {

    private String MODULE_PATH;
    public final static HashMap<String, XC_InitPackageResources.InitPackageResourcesParam> resparams = new HashMap<>();

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
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