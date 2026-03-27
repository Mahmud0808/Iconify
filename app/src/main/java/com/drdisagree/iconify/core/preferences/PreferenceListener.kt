package com.drdisagree.iconify.core.preferences

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import com.drdisagree.iconify.core.common.LocalPreferenceController
import com.drdisagree.iconify.data.keys.Key

/**
 * Registers a preference listener scoped to the current composition.
 * Automatically removed when this composable leaves the tree.
 *
 * @param key  Specific key to watch, or null to watch ALL keys.
 *
 * Usage:
 *   PreferenceListener("dark_mode") { event ->
 *       val isDark = (event.newValue as PrefValue.BoolValue).v
 *   }
 */
@Composable
fun PreferenceListener(
    key: String? = null,
    callback: (PreferenceChangeEvent) -> Unit,
) {
    val controller = LocalPreferenceController.current
    val latestCallback by rememberUpdatedState(callback)

    DisposableEffect(controller, key) {
        val handle = controller.addListener(key) { latestCallback(it) }

        onDispose { controller.removeListener(handle) }
    }
}

@Composable
fun PreferenceListener(
    key: Key,
    callback: (PreferenceChangeEvent) -> Unit,
) {
    val controller = LocalPreferenceController.current
    val latestCallback by rememberUpdatedState(callback)

    DisposableEffect(controller, key) {
        val handle = controller.addListener(key) { latestCallback(it) }

        onDispose { controller.removeListener(handle) }
    }
}