package com.drdisagree.iconify

import android.app.Application
import android.content.Context
import com.drdisagree.iconify.utils.helper.LocaleHelper
import com.google.android.material.color.DynamicColors
import java.lang.ref.WeakReference

class Iconify : Application() {

    companion object {
        private var instance: Iconify? = null
        private var contextReference: WeakReference<Context>? = null

        val appContext: Context
            get() {
                if (contextReference == null || contextReference?.get() == null) {
                    contextReference = WeakReference(
                        instance?.applicationContext ?: getInstance().applicationContext
                    )
                }
                return contextReference!!.get()!!
            }

        val appContextLocale: Context
            get() {
                return LocaleHelper.setLocale(appContext)
            }

        private fun getInstance(): Iconify {
            if (instance == null) {
                instance = Iconify()
            }
            return instance!!
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        contextReference = WeakReference(applicationContext)
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}