package com.drdisagree.iconify.xposed.core.layout

import com.drdisagree.iconify.xposed.core.hook.UnhookHandle
import com.drdisagree.iconify.xposed.core.log.log

class LayoutHookHelper {

    private var packageName: String? = null
    private var resourceType: String = "layout"
    private var resourceName: String? = null
    private var printError = true
    private var throwError = false
    private val handle = UnhookHandle()

    fun packageName(packageName: String): LayoutHookHelper = apply {
        this.packageName = packageName
    }

    fun resource(resourceType: String, resourceName: String): LayoutHookHelper = apply {
        this.resourceType = resourceType
        this.resourceName = resourceName
    }

    fun suppressError(): LayoutHookHelper = apply {
        printError = false
    }

    fun throwError(): LayoutHookHelper = apply {
        printError = false
        throwError = true
    }

    fun run(callback: (LayoutInflatedParam) -> Unit): LayoutHookHelper = apply {
        val packageName = requireNotNull(packageName) { "packageName must be set" }
        val resourceName = requireNotNull(resourceName) { "resourceName must be set" }

        val registered = LayoutHookManager.register(
            LayoutHookEntry(packageName, resourceType, resourceName, callback, handle)
        )

        if (!registered) {
            val message = "Layout not found: $packageName:$resourceType/$resourceName"
            when {
                throwError -> throw IllegalStateException(message)
                printError -> log("LayoutHookHelper", message)
            }
        }
    }

    fun getUnhookHandle(): UnhookHandle = handle
}
