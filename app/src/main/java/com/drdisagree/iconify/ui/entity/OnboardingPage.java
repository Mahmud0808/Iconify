package com.drdisagree.iconify.ui.entity;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.drdisagree.iconify.R;

@SuppressWarnings("unused")
public enum OnboardingPage {

    ONE(R.string.onboarding_title_1, R.string.onboarding_subtitle_1, R.string.onboarding_desc_1, R.drawable.onboarding_img_1),
    TWO(R.string.onboarding_title_2, R.string.onboarding_subtitle_2, R.string.onboarding_desc_2, R.drawable.onboarding_img_2),
    THREE(R.string.onboarding_title_3, R.string.onboarding_subtitle_3, R.string.onboarding_desc_3, R.drawable.onboarding_img_3);

    @StringRes
    private final int titleResource;

    @StringRes
    private final int subTitleResource;

    @StringRes
    private final int descriptionResource;

    @DrawableRes
    private final int logoResource;

    OnboardingPage(int titleResource, int subTitleResource, int descriptionResource, int logoResource) {
        this.titleResource = titleResource;
        this.subTitleResource = subTitleResource;
        this.descriptionResource = descriptionResource;
        this.logoResource = logoResource;
    }

    public int getTitleResource() {
        return titleResource;
    }

    public int getSubTitleResource() {
        return subTitleResource;
    }

    public int getDescriptionResource() {
        return descriptionResource;
    }

    public int getLogoResource() {
        return logoResource;
    }
}
