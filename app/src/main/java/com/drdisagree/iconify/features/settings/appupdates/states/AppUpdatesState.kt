package com.drdisagree.iconify.features.settings.appupdates.states

import com.drdisagree.iconify.features.changelog.models.ChangelogData

sealed class AppUpdatesState {
    data object Idle : AppUpdatesState()
    data object Loading : AppUpdatesState()
    data class Downloading(val progress: Float) : AppUpdatesState()
    data object Installing : AppUpdatesState()
    data class UpdateAvailable(
        val versionName: String,
        val changelog: ChangelogData,
        val downloadUrl: String
    ) : AppUpdatesState()
    data class UpToDate(
        val currentVersion: String,
        val latestVersion: String
    ) : AppUpdatesState()
    data class Error(
        val currentVersion: String
    ) : AppUpdatesState()
}