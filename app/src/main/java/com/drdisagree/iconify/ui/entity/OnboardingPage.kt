package com.drdisagree.iconify.ui.entity

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.drdisagree.iconify.R

enum class OnboardingPage(
    @field:StringRes val titleResource: Int,
    @field:StringRes val subTitleResource: Int,
    @field:StringRes val descriptionResource: Int,
    @field:DrawableRes val logoResource: Int
) {
    ONE(
        R.string.onboarding_title_1,
        R.string.onboarding_subtitle_1,
        R.string.onboarding_desc_1,
        R.drawable.onboarding_img_1
    ),
    TWO(
        R.string.onboarding_title_2,
        R.string.onboarding_subtitle_2,
        R.string.onboarding_desc_2,
        R.drawable.onboarding_img_2
    ),
    THREE(
        R.string.onboarding_title_3,
        R.string.onboarding_subtitle_3,
        R.string.onboarding_desc_3,
        R.drawable.onboarding_img_3
    )
}
