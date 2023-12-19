package com.zstronics.ceibro.ui.locationv2.locationdrawing

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.state.BaseState
import javax.inject.Inject

class LocationDrawingV2State @Inject constructor(
) : BaseState(), ILocationDrawingV2.State {

    override var isFilterVisible: MutableLiveData<Boolean> = MutableLiveData(false)


}