package com.zstronics.ceibro.ui.chat.newchat

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase

interface INewChat {
    interface State : IBase.State {
        val name: MutableLiveData<String>
    }

    interface ViewModel : IBase.ViewModel<State> {
        fun loadProjects()
    }
}