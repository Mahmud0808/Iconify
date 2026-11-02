package com.drdisagree.iconify.xposed.core.resources

import android.annotation.SuppressLint
import com.drdisagree.iconify.xposed.core.hook.UnhookHandle

class ResourceHookBuilder internal constructor(private val type: ResourceHookType) {

    private var packageName: String? = null
    private var condition: () -> Boolean = { true }
    private val pendingReplacements = mutableListOf<ResourceReplacement>()
    private val pendingOffsets = mutableListOf<ResourceOffset>()

    fun whenCondition(condition: () -> Boolean): ResourceHookBuilder = apply {
        this.condition = condition
    }

    fun forPackageName(packageName: String): ResourceHookBuilder = apply {
        this.packageName = packageName
    }

    fun addResource(name: String, value: () -> Any): ResourceHookBuilder = apply {
        val resId = resourceId(name)
        if (resId != 0) {
            pendingReplacements += ResourceReplacement(resId, type, value, condition, UnhookHandle())
        }
    }

    fun addResources(names: Iterable<String>, value: () -> Any): ResourceHookBuilder = apply {
        names.forEach { addResource(it, value) }
    }

    fun addOffset(name: String, offset: () -> Float): ResourceHookBuilder = apply {
        require(type.supportsOffset) { "addOffset() is not supported for ${type.name}" }
        val resId = resourceId(name)
        if (resId != 0) {
            pendingOffsets += ResourceOffset(resId, offset, condition, UnhookHandle())
        }
    }

    fun apply(): UnhookHandle = UnhookHandle().also { owner ->
        ResourceHookManager.register(type, pendingReplacements, pendingOffsets, owner)
    }

    @SuppressLint("DiscouragedApi")
    private fun resourceId(name: String): Int {
        val context = ResourceHookManager.context ?: return 0
        val packageName = requireNotNull(packageName) { "forPackageName() must be called first" }
        return context.resources.getIdentifier(name, type.resourceType, packageName)
    }
}
