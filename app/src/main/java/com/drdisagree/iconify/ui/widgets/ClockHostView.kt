package com.drdisagree.iconify.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.View.MeasureSpec.EXACTLY
import android.widget.FrameLayout
import com.drdisagree.iconify.utils.ScreenSizeCalculator

/**
 * The parent view for each clock view in picker carousel This view will give a container with the
 * same size of lockscreen to layout clock and scale down it to the size in picker carousel
 * according to ratio of preview to LS
 */
class ClockHostView(
    context: Context,
    attrs: AttributeSet?,
) : FrameLayout(context, attrs) {

    private var previewRatio: Float = 1F
        set(value) {
            if (field != value) {
                field = value
                scaleX = previewRatio
                scaleY = previewRatio
                invalidate()
            }
        }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val screenSize = ScreenSizeCalculator.getInstance().getScreenSize(display)
        previewRatio = measuredWidth / screenSize.x.toFloat()
    }

    /**
     * In clock picker, we want to clock layout and render at lockscreen size and scale down so that
     * the preview in clock carousel will be the same as lockscreen
     */
    override fun measureChildWithMargins(
        child: View?,
        parentWidthMeasureSpec: Int,
        widthUsed: Int,
        parentHeightMeasureSpec: Int,
        heightUsed: Int
    ) {
        val screenSize = ScreenSizeCalculator.getInstance().getScreenSize(display)
        super.measureChildWithMargins(
            child,
            MeasureSpec.makeMeasureSpec(screenSize.x, EXACTLY),
            widthUsed,
            MeasureSpec.makeMeasureSpec(screenSize.y, EXACTLY),
            heightUsed
        )
    }
}
