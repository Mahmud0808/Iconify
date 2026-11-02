package com.drdisagree.iconify.xposed.core.resources

import com.drdisagree.iconify.xposed.core.hook.UnhookHandle

data class ResourceOffset(
    val resId: Int,
    val value: () -> Float,
    val condition: () -> Boolean,
    val owner: UnhookHandle
)
