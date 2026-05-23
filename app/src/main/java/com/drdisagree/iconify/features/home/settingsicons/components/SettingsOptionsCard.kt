package com.drdisagree.iconify.features.home.settingsicons.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drdisagree.iconify.core.ui.components.preferences.PreferenceContainer
import com.drdisagree.iconify.core.ui.utils.ItemPosition
import com.drdisagree.iconify.core.ui.utils.cardCorners

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsOptionsCard(
    modifier: Modifier = Modifier,
    itemPosition: ItemPosition = ItemPosition.SOLO,
    title: String,
    buttonLabels: List<String>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
) {
    val corners = cardCorners(itemPosition)

    PreferenceContainer(
        modifier = modifier,
        shape = RoundedCornerShape(
            topStart = corners.topStart,
            topEnd = corners.topEnd,
            bottomStart = corners.bottomStart,
            bottomEnd = corners.bottomEnd
        ),
        contentPadding = PaddingValues(0.dp),
        isEnabled = true,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 14.dp,
                    bottom = 4.dp
                )
            )
            Row(
                Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
            ) {
                buttonLabels.forEachIndexed { index, label ->
                    ToggleButton(
                        checked = selectedIndex == index,
                        onCheckedChange = { onItemSelected(index) },
                        modifier = Modifier.semantics { role = Role.RadioButton },
                        shapes = when (index) {
                            0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                            buttonLabels.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                            else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                        },
                    ) {
                        Text(text = label)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun SettingsOptionsCardPreview() {
    SettingsOptionsCard(
        title = "Icon Shape",
        buttonLabels = listOf("Squircle", "Circle", "Teardrop"),
        selectedIndex = 0,
        onItemSelected = {}
    )
}