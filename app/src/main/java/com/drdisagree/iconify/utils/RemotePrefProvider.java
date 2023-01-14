package com.drdisagree.iconify.utils;

import com.crossbowffs.remotepreferences.RemotePreferenceFile;
import com.crossbowffs.remotepreferences.RemotePreferenceProvider;
import com.drdisagree.iconify.BuildConfig;

public class RemotePrefProvider extends RemotePreferenceProvider {
    public RemotePrefProvider() {
        super(BuildConfig.APPLICATION_ID, new RemotePreferenceFile[]{new RemotePreferenceFile(BuildConfig.APPLICATION_ID + "_xpreferences", true)});
    }
}