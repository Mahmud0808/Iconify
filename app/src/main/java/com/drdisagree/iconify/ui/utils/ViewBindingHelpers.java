package com.drdisagree.iconify.ui.utils;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static com.bumptech.glide.request.RequestOptions.centerCropTransform;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

public class ViewBindingHelpers {

    public static void setImageUrl(ImageView imageView, String url) {
        Glide.with(imageView.getContext()).load(url.replace("http://", "https://")).apply(centerCropTransform()).transition(withCrossFade()).into(imageView);
    }

    public static void setDrawable(ImageView imageView, Drawable drawable) {
        Glide.with(imageView.getContext()).load(drawable).into(imageView);
    }

    public static void setDrawable(ViewGroup viewGroup, Drawable drawable) {
        Glide.with(viewGroup.getContext()).load(drawable).into(new CustomTarget<Drawable>() {
            @Override
            public void onResourceReady(@NonNull Drawable resource, Transition<? super Drawable> transition) {
                viewGroup.setBackground(resource);
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {

            }
        });
    }

    public static void setBitmap(ImageView imageView, Bitmap bitmap) {
        Drawable drawable = new BitmapDrawable(imageView.getContext().getResources(), bitmap);
        setDrawable(imageView, drawable);
    }
}