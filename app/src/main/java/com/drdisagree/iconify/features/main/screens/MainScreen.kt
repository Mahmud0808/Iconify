package com.drdisagree.iconify.features.main.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.drdisagree.iconify.app.navigation.NavRoutes
import com.drdisagree.iconify.core.common.LocalNavController
import com.drdisagree.iconify.core.common.LocalSettings

@Composable
fun MainScreen() {
    val settings = LocalSettings.current
    val navController = LocalNavController.current

    LaunchedEffect(settings.isXposedOnlyMode) {
        val destination = if (settings.isXposedOnlyMode) {
            NavRoutes.MainGraph.Xposed.Root
        } else {
            NavRoutes.MainGraph.Home.Root
        }

        navController.navigate(destination) {
            popUpTo(NavRoutes.MainGraph.Root) { inclusive = true }
            launchSingleTop = true
        }
    }
}