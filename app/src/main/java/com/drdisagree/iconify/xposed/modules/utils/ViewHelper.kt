package com.drdisagree.iconify.xposed.modules.utils

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
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

    fun findViewWithTagAndChangeColor(view: View?, tagContains: String, color1: Int, color2: Int, cornerRadius: Int) {
        if (view == null) return

        val drawable = GradientDrawable()
        drawable.colors = intArrayOf(color1, color2)
        drawable.cornerRadius = view.context.toPx(cornerRadius).toFloat()

        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val child: View = view.getChildAt(i)
                checkTagAndChangeBackgroundColor(child, tagContains, drawable)

                if (child is ViewGroup) {
                    checkTagAndChangeBackgroundColor(child, tagContains, drawable)
                }
            }
        } else {
            checkTagAndChangeBackgroundColor(view, tagContains, drawable)
        }

    }

    private fun checkTagAndChangeColor(view: View, tag: String, color: Int) {
        if (view.tag?.toString()?.let { isTagMatch(tag, it) } == true) {
            changeViewColor(view, color)
        }
    }

    private fun checkTagAndChangeBackgroundColor(view: View, tag: String, bkg: Drawable) {
        if (view.tag?.toString()?.let { isTagMatch(tag, it) } == true) {
            changeViewBackgroundColor(view, bkg)
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

    private fun changeViewBackgroundColor(view: View, bkg: Drawable) {
        view.background = bkg
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
        if (child.tag?.toString()?.let { isTagMatch("nolineheight", it) } == true) {
            return
        }

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
                    setTextScaling(child, scaleFactor)
                }
            }
        } else if (view is TextView) {
            setTextScaling(view, scaleFactor)
        }
    }

    private fun setTextScaling(view: View, scaleFactor: Float) {
        val originalSize = (view as TextView).textSize
        val newSize = originalSize * scaleFactor
        view.setTextSize(TypedValue.COMPLEX_UNIT_PX, newSize)
    }

    fun View.findViewContainsTag(tag: String): View? {
        if (this is ViewGroup) {
            for (i in 0 until childCount) {
                val child = getChildAt(i)

                if (child.tag?.toString()?.let { isTagMatch(tag, it) } == true) {
                    return child
                }

                if (child is ViewGroup) {
                    val result = child.findViewContainsTag(tag)
                    if (result != null) {
                        return result
                    }
                }
            }
        } else {
            if (getTag()?.toString()?.let { isTagMatch(tag, it) } == true) {
                return this
            }
        }

        return null
    }

    private fun isTagMatch(tagToCheck: String, targetTag: String): Boolean {
        val parts = targetTag.split("|")
        return parts.any { it.trim() == tagToCheck }
    }
}
