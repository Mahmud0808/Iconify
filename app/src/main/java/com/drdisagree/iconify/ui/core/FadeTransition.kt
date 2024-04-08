package com.drdisagree.iconify.ui.core;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.transition.Transition;
import android.transition.TransitionValues;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.drdisagree.iconify.R;

@SuppressWarnings("unused")
public class FadeTransition extends Transition {

    private static final String PROP_NAME_ALPHA = "android:custom:alpha";
    private float startAlpha = 0.0f;
    private float endAlpha = 1.0f;

    public FadeTransition(float startAlpha, float endAlpha) {
        this.startAlpha = startAlpha;
        this.endAlpha = endAlpha;
    }

    public FadeTransition(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FadeTransition);
        startAlpha = a.getFloat(R.styleable.FadeTransition_startAlpha, startAlpha);
        endAlpha = a.getFloat(R.styleable.FadeTransition_endAlpha, endAlpha);
        a.recycle();
    }

    private void captureValues(TransitionValues transitionValues) {
        transitionValues.values.put(PROP_NAME_ALPHA, transitionValues.view.getAlpha());
    }

    @Override
    public void captureStartValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    @Override
    public void captureEndValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    @Override
    public Animator createAnimator(ViewGroup sceneRoot, TransitionValues startValues, TransitionValues endValues) {
        View view = endValues.view;
        if (startAlpha != endAlpha) {
            view.setAlpha(endAlpha);
        }
        return ObjectAnimator.ofFloat(view, View.ALPHA, startAlpha, endAlpha);
    }
}
