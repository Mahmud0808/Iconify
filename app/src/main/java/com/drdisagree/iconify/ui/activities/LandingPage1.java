package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.utils.SystemUtil.isDarkMode;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.utils.SystemUtil;

public class LandingPage1 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SystemUtil.isDarkMode()) {
            getWindow().getDecorView().setBackgroundColor(Color.parseColor("#FFF0F3"));
            getWindow().setStatusBarColor(Color.parseColor("#FFF0F3"));
            getWindow().setNavigationBarColor(Color.parseColor("#FFF0F3"));
        } else {
            getWindow().getDecorView().setBackgroundColor(Color.parseColor("#191213"));
            getWindow().setStatusBarColor(Color.parseColor("#191213"));
            getWindow().setNavigationBarColor(Color.parseColor("#191213"));
        }

        setContentView(R.layout.activity_landing_page_one);

        if (isDarkMode())
            ((LottieAnimationView) findViewById(R.id.welcome_anim)).setAnimation("Lottie/anim_view_one_night.lottie");

        ((Button) findViewById(R.id.btn_next)).setOnClickListener(v -> startActivity(new Intent(LandingPage1.this, LandingPage2.class)));

        ((Button) findViewById(R.id.btn_skip)).setOnClickListener(v -> startActivity(new Intent(LandingPage1.this, LandingPage3.class)));
    }
}