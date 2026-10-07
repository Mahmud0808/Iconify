package com.drdisagree.iconify.xposed.modules.quicksettings

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.xposed.modules.extras.callbacks.BootCallback
import com.drdisagree.iconify.xposed.modules.extras.callbacks.ThemeChangeCallback
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.ComposeViewHost
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.ShadeSceneInjector
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.TouchAnimator
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.ComposeToolkit
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethodMatchPattern
import com.drdisagree.iconify.xposed.utils.SceneContainer
import de.robv.android.xposed.XC_MethodHook.MethodHookParam
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import java.lang.reflect.Method
import java.util.Collections
import java.util.WeakHashMap

class HeaderClockCompose(context: Context) : HeaderClock(context) {

    private class HostViews(
        val header: LinearLayout,
        val clock: LinearLayout,
        val icons: LinearLayout
    ) {
        var animator: TouchAnimator? = null
        var lastShadeFraction = -1f
        var lastQsFraction = -1f
        var lastPanelExpansion = -1f
        var lastQsExpansion = -1f
        var lastOrientation = -1
    }

    private val hosts: MutableMap<View, HostViews> = Collections.synchronizedMap(WeakHashMap())

    override val hiddenVisibility: Int = View.INVISIBLE

    override fun currentShadeExpandedFraction(): Float = SceneContainer.panelExpansion

    override fun currentQsExpandedFraction(): Float = SceneContainer.qsExpansion

    override fun updatePrefs(vararg key: String) {
        super.updatePrefs(*key)
        if (key.isEmpty()) return

        hosts.values.toList().forEach { views ->
            withHostViews(views) {
                updateClockView()
                buildHeaderViewExpansion()
            }
        }
    }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        initResources(mContext)
        SceneContainer.trackShadeExpansion()
        hookActivityStarter()
        hideStockHeaderElements()

        ShadeSceneInjector.addBehindContent(
            order = 1,
            key = GROUP_KEY,
            elementName = ELEMENT_NAME
        ) { context -> createHost(context) }

        try {
            ThemeChangeCallback.getInstance().registerThemeChangedCallback(
                object : ThemeChangeCallback.OnThemeChangedListener {
                    override fun onThemeChanged() {
                        hosts.values.toList().forEach { views ->
                            withHostViews(views) { updateClockView() }
                        }
                    }
                }
            )
        } catch (_: Throwable) {
        }

        BootCallback.registerBootListener {
            hosts.values.toList().forEach { views ->
                withHostViews(views) { updateClockView() }
            }
        }
    }

    private fun createHost(context: Context): View {
        val views = HostViews(
            header = LinearLayout(context),
            clock = LinearLayout(context),
            icons = LinearLayout(context)
        )

        val host = FrameLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        withHostViews(views) {
            setupHeaderClockContainer()
            mQsIconsContainer.visibility = View.GONE
            host.addView(mQsHeaderClockContainer)
            buildHeaderViewExpansion()
            updateClockView()
        }
        hosts[host] = views

        ComposeViewHost.runEveryFrameWhileAttached(host) {
            val shadeExpansion = SceneContainer.panelExpansion
            val qsExpansion = SceneContainer.qsExpansion
            val orientation = mContext.resources.configuration.orientation
            if (shadeExpansion != views.lastPanelExpansion ||
                qsExpansion != views.lastQsExpansion ||
                orientation != views.lastOrientation
            ) {
                val orientationChanged =
                    orientation != views.lastOrientation && views.lastOrientation != -1
                views.lastPanelExpansion = shadeExpansion
                views.lastQsExpansion = qsExpansion
                views.lastOrientation = orientation
                withHostViews(views) {
                    if (orientationChanged) buildHeaderViewExpansion()
                    updateQSHeaderClockState()
                }
            }
        }

        return host
    }

    private fun withHostViews(views: HostViews, block: () -> Unit) {
        mQsHeaderClockContainer = views.header
        mQsClockContainer = views.clock
        mQsIconsContainer = views.icons
        mQQSContainerAnimator = views.animator
        lastShadeExpandedFraction = views.lastShadeFraction
        lastQsExpandedFraction = views.lastQsFraction
        try {
            block()
        } finally {
            views.animator = mQQSContainerAnimator
            views.lastShadeFraction = lastShadeExpandedFraction
            views.lastQsFraction = lastQsExpandedFraction
        }
    }

    private fun hideStockHeaderElements() {
        val shadeHeaderClass = findClass(SHADE_HEADER_CLASS, suppressError = true)

        shadeHeaderClass
            .hookMethodMatchPattern("Clock-.*")
            .suppressError()
            .runBefore { param ->
                if (showHeaderClock) hideModifierArgument(param)
            }

        findClass(
            "$SYSTEMUI_PACKAGE.shade.ui.composable.VariableDayDateKt",
            suppressError = true
        )
            .hookMethodMatchPattern("VariableDayDate.*")
            .suppressError()
            .runBefore { param ->
                if (!showHeaderClock) return@runBefore
                if (!ComposeToolkit.isCalledFrom(SHADE_HEADER_CLASS)) return@runBefore
                hideModifierArgument(param)
            }

        shadeHeaderClass
            .hookMethod("ShadeCarrierGroup")
            .suppressError()
            .runBefore { param ->
                if (showHeaderClock && hideQsCarrierGroup) hideModifierArgument(param)
            }

        shadeHeaderClass
            .hookMethod("StatusIcons")
            .suppressError()
            .runBefore { param ->
                if (showHeaderClock && hideStatusIcons) hideModifierArgument(param)
            }

        shadeHeaderClass
            .hookMethodMatchPattern("BatteryInfo(-.*)?")
            .suppressError()
            .runBefore { param ->
                if (showHeaderClock && hideStatusIcons) hideModifierArgument(param)
            }
    }

    private fun hideModifierArgument(param: MethodHookParam) {
        val method = param.method as Method
        val index = ComposeToolkit.parameterIndex(method, ComposeToolkit.MODIFIER_CLASS)
        if (index == -1) return

        val original = param.args[index]
        if (original != null && ComposeViewHost.isHiddenModifier(original)) return

        val hidden = ComposeViewHost.hiddenModifier(original ?: ComposeToolkit.emptyModifier ?: return)
            ?: return
        param.args[index] = hidden
        ComposeToolkit.clearDefaultBit(method, param.args, index)
    }

    companion object {
        private const val GROUP_KEY = 0x1C0B1C1C
        private const val ELEMENT_NAME = "IconifyHeaderClock"
        private const val SHADE_HEADER_CLASS = "$SYSTEMUI_PACKAGE.shade.ui.composable.ShadeHeaderKt"
    }
}
