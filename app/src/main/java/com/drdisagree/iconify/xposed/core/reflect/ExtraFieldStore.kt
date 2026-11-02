package com.drdisagree.iconify.xposed.core.reflect

import java.lang.ref.ReferenceQueue
import java.util.concurrent.ConcurrentHashMap

object ExtraFieldStore {

    private val queue = ReferenceQueue<Any>()
    private val fields = ConcurrentHashMap<IdentityWeakReference, ConcurrentHashMap<String, Any>>()

    fun get(owner: Any, key: String): Any? {
        purge()
        return fields[IdentityWeakReference(owner)]?.get(key)
    }

    fun set(owner: Any, key: String, value: Any?): Any? {
        purge()
        if (value == null) return remove(owner, key)
        return fields
            .computeIfAbsent(IdentityWeakReference(owner, queue)) { ConcurrentHashMap() }
            .put(key, value)
    }

    fun remove(owner: Any, key: String): Any? {
        purge()
        val reference = IdentityWeakReference(owner)
        val values = fields[reference] ?: return null
        val removed = values.remove(key)
        if (values.isEmpty()) fields.remove(reference, values)
        return removed
    }

    private fun purge() {
        while (true) {
            val reference = queue.poll() ?: return
            fields.remove(reference)
        }
    }
}
