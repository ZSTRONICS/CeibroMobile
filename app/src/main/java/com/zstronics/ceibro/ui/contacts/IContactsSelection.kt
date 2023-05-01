package com.zstronics.ceibro.ui.contacts

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase
import com.zstronics.ceibro.data.repos.dashboard.contacts.SyncContactsRequest

interface IContactsSelection {
    interface State : IBase.State {
        var searchName: MutableLiveData<String>
    }

    interface ViewModel : IBase.ViewModel<State> {
        fun syncContacts(
            selectedContacts: List<SyncContactsRequest.CeibroContactLight>,
            onSuccess: () -> Unit
        )
    }
}