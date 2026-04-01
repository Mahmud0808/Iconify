package com.drdisagree.iconify.core.ui.components.others

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.drdisagree.iconify.core.common.LocalDarkMode
import com.drdisagree.iconify.core.common.LocalHazeState
import com.drdisagree.iconify.core.common.LocalInnerPadding
import com.drdisagree.iconify.core.common.LocalLayerBackdrop
import com.drdisagree.iconify.core.common.LocalNavController
import com.drdisagree.iconify.core.common.LocalSettings
import com.drdisagree.iconify.core.common.defaultSettings
import com.drdisagree.iconify.core.preferences.PrefValue
import com.drdisagree.iconify.data.storage.ExternalChange
import com.drdisagree.iconify.data.storage.PreferenceStorage
import com.drdisagree.iconify.services.providers.ProvideSharedPreferencesController
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun PreviewComposable(content: @Composable () -> Unit) {
    val hazeState = remember { HazeState() }
    val backdropColor = MaterialTheme.colorScheme.surface
    val backdrop = rememberLayerBackdrop {
        drawRect(backdropColor)
        drawContent()
    }
    val navController = rememberNavController()
    val isDarkTheme = false
    val innerPadding = PaddingValues(0.dp)

    ProvideSharedPreferencesController {
        CompositionLocalProvider(
            LocalHazeState provides hazeState,
            LocalLayerBackdrop provides backdrop,
            LocalNavController provides navController,
            LocalDarkMode provides isDarkTheme,
            LocalInnerPadding provides innerPadding,
            LocalSettings provides defaultSettings
        ) {
            content()
        }
    }
}

class FakeSharedPrefsStorage : PreferenceStorage {

    private val data = mutableMapOf<String, PrefValue>()

    override val externalChanges: Flow<ExternalChange> = emptyFlow()

    override fun loadAll(): Map<String, PrefValue> = data.toMap()

    override fun read(key: String, defaultValue: PrefValue): Any = data[key] ?: defaultValue

    override fun write(key: String, value: PrefValue) {
        data[key] = value
    }

    override fun clearAll() {
        data.clear()
    }

    override fun dispose() {
        data.clear()
    }
}