package com.drdisagree.iconify.features.changelog.models

import androidx.annotation.StringRes

data class ChangelogData(
    @param:StringRes val titleRes: Int,
    val titleArg: String? = null,
    val contents: List<String>
)