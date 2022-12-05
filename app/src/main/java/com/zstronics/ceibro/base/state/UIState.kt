package com.zstronics.ceibro.base.state

sealed class UIState {
    data class Loading(val isLoading: Boolean, val message: String) : UIState()
    data class Message(val message: String) : UIState()
    data class Alert(val message: String) : UIState()
    data class Error(val message: String) : UIState()
}