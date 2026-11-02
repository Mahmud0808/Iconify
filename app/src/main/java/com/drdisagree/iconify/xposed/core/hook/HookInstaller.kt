package com.drdisagree.iconify.xposed.core.hook

import com.drdisagree.iconify.xposed.core.XposedRuntime
import io.github.libxposed.api.XposedInterface
import java.lang.reflect.Constructor
import java.lang.reflect.Executable
import java.lang.reflect.Method

object HookInstaller {

    fun hook(
        executable: Executable,
        hooker: XposedInterface.Hooker,
        priority: Int = XposedInterface.PRIORITY_DEFAULT
    ): XposedInterface.HookHandle = XposedRuntime.api
        .hook(executable)
        .setPriority(priority)
        .setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE)
        .intercept(hooker)

    fun invokeOriginal(executable: Executable, thisObject: Any?, vararg args: Any?): Any? =
        when (executable) {
            is Method -> XposedRuntime.api
                .getInvoker(executable)
                .setType(XposedInterface.Invoker.Type.ORIGIN)
                .invoke(thisObject, *args)

            is Constructor<*> -> XposedRuntime.api
                .getInvoker(executable)
                .setType(XposedInterface.Invoker.Type.ORIGIN)
                .let { invoker ->
                    if (thisObject == null) invoker.newInstance(*args)
                    else invoker.invoke(thisObject, *args)
                }

            else -> throw IllegalArgumentException("Unsupported executable: $executable")
        }

    fun deoptimize(executable: Executable): Boolean = XposedRuntime.api.deoptimize(executable)
}
