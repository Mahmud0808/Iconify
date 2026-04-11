@file:Suppress("unused")

package com.drdisagree.iconify.xposed.modules.extras.utils.toolkit

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.toPx
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.defaultClassLoader
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.frameworkClassLoader
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.hookMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.log
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.runAfter
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.runBefore
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedInterface.CtorInvoker
import io.github.libxposed.api.XposedInterface.ExceptionMode
import io.github.libxposed.api.XposedInterface.HookHandle
import io.github.libxposed.api.XposedInterface.Invoker
import java.lang.ref.WeakReference
import java.lang.reflect.Constructor
import java.lang.reflect.Executable
import java.lang.reflect.Method
import java.util.regex.Pattern
import de.robv.android.xposed.XposedHelpers as RobvHelpers

typealias HookCallback = (MethodHookParam) -> Unit
typealias HookCallbackWithProceed = (param: MethodHookParam, proceed: Proceed) -> Any?

class Proceed(private val action: (Array<Any?>?) -> Any?) {
    operator fun invoke(overrideArgs: Array<Any?>? = null): Any? {
        return action(overrideArgs)
    }
}

object XposedHook {

    private var defaultClassLoader: ClassLoader? = null
    private var frameworkClassLoader: ClassLoader? = null
    lateinit var defaultXposedInterface: XposedInterface

    fun setDefaultClassLoader(classLoader: ClassLoader?) {
        defaultClassLoader = classLoader
    }

    fun setFrameworkClassLoader(classLoader: ClassLoader?) {
        frameworkClassLoader = classLoader
    }

    fun setXposedInterface(xposedInterface: XposedInterface) {
        this.defaultXposedInterface = xposedInterface
    }

    /**
     * Tries to find and load a class from a list of possible names.
     *
     * It searches through the provided [classLoader], the [defaultClassLoader],
     * and finally the [frameworkClassLoader] (if available).
     *
     * @param classNames One or more class names to search for. The function returns the first one found.
     * @param classLoader The specific [ClassLoader] to use. If null, uses the default ones.
     * @param suppressError If true, prevents logging an error message if the class is not found.
     * @param throwException If true, throws a [Throwable] if no classes are found.
     * @return The [Class] object if found, or null otherwise.
     * @throws IllegalStateException If no class loader has been initialized.
     * @throws Throwable If [throwException] is true and the class is not found.
     */
    fun findClass(
        vararg classNames: String,
        classLoader: ClassLoader? = null,
        suppressError: Boolean = false,
        throwException: Boolean = false
    ): Class<*>? {
        if (classLoader == null && defaultClassLoader == null && frameworkClassLoader == null) {
            throw IllegalStateException("XposedHook must be initialized first")
        }

        fun tryLoad(name: String, loader: ClassLoader): Class<*>? = try {
            loader.loadClass(name)
        } catch (_: ClassNotFoundException) {
            null
        }

        classNames.forEach { name ->
            tryLoad(
                name,
                classLoader ?: defaultClassLoader ?: frameworkClassLoader!!
            )?.let { return it }
        }

        if (throwException) {
            if (classNames.size == 1)
                throw Throwable("Class not found: ${classNames[0]}")
            else
                throw Throwable("None of the classes were found: ${classNames.joinToString()}")
        } else if (!suppressError) {
            if (classNames.size == 1)
                log(XposedHook, "Class not found: ${classNames[0]}")
            else
                log(XposedHook, "None of the classes were found: ${classNames.joinToString()}")
        }

        return null
    }

    /**
     * Prepares a hook for one or more methods within the current class.
     *
     * This returns a [MethodHookHelper] which allows for further configuration,
     * such as specifying parameter types, setting priority, and defining the
     * callback logic (before, after, or replace).
     *
     * @param methodNames The names of the methods to be hooked.
     * @return A [MethodHookHelper] instance to configure and apply the hook.
     */
    fun Class<*>?.hookMethod(vararg methodNames: String): MethodHookHelper {
        return MethodHookHelper(
            clazz = this,
            methodNames = methodNames,
            xposedInterface = defaultXposedInterface
        )
    }

    /**
     * Prepares a hook for the constructor(s) of the given class.
     *
     * If [MethodHookHelper.parameters] is called on the returned helper, it will target
     * the specific constructor matching those parameter types. Otherwise, it will
     * target all declared constructors of the class.
     *
     * @return A [MethodHookHelper] instance to configure and apply the hook.
     */
    fun Class<*>?.hookConstructor(): MethodHookHelper {
        return MethodHookHelper(
            clazz = this,
            xposedInterface = defaultXposedInterface
        )
    }

    /**
     * Prepares to hook all methods in the class that match the provided regular expression pattern.
     *
     * This allows for bulk hooking of methods based on naming conventions or obfuscated names
     * that follow a specific format.
     *
     * @param methodNamePattern A regular expression string to match against method names.
     * @return A [MethodHookHelper] to further configure parameters and the hook callback.
     */
    fun Class<*>?.hookMethodMatchPattern(methodNamePattern: String): MethodHookHelper {
        return MethodHookHelper(
            clazz = this,
            methodNames = arrayOf(methodNamePattern),
            isPattern = true,
            xposedInterface = defaultXposedInterface
        )
    }

    /**
     * Hooks the [Method] and provides a [callback] that can intercept the execution,
     * modify arguments, and manually decide when to call the original implementation.
     *
     * @param callback A lambda receiving [MethodHookParam] and a [Proceed] function.
     * @return A [MethodHookHelper] instance for further configuration or unhooking.
     */
    fun Method.run(callback: HookCallbackWithProceed): MethodHookHelper {
        return MethodHookHelper(method = this, xposedInterface = defaultXposedInterface)
            .run(callback)
    }

    /**
     * Hooks the current [Method] to run the provided [callback] before the original method is executed.
     *
     * @param callback The lambda to be executed before the original method.
     *                 It receives a [MethodHookParam] which can be used to read/modify arguments
     *                 or set a result to skip the original method.
     * @return A [MethodHookHelper] instance to allow for further configuration or unhooking.
     */
    fun Method.runBefore(callback: HookCallback): MethodHookHelper {
        return MethodHookHelper(method = this, xposedInterface = defaultXposedInterface)
            .runBefore(callback)
    }

    /**
     * Hooks the [Method] to run a [callback] after the original method has executed.
     *
     * @param callback The logic to execute after the original method call.
     * @return A [MethodHookHelper] instance to further configure or track the hook.
     */
    fun Method.runAfter(callback: HookCallback): MethodHookHelper {
        return MethodHookHelper(method = this, xposedInterface = defaultXposedInterface)
            .runAfter(callback)
    }

    /**
     * Replaces the original implementation of this [Method] with the provided [callback].
     *
     * Unlike [runBefore] or [runAfter], the original method will not be executed unless
     * specifically handled. The return value of the method will be the value assigned to
     * [MethodHookParam.result] within the callback.
     *
     * @param callback A lambda that receives the [MethodHookParam] to handle the replacement logic.
     * @return A [MethodHookHelper] instance for further configuration or unhooking.
     */
    fun Method.replace(callback: HookCallback): MethodHookHelper {
        return MethodHookHelper(method = this, xposedInterface = defaultXposedInterface)
            .replace(callback)
    }

    /**
     * Returns an ORIGIN invoker for the given method.
     *
     * Calling through this invoker will execute the original implementation,
     * bypassing all registered hooks.
     *
     * @param method The method to create an invoker for.
     * @return An invoker set to [Invoker.Type.ORIGIN].
     */
    fun originInvoker(method: Method): Invoker<*, Method> =
        defaultXposedInterface.getInvoker(method).also { it.setType(Invoker.Type.ORIGIN) }

    /**
     * Returns an ORIGIN invoker for the given constructor — calls the original
     * implementation, bypassing all hooks.
     */
    fun originInvoker(constructor: Constructor<*>): CtorInvoker<*> =
        defaultXposedInterface.getInvoker(constructor).also { it.setType(Invoker.Type.ORIGIN) }

    /**
     * Returns a FULL-CHAIN invoker for the given method — calls the method
     * normally, running through all registered hooks.
     */
    fun fullChainInvoker(method: Method): Invoker<*, Method> =
        defaultXposedInterface.getInvoker(method).also { it.setType(Invoker.Type.Chain.FULL) }

    /**
     * Returns a chain invoker for the given method that starts the hook chain
     * from [maxPriority]. Hooks with a priority higher than [maxPriority]
     * are skipped.
     */
    fun chainInvoker(method: Method, maxPriority: Int): Invoker<*, Method> =
        defaultXposedInterface.getInvoker(method)
            .also { it.setType(Invoker.Type.Chain(maxPriority)) }

    /**
     * Invokes a method as "special" (i.e. non-virtually, like super.foo())
     * using the invoker API.
     *
     * @param method      the method to invoke
     * @param thisObject  the receiver
     * @param args        arguments to pass
     */
    fun invokeSpecial(method: Method, thisObject: Any, vararg args: Any?): Any? {
        val invoker = defaultXposedInterface.getInvoker(method)
        return invoker.invokeSpecial(thisObject, *args)
    }

    /**
     * Creates a new instance "specially" (calls a specific constructor
     * in a superclass) using the invoker API.
     *
     * @param constructor the constructor to invoke
     * @param args        arguments to pass
     */
    fun <T, U> newInstanceSpecial(
        constructor: Constructor<T>,
        asClass: Class<U>,
        vararg args: Any?
    ): U {
        val invoker = defaultXposedInterface.getInvoker(constructor)
        return invoker.newInstanceSpecial(asClass, *args)
    }

    /**
     * Deoptimizes the executable (method or constructor) so that hooks
     * on it take effect even if it was previously JIT-compiled.
     *
     * @param executable The method or constructor to deoptimize.
     * @return true if deoptimization succeeded.
     */
    fun deoptimize(executable: Executable): Boolean =
        defaultXposedInterface.deoptimize(executable)

    /**
     * Convenience overload — deoptimizes the given [Method] so that hooks
     * on it take effect even if it was previously JIT-compiled.
     *
     * @param method the method to deoptimize
     * @return true if deoptimization succeeded, false if method is null
     */
    fun deoptimize(method: Method?): Boolean =
        method?.let { defaultXposedInterface.deoptimize(it) } ?: false

    fun log(message: Any?) {
        defaultXposedInterface.log(0, "Iconify", message.toString())
    }

    fun log(tag: String, message: Any?) {
        defaultXposedInterface.log(0, "Iconify - $tag", message.toString())
    }

    fun <T : Any> log(clazz: T, message: Any?) {
        defaultXposedInterface.log(
            0,
            "Iconify - ${clazz.javaClass.simpleName.replace($$"$Companion", "")}",
            message.toString()
        )
    }

    fun <T : Any> log(clazz: T, throwable: Throwable?) {
        defaultXposedInterface.log(
            1,
            "Iconify - ${clazz.javaClass.simpleName.replace($$"$Companion", "")}",
            throwable.toString()
        )
    }

    fun <T : Any> log(clazz: T, exception: Exception?) {
        defaultXposedInterface.log(
            1,
            "Iconify - ${clazz.javaClass.simpleName.replace($$"$Companion", "")}",
            exception.toString()
        )
    }

    fun <T : Any> log(clazz: T, message: Any?, throwable: Throwable?) {
        defaultXposedInterface.log(
            1,
            "Iconify - ${clazz.javaClass.simpleName.replace($$"$Companion", "")}",
            message.toString(),
            throwable
        )
    }
}

class MethodHookHelper(
    private val clazz: Class<*>?,
    private val methodNames: Array<out String>? = null,
    private val isPattern: Boolean = false,
    private val method: Method? = null,
    private val xposedInterface: XposedInterface
) {

    constructor(
        clazz: Class<*>?,
        methodNames: Array<out String>? = null,
        isPattern: Boolean = false,
        xposedInterface: XposedInterface
    ) : this(clazz, methodNames, isPattern, null, xposedInterface)

    constructor(
        method: Method,
        xposedInterface: XposedInterface
    ) : this(null, null, false, method, xposedInterface)

    private var parameterTypes: Array<Any?>? = null
    private var priority: Int = 0
    private var printError: Boolean = true
    private var throwError: Boolean = false
    private val unhooks = mutableSetOf<HookHandle>()

    /**
     * Sets the parameter types to match when looking up the method or constructor to hook.
     *
     * @param parameterTypes An array of [Class] objects representing the method's parameter types.
     * If not set or empty, all overloads of the method may be hooked depending on the dispatcher.
     * @return This [MethodHookHelper] instance for chaining.
     */
    @Suppress("UNCHECKED_CAST")
    fun parameters(vararg parameterTypes: Any?): MethodHookHelper {
        this.parameterTypes = parameterTypes as Array<Any?>?
        return this
    }

    /**
     * Executes the hook for the specified method, methods matching a pattern, or constructor.
     *
     * This version provides a [Proceed] callback, allowing the hook to manually control
     * when (or if) the original method is called, as well as modify arguments before
     * the call or the result after the call.
     *
     * @param callback A lambda that receives the [MethodHookParam] and a [Proceed] function.
     * @return This [MethodHookHelper] instance for chaining.
     */
    fun run(callback: HookCallbackWithProceed): MethodHookHelper {
        if (method != null) {
            hookExecutable(method, callback)
        } else if (methodNames.isNullOrEmpty()) {
            hookConstructor(callback)
        } else {
            dispatchMethods { hookExecutable(it, callback) }
        }
        return this
    }

    /**
     * Hooks the specified method(s) or constructor to run a callback before the original
     * implementation is executed.
     *
     * Inside the callback, you can access and modify method arguments, or set a result/throwable
     * to skip the original method execution (return early).
     *
     * @param callback The function to execute before the hooked method.
     * @return The current [MethodHookHelper] instance for chaining.
     */
    fun runBefore(callback: HookCallback): MethodHookHelper {
        if (method != null) {
            hookExecutable(method, before = callback)
        } else if (methodNames.isNullOrEmpty()) {
            hookConstructor(before = callback)
        } else {
            dispatchMethods { hookExecutable(it, before = callback) }
        }
        return this
    }

    /**
     * Hooks the specified method(s) or constructor(s) to execute a callback after the original
     * implementation has finished.
     *
     * The callback is invoked even if the original method threw an exception, unless the
     * exception was handled by a previous hook.
     *
     * @param callback The lambda to execute after the method completion.
     * @return This [MethodHookHelper] instance for chaining.
     */
    fun runAfter(callback: HookCallback): MethodHookHelper {
        if (method != null) {
            hookExecutable(method, after = callback)
        } else if (methodNames.isNullOrEmpty()) {
            hookConstructor(after = callback)
        } else {
            dispatchMethods { hookExecutable(it, after = callback) }
        }
        return this
    }

    /**
     * Replaces the original implementation of the method(s) or constructor(s) with the provided [callback].
     *
     * Unlike [run], this does not provide a proceed block; the original method will not be called
     * unless manually handled. If the callback sets [MethodHookParam.result], that value will be
     * returned to the caller. If [MethodHookParam.throwable] is set, that exception will be thrown.
     *
     * @param callback A function to execute in place of the original method.
     * @return The current [MethodHookHelper] instance for chaining.
     */
    fun replace(callback: HookCallback): MethodHookHelper {
        if (method != null) {
            hookExecutableReplace(method, callback)
        } else {
            dispatchMethods(
                onNotFound = { name ->
                    if (printError && clazz != null && methodNames?.size == 1)
                        log(XposedHook, "Method not found: $name in ${clazz.simpleName}")
                    else if (throwError)
                        throw Throwable("Method not found: $name in ${clazz?.simpleName}")
                }
            ) { hookExecutableReplace(it, callback) }
        }
        return this
    }

    /**
     * Sets the priority of this hook. Hooks with higher priority are called first.
     *
     * @param priority The priority value.
     * @return This [MethodHookHelper] instance for chaining.
     */
    fun setPriority(priority: Int): MethodHookHelper {
        this.priority = priority
        return this
    }

    /**
     * Gets the set of [HookHandle]s representing all active hooks
     * managed by this [MethodHookHelper] instance.
     *
     * @return A [Set] of hook handles that can be used to manually unhook specific methods.
     */
    fun getUnhooks(): Set<HookHandle> = unhooks

    /**
     * Unhooks all methods and constructors previously registered by this [MethodHookHelper] instance.
     *
     * This iterates through all active [XposedInterface.HookHandle]s stored in this helper,
     * calls [XposedInterface.HookHandle.unhook] on each, and then clears the internal list.
     */
    fun unhookAll() {
        unhooks.forEach { it.unhook() }
        unhooks.clear()
    }

    /**
     * Disables error logging for this hook if the target method or constructor cannot be found.
     *
     * By default, the helper will log an error to the Xposed log if a hook fails to find
     * its target. Calling this method prevents that behavior.
     *
     * @return This [MethodHookHelper] instance for chaining.
     */
    fun suppressError(): MethodHookHelper {
        printError = false
        return this
    }

    /**
     * Configures the helper to throw a [Throwable] if the target method or constructor
     * cannot be found. This also automatically calls [suppressError] to prevent
     * duplicate logging.
     *
     * @return This [MethodHookHelper] instance for chaining.
     */
    fun throwError(): MethodHookHelper {
        suppressError()
        throwError = true
        return this
    }

    private fun makeParam(
        executable: Executable,
        thisObject: Any?,
        args: Array<Any?>?
    ): MethodHookParam {
        return MethodHookParam().also {
            it.thisObject = thisObject
            it.method = executable
            it.args = args
        }
    }

    private fun hookExecutable(
        executable: Executable,
        before: (HookCallback)? = null,
        after: (HookCallback)? = null
    ) {
        val hooker = xposedInterface.hook(executable)
            .setExceptionMode(resolveExceptionMode())
            .setPriority(priority)
            .intercept { chain ->
                val param = makeParam(
                    chain.executable,
                    chain.thisObject,
                    chain.args.toTypedArray()
                )

                val body = {
                    before?.invoke(param)

                    if (param.hasThrowable()) throw param.throwable!!

                    if (!param.isReturnEarly) {
                        try {
                            param.result = chain.proceed(param.args)
                        } catch (t: Throwable) {
                            param.throwable = t
                        }

                        after?.invoke(param)
                    }

                    if (param.hasThrowable()) throw param.throwable!!
                    param.result
                }

                if (!printError && !throwError) {
                    runCatching { body() }.getOrElse { param.result }
                } else {
                    body()
                }
            }
        unhooks += hooker
    }

    private fun hookExecutable(
        executable: Executable,
        callback: HookCallbackWithProceed
    ) {
        val hooker = xposedInterface.hook(executable)
            .setExceptionMode(resolveExceptionMode())
            .setPriority(priority)
            .intercept { chain ->
                val param = makeParam(
                    chain.executable,
                    chain.thisObject,
                    chain.args.toTypedArray()
                )

                val proceed = Proceed { overrideArgs ->
                    val argsToUse = overrideArgs ?: param.args
                    chain.proceed(argsToUse)
                }

                val body = {
                    val result = callback(param, proceed)
                    if (param.hasThrowable()) throw param.throwable!!
                    result ?: param.result
                }

                if (!printError && !throwError) {
                    runCatching { body() }.getOrElse { param.result }
                } else {
                    body()
                }
            }
        unhooks += hooker
    }

    private fun hookExecutableReplace(
        executable: Executable,
        callback: HookCallback
    ) {
        val hooker = xposedInterface.hook(executable)
            .setExceptionMode(resolveExceptionMode())
            .setPriority(priority)
            .intercept { chain ->
                val param = makeParam(
                    chain.executable,
                    chain.thisObject,
                    chain.args.toTypedArray()
                )

                val body = {
                    callback(param)
                    if (param.hasThrowable()) throw param.throwable!!
                    param.result
                }

                if (!printError && !throwError) {
                    runCatching { body() }.getOrElse { param.result }
                } else {
                    body()
                }
            }
        unhooks += hooker
    }

    private fun hookConstructor(
        before: (HookCallback)? = null,
        after: (HookCallback)? = null
    ): MethodHookHelper {
        if (clazz == null) return this
        resolveConstructors().forEach { hookExecutable(it, before, after) }
        return this
    }

    private fun hookConstructor(
        callback: HookCallbackWithProceed
    ): MethodHookHelper {
        if (clazz == null) return this
        resolveConstructors().forEach { hookExecutable(it, callback) }
        return this
    }

    private fun resolveConstructors(): List<Constructor<*>> {
        if (clazz == null) return emptyList()
        return if (parameterTypes.isNullOrEmpty()) {
            clazz.declaredConstructors.toList()
        } else {
            val types = parameterTypes!!.filterIsInstance<Class<*>>().toTypedArray()
            try {
                listOf(clazz.getDeclaredConstructor(*types))
            } catch (_: NoSuchMethodException) {
                if (printError) log(XposedHook, "Constructor not found in ${clazz.simpleName}")
                else if (throwError) throw Throwable("Constructor not found in ${clazz.simpleName}")
                emptyList()
            }
        }
    }

    private fun dispatchMethods(
        onNotFound: ((String) -> Unit)? = null,
        action: (Method) -> Unit
    ) {
        if (clazz == null) return
        var foundAny = false

        val candidates = (clazz.declaredMethods.toList() + clazz.methods.toList()).distinctBy { it }

        methodNames?.forEach { methodName ->
            if (isPattern) {
                val pattern = Pattern.compile(methodName)
                candidates
                    .filter { pattern.matcher(it.name).matches() && matchesParamTypes(it) }
                    .forEach { action(it); foundAny = true }
            } else {
                candidates
                    .filter { it.name == methodName && matchesParamTypes(it) }
                    .forEach { action(it); foundAny = true }
            }
        }

        if (!foundAny) {
            val errorMessage =
                "Method(s) not found: ${methodNames?.joinToString()} in Class ${clazz.simpleName}"
            if (onNotFound != null) {
                onNotFound(methodNames?.firstOrNull() ?: "?")
            } else if (printError) {
                log(XposedHook, errorMessage)
            } else if (throwError) {
                throw Throwable(errorMessage)
            }
        }
    }

    private fun matchesParamTypes(method: Method): Boolean {
        if (parameterTypes.isNullOrEmpty()) return true
        val expected = parameterTypes!!.filterIsInstance<Class<*>>()
        return method.parameterTypes.toList() == expected
    }

    private fun resolveExceptionMode(): ExceptionMode = when {
        printError -> ExceptionMode.PROTECTIVE
        throwError -> ExceptionMode.PASSTHROUGH
        else -> ExceptionMode.PASSTHROUGH
    }
}

object ResourceHookManager {

    private val hookedResources = mutableListOf<HookData>()
    private var contextRef: WeakReference<Context>? = null

    fun init(context: Context) {
        contextRef = WeakReference(context)

        applyHooks()
    }

    fun hookDimen(): HookBuilder {
        return HookBuilder(HookType.DIMENSION)
    }

    fun hookBoolean(): HookBuilder {
        return HookBuilder(HookType.BOOLEAN)
    }

    fun hookInteger(): HookBuilder {
        return HookBuilder(HookType.INTEGER)
    }

    private fun applyHooks() {
        val context = contextRef?.get() ?: throw IllegalStateException("Context cannot be null")

        HookType.entries.forEach { hookType ->
            hookType.methods.forEach { method ->
                Resources::class.java
                    .hookMethod(method)
                    .run { param, proceed ->
                        val hookData = hookedResources.find {
                            it.method == method && it.resId == param.args[0] && it.condition.invoke()
                        } ?: return@run proceed()

                        if (method == "getDimensionPixelSize") {
                            param.result = context.toPx(hookData.value.invoke() as Int)
                        } else {
                            param.result = hookData.value.invoke()
                        }
                    }
            }
        }
    }

    class HookBuilder(private val hookType: HookType) {

        private var packageName: String? = null
        private var condition: () -> Boolean = { true }
        private val resourcesToHook = mutableListOf<HookData>()

        fun whenCondition(condition: () -> Boolean): HookBuilder {
            this.condition = condition
            return this
        }

        fun forPackageName(packageName: String): HookBuilder {
            this.packageName = packageName
            return this
        }

        @SuppressLint("DiscouragedApi")
        fun addResource(name: String, value: () -> Any): HookBuilder {
            val context = contextRef?.get() ?: return this
            if (packageName == null) throw IllegalArgumentException("packageName must be set")

            val resId = context.resources.getIdentifier(
                name,
                hookType.resourceType,
                packageName
            )

            if (resId != 0) {
                hookType.methods.forEach { method ->
                    resourcesToHook.add(HookData(resId, method, value, condition))
                }
            }

            return this
        }

        fun apply() {
            resourcesToHook.forEach { resource ->
                if (!hookedResources.contains(resource)) {
                    hookedResources.add(resource)
                }
            }
        }
    }

    data class HookData(
        val resId: Int,
        val method: String,
        val value: () -> Any,
        val condition: () -> Boolean
    )

    enum class HookType(val resourceType: String, val methods: List<String>) {
        BOOLEAN(
            "bool",
            listOf("getBoolean")
        ),
        INTEGER(
            "integer",
            listOf("getInteger")
        ),
        DIMENSION(
            "dimen",
            listOf("getDimension", "getDimensionPixelOffset", "getDimensionPixelSize")
        )
    }
}

object XposedHelpers {

    fun newInstance(clazz: Class<*>?, vararg args: Any?): Any? {
        return RobvHelpers.newInstance(clazz, *args)
    }

    fun Any?.callMethod(methodName: String): Any? {
        if (this == null) return null

        return try {
            RobvHelpers.callMethod(this, methodName)
        } catch (t: Throwable) {
            log(XposedHook, "Method not found: $methodName in ${this::class.java.simpleName}")
            null
        }
    }

    fun Any?.callMethod(methodName: String, vararg args: Any?): Any? {
        if (this == null) return null

        return try {
            RobvHelpers.callMethod(this, methodName, *args)
        } catch (t: Throwable) {
            val argTypes = args.map { it?.javaClass?.simpleName ?: "null" }
            log(
                XposedHook,
                "Method not found: $methodName with arguments ${argTypes.joinToString()} in ${this::class.java.simpleName}"
            )
            null
        }
    }

    fun Any?.callMethodSilently(methodName: String): Any? {
        if (this == null) return null

        return try {
            RobvHelpers.callMethod(this, methodName)
        } catch (_: Throwable) {
            null
        }
    }

    fun Any?.callMethodSilently(methodName: String, vararg args: Any?): Any? {
        if (this == null) return null

        return try {
            RobvHelpers.callMethod(this, methodName, *args)
        } catch (_: Throwable) {
            null
        }
    }

    fun Class<*>?.callStaticMethod(methodName: String): Any? {
        if (this == null) return null

        return try {
            RobvHelpers.callStaticMethod(this, methodName)
        } catch (t: Throwable) {
            log(XposedHook, "Static method not found: $methodName in ${this.simpleName}")
            null
        }
    }

    fun Class<*>?.callStaticMethod(methodName: String, vararg args: Any?): Any? {
        if (this == null) return null

        return try {
            RobvHelpers.callStaticMethod(this, methodName, *args)
        } catch (t: Throwable) {
            val argTypes = args.map { it?.javaClass?.simpleName ?: "null" }
            log(
                XposedHook,
                "Static method not found: $methodName with arguments ${argTypes.joinToString()} in ${this.simpleName}"
            )
            null
        }
    }

    fun Class<*>?.callStaticMethodSilently(methodName: String): Any? {
        if (this == null) return null

        return try {
            RobvHelpers.callStaticMethod(this, methodName)
        } catch (_: Throwable) {
            null
        }
    }

    fun Class<*>?.callStaticMethodSilently(methodName: String, vararg args: Any?): Any? {
        if (this == null) return null

        return try {
            RobvHelpers.callStaticMethod(this, methodName, *args)
        } catch (_: Throwable) {
            null
        }
    }

    fun Any?.getField(fieldName: String): Any {
        if (this == null) throw NoSuchFieldError("Field not found: $fieldName, object is null")

        return RobvHelpers.getObjectField(this, fieldName)
    }

    fun Any?.getFieldSilently(fieldName: String): Any? {
        if (this == null) return null

        return try {
            RobvHelpers.getObjectField(this, fieldName)
        } catch (_: Throwable) {
            null
        }
    }

    fun Any?.setField(fieldName: String, value: Any?) {
        if (this == null) return

        RobvHelpers.setObjectField(this, fieldName, value)
    }

    fun Any?.setFieldSilently(fieldName: String, value: Any?) {
        if (this == null) return

        try {
            RobvHelpers.setObjectField(this, fieldName, value)
        } catch (_: Throwable) {
        }
    }

    fun Any?.getAnyField(vararg fieldNames: String): Any? {
        if (this == null) return null

        fieldNames.forEach { name ->
            try {
                return RobvHelpers.getObjectField(this, name)
            } catch (_: Throwable) {
            }
        }
        log(
            XposedHook,
            "Field not found: any of [${fieldNames.joinToString()}] in ${this::class.java.simpleName}"
        )
        return null
    }

    fun Any?.setAnyField(value: Any?, vararg fieldNames: String) {
        if (this == null) return

        fieldNames.forEach { name ->
            try {
                RobvHelpers.setObjectField(this, name, value)
                return
            } catch (_: Throwable) {
            }
        }
        log(
            XposedHook,
            "Field not found: any of [${fieldNames.joinToString()}] in ${this::class.java.simpleName}"
        )
    }

    fun Class<*>?.getStaticField(fieldName: String): Any {
        if (this == null) throw NoSuchFieldError("Static field not found: $fieldName, class is null")

        return RobvHelpers.getStaticObjectField(this, fieldName)
    }

    fun Class<*>?.getStaticFieldSilently(fieldName: String): Any? {
        if (this == null) return null

        return try {
            RobvHelpers.getStaticObjectField(this, fieldName)
        } catch (_: Throwable) {
            null
        }
    }

    fun Class<*>?.setStaticField(fieldName: String, value: Any?) {
        if (this == null) return

        try {
            RobvHelpers.setStaticObjectField(this, fieldName, value)
        } catch (t: Throwable) {
            log(XposedHook, "Static field not found: $fieldName in ${this.simpleName}")
        }
    }

    fun Class<*>?.setStaticFieldSilently(fieldName: String, value: Any?) {
        if (this == null) return

        try {
            RobvHelpers.setStaticObjectField(this, fieldName, value)
        } catch (_: Throwable) {
        }
    }

    fun Class<*>?.getAnyStaticField(vararg fieldNames: String): Any? {
        if (this == null) return null

        fieldNames.forEach { name ->
            try {
                return RobvHelpers.getStaticObjectField(this, name)
            } catch (_: Throwable) {
            }
        }
        log(
            XposedHook,
            "Static field not found: any of [${fieldNames.joinToString()}] in ${this.simpleName}"
        )
        return null
    }

    fun Any?.getExtraField(fieldName: String): Any {
        if (this == null) throw NoSuchFieldError("Extra field not found: $fieldName, object is null")

        return RobvHelpers.getAdditionalInstanceField(this, fieldName)
    }

    fun Any?.getExtraFieldSilently(fieldName: String): Any? {
        if (this == null) return null

        return try {
            RobvHelpers.getAdditionalInstanceField(this, fieldName)
        } catch (_: Throwable) {
            log(
                XposedHook,
                "Extra field not found: $fieldName in ${this::class.java.simpleName}"
            )
            null
        }
    }

    fun Any?.setExtraField(fieldName: String, value: Any?) {
        if (this == null) return

        RobvHelpers.setAdditionalInstanceField(this, fieldName, value)
    }
}