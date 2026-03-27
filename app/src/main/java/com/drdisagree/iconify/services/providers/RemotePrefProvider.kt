package com.drdisagree.iconify.services.providers

import com.crossbowffs.remotepreferences.RemotePreferenceFile
import com.crossbowffs.remotepreferences.RemotePreferenceProvider
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.data.config.RPrefs.getPrefsName

class RemotePrefProvider : RemotePreferenceProvider(
    BuildConfig.APPLICATION_ID,
    arrayOf(RemotePreferenceFile(getPrefsName(), true))
)