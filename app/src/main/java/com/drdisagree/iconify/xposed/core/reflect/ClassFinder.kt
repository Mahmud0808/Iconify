@file:Suppress("unused")

package com.drdisagree.iconify.xposed.core.reflect

import com.drdisagree.iconify.xposed.core.XposedRuntime
import com.drdisagree.iconify.xposed.core.log.log

fun findClass(
    vararg classNames: String,
    suppressError: Boolean = false,
    throwException: Boolean = false
): Class<*>? {
    check(XposedRuntime.isPackageReady) { "findClass() called before the package is ready" }

    for (name in classNames) {
        findClassIfExists(name, XposedRuntime.classLoader)?.let { return it }
    }

    val message = if (classNames.size == 1) "Class not found: ${classNames[0]}"
    else "None of the classes were found: ${classNames.joinToString()}"

    when {
        throwException -> throw ClassNotFoundException(message)
        !suppressError -> log("ClassFinder", message)
    }

    return null
}

fun findClassIfExists(className: String, classLoader: ClassLoader?): Class<*>? = try {
    Class.forName(className, false, classLoader)
} catch (_: ClassNotFoundException) {
    null
} catch (_: LinkageError) {
    null
}

fun findClassOrThrow(className: String, classLoader: ClassLoader?): Class<*> =
    Class.forName(className, false, classLoader)
