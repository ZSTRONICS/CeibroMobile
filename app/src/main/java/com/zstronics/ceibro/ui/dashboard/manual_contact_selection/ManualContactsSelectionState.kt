package com.zstronics.ceibro.ui.dashboard.manual_contact_selection

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.state.BaseState
import javax.inject.Inject

class ManualContactsSelectionState @Inject constructor() : BaseState(),
    IManualContactsSelection.State {
    override var searchName: MutableLiveData<String> = MutableLiveData()
}