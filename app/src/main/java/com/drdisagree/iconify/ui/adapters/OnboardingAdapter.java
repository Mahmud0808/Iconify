package com.drdisagree.iconify.ui.adapters;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.ui.fragments.Onboarding;

public class OnboardingAdapter extends FragmentStateAdapter {

    private final Context context;

    public OnboardingAdapter(@NonNull FragmentActivity fragmentActivity, Context context) {
        super(fragmentActivity);
        this.context = context;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return getFragment(position);
    }

    public Fragment getFragment(int position) {
        return switch (position) {
            default -> Onboarding.newInstance(
                    context.getResources().getString(R.string.onboarding_title_1),
                    context.getResources().getString(R.string.onboarding_desc_1),
                    R.raw.onboarding_lottie_1,
                    R.color.onboarding_text_one_primary,
                    R.color.onboarding_text_one_secondary
            );
            case 1 -> Onboarding.newInstance(
                    context.getResources().getString(R.string.onboarding_title_2),
                    context.getResources().getString(R.string.onboarding_desc_2),
                    R.raw.onboarding_lottie_2,
                    R.color.onboarding_text_two_primary,
                    R.color.onboarding_text_two_secondary
            );
            case 2 -> Onboarding.newInstance(
                    context.getResources().getString(R.string.onboarding_title_3),
                    context.getResources().getString(R.string.onboarding_desc_3),
                    R.raw.onboarding_lottie_3,
                    R.color.onboarding_text_three_primary,
                    R.color.onboarding_text_three_secondary
            );
        };
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
