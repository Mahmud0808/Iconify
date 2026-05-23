package com.drdisagree.iconify.core.preferences

import android.util.Log

sealed class PrefValue {
    data class BoolValue(val v: Boolean) : PrefValue()
    data class IntValue(val v: Int) : PrefValue()
    data class FloatValue(val v: Float) : PrefValue()
    data class DoubleValue(val v: Double) : PrefValue()
    data class StringValue(val v: String) : PrefValue()
    data class StringSetValue(val v: Set<String>) : PrefValue()
    object None : PrefValue()
}

fun Any?.toPrefValue(): PrefValue {
    return if (this == null) PrefValue.None
    else when (this) {
        is Boolean -> PrefValue.BoolValue(this)
        is Int -> PrefValue.IntValue(this)
        is Float -> PrefValue.FloatValue(this)
        is Double -> PrefValue.DoubleValue(this)
        is String -> PrefValue.StringValue(this)
        is Set<*> -> PrefValue.StringSetValue(filterIsInstance<String>().toSet())
        else -> {
            Log.w("PrefValue", "Unsupported type for PrefValue: ${this::class}")
            PrefValue.None
        }
    }
}

inline fun <reified T> PrefValue.toValueOrNull(): T? {
    val value = when (this) {
        is PrefValue.BoolValue -> v
        is PrefValue.IntValue -> v
        is PrefValue.FloatValue -> v
        is PrefValue.DoubleValue -> v
        is PrefValue.StringValue -> v
        is PrefValue.StringSetValue -> v
        PrefValue.None -> null
    }

    return value as? T
}