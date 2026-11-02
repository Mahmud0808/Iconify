@file:Suppress("unused")

package com.drdisagree.iconify.xposed.core.hook

import io.github.libxposed.api.XposedInterface
import java.lang.reflect.Executable

fun Class<*>?.hookMethod(vararg methodNames: String): MethodHookHelper =
    MethodHookHelper(clazz = this, methodNames = methodNames)

fun Class<*>?.hookConstructor(): MethodHookHelper =
    MethodHookHelper(clazz = this)

fun Class<*>?.hookMethodMatchPattern(methodNamePattern: String): MethodHookHelper =
    MethodHookHelper(clazz = this, methodNames = arrayOf(methodNamePattern), isPattern = true)

fun Executable.intercept(hooker: (XposedInterface.Chain) -> Any?): MethodHookHelper =
    MethodHookHelper(this).intercept(hooker)

fun Executable.runBefore(callback: (XposedInterface.Chain) -> Unit): MethodHookHelper =
    MethodHookHelper(this).runBefore(callback)

fun Executable.runAfter(callback: (XposedInterface.Chain, Any?) -> Unit): MethodHookHelper =
    MethodHookHelper(this).runAfter(callback)

fun Executable.invokeOriginal(thisObject: Any?, vararg args: Any?): Any? =
    HookInstaller.invokeOriginal(this, thisObject, *args)
