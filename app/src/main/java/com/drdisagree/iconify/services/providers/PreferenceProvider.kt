package com.drdisagree.iconify.services.providers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import com.drdisagree.iconify.core.common.LocalPreferenceController
import com.drdisagree.iconify.core.di.PreferenceEntryPoint
import com.drdisagree.iconify.core.ui.components.others.FakeSharedPrefsStorage
import com.drdisagree.iconify.core.preferences.PreferenceController
import dagger.hilt.android.EntryPointAccessors

@Composable
fun ProvideSharedPreferencesController(content: @Composable () -> Unit) {
    val previewMode = LocalInspectionMode.current

    val controller = if (previewMode) {
        remember { PreferenceController(FakeSharedPrefsStorage()) }
    } else {
        val context = LocalContext.current.applicationContext

        val preferenceStorage = remember {
            EntryPointAccessors.fromApplication(
                context,
                PreferenceEntryPoint::class.java
            ).sharedPrefsStorage()
        }

        remember(preferenceStorage) {
            PreferenceController(preferenceStorage)
        }
    }

    DisposableEffect(controller) {
        onDispose { controller.dispose() }
    }

    ProvidePreferenceController(controller, content)
}

@Composable
fun ProvideDataStoreController(content: @Composable () -> Unit) {
    val context = LocalContext.current

    val preferenceStorage = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            PreferenceEntryPoint::class.java
        ).dataStoreStorage()
    }

    val controller = remember(preferenceStorage) {
        PreferenceController(preferenceStorage)
    }

    DisposableEffect(controller) {
        onDispose { controller.dispose() }
    }

    ProvidePreferenceController(controller, content)
}

@Composable
private fun ProvidePreferenceController(
    controller: PreferenceController,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalPreferenceController provides controller) {
        content()
    }
}