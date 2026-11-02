package com.drdisagree.iconify.xposed.core.hook

import com.drdisagree.iconify.xposed.core.log.log
import com.drdisagree.iconify.xposed.core.reflect.MemberResolver
import io.github.libxposed.api.XposedInterface
import java.lang.reflect.Executable

class MethodHookHelper private constructor(
    private val clazz: Class<*>?,
    private val methodNames: Array<out String>?,
    private val isPattern: Boolean,
    private val directMethod: Executable?
) {

    constructor(
        clazz: Class<*>?,
        methodNames: Array<out String>? = null,
        isPattern: Boolean = false
    ) : this(clazz, methodNames, isPattern, null)

    constructor(executable: Executable) : this(null, null, false, executable)

    private var parameterTypes: Array<out Any?>? = null
    private var printError = true
    private var throwError = false
    private var priority = XposedInterface.PRIORITY_DEFAULT
    private val handle = UnhookHandle()

    fun parameters(vararg parameterTypes: Any?): MethodHookHelper = apply {
        this.parameterTypes = parameterTypes
    }

    fun priority(priority: Int): MethodHookHelper = apply {
        this.priority = priority
    }

    fun suppressError(): MethodHookHelper = apply {
        printError = false
    }

    fun throwError(): MethodHookHelper = apply {
        printError = false
        throwError = true
    }

    fun intercept(hooker: (XposedInterface.Chain) -> Any?): MethodHookHelper =
        install { chain -> hooker(chain) }

    fun runBefore(callback: (XposedInterface.Chain) -> Unit): MethodHookHelper =
        install { chain ->
            callback(chain)
            chain.proceed()
        }

    fun runAfter(callback: (XposedInterface.Chain, Any?) -> Unit): MethodHookHelper =
        install { chain ->
            chain.proceed().also { result -> callback(chain, result) }
        }

    fun getUnhookHandle(): UnhookHandle = handle

    private fun install(hooker: XposedInterface.Hooker): MethodHookHelper {
        val targets = resolveTargets()

        if (targets.isEmpty()) {
            reportMissing()
            return this
        }

        targets.forEach { executable ->
            try {
                handle.add(HookInstaller.hook(executable, hooker, priority))
            } catch (throwable: Throwable) {
                if (throwError) throw throwable
                if (printError) log(TAG, "Failed to hook $executable: $throwable")
            }
        }

        return this
    }

    private fun resolveTargets(): List<Executable> {
        directMethod?.let { return listOf(it) }
        val clazz = clazz ?: return emptyList()

        if (methodNames.isNullOrEmpty()) {
            return MemberResolver.findConstructors(clazz, parameterTypes)
        }

        return methodNames.flatMap { name ->
            MemberResolver.findMethods(clazz, name, isPattern, parameterTypes)
        }
    }

    private fun reportMissing() {
        val clazz = clazz ?: return
        val isConstructor = methodNames.isNullOrEmpty()
        if (isConstructor && parameterTypes.isNullOrEmpty() && !throwError) return

        val message = if (isConstructor) {
            "Constructor not found in ${clazz.simpleName}"
        } else {
            "Method(s) not found: ${methodNames?.joinToString()} in ${clazz.simpleName}"
        }

        when {
            throwError -> throw NoSuchMethodError(message)
            printError -> log(TAG, message)
        }
    }

    private companion object {
        const val TAG = "MethodHookHelper"
    }
}
