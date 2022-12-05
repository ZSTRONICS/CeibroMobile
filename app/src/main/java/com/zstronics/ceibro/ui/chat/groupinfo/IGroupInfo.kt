package com.zstronics.ceibro.ui.chat.groupinfo

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase
import com.zstronics.ceibro.data.repos.chat.room.Project

interface IGroupInfo {
    interface State : IBase.State {
        val chatTitle: MutableLiveData<String>
        val project: MutableLiveData<Project>
        val isGroupChat: MutableLiveData<Boolean>
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}