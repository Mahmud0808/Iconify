package com.drdisagree.iconify.features.xposed.lockscreen.location.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.common.LocalNavController
import com.drdisagree.iconify.core.ui.components.extensions.secondaryText
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable
import com.drdisagree.iconify.core.ui.components.others.innerPaddingValues
import com.drdisagree.iconify.core.ui.components.scaffolds.AppScaffold
import com.drdisagree.iconify.features.xposed.lockscreen.common.viewmodels.WeatherViewModel
import com.drdisagree.iconify.features.xposed.lockscreen.location.models.LocationBrowseItem
import com.drdisagree.iconify.features.xposed.lockscreen.location.states.LocationBrowseUiState
import com.drdisagree.iconify.features.xposed.lockscreen.location.viewmodels.LocationBrowseViewModel

@Composable
fun LocationBrowseScreen(
    locationViewModel: LocationBrowseViewModel = hiltViewModel(),
    weatherViewModel: WeatherViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val navController = LocalNavController.current

    val uiState by locationViewModel.uiState.collectAsStateWithLifecycle()

    val locationSavedString = stringResource(R.string.toast_selected_successfully)

    LocationBrowseScreenContent(
        uiState = uiState,
        onQueryChange = locationViewModel::onQueryChange,
        onLocationClick = { item ->
            locationViewModel.saveLocation(item)
            weatherViewModel.onLocationNameReceived()
            navController.popBackStack()

            Toast.makeText(
                context,
                locationSavedString,
                Toast.LENGTH_SHORT
            ).show()
        }
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun LocationBrowseScreenContent(
    uiState: LocationBrowseUiState,
    onQueryChange: (String) -> Unit,
    onLocationClick: (LocationBrowseItem) -> Unit
) {
    val scrollState = rememberScrollState()

    AppScaffold(
        title = stringResource(R.string.custom_location_title),
        showBackIcon = true,
    ) { innerPadding, _ ->
        val padding = innerPaddingValues(
            innerPadding = innerPadding,
            start = 16.dp,
            end = 16.dp,
            top = 16.dp
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(padding)
                .padding(bottom = 16.dp)
        ) {
            OutlinedTextField(
                value = uiState.query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null
                    )
                },
                label = { Text(stringResource(R.string.location_query_hint)) }
            )

            if (uiState.isLoading) {
                LinearWavyProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
            }

            uiState.locations.forEachIndexed { index, item ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.small)
                        .clickable { onLocationClick(item) }
                        .padding(vertical = 8.dp, horizontal = 16.dp)
                ) {
                    Text(
                        text = item.city,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = item.cityExt,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.secondaryText()
                    )
                }

                if (index < uiState.locations.lastIndex) HorizontalDivider(
                    modifier = Modifier.padding(
                        vertical = 4.dp
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LocationScreenPreview() {
    val sampleLocations = listOf(
        LocationBrowseItem(
            cityExt = "NY, USA",
            countryId = "US",
            city = "New York",
            lat = 40.7128,
            lon = -74.0060
        ),
        LocationBrowseItem(
            cityExt = "London, UK",
            countryId = "GB",
            city = "London",
            lat = 51.5074,
            lon = -0.1278
        ),
        LocationBrowseItem(
            cityExt = "Tokyo, Japan",
            countryId = "JP",
            city = "Tokyo",
            lat = 35.6895,
            lon = 139.6917
        )
    )

    val mockState = LocationBrowseUiState(
        query = "Lo",
        isLoading = false,
        locations = sampleLocations
    )

    PreviewComposable {
        LocationBrowseScreenContent(
            uiState = mockState,
            onQueryChange = {},
            onLocationClick = {}
        )
    }
}