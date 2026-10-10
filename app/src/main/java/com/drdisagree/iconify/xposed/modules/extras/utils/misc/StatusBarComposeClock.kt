package com.drdisagree.iconify.xposed.modules.extras.utils.misc

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.ViewHelper.reAddView
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.ComposeToolkit
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getExtraFieldSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.setExtraField
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import java.lang.ref.WeakReference
import java.lang.reflect.Method

object StatusBarComposeClock {

    private var installed = false
    private var clockRef: WeakReference<View>? = null
    private var hostRef: WeakReference<FrameLayout>? = null

    private val isNeeded: Boolean
        get() = Xprefs.getBoolean(XposedKey.STATUSBAR_CLOCK_CHIP) ||
                Xprefs.getBoolean(XposedKey.STATUSBAR_CLOCK_TEXT_SIZE_SWITCH) ||
                Xprefs.getBoolean(XposedKey.STATUSBAR_CLOCK_CLICKABLE) ||
                Xprefs.getBoolean(XposedKey.STATUSBAR_LINK_TO_CUSTOM_COLOR) ||
                isClockMoved

    private val isClockMoved: Boolean
        get() = !Xprefs.getBoolean(XposedKey.DUAL_STATUSBAR) &&
                Xprefs.getString(XposedKey.STATUSBAR_CLOCK_POSITION) != "0"

    private val isHostActive: Boolean
        get() = hostRef?.get()?.isAttachedToWindow == true

    @SuppressLint("DiscouragedApi")
    fun install() {
        if (installed) return
        installed = true
        if (!ComposeViewHost.isAvailable) return

        val clockComposableClass = findClass(CLOCK_KT_CLASS, suppressError = true) ?: return

        findClass("$SYSTEMUI_PACKAGE.statusbar.phone.PhoneStatusBarView")
            .hookMethod("onFinishInflate")
            .runAfter { param ->
                val view = param.thisObject as ViewGroup
                val id = view.resources.getIdentifier("clock", "id", SYSTEMUI_PACKAGE)
                view.findViewById<View>(id)?.let { clock ->
                    clockRef = WeakReference(clock)
                    guardClockState(clock)
                }
            }

        clockComposableClass
            .hookMethod(*CLOCK_METHODS)
            .suppressError()
            .runBefore { param ->
                if (!isNeeded || clockRef?.get() == null) return@runBefore
                if (!isCalledFromStatusBar()) return@runBefore

                val method = param.method as Method
                val composer = param.args.getOrNull(
                    ComposeToolkit.parameterIndex(method, ComposeToolkit.COMPOSER_CLASS)
                ) ?: return@runBefore
                val modifier = param.args.getOrNull(
                    ComposeToolkit.parameterIndex(method, ComposeToolkit.MODIFIER_CLASS)
                )

                ComposeViewHost.emitInGroup(composer, GROUP_KEY, modifier) { createHost(it) }
                param.result = null
            }
    }

    private fun guardClockState(clock: View) {
        if (clock.getExtraFieldSilently(GUARD_FIELD) == true) return
        clock.setExtraField(GUARD_FIELD, true)

        ComposeViewHost.runBeforeEachDraw(clock) {
            if (!isNeeded) return@runBeforeEachDraw

            if (isHostActive) {
                if (clock.visibility != View.VISIBLE) clock.visibility = View.VISIBLE
                if (clock.alpha != 1f) clock.alpha = 1f
            } else if (clock.visibility != View.GONE) {
                clock.visibility = View.GONE
            }
        }
    }

    private fun isCalledFromStatusBar(): Boolean =
        Thread.currentThread().stackTrace.any { it.className.startsWith(STATUS_BAR_CLOCK_CALLER) }

    private fun createHost(context: Context): View = FrameLayout(context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        clipChildren = false
        clipToPadding = false

        addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                val host = v as FrameLayout
                hostRef = WeakReference(host)

                host.post {
                    if (hostRef?.get() !== host || !host.isAttachedToWindow) return@post
                    val clock = clockRef?.get() ?: return@post

                    if (!isClockMoved) host.reAddView(clock)
                    clock.visibility = View.VISIBLE
                    clock.alpha = 1f
                    guardClockState(clock)
                }
            }

            override fun onViewDetachedFromWindow(v: View) {
                if (hostRef?.get() === v) hostRef = null
            }
        })
    }

    private const val CLOCK_KT_CLASS = "$SYSTEMUI_PACKAGE.clock.ui.composable.ClockKt"
    private val CLOCK_METHODS = arrayOf("Clock-sW7UJKQ", "Clock")
    private const val STATUS_BAR_CLOCK_CALLER =
        "$SYSTEMUI_PACKAGE.statusbar.pipeline.shared.ui.composable.StatusBarRootKt\$addStartSideComposable"
    private const val GROUP_KEY = 0x1C0B1C30
    private const val GUARD_FIELD = "iconifyComposeClockGuard"
}
