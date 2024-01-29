package com.drdisagree.iconify.ui.core;

import android.content.res.Configuration;
import android.view.View;
import android.widget.ImageView;

import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;

import com.drdisagree.iconify.R;

@SuppressWarnings("unused")
public class Transform {

    public static CompositePageTransformer getPageCompositePageTransformer() {
        CompositePageTransformer pageTransformer = new CompositePageTransformer();
        pageTransformer.addTransformer(new MarginPageTransformer(40));
        pageTransformer.addTransformer((page, position) -> {
            float r = 1 - Math.abs(position);
            page.setScaleY(0.85f + r * 0.15f);
            setParallaxTransformation(page, position);
        });
        return pageTransformer;
    }

    public static void setParallaxTransformation(View page, float position) {
        ImageView parallaxView = page.findViewById(R.id.img);
        boolean isLandscape = page.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;

        if (position < -1) {
            // This page is way off-screen to the left.
            page.setAlpha(1f);
        } else if (position <= 1) {
            // [-1,1]
            parallaxView.setTranslationX(-position * ((isLandscape ? page.getHeight() : page.getWidth()) / 2f)); // Half the normal speed
        } else {
            // (1,+Infinity]
            // This page is way off-screen to the right.
            page.setAlpha(1f);
        }
    }
}