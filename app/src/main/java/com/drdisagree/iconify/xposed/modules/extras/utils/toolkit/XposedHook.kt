@file:Suppress("unused")

package com.drdisagree.iconify.xposed.modules.extras.utils.toolkit

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.content.res.XResources
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.ViewHelper.toPx
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.ResourceHookManager.init
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge.hookAllConstructors
import de.robv.android.xposed.XposedBridge.hookMethod
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.XposedHelpers.findAndHookConstructor
import de.robv.android.xposed.XposedHelpers.getStaticObjectField
import de.robv.android.xposed.callbacks.XC_LayoutInflated
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.lang.ref.WeakReference
import java.lang.reflect.Method
import java.util.regex.Pattern

class XposedHook {
    companion object {
        lateinit var loadPackageParam: XC_LoadPackage.LoadPackageParam

        fun init(loadPackageParam: XC_LoadPackage.LoadPackageParam) {
            this.loadPackageParam = loadPackageParam
        }

        /**
         * Tries each [classNames] in order, returning the first one found.
         * Returns `null` on failure, logging or throwing based on [suppressError]
         * / [throwException].
         */
        fun findClass(
            vararg classNames: String,
            suppressError: Boolean = false,
            throwException: Boolean = false
        ): Class<*>? {
            if (!this::loadPackageParam.isInitialized) {
                throw IllegalStateException("XposedHook.init() must be called before findClass()")
            }

            for (name in classNames) {
                val clazz = XposedHelpers.findClassIfExists(name, loadPackageParam.classLoader)
                if (clazz != null) return clazz
            }

            val msg = if (classNames.size == 1) "Class not found: ${classNames[0]}"
            else "None of the classes were found: ${classNames.joinToString()}"

            when {
                throwException -> throw Throwable(msg)
                !suppressError -> log(XposedHook, msg)
            }

            return null
        }
    }
}

class MethodHookHelper private constructor(
    private val clazz: Class<*>?,
    private val methodNames: Array<out String>?,
    private val isPattern: Boolean,
    private val directMethod: Method?
) {

    constructor(
        clazz: Class<*>?,
        methodNames: Array<out String>? = null,
        isPattern: Boolean = false
    ) : this(clazz, methodNames, isPattern, null)

    constructor(method: Method) : this(null, null, false, method)

    private var parameterTypes: Array<Any?>? = null
    private var printError = true
    private var throwError = false
    private val handle = UnhookHandle()

    @Suppress("UNCHECKED_CAST")
    fun parameters(vararg parameterTypes: Any?): MethodHookHelper {
        this.parameterTypes = parameterTypes as Array<Any?>
        return this
    }

    /** Suppress "not found" log output. */
    fun suppressError(): MethodHookHelper {
        printError = false
        return this
    }

    /** Throw instead of logging when a target cannot be found. */
    fun throwError(): MethodHookHelper {
        suppressError(); throwError = true
        return this
    }

    /** Install a raw [XC_MethodHook] that manages both before and after. */
    fun run(callback: XC_MethodHook): MethodHookHelper =
        hookWithCallback(callback)

    /** Run [callback] **before** the hooked method body executes. */
    fun runBefore(callback: (XC_MethodHook.MethodHookParam) -> Unit): MethodHookHelper =
        hookWithCallback(object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) = callback(param)
        })

    /** Run [callback] **after** the hooked method returns. */
    fun runAfter(callback: (XC_MethodHook.MethodHookParam) -> Unit): MethodHookHelper =
        hookWithCallback(object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) = callback(param)
        })

    /**
     * Replace the hooked method entirely — the original body is **not** called.
     * Set `param.result` inside [callback] to provide a return value.
     */
    fun replace(callback: (XC_MethodHook.MethodHookParam) -> Unit): MethodHookHelper =
        hookWithCallback(object : XC_MethodReplacement() {
            override fun replaceHookedMethod(param: MethodHookParam): Any? {
                callback(param)
                return param.result
            }
        })

    /**
     * Returns the live [UnhookHandle] that accumulates every token registered
     * through this builder.  Safe to call at any point in the chain.
     */
    fun getUnhookHandle(): UnhookHandle = handle

    private fun hookWithCallback(callback: XC_MethodHook): MethodHookHelper {
        when {
            directMethod != null -> handle.add(hookMethod(directMethod, callback))

            methodNames.isNullOrEmpty() -> {
                if (clazz == null) return this
                if (parameterTypes.isNullOrEmpty()) {
                    hookAllConstructors(clazz, callback).forEach { handle.add(it) }
                } else {
                    handle.add(findAndHookConstructor(clazz, *parameterTypes!!, callback))
                }
            }

            else -> {
                var foundAny = false
                methodNames.forEach { nameOrPattern ->
                    findCandidates(nameOrPattern).forEach { method ->
                        handle.add(hookMethod(method, callback))
                        foundAny = true
                    }
                }
                if (!foundAny && clazz != null) {
                    val msg = "Method(s) not found: ${methodNames.joinToString()} " +
                            "in ${clazz.simpleName}"
                    when {
                        throwError -> throw Throwable(msg)
                        printError -> log(XposedHook, msg)
                    }
                }
            }
        }
        return this
    }

    /**
     * Returns all methods on [clazz] whose name matches [nameOrPattern].
     *
     * Merges declared (private/protected) and inherited (public) methods,
     * de-duplicated by signature.  When [parameterTypes] is set the result is
     * narrowed to the best-matching overload(s).
     */
    private fun findCandidates(nameOrPattern: String): List<Method> {
        if (clazz == null) return emptyList()

        // Merge declared + inherited, de-duplicate by name + parameter signature
        val allMethods = (clazz.declaredMethods.asSequence() + clazz.methods.asSequence())
            .distinctBy { m -> m.name + m.parameterTypes.joinToString(",") { it.name } }
            .toList()

        // Filter by name or compiled pattern
        val matched = if (isPattern) {
            val pattern = Pattern.compile(nameOrPattern)
            allMethods.filter { pattern.matcher(it.name).matches() }
        } else {
            allMethods.filter { it.name == nameOrPattern }
        }

        // Optionally narrow to a specific overload
        if (!parameterTypes.isNullOrEmpty()) {
            return matched.filter { method ->
                val types = method.parameterTypes
                if (types.size != parameterTypes!!.size) return@filter false
                types.indices.all { i ->
                    when (val expected = parameterTypes!![i]) {
                        null -> true
                        is Class<*> -> types[i] == expected
                        is String -> types[i].name == expected
                        else -> false
                    }
                }
            }
        }

        return matched
    }
}

fun Class<*>?.hookMethod(vararg methodNames: String): MethodHookHelper =
    MethodHookHelper(clazz = this, methodNames = methodNames)

fun Class<*>?.hookConstructor(): MethodHookHelper =
    MethodHookHelper(clazz = this)

fun Class<*>?.hookMethodMatchPattern(methodNamePattern: String): MethodHookHelper =
    MethodHookHelper(clazz = this, methodNames = arrayOf(methodNamePattern), isPattern = true)

fun Method.run(callback: XC_MethodHook): MethodHookHelper =
    MethodHookHelper(this).run(callback)

fun Method.runBefore(callback: (XC_MethodHook.MethodHookParam) -> Unit): MethodHookHelper =
    MethodHookHelper(this).runBefore(callback)

fun Method.runAfter(callback: (XC_MethodHook.MethodHookParam) -> Unit): MethodHookHelper =
    MethodHookHelper(this).runAfter(callback)

fun Method.replace(callback: (XC_MethodHook.MethodHookParam) -> Unit): MethodHookHelper =
    MethodHookHelper(this).replace(callback)

fun XResources.hookLayout(): LayoutHookHelper = LayoutHookHelper(this)

class LayoutHookHelper(private val xResources: XResources) {

    private var packageName: String? = null
    private var resourceType: String? = null
    private var resourceName: String? = null
    private var printError = true
    private var throwError = false
    private val handle = UnhookHandle()

    fun packageName(packageName: String): LayoutHookHelper {
        this.packageName = packageName
        return this
    }

    fun resource(resourceType: String, resourceName: String): LayoutHookHelper {
        this.resourceType = resourceType; this.resourceName = resourceName
        return this
    }

    fun run(callback: (XC_LayoutInflated.LayoutInflatedParam) -> Unit): LayoutHookHelper {
        requireNotNull(packageName) { "packageName must be set" }
        requireNotNull(resourceType) { "resourceType must be set" }
        requireNotNull(resourceName) { "resourceName must be set" }

        try {
            val unhook = xResources.hookLayout(
                packageName, resourceType, resourceName,
                object : XC_LayoutInflated() {
                    override fun handleLayoutInflated(param: LayoutInflatedParam) = callback(param)
                }
            )
            handle.add(unhook)
        } catch (t: Throwable) {
            when {
                throwError -> throw t; printError -> log(XposedHook, t)
            }
        }

        return this
    }

    /** Returns the [UnhookHandle] for every layout hook registered here. */
    fun getUnhookHandle(): UnhookHandle = handle

    /** Silence error logs. Call before [run]. */
    fun suppressError(): LayoutHookHelper {
        printError = false
        return this
    }

    /** Throw on errors. Call before [run]. */
    fun throwError(): LayoutHookHelper {
        suppressError(); throwError = true
        return this
    }
}

object ResourceHookManager {

    private val hookedResources = mutableListOf<HookData>()
    private var contextRef: WeakReference<Context>? = null
    private val handle = UnhookHandle()

    fun init(context: Context) {
        contextRef = WeakReference(context)
        applyHooks()
    }

    fun hookDimen(): HookBuilder = HookBuilder(HookType.DIMENSION)
    fun hookBoolean(): HookBuilder = HookBuilder(HookType.BOOLEAN)
    fun hookInteger(): HookBuilder = HookBuilder(HookType.INTEGER)

    /**
     * Returns the [UnhookHandle] for every [Resources] method hook installed
     * during [init].  Call [UnhookHandle.unhook] to remove them all.
     */
    fun getUnhookHandle(): UnhookHandle = handle

    /**
     * Removes all [Resources] hooks and clears the registered resource list.
     * Call [init] again to re-apply hooks later.
     */
    fun unhookAll() {
        handle.unhook()
        hookedResources.clear()
    }

    private fun applyHooks() {
        val context = contextRef?.get()
            ?: throw IllegalStateException("Context is null — call init() first")

        HookType.entries.forEach { hookType ->
            hookType.methods.forEach { methodName ->
                val methodHandle = Resources::class.java
                    .hookMethod(methodName)
                    .runBefore { param ->
                        val data = hookedResources.find {
                            it.method == methodName &&
                                    it.resId == param.args[0] &&
                                    it.condition()
                        } ?: return@runBefore

                        param.result = when (methodName) {
                            "getDimensionPixelSize",
                            "getDimensionPixelOffset" -> context.toPx(data.value() as Int)

                            else -> data.value()
                        }
                    }
                    .getUnhookHandle()

                handle.merge(methodHandle)
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
            requireNotNull(packageName) { "forPackageName() must be called first" }

            val resId = context.resources.getIdentifier(
                name,
                hookType.resourceType,
                packageName
            )

            if (resId != 0) {
                hookType.methods.forEach { method ->
                    resourcesToHook.add(HookData(resId, method, value, condition, UnhookHandle()))
                }
            }

            return this
        }

        fun apply(): UnhookHandle {
            val localHandle = UnhookHandle()

            resourcesToHook.forEach { pending ->
                val isDuplicate = hookedResources.any { existing ->
                    existing.resId == pending.resId && existing.method == pending.method
                }

                if (!isDuplicate) {
                    hookedResources.add(pending.copy(owner = localHandle))
                }
            }

            localHandle.addCustom {
                hookedResources.removeAll { it.owner == localHandle }
            }

            return localHandle
        }
    }

    data class HookData(
        val resId: Int,
        val method: String,
        val value: () -> Any,
        val condition: () -> Boolean,
        val owner: UnhookHandle
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

class UnhookHandle {

    // We store a list of lambdas that perform the unhooking action
    private val unhookActions = mutableSetOf<() -> Unit>()

    val size: Int get() = unhookActions.size
    val isEmpty: Boolean get() = unhookActions.isEmpty()

    /** Reverts every hook (Method, Layout, etc.) tracked by this handle. */
    fun unhook() {
        unhookActions.forEach { it.invoke() }
        unhookActions.clear()
    }

    /** Add a Method unhook token */
    internal fun add(token: XC_MethodHook.Unhook) {
        unhookActions.add { token.unhook() }
    }

    /** Add a Layout unhook token */
    internal fun add(token: XC_LayoutInflated.Unhook) {
        unhookActions.add { token.unhook() }
    }

    /**
     * Adds a custom cleanup action to be executed when [unhook] is called.
     * Use this to manage non-standard hooks or resource cleanup that aren't
     * automatically handled by the standard Xposed unhook tokens.
     */
    internal fun addCustom(action: () -> Unit) {
        unhookActions.add(action)
    }

    /** Merges another handle into this one */
    internal fun merge(other: UnhookHandle) {
        unhookActions.addAll(other.unhookActions)
    }

    operator fun plus(other: UnhookHandle): UnhookHandle {
        val newHandle = UnhookHandle()
        newHandle.unhookActions.addAll(this.unhookActions)
        newHandle.unhookActions.addAll(other.unhookActions)
        return newHandle
    }
}

fun Any?.callMethod(methodName: String): Any? {
    if (this == null) return null
    return XposedHelpers.callMethod(this, methodName)
}

fun Any?.callMethod(methodName: String, vararg args: Any?): Any? {
    if (this == null) return null
    return XposedHelpers.callMethod(this, methodName, *args)
}

fun Any?.callMethodSilently(methodName: String): Any? {
    if (this == null) return null
    return try {
        XposedHelpers.callMethod(this, methodName)
    } catch (_: Throwable) {
        null
    }
}

fun Any?.callMethodSilently(methodName: String, vararg args: Any?): Any? {
    if (this == null) return null
    return try {
        XposedHelpers.callMethod(this, methodName, *args)
    } catch (_: Throwable) {
        null
    }
}

fun Class<*>?.callStaticMethod(methodName: String): Any? {
    if (this == null) return null
    return XposedHelpers.callStaticMethod(this, methodName)
}

fun Class<*>?.callStaticMethod(methodName: String, vararg args: Any?): Any? {
    if (this == null) return null
    return XposedHelpers.callStaticMethod(this, methodName, *args)
}

fun Class<*>?.callStaticMethodSilently(methodName: String): Any? {
    if (this == null) return null
    return try {
        XposedHelpers.callStaticMethod(this, methodName)
    } catch (_: Throwable) {
        null
    }
}

fun Class<*>?.callStaticMethodSilently(methodName: String, vararg args: Any?): Any? {
    if (this == null) return null
    return try {
        XposedHelpers.callStaticMethod(this, methodName, *args)
    } catch (_: Throwable) {
        null
    }
}

fun Any?.getField(fieldName: String): Any {
    if (this == null) throw NoSuchFieldError("Field not found: $fieldName — object is null")
    return XposedHelpers.getObjectField(this, fieldName)
}

fun Any?.getFieldSilently(fieldName: String): Any? {
    if (this == null) return null
    return try {
        XposedHelpers.getObjectField(this, fieldName)
    } catch (_: Throwable) {
        null
    }
}

fun Any?.setField(fieldName: String, value: Any?) =
    XposedHelpers.setObjectField(this, fieldName, value)

fun Any?.setFieldSilently(fieldName: String, value: Any?) {
    try {
        XposedHelpers.setObjectField(this, fieldName, value)
    } catch (_: Throwable) {
    }
}

fun Class<*>?.getStaticField(fieldName: String): Any {
    if (this == null) throw NoSuchFieldError("Field not found: $fieldName — class is null")
    return getStaticObjectField(this, fieldName)
}

fun Class<*>?.getStaticFieldSilently(fieldName: String): Any? {
    if (this == null) return null
    return try {
        getStaticObjectField(this, fieldName)
    } catch (_: Throwable) {
        null
    }
}

fun Class<*>?.setStaticField(fieldName: String, value: Any?) =
    XposedHelpers.setStaticObjectField(this, fieldName, value)

fun Class<*>?.setStaticFieldSilently(fieldName: String, value: Any?) {
    try {
        XposedHelpers.setStaticObjectField(this, fieldName, value)
    } catch (_: Throwable) {
    }
}

fun Any?.getAnyField(vararg fieldNames: String): Any? {
    fieldNames.forEach { fieldName ->
        try {
            return XposedHelpers.getObjectField(this, fieldName)
        } catch (_: Throwable) {
        }
    }
    throw NoSuchFieldError("Field not found: ${fieldNames.joinToString()}")
}

fun Any?.setAnyField(value: Any?, vararg fieldNames: String) {
    fieldNames.forEach { fieldName ->
        try {
            return XposedHelpers.setObjectField(this, fieldName, value)
        } catch (_: Throwable) {
        }
    }
    throw NoSuchFieldError("Field not found: ${fieldNames.joinToString()}")
}

fun Class<*>?.getAnyStaticField(vararg fieldNames: String): Any? {
    fieldNames.forEach { fieldName ->
        try {
            return getStaticObjectField(this, fieldName)
        } catch (_: Throwable) {
        }
    }
    throw NoSuchFieldError("Field not found: ${fieldNames.joinToString()}")
}

fun Any?.getExtraField(fieldName: String): Any =
    XposedHelpers.getAdditionalInstanceField(this, fieldName)

fun Any?.getExtraFieldSilently(fieldName: String): Any? =
    try {
        XposedHelpers.getAdditionalInstanceField(this, fieldName)
    } catch (_: Throwable) {
        null
    }

fun Any?.setExtraField(fieldName: String, value: Any?) {
    XposedHelpers.setAdditionalInstanceField(this, fieldName, value)
}