package com.drdisagree.iconify.core.ui.components.preferences

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.FontDownload
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.VideoFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.drdisagree.iconify.core.preferences.PrefStringRes
import com.drdisagree.iconify.core.preferences.PreferenceController
import com.drdisagree.iconify.core.preferences.PreferenceDefinition
import com.drdisagree.iconify.core.preferences.PreferenceType
import com.drdisagree.iconify.core.preferences.resolve
import com.drdisagree.iconify.core.preferences.stringRes
import com.drdisagree.iconify.core.ui.components.others.withHaptic

sealed class FilePickerType {

    abstract val mimeTypes: List<String>
    abstract val label: PrefStringRes

    object Image : FilePickerType() {
        override val mimeTypes = listOf("image/*")
        override val label = stringRes("Pick image")
    }

    object Video : FilePickerType() {
        override val mimeTypes = listOf("video/*")
        override val label = stringRes("Pick video")
    }

    object Audio : FilePickerType() {
        override val mimeTypes = listOf("audio/*")
        override val label = stringRes("Pick audio")
    }

    object Pdf : FilePickerType() {
        override val mimeTypes = listOf("application/pdf")
        override val label = stringRes("Pick PDF")
    }

    object Document : FilePickerType() {
        override val mimeTypes = listOf(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain",
        )
        override val label = stringRes("Pick document")
    }

    object Font : FilePickerType() {
        override val mimeTypes = listOf(
            "font/ttf",
            "font/otf",
            "font/woff",
            "font/woff2",
            "application/x-font-ttf",
            "application/x-font-opentype",
            "application/font-woff",
            "application/vnd.ms-fontobject"
        )
        override val label = stringRes("Pick font")
    }

    object Any : FilePickerType() {
        override val mimeTypes = listOf("*/*")
        override val label = stringRes("Pick file")
    }

    data class Custom(
        override val mimeTypes: List<String>,
        override val label: PrefStringRes = stringRes("Pick file"),
    ) : FilePickerType()
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FilePickerPreferenceItem(
    def: PreferenceDefinition,
    prefController: PreferenceController,
    shape: RoundedCornerShape,
    isEnabled: Boolean,
    summary: String?,
    type: PreferenceType.FilePicker,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val uriString by prefController.observe(def.key, "")
    val uri: Uri? = remember(uriString) { uriString.takeIf { it.isNotEmpty() }?.let(Uri::parse) }
    val fileName: String? = remember(uri) { uri?.let { resolveFileName(context, it) } }

    val isImageType = type.pickerType is FilePickerType.Image
    val isImage: Boolean = remember(uri) {
        uri?.let { context.contentResolver.getType(it)?.startsWith("image/") } ?: false
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { pickedUri ->
        if (pickedUri != null && isEnabled) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    pickedUri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
            val uriStr = pickedUri.toString()
            if (type.saveFileUri) prefController.setString(def.key, uriStr)
            type.onFileSelected(prefController, uriStr)
        }
    }

    val documentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { pickedUri ->
        if (pickedUri != null && isEnabled) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    pickedUri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
            val uriStr = pickedUri.toString()
            if (type.saveFileUri) prefController.setString(def.key, uriStr)
            type.onFileSelected(prefController, uriStr)
        }
    }

    val onPickClick = withHaptic {
        if (isEnabled) {
            if (isImageType) {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            } else {
                documentLauncher.launch(type.pickerType.mimeTypes.toTypedArray())
            }
        }
    }

    PreferenceContainer(
        shape = shape,
        isEnabled = isEnabled,
        modifier = modifier,
        minLine = if (summary.isNullOrEmpty()) 2 else 3,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                LeadingIcon(def.icon, isEnabled)
                TitleSummaryBlock(def.title, summary, isEnabled)
            }

            Spacer(Modifier.height(10.dp))

            //            if (isImage && uri != null) {
            //                Box(
            //                    modifier = Modifier
            //                        .fillMaxWidth()
            //                        .height(160.dp)
            //                        .clip(RoundedCornerShape(8.dp))
            //                        .border(
            //                            1.dp,
            //                            MaterialTheme.colorScheme.outlineVariant,
            //                            RoundedCornerShape(8.dp),
            //                        ),
            //                ) {
            //                    AsyncImage(
            //                        model = uri,
            //                        contentDescription = "Preview",
            //                        contentScale = ContentScale.Crop,
            //                        modifier = Modifier.fillMaxSize(),
            //                    )
            //                }
            //                Spacer(Modifier.height(8.dp))
            //            }

            if (uri != null && type.saveFileUri) {
                FilenameChip(
                    name = fileName ?: "Unknown file",
                    type = type.pickerType,
                    isEnabled = isEnabled,
                    onClear = withHaptic {
                        if (type.saveFileUri) {
                            prefController.setString(def.key, "")
                        }
                        type.onFileSelected(prefController, "")
                    },
                )
                Spacer(Modifier.height(8.dp))
            }

            Button(
                onClick = onPickClick,
                enabled = isEnabled,
                shapes = ButtonDefaults.shapes(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(text = if (uri != null) "Replace" else type.pickerType.label.resolve())
            }
        }
    }
}

private fun resolveFileName(context: Context, uri: Uri): String? {
    runCatching {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val col = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (col != -1 && cursor.moveToFirst()) return cursor.getString(col)
        }
    }
    return uri.lastPathSegment
}

@Composable
private fun FileTypeIcon(type: FilePickerType, contentColor: Color) {
    val icon = when (type) {
        FilePickerType.Image -> Icons.Rounded.Image
        FilePickerType.Video -> Icons.Rounded.VideoFile
        FilePickerType.Audio -> Icons.Rounded.AudioFile
        FilePickerType.Pdf -> Icons.Rounded.PictureAsPdf
        FilePickerType.Document -> Icons.Rounded.Description
        FilePickerType.Font -> Icons.Rounded.FontDownload
        else -> Icons.Rounded.AttachFile
    }

    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = contentColor,
        modifier = Modifier.size(16.dp),
    )
}

@Composable
private fun FilenameChip(
    name: String,
    type: FilePickerType,
    isEnabled: Boolean,
    onClear: () -> Unit
) {
    val containerColor = MaterialTheme.colorScheme.secondaryContainer
    val contentColor = MaterialTheme.colorScheme.onSecondaryContainer

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .wrapContentSize()
            .clip(RoundedCornerShape(50))
            .background(containerColor)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        FileTypeIcon(type, contentColor)
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        IconButton(
            onClick = onClear,
            modifier = Modifier
                .padding(start = 4.dp)
                .size(20.dp),
            enabled = isEnabled
        ) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = "Clear",
                tint = contentColor,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}