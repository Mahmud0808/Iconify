package com.drdisagree.iconify.xposed.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class ViewHelper {

    public static void setMargins(Object viewGroup, Context context, int left, int top, int right, int bottom) {
        if (viewGroup instanceof View) {
            if (((View) viewGroup).getLayoutParams() instanceof LinearLayout.LayoutParams)
                ((LinearLayout.LayoutParams) ((View) viewGroup).getLayoutParams()).setMargins(dp2px(context, left), dp2px(context, top), dp2px(context, right), dp2px(context, bottom));
            else if (((View) viewGroup).getLayoutParams() instanceof FrameLayout.LayoutParams)
                ((FrameLayout.LayoutParams) ((View) viewGroup).getLayoutParams()).setMargins(dp2px(context, left), dp2px(context, top), dp2px(context, right), dp2px(context, bottom));
        } else if (viewGroup instanceof ViewGroup.MarginLayoutParams)
            ((ViewGroup.MarginLayoutParams) viewGroup).setMargins(dp2px(context, left), dp2px(context, top), dp2px(context, right), dp2px(context, bottom));
        else
            throw new IllegalArgumentException("The viewGroup object has to be either a View or a ViewGroup.MarginLayoutParams. Found " + viewGroup.getClass().getSimpleName() + " instead.");
    }

    public static void setPaddings(ViewGroup viewGroup, Context context, int left, int top, int right, int bottom) {
        viewGroup.setPadding(dp2px(context, left), dp2px(context, top), dp2px(context, right), dp2px(context, bottom));
    }

    public static int dp2px(Context context, float dp) {
        return dp2px(context, (int) dp);
    }

    public static int dp2px(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static void setAlphaForBackgroundDrawables(View view, float alpha) {
        setAlphaForBackgroundDrawables(view, (int) (alpha * 255));
    }

    private static void setAlphaForBackgroundDrawables(View view, int alpha) {
        if (view instanceof ViewGroup viewGroup) {
            int childCount = viewGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = viewGroup.getChildAt(i);
                setAlphaForBackgroundDrawablesRecursive(child, alpha);
            }
        }
    }

    private static void setAlphaForBackgroundDrawablesRecursive(View view, int alpha) {
        Drawable backgroundDrawable = view.getBackground();
        if (backgroundDrawable != null) {
            backgroundDrawable.setAlpha(alpha);
        }

        if (view instanceof ViewGroup viewGroup) {
            int childCount = viewGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = viewGroup.getChildAt(i);
                setAlphaForBackgroundDrawablesRecursive(child, alpha);
            }
        }
    }
}
