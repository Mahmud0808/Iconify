package com.drdisagree.iconify.ui.utils;

import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CarouselLayoutManager extends LinearLayoutManager {

    private float minifyAmount = 0.05f;
    private float minifyDistance = 0.9f;

    public CarouselLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int scrolled = super.scrollHorizontallyBy(dx, recycler, state);
        updateScaleFactors();
        return scrolled;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        super.onLayoutChildren(recycler, state);
        updateScaleFactors();
    }

    private void updateScaleFactors() {
        float parentMidpoint = getWidth() / 2.0f;
        float d0 = 0.00f;
        float d1 = parentMidpoint * minifyDistance;
        float s0 = 1.0f;
        float s1 = 1.0f - minifyAmount;

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child == null) continue;

            float childMidpoint = (getDecoratedLeft(child) + getDecoratedRight(child)) / 2.f;
            float d = Math.min(d1, Math.abs(parentMidpoint - childMidpoint));
            float scaleFactor = s0 + (s1 - s0) * (d - d0) / (d1 - d0);

            child.setScaleX(scaleFactor);
            child.setScaleY(scaleFactor);
        }
    }

    // Getters and setters for customization options
    public void setMinifyAmount(float minifyAmount) {
        this.minifyAmount = minifyAmount;
    }

    public void setMinifyDistance(float minifyDistance) {
        this.minifyDistance = minifyDistance;
    }
}

