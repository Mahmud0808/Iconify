package com.drdisagree.iconify.features.playground.screens

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.preferences.PreferenceScreen
import com.drdisagree.iconify.core.preferences.preferenceScreen
import com.drdisagree.iconify.core.preferences.stringRes
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable
import com.drdisagree.iconify.core.ui.components.others.withHaptic

fun playgroundPreferences(
    onSendNotification: () -> Unit = {},
) = preferenceScreen {
    category {
        action(
            key = "notificationSender",
            title = stringRes("Notification Sender"),
            summary = { stringRes("Send a custom notification") },
            onClick = { onSendNotification() }
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PlaygroundScreen() {
    val context = LocalContext.current
    val activity = LocalActivity.current
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var notificationTitle by rememberSaveable { mutableStateOf("") }
    var notificationBody by rememberSaveable { mutableStateOf("") }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showDialog = true
        } else {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(
                    activity!!,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            ) {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }
        }
    }

    fun checkAndShowNotificationDialog() {
        val permission = Manifest.permission.POST_NOTIFICATIONS

        when {
            context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED -> {
                showDialog = true
            }

            else -> {
                permissionLauncher.launch(permission)
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = "Send Notification") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = notificationTitle,
                        onValueChange = { notificationTitle = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = notificationBody,
                        onValueChange = { notificationBody = it },
                        label = { Text("Body") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    shapes = ButtonDefaults.shapes(),
                    onClick = withHaptic {
                        showDialog = false
                        notificationTitle = ""
                        notificationBody = ""
                    }
                ) { Text(stringResource(android.R.string.cancel)) }
            },
            confirmButton = {
                Button(
                    shapes = ButtonDefaults.shapes(),
                    onClick = withHaptic {
                        sendNotification(context, notificationTitle, notificationBody)
                        showDialog = false
                        notificationTitle = ""
                        notificationBody = ""
                    }
                ) { Text("Send") }
            }
        )
    }

    PreferenceScreen(
        items = playgroundPreferences(onSendNotification = { checkAndShowNotificationDialog() }),
        title = "Playground",
        showBackIcon = true,
        showActionIcon = true
    )
}

private fun sendNotification(context: Context, title: String, body: String) {
    val channelId = "playground_notifications"
    val channelName = "Playground Notifications"

    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val channel =
        NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
    notificationManager.createNotificationChannel(channel)

    val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.ic_statusbar_logo_android)
        .setContentTitle(title)
        .setContentText(body)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)

    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        NotificationManagerCompat
            .from(context)
            .notify(
                System.currentTimeMillis().toInt(),
                builder.build()
            )
    }
}

@Preview(showBackground = true)
@Composable
private fun PlaygroundScreenPreview() {
    PreviewComposable {
        PlaygroundScreen()
    }
}