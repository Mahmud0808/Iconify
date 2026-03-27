package com.drdisagree.iconify.xposed.utils

import android.content.Context
import com.crossbowffs.remotepreferences.RemotePreferences
import com.drdisagree.iconify.data.keys.Key

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

    fun getBoolean(key: String): Boolean {
        return getBoolean(key, false)
    }

    fun getBoolean(key: Key): Boolean {
        return getBoolean(key.name, key.default as? Boolean ?: false)
    }

    fun getString(key: Key): String? {
        return getString(key.name, key.default as? String)
    }
}
