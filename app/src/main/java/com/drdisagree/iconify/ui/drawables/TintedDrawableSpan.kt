package com.drdisagree.iconify.ui.drawables

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.FontMetricsInt
import android.graphics.drawable.Drawable
import android.text.style.DynamicDrawableSpan


/**
 * [DynamicDrawableSpan] which draws a drawable tinted with the current paint color.
 */
class TintedDrawableSpan(context: Context, resourceId: Int) :
    DynamicDrawableSpan(ALIGN_BOTTOM) {
    private val mDrawable = context.getDrawable(resourceId)!!.mutate()
    private var mOldTint = 0

    init {
        mDrawable.setTint(0)
    }

    override fun getSize(
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
        fm: FontMetricsInt?
    ): Int {
        var fm = fm
        fm = fm ?: paint.fontMetricsInt
        val iconSize = fm!!.bottom - fm.top
        mDrawable.setBounds(0, 0, iconSize, iconSize)
        return super.getSize(paint, text, start, end, fm)
    }

    override fun draw(
        canvas: Canvas, text: CharSequence,
        start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint
    ) {
        val color = paint.color
        if (mOldTint != color) {
            mOldTint = color
            mDrawable.setTint(mOldTint)
        }
        super.draw(canvas, text, start, end, x, top, y, bottom, paint)
    }

    override fun getDrawable(): Drawable {
        return mDrawable
    }
}
