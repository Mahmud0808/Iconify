package com.drdisagree.iconify.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.core.widget.NestedScrollView
import kotlin.math.abs

/**
 * This nested scroll view will detect horizontal touch movements and stop vertical scrolls when a
 * horizontal touch movement is detected.
 */
class HorizontalTouchMovementAwareNestedScrollView(context: Context, attrs: AttributeSet?) :
    NestedScrollView(context, attrs) {

    private var startXPosition = 0f
    private var startYPosition = 0f
    private var isHorizontalTouchMovement = false

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startXPosition = event.x
                startYPosition = event.y
                isHorizontalTouchMovement = false
            }
            MotionEvent.ACTION_MOVE -> {
                val xMoveDistance = abs(event.x - startXPosition)
                val yMoveDistance = abs(event.y - startYPosition)
                if (
                    !isHorizontalTouchMovement &&
                    xMoveDistance > yMoveDistance &&
                    xMoveDistance > ViewConfiguration.get(context).scaledTouchSlop
                ) {
                    isHorizontalTouchMovement = true
                }
            }
            else -> {}
        }
        return if (isHorizontalTouchMovement) {
            // We only want to intercept the touch event when the touch moves more vertically than
            // horizontally. So we return false.
            false
        } else {
            super.onInterceptTouchEvent(event)
        }
    }
}