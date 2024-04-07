package com.drdisagree.iconify.xposed.modules.utils

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import de.robv.android.xposed.XposedBridge
import java.util.Locale

object ViewHelper {

    fun setMargins(viewGroup: Any, context: Context, left: Int, top: Int, right: Int, bottom: Int) {
        when (viewGroup) {
            is View -> {
                when (val layoutParams = viewGroup.layoutParams) {
                    is LinearLayout.LayoutParams -> {
                        layoutParams.setMargins(
                            context.toPx(left),
                            context.toPx(top),
                            context.toPx(right),
                            context.toPx(bottom)
                        )
                    }

                    is FrameLayout.LayoutParams -> {
                        layoutParams.setMargins(
                            context.toPx(left),
                            context.toPx(top),
                            context.toPx(right),
                            context.toPx(bottom)
                        )
                    }

                    is RelativeLayout.LayoutParams -> {
                        layoutParams.setMargins(
                            context.toPx(left),
                            context.toPx(top),
                            context.toPx(right),
                            context.toPx(bottom)
                        )
                    }

                    else -> {
                        XposedBridge.log("Unsupported type: $layoutParams")
                    }
                }
            }

            is MarginLayoutParams -> {
                viewGroup.setMargins(
                    context.toPx(left),
                    context.toPx(top),
                    context.toPx(right),
                    context.toPx(bottom)
                )
            }

            else -> {
                throw IllegalArgumentException("The viewGroup object has to be either a View or a ViewGroup.MarginLayoutParams. Found ${viewGroup.javaClass.simpleName} instead.")
            }
        }
    }

    fun setPaddings(
        viewGroup: ViewGroup,
        context: Context,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ) {
        viewGroup.setPadding(
            context.toPx(left),
            context.toPx(top),
            context.toPx(right),
            context.toPx(bottom)
        )
    }

    fun Context.toPx(dp: Int): Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp.toFloat(),
        resources.displayMetrics
    ).toInt()

    fun findViewWithTagAndChangeColor(view: View?, tagContains: String, color: Int) {
        if (view == null) return

        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val child: View = view.getChildAt(i)
                checkTagAndChangeColor(child, tagContains, color)

                if (child is ViewGroup) {
                    findViewWithTagAndChangeColor(child, tagContains, color)
                }
            }
        } else {
            checkTagAndChangeColor(view, tagContains, color)
        }
    }

    private fun checkTagAndChangeColor(view: View, tagContains: String, color: Int) {
        val tagObject = view.tag
        if (tagObject != null && tagObject.toString().lowercase(Locale.getDefault())
                .contains(tagContains)
        ) {
            changeViewColor(view, color)
        }
    }

    private fun changeViewColor(view: View, color: Int) {
        when (view) {
            is TextView -> {
                view.setTextColor(color)

                val drawablesRelative: Array<Drawable?> = view.getCompoundDrawablesRelative()
                for (drawable in drawablesRelative) {
                    drawable?.let {
                        it.mutate()
                        it.setTint(color)
                        it.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
                    }
                }

                val drawables: Array<Drawable?> = view.getCompoundDrawables()
                for (drawable in drawables) {
                    drawable?.let {
                        it.mutate()
                        it.setTint(color)
                        it.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
                    }
                }
            }

            is ImageView -> {
                view.setColorFilter(color)
            }

            is ViewGroup -> {
                view.setBackgroundTintList(ColorStateList.valueOf(color))
            }

            is ProgressBar -> {
                view.setProgressTintList(ColorStateList.valueOf(color))
                view.setProgressBackgroundTintList(ColorStateList.valueOf(color))
            }

            else -> {
                view.background.mutate().setTint(color)
            }
        }
    }

    fun applyFontRecursively(view: View?, typeface: Typeface?) {
        if (view == null) return

        if (view is ViewGroup) {
            val childCount: Int = view.childCount

            for (i in 0 until childCount) {
                val child: View = view.getChildAt(i)

                if (child is ViewGroup) {
                    applyFontRecursively(child, typeface)
                } else (child as? TextView)?.setTypeface(typeface)
            }
        } else (view as? TextView)?.setTypeface(typeface)
    }

    fun applyTextMarginRecursively(context: Context, view: View?, topMargin: Int) {
        if (view == null) return

        val topMarginInDp = context.toPx(topMargin)

        if (view is ViewGroup) {
            val childCount: Int = view.childCount

            for (i in 0 until childCount) {
                val child: View = view.getChildAt(i)

                if (child is ViewGroup) {
                    applyTextMarginRecursively(context, child, topMargin)
                } else if (child is TextView) {
                    setTextMargins(child, topMarginInDp)
                }
            }
        } else if (view is TextView) {
            setTextMargins(view, topMarginInDp)
        }
    }

    private fun setTextMargins(child: View, topMarginInDp: Int) {
        if (child.tag != null && child.tag.toString().lowercase(Locale.getDefault())
                .contains("nolineheight")
        ) return

        when (val params = child.layoutParams) {
            is LinearLayout.LayoutParams -> {
                params.topMargin += topMarginInDp
                child.setLayoutParams(params)
            }

            is FrameLayout.LayoutParams -> {
                params.topMargin += topMarginInDp
                child.setLayoutParams(params)
            }

            is RelativeLayout.LayoutParams -> {
                params.topMargin += topMarginInDp
                child.setLayoutParams(params)
            }

            else -> {
                XposedBridge.log("Invalid params: $params")
            }
        }
    }

    fun applyTextScalingRecursively(view: View?, scaleFactor: Float) {
        if (view == null) return

        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val child: View = view.getChildAt(i)

                if (child is ViewGroup) {
                    applyTextScalingRecursively(child, scaleFactor)
                } else if (child is TextView) {
                    setTestScaling(child, scaleFactor)
                }
            }
        } else if (view is TextView) {
            setTestScaling(view, scaleFactor)
        }
    }

    private fun setTestScaling(view: View, scaleFactor: Float) {
        val originalSize = (view as TextView).textSize
        val newSize = originalSize * scaleFactor
        view.setTextSize(TypedValue.COMPLEX_UNIT_PX, newSize)
    }
}
