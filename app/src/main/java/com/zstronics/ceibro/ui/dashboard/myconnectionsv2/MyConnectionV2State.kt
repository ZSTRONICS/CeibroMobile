package com.zstronics.ceibro.ui.dashboard.myconnectionsv2

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.state.BaseState
import javax.inject.Inject

class MyConnectionV2State @Inject constructor() : BaseState(), IMyConnectionV2.State {
    override val isAutoSyncEnabled: MutableLiveData<Boolean> = MutableLiveData(false)
    override var searchName: MutableLiveData<String> = MutableLiveData()
}