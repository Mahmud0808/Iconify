package com.drdisagree.iconify.ui.preferences.preferencesearch;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewAnimationUtils;

import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

public class AnimationUtils {
    public static void registerCircularRevealAnimation(final Context context, final View view, final RevealAnimationSetting revealSettings) {
        final int startColor = revealSettings.getColorAccent();
        final int endColor = getBackgroundColor(view);

        view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                v.removeOnLayoutChangeListener(this);
                view.setVisibility(View.VISIBLE);
                int cx = revealSettings.getCenterX();
                int cy = revealSettings.getCenterY();
                int width = revealSettings.getWidth();
                int height = revealSettings.getHeight();
                int duration = context.getResources().getInteger(android.R.integer.config_longAnimTime);

                //Simply use the diagonal of the view
                float finalRadius = (float) Math.sqrt(width * width + height * height);
                Animator anim = ViewAnimationUtils.createCircularReveal(v, cx, cy, 0, finalRadius).setDuration(duration);
                anim.setInterpolator(new FastOutSlowInInterpolator());
                anim.start();
                startColorAnimation(view, startColor, endColor, duration);
            }
        });
    }

    private static void startColorAnimation(final View view, final int startColor, final int endColor, int duration) {
        ValueAnimator anim = new ValueAnimator();
        anim.setIntValues(startColor, endColor);
        anim.setEvaluator(new ArgbEvaluator());
        anim.addUpdateListener(valueAnimator -> view.setBackgroundColor((Integer) valueAnimator.getAnimatedValue()));
        anim.setDuration(duration);
        anim.start();
    }

    @SuppressWarnings("unused")
    public static void startCircularExitAnimation(final Context context, final View view, final RevealAnimationSetting revealSettings, final OnDismissedListener listener) {
        final int startColor = getBackgroundColor(view);
        final int endColor = revealSettings.getColorAccent();

        int cx = revealSettings.getCenterX();
        int cy = revealSettings.getCenterY();
        int width = revealSettings.getWidth();
        int height = revealSettings.getHeight();
        int duration = context.getResources().getInteger(android.R.integer.config_longAnimTime);

        float initRadius = (float) Math.sqrt(width * width + height * height);
        Animator anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, initRadius, 0);
        anim.setDuration(duration);
        anim.setInterpolator(new FastOutSlowInInterpolator());
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.INVISIBLE);
                listener.onDismissed();
            }
        });
        anim.start();
        startColorAnimation(view, startColor, endColor, duration);
    }

    private static int getBackgroundColor(View view) {
        int color = Color.TRANSPARENT;
        Drawable background = view.getBackground();
        if (background instanceof ColorDrawable) {
            color = ((ColorDrawable) background).getColor();
        }
        return color;
    }

    public interface OnDismissedListener {
        void onDismissed();
    }
}
