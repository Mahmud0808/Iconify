package com.drdisagree.iconify.xposed.modules.quicksettings

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import com.bosphere.fadingedgelayout.FadingEdgeLayout
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.ComposeViewHost
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.ShadeSceneInjector
import com.drdisagree.iconify.xposed.utils.SceneContainer
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import java.util.Collections
import java.util.WeakHashMap

class HeaderImageCompose(context: Context) : HeaderImage(context) {

    private class HostViews(val layout: FadingEdgeLayout, val image: ImageView?) {
        var lastLayoutAlpha = -1f
        var lastExpansion = -1f
        var lastOrientation = -1
    }

    private val hosts: MutableMap<View, HostViews> = Collections.synchronizedMap(WeakHashMap())

    override fun currentShadeExpandedFraction(): Float = SceneContainer.panelExpansion

    override fun updatePrefs(vararg key: String) {
        super.updatePrefs(*key)
        if (key.isEmpty()) return

        val layoutHeight = headerImageLayoutHeight()
        synchronized(hosts) { hosts.toList() }.forEach { (host, views) ->
            views.layout.layoutParams = views.layout.layoutParams.apply { height = layoutHeight }
            host.layoutParams = host.layoutParams.apply { height = hostHeight(layoutHeight) }
            withHostViews(views) { updateQSHeaderImage() }
        }
    }

    private fun hostHeight(layoutHeight: Int): Int =
        if (layoutHeight == ViewGroup.LayoutParams.MATCH_PARENT) {
            ViewGroup.LayoutParams.MATCH_PARENT
        } else {
            ViewGroup.LayoutParams.WRAP_CONTENT
        }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        SceneContainer.trackShadeExpansion()

        ShadeSceneInjector.addBehindContent(
            order = 0,
            key = GROUP_KEY,
            elementName = ELEMENT_NAME
        ) { context -> createHost(context) }
    }

    private fun createHost(context: Context): View {
        val layout = createHeaderImageLayout()
        val views = HostViews(layout, mQsHeaderImageView)

        val host = FrameLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                hostHeight(layout.layoutParams.height)
            )
            clipChildren = true
            addView(layout)
        }
        hosts[host] = views

        layout.addOnLayoutChangeListener { _, left, _, right, _, oldLeft, _, oldRight, _ ->
            if (right - left != oldRight - oldLeft) {
                layout.post {
                    withHostViews(views) {
                        layout.setFadeSizes(0, 0, 0, 0)
                        applyFadeEdges()
                    }
                }
            }
        }

        withHostViews(views) { updateQSHeaderImage() }

        ComposeViewHost.runEveryFrameWhileAttached(host) {
            val expansion = SceneContainer.panelExpansion
            val orientation = mContext.resources.configuration.orientation
            if (expansion != views.lastExpansion || orientation != views.lastOrientation) {
                views.lastExpansion = expansion
                views.lastOrientation = orientation
                withHostViews(views) { updateQSHeaderImageState() }
            }
        }

        return host
    }

    private fun withHostViews(views: HostViews, block: () -> Unit) {
        mQsHeaderImageLayout = views.layout
        mQsHeaderImageView = views.image
        lastLayoutAlpha = views.lastLayoutAlpha
        try {
            block()
        } finally {
            views.lastLayoutAlpha = lastLayoutAlpha
        }
    }

    companion object {
        private const val GROUP_KEY = 0x1C0B1A6E
        private const val ELEMENT_NAME = "IconifyHeaderImage"
    }
}
