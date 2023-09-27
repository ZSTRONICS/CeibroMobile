package com.zstronics.ceibro.ui.dashboard.myconnectionsv2

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase

interface IMyConnectionV2 {
    interface State : IBase.State {
        var isAutoSyncEnabled: MutableLiveData<Boolean>
        var searchName: MutableLiveData<String>
        var deviceInfo: String
        var contactsPermission: String
        var localContactsSize: Int
        var dbContactsSize: Int
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}