@file:Suppress("unused")

package com.drdisagree.iconify.xposed.core.reflect

fun Any?.getField(fieldName: String): Any {
    if (this == null) throw NoSuchFieldError("Field not found: $fieldName: object is null")
    return MemberResolver.findField(javaClass, fieldName).get(this)
        ?: throw NullPointerException("Field $fieldName is null in ${javaClass.name}")
}

fun Any?.getFieldSilently(fieldName: String): Any? {
    if (this == null) return null
    return try {
        MemberResolver.findField(javaClass, fieldName).get(this)
    } catch (_: Throwable) {
        null
    }
}

fun Any?.setField(fieldName: String, value: Any?) {
    if (this == null) throw NoSuchFieldError("Field not found: $fieldName: object is null")
    MemberResolver.findField(javaClass, fieldName).set(this, value)
}

fun Any?.setFieldSilently(fieldName: String, value: Any?) {
    try {
        setField(fieldName, value)
    } catch (_: Throwable) {
    }
}

fun Class<*>?.getStaticField(fieldName: String): Any {
    if (this == null) throw NoSuchFieldError("Field not found: $fieldName: class is null")
    return MemberResolver.findField(this, fieldName).get(null)
        ?: throw NullPointerException("Static field $fieldName is null in $name")
}

fun Class<*>?.getStaticFieldSilently(fieldName: String): Any? {
    if (this == null) return null
    return try {
        MemberResolver.findField(this, fieldName).get(null)
    } catch (_: Throwable) {
        null
    }
}

fun Class<*>?.setStaticField(fieldName: String, value: Any?) {
    if (this == null) throw NoSuchFieldError("Field not found: $fieldName: class is null")
    MemberResolver.findField(this, fieldName).set(null, value)
}

fun Class<*>?.setStaticFieldSilently(fieldName: String, value: Any?) {
    try {
        setStaticField(fieldName, value)
    } catch (_: Throwable) {
    }
}

fun Any?.getAnyField(vararg fieldNames: String): Any? {
    if (this != null) {
        fieldNames.forEach { fieldName ->
            MemberResolver.findFieldIfExists(javaClass, fieldName)?.let { return it.get(this) }
        }
    }
    throw NoSuchFieldError("Field not found: ${fieldNames.joinToString()}")
}

fun Any?.setAnyField(value: Any?, vararg fieldNames: String) {
    if (this != null) {
        fieldNames.forEach { fieldName ->
            MemberResolver.findFieldIfExists(javaClass, fieldName)?.let { return it.set(this, value) }
        }
    }
    throw NoSuchFieldError("Field not found: ${fieldNames.joinToString()}")
}

fun Class<*>?.getAnyStaticField(vararg fieldNames: String): Any? {
    if (this != null) {
        fieldNames.forEach { fieldName ->
            MemberResolver.findFieldIfExists(this, fieldName)?.let { return it.get(null) }
        }
    }
    throw NoSuchFieldError("Field not found: ${fieldNames.joinToString()}")
}

fun Class<*>?.hasField(fieldName: String): Boolean =
    this != null && MemberResolver.findFieldIfExists(this, fieldName) != null
