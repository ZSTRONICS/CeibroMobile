package com.zstronics.ceibro.ui.contacts

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.state.BaseState
import javax.inject.Inject

class ContactsSelectionState @Inject constructor() : BaseState(), IContactsSelection.State {
    override var searchName: MutableLiveData<String> = MutableLiveData()
}