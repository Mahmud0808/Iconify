package com.drdisagree.iconify.core.ui.utils

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel

/**
 * Returns an existing Hilt ViewModel or creates a new one scoped to the Activity.
 * This ensures the same instance is shared across all composables in the app.
 */
@Composable
inline fun <reified T : ViewModel> sharedHiltViewModel(): T {
    val activity = LocalActivity.current as? ComponentActivity
        ?: throw IllegalStateException("LocalActivity is not a ComponentActivity or is null.")

    return hiltViewModel(activity)
}