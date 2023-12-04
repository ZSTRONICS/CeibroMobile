package com.zstronics.ceibro.ui.locationv2

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.state.BaseState
import javax.inject.Inject

class LocationsV2State @Inject constructor() : BaseState(), ILocations.State {

   override var isFilterVisible: MutableLiveData<Boolean> = MutableLiveData(true)
}