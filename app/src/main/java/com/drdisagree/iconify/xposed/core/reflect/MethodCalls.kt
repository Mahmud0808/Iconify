@file:Suppress("unused")

package com.drdisagree.iconify.xposed.core.reflect

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

private fun Method.invokeUnwrapped(receiver: Any?, args: Array<out Any?>): Any? = try {
    invoke(receiver, *args)
} catch (exception: InvocationTargetException) {
    throw exception.cause ?: exception
}

fun Any?.callMethod(methodName: String, vararg args: Any?): Any? {
    if (this == null) return null
    return MemberResolver.findMethodBestMatch(javaClass, methodName, args).invokeUnwrapped(this, args)
}

fun Any?.callMethodSilently(methodName: String, vararg args: Any?): Any? = try {
    callMethod(methodName, *args)
} catch (_: Throwable) {
    null
}

fun Class<*>?.callStaticMethod(methodName: String, vararg args: Any?): Any? {
    if (this == null) return null
    return MemberResolver.findMethodBestMatch(this, methodName, args).invokeUnwrapped(null, args)
}

fun Class<*>?.callStaticMethodSilently(methodName: String, vararg args: Any?): Any? = try {
    callStaticMethod(methodName, *args)
} catch (_: Throwable) {
    null
}

fun Class<*>?.createInstance(vararg args: Any?): Any? {
    if (this == null) return null
    return try {
        MemberResolver.findConstructorBestMatch(this, args).newInstance(*args)
    } catch (exception: InvocationTargetException) {
        throw exception.cause ?: exception
    }
}
