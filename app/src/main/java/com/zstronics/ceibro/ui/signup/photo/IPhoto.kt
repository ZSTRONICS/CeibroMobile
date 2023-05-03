package com.zstronics.ceibro.ui.signup.photo

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase

interface IPhoto {
    interface State : IBase.State {
        val isPhotoPicked: MutableLiveData<Boolean>
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}