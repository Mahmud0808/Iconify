package com.drdisagree.iconify.core.ui.components.preferences

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.preferences.PrefValue
import com.drdisagree.iconify.core.preferences.PreferenceController
import com.drdisagree.iconify.core.preferences.PreferenceDefinition
import com.drdisagree.iconify.core.ui.components.others.withHaptic
import com.drdisagree.iconify.helpers.fromHex
import com.drdisagree.iconify.helpers.fromHexSafe
import com.github.skydoves.colorpicker.compose.AlphaSlider
import com.github.skydoves.colorpicker.compose.AlphaTile
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.materialkolor.ktx.toHex

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ColorPickerPreferenceItem(
    def: PreferenceDefinition,
    prefController: PreferenceController,
    shape: RoundedCornerShape,
    isEnabled: Boolean,
    summary: String?,
    modifier: Modifier,
) {
    val colorPickerController = rememberColorPickerController()
    var showDialog by rememberSaveable { mutableStateOf(false) }
    val storedValue by prefController.observe(def.key, (def.defaultValue as PrefValue.StringValue).v)
    var dialogSessionKey by rememberSaveable { mutableIntStateOf(0) }

    PreferenceContainer(
        shape = shape,
        isEnabled = isEnabled,
        modifier = modifier,
        minLine = if (summary.isNullOrEmpty()) 1 else 2,
        onClick = withHaptic {
            if (isEnabled) {
                dialogSessionKey++
                showDialog = true
            }
        }
    ) {
        LeadingIcon(def.icon, isEnabled)
        TitleSummaryBlock(def.title, summary, isEnabled)
        AlphaTile(
            modifier = Modifier
                .size(40.dp)
                .clip(MaterialShapes.Cookie9Sided.toShape())
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outline,
                    MaterialShapes.Cookie9Sided.toShape()
                ),
            selectedColor = Color.fromHex(storedValue)
        )
    }

    if (showDialog) {
        var draft by rememberSaveable(dialogSessionKey) { mutableStateOf(storedValue) }
        var hexInput by rememberSaveable(dialogSessionKey) {
            mutableStateOf(storedValue.removePrefix("#"))
        }

        LaunchedEffect(dialogSessionKey, draft) {
            colorPickerController.selectByColor(
                color = Color.fromHexSafe(draft) ?: Color.fromHex(storedValue),
                fromUser = false
            )
        }

        Dialog(onDismissRequest = { showDialog = false }) {
            Surface(
                shape = MaterialTheme.shapes.large,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Pick a color",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(
                            8.dp,
                            Alignment.CenterHorizontally
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AlphaTile(
                            modifier = Modifier
                                .width(48.dp)
                                .height(32.dp)
                                .clip(MaterialTheme.shapes.small)
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline,
                                    MaterialTheme.shapes.small
                                ),
                            selectedColor = Color.fromHex(storedValue)
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                        AlphaTile(
                            modifier = Modifier
                                .width(48.dp)
                                .height(32.dp)
                                .clip(MaterialTheme.shapes.small)
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline,
                                    MaterialTheme.shapes.small
                                ),
                            selectedColor = colorPickerController.selectedColor.value
                        )
                    }

                    HsvColorPicker(
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .size(220.dp),
                        controller = colorPickerController,
                        initialColor = Color.fromHexSafe(draft) ?: Color.fromHex(storedValue),
                        onColorChanged = { colorEnvelope: ColorEnvelope ->
                            draft = colorEnvelope.color.toHex()
                            hexInput = draft.removePrefix("#")
                        }
                    )
                    AlphaSlider(
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .height(28.dp)
                            .widthIn(max = 280.dp)
                            .fillMaxWidth(),
                        controller = colorPickerController,
                    )
                    BrightnessSlider(
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .height(28.dp)
                            .widthIn(max = 280.dp)
                            .fillMaxWidth(),
                        controller = colorPickerController,
                    )

                    val isValidHex = hexInput.length == 6 || hexInput.length == 8

                    OutlinedTextField(
                        value = hexInput,
                        onValueChange = { raw ->
                            val sanitized = raw
                                .removePrefix("#")
                                .filter { it.isDigit() || it.uppercaseChar() in 'A'..'F' }
                                .take(8)
                                .uppercase()
                            hexInput = sanitized

                            if (sanitized.length == 6 || sanitized.length == 8) {
                                draft = "#$sanitized"
                                colorPickerController.selectByColor(
                                    color = Color.fromHex(draft),
                                    fromUser = true
                                )
                            }
                        },
                        label = { Text("Hex color") },
                        prefix = { Text("#") },
                        isError = hexInput.isNotEmpty() && !isValidHex,
                        supportingText = if (hexInput.isNotEmpty() && !isValidHex) {
                            { Text("Enter 6 (RRGGBB) or 8 (AARRGGBB) hex digits") }
                        } else null,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            keyboardType = KeyboardType.Ascii
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                    ) {
                        TextButton(onClick = withHaptic { showDialog = false }) {
                            Text("Cancel")
                        }
                        TextButton(
                            onClick = withHaptic {
                                val toSave = when {
                                    isValidHex -> "#$hexInput"
                                    draft != storedValue -> draft
                                    else -> storedValue
                                }
                                prefController.setString(def.key, toSave)
                                showDialog = false
                            }
                        ) {
                            Text(stringResource(R.string.btn_select))
                        }
                    }
                }
            }
        }
    }
}