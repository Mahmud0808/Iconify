package com.drdisagree.iconify.app

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp
import java.lang.ref.WeakReference

@HiltAndroidApp
class Iconify : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        contextReference = WeakReference(applicationContext)
    }

    companion object {

        private lateinit var instance: Iconify
        private lateinit var contextReference: WeakReference<Context>

        val appContext: Context
            get() {
                if (!this::contextReference.isInitialized || contextReference.get() == null) {
                    contextReference = WeakReference(getInstance().applicationContext)
                }
                return contextReference.get()!!
            }

        private fun getInstance(): Iconify {
            if (!this::instance.isInitialized) {
                instance = Iconify()
            }
            return instance
        }
    }
}