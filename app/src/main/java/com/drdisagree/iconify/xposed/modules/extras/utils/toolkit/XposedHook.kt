package com.drdisagree.iconify.xposed.modules.extras.utils.toolkit

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.content.res.XResources
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.toPx
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge.hookAllConstructors
import de.robv.android.xposed.XposedBridge.hookAllMethods
import de.robv.android.xposed.XposedBridge.hookMethod
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.XposedHelpers.findAndHookConstructor
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
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
            Companion.loadPackageParam =
                loadPackageParam
        }

        fun findClass(
            vararg classNames: String,
            suppressError: Boolean = false,
            throwException: Boolean = false
        ): Class<*>? {
            if (Companion::loadPackageParam.isInitialized.not()) {
                throw IllegalStateException("XposedHook.init() must be called before XposedHook.findClass()")
            }

            for (className in classNames) {
                val clazz = XposedHelpers.findClassIfExists(
                    className,
                    loadPackageParam.classLoader
                )
                if (clazz != null) return clazz
            }

            if (throwException) {
                if (classNames.size == 1) {
                    throw Throwable("Class not found: ${classNames[0]}")
                } else {
                    throw Throwable("None of the classes were found: ${classNames.joinToString()}")
                }
            } else if (!suppressError) {
                if (classNames.size == 1) {
                    log(XposedHook, "Class not found: ${classNames[0]}")
                } else {
                    log(XposedHook, "None of the classes were found: ${classNames.joinToString()}")
                }
            }

            return null
        }
    }
}

fun Class<*>?.hookMethod(vararg methodNames: String): MethodHookHelper {
    return MethodHookHelper(
        this,
        methodNames
    )
}

fun Class<*>?.hookConstructor(): MethodHookHelper {
    return MethodHookHelper(this)
}

fun Class<*>?.hookMethodMatchPattern(methodNamePattern: String): MethodHookHelper {
    return MethodHookHelper(
        this,
        arrayOf(methodNamePattern),
        true
    )
}

class MethodHookHelper(
    private val clazz: Class<*>?,
    private val methodNames: Array<out String>? = null,
    private val isPattern: Boolean = false,
    private val method: Method? = null
) {

    constructor(
        clazz: Class<*>?,
        methodNames: Array<out String>? = null,
        isPattern: Boolean = false
    ) : this(
        clazz,
        methodNames,
        isPattern,
        null
    )

    constructor(
        method: Method
    ) : this(
        null,
        null,
        false,
        method
    )

    private var parameterTypes: Array<Any?>? = null
    private var printError: Boolean = true
    private var throwError: Boolean = false

    @Suppress("UNCHECKED_CAST")
    fun parameters(vararg parameterTypes: Any?): MethodHookHelper {
        this.parameterTypes = parameterTypes as Array<Any?>?
        return this
    }

    fun run(callback: XC_MethodHook): MethodHookHelper {
        if (method != null) { // hooking directly via Method instance
            hookMethod(method, callback)
        } else if (methodNames.isNullOrEmpty()) { // hooking constructor
            hookConstructor(callback)
        } else { // hooking method
            var foundAnyMethod = false

            methodNames.forEach { methodName ->
                if (isPattern) {
                    val pattern = Pattern.compile(methodName)
                    clazz?.declaredMethods?.toList()?.union(clazz.methods.toList())
                        ?.forEach { method ->
                        if (pattern.matcher(method.name).matches()) {
                            hookMethod(method, callback)
                            foundAnyMethod = true
                        }
                    }
                } else {
                    clazz?.declaredMethods?.toList()?.union(clazz.methods.toList())
                        ?.find { it.name == methodName }?.let { method ->
                            hookMethod(method, callback)
                            foundAnyMethod = true
                        }
                }
            }

            if (!foundAnyMethod) {
                val errorMessage =
                    "Method(s) not found: ${methodNames.joinToString()} in Class ${clazz?.simpleName}"

                if (printError && clazz != null) {
                    log(XposedHook, errorMessage)
                } else if (throwError) {
                    throw Throwable(errorMessage)
                }
            }
        }

        return this
    }

    fun runBefore(callback: (XC_MethodHook.MethodHookParam) -> Unit): MethodHookHelper {
        if (method != null) { // hooking directly via Method instance
            hookMethodBefore(method, callback)
        } else if (methodNames.isNullOrEmpty()) { // hooking constructor
            hookConstructorBefore(callback)
        } else { // hooking method
            var foundAnyMethod = false

            methodNames.forEach { methodName ->
                if (isPattern) {
                    val pattern = Pattern.compile(methodName)
                    clazz?.declaredMethods?.toList()?.union(clazz.methods.toList())
                        ?.forEach { method ->
                        if (pattern.matcher(method.name).matches()) {
                            hookMethodBefore(method, callback)
                            foundAnyMethod = true
                        }
                    }
                } else {
                    clazz?.declaredMethods?.toList()?.union(clazz.methods.toList())
                        ?.find { it.name == methodName }?.let { method ->
                            hookMethodBefore(method, callback)
                            foundAnyMethod = true
                        }
                }
            }

            if (!foundAnyMethod) {
                val errorMessage =
                    "Method(s) not found: ${methodNames.joinToString()} in Class ${clazz?.simpleName}"

                if (printError && clazz != null) {
                    log(XposedHook, errorMessage)
                } else if (throwError) {
                    throw Throwable(errorMessage)
                }
            }
        }

        return this
    }

    fun runAfter(callback: (XC_MethodHook.MethodHookParam) -> Unit): MethodHookHelper {
        if (method != null) { // hooking directly via Method instance
            hookMethodAfter(method, callback)
        } else if (methodNames.isNullOrEmpty()) { // hooking constructor
            hookConstructorAfter(callback)
        } else { // hooking method
            var foundAnyMethod = false

            methodNames.forEach { methodName ->
                if (isPattern) {
                    val pattern = Pattern.compile(methodName)
                    clazz?.declaredMethods?.toList()?.union(clazz.methods.toList())
                        ?.forEach { method ->
                        if (pattern.matcher(method.name).matches()) {
                            hookMethodAfter(method, callback)
                            foundAnyMethod = true
                        }
                    }
                } else {
                    clazz?.declaredMethods?.toList()?.union(clazz.methods.toList())
                        ?.find { it.name == methodName }?.let { method ->
                            hookMethodAfter(method, callback)
                            foundAnyMethod = true
                        }
                }
            }

            if (!foundAnyMethod) {
                val errorMessage =
                    "Method(s) not found: ${methodNames.joinToString()} in Class ${clazz?.simpleName}"

                if (printError && clazz != null) {
                    log(XposedHook, errorMessage)
                } else if (throwError) {
                    throw Throwable(errorMessage)
                }
            }
        }

        return this
    }

    fun replace(callback: (XC_MethodHook.MethodHookParam) -> Unit): MethodHookHelper {
        if (method != null) { // hooking directly via Method instance
            hookMethodReplace(method, callback)
        } else {
            methodNames?.forEach { methodName ->
                if (isPattern) {
                    val pattern = Pattern.compile(methodName)
                    clazz?.declaredMethods?.toList()?.union(clazz.methods.toList())
                        ?.forEach { method ->
                        if (pattern.matcher(method.name).matches()) {
                            hookMethodReplace(method, callback)
                        }
                    }
                } else {
                    clazz?.declaredMethods?.toList()?.union(clazz.methods.toList())
                        ?.find { it.name == methodName }?.let { method ->
                        hookMethodReplace(method, callback)
                    } ?: run {
                        if (printError) {
                            if (clazz != null && methodNames!!.size == 1) {
                                log(
                                    XposedHook,
                                    "Method not found: $methodName in ${clazz.simpleName}"
                                )
                            }
                        } else if (throwError) {
                            throw Throwable("Method not found: $methodName in ${clazz?.simpleName}")
                        }
                    }
                }
            }
        }

        return this
    }

    private fun hookConstructor(callback: XC_MethodHook): MethodHookHelper {
        if (clazz == null) return this

        if (parameterTypes.isNullOrEmpty()) {
            hookAllConstructors(clazz, callback)
        } else {
            findAndHookConstructor(
                clazz,
                *parameterTypes!!,
                callback
            )
        }

        return this
    }

    private fun hookConstructorBefore(callback: (XC_MethodHook.MethodHookParam) -> Unit): MethodHookHelper {
        if (clazz == null) return this

        if (parameterTypes.isNullOrEmpty()) {
            hookAllConstructors(clazz, object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    callback(param)
                }
            })
        } else {
            findAndHookConstructor(
                clazz,
                *parameterTypes!!,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        callback(param)
                    }
                }
            )
        }

        return this
    }

    private fun hookConstructorAfter(callback: (XC_MethodHook.MethodHookParam) -> Unit): MethodHookHelper {
        if (clazz == null) return this

        if (parameterTypes.isNullOrEmpty()) {
            hookAllConstructors(clazz, object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    callback(param)
                }
            })
        } else {
            findAndHookConstructor(
                clazz,
                *parameterTypes!!,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        callback(param)
                    }
                }
            )
        }

        return this
    }

    private fun hookMethodBefore(
        method: Method,
        callback: (XC_MethodHook.MethodHookParam) -> Unit
    ) {
        if (clazz == null) {
            hookMethod(method, object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    callback(param)
                }
            })
        } else {
            if (parameterTypes.isNullOrEmpty()) {
                hookAllMethods(clazz, method.name, object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        callback(param)
                    }
                })
            } else {
                findAndHookMethod(
                    clazz,
                    method.name,
                    *parameterTypes!!,
                    object : XC_MethodHook() {
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            callback(param)
                        }
                    }
                )
            }
        }
    }

    private fun hookMethodAfter(
        method: Method,
        callback: (XC_MethodHook.MethodHookParam) -> Unit
    ) {
        if (clazz == null) {
            hookMethod(method, object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    callback(param)
                }
            })
        } else {
            if (parameterTypes.isNullOrEmpty()) {
                hookAllMethods(clazz, method.name, object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        callback(param)
                    }
                })
            } else {
                findAndHookMethod(
                    clazz,
                    method.name,
                    *parameterTypes!!,
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            callback(param)
                        }
                    }
                )
            }
        }
    }

    private fun hookMethodReplace(
        method: Method,
        callback: (XC_MethodHook.MethodHookParam) -> Unit
    ) {
        if (clazz == null) {
            hookMethod(method, object : XC_MethodReplacement() {
                override fun replaceHookedMethod(param: MethodHookParam): Any? {
                    callback(param)
                    return null
                }
            })
        } else {
            if (parameterTypes.isNullOrEmpty()) {
                hookAllMethods(clazz, method.name, object : XC_MethodReplacement() {
                    override fun replaceHookedMethod(param: MethodHookParam): Any? {
                        callback(param)
                        return null
                    }
                })
            } else {
                findAndHookMethod(
                    clazz,
                    method.name,
                    *parameterTypes!!,
                    object : XC_MethodReplacement() {
                        override fun replaceHookedMethod(param: MethodHookParam): Any? {
                            callback(param)
                            return null
                        }
                    }
                )
            }
        }
    }

    /*
     * Call before running any hook
     */
    fun suppressError(): MethodHookHelper {
        printError = false
        return this
    }

    /*
     * Call before running any hook
     */
    fun throwError(): MethodHookHelper {
        suppressError()
        throwError = true
        return this
    }
}

fun Method.run(callback: XC_MethodHook): MethodHookHelper {
    return MethodHookHelper(this)
        .run(callback)
}

fun Method.runBefore(callback: (XC_MethodHook.MethodHookParam) -> Unit): MethodHookHelper {
    return MethodHookHelper(this)
        .runBefore(callback)
}

fun Method.runAfter(callback: (XC_MethodHook.MethodHookParam) -> Unit): MethodHookHelper {
    return MethodHookHelper(this)
        .runAfter(callback)
}

fun Method.replace(callback: (XC_MethodHook.MethodHookParam) -> Unit): MethodHookHelper {
    return MethodHookHelper(this)
        .replace(callback)
}

fun XResources.hookLayout(): LayoutHookHelper {
    return LayoutHookHelper(this)
}

class LayoutHookHelper(private val xResources: XResources) {

    private var packageName: String? = null
    private var resourceType: String? = null
    private var resourceName: String? = null
    private var printError: Boolean = true
    private var throwError: Boolean = false

    fun packageName(packageName: String): LayoutHookHelper {
        this.packageName = packageName
        return this
    }

    fun resource(
        resourceType: String,
        resourceName: String
    ): LayoutHookHelper {
        this.resourceType = resourceType
        this.resourceName = resourceName
        return this
    }

    fun run(callback: (XC_LayoutInflated.LayoutInflatedParam) -> Unit): LayoutHookHelper {
        if (packageName == null || resourceType == null || resourceName == null) {
            throw IllegalArgumentException("packageName, resourceType and resourceName must be set")
        }

        try {
            xResources.hookLayout(
                packageName,
                resourceType,
                resourceName,
                object : XC_LayoutInflated() {
                    override fun handleLayoutInflated(param: LayoutInflatedParam) {
                        callback(param)
                    }
                }
            )
        } catch (throwable: Throwable) {
            if (printError) {
                log(XposedHook, throwable)
            } else if (throwError) {
                throw throwable
            }
        }

        return this
    }

    /*
     * Call before running any hook
     */
    fun suppressError(): LayoutHookHelper {
        printError = false
        return this
    }

    /*
     * Call before running any hook
     */
    fun throwError(): LayoutHookHelper {
        throwError = true
        return this
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
        val context = contextRef!!.get() ?: throw IllegalStateException("Context is null")

        HookType.entries.forEach { hookType ->
            hookType.methods.forEach { method ->
                Resources::class.java
                    .hookMethod(method)
                    .runBefore { param ->
                        val hookData = hookedResources.find {
                            it.method == method && it.resId == param.args[0] && it.condition.invoke()
                        } ?: return@runBefore

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
    if (this == null) throw NoSuchFieldError("Field not found: $fieldName, object is null")

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

fun Any?.setField(fieldName: String, value: Any?) {
    XposedHelpers.setObjectField(this, fieldName, value)
}

fun Any?.setFieldSilently(fieldName: String, value: Any?) {
    try {
        XposedHelpers.setObjectField(this, fieldName, value)
    } catch (_: Throwable) {
    }
}

fun Class<*>?.getStaticField(fieldName: String): Any {
    if (this == null) throw NoSuchFieldError("Field not found: $fieldName, class is null")

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

fun Class<*>?.setStaticField(fieldName: String, value: Any?) {
    XposedHelpers.setStaticObjectField(this, fieldName, value)
}

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

fun Any?.getExtraField(fieldName: String): Any {
    return XposedHelpers.getAdditionalInstanceField(this, fieldName)
}

fun Any?.getExtraFieldSilently(fieldName: String): Any? {
    return try {
        XposedHelpers.getAdditionalInstanceField(this, fieldName)
    } catch (_: Throwable) {
        null
    }
}

fun Any?.setExtraField(fieldName: String, value: Any?) {
    XposedHelpers.setAdditionalInstanceField(this, fieldName, value)
}