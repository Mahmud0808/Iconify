package com.drdisagree.iconify.xposed.modules.lockscreen

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.View.OnAttachStateChangeListener
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.callbacks.KeyguardShowingCallback
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.modules.extras.views.LockscreenVisualizerView
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import com.drdisagree.iconify.xposed.utils.XPrefs.XprefsIsInitialized
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

@SuppressLint("DiscouragedApi")
class LockscreenVisualizer(context: Context) : ModPack(context), KeyguardShowingCallback.KeyguardShowingListener {

    private var visualizerEnabled = false
    private var colorMode = 1
    private var staticColor = Color.rgb(224, 184, 99)
    private var gradientStartColor = Color.rgb(224, 184, 99)
    private var gradientEndColor = Color.rgb(255, 106, 136)
    private var lavaSpeedSeconds = 24f
    private var sensitivity = 1.42f
    private var visualizerHeightDp = 500f
    private var barThicknessDp = 19f
    private var smoothness = 50f
    private var renderFps = 120
    private var keyguardRootView: ViewGroup? = null
    private var visualizerView: LockscreenVisualizerView? = null
    private var isKeyguardVisible = false
    private var keyguardCallbackRegistered = false

    override fun updatePrefs(vararg key: String) {
        if (!XprefsIsInitialized) return

        Xprefs.apply {
            visualizerEnabled = getBoolean(XposedKey.LOCKSCREEN_VISUALIZER)
            colorMode = getString(XposedKey.LOCKSCREEN_VISUALIZER_COLOR_MODE).toInt()
            staticColor = getColor(XposedKey.LOCKSCREEN_VISUALIZER_STATIC_COLOR)
            gradientStartColor = getColor(XposedKey.LOCKSCREEN_VISUALIZER_GRADIENT_COLOR_START)
            gradientEndColor = getColor(XposedKey.LOCKSCREEN_VISUALIZER_GRADIENT_COLOR_END)
            lavaSpeedSeconds = getFloat(XposedKey.LOCKSCREEN_VISUALIZER_LAVA_SPEED)
            sensitivity = getFloat(XposedKey.LOCKSCREEN_VISUALIZER_SENSITIVITY)
            visualizerHeightDp = getFloat(XposedKey.LOCKSCREEN_VISUALIZER_HEIGHT)
            barThicknessDp = getFloat(XposedKey.LOCKSCREEN_VISUALIZER_BAR_THICKNESS)
            smoothness = getFloat(XposedKey.LOCKSCREEN_VISUALIZER_SMOOTHNESS)
            renderFps = getString(XposedKey.LOCKSCREEN_VISUALIZER_FPS).toInt()
        }

        when (key.firstOrNull()) {
            XposedKey.LOCKSCREEN_VISUALIZER.name,
            XposedKey.LOCKSCREEN_VISUALIZER_COLOR_MODE.name,
            XposedKey.LOCKSCREEN_VISUALIZER_STATIC_COLOR.name,
            XposedKey.LOCKSCREEN_VISUALIZER_GRADIENT_COLOR_START.name,
            XposedKey.LOCKSCREEN_VISUALIZER_GRADIENT_COLOR_END.name,
            XposedKey.LOCKSCREEN_VISUALIZER_LAVA_SPEED.name,
            XposedKey.LOCKSCREEN_VISUALIZER_SENSITIVITY.name,
            XposedKey.LOCKSCREEN_VISUALIZER_HEIGHT.name,
            XposedKey.LOCKSCREEN_VISUALIZER_BAR_THICKNESS.name,
            XposedKey.LOCKSCREEN_VISUALIZER_SMOOTHNESS.name,
            XposedKey.LOCKSCREEN_VISUALIZER_FPS.name -> updateVisualizer()
        }
    }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        registerKeyguardCallback()

        val aodBurnInSectionClass =
            findClass("$SYSTEMUI_PACKAGE.keyguard.ui.view.layout.sections.AodBurnInSection")

        fun viewAttached(entryV: View) {
            Handler(Looper.getMainLooper()).postDelayed({
                val rootView = (entryV.parent as? ViewGroup)
                    ?.rootView
                    ?.findViewById<ViewGroup>(
                        mContext.resources.getIdentifier(
                            "keyguard_root_view",
                            "id",
                            mContext.packageName
                        )
                    ) ?: return@postDelayed

                keyguardRootView = rootView
                updateVisualizer()
            }, 1000)
        }

        aodBurnInSectionClass
            .hookMethod("addViews")
            .runAfter { param ->
                val entryV = param.args[0] as View

                entryV.addOnAttachStateChangeListener(object : OnAttachStateChangeListener {
                    override fun onViewAttachedToWindow(v: View) {
                        viewAttached(entryV)
                    }

                    override fun onViewDetachedFromWindow(v: View) {
                        isKeyguardVisible = false
                        removeVisualizer(smooth = true)
                    }
                })

                if (entryV.isAttachedToWindow) {
                    viewAttached(entryV)
                }
            }
    }

    override fun onKeyguardShown() {
        isKeyguardVisible = true
        Handler(Looper.getMainLooper()).post {
            updateVisualizer()
            visualizerView?.showFromBottom(resetLevels = true)
        }
    }

    override fun onKeyguardDismissed() {
        isKeyguardVisible = false
        Handler(Looper.getMainLooper()).post {
            removeVisualizer(smooth = true)
        }
    }

    private fun registerKeyguardCallback() {
        if (keyguardCallbackRegistered) return

        runCatching {
            KeyguardShowingCallback.getInstance().registerKeyguardShowingListener(this)
            keyguardCallbackRegistered = true
        }
    }

    private fun updateVisualizer() {
        Handler(Looper.getMainLooper()).post {
            if (visualizerEnabled && isKeyguardVisible) {
                addVisualizer()
            } else {
                removeVisualizer(smooth = true)
            }
        }
    }

    private fun addVisualizer() {
        val rootView = keyguardRootView ?: return

        val existingView = visualizerView
        if (existingView != null && existingView.parent === rootView) {
            configureVisualizer(existingView)
            existingView.showFromBottom(resetLevels = true)
            return
        }

        removeVisualizer()

        val view = LockscreenVisualizerView(mContext).apply {
            tag = VISUALIZER_TAG
            id = View.generateViewId()
            layoutParams = createLayoutParams(rootView)
        }

        configureVisualizer(view)

        rootView.addView(view, 0)
        visualizerView = view
        view.showFromBottom(resetLevels = true)
    }

    private fun removeVisualizer(smooth: Boolean = false) {
        val view = visualizerView

        if (view != null) {
            if (smooth && view.parent != null) {
                view.hideAndRemove {
                    (view.parent as? ViewGroup)?.removeView(view)
                    if (visualizerView === view) {
                        visualizerView = null
                    }
                }
            } else {
                view.setVisualizerEnabled(false)
                (view.parent as? ViewGroup)?.removeView(view)
                if (visualizerView === view) {
                    visualizerView = null
                }
            }
        }

        keyguardRootView
            ?.findViewWithTag<View>(VISUALIZER_TAG)
            ?.takeIf { it !== view }
            ?.let { existingView ->
                (existingView as? LockscreenVisualizerView)?.hideAndRemove {
                    (existingView.parent as? ViewGroup)?.removeView(existingView)
                } ?: run {
                    (existingView.parent as? ViewGroup)?.removeView(existingView)
                }
            }
    }

    private fun configureVisualizer(view: LockscreenVisualizerView) {
        view.configure(
            colorMode = colorMode,
            staticColor = staticColor,
            gradientStartColor = gradientStartColor,
            gradientEndColor = gradientEndColor,
            lavaSpeedSeconds = lavaSpeedSeconds,
            sensitivity = sensitivity,
            visualizerHeightDp = visualizerHeightDp,
            barThicknessDp = barThicknessDp,
            smoothness = smoothness,
            renderFps = renderFps
        )
    }

    private fun createLayoutParams(rootView: ViewGroup): ViewGroup.LayoutParams {
        val height = ViewGroup.LayoutParams.MATCH_PARENT
        val bottomMargin = 0

        return when (rootView) {
            is ConstraintLayout -> ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                height
            ).apply {
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                setMargins(0, 0, 0, bottomMargin)
            }

            is FrameLayout -> FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                height
            ).apply {
                gravity = Gravity.BOTTOM
                setMargins(0, 0, 0, bottomMargin)
            }

            else -> ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                height
            )
        }
    }

    companion object {
        private const val VISUALIZER_TAG = "iconify_lockscreen_visualizer"
    }
}