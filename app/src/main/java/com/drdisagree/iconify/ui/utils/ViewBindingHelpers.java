package com.drdisagree.iconify.ui.utils;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static com.bumptech.glide.request.RequestOptions.centerCropTransform;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

public class ViewBindingHelpers {

    public static void setImageUrl(ImageView imageView, String url) {
        Glide.with(imageView.getContext()).load(url.replace("http://", "https://")).apply(centerCropTransform()).transition(withCrossFade()).into(imageView);
    }

    public static void setDrawable(ImageView imageView, Drawable drawable) {
        Glide.with(imageView.getContext()).load(drawable).into(imageView);
    }

    public static void setDrawable(LinearLayout linearLayout, Drawable drawable) {
        Glide.with(linearLayout.getContext()).load(drawable).into(new SimpleTarget<Drawable>() {
            @Override
            public void onResourceReady(@NonNull Drawable resource, Transition<? super Drawable> transition) {
                linearLayout.setBackground(resource);
            }
        });
    }
}