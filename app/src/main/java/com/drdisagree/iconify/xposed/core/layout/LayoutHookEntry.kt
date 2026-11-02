package com.drdisagree.iconify.xposed.core.layout

import com.drdisagree.iconify.xposed.core.hook.UnhookHandle

data class LayoutHookEntry(
    val packageName: String,
    val resourceType: String,
    val resourceName: String,
    val callback: (LayoutInflatedParam) -> Unit,
    val owner: UnhookHandle
)
