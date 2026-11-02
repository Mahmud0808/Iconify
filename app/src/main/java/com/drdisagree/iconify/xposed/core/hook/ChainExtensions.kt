@file:Suppress("unused")

package com.drdisagree.iconify.xposed.core.hook

import io.github.libxposed.api.XposedInterface

fun XposedInterface.Chain.argsCopy(): Array<Any?> = args.toTypedArray()

inline fun XposedInterface.Chain.proceedWithArgs(transform: (Array<Any?>) -> Unit): Any? {
    val args = argsCopy()
    transform(args)
    return proceed(args)
}

val XposedInterface.Chain.instance: Any
    get() = thisObject ?: throw IllegalStateException("${executable.name} is static")
