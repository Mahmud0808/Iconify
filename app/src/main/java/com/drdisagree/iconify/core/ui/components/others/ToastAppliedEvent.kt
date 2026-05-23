package com.drdisagree.iconify.core.ui.components.others

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.drdisagree.iconify.R
import com.drdisagree.iconify.data.events.ToastUiEvent
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun ToastAppliedEvent(uiEvent: SharedFlow<ToastUiEvent>) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        uiEvent.collect { event ->
            when (event) {
                ToastUiEvent.Applied -> Toast.makeText(
                    context,
                    R.string.toast_applied,
                    Toast.LENGTH_SHORT
                ).show()

                ToastUiEvent.Disabled -> Toast.makeText(
                    context,
                    R.string.toast_disabled,
                    Toast.LENGTH_SHORT
                ).show()

                ToastUiEvent.Error -> Toast.makeText(
                    context,
                    R.string.toast_error,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}