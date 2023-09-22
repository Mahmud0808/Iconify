package com.drdisagree.iconify.ui.views;

import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableWrapper;
import android.graphics.drawable.InsetDrawable;

import androidx.annotation.NonNull;

public final class RoundedCornerProgressDrawable extends InsetDrawable {

    public RoundedCornerProgressDrawable() {
        this(null);
    }

    public RoundedCornerProgressDrawable(Drawable drawable) {
        super(drawable, 0);
    }

    public int getChangingConfigurations() {
        return super.getChangingConfigurations() | ActivityInfo.CONFIG_DENSITY;
    }

    public Drawable.ConstantState getConstantState() {
        return new RoundedCornerState(super.getConstantState());
    }

    protected void onBoundsChange(Rect rect) {
        super.onBoundsChange(rect);
        onLevelChange(getLevel());
    }

    public boolean onLayoutDirectionChanged(int level) {
        onLevelChange(getLevel());
        return super.onLayoutDirectionChanged(level);
    }

    protected boolean onLevelChange(int n) {
        Drawable drawable = getDrawable();
        Rect bounds;
        if (drawable == null) {
            bounds = null;
        } else {
            bounds = drawable.getBounds();
        }
        int height = getBounds().height();
        int level = (getBounds().width() - getBounds().height()) * n / 10000;
        drawable = getDrawable();
        if (drawable != null && bounds != null) {
            drawable.setBounds(getBounds().left, bounds.top, getBounds().left + (height + level), bounds.bottom);
        }
        return super.onLevelChange(level);
    }

    private static final class RoundedCornerState extends Drawable.ConstantState {
        private final Drawable.ConstantState mWrappedState;

        public RoundedCornerState(Drawable.ConstantState wrappedState) {
            mWrappedState = wrappedState;
        }

        public int getChangingConfigurations() {
            return mWrappedState.getChangingConfigurations();
        }

        @NonNull
        public Drawable newDrawable() {
            return newDrawable(null, null);
        }

        @NonNull
        public Drawable newDrawable(Resources resources, Resources.Theme theme) {
            Drawable drawable = mWrappedState.newDrawable(resources, theme);
            return new RoundedCornerProgressDrawable(((DrawableWrapper) drawable).getDrawable());
        }
    }
}