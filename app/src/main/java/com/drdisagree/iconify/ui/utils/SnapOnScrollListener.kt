package com.drdisagree.iconify.ui.utils;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

public class SnapOnScrollListener extends RecyclerView.OnScrollListener {
    private final SnapHelper snapHelper;
    private Behavior behavior = Behavior.NOTIFY_ON_SCROLL;
    private OnSnapPositionChangeListener onSnapPositionChangeListener;

    private int snapPosition = RecyclerView.NO_POSITION;

    public SnapOnScrollListener(SnapHelper snapHelper) {
        this.snapHelper = snapHelper;
    }

    public SnapOnScrollListener(SnapHelper snapHelper, Behavior behavior, OnSnapPositionChangeListener onSnapPositionChangeListener) {
        this.snapHelper = snapHelper;
        this.behavior = behavior;
        this.onSnapPositionChangeListener = onSnapPositionChangeListener;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        if (behavior == Behavior.NOTIFY_ON_SCROLL) {
            maybeNotifySnapPositionChange(recyclerView);
        }
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        if (behavior == Behavior.NOTIFY_ON_SCROLL_STATE_IDLE
                && newState == RecyclerView.SCROLL_STATE_IDLE) {
            maybeNotifySnapPositionChange(recyclerView);
        }
    }

    private void maybeNotifySnapPositionChange(RecyclerView recyclerView) {
        int snapPosition = getSnapPosition(recyclerView);
        boolean snapPositionChanged = this.snapPosition != snapPosition;
        if (snapPositionChanged) {
            if (onSnapPositionChangeListener != null) {
                onSnapPositionChangeListener.onSnapPositionChange(snapPosition);
            }
            this.snapPosition = snapPosition;
        }
    }

    public int getSnapPosition(RecyclerView recyclerView) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager == null) {
            return RecyclerView.NO_POSITION;
        }
        View snapView = snapHelper.findSnapView(layoutManager);
        if (snapView == null) {
            return RecyclerView.NO_POSITION;
        }
        return layoutManager.getPosition(snapView);
    }


    public enum Behavior {
        NOTIFY_ON_SCROLL,
        NOTIFY_ON_SCROLL_STATE_IDLE
    }

    public interface OnSnapPositionChangeListener {
        void onSnapPositionChange(int position);
    }
}
