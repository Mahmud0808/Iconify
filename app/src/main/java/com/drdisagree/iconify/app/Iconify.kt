package com.drdisagree.iconify.app

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp
import java.lang.ref.WeakReference

@HiltAndroidApp
class Iconify : Application() {

    override fun onCreate() {
        super.onCreate()

        _instance = this
        _contextReference = WeakReference(applicationContext)
    }

    companion object {

        private lateinit var _instance: Iconify
        private lateinit var _contextReference: WeakReference<Context>

        private var instance: Iconify
            get() {
                if (this::_instance.isInitialized.not()) {
                    _instance = Iconify()
                }
                return _instance
            }
            set(value) {
                _instance = value
            }

        val appContext: Context
            get() {
                if (this::_contextReference.isInitialized.not() || _contextReference.get() == null) {
                    _contextReference = WeakReference(instance.applicationContext)
                }
                return _contextReference.get()!!
            }
    }
}