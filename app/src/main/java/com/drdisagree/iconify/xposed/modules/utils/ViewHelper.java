package com.drdisagree.iconify.xposed.modules.utils;

import static de.robv.android.xposed.XposedBridge.log;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ViewHelper {

    public static void setMargins(Object viewGroup, Context context, int left, int top, int right, int bottom) {
        if (viewGroup instanceof View) {
            ViewGroup.LayoutParams layoutParams = ((View) viewGroup).getLayoutParams();

            if (layoutParams instanceof LinearLayout.LayoutParams) {
                ((LinearLayout.LayoutParams) layoutParams).setMargins(dp2px(context, left), dp2px(context, top), dp2px(context, right), dp2px(context, bottom));
            } else if (layoutParams instanceof FrameLayout.LayoutParams) {
                ((FrameLayout.LayoutParams) layoutParams).setMargins(dp2px(context, left), dp2px(context, top), dp2px(context, right), dp2px(context, bottom));
            } else if (layoutParams instanceof RelativeLayout.LayoutParams) {
                ((RelativeLayout.LayoutParams) layoutParams).setMargins(dp2px(context, left), dp2px(context, top), dp2px(context, right), dp2px(context, bottom));
            } else {
                log("Unsupported type: " + layoutParams.toString());
            }
        } else if (viewGroup instanceof ViewGroup.MarginLayoutParams) {
            ((ViewGroup.MarginLayoutParams) viewGroup).setMargins(dp2px(context, left), dp2px(context, top), dp2px(context, right), dp2px(context, bottom));
        } else {
            throw new IllegalArgumentException("The viewGroup object has to be either a View or a ViewGroup.MarginLayoutParams. Found " + viewGroup.getClass().getSimpleName() + " instead.");
        }
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

    public static void findViewWithTagAndChangeColor(ViewGroup parent, String tagContains, int color) {
        if (parent == null || parent.getChildCount() == 0) {
            return;
        }

        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);

            Object tagObject = child.getTag();
            if (tagObject != null && tagObject.toString().contains(tagContains)) {
                changeViewColor(child, color);
            }

            if (child instanceof ViewGroup) {
                findViewWithTagAndChangeColor((ViewGroup) child, tagContains, color);
            }
        }
    }

    private static void changeViewColor(View view, int color) {
        if (view instanceof TextView textView) {
            textView.setTextColor(color);

            Drawable[] drawablesRelative = textView.getCompoundDrawablesRelative();
            for (Drawable drawable : drawablesRelative) {
                if (drawable != null) {
                    drawable.mutate();
                    drawable.setTint(color);
                    drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
                }
            }

            Drawable[] drawables = textView.getCompoundDrawables();
            for (Drawable drawable : drawables) {
                if (drawable != null) {
                    drawable.mutate();
                    drawable.setTint(color);
                    drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
                }
            }
        } else if (view instanceof ImageView imageView) {
            imageView.setColorFilter(color);
        } else if (view instanceof ViewGroup viewGroup) {
            viewGroup.setBackgroundTintList(ColorStateList.valueOf(color));
        } else if (view instanceof ProgressBar progressBar) {
            progressBar.setProgressTintList(ColorStateList.valueOf(color));
            progressBar.setProgressBackgroundTintList(ColorStateList.valueOf(color));
        } else {
            view.getBackground().mutate().setTint(color);
        }
    }

    public static void applyFontRecursively(ViewGroup viewGroup, Typeface typeface) {
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof ViewGroup) {
                applyFontRecursively((ViewGroup) child, typeface);
            } else if (child instanceof TextView textView) {
                textView.setTypeface(typeface);
            }
        }
    }

    public static void applyTextMarginRecursively(ViewGroup viewGroup, int topMargin) {
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof ViewGroup) {
                applyTextMarginRecursively((ViewGroup) child, topMargin);
            } else if (child instanceof TextView) {
                ViewGroup.LayoutParams params = child.getLayoutParams();
                if (params instanceof LinearLayout.LayoutParams linearParams) {
                    linearParams.topMargin += topMargin;
                    child.setLayoutParams(linearParams);
                } else if (params instanceof FrameLayout.LayoutParams frameParams) {
                    frameParams.topMargin += topMargin;
                    child.setLayoutParams(frameParams);
                } else if (params instanceof RelativeLayout.LayoutParams relativeParams) {
                    relativeParams.topMargin += topMargin;
                    child.setLayoutParams(relativeParams);
                } else {
                    log("Invalid params: " + params);
                }
            }
        }
    }

    public static void applyTextScalingRecursively(ViewGroup viewGroup, float scaleFactor) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof ViewGroup) {
                applyTextScalingRecursively((ViewGroup) child, scaleFactor);
            } else if (child instanceof TextView textView) {
                float originalSize = textView.getTextSize();
                float newSize = originalSize * scaleFactor;
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, newSize);
            }
        }
    }
}
