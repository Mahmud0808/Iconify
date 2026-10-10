package com.drdisagree.iconify.xposed.modules.lockscreen

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.LockscreenSceneInjector
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

class LockscreenVisualizerCompose(context: Context) : LockscreenVisualizer(context) {

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        registerDozeListener()

        LockscreenSceneInjector.addBehindContent(
            order = 1,
            key = GROUP_KEY,
            isEnabled = { visualizerEnabled }
        ) { context -> createHost(context) }
    }

    private fun createHost(context: Context): View = FrameLayout(context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                keyguardRootView = v as ViewGroup
                isKeyguardVisible = true
                updateVisualizer()
            }

            override fun onViewDetachedFromWindow(v: View) {
                if (keyguardRootView !== v) return
                isKeyguardVisible = false
                removeVisualizer()
                keyguardRootView = null
            }
        })
    }

    companion object {
        private const val GROUP_KEY = 0x1C0B1C20
    }
}
