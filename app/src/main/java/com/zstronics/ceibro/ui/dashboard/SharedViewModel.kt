package com.zstronics.ceibro.ui.dashboard

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    val isToMeUnread = MutableLiveData<Boolean>()
    val isFromMeUnread = MutableLiveData<Boolean>()
    val isHiddenUnread = MutableLiveData<Boolean>()
    val isConnectedToServer: MutableLiveData<Boolean> = MutableLiveData(false)
    val projectSearchQuery: MutableLiveData<String> = MutableLiveData("")
    val drawingSearchQuery: MutableLiveData<String> = MutableLiveData("")
}