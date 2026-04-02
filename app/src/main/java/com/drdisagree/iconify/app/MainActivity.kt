package com.drdisagree.iconify.app

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnAttachStateChangeListener
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toDrawable
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.app.navigation.NavGraph
import com.drdisagree.iconify.core.common.LocalPreferenceController
import com.drdisagree.iconify.core.common.LocalSettings
import com.drdisagree.iconify.core.preferences.PreferenceScreenItem
import com.drdisagree.iconify.core.ui.components.others.BLUR_RADIUS
import com.drdisagree.iconify.core.ui.theme.MyAppTheme
import com.drdisagree.iconify.data.common.References.PREFERENCE_LIST
import com.drdisagree.iconify.data.config.Config
import com.drdisagree.iconify.data.states.AppState
import com.drdisagree.iconify.services.providers.AppProviders
import com.drdisagree.iconify.xposed.modules.extras.utils.BitmapSubjectSegmenter
import com.topjohnwu.superuser.Shell
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.util.function.Consumer
import kotlin.coroutines.resume

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var isInitializing = true

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashscreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashscreen.setKeepOnScreenCondition { isInitializing }
        enableEdgeToEdge()
        setupWindowBlurListener()

        Shell.enableVerboseLogging = BuildConfig.DEBUG

        if (Shell.getCachedShell() == null) {
            Shell.setDefaultBuilder(
                Shell.Builder
                    .create()
                    .setFlags(Shell.FLAG_MOUNT_MASTER)
                    .setTimeout(10)
            )
        }

        setContent {
            AppProviders(this) {
                val context = LocalContext.current
                val settingsLoaded = LocalSettings.current.isLoaded
                var appLoaded by rememberSaveable { mutableStateOf(false) }
                val segmenter = remember { BitmapSubjectSegmenter(context) }

                LaunchedEffect(settingsLoaded, appLoaded) {
                    if (settingsLoaded && appLoaded) {
                        segmenter.checkModelAvailability { response ->
                            Log.d(
                                "MLKit",
                                "Model availability: ${response.areModulesAvailable()}"
                            )
                        }
                        delay(500)
                        isInitializing = false
                    }
                }

                MyAppTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        MyApp { appLoaded = true }
                    }
                }
            }
        }
    }

    private fun setupWindowBlurListener() {
        window.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

        val windowBlurEnabledListener: Consumer<Boolean?> = Consumer { blursEnabled: Boolean ->
            window.setBackgroundBlurRadius(if (blursEnabled) BLUR_RADIUS else 0)
        }

        window.decorView.addOnAttachStateChangeListener(
            object : OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {
                    windowManager.addCrossWindowBlurEnabledListener(windowBlurEnabledListener)
                }

                override fun onViewDetachedFromWindow(v: View) {
                    windowManager.removeCrossWindowBlurEnabledListener(windowBlurEnabledListener)
                }
            }
        )
    }

    @Composable
    fun MyApp(onLoaded: () -> Unit = {}) {
        var appState by rememberSaveable(
            stateSaver = Saver(
                save = { state ->
                    when (state) {
                        is AppState.Loading -> null
                        is AppState.Ready -> state.skipOnboarding
                    }
                },
                restore = { saved: Boolean -> AppState.Ready(saved) }
            )
        ) { mutableStateOf(AppState.Loading) }

        when (val state = appState) {
            is AppState.Loading -> {
                InitPreferences { state ->
                    appState = state
                }
            }

            is AppState.Ready -> {
                NavGraph(skipOnboarding = state.skipOnboarding)
                LaunchedEffect(Unit) {
                    onLoaded()
                }
            }
        }
    }

    @Composable
    private fun InitPreferences(onLoaded: (AppState.Ready) -> Unit) {
        val prefController = LocalPreferenceController.current

        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                val defaults = PREFERENCE_LIST
                    .filterIsInstance<PreferenceScreenItem.Category>()
                    .flatMap { it.definition.preferences }
                    .associate { pref -> pref.key to pref.defaultValue }
                prefController.initAll(defaults)
            }

            val shellReady = withContext(Dispatchers.IO) {
                try {
                    withTimeout(15_000L) {
                        suspendCancellableCoroutine { cont ->
                            Shell.getShell { shell ->
                                if (cont.isActive) cont.resume(shell.isRoot)
                            }
                        }
                    }
                } catch (_: TimeoutCancellationException) {
                    Log.w(TAG, "Shell timed out — continuing without root")
                    false
                } catch (e: Exception) {
                    Log.e(TAG, "Shell error", e)
                    false
                }
            }

            val skipOnboarding = withContext(Dispatchers.IO) {
                if (!shellReady) {
                    false
                } else {
                    try {
                        Config.shouldSkipOnboarding()
                    } catch (e: Exception) {
                        Log.e(TAG, "shouldSkipOnboarding error", e)
                        false
                    }
                }
            }

            onLoaded(AppState.Ready(skipOnboarding))
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}