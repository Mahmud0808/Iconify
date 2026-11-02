@file:Suppress("unused")

package com.drdisagree.iconify.xposed.core.reflect

fun Any?.getExtraField(fieldName: String): Any {
    if (this == null) throw NullPointerException("Extra field $fieldName: object is null")
    return ExtraFieldStore.get(this, fieldName)
        ?: throw NullPointerException("Extra field $fieldName is not set")
}

fun Any?.getExtraFieldSilently(fieldName: String): Any? {
    if (this == null) return null
    return ExtraFieldStore.get(this, fieldName)
}

fun Any?.setExtraField(fieldName: String, value: Any?) {
    if (this == null) throw NullPointerException("Extra field $fieldName: object is null")
    ExtraFieldStore.set(this, fieldName, value)
}

fun Any?.removeExtraField(fieldName: String): Any? {
    if (this == null) return null
    return ExtraFieldStore.remove(this, fieldName)
}
