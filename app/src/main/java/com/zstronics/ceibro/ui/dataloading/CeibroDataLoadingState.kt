package com.zstronics.ceibro.ui.dataloading

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.state.BaseState
import javax.inject.Inject

class CeibroDataLoadingState @Inject constructor() : BaseState(), ICeibroDataLoading.State {
    override val syncProgress: MutableLiveData<Int> = MutableLiveData(0)
}