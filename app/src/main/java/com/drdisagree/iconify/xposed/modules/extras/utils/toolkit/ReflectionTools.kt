package com.drdisagree.iconify.xposed.modules.extras.utils.toolkit

import android.util.ArraySet
import java.lang.reflect.Method
import java.util.regex.Pattern

fun Any?.isMethodAvailable(methodName: String, vararg parameterTypes: Class<*>?): Boolean {
    if (this == null) return false

    if (this is Class<*>) return if (parameterTypes.isEmpty()) {
        declaredMethods.toList().union(methods.toList()).any { it.name == methodName }
    } else {
        try {
            getDeclaredMethod(methodName, *parameterTypes)
            true
        } catch (_: NoSuchMethodException) {
            try {
                getMethod(methodName, *parameterTypes)
                true
            } catch (_: NoSuchMethodException) {
                false
            }
        }
    }

    return try {
        if (parameterTypes.isEmpty()) {
            this::class.java.declaredMethods.toList().union(this::class.java.methods.toList())
                .any { it.name == methodName }
        } else {
            try {
                this::class.java.getDeclaredMethod(methodName, *parameterTypes)
                true
            } catch (_: NoSuchMethodException) {
                try {
                    this::class.java.getMethod(methodName, *parameterTypes)
                    true
                } catch (_: NoSuchMethodException) {
                    false
                }
            }
        }
    } catch (_: NoSuchMethodException) {
        false
    }
}

fun Class<*>?.isFieldAvailable(fieldName: String): Boolean {
    if (this == null) return false

    return try {
        this::class.java.getDeclaredField(fieldName)
        true
    } catch (_: NoSuchFieldException) {
        false
    }
}

fun Class<*>?.findMethod(namePattern: String): Method? {
    if (this == null) return null

    val methods: Array<Method> = declaredMethods.toList().union(methods.toList()).toTypedArray()

    for (method in methods) {
        if (Pattern.matches(namePattern, method.name)) {
            return method
        }
    }

    return null
}

fun Class<*>?.findMethods(namePattern: String): Set<Method> {
    if (this == null) return emptySet()

    val result: MutableSet<Method> = ArraySet()
    val methods: Array<Method> = declaredMethods.toList().union(methods.toList()).toTypedArray()

    methods.forEach { method ->
        if (Pattern.matches(namePattern, method.name)) {
            result.add(method)
        }
    }

    return result
}