package com.drdisagree.iconify.xposed.modules.extras.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.ViewHelper.toPx

object ArcProgressWidget {

    fun generateBitmap(
        context: Context,
        percentage: Int,
        textInside: String,
        textInsideSizePx: Int,
        textBottom: String = "...",
        textBottomSizePx: Int,
        typeface: Typeface
    ): Bitmap {
        return generateBitmap(
            context,
            percentage,
            textInside,
            textInsideSizePx,
            null,
            28,
            textBottom,
            textBottomSizePx,
            typeface
        )
    }

    fun generateBitmap(
        context: Context,
        percentage: Int,
        textInside: String,
        textInsideSizePx: Int,
        iconDrawable: Drawable?,
        iconSizePx: Int,
        typeface: Typeface,
        @ColorInt progressColor: Int,
        @ColorInt textColor: Int
    ): Bitmap {
        return generateBitmap(
            context,
            percentage,
            textInside,
            textInsideSizePx,
            iconDrawable,
            iconSizePx,
            "Usage",
            28,
            typeface,
            progressColor,
            textColor
        )
    }

    fun generateBitmap(
        context: Context,
        percentage: Int = 100,
        textInside: String = "☺",
        textInsideSizePx: Int,
        iconDrawable: Drawable? = null,
        iconSizePx: Int,
        textBottom: String = "...",
        textBottomSizePx: Int = 28,
        typeface: Typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD),
        @ColorInt progressColor: Int = Color.WHITE,
        @ColorInt textColor: Int = Color.WHITE
    ): Bitmap {
        val width = 400
        val height = 400
        val stroke = 40
        val padding = 5
        val minAngle = 135
        val maxAngle = 275

        val paint =
            Paint(Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG or Paint.ANTI_ALIAS_FLAG).apply {
                strokeWidth = stroke.toFloat()
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
            }

        val mTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = context.toPx(textInsideSizePx).toFloat()
            color = textColor
            textAlign = Paint.Align.CENTER
        }

        val arc = RectF()
        arc[(stroke.toFloat() / 2) + padding,
            (stroke.toFloat() / 2) + padding,
            width - padding - (stroke.toFloat() / 2)] = height - padding - (stroke.toFloat() / 2)

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        paint.color = Color.argb(
            75,
            Color.red(progressColor),
            Color.green(progressColor),
            Color.blue(progressColor)
        )
        canvas.drawArc(arc, minAngle.toFloat(), maxAngle.toFloat(), false, paint)

        paint.color = progressColor
        canvas.drawArc(
            arc,
            minAngle.toFloat(),
            (maxAngle.toFloat() / 100) * percentage,
            false,
            paint
        )

        mTextPaint.setTypeface(typeface)

        canvas.drawText(
            textInside,
            bitmap.width.toFloat() / 2,
            (bitmap.height - mTextPaint.ascent() * 0.7f) / 2,
            mTextPaint
        )

        iconDrawable?.apply {
            val size: Int = context.toPx(iconSizePx)
            val left = (bitmap.width - size) / 2
            val top: Int =
                bitmap.height - (size / 1.3).toInt() - (stroke + padding) - context.toPx(4)
            val right = left + size
            val bottom = top + size

            setBounds(left, top, right, bottom)
            colorFilter = BlendModeColorFilter(textColor, BlendMode.SRC_IN)
            draw(canvas)
        } ?: run {
            mTextPaint.textSize = context.toPx(textBottomSizePx).toFloat()
            canvas.drawText(
                textBottom,
                bitmap.width.toFloat() / 2,
                (bitmap.height - (stroke + padding)).toFloat(),
                mTextPaint
            )
        }

        return bitmap
    }
}