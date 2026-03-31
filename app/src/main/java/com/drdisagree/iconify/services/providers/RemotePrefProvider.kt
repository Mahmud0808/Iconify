package com.drdisagree.iconify.services.providers

import com.crossbowffs.remotepreferences.RemotePreferenceFile
import com.crossbowffs.remotepreferences.RemotePreferenceProvider
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.data.common.XposedConst.PREF_FILE_NAME

class RemotePrefProvider : RemotePreferenceProvider(
    BuildConfig.APPLICATION_ID,
    arrayOf(RemotePreferenceFile(PREF_FILE_NAME, true))
)