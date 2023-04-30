package com.zstronics.ceibro.ui.contacts

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase

interface IContactsSelection {
    interface State : IBase.State {
        var searchName: MutableLiveData<String>
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}