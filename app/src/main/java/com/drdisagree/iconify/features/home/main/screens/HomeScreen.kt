package com.drdisagree.iconify.features.home.main.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.preferences.PreferenceScreen
import com.drdisagree.iconify.core.preferences.preferenceScreen
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable
import com.drdisagree.iconify.core.ui.utils.sharedHiltViewModel
import com.drdisagree.iconify.features.common.viewmodels.BottomNavViewModel
import com.drdisagree.iconify.features.home.main.components.HomeBannerCard
import com.drdisagree.iconify.features.home.main.components.HomeCategories
import kotlinx.coroutines.delay

val homePreferences = preferenceScreen {
    composable(key = "header_image") {
        HomeBannerCard()
    }

    composable(key = "home_cards") {
        HomeCategories(modifier = Modifier.padding(top = 16.dp))
    }
}

@Composable
fun HomeScreen(bottomNavViewModel: BottomNavViewModel = sharedHiltViewModel()) {
    LaunchedEffect(Unit) {
        delay(300)
        bottomNavViewModel.showBottomBar(true)
    }

    HomeScreenContent()
}

@Composable
private fun HomeScreenContent() {
    PreferenceScreen(
        items = homePreferences,
        title = stringResource(R.string.app_name),
        subtitle = stringResource(R.string.app_motto),
        showActionIcon = false
    )
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    PreviewComposable {
        HomeScreenContent()
    }
}