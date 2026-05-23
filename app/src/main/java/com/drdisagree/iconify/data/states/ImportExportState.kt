package com.drdisagree.iconify.data.states

import android.net.Uri
import androidx.annotation.StringRes

sealed interface ImportExportState {
    data object Idle : ImportExportState
    data object Loading : ImportExportState

    /**
     * Emitted after the user picks a backup file — the composable should show a confirmation
     * dialog and then call [com.drdisagree.iconify.features.common.viewmodels.ImportExportViewModel.confirmImport] with this URI.
     */
    data class AwaitingConfirmation(val uri: Uri) : ImportExportState

    data class Success(@param:StringRes val messageRes: Int) : ImportExportState
    data class Failure(@param:StringRes val messageRes: Int) : ImportExportState
}