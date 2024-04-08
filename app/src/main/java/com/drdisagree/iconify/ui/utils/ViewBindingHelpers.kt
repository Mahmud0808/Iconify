package com.drdisagree.iconify.ui.utils

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

object ViewBindingHelpers {

    @JvmStatic
    fun setImageUrl(imageView: ImageView, url: String) {
        Glide.with(imageView.context).load(url.replace("http://", "https://"))
            .apply(RequestOptions.centerCropTransform())
            .transition(DrawableTransitionOptions.withCrossFade()).into(imageView)
    }

    @JvmStatic
    fun setDrawable(imageView: ImageView, drawable: Drawable?) {
        Glide.with(imageView.context).load(drawable).into(imageView)
    }

    @JvmStatic
    fun setDrawableWithAnimation(imageView: ImageView, drawable: Drawable?) {
        Glide.with(imageView.context).load(drawable)
            .transition(DrawableTransitionOptions.withCrossFade()).into(imageView)
    }

    @JvmStatic
    fun setDrawable(viewGroup: ViewGroup, drawable: Drawable?) {
        Glide.with(viewGroup.context).load(drawable).into(object : CustomTarget<Drawable?>() {
            override fun onResourceReady(
                resource: Drawable,
                transition: Transition<in Drawable?>?
            ) {
                viewGroup.background = resource
            }

            override fun onLoadCleared(placeholder: Drawable?) {}
        })
    }

    @JvmStatic
    fun setDrawableWithAnimation(viewGroup: ViewGroup, drawable: Drawable?) {
        Glide.with(viewGroup.context).load(drawable)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(object : CustomTarget<Drawable?>() {
                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable?>?
                ) {
                    viewGroup.background = resource
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }

    @JvmStatic
    fun setBitmap(imageView: ImageView, bitmap: Bitmap?) {
        val drawable: Drawable = BitmapDrawable(imageView.context.resources, bitmap)
        setDrawable(imageView, drawable)
    }

    @JvmStatic
    fun setBitmapWithAnimation(imageView: ImageView, bitmap: Bitmap?) {
        val drawable: Drawable = BitmapDrawable(imageView.context.resources, bitmap)
        setDrawableWithAnimation(imageView, drawable)
    }
}