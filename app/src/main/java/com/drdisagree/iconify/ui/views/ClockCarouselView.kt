package com.drdisagree.iconify.ui.views

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.constraintlayout.helper.widget.Carousel
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.view.doOnPreDraw
import com.drdisagree.iconify.R
import com.drdisagree.iconify.ui.models.ClockCarouselItemViewModel
import com.drdisagree.iconify.ui.widgets.ClockHostView
import kotlin.math.max

class ClockCarouselView(
    context: Context,
    attrs: AttributeSet,
) :
    FrameLayout(
        context,
        attrs,
    ) {

    private val carousel: Carousel
    private val motionLayout: MotionLayout
    private lateinit var adapter: ClockCarouselAdapter
    private var clockSelectedListener: OnClockSelected? = null
    private var toCenterClockScaleView: View? = null
    private var offCenterClockScaleView: View? = null
    private var toCenterCardView: View? = null
    private var offCenterCardView: View? = null
    private var toCenterTextView: View? = null
    private var offCenterTextView: View? = null

    init {
        val clockCarousel = LayoutInflater.from(context).inflate(R.layout.clock_carousel, this)
        carousel = clockCarousel.requireViewById(R.id.carousel)
        motionLayout = clockCarousel.requireViewById(R.id.motion_container)
    }

    // This function is for the custom accessibility action to trigger a transition to the next
    // carousel item. If the current item is the last item in the carousel, the next item
    // will be the first item.
    fun transitionToNext() {
        if (carousel.count != 0) {
            val index = (carousel.currentIndex + 1) % carousel.count
            carousel.jumpToIndex(index)
            // Explicitly called this since using transitionToIndex(index) leads to
            // race-condition between announcement of content description of the correct clock-face
            // and the selection of clock face itself
            adapter.onNewItem(index)
        }
    }

    // This function is for the custom accessibility action to trigger a transition to
    // the previous carousel item. If the current item is the first item in the carousel,
    // the previous item will be the last item.
    fun transitionToPrevious() {
        if (carousel.count != 0) {
            val index = (carousel.currentIndex + carousel.count - 1) % carousel.count
            carousel.jumpToIndex(index)
            // Explicitly called this since using transitionToIndex(index) leads to
            // race-condition between announcement of content description of the correct clock-face
            // and the selection of clock face itself
            adapter.onNewItem(index)
        }
    }

    fun scrollToNext() {
        if (
            carousel.count <= 1 ||
            (carousel.currentIndex == carousel.count - 1)
        ) {
            // No need to scroll if the count is equal or less than 1
            return
        }
        if (motionLayout.currentState == R.id.start) {
            motionLayout.transitionToState(R.id.next, TRANSITION_DURATION)
        }
    }

    fun scrollToPrevious() {
        if (carousel.count <= 1 || (carousel.currentIndex == 0)) {
            // No need to scroll if the count is equal or less than 1
            return
        }
        if (motionLayout.currentState == R.id.start) {
            motionLayout.transitionToState(R.id.previous, TRANSITION_DURATION)
        }
    }

    fun setUpClockCarouselView(
        clocks: List<ClockCarouselItemViewModel>,
        clockListener: OnClockSelected
    ) {
        clockSelectedListener = clockListener
        Log.d("ClockCarouselView", "Setting up clock carousel view ${clocks.size}")
        adapter = ClockCarouselAdapter(clocks, clockSelectedListener!!)
        carousel.setAdapter(adapter)
        val indexOfSelectedClock =
            clocks
                .indexOfFirst { it.isSelected }
                // If not found, default to the first clock as selected:
                .takeIf { it != -1 }
                ?: 0
        carousel.jumpToIndex(indexOfSelectedClock)
        motionLayout.setTransitionListener(
            object : MotionLayout.TransitionListener {

                override fun onTransitionStarted(
                    motionLayout: MotionLayout?,
                    startId: Int,
                    endId: Int
                ) {
                    if (motionLayout == null) {
                        return
                    }
                    prepareDynamicClockView(motionLayout, endId)
                    prepareCardView(motionLayout, endId)
                    setCarouselItemAnimationState(true)
                }

                override fun onTransitionChange(
                    motionLayout: MotionLayout?,
                    startId: Int,
                    endId: Int,
                    progress: Float,
                ) {
                    onDynamicClockViewTransition(progress)
                    onCardViewTransition(progress)
                }

                override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
                    setCarouselItemAnimationState(currentId == R.id.start)
                }

                private fun prepareDynamicClockView(motionLayout: MotionLayout, endId: Int) {
                    offCenterClockScaleView = motionLayout.findViewById(R.id.clock_scale_view_2)
                    toCenterClockScaleView =
                        motionLayout.findViewById(
                            if (endId == R.id.next) R.id.clock_scale_view_3
                            else R.id.clock_scale_view_1
                        )
                    offCenterTextView = motionLayout.findViewById(R.id.clock_style_2)
                    toCenterTextView =
                        motionLayout.findViewById(
                            if (endId == R.id.next) R.id.clock_style_3
                            else R.id.clock_style_1
                        )
                }

                private fun prepareCardView(motionLayout: MotionLayout, endId: Int) {
                    offCenterCardView = motionLayout.findViewById(R.id.item_card_2)
                    toCenterCardView =
                        motionLayout.findViewById(
                            if (endId == R.id.next) R.id.item_card_3 else R.id.item_card_1
                        )
                    offCenterTextView = motionLayout.findViewById(R.id.clock_style_2)
                    toCenterTextView =
                        motionLayout.findViewById(
                            if (endId == R.id.next) R.id.clock_style_3
                            else R.id.clock_style_1
                        )
                }

                private fun onCardViewTransition(progress: Float) {
                    offCenterCardView?.alpha = getShowingAlpha(progress)
                    toCenterCardView?.alpha = getHidingAlpha(progress)
                    toCenterTextView?.alpha = getShowingAlphaText(progress)
                    offCenterTextView?.alpha = getHidingAlphaText(progress)
                }

                private fun onDynamicClockViewTransition(progress: Float) {
                    val scalingDownScale = getScalingDownScale(progress)
                    val scalingUpScale = getScalingUpScale(progress)
                    offCenterClockScaleView?.scaleX = scalingDownScale
                    offCenterClockScaleView?.scaleY = scalingDownScale
                    toCenterClockScaleView?.scaleX = scalingUpScale
                    toCenterClockScaleView?.scaleY = scalingUpScale
                }

                private fun setCarouselItemAnimationState(isStart: Boolean) {
                    onDynamicClockViewTransition(if (isStart) 0f else 1f)
                    onCardViewTransition(if (isStart) 0f else 1f)
                }

                override fun onTransitionTrigger(
                    motionLayout: MotionLayout?,
                    triggerId: Int,
                    positive: Boolean,
                    progress: Float
                ) {}
            }
        )
    }

    fun setSelectedClockIndex(
        index: Int,
    ) {
        // 1. setUpClockCarouselView() can possibly not be called before setSelectedClockIndex().
        //    We need to check if index out of bound.
        // 2. jumpToIndex() to the same position can cause the views unnecessarily populate again.
        //    We only call jumpToIndex when the index is different from the current carousel.
        if (carousel.count == 0) {
            Log.d("ClockCarouselView", "No clock to select")
            return
        }
        if (index < carousel.count && index != carousel.currentIndex) {
            carousel.jumpToIndex(index)
        }
    }

    fun setCarouselCardColor(color: Int) {
        itemViewIds.forEach { id ->
            val cardViewId = getClockCardViewId(id)
            cardViewId?.let {
                val cardView = motionLayout.requireViewById<View>(it)
                cardView.backgroundTintList = ColorStateList.valueOf(color)
            }
        }
    }

    private class ClockCarouselAdapter(
        val clocks: List<ClockCarouselItemViewModel>,
        private val onClockSelected: OnClockSelected
    ) : Carousel.Adapter {

        fun getContentDescription(index: Int): String {
            return clocks[index].contentDescription
        }

        override fun count(): Int {
            return clocks.size
        }

        override fun populate(view: View?, index: Int) {
            val viewRoot = view as? ViewGroup ?: return
            val cardView =
                getClockCardViewId(viewRoot.id)?.let { viewRoot.findViewById(it) as? View }
                    ?: return
            val clockScaleView =
                getClockScaleViewId(viewRoot.id)?.let { viewRoot.findViewById(it) as? View }
                    ?: return
            val clockHostView =
                getClockHostViewId(viewRoot.id)?.let { viewRoot.findViewById(it) as? ClockHostView }
                    ?: return
            val clockTextView =
                getClockTextId(viewRoot.id)?.let { viewRoot.findViewById(it) as? View }
                    ?: return

            // Add the clock view to the clock host view
            clockHostView.removeAllViews()
            val clockView =
                clocks[index].view
            // The clock view might still be attached to an existing parent. Detach before adding to
            // another parent.
            (clockView.parent as? ViewGroup)?.removeView(clockView)
            clockHostView.addView(clockView)

            val isMiddleView = isMiddleView(viewRoot.id)

            // Accessibility
            viewRoot.contentDescription = getContentDescription(index)
            viewRoot.isSelected = isMiddleView
            (clockTextView as TextView).text = clocks[index].clockName

            initializeDynamicClockView(
                isMiddleView,
                clockScaleView,
                clockHostView,
                clockTextView
            )
            cardView.alpha = if (isMiddleView) 0f else 1f
            clockTextView.alpha = if (isMiddleView) 1f else 0f
        }

        private fun initializeDynamicClockView(
            isMiddleView: Boolean,
            clockScaleView: View,
            clockHostView: ClockHostView,
            clockTextView: View
        ) {
            clockHostView.doOnPreDraw {
                it.pivotX = it.width / 2F
                it.pivotY = it.height / 2F
            }

            if (isMiddleView) {
                clockScaleView.scaleX = 1f
                clockScaleView.scaleY = 1f
                clockTextView.alpha = 1f
            } else {
                clockScaleView.scaleX = CLOCK_CAROUSEL_VIEW_SCALE
                clockScaleView.scaleY = CLOCK_CAROUSEL_VIEW_SCALE
                clockTextView.alpha = 0f
            }
        }

        override fun onNewItem(index: Int) {
            onClockSelected.onClockSelected(clocks[index])
        }
    }

    companion object {
        // The carousel needs to have at least 5 different clock faces to be infinite
        const val MIN_CLOCKS_TO_ENABLE_INFINITE_CAROUSEL = 5
        const val CLOCK_CAROUSEL_VIEW_SCALE = 0.5f
        const val TRANSITION_DURATION = 250

        val itemViewIds =
            listOf(
                R.id.item_view_0,
                R.id.item_view_1,
                R.id.item_view_2,
                R.id.item_view_3,
                R.id.item_view_4
            )

        fun getScalingUpScale(progress: Float) =
            CLOCK_CAROUSEL_VIEW_SCALE + progress * (1f - CLOCK_CAROUSEL_VIEW_SCALE)

        fun getScalingDownScale(progress: Float) = 1f - progress * (1f - CLOCK_CAROUSEL_VIEW_SCALE)

        // This makes the card only starts to reveal in the last quarter of the trip so
        // the card won't overlap the preview.
        fun getShowingAlpha(progress: Float) = max(progress - 0.75f, 0f) * 4

        // This makes the card starts to hide in the first quarter of the trip so the
        // card won't overlap the preview.
        fun getHidingAlpha(progress: Float) = max(1f - progress * 4, 0f)

        // This makes the card only starts to reveal in the last quarter of the trip so
        // the card won't overlap the preview.
        fun getShowingAlphaText(progress: Float) = max(progress - 0.75f, 0f) * 4

        // This makes the card starts to hide in the first quarter of the trip so the
        // card won't overlap the preview.
        fun getHidingAlphaText(progress: Float) = max(1f - progress * 4, 0f)

        fun getClockHostViewId(rootViewId: Int): Int? {
            return when (rootViewId) {
                R.id.item_view_0 -> R.id.clock_host_view_0
                R.id.item_view_1 -> R.id.clock_host_view_1
                R.id.item_view_2 -> R.id.clock_host_view_2
                R.id.item_view_3 -> R.id.clock_host_view_3
                R.id.item_view_4 -> R.id.clock_host_view_4
                else -> null
            }
        }

        fun getClockScaleViewId(rootViewId: Int): Int? {
            return when (rootViewId) {
                R.id.item_view_0 -> R.id.clock_scale_view_0
                R.id.item_view_1 -> R.id.clock_scale_view_1
                R.id.item_view_2 -> R.id.clock_scale_view_2
                R.id.item_view_3 -> R.id.clock_scale_view_3
                R.id.item_view_4 -> R.id.clock_scale_view_4
                else -> null
            }
        }

        fun getClockCardViewId(rootViewId: Int): Int? {
            return when (rootViewId) {
                R.id.item_view_0 -> R.id.item_card_0
                R.id.item_view_1 -> R.id.item_card_1
                R.id.item_view_2 -> R.id.item_card_2
                R.id.item_view_3 -> R.id.item_card_3
                R.id.item_view_4 -> R.id.item_card_4
                else -> null
            }
        }

        fun getClockTextId(rootViewId: Int): Int? {
            return when (rootViewId) {
                R.id.item_view_0 -> R.id.clock_style_0
                R.id.item_view_1 -> R.id.clock_style_1
                R.id.item_view_2 -> R.id.clock_style_2
                R.id.item_view_3 -> R.id.clock_style_3
                R.id.item_view_4 -> R.id.clock_style_4
                else -> null
            }
        }

        fun isMiddleView(rootViewId: Int): Boolean {
            return rootViewId == R.id.item_view_2
        }

        fun getCenteredHostViewPivotX(hostView: View): Float {
            return if (hostView.layoutDirection == LAYOUT_DIRECTION_RTL) hostView.width.toFloat() else 0F
        }

        private fun getTranslationDistance(
            hostLength: Int,
            frameLength: Int,
            edgeDimen: Int,
        ): Float {
            return ((hostLength - frameLength) / 2 - edgeDimen).toFloat()
        }
    }

    fun interface OnClockSelected {
        fun onClockSelected(clock: ClockCarouselItemViewModel)
    }
}