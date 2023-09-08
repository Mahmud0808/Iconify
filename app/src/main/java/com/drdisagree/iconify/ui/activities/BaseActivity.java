package com.drdisagree.iconify.ui.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.ui.utils.ThemeHelper;
import com.drdisagree.iconify.utils.helpers.LocaleHelper;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.shape.MaterialShapeDrawable;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(ThemeHelper.getTheme());
        super.onCreate(savedInstanceState);

        setupEdgetoEdge();
    }

    private void setupEdgetoEdge() {
        try {
            ((AppBarLayout) findViewById(R.id.appBarLayout)).setStatusBarForeground(MaterialShapeDrawable.createWithElevationOverlay(getApplicationContext()));
        } catch (Exception ignored) {
        }
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}