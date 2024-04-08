package com.drdisagree.iconify.ui.utils;

import android.app.Activity;
import android.content.Context;

import com.drdisagree.iconify.R;

public class Animatoo {
    public static void animateZoom(Context context) {
        ((Activity) context).overridePendingTransition(
                R.anim.animate_zoom_enter,
                R.anim.animate_zoom_exit
        );
    }

    public static void animateFade(Context context) {
        ((Activity) context).overridePendingTransition(
                R.anim.animate_fade_enter,
                R.anim.animate_fade_exit
        );
    }

    public static void animateWindmill(Context context) {
        ((Activity) context).overridePendingTransition(
                R.anim.animate_windmill_enter,
                R.anim.animate_windmill_exit
        );
    }

    public static void animateSpin(Context context) {
        ((Activity) context).overridePendingTransition(
                R.anim.animate_spin_enter,
                R.anim.animate_spin_exit
        );
    }

    public static void animateDiagonal(Context context) {
        ((Activity) context).overridePendingTransition(
                R.anim.animate_diagonal_right_enter,
                R.anim.animate_diagonal_right_exit
        );
    }

    public static void animateSplit(Context context) {
        ((Activity) context).overridePendingTransition(
                R.anim.animate_split_enter,
                R.anim.animate_split_exit
        );
    }

    public static void animateShrink(Context context) {
        ((Activity) context).overridePendingTransition(
                R.anim.animate_shrink_enter,
                R.anim.animate_shrink_exit
        );
    }

    public static void animateCard(Context context) {
        ((Activity) context).overridePendingTransition(
                R.anim.animate_card_enter,
                R.anim.animate_card_exit
        );
    }

    public static void animateInAndOut(Context context) {
        ((Activity) context).overridePendingTransition(
                R.anim.animate_in_out_enter,
                R.anim.animate_in_out_exit
        );
    }

    public static void animateSwipeLeft(Context context) {
        ((Activity) context).overridePendingTransition(
                R.anim.animate_swipe_left_enter,
                R.anim.animate_swipe_left_exit
        );
    }

    public static void animateSwipeRight(Context context) {
        ((Activity) context).overridePendingTransition(
                R.anim.animate_swipe_right_enter,
                R.anim.animate_swipe_right_exit
        );
    }

    public static void animateSlideLeft(Context context) {
        ((Activity) context).overridePendingTransition(
                R.anim.animate_slide_left_enter,
                R.anim.animate_slide_left_exit
        );
    }

    public static void animateSlideRight(Context context) {
        ((Activity) context).overridePendingTransition(
                R.anim.animate_slide_in_left,
                R.anim.animate_slide_out_right
        );
    }

    public static void animateSlideDown(Context context) {
        ((Activity) context).overridePendingTransition(
                R.anim.animate_slide_down_enter,
                R.anim.animate_slide_down_exit
        );
    }

    public static void animateSlideUp(Context context) {
        ((Activity) context).overridePendingTransition(
                R.anim.animate_slide_up_enter,
                R.anim.animate_slide_up_exit
        );
    }
}
