package com.drdisagree.iconify.features.xposed.lockscreen.common.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable
import com.drdisagree.iconify.core.ui.utils.navigationBarHeight
import com.drdisagree.iconify.features.xposed.lockscreen.common.models.WeatherIconPackItem
import com.drdisagree.iconify.features.xposed.lockscreen.common.viewmodels.WeatherViewModel

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
fun WeatherIconPackBottomSheet(
    iconPacks: List<WeatherIconPackItem>,
    selectedIconPackIndex: Int,
    weatherViewModel: WeatherViewModel?,
    onDismiss: () -> Unit
) {
    val navbarHeight = navigationBarHeight()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        contentWindowInsets = { WindowInsets.safeDrawing.only(WindowInsetsSides.Top) },
    ) {
        Text(
            text = stringResource(R.string.weather_icon_pack_title),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp)
                .align(Alignment.CenterHorizontally)
        )

        HorizontalDivider()

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 16.dp + navbarHeight
            ),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            itemsIndexed(iconPacks) { index, pack ->
                val isSelected = index == selectedIconPackIndex

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { weatherViewModel?.onIconPackSelected(index) },
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = if (isSelected) {
                        BorderStroke(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        null
                    }
                ) {
                    ListItem(
                        headlineContent = { Text(pack.label) },
                        leadingContent = {
                            pack.drawable?.let { drawable ->
                                Image(
                                    bitmap = drawable
                                        .toBitmap(48, 48)
                                        .asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        },
                        trailingContent = {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = Color.Transparent
                        )
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun WeatherIconPackBottomSheetPreview() {
    PreviewComposable {
        WeatherIconPackBottomSheet(
            iconPacks = emptyList(),
            selectedIconPackIndex = 0,
            weatherViewModel = null,
            onDismiss = {}
        )
    }
}