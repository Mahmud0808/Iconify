package com.drdisagree.iconify.ui.activities;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.drdisagree.iconify.databinding.ActivityOnboardingBinding;
import com.drdisagree.iconify.ui.views.OnboardingView;

public class OnboardingActivity extends AppCompatActivity {

    ActivityOnboardingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                try {
                    OnboardingView.navigateToPrevSlide();
                } catch (Exception ignored) {
                    OnboardingActivity.this.finish();
                    System.exit(0);
                }
            }
        });
    }
}
