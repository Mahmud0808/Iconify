package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.utils.SystemUtil.isDarkMode;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.RenderMode;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.utils.SystemUtil;

public class LandingPage2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SystemUtil.isDarkMode()) {
            getWindow().getDecorView().setBackgroundColor(Color.parseColor("#EFF7FA"));
            getWindow().setStatusBarColor(Color.parseColor("#EFF7FA"));
            getWindow().setNavigationBarColor(Color.parseColor("#EFF7FA"));
        } else {
            getWindow().getDecorView().setBackgroundColor(Color.parseColor("#111617"));
            getWindow().setStatusBarColor(Color.parseColor("#111617"));
            getWindow().setNavigationBarColor(Color.parseColor("#111617"));
        }

        setContentView(R.layout.activity_landing_page_two);

        ((LottieAnimationView) findViewById(R.id.welcome_anim)).setAnimation(!isDarkMode() ? R.raw.anim_view_two_day : R.raw.anim_view_two_night);

        ((LottieAnimationView) findViewById(R.id.welcome_anim)).setRenderMode(RenderMode.HARDWARE);

        findViewById(R.id.btn_next).setOnClickListener(v -> startActivity(new Intent(LandingPage2.this, LandingPage3.class)));

        findViewById(R.id.btn_back).setOnClickListener(v -> onBackPressed());

        findViewById(R.id.btn_skip).setOnClickListener(v -> startActivity(new Intent(LandingPage2.this, LandingPage3.class)));
    }
}