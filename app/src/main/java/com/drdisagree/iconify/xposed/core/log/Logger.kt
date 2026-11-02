@file:Suppress("unused")

package com.drdisagree.iconify.xposed.core.log

import android.util.Log
import com.drdisagree.iconify.xposed.core.XposedRuntime

private const val LOG_TAG = "Iconify"

private fun write(priority: Int, message: String, throwable: Throwable? = null) {
    if (XposedRuntime.isAttached) {
        XposedRuntime.api.log(priority, LOG_TAG, message, throwable)
    } else {
        Log.println(priority, LOG_TAG, message + (throwable?.let { "\n" + Log.getStackTraceString(it) } ?: ""))
    }
}

private fun Any.logName(): String = when (this) {
    is String -> this
    is Class<*> -> simpleName
    else -> javaClass.simpleName.replace($$"$Companion", "").ifEmpty { javaClass.name }
}

fun log(message: String?) {
    write(Log.INFO, message.toString())
}

fun log(message: Any?) {
    if (message is Throwable) write(Log.ERROR, message.toString(), message)
    else write(Log.INFO, message.toString())
}

fun log(tag: String, message: Any?) {
    if (message is Throwable) write(Log.ERROR, "$tag: $message", message)
    else write(Log.INFO, "$tag: $message")
}

fun <T : Any> log(owner: T, message: Any?) {
    if (message is Throwable) write(Log.ERROR, "${owner.logName()}: $message", message)
    else write(Log.INFO, "${owner.logName()}: $message")
}

fun <T : Any> log(owner: T, throwable: Throwable?) {
    write(Log.ERROR, "${owner.logName()}: $throwable", throwable)
}
