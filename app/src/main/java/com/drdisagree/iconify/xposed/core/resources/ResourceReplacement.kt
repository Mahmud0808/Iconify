package com.drdisagree.iconify.xposed.core.resources

import com.drdisagree.iconify.xposed.core.hook.UnhookHandle

data class ResourceReplacement(
    val resId: Int,
    val type: ResourceHookType,
    val value: () -> Any,
    val condition: () -> Boolean,
    val owner: UnhookHandle
)
