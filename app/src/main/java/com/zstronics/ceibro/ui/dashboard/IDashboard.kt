package com.zstronics.ceibro.ui.dashboard

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase

interface IDashboard {
    interface State : IBase.State {
        val toMeSelected: MutableLiveData<Boolean>
        val inboxSelected: MutableLiveData<Boolean>
        val fromMeSelected: MutableLiveData<Boolean>
        val hiddenSelected: MutableLiveData<Boolean>
        val locationSelected: MutableLiveData<Boolean>
        val locationProjectSelected: MutableLiveData<Boolean>
        val locationDrawingSelected: MutableLiveData<Boolean>
        val locationViewSelected: MutableLiveData<Boolean>
        val projectsSelected: MutableLiveData<Boolean>
        val setAddTaskButtonVisibility: MutableLiveData<Boolean>
        val selectedItem: MutableLiveData<Int>
        var connectionCount: MutableLiveData<Int>
    }

    interface ViewModel : IBase.ViewModel<State> {
        fun handleSocketEvents()
    }
}