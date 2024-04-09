package com.drdisagree.iconify.ui.utils

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs
import kotlin.math.min

class CarouselLayoutManager(context: Context?, orientation: Int, reverseLayout: Boolean) :
    LinearLayoutManager(context, orientation, reverseLayout) {

    private var minifyAmount = 0.05f
    private var minifyDistance = 0.9f

    override fun scrollHorizontallyBy(
        dx: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int {
        val scrolled = super.scrollHorizontallyBy(dx, recycler, state)

        updateScaleFactors()

        return scrolled
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        super.onLayoutChildren(recycler, state)

        updateScaleFactors()
    }

    private fun updateScaleFactors() {
        val parentMidpoint = width / 2.0f
        val d0 = 0.00f
        val d1 = parentMidpoint * minifyDistance
        val s0 = 1.0f
        val s1 = 1.0f - minifyAmount

        for (i in 0 until childCount) {
            val child = getChildAt(i) ?: continue
            val childMidpoint = (getDecoratedLeft(child) + getDecoratedRight(child)) / 2f
            val d = min(d1.toDouble(), abs((parentMidpoint - childMidpoint).toDouble())).toFloat()
            val scaleFactor = s0 + (s1 - s0) * (d - d0) / (d1 - d0)

            child.scaleX = scaleFactor
            child.scaleY = scaleFactor
        }
    }

    fun setMinifyAmount(minifyAmount: Float) {
        this.minifyAmount = minifyAmount
    }

    fun setMinifyDistance(minifyDistance: Float) {
        this.minifyDistance = minifyDistance
    }
}
