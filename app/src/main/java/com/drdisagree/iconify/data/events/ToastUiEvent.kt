package com.drdisagree.iconify.data.events

sealed class ToastUiEvent {
    object Applied : ToastUiEvent()
    object Disabled : ToastUiEvent()
    object Error : ToastUiEvent()
}