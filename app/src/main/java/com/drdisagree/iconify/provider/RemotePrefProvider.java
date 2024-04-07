package com.drdisagree.iconify.provider;

import static com.drdisagree.iconify.common.Resources.SharedXPref;

import com.crossbowffs.remotepreferences.RemotePreferenceFile;
import com.crossbowffs.remotepreferences.RemotePreferenceProvider;
import com.drdisagree.iconify.BuildConfig;

public class RemotePrefProvider extends RemotePreferenceProvider {
    public RemotePrefProvider() {
        super(BuildConfig.APPLICATION_ID, new RemotePreferenceFile[]{new RemotePreferenceFile(SharedXPref, true)});
    }
}