package com.drdisagree.iconify.ui.views

import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.content.res.Resources.Theme
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.DrawableWrapper
import android.graphics.drawable.InsetDrawable

class RoundedCornerProgressDrawable @JvmOverloads constructor(drawable: Drawable? = null) :
    InsetDrawable(drawable, 0) {

    override fun getChangingConfigurations(): Int {
        return super.getChangingConfigurations() or ActivityInfo.CONFIG_DENSITY
    }

    override fun getConstantState(): ConstantState {
        return RoundedCornerState(super.getConstantState())
    }

    override fun onBoundsChange(rect: Rect) {
        super.onBoundsChange(rect)
        onLevelChange(level)
    }

    override fun onLayoutDirectionChanged(level: Int): Boolean {
        onLevelChange(getLevel())
        return super.onLayoutDirectionChanged(level)
    }

    override fun onLevelChange(n: Int): Boolean {
        var drawable = drawable
        val bounds: Rect? = drawable?.getBounds()
        val height = getBounds().height()
        val level = (getBounds().width() - getBounds().height()) * n / 10000

        drawable = getDrawable()

        if (drawable != null && bounds != null) {
            drawable.setBounds(
                getBounds().left,
                bounds.top,
                getBounds().left + (height + level),
                bounds.bottom
            )
        }

        return super.onLevelChange(level)
    }

    private class RoundedCornerState(private val mWrappedState: ConstantState?) : ConstantState() {

        override fun getChangingConfigurations(): Int {
            return mWrappedState!!.changingConfigurations
        }

        override fun newDrawable(): Drawable {
            return newDrawable(null, null)
        }

        override fun newDrawable(resources: Resources?, theme: Theme?): Drawable {
            val drawable = mWrappedState!!.newDrawable(resources, theme)
            return RoundedCornerProgressDrawable((drawable as DrawableWrapper).drawable)
        }
    }
}