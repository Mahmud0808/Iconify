package com.drdisagree.iconify.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

/** The SectionView base for views */
public abstract class SectionView extends LinearLayout {

    /** The callback for the section view updates. */
    public interface SectionViewListener {
        void onViewActivated(@Nullable Context context, boolean viewActivated);
    }

    protected SectionViewListener mSectionViewListener;
    private String mTitle;

    public SectionView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getTitle() {
        return mTitle;
    }

    /** Sets the listener to the {@code SectionView} instance for reacting the view changes. */
    public void setViewListener(SectionViewListener sectionViewListener) {
        mSectionViewListener = sectionViewListener;
    }
}
