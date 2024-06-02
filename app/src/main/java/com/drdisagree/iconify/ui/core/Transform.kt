package com.drdisagree.iconify.ui.core

import android.content.res.Configuration
import android.view.View
import android.widget.ImageView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import com.drdisagree.iconify.R
import kotlin.math.abs

@Suppress("unused")
object Transform {

    val pageCompositePageTransformer: CompositePageTransformer
        get() {
            val pageTransformer = CompositePageTransformer()
            pageTransformer.addTransformer(MarginPageTransformer(40))
            pageTransformer.addTransformer { page: View, position: Float ->
                val r = (1 - abs(position.toDouble())).toFloat()
                page.scaleY = 0.85f + r * 0.15f
                setParallaxTransformation(page, position)
            }
            return pageTransformer
        }

    @JvmStatic
    fun setParallaxTransformation(page: View, position: Float) {
        val parallaxView = page.findViewById<ImageView>(R.id.img)
        val isLandscape =
            page.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        if (position < -1) {
            // This page is way off-screen to the left.
            page.setAlpha(1f)
        } else if (position <= 1) {
            // [-1,1]
            parallaxView.translationX =
                -position * ((if (isLandscape) page.height else page.width) / 2f) // Half the normal speed
        } else {
            // (1,+Infinity]
            // This page is way off-screen to the right.
            page.setAlpha(1f)
        }
    }
}