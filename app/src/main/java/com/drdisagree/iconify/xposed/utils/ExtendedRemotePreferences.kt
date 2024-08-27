package com.drdisagree.iconify.xposed.utils

import android.content.Context
import com.crossbowffs.remotepreferences.RemotePreferences
import com.drdisagree.iconify.ui.preferences.SliderPreference

@Suppress("unused")
class ExtendedRemotePreferences : RemotePreferences {

    constructor(context: Context, authority: String, prefFileName: String) : super(
        context,
        authority,
        prefFileName
    )

    constructor(
        context: Context,
        authority: String,
        prefFileName: String,
        strictMode: Boolean
    ) : super(context, authority, prefFileName, strictMode)

    fun getBoolean(key: String?): Boolean {
        return getBoolean(key, false)
    }

    fun getSliderInt(key: String?, defaultVal: Int): Int {
        return SliderPreference.getSingleIntValue(this, key, defaultVal)
    }

    fun getSliderFloat(key: String?, defaultVal: Float): Float {
        return SliderPreference.getSingleFloatValue(this, key, defaultVal)
    }

    fun getSliderValues(key: String?, defaultValue: Float): List<Float> {
        return SliderPreference.getValues(this, key, defaultValue)
    }
}
