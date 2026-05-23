package com.drdisagree.iconify.features.changelog.states

import com.drdisagree.iconify.features.changelog.models.ChangelogData

sealed class ChangelogState {
    object Loading : ChangelogState()
    object Error : ChangelogState()
    data class Success(val data: ChangelogData) : ChangelogState()
}