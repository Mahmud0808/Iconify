package com.drdisagree.iconify.xposed.modules.extras.utils.misc

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.drdisagree.iconify.data.common.Preferences.ICONIFY_LOCKSCREEN_CONTAINER_TAG
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.LockscreenSceneInjector.Anchor
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.ViewHelper.removeViewFromParent
import java.lang.ref.WeakReference
import java.util.Collections
import java.util.WeakHashMap
import java.util.concurrent.CopyOnWriteArrayList

object LockscreenComposeContainer {

    enum class Slot { MAIN, BELOW_LARGE_CLOCK }

    interface Listener {
        fun onContainerAttached(container: LinearLayout, slot: Slot)
        fun onContainerDetached(container: LinearLayout, slot: Slot) {}
    }

    private class Provider(
        val isEnabled: () -> Boolean,
        val replacesClock: () -> Boolean,
        val wantsLowerSlot: () -> Boolean
    )

    private class MarkerView(context: Context) : View(context) {
        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), 0)
        }
    }

    private class SlotHost(context: Context, val holder: SlotHolder) : FrameLayout(context) {
        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)

            if (childCount > 0) {
                if (measuredHeight > 0) holder.lastHostHeight = measuredHeight
            } else if (holder.lastHostHeight > 0) {
                setMeasuredDimension(
                    measuredWidth,
                    resolveSize(holder.lastHostHeight, heightMeasureSpec)
                )
            }
        }
    }

    private class OverlayHost(context: Context, val holder: SlotHolder) : FrameLayout(context) {
        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            val markerWidth = holder.marker?.width ?: 0
            val widthSpec = if (markerWidth > 0) {
                MeasureSpec.makeMeasureSpec(markerWidth, MeasureSpec.EXACTLY)
            } else {
                widthMeasureSpec
            }
            super.onMeasure(widthSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
        }
    }

    private class SlotHolder(val slot: Slot, val tag: String) {
        val hosts: MutableSet<FrameLayout> = Collections.newSetFromMap(WeakHashMap())
        var ref: WeakReference<LinearLayout>? = null
        var markerRef: WeakReference<View>? = null
        var overlayRef: WeakReference<OverlayHost>? = null
        private var lastOffset = 0 to 0
        var lastHostHeight = 0
        private var lastMarkerLocation = 0 to 0

        val container: LinearLayout?
            get() = ref?.get()

        val marker: View?
            get() = markerRef?.get()?.takeIf { it.isAttachedToWindow }

        private val overlay: OverlayHost?
            get() = overlayRef?.get()?.takeIf { it.isAttachedToWindow }

        fun obtain(context: Context): LinearLayout = container ?: LinearLayout(context).apply {
            id = View.generateViewId()
            tag = this@SlotHolder.tag
            orientation = LinearLayout.VERTICAL
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {
                    listeners.forEach { it.onContainerAttached(this@apply, slot) }
                }

                override fun onViewDetachedFromWindow(v: View) {
                    listeners.forEach { it.onContainerDetached(this@apply, slot) }
                    reevaluate()
                }
            })
            ref = WeakReference(this)
        }

        fun createHost(context: Context): View {
            val host = SlotHost(context, this).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                clipChildren = false
                clipToPadding = false
            }
            hosts += host
            host.addOnAttachStateChangeListener(reevaluateOnAttach)
            obtain(context)
            return host
        }

        fun createMarker(context: Context): View = MarkerView(context).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0)
            addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {
                    markerRef = WeakReference(v)
                    reevaluate()
                }

                override fun onViewDetachedFromWindow(v: View) {
                    if (markerRef?.get() === v) markerRef = null
                    reevaluate()
                }
            })
            addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> this@SlotHolder.overlay?.requestLayout() }
        }

        fun createOverlay(context: Context): View {
            val host = OverlayHost(context, this).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                clipChildren = false
                clipToPadding = false
            }
            overlayRef = WeakReference(host)
            host.addOnAttachStateChangeListener(reevaluateOnAttach)
            ComposeViewHost.runBeforeEachDraw(host) {
                val marker = marker ?: return@runBeforeEachDraw
                val location = IntArray(2).also { marker.getLocationInWindow(it) }
                val current = location[0] to location[1]
                if (current != lastMarkerLocation) {
                    lastMarkerLocation = current
                    host.requestLayout()
                }
            }
            obtain(context)
            return host
        }

        fun overlayOffset(): Pair<Int, Int> {
            val host = overlayRef?.get() ?: return lastOffset
            val marker = marker ?: return lastOffset
            val hostLocation = IntArray(2).also { host.getLocationInWindow(it) }
            val markerLocation = IntArray(2).also { marker.getLocationInWindow(it) }
            val baseX = hostLocation[0] - lastOffset.first
            val baseY = hostLocation[1] - lastOffset.second
            lastOffset = (markerLocation[0] - baseX) to (markerLocation[1] - baseY)
            return lastOffset
        }

        private val reevaluateOnAttach = object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                reevaluate()
            }

            override fun onViewDetachedFromWindow(v: View) {
                reevaluate()
            }
        }

        fun reevaluate() {
            val overlay = overlay
            val target: FrameLayout? = if (marker != null && overlay != null) {
                overlay
            } else {
                synchronized(hosts) { hosts.toList() }.lastOrNull { it.isAttachedToWindow }
            }
            target ?: return
            val view = container ?: obtain(target.context)
            if (view.parent === target) return
            target.post {
                if (!target.isAttachedToWindow || view.parent === target) return@post
                view.removeViewFromParent()
                target.addView(view)
                overlayRef?.get()?.requestLayout()
            }
        }
    }

    private val providers = CopyOnWriteArrayList<Provider>()
    private val listeners = CopyOnWriteArrayList<Listener>()
    private val mainSlot = SlotHolder(Slot.MAIN, ICONIFY_LOCKSCREEN_CONTAINER_TAG)
    private val lowerSlot = SlotHolder(Slot.BELOW_LARGE_CLOCK, LOWER_CONTAINER_TAG)

    val isActive: Boolean
        get() = providers.any { it.isEnabled() }

    val isReplacingClock: Boolean
        get() = providers.any { it.isEnabled() && it.replacesClock() }

    private val isLowerSlotWanted: Boolean
        get() = providers.any { it.isEnabled() && it.wantsLowerSlot() }

    fun register(
        isEnabled: () -> Boolean,
        replacesClock: () -> Boolean = { false },
        wantsLowerSlot: () -> Boolean = { false },
        listener: Listener
    ) {
        providers += Provider(isEnabled, replacesClock, wantsLowerSlot)
        listeners += listener
        listOf(mainSlot, lowerSlot).forEach { holder ->
            holder.container
                ?.takeIf { it.isAttachedToWindow }
                ?.let { listener.onContainerAttached(it, holder.slot) }
        }

        val mainBesideStockClock = { isActive && !isReplacingClock }
        val lowerBesideStockClock = { isLowerSlotWanted && !isReplacingClock }

        LockscreenSceneInjector.replaceClockRegion(
            key = REPLACE_GROUP_KEY,
            isEnabled = { isReplacingClock }
        ) { context -> mainSlot.createHost(context) }

        LockscreenSceneInjector.addAnchor(
            Anchor.SMALL_CLOCK_SMARTSPACE,
            key = APPEND_GROUP_KEY,
            isEnabled = mainBesideStockClock
        ) { context -> mainSlot.createHost(context) }

        LockscreenSceneInjector.addAnchor(
            Anchor.LARGE_CLOCK_SMARTSPACE,
            key = MAIN_MARKER_GROUP_KEY,
            isEnabled = mainBesideStockClock
        ) { context -> mainSlot.createMarker(context) }

        LockscreenSceneInjector.addAnchor(
            Anchor.LARGE_CLOCK_DATE,
            key = LOWER_MARKER_GROUP_KEY,
            isEnabled = lowerBesideStockClock
        ) { context -> lowerSlot.createMarker(context) }

        LockscreenSceneInjector.addOverlay(
            key = MAIN_OVERLAY_GROUP_KEY,
            isEnabled = mainBesideStockClock,
            modifier = { ComposeViewHost.offsetModifier { mainSlot.overlayOffset() } }
        ) { context -> mainSlot.createOverlay(context) }

        LockscreenSceneInjector.addOverlay(
            key = LOWER_OVERLAY_GROUP_KEY,
            isEnabled = lowerBesideStockClock,
            modifier = { ComposeViewHost.offsetModifier { lowerSlot.overlayOffset() } }
        ) { context -> lowerSlot.createOverlay(context) }
    }

    fun containerOrNull(slot: Slot = Slot.MAIN): LinearLayout? = when (slot) {
        Slot.MAIN -> mainSlot.container
        Slot.BELOW_LARGE_CLOCK -> lowerSlot.container
    }

    private const val LOWER_CONTAINER_TAG = "iconify_lockscreen_lower_container"
    private const val REPLACE_GROUP_KEY = 0x1C0B1C0C
    private const val APPEND_GROUP_KEY = 0x1C0B1C0D
    private const val MAIN_MARKER_GROUP_KEY = 0x1C0B1C0E
    private const val LOWER_MARKER_GROUP_KEY = 0x1C0B1C0F
    private const val MAIN_OVERLAY_GROUP_KEY = 0x1C0B1C10
    private const val LOWER_OVERLAY_GROUP_KEY = 0x1C0B1C11
}
