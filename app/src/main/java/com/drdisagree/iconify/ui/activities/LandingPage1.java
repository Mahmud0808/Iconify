package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.utils.SystemUtil.isDarkMode;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.RenderMode;
import com.drdisagree.iconify.R;

public class LandingPage1 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.landing_page_one_background));
        getWindow().setStatusBarColor(getResources().getColor(R.color.landing_page_one_background));
        getWindow().setNavigationBarColor(getResources().getColor(R.color.landing_page_one_background));
        setContentView(R.layout.activity_landing_page_one);

        ((LottieAnimationView) findViewById(R.id.welcome_anim)).setAnimation(!isDarkMode() ? R.raw.anim_view_one_day : R.raw.anim_view_one_night);

        ((LottieAnimationView) findViewById(R.id.welcome_anim)).setRenderMode(RenderMode.HARDWARE);

        findViewById(R.id.btn_next).setOnClickListener(v -> startActivity(new Intent(LandingPage1.this, LandingPage2.class)));

        findViewById(R.id.btn_skip).setOnClickListener(v -> startActivity(new Intent(LandingPage1.this, LandingPage3.class)));
    }
}