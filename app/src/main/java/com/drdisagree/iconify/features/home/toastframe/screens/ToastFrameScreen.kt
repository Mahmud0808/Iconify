package com.drdisagree.iconify.features.home.toastframe.screens

import android.widget.Toast
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.ui.components.dialogs.LoadingDialog
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable
import com.drdisagree.iconify.core.ui.components.others.innerPaddingValues
import com.drdisagree.iconify.core.ui.components.scaffolds.AppScaffold
import com.drdisagree.iconify.data.events.ToastUiEvent
import com.drdisagree.iconify.data.models.ToastFramePreview
import com.drdisagree.iconify.data.states.UiText
import com.drdisagree.iconify.features.home.toastframe.components.ToastFrameCard
import com.drdisagree.iconify.features.home.toastframe.viewmodels.ToastFrameViewModel

private val toastFrameList = listOf(
    R.drawable.toast_frame_style_1,
    R.drawable.toast_frame_style_2,
    R.drawable.toast_frame_style_3,
    R.drawable.toast_frame_style_4,
    R.drawable.toast_frame_style_5,
    R.drawable.toast_frame_style_6,
    R.drawable.toast_frame_style_7,
    R.drawable.toast_frame_style_8,
    R.drawable.toast_frame_style_9,
    R.drawable.toast_frame_style_10,
    R.drawable.toast_frame_style_11,
    R.drawable.toast_frame_style_12,
).mapIndexed { index, style ->
    ToastFramePreview(
        id = (index + 1).toString(),
        title = UiText.Res.of(resId = R.string.style, (index + 1)),
        toastStyle = style
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToastFrameScreen(toastFrameViewModel: ToastFrameViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val toastFrames by rememberSaveable { mutableStateOf(toastFrameList) }
    val toastFrameStyle by toastFrameViewModel.toastFrameStyle.collectAsStateWithLifecycle()
    val isApplying by toastFrameViewModel.isLoading.collectAsStateWithLifecycle()

    LaunchedEffect(toastFrames) {
        toastFrameViewModel.refreshState()
    }

    if (isApplying) {
        LoadingDialog()
    }

    LaunchedEffect(Unit) {
        toastFrameViewModel.uiEvent.collect { event ->
            when (event) {
                ToastUiEvent.Applied -> Toast.makeText(
                    context,
                    R.string.toast_applied,
                    Toast.LENGTH_SHORT
                ).show()

                ToastUiEvent.Disabled -> Toast.makeText(
                    context,
                    R.string.toast_disabled,
                    Toast.LENGTH_SHORT
                ).show()

                ToastUiEvent.Error -> Toast.makeText(
                    context,
                    R.string.toast_error,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    AppScaffold(
        title = stringResource(R.string.activity_title_toast_frame),
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
                toastFrames.forEach { toast ->
                    val isApplied = toastFrameStyle == toast.id

                    ToastFrameCard(
                        toastFrame = toast.copy(isApplied = isApplied),
                        onClick = {
                            toastFrameViewModel.togglePack(toast.id)
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Phone", device = Devices.PHONE)
@Composable
fun IconShapeScreenPhonePreview() {
    PreviewComposable {
        ToastFrameScreen()
    }
}

@Preview(showBackground = true, name = "Tablet", device = Devices.TABLET)
@Composable
fun IconShapeScreenTabletPreview() {
    PreviewComposable {
        ToastFrameScreen()
    }
}