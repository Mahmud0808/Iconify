package com.drdisagree.iconify.xposed.modules.views

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import androidx.core.content.ContextCompat
import com.drdisagree.iconify.xposed.modules.utils.ViewHelper.toPx

object ChipDrawable {

    enum class GradientDirection(val index: Int) {
        TOP_BOTTOM(0),
        BOTTOM_TOP(1),
        LEFT_RIGHT(2),
        RIGHT_LEFT(3),
        TOP_LEFT_BOTTOM_RIGHT(4),
        TOP_RIGHT_BOTTOM_LEFT(5),
        BOTTOM_LEFT_TOP_RIGHT(6),
        BOTTOM_RIGHT_TOP_LEFT(7);

        companion object {
            fun fromIndex(index: Int): GradientDirection {
                return entries.firstOrNull { it.index == index } ?: TOP_BOTTOM
            }

            fun GradientDirection.toIndex(): Int {
                return this.index
            }
        }
    }


    fun createChipDrawable(
        context: Context,
        accentFill: Boolean = true,
        startColor: Int = 0xFF0000,
        endColor: Int = 0x00FF00,
        gradientDirection: GradientDirection = GradientDirection.LEFT_RIGHT,
        padding: IntArray = intArrayOf(0, 0, 0, 0),
        strokeEnabled: Boolean = false,
        accentStroke: Boolean = true,
        strokeWidth: Int = 2,
        strokeColor: Int = 0x000000,
        dashedBorderEnabled: Boolean = false,
        dashWidth: Int = 4,
        dashGap: Int = 4,
        cornerRadii: FloatArray = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
    ): LayerDrawable {
        val gradientDrawable = GradientDrawable().apply {
            colors = if (accentFill) {
                val color = ContextCompat.getColor(context, android.R.color.system_accent1_400)
                intArrayOf(color, color)
            } else {
                intArrayOf(startColor, endColor)
            }
            orientation = when (gradientDirection) {
                GradientDirection.TOP_BOTTOM -> GradientDrawable.Orientation.LEFT_RIGHT
                GradientDirection.BOTTOM_TOP -> GradientDrawable.Orientation.RIGHT_LEFT
                GradientDirection.LEFT_RIGHT -> GradientDrawable.Orientation.TOP_BOTTOM
                GradientDirection.RIGHT_LEFT -> GradientDrawable.Orientation.BOTTOM_TOP
                GradientDirection.TOP_LEFT_BOTTOM_RIGHT -> GradientDrawable.Orientation.TL_BR
                GradientDirection.TOP_RIGHT_BOTTOM_LEFT -> GradientDrawable.Orientation.TR_BL
                GradientDirection.BOTTOM_LEFT_TOP_RIGHT -> GradientDrawable.Orientation.BL_TR
                GradientDirection.BOTTOM_RIGHT_TOP_LEFT -> GradientDrawable.Orientation.BR_TL
            }
            shape = GradientDrawable.RECTANGLE
            setPadding(
                context.toPx(padding[0]),
                context.toPx(padding[1]),
                context.toPx(padding[2]),
                context.toPx(padding[3])
            )
            setCornerRadii(
                cornerRadii.map { context.toPx(it.toInt()).toFloat() }.toFloatArray()
            )
            if (strokeEnabled) {
                val color = if (accentStroke) {
                    ContextCompat.getColor(context, android.R.color.system_accent3_400)
                } else {
                    strokeColor
                }
                if (dashedBorderEnabled) {
                    setStroke(
                        context.toPx(strokeWidth),
                        color,
                        context.toPx(dashWidth).toFloat(),
                        context.toPx(dashGap).toFloat()
                    )
                } else {
                    setStroke(
                        context.toPx(strokeWidth),
                        color
                    )
                }
            }
        }

        return LayerDrawable(arrayOf(gradientDrawable))
    }
}