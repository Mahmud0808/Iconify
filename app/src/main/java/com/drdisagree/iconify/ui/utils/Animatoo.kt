package com.drdisagree.iconify.ui.utils

import android.app.Activity
import android.content.Context
import com.drdisagree.iconify.R

@Suppress("deprecation")
object Animatoo {

    @JvmStatic
    fun animateZoom(context: Context) {
        (context as Activity).overridePendingTransition(
            R.anim.animate_zoom_enter,
            R.anim.animate_zoom_exit
        )
    }

    @JvmStatic
    fun animateFade(context: Context) {
        (context as Activity).overridePendingTransition(
            R.anim.animate_fade_enter,
            R.anim.animate_fade_exit
        )
    }

    @JvmStatic
    fun animateWindmill(context: Context) {
        (context as Activity).overridePendingTransition(
            R.anim.animate_windmill_enter,
            R.anim.animate_windmill_exit
        )
    }

    @JvmStatic
    fun animateSpin(context: Context) {
        (context as Activity).overridePendingTransition(
            R.anim.animate_spin_enter,
            R.anim.animate_spin_exit
        )
    }

    @JvmStatic
    fun animateDiagonal(context: Context) {
        (context as Activity).overridePendingTransition(
            R.anim.animate_diagonal_right_enter,
            R.anim.animate_diagonal_right_exit
        )
    }

    @JvmStatic
    fun animateSplit(context: Context) {
        (context as Activity).overridePendingTransition(
            R.anim.animate_split_enter,
            R.anim.animate_split_exit
        )
    }

    @JvmStatic
    fun animateShrink(context: Context) {
        (context as Activity).overridePendingTransition(
            R.anim.animate_shrink_enter,
            R.anim.animate_shrink_exit
        )
    }

    @JvmStatic
    fun animateCard(context: Context) {
        (context as Activity).overridePendingTransition(
            R.anim.animate_card_enter,
            R.anim.animate_card_exit
        )
    }

    @JvmStatic
    fun animateInAndOut(context: Context) {
        (context as Activity).overridePendingTransition(
            R.anim.animate_in_out_enter,
            R.anim.animate_in_out_exit
        )
    }

    @JvmStatic
    fun animateSwipeLeft(context: Context) {
        (context as Activity).overridePendingTransition(
            R.anim.animate_swipe_left_enter,
            R.anim.animate_swipe_left_exit
        )
    }

    @JvmStatic
    fun animateSwipeRight(context: Context) {
        (context as Activity).overridePendingTransition(
            R.anim.animate_swipe_right_enter,
            R.anim.animate_swipe_right_exit
        )
    }

    @JvmStatic
    fun animateSlideLeft(context: Context) {
        (context as Activity).overridePendingTransition(
            R.anim.animate_slide_left_enter,
            R.anim.animate_slide_left_exit
        )
    }

    @JvmStatic
    fun animateSlideRight(context: Context) {
        (context as Activity).overridePendingTransition(
            R.anim.animate_slide_in_left,
            R.anim.animate_slide_out_right
        )
    }

    @JvmStatic
    fun animateSlideDown(context: Context) {
        (context as Activity).overridePendingTransition(
            R.anim.animate_slide_down_enter,
            R.anim.animate_slide_down_exit
        )
    }

    @JvmStatic
    fun animateSlideUp(context: Context) {
        (context as Activity).overridePendingTransition(
            R.anim.animate_slide_up_enter,
            R.anim.animate_slide_up_exit
        )
    }
}
