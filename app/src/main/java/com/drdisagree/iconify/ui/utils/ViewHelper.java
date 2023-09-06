package com.drdisagree.iconify.ui.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.TypedValue;

import androidx.annotation.AttrRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.drdisagree.iconify.Iconify;

import java.util.Objects;

public class ViewHelper {

    public static void disableNestedScrolling(ViewPager2 viewPager) {
        RecyclerView recyclerView = null;

        for (int i = 0; i < viewPager.getChildCount(); i++) {
            if (viewPager.getChildAt(i) instanceof RecyclerView) {
                recyclerView = (RecyclerView) viewPager.getChildAt(i);
                break;
            }
        }

        if (recyclerView != null) {
            recyclerView.setNestedScrollingEnabled(false);
        }
    }

    public static void setHeader(Context context, Toolbar toolbar, int title) {
        ((AppCompatActivity) context).setSupportActionBar(toolbar);
        Objects.requireNonNull(((AppCompatActivity) context).getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) context).getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setTitle(title);
    }

    public static int dp2px(float dp) {
        return dp2px((int) dp);
    }

    public static int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Objects.requireNonNull(Iconify.getAppContext()).getResources().getDisplayMetrics());
    }
}
