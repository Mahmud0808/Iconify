package com.drdisagree.iconify.ui.adapters;

import static com.drdisagree.iconify.utils.SystemUtil.isDarkMode;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.ui.fragments.Onboarding;

public class OnboardingAdapter extends FragmentStateAdapter {

    private final Context context;
    private Fragment currentFragment = null;

    public OnboardingAdapter(@NonNull FragmentActivity fragmentActivity, Context context) {
        super(fragmentActivity);
        this.context = context;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            default:
            case 0:
                currentFragment = Onboarding.newInstance(
                        context.getResources().getString(R.string.onboarding_title_1),
                        context.getResources().getString(R.string.onboarding_desc_1),
                        !isDarkMode() ? R.raw.onboarding_lottie_1_light : R.raw.onboarding_lottie_1_dark
                );
                return currentFragment;
            case 1:
                currentFragment = Onboarding.newInstance(
                        context.getResources().getString(R.string.onboarding_title_2),
                        context.getResources().getString(R.string.onboarding_desc_2),
                        !isDarkMode() ? R.raw.onboarding_lottie_2_light : R.raw.onboarding_lottie_2_dark
                );
                return currentFragment;
            case 2:
                currentFragment = Onboarding.newInstance(
                        context.getResources().getString(R.string.onboarding_title_3),
                        context.getResources().getString(R.string.onboarding_desc_3),
                        !isDarkMode() ? R.raw.onboarding_lottie_3_light : R.raw.onboarding_lottie_3_dark
                );
                return currentFragment;
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }

    public Fragment getCurrentFragment() {
        return currentFragment;
    }
}
