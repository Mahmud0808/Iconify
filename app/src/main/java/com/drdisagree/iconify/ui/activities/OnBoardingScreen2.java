package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.utils.SystemUtil.isDarkMode;

import android.content.Intent;
import android.os.Bundle;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.RenderMode;
import com.drdisagree.iconify.R;

public class OnBoardingScreen2 extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.landing_page_two_background));
        getWindow().setStatusBarColor(getResources().getColor(R.color.landing_page_two_background));
        getWindow().setNavigationBarColor(getResources().getColor(R.color.landing_page_two_background));
        setContentView(R.layout.activity_onboarding_screen_two);

        ((LottieAnimationView) findViewById(R.id.welcome_anim)).setAnimation(!isDarkMode() ? R.raw.anim_view_two_day : R.raw.anim_view_two_night);

        ((LottieAnimationView) findViewById(R.id.welcome_anim)).setRenderMode(RenderMode.HARDWARE);

        findViewById(R.id.btn_next).setOnClickListener(v -> startActivity(new Intent(OnBoardingScreen2.this, OnBoardingScreen3.class)));

        findViewById(R.id.btn_back).setOnClickListener(v -> onBackPressed());

        findViewById(R.id.btn_skip).setOnClickListener(v -> startActivity(new Intent(OnBoardingScreen2.this, OnBoardingScreen3.class)));
    }
}