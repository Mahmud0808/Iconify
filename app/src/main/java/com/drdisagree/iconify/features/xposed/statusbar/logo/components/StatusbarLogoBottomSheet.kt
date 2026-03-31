package com.drdisagree.iconify.features.xposed.statusbar.logo.components

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
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.drdisagree.iconify.features.xposed.statusbar.logo.models.StatusbarLogoItem

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
fun StatusbarLogoBottomSheet(
    sheetState: SheetState,
    iconPacks: List<StatusbarLogoItem>,
    selectedItemIndex: Int,
    onItemClick: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val navbarHeight = navigationBarHeight()

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss,
        contentWindowInsets = { WindowInsets.safeDrawing.only(WindowInsetsSides.Top) },
    ) {
        Text(
            text = stringResource(R.string.status_bar_logo_style_title),
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
                val isSelected = index == selectedItemIndex

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onItemClick(index) },
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

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun StatusbarLogoBottomSheetPreview() {
    PreviewComposable {
        StatusbarLogoBottomSheet(
            sheetState = rememberModalBottomSheetState(),
            iconPacks = emptyList(),
            selectedItemIndex = 0,
            onItemClick = {},
            onDismiss = {}
        )
    }
}