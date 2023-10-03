package com.zstronics.ceibro.ui.dashboard.myconnectionsv2

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.state.BaseState
import javax.inject.Inject

class MyConnectionV2State @Inject constructor() : BaseState(), IMyConnectionV2.State {
    override var isAutoSyncEnabled: MutableLiveData<Boolean> = MutableLiveData(false)
    override var searchName: MutableLiveData<String> = MutableLiveData()
    override var deviceInfo: String = ""
    override var contactsPermission: String = ""
    override var localContactsSize: Int = -1
    override var dbContactsSize: Int = -1
    override var isCursorValid: Boolean = false
    override var isValidSession: Boolean = false
    override var newUpdatedContactListSize: Int = -1
}