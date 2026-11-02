package com.drdisagree.iconify.xposed.core.hook

import io.github.libxposed.api.XposedInterface
import java.util.concurrent.CopyOnWriteArraySet

class UnhookHandle {

    private val unhookActions = CopyOnWriteArraySet<() -> Unit>()

    val size: Int get() = unhookActions.size
    val isEmpty: Boolean get() = unhookActions.isEmpty()

    fun unhook() {
        unhookActions.forEach { it.invoke() }
        unhookActions.clear()
    }

    internal fun add(handle: XposedInterface.HookHandle) {
        unhookActions.add { handle.unhook() }
    }

    internal fun addCustom(action: () -> Unit) {
        unhookActions.add(action)
    }

    internal fun merge(other: UnhookHandle) {
        unhookActions.addAll(other.unhookActions)
    }

    operator fun plus(other: UnhookHandle): UnhookHandle = UnhookHandle().also {
        it.unhookActions.addAll(unhookActions)
        it.unhookActions.addAll(other.unhookActions)
    }
}
