package com.drdisagree.iconify.features.home.notification.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.ui.components.dialogs.LoadingDialog
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable
import com.drdisagree.iconify.core.ui.components.others.ToastAppliedEvent
import com.drdisagree.iconify.core.ui.components.others.innerPaddingValues
import com.drdisagree.iconify.core.ui.components.scaffolds.AppScaffold
import com.drdisagree.iconify.data.models.NotificationPreview
import com.drdisagree.iconify.features.common.models.UiText
import com.drdisagree.iconify.features.home.notification.components.NotificationCard
import com.drdisagree.iconify.features.home.notification.viewmodels.NotificationViewModel

private val notificationList = listOf(
    Pair(
        "Default",
        R.drawable.preview_notification_default
    ),
    Pair(
        "Layers",
        R.drawable.preview_notification_layers
    ),
    Pair(
        "Thin Outline",
        R.drawable.preview_notification_thin_outline
    ),
    Pair(
        "Bottom Outline",
        R.drawable.preview_notification_bottom_outline
    ),
    Pair(
        "Neumorph",
        R.drawable.preview_notification_neumorph
    ),
    Pair(
        "Stack",
        R.drawable.preview_notification_stack
    ),
    Pair(
        "Side Stack",
        R.drawable.preview_notification_side_stack
    ),
    Pair(
        "Outline",
        R.drawable.preview_notification_outline
    ),
    Pair(
        "Leafy Outline",
        R.drawable.preview_notification_leafy_outline
    ),
    Pair(
        "Lighty",
        R.drawable.preview_notification_lighty
    ),
    Pair(
        "Neumorph Outline",
        R.drawable.preview_notification_neumorph_outline
    ),
    Pair(
        "Cyberponk",
        R.drawable.preview_notification_cyberponk
    ),
    Pair(
        "Cyberponk v2",
        R.drawable.preview_notification_cyberponk_v2
    ),
    Pair(
        "Thread Line",
        R.drawable.preview_notification_thread_line
    ),
    Pair(
        "Faded",
        R.drawable.preview_notification_faded
    ),
    Pair(
        "Dumbbell",
        R.drawable.preview_notification_dumbbell
    ),
    Pair(
        "Semi Transparent",
        R.drawable.preview_notification_semi_transparent
    ),
    Pair(
        "Pitch Black",
        R.drawable.preview_notification_pitch_black
    ),
    Pair(
        "Duoline",
        R.drawable.preview_notification_duoline
    ),
    Pair(
        "iOS",
        R.drawable.preview_notification_ios
    ),
).mapIndexed { index, (name, style) ->
    NotificationPreview(
        id = (index + 1).toString(),
        title = UiText.Text(name),
        notificationStyle = style
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(notificationViewModel: NotificationViewModel = hiltViewModel()) {
    val scrollState = rememberScrollState()
    val notifications by rememberSaveable { mutableStateOf(notificationList) }
    val notificationStyle by notificationViewModel.notificationStyle.collectAsStateWithLifecycle()
    var expandedPackId by rememberSaveable { mutableStateOf<String?>(null) }
    val isApplying by notificationViewModel.isLoading.collectAsStateWithLifecycle()

    LaunchedEffect(notifications) {
        notificationViewModel.refreshState()
    }

    if (isApplying) {
        LoadingDialog()
    }

    ToastAppliedEvent(notificationViewModel.uiEvent)

    AppScaffold(
        title = stringResource(R.string.activity_title_notification),
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
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                notifications.forEach { notification ->
                    val isApplied = notificationStyle == notification.id

                    NotificationCard(
                        notification = notification.copy(isApplied = isApplied),
                        isSelected = expandedPackId == notification.id,
                        onClick = {
                            expandedPackId =
                                if (expandedPackId == notification.id) null else notification.id
                        },
                        onActionClick = {
                            notificationViewModel.togglePack(notification.id)
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Phone", device = Devices.PHONE)
@Composable
private fun NotificationScreenPhonePreview() {
    PreviewComposable {
        NotificationScreen()
    }
}

@Preview(showBackground = true, name = "Tablet", device = Devices.TABLET)
@Composable
private fun NotificationScreenTabletPreview() {
    PreviewComposable {
        NotificationScreen()
    }
}