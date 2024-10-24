package com.drdisagree.iconify.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.children
import com.drdisagree.iconify.utils.ScreenSizeCalculator

/**
 * [FrameLayout] that sizes itself and its children layout with a given fixed width and a calculated
 * height according to the screen aspect ratio.
 */
class FixedWidthDisplayRatioFrameLayout(
    context: Context,
    attrs: AttributeSet?,
) : FrameLayout(context, attrs) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val screenAspectRatio = ScreenSizeCalculator.getInstance().getScreenAspectRatio(context)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = (width * screenAspectRatio).toInt()
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY),
        )
        children.forEach { child ->
            child.measure(
                MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY),
            )
        }
    }
}