package com.drdisagree.iconify.ui.utils

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper

class SnapOnScrollListener : RecyclerView.OnScrollListener {

    private val snapHelper: SnapHelper
    private var behavior = Behavior.NOTIFY_ON_SCROLL
    private var onSnapPositionChangeListener: OnSnapPositionChangeListener? = null

    private var snapPosition = RecyclerView.NO_POSITION

    constructor(snapHelper: SnapHelper) {
        this.snapHelper = snapHelper
    }

    constructor(
        snapHelper: SnapHelper,
        behavior: Behavior,
        onSnapPositionChangeListener: OnSnapPositionChangeListener?
    ) {
        this.snapHelper = snapHelper
        this.behavior = behavior
        this.onSnapPositionChangeListener = onSnapPositionChangeListener
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        if (behavior == Behavior.NOTIFY_ON_SCROLL) {
            maybeNotifySnapPositionChange(recyclerView)
        }
    }

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        if (behavior == Behavior.NOTIFY_ON_SCROLL_STATE_IDLE
            && newState == RecyclerView.SCROLL_STATE_IDLE
        ) {
            maybeNotifySnapPositionChange(recyclerView)
        }
    }

    private fun maybeNotifySnapPositionChange(recyclerView: RecyclerView) {
        val snapPosition = getSnapPosition(recyclerView)
        val snapPositionChanged = this.snapPosition != snapPosition

        if (snapPositionChanged) {
            if (onSnapPositionChangeListener != null) {
                onSnapPositionChangeListener!!.onSnapPositionChange(snapPosition)
            }
            this.snapPosition = snapPosition
        }
    }

    private fun getSnapPosition(recyclerView: RecyclerView): Int {
        val layoutManager = recyclerView.layoutManager ?: return RecyclerView.NO_POSITION
        val snapView = snapHelper.findSnapView(layoutManager) ?: return RecyclerView.NO_POSITION
        return layoutManager.getPosition(snapView)
    }


    enum class Behavior {
        NOTIFY_ON_SCROLL,
        NOTIFY_ON_SCROLL_STATE_IDLE
    }

    interface OnSnapPositionChangeListener {
        fun onSnapPositionChange(position: Int)
    }
}
