package com.drdisagree.iconify.xposed.core.layout

import android.annotation.SuppressLint
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.drdisagree.iconify.xposed.core.XposedRuntime
import com.drdisagree.iconify.xposed.core.hook.UnhookHandle
import com.drdisagree.iconify.xposed.core.hook.hookMethod
import com.drdisagree.iconify.xposed.core.log.log
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

object LayoutHookManager {

    private val entries = ConcurrentHashMap<Int, CopyOnWriteArrayList<LayoutHookEntry>>()
    private val handle = UnhookHandle()

    @Volatile
    private var hooked = false

    fun register(entry: LayoutHookEntry): Boolean {
        val resId = resolve(entry)
        if (resId == 0) return false

        entries.getOrPut(resId) { CopyOnWriteArrayList() }.add(entry)
        entry.owner.addCustom {
            entries[resId]?.remove(entry)
            entries.remove(resId, CopyOnWriteArrayList())
        }

        installHooks()
        return true
    }

    fun unhookAll() {
        handle.unhook()
        entries.clear()
        hooked = false
    }

    @SuppressLint("DiscouragedApi")
    private fun resolve(entry: LayoutHookEntry): Int {
        val context = XposedRuntime.hostContext ?: return 0
        return context.resources.getIdentifier(
            entry.resourceName,
            entry.resourceType,
            entry.packageName
        )
    }

    @Synchronized
    private fun installHooks() {
        if (hooked) return
        hooked = true

        handle.merge(
            LayoutInflater::class.java
                .hookMethod("inflate")
                .parameters(Int::class.javaPrimitiveType, ViewGroup::class.java, Boolean::class.javaPrimitiveType)
                .runAfter { chain, result ->
                    if (entries.isEmpty()) return@runAfter
                    val resId = chain.args[0] as? Int ?: return@runAfter
                    val view = result as? View ?: return@runAfter
                    dispatch(resId, view)
                }
                .getUnhookHandle()
        )

        handle.merge(
            LayoutInflater::class.java
                .hookMethod("parseInclude")
                .suppressError()
                .intercept { chain ->
                    if (entries.isEmpty()) return@intercept chain.proceed()
                    val attrs = chain.args.lastOrNull() as? AttributeSet
                    val layoutId = attrs?.getAttributeResourceValue(null, "layout", 0) ?: 0
                    val result = chain.proceed()

                    if (layoutId != 0 && entries.containsKey(layoutId)) {
                        val parent = chain.args.firstOrNull { it is ViewGroup } as? ViewGroup
                        parent?.getChildAt(parent.childCount - 1)?.let { dispatch(layoutId, it) }
                    }

                    result
                }
                .getUnhookHandle()
        )
    }

    private fun dispatch(resId: Int, view: View) {
        entries[resId]?.forEach { entry ->
            try {
                entry.callback(LayoutInflatedParam(view, view.resources, resId))
            } catch (throwable: Throwable) {
                log("LayoutHookManager", "${entry.resourceName}: $throwable")
            }
        }
    }
}
