package com.drdisagree.iconify.provider

import com.crossbowffs.remotepreferences.RemotePreferenceFile
import com.crossbowffs.remotepreferences.RemotePreferenceProvider
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.common.Resources

class RemotePrefProvider : RemotePreferenceProvider(
    BuildConfig.APPLICATION_ID,
    arrayOf(RemotePreferenceFile(Resources.SharedXPref, true))
)