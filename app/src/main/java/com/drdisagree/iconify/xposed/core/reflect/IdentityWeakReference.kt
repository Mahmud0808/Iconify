package com.drdisagree.iconify.xposed.core.reflect

import java.lang.ref.ReferenceQueue
import java.lang.ref.WeakReference

internal class IdentityWeakReference(
    referent: Any,
    queue: ReferenceQueue<Any>? = null
) : WeakReference<Any>(referent, queue) {

    private val hash = System.identityHashCode(referent)

    override fun hashCode(): Int = hash

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IdentityWeakReference) return false
        val referent = get() ?: return false
        return referent === other.get()
    }
}
