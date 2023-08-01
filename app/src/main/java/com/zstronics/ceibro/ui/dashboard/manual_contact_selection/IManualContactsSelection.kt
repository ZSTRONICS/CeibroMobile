package com.zstronics.ceibro.ui.dashboard.manual_contact_selection

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase

interface IManualContactsSelection {
    interface State : IBase.State {
        var searchName: MutableLiveData<String>
    }

    interface ViewModel : IBase.ViewModel<State>
}