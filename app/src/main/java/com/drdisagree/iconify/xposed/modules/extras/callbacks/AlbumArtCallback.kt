package com.drdisagree.iconify.xposed.modules.extras.callbacks

object AlbumArtCallback {

    private val listeners = mutableListOf<AlbumArtVisibilityListener>()
    private val lock = Any()

    fun interface AlbumArtVisibilityListener {
        fun onVisibilityChanged()
    }

    fun notifyVisibilityChanged() {
        val snapshot = synchronized(lock) { listeners.toList() }
        snapshot.forEach { it.onVisibilityChanged() }
    }

    fun registerVisibilityListener(listener: AlbumArtVisibilityListener) {
        synchronized(lock) {
            if (!listeners.contains(listener)) {
                listeners.add(listener)
            }
        }
    }

    fun unregisterVisibilityListener(listener: AlbumArtVisibilityListener) {
        synchronized(lock) {
            listeners.remove(listener)
        }
    }
}