package com.drdisagree.iconify.xposed.utils

import android.content.Context
import androidx.core.graphics.toColorInt
import com.crossbowffs.remotepreferences.RemotePreferences
import com.drdisagree.iconify.data.keys.Key
import com.drdisagree.iconify.data.keys.XposedKey
import kotlin.math.roundToInt

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

    fun getString(key: Key): String {
        return getString(key.name, key.default as? String ?: "")!!
    }

    fun getInt(key: Key): Int {
        return getFloat(key.name, key.default as? Float ?: 0f).roundToInt()
    }

    fun getFloat(key: Key): Float {
        return getFloat(key.name, key.default as? Float ?: 0f)
    }

    fun getDouble(key: Key): Double {
        return getFloat(key.name, key.default as? Float ?: 0f).toDouble()
    }

    fun getColor(key: XposedKey) = getString(key).toColorInt()
}
