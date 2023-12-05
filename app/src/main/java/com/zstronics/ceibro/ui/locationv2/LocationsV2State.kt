package com.zstronics.ceibro.ui.locationv2

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.state.BaseState
import javax.inject.Inject

class LocationsV2State @Inject constructor(
) : BaseState(), ILocations.State {

    override var isFilterVisible: MutableLiveData<Boolean> = MutableLiveData(true)

    override var isToNewClicked: MutableLiveData<Boolean> = MutableLiveData(false)
    override var isToOngoingClicked: MutableLiveData<Boolean> = MutableLiveData(false)
    override var isToDoneClicked: MutableLiveData<Boolean> = MutableLiveData(false)

    override var isFromUnreadClicked: MutableLiveData<Boolean> = MutableLiveData(false)
    override var isFromOngoingClicked: MutableLiveData<Boolean> = MutableLiveData(false)
    override var isFromDoneClicked: MutableLiveData<Boolean> = MutableLiveData(false)

    override var isHiddenOngoingClicked: MutableLiveData<Boolean> = MutableLiveData(false)
    override var isHiddenDoneClicked: MutableLiveData<Boolean> = MutableLiveData(false)
    override var isHiddenCancelled: MutableLiveData<Boolean> = MutableLiveData(false)
}