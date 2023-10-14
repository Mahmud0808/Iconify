package com.drdisagree.iconify.ui.fragments;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.databinding.FragmentOnboardingBinding;
import com.drdisagree.iconify.utils.SystemUtil;
import com.drdisagree.iconify.utils.helper.DisplayUtil;

import java.util.Objects;

public class Onboarding extends BaseFragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";
    private static final String ARG_PARAM4 = "param4";
    private static final String ARG_PARAM5 = "param5";
    private String title;
    private String description;
    private int imageResource;
    private int textColorPrimary, textColorSecondary;
    private FragmentOnboardingBinding binding;

    public static Onboarding newInstance(String title, String description, int imageResource, int textColorPrimary, int textColorSecondary) {
        Onboarding fragment = new Onboarding();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, title);
        args.putString(ARG_PARAM2, description);
        args.putInt(ARG_PARAM3, imageResource);
        args.putInt(ARG_PARAM4, textColorPrimary);
        args.putInt(ARG_PARAM5, textColorSecondary);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            title = getArguments().getString(ARG_PARAM1);
            description = getArguments().getString(ARG_PARAM2);
            imageResource = getArguments().getInt(ARG_PARAM3);
            textColorPrimary = getArguments().getInt(ARG_PARAM4);
            textColorSecondary = getArguments().getInt(ARG_PARAM5);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOnboardingBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        binding.textOnboardingTitle.setText(title);
        binding.textOnboardingDescription.setText(description);
        binding.imageOnboarding.setAnimation(imageResource);
        binding.textOnboardingTitle.setTextColor(ContextCompat.getColor(requireActivity(), textColorPrimary));
        binding.textOnboardingDescription.setTextColor(ContextCompat.getColor(requireActivity(), textColorSecondary));

        remeasureConstraints();

        return view;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        remeasureConstraints();
    }

    private void remeasureConstraints() {
        binding.getRoot().requestLayout();
        int screenWidth = SystemUtil.getScreenWidth(requireActivity());
        int screenHeight = SystemUtil.getScreenHeight(requireActivity());
        Configuration configuration = getResources().getConfiguration();

        if (screenWidth > screenHeight || configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            ConstraintLayout.LayoutParams lottieLayoutParams = new ConstraintLayout.LayoutParams(0, ConstraintLayout.LayoutParams.WRAP_CONTENT);
            lottieLayoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
            lottieLayoutParams.endToStart = R.id.text_onboarding;
            lottieLayoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
            lottieLayoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
            binding.imageOnboarding.setLayoutParams(lottieLayoutParams);

            ConstraintLayout.LayoutParams textLayoutParams = new ConstraintLayout.LayoutParams(0, ConstraintLayout.LayoutParams.WRAP_CONTENT);
            textLayoutParams.startToEnd = R.id.image_onboarding;
            textLayoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
            textLayoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
            textLayoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
            textLayoutParams.setMarginStart(DisplayUtil.IntToDp(28));
            textLayoutParams.setMarginEnd(DisplayUtil.IntToDp(28));
            binding.textOnboarding.setLayoutParams(textLayoutParams);
        } else {
            ConstraintLayout.LayoutParams lottieLayoutParams = (ConstraintLayout.LayoutParams) binding.imageOnboarding.getLayoutParams();
            lottieLayoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
            lottieLayoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
            lottieLayoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
            lottieLayoutParams.bottomToTop = R.id.guideline_lottie;
            binding.imageOnboarding.setLayoutParams(lottieLayoutParams);

            ConstraintLayout.LayoutParams textLayoutParams = (ConstraintLayout.LayoutParams) binding.textOnboarding.getLayoutParams();
            textLayoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
            textLayoutParams.endToStart = R.id.guideline_text;
            textLayoutParams.topToBottom = R.id.image_onboarding;
            textLayoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
            textLayoutParams.setMarginStart(DisplayUtil.IntToDp(28));
            textLayoutParams.setMarginEnd(DisplayUtil.IntToDp(28));
            binding.textOnboarding.setLayoutParams(textLayoutParams);
        }

        if (Objects.equals(title, requireActivity().getResources().getString(R.string.onboarding_title_1))) {
            binding.imageOnboarding.setScaleX(1.06f);
            binding.imageOnboarding.setScaleY(1.06f);
        } else if (Objects.equals(title, requireActivity().getResources().getString(R.string.onboarding_title_2))) {
            binding.imageOnboarding.setScaleX(0.88f);
            binding.imageOnboarding.setScaleY(0.88f);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void animateUpdateTextView(int title, int desc) {
        if (binding.textOnboardingTitle.getText() == getResources().getString(title) && binding.textOnboardingDescription.getText() == getResources().getString(desc))
            return;

        AlphaAnimation anim = new AlphaAnimation(1.0f, 0.0f);
        anim.setDuration(400);
        anim.setRepeatCount(1);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                binding.textOnboardingTitle.setText(getResources().getString(title));
                binding.textOnboardingDescription.setText(getResources().getString(desc));
            }
        });

        binding.textOnboardingTitle.startAnimation(anim);
        binding.textOnboardingDescription.startAnimation(anim);
    }

    public void updateTextView(int title, int desc) {
        if (binding.textOnboardingTitle.getText() == getResources().getString(title) && binding.textOnboardingDescription.getText() == getResources().getString(desc))
            return;

        binding.textOnboardingTitle.setText(getResources().getString(title));
        binding.textOnboardingDescription.setText(getResources().getString(desc));
    }
}
