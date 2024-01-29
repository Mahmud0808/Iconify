package com.drdisagree.iconify.ui.core;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.res.TypedArray;
import android.transition.TransitionValues;
import android.transition.Visibility;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.view.ViewCompat;

import com.drdisagree.iconify.R;

@SuppressWarnings("unused")
public class PopTransition extends Visibility {

    private float startScale = 0.0f;
    private float endScale = 1.0f;

    public PopTransition(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PopTransition);
        startScale = a.getFloat(R.styleable.PopTransition_startScale, startScale);
        endScale = a.getFloat(R.styleable.PopTransition_endScale, endScale);
        a.recycle();
    }

    @Override
    public Animator onAppear(ViewGroup sceneRoot, View view, TransitionValues startValues, TransitionValues endValues) {
        view.setScaleX(startScale);
        view.setScaleY(startScale);
        return ObjectAnimator.ofPropertyValuesHolder(
                view,
                PropertyValuesHolder.ofFloat(View.SCALE_X, endScale),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, endScale)
        );
    }

    @Override
    public Animator onDisappear(ViewGroup sceneRoot, View view, TransitionValues startValues, TransitionValues endValues) {
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(
                view,
                PropertyValuesHolder.ofFloat(View.SCALE_X, endScale),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, endScale)
        );
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Reset View X & Y to allow shared element to return with proper start dimension
                view.setScaleX(startScale);
                view.setScaleY(startScale);
                ViewCompat.postInvalidateOnAnimation(view);
            }
        });
        return animator;
    }
}
