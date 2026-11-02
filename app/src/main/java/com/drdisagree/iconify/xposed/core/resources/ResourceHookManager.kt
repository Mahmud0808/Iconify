package com.drdisagree.iconify.xposed.core.resources

import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import com.drdisagree.iconify.xposed.core.XposedRuntime
import com.drdisagree.iconify.xposed.core.hook.UnhookHandle
import com.drdisagree.iconify.xposed.core.hook.hookMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.ViewHelper.toPx
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.roundToInt

object ResourceHookManager {

    private val replacements = ResourceHookType.entries
        .associateWith { ConcurrentHashMap<Int, CopyOnWriteArrayList<ResourceReplacement>>() }
    private val offsets = ConcurrentHashMap<Int, CopyOnWriteArrayList<ResourceOffset>>()
    private val hookedTypes = ConcurrentHashMap.newKeySet<ResourceHookType>()
    private val handle = UnhookHandle()

    @Volatile
    private var typedArrayOffsetsHooked = false

    internal val context: Context?
        get() = XposedRuntime.hostContext

    fun hookDimen(): ResourceHookBuilder = ResourceHookBuilder(ResourceHookType.DIMENSION)

    fun hookBoolean(): ResourceHookBuilder = ResourceHookBuilder(ResourceHookType.BOOLEAN)

    fun hookInteger(): ResourceHookBuilder = ResourceHookBuilder(ResourceHookType.INTEGER)

    fun hookDrawable(): ResourceHookBuilder = ResourceHookBuilder(ResourceHookType.DRAWABLE)

    fun getUnhookHandle(): UnhookHandle = handle

    fun unhookAll() {
        handle.unhook()
        replacements.values.forEach { it.clear() }
        offsets.clear()
        hookedTypes.clear()
        typedArrayOffsetsHooked = false
    }

    internal fun register(
        type: ResourceHookType,
        pendingReplacements: List<ResourceReplacement>,
        pendingOffsets: List<ResourceOffset>,
        owner: UnhookHandle
    ) {
        val typeReplacements = replacements.getValue(type)

        pendingReplacements.forEach { pending ->
            val entries = typeReplacements.getOrPut(pending.resId) { CopyOnWriteArrayList() }
            if (entries.isEmpty()) entries.add(pending.copy(owner = owner))
        }

        pendingOffsets.forEach { pending ->
            offsets.getOrPut(pending.resId) { CopyOnWriteArrayList() }.add(pending.copy(owner = owner))
        }

        if (pendingReplacements.isNotEmpty() || pendingOffsets.isNotEmpty()) hookType(type)
        if (pendingOffsets.isNotEmpty()) hookTypedArrayOffsets()

        owner.addCustom {
            typeReplacements.values.forEach { entries -> entries.removeAll { it.owner == owner } }
            typeReplacements.values.removeAll { it.isEmpty() }
            offsets.values.forEach { entries -> entries.removeAll { it.owner == owner } }
            offsets.values.removeAll { it.isEmpty() }
        }
    }

    private fun hookType(type: ResourceHookType) {
        if (!hookedTypes.add(type)) return
        val typeReplacements = replacements.getValue(type)

        type.methods.forEach { methodName ->
            val toPixels = type.convertsToPixels(methodName)

            handle.merge(
                Resources::class.java
                    .hookMethod(methodName)
                    .intercept { chain ->
                        val resId = chain.args.firstOrNull() as? Int
                            ?: return@intercept chain.proceed()

                        if (typeReplacements.isNotEmpty()) {
                            replacementFor(typeReplacements, resId, toPixels)?.let {
                                return@intercept it
                            }
                        }

                        val result = chain.proceed()
                        if (type.supportsOffset && offsets.isNotEmpty()) {
                            applyOffset(resId, result)
                        } else {
                            result
                        }
                    }
                    .getUnhookHandle()
            )

            if (type.replaceInTypedArray) {
                handle.merge(
                    TypedArray::class.java
                        .hookMethod(methodName)
                        .intercept { chain ->
                            if (typeReplacements.isEmpty()) return@intercept chain.proceed()
                            val index = chain.args.firstOrNull() as? Int
                                ?: return@intercept chain.proceed()
                            val resId = (chain.thisObject as TypedArray).getResourceId(index, 0)
                            if (resId == 0) return@intercept chain.proceed()
                            replacementFor(typeReplacements, resId, toPixels) ?: chain.proceed()
                        }
                        .getUnhookHandle()
                )
            }
        }
    }

    @Synchronized
    private fun hookTypedArrayOffsets() {
        if (typedArrayOffsetsHooked) return
        typedArrayOffsetsHooked = true

        ResourceHookType.DIMENSION.methods.forEach { methodName ->
            handle.merge(
                TypedArray::class.java
                    .hookMethod(methodName)
                    .intercept { chain ->
                        val result = chain.proceed()
                        if (offsets.isEmpty()) return@intercept result
                        val index = chain.args.firstOrNull() as? Int ?: return@intercept result
                        val resId = (chain.thisObject as TypedArray).getResourceId(index, 0)
                        if (resId != 0) applyOffset(resId, result) else result
                    }
                    .getUnhookHandle()
            )
        }
    }

    private fun replacementFor(
        typeReplacements: Map<Int, List<ResourceReplacement>>,
        resId: Int,
        toPixels: Boolean
    ): Any? {
        val data = typeReplacements[resId]?.firstOrNull { it.condition() } ?: return null
        val value = data.value()
        return if (toPixels) context?.toPx(value as Int) else value
    }

    private fun applyOffset(resId: Int, value: Any?): Any? {
        val context = context ?: return value
        val data = offsets[resId]?.firstOrNull { it.condition() } ?: return value
        val offset = data.value()
        if (offset == 0f) return value

        val offsetPx = offset * context.resources.displayMetrics.density
        return when (value) {
            is Float -> (value + offsetPx).coerceAtLeast(0f)
            is Int -> (value + offsetPx).roundToInt().coerceAtLeast(0)
            else -> value
        }
    }
}
