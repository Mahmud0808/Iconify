package com.drdisagree.iconify.core.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.withRotation
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.drdisagree.iconify.app.Iconify.Companion.appContext
import com.drdisagree.iconify.data.common.Const

object ViewHelper {

    fun disableNestedScrolling(viewPager: ViewPager2) {
        var recyclerView: RecyclerView? = null

        for (i in 0 until viewPager.childCount) {
            if (viewPager.getChildAt(i) is RecyclerView) {
                recyclerView = viewPager.getChildAt(i) as RecyclerView
                break
            }
        }

        if (recyclerView != null) {
            recyclerView.isNestedScrollingEnabled = false
        }
    }

    fun setHeader(context: Context, toolbar: Toolbar, title: Any) {
        (context as AppCompatActivity).setSupportActionBar(toolbar)
        context.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        context.supportActionBar?.setDisplayShowHomeEnabled(true)
        if (title is Int) {
            toolbar.setTitle(title)
        } else if (title is String) {
            toolbar.setTitle(title)
        }
    }

    fun setHeader(
        context: Context,
        fragmentManager: FragmentManager,
        toolbar: Toolbar,
        title: Int
    ) {
        setHeader(
            context,
            fragmentManager,
            toolbar,
            context.resources.getString(title)
        )
    }

    fun setHeader(
        context: Context,
        fragmentManager: FragmentManager,
        toolbar: Toolbar,
        title: String
    ) {
        toolbar.setTitle(title)
        (context as AppCompatActivity).setSupportActionBar(toolbar)
        context.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        context.supportActionBar?.setDisplayShowHomeEnabled(true)

        toolbar.setNavigationOnClickListener {
            Handler(Looper.getMainLooper()).postDelayed(
                { fragmentManager.popBackStack() }, Const.FRAGMENT_BACK_BUTTON_DELAY.toLong()
            )
        }
    }

    fun dp2px(dp: Float): Int {
        return dp2px(dp.toInt())
    }

    fun dp2px(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            appContext.resources.displayMetrics
        ).toInt()
    }

    private fun getRotateDrawable(d: Drawable, angle: Float): Drawable {
        val arD = arrayOf(d)
        return object : LayerDrawable(arD) {
            override fun draw(canvas: Canvas) {
                canvas.withRotation(
                    angle,
                    d.bounds.width().toFloat() / 2,
                    d.bounds.height().toFloat() / 2
                ) {
                    super.draw(this)
                }
            }

            override fun getConstantState(): ConstantState {
                return RotateDrawableConstantState(d, angle)
            }
        }
    }

    private class RotateDrawableConstantState(
        private val drawable: Drawable,
        private val angle: Float
    ) : Drawable.ConstantState() {
        override fun newDrawable(): Drawable {
            return getRotateDrawable(drawable.constantState?.newDrawable() ?: drawable, angle)
        }

        override fun getChangingConfigurations(): Int {
            return drawable.changingConfigurations
        }
    }

    private fun getDrawable(context: Context, @DrawableRes batteryRes: Int): Drawable? {
        return ResourcesCompat.getDrawable(context.resources, batteryRes, context.theme)
    }

    fun setTextRecursively(viewGroup: ViewGroup, text: String?) {
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            if (child is ViewGroup) {
                setTextRecursively(child, text)
            } else if (child is TextView) {
                child.text = text
            }
        }
    }

    fun applyTextSizeRecursively(viewGroup: ViewGroup, textSize: Int) {
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            if (child is ViewGroup) {
                applyTextSizeRecursively(child, textSize)
            } else if (child is TextView) {
                child.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize.toFloat())
            }
        }
    }

}